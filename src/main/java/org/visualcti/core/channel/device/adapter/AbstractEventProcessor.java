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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * Events Processor: The native device's events producer and notifier adapter
 */
public class AbstractEventProcessor extends RunnableUnitAdapter implements DeviceEvent.Processor {
    // the queue of device events from the events provider
    private final BlockingQueue<DeviceEvent> deviceEvents = new LinkedBlockingQueue<>();
    // the thread where events provider is running
    protected final AtomicReference<Thread> providerEventsThread = new AtomicReference<>(null);
    // how long system will wait for device event appearance (milliseconds)
    protected transient long howLongWaitForDeviceEvent = 100;
    // the executor for device events processing threads
    protected final transient Executor deviceEventExecutor;
    // the provider of device events
    protected final transient DeviceEvent.Provider eventsProvider;
    // the provider of device events
    protected final transient DeviceEvent.Listener.Hub eventListenersHub;

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected AbstractEventProcessor(final Executor deviceEventExecutor,
                                     final DeviceEvent.Provider eventsProvider,
                                     final DeviceEvent.Listener.Hub eventListenersHub) {
        this.deviceEventExecutor = deviceEventExecutor;
        this.eventsProvider = eventsProvider;
        this.eventListenersHub = eventListenersHub;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractEventProcessor)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * <accessor>
     * To get access to the device's events provider
     *
     * @return the device's events provider reference
     */
    @Override
    public DeviceEvent.Provider getProvider() {
        return eventsProvider;
    }

    /**
     * <accessor>
     * To get access to the device's event listeners hub
     *
     * @return the device's event listeners hub reference
     */
    @Override
    public DeviceEvent.Listener.Hub getHub() {
        return eventListenersHub;
    }

    /**
     * <mutator>
     * To store current events grabber thread
     *
     * @param eventsGrabberThread current events grabber thread instance
     * @see #grabProviderEvents()
     */
    @Override
    public void eventsGrabberThread(final Thread eventsGrabberThread) {
        final Thread currentProviderEventsThread = providerEventsThread.get();
        if (eventsGrabberThread == null && currentProviderEventsThread != null) {
            // interrupting the working thread, maybe it in the native call
            currentProviderEventsThread.interrupt();
        }
        providerEventsThread.getAndSet(eventsGrabberThread);
    }

    /**
     * <accessor>
     * To get how long the processor will wait for device event appearance (milliseconds)
     *
     * @return the value
     * @see #grabProviderEvents()
     */
    @Override
    public long getHowLongWaitForDeviceEvent() {
        return howLongWaitForDeviceEvent;
    }

    /**
     * <action>
     * Sending grabbed provider's device-event for further processing
     *
     * @param event device-event for the processing
     * @see #grabProviderEvents()
     */
    @Override
    public void sendForProcessing(DeviceEvent event) {
        if (!deviceEvents.offer(event)) {
            Tools.error("Can't add event " + event);
            dispatchError("Cannot put the device-event to the queue");
        }
    }

    /**
     * <taker>
     * To take the device event grabbed and sent device event
     *
     * @return taken device event
     * @throws InterruptedException if thread is interrupted
     */
    @Override
    public DeviceEvent takeSentEvent() throws InterruptedException {
        return deviceEvents.poll(howLongWaitForDeviceEvent, TimeUnit.MILLISECONDS);
    }

    /**
     * <checker>
     * To check is there any unprocessed event there
     *
     * @return true if events queue is empty
     * @see #processingDeviceEvents()
     */
    @Override
    public boolean isThereNoUnprocessedEvents() {
        return deviceEvents.isEmpty();
    }

    /**
     * <action>
     * The processor is taking the little break
     *
     * @return the break is taken well
     * @see RunnableUnitAdapter#dispatchError(Throwable, String)
     */
    @Override
    public boolean isNotTakenTheBreak() {
        try {
            TimeUnit.MILLISECONDS.sleep(howLongWaitForDeviceEvent);
            return false;
        } catch (InterruptedException e) {
            super.dispatchError(e, "Empty received device-event's sleep interrupted");
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
            return true;
        }
    }

    /**
     * <mutator>
     * To set up the current state of unit (active/passive/broken)
     *
     * @param unitState new value of unit state
     * @see UnitState
     * @see RunnableUnitAdapter#currentUnitState(UnitState)
     * @see RunnableUnitAdapter#dispatchEvent(String)
     */
    @Override
    public void currentUnitState(UnitState unitState) {
        super.currentUnitState(unitState);
        // dealing with event dispatching factory's thread
        switch (unitState) {
            case ACTIVE:
                super.dispatchEvent("Starting device event processing.");
                // starting the device events processing
                startingDeviceEventProcessing();
                break;
            case PASSIVE:
                super.dispatchEvent("Stopping device event processing.");
                // stopping the device events processing
                stoppingDeviceEventProcessing();
                break;
            default:
                break;
        }
    }

    /// private methods
    // starting the events processing
    private void startingDeviceEventProcessing() {
        final CountDownLatch eventQueueLatch = new CountDownLatch(1);
        deviceEventExecutor.execute(() -> {
            // clearing the queue
            deviceEvents.clear();
            eventQueueLatch.countDown();
            processingDeviceEvents();
        });
        try {
            if (eventQueueLatch.await(howLongWaitForDeviceEvent, TimeUnit.MILLISECONDS)) {
                // processing events queue thread is started
                // run provider events grabbing
                deviceEventExecutor.execute(this::grabProviderEvents);
            } else {
                dispatchError("Cannot grab provider events");
                super.currentUnitState(UnitState.BROKEN);
            }
        } catch (InterruptedException e) {
            dispatchError(e, "Cannot grab provider events");
            super.currentUnitState(UnitState.BROKEN);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }

    // stopping the events processing
    private void stoppingDeviceEventProcessing() {
        // putting end os queue marker
        sendForProcessing(DeviceEvent.EMPTY);
        // stopping provider events thread
        eventsGrabberThread(null);
    }
}
