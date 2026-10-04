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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider;
import org.visualcti.workflow.hardware.javasound.io.Constants;

/**
 * Provider Facade Part:Class-Utility: The telephony service provider facade 'audio capturing implementation'
 * (for the JavaSound implementation)
 *
 * @see org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider
 */
public final class CaptureUtils extends CommonUtils implements Constants {
    /**
     * <action>
     * To capture audio data and save recorded data to the output file in the WAVE format
     *
     * @param handle         the handle of the opened resource (sound card device's handle)
     * @param outputFilePath the path to the file which will content captured(recorded) media data
     * @param format         parameter determining the type of the decoder for transformation the sound data
     * @param <H>            sound-card device handle type
     * @see SoundCardServiceProvider#startAudioRecording(H, Path, Audio, int, int)
     */
    public static <H extends SoundCardHandle> void capturingAudioToFile(
            final H handle, final Path outputFilePath, final Audio format
    ) {
        final AudioFormat audioFormat = format.toFormat();
        if (audioFormat == null) {
            Tools.error("Invalid audio format :" + format);
            return;
        }
        // capturing audio to the file
        try {
            // preparing audio data capturing stuff
            final TargetDataLine target = preparingAudioCapturing(handle, audioFormat);
            // mark the operation as in progress
            startOperation(handle);
            // capturing the audio and save it to the file
            doingCapturingToFile(handle, outputFilePath, target);
            // finalizing the audio capturing
            target.stop();
            target.close();
        } catch (LineUnavailableException | IOException e) {
            Tools.error("Failed to record the audio.");
            e.printStackTrace(Tools.err);
        } catch (IllegalArgumentException e) {
            Tools.error("Failed to record the audio by format reason.");
            e.printStackTrace(Tools.err);
        } finally {
            // detaching the line from device's handle
            handle.setTargetLine(null);
            operationComplete(handle);
        }
    }

    /// private methods
    // preparing stuff for audio capturing
    private static <H extends SoundCardHandle> TargetDataLine preparingAudioCapturing(
            final H handle, final AudioFormat audioFormat
    ) throws LineUnavailableException {
        // getting target line by audio format
        final TargetDataLine target = AudioSystem.getTargetDataLine(audioFormat);
        // adjusting and starting the target data line channel
        target.open(audioFormat);
        handle.setTargetLine(target);
        target.start();
        return target;
    }

    // capturing the audio to output file (full cycle)
    private static <H extends SoundCardHandle> void doingCapturingToFile(
            final H handle, final Path outputFilePath, final TargetDataLine target
    ) throws IOException {
        // preparing after party actions queue for capture completing
        final BlockingQueue<Runnable> afterPartyQueue = getAfterPartyQueue(handle);
        //
        // capturing audio to the temporary file
        final Path tempRawAudioPath = capturedRawAudio(handle, afterPartyQueue, target);
        //
        // audio capturing operation is completed
        finalizeOperation(tempRawAudioPath, target.getFormat(), outputFilePath.toFile());
        //
        postOperation(afterPartyQueue);
    }

    // capturing audio to the temporary file
    private static <H extends SoundCardHandle> Path capturedRawAudio(
            final H handle, final BlockingQueue<Runnable> completedAction, final TargetDataLine target
    ) throws IOException {
        // preparing temporary file for the captured RAW audio data
        final Path tempRawAudioPath = Files.createTempFile("audio", ".rawdata");
        tempRawAudioPath.toFile().deleteOnExit();
        int bytesCaptured;
        final byte[] buffer = new byte[BUFFER_SIZE];
        try (final OutputStream out = Files.newOutputStream(tempRawAudioPath)) {
            //
            // audio capturing to the temporary file operation is started
            while (Boolean.TRUE.equals(handle.isTargetActive()) && completedAction.isEmpty()) {
                // getting audio chunk from the audio input
                if ((bytesCaptured = target.read(buffer, 0, buffer.length)) > 0) {
                    // saving chunk to the temporary file
                    out.write(buffer, 0, bytesCaptured);
                } else {
                    break;
                }
            }
            // finalizing transfer operation
            out.flush();
        }
        // returning the path to file with raw audio data
        return tempRawAudioPath;
    }

    // finalizing the audio capturing operation
    private static void finalizeOperation(
            final Path tempRawAudioPath, final AudioFormat audioFormat, final File outputFile
    ) throws IOException {
        // saving the audio recording result
        final long rawDataSize = Files.size(tempRawAudioPath);
        try (
                final InputStream rawDataInput = Files.newInputStream(tempRawAudioPath);
                final AudioInputStream audioIn = new AudioInputStream(rawDataInput, audioFormat, rawDataSize)
        ) {
            AudioSystem.write(audioIn, AudioFileFormat.Type.WAVE, outputFile);
        }
        //
        // cleaning the operation's stuff
        Files.delete(tempRawAudioPath);
    }

    // private constructor
    private CaptureUtils() {
    }
}
