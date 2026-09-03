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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.core.executable.task.Task;
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
public interface Device<H, F extends Factory<H, ?>> extends ServerUnit, XmlAware {
    // the value of type the server unit
    String UNIT_TYPE = "[channel-device]";
    // XML-Configuration elements' and attributes' names
    String DEFAULT_ROOT = "default";
    String DEVICE_ROOT = "device";
    String DEVICE_NAME_ATTRIBUTE = "name";
    String DEVICE_TYPE_ATTRIBUTE = "type";

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
     * <mutator>
     * To apply device's parameters from the xml-element of the vendor's configuration
     *
     * @param vendorSpecificConfigurationXml the configuration of the vendor specific device's parameters
     * @see Element
     */
    default Device<H, F> applyDeviceParameters(final Element vendorSpecificConfigurationXml) throws IOException {
        // doing nothing here
        return this;
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
     * <mutator>
     * To store configured parameter value to device's parameters by name
     *
     * @param name  the name of configured parameter
     * @param value the value of configured parameter
     * @see ParameterName
     * @see ConfigurationParameter
     */
    void setParameter(ParameterName name, ConfigurationParameter value);

    /**
     * <accessor>
     * To get the hardware parameters used in the device
     *
     * @return stream to parameter names
     * @see ParameterName
     * @see Stream
     * @see #hasHardwareParameters()
     */
    default Collection<ParameterName> hardwareParameterNames() {
        return Collections.emptySet();
    }

    /**
     * <accessor>
     * To check is there any hardware parameter in device
     *
     * @return true if the hardware parameters there
     */
    default boolean hasHardwareParameters() {
        return hardwareParameterNames().stream().map(this::getParameter).anyMatch(Optional::isPresent);
    }

    /**
     * <builder>
     * To create the session for the opened device resource handle
     *
     * @param openedDeviceHandle the handle of the opened device resource
     * @return built device session
     * @throws IOException if device cannot create the session for device handle
     * @see DeviceActivitySession
     */
    default DeviceActivitySession<H> createSessionFor(H openedDeviceHandle) throws IOException {
        throw new IOException("Not supported yet.");
    }

    /**
     * <action>
     * To create and start device's session
     *
     * @return opened device's session
     * @throws IOException if device cannot start the session
     */
    default DeviceActivitySession<H> startSession() throws IOException {
        // opening the device provider resource
        final H deviceHandle = serviceProvider().openResource(getName());
        // to check opened device's handle value
        if (isInvalidHandle(deviceHandle)) {
            throw new IOException("Invalid device handle!");
        }
        // stopping and detach the after open session, if it's exists
        findInitiatedSession().ifPresent(this::detachAndClose);
        // filling device specific parameters
        fillingDeviceSpecific(deviceHandle);
        // building new session for the device handle
        final DeviceActivitySession<H> session = createSessionFor(deviceHandle);
        // add the created device session as device events listener
        getFactory().getHub().addDeviceEventListenerFor(getName(), session);
        // notifying about created session state
        stateChangedFor(session);
        // reruns built well device session
        return session;
    }

    /**
     * <action>
     * Getting the device hardware parameters from service provider by device's handle
     * and store them to the device's basic parameters
     *
     * @param handle valid opened device handle value
     * @see #startSession()
     * @see ServiceProvider#resourceParameter(H, ParameterName)
     * @see #hasHardwareParameters()
     * @see #hardwareParameterNames()
     * @see #setParameter(ParameterName, ConfigurationParameter)
     */
    default void fillingDeviceSpecific(H handle) {
        if (!this.hasHardwareParameters()) {
            //
            // no hardware parameters loaded from service provider
            final ServiceProvider<H> provider = serviceProvider();
            //
            // iterating hardware parameters names
            hardwareParameterNames().forEach(name ->
                    // getting the hardware parameter by name from the device's service provider
                    provider.resourceParameter(handle, name).ifPresent(
                            // storing resource's hardware parameter to the basic device parameters map
                            parameter -> Device.this.setParameter(name, parameter)
                    )
            );
        }
    }

    /**
     * <checker>
     * To check the value of device handle
     *
     * @param deviceHandle handle after open resource operation
     * @return true if value is invalid
     * @see #startSession()
     */
    default boolean isInvalidHandle(H deviceHandle) {
        return deviceHandle == null;
    }

    /**
     * <action>
     * To stop device's session and detach it from device events stream
     *
     * @param session opened device's session
     * @see DeviceActivitySession#terminate()
     * @see DeviceActivitySession#close()
     * @see DeviceActivitySession#getDeviceHandle()
     * @see ServiceProvider#closeResource(Object)
     * @see #getFactory()
     * @see #getName()
     * @see Factory#getHub()
     * @see DeviceEvent.Listener.Hub#removeDeviceEventListenerFor(String, DeviceEvent.Listener)
     */
    default void detachAndClose(final DeviceActivitySession<H> session) {
        // detach
        // removing the device session as an events listener from the factory
        getFactory().getHub().removeDeviceEventListenerFor(getName(), session);
        // close
        try {
            // terminating current device activities of the session
            session.terminate();
            // getting the session's device handle
            final H handleToClose = session.getDeviceHandle();
            // closing the device provider's resource (can throw IOException)
            if (!findSessionByHandle(handleToClose).isPresent()) {
                // handle to close is unique among the sessions' handles so the resource can be closed
                serviceProvider().closeResource(handleToClose);
            }
            // closing current device session
            session.close();
        } catch (IOException e) {
            dispatchError(e, "Cannot stop and detach opened device session");
        }
    }

    /**
     * <notify>
     * To notify, about device's session state changed
     *
     * @param session the session with new value of the state
     * @see DeviceActivitySession#getState()
     */
    void stateChangedFor(DeviceActivitySession<H> session);

    /**
     * <accessor>
     * To get the stream of the states of the active device's sessions
     *
     * @return stream of active device's sessions states
     * @see DeviceActivitySession#getState()
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
     * @see DeviceActivitySession
     * @see #isOpened()
     * @see #findSessionByHandle(Object)
     * @see #close()
     */
    default Stream<DeviceActivitySession<H>> sessions() {
        return getFactory().getHub().eventListeners(getName()).filter(DeviceActivitySession.class::isInstance)
                .map(session -> (DeviceActivitySession<H>) session);
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
        final DeviceActivitySession<H> session = startSession();
        if (!session.isOpened()) {
            // removing the broken device session as device events listener from the factory
            getFactory().getHub().removeDeviceEventListenerFor(getName(), session);
            // starting is failed
            final String message = "Device Session could not be opened!";
            dispatchError(message);
            throw new IOException(message);
        } else {
            // marking session as 'initiated' for further detaching and close
            session.parameter(Parameter.INITIATED, true);
            dispatchEvent("Opened device :" + getDeviceName());
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
     * @see DeviceActivitySession
     * @see DeviceActivitySession#hasDeviceHandle(H)
     * @see #sessions()
     */
    default Optional<DeviceActivitySession<H>> findSessionByHandle(final H deviceHandle) {
        return sessions().filter(session -> session.hasDeviceHandle(deviceHandle)).findFirst();
    }

    /**
     * <accessor>
     * To look for the session with created after device open operation
     *
     * @return found context or empty
     * @see Optional
     * @see DeviceActivitySession
     * @see DeviceActivitySession#parameterOrDefault(ParameterName, Object)
     * @see Parameter#INITIATED
     * @see #sessions()
     * @see #open()
     */
    default Optional<DeviceActivitySession<H>> findInitiatedSession() {
        return sessions().filter(session ->
                session.parameterOrDefault(Parameter.INITIATED, false)
        ).findFirst();
    }

    /**
     * <action>
     * Closing of the device, if there are no active operations and
     * the expectation of the end of current operation still executing
     *
     * @throws IOException if channel cannot be closed
     * @see #detachAndClose(DeviceActivitySession)
     */
    @Override
    default void close() throws IOException {
        // closing device's resource and removing sessions as device events listener
        for (final DeviceActivitySession<H> session : (Iterable<DeviceActivitySession<H>>) sessions()::iterator) {
            // detaching  and closing the opened device session
            detachAndClose(session);
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
        final ConfigurationParameter attempt = getParameter(Parameter.REPAIR_ATTEMPT).orElse(null);
        final int repairTryAttempts = attempt != null ? attempt.getValue() : 20;
        final ConfigurationParameter nextTryIn = getParameter(Parameter.REPAIR_TIMEOUT).orElse(null);
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
     * Device States Enumeration: The states of the device
     *
     * @see DeviceActivitySession#getState()
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
     * The parent of configured parameter names enumerations
     */
    interface ParameterName {
        String value();
    }

    /**
     * Enumeration: Parameter names for device activity
     */
    enum Parameter implements ParameterName {
        INITIATED("DEVICE-SESSION-AFTER-OPEN"),
        LATCH("DEVICE-OPERATION-LATCH"),
        RESULT("DEVICE-OPERATION-RESULT"),
        SHARED("DEVICE-SHARED-SESSIONS"),
        PROVIDER("DEVICE-SERVICE-PROVIDER"),
        NAME("DEVICE-NAME"),
        DEVICE_HANDLE("DEVICE-SESSION-HANDLE"),
        FAX_DEVICE_HANDLE("FAX-SESSION-HANDLE"),
        STATE("DEVICE-SESSION-STATE"),
        ALIVE("DEVICE-SESSION-CONNECTED"),
        USER_INPUT("SESSION-USER-INPUT"),
        TERMINATE("DEVICE-SESSION-TERMINATED"),
        OPEN("DEVICE-SESSION-OPENED"),
        REPAIR_ATTEMPT("DEVICE-REPAIR-ATTEMPT"),
        REPAIR_TIMEOUT("DEVICE-REPAIR-TIMEOUT");
        private final String name;

        Parameter(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name.toLowerCase();
        }

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
         * @see DeviceActivitySession#getDeviceHandle()
         */
        H openResource(String name) throws IOException;

        /**
         * <action>
         * To close the device related resource
         *
         * @param handle the handle of the opened resource (device's implementation)
         * @throws IOException if channel's resource cannot be closed
         * @see DeviceActivitySession#getDeviceHandle()
         */
        void closeResource(H handle) throws IOException;

        /**
         * <acessor>
         * To get resource's specific device parameter by parameter name
         *
         * @param handle the handle of the opened resource
         * @param name   the name of parameter to get
         * @return exists parameter value or empty if not exists
         * @see ParameterName
         * @see ConfigurationParameter
         * @see Optional
         */
        default Optional<ConfigurationParameter> resourceParameter(H handle, ParameterName name) {
            return Optional.empty();
        }

        /**
         * <acessor>
         * To find any handler for the resource by name
         *
         * @param name the name of the opened resource
         * @return handle to opened resource or empty
         * @see Optional
         * @see #openResource(String)
         * @see #open()
         */
        default Optional<H> handleByName(String name) {
            return Optional.empty();
        }

        /**
         * <acessor>
         * To get the collection of allowed devices names
         *
         * @return collection of allowed names
         * @see Collection
         * @see Factory#buildDevice(String, ServiceProvider)
         */
        default Collection<String> allowedDevices() {
            return Collections.emptyList();
        }
    }
}
