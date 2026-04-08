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
package org.visualcti.briquette.core;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.telephony.*;
import org.visualcti.briquette.control.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the container for receive the sound's data</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class VoxTarget extends mediaTarget
{
  /**
   * <accessor>
   * To get access to type of the Target's data
   * @return the type's ID
   */
  protected final int getRawTypeID() {return Symbol.VOICE;}
  /**
   * <accessor>
   * To get access to media gorup to the target
   * @return the group's name
   */
  protected final String getMediaGroup() {return "VOX";}
/**
 * <member>
 * The default Symbol for any sound's container
 * */
public static final Symbol defaultTarget = Record.system_cti_Voice;
  /**
   * <producer>
   * To make a default name of the target's Symbol by type
   * @param type type ID of the media
   * @return default Symbol
   */
  protected final Symbol defaultSymbol(int type) {
    switch( type )
    {
      case Media.RAW:
        return defaultTarget;
      case Media.FILE:
        return Symbol.newConst("sound.wav");
    }
    return Symbol.newConst("<none>");
  }
  /**
   * <producer>
   * To make the RAW (memory) container
   * @param caller owner of Symbols
   * @return the container
   * @throws IOException if some wrong
   */
  protected final memoryContainer getRawContainer(Subroutine caller) throws IOException
  {
    return new memorySoundContainer(caller);
  }
  /**
   * <producer>
   * To make the File container
   * @param caller owner of the Symbols
   * @return the container
   * @throws IOException
   */
  protected final fileContainer fileRawContainer(Subroutine caller) throws IOException
  {
    return new fileSoundContainer(caller);
  }
  /**
   * <mutator>
   * To setup the format of sound's stream
   * */
  public void setFormat(Audio format) {this.format = format;}
  /**
   * <accessor>
   * To get access to container's format
   * */
  public final Audio getFormat() {return format;}
/**
 * <attribute>
 * The format of the sound
 * */
private Audio format=null;
  /**
   * <producer>
   * To make a valid sound file's image
   * */
  private final byte[] validImage(byte[] body){
    return mediaProducer.makeSoundImage(this.format,body);
  }
  /**
   * <container>
   * The conatiner, to store data to the memory
   * */
  private final class memorySoundContainer extends memoryContainer {
    /** to return a valid image of the sound's file */
    protected final byte[] image(){
      byte[] rawdata = super.image();
      int seconds = (int) ( rawdata.length / format.getSampleRate() );
      super.owner.set(Record.system_cti_Voice_seconds,new Integer(seconds));
      return VoxTarget.this.validImage( rawdata );
    }
    /** constructor */
    memorySoundContainer(Subroutine caller) throws IOException
    {
      super(caller,getContent());
      if ( format == null ) throw new IOException("Not defined the Format of container");
    }
    protected final Symbol defaultTarget(){return defaultTarget;}
    /** to validate the target's Symbol */
    protected final boolean isValid(Symbol target){
      return super.isValid(target) && target.getTypeID() == Symbol.VOICE;
    }
  }
  /**
   * <container>
   * The conatiner, to store data to the file
   * */
  private class fileSoundContainer extends fileContainer {
    /** to return a valid image of the sound's file */
    protected final byte[] image(){
      byte[] rawdata = super.image();
      int seconds = (int) ( rawdata.length / format.getSampleRate() );
      super.owner.set(Record.system_cti_Voice_seconds,new Integer(seconds));
      return VoxTarget.this.validImage( rawdata );
    }
    /** constructor */
    fileSoundContainer(Subroutine caller) throws IOException
    {
      super(caller,getContent());
      if ( format == null ) throw new IOException("Not defined the Format of container");
    }
    protected final Symbol defaultTarget(){return defaultTarget;}
  }
}
