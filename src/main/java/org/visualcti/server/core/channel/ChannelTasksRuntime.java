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

import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;

/**
 * Tasks Runners Group: container of the tasks runners for particular channel-device
 *
 * @see ChannelTaskRunner
 */
public interface ChannelTasksRuntime extends RunnableServerUnit {
    String ROOT_ELEMENT_NAME = "ChannelTasksRuntime";
    String SERVER_UNIT_DESCRIPTION = "The runtime of the server to manage channel-devices";
    // the value of the server unit name
    String UNIT_NAME = "Server Runtime";
    // the value of type of the server unit
    String UNIT_TYPE = "[server-runtime-environment]";
    // the value of the server unit path in the registry
    String UNIT_PATH = "Runtime";
    // the timer for tasks
    Timer timer = new Timer(true);

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
     * To get Path to unit instance in repository
     *
     * @return the value
     */
    @Override
    default String getPath() {
        return UNIT_PATH;
    }

    /**
     * <accessor>
     * To get access to the timer
     *
     * @return one instance of the timer
     * @see Timer
     * @see java.util.TimerTask
     */
    default Timer getTimer() {
        return timer;
    }


    /**
     * <mutator>
     * To add task runners for the channel-devices factory
     *
     * @param factory the instance of devices factory
     * @return true if it's succeeded
     * @see Factory#channels()
     * @see #addRunnerFor(Channel)
     */
    default boolean prepareRunnersFor(Factory factory) {
        return Arrays.stream(factory.channels())
                .map(this::addRunnerFor)
                .reduce(true, (a, b) -> a && b);
    }

    /**
     * <mutator>
     * To add task runner for the channel
     *
     * @param channel the instance to add
     * @return true if it's succeeded
     * @see Channel
     */
    boolean addRunnerFor(Channel channel);

    /**
     * <accessor>
     * To get access to group's children as task runners
     *
     * @return group's runners stream
     * @see RunnableServerUnit#runnableChildren()
     * @see ChannelTaskRunner
     */
    default Stream<ChannelTaskRunner> runners() {
        return runnableChildren().filter(ChannelTaskRunner.class::isInstance).map(ChannelTaskRunner.class::cast);
    }

    /**
     * <accessor>
     * To get access to the executor
     *
     * @return the value
     * @see Executor
     * @see Executor#execute(Runnable)
     */
    Executor getExecutor();

    /**
     * <mutator>
     * To add child to the server unit composite units tree
     *
     * @param unit the unit instance to add
     * @return true if it's succeeded
     * @see ChannelTaskRunner
     * @see RunnableServerUnit#add(ServerUnit)
     */
    @Override
    default boolean add(ServerUnit unit) {
        if (unit instanceof ChannelTaskRunner) {
            return RunnableServerUnit.super.add(unit);
        } else {
            return false;
        }
    }
}
