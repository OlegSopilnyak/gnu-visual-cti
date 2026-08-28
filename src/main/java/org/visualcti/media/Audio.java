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

import java.io.IOException;
import java.util.*;

import javax.media.format.*;

/**
 * enumeration describe the formats of audio data
 * this is proxy between device and audio formats
 */
public enum Audio implements AudioMedia {
    //<predefined audios>
    //
    // Algorithm  = ULAW, Sample rate = 8000 samples per second
    ULAW_8(_Ulaw_ALG, 8000),
    // Algorithm  = ALAW, Sample rate = 8000 samples per second
    ALAW_8(_Alaw_ALG, 8000),
    // Algorithm  = LINEAR 8, Sample rate = 8000 samples per second
    LINEAR(_Linear_ALG, 8000),
    // Algorithm  = LINEAR 8, Sample rate = 8000 samples per second
    LINEAR_8(_Linear8_ALG, 8000),
    // Algorithm  = LINEAR 8, Sample rate = 11025 samples per second
    LINEAR_11(_Linear_ALG, 11025),
    LINEAR_16(_Linear16_ALG, 11025),
    // Algorithm  = Dialogic/OKI, Sample rate = 6000 samples per second
    ADPCM_6(_Dialogic_ALG, 6000),
    // Algorithm  = Dialogic/OKI, Sample rate = 8000 samples per second
    ADPCM_8(_Dialogic_ALG, 8000);

    /**
     * <accessor>
     * To get the Audio from AudioFormat
     *
     * @see AudioFormat
     */
    public static Audio from(final AudioFormat format) throws UnsupportedFormatException {
        final double sampleRate = format.getSampleRate();
        try {
            AudioMedia.checkSampleRate((double) sampleRate);
        } catch (IOException e) {
            throw new UnsupportedFormatException(format);
        }
        final String algorithmName;
        final String formatEncodingName = format.getEncoding();
        switch (formatEncodingName) {
            case AudioFormat.ALAW:
                algorithmName = Audio._Alaw_ALG;
                break;
            case AudioFormat.ULAW:
                algorithmName = Audio._Ulaw_ALG;
                break;
            case AudioFormat.LINEAR:
                algorithmName = Audio._Linear8_ALG;
                break;
            default:
                throw new UnsupportedFormatException(format);
        }
        return fromString(algorithmName + "/" + sampleRate);
    }

    /**
     * <accessor>
     * To get access to te audio as AudioFormat
     *
     * @return the format or null if invalid
     * @see AudioFormat
     */
    public AudioFormat toAudioFormat() {
        return new AudioFormat(codec, sampleRate, 8, 1);
    }

    /**
     * <accessor>
     * to restore object from string
     */
    public static Audio fromString(final String mediaFormatValue) {
        try {
            final String[] split = mediaFormatValue.split("/");
            final String algorithmName = split[0].trim();
            final short sampleRate = Short.parseShort(split[1].trim());
            return Arrays.stream(values()).filter(format ->
                    format.codec.equalsIgnoreCase(algorithmName) && format.sampleRate == sampleRate
            ).findFirst().orElse(null);
        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    // Name of codec(algorithm)
    private final String codec;
    // The sample rate of audio data
    private final short sampleRate;

    /**
     * <accessor>
     * get sample rate of audio data
     */
    public final short getSampleRate() {
        return this.sampleRate;
    }

    /**
     * <constructor>
     * For internal use only!
     */
    Audio(String codec, int sampleRate) {
        this.codec = codec;
        this.sampleRate = (short) sampleRate;
    }

    @Override
    public String toString() {
        return this.codec + "/" + this.sampleRate;
    }
}
