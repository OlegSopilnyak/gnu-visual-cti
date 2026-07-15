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
package org.visualcti.core.channel.device.adapter;


import static org.visualcti.core.channel.device.Device.State.CLOSED;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * Abstract Device of the Channel: The root device through which task can interact with external world
 *
 * @param <H> the type of the device's low-level operations handle
 * @param <F> the type of channel device factory
 * @see Device
 * @see Device.ServiceProvider
 */
public class AbstractDevice<H, F extends Factory<?>> extends ServerUnitAdapter implements Device<H, F> {
    // the basic parameters of the device
    protected final Map<ParameterName, ConfigurationParameter> parameters = new ConcurrentHashMap<>();
    // the service provider of the channel device actions functionality
    protected final transient ServiceProvider<H> serviceProvider;
    // the map of sessions' states of the device
    private final Map<H, DeviceStateValue> sessionStates = new ConcurrentHashMap<>();

    /**
     * <builder>
     * Constructor of the device of the channel
     *
     * @param provider the provider of the channel device actions
     * @see Device.ServiceProvider
     */
    protected AbstractDevice(ServiceProvider<H> provider) {
        this.serviceProvider = provider;
    }

    /**
     * <accessor>
     * To get reference to the channel-devices service provider to do this channel-device low-level operations
     *
     * @return the service provider associated with the channel-device
     * @see ServiceProvider
     */
    @Override
    public ServiceProvider<H> serviceProvider() {
        return serviceProvider;
    }

    /**
     * <notify>
     * To notify, about device's context state changed
     *
     * @param session the context with new value of the state
     * @see AbstractDeviceSession#setState(DeviceStateValue)
     * @see #dispatchError(String)
     * @see Session#getState()
     * @see Session#getDeviceHandle()
     */
    @Override
    public void stateChangedFor(final Session<H> session) {
        if (isInvalid(session)) {
            dispatchError("Wrong state changed notification for\n" + session);
        } else {
            // session is valid working with it
            if (session.getState() == CLOSED) {
                // last state before session's detaching, removing session's state from the states map
                sessionStates.remove(session.getDeviceHandle());
            } else {
                // changed state, updating the states map
                sessionStates.put(session.getDeviceHandle(), session.getState());
            }
        }
    }

    /**
     * <accessor>
     * To get the stream of the states of the active device's sessions
     *
     * @return stream of active device's sessions states
     * @see DeviceStateValue
     * @see #stateChangedFor(Session)
     */
    @Override
    public Stream<DeviceStateValue> getStates() {
        return Stream.of(sessionStates.values().toArray(new DeviceStateValue[0]));
    }

    /**
     * <action>
     * Closing the channel device removing all sessions from device factory as events listeners
     *
     * @throws IOException if an I/O error occurs
     * @see Device#close()
     */
    @Override
    public void close() throws IOException {
        Device.super.close();
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
    @Override
    public Optional<ConfigurationParameter> getParameter(final ParameterName name) {
        return Optional.ofNullable(parameters.get(name));
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AbstractDevice)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /// / private methods
    // to check the device session integrity
    private boolean isInvalid(Session<H> session) {
        return session == null || session.getDeviceHandle() == null || session.getState() == null;
    }
}
