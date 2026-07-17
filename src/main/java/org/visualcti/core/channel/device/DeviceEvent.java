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
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.visualcti.server.core.unit.RunnableServerUnit;

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

        /**
         * <action>
         * To reject unprocessing device event
         *
         * @param event device event to reject
         */
        void reject(DeviceEvent event);
    }

    /**
     * EventsProcessor: The native device's events processor
     */
    interface Processor extends RunnableServerUnit {
        // predicate to test whether device event valid or not
        Predicate<DeviceEvent> isValidEvent =
                event -> event != null && isEmptyString.negate().test(event.getDeviceName());
        /**
         * <accessor>
         * To get access to the device's events provider
         *
         * @return the device's events provider reference
         */
        Provider getProvider();

        /**
         * <accessor>
         * To get access to the device's event listeners hub
         *
         * @return the device's event listeners hub reference
         */
        Listener.Hub getHub();

        /**
         * <action>
         * To grab device events from the device event provider and put them somewhere for the event processing
         *
         * @see #getProvider()
         * @see #eventsGrabberThread(Thread)
         * @see #sendForProcessing(DeviceEvent)
         * @see DeviceEvent.Provider#getEvent(long)
         * @see RunnableServerUnit#isStarted()
         */
        default void grabProviderEvents() {
            eventsGrabberThread(Thread.currentThread());
            while (isStarted()) {
                getProvider().getEvent(getHowLongWaitForDeviceEvent()).ifPresent(this::sendForProcessing);
            }
        }

        /**
         * <mutator>
         * To store current events grabber thread
         *
         * @param eventsGrabberThread current events grabber thread instance
         * @see #grabProviderEvents()
         */
        void eventsGrabberThread(final Thread eventsGrabberThread);

        /**
         * <accessor>
         * To get how long the processor will wait for device event appearance (milliseconds)
         *
         * @return the value
         * @see #grabProviderEvents()
         */
        long getHowLongWaitForDeviceEvent();

        /**
         * <action>
         * Sending grabbed provider's device-event for further processing
         *
         * @param event device-event for the processing
         * @see #grabProviderEvents()
         */
        void sendForProcessing(DeviceEvent event);

        /**
         * <event-action>
         * To notify all device event listeners
         *
         * @param event the event to notify the listener
         * @see #getHub()
         * @see DeviceEvent#getDeviceName()
         * @see Listener.Hub#eventListeners(String)
         * @see Listener#accept(DeviceEvent)
         * @see Provider#reject(DeviceEvent)
         * @see #processingDeviceEvents()
         */
        default void notifyListeners(final DeviceEvent event) {
            final boolean notified = getHub().eventListeners(event.getDeviceName())
                    .map(listener -> listener.accept(event))
                    .reduce(true, (oldValue, newValue) -> oldValue && newValue);
            if (!notified) {
                dispatchError("Event rejected :" + event);
                getProvider().reject(event);
            }
        }

        /**
         * <taker>
         * To take the device event grabbed and sent device event
         *
         * @return taken device event
         * @throws InterruptedException if thread is interrupted
         */
        DeviceEvent takeSentEvent() throws InterruptedException;

        /**
         * <checker>
         * To check is there any unprocessed event there
         *
         * @return true if events queue is empty
         * @see #processingDeviceEvents()
         */
        boolean isThereNoUnprocessedEvents();

        /**
         * <action>
         * The processor is taking the little break
         *
         * @return the break is taken well
         */
        boolean isNotTakenTheBreak();

        /**
         * <action>
         * To get and process device's events
         *
         * @see #isStarted()
         * @see DeviceEvent
         * @see #takeSentEvent()
         * @see #isNotTakenTheBreak()
         * @see #notifyListeners(DeviceEvent)
         * @see #dispatchError(Throwable, String)
         */
        default void processingDeviceEvents() {
            while (isStarted()) {
                try {
                    final DeviceEvent deviceEvent = takeSentEvent();
                    if (!isStarted() || DeviceEvent.EMPTY.equals(deviceEvent)) {
                        // factory stopped or end of events queue is reached, stop the events processing
                        return;
                    } else if (isValidEvent.test(deviceEvent)) {
                        // pooled event is correct, notify listeners
                        // processing the device-event through device's events' listeners
                        notifyListeners(deviceEvent);
                    } else if (isThereNoUnprocessedEvents() && isNotTakenTheBreak()) {
                        // for incorrect pooled device-event with started factory taking the little break
                        //  which could be interrupted outside
                        return;
                    }
                } catch (InterruptedException e) {
                    dispatchError(e, "Cannot pool grabbed provider event");
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
