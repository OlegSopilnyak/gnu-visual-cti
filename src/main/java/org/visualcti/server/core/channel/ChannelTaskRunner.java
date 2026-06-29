/*
##############################################################################
##
##  DO NOT REMOVE THIS LICENSE AND COPYRIGHT NOTICE FOR ANY REASON
##
##############################################################################

GNU VisualCTI - A Java multi-platform Computer Telephony Application Server
Copyright (C) 2002 by Oleg Sopilnyak.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Contact oleg.sopilnyak@gmail.com or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server.core.channel;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.UnaryOperator;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.task.Environment;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * Tasks Runner: entity to run task from tasks-pool for particular channel-device
 */
public interface ChannelTaskRunner<D extends Device<?>> extends RunnableServerUnit, DeviceEvent.Listener {
    String ROOT_ELEMENT_NAME = "ChannelTaskRunner";
    String SERVER_UNIT_DESCRIPTION = "The runner of task for particular channel-device";
    // the value of type the server unit
    String UNIT_TYPE = "[channel-task-runner]";
    // the name of channel-device in the task's environment
    String ENVIRONMENT_PART_DEVICE_NAME = "channel <:current device:>";
    // the name of runner's environment part, standard task's output
    String ENVIRONMENT_PART_TASK_STANDARD_OUTPUT = "stdout";
    // the name of runner's environment part, error task's output
    String ENVIRONMENT_PART_TASK_ERROR_OUTPUT = "stderr";
    String ENVIRONMENT_PART_TASK_TIMER = "timer";
    // preparing task state function
    UnaryOperator<String> runnerTaskState = currentTaskName -> String.format("current%n%s%n", currentTaskName);

    /**
     * <accessor>
     * To get access to the group of tasks runners
     *
     * @return the value
     * @see ChannelTasksRuntime
     */
    default ChannelTasksRuntime getGroup() {
        return getOwner() instanceof ChannelTasksRuntime ? (ChannelTasksRuntime) getOwner() : null;
    }

    /**
     * <accessor>
     * To get access to environment for task execution
     *
     * @return the value
     * @see Environment
     * @see Task#setEnv(Environment)
     */
    Environment getEnvironment();

    /**
     * <accessor>
     * To get access to the tasks pool associated with the channel
     *
     * @return the value
     * @see TasksPoolUnit
     */
    TasksPoolUnit getTasksPool();

    /**
     * <accessor>
     * To get access to channel for task execution
     *
     * @return the value
     * @see Channel
     */
    Channel<D> getChannel();

    /**
     * <lock>
     * To get access to channel's exclusive access lock for task execution
     *
     * @return the value
     */
    Lock getExclusiveAccessLock();

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     * @see Channel#getName()
     */
    @Override
    default String getName() {
        return getChannel().getName();
    }

    /**
     * <accessor>
     * To get Type of unit
     *
     * @return the value
     */
    default String getType() {
        return UNIT_TYPE;
    }

    /**
     * <accessor>
     * To check is unit needs to be registered in units registry
     *
     * @return true if unit needed registration
     * @see UnitRegistry#register(ServerUnit)
     * @see ServerUnitAdapter#setOwner(ServerUnit)
     */
    @Override
    default boolean isNeedRegistration() {
        return false;
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     *
     * @throws IOException if something went wrong during start internal parts
     * @see #Start()
     */
    @Override
    default void startUnitRunnable() throws IOException {
        // getting channel device instance
        final D device = getChannel().getDevice();
        // opening channel device if it didn't open yet
        if (!device.isOpened()) {
            // opening the device just for check availability
            device.open();
        }
        // closing the device after opening check
        device.close();
        // starting tasks pool
        getTasksPool().Start();
        // preparing environment for executing task
        prepareEnvironment(this);
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     *
     * @throws IOException if something went wrong during the internal parts stopping
     * @see #Stop()
     */
    @Override
    default void stopUnitRunnable() throws IOException {
        // getting channel device instance
        final Device<?> device = getChannel().getDevice();
        // closing channel device if it did open yet
        if (device.isOpened()) {
            // closing the device
            device.close();
        }
        // stopping tasks pool
        getTasksPool().Stop();
        // cleaning runner's environment
        getEnvironment().clear();
    }

    /**
     * <action>
     * Whether the given event is accepted by this listener.
     *
     * @param event the fired Event
     * @return true if the event accepted for the processing
     */
    @Override
    default boolean accept(DeviceEvent event) {
        final Channel<?> channel = getChannel();
        final Device<?> device = channel.getDevice();
        if (isStarted() && eventCompliesDevice(event, device)) {
            // runner state and event are good, checking event type
            return event.getEventType() != null;
        } else {
            // event not for the channel-device or runner isn't started
            return false;
        }
        // event is accepted by task runner
    }

    /**
     * <action>
     * To run the task from tasks pool
     *
     * @throws IOException if it cannot run task properly
     */
    default void runChannelTask() throws IOException {
        if (!isStarted()) {
            // the runner isn't started yet
            return;
        }
        // to get the next form tasks pool, associated with the channel
        final Task taskToRun = getTasksPool().next();
        if (taskToRun == null) {
            // no tasks to run in the tasks pool, stopping runner and leaving the method
            Stop();
            return;
        }
        // locking the access to the runner instance
        Lock lock = getExclusiveAccessLock();
        lock.lock();
        // to get the channel-device instance for the task's execution
        try (final Device<?> channelDevice = getChannel().getDevice()) {
            channelDevice.open();
            // attaching the task to the tasks runner
            attachTask(taskToRun);
            // unlocking the access to the runner instance
            lock.unlock();
            lock = null;
            // starting task's execution
            taskToRun.execute();
        } catch (DeviceMalfunction malfunction) {
            // detected channel device malfunction, notify about it
            deviceMalfunctionInTask(malfunction, taskToRun);
        } finally {
            // unlocking the access to execution sequence
            if (lock != null) {
                lock.unlock();
            }
            // detaching the task from the tasks runner
            detachTask(taskToRun);
        }
    }

    /**
     * <error-hanler>
     * To handle channel-device malfunction during the task execution
     *
     * @param malfunction the value
     * @param runningTask running task
     * @throws IOException if something went wrong
     * @see DeviceMalfunction
     * @see Task
     */
    void deviceMalfunctionInTask(DeviceMalfunction malfunction, Task runningTask) throws IOException;

    /**
     * <action>
     * To attach task to the runner before execution one
     *
     * @param task the task to execute
     */
    default void attachTask(Task task) {
        // to get runner task's environment
        final Environment environment = getEnvironment();
        // to prepare the runtime environment for the task
        environment.setPart(ENVIRONMENT_PART_TASK_TIMER, getGroup().getTimer());
//        environment.setPart( "database",  group.getDatabase() );
//        environment.setPart( "messenger", group.getMessenger());
        // attaching configured environment to the task to execute
        task.setEnv(environment);
        // sending notification about task's attachment to the runner
        dispatchEvent(runnerTaskState.apply(task.getName()));
        // adjust the channel's task state
        getChannel().beforeStart(task);
    }

    /**
     * <action>
     * To detach task from the runner after execution one
     *
     * @param task the executed task
     */
    default void detachTask(Task task) {
        // cleaning the environment from the executed task
        task.setEnv(null);
        // sending notification about task's detachment from the runner
        dispatchEvent(runnerTaskState.apply(" "));
        // adjust the channel's task state
        getChannel().afterStop(task);
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @see #getUnitClass()
     */
    @Override
    default Class<? extends ServerUnit> getUnitExtendsClass() {
        return ChannelTaskRunner.class;
    }

    /// // private methods
    // to check is event complies with the device
    static boolean eventCompliesDevice(DeviceEvent event, Device<?> device) {
        return Objects.equals(event.getDeviceName(), device.getName())
                && Objects.equals(event.getVendor(), device.getFactory().getVendor());
    }

    // preparing the environment during the runner start
    static void prepareEnvironment(ChannelTaskRunner<?> runner) {
        // to get runner task's environment
        final Environment environment = runner.getEnvironment();
        environment.clear();
        // prepare channel-device part of the environment
        final Device<?> channelDevice = runner.getChannel().getDevice();
        final String currentDeviceName = "/channel/device/" + channelDevice.getDeviceName();
        environment.setPart(ENVIRONMENT_PART_DEVICE_NAME, currentDeviceName);
        environment.setPart(currentDeviceName, channelDevice);
        // prepare task's output streams
        final TaskRunnerStream stdout = new TaskRunnerStream() {
            @Override
            public void notifyOwner(String stringToPrint) {
                runner.dispatchEvent(stringToPrint);
            }
        };
        final TaskRunnerStream stderr = new TaskRunnerStream() {
            @Override
            public void notifyOwner(String stringToPrint) {
                runner.dispatchError(stringToPrint);
            }
        };
        // put them to the environment as parts of it
        environment.setPart(ENVIRONMENT_PART_TASK_STANDARD_OUTPUT, stdout);
        environment.setPart(ENVIRONMENT_PART_TASK_ERROR_OUTPUT, stderr);
    }
}
