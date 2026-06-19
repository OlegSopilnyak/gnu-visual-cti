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
import java.util.function.UnaryOperator;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.channel.device.Device;
import org.visualcti.server.core.channel.device.DeviceMalfunction;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.task.Environment;

/**
 * Tasks Runner: entity to run task from tasks-pool for particular channel-device
 */
public interface TaskRunner extends RunnableServerUnit {
    // the name of channel-device in the task's environment
    String ENVIRONMENT_PART_DEVICE_NAME = "channel <:current device:>";
    // the name of runner's environment part, standard task's output
    String ENVIRONMENT_PART_TASK_STANDARD_OUTPUT = "stdout";
    // the name of runner's environment part, error task's output
    String ENVIRONMENT_PART_TASK_ERROR_OUTPUT = "stderr";

    /**
     * <accessor>
     * To get access to environment for task execution
     *
     * @return the value
     * @see Environment
     * @see org.visualcti.server.core.executable.task.Task#setEnv(Environment)
     */
    Environment getEnvironment();

    /**
     * <accessor>
     * To get access to the tasks pool associated with the channel
     *
     * @return the value
     * @see Environment
     * @see org.visualcti.server.core.executable.task.Task#setEnv(Environment)
     */
    TasksPoolUnit getTasksPool();

    /**
     * <accessor>
     * To get access to channel for task execution
     *
     * @return the value
     * @see Channel
     */
    Channel getChannel();

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    default String getName() {
        return getChannel().getName();
    }

    /**
     * <accessor>
     * To check is unit needs to be registered in units registry
     *
     * @return true if unit needed registration
     * @see UnitRegistry#register(ServerUnit)
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
        // starting tasks pool
        getTasksPool().Start();
        // preparing runner's environment
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
        // stopping tasks pool
        getTasksPool().Stop();
        // clearing runner's environment
        getEnvironment().clear();
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
        // to get the channel-device instance for the task's run
        try (final Device channelDevice = getChannel().getDevice()) {
            channelDevice.open();
            // attaching the task to the tasks runner
            attachTask(this, taskToRun);
            // starting task's execution
            taskToRun.execute();
        } catch (DeviceMalfunction malfunction) {
            // detected channel device malfunction, notify about it
            dispatchError(malfunction, "Error in the channel-device.");
            // trying to repair broken channel-device
            if (!malfunction.repairMalfunction()) {
                // the device is broken so stopping the task-runner
                Stop();
            }
        } finally {
            // detaching the task from the tasks runner
            detachTask(this, taskToRun);
        }
    }

    /// // private methods
    UnaryOperator<String> runnerTaskState = currentTaskName -> String.format("current%n%s%n", currentTaskName);

    // to attach task before execution
    static void attachTask(TaskRunner runner, Task task) throws IOException {
        // to get runner task's environment
        final Environment environment = runner.getEnvironment();
        // to prepare the runtime environment for the task
//        environment.setPart( "timer",     group.getTimer()    );
//        environment.setPart( "database",  group.getDatabase() );
//        environment.setPart( "messenger", group.getMessenger());
        // attaching configured environment to the task to execute
        task.setEnv(environment);
        // sending notification about task's attachment to the runner
        runner.dispatchEvent(runnerTaskState.apply(task.getName()));
    }

    // to detach task after execution
    static void detachTask(TaskRunner runner, Task task) {
        // cleaning the environment from the executed task
        task.setEnv(null);
        // sending notification about task's detachment from the runner
        runner.dispatchEvent(runnerTaskState.apply(" "));
    }

    static void prepareEnvironment(TaskRunner runner) {
        // to get runner task's environment
        final Environment environment = runner.getEnvironment();
        environment.clear();
        // prepare channel-device part of the environment
        final Device channelDevice = runner.getChannel().getDevice();
        final String currentDeviceName = "/channel/device/" + channelDevice.getDeviceName();
        environment.setPart(ENVIRONMENT_PART_DEVICE_NAME, currentDeviceName);
        environment.setPart(currentDeviceName, channelDevice);
        // prepare task's output streams
        final TaskRunnerStream stdout = new TaskRunnerStream() {
            @Override
            public void notifyOwner(String printed) {
                runner.dispatchEvent(printed);
            }
        };
        final TaskRunnerStream stderr = new TaskRunnerStream() {
            @Override
            public void notifyOwner(String printed) {
                runner.dispatchError(printed);
            }
        };
        // put them to the environment as parts of it
        environment.setPart(ENVIRONMENT_PART_TASK_STANDARD_OUTPUT, stdout);
        environment.setPart(ENVIRONMENT_PART_TASK_ERROR_OUTPUT, stderr);
    }
}
