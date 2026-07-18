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
package org.visualcti.core.channel.telephony;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.Factory;

/**
 * The Factory of the Telephony Devices: The factory of the telephony channel-devices
 *
 * @param <H> the type of device's handle (for low-level operations)
 * @param <D> the type of factory's devices
 * @see TelephonyDevice
 * @see Factory
 */
@SuppressWarnings("uncecked")
public interface TelephonyDeviceFactory<H, D extends TelephonyDevice<?, ?>> extends Factory<H, D> {
    // the value of type the server unit
    String UNIT_TYPE = "[telephony-channel-devices-board]";

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
     * <aceessor>
     * to get the device instance by the name
     *
     * @param name the name of device in the factory
     * @return the device or empty, if device with name is not in the factory
     * @see #devices()
     * @see Device
     * @see Optional
     */
    @Override
    default Optional<D> getDevice(String name) {
        return Factory.super.getDevice(name);
    }

    /**
     * <producer>
     * To make the stream of devices.
     *
     * @return the stream of devices
     * @see TelephonyDevice
     * @see Stream
     * @see Factory#devices()
     */
    @Override
    default Stream<D> devices() {
        return Factory.super.devices().filter(TelephonyDevice.class::isInstance);
    }

    /**
     * <aceessor>
     * to get the array of available factory's channels
     *
     * @return the array of available channels
     * @see Channel
     */
    @Override
    Collection<Channel<?>> channels();
}
