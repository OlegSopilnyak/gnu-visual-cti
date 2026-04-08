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
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The parser of media streams</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public final class mediaParser {
/**
 * <producer>
 * To make a Sound object from the stream ;-)
 * @param source
 * @return
 */
  public static final Sound getSound(InputStream source, Audio rawFormat)
  {
    // by default, Dialogic RAW format
    Audio format = rawFormat;
    QuotedInputStream data = new QuotedInputStream(source);
    try{
      // to move stream's point to begin of format's data
      int nread = mediaParser.skipToFormatChunk(data);
      wavHeader header = new wavHeader();
      // to restore the wav_type
      int length = rllong(data);
      int endLength = (nread += 4) + length;
      int wav_type = rlshort(data); nread += 2;
      header.setWavType(wav_type);
      // to restore the channels
      int channels = rlshort(data);nread += 2;
      header.setChannels(channels);
      // to restore the sample rate
      double sampleRate = rllong(data); nread += 4;
      header.setSampleRate(sampleRate);
      // to restore the sample's size in bits
      int avgBytesPerSec = rllong(data); nread += 4;
      short blockAlign = rlshort(data);  nread += 2;
      int sampleSizeInBits = rlshort(data);  nread += 2;
      header.setSampleSizeInBits(sampleSizeInBits);
      if(length % 2 != 0)  length++;
      // to skip other chunk's information
      int skipping = endLength - nread;
      if (skipping > 0) {
          // skip format's extensions
          nread += data.skipBytes(skipping);
      }else
      if (skipping < 0) throw new IOException("Invalid length of format chunk");
      // finally skip to the data's chunk
      nread = mediaParser.skipToDataChunk(data,header,nread);
      // to make the format from the header
      format = mediaParser.restoreAudio(header);
      if ( format == null ) return null;// unknown format
    }catch(IOException e){
      // not a WAV format, or stream's I/O exception
    }
    return new soundWrapper(format, data );
  }
/**
 * <producer>
 * To make the audio format from wav's header
 * @param header the header
 * @return format or null id not supported Wav format
 */
private static final Audio restoreAudio(wavHeader header){
  int sampleRate = (int)header.sampleRate;
  switch( header.wavType ){
    case WAVE_FORMAT_PCM:
      if (sampleRate == 8000) return Audio.LINEAR;
      else
      if (sampleRate == 11025) return Audio.LINEAR_11;
      break;
    case WAVE_FORMAT_MULAW:
      if (sampleRate == 8000) return Audio.ULAW_8;
      break;
    case WAVE_FORMAT_ALAW:
      if (sampleRate == 8000) return Audio.ALAW_8;
      break;
    case WAVE_FORMAT_DIALOGIC_ADPCM:
    case WAVE_FORMAT_OKI_ADPCM:
      if (sampleRate == 6000) return Audio.ADPCM_6;
      else
      if (sampleRate == 8000) return Audio.ADPCM_8;
      break;
  }
  return null;
}
/**
 * <parser>
 * To read and solve data's chunk
 * @param data the stream
 * @param header the store
 * @param nread bytes count
 * @return position in the stream (header size)
 * @throws IOException if no data's chunk, or some wrong in wrapped stream
 */
private final static int skipToDataChunk
                          (
                          QuotedInputStream data,
                          wavHeader header,
                          int nread
                          ) throws IOException {
  byte chunkHeader[]=new byte[4];
  do {
      int bytesRead = data.read(chunkHeader, 0, 4); nread += 4;
      if(bytesRead == -1)
          throw new IOException("reached EOF before finding data chunk");
      String chunkName = new String(chunkHeader);
      if( !chunkName.startsWith("data") ) {
          // skip not a data's chunk
          int length = rllong(data); nread += 4;
          if(length % 2 > 0) length++;
          nread += data.skipBytes(length);
      } else  {
          // data chunk signature finded, store the data's length
          int length = rllong(data);
          header.setDataLength(length);

          int offset = nread += 4;
          // The stream to audio data
          return offset;
      }
  }while(true);
}
/**
 * <parser>
 * To read & check the header of the stream
 * @param data the stream
 * @return ount of used bytes
 * @throws IOException if som wrong in wrapped stream
 */
private static int skipToFormatChunk(QuotedInputStream data) throws IOException {
    int nread = 0;
    // try solve the magics fields
    int riffField = data.readInt(); nread += 4;
    int length = rllong(data);  nread += 4;
    int waveField = data.readInt(); nread += 4;
    if(riffField != RIFF_MAGIC || waveField != WAVE_MAGIC)
        throw new IOException("header's Magics is Bad");

    byte chunkHeader[]=new byte[4];
    int bytesRead = -1;
    do {// to read a chunk's header
        bytesRead = data.read(chunkHeader, 0, 4);nread += 4;
        if(bytesRead == -1){
            // unexpected end of file
            break;
        }
        // to make the chunk's name
        String chunkName = new String(chunkHeader);
        if( chunkName.startsWith("fmt ") ){
            // finded signature of chunk of format
            break;
        }
        // to skip not format's chunk
        length = rllong(data);  nread += 4;
        if(length % 2 > 0)     length++;
        nread += data.skipBytes(length);
    } while(true);
    // to solve the reads
    if(bytesRead == -1){
        // this is not wav (or damaged) file exception
        throw new IOException("InputStream is not a WAV file");
    }
    // the count of used bytes
    return nread;
}
/*<reader> to read 4 bytes */
private final static int rllong(QuotedInputStream data) throws IOException {
  int integer = data.readInt();
  int b1 = (integer & 0xff) << 24;
  int b2 = (integer & 0xff00) << 8;
  int b3 = (integer & 0xff0000) >> 8;
  int b4 = (integer & 0xff000000) >>> 24;
  integer = b1 | b2 | b3 | b4;
  return integer;
}
/*<reader> to read 2 bytes */
private final static short rlshort(QuotedInputStream data)throws IOException {
  short short_integer = data.readShort();
  short high = (short)((short_integer & 0xff) << 8);
  short low = (short)((short_integer & 0xff00) >>> 8);
  short_integer = (short)(high | low);
  return short_integer;
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
}
/***
 * <stream>
 * Inner class
 * The InputStream for quoted read the media data
 */
private static final class QuotedInputStream extends InputStream {
    private boolean isQuoted=false;
    private int length=-1;
    private final InputStream in;
    public QuotedInputStream(InputStream source){this.in=source;}
    /**
     * <action>
     * To close wrapped stream
     * @throws IOException if some wrong
     */
    public final void close() throws IOException{this.in.close();}
    /**
     * <mutator>
     * To setup max length of the Stream
     * @param length maximum bytes quantity
     */
    public final void setLength(int length)
    {
        this.length=length; this.isQuoted=true;
    }
    /**
     * <reader>
     * To read the buffer
     * @param b the buffer
     * @param off buffer's offset
     * @param len bytes quantity
     * @return count of bytes
     * @throws IOException if some wrong wrapped stream
     */
    public final int read(byte[] b, int off, int len) throws IOException
    {
        if ( this.isQuoted )
        {   // quoted read
            if (this.length <= 0) return -1;
            len = Math.min(len,this.length);
            int count=this.in.read(b,off,len);
            if (this.length < count)
            {
                count = this.length; this.length=0;
            } else {
                this.length -= count;
            }
            return count;
        } else
          // unquoted read
          return in.read(b,off,len);
    }
    /**
     * <reader>
     * To read One byte
     * @return the byte
     * @throws IOException if some wrong in wrapped stream
     */
    public final int read() throws IOException
    {
        if (this.isQuoted){
            if (this.length <= 0) return -1;
            else this.length--;
        }
        // to read from the wrapped stream
        return in.read();
    }
    /**
     * <reader>
     * To read a 4 bytes & convert it to "int"
     * @return the int
     * @throws IOException is some wrong in wrapped stream
     */
    private final int readInt() throws IOException {
            int ch1 = this.in.read();
            int ch2 = this.in.read();
            int ch3 = this.in.read();
            int ch4 = this.in.read();
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    /**
     * <reader>
     * To read a 2 bytes & convert it to "short"
     * @return the short
     * @throws IOException is some wrong in wrapped stream
     */
    private final short readShort() throws IOException {
            int ch1 = in.read();
            int ch2 = in.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();
            return (short)((ch1 << 8) + (ch2 << 0));
        }
    /**
     * <reader>
     * To skip the bytes in wrapped stream
     * @param n bytes count to skip
     * @return the count
     * @throws IOException is some wrong in wrapped stream
     */
    private final int skipBytes(int n) throws IOException {
            for (int i = 0 ; i < n ; i += (int)in.skip(n - i));
            return n;
    }
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
