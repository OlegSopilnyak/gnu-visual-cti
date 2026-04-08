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
package org.visualcti.server.hardware.provider.soundcard;

import org.visualcti.server.hardware.*;
import org.visualcti.media.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.control.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * Class to record the sound</p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

final class soundRecorder
{
private static final File soundFile = new File("./soundcard.record.wav");
private final AudioFormat cardFormat;
private final MediaLocator cardLocator;
private Processor recorder = null;
private DataSink filewriter = null;
private transient String reason = null;
private final Object Semaphore = new Object();
  /**
   * <contructor>
   * To build the sound's recorder
   * @param format the format of capture device
   */
  public soundRecorder(Format format,MediaLocator sound)
  {
    this.cardFormat=(AudioFormat)format; this.cardLocator=sound;
  }
/**
 * To update reason's attribute
 * @param reason new reason's value
 */
  private final void reason(String reason){
    synchronized(soundRecorder.class){this.reason=reason;}
  }
  /**
   * <action>
   * To record the sound and store it to output stream
   * @param out stream for the data
   * @param format format of the data
   * @param maxtime maxtime to record
   * @return termionation's reason
   */
  public final String record(OutputStream out,AudioFormat format,int maxtime)
  {
    this.reason( Reason.IO.TIMEOUT );
    ControllerListener control = null;
    URL soundFileURL = null;
    try{
      soundFileURL = soundRecorder.soundFile.toURL();
      this.recorder = javax.media.Manager.createProcessor( this.cardLocator );
      control = new ControllerAdapter(){
        public final void configureComplete(ConfigureCompleteEvent e) {wakeup();}
        public final void controllerError(ControllerErrorEvent e){wakeup();}
      };
      this.recorder.addControllerListener(control);
    }catch (IOException e) {
      this.reason(null);return Reason.IO.FORMAT;
    }catch (NoProcessorException e) {
      this.reason(null);return Reason.IO.FORMAT;
    }
    this.recorder.configure();
    if ( this.recorder.getState() != Processor.Configured ) this.sleep(1000);
    if ( this.recorder.getState() != Processor.Configured ){
      this.terminate(null);return Reason.ERROR;
    }
    this.recorder.removeControllerListener(control);
    this.recorder.setContentDescriptor(new FileTypeDescriptor(FileTypeDescriptor.WAVE));
    TrackControl track[] = this.recorder.getTrackControls();
    boolean encodingPossible = false;
    // Go through the tracks and try to program one of them
    // to output "format" data.
    for (int i = 0;i < track.length;i++) {
      try {track[i].setFormat(format); encodingPossible = true;
      } catch (Exception e) {
        // cannot convert to "format" disable the track
        track[i].setEnabled(false);
      }
    }
    // processor can't convert to desire format
    if (!encodingPossible) {this.terminate(null);return Reason.IO.FORMAT;}
    control = new ControllerAdapter(){
      public void realizeComplete(RealizeCompleteEvent e){wakeup();}
      public final void controllerError(ControllerErrorEvent e){wakeup();}
    };
    this.recorder.addControllerListener(control);
    this.recorder.realize(); this.sleep(-1);
    if ( this.recorder.getState() != Processor.Realized ){
      this.terminate(null);return Reason.ERROR;
    }
    // processor prepared to record...
    this.recorder.removeControllerListener(control);

    // get the output of the processor
    DataSource source = this.recorder.getDataOutput();
    // create a File protocol MediaLocator with the location of the
    // file to which the data is to be written
    MediaLocator dest = new MediaLocator(soundFileURL);
    // create a datasink to do the file writing & open the sink to
    // make sure we can write to it.
    try {
        this.filewriter = javax.media.Manager.createDataSink(source, dest);
        this.filewriter.open(); this.filewriter.start();
    } catch (NoDataSinkException e) {
      this.terminate(null);return Reason.ERROR;
    } catch (IOException e) {
      this.terminate(null);return Reason.ERROR;
    }
    control = new ControllerAdapter(){
      public final void controllerError(ControllerErrorEvent e){
        reason(Reason.ERROR); wakeup();
      }
      public final void stop(StopEvent e){wakeup();}
    };
    this.recorder.addControllerListener(control);
    this.recorder.setStopTime( new Time((double)maxtime) );
    this.recorder.start(); this.sleep( -1 );
    // end of record's process
    this.closeRecorder();this.closeFileWriter();
    String reason = this.reason; this.reason(null);
    if (Reason.IO.TIMEOUT.equals(reason) || Reason.IO.DTMF.equals(reason))
    {// to store the recorded data to output stream
      try {
        FileInputStream in = new FileInputStream(soundFile);
        Sound image = mediaParser.getSound(in, Audio.LINEAR);
        in.close(); in = null;
        int counter = -1; byte[] buf = new byte[4096];
        InputStream input = image.getInputStream();
        while( (counter = input.read(buf)) > 0) out.write(buf,0,counter);
        input.close(); image = null; input = null; buf = null;
      }
      catch (IOException e) {}
    }
    System.gc();
    return reason;
  }
  private final void sleep(int duration){
    synchronized (Semaphore) {
      try {
        if (duration > 0)
          Semaphore.wait(duration); else Semaphore.wait();
      }catch (InterruptedException e) {}
    }
  }
  private final void wakeup(){synchronized(Semaphore){Semaphore.notify();}}
  /**
   * <action>
   * To terminate playing by some reason
   * @param reason termination's reason
   */
  final void terminate(String reason)
  {
    if ( this.reason == null ) return;
    this.closeRecorder();
    this.closeFileWriter();
    this.reason(reason);
    System.gc();
  }
  private final void closeRecorder(){
    try{
      this.recorder.stop();
      this.recorder.deallocate();
      this.recorder.close();
    }catch(NullPointerException e){}
    this.recorder = null;
  }
  private final void closeFileWriter(){
    try{
      try {this.filewriter.stop();}catch (IOException e) {}
      this.filewriter.close();
    }catch(NullPointerException e){}
    this.filewriter = null;
  }
}
