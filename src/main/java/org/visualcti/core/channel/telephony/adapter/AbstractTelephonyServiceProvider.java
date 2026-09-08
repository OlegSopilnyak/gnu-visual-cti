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
package org.visualcti.core.channel.telephony.adapter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceEventsProcessor;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.util.Tools;

/**
 * Provider Facade: The telephony service provider facade 'basic implementation'
 * (for the further manufacturer's implementation)
 *
 * @param <H> the type of the device's low-level operations handle
 * @see TelephonyServiceProvider
 */
public abstract class AbstractTelephonyServiceProvider<H> implements TelephonyServiceProvider<H> {
    // holder of the opened resource handlers by resource name
    private final Map<String, List<H>> openedResources = new ConcurrentHashMap<>();
    // holder of the enabled event-types by opened resource handlers
    private final Map<H, Set<OperationResultValue>> resourceEventTypes = new ConcurrentHashMap<>();
    // native events access lock
    private final Lock nativeEventsAccessLock = new ReentrantLock(true);
    // the blocking queue of the native events
    private final BlockingQueue<DeviceEvent<H>> nativeEvents = new LinkedBlockingQueue<>();

    /**
     * <action>
     * To open the device-related resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if the channel's resource cannot be opened or activated
     * @see Device#getName()
     * @see DeviceActivitySession#getDeviceHandle()
     * @see #nativeResourceOpen(String)
     */
    @Override
    public H openResource(final String name) throws IOException {
        final H handle = nativeResourceOpen(name);
        if (isValid(handle)) {
            // adding handle to the opened resource handlers
            openedResources.compute(name,
                    (resourceName, handlesList) -> handlesList == null ? new LinkedList<>() : handlesList
            ).add(handle);
        }
        return handle;
    }

    /**
     * <accessor>
     * Checks whether the provided handle is valid.
     *
     * @param handle the handle to be validated
     * @return true if the handle is valid, false otherwise
     */
    protected boolean isValid(final H handle) {
        return handle != null;
    }

    /**
     * <native-call>
     * To open the device-related resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if the channel's resource cannot be opened or activated
     * @see #openResource(String)
     */
    protected H nativeResourceOpen(final String name) throws IOException {
        return null;
    }

    /**
     * <accessor>
     * To check is the resource opened successfully
     *
     * @param handle the connected telephony device handle
     * @return true if resource was opened
     * @see #openResource(String)
     * @see #closeResource(H)
     */
    protected boolean isOpened(final H handle) {
        return openedResources.values().stream().flatMap(Collection::stream)
                .distinct().anyMatch(h -> h.equals(handle));
    }

    /**
     * <accessor>
     * To check is the resource with name already opened successfully
     *
     * @param name the name of the telephony device
     * @return true if the resource was already opened
     * @see #openResource(String)
     * @see #closeResource(H)
     */
    protected boolean isOpened(final String name) {
        final List<H> handles = openedResources.get(name);
        return handles != null && !handles.isEmpty();
    }

    /**
     * <acessor>
     * To find any handler for the resource by name
     *
     * @param name the name of the opened resource
     * @return handle to opened resource or empty
     * @see Optional
     * @see #openResource(String)
     */
    @Override
    public Optional<H> handleByName(String name) {
        final List<H> handles = openedResources.get(name);
        return handles != null && !handles.isEmpty() ? Optional.ofNullable(handles.get(0)) : Optional.empty();
    }

    /**
     * <action>
     * To close the device-related resource
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @see DeviceActivitySession#getDeviceHandle()
     * @see #nativeResourceClose(H)
     */
    @Override
    public void closeResource(final H handle) {
        resourcesByHandle(handle).ifPresent(resourceEntry -> {
            final String deviceName = resourceEntry.getKey();
            // removing closed resource handle from device's list of the opened handles
            final List<H> handles = resourceEntry.getValue().stream()
                    .filter(this::isValid).filter(h -> !Objects.equals(h, handle))
                    .collect(Collectors.toList());
            // closing resource natively
            nativeResourceClose(handle);
            // dealing with opened resource map-entry
            if (handles.isEmpty()) {
                // there is no any opened handle associated with device name
                openedResources.remove(deviceName);
            } else {
                // there is an opened handle associated with device name
                openedResources.put(deviceName, handles);
            }
        });
    }

    /**
     * <native-call>
     * To close the device-related resource (device's implementation)
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @see #closeResource(H)
     */
    protected void nativeResourceClose(H handle) {
        // doing nothing here
    }

    /**
     * <action>
     * To end up (handset off) the phone call.
     *
     * @param handle the telephony device handle
     * @return true if the operation completed successfully or device with handle is already disconnected
     * @see CallsPortEngine#dropCall(PhoneCallSession)
     * @see #nativeHandsetOff(H)
     */
    @Override
    public boolean handsetOff(H handle) {
        return resourcesByHandle(handle)
                .map(entry -> isHandsetOff(handle) || nativeHandsetOff(handle))
                .orElse(false);
    }

    /**
     * <native-call>
     * To end up (handset off) the phone call.
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @return true if the operation completed successfully
     * @see #handsetOff(H)
     */
    protected boolean nativeHandsetOff(H handle) {
        // doing nothing here
        return false;
    }

    /**
     * <native-call>
     * To check is the phone call's handset off.
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @return true if the phone call's handset off (call isn't active)
     * @see #handsetOff(H)
     */
    protected boolean isHandsetOff(H handle) {
        // doing nothing here
        return true;
    }

    /**
     * <action>
     * To answer to an incoming phone call.
     *
     * @param handle the telephony device handle
     * @return true if the operation completed successfully
     * @see CallsPortEngine#waitForCall(PhoneCallSession, int, int, boolean)
     * @see #nativeAnswerCall(H)
     */
    @Override
    public boolean answerCall(H handle) {
        return resourcesByHandle(handle)
                .map(resourceEntry -> nativeAnswerCall(handle))
                .orElse(false);
    }

    /**
     * <native-call>
     * To answer to an incoming phone call.
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @return true if the operation completed successfully
     * @see #answerCall(H)
     */
    protected boolean nativeAnswerCall(H handle) {
        // doing nothing here
        return false;
    }

    /**
     * <accessor>
     * To get the caller's phone number
     *
     * @param handle the connected telephony device handle
     * @return caller's phone number value
     * @see #nativeCallerID(H)
     */
    @Override
    public PhoneCall.Number getCallerID(H handle) {
        return resourcesByHandle(handle)
                .map(resourceEntry -> nativeCallerID(handle))
                .orElse(PhoneCall.Number.EMPTY);
    }

    /**
     * <native-call>
     * To get the caller's phone number
     *
     * @param handle the connected telephony device handle
     * @return caller's phone number value
     * @see #getCallerID(H)
     */
    protected PhoneCall.Number nativeCallerID(H handle) {
        return PhoneCall.Number.EMPTY;
    }

    /**
     * <action>
     * To start making the outgoing phone call
     *
     * @param handle  the telephony device handle
     * @param number  the called phone number
     * @param timeout the maximum waiting time for the answer (sec) to the outgoing call
     * @return true if the operation started successfully
     * @see CallsPortEngine#makeCall(PhoneCallSession, PhoneCall.Number, int)
     * @see #nativeStartCalling(Object, PhoneCall.Number, int)
     */
    @Override
    public boolean startCalling(H handle, PhoneCall.Number number, int timeout) {
        return resourcesByHandle(handle)
                .map(resourceEntry -> nativeStartCalling(handle, number, timeout))
                .orElse(false);
    }

    /**
     * <native-call>
     * To answer to an incoming phone call.
     *
     * @param handle  the handle of the opened resource (device's implementation)
     * @param number  the called phone number
     * @param timeout the maximum waiting time for the answer (sec) from outgoing call side
     * @return true if the operation completed successfully
     * @see #startCalling(H, PhoneCall.Number, int)
     */
    protected boolean nativeStartCalling(H handle, PhoneCall.Number number, int timeout) {
        // doing nothing here
        return false;
    }

    /**
     * <action>
     * To get the device event from events native during a particular timeframe
     *
     * @param during time-frame for event's getting (milliseconds)
     * @return detected event or empty
     * @see DeviceEvent
     * @see Optional
     * @see DeviceEventsProcessor#grabProviderEvents()
     * @see #nativeGetEvent(long)
     */
    @Override
    public Optional<DeviceEvent<H>> getEvent(final long during) {
        return Optional.ofNullable(safeEvent(() -> {
            if (during < 0) {
                // wrong timeout value
                return null;
            }
            // calculating timeout for native event polling and native event getting
            final long timeout = during / 2;
            final DeviceEvent<H> nativeEvent;
            try {
                nativeEvent = nativeEvents.poll(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Tools.error("Interrupted while waiting for native event");
                e.printStackTrace(Tools.err);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                return null;
            }
            // according to polled event, whether it is null or not, we return allowed device-event
            // or allowed device-event getting from the native
            return allowedEvent(nativeEvent == null ? nativeGetEvent(timeout) : nativeEvent);
        }));
    }

    /**
     * <checker>
     * To check the event's allowance and return it if allowed
     *
     * @param event the event to check
     * @return the input event if it's allowed or null otherwise
     */
    protected DeviceEvent<H> allowedEvent(final DeviceEvent<H> event) {
        if (event == null) {
            // event is null
            return null;
        }
        // analyzing the event's device handle
        final H deviceHandle = event.getDeviceHandle();
        if (isOpened(deviceHandle)) {
            // analyzing the reason of the event's occurrence to decide whether to return the event or not
            return event.<OperationResultValue>getOption(DeviceEvent.Option.REASON)
                    .filter(allowedEventTypesFor(deviceHandle)::contains)
                    .map(reason -> event)
                    .orElse(null);
        } else {
            // event's handle of device ain't opened
            return null;
        }

    }

    /**
     * <action>
     * To put the device event for further processing
     * used for special device events or for regular device events (from native)
     *
     * @param deviceEvent device event for further processing
     * @return true if the event was put successfully
     */
    public boolean putEvent(final DeviceEvent<H> deviceEvent) {
        return safeEvent(() -> nativeEvents.offer(deviceEvent));
    }

    /**
     * <native-call>
     * To get the device event from the events provider during a particular timeframe
     *
     * @param during time-frame for event's getting (milliseconds)
     * @return detected event or empty
     * @see DeviceEvent
     * @see #getEvent(long)
     */
    protected DeviceEvent<H> nativeGetEvent(long during) {
        return null;
    }

    /**
     * <action>
     * To enable particular type events producing for the particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is enabled
     * @param eventType    the type of events to enable
     * @see DeviceActivitySession#getDeviceHandle()
     * @see OperationResultValue
     * @see #enableEvent(H, OperationResultValue)
     * @see #nativeEnableEvents(H, String)
     */
    @Override
    public void enableEvents(H deviceHandle, OperationResultValue eventType) {
        resourcesByHandle(deviceHandle).ifPresent(e -> enableEvent(deviceHandle, eventType));
    }

    /**
     * <accessor>
     * To get event types enabled for the opened resource
     *
     * @param deviceHandle the handle to the opened resource
     * @return the set of enabled event types for the resource
     */
    protected Set<OperationResultValue> enabledEventTypes(H deviceHandle) {
        return resourcesByHandle(deviceHandle)
                .map(e -> resourceEventTypes.getOrDefault(deviceHandle, Collections.emptySet()))
                .orElse(Collections.emptySet());
    }

    /**
     * <native-call>
     * To enable particular type events producing for particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is enabled
     * @param eventType    the type of events to enable
     * @see #enableEvents(H, OperationResultValue)
     */
    protected void nativeEnableEvents(H deviceHandle, String eventType) {
        // doing nothing here yet
    }

    /**
     * <action>
     * To disable particular type events producing for particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is disabled
     * @param eventType    the type of events to disable
     * @see DeviceActivitySession#getDeviceHandle()
     * @see OperationResultValue
     * @see #nativeDisableEvents(H, String)
     */
    @Override
    public void disableEvents(H deviceHandle, OperationResultValue eventType) {
        resourcesByHandle(deviceHandle).ifPresent(e -> disableEvent(deviceHandle, eventType));
    }

    /**
     * <native-call>
     * To disable particular type events producing for particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is enabled
     * @param eventType    the type of events to enable
     * @see #disableEvents(H, OperationResultValue)
     */
    protected void nativeDisableEvents(H deviceHandle, String eventType) {
        // doing nothing here yet
    }

    /**
     * <action>
     * To reject unprocessing device event
     *
     * @param event device event to reject
     */
    @Override
    public void reject(DeviceEvent<H> event) {
        // doing nothing here yet
    }

    /// private methods
    // to look for map-entry contains the handle value in opened resources map
    private Optional<Map.Entry<String, List<H>>> resourcesByHandle(final H handle) {
        return openedResources.entrySet().stream()
                .filter(resourceEntry -> resourceEntry.getValue().contains(handle))
                .findFirst();
    }

    // to get the set of allowed event types for the opened resource by handle
    private Set<OperationResultValue> allowedEventTypesFor(final H handle) {
        return resourceEventTypes.getOrDefault(handle, Collections.emptySet());
    }

    // enabling events type for the opened resource by handle
    private void enableEvent(final H handle, final OperationResultValue type) {
        final Set<OperationResultValue> enabledEventTypes = new HashSet<>(
                resourceEventTypes.compute(handle, (k, v) -> v == null ? new HashSet<>() : v)
        );
        if (!enabledEventTypes.contains(type) && enabledEventTypes.add(type)) {
            // put the updated set of enabled event types back into the map
            resourceEventTypes.put(handle, enabledEventTypes);
            // notify about the enabled event type
            nativeEnableEvents(handle, type.getValue());
        }
    }

    // disabling events type for the opened resource by handle
    private void disableEvent(final H handle, final OperationResultValue type) {
        final Set<OperationResultValue> enabledEventTypes = new HashSet<>(allowedEventTypesFor(handle));
        if (type == EventType.ALL || enabledEventTypes.remove(type)) {
            if (enabledEventTypes.isEmpty() || type == EventType.ALL) {
                // there are no more allowed event types for the handle, or the disabling of ALL events has been requested
                resourceEventTypes.remove(handle);
            } else {
                // put the updated set of enabled event types back into the map
                resourceEventTypes.put(handle, enabledEventTypes);
            }
            nativeDisableEvents(handle, type.getValue());
        }
    }

    // to do operation with native events safely
    private <T> T safeEvent(final Supplier<T> eventSupplier) {
        try {
            nativeEventsAccessLock.lock();
            return eventSupplier.get();
        } finally {
            nativeEventsAccessLock.unlock();
        }
    }
}
