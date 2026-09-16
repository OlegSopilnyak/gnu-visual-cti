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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.TelephonyTone;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;

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
    // the size of buffer that is using for media-transmitting operations
    private static final int BUFFER_SIZE = 2048;

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
            if (format != null) {
                if (AudioSystem.isLineSupported(new DataLine.Info(DataLine.class, format))) {
                    formats.add(audio);
                }
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
    protected boolean nativeStartAudioPlaying(H handle, String filePath, Audio format, int timeout) {
        final File audioFile = Paths.get(filePath).toFile();
        if (isOpened(handle) && audioFile.exists() && handle.getSource() != null) {
            // starting playing back the file in the separate thread
            scheduler.schedule(() -> nativePlayingBackAudioFile(handle, audioFile), 0, TimeUnit.MILLISECONDS);
            // stopping playing when the playing timeout is reached
            postponedActivity(handle, scheduler.schedule(() -> {
                // removing the source line from the handle (to stop playing the audio loop)
                handle.setSourceLine(null);
                // removing postponed activity for the handle
                deviceActivity.remove(handle);
            }, timeout, TimeUnit.SECONDS));
            return true;
        } else {
            // didn't start playing
            return false;
        }
    }

    @Override
    protected void nativeStopAudioPlaying(H handle) {
        // getting the playback channel line from the handle instance
        final SourceDataLine playbackChannel = handle.getSourceLine();
        // checking is there alive playback channel line
        if (playbackChannel != null) {
            // stopping the playing back on the channel and closing it
            playbackChannel.stop();
            playbackChannel.close();
        }
        // removing the playback channel line from the handle
        handle.setSourceLine(null);
        // cancelling current shadow activity associated with the given handle
        cancelPostponedActivity(handle);
    }

    @Override
    protected boolean nativeStartAudioRecording(H handle, String filePath, Audio format, int silence, int timeout) {
        final File audioFile = Paths.get(filePath).toFile();
        if (isOpened(handle) && audioFile.exists() && handle.getTarget() != null) {
            // starting recording input audio to the file in the separate thread
            scheduler.schedule(() -> nativeRecordingAudioToFile(handle, audioFile, format), 0, TimeUnit.MILLISECONDS);
            // stopping record when the recording timeout is reached
            postponedActivity(handle, scheduler.schedule(() -> {
                // removing the target line from the handle (to stop capturing the audio loop)
                handle.setTargetLine(null);
                // removing postponed activity for the handle
                deviceActivity.remove(handle);
            }, timeout, TimeUnit.SECONDS));
            return true;
        } else {
            // didn't start recording
            return false;
        }
    }

    @Override
    protected void nativeStopAudioRecording(H handle) {
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
        // cancelling postponed activity associated with the given handle
        cancelPostponedActivity(handle);
    }

    @Override
    protected void nativeDialingDtmf(H handle, String toDial) {
        if (handle.getSource() != null && toDial != null && !toDial.trim().isEmpty()) {
            final ByteArrayOutputStream finalBuffer = new ByteArrayOutputStream();
            final Consumer<byte[]> concatBuffers = buffer -> {
                try {
                    finalBuffer.write(buffer);
                } catch (IOException e) {
                    // doing nothing here
                }
            };
            try {
                // composing buffer from DTMF string
                toDial.chars().mapToObj(c -> DTMF.get((char) c)).filter(Objects::nonNull)
                        .map(SoundCardServiceProvider::simpleToneBuffer).forEach(concatBuffers);
                finalBuffer.flush();
                finalBuffer.close();
            } catch (IOException e) {
                Tools.error("Error during dialing DTMF :" + toDial);
                e.printStackTrace(Tools.err);
                return;
            }
            // playing composed buffer
            playToneFrom(finalBuffer.toByteArray());
        }
    }

    // the container of DTMF tones
    private static final Map<Character, TelephonyTone> DTMF = new ConcurrentHashMap<>();

    static {
        DTMF.put('1', makeDtmfTone(697, 1209));
        DTMF.put('2', makeDtmfTone(697, 1336));
        DTMF.put('3', makeDtmfTone(697, 1477));
        DTMF.put('4', makeDtmfTone(770, 1209));
        DTMF.put('5', makeDtmfTone(770, 1336));
        DTMF.put('6', makeDtmfTone(770, 1477));
        DTMF.put('7', makeDtmfTone(852, 1209));
        DTMF.put('8', makeDtmfTone(852, 1336));
        DTMF.put('9', makeDtmfTone(852, 1477));
        DTMF.put('*', makeDtmfTone(941, 1209));
        DTMF.put('0', makeDtmfTone(941, 1336));
        DTMF.put('#', makeDtmfTone(941, 1477));
    }

    private static TelephonyTone makeDtmfTone(int lowFreqHz, int highFreqHz) {
        final TelephonyTone tone = new TelephonyTone(ToneId.DTMF);
        tone.getPrimary().setFrequencyHz(lowFreqHz);
        tone.getSecondary().setFrequencyHz(highFreqHz);
        tone.getDuration().setOnTime(200);
        return tone;
    }

    // to make raw audio data for the tone's playing
    private static byte[] simpleToneBuffer(TelephonyTone tone) {
        final int duration = tone.getDuration().getOnTime() < 0 ? 200 : tone.getDuration().getOnTime();
        if (tone.getSecondary().getFrequencyHz() == 0) {
            return singleTonesBuffer(tone.getPrimary().getFrequencyHz(), duration);
        } else {
            return dualTonesBuffer(tone.getPrimary().getFrequencyHz(), tone.getSecondary().getFrequencyHz(), duration);
        }
    }

    // to make raw audio data for dual tones playing
    private static byte[] dualTonesBuffer(int lowFreqHz, int highFreqHz, int durationMs) {
        final int numSamples = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        final byte[] buffer = new byte[numSamples * 2]; // 16-bit PCM (2 bytes per sample)

        for (int i = 0; i < numSamples; i++) {
            double angle1 = 2.0 * Math.PI * lowFreqHz * i / SAMPLE_RATE;
            double angle2 = 2.0 * Math.PI * highFreqHz * i / SAMPLE_RATE;

            // Combine both sine waves and scale amplitude to avoid clipping
            double sample = 0.5 * (Math.sin(angle1) + Math.sin(angle2));
            short val = (short) (sample * Short.MAX_VALUE);

            // Write little-endian 16-bit PCM sample into byte buffer
            buffer[2 * i] = (byte) (val & 0x00ff);
            buffer[2 * i + 1] = (byte) ((val & 0xff00) >>> 8);
        }
        return buffer;
    }

    // to make raw audio data for single tone playing
    private static byte[] singleTonesBuffer(int hz, int durationMs) {
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples * 2]; // 16-bit PCM, 2 bytes per sample

        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * hz / SAMPLE_RATE;
            short sample = (short) (Math.sin(angle) * 10000); // Scale amplitude

            // Low byte
            buffer[2 * i] = (byte) (sample & 0xFF);
            // High byte
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return buffer;
    }

    // to play tone from the buffer
    private void playToneFrom(byte[] buffer) {
        if (buffer != null && buffer.length > 0) {
            try {
                final AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                final SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected boolean nativeStartToneSending(H handle, ToneId toneId) {
        return super.nativeStartToneSending(handle, toneId);
    }

    @Override
    protected void nativeStopToneSending(H handle) {
        super.nativeStopToneSending(handle);
    }

    @Override
    protected void nativeBeginToneRegistering(H handle) {
        super.nativeBeginToneRegistering(handle);
    }

    @Override
    protected void nativeRegisterTone(H handle, TelephonyTone tone) {
        super.nativeRegisterTone(handle, tone);
    }

    @Override
    protected void nativeCommitToneRegistering(H handle) {
        super.nativeCommitToneRegistering(handle);
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
        nativeSetResourceParameter(handle, ALLOWED_CODECS,
                ConfigurationParameter.of(ALLOWED_CODECS.value(), availableFormatsFor(handle))
        );
        return handle;
    }

    // starting fax activity
    private boolean nativeStartFax(H handle, String filePath, String errorReason) {
        if (isOpened(handle) && Paths.get(filePath).toFile().exists()) {
            // preparing the hardware error event putting
            final Runnable activity = () -> {
                putEvent(faxDeviceError(handle, errorReason));
                deviceActivity.remove(handle);
            };
            // postpone hardware error trowing in 50 millis
            postponedActivity(handle, scheduler.schedule(activity, 50, TimeUnit.MILLISECONDS));
            return true;
        } else {
            // wasn't start operation
            return false;
        }
    }

    // playing back the audio file using handle's source line
    private void nativePlayingBackAudioFile(final H handle, final File audioFile) {
        try (final AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile)) {
            final AudioFormat audioFormat = audioStream.getFormat();
            final SourceDataLine channel = AudioSystem.getSourceDataLine(audioFormat);
            //
            // adjusting source line listener
            channel.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    // Tools.print("--- Stopped playing file: " + audioFile.getName());
                    // removing the source line from the handle (to stop capturing the audio loop)
                    handle.setSourceLine(null);
                }
            });
            // adjusting and starting the source data line channel
            channel.open(audioFormat);
            handle.setSourceLine(channel);
            channel.start();
            // playing back the audio
            playbackAudio(handle, audioStream, channel, audioFile);
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            // detaching the line from device's handle
            handle.setSourceLine(null);
            Tools.error("Failed to playback the audio");
            e.printStackTrace(Tools.err);
        } finally {
            handle.inProgress(false);
        }
    }

    // playing back the audio
    private void playbackAudio(H handle, AudioInputStream audioStream, SourceDataLine channel, File audioFile) throws IOException {
        //
        // playing back audio stuff preparation
        final byte[] buffer = new byte[BUFFER_SIZE];
        int bytesToPlay;
        // playing back audio operation is started
        handle.inProgress(true);
        // getting audio chunks from the file and playing them back
        while (handle.isSourceActive()) {
            // getting audio chunk from the file
            if ((bytesToPlay = audioStream.read(buffer, 0, buffer.length)) > 0) {
                // playing back the audio chunk through the started channel
                channel.write(buffer, 0, bytesToPlay);
            } else {
                break;
            }
        }
        final String message = "--- Finished playing file: " + audioFile.getName();
        // Tools.print(message);
        // audio playing operation is completed
        if (handle.isSourceActive()) {
            // the end of audio stream is reached, sending EOF event
            // putting the event about the end of file reached
            putEvent(stopIt(handle, "Audio recording...", Result.IO.EOF));
        } else
            // audio playing back is terminated outside
            if (channel.isActive()) {
                // timeout state applied outside
                // putting the operation timeout event
                putEvent(stopIt(handle, "Audio recording...", Result.TIMEOUT));
            } else {
                // audio playing back is stopped outside
                // putting the event about the end of file reached
                putEvent(stopIt(handle, "Audio recording...", Result.IO.EOF));
            }
        //
        // finalizing the audio playing if source data line is active
        if (channel.isActive()) {
            // Wait for buffer to empty before closing
            channel.drain();
            //
            // finishing up the channel's playback
            channel.stop();
            channel.close();
        }
    }

    // recording the audio to file using handle's target line
    private void nativeRecordingAudioToFile(final H handle, final File outputFile, final Audio format) {
        final AudioFormat audioFormat = format.toFormat();
        if (audioFormat == null) {
            Tools.error("Invalid audio format :" + format);
            return;
        }
        try {
            final TargetDataLine target = AudioSystem.getTargetDataLine(audioFormat);
            target.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    // Tools.print("--- Stopped recording to file: " + outputFile.getName());
                    // detaching the line from device's handle
                    handle.setTargetLine(null);
                }
            });
            // adjusting and starting the target data line channel
            target.open(audioFormat);
            handle.setTargetLine(target);
            target.start();
            // capturing the audio and save it to the file
            captureAudio(handle, outputFile, target, audioFormat);
        } catch (LineUnavailableException | IOException e) {
            // detaching the line from device's handle
            handle.setTargetLine(null);
            Tools.error("Failed to record the audio");
            e.printStackTrace(Tools.err);
        } finally {
            handle.inProgress(false);
        }
    }

    // capturing the audio
    private void captureAudio(H handle, File outputFile, TargetDataLine target, AudioFormat audioFormat) throws IOException {
        //
        // audio capturing operation stuff preparation
        final File tempRawAudioFile = Files.createTempFile("audio", ".rawdata").toFile();
        tempRawAudioFile.deleteOnExit();
        final byte[] buffer = new byte[BUFFER_SIZE];
        int bytesCaptured;
        try (final FileOutputStream out = new FileOutputStream(tempRawAudioFile)) {
            // audio capturing operation is started
            handle.inProgress(true);
            while (handle.isTargetActive()) {
                // getting audio chunk from the audio input
                if ((bytesCaptured = target.read(buffer, 0, buffer.length)) > 0) {
                    // saving chunk to the temporary file
                    out.write(buffer, 0, bytesCaptured);
                } else {
                    break;
                }
            }
            //
            // audio capturing operation is completed
            if (target.isActive()) {
                // there is audio data to capture
                // sending operation timeout event
                putEvent(stopIt(handle, "Audio recording...", Result.TIMEOUT));
                //
                // finalizing the audio capturing
                target.stop();
                target.close();
            } else {
                // audio capturing is stopped outside
                // putting the event about end of file reached state
                putEvent(stopIt(handle, "Audio recording...", Result.IO.EOF));
            }
        }
        // Tools.print("--- Finished recording to file: " + tempRawAudioFile.getName());
        // saving the audio recording result
        try (
                final FileInputStream fileIn = new FileInputStream(tempRawAudioFile);
                final AudioInputStream audioIn = new AudioInputStream(fileIn, audioFormat, tempRawAudioFile.length())
        ) {
            AudioSystem.write(audioIn, AudioFileFormat.Type.WAVE, outputFile);
        }
        //
        // cleaning the operation's stuff
        if (!tempRawAudioFile.delete()) {
            throw new IOException("Failed to delete temp file: " + tempRawAudioFile.getAbsolutePath());
        }
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
        return SoundCardEvent.<H>of(DeviceEvent.Type.DEVICE_SPECIFIC).description(description)
                .deviceHandle(handle).deviceName(SOUND_DEVICE).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, reason);
    }

    private static <H> DeviceEvent<H> faxDeviceError(H handle, String description) {
        return SoundCardEvent.<H>of(DeviceEvent.Type.MALFUNCTION).description(description)
                .deviceHandle(handle).deviceName(SOUND_DEVICE).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, (OperationResultValue) Result.FAX.COMPATIBILITY);
    }

    // to cancel any shadow activity running for the given handle
    private void cancelPostponedActivity(H handle) {
        final ScheduledFuture<?> currentActivity = deviceActivity.remove(handle);
        if (currentActivity != null && !currentActivity.isDone()) {
            currentActivity.cancel(true);
        }
    }

    // to make started the shadow activity for the given handle
    private void postponedActivity(H handle, ScheduledFuture<?> activity) {
        final ScheduledFuture<?> previousActivity = deviceActivity.put(handle, activity);
        if (previousActivity != null && !previousActivity.isDone()) {
            previousActivity.cancel(true);
        }
    }
}
