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

import static org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider.SAMPLE_RATE;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.TelephonyTone;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;

/**
 * Provider Facade Part:Class-Utility: The telephony service provider facade 'telephony tones playing back implementation'
 * (for the JavaSound implementation)
 *
 * @see org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider
 */
public class ToneUtils {
    // the format for tone's playing back
    public static final AudioFormat TONE_AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    // the container of DTMF tones
    private static final Map<Character, TelephonyTone> DTMF = new ConcurrentHashMap<>();

    static {
        // initializing dtmf tones table
        initializeDtmfTones();
    }


    /**
     * <action>
     * To play back the telephony tone using handle's source line
     *
     * @param handle        the handle of the opened resource (sound card device's handle)
     * @param tone          the telephony tone to send
     * @param resultUpdater the updater of play operation result value
     * @param <H>           sound-card device handle type
     */
    public static <H extends SoundCardHandle> void playingBackTone(
            final H handle, final TelephonyTone tone, final Consumer<OperationResultValue> resultUpdater
    ) {
        final byte[] toneBytesBuffer = toneBuffer(tone);
        if (toneBytesBuffer.length == 0) {
            // tone's data buffer ain't generated
            // putting the event about the end of file reached
            resultUpdater.accept(Result.IO.EOF);
            return;
        }
        try (ToneAudioInputStream toneIn = new ToneAudioInputStream(toneBytesBuffer)) {
            //
            // preparing audio data playing stuff
            final SourceDataLine channel = PlaybackUtils.beforeAudioPlaying(handle, TONE_AUDIO_FORMAT);
            //
            // playing back the tone as audio data input stream
            PlaybackUtils.playingBackAudioStream(handle, toneIn, channel);
            //
            // finalizing the tone as audio input stream playing
            finalizeTonePlayingBack(channel, resultUpdater);
        } catch (LineUnavailableException | IOException e) {
            Tools.error("Failed to playback the tone :" + tone);
            e.printStackTrace(Tools.err);
        } finally {
            // detaching the line from device's handle
            handle.setSourceLine(null);
            handle.inProgress(false);
        }
    }


    /**
     * <action>
     * To dial DTMF string using handle's source line
     *
     * @param toDial the DTMF string to dial
     */
    public static void dialingDtmf(final String toDial) {
        // playing composed buffer
        playComposedToneBuffer(
                // making audio buffer for string to dial
                composeAudioBufferFor(toDial)
        );
    }

    /// private methods
    // making audio buffer for string to dial
    private static byte[] composeAudioBufferFor(String toDial) {
        final ByteArrayOutputStream finalBuffer = new ByteArrayOutputStream();
        // consumer to concatenate DTMF bytes buffers
        final Consumer<byte[]> concatToneBuffers = buffer -> {
            try {
                finalBuffer.write(buffer);
            } catch (IOException e) {
                // doing nothing here
            }
        };
        try {
            // composing buffer from DTMF string
            toDial.chars().mapToObj(c -> DTMF.get((char) c)).filter(Objects::nonNull)
                    .map(ToneUtils::simpleToneBuffer).forEach(concatToneBuffers);
            finalBuffer.flush();
            finalBuffer.close();
        } catch (IOException e) {
            Tools.error("Error during made DTMF string audio buffer :" + toDial);
            e.printStackTrace(Tools.err);
            return new byte[0];
        }
        // returns made audio buffer
        return finalBuffer.toByteArray();
    }

    // finalizing the tone as audio input stream playing
    private static void finalizeTonePlayingBack(
            SourceDataLine channel, Consumer<OperationResultValue> resultUpdater
    ) {
        // putting the event about the end of audio data stream reached
        resultUpdater.accept(Result.IO.EOF);
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

    // to play tone from the bytes buffer
    private static void playComposedToneBuffer(byte[] buffer) {
        if (buffer != null && buffer.length > 0) {
            try {
                final SourceDataLine line = AudioSystem.getSourceDataLine(TONE_AUDIO_FORMAT);
                line.open(TONE_AUDIO_FORMAT);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (durationMs <= 0) {
            // wrong duration value
            return new byte[0];
        }
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
        int numSamples = (int) (SAMPLE_RATE * ((durationMs <= 0 ? 200 : durationMs) / 1000.0));
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

    // to make raw audio data for silence playing
    private static byte[] silenceBuffer(int durationMs) {
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples * 2]; // 16-bit PCM, 2 bytes per sample
        Arrays.fill(buffer, (byte) 0);
        return buffer;
    }

    // to make raw audio data for the telephony tone playing
    private static byte[] toneBuffer(TelephonyTone tone) {
        if (tone == null || tone.getPrimary().getFrequencyHz() <= 0) {
            // empty tone or not declared primary tone frequency
            return new byte[0];
        }
        // generating final version of the tone's buffer
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            final int primaryFreq = tone.getPrimary().getFrequencyHz();
            final int secondFreq = tone.getSecondary().getFrequencyHz();
            final int playing = tone.getDuration().getOnTime();
            final int silence = tone.getDuration().getOffTime();
            // write the sound part of the tone
            result.write(secondFreq <= 0
                    // only the primary tone's frequency is declared (single)
                    ? singleTonesBuffer(primaryFreq, playing)
                    // the botch frequencies are declared (dual)
                    : dualTonesBuffer(primaryFreq, secondFreq, playing)
            );
            if (silence > 0) {
                // write the silence part of the tone
                result.write(silenceBuffer(silence));
            }
            // prepare stream for the result's getting
            result.flush();
            // tone result buffer getting
            return result.toByteArray();
        } catch (IOException e) {
            Tools.error("Cannot generate buffer for :" + tone.getId());
            e.printStackTrace(Tools.err);
            // nothing to generate (error detected)
            return new byte[0];
        }
    }

    // to init the DTMF tones table
    private static void initializeDtmfTones() {
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

    // making telephony tone instance for DTMF frequencies
    private static TelephonyTone makeDtmfTone(int lowFreqHz, int highFreqHz) {
        final TelephonyTone tone = new TelephonyTone(ToneId.DTMF);
        tone.getPrimary().setFrequencyHz(lowFreqHz);
        tone.getSecondary().setFrequencyHz(highFreqHz);
        tone.getDuration().setOnTime(200);
        return tone;
    }

    // private constructor
    private ToneUtils() {
    }

    /// inner classes
    // the input stream of generated tone's audio data
    private static class ToneAudioInputStream extends InputStream {
        private final byte[] buffer;
        private int index;

        private ToneAudioInputStream(byte[] buffer) {
            this.buffer = buffer;
            index = 0;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            } else {
                int data = nextByte();
                if (data == -1) {
                    return -1;
                }
                b[off] = (byte) data;
                int stored = 1;
                for (; stored < len; stored++) {
                    data = nextByte();
                    if (data == -1) {
                        break;
                    }
                    b[off + stored] = (byte) data;
                }
                return stored;
            }
        }

        @Override
        public int read() throws IOException{
            throw new IOException("Not implemented here.");
        }

        // getting next read byte value or -1
        private int nextByte() {
            return buffer == null || buffer.length == 0 ? -1 : fromRing();
        }

        // getting byte from buffer's ring
        private int fromRing() {
            try {
                return buffer[index];
            } finally {
                final int followingIndex = index + 1;
                index = followingIndex >= buffer.length ? 0 : followingIndex;
            }
        }
    }
}
