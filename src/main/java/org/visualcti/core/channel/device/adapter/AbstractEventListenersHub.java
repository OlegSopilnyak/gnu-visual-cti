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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;

/**
 * Events Listeners Hub: The hub of the native device's event listeners
 */
public class AbstractEventListenersHub implements DeviceEvent.Listener.Hub {
    // the listeners of factory's device events
    private final Map<String, List<DeviceEvent.Listener>> factoryEventListeners = new ConcurrentHashMap<>();

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
     * @return true if added well
     * @see DeviceEvent.Listener
     * @see Device#getName()
     */
    @Override
    public boolean addDeviceEventListenerFor(String deviceName, DeviceEvent.Listener listener) {
        final List<DeviceEvent.Listener> deviceListeners = deviceListenersFor(deviceName);
        return (listener != null && !deviceListeners.contains(listener)) && deviceListeners.add(listener);
    }

    /**
     * <mutator>
     * To remove device events listener for particular device's events
     *
     * @param deviceName the name of device to listen events from
     * @param listener   the listener instance
     * @return true if removed well
     * @see DeviceEvent.Listener
     * @see Device#getName()
     */
    @Override
    public boolean removeDeviceEventListenerFor(String deviceName, DeviceEvent.Listener listener) {
        return deviceListenersFor(deviceName).remove(listener);
    }

    //// private methods
    // getting events listeners fot the device
    private List<DeviceEvent.Listener> deviceListenersFor(final String deviceName) {
        return factoryEventListeners.computeIfAbsent(deviceName, name -> new LinkedList<>());
    }
}
