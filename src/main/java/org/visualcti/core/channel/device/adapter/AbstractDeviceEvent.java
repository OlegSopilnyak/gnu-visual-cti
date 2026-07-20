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

import static org.visualcti.core.channel.device.DeviceEvent.Type.INCOMING;
import static org.visualcti.core.channel.device.DeviceEvent.Type.MALFUNCTION;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.Factory;

/**
 * The event from the channel-device side
 *
 * @param <H> the type of device's handle (for low-level operations)
 * @see DeviceEvent
 */
public class AbstractDeviceEvent<H> implements DeviceEvent<H> {
    private final Type eventType;
    private H deviceHandle;
    private String deviceName;
    private String vendor;
    private String description;
    private final Map<Device.ParameterName, Object> parameters = new ConcurrentHashMap<>();

    public AbstractDeviceEvent() {
        this(Type.DEVICE_SPECIFIC);
    }

    /**
     * <builder>
     * The builder of device event instance
     *
     * @param eventType the type of the event
     * @return built device event instance
     * @param <H> the type of device's handle (for low-level operations)
     */
    public static <H> AbstractDeviceEvent<H> of(Type eventType) {
        return new AbstractDeviceEvent<>(eventType);
    }

    /**
     * <builder>
     * The builder of device event instance from previous device event (parent event type is ignoring)
     *
     * @param eventType the type of the event
     * @param parent the parent of new event instance
     * @return built device event instance
     * @param <H> the type of device's handle (for low-level operations)
     * @see #of(Type)
     * @see DeviceEvent#options()
     * @see DeviceEvent#getOption(Device.ParameterName)
     * @see DeviceEvent#getDeviceHandle()
     * @see DeviceEvent#getDeviceName()
     * @see DeviceEvent#getVendor()
     * @see DeviceEvent#getDescription()
     */
    public static <H> AbstractDeviceEvent<H> of(Type eventType, DeviceEvent<H> parent) {
        final AbstractDeviceEvent<H> event = of(eventType);
        //
        // copying event's options
        parent.options().forEach(name -> event.option(name, parent.getOption(name).orElse(null)));
        //
        // copying the rest fields from parent
        return event
                .deviceHandle(parent.getDeviceHandle())
                .deviceName(parent.getDeviceName())
                .vendor(parent.getVendor())
                .description(parent.getDescription());
    }

    /**
     * <builder>
     * The builder of the incoming device event instance from previous device event (parent event type is ignoring)
     *
     * @param parent the parent of new event instance
     * @return built device event instance
     * @param <H> the type of device's handle (for low-level operations)
     * @see #of(Type, DeviceEvent)
     * @see Type#INCOMING
     */
    public static <H> DeviceEvent<H> incoming(final DeviceEvent<H> parent) {
        return of(INCOMING, parent);
    }

    /**
     * <builder>
     * The builder of the malfunction device event instance from previous device event (parent event type is ignoring)
     *
     * @param parent the parent of new event instance
     * @return built device event instance
     * @param <H> the type of device's handle (for low-level operations)
     * @see #of(Type, DeviceEvent)
     * @see Type#MALFUNCTION
     */
    public static <H> AbstractDeviceEvent<H> malfunction(final DeviceEvent<H> parent) {
        return of(MALFUNCTION, parent);
    }

    private AbstractDeviceEvent(Type eventType) {
        this.eventType = eventType;
    }

    /**
     * <accessor>
     * to get the type of event occurred for the device
     *
     * @return the value
     * @see Type
     */
    @Override
    public Type getEventType() {
        return eventType;
    }

    /**
     * <accessor>
     * To get the device's internal handle
     *
     * @return the value
     * @see Device.Session#getDeviceHandle()
     */
    @Override
    public H getDeviceHandle() {
        return deviceHandle;
    }

    public AbstractDeviceEvent<H> deviceHandle(H deviceHandle) {
        this.deviceHandle = deviceHandle;
        return this;
    }

    /**
     * <accessor>
     * to get the name of device, where the event has occurred
     *
     * @return the value
     * @see Device#getName()
     */
    @Override
    public String getDeviceName() {
        return deviceName;
    }

    public AbstractDeviceEvent<H> deviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    /**
     * <accessor>
     * get access to event device's vendor name
     *
     * @return vendor's name
     * @see Factory#getVendor()
     */
    @Override
    public String getVendor() {
        return vendor;
    }

    public AbstractDeviceEvent<H> vendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    /**
     * <accessor>
     * get access to the event's description
     *
     * @return event's description
     */
    @Override
    public String getDescription() {
        return description;
    }

    public AbstractDeviceEvent<H> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * <accessor>
     * To get event option value by name
     *
     * @param name the name of option's parameter
     * @return the value or empty
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOption(final Device.ParameterName name) {
        return Optional.ofNullable((T) parameters.get(name));
    }

    public <T> AbstractDeviceEvent<H> option(final Device.ParameterName name, final T value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * <accessor>
     * To get device event's options names
     *
     * @return the stream to options names
     * @see Device.ParameterName
     * @see Stream
     */
    @Override
    public Stream<Device.ParameterName> options() {
        return parameters.keySet().stream();
    }
}
