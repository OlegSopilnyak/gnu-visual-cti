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
package org.visualcti.workflow.hardware.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.TelephonyTone;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.io.audio.CaptureUtils;
import org.visualcti.workflow.hardware.javasound.io.audio.PlaybackUtils;
import org.visualcti.workflow.hardware.javasound.io.audio.ToneUtils;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device service provider of the telephony device features</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyServiceProvider
 * @see SoundCardHandle
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
    protected Collection<String> nativeAllowedDevices() {
        return Collections.singleton(SOUND_DEVICE);
    }

    @Override
    protected H nativeResourceOpen(final String name) throws IOException {
        if (isOpened(name)) {
            throw new IOException("Device :" + name + ": is already opened");
        }
        // registering codecs for the opened resource
        return registerResourceCodecs(SOUND_DEVICE.equals(name)
                // building the device's handle instance
                ? soundCardResourceHandle()
                // wrong handle instance
                : SoundCardHandle.wrong()
        );
    }

    @Override
    protected List<Audio> availableFormatsFor(H handle) {
        if (!isValid(handle)) {
            return Collections.emptyList();
        }
        // getting the available formats for the correctly opened resource
        final List<Audio> formats = new ArrayList<>(20);
        // iterating over the available audio formats
        for (final Audio audio : Audio.values()) {
            final AudioFormat format = audio.toFormat();
            if (format != null && AudioSystem.isLineSupported(new DataLine.Info(DataLine.class, format))) {
                formats.add(audio);
            }
        }
        // returning the available formats
        return formats;
    }

    @Override
    protected Optional<ConfigurationParameter> nativeFindResourceParameter(H handle, Device.ParameterName name) {
        return super.nativeFindResourceParameter(handle, name);
    }

    @Override
    protected boolean nativeSetResourceParameter(H handle, Device.ParameterName name, ConfigurationParameter parameter) {
        return super.nativeSetResourceParameter(handle, name, parameter);
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
        return nativeStartFax(handle, filePath, "Started fax transmission");
    }


    @Override
    protected void nativeStopFaxTransmitting(H handle) {
        if (isOpened(handle) && hasShadowActivity(handle)) {
            // stopping the fax-operation
            putEvent(stopIt(handle, "Stopping fax transmission"));
            // cancelling current shadow activity associated with the given handle
            cancelPostponedActivity(handle);
        }
    }

    @Override
    protected boolean nativeStartFaxReceiving(H handle, String filePath, boolean issueVoiceRequest) {
        return nativeStartFax(handle, filePath, "Started fax receiving");
    }


    @Override
    protected void nativeStopFaxReceiving(H handle) {
        if (isOpened(handle) && hasShadowActivity(handle)) {
            // stopping the fax-operation
            putEvent(stopIt(handle, "Stopping fax receiving"));
            // cancelling current shadow activity associated with the given handle
            cancelPostponedActivity(handle);
        }
    }

    @Override
    protected boolean isReadyToPlay(H handle) {
        return handle.getSource() != null;
    }

    @Override
    protected void asyncAudioFilePlaying(File audioFile, Audio format, H handle) {
        scheduler.schedule(() -> nativePlayingBackAudioFile(handle, audioFile), 0, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopAudioFilePlaying(H handle) {
        // removing the source line from the handle (will stop the loop of the audio playing)
        handle.setSourceLine(null);
        // removing postponed activity for the handle
        cancelPostponedActivity(handle);
    }

    @Override
    protected void nativeStopAudioFilePlaying(H handle) {
        // getting the playback channel line from the handle instance
        final SourceDataLine playbackChannel = handle.getSourceLine();
        // checking is there alive playback channel line
        if (playbackChannel != null) {
            // stopping the playing back on the channel and closing it
            playbackChannel.stop();
            playbackChannel.close();
        }
        // to stop playing back the audio file
        stopAudioFilePlaying(handle);
    }

    @Override
    protected boolean isReadyToRecord(H handle) {
        return handle.getTarget() != null;
    }

    @Override
    protected void asyncAudioFileRecording(Path targetFilePath, Audio format, H handle, int silence) {
        scheduler.schedule(
                () -> capturingAudioToFile(targetFilePath, format, handle), 0, TimeUnit.MILLISECONDS
        );
    }

    @Override
    protected void stopAudioFileRecording(H handle, OperationResultValue reason) {
        // completing audio data capturing
        CaptureUtils.completeCapturing(handle, () -> putEvent(stopIt(handle, AUDIO_RECORDING, reason)));
        // removing postponed activity for the handle
        deviceActivity.remove(handle);
    }

    @Override
    protected void nativeStopAudioFileRecording(H handle) {
        // getting the record channel line from the handle instance
        final TargetDataLine recordChannel = handle.getTargetLine();
        // checking is there alive record channel line
        if (recordChannel != null) {
            // stopping the recording on the channel and closing it
            recordChannel.stop();
            recordChannel.close();
        }
        // removing the target line from the handle (to stop capturing the audio loop)
        handle.setTargetLine(null);
        // waiting for completing of the audio recording operation
        CaptureUtils.waitForOperationComplete(handle);
        // cancelling postponed activity associated with the given handle
        cancelPostponedActivity(handle);
    }

    @Deprecated
    @Override
    protected void schedulePostponedAction(H handle, Runnable action, int runInSeconds) {
        super.schedulePostponedAction(handle, action, runInSeconds);
    }

    @Override
    protected void schedulePostponedAction(H handle, Runnable action, long after, TimeUnit unit) {
        schedulePostponedActivity(handle, action, runActionIn(after, unit));
    }

    /**
     * <action>
     * To prepare timeout device event runnable
     *
     * @param handle     the telephony device opened handle
     * @param actionName   the name of the action timeout will send for
     * @see #timeoutEventIn(H, String, long, TimeUnit)
     */
    @Override
    protected Runnable sendTimeoutEventFor(H handle, String actionName) {
        return () -> putEvent(stopIt(handle, actionName, Result.TIMEOUT));
    }

    @Override
    protected void nativeDialingDtmf(H handle, String toDial) {
        if (handle.getSource() != null && toDial != null && !toDial.trim().isEmpty()) {
            ToneUtils.dialingDtmf(toDial);
        }
    }

    @Override
    protected boolean nativeStartToneSending(H handle, TelephonyTone tone) {
        return super.nativeStartToneSending(handle, tone) && nativeStartTonePlaying(handle, tone);
    }

    @Override
    protected void nativeStopToneSending(H handle) {
        // calling native stop audio paying back method
        nativeStopAudioFilePlaying(handle);
    }

    @Override
    protected void nativeBeginToneRegistering(H handle) {
        Tools.print("=== Beginning tones registration for :" + handle);
    }

    @Override
    protected void nativeRegisterTone(H handle, TelephonyTone tone) {
        Tools.print("=== Registering tone :" + tone);
        Tools.print("=== For :" + handle);
    }

    @Override
    protected void nativeCommitToneRegistering(H handle) {
        Tools.print("=== Commiting tones registration for :" + handle);
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
    // registering the available codecs for the opened resource by the resource's handle
    private H registerResourceCodecs(H handle) {
        nativeSetResourceParameter(handle, ALLOWED_CODECS_NAME,
                ConfigurationParameter.of(ALLOWED_CODECS_NAME.value(), availableFormatsFor(handle))
        );
        return handle;
    }

    // starting fax activity
    private boolean nativeStartFax(H handle, String filePath, String errorReason) {
        if (isOpened(handle) && Paths.get(filePath).toFile().exists()) {
            // to postpone hardware-error event trowing in 50 millis
            schedulePostponedActivity(handle, () -> {
                putEvent(faxDeviceError(handle, errorReason));
                deviceActivity.remove(handle);
            }, runActionIn(50));
            return true;
        } else {
            // wasn't start operation
            return false;
        }
    }

    // playing back the audio file using handle's source line
    private void nativePlayingBackAudioFile(final H handle, final File audioFile) {
        // updater of audio playback operation result value
        final Consumer<OperationResultValue> operationResultUpdater =
                result -> putEvent(stopIt(handle, AUDIO_PLAYING, result));
        // To play back the audio file using handle's source line
        PlaybackUtils.playingBackAudioFile(handle, audioFile, operationResultUpdater);
    }

    // returns the singleton instance of SoundCardHandle.
    private static <H extends SoundCardHandle> H soundCardResourceHandle() {
        if (handle.get() != null) {
            return (H) handle.get();
        }
        synchronized (SoundCardHandle.class) {
            if (handle.get() == null) {
                handle.getAndSet(SoundCardHandle.of(supportedSourceDataLine(), supportedTargetDataLine()));
            }
        }
        return (H) handle.get();
    }

    private static DataLine.Info supportedSourceDataLine() {
        if (AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE).length > 0) {
            final DataLine.Info dataSource = new DataLine.Info(SourceDataLine.class, null);
            return AudioSystem.isLineSupported(dataSource) ? dataSource : null;
        } else {
            return null;
        }
    }

    private static DataLine.Info supportedTargetDataLine() {
        if (AudioSystem.getTargetLineInfo(Port.Info.SPEAKER).length > 0) {
            final DataLine.Info dataSource = new DataLine.Info(TargetDataLine.class, null);
            return AudioSystem.isLineSupported(dataSource) ? dataSource : null;
        } else {
            return null;
        }
    }

    private static <H> DeviceEvent<H> stopIt(H handle, String description) {
        return stopIt(handle, description, Result.IO.EOF);
    }

    private static <H> DeviceEvent<H> stopIt(H handle, String description, OperationResultValue reason) {
        return AbstractDeviceEvent.<H>of(DeviceEvent.Type.DEVICE_SPECIFIC).description(description)
                .deviceHandle(handle).deviceName(SOUND_DEVICE).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, reason);
    }

    private static <H> DeviceEvent<H> faxDeviceError(H handle, String description) {
        return AbstractDeviceEvent.<H>of(DeviceEvent.Type.MALFUNCTION).description(description)
                .deviceHandle(handle).deviceName(SOUND_DEVICE).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, (OperationResultValue) Result.FAX.COMPATIBILITY);
    }

    // to cancel any shadow (postponed) activity running for the given handle
    private void cancelPostponedActivity(H handle) {
        final ScheduledFuture<?> currentActivity = deviceActivity.remove(handle);
        if (currentActivity != null && !currentActivity.isDone()) {
            currentActivity.cancel(true);
        }
    }

    // to start tone's sending as audio playback
    private boolean nativeStartTonePlaying(H handle, TelephonyTone tone) {
        if (handle.getSource() != null) {
            // updater of audio playback operation result value
            final Consumer<OperationResultValue> operationResultUpdater =
                    result -> putEvent(stopIt(handle, TONE_PLAYING, result));
            //
            // starting playing back the tone in the separated thread
            scheduler.schedule(() -> ToneUtils.playingBackTone(handle, tone, operationResultUpdater),
                    0, TimeUnit.MILLISECONDS
            );
            return true;
        } else {
            // didn't start playing
            return false;
        }
    }

    // capturing audio data and save recorded data to the output file in the WAVE format
    private void capturingAudioToFile(Path outputFilePath, Audio format, H handle) {
        CaptureUtils.capturingAudioToFile(handle, outputFilePath, format);
        // cancelling postponed activity associated with the given handle
        cancelPostponedActivity(handle);
    }

    // to schedule postponed activity and register it for the given handle
    private void schedulePostponedActivity(final H handle, final Runnable activity, final RunActivityIn runIn) {
        final ScheduledFuture<?> previousActivity = deviceActivity.put(handle,
                scheduler.schedule(activity, runIn.delay, runIn.unit)
        );
        if (previousActivity != null && !previousActivity.isDone()) {
            previousActivity.cancel(true);
        }
    }

    // to build run-activity-in-delay instance delay & time-unit
    private static RunActivityIn runActionIn(long delay, TimeUnit unit) {
        return RunActivityIn.of(delay, unit);
    }

    // to build run-activity-in-delay instance delay in milliseconds
    private static RunActivityIn runActionIn(long delay) {
        return RunActivityIn.of(delay);
    }

    /// inner classes
    // scheduler postpone values parameters wrapper
    private static class RunActivityIn {
        final long delay;
        final TimeUnit unit;

        private RunActivityIn(long delay, TimeUnit unit) {
            this.delay = delay;
            this.unit = unit;
        }

        static RunActivityIn of(long delay, TimeUnit unit) {
            return new RunActivityIn(delay, unit);
        }

        static RunActivityIn of(long delay) {
            return new RunActivityIn(delay, TimeUnit.MILLISECONDS);
        }
    }
}
