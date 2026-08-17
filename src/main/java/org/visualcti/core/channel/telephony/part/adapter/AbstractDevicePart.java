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
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.telephony.TelephonyDeviceCore;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.TelephonyDevicePart;

/**
 * The Part of the Telephony Channel Device: The device part adapter for communicate with device factory
 *
 * @param <H> the type of the telephony device's low-level operations handle
 * @see TelephonyDevicePart
 * @see XmlAware
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDevicePart<H> implements TelephonyDevicePart<H>, XmlAware {
    // predicate to test whether device handle is valid or not
    protected final transient Predicate<H> validResourceHandle =
            handle -> !Objects.equals(handle, wrongHandle()) || !Objects.equals(handle, errorHandle());
    // predicate to test whether device session is closed or not
    protected final transient Predicate<PhoneCallSession<H>> closedSession =
            phoneSession -> phoneSession.getState() == Device.State.CLOSED;
    // predicate to test whether device handle is valid or not
    protected final transient Predicate<PhoneCallSession<H>> validDeviceHandle =
            phoneSession -> validResourceHandle.test(phoneSession.parameter(Device.Parameter.DEVICE_HANDLE));
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
     * The unconditional phone call disconnection:
     * 1. Break telephony connection with all joint sessions
     * 2. End up the current phone call (hang off)
     * 3. Detaching from all joint phone call sessions
     *
     * @param session the phone call's session, device is working with
     * @see PhoneCallSession#joint()
     * @see TelephonyServiceProvider#breakConnection(H, H)
     * @see PhoneCallSession#detachAll()
     * @see TelephonyServiceProvider#handsetOff(H)
     */
    @Override
    public void disconnect(final PhoneCallSession<H> session) {
        if (!isOpened(session)) {
            // the session didn't open or already closed
            return;
        }
        // getting the device's handle
        final H handle = session.getDeviceHandle();
        // getting device service provider
        final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
        // dropping telephony call on the device service provider site
        if (serviceProvider.handsetOff(handle)) {
            // after possible connect with another phone number
            // breaking the connections with all joint phone call sessions
            session.joint().map(phoneCall -> (PhoneCallSession<H>) phoneCall)
                    .map(DeviceActivitySession::getDeviceHandle)
                    .forEach(second -> serviceProvider.breakConnection(second, handle));
            // detaching all possible joint sessions
            session.detachAll();
            // disconnecting the phone call session
            disconnectingTheSession(session);
            // disable all events producing for the opened handle
            serviceProvider.disableEvents(handle);
            // enable producing incoming call events for the opened handle
            serviceProvider.enableEvents(handle, Result.CALL.RINGS);
        } else {
            // malfunction in device is detected
            session.setState(Device.State.ERROR);
            // drop call didn't work properly on the service provider side, notify about it
            breakingTheSession(session, "Cannot disconnect on the service provider side.");
        }

    }

    /**
     * <action>
     * To process the error operation complete during device's call
     *
     * @param session the phone call's session, device is working with
     * @param reason  the reason of malfunction
     * @see #onDeviceError(DeviceActivitySession, String, boolean)
     * @see Result#ERROR
     */
    protected void onDeviceError(DeviceActivitySession<H> session, String reason) {
        onDeviceError(session, reason, true);
    }

    /**
     * <action>
     * To process the error operation complete during device's call
     *
     * @param session        the phone call's session, device is working with
     * @param reason         the reason of malfunction
     * @param throwException the flag is it should throw the Error
     * @see Result#ERROR
     * @see DeviceMalfunction
     */
    protected void onDeviceError(DeviceActivitySession<H> session, String reason, boolean throwException) {
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

    /**
     * <action>
     * To mark as error phone call session using device event
     *
     * @param session the phone call's session, device is working with
     * @param reason  the reason of malfunction
     * @param <H>     the general type of the device handle
     */
    protected static <H> void breakingTheSession(final PhoneCallSession<H> session, final String reason) {
        final DeviceEvent<H> event = AbstractDeviceEvent.<H>of(DeviceEvent.Type.MALFUNCTION)
                .deviceName(session.getDevice().getName()).deviceHandle(session.getDeviceHandle())
                .description(reason)
                .option(DeviceEvent.Option.REASON, Result.ERROR);
        // delivering built event
        session.accept(event);
    }

    /**
     * <checker>
     * To check is phone call session opened
     *
     * @param session the phone call's session, device is working with
     * @return true if session opened well
     * @see PhoneCallSession#parameter(Device.ParameterName)
     * @see Device.Parameter#DEVICE_HANDLE
     * @see PhoneCallSession#setState(DeviceStateValue)
     * @see Device.State#CLOSED
     */
    public boolean isOpened(final PhoneCallSession<H> session) {
        if (closedSession.negate().and(validDeviceHandle).test(session)) {
            // the state of the session is valid (not closed) and value of device's handle is valid as well
            return true;
        } else {
            // the value of device's handle is invalid, set up state as closed
            session.setState(Device.State.CLOSED);
            return false;
        }
    }


    /// private methods
    // disconnecting phone call session using device event
    private static <H> void disconnectingTheSession(final PhoneCallSession<H> session) {
        final DeviceEvent<H> event = AbstractDeviceEvent.<H>of(DeviceEvent.Type.DEVICE_SPECIFIC)
                .deviceName(session.getDevice().getName()).deviceHandle(session.getDeviceHandle())
                .description("Disconnecting after dropped call.")
                .option(DeviceEvent.Option.REASON, Result.CALL.DISCONNECT);
        // delivering built event
        session.accept(event);
    }
}
