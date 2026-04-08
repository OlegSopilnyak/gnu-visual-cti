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

import java.io.*;

import java.awt.*;
import javax.swing.*;

import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.server.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.provider.*;
import org.visualcti.server.hardware.proxy.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound's device the emulator of telephony device</p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class soundDevice extends stubDevice
{
/**
 * <attribute>
 * The UI for device
 */
private final soundDeviceUI ui;
/**
 * <accessor>
 * To get access to device's UI
 * @return the User Interface of the device
 */
  public final JPanel getUI(){return this.ui;}
/**
 * <flag>
 * The flag of termination
 */
private transient boolean cancel = false;
/**
 * <constructor>
 * Constructor of the device
 * @param factory the owner of device
 */
  public soundDevice(soundDeviceFactory factory)
  {
    super("SoundCard",factory);
    this.ui = new soundDeviceUI(this);
    this.ui.online();
  }
  /**
   * <debug>
   * To show the ui of device in the Frame
   */
  final void show(){
    JFrame frame = new JFrame("emulator");
    frame.getContentPane().add(this.ui,BorderLayout.CENTER);
    frame.pack(); frame.setVisible(true);
  }
  /**
   * <notify>
   * Reaction to device's state event
   * @param action the state's event
   */
  public final void dispatch(unitAction action) {}
  /**
   * <notify>
   * To notify, about state changed
   * @param state new state's value
   */
  protected final void stateChanged(String state){this.ui.setState(state);}
  /**
   * <translator>
   * To restore the device configuration from XML
   * @param xml the configuration
   */
  protected final void processConfiguration(Element xml){}
  /**
   * <action>
   * To open the device
   * @throws java.io.IOException if already opened
   */
  public final void open() throws java.io.IOException{
    super.open();this.dropCall();
  }
  /**
   * <action>
   * To close the device
   * @throws java.io.IOException if not opened
   */
  public final void close() throws java.io.IOException{
    this.dropCall(); super.close();
  }
  /**
   * <action>
   *  The unconditional termination anyone current CTI of operation:
   *   	1. operations with calls (waiting, make call, connect)
   *    2. exchanges of the data (voice or fax)
   *    3. device get an user's input
   * @throws IOException if the device can't terminate current operation
   * @see deviceProxy
   */
  public final void terminate() throws java.io.IOException
  {
    super.terminate();
    this.cancel = true;
    String state = super.getUnitState();
    if ( deviceProxy.DS_WAIT.equals(state) )
      this.waitTerminate(); else
    if ( deviceProxy.DS_DIAL.equals(state) )
      this.makeTerminate(); else
    if ( deviceProxy.DS_GTDIG.equals(state) )
      this.getDigitsTerminate(); else
    if ( deviceProxy.DS_PLAY.equals(state) )
      this.playTerminate(); else
    if ( deviceProxy.DS_RECD.equals(state) )
      this.recordTerminate();
  }
  /**
   * <call.param>
   * The parameter of the telephone call
   * @return called number
   */
  public final String getCalledNumber(){return this.ui.getCalling();}
  /**
   * <call.param>
   * The parameter of the telephone call
   * @return calling's number (CallerID)
   */
  public final String getCallingNumber(){return this.ui.getCaller();}
  /**
   * <action>
   * To drop telephone call
   */
  public final void dropCall(){
    this.call.disconnected=true; this.ui.offline();
    try{this.terminate();}catch(java.io.IOException e){}
    this.digitsBuffer = "";
  }
  /**
   * <accessor>
   * To get the state of the call
   * @return
   */
  public final boolean isDisconnected(){return this.call.disconnected;}
/**
 * <wrapper>
 * The class-wraper of telephone call
 */
private final class Call
{
private transient boolean disconnected = false;
private transient boolean alerted = false;
private transient String answer = null;
}
/**
 * <attribute>
 * The call's instance of device
 */
private final Call call = new Call();
/**
 * <attribute>
 * The count of rings before stop wait call
 */
  private int ringsCount = -2;
  /**
   * <notify>
   * The notify from UI "Ring" button
   */
  final void callAlerted(){
    if ( this.ringsCount <= 0 || !this.call.disconnected) return;
    this.ringsCount--; this.call.alerted = true;
    if ( this.ringsCount == 0 && this.call.disconnected ){
      synchronized(this.call){this.call.notify();}
    }
  }
  /**
   * <reason>
   * To solve the reason of termination
   * @param reason reason by default
   * @return right reason
   */
  private final String reason(String reason){
    return
        this.call.disconnected ? Reason.CALL.DISCONNECT:
        this.cancel ? Reason.TERMINATED:
        reason;
  }
  /**
   * <action>
   * To wait the incoming call or timeout
   * @param rings number rings to wait
   * @param timeout maxtime to wait
   * @param answer flag is answer the incoming call
   * @return reason of terminetion
   */
  public final String waitForCall(int rings , int timeout, boolean answer)
  {
    super.newState(DS_WAIT);
    this.ringsCount = rings;
    synchronized( this.call )
    {
      if (answer && this.call.disconnected) this.ui.waitCall();
      this.call.alerted = this.cancel = false;
      timeout = timeout < 0 ? Short.MAX_VALUE:timeout;
      // to wait a timeout
      try{this.call.wait(timeout*1000);}catch(Exception e){}
    }
    this.ringsCount = -2; super.newState(DS_IDLE);
    if ( this.call.alerted && this.call.disconnected && answer ) {
      this.call.disconnected = false; this.ui.online();
    }
    String reason = this.call.alerted ? Reason.CALL.RINGS:Reason.IO.TIMEOUT;
    return rings > 0 ? reason:this.reason(reason);
  }
  /**
   * To notify "call" semaphore
   */
  private final void notifyCall(){synchronized( this.call ){this.call.notify();}}
  /**
   * To terminate wait call method
   */
  private final void waitTerminate() {
    if ( DS_WAIT.equals(super.getUnitState()) ) notifyCall();
  }
  /**
   * <notify>
   * To notify from UI "Answer" button
   * @param answer CallAnalize result
   */
  final void answerCall(String answer){
    if ( this.call.answer == null ) return;
    this.call.answer = answer;
    synchronized(this.call){this.call.notify();}
  }
  /**
   * <action>
   * To make outgoing call
   * @param number the target number
   * @param timeout timeout to make call
   * @return the reason of termination
   */
  public final String makeCall(String number, int timeout)
  {
    this.newState(DS_DIAL);
    synchronized( this.call ){
      this.call.answer = Reason.IO.TIMEOUT;
      if ( this.isDisconnected() ) this.ui.online();
      this.ui.makeCall(number);
      this.cancel = false;
      // to wait a timeout if no autoanswer
      if ( Reason.IO.TIMEOUT.equals(this.call.answer) )
        try{this.call.wait(timeout*1000);}catch(Exception e){}
    }
    this.newState(DS_IDLE);
    String answer = this.call.answer; this.call.answer=null;
    if ( this.isDisconnected() ) {
      if ( this.isPositive(answer) ) this.call.disconnected = false;
      else this.ui.offline();
    }
    return this.reason( answer );
  }
  /**
   * Check is positive answer to make call
   * @param answer the answer to check
   * @return true if Voice or Fax answer
   */
  private final boolean isPositive(String answer){
    return Reason.CA.VOICE.equals(answer) || Reason.CA.FAX.equals(answer);
  }
  /**
   * To terminate make call method
   */
  private final void makeTerminate() {
    if ( DS_DIAL.equals(super.getUnitState()) ) notifyCall();
  }
/**
 * <attribute>
 * The value of user's input
 */
private String digitsBuffer="";
  /**
   * <notify>
   * From UI notification when pressed button in dialpad
   * @param symbol pressed symbol
   */
  final void dtmfPressed(String symbol)
  {
    // to check the termination's mask
    if ( this.termMask.indexOf(symbol) == -1 ) return;
    String state = super.getUnitState();
    // to terminate the exchange with DTMF reason
    if ( deviceProxy.DS_PLAY.equals(state) )
    {this.player().terminate(Reason.IO.DTMF);} else
    if ( deviceProxy.DS_RECD.equals(state) )
    {this.recorder().terminate(Reason.IO.DTMF);}
  }
  /**
   * <action>
   * To get an user's input
   * @param digitsCount count of symbols to get
   * @param timeout maxtime to wait one symbol
   * @param termMask set of symbol to terminate the input
   * @return reason of termination
   */
  public final String getDigits(int digitsCount, int timeout, String termMask)
  {
    if ( this.isDisconnected() ) return Reason.CALL.DISCONNECT;
    String symbol=""; boolean DTMF=false; int i=0;
    super.newState(deviceProxy.DS_GTDIG);
    for(;i < digitsCount;i++)
    {
      if ( this.cancel || this.call.disconnected ) break;
      symbol = this.ui.getSymbol(timeout);
      if ( "".equals(symbol) ) break;
      if ( termMask.indexOf(symbol) != -1) {DTMF=true;break;}
      this.digitsBuffer += symbol;
    }
    DTMF = DTMF || (digitsCount == i);
    super.newState(deviceProxy.DS_IDLE);
    return this.reason(DTMF ? Reason.IO.DTMF:Reason.IO.TIMEOUT);
  }
  /**
   * To terminate get digits
   */
  private final void getDigitsTerminate(){
    if ( DS_GTDIG.equals(super.getUnitState()) ) ui.terminateGetSymbol();
    this.digitsBuffer = "";
  }
  /**
   * <action>
   * To get access to user's input value
   * @return the value
   */
  public final String getDigitsBuffer()
  {
    String buffer = this.digitsBuffer; this.digitsBuffer="";
    return buffer;
  }
  /**
   * <action>
   *   To dial DTMF to line
   * @param toDial sequence of symbols to dial, like "555#1234*"
   * @see ToneGenerator
   */
  public final void dial(String toDial)
  {
    super.newState(DS_DIAL);
    super.newState(DS_IDLE);
  }
  /**
   * <action>
   * To play the tone
   * @param toneID identifier of a signal
   * @see Tone
   * @see ToneGenerator
   */
  public final void playTone(String toneID)
  {
    super.newState(DS_TONE);
    super.newState(DS_IDLE);
  }
  /**
   * <action>
   * To play the tone
   * @param toneID identifier of a signal
   * @param time duration in seconds
   * @see Tone
   * @see ToneGenerator
   */
  public final void playTone(String toneID, float time)
  {
    super.newState(DS_TONE);
    super.newState(DS_IDLE);
  }
private final static Audio[] validFormat =
      new Audio[]{Audio.ALAW_8,Audio.ULAW_8,Audio.LINEAR};
  /**
   * <accessor>
   * To get access to valid formats' set
   * @return formats set
   */
  public final Audio[] canPlay(){return validFormat;}
  public final Audio getRawFormat(){return Audio.LINEAR;}
  /**
   * <action>
   * To play the sound
   * @param toPlay source to play
   * @param termmask termination mask
   * @param timeout maxtime to play
   * @return termination's reason
   */
  public final String play
      (
      Sound toPlay,
      String termmask,
      int timeout
      )
  {
    try{
      InputStream source = toPlay.getInputStream();
      Audio format = toPlay.getFormat();
      return this.play(source, termmask, timeout, format);
    }catch(IOException e){
      return Reason.IO.FORMAT;
    }
  }
private transient String termMask = "";
  private final boolean isDTMF_terminated(String mask){
    String buf = this.ui.dtmfBuffer.getText();
    for(int i=0;i < buf.length();i++){
      String symbol = buf.substring(i,i+1);
      if ( mask.indexOf(symbol) != -1) return true;
    }
    return false;
  }
  /**
   * <action>
   * To play the sound
   * @param source the stream to sound's data
   * @param termmask termination's mask
   * @param timeout maxtime to play
   * @param format format of the stream
   * @return termination's reason
   */
  public final String play
      (
      InputStream source,
      String termmask,
      int timeout,
      Audio format
      )
  {
    if ( this.isDisconnected() ) return Reason.CALL.DISCONNECT;
    if ( isDTMF_terminated(termmask) ) return Reason.IO.DTMF;
    super.newState(DS_PLAY);
    this.termMask = termmask;
    String reason = player().play( source, format.getAudioFormat() );
    this.termMask = "";
    super.newState(DS_IDLE);
    return this.reason(reason);
  }
  private final soundPlayer player()
  {
    return ( (soundDeviceFactory)super.getFactory()).player;
  }
  private final void playTerminate(){player().terminate(Reason.TERMINATED);}
  /**
   * <access>
   * To get access to record formats
   * @return formats set
   */
  public final Audio[] canRecord(){return validFormat;}
/**
 * <attribute>
 * The record's format
 */
private Audio recordFormat = Audio.LINEAR;
  /**
   * <mutator>
   * From UI set record format
   * @param format record's format
   */
  final void setRecordFormat(Audio format){this.recordFormat=format;}
  /**
   * <accessor>
   * To get access to record's format
   * @return the record's format
   */
  public final Audio getRecordFormat(){return this.recordFormat;}
  /**
   * <action>
   * To record the sound
   * @param target stream to save the sound's data
   * @param termmask termination.s mask
   * @param silence time to terminate by silence
   * @param timeout maxtime to record
   * @return termination's reason
   */
  public final String record
      (
      OutputStream target,
      String termmask,
      int silence,
      int timeout
      )
  {
    if ( this.isDisconnected() ) return Reason.CALL.DISCONNECT;
    if ( isDTMF_terminated(termmask) ) return Reason.IO.DTMF;
    super.newState(deviceProxy.DS_RECD);
    this.termMask = termmask;
    String reason =
        recorder().record(target, this.recordFormat.getAudioFormat(),timeout);
    this.termMask = "";
    super.newState(deviceProxy.DS_IDLE);
    return this.reason(reason);
  }
  private final soundRecorder recorder()
  {
    return ( (soundDeviceFactory)super.getFactory()).recorder;
  }
  private final void recordTerminate(){recorder().terminate(Reason.TERMINATED);}
/**
 * Class-wrapper of fax-machine properties
 */
final class faxMachine
{
    int transferredPages = 0;
    String remoteID = "";
    String localID = "";
    String faxHeader = "";
}
final faxMachine fax = new faxMachine();
  /**
   * <accessor>
   * To get access to the quantity of the transferred fax-pages
   * @return the quantity
   * @see faxMachine
   */
  public final int getTransferredPages()
  {
    return this.fax.transferredPages;
  }
  /**
   * <accessor>
   * To get access to the local ID of the remote fax-machine
   * @return local ID of remote fax-machine
   * @see faxMachine
   */
  public final String getRemoteID()
  {
    return this.fax.remoteID;
  }
  /**
   * <mutator>
   * To establish heading of page of the fax-document
   * @param header new value
   * @see faxMachine
   */
  public final void setFaxHeader(String header)
  {
    this.fax.faxHeader = header;
  }
  /**
   * <mutator>
   * To establish fax local ID for fax machine
   * @param localID new value
   * @see faxMachine
   */
  public final void setFaxLocalID(String localID)
  {
    this.fax.localID = localID;
  }
  /**
   * <action>
   *   To receive a fax's document.
   * @param target the stream for a save the received fax document in a TIFF format
   * @param pollingMode flag, to initiate receive of a fax in a polling mode;
   * @param issueVoiceRequest upon termination of receive to give out a
   *                        sound signal on the remote fax-device
   * @return reason of completion
   * @see faxReceiver
   */
  public final String receive
      (
      OutputStream target,
      boolean pollingMode,
      boolean issueVoiceRequest
      )
  {
    String reason = Reason.FAX.COMPATIBILITY;
    super.newState(DS_RECVFAX);
    super.newState(DS_IDLE);
    return this.reason(reason);
  }
  /**
  * <action>
  *    To transmit the fax
   * @param source stream to fax data
   * @param format format of data in the stream(resolution is a field)
   * @param issueVoiceRequest upon termination of reception to give out a
  *                            sound signal on the remote fax-device
   * @return reason of completion<br>
   * @see Fax
   * @see faxTransmitter
   */
  public final String transmit
      (
      InputStream source,
      Fax format,
      boolean issueVoiceRequest
      )
  {
    String reason = Reason.FAX.COMPATIBILITY;
    super.newState(DS_SENDFAX);
    super.newState(DS_IDLE);

    return this.reason(reason);
  }
  /**
  * <action>
  *    To transmit the fax
   * @param doc  The pair (Stream & format)
   * @param issueVoiceRequest upon termination of reception to give out a
   *                            sound signal on the remote fax-device
   * @return reason of completion<br>
   * @see org.visualcti.media.Document
   * @see faxTransmitter
   */
  public final String transmit
      (
      org.visualcti.media.Document source,
      boolean issueVoiceRequest
      )
  {
    try{
      InputStream src = source.getInputStream();
      Fax format = source.getFormat();
      return this.transmit(src, format, issueVoiceRequest);
    }catch(IOException e){
      return Reason.FAX.COMPATIBILITY;
    }
  }
}
