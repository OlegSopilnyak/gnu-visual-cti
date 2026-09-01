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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.media.Sound;

/**
 * The Factory of the Devices: The factory of the telephony channel-devices
 *
 * @param <H> the type of the telephony device's low-level operations' handle
 * @param <TD> the type of factory's telephony device
 * @see TelephonyDevice
 * @see Factory
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface TelephonyFactory<H, TD extends TelephonyDevice<?, ?>> extends Factory<H, TD> {
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
     * To get the device instance from the factory by it name
     *
     * @param deviceName the name of device in the factory
     * @return the device or empty, if device with name is not exists in the factory
     * @see Factory#getDevice(String)
     */
    @Override
    default Optional<TD> getDevice(final String deviceName) {
        return Factory.super.getDevice(deviceName);
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
    default Stream<TD> devices() {
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

    /**
     * <action>
     * To share active phone call session for the connection feature
     *
     * @param handle the phone call's session's device handle, device is working with
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    default void shareDevice(final H handle) {
        devices()
                .filter(device -> deviceContainsHandle(device, handle))
                .map(device -> sessionForHandle(device, handle))
                .filter(Objects::nonNull).findFirst()
                .ifPresent(this::shareDevice);
    }

    /**
     * <action>
     * To share opened phone call session for the connection feature
     *
     * @param session the phone call's session, device is working with
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    default void shareDevice(PhoneCallSession<H> session) {
        // feature isn't supported here
        session.operationResult(Result.CALL.Analysis.NO_DIAL_TONE);
    }

    /**
     * <action>
     * To un-share active phone call session for the connection feature
     *
     * @param handle the phone call's session's device handle, device is working with
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    default void unShareDevice(H handle) {
        devices()
                .filter(device -> deviceContainsHandle(device, handle))
                .map(device -> sessionForHandle(device, handle))
                .filter(Objects::nonNull).findFirst()
                .ifPresent(this::unShareDevice);
    }

    /**
     * <action>
     * To un-share opened phone call session for the connection feature
     *
     * @param session the phone call's session, device is working with
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    default void unShareDevice(PhoneCallSession<H> session) {
        // doing nothing here
    }

    /**
     * <finder>
     * To find shared telephony device session for the connection feature
     *
     * @param callableNumber the number to connect to
     * @param master the session which will capture and join the connectable session
     * @return the ready for connect session or empty if not exists
     * @see Optional
     * @see PhoneCallSession
     * @see PhoneCall.Number
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    Optional<PhoneCallSession<H>> findConnectableFor(PhoneCall.Number callableNumber, PhoneCallSession<H> master);

    // to check is device has the session with the handle
    static <H> boolean deviceContainsHandle(TelephonyDevice device, H handle) {
        return device.findSessionByHandle(handle).isPresent();
    }

    // to get the session with handle of the device
    static <H> PhoneCallSession<H> sessionForHandle(TelephonyDevice device, H handle) {
        return (PhoneCallSession) device.findSessionByHandle(handle).orElse(null);
    }
}
