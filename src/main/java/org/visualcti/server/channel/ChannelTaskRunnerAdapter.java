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
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.Element;
import org.visualcti.server.core.channel.Channel;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.task.Environment;
import org.visualcti.server.unit.RunnableUnitAdapter;

/**
 * Adapter: entity to run task from tasks-pool for particular channel-device
 */
public class ChannelTaskRunnerAdapter extends RunnableUnitAdapter implements ChannelTaskRunner {
    private final transient Environment environment;
    private final transient Channel channel;
    private final transient TasksPoolUnit tasksPool;
    private final transient Lock exclusiveAccessLock;

    protected ChannelTaskRunnerAdapter(Environment environment, Channel channel, TasksPoolUnit tasksPool) {
        this.environment = environment;
        this.channel = channel;
        this.tasksPool = tasksPool;
        exclusiveAccessLock = new ReentrantLock(true);
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
}
