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
package org.visualcti.server.hardware.provider.javasound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Port;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device service provider of the telephony device features</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see org.visualcti.core.channel.telephony.TelephonyServiceProvider#openResource(String)
 * @see AbstractTelephonyServiceProvider
 */
@SuppressWarnings("unchecked")
public class SoundCardServiceProvider<H extends SoundCardHandle> extends AbstractTelephonyServiceProvider<H> {
    // the name of sound card device as a telephony device
    public static final String SOUND_DEVICE = "SoundCard";
    public static final String DEVICE_FACTORY_VENDOR = "JavaSound";
    // reference to the sound-card handle as singleton
    private static final AtomicReference<SoundCardHandle> handle = new AtomicReference<>(null);
    // the state of handset true = handset is off false = handset is on
    private final AtomicBoolean handsetOff = new AtomicBoolean(true);
    // reference to the sound-card phone number as singleton
    private final AtomicReference<PhoneCall.Number> callerID = new AtomicReference<>(PhoneCall.Number.EMPTY);
    // executor for scheduling device activities tasks
    private final ScheduledExecutorService scheduler;
    // map of device activity tasks
    private final Map<H, ScheduledFuture<?>> deviceActivity = new ConcurrentHashMap<>();

    public SoundCardServiceProvider(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Collection<String> allowedDevices() {
        return Collections.singleton(SOUND_DEVICE);
    }

    @Override
    protected H nativeResourceOpen(final String name) throws IOException {
        if (isOpened(name)) {
            throw new IOException("Device :" + name + ": is already opened");
        }
        return SOUND_DEVICE.equals(name) ? (H) soundCardResourceHandle() : SoundCardHandle.wrong();
    }

    @Override
    protected boolean isOpened(final H handle) {
        return super.isOpened(handle);
    }

    @Override
    protected boolean isValid(final H handle) {
        return super.isValid(handle) && handle.canUse();
    }

    @Override
    protected void nativeResourceClose(H handle) {
        // doing nothing here
        super.nativeResourceClose(handle);
    }

    @Override
    protected boolean isHandsetOff(H handle) {
        return isOpened(handle) && handsetOff.get();
    }

    @Override
    protected boolean nativeHandsetOff(H handle) {
        if (isOpened(handle)) {
            handsetOff.getAndSet(true);
            callerID.getAndSet(PhoneCall.Number.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public boolean canAcceptCall(final H handle) {
        return isOpened(handle);
    }

    @Override
    protected boolean nativeAnswerCall(H handle) {
        if (isOpened(handle) && handsetOff.get()) {
            handsetOff.getAndSet(false);
            return true;
        }
        return false;
    }

    @Override
    protected PhoneCall.Number nativeCallerID(H handle) {
        return isOpened(handle) ? callerID.get() : PhoneCall.Number.EMPTY;
    }

    public void callerID(final PhoneCall.Number phoneNumber) {
        this.callerID.getAndSet(phoneNumber);
    }

    @Override
    public boolean canMakeCall(final H handle) {
        return isOpened(handle);
    }

    @Override
    protected boolean nativeStartCalling(H handle, PhoneCall.Number number, int timeout) {
        return isOpened(handle) && number != null && number != PhoneCall.Number.EMPTY && timeout > 0;
    }

    @Override
    protected DeviceEvent<H> nativeGetEvent(long during) {
        return null;
    }

    @Override
    protected DeviceEvent<H> allowedEvent(final DeviceEvent<H> event) {
        return super.allowedEvent(event);
    }

    @Override
    protected void nativeEnableEvents(H deviceHandle, String eventType) {
        // doing nothing here yet
    }

    @Override
    protected void nativeDisableEvents(H deviceHandle, String eventType) {
        // doing nothing here yet
    }

    @Override
    protected void nativeEventRejected(H handle, DeviceEvent<H> event) {
        // doing nothing here yet
    }

    @Override
    public boolean canFax(final H handle) {
        return isOpened(handle);
    }

    @Override
    protected H nativeFaxResourceOpen(String name) {
        return SOUND_DEVICE.equals(name) ? (H) soundCardResourceHandle() : SoundCardHandle.wrong();
    }

    @Override
    protected void nativeFaxResourceClose(H handle) {
        // doing nothing here
        super.nativeFaxResourceClose(handle);
    }

    @Override
    protected boolean nativeStartFaxTransmitting(H handle, String filePath, boolean issueVoiceRequest,
                                                 boolean isTiff, boolean isHighResolution,
                                                 int firstPageNumber, int totalPages) {
        if (isOpened(handle) && Paths.get(filePath).toFile().exists()) {
            // emulating transmission starting, trowing the hardware error in 50 millis
            startActivity(handle, scheduler.schedule(
                    () -> putEvent(faxDeviceError(handle, "Started fax transmission")),
                    50, TimeUnit.MILLISECONDS)
            );
            return true;
        }
        return false;
    }

    @Override
    protected void nativeStopFaxTransmitting(H handle) {
        if (isOpened(handle) && hasShadowActivity(handle)) {
            // stopping the fax-operation
            putEvent(stopIt(handle, "Stopping fax transmission"));
            // cancelling current shadow activity associated with the given handle
            cancelActivity(handle);
        }
    }

    @Override
    protected boolean nativeStartFaxReceiving(H handle, String filePath, boolean issueVoiceRequest) {
        if (isOpened(handle) && Paths.get(filePath).toFile().exists()) {
            // emulating transmission starting, trowing the hardware error in 50 millis
            startActivity(handle, scheduler.schedule(
                    () -> putEvent(faxDeviceError(handle, "Started fax receiving")),
                    50, TimeUnit.MILLISECONDS)
            );
            return true;
        }
        return false;
    }


    @Override
    protected void nativeStopFaxReceiving(H handle) {
        if (isOpened(handle) && hasShadowActivity(handle)) {
            // stopping the fax-operation
            putEvent(stopIt(handle, "Stopping fax receiving"));
            // cancelling current shadow activity associated with the given handle
            cancelActivity(handle);
        }
    }

    /**
     * <accessor>
     * To check is there any shadow activity running for the given handle
     *
     * @param handle the handle of the opened resource (sound card device's handle)
     * @return true if there is any shadow activity running for the given handle
     */
    public boolean hasShadowActivity(final H handle) {
        final ScheduledFuture<?> currentActivity = deviceActivity.get(handle);
        return currentActivity != null && !currentActivity.isDone();
    }

    /// private methods
    // returns the singleton instance of SoundCardHandle.
    private static <H extends SoundCardHandle> H soundCardResourceHandle() {
        if (handle.get() != null) {
            return (H) handle.get();
        }
        synchronized (SoundCardHandle.class) {
            if (handle.get() == null) {
                handle.getAndSet(createHandle());
            }
        }
        return (H) handle.get();
    }

    private static SoundCardHandle createHandle() {
        final Line.Info[] microphones = AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE);
        final Line.Info source = microphones.length > 0 ? microphones[0] : null;
        final Line.Info[] speakers = AudioSystem.getTargetLineInfo(Port.Info.SPEAKER);
        final Line.Info target = speakers.length > 0 ? speakers[0] : null;
        return SoundCardHandle.of(source, target);
    }

    private static <H> DeviceEvent<H> stopIt(H handle, String description) {
        return SoundCardEvent.<H>of(DeviceEvent.Type.DEVICE_SPECIFIC).description(description)
                .deviceHandle(handle).deviceName(SOUND_DEVICE).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, Result.IO.EOF);
    }

    private static <H> DeviceEvent<H> faxDeviceError(H handle, String description) {
        return SoundCardEvent.<H>of(DeviceEvent.Type.MALFUNCTION).description(description)
                .deviceHandle(handle).deviceName(SOUND_DEVICE).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, (OperationResultValue) Result.FAX.COMPATIBILITY);
    }

    // to cancel any shadow activity running for the given handle
    private void cancelActivity(H handle) {
        final ScheduledFuture<?> currentActivity = deviceActivity.remove(handle);
        if (currentActivity != null && !currentActivity.isDone()) {
            currentActivity.cancel(true);
        }
    }

    // to make started the shadow activity for the given handle
    private void startActivity(H handle, ScheduledFuture<?> activity) {
        final ScheduledFuture<?> previousActivity = deviceActivity.put(handle, activity);
        if (previousActivity != null && !previousActivity.isDone()) {
            previousActivity.cancel(true);
        }
    }
}
