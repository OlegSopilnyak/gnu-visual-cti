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

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.task.Environment;

/**
 * Device of the Channel: The root device through which task communicate with external world
 *
 * @param <H> the type of the device's low-level operations handle
 * @param <F> the type of channel device factory
 * @see Task#setEnv(Environment)
 * @see Environment#setPart(String, Object)
 * @see Environment#getPart(String, Class)
 * @see ServerUnit
 */
@SuppressWarnings("unchecked")
public interface Device<H, F extends Factory<H, ?>> extends ServerUnit {
    // the value of type the server unit
    String UNIT_TYPE = "[channel-device]";
    // parameter name of the quantity of repair attempts
    ParameterName REPAIR_ATTEMPT = Repair.ATTEMPT;
    // parameter name of the timeout between repair attempts
    ParameterName REPAIR_TIMEOUT = Repair.TIMEOUT;

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
     * To get reference to the channel-devices service provider to do this channel-device low-level operations
     *
     * @return the service provider associated with the channel-device
     * @see ServiceProvider
     */
    default ServiceProvider<H> serviceProvider() {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * <builder>
     * To create the session for the opened device resource handle
     *
     * @param openedDeviceHandle the handle of the opened device resource
     * @return built device session
     * @throws IOException if device cannot create the session for device handle
     * @see Session
     */
    default Session<H> createSessionFor(H openedDeviceHandle) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <action>
     * To create and start device's session
     *
     * @return opened device's session
     * @throws IOException if device cannot start the session
     */
    default Session<H> startSession() throws IOException {
        // opening the device provider resource
        final H deviceHandle = serviceProvider().openResource(getName());
        // stopping and detach the old session, if it exists
        findSessionByHandle(deviceHandle).ifPresent(this::stopAndDetach);
        // building new session for the device handle
        final Session<H> session = createSessionFor(deviceHandle);
        // add the device session as device events listener
        getFactory().getHub().addDeviceEventListenerFor(getName(), session);
        // notifying about created session state
        stateChangedFor(session);
        // reruns built well device session
        return session;
    }

    /**
     * <action>
     * To stop device's session and detach it from device events stream
     *
     * @param session opened device's session
     */
    default void stopAndDetach(final Session<H> session) {
        try {
            // terminating current device activities of the session
            session.terminate();
            // closing current device session
            session.close();
            // closing device provider resource (can throw IOException)
            serviceProvider().closeResource(session.getDeviceHandle());
        } catch (IOException e) {
            dispatchError(e, "Cannot stop and detach opened device session");
        }
        // removing the device session as an events listener from the factory
        getFactory().getHub().removeDeviceEventListenerFor(getName(), session);

    }

    /**
     * <notify>
     * To notify, about device's session state changed
     *
     * @param session the session with new value of the state
     * @see Session#getState()
     */
    void stateChangedFor(Session<H> session);

    /**
     * <accessor>
     * To get the stream of the states of the active device's sessions
     *
     * @return stream of active device's sessions states
     * @see Session#getState()
     * @see DeviceStateValue
     */
    Stream<DeviceStateValue> getStates();

    /**
     * <accessor>
     * To get the stream of active device sessions
     *
     * @return stream of active device sessions
     * @see #getFactory()
     * @see #getName()
     * @see DeviceEvent.Listener.Hub#eventListeners(String)
     * @see Session
     * @see #isOpened()
     * @see #findSessionByHandle(Object)
     * @see #close()
     */
    default Stream<Session<H>> sessions() {
        return getFactory().getHub().eventListeners(getName())
                .filter(Session.class::isInstance).map(context -> (Session<H>) context);
    }

    /**
     * <action>
     * Opening and activation of the channel-device.
     *
     * @throws IOException if channel cannot be opened or activated
     * @see #startSession()
     */
    default void open() throws IOException {
        // trying to start session
        final Session<H> session = startSession();
        if (!session.isOpened()) {
            // removing the broken device session as device events listener from the factory
            getFactory().getHub().removeDeviceEventListenerFor(getName(), session);
            // starting is failed
            final String message = "Device Session could not be opened!";
            dispatchError(message);
            throw new IOException(message);
        }
    }

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     */
    default boolean isOpened() {
        return sessions().findAny().isPresent();
    }

    /**
     * <accessor>
     * To look for the session with particular device handle
     *
     * @param deviceHandle the device handle to look for the context by
     * @return found context or empty
     * @see Optional
     * @see Session
     * @see #sessions()
     * @see #open()
     */
    default Optional<Session<H>> findSessionByHandle(H deviceHandle) {
        return sessions().filter(context -> Objects.equals(context.getDeviceHandle(), deviceHandle)).findFirst();
    }

    /**
     * <action>
     * Closing of the device, if there are no active operations and
     * the expectation of the end of current operation still executing
     *
     * @throws IOException if channel cannot be closed
     * @see #getName()
     * @see #getFactory()
     * @see #serviceProvider()
     * @see DeviceEvent.Listener.Hub#eventListeners(String)
     * @see ServiceProvider#closeResource(Object)
     * @see DeviceEvent.Listener.Hub#removeDeviceEventListenerFor(String, DeviceEvent.Listener)
     */
    @Override
    default void close() throws IOException {
        // closing device's resource and removing sessions as device events listener
        for (final Session<H> deviceSession : sessions().toArray(Session[]::new)) {
            // stopping the opened device session
            stopAndDetach(deviceSession);
        }
    }

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
     * @return true if device is repaired well
     * @see #close()
     * @see #open()
     * @see #isOpened()
     * @see #dispatchError(Throwable, String)
     * @see TimeUnit#SECONDS
     * @see TimeUnit#sleep(long)
     */
    default boolean repair() {
        final ConfigurationParameter attempt = getParameter(REPAIR_ATTEMPT).orElse(null);
        final int repairTryAttempts = attempt != null ? attempt.getValue() : 20;
        final ConfigurationParameter nextTryIn = getParameter(REPAIR_TIMEOUT).orElse(null);
        final int nextTryInSeconds = nextTryIn != null ? nextTryIn.getValue() : 3000;
        // repairing sequence
        try {
            // closing the device
            close();
            // trying 20 times to open the device
            for (int i = 1; i <= repairTryAttempts; i++) {
                // try to open the device
                tryToOpenTheDevice(this);
                // check device opening result
                if (isOpened()) {
                    // device repaired well
                    return true;
                }
                // sleeping 3 sec, before next try
                sleepMilliseconds(nextTryInSeconds);
            }
        } catch (IOException e) {
            dispatchError(e, "Cannot repair device: " + getDeviceName());
        }
        // not repaired
        return false;
    }

    static void tryToOpenTheDevice(final Device<?, ?> device) throws IOException {
        try {
            device.open();
        } catch (Exception e) {
            device.dispatchError(e, "Cannot open device: " + device.getDeviceName());
        }
    }

    static void sleepMilliseconds(final long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Enumeration: Parameter names for device repair activity
     */
    enum Repair implements ParameterName {
        ATTEMPT("ATTEMPT"),
        TIMEOUT("TIMEOUT");

        private final String name;

        Repair(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name.toLowerCase();
        }
    }

    /**
     * The parent of configured parameter names enumerations
     */
    interface ParameterName {
        String value();
    }

    /**
     * Device States Enumeration: The states of the device
     *
     * @see Session#getState()
     * @see DeviceStateValue
     */
    enum State implements DeviceStateValue {
        // hardware error on the device
        ERROR("ERROR"),
        // device is closed
        CLOSED("CLOSED"),
        // device activity was closed
        STOPD("STOPED"),
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

    /**
     * Device Activity Session: The session of device's activity for the task
     *
     * @param <H> the type of the device's low-level operations handle
     */
    interface Session<H> extends DeviceEvent.Listener, Closeable {
        /**
         * <accessor>
         * To get access to device-owner of the context
         *
         * @return the device-owner reference
         */
        Device<H, ? extends Factory<H, ?>> getDevice();

        /**
         * <accessor>
         * To get access to opened device's internal handle
         *
         * @return device's handle
         */
        H getDeviceHandle();

        /**
         * <accessor>
         * Check, is device already opened
         *
         * @return true if it's opened
         */
        boolean isOpened();

        /**
         * <accessor>
         * To get access to device's internal name
         *
         * @return device's name
         * @see Device#getName()
         */
        String getDeviceName();

        /**
         * <accessor>
         * To get access to context's termination flag
         *
         * @return the flag's value
         */
        boolean isTerminated();

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
         * To get access to the state of the channel-device context
         *
         * @return value of device state
         * @see DeviceStateValue#getValue()
         * @see Device.State
         */
        DeviceStateValue getState();

        /**
         * <mutator>
         * To set up the new state value of the channel-device context
         *
         * @param state new value of device state
         * @see DeviceStateValue#getValue()
         */
        void setState(DeviceStateValue state);

        /**
         * <accssor>
         * To check up the condition of the channel-device context
         *
         * @return true if the device context is in service (connected)
         */
        boolean isAlive();

    }

    /**
     * Device Activity Service Provider: The provider of device's activity
     *
     * @param <H> the type of the device's low-level operations handle
     */
    interface ServiceProvider<H> extends DeviceEvent.Provider<H> {
        /**
         * <action>
         * To open the device related resource (device's implementation)
         *
         * @param name the name of the resource
         * @return handle for the opened resource
         * @throws IOException if channel's resource cannot be opened or activated
         * @see Session#getDeviceHandle()
         */
        H openResource(String name) throws IOException;

        /**
         * <action>
         * To open the device related resource
         *
         * @param handle the handle of the opened resource (device's implementation)
         * @throws IOException if channel's resource cannot be closed
         * @see Session#getDeviceHandle()
         */
        void closeResource(H handle) throws IOException;
    }
}
