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
package org.visualcti.core.channel.telephony.part.adapter;

import java.util.Objects;
import java.util.function.Predicate;
import org.visualcti.core.XmlAware;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.core.channel.telephony.TelephonyDeviceCore;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.part.TelephonyDevicePart;

/**
 * The Part of the Telephony Channel Device: The device part adapter for communicate with device factory
 *
 * @see TelephonyDeviceFactory
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDevicePart<H> implements TelephonyDevicePart<H>, XmlAware {
    protected final transient Predicate<H> validResourceHandle =
            handle -> !Objects.equals(handle, wrongHandle()) || !Objects.equals(handle, errorHandle());
    // the core of the telephony device
    protected transient TelephonyDeviceCore<H> deviceCore;

    /**
     * <mutator>
     * To assign device core which will be used in the device part
     *
     * @param deviceCore device core will be used in the part
     */
    @Override
    public <P extends TelephonyDevicePart<?>> P uses(TelephonyDeviceCore<H> deviceCore) {
        this.deviceCore = deviceCore;
        return (P) this;
    }

    /**
     * <action>
     * To process the error operation complete during device's call
     *
     * @param session the phone call's session, device is working with
     * @param reason the reason of malfunction
     * @see #onDeviceError(Device.Session, String, boolean)
     * @see Result#ERROR
     */
    protected void onDeviceError(Device.Session<H> session, String reason) {
        onDeviceError(session, reason, true);
    }

    /**
     * <action>
     * To process the error operation complete during device's call
     *
     * @param session the phone call's session, device is working with
     * @param reason the reason of malfunction
     * @param throwException the flag is it should throw the Error
     * @see Result#ERROR
     * @see DeviceMalfunction
     */
    protected void onDeviceError(Device.Session<H> session, String reason, boolean throwException) {
        session.setState(Device.State.ERROR);
        session.getDevice().dispatchError(reason);
        if (throwException) {
            throw new DeviceMalfunction(session.getDevice(), reason);
        }

    }
    /**
     * <accessor>
     * To get access to the wrong value device's low-level handle
     *
     * @return the value for handle of unopened device
     */
    protected H wrongHandle() {
        return null;
    }

    /**
     * <accessor>
     * To get access to the error value device's low-level handle
     *
     * @return the value for handle of corrupted device
     */
    protected H errorHandle() {
        return null;
    }
}
