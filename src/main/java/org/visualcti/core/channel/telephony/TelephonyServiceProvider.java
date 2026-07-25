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
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;

/**
 * Provider Facade: The telephony service provider facade (for manufacturer implementation)
 *
 * @param <H> the type of the device's low-level operations handle
 * @see Device.ServiceProvider
 */
public interface TelephonyServiceProvider<H>  extends Device.ServiceProvider<H> {
    /**
     * <action>
     * To disable ALL events producing for particular device from the events provider
     *
     *
     * @param deviceHandle device handle of the device for which events producing is disabled
     * @see Device.Session#getDeviceHandle()
     * @see DeviceEvent.Provider#disableEvents(H)
     * @see #disableEvents(H, OperationResultValue)
     * @see EventType#ALL
     */
    @Override
    default void disableEvents(H deviceHandle) {
        disableEvents(deviceHandle, EventType.ALL);
    }

    /**
     * <action>
     * To end a phone call.
     *
     * @param handle the telephony device handle
     * @return true if operation complete successfully
     * @see CallsPortEngine#dropCall(PhoneCallSession)
     */
    boolean dropCall(H handle);

    /**
     * <action>
     * To answer to an incoming phone call.
     *
     * @param handle the telephony device handle
     * @return true if operation complete successfully
     * @see CallsPortEngine#waitForCall(PhoneCallSession, int, int, boolean)
     */
    boolean answerCall(H handle);

    /**
     * <accessor>
     * To get the caller's phone number
     *
     * @param handle the connected telephony device handle
     * @return caller's phone number value
     */
    PhoneCall.Number getCallerID(H handle);


    /**
     * <action>
     * To start making the outgoing phone call
     *
     * @param handle the telephony device handle
     * @param number the called phone number
     * @param timeout maximal waiting time for the answer (sec) to outgoing call
     * @return true if operation started successfully
     * @see CallsPortEngine#makeCall(PhoneCallSession, PhoneCall.Number, int)
     */
    boolean startCalling(H handle, PhoneCall.Number number, int timeout);

    /**
     * <accessor>
     * To check, whether service provider can accept incoming calls for device by handle
     *
     * @param handle the device's opened handle to check the feature in the service provider
     * @return true if device can accept incoming phone calls
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canAcceptCall()
     */
    default boolean canAcceptCall(H handle) {
        return false;
    }

    /**
     * <accessor>
     * To check, whether service provider can accept incoming calls for device with name
     *
     * @param name the device's name to check in the provider
     * @return true if device can accept incoming phone calls
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canAcceptCall()
     */
    default boolean canAcceptCall(String name) {
        return handleByName(name).map(this::canAcceptCall).orElse(false);
    }

    /**
     * <accessor>
     * To check, whether service provider can make the outgoing calls for device by handle
     *
     * @param handle the device's opened handle to check the feature in the service provider
     * @return true if device can make the outgoing phone calls
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canMakeCall()
     */
    default boolean canMakeCall(H handle) {
        return false;
    }

    /**
     * <accessor>
     * To check, whether service provider can make the outgoing calls for device with name
     *
     * @param name the device's name to check in the provider
     * @return true if device can make the outgoing phone calls
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canMakeCall()
     */
    default boolean canMakeCall(String name) {
        return handleByName(name).map(this::canMakeCall).orElse(false);
    }

    /**
     * <accessor>
     * To check, whether device can be used in operations of connections (conference)
     *
     * @param handle the device's opened handle to check the feature in the service provider
     * @return true if device can be shared for another device
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canBeConnected()
     */
    default boolean canBeConnected(H handle) {
        return false;
    }

    /**
     * <accessor>
     * To check, whether device can be used in operations of connections (conference)
     *
     * @param name the device's name to check in the provider
     * @return true if device can be shared for another device
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canBeConnected()
     */
    default boolean canBeConnected(String name) {
        return handleByName(name).map(this::canBeConnected).orElse(false);
    }

    /**
     * <accessor>
     * To check, whether device can operate with fax-machines
     *
     * @param handle the device's opened handle to check the feature in the service provider
     * @return true if device can operate with fax-machines
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canFax()
     */
    default boolean canFax(H handle) {
        return false;
    }

    /**
     * <accessor>
     * To check, whether device can operate with fax-machines
     *
     * @param name the device's name to check in the provider
     * @return true if device can operate with fax-machines
     * @see TelephonyDevice#getName()
     * @see TelephonyDevice#canFax()
     */
    default boolean canFax(String name) {
        return handleByName(name).map(this::canFax).orElse(false);
    }

    /**
     * EventType Enumeration: The results of all events type
     */
    enum EventType implements OperationResultValue {
        ALL("ALL");
        // status value
        private final String status;

        EventType(String status) {
            this.status = status;
        }

        @Override
        public String getValue() {
            return status;
        }
    }
}
