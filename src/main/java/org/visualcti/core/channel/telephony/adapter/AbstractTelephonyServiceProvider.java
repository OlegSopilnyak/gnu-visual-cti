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
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceEventsProcessor;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.media.Audio;
import org.visualcti.media.Fax;
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
     * @see TelephonyServiceProvider#openResource(String)
     */
    @Override
    public H openResource(final String name) throws IOException {
        return internalResourceOpen(name, this::nativeResourceOpen);
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
    protected H nativeResourceOpen(String name) throws IOException {
        return null;
    }

    /**
     * <accessor>
     * Checks whether the provided handle is valid.
     *
     * @param handle the handle to be validated
     * @return true if the handle is valid, false otherwise
     */
    protected boolean isValid(H handle) {
        return handle != null;
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
    protected boolean isOpened(H handle) {
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
    protected boolean isOpened(String name) {
        final List<H> handles = openedResources.get(name);
        return handles != null && !handles.isEmpty();
    }

    /**
     * <acessor>
     * To find any handler for the resource by name
     *
     * @param name the name of the opened resource
     * @return handle to opened resource or empty
     * @see TelephonyServiceProvider#handleByName(String)
     * @see Optional
     */
    @Override
    public Optional<H> handleByName(final String name) {
        final List<H> handles = openedResources.get(name);
        return handles != null && !handles.isEmpty() ? Optional.ofNullable(handles.get(0)) : Optional.empty();
    }

    /**
     * <action>
     * To close the device-related resource
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @see DeviceActivitySession#getDeviceHandle()
     * @see TelephonyServiceProvider#closeResource(H)
     */
    @Override
    public void closeResource(final H handle) {
        internalCloseResource(handle, this::nativeResourceClose);
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
     * @return true if the operation completed successfully or the device with the handle is already disconnected
     * @see TelephonyDevice#dropCall(PhoneCallSession)
     * @see TelephonyServiceProvider#handsetOff(H)
     */
    @Override
    public boolean handsetOff(final H handle) {
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
     * @see TelephonyDevice#waitForCall(PhoneCallSession, int, int, boolean)
     * @see TelephonyServiceProvider#answerCall(H)
     */
    @Override
    public boolean answerCall(final H handle) {
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
     * @see TelephonyServiceProvider#getCallerID(H)
     */
    @Override
    public PhoneCall.Number getCallerID(final H handle) {
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
     * @see TelephonyDevice#makeCall(PhoneCallSession, PhoneCall.Number, int)
     * @see TelephonyServiceProvider#startCalling(H, PhoneCall.Number, int)
     */
    @Override
    public boolean startCalling(final H handle, final PhoneCall.Number number, final int timeout) {
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
     * @param timeout the maximum waiting time for the answer (sec) from the outgoing call side
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
     * @see TelephonyServiceProvider#getEvent(long)
     */
    @Override
    public Optional<DeviceEvent<H>> getEvent(final long during) {
        return Optional.ofNullable(safeEventOperation(() -> {
            if (during < 0) {
                // wrong timeout value
                return null;
            }
            // calculating timeout for native event polling and native event getting
            final long timeout = during / 2;
            final DeviceEvent<H> nativeDeviceEvent;
            try {
                nativeDeviceEvent = nativeEvents.poll(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Tools.error("Interrupted while waiting for native event");
                e.printStackTrace(Tools.err);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                return null;
            }
            // according to polled event, whether it is null or not, we return allowed device-event
            // or allowed device-event getting from the native
            return allowedEvent(nativeDeviceEvent == null ? nativeGetEvent(timeout) : nativeDeviceEvent);
        }));
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
     * <checker>
     * To check the event's allowance and return it if allowed
     *
     * @param event the event to check
     * @return the input event if it's allowed or null otherwise
     * @see #getEvent(long)
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
        return safeEventOperation(() -> nativeEvents.offer(deviceEvent));
    }

    /**
     * <action>
     * To enable particular type events producing for the particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is enabled
     * @param eventType    the type of events to enable
     * @see DeviceActivitySession#getDeviceHandle()
     * @see OperationResultValue
     * @see TelephonyServiceProvider#enableEvents(H, OperationResultValue)
     */
    @Override
    public void enableEvents(final H deviceHandle, final OperationResultValue eventType) {
        resourcesByHandle(deviceHandle).ifPresent(e -> internalEnableEventsFor(deviceHandle, eventType));
    }

    /**
     * <native-call>
     * To enable particular type events producing for the particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing is enabled
     * @param eventType    the type of events to enable
     * @see #enableEvents(H, OperationResultValue)
     */
    protected void nativeEnableEvents(H deviceHandle, String eventType) {
        // doing nothing here yet
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
     * <action>
     * To disable particular type events producing for the particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing are disabled
     * @param eventType    the type of events to disable
     * @see DeviceActivitySession#getDeviceHandle()
     * @see OperationResultValue
     * @see TelephonyServiceProvider#disableEvents(H, OperationResultValue)
     */
    @Override
    public void disableEvents(final H deviceHandle, final OperationResultValue eventType) {
        resourcesByHandle(deviceHandle).ifPresent(e -> internalDisableEventsFor(deviceHandle, eventType));
    }

    /**
     * <native-call>
     * To disable particular type events producing for the particular device from the events provider
     *
     * @param deviceHandle device handle of the device for which events producing are enabled
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
     * @see DeviceEventsProcessor#notifyListeners(DeviceEvent)
     * @see TelephonyServiceProvider#reject(DeviceEvent)
     */
    @Override
    public void reject(final DeviceEvent<H> event) {
        final H handle = event.getDeviceHandle();
        resourcesByHandle(handle).ifPresent(e -> nativeEventRejected(handle, event));
    }

    /**
     * <native-call>
     * To reject unprocessing device event
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @param event  device event to reject
     * @see #reject(DeviceEvent)
     */
    protected void nativeEventRejected(H handle, DeviceEvent<H> event) {
        // doing nothing here yet
    }

    /**
     * <action>
     * To open the device-related fax-resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if the channel's fax resource cannot be opened or activated
     * @see org.visualcti.core.channel.telephony.part.FaxMachineEngine#open(PhoneCallSession)
     * @see TelephonyServiceProvider#openFaxResource(String)
     */
    @Override
    public H openFaxResource(String name) throws IOException {
        return internalResourceOpen(name, this::nativeFaxResourceOpen);
    }

    /**
     * <native-call>
     * To open the device-related fax-resource (device's implementation)
     *
     * @param name the name of the resource
     * @return handle for the opened resource
     * @throws IOException if the channel's resource cannot be opened or activated
     * @see #openFaxResource(String)
     */
    protected H nativeFaxResourceOpen(String name) throws IOException {
        return null;
    }

    /**
     * <action>
     * To close the device-related fax-resource
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @see org.visualcti.core.channel.telephony.part.FaxMachineEngine#close(PhoneCallSession)
     * @see TelephonyServiceProvider#closeFaxResource(H)
     */
    @Override
    public void closeFaxResource(final H handle) {
        internalCloseResource(handle, this::nativeFaxResourceClose);
    }

    /**
     * <native-call>
     * To close the device-related fax-resource (device's implementation)
     *
     * @param handle the handle of the opened resource (device's implementation)
     * @see #closeFaxResource(H)
     */
    protected void nativeFaxResourceClose(H handle) {
        // doing nothing here
    }

    /**
     * <action>
     * To start receiving the fax document
     *
     * @param handle            the telephony device handle
     * @param filePath          the path to the file for the receiving fax document content
     * @param issueVoiceRequest upon termination of receiver to give out a
     *                          sound signal on the remote fax-device
     * @return true if the operation started successfully
     * @see TelephonyDevice#receive(PhoneCallSession, OutputStream, boolean, boolean)
     * @see TelephonyServiceProvider#startFaxReceiving(H, String, boolean)
     */
    @Override
    public boolean startFaxReceiving(final H handle, final String filePath, final boolean issueVoiceRequest) {
        return nativeStartFaxReceiving(handle, filePath, issueVoiceRequest);
    }

    /**
     * <native-call>
     * To start receiving the fax document
     *
     * @param handle            the telephony device handle
     * @param filePath          the path to the file for the receiving fax document content
     * @param issueVoiceRequest upon termination of receiver to give out a
     *                          sound signal on the remote fax-device
     * @return true if the operation started successfully
     * @see #startFaxReceiving(H, String, boolean)
     */
    protected boolean nativeStartFaxReceiving(H handle, String filePath, boolean issueVoiceRequest) {
        return isOpened(handle) && filePath != null && !filePath.trim().isEmpty();
    }

    /**
     * <action>
     * To stop (interrupt) receiving the fax document
     *
     * @param handle the telephony device handle
     * @see TelephonyDevice#transmit(PhoneCallSession, InputStream, Fax, boolean)
     * @see TelephonyServiceProvider#stopFaxReceiving(H)
     */
    @Override
    public void stopFaxReceiving(final H handle) {
        nativeStopFaxReceiving(handle);
    }

    /**
     * <native-call>
     * To stop (interrupt) receiving the fax document
     *
     * @param handle the telephony device handle
     * @see #stopFaxReceiving(H)
     */
    protected void nativeStopFaxReceiving(H handle) {
        // doing nothing here
    }

    /**
     * <action>
     * To start transmitting the fax document
     *
     * @param handle            the telephony device handle
     * @param filePath          the path to the file, fax document content
     * @param issueVoiceRequest upon termination of receiver to give out a
     *                          sound signal on the remote fax-device
     * @param isTiff            the parameter of transmitting document page
     * @param isHighResolution  the parameter of transmitting document page
     * @param firstPageNumber   transmit from page
     * @param totalPages        transmit pages (negative value means all available pages)
     *                          sound signal on the remote fax-device
     * @return true if the operation started successfully
     * @see TelephonyDevice#transmit(PhoneCallSession, InputStream, Fax, boolean)
     * @see TelephonyServiceProvider#startFaxTransmitting(H, String, boolean, boolean, boolean, int, int)
     */
    @Override
    public boolean startFaxTransmitting(final H handle, final String filePath, final boolean issueVoiceRequest,
                                        final boolean isTiff, final boolean isHighResolution,
                                        final int firstPageNumber, final int totalPages) {
        return nativeStartFaxTransmitting(
                handle, filePath, issueVoiceRequest, isTiff, isHighResolution, firstPageNumber, totalPages
        );
    }

    /**
     * <native-call>
     * To start transmitting the fax document
     *
     * @param handle            the telephony device handle
     * @param filePath          the path to the file, fax document content
     * @param issueVoiceRequest upon termination of receiver to give out a
     *                          sound signal on the remote fax-device
     * @param isTiff            the parameter of transmitting document page
     * @param isHighResolution  the parameter of transmitting document page
     * @param firstPageNumber   transmit from page
     * @param totalPages        transmit pages (negative value means all available pages)
     *                          sound signal on the remote fax-device
     * @return true if the operation started successfully
     * @see #startFaxTransmitting(H, String, boolean, boolean, boolean, int, int)
     */
    protected boolean nativeStartFaxTransmitting(H handle, String filePath, boolean issueVoiceRequest,
                                                 boolean isTiff, boolean isHighResolution,
                                                 int firstPageNumber, int totalPages) {
        return isOpened(handle) && filePath != null && !filePath.trim().isEmpty();
    }

    /**
     * <action>
     * To stop (interrupt) transmitting the fax document
     *
     * @param handle the telephony device handle
     * @see TelephonyDevice#transmit(PhoneCallSession, InputStream, Fax, boolean)
     * @see TelephonyServiceProvider#stopFaxTransmitting(H)
     */
    @Override
    public void stopFaxTransmitting(final H handle) {
        nativeStopFaxTransmitting(handle);
    }

    /**
     * <native-call>
     * To stop (interrupt) transmitting the fax document
     *
     * @param handle the telephony device handle
     * @see #stopFaxTransmitting(H)
     */
    protected void nativeStopFaxTransmitting(H handle) {
        // doing nothing here
    }

    /**
     * <action>
     * To start playing media from the temporary file with the particular media format
     *
     * @param handle   the telephony device handle
     * @param filePath the path to the file which contents the media data
     * @param format   parameter determining the type of the decoder for transformation the sound data
     * @param timeout  maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return true if the operation started successfully
     * @see TelephonyDevice#playbackAudio(PhoneCallSession, InputStream, Audio, String, int)
     * @see TelephonyServiceProvider#startAudioPlaying(H, String, Audio, int)
     */
    @Override
    public boolean startAudioPlaying(H handle, String filePath, Audio format, int timeout) {
        return nativeStartAudioPlaying(handle, filePath, format, timeout);
    }

    /**
     * <native-call>
     * To start playing media from the temporary file with the particular media format
     *
     * @param handle   the telephony device handle
     * @param filePath the path to the file which contents the media data
     * @param format   parameter determining the type of the decoder for transformation the sound data
     * @param timeout  maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return true if the operation started successfully
     * @see #startAudioPlaying(H, String, Audio, int)
    */
    protected boolean nativeStartAudioPlaying(H handle, String filePath, Audio format, int timeout) {
        return isOpened(handle) && filePath != null && !filePath.trim().isEmpty();
    }

    /**
     * <action>
     * To stop (interrupt) playing media
     *
     * @param handle the telephony device handle
     * @see TelephonyServiceProvider#stopAudioPlaying(H)
     */
    @Override
    public void stopAudioPlaying(H handle) {
        TelephonyServiceProvider.super.stopAudioPlaying(handle);
    }

    /**
     * <action>
     * To start recording media to the temporary file with the particular media format
     *
     * @param handle   the telephony device handle
     * @param filePath the path to the file which contents the media data
     * @param format   parameter determining the type of the decoder for transformation the sound data
     * @param silence  time (seconds) how long silence in a line is allowed, after which the record operation will be finished.
     * @param timeout  maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return true if the operation started successfully
     * @see TelephonyDevice#recordAudio(PhoneCallSession, OutputStream, Audio, String, int, int)
     * @see TelephonyServiceProvider#startAudioRecording(H, String, Audio, int, int)
     */
    @Override
    public boolean startAudioRecording(H handle, String filePath, Audio format, int silence, int timeout) {
        return TelephonyServiceProvider.super.startAudioRecording(handle, filePath, format, silence, timeout);
    }

    /**
     * <action>
     * To stop (interrupt) playing media
     *
     * @param handle the telephony device handle
     * @see TelephonyServiceProvider#stopAudioRecording(H)
     */
    @Override
    public void stopAudioRecording(H handle) {
        TelephonyServiceProvider.super.stopAudioRecording(handle);
    }

    /// private methods
    @FunctionalInterface
    private interface NativeResourceOpen<T, R> {
        R apply(T t) throws IOException;
    }

    // common used resource's open method
    private H internalResourceOpen(final String name, final NativeResourceOpen<String, H> nativeOpen) throws IOException {
        final H handle = nativeOpen.apply(name);
        if (isValid(handle)) {
            // adding handle to the opened resource handlers
            openedResources.compute(name,
                    (resourceName, handlesList) -> handlesList == null ? new LinkedList<>() : handlesList
            ).add(handle);
        }
        return handle;
    }

    // common used resource's open method
    private void internalCloseResource(H handle, Consumer<H> nativeClose) {
        final Optional<Map.Entry<String, List<H>>> handleEntry = resourcesByHandle(handle);
        if (handleEntry.isPresent()) {
            final Map.Entry<String, List<H>> resourceEntry = handleEntry.get();
            final String deviceName = resourceEntry.getKey();
            // removing closed resource handle from device's list of the opened handles
            final List<H> handles = resourceEntry.getValue().stream()
                    .filter(this::isValid).filter(h -> !Objects.equals(h, handle))
                    .collect(Collectors.toList());
            // closing resource natively
            nativeClose.accept(handle);
            // dealing with opened resource map-entry
            if (handles.isEmpty()) {
                // there is no any opened handle associated with device name
                openedResources.remove(deviceName);
            } else {
                // there is an opened handle associated with device name
                openedResources.put(deviceName, handles);
            }
        }
    }

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
    private void internalEnableEventsFor(final H handle, final OperationResultValue type) {
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
    private void internalDisableEventsFor(final H handle, final OperationResultValue type) {
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
    private <T> T safeEventOperation(final Supplier<T> operation) {
        try {
            nativeEventsAccessLock.lock();
            return operation.get();
        } finally {
            nativeEventsAccessLock.unlock();
        }
    }
}
