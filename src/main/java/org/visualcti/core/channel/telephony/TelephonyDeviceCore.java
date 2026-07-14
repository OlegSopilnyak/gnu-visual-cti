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


import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;

/**
 * Device of the Telephony Channel: The core part of the telephony device type
 *
 * @see TelephonyServiceProvider
 * @param <H> the type of the device's low-level operations handle
 */
public interface TelephonyDeviceCore<H> {
    /**
     * <accessor>
     * To get access to device's low-level handle
     *
     * @return the handle to manipulate the device features
     */
//    H getHandle();

    /**
     * <accessor>
     * To get access to the current device's telephony events provider
     *
     * @return the reference to the events provider singleton
     * @see TelephonyServiceProvider
     */
    TelephonyServiceProvider<H> getProvider();

    /**
     * <accessor>
     * To get access to the current device's state
     *
     * @return the value
     * @see DeviceStateValue
     * @see Device.State
     * @see TelephonyDevice.State
     */
//    DeviceStateValue getState();
}
