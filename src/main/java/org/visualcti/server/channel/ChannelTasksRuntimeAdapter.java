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
import java.util.concurrent.Executor;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.core.channel.Channel;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.server.core.channel.ChannelTasksRuntime;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.task.Environment;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * Adapter: container of the tasks runners for particular channel-device
 *
 * @see ChannelTasksRuntime
 */
public abstract class ChannelTasksRuntimeAdapter extends RunnableUnitAdapter implements ChannelTasksRuntime {
    // the threads pool for runner's task execution
    private final transient Executor taskExecutor;

    protected ChannelTasksRuntimeAdapter(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.unitPath = UNIT_PATH;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ChannelTasksRuntimeAdapter)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
        this.unitPath = UNIT_PATH;
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        return UNIT_NAME;
    }

    /**
     * <accessor>
     * To get access to the executor
     *
     * @return the value
     * @see Executor
     * @see Executor#execute(Runnable)
     */
    @Override
    public Executor getExecutor() {
        return taskExecutor;
    }

    /**
     * <mutator>
     * to add child to the server unit composite units tree<BR/>
     * set up the owner for the child unit current unit
     *
     * @param unit the unit to add
     * @return true if it's succeeded
     * @see ServerUnitAdapter#add(ServerUnit)
     */
    @Override
    public boolean add(ServerUnit unit) {
        if (unit instanceof ChannelTaskRunner) {
            return super.add(unit);
        }
        return false;
    }

    /**
     * <mutator>
     * To add task runner for the channel
     *
     * @param channel the instance to add
     * @return true if it's succeeded
     */
    @Override
    public boolean addRunnerFor(Channel channel) {
        try {
            // preparing task pool for the channel
            final TasksPoolUnit tasksPool = UnitRegistry.lookup(TaskPoolsManager.class)
                    .getTaskPool(channel.getName(), channel.getDeviceVendor());
            // creating task runner for the channel
            final ChannelTaskRunner taskRunner = new ChannelTaskRuntime(new Environment(), channel, tasksPool);
            // add created task runner as device-events listener to the channel's device-factory
            channel.addDeviceEventListenerFor(taskRunner);
            // add task runner to the server's runtime
            return add(taskRunner);
        } catch (ServerUnitException e) {
            dispatchError(e, "Cannot found task pools manager in the units registry.");
            return false;
        }
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
        return ChannelTasksRuntime.class;
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
        return ChannelTasksRuntimeAdapter.class;
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

    //// inner classes
    private static class ChannelTaskRuntime extends ChannelTaskRunnerAdapter {
        protected ChannelTaskRuntime(Environment environment, Channel channel, TasksPoolUnit tasksPool) {
            super(environment, channel, tasksPool);
        }
    }
}
