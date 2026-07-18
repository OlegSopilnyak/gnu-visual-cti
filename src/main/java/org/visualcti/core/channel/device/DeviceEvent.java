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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.visualcti.core.channel.device.operation.OperationResultValue;

/**
 * The event from the channel-device side
 *
 * @param <H> the type of device's handle (for low-level operations)
 * @see Device
 * @see Factory
 */
public interface DeviceEvent<H> {
    /**
     * Special Event: The event marker for the events queue
     */
    DeviceEvent<?> EMPTY = new DeviceEvent() {
        @Override
        public Type getEventType() {
            return null;
        }

        @Override
        public Object getDeviceHandle() {
            return null;
        }

        @Override
        public String getDeviceName() {
            return "";
        }

        @Override
        public String getVendor() {
            return "";
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public Map<String, Object> getOptions() {
            return Collections.emptyMap();
        }
    };

    /**
     * <accessor>
     * to get the type of event occurred for the device
     *
     * @return the value
     * @see Type
     */
    Type getEventType();

    /**
     * <accessor>
     * To get the device's internal handle
     *
     * @return the value
     * @see Device.Session#getDeviceHandle()
     */
    H getDeviceHandle();

    /**
     * <accessor>
     * to get the name of device, where the event has occurred
     *
     * @return the value
     * @see Device#getName()
     */
    String getDeviceName();

    /**
     * <accessor>
     * get access to event device's vendor name
     *
     * @return vendor's name
     * @see Factory#getVendor()
     */
    String getVendor();

    /**
     * <accessor>
     * get access to the event's description
     *
     * @return event's description
     */
    String getDescription();

    /**
     * <accessor>
     * get access to the event's options
     *
     * @return the options of the event
     */
    Map<String, Object> getOptions();

    /**
     * The types of device's events
     */
    enum Type {
        // received signal from device about incoming activity (incoming call, HTTP request, incoming message, etc.)
        INCOMING,
        // detected malfunction during device working activity
        MALFUNCTION,
        // channel-device specific event
        DEVICE_SPECIFIC
    }

    /**
     * EventListener: The listener of the channel device events
     */
    interface Listener {
        /**
         * <action>
         * Whether the given event is accepted by this listener.
         *
         * @param event the fired Event
         * @return true if the event accepted for the processing
         */
        boolean accept(DeviceEvent<?> event);

        /**
         * Events Listeners Hub: The hub of the native device's event listeners
         */
        interface Hub {
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
            Stream<DeviceEvent.Listener> eventListeners(String deviceName);

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
            boolean addDeviceEventListenerFor(String deviceName, DeviceEvent.Listener listener);


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
            boolean removeDeviceEventListenerFor(String deviceName, DeviceEvent.Listener listener);
        }
    }

    /**
     * EventsProvider: The native device's events provider
     *
     * @param <H> the type of device's handle (for low-level operations)
     */
    interface Provider<H> {
        /**
         * <action>
         * To get the device event from events provider during particular timeframe
         *
         * @param during time-frame for event's getting
         * @return detected event or empty
         * @see DeviceEvent
         * @see Optional
         * @see DeviceEventsProcessor#grabProviderEvents()
         */
        Optional<DeviceEvent<H>> getEvent(long during);

        /**
         * <action>
         * To enable particular type events producing for particular device from the events provider
         *
         *
         * @param deviceHandle device handle of the device for which events producing is enabled
         * @param eventType the type of events to enable
         * @see Device.Session#getDeviceHandle()
         * @see OperationResultValue
         */
        void enableEvents(H deviceHandle, OperationResultValue eventType);

        /**
         * <action>
         * To disable particular type events producing for particular device from the events provider
         *
         *
         * @param deviceHandle device handle of the device for which events producing is disabled
         * @param eventType the type of events to disable
         * @see Device.Session#getDeviceHandle()
         * @see OperationResultValue
         */
        void disableEvents(H deviceHandle, OperationResultValue eventType);

        /**
         * <action>
         * To disable ALL events producing for particular device from the events provider
         *
         *
         * @param deviceHandle device handle of the device for which events producing is disabled
         * @see Device.Session#getDeviceHandle()
         * @see DeviceEvent.Listener
         */
        void disableEvents(H deviceHandle);

        /**
         * <action>
         * To reject unprocessing device event
         *
         * @param event device event to reject
         */
        void reject(DeviceEvent<H> event);
    }
}
