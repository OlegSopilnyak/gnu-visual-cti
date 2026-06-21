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

import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.channel.device.Device;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.task.Environment;

/**
 * The Channel: The channel through device of which task is communicating with external world
 *
 * @see Task#setEnv(Environment)
 * @see Environment#setPart(String, Object)
 * @see Environment#getPart(String, Class)
 * @see ServerUnit
 */
public interface Channel extends ServerUnit {
    // the value of type the server unit
    String UNIT_TYPE = "[channel]";

    /**
     * <accessor>
     * To get the device of the channel
     *
     * @return channel-device instance associated with the channel
     */
    Device getDevice();

    /**
     * <accessor>
     * To get access to the status of the channel
     *
     * @return status
     * @see Device#getStatus()
     */
    default String getStatus() {
        return getDevice().getStatus();
    }

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     * @see Device#isOpened()
     */
    default boolean isOpened() {
        final Device channelDevice = getDevice();
        return channelDevice != null && channelDevice.isOpened();
    }

    /**
     * <action>
     * Before start task execution on the channel
     *
     * @param task task which is going to be executed
     * @see #isBusy()
     */
    default void beforeStart(Task task) {
        throw new UnsupportedOperationException("Please implement me!");
    }

    /**
     * <action>
     * After stop task execution on the channel
     *
     * @param task task which was executed
     * @see #isBusy()
     */
    default void afterStop(Task task) {
        throw new UnsupportedOperationException("Please implement me!");
    }

    /**
     * <accessor>
     * To check is channel busy to accept incoming device event
     *
     * @return true if channel is busy
     */
    default boolean isBusy() {
        // by default channel support multiple usage
        return false;
    }

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    default String getType() {
        return UNIT_TYPE;
    }

    /**
     * <accessor>
     * To get the Name of the unit to show in UI
     *
     * @return the value
     * @see Device#getName()
     */
    @Override
    default String getName() {
        return getDevice().getName();
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
}
