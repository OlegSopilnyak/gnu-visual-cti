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
package org.visualcti.server.hardware.provider;

import java.io.*;

import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.server.*;
import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,
 * The Class-stub, for realizing base features of telephony device </p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public class stubDevice extends serverUnitAdapter implements generalDeviceProxy
{
  public int getHandle(){return -1;}
/**
 * <attribute>
 * The name of device, in factory
 */
private final String name;
    /**
     * <constructor>
     * @param name name of the device
     * @param owner the factory
     */
    public stubDevice(String name, deviceFactory owner )
    {
        // to store the name
        this.name = name;
        // to store the factory as units group
        this.setOwner( owner );
    }
  /**
   * <restore>
   * To restore the device's properties from XML
   * @param xml stored device's properties
   */
  protected void processConfiguration(Element xml){}
  /**
   * <accessor>
   * To get access to Unit's name
   * @return the name
   */
  public String getName(){return this.name;}
  /**
   * <accessor>
   * To get access to unit's type
   * @return the type
   */
  public String getType(){return "CT-device";}
  /**
   * <accessor>
   * To get access to Uni's icon
   * @return the icon's body
   */
  public byte[] getIcon(){return null;}
/**
 * <attribute>
 * The state of the device
 */
protected volatile String state = DS_CLOSED;
  /**
   * <accessor>
   * To get access to Unit's state
   * @return device's state
   */
  public final String getUnitState(){return this.state;}
  /**
   * <notify>
   * To notify, about state changed
   * @param state new state's value
   */
  protected void stateChanged(String state){}
  /**
   * <mutator>
   * To update Unit's state
   * @param state new state
   */
  protected final void newState(String state)
  {
    this.dispatch(new unitEvent(this,unitEvent.STATE_ID,this.state=state));
    this.stateChanged( state );
  }
  /**
   * <mutator>
   * To change the owner of device
   * @param owner other Unit
   */
  public void setOwner(serverUnit owner)
  {
      if (owner instanceof deviceFactory) super.setOwner(owner);
  }


//####################################################
//////////////////// Methods from generalDeviceProxy
  /**
   * <accessor>
   * To get access to device's factory
   * @return the factory of this device
   * @see generalDevice
   */
  public deviceFactory getFactory()
  {
    return (deviceFactory)this.getOwner();
  }
/**
 * <flag>
 * Flag, device is opened
 */
private volatile boolean opened = false;
  /**
   * <action>
   * To open and activate the device.
   * If the device can't open, device may throw the exception
   * @throws IOException if some wrong
   * @see generalDevice
   */
  public void open() throws java.io.IOException
  {
      this.opened=true; this.newState(DS_IDLE);
  }
  /**
   * <accessor>
   * Check, is device already opened
   * @return true if device already opened
   * @see generalDevice
   */
  public final boolean isOpened(){return this.opened;}
  /**
   * <action>
   *  To close the device, if there are no active operations and
   *  the expectation of end of the current operation, if still execute
   * @throws IOException if the device can't close
   * @see generalDevice
   */
  public void close() throws java.io.IOException
  {
    this.terminate();
    this.opened=false; this.newState(DS_CLOSED);
  }
  /**
   * <action>
   * Attempt to restore serviceability of the device.
   * @return true if device is restored
   * @see generalDevice
   */
  public boolean restore(){return true;}



//####################################################
//////////////////// Methods from deviceProxy
  /**
   * <accessor>
   * To get access to current device's status
   * @return current status as state of Unit
   * @see deviceProxy
   */
  public String getStatus(){return this.state;}
  /**
   * <accessor>
   * To get access to the name of device. The name is unique and
   * is established by the driver of the equipment.
   * @return the device name as name of the Unit
   * @see deviceProxy
   */
  public String getDeviceName(){return this.name;}
  /**
   * <action>
   *  The unconditional termination anyone current CTI of operation:
   *   	1. operations with calls (waiting, make call, connect)
   *    2. exchanges of the data (voice or fax)
   *    3. device get an user's input
   * @throws IOException if the device can't terminate current operation
   * @see deviceProxy
   */
  public void terminate() throws java.io.IOException{
    synchronized( this.semaphore ) {this.semaphore.notifyAll();}
  }
/**
 * <semaphore>
 * The semaphore for stub's operations
 */
private final Object semaphore = new Object();



//####################################################
//////////////////// Methods from CallControl
  /**
   * <accessor>
   *   The additional information on a telephony call.
   * @param parameter the parameter's name
   * @return the parameter's value
   * @see CallControl
   */
  public String getCallAdditional(String parameter){return "";}
  /**
   * <accessor>
   *  The phone number of the caused subscriber (DNIS or DID). For analog
   *  channels (without DID of service) returns an own telephone number of port,
   *  established in properties of port.
   * @return the phone's number
   * @see CallControl
   */
  public String getCalledNumber(){return "";}
  /**
   * <accessor>
   *  The phone number calling (ANI) (initiator of a call).
   * @return the phone's number
   * @see CallControl
   */
  public String getCallingNumber(){return "";}
  /**
   * <accessor>
   *   To check up a condition of a telephone call
   * @return true  - "The call is not served any more", false - "The call is served"
   * @see CallControl
   */
  public boolean isDisconnected(){return true;}
  /**
   * <accessor>
   * Check, whether device can accept the incoming call
   * This flag, the factory may set in properties of the device
   * @return the flag
   * @see CallControl
   */
  public boolean canWaitForCall(){return false;}
  /**
   * <action>
   * To wait an incoming call or when timeout expired
   * @param rings quantity of the rings
   * @param timeout period to wait (in seconds)
   * @param answer flag, is answer to incoming call
   * @return the reason of completion
   * @see CallControl
   */
  public String waitForCall(int rings , int timeout, boolean answer){
    this.newState(DS_WAIT);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    this.isCall = !this.isCall;
    return this.isCall ? Reason.CALL.RINGS:Reason.IO.TIMEOUT;
  }
/**
 * <flag>
 * The flag for emulate the work ;-)
 */
private boolean isCall = true;
  /**
   * <accessor>
   * Check, whether device can make the outgoing call
   * This flag, the factory may set in properties of the device
   * @return the flag
   * @see CallControl
   */
  public boolean canMakeCall(){return false;}
  /**
   * <action>
   * To make call and get answer
   * @param number the phone number
   * @param timeout maximum waiting time of the answer (seconds), after which
                the reason Reason.CA.NO_ANSWER comes back.
   * @return reason of completion
   * @see CallControl
   */
  public String makeCall(String number, int timeout){
    this.newState(DS_DIAL);
    return Reason.CA.NO_DIAL_TONE;
  }
  /**
   * <accessor>
   * The check, whether device can be used in operations of connections
   * This flag, the factory may set in properties of the device
   * @return the flag
   * @see CallControl
   */
  public boolean canUsedInConnect(){return false;}
  /**
   * <action>
   * To connect to other trunk
   * @param number telephone number or channel's name
   * @param timeout maximal waiting time of the answer (sec), after which
                the NO_ANSWER comes back.
   * @param toPlay The sound to play during connect's operation
   * @return reason of completion
   * @see CallControl
   */
  public String connect(String number, int timeout,Sound toPlay){
    this.newState(DS_DIAL);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.CA.NO_DIAL_TONE;
  }
  /**
   * <action>
   *  To break off telephone connection.
   * @see CallControl
   */
  public void dropCall(){}



//####################################################
//////////////////// Methods from ToneDetector
  /**
   * <action>
   *  To receive the user's input from a phoneline.
   * @param digitsCount quantity of expected symbols
   * @param timeout maximum time to wait the input of next symbol
   * @param termMask set of symbols interrupting input (mask). The mask is transferred by a line
   *                     as any combination of symbols (0-9,*,#), divided by comma,
   *                     For example: " 1, 2, #, 0 ".
   *                     Symbol from termMask will not placed to detector's buffer
   * @return reason of completion
   * @see ToneDetector
   */
  public String getDigits(int digitsCount, int timeout, String termMask){
    this.newState(DS_GTDIG);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.IO.TIMEOUT;
  }
  /**
   * <accessor>
   *    To receive entered symbols.
   *    The string from symbols from buffers of detector comes back.
   * @return user's input
   * @see ToneDetector
   */
  public String getDigitsBuffer(){return "";}



//####################################################
//////////////////// Methods from ToneGenerator
  /**
   * <action>
   *   To dial DTMF to line
   * @param toDial sequence of symbols to dial, like "555#1234*"
   * @see ToneGenerator
   */
  public void dial(String toDial){this.newState(DS_DIAL);}
  /**
   * <action>
   * To play the tone
   * @param toneID identifier of a signal
   * @see Tone
   * @see ToneGenerator
   */
  public void playTone(String toneID){this.newState(DS_TONE);}
  /**
   * <action>
   * To play the tone
   * @param toneID identifier of a signal
   * @param time duration in seconds
   * @see Tone
   * @see ToneGenerator
   */
  public void playTone(String toneID, float time){this.newState(DS_TONE);}



//####################################################
//////////////////// Methods from Player
  /**
   * <accessor>
   * To get access to formats array
   * @return the array of supported audio formats for playback
   *  or null if playback is not supported
   * @see Audio
   * @see Player
   */
  public Audio[] canPlay(){return null;}
  /**
   * <accessor>
   * To get access to default format to play raw data (without header)
   * @return the format for the play raw data
   */
  public Audio getRawFormat(){return Audio.LINEAR;}
  /**
   * <action>
   * To playback the sound
   * @param toPlay the Pair (input stream & format) for playback in a telephone line;
   * @param termmask set of symbols, interrupting playback (mask). A mask
   *                 is transferred by a string as any combination
   *                 of symbols (0-9,*,#), divided by comma,
   *                 for example: " 1, 2, #, 0 ".

   * @param timeout maximal time of playback (-1 for unlimited) seconds
   * @return reason of completion
   * @see Sound
   * @see Player
   */
  public String play(Sound toPlay, String termmask, int timeout)
  {
    this.newState(DS_PLAY);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.IO.FORMAT;
  }
  /**
   * <action>
   * To playback the sound
   * @param source the input stream, from which undertake sound data for
   *                 playback in a telephone line;
   * @param termmask set of symbols, interrupting playback (mask). A mask
   *                 is transferred by a string as any combination
   *                 of symbols (0-9,*,#), divided by comma,
   *                 for example: " 1, 2, #, 0 ".

   * @param timeout maximal time of playback (-1 for unlimited) seconds
   * @param format parameter determining type of the decoder for
   *                    transformation the sound data.
   * @return reason of completion
   * @see Audio
   * @see Player
   */
  public String play(InputStream source, String termmask, int timeout, Audio format)
  {
    this.newState(DS_PLAY);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.IO.FORMAT;
  }



//####################################################
//////////////////// Methods from Recorder
  /**
   * <accessor>
   *  must return the array of supported audio formats for recording
   *  or null if recording is not supported
   * @return array of supported formats
   * @see Audio
   * @see Recorder
   */
  public Audio[] canRecord(){return null;}
  /**
   * <accessor>
   * To get access to current record's format
   * @return the format
   */
  public Audio getRecordFormat(){return Audio.LINEAR;}
  /**
   * <action>
   * To record from phone's line
   * @param target the output stream, where is made record of the sound data;
   * @param termmask set of symbols, interrupting playback (mask). A mask
   *                 is transferred by a string as any combination
   *                 of symbols (0-9,*,#), divided by comma,
   *                 for example: " 1, 2, #, 0 ".
   * @param silence time of long silence in a line, after which record
   *                 the record should be completed.
   * @param timeout maximal time of record.
   * @param format parameter determining type of the coder
   *                    for coding of a sound.
   * @return reason of completion
   * @see Audio
   * @see Recorder
   */
  public String record(OutputStream target, String termmask,int silence, int timeout)
  {
    this.newState(DS_RECD);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.IO.FORMAT;
  }



//####################################################
//////////////////// Methods from faxMachine
  /**
   * <accessor>
   * To get access to the quantity of the transferred fax-pages
   * @return the quantity
   * @see faxMachine
   */
  public int getTransferredPages(){return 0;}
  /**
   * <accessor>
   * To get access to the local ID of the remote fax-machine
   * @return local ID of remote fax-machine
   * @see faxMachine
   */
  public String getRemoteID(){return "";}
  /**
   * <mutator>
   * To establish heading of page of the fax-document
   * @param header new value
   * @see faxMachine
   */
  public void setFaxHeader(String header){}
  /**
   * <mutator>
   * To establish fax local ID for fax machine
   * @param localID new value
   * @see faxMachine
   */
  public void setFaxLocalID(String localID){}



//####################################################
//////////////////// Methods from faxReceiver
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
  public String receive(OutputStream target, boolean pollingMode, boolean issueVoiceRequest)
  {
    this.newState(DS_RECVFAX);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.FAX.COMPATIBILITY;
  }



//####################################################
//////////////////// Methods from faxTransmitter
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
  public String transmit(InputStream source, Fax format, boolean issueVoiceRequest )
  {
    this.newState(DS_SENDFAX);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.FAX.COMPATIBILITY;
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
  public String transmit(org.visualcti.media.Document source, boolean issueVoiceRequest )
  {
    this.newState(DS_SENDFAX);
    synchronized(this.semaphore){
      try{this.semaphore.wait(1000);}catch(Exception e){}
    }
    return Reason.FAX.COMPATIBILITY;
  }
}
