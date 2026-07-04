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

import java.util.Optional;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultiMedeaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.server.core.unit.ServerUnit;


/**
 * Device of the Telephony Channel: The root device through which task communicate with computer telephony equipment
 *
 * @see TelephonyDeviceFactory
 * @see Device
 * @see CallsPortEngine
 * @see TonesEngine
 */
public interface TelephonyDevice<F extends TelephonyDeviceFactory<?>> extends Device<F>,
        // phone calls control engine
        CallsPortEngine,
        // phone line's the tones generator and the user input getter
        TonesEngine,
        // phone line's playback record features engine
        MultiMedeaEngine,
        // phone line's fax-machine features engine
        FaxMachineEngine {
    //
    // the value of type the device as the server unit
    String UNIT_TYPE = "[telephony-channel-device]";

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     * @see ServerUnit#getType()
     */
    @Override
    default String getType() {
        return UNIT_TYPE;
    }

    /**
     * <accessor>
     * To get access to device's name
     *
     * @return the value
     * @see ServerUnit#getName()
     */
    @Override
    String getName();

    /**
     * <accessor>
     * To get access to the channel-device configured parameter value
     *
     * @param name the name of configured parameter
     * @return the parameter value or empty
     * @see ConfigurationParameter
     * @see ParameterName
     * @see Optional
     * @see Device#getParameter(ParameterName)
     * @see CallsPortEngine.CallParameter
     */
    @Override
    Optional<ConfigurationParameter> getParameter(ParameterName name);

    /**
     * <accessor>
     * To get access to the current device's state
     *
     * @return the value
     * @see DeviceStateValue
     * @see Device.State
     * @see TelephonyDevice.State
     */
    @Override
    DeviceStateValue getState();

    /**
     * <accessor>
     * To check, whether device can accept incoming calls
     * This flag, the factory may set up it in the properties of the device
     *
     * @return true if device can accept incoming phone calls
     * @see CallsPortEngine#canAcceptCall()
     */
    @Override
    default boolean canAcceptCall() {
        return (boolean) getParameter(CallParameter.ACCEPT_CALL_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
    }

    /**
     * <accessor>
     * To check, whether device can make the outgoing call
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can make outgoing calls
     * @see CallsPortEngine#canMakeCall()
     */
    @Override
    default boolean canMakeCall() {
        return (boolean) getParameter(CallParameter.MAKE_CALL_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
    }

    /**
     * <accessor>
     * To check, whether device can be used in operations of connections (conference)
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can be shared for another device
     * @see CallsPortEngine#canBeConnected()
     */
    @Override
    default boolean canBeConnected() {
        return (boolean) getParameter(CallParameter.SHARE_CALL_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
    }

    /**
     * <accessor>
     * To get access to device's low-level handle
     *
     * @param <H> the type of the device's low-level handle
     * @return the handle to manipulate the device features
     */
    <H> H getHandle();

    /**
     * Telephony Device States Enumeration: The states of the device
     *
     * @see Device#getState()
     * @see DeviceStateValue
     */
    enum State implements DeviceStateValue {
        // device is waiting for an incoming call
        WAIT("WAIT"),
        // device is playing back outgoing media-stream
        PLAY("PLAY"),
        // device is recording incoming media-stream
        RECORD("RECORD"),
        // device is dialing the phone number in order to build outgoing phone call
        DIAL("DIAL"),
        // device is getting user input (getting digits)
        GTDIG("GETTING DIGITS"),
        // device is generating a tone
        TONE("TONE SEND"),
        // device's operation was terminated
        STOPD("STOPPED"),
        // device is sending a fax document
        SENDFAX("FAX SEND"),
        // device is receiving a fax document
        RECVFAX("FAX RECV");

        private final String value;

        State(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
