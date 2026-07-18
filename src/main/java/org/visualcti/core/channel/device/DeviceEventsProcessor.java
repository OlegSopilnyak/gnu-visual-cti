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

import java.util.function.Predicate;
import org.visualcti.server.core.unit.RunnableServerUnit;

/**
 * EventsProcessor: The native device's events processor
 *
 * @param <H> the type of device's handle (for low-level operations)
 */
public interface DeviceEventsProcessor<H> extends RunnableServerUnit {
    // predicate to test whether device event valid or not
    Predicate<DeviceEvent<?>> VALID_DEVICE_EVENT =
            deviceEvent -> deviceEvent != null && isEmptyString.negate().test(deviceEvent.getDeviceName());

    /**
     * <accessor>
     * To get access to the device's events provider
     *
     * @return the device's events provider reference
     */
    DeviceEvent.Provider<H> getProvider();

    /**
     * <accessor>
     * To get access to the device's event listeners hub
     *
     * @return the device's event listeners hub reference
     */
    DeviceEvent.Listener.Hub getHub();

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
    void sendForProcessing(DeviceEvent<H> event);

    /**
     * <event-action>
     * To notify all device event listeners
     *
     * @param event the event to notify the listener
     * @see #getHub()
     * @see DeviceEvent#getDeviceName()
     * @see DeviceEvent.Listener.Hub#eventListeners(String)
     * @see DeviceEvent.Listener#accept(DeviceEvent)
     * @see DeviceEvent.Provider#reject(DeviceEvent)
     * @see #processingDeviceEvents()
     */
    default void notifyListeners(final DeviceEvent<H> event) {
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
    DeviceEvent<H> takeSentEvent() throws InterruptedException;

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
                final DeviceEvent<H> deviceEvent = takeSentEvent();
                if (!isStarted() || DeviceEvent.EMPTY.equals(deviceEvent)) {
                    // factory stopped or end of events queue is reached, stop the events processing
                    return;
                } else if (VALID_DEVICE_EVENT.test(deviceEvent)) {
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
