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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.visualcti.core.channel.Channel;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * The Factory of the Devices Adapter: The factory of the channel-devices
 *
 * @param <D> the type of factory's devices
 * @see Factory
 * @see RunnableUnitAdapter
 */
@SuppressWarnings("unchecked")
public class AbstractFactory<D extends Device<?>> extends RunnableUnitAdapter implements Factory<D> {
    // the executor for device events processing threads
    protected final transient Executor deviceEventExecutor;
    // the provider of device events
    protected final transient DeviceEvent.Provider eventsProvider;
    // predicate to test whether device event valid or not
    protected static final Predicate<DeviceEvent> isValidEvent =
            event -> event != null && isEmptyString.negate().test(event.getDeviceName());
    // the thread where events provider is running
    protected final AtomicReference<Thread> providerEventsThread = new AtomicReference<>(null);
    // the queue of device events from the events provider
    private final BlockingQueue<DeviceEvent> deviceEvents = new LinkedBlockingQueue<>();
    // how long system will wait for device event appearance (milliseconds)
    protected long howLongWaitForDeviceEvent = 100;
    // the listeners of factory's device events
    private final Map<String, List<DeviceEvent.Listener>> factoryEventListeners = new ConcurrentHashMap<>();
    // the holder of factory's device channels
    private final AtomicReference<Channel<D>[]> channelsHolder = new AtomicReference<>(new Channel[0]);

    protected AbstractFactory(Executor deviceEventExecutor, DeviceEvent.Provider eventsProvider) {
        this.deviceEventExecutor = deviceEventExecutor;
        this.eventsProvider = eventsProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractFactory)) return false;
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
     * <action>
     * To start the internal runnable parts of the unit
     * Grabbing the factory's devices and making channels for them
     *
     * @throws IOException if something went wrong during the internal parts starting
     * @see #Start()
     * @see #makeChannelFor(Device)
     */
    @Override
    public void startUnitRunnable() throws IOException {
        channelsHolder.getAndSet(devices().map(this::makeChannelFor).toArray(Channel[]::new));
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Clearing built in startUnitRunnable channels array
     *
     * @throws IOException if something went wrong during the internal parts stopping
     * @see #Stop()
     * @see #startUnitRunnable()
     */
    @Override
    public void stopUnitRunnable() throws IOException {
        channelsHolder.getAndSet(new Channel[0]);
    }

    /**
     * <builder>
     * To make the channel for device
     *
     * @param device channel to build for
     * @return built channel
     */
    protected Channel<D> makeChannelFor(Device<?> device) {
        throw new UnsupportedOperationException("Not supported here. Please implement it in the descendent.");
    }

    /**
     * <mutator>
     * To set up the current state of unit (active/passive/broken)
     *
     * @param unitState new value of unit state
     * @see UnitState
     */
    @Override
    public void currentUnitState(UnitState unitState) {
        super.currentUnitState(unitState);
        // dealing with event dispatching factory's thread
        switch (unitState) {
            case ACTIVE:
                dispatchEvent("Starting device event processing.");
                startingDeviceEventProcessing();
                break;
            case PASSIVE:
                dispatchEvent("Stopping device event processing.");
                stoppingDeviceEventProcessing();
                break;
            default:
                break;
        }
    }

    /**
     * <producer>
     * To grab device events from the device event provider abd put them to device events queue
     *
     * @see DeviceEvent
     * @see DeviceEvent.Provider#getEvent(long)
     * @see #processing(DeviceEvent)
     */
    protected void grabProviderEvents() {
        providerEventsThread.getAndSet(Thread.currentThread());
        while (isStarted()) {
            eventsProvider.getEvent(howLongWaitForDeviceEvent).ifPresent(this::processing);
        }
    }

    /**
     * <action>
     * To get and process device's events
     *
     * @see #isStarted()
     * @see DeviceEvent
     * @see #takeDeviceEvent()
     * @see #notifyListeners(DeviceEvent)
     * @see org.visualcti.server.core.unit.ServerUnit#dispatchError(Throwable, String)
     */
    protected void processingDeviceEvents() {
        while (isStarted()) {
            try {
                final DeviceEvent deviceEvent = takeDeviceEvent();
                if (!isStarted() || DeviceEvent.EMPTY.equals(deviceEvent)) {
                    // factory stopped or end of events queue is reached, stop the events processing
                    return;
                } else if (isValidEvent.test(deviceEvent)) {
                    // pooled event is correct, notify listeners
                    // processing the device-event through device's events' listeners
                    notifyListeners(deviceEvent);
                } else if (isThereNoUnprocessedEvents() && !takingTheBreak()) {
                    // for incorrect pooled device-event with started factory taking the little break
                    //  which could be interrupted outside
                    return;
                }
            } catch (InterruptedException e) {
                super.dispatchError(e, "Cannot pool provider event");
            }
        }
    }

    private boolean takingTheBreak() {
        try {
            TimeUnit.MILLISECONDS.sleep(howLongWaitForDeviceEvent);
            return true;
        } catch (InterruptedException e) {
            super.dispatchError(e, "Empty received device-event's sleep interrupted");
            return false;
        }
    }

    /**
     * <action>
     * Putting grabbed provider's device-event for further processing
     *
     * @param event device-event for the processing
     * @see #grabProviderEvents()
     * @see #dispatchError(String)
     */
    protected void processing(DeviceEvent event) {
        if (!deviceEvents.offer(event)) {
            Tools.error("Can't add event " + event);
            dispatchError("Cannot put the device-event to the queue");
        }
    }

    /**
     * <checker>
     * To check is there any unprocessed event there
     *
     * @return true if events queue is empty
     * @see #processingDeviceEvents()
     */
    protected boolean isThereNoUnprocessedEvents() {
        return deviceEvents.isEmpty();
    }

    /**
     * <action>
     * Taking one device event from events queue
     *
     * @return device event or null
     * @throws InterruptedException taking process was interrupted
     * @see #processingDeviceEvents()
     * @see BlockingQueue#poll(long, TimeUnit)
     */
    protected DeviceEvent takeDeviceEvent() throws InterruptedException {
        return deviceEvents.poll(howLongWaitForDeviceEvent, TimeUnit.MILLISECONDS);
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
        return channelsHolder.get();
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
        }
    }

    // stopping the events processing
    private void stoppingDeviceEventProcessing() {
        // putting end os queue marker
        processing(DeviceEvent.EMPTY);
        // stopping provider events thread
        if (providerEventsThread.get() != null) {
            // stopping thread, maybe it in the native call
            providerEventsThread.get().interrupt();
            providerEventsThread.getAndSet(null);
        }
    }

    // getting events listeners fot the device
    private List<DeviceEvent.Listener> deviceListenersFor(final String deviceName) {
        return factoryEventListeners.computeIfAbsent(deviceName, name -> new LinkedList<>());
    }
}
