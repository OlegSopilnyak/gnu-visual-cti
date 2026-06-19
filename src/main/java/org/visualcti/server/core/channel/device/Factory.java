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
package org.visualcti.server.core.channel.device;

import java.util.Optional;
import java.util.stream.Stream;
import org.visualcti.server.core.unit.RunnableServerUnit;

/**
 * The Factory of the Devices: The factory of the channel-devices
 *
 * @see Device
 * @see RunnableServerUnit
 */
interface Factory extends RunnableServerUnit {
    // The name of root XML's Element
    String ELEMENT = "factory";

    /**
     * <accessor>
     * get access to factory's vendor name
     *
     * @return vendor's name
     */
    String getVendor();

    /**
     * <accessor>
     * get access to factory's version
     *
     * @return the version
     */
    String getVersion();

    /**
     * <accessor>
     * To get the Name of the unit to show in UI
     *
     * @return the value
     * @see #getVendor()
     */
    @Override
    default String getName() {
        return "provider/" + getVendor();
    }

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    default String getType() {
        return "channel-device-board";
    }

    /**
     * <producer>
     * To make the stream of devices.
     *
     * @return the stream of devices
     */
    default Stream<Device> devices() {
        return children().filter(Device.class::isInstance).map(Device.class::cast);
    }

    /**
     * <aceessor>
     * to get the device instance by the name
     *
     * @param name the name of device in the factory
     * @return the device or empty, if device with name is not in the factory
     */
    default Optional<Device> getDevice(String name) {
        return devices().filter(d -> d.getName().equals(name)).findFirst();
    }
}
