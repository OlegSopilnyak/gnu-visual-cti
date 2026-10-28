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
package org.visualcti.media;

import javax.sound.sampled.AudioFormat;

import java.io.IOException;
import java.util.Arrays;
import org.visualcti.util.Tools;

/**
 * enumeration describes the formats of audio data
 * this is proxy between device and audio formats
 */
public enum Audio implements AudioMedia {
    //<predefined audios>
    //
    // Algorithm = ULAW, Sample rate = 8000 samples per second
    ULAW_8(_Ulaw_ALG, 8000),
    // Algorithm = ALAW, Sample rate = 8000 samples per second
    ALAW_8(_Alaw_ALG, 8000),
    // Algorithm = LINEAR 8, Sample rate = 8000 samples per second
    LINEAR(_Linear_ALG, 8000),
    // Algorithm = LINEAR 8, Sample rate = 8000 samples per second
    LINEAR_8(_Linear8_ALG, 8000),
    // Algorithm = LINEAR 8, Sample rate = 11,025 samples per second
    LINEAR_11(_Linear_ALG, 11025),
    LINEAR_16(_Linear16_ALG, 11025),
    // Algorithm = Dialogic/OKI, Sample rate = 6000 samples per second
    ADPCM_6(_Dialogic_ALG, 6000),
    // Algorithm = Dialogic/OKI, Sample rate = 8000 samples per second
    ADPCM_8(_Dialogic_ALG, 8000);

    /**
     * <finder>
     * To find the Audio instance by the AudioFormat
     *
     * @param format the audio format correlated with the Audio
     * @return found Audio or null
     * @see AudioFormat
     */
    public static Audio of(final AudioFormat format) {
        // dealing with format's sample rate
        final float sampleRate = format.getSampleRate();
        try {
            AudioMedia.checkSampleRate(sampleRate);
        } catch (IOException e) {
            e.printStackTrace(Tools.err);
            return null;
        }
        // dealing with format's encoding
        final AudioFormat.Encoding encoding = format.getEncoding();
        if (AudioFormat.Encoding.ALAW.equals(encoding)) {
            return of(AudioMedia._Alaw_ALG + "/" + sampleRate);
        } else if (AudioFormat.Encoding.ULAW.equals(encoding)) {
            return of(AudioMedia._Ulaw_ALG + "/" + sampleRate);
        } else if (AudioFormat.Encoding.PCM_SIGNED.equals(encoding)) {
            return of(AudioMedia._Linear8_ALG + "/" + sampleRate);
        }
        // Not supported encoding algorithm
        return null;
    }

    /**
     * <finder>
     * to find the Audio instance by the String
     *
     * @param audioAsString the Audio as String presentation
     * @return found Audio or null
     */
    public static Audio of(final String audioAsString) {
        try {
            final String[] split = audioAsString.split("/");
            final String algorithmName = split[0].trim();
            final short sampleRate = Short.parseShort(split[1].trim());
            return Arrays.stream(values()).filter(format ->
                    format.codec.equalsIgnoreCase(algorithmName) && format.sampleRate == sampleRate
            ).findFirst().orElse(null);
        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Something went wrong during audio as string parsing
            return null;
        }
    }

    /**
     * <converter>
     * To convert the Audio to the AudioFormat
     *
     * @return the format or null if invalid
     * @see AudioFormat
     */
    public AudioFormat toFormat() {
        switch (codec) {
            case _Linear_ALG:
            case _Linear8_ALG:
                return new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                        sampleRate, 8, 1, 1, sampleRate, false);
            case _Linear16_ALG:
                return new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                        sampleRate, 16, 1, 1, sampleRate, false);
            case _Alaw_ALG:
                return new AudioFormat(AudioFormat.Encoding.ALAW,
                        sampleRate, 8, 1, 1, sampleRate, false);
            case _Ulaw_ALG:
                return new AudioFormat(AudioFormat.Encoding.ULAW,
                        sampleRate, 8, 1, 1, sampleRate, false);
            default:
                // Unknown codec type
                return null;
        }
    }

    // Name of codec(algorithm)
    private final String codec;
    // The sample rate of audio data
    private final short sampleRate;

    /**
     * <constructor>
     * For internal usage only!
     *
     * @param codec name of the audio codec algorithm
     * @param sampleRate sample rate of the audio stream data
     */
    Audio(String codec, int sampleRate) {
        this.codec = codec;
        this.sampleRate = (short) sampleRate;
    }

    /**
     * <accessor>
     * get sample rate of audio data
     *
     * @return the value
     */
    public final short getSampleRate() {
        return sampleRate;
    }

    @Override
    public String toString() {
        return codec + "/" + sampleRate;
    }
}
