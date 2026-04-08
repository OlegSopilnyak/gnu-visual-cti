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
package org.visualcti.server.hardware.provider.dialogic;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.proxy.*;
import org.visualcti.server.hardware.proxy.part.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * VOX (Voice's play/record & Tones detect/generate) part of device</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
abstract class voxChannel extends networkChannel
                          implements Player,Recorder,ToneDetector,ToneGenerator
{
/**
 * <attribute>
 * For this handle codecs is did open?
 */
private volatile int lastHandle = Hardware.DX_ERROR;
/**
 * <flag>
 * The flag, is last action terminated
 */
private volatile boolean terminated = false;
  /**
   * <action>
   * To open and activate the device.
   * If the device can't open, device may throw the exception
   * @throws IOException if some wrong
   * @see generalDevice
   */
  public void open() throws java.io.IOException
  {
    int handle = this.getHandle();
    if ( handle == Hardware.DX_ERROR ) return;
    // to setup the context's codecs set
    if (handle != this.lastHandle) VOX.openCodecs( this.lastHandle=handle );
    // to open a device's network part
    super.open();
    // to setup device's tones table
    this.setupTones();
  }
  /**
   * <action>
   *  To close the device, if there are no active operations and
   *  the expectation of end of the current operation, if still execute
   * @throws IOException if the device can't close
   * @see generalDevice
   */
  public void close() throws java.io.IOException
  {
    // to clear a tones table
    this.clearTones();
    // to clear a digits buffer
    VOX.digitsBuffer( this.getHandle() );
    // to close the device's network part
    super.close();
  }
  /**
   * <notify>
   * Notify, the device have the new state
   * @param state new state's value
   */
  protected void stateChanged(String state){super.stateChanged(state);
    Context context = Context.get( this.getHandle() );
    if ( context != null && this.isActive() ) {
      context.setTerminated(this.terminated=false);
    }
  }
  /**
  <action>
   The unconditional termination anyone current CTI of operation:
          1. operations with calls (waiting, make call, connect)
          2. exchanges of the data (voice or fax)
   * @throws IOException If the device can't terminate current operation
   */
  public void terminate() throws java.io.IOException
  {
    if ( this.isActive() )
    {
      // to setup the flag
      this.terminated = true;
      // to terminate the I/O operations
      Hardware.terminate(this.getHandle());
      // to wait a termination's completion
      while( this.isActive() ) { Thread.yield();
        try{Thread.sleep(100);}catch(Exception e){}
      }
      super.newState(deviceProxy.DS_STOPD);
    }else
      // to terminate the device's network part
      super.terminate();
  }
    /* to check, is this device's part is active */
    private final boolean isActive() {
      if ( !super.isOpened() ) return false;
      // to get current state
      String state = super.getStatus();
      return
        deviceProxy.DS_PLAY.equals(state) ||
        deviceProxy.DS_GTDIG.equals(state) ||
        deviceProxy.DS_DIAL.equals(state) ||
        deviceProxy.DS_TONE.equals(state) ||
        deviceProxy.DS_RECD.equals(state);
    }
    /* to setup tones table */
    private final void setupTones(){int handle = this.getHandle();
      synchronized( this.getFactory() ){
        // to clear old tones table
        VOX.beginMakeTones( handle );
        // to iterate the tones table
        for(Iterator i=this.Tones.entrySet().iterator();i.hasNext();){
          Map.Entry entry = (Map.Entry)i.next();
          //String name = (String)entry.getKey();
          String description = (String)entry.getValue();
          // to update the tone
          VOX.updateTone( handle, description );
        }
        // to store the updates
        VOX.endMakeTones( handle );
      }
    }
    /* to clear tones table */
    private final void clearTones(){int handle = this.getHandle();
      synchronized(this.getFactory()){
        // to clear old tones table
        VOX.beginMakeTones( handle );
        // to store the updates
        VOX.endMakeTones( handle );
      }
    }
/**
 * <pool>
 * The pool of tones
 */
private final Map Tones = new HashMap();
/**
 * <attribute>
 * The format for record's operation
 */
private Audio recFormat = Audio.ADPCM_6;
  /**
   * <accessor>
   * To get access to current record's format
   * @return the format
   */
  public final Audio getRecordFormat(){return this.recFormat;}
/**
 * <attribute>
 * The format for record's operation
 */
private Audio playFormat = Audio.ADPCM_6;
  /**
   * <accessor>
   * To get access to the format of a raw Data by play
   * @return the format
   */
  public final Audio getRawFormat(){return this.playFormat;}
  /**
   * <restore>
   * To restore the device's properties from XML
   * @param xml stored device's properties
   */
  protected void processConfiguration(Element xml) {
    // to configure the parent (network part)
    super.processConfiguration( xml );
    // to init the parameters
    this.Tones.clear();
    this.Tones.putAll(Factory.defaultTones);
    this.recFormat = Audio.ADPCM_6;
    this.playFormat = Audio.ADPCM_6;
    // to check device's parameters
    Factory.devParams pars = this.getDeviceProperties();
    this.Tones.putAll( pars.getTones() );
    Audio
    format = (Audio)pars.getFormats().get( "record" );
    if ( format != null ) this.recFormat = format;
    format = (Audio)pars.getFormats().get( "play" );
    if ( format != null ) this.playFormat = format;
  }
  /**
   * <accessor>
   * To get access to device's handle
   * @return the device's handle
   */
  public abstract int getHandle();
  /**
   * <constructor>
   * To make the network's part of device
   * @param name device's name
   * @param owner device's factory
   */
  voxChannel(String name, Factory owner){super(name, owner);}
  /**
   * <accessor>
   * to return the array of formats to play
   * @return the array
   */
  public final Audio[] canPlay()
  {
    return VOX.getAvailableCodecs( this.getHandle() );
  }
  /**
   * <action>
   *  to playback from the stream
   * @param source the InputStream to media's source
   * @param termmask the mask
   * @param timeout maxtime to play
   * @param format data's format
   * @return termination's reason
   */
  public final String play(InputStream source, String termmask, int timeout, Audio format)
  {
    if ( !super.isOpened() ) return deviceProxy.DS_CLOSED;
    int handle = this.getHandle();
    // to play the stream
    super.newState( deviceProxy.DS_PLAY );
    String reason = VOX.play
                          (
                          handle,
                          source,
                          termmask,
                          timeout,
                          format
                          );
    // now the device is Idle
    super.newState( deviceProxy.DS_IDLE );
    return Hardware.reason( handle, reason );
  }
  /*<action> to playback from the sound */
  /**
   * <action>
   *  to playback from the sound
   * @param sound the definition of the source
   * @param termmask the mask
   * @param timeout maxtime to play
   * @return termination's reason
   */
  public final String play(Sound sound, String termmask, int timeout)
  {
    try{
      return this.play
                    (
                    sound.getInputStream(),
                    termmask,
                    timeout,
                    sound.getFormat()
                    );
    }catch(IOException e){
      // some wrong with sound
      super.dispatch( new org.visualcti.server.unitError(this,e) );
      return Reason.ERROR;
    }
  }
  /**
   * <accessor>
   * to return the array of formats for record
   * @return the array
   */
  public final Audio[] canRecord()
  {
    return VOX.getAvailableCodecs( this.getHandle() );
  }
  /**
   * <action>
   *  to record the voice
   * @param target OutputStream to destination
   * @param termmask the mask
   * @param silence maxtime to silence's detect termination
   * @param timeout maxtime to record
   * @return termination's reason
   */
  public final String record(OutputStream target, String termmask,int silence, int timeout)
  {
    if ( !super.isOpened() ) return deviceProxy.DS_CLOSED;
    int handle = this.getHandle();
    // to record the voice
    super.newState( deviceProxy.DS_RECD );
    String reason = VOX.record
                          (
                          handle,
                          target,
                          termmask,
                          silence,
                          timeout,
                          this.recFormat
                          );
    // now the device is Idle
    super.newState( deviceProxy.DS_IDLE );
    return Hardware.reason( handle, reason );
  }
  /**
   * <action>
   *  to accept an user's input
   * @param digitsCount quantity of symbols
   * @param timeout maxtime to one symbol's input
   * @param termMask the mask
   * @return termination's reason
   */
  public final String getDigits(int digitsCount, int timeout, String termMask)
  {
    if ( !super.isOpened() ) return deviceProxy.DS_CLOSED;
    int handle = this.getHandle();
    // to get an user's input
    super.newState( deviceProxy.DS_GTDIG );
    String reason = this.getDigits(handle,digitsCount,timeout,termMask);
    // now the device is Idle
    super.newState( deviceProxy.DS_IDLE );
    return Hardware.reason( handle, reason );
  }
    /* to retvive the user's inputs */
    private final String getDigits
                                    (
                                    int handle,
                                    int count,
                                    int timeout,
                                    String mask
                                    ){
      Context context = Context.get( handle );// to get device's context by handle
      if (context == null) return Reason.ERROR;// invalid handle
      if ( context.isDisconnected() ) return Reason.CALL.DISCONNECT;
      context.getDigitsBuffer();// to clear a context's buffer
      // try to receive the "count" of user's inputs
      String reason=""; boolean symbolEntered = false, truncate = false;
      if ( mask == null ) mask = "";
      // try "count" times to retrive the input
      for (int i=0;i < count;i++) {
        if (
            this.terminated || context.isDisconnected() ||
            Reason.ERROR.equals(reason=VOX.retriveSignal(handle,timeout))
            ) {// not normal completion of retrive the user's input
          // no accepted user's inputs
          symbolEntered = false; break;
        } else if ( Reason.IO.TIMEOUT.equals(reason) ){// input's timeout reason
          if ( symbolEntered ) {// terminated by timeout, but symbol(s) is entered
            // to save result to internal buffer
            context.addDigits( VOX.digitsBuffer(handle) );// (native)
            return Reason.IO.DTMF;
          }
          break;
        }
        // To get entered symbol as reason's ID from the last hardware's event
        int symbol = context.getLastEvent().getReasonID();
        if ( mask.indexOf(symbol) != -1 ){//reason : detected DTMF from the mask
          truncate = true; break;
        }
        // accepted input in the Dialogic's native buffer
        symbolEntered = true;
      }
      // to solve the Dialogic's native buffer
      String userInputs = VOX.digitsBuffer(handle);// (native)
      if ( symbolEntered ){// To copy the inputs to context's buffer
        // is input contains termintaion symbol
        if (truncate) userInputs = userInputs.substring(0, userInputs.length()-1);
        // to store the inputs
        context.addDigits ( userInputs );
      }
      // the reason of commnad's completion
      return reason;
    }
  /**
   * <action>
   *  to get value of user's input
   * @return user's input
   */
  public final String getDigitsBuffer()
  {
    return super.isOpened() ? VOX.getDigitsBuffer(this.getHandle()):"";
  }
  /**
   * <action>
   *  to dial the DTMF
   * @param toDial string to dial
   */
  public final void dial(String toDial)
  {
    if ( !super.isOpened() ) return;
    super.newState( deviceProxy.DS_DIAL );
    VOX.sendDigits(this.getHandle(),toDial);
    super.newState( deviceProxy.DS_IDLE );
  }
  /**
   * <action>
   *  to play tone by toneID
   * @param toneID the ID
   */
  public final void playTone(String toneID){this.playTone(toneID,(float).5);}
  /**
   * <action> to play tone by toneID
   * @param toneID the ID
   * @param time maxtime to play
   */
  public final void playTone(String toneID, float time)
  {
    if ( !super.isOpened() ) return;
    int duration = (int)(time * 1000);
    super.newState( deviceProxy.DS_TONE );
    VOX.playTone(this.getHandle(),1000,0,duration);
    super.newState( deviceProxy.DS_IDLE );
  }
}
