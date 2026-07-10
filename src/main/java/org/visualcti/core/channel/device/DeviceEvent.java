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

/**
 * The event from the channel-device side
 *
 * @see Device
 * @see Factory
 */
public interface DeviceEvent {
    /**
     * Special Event: The event marker for the events queue
     */
    DeviceEvent EMPTY = new DeviceEvent() {
        @Override
        public Type getEventType() {
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
        boolean accept(DeviceEvent event);
    }

    /**
     * EventsProvider: The native device's events provider
     */
    interface Provider {
        /**
         * <action>
         * To get the device event from events provider during particular time-frame
         *
         * @param during time-frame for event's getting
         * @return detected event or empty
         * @see DeviceEvent
         * @see Optional
         */
        Optional<DeviceEvent> getEvent(long during);
    }
}
