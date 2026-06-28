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
package org.visualcti.server.channel.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.server.unit.RunnableUnitAdapter;

/**
 * The Factory of the Devices Adapter: The factory of the channel-devices
 *
 * @param <D> the type of factory's devices
 * @see Factory
 * @see RunnableUnitAdapter
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDeviceFactory<D extends Device<?>> extends RunnableUnitAdapter implements Factory<D> {
    // the listeners of factory's device events
    private final Map<String, List<DeviceEvent.Listener>> factoryEventListeners = new ConcurrentHashMap<>();

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AbstractDeviceFactory)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * <accessor>
     * get access to factory's vendor name
     *
     * @return vendor's name
     * @see Factory#getName()
     */
    @Override
    public String getVendor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <accessor>
     * get access to factory's version
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <aceessor>
     * to get the array of available factory's channels
     *
     * @return the array of available channels
     * @see Channel
     */
    @Override
    public Channel<D>[] channels() {
        return new Channel[0];
    }

    /**
     * <aceessor>
     * to get the event listener of the device
     *
     * @param deviceName the name of device in the factory
     * @return the stream of the listeners of the device with name
     * @see #addDeviceEventListenerFor(String, DeviceEvent.Listener)
     * @see #removeDeviceEventListenerFor(String, DeviceEvent.Listener)
     * @see DeviceEvent.Listener
     * @see Stream
     */
    @Override
    public Stream<DeviceEvent.Listener> eventListeners(String deviceName) {
        return deviceListenersFor(deviceName).stream();
    }

    /**
     * <mutator>
     * To add device events listener for particular device's events
     *
     * @param deviceName the name of device to listen events from
     * @param listener   the listener instance
     * @see DeviceEvent.Listener
     */
    @Override
    public void addDeviceEventListenerFor(String deviceName, DeviceEvent.Listener listener) {
        final List<DeviceEvent.Listener> deviceListeners = deviceListenersFor(deviceName);
        if (!deviceListeners.contains(listener)) {
            deviceListeners.add(listener);
        }
    }

    /**
     * <mutator>
     * To remove device events listener for particular device's events
     *
     * @param deviceName the name of device to listen events from
     * @param listener   the listener instance
     * @see DeviceEvent.Listener
     */
    @Override
    public void removeDeviceEventListenerFor(String deviceName, DeviceEvent.Listener listener) {
        deviceListenersFor(deviceName).remove(listener);
    }

    /**
     * <event-action>
     * To notify all device event listener
     *
     * @param event the event to notify
     */
    protected void notifyListeners(DeviceEvent event) {
        final boolean notified = deviceListenersFor(event.getDeviceName()).stream()
                .map(listener -> listener.accept(event))
                .reduce(true, (oldValue, newValue) -> oldValue && newValue);
        if (!notified) {
            dispatchError("Event rejected :" + event);
            reject(event);
        }
    }

    /**
     * <event-action>
     * To reject the event for the device
     *
     * @param event the event to notify
     */
    protected void reject(DeviceEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /// private methods
    private List<DeviceEvent.Listener> deviceListenersFor(final String deviceName) {
        return factoryEventListeners.computeIfAbsent(deviceName, name -> new LinkedList<>());
    }
}
