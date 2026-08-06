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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Sound;

/**
 * Adapter: The Part of the Telephony Channel Device: The root device part of the telephony multimedia (playback/record) management
 *
 * @param <H> the type for low-level telephony operations device handle
 * @see MultimediaEngine
 * @see AbstractDevicePart
 */
public class AbstractMultimediaEngine<H> extends AbstractDevicePart<H> implements MultimediaEngine<H> {
    /**
     * <accessor>
     * Returns the array of supported audio formats for playing back,
     * null if playback is not supported
     *
     * @return the array of the supported playback formats supported by device or null if device can't play back
     */
    @Override
    public Audio[] canPlay() {
        final Optional<ConfigurationParameter> allowedCodecs = deviceCore.getParameter(Parameter.ALLOWED_CODECS);
        final List<Audio> codecs = allowedCodecs.isPresent() ? allowedCodecs.get().getValue() : Collections.emptyList();
        return codecs.toArray(new Audio[0]);
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
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @param format                 parameter determining type of the decoder for transformation the sound data
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
    public OperationResultValue playbackAudio(
            final PhoneCallSession<H> session, final InputStream source, final String terminationSymbolsMask,
            final int timeout, final Audio format) {
        if (session.isOpened() && session.isAlive() && canPlay(format)) {
            // staring audio data transmitting
            session.getDevice().dispatchEvent("Playback audio is starting...");
            session.setState(TelephonyDevice.State.PLAY);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.parameter(Device.Parameter.DEVICE_HANDLE);
            // adjusting events producing rules
            serviceProvider.enableEvents(deviceHandle, Result.IO.DTMF);
            final File tempFile;
            try {
                // creating temporary data media file
                tempFile = File.createTempFile(session.getDevice().getName(), ".audio");
                copyMediaData(tempFile, source);
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.AUDIO_TEMPORARY, tempFile);
                // staring audio data transmitting by service provider
                final String tempFileName = tempFile.getAbsolutePath();
                final boolean starting = serviceProvider.startAudioPlaying(deviceHandle, tempFileName, timeout, format);
                if (!starting) {
                    // throwing DeviceMalfunction error here
                    onDeviceError(session, "Cannot start playing the audio file");
                    // unreachable statement
                    return Result.ERROR;
                }
                // start to wait for the operation result
                session.operationComplete(Result.NONE);
                // waiting for an event during the audio data transmitting
                session.waitForOperationComplete(timeout * 1000L);
                final OperationResultValue operationResult = session.operationResult();
                // checking the operation result value after waiting operation complete
                if (operationResult == Result.ERROR) {
                    // device hardware error is detected
                    // stopping audio data transmitting by service provider
                    serviceProvider.stopAudioPlaying(deviceHandle);
                    // throwing DeviceMalfunction error here
                    onDeviceError(session, "Playback audio is failed.");
                    // unreachable statement
                    return operationResult;
                    // checking for the end of data operation result
                } else if (operationResult == Result.IO.EOF) {
                    // operation is complete
                    session.getDevice().dispatchEvent("Playback audio is completed.");
                    // deleting temporary file
                    if (tempFile.delete()) {
                        // stopping audio data transmitting by service provider
                        serviceProvider.stopAudioPlaying(deviceHandle);
                    }
                    // checking for the termination of the operation
                } else if (session.isTerminated()) {
                    // operation termination is detected
                    // removing unnecessary temp file
                    if (tempFile.delete()) {
                        // stopping audio data transmitting by service provider
                        serviceProvider.stopAudioPlaying(deviceHandle);
                        session.operationResult(Result.TERMINATED);
                        session.setState(Device.State.IDLE);
                    }
                    return Result.TERMINATED;
                    // checking for the disconnection during the operation
                } else if (session.isDisconnected()) {
                    session.getDevice().dispatchError("Playback audio is failed. The connection is lost.");
                    // stopping audio data transmitting by service provider
                    serviceProvider.stopAudioPlaying(deviceHandle);
                    session.operationResult(Result.CALL.DISCONNECT);
                    session.setState(Device.State.ERROR);
                    return Result.CALL.DISCONNECT;
                }
            } catch (InterruptedException e) {
                session.getDevice().dispatchError(e, "Cannot wait audio data transmitting completion.");
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                // stopping audio data transmitting by service provider
                serviceProvider.stopAudioPlaying(deviceHandle);
                return Result.ERROR;
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Temporary file creation failed.");
                return Result.ERROR;
            }
            // operation is complete
            session.getDevice().dispatchEvent("Playback audio is finished.");
            // stopping audio data transmitting by service provider
            serviceProvider.stopAudioPlaying(deviceHandle);
            // make send tone operation is complete
            session.setState(Device.State.IDLE);
            return session.operationResult();
        }
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
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.parameter(Device.Parameter.DEVICE_HANDLE);
            // adjusting events producing rules
            serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
            final File tempFile;
            try {
                // creating temporary data media file
                tempFile = File.createTempFile(session.getDevice().getName(), ".audio");
                copyMediaData(tempFile, sound.getInputStream());
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.AUDIO_TEMPORARY, tempFile);
                // staring audio data transmitting by service producer
                final String tempFileName = tempFile.getAbsolutePath();
                final boolean starting = serviceProvider.startAudioPlaying(deviceHandle, tempFileName, -1, format);
                if (!starting) {
                    throw new IOException("Cannot start audio playing");
                }
                return true;
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Cannot start playing the audio file");
            }
        }
        return false;
    }

    /**
     * <accessor>
     * To get access to the default audio format of recording
     *
     * @return the default format for the voice record operation or null if device can't record
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
     * @param terminationSymbolsMask set of symbols finishing up the recording (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param silence                time (seconds) how long silence in a line is allowed, after which the record operation be finished.
     * @param timeout                maximum time of recording in seconds
     * @param format                 parameter determining type of the record audio data
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
            final PhoneCallSession<H> session, final OutputStream target, final String terminationSymbolsMask,
            final int silence, final int timeout, final Audio format) {
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
}
