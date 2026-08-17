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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
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

    /**
     * <accessor>
     * Returns the array of supported audio formats(codecs) for playing back,
     * empty array if playback is not supported
     * The codecs will be loading during telephony device session starting process
     *
     * @return the array of the supported playback formats supported by device or empty array if device can't play back
     * @see TelephonyDevice#canPlay()
     * @see TelephonyDevice#startSession()
     */
    @Override
    public Audio[] canPlay() {
        return deviceCore.getParameter(Parameter.ALLOWED_CODECS)
                .<List<Audio>>map(ConfigurationParameter::getValue)
                .orElse(Collections.emptyList())
                .toArray(new Audio[0]);
    }

    /**
     * <accessor>
     * To get access to audio format to play raw data (without header)
     *
     * @return the format for the play or null if device can't play back
     */
    @Override
    public Audio getRawFormat() {
        return deviceCore.getParameter(Parameter.PLAYBACK_CODEC)
                .<Audio>map(ConfigurationParameter::getValue)
                .orElse(null);
    }

    /**
     * <action>
     * Playback the audio stream data.
     *
     * @param session                the phone call's session, device is working with
     * @param source                 the input stream, from which undertake sound data for playback in a telephone line
     * @param format                 parameter determining type of the decoder for transformation the sound data
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return the operation's result<p>
     * {@link Result.IO#EOF} - the playback reached end of stream;
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
        if (session.isOpened() && session.isAlive() && canPlay(format)) {
            // staring audio data transmitting
            session.getDevice().dispatchEvent("Playback audio is starting...");
            session.setState(TelephonyDevice.State.PLAY);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.getDeviceHandle();
            //
            // creating the temporary data media file
            final File tempFile;
            try {
                tempFile = File.createTempFile(session.getDevice().getName(), ".audio");
                copyMediaData(tempFile, source);
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.AUDIO_TEMPORARY, tempFile);
                //
                // enabling DTMF termination
                serviceProvider.enableEvents(deviceHandle, Result.IO.DTMF);
                // staring audio data transmitting by service provider
                final String tempFileName = tempFile.getAbsolutePath();
                final boolean starting = serviceProvider.startAudioPlaying(deviceHandle, tempFileName, format, timeout);
                if (!starting) {
                    // start playing is failed
                    final String errorReason = "Cannot start playing the audio file.";
                    return playbackAudioError(deviceHandle, tempFile, session, errorReason);
                }
                final long endMark = System.currentTimeMillis() + timeout * 1000L;
                // start waiting for the final operation result
                waitingForTheNextEvent(session, timeout * 1000L);
                //
                // processing the operation result after started waiting (several iterations may be)
                while (true) {
                    // getting the operation result after waiting for operation complete
                    final OperationResultValue operationResult = session.operationResult();
                    // checking the operation result value
                    if (operationResult == Result.ERROR) {
                        // device hardware error is detected
                        final String errorReason = "Playback audio is failed.";
                        return playbackAudioError(deviceHandle, tempFile, session, errorReason);
                        // checking for the end of data operation result
                    } else if (operationResult == Result.IO.EOF) {
                        // stopping audio data transmitting by service provider
                        stopAudioPlaying(serviceProvider, deviceHandle);
                        // deleting temporary file
                        if (!tempFile.delete()) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        } else {
                            // finishing the processing
                            break;
                        }
                        // checking for the user input during the operation
                    } else if (operationResult == Result.IO.DTMF) {
                        // calculating duration for the next waiting operation complete
                        final long waitForNextOperationComplete = endMark - System.currentTimeMillis();
                        // is termination mask empty?
                        if (isEmpty(terminationSymbolsMask)) {
                            // continue waiting for the next event
                            waitingForTheNextEvent(session, waitForNextOperationComplete);
                            // go to the operation result processing after waiting
                            continue;
                        }
                        // user input detected
                        final String userInput = session.parameter(Device.Parameter.USER_INPUT);
                        if (!isEmpty(userInput)) {
                            // getting the last symbol of the user input in the context
                            final String lastSymbol = userInput.substring(userInput.length() - 1);
                            // analyzing the last user input symbol
                            //
                            // is the symbol not from the termination mask?
                            if (terminationSymbolsMask.contains(lastSymbol)) {
                                break;
                                // can it continue waiting?
                            } else if (waitForNextOperationComplete > 0L) {
                                // continue waiting for the next event
                                waitingForTheNextEvent(session, waitForNextOperationComplete);
                            }
                        } else if (endMark < System.currentTimeMillis() && tempFile.delete()) {
                            // timeout is expired, finishing up the operation by timeout reason
                            session.operationResult(Result.TIMEOUT);
                            // stopping audio data transmitting by service provider
                            stopAudioPlaying(serviceProvider, deviceHandle);
                            break;
                        }
                        // checking for the operation's interruption
                    } else if (session.isTerminated()) {
                        // operation termination is detected
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
                        session.getDevice().dispatchError("Playback audio is failed. The connection is lost.");
                        // stopping audio data transmitting by service provider
                        stopAudioPlaying(serviceProvider, deviceHandle);
                        // deleting temporary file
                        if (!tempFile.delete()) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        session.operationResult(Result.CALL.DISCONNECT);
                        session.setState(Device.State.ERROR);
                        return Result.CALL.DISCONNECT;
                    } else {
                        session.operationResult(Result.TIMEOUT);
                        break;
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
            stopAudioPlaying(serviceProvider, deviceHandle);
            // make send tone operation is complete
            session.setState(Device.State.IDLE);
            return session.operationResult();
        }
        // playback operation didn't finish well
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
    public boolean asyncPlaybackAudio(PhoneCallSession<H> session, Sound sound) {
        final Audio format = sound.getFormat();
        if (session.isOpened() && session.isAlive() && canPlay(format)) {
            // staring audio data transmitting
            session.getDevice().dispatchEvent("Playback audio is starting...");
            session.setState(TelephonyDevice.State.PLAY);
            session.operationResult(Result.NONE);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.getDeviceHandle();
            //
            // disabling DTMF termination
            serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
            //
            // creating temporary data media file
            final File tempFile;
            try {
                tempFile = File.createTempFile(session.getDevice().getName(), ".audio");
                copyMediaData(tempFile, sound.getInputStream());
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.AUDIO_TEMPORARY, tempFile);
                // staring audio data transmitting by service producer
                final String tempFileName = tempFile.getAbsolutePath();
                return serviceProvider.startAudioPlaying(deviceHandle, tempFileName, format, -1);
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
        if (session.isOpened() && session.isAlive() && canRecord(format)) {
            // staring audio data transmitting
            session.getDevice().dispatchEvent("Audio record is starting...");
            session.setState(TelephonyDevice.State.RECORD);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.getDeviceHandle();
            //
            // creating the temporary data media file
            final File tempFile;
            try {
                tempFile = File.createTempFile(session.getDevice().getName(), ".audio");
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.AUDIO_TEMPORARY, tempFile);
                //
                // enabling DTMF termination
                serviceProvider.enableEvents(deviceHandle, Result.IO.DTMF);
                // staring audio data transmitting by service provider
                final String tempFileName = tempFile.getAbsolutePath();
                final boolean starting = serviceProvider.startAudioRecording(deviceHandle, tempFileName, format, silence, timeout);
                if (!starting) {
                    // start recording is failed
                    final String errorReason = "Cannot start recording the audio file.";
                    return recordAudioError(deviceHandle, tempFile, session, errorReason);
                }
                // start waiting for the final operation result
                session.operationComplete(Result.NONE);
                for (int second = 0; second < timeout; second++) {
                    // waiting for an event during the audio data transmitting
                    session.waitingForOperationComplete(1000L);
                    final OperationResultValue operationResult = session.operationResult();
                    // checking the operation result value after waiting operation complete
                    if (operationResult == Result.ERROR) {
                        // device hardware error is detected
                        final String errorReason = "Record audio is failed.";
                        return recordAudioError(deviceHandle, tempFile, session, errorReason);
                        // checking for the end of data operation result
                        // checking for the silence detection in the recording operation
                    } else if (operationResult == Result.IO.EOF || operationResult == Result.IO.SILENCE) {
                        // stopping audio data transmitting by service provider
                        stopAudioRecording(serviceProvider, deviceHandle);
                        // copying recorded data to the target, removing unnecessary temp file
                        if (!copyRecordedData(tempFile, target)) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        } else {
                            // finishing up the events processing
                            break;
                        }
                        // checking for the user input during the operation
                    } else if (operationResult == Result.IO.DTMF) {
                        // is termination symbols mask is not empty?
                        if (!isEmpty(terminationSymbolsMask)) {
                            // user input detected
                            final String userInput = session.parameter(Device.Parameter.USER_INPUT);
                            // checking the user input content
                            if (!isEmpty(userInput)) {
                                // getting the last symbol of the user input in the context
                                final String lastSymbol = userInput.substring(userInput.length() - 1);
                                // analyzing the last user input symbol
                                if (terminationSymbolsMask.contains(lastSymbol)) {
                                    // the symbol from the termination mask
                                    // stopping audio data transmitting by service provider
                                    stopAudioRecording(serviceProvider, deviceHandle);
                                    // copying recorded data to the target, removing unnecessary temp file
                                    if (!copyRecordedData(tempFile, target)) {
                                        session.setState(Device.State.ERROR);
                                        return Result.ERROR;
                                    }
                                    // finishing up the events processing
                                    break;
                                }
                            }
                        }
                        // clearing operation result value
                        session.operationResult(Result.NONE);
                        // checking for the operation's interruption
                    } else if (session.isTerminated()) {
                        // operation termination is detected
                        // stopping audio data transmitting by service provider
                        stopAudioRecording(serviceProvider, deviceHandle);
                        // copying recorded data to the target, removing unnecessary temp file
                        if (copyRecordedData(tempFile, target)) {
                            session.operationResult(Result.TERMINATED);
                            session.setState(Device.State.IDLE);
                        }
                        return Result.TERMINATED;
                        // checking for the disconnection during the operation
                    } else if (session.isDisconnected()) {
                        session.getDevice().dispatchError("Recording audio is failed. The connection is lost.");
                        // stopping audio data transmitting by service provider
                        stopAudioRecording(serviceProvider, deviceHandle);
                        // copying recorded data to the target, removing unnecessary temp file
                        if (!tempFile.delete()) {
                            session.setState(Device.State.ERROR);
                            return Result.ERROR;
                        }
                        session.operationResult(Result.CALL.DISCONNECT);
                        session.setState(Device.State.ERROR);
                        return Result.CALL.DISCONNECT;
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
    // copying media data to the temporary file from the source input stream
    private void copyMediaData(File tempFile, InputStream source) throws IOException {
        final int DEFAULT_BUFFER_SIZE = 8192;
        try (final InputStream in = new BufferedInputStream(source);
             final OutputStream target = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                target.write(buffer, 0, read);
            }
        }
    }

    // copying recorded audio data from temporary file to the target output stream
    private boolean copyRecordedData(final File targetFile, final OutputStream target) throws IOException {
        final int DEFAULT_BUFFER_SIZE = 8192;
        try (final InputStream in = new BufferedInputStream(new FileInputStream(targetFile))) {
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                target.write(buffer, 0, read);
            }
        }
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
    private static <H> void waitingForTheNextEvent(PhoneCallSession<H> session, long waitForNextOperationComplete) throws InterruptedException {
        // continue waiting for the next event
        session.operationResult(Result.NONE);
        // continue waiting for a bit lesser duration
        session.waitingForOperationComplete(waitForNextOperationComplete);
    }
}
