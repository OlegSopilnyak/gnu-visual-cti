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
package org.visualcti.workflow.hardware.javasound.io.audio;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.io.Constants;

/**
 * Provider Facade Part:Class-Utility: The telephony service provider facade 'audio playing back implementation'
 * (for the JavaSound implementation)
 *
 * @see org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider
 */
public final class PlaybackUtils implements Constants {
    /**
     * <action>
     * To play back the audio file using handle's source line
     *
     * @param handle        the handle of the opened resource (sound card device's handle)
     * @param audioFile     the audio file to play
     * @param resultUpdater the updater of play operation result value
     * @param <H>           sound-card device handle type
     * @see org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider#startAudioPlaying(H, Path, Audio, int)
     */
    public static <H extends SoundCardHandle> void playingBackAudioFile(
            final H handle, final File audioFile, final Consumer<OperationResultValue> resultUpdater
    ) {
        try (final AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile)) {
            //
            // preparing audio data playing stuff
            final SourceDataLine channel = beforeAudioPlaying(handle, audioStream.getFormat());
            //
            // playing back the audio
            playingBackAudioStream(handle, audioStream, channel);
            //
            // finalizing the audio file playing
            finalizeAudioFilePlayingBack(handle, channel, resultUpdater);
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            Tools.error("Failed to playback the audio");
            e.printStackTrace(Tools.err);
        } finally {
            // detaching the line from device's handle
            handle.setSourceLine(null);
            handle.inProgress(false);
        }
    }

    // finalizing the audio file playing
    private static <H extends SoundCardHandle> void finalizeAudioFilePlayingBack(
            H handle, SourceDataLine channel, Consumer<OperationResultValue> resultUpdater
    ) {
        //
        // finalizing the audio file playing if source data line is active
        if (channel.isActive()) {
            // Wait for buffer to empty before closing
            channel.drain();
        }
        // audio playing operation is completed
        if (Boolean.TRUE.equals(handle.isSourceActive())) {
            // the end of audio stream is reached, sending EOF event
            resultUpdater.accept(Result.IO.EOF);
        } else
            // audio playing back is terminated outside
            if (channel.isActive()) {
                // timeout state applied outside
                // putting the operation timeout event
                resultUpdater.accept(Result.TIMEOUT);
            } else {
                // audio playing back is stopped outside
                // putting the event about the end of file reached
                resultUpdater.accept(Result.IO.EOF);
            }
        //
        // finishing up the channel's playback regardless it's status
        channel.stop();
        channel.close();
    }

    // preparing audio data playing stuff
    static <H extends SoundCardHandle> SourceDataLine beforeAudioPlaying(
            final H handle, final AudioFormat audioFormat
    ) throws LineUnavailableException {
        // preparing source data line for the audio playing
        final SourceDataLine channel = AudioSystem.getSourceDataLine(audioFormat);
        //
        // adjusting source line listener
        channel.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                // removing the source line from the handle (to stop capturing the audio loop)
                handle.setSourceLine(null);
            }
        });
        // adjusting and starting the source data line channel
        channel.open(audioFormat);
        handle.setSourceLine(channel);
        channel.start();
        return channel;
    }

    /**
     * <action>
     * To play raw audio data stream through source data line
     *
     * @param handle              the handle of the opened resource (sound card device's handle)
     * @param rawAudioInputStream the raw audio data input stream
     * @param channel             the source data line
     * @param <H>                 sound-card device handle type
     * @throws IOException throws when something went wrong
     */
    static <H extends SoundCardHandle> void playingBackAudioStream(
            final H handle, final InputStream rawAudioInputStream, final SourceDataLine channel
    ) throws IOException {
        //
        // playing back audio stuff preparation
        final byte[] buffer = new byte[BUFFER_SIZE];
        int bytesToPlay;
        // playing back audio operation is started
        handle.inProgress(true);
        // getting audio chunks from the file and playing them back
        while (Boolean.TRUE.equals(handle.isSourceActive())) {
            // getting audio chunk from the file
            if ((bytesToPlay = rawAudioInputStream.read(buffer, 0, buffer.length)) > 0) {
                // playing back the audio chunk through the started channel
                channel.write(buffer, 0, bytesToPlay);
            } else {
                break;
            }
        }
    }

    // private constructor
    private PlaybackUtils() {
    }
}
