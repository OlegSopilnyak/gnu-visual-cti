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
package org.visualcti.core.channel.device;

import java.io.IOException;
import java.util.Optional;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.task.Environment;

/**
 * Device of the Channel: The root device through which task communicate with external world
 *
 * @param <F> the type of channel device factory
 * @see Task#setEnv(Environment)
 * @see Environment#setPart(String, Object)
 * @see Environment#getPart(String, Class)
 * @see ServerUnit
 */
@SuppressWarnings("unchecked")
public interface Device<F extends Factory<?>> extends ServerUnit {
    // the value of type the server unit
    String UNIT_TYPE = "[channel-device]";

    /**
     * <accessor>
     * To get reference to the channel-devices factory, the owner of this channel-device
     *
     * @return the factory-owner of the channel-device or throws DeviceMalfunction
     * @see #getFactoryOptional()
     * @see Factory
     * @see DeviceMalfunction
     */
    default F getFactory() {
        return getFactoryOptional()
                .orElseThrow(() -> new DeviceMalfunction(this, "No Factory for the Device!"))
                ;
    }

    /**
     * <accessor>
     * To get optional reference to the channel-devices factory, the owner of this channel-device
     *
     * @return the optional factory-owner of the channel-device
     * @see Factory
     * @see Optional
     */
    default Optional<F> getFactoryOptional() {
        final ServerUnit owner = getOwner();
        return Optional.ofNullable((owner instanceof Factory) ? (F) owner : null);
    }

    /**
     * <accessor>
     * To get access to the channel-device configured parameter value
     *
     * @param name the name of configured parameter
     * @return the parameter value or empty
     * @see ConfigurationParameter
     * @see ParameterName
     * @see Optional
     */
    Optional<ConfigurationParameter> getParameter(ParameterName name);

    /**
     * <action>
     * Opening and activation of the channel-device.
     *
     * @throws IOException if channel cannot be opened or activated
     */
    void open() throws IOException;

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     */
    boolean isOpened();

    /**
     * <action>
     * Closing of the device, if there are no active operations and
     * the expectation of the end of current operation still executing
     *
     * @throws IOException if channel cannot be closed
     */
    @Override
    void close() throws IOException;

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @throws IOException If the device can't terminate current operation
     */
    void terminate() throws IOException;

    /**
     * <accessor>
     * To get access to the state of the channel-device
     *
     * @return value of device state
     * @see DeviceStateValue#getValue()
     * @see Device.State
     */
    DeviceStateValue getState();

    /**
     * <mutator>
     * To set up the new state value of the channel-device
     *
     * @param state new value of device state
     * @see DeviceStateValue#getValue()
     */
    void setState(DeviceStateValue state);

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
     * <accessor>
     * To get the Name of the device to use it the task runner
     *
     * @return the value
     * @see ChannelTaskRunner
     * @see #getFactory()
     * @see Factory#getVendor()
     * @see #getName()
     */
    default String getDeviceName() {
        return getFactory().getVendor() + "/" + getName();
    }

    /**
     * <accessor>
     * To check is unit needs to be registered in units registry
     *
     * @return true if unit needed registration
     * @see UnitRegistry#register(ServerUnit)
     */
    @Override
    default boolean isNeedRegistration() {
        return false;
    }

    /**
     * <repair>
     * Try to repair device after malfunction
     *
     * @return true if repaired well
     */
    boolean repair();

    /**
     * The parent of configured parameter names enumerations
     */
    interface ParameterName {
        String value();
    }

    /**
     * Device States Enumeration: The states of the device
     *
     * @see Device#getState()
     * @see DeviceStateValue
     */
    enum State implements DeviceStateValue {
        // hardware error on the device
        ERROR("ERROR"),
        // device is closed
        CLOSED("CLOSED"),
        // device is doing nothing at the moment
        IDLE("IDLE");

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
