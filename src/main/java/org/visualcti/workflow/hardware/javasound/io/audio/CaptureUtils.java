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
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
public final class CaptureUtils implements Constants {
    private static final Map<SoundCardHandle, BlockingQueue<Runnable>> afterParty = new ConcurrentHashMap<>();
    public static final String QUEUE_IS_FULL = "CaptureUtils: AfterParty queue is full!!!";

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
            final TargetDataLine target = beforeAudioCapturing(handle, audioFormat);
            // mark the operation as in progress
            startedOperation(handle);
            // capturing the audio and save it to the file
            captureAudioToOutputFile(handle, outputFilePath, target);
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

    /**
     * <action>
     * To complete audio data capturing and wait for it
     *
     * @param <H>            sound-card device handle type
     * @param handle         the handle of the opened resource (sound card device's handle)
     * @param afterRecording what it has to do after operation complete
     * @see #waitForOperationComplete(H)
     */
    public static <H extends SoundCardHandle> void completeCapturing(final H handle, final Runnable afterRecording) {
        final BlockingQueue<Runnable> completedAction = afterParty.get(handle);
        if (completedAction != null && !completedAction.offer(afterRecording)) {
            Tools.error(QUEUE_IS_FULL);
            return;
        }
        // waiting for operation isn't complete
        waitForOperationComplete(handle);
    }

    /**
     * <action>
     * To wait for operation isn't complete
     *
     * @param <H>    sound-card device handle type
     * @param handle the handle of the opened resource (sound card device's handle)
     * @see #waitForOperationComplete(H)
     */
    public static <H extends SoundCardHandle> void waitForOperationComplete(final H handle) {
        while (handle.isOperationInProgress()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Tools.error("Operation completing is interrupted");
                e.printStackTrace(Tools.err);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            }
        }
    }

    // to mark operation for handle as started
    private static <H extends SoundCardHandle> void startedOperation(H handle) {
        handle.inProgress(true);
        final BlockingQueue<Runnable> previous = afterParty.put(handle, new ArrayBlockingQueue<>(1, true));
        if (previous != null) {
            final Runnable afterComplete = () -> Tools.error("CaptureUtils: Start Operation, Lost Runnable");
            // freeing previous queue
            if (!previous.offer(afterComplete)) {
                Tools.error(QUEUE_IS_FULL);
            }
        }
    }

    // to mark operation for handle as completed
    private static <H extends SoundCardHandle> void operationComplete(H handle) {
        // operation is completed
        handle.inProgress(false);
        // removing capturing operation's count down latch
        final BlockingQueue<Runnable> previous = afterParty.remove(handle);
        if (previous != null) {
            final Runnable afterComplete = () -> Tools.error("CaptureUtils: Complete Operation, Lost Runnable");
            // freeing previous queue
            if (!previous.offer(afterComplete)) {
                Tools.error(QUEUE_IS_FULL);
            }
        }
    }

    // preparing TargetDataLine for audio capturing
    private static <H extends SoundCardHandle> TargetDataLine beforeAudioCapturing(
            final H handle, final AudioFormat audioFormat
    ) throws LineUnavailableException {
        final TargetDataLine target = AudioSystem.getTargetDataLine(audioFormat);
        // adjusting and starting the target data line channel
        target.open(audioFormat);
        handle.setTargetLine(target);
        target.start();
        return target;
    }

    // capturing the audio (full cycle)
    private static <H extends SoundCardHandle> void captureAudioToOutputFile(
            final H handle, final Path outputFilePath, final TargetDataLine target
    ) throws IOException {
        // preparing after party actions queue for capture completing
        final BlockingQueue<Runnable> completedAction = afterParty.get(handle);
        if (completedAction == null) {
            // something went wrong
            throw new IOException("Sound capturing is started in a wrong way.");
        }
        //
        // capturing audio to the temporary file
        final Path tempRawAudioPath = capturingRawAudio(handle, completedAction, target);
        //
        // audio capturing operation is completed
        finalizeCapturing(tempRawAudioPath, target.getFormat(), outputFilePath.toFile());
        //
        // waiting for the audio capturing count down latch's freeing
        try {
            if (!completedAction.isEmpty()) {
                completedAction.take().run();
            }
        } catch (InterruptedException e) {
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
            throw new IOException("Count Down Latch is interrupted.", e);
        }
    }

    // capturing audio to the temporary file
    private static <H extends SoundCardHandle> Path capturingRawAudio(
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
        }
        // returning the path to file with raw audio data
        return tempRawAudioPath;
    }

    // finalizing the audio capturing
    private static void finalizeCapturing(
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
