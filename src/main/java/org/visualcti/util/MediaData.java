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
package org.visualcti.util;

import java.io.*;
/**
Class for solve raw-data bytes array
*/
public class MediaData
{
    /** class for define content types */
    public static class Type 
    {
/** unknown type */
public static final int UNKNOWN = 0;
/** content is wav-sound */
public static final int SOUND = 1;
/** content is tiff-fax */
public static final int FAX = 2;
    }
    /**
    To check header of raw-data for understand data-type
    */
    public static int getTypeByHeader(byte[]header)
    {
        if (MediaData.isWaveHeader(header)) return Type.SOUND;
        else
        if (MediaData.isTiffHeader(header)) return Type.FAX;
        else
        return Type.UNKNOWN;
    }
        /**
        test, if data is WAV header
        */
        private static boolean isWaveHeader(byte[]data){
            try{
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                DataInputStream source = new DataInputStream( in );
                int riffField = source.readInt();
                int length    = rllong(source);
                int waveField = source.readInt();
                return  
                    riffField == RIFF_MAGIC &&
                    waveField == WAVE_MAGIC;
            }catch(IOException e){}
            return false;
        }
        /**
        test, if data is TIFF header
        */
        private static boolean isTiffHeader(byte[]data){
		    int header = getDWord(data,0);
		    return
		        header == 0x002a4949 ||
		        header == 0x2a004d4d;
        }
/** values of some wav header parts */    
final static int RIFF_MAGIC = 0x52494646;
/** values of some wav header parts */    
final static int WAVE_MAGIC = 0x57415645;
/** values of some wav header parts */    
final static int WAVE_FORMAT_PCM = 1;
/** values of some wav header parts */    
final static int WAVE_FORMAT_ADPCM = 2;
/** values of some wav header parts */    
final static int WAVE_FORMAT_ALAW = 6;
/** values of some wav header parts */    
final static int WAVE_FORMAT_MULAW = 7;
        /**
        to get value of audio-codec as string
        */
        private static String getWaveCodec(int codec){
            switch(codec){
                case WAVE_FORMAT_PCM:   return "PCM";
                case WAVE_FORMAT_ADPCM: return "ADPCM";
                case WAVE_FORMAT_ALAW:  return "ALAW";
                case WAVE_FORMAT_MULAW: return "ULAW";
            }
            return "???";
        }
    /**
    Test, if data is valid WAV file image
    */
    public static boolean isValidWave(byte[] data,StringBuffer info){
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DataInputStream source = new DataInputStream( in );
        try {
            byte buf[]=new byte[4];
            int nread = 0;
            int riffField = source.readInt(); nread += 4;
            int length = rllong(source);  nread += 4;
            int waveField = source.readInt(); nread += 4;
            int bytesRead;
            if(riffField != RIFF_MAGIC || waveField != WAVE_MAGIC) {
                throw new IOException("header Magics is Bad");
            }
            do {
                bytesRead = source.read(buf, 0, 4);nread += 4;
                if(bytesRead == -1){
                    /* unexpected end of file */
                    break;
                }
                String s = new String(buf);
                if( s.startsWith("fmt ") ){
                    /* finded signature of format chunk */
                    break;
                }
                /* skip not format chunk */
                length = rllong(source);  nread += 4;
                if(length % 2 > 0)     length++;
                nread += source.skipBytes(length);
            } while(true);
            if(bytesRead == -1){
                /* this is not wav (or damaged) file exception */
                throw new IOException("InputStream is not a WAV file");
            }
            info.append("Sound WAV ");
            length = rllong(source);
            int endLength = (nread += 4) + length;
            int wav_type = rlshort(source); nread += 2;
            info.append( getWaveCodec(wav_type) );info.append(", ");
                    
            int channels = rlshort(source);nread += 2;
                    
            double sampleRate = rllong(source); nread += 4;
            info.append( sampleRate ); info.append(" Hz");
                    
            int avgBytesPerSec = rllong(source); nread += 4;
            short blockAlign = rlshort(source);  nread += 2;
            int sampleSizeInBits = rlshort(source);  nread += 2;
            return true;
        }catch(IOException e){
            return false;
        }
    }
    /**
    Check, if data is valid TIFF file image */
    public static boolean isValidTiff(byte[] buffer, StringBuffer info){
		int header = getDWord(buffer,0);
		if(header!=0x002a4949 && header!=0x2a004d4d) return false;
		int firstIFD = getDWord(buffer,4);
		int tags = getWord(buffer, firstIFD);
		int tagpointer = firstIFD + 2;
		int dtsp=0, dtcnt=0;
		int imageh=-1, rps=-1, iwidth=0;
		int compression = 0;
		int badlines,pages = -1;
		for(int t = 0; t < tags*12 ;t += 12){
			int pointer = getWord (buffer, tagpointer+t  );
			int dtt     = getWord (buffer, tagpointer+t+2);
			int dtc     = getDWord(buffer, tagpointer+t+4);
			int dto     = getDWord(buffer, tagpointer+t+8);
			switch(pointer){
			case 259:/* COMRESSIONN TAG */
				if(dtt == 4) {
				    compression = getDWord(buffer, tagpointer+t+8);
				}else if(dtt == 3) {
				    compression = getWord (buffer, tagpointer+t+8);
				}
				if(compression!=3 && compression!=4) return false;
				break;
			case 297:/* PAGE COUNT TAG */
				pages = getWord(buffer, tagpointer+t+10);
				break;
			case 256:/* IMAGE WIDTH TAG */
				iwidth = getDWord(buffer, tagpointer+t+8);
				break;
			case 326:/* BAD LINES TAG */
				badlines = getDWord(buffer, tagpointer+t+8);
				break;
			case 257:/* IMAGE HIGH TAG */
				imageh = getDWord(buffer, tagpointer+t+8);
				break;
			case 278:/* ROWS PER STRIP TAG */
				if(dtt == 4) {
				    rps = getDWord(buffer, tagpointer+t+8);
				}else if(dtt == 3) {
				    rps = getWord(buffer, tagpointer+t+8);
				}
			}
		}
		if
		    (
		    imageh != -1    &&
		    rps != -1       && 
		    imageh != rps   || 
		    iwidth == 0
		    )
		{
			return false;
		}
		if(pages == 0 || pages == -1){
			int nextIFD = firstIFD, ifds = 0;
			do{
				tags    = getWord (buffer, nextIFD);
				nextIFD = getDWord(buffer, nextIFD+2 + (tags*12) );
				ifds++;
			}while(nextIFD != 0);
			int ifd = firstIFD;
			for(int i=0; i < ifds ;i++){
				tags = getWord(buffer, ifd);
				if(pages == -1) {
					byte[] pgtag = {41,1,3,0,2,0,0,0};
        					
					for(int t=0; t<tags*12; t=t+12){
						int tnum = getWord(buffer, ifd+2+t);
						if  (
						    tnum==258 || tnum==259 || tnum==266 || 
						    tnum==256 || tnum==277 || tnum==257 || 
						    tnum==292 || tnum==282 || tnum==262 ||
							tnum==278 || tnum==293 || tnum==283 || 
							tnum==273 || tnum==279
							) continue;
						for(int j=0; j < 8 ;j++){
							buffer[ ifd+2+t+j ] = pgtag[j];
						}
						break;
					}
				}
				for(int t = 0; t<tags*12 ;t += 12){		
					if( getWord(buffer, ifd+2+t) == 297 ){
						buffer[ifd+t+10] = loByte(i);
						buffer[ifd+t+11] = hiByte(i);
						buffer[ifd+t+12] = loByte(ifds);
						buffer[ifd+t+13] = hiByte(ifds);
					}
				}
				ifd = getDWord(buffer, ifd+2+tags*12);
			}
		}
		info.append("TIFF G").append(compression).append(", ");
		info.append(imageh).append("x").append(iwidth);
		info.append(", pages ").append(pages);
		return true;
    }
            protected static int rllong(DataInputStream dis)
                throws IOException
            {
                int i = dis.readInt();
                int b1 = (i & 0xff) << 24;
                int b2 = (i & 0xff00) << 8;
                int b3 = (i & 0xff0000) >> 8;
                int b4 = (i & 0xff000000) >>> 24;
                i = b1 | b2 | b3 | b4;
                return i;
            }
            protected static short rlshort(DataInputStream dis)
                throws IOException
            {
                short s = 0;
                s = dis.readShort();
                short high = (short)((s & 0xff) << 8);
                short low = (short)((s & 0xff00) >>> 8);
                s = (short)(high | low);
                return s;
            }
	        protected static int getDWord(byte[] array, int offset){
		        int dw = 0;
		        try{
		            dw = 
		                    array[offset+3]<<24                |
		                (array[offset+2]<<16) & 0x00ff0000  |
			            (array[offset+1]<< 8) & 0x0000ff00  |
			                array[offset]        & 0x000000ff;
		        }catch(ArrayIndexOutOfBoundsException e){}
		        return dw;
	        }
	        protected static int getWord(byte[] array, int offset){
		        int w=0;
		        try{
		            w = 
		                (array[offset+1]<<8)& 0x0000ff00 |
		                    array[offset]      & 0x000000ff;
		        }catch(ArrayIndexOutOfBoundsException e){return 0;}
		        return w;
	        }
	        static protected byte loByte(int value){
		        return (byte)value;
	        }
	        static protected byte hiByte(int value){
		        return (byte)(value>>8);
	        }
}
