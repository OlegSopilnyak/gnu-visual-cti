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

import java.io.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * To build the Immages of Media-files</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public class mediaProducer
{
  /**
   * <producer>
   * To make the body of sound's file by format & raw-data
   * @param format the format of the sound
   * @param rawdata the body of the sound
   * @return the image of sound's file
   */
  public static final byte[] makeSoundImage(Audio format,byte[]rawdata)
  {
    if ( Audio.ADPCM_6.equals(format) || Audio.ADPCM_8.equals(format) )
    {
      // Dialogic's VOX format
      return rawdata;
    }
    // to make the header of WAV
    wavHeader header = new wavHeader();
    header.setChannels(1);
    header.setDataLength(rawdata.length);
    header.setSampleSizeInBits(8);
    if ( Audio.ALAW_8.equals(format) )
    {
      header.setWavType(WAVE_FORMAT_ALAW);
      header.setSampleRate(8000);
    }else
    if ( Audio.LINEAR.equals(format) )
    {
      header.setWavType(WAVE_FORMAT_PCM);
      header.setSampleRate(8000);
    }else
    if ( Audio.LINEAR_8.equals(format) )
    {
      header.setWavType(WAVE_FORMAT_PCM);
      header.setSampleRate(8000);
    }else
    if ( Audio.LINEAR_11.equals(format) )
    {
      header.setWavType(WAVE_FORMAT_PCM);
      header.setSampleRate(11025);
    }else
    if ( Audio.ULAW_8.equals(format) )
    {
      header.setWavType(WAVE_FORMAT_MULAW);
      header.setSampleRate(8000);
    }else return rawdata;
    ByteArrayOutputStream image = new ByteArrayOutputStream();
    try{
      image.write(header.toByteArray()); image.write(rawdata);
      rawdata = image.toByteArray(); image.close(); image = null;
    }catch(IOException e){}
    return rawdata;
  }
/***
 * <wrapper>
 * The class for wrap the information about WAV's header
 */
private static final class wavHeader{
  private int wavType = -1;
  private final void setWavType(int wavType){this.wavType=wavType;}
  private int channels = -1;
  private final void setChannels(int channels){this.channels=channels;}
  private double sampleRate = -1;
  private final void setSampleRate(double sampleRate){this.sampleRate=sampleRate;}
  private int sampleSizeInBits=-1;
  private final void setSampleSizeInBits(int sampleSizeInBits){this.sampleSizeInBits=sampleSizeInBits;}
  private int dataLength=-1;
  private final void setDataLength(int dataLength){this.dataLength=dataLength;}
  private final byte[] toByteArray(){
      ByteArrayOutputStream buffer=new ByteArrayOutputStream();
      DataOutputStream data=new DataOutputStream(buffer);
      try {
          data.writeInt(RIFF_MAGIC);
          if((long)this.dataLength != -1L) {
              int riffLength = (this.dataLength + WAVE_HEADERSIZE) - 8;
              data.writeInt(big2little(riffLength));
          } else      data.writeInt(-1);
          data.writeInt(WAVE_MAGIC);
          data.writeInt(FMT_MAGIC);
          int fmtLength = 16;
          data.writeInt(big2little(fmtLength));
          data.writeShort(big2littleShort((short)this.wavType));
          data.writeShort(big2littleShort((short)this.channels));
          data.writeInt(big2little((short)this.sampleRate));
          int avgBytesPerSec = (this.channels * this.sampleSizeInBits * (int)this.sampleRate) / 8;
          data.writeInt(big2little(avgBytesPerSec));
          short blockAlign = (short)((this.sampleSizeInBits / 8) * this.channels);
          data.writeShort(big2littleShort(blockAlign));
          data.writeShort(big2littleShort((short)this.sampleSizeInBits));
          data.writeInt(DATA_MAGIC);
          data.writeInt(big2little(this.dataLength));
          data.flush(); data.close();
      } catch(IOException e){
      }
      return buffer.toByteArray();
  }
}
private static final int big2little(int i)
{
    int b1 = (i & 0xff) << 24;
    int b2 = (i & 0xff00) << 8;
    int b3 = (i & 0xff0000) >> 8;
    int b4 = (i & 0xff000000) >>> 24;
    i = b1 | b2 | b3 | b4;
    return i;
}

private static final short big2littleShort(short i)
{
    short high = (short)((i & 0xff) << 8);
    short low = (short)((i & 0xff00) >>> 8);
    i = (short)(high | low);
    return i;
}
/* The consts of Sounds  */
public static final int RIFF_MAGIC = 0x52494646;
public static final int WAVE_MAGIC = 0x57415645;
public static final int FMT_MAGIC = 0x666d7420;
public static final int DATA_MAGIC = 0x64617461;
public static final int FACT_MAGIC = 0x66616374;

public static final int WAVE_FORMAT_UNKNOWN = 0;
public static final int WAVE_FORMAT_PCM = 1;
public static final int WAVE_FORMAT_ADPCM = 2;
public static final int WAVE_FORMAT_ALAW = 6;
public static final int WAVE_FORMAT_MULAW = 7;
public static final int WAVE_FORMAT_OKI_ADPCM = 16;
public static final int WAVE_FORMAT_DIGISTD = 21;
public static final int WAVE_FORMAT_DIGIFIX = 22;
public static final int WAVE_FORMAT_DIALOGIC_ADPCM = 23;
public static final int WAVE_FORMAT_TRUESPEECH = 0x22;
public static final int WAVE_IBM_FORMAT_MULAW = 257;
public static final int WAVE_IBM_FORMAT_ALAW = 258;
public static final int WAVE_IBM_FORMAT_ADPCM = 259;
public static final int WAVE_FORMAT_DVI_ADPCM = 17;
public static final int WAVE_FORMAT_SX7383 = 7175;
public static final int WAVE_HEADERSIZE = 44;
public static final int WAVE_DATASIZE_OFFSET = 40;
public static final int WAVE_MICROSOFT_G723 =0x42;
public static final int WAVE_GSM_610 =0x31;
}
