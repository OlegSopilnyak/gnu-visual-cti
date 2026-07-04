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

import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;

/**
 * The Telephony Channel: The channel through device of which task is communicating with computer telephony equipment
 *
 * @see Channel
 * @see TelephonyDevice
 */
public interface TelephonyChannel<D extends TelephonyDevice<?>> extends Channel<D> {
    // the value of type of the server unit
    String UNIT_TYPE = "[telephony-channel]";

    /**
     * <accessor>
     * To get the device of the channel
     *
     * @return channel-device instance associated with the channel
     */
    @Override
    D getDevice();

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     * @see #UNIT_TYPE
     */
    @Override
    default String getType() {
        return UNIT_TYPE;
    }

    /**
     * <accessor>
     * To check is channel busy to accept incoming device event
     *
     * @return true if channel's device is opened
     * @see Device#isOpened()
     */
    @Override
    default boolean isBusy() {
        return getDevice().isOpened();
    }
}
