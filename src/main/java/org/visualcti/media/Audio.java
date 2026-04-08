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

Contact oleg@visualcti.org or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg@visualcti.org
Home Phone:	380-62-3851086 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.media;

import java.io.*;
import java.util.*;
import javax.media.format.*;
/**
class describe the format of audio data
this is proxy between device and audio formats
*/
public final class Audio
{
/**
<predefined algorithm>
Algorithm  = ULAW
*/
public static final String _Ulaw_ALG = "ULAW";
/**
<predefined algorithm>
Algorithm  = ALAW
*/
public static final String _Alaw_ALG = "ALAW";
/**
<predefined algorithm>
Algorithm  = LINEAR (8 bit sample)
*/
public static final String _Linear8_ALG = "LINEAR8";
/**
<predefined algorithm>
Algorithm  = LINEAR (8 bit sample)
*/
public static final String _Linear_ALG = "LINEAR";
/**
<predefined algorithm>
Algorithm  = LINEAR (16 bit sample)
*/
public static final String _Linear16_ALG = "LINEAR16";
/**
<predefined algorithm>
Algorithm  = Dialogic/OKI
*/
public static final String _Dialogic_ALG = "OKI";
/**
valid algorithms pool
*/
private final static String valid_ALG[] = new String[]
{
    _Ulaw_ALG,
    _Alaw_ALG,
    _Linear_ALG,
    _Linear8_ALG,
    _Linear16_ALG,
    _Dialogic_ALG
};
/**
valid sample rates pool
*/
private final static double valid_RATE[] = new double[]
{
    6000,
    8000,
    11025
};
/**
to check audio codec algorithm name
*/
private static void checkAlgorithm(String algorithm) throws Exception
{
    for(int i=0;i < valid_ALG.length;i++) {
        if (valid_ALG[i].equals(algorithm)) return;
    }
    throw new Exception("invalid audio algorithm "+algorithm);
}
/**
to check audio sample rate
*/
private static void checkSampleRate(double rate) throws Exception
{
    for(int i=0;i < valid_RATE.length;i++) {
        if (rate == valid_RATE[i]) return;
    }
    throw new Exception("invalid sample rate = "+rate);
}
/**
validate the Audio object
*/
private static Audio valid(Audio audio) throws Exception
{
    Audio.checkAlgorithm(audio.getCodec());
    Audio.checkSampleRate(audio.getSampleRate());
    return audio;
}
/**
<predefined audio>
Algorithm  = ULAW, Sample rate = 8000 samples per second
*/
public static final Audio ULAW_8 = new Audio(_Ulaw_ALG,8000);
/**
<predefined audio>
Algorithm  = ALAW, Sample rate = 8000 samples per second
*/
public static final Audio ALAW_8 = new Audio(_Alaw_ALG,8000);
/**
<predefined audio>
Algorithm  = LINEAR 8, Sample rate = 8000 samples per second
*/
public static final Audio LINEAR = new Audio(_Linear_ALG,8000);
/**
<predefined audio>
Algorithm  = LINEAR 8, Sample rate = 8000 samples per second
*/
public static final Audio LINEAR_8 = new Audio(_Linear8_ALG,8000);
/**
<predefined audio>
Algorithm  = LINEAR 8, Sample rate = 11025 samples per second
*/
public static final Audio LINEAR_11 = new Audio(_Linear_ALG,11025);
/**
<predefined audio>
Algorithm  = Dialogic/OKI, Sample rate = 6000 samples per second
*/
public static final Audio ADPCM_6 = new Audio(_Dialogic_ALG,6000);
/**
<predefined audio>
Algorithm  = Dialogic/OKI, Sample rate = 8000 samples per second
*/
public static final Audio ADPCM_8 = new Audio(_Dialogic_ALG,8000);
/**
<attribute>
Name of codec(algorithm)
*/
private String codec;
    /**
    <accessor>
    get Codec's name
    */
    public final String getCodec(){return this.codec;}
/**
The sample rate of audio data
*/
private short sampleRate;
    /**
    <accessor>
    get sample rate of audio data
    */
    public final short getSampleRate(){return this.sampleRate;}
    /**
    <constructor>
    For internal use only!
    */
    private Audio(String codec,int sampleRate)
    {
        this.codec=codec; this.sampleRate=(short)sampleRate;
    }
    /**
     * <mutator>
     * To adjust the Audio from AudioFormat
     * */
    public final Audio apply
                          (
                          AudioFormat format
                          )
                          throws UnsupportedFormatException
    {
      double sampleRate = format.getSampleRate();
      String codec = "???";
      try {this.checkSampleRate(sampleRate);
      }catch(Exception e){
        throw new UnsupportedFormatException(format);
      }
      String coder = format.getEncoding();
      if ( coder.equals(AudioFormat.ALAW) )
        codec = Audio._Alaw_ALG; else
      if ( coder.equals(AudioFormat.ULAW) )
        codec = Audio._Ulaw_ALG; else
      if ( coder.equals(AudioFormat.LINEAR) )
        codec = Audio._Linear8_ALG;
      else
        throw new UnsupportedFormatException(format);
      try {
        this.checkAlgorithm(codec);
      }catch(Exception e){
        throw new UnsupportedFormatException(format);
      }
      // to apply changes
      this.codec = codec;
      this.sampleRate = (short)sampleRate;
      return this;
    }
    /**
     * <accessor>
     * To get access to AudioFormat
     * @return the format or null if invalid
     */
    public final AudioFormat getAudioFormat()
    {
      if ( this.codec == null ) return null;
      String coder = "???";
      if ( this.codec.equalsIgnoreCase(AudioFormat.ALAW) )
        coder = AudioFormat.ALAW; else
      if ( this.codec.equalsIgnoreCase(AudioFormat.ULAW) )
        coder = AudioFormat.ULAW; else
      if ( this.codec.equalsIgnoreCase(AudioFormat.LINEAR) )
        coder = AudioFormat.LINEAR;
      else return null;
      return new AudioFormat( coder, this.sampleRate, 8, 1 );
    }
    /**
    String representation of object
    */
    public final String toString(){return this.codec+"/"+this.sampleRate;}
    /**
    equals
    */
    public final boolean equals(Object o)
    {
        if      (o == this) return true;
        else if (o == null) return false;
        else if (o instanceof Audio)
        {
            Audio audio = (Audio)o;
            return audio.codec.equals(this.codec) && audio.sampleRate == this.sampleRate;
        }
        else return false;
    }
    /**
    hash code
    */
    public final int hashCode(){return this.codec.hashCode() ^ this.sampleRate;}
    /**
    to restore object from string
    */
    public static final Audio fromString(String audio)
    {
        StringTokenizer st = new StringTokenizer(audio,"/");
        try {
            String alg = st.nextToken();
            short rate = Short.parseShort(st.nextToken());
            return Audio.valid( new Audio(alg,rate) );// valid object
        }catch(Exception e){}
        return null;// some mistake detected
    }
    /**
     * <copy>
     * To make copy of Audio
     * @return the copy
     */
    public final Audio copy(){return new Audio(this.codec,this.sampleRate);}
}
