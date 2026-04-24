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
package org.visualcti.briquette.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.visualcti.briquette.Program;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.telephony.PlayAction;
import org.visualcti.media.Audio;
import org.visualcti.media.Sound;
import org.visualcti.media.mediaParser;
import org.visualcti.server.hardware.Tone;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow source to Voice stream</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public final class VoxSource extends mediaSource implements Sound
{
/**
 * <const>
 * The type of source, Text-to-Speech
 * */
public final static int TTS = 2;
/**
 * <const>
 * The type of source, Generated Tone
 * */
public final static int TONE = 3;
  /**
   * <accessor>
   * To get list of valid type's names
   * */
  public final List getTypes(){
    List types = new ArrayList(super.getTypes());
    types.add("TTS");types.add("TONE");
    return types;
  }
  /**
   * <accessor>
   * To get access to content's Symbol by default
   * */
  protected final Symbol defaultSymbol(int type)
  {
    switch( type )
    {
      case Media.RAW:
        return PlayAction.system_cti_Voice;
      case Media.FILE:
        return Symbol.newConst("sound.wav");
      case VoxSource.TTS:
        return Program.system_Date;
      case VoxSource.TONE:
        return Symbol.newConst( Tone.RINGBACK1 );
    }
    return Symbol.newConst("<none>");
  }
  /**
   * <accessor>
   * To get access to the name of group
   * */
  protected final String getMediaGroup(){return "VOX";}
  /**
   * <accessor>
   * to get access to RAW type ID
   * */
  protected final int getRawTypeID(){return Symbol.VOICE;}
  /**
   * <format>
   * The format of the stream
   * */
  private Audio format = Audio.ULAW_8;
  /**
   * <accessor>
   * To get access to format of the sream
   * */
  public final Audio getFormat(){return this.format;}
  /**
   * <mutator>
   * To setup the format of source
   * */
  public final void setFormat(Audio format) {this.format = format;}
  /**
   * <attribute>
   * The owner of the stream
   * */
  private Subroutine owner=null;
  /**
   * <mutator>
   * To assign the source with Subroutine
   * */
  public final void setOwner(Subroutine owner) {this.owner = owner;}
  /**
   * <accessor>
   * To get access sound's audio-data Stream
   * */
  public final InputStream getInputStream() throws IOException
  {
    if (this.owner == null) throw new IOException("Can't get access to owner of the source");
    return this.getInputStream( this.owner );
  }
  /**
   * <producer>
   * To make the stream to media source
   * */
  public final InputStream getInputStream(Subroutine caller) throws IOException
  {
    this.owner = caller;
    switch ( super.getType() )
    {
      case VoxSource.TTS:
        // get the text to talk
        String text = super.getContentValue( caller ).toString();
        // to make stream to Text-To-Speech
        return new ByteArrayInputStream(new byte[]{0});

      case VoxSource.TONE:
        // get the name of tone for play
        String signal = super.getContentValue( caller ).toString();
        if ( Arrays.asList(Tone.STATION).contains(signal) )
          // to make stream to Tone's generator
          return new ByteArrayInputStream(new byte[]{0});
        else
          throw new IOException("Can't generate the Signal ["+signal+"]");

      default:
        // try to solve the format of the stream
        return this.check( super.getInputStream(caller) );
    }
  }
/**
 * <attribute>
 * The format to play a raw data
 */
private Audio defaultFormat = Audio.LINEAR;
  /**
   * <mutator>
   * To assign the default format to play raw (without header) data's stream
   * @param format
   */
  public final void setDefaultFormat(Audio format){this.defaultFormat=format;}
  /**
   * <check>
   * to solve the format of audio stream
   * */
  private final InputStream check(InputStream in) throws IOException {
/*
    try {
      //Sound sound = mediaParser.getSound(in);
      AudioInputStream audio = AudioSystem.getAudioInputStream( in );
      this.format.apply( audio.getFormat() );
      return audio;
    }catch(Exception e){
      e.printStackTrace();
    }
*/
    Sound sound = mediaParser.getSound(in, this.defaultFormat);
    if ( sound == null ) {
      in.close();
      throw new IOException("Unsupported format");
    }
    this.format = sound.getFormat();
    return sound.getInputStream();
  }
  /**
   * <producer>
   * to make copy of VoxSource
   * */
  public VoxSource copy()
  {
    VoxSource copy = new VoxSource();
    copy.setType( this.getType() );
    copy.setContent( this.getContent().copy() );
    return copy;
  }
}
