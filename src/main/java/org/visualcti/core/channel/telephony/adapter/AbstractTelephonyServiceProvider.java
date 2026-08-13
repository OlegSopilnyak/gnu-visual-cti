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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceEventsProcessor;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;

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

    /**
     * <action>
     * To open the device related resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if channel's resource cannot be opened or activated
     * @see Device#getName()
     * @see Device.Session#getDeviceHandle()
     * @see #nativeResourceOpen(String)
     */
    @Override
    public H openResource(String name) throws IOException {
        final H handle = nativeResourceOpen(name);
        if (handle != null) {
            // adding handle to the opened resource handlers
            openedResources.compute(name,
                    (resourceName, handlesList) -> handlesList == null ? new LinkedList<>() : handlesList
            ).add(handle);
        }
        return handle;
    }

    /**
     * <native-call>
     * To open the device related resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if channel's resource cannot be opened or activated
     * @see #openResource(String)
     */
    protected H nativeResourceOpen(String name) throws IOException {
        return null;
    }


    /**
     * <accessor>
     * To check is resource was opened successfully
     *
     * @param handle the connected telephony device handle
     * @return true if resource was opened
     * @see #openResource(String)
     * @see #closeResource(H)
     */
    protected boolean isOpened(H handle) {
        return openedResources.values().stream().flatMap(Collection::stream)
                .distinct().anyMatch(h -> h.equals(handle));
    }

    /**
     * <action>
     * To open the device related resource
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @throws IOException if channel's resource cannot be closed
     * @see Device.Session#getDeviceHandle()
     * @see #nativeResourceClose(H)
     */
    @Override
    public void closeResource(H handle) throws IOException {
        resourcesByHandle(handle).ifPresent(resourceEntry -> {
            final String deviceName = resourceEntry.getKey();
            final List<H> handles = new ArrayList<>(resourceEntry.getValue());
            // closing resource natively
            nativeResourceClose(handle);
            // removing closed resource handle from device's handles list
            handles.remove(handle);
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
     * To close the device related resource (device's implementation)
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
     * @return true if operation complete successfully or device with handle is already disconnected
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
     * @return true if operation complete successfully
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
     * @return true if operation complete successfully
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
     * @return true if operation complete successfully
     * @see #answerCall(Object)
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
     * @param timeout the maximum waiting time for the answer (sec) to outgoing call
     * @return true if operation started successfully
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
     * @return true if operation complete successfully
     * @see #startCalling(H, PhoneCall.Number, int)
     */
    protected boolean nativeStartCalling(H handle, PhoneCall.Number number, int timeout) {
        // doing nothing here
        return false;
    }

    /**
     * <action>
     * To get the device event from events native during particular timeframe
     *
     * @param during time-frame for event's getting
     * @return detected event or empty
     * @see DeviceEvent
     * @see Optional
     * @see DeviceEventsProcessor#grabProviderEvents()
     * @see #nativeGetEvent(long)
     */
    @Override
    public Optional<DeviceEvent<H>> getEvent(long during) {
        return Optional.ofNullable(nativeGetEvent(during));
    }

    /**
     * <native-call>
     * To get the device event from events provider during particular timeframe
     *
     * @param during time-frame for event's getting
     * @return detected event or empty
     * @see DeviceEvent
     * @see #getEvent(long)
     */
    protected DeviceEvent<H> nativeGetEvent(long during) {
        return null;
    }

    /**
     * <action>
     * To enable particular type events producing for particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is enabled
     * @param eventType    the type of events to enable
     * @see Device.Session#getDeviceHandle()
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
     * To get event types enabled for te opened resource
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
     * @see Device.Session#getDeviceHandle()
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
    private Optional<Map.Entry<String, List<H>>> resourcesByHandle(H handle) {
        return openedResources.entrySet().stream()
                .filter(resourceEntry -> resourceEntry.getValue().contains(handle))
                .findFirst();
    }

    // enabling events type for the opened resource by handle
    private void enableEvent(H handle, OperationResultValue type) {
        final Set<OperationResultValue> enabledEventTypes = new HashSet<>(
                resourceEventTypes.compute(handle, (k, v) -> v == null ? new HashSet<>() : v)
        );
        if (!enabledEventTypes.contains(type) && enabledEventTypes.add(type)) {
            resourceEventTypes.put(handle, enabledEventTypes);
            nativeEnableEvents(handle, type.getValue());
        }
    }

    // disabling events type for the opened resource by handle
    private void disableEvent(H handle, OperationResultValue type) {
        final Set<OperationResultValue> enabledEventTypes = new HashSet<>(
                resourceEventTypes.compute(handle, (k, v) -> v == null ? new HashSet<>() : v)
        );
        if (type == EventType.ALL || (enabledEventTypes.contains(type) && enabledEventTypes.remove(type))) {
            if (enabledEventTypes.isEmpty() || type == EventType.ALL) {
                resourceEventTypes.remove(handle);
            } else {
                resourceEventTypes.put(handle, enabledEventTypes);
            }
            nativeDisableEvents(handle, type.getValue());
        }
    }
}
