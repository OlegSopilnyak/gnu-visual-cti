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
package org.visualcti.server.hardware.provider.soundcard;

import org.visualcti.server.hardware.*;
import org.visualcti.media.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import java.io.*;
import java.util.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * The class to play the stream via JMF</p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

final class soundPlayer
{
private static final File soundFile = new File("./soundcard.play.wav");
private final AudioFormat format;
private final MediaLocator sound;
  /**
   * <constructor>
   * To make the sound's player
   * @param desireFormat desire format of the player
   * @param sound the locator to soundcard
   */
public soundPlayer(Format desireFormat, MediaLocator sound)
{
  this.format=(AudioFormat)desireFormat;
  this.sound = sound;
}
private transient Player device=null;
private transient String reason = null;
/**
 * To update reason's attribute
 * @param reason
 */
  private final void reason(String reason){
    synchronized(soundPlayer.class){this.reason=reason;}
  }
  /**
   * To store the stream's data to file
   * @param in stream to data
   * @param format format of data
   * @return true if stored
   */
  private static final boolean store(InputStream in,AudioFormat format){
    try{
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      int counter = -1; byte buffer[]=new byte[4096];
      while( (counter = in.read(buffer)) > 0 ) buf.write(buffer,0,counter);
      in.close(); buffer = null; buffer = buf.toByteArray(); buf = null;
      Audio fmt = Audio.LINEAR.copy().apply(format);
      byte[] image = mediaProducer.makeSoundImage( fmt, buffer ); buffer=null;
      FileOutputStream out = new FileOutputStream(soundFile);
      out.write(image); out.close(); out = null; System.gc();
      return true;
    }catch (Exception e){
      e.printStackTrace();
      return false;
    }
  }
  /**
   * <action>
   * To play the sound from the srteam
   * @param in sound's data stream
   * @param format format of sound's data
   * @return the reason of termintaion
   */
  final String play(InputStream in,AudioFormat format)
  {
    this.reason(Reason.IO.EOF);
    if ( !store(in,format) ){this.reason(null);return Reason.IO.FORMAT;}
    try{
      this.device=javax.media.Manager.createRealizedPlayer( soundFile.toURL() );
    }catch(CannotRealizeException e){
      this.reason(null);
      return Reason.IO.FORMAT;//*/
    }catch(NoPlayerException e){
      this.reason(null);
      return Reason.IO.FORMAT;
    }catch(IOException e){
      this.reason(null);
      return Reason.IO.FORMAT;
    }
    ControllerListener control = new ControllerAdapter(){
      /*
      public final void controllerUpdate(ControllerEvent e)
      {
        System.out.println("ControllerEvent " + e);
        super.controllerUpdate(e);
      }*/
      public final void stop(StopEvent e){this.wakeup();}
      public final void controllerError(ControllerErrorEvent e){this.wakeup();}
      private final void wakeup(){synchronized(Semaphore){Semaphore.notify();}}
    };
    this.device.addControllerListener( control );
    this.device.start();
    synchronized(Semaphore){
      try{Semaphore.wait();}catch(Exception e){}
    }
    this.device.removeControllerListener( control );
    this.device.stop(); this.device.deallocate(); this.device.close();
    String reason = this.reason; this.reason(null); this.device=null;
    return reason;
  }
/**
 * <semaphore>
 * The semaphore of device's playback
 */
private final Object Semaphore = new Object();
  /**
   * <action>
   * To terminate playing by some reason
   * @param reason termination's reason
   */
  final void terminate(String reason)
  {
    if ( this.reason == null ) return;
    this.reason(reason); this.device.stop();
  }


}
