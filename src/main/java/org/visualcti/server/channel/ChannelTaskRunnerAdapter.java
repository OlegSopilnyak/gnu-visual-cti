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
package org.visualcti.server.channel;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.server.core.channel.Channel;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.server.core.channel.device.Device;
import org.visualcti.server.core.channel.device.DeviceEvent;
import org.visualcti.server.core.channel.device.DeviceMalfunction;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.task.Environment;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * Adapter: entity to run task from tasks-pool for particular channel-device
 */
abstract class ChannelTaskRunnerAdapter extends RunnableUnitAdapter implements ChannelTaskRunner {
    private final transient Environment environment;
    private final transient Channel channel;
    private final transient TasksPoolUnit tasksPool;
    private final transient Map<String, Integer> executingTasks;
    private final transient Lock exclusiveAccessLock;
    private transient ScheduledExecutorService nextRunnerStepExecutor;

    protected ChannelTaskRunnerAdapter(Environment environment, Channel channel, TasksPoolUnit tasksPool) {
        this.environment = environment;
        this.channel = channel;
        this.tasksPool = tasksPool;
        // the counter of tasks in executing state at the moment
        executingTasks = new ConcurrentHashMap<>();
        this.exclusiveAccessLock = new ReentrantLock(true);
        this.unitPath = defaultUnitPath();
    }

    /**
     * To get access to next step executor
     *
     * @return the value
     */
    Executor getNextRunnerStepExecutor() {
        return nextRunnerStepExecutor;
    }

    /**
     * <builder>
     * To build default unit's path
     *
     * @return the value
     * @see org.visualcti.server.UnitRegistry#register(ServerUnit)
     */
    protected String defaultUnitPath() {
        return "Runner/" + channel.getDeviceVendor() + "/" + channel.getName();
    }

    /**
     * <action>
     * Closing the server unit, releasing attached resources and restoring original unitPath
     *
     * @throws IOException if an I/O error occurs
     * @see #unitPath
     */
    @Override
    public void close() throws IOException {
        this.unitPath = defaultUnitPath();
        if (this.nextRunnerStepExecutor != null) {
            this.nextRunnerStepExecutor.shutdownNow();
            this.nextRunnerStepExecutor = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChannelTaskRunner)) return false;
        if (!super.equals(o)) return false;
        ChannelTaskRunner that = (ChannelTaskRunner) o;
        return Objects.equals(getChannel().getName(), that.getChannel().getName())
                && Objects.equals(getTasksPool().getName(), that.getTasksPool().getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getChannel().getName(), getTasksPool().getName());
    }

    /**
     * <accessor>
     * To get access to environment for task execution
     *
     * @return the value
     * @see Environment
     */
    @Override
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * <accessor>
     * To get access to the tasks pool associated with the channel
     *
     * @return the value
     * @see TasksPoolUnit
     */
    @Override
    public TasksPoolUnit getTasksPool() {
        return tasksPool;
    }


    /**
     * <accessor>
     * To get access to channel for task execution
     *
     * @return the value
     * @see Channel
     */
    @Override
    public Channel getChannel() {
        return channel;
    }

    /**
     * <lock>
     * To get access to channel's exclusive access lock for task execution
     *
     * @return the value
     */
    @Override
    public Lock getExclusiveAccessLock() {
        return exclusiveAccessLock;
    }

    /**
     * <accessor>
     * To get the name of the root element name in XML result
     *
     * @return the name of root element
     * @see XmlAware#getRootElementName()
     */
    @Override
    public String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @return the instance of class which extends server unit main class
     * @see ServerUnit#getUnitExtendsClass()
     */
    @Override
    public Class<? extends ServerUnit> getUnitExtendsClass() {
        return ChannelTaskRunner.class;
    }

    /**
     * <accessor>
     * To get description of the unit
     *
     * @see ServerUnitAdapter#getUnitDescription()
     */
    @Override
    protected String getUnitDescription() {
        return SERVER_UNIT_DESCRIPTION;
    }

    /**
     * <accessor>
     * To get main class of the unit
     *
     * @return class-implementation of base server unit type
     * @see ServerUnit#getUnitClass()
     */
    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return ChannelTaskRunnerAdapter.class;
    }

    /**
     * <converter>
     * To prepare base parameters of the unit using XML Element and correct xml if it's needed
     * Here we don't use builder feature of the server unit so doing nothing from XML-element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #setXML(Element)
     * @see #getUnitBuilderClass()
     * @see #getUnitBuilderMethodName()
     */
    @Override
    protected void settingUpBasePart(Element xml) {
        // doing nothing, keeps original XML
    }

    /**
     * <converter>
     * To prepare main parameters of the unit using XML Element
     * To build and configure unit we're not using external XML-element so doing nothing
     *
     * @param xml the XML Element of the unit
     * @throws IOException if something went wrong
     * @see Element
     */
    @Override
    protected void settingUpMainPart(Element xml) throws IOException {
        // doing nothing, keeps original XML
    }

    /**
     * <action>
     * Whether the given event is accepted by this listener.
     *
     * @param event the fired Event
     * @return true if the event accepted for the processing
     * @see ChannelTaskRunner#accept(DeviceEvent)
     * @see DeviceEvent
     * @see Channel
     */
    @Override
    public boolean accept(final DeviceEvent event) {
        if (!ChannelTaskRunner.super.accept(event)) {
            dispatchError("Rejected invalid event: " + event);
            return false;
        }
        // runner is started well and received correct event
        // doing according the device event type
        switch (event.getEventType()) {
            case INCOMING:
                if (channel.isBusy()) {
                    // the channel is busy to accept incoming event
                    return false;
                }
                // launching the task in separate thread
                getGroup().getExecutor().execute(this::launchChannelTask);
                break;
            case MALFUNCTION:
                // detected channel device malfunction event, notify about it
                dispatchError(event.getDescription());
                // terminating current task because of device's malfunction
                tasksPool.current().stopExecute();
                // trying to repair the broken device
                final Device device = channel.getDevice();
                try {
                    // terminate current device activity
                    device.terminate();
                    // repairing terminated device
                    if (!device.repair()) {
                        // the device repairing is failed
                        // stopping runner and mark it as broken
                        stopBrokenDeviceRunner();
                    }
                } catch (IOException e) {
                    dispatchError(e, "Cannot repair broken device.");
                }
                break;
            default:
                dispatchError("Unknown event type: " + event.getEventType());
                return false;
        }
        // event is accepted by task runner
        return true;
    }

    /**
     * <action>
     * To attach task to the runner before execution one
     *
     * @param task the task to execute
     */
    @Override
    public void attachTask(Task task) {
        ChannelTaskRunner.super.attachTask(task);
        // incrementing particular task counter
        executingTasks.compute(task.getName(), (name, counter) -> counter == null ? 1 : counter + 1);
    }

    /**
     * <action>
     * To detach task from the runner after execution one
     *
     * @param task the executed task
     */
    @Override
    public void detachTask(Task task) {
        ChannelTaskRunner.super.detachTask(task);
        // decrementing particular task counter
        executingTasks.compute(task.getName(), (name, counter) -> counter == null ? 0 : counter - 1);
    }

    /**
     * <accessor>
     * To get the quantity of tasks executing in the channel runner now
     *
     * @return how many tasks are executing now
     * @see #attachTask(Task)
     * @see #detachTask(Task)
     */
    public int executingTaskCount() {
        return executingTasks.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * <error-hanler>
     * To handle channel-device malfunction during the task execution
     *
     * @param malfunction the value
     * @param runningTask running task
     * @throws IOException if something went wrong
     * @see DeviceMalfunction#repairMalfunction()
     * @see Task#stopExecute()
     * @see #Stop()
     */
    @Override
    public void deviceMalfunctionInTask(DeviceMalfunction malfunction, Task runningTask) throws IOException {
        dispatchError(malfunction, "Error in the channel-device of task: " + runningTask.getName());
        // trying to repair broken channel-device
        if (!malfunction.repairMalfunction()) {
            // stopping current task execution
            runningTask.stopExecute();
            // stopping runner and mark it as broken
            stopBrokenDeviceRunner();
        }
    }

    /**
     * <action>
     * To start the channel's tasks runner
     *
     * @throws IOException if the unit can't be started
     * @see org.visualcti.server.core.unit.RunnableServerUnit#Start()
     */
    @Override
    public void Start() throws IOException {
        super.Start();
        // prepare next step iteration executor
        this.nextRunnerStepExecutor = Executors.newSingleThreadScheduledExecutor();
        // run the first step
        nextRunnerStep();
    }

    /**
     * <action>
     * To stop the channel's tasks runner
     *
     * @throws IOException if the unit can't be stopped
     */
    @Override
    public void Stop() throws IOException {
        super.Stop();
        // stopping the next step iteration executor
        if (this.nextRunnerStepExecutor != null) {
            this.nextRunnerStepExecutor.shutdownNow();
            this.nextRunnerStepExecutor = null;
        }
    }

    /**
     * <action>
     * To run the task from tasks pool
     *
     * @throws IOException if it cannot run task properly
     * @see ChannelTaskRunner#runChannelTask()
     */
    @Override
    public void runChannelTask() throws IOException {
        ChannelTaskRunner.super.runChannelTask();
        // running next runner's iteration
        nextRunnerStep();
    }

    /// / inner classes
    // class event to push runner's next iteration without hardware's device event
    private static class NextIterationEvent implements DeviceEvent {
        private final Device device;

        private NextIterationEvent(Device device) {
            this.device = device;
        }

        @Override
        public Type getEventType() {
            return Type.INCOMING;
        }

        @Override
        public String getDeviceName() {
            return device.getName();
        }

        @Override
        public String getVendor() {
            return device.getFactory().getVendor();
        }

        @Override
        public String getDescription() {
            return "Start Next Iteration Task";
        }

        @Override
        public Map<String, Object> getOptions() {
            return Collections.emptyMap();
        }
    }

    /// / private methods
    // to launch runner's next iteration, works like the steps loop
    private void nextRunnerStep() {
        if (!isStarted() || executingTaskCount() != 0) {
            // channel task runner isn't started or there is working task there
            return;
        }
        // pushing next iteration event to the runner in 0.5 second
        dispatchEvent("Pushing the next iteration event");
        nextRunnerStepExecutor.schedule(() ->
                this.accept(new NextIterationEvent(channel.getDevice())), 500, TimeUnit.MILLISECONDS
        );
    }

    // stopping runner and mark it as a broken unit
    private void stopBrokenDeviceRunner() throws IOException {
        // the runner's device is broken so stopping the runner
        Stop();
        // mark runner as broken server unit
        unitState = UnitState.BROKEN;
    }

    // running channel task
    private void launchChannelTask() {
        try {
            dispatchEvent("Starting channel task on the runner:" + getName());
            // running the channel task
            runChannelTask();
        } catch (IOException e) {
            dispatchError(e, "Cannot start channel task on the runner:" + getName());
        }
    }
}
