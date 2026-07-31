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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.media.Fax;
import org.visualcti.media.Sound;

/**
 * Provider Facade: The telephony service provider facade (for manufacturer implementation)
 *
 * @param <H> the type of the device's low-level operations handle
 * @see Device.ServiceProvider
 */
public interface TelephonyServiceProvider<H> extends Device.ServiceProvider<H> {
    /**
     * <action>
     * To open the device related resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if channel's fax resource cannot be opened or activated
     * @see Device#getName()
     * @see Device.Session#parameter(Device.ParameterName, Object)
     * @see Device.Parameter#FAX_DEVICE_HANDLE
     */
    default H openFaxResource(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <action>
     * To close the device related resource
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @see Device.Session#getDeviceHandle()
     * @see Device.Session#parameter(Device.ParameterName, Object)
     * @see Device.Parameter#FAX_DEVICE_HANDLE
     */
    default void closeFaxResource(H handle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <action>
     * To disable ALL events producing for particular device from the events provider
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
     * @param handle  the telephony device handle
     * @param number  the called phone number
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
     * <action>
     * To separate two resources on the low-level after the conference connection finished
     *
     * @param secondHandle  the second resource's handle
     * @param primaryHandle the primary resource's handle
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    default void breakConnection(H secondHandle, H primaryHandle) {
        // doing nothing here
    }

    /**
     * <action>
     * To join two resources on the low-level for the conference connection
     *
     * @param secondHandle  the second resource's handle
     * @param primaryHandle the primary resource's handle
     * @return true if they are joint well
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    default boolean makeConnection(H secondHandle, H primaryHandle) {
        return false;
    }

    /**
     * <action>
     * To start receiving fax document
     *
     * @param handle            the telephony device handle
     * @param filePath          the path to the file for the receiving fax document content
     * @param issueVoiceRequest upon termination of receive to give out a
     *                          sound signal on the remote fax-device
     * @return true if operation started successfully
     * @see FaxMachineEngine#receive(PhoneCallSession, OutputStream, boolean, boolean)
     */
    default boolean startFaxReceiving(H handle, String filePath, boolean issueVoiceRequest) {
        return false;
    }

    /**
     * <action>
     * To stop (interrupt) receiving fax document
     *
     * @param handle the telephony device handle
     * @see FaxMachineEngine#transmit(PhoneCallSession, InputStream, Fax, boolean)
     */
    default void stopFaxReceiving(H handle) {

    }

    /**
     * <action>
     * To start transmitting fax document
     *
     * @param handle            the telephony device handle
     * @param filePath          the path to the file, fax document content
     * @param issueVoiceRequest upon termination of receive to give out a
     * @param isTiff            the parameter of transmitting document page
     * @param isHighResolution  the parameter of transmitting document page
     * @param firstPageNumber   transmit from page
     * @param totalPages        transmit pages (negative value means all available pages)
     *                          sound signal on the remote fax-device
     * @return true if operation started successfully
     * @see FaxMachineEngine#transmit(PhoneCallSession, InputStream, Fax, boolean)
     */
    default boolean startFaxTransmitting(H handle, String filePath, boolean issueVoiceRequest,
                                 boolean isTiff, boolean isHighResolution, int firstPageNumber, int totalPages){
        return false;
    }

    /**
     * <action>
     * To stop (interrupt) transmitting fax document
     *
     * @param handle the telephony device handle
     * @see FaxMachineEngine#transmit(PhoneCallSession, InputStream, Fax, boolean)
     */
    default void stopFaxTransmitting(H handle) {

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
