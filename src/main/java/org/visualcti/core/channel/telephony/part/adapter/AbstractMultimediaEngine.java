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
package org.visualcti.core.channel.telephony.part.adapter;

import static org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider.AUDIO_PLAYING;
import static org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider.AUDIO_RECORDING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Sound;
import org.visualcti.util.Tools;

/**
 * Adapter: The Part of the Telephony Channel Device: The root device part of the telephony multimedia (playback/record) management
 *
 * @param <H> the type of the telephony device's low-level operations handle
 * @see MultimediaEngine
 * @see AbstractDevicePart
 */
public abstract class AbstractMultimediaEngine<H> extends AbstractDevicePart<H> implements MultimediaEngine<H> {
    // predicate to check is operation in progress
    private static final Predicate<DeviceStateValue> isOperationInProgress =
            state -> state == TelephonyDevice.State.PLAY || state == TelephonyDevice.State.RECORD;
    // predicate to check is string is empty
    private static final Predicate<String> isStringEmpty = str -> str == null || str.trim().isEmpty();
    // predicate for audio playing correct operation result completion
    private static final Predicate<OperationResultValue> isPlayingCompleted = result ->
            result == Result.TIMEOUT || result == Result.IO.EOF;
    // predicate for audio recording correct operation result completion
    private static final Predicate<OperationResultValue> isRecordCompleted = result ->
            result == Result.TIMEOUT || result == Result.IO.EOF || result == Result.IO.SILENCE;
    // the IO buffer capacity
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * <accessor>
     * Returns the array of supported audio formats(codecs) for playing back,
     * empty array if playback is not supported
     * The codecs will be loading during telephony device session starting process
     *
     * @return the array of the supported playback formats supported by device or empty array if device can't play back
     * @see TelephonyDevice#canPlay()
     * @see TelephonyDevice#startSession()
     * @see #playbackCodecs()
     */
    @Override
    public Audio[] canPlay() {
        return playbackCodecs();
    }

    /**
     * <accessor>
     * To get access to audio format to play raw data (without header)
     *
     * @return the format for the play or null if device can't play back
     * @see #playbackRawCodec()
     */
    @Override
    public Audio getRawFormat() {
        return playbackRawCodec();
    }

    /**
     * <action>
     * Playback the audio stream data.
     *
     * @param session                a phone call's session, the device is working with
     * @param source                 the input stream, from which undertake sound data for playback in a telephone line
     * @param format                 parameter determining the type of the decoder for transformation the sound data
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return the operation's result<p>
     * {@link Result.IO#EOF} - the playback reached the end of stream;
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols(PhoneCallSession)};<BR/>
     * {@link Result#TIMEOUT} - the time of playback was exceeded.<BR/>
     * {@link Result.CALL#DISCONNECT} - the playback is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#FORMAT} - the format of audio does not support by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     * @see OperationResultValue
     */
    @Override
    public OperationResultValue playbackAudio(final PhoneCallSession<H> session,
                                              final InputStream source, final Audio format,
                                              final String terminationSymbolsMask, final int timeout) {
        if (canProceed(session, () -> canPlay(format))) {
            //
            // creating audio play back context for the audio playing
            final PlaybackContext context = buildingPlaybackContextFor(session);
            context.source = source;
            context.format = format;
            context.timeout = timeout;
            context.termMask = terminationSymbolsMask;
            //
            // adjusting other operation's stuff
            final TelephonyServiceProvider<H> serviceProvider = context.serviceProvider;
            final H deviceHandle = context.deviceHandle;
            //
            // playing back the audio data stream
            try {
                // starting the audio data playing back
                if (!startAudioPlaying(session, context)) {
                    // to start playing is failed
                    final String errorReason = "Cannot start playing the audio data stream.";
                    return playbackAudioError(deviceHandle, context.tempFile, session, errorReason);
                }
                // saving the temporary data media file
                final File tempFile = context.tempFile;
                // enabling end-of-file operation results
                serviceProvider.enableEvents(deviceHandle, Result.IO.EOF);
                // according to timeout value waiting for timeout as well
                if (timeout > 0) {
                    // enabling timeout operation results
                    serviceProvider.enableEvents(deviceHandle, Result.TIMEOUT);
                    serviceProvider.timeoutEventIn(deviceHandle, AUDIO_PLAYING, timeout, TimeUnit.SECONDS);
                }
                //
                // processing the operation result after started waiting (several iterations maybe)
                while (true) {
                    // waiting for the event during 1 second
                    oneSecondWaitingForOperationCompleteEvent(session);
                    // getting the operation result after waiting for operation complete
                    final OperationResultValue operationResult = session.operationResult();
                    //
                    // checking end-of-file or timeout operation results
                    if (isPlayingCompleted.test(operationResult)) {
                        // deleting temporary file
                        if (!tempFile.delete()) {
                            // for some reason didn't delete the temporary file
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        // operation is completed (leaving the loop)
                        break;
                        // checking for the user input during the operation
                    } else if (operationResult == Result.IO.DTMF
                            && context.isTerminatedBy(session.parameter(Device.Parameter.USER_INPUT))) {
                        // deleting temporary file
                        if (!tempFile.delete()) {
                            // for some reason didn't delete the temporary file
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        // operation is terminated by DTMF input (leaving the loop)
                        break;
                        // checking device's hardware error
                    } else if (operationResult == Result.ERROR) {
                        // device hardware error is detected
                        final String errorReason = "Playback audio is failed.";
                        return playbackAudioError(deviceHandle, tempFile, session, errorReason);
                        // checking for the operation's interruption
                    } else if (session.isTerminated()) {
                        // stopping audio data transmitting by service provider
                        stopAudioPlaying(serviceProvider, deviceHandle);
                        // removing unnecessary temp file
                        if (tempFile.delete()) {
                            session.operationResult(Result.TERMINATED);
                            session.setState(Device.State.IDLE);
                        }
                        return Result.TERMINATED;
                        // checking for the disconnection during the operation
                    } else if (session.isDisconnected()) {
                        // stopping audio data transmitting by service provider
                        stopAudioPlaying(serviceProvider, deviceHandle);
                        // deleting temporary file
                        if (!tempFile.delete()) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        session.setState(Device.State.ERROR);
                        breakingTheSession(session, "Playback audio is failed. The connection is lost.");
                        //
                        // disconnecting form the current phone call
                        disconnect(session);
                        // preparing method's response
                        session.operationResult(Result.CALL.DISCONNECT);
                        return Result.CALL.DISCONNECT;
                    }
                }
            } catch (InterruptedException e) {
                session.getDevice().dispatchError(e, "Cannot wait audio data transmitting completion.");
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                // stopping audio data transmitting by service provider
                stopAudioPlaying(serviceProvider, deviceHandle);
                session.setState(Device.State.ERROR);
                return Result.ERROR;
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Temporary file creation failed.");
                session.setState(Device.State.ERROR);
                return Result.ERROR;
            }
            // operation is completed successfully by any reason
            session.getDevice().dispatchEvent("Playback audio is completed.");
            // unconditional stopping playback's operation
            stopAudioPlaying(serviceProvider, deviceHandle);
            // make playback's operation is complete
            session.setState(Device.State.IDLE);
            return session.operationResult();
        }
        // playback operation didn't start well
        session.setState(Device.State.ERROR);
        return Result.ERROR;
    }

    /**
     * <action>
     * Playback the audio stream data in asynchronous mode.
     *
     * @param session the phone call's session, device is working with
     * @param sound   the audio sound playing back in a telephone line asynchronously
     * @return true if it starts playing the sound well
     */
    @Override
    public boolean asyncPlaybackAudio(final PhoneCallSession<H> session, final Sound sound) {
        if (canProceed(session, () -> canPlay(sound.getFormat()))) {
            try {
                //
                // creating audio play back context for the audio playing
                final PlaybackContext context = buildingPlaybackContextFor(session);
                context.source = sound.getInputStream();
                context.format = sound.getFormat();
                //
                // trying to start playing
                return startAudioPlaying(session, context);
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Cannot create the temporary audio file");
            }
        }
        session.setState(Device.State.ERROR);
        session.operationResult(Result.ERROR);
        return false;
    }

    /**
     * <accessor>
     * To get access to the default audio format of recording
     * The codec will be loading during telephony device session starting process
     *
     * @return the default format for the voice record operation or null if device can't record
     * @see TelephonyDevice#startSession()
     */
    @Override
    public Audio getRecordFormat() {
        return deviceCore.getParameter(Parameter.RECORD_CODEC)
                .<Audio>map(ConfigurationParameter::getValue).orElse(null);
    }

    /**
     * <action>
     * Record the audio data from telephone line.
     *
     * @param session                the phone call's session, device is working with
     * @param target                 the output stream where recorded data will be placed
     * @param format                 parameter determining type of the record audio data
     * @param terminationSymbolsMask set of symbols finishing up the recording (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param silence                time (seconds) how long silence in a line is allowed, after which the record operation will be finished.
     * @param timeout                maximum time of recording in seconds
     * @return the operation's result
     * <p>
     * {@link Result#TIMEOUT} - the time of audio record was exceeded.<BR/>
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols(PhoneCallSession)};<BR/>
     * {@link Result.CALL#DISCONNECT} - the record is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#SILENCE} - silence exceeded in a line;<BR/>
     * {@link Result.IO#FORMAT} - the format is not supported by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     * @see OperationResultValue
     */
    @Override
    public OperationResultValue recordAudio(
            final PhoneCallSession<H> session, final OutputStream target, final Audio format,
            final String terminationSymbolsMask, final int silence, final int timeout) {
        if (canProceed(session, () -> canRecord(format))) {
            // staring the audio recording
            final RecordContext context = buildingRecordContextFor(session);
            context.target = target;
            context.format = format;
            context.timeout = timeout;
            context.silence = silence;
            context.termMask = terminationSymbolsMask;
            //
            // adjusting other operation's stuff
            final TelephonyServiceProvider<H> serviceProvider = context.serviceProvider;
            final H deviceHandle = context.deviceHandle;
            //
            // recording to the audio data stream
            try {
                // starting the audio data recording
                if (!startRecording(session, context)) {
                    // to start recording is failed
                    final String errorReason = "Cannot start recording the audio file.";
                    return recordAudioError(deviceHandle, context.tempFile, session, errorReason);
                }
                // saving the temporary data media file
                final File tempFile = context.tempFile;
                // enabling end-of-file operation results
                serviceProvider.enableEvents(deviceHandle, Result.IO.EOF);
                // enabling timeout operation results
                serviceProvider.enableEvents(deviceHandle, Result.TIMEOUT);
                // to schedule the timeout device-event after reached the value of timeout parameter in seconds
                serviceProvider.timeoutEventIn(deviceHandle, AUDIO_RECORDING, timeout, TimeUnit.SECONDS);
                //
                while (true) {
                    // waiting for the event during 1 second
                    oneSecondWaitingForOperationCompleteEvent(session);
                    // getting the operation result after waiting for operation complete
                    final OperationResultValue operationResult = session.operationResult();
                    //
                    // checking timeout, end-of-file or silence operation results
                    if (isRecordCompleted.test(operationResult)) {
                        // stopping audio data transmitting by service provider
                        // and copying recorded data to the target, removing unnecessary temp file
                        if (!copyRecordedData(context, tempFile, target)) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        // record operation is completed (leaving the loop)
                        break;
                        // checking for the user input during the operation
                    } else if (operationResult == Result.IO.DTMF
                            && context.isTerminatedBy(session.parameter(Device.Parameter.USER_INPUT))) {
                        // stopping audio data transmitting by service provider
                        // and copying recorded data to the target, removing unnecessary temp file
                        if (!copyRecordedData(context, tempFile, target)) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        // operation is completed by masked DTMF input (leaving the loop)
                        break;
                        // checking device's hardware error
                    } else if (operationResult == Result.ERROR) {
                        // device hardware error is detected
                        final String errorReason = "Record audio is failed.";
                        return recordAudioError(deviceHandle, tempFile, session, errorReason);
                        // checking for the operation's interruption
                    } else if (session.isTerminated()) {
                        // stopping audio data transmitting by service provider
                        stopAudioRecording(serviceProvider, deviceHandle);
                        // copying recorded data to the target, removing unnecessary temp file
                        if (copyRecordedData(context, tempFile, target)) {
                            session.operationResult(Result.TERMINATED);
                            session.setState(Device.State.IDLE);
                        }
                        return Result.TERMINATED;
                        // checking for the disconnection during the operation
                    } else if (session.isDisconnected()) {
                        // stopping audio data transmitting by service provider
                        stopAudioRecording(serviceProvider, deviceHandle);
                        session.setState(Device.State.ERROR);
                        breakingTheSession(session, "Recording audio is failed. The connection is lost.");
                        //
                        // disconnecting form the current phone call
                        disconnect(session);
                        session.operationResult(Result.CALL.DISCONNECT);
                        // copying recorded data to the target, removing unnecessary temp file
                        return copyRecordedData(context, tempFile, target) ? Result.CALL.DISCONNECT : Result.ERROR;
                    }
                }
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Temporary file creation failed.");
                session.setState(Device.State.ERROR);
                return Result.ERROR;
            } catch (InterruptedException e) {
                session.getDevice().dispatchError(e, "Cannot wait audio data transmitting completion.");
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                // stopping audio data transmitting by service provider
                stopAudioRecording(serviceProvider, deviceHandle);
                session.setState(Device.State.ERROR);
                return Result.ERROR;
            }
            // operation is complete
            session.getDevice().dispatchEvent("Record audio is completed.");
            // unconditional stopping audio record's operation
            stopAudioRecording(serviceProvider, deviceHandle);
            // make send tone operation is complete
            session.setState(Device.State.IDLE);
            return session.operationResult();
        }
        // record operation didn't finish well
        session.setState(Device.State.ERROR);
        return Result.ERROR;
    }

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @param session the phone call's session, device is working with
     * @throws IOException If the device can't terminate current operation
     * @see PhoneCallSession
     */
    @Override
    public void terminate(PhoneCallSession<H> session) throws IOException {
        if (isOperationInProgress.test(session.getState())) {
            session.operationComplete(Result.TERMINATED);
        }
        session.terminate();
    }

    /// private methods
    // to check input session's state
    private boolean canProceed(final PhoneCallSession<H> session, BooleanSupplier isSupportFormat) {
        return session.isAlive() && isOpened(session) && isSupportFormat.getAsBoolean();
    }

    //  Returns the array of supported audio formats(codecs) for playing back
    private Audio[] playbackCodecs() {
        return deviceCore.getParameter(Parameter.ALLOWED_CODECS)
                .<List<Audio>>map(ConfigurationParameter::getValue)
                .orElse(Collections.emptyList())
                .toArray(new Audio[0]);
    }

    // Returns the audio format to play raw data (without header)
    private Audio playbackRawCodec() {
        return deviceCore.getParameter(Parameter.PLAYBACK_CODEC)
                .<Audio>map(ConfigurationParameter::getValue)
                .orElse(null);
    }

    private void copyMediaData(final Path tempFilePath, final InputStream source) throws IOException {
        try (final InputStream in = new BufferedInputStream(source);
             final OutputStream target = new BufferedOutputStream(Files.newOutputStream(tempFilePath))) {
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                target.write(buffer, 0, read);
            }
        }
    }

    // preparing the playing back audio context for the session
    private PlaybackContext buildingPlaybackContextFor(final PhoneCallSession<H> session) {
        session.getDevice().dispatchEvent("Playback audio is starting...");
        session.setState(TelephonyDevice.State.PLAY);
        //
        // getting device service provider
        final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
        // getting session's device handle
        final H deviceHandle = session.getDeviceHandle();
        //
        // adjusting events producing rules
        serviceProvider.disableEvents(deviceHandle);
        serviceProvider.enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        //
        // creating audio play back context for the audio playing
        final PlaybackContext context = new PlaybackContext();
        context.serviceProvider = serviceProvider;
        context.deviceHandle = deviceHandle;
        return context;
    }

    // starting audio playing back
    private boolean startAudioPlaying(
            final PhoneCallSession<H> session, final PlaybackContext context
    ) throws IOException {
        // creating the temporary audio data file
        final Path tempFilePath = Files.createTempFile(session.getDeviceName(), ".audio.wav");
        copyMediaData(tempFilePath, context.source);
        tempFilePath.toFile().deleteOnExit();
        context.tempFile = tempFilePath.toFile();
        // saving to session the reference to the temporary file for tests purposes
        session.parameter(Parameter.AUDIO_TEMPORARY, context.tempFile);
        // adjusting the device events management
        // adjust DTMF device events masking
        if (context.hasTerminationMask()) {
            context.serviceProvider.enableEvents(context.deviceHandle, Result.IO.DTMF);
        } else {
            context.serviceProvider.disableEvents(context.deviceHandle, Result.IO.DTMF);
        }
        // trying to start audio playing back
        return context.serviceProvider.startAudioPlaying(
                context.deviceHandle, tempFilePath, context.format, context.timeout
        );
    }

    // preparing the recording audio context for the session
    private RecordContext buildingRecordContextFor(final PhoneCallSession<H> session) {
        session.getDevice().dispatchEvent("Audio record is starting...");
        session.setState(TelephonyDevice.State.RECORD);
        //
        // getting device service provider
        final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
        // getting session's device handle
        final H deviceHandle = session.getDeviceHandle();
        //
        // adjusting events producing rules
        serviceProvider.disableEvents(deviceHandle);
        serviceProvider.enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        //
        // creating audio play back context for the audio playing
        final RecordContext context = new RecordContext();
        context.serviceProvider = serviceProvider;
        context.deviceHandle = deviceHandle;
        return context;
    }

    // starting audio recording
    private boolean startRecording(
            final PhoneCallSession<H> session, final RecordContext context
    ) throws IOException {
        // creating the temporary audio data file
        final Path tempFilePath = Files.createTempFile(session.getDeviceName(), ".audio");
        tempFilePath.toFile().deleteOnExit();
        context.tempFile = tempFilePath.toFile();
        // saving to session the reference to the temporary file for tests purposes
        session.parameter(Parameter.AUDIO_TEMPORARY, context.tempFile);
        // check parameters
        if (context.timeout < 0) {
            Tools.error("Wrong value of record timeout :" + context.timeout);
            return false;
        }
        // adjusting the device events management
        // adjust device events masking for DTMF
        if (context.hasTerminationMask()) {
            context.serviceProvider.enableEvents(context.deviceHandle, Result.IO.DTMF);
        } else {
            context.serviceProvider.disableEvents(context.deviceHandle, Result.IO.DTMF);
        }
        // adjust device events masking for the silence during the recording
        if (context.silence > 0) {
            // enabling termination by silence
            context.serviceProvider.enableEvents(context.deviceHandle, Result.IO.SILENCE);
        } else {
            // disabling termination by silence
            context.serviceProvider.disableEvents(context.deviceHandle, Result.IO.SILENCE);
        }
        // trying to start audio recording
        return context.serviceProvider.startAudioRecording(
                context.deviceHandle, context.tempFile.toPath(), context.format, context.silence, context.timeout
        );
    }

    // copying recorded audio data from temporary file to the target output stream
    private boolean copyRecordedData(
            final GeneralContext context, final File targetFile, final OutputStream target
    ) throws IOException {
        context.serviceProvider.stopAudioRecording(context.deviceHandle);
        Files.copy(targetFile.toPath(), target);
        return targetFile.delete();
    }

    private Result playbackAudioError(H deviceHandle, File tempFile, PhoneCallSession<H> session, String reason) {
        // stopping audio data transmitting by service provider
        stopAudioPlaying(deviceCore.getProvider(), deviceHandle);
        // deleting temporary file
        if (!tempFile.delete()) {
            // session is broken
            session.setState(Device.State.ERROR);
        } else {
            // throwing DeviceMalfunction error here
            onDeviceError(session, reason);
        }
        return Result.ERROR;
    }

    private static <H> void stopAudioPlaying(TelephonyServiceProvider<H> serviceProvider, H deviceHandle) {
        // disabling DTMF termination
        serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
        // stopping audio data transmitting by service provider
        serviceProvider.stopAudioPlaying(deviceHandle);
    }

    private Result recordAudioError(H deviceHandle, File tempFile, PhoneCallSession<H> session, String reason) {
        // stopping audio data transmitting by service provider
        stopAudioRecording(deviceCore.getProvider(), deviceHandle);
        // deleting temporary file
        if (!tempFile.delete()) {
            // session is broken
            session.setState(Device.State.ERROR);
        } else {
            // throwing DeviceMalfunction error here
            onDeviceError(session, reason);
        }
        return Result.ERROR;
    }

    private static <H> void stopAudioRecording(TelephonyServiceProvider<H> serviceProvider, H deviceHandle) {
        // stopping audio data transmitting by service provider
        serviceProvider.stopAudioRecording(deviceHandle);
        // disabling DTMF termination
        serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
    }

    // waiting for the next event for the device's session
    private static <H> void oneSecondWaitingForOperationCompleteEvent(PhoneCallSession<H> session) throws InterruptedException {
        // continue waiting for the next event
        session.operationResult(Result.NONE);
        // continue waiting for a bit lesser duration
        session.waitingForOperationComplete(1000L);
    }

    /// inner classes
    //  the general context for multimedia IO operation
    private class GeneralContext {
        TelephonyServiceProvider<H> serviceProvider;
        H deviceHandle;
        File tempFile;
        Audio format;
        int timeout = -1;
        String termMask = "";

        boolean hasTerminationMask() {
            return !isStringEmpty.test(termMask);
        }

        boolean isTerminatedBy(final String userInput) {
            return hasTerminationMask() && !isStringEmpty.test(userInput) && isFromMask(userInput);
        }

        private boolean isFromMask(String userInput) {
            // getting the last symbol of the user input in the context
            final String lastSymbol = userInput.substring(userInput.length() - 1);
            // analyzing the last user input symbol
            //
            // is the symbol from the termination mask?
            return termMask.contains(lastSymbol);
        }
    }

    // the context for audio playing back operation
    private class PlaybackContext extends GeneralContext {
        InputStream source;
    }

    // the context for audio record operation
    private class RecordContext extends GeneralContext {
        OutputStream target;
        int silence;
    }
}
