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

import java.util.*;

import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.server.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.provider.*;
import org.visualcti.server.hardware.proxy.*;
import org.visualcti.server.hardware.proxy.part.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Network (Call Control Resource) part of device</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

abstract class networkChannel extends stubDevice implements CallControl
{
/**
 * <atribute>
 * Last value of device's handle
 */
private volatile int lastHandle=Hardware.DX_ERROR;
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
    super.open();
    // to check the handle's change (first time call)
    if ( handle != this.lastHandle )
    {
      // to change the hook state to on
      CCM.setHookState( this.lastHandle=handle, Hardware.DX_ONHOOK );
    }
    // to finish the call
    if ( !this.isDisconnected() ) this.dropCall();
    // to disable the events
    Hardware.disableEvt( handle );
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
    // to finish the call
    if ( !this.isDisconnected() ) this.dropCall();
    // to disable the events
    Hardware.disableEvt( this.getHandle() );
    // to close the stub (clear flag)
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
      // to terminate the channel's activity
      Hardware.terminate( this.getHandle() );
      // to terminate CCM's share
      CCM.terminateConnection( this );
      // to wait a termination's completion
      while( this.isActive() ) { Thread.yield();
        try{Thread.sleep(100);}catch(Exception e){}
      }
      super.newState(deviceProxy.DS_STOPD);
    }
    else
     super.terminate();
  }
    /* to check, is device's part is active */
    private final boolean isActive() {
      if ( !super.isOpened() ) return false;
      String state = super.getStatus();
      return
        deviceProxy.DS_WAIT.equals(state) ||
        deviceProxy.DS_DIAL.equals(state);
    }
/*<flag> is device have wait call feature */
private boolean isCanWaitCall=false;
/*<flag> is device have make call feature */
private boolean isCanMakeCall=false;
/*<flag> is device may used as shared port */
private boolean isCanUsedInConnect=false;
/**
 * <const>
 * The name of XML's root for the device
 */
private final static String ELEMENT = "network";
  /**
   * <accessor>
   * To get access to device's handle
   * @return the device's handle
   */
  public abstract int getHandle();
  /**
   * <accessor>
   * To get access to device's properties
   * @return device's parameters
   */
  abstract Factory.devParams getDeviceProperties();
  /**
   * <action>
   * The request to save the configuration's changes
   */
  protected abstract void save();
  /**
   * <constructor>
   * To make the network's part of device
   * @param name device's name
   * @param owner device's factory
   */
  networkChannel(String name, Factory owner)
  {
    super(name, owner);
  }
  /**
   * <restore>
   * To restore the device's properties from XML
   * @param xml stored device's properties
   */
  protected void processConfiguration(Element xml) {
    super.processConfiguration( xml );
    // to init the parameters
    this.initParameters();
    // to check XML
    Factory.devParams params = this.getDeviceProperties();
    Map network = params.getNetwork();

    if ( xml == null || (xml=xml.getChild(ELEMENT)) == null ) return;
    for(Iterator i=xml.getChildren(Parameter.ELEMENT).iterator();i.hasNext();){
      this.restoreParameter( (Element)i.next() );
    }
    // to solve the flags
    Parameter par = (Parameter)this.parameters.get( "IN" );
    if ( par != null )
    {
      try{this.isCanWaitCall=par.getBooleanValue();}catch(Exception e){}
    }
    par = (Parameter)this.parameters.get( "OUT" );
    if ( par != null )
    {
      try{this.isCanMakeCall=par.getBooleanValue();}catch(Exception e){}
    }
    par = (Parameter)this.parameters.get( "SHARE" );
    if ( par != null )
    {
      try{this.isCanUsedInConnect=par.getBooleanValue();}catch(Exception e){}
    }
  }
    /* to restore & save the parameter */
    private final void restoreParameter(Element xml){
      try{
        Parameter par = Parameter.restore( xml );
        this.parameters.put( par.getName().toUpperCase(), par );
      }catch(Exception e){}
    }
    /* to init the network's parameters */
    private final void initParameters(){
      this.parameters.clear();
      this.isCanWaitCall = false;
      this.isCanMakeCall = false;
      this.isCanUsedInConnect = false;
    }
/**
 * <pool>
 * The pool of call additional parameters
 */
private final Map parameters = new HashMap();
  /**
   * <accessor>
   * to get access to call's parameters by name
   * @param parameter parameter's name
   * @return parameter's value
   */
  public final String getCallAdditional(String parameter)
  {
    try{
      parameter = parameter.toUpperCase();
      Parameter par = (Parameter)this.parameters.get( parameter );
      return par.getValue().toString();
    }catch(Exception e){}
    return "";
  }
  /**
   * <mutator>
   * to setup call's additional parameter
   * @param parName the name of parameter
   * @param value the value of parameter
   */
  public final void setCallAdditional(String parName,String value)
  {
    Parameter par = new Parameter(parName,value);
    this.parameters.put( par.getName().toUpperCase(), par );
  }
  /**
   * <accessor>
   * to get access to called number
   * @return the number
   */
  public final String getCalledNumber()
  {
    return this.getCallAdditional( CallControl.CALLED );
  }
  /**
   * <accessor>
   * to get access to calling number
   * @return the number
   */
  public final String getCallingNumber()
  {
    return this.getCallAdditional( CallControl.CALLING );
  }
  /**
   * <accessor>
   * to check, is not exists the active call
   * @return false if exists the active call
   */
  public final boolean isDisconnected()
  {
    return CCM.isDisconnected( this.getHandle() );
  }
  /**
   * <accssor>
   * to check is device can wait to incoming calls
   * @return true, if can
   */
  public final boolean canWaitForCall() {return this.isCanWaitCall;}
  /**
   * <action>
   * to wait call or timeout
   * @param rings rings count to detected alert
   * @param timeout maxtime to wait
   * @param answer flag is answer to call
   * @return termination's reason
   */
  public final String waitForCall(int rings, int timeout, boolean answer)
  {
    if ( !super.isOpened() ) return deviceProxy.DS_CLOSED;
    // to make the method's call environment
    boolean everyTime = timeout < 0;
    boolean waitCall = rings > 0 && this.isCanWaitCall;
    long border = System.currentTimeMillis() + timeout*1000;
    int handle = this.getHandle();
    if ( waitCall )
    { // to store origin phone as called
      String phone = this.getCallAdditional(CallControl.ORIGIN);
      this.setCallAdditional(CallControl.CALLED, phone );
      // to enable rings events
      Hardware.enableEvt( handle, Reason.CALL.RINGS );
    }
    super.newState( deviceProxy.DS_WAIT );
    // waiting's main loop
    while ( !this.terminated && this.isOpened() )
    {
      if ( waitCall && CCM.isIncomingCall(handle) )
      {
        // to store the calling number
        String callerID = Hardware.getCallerID(handle);
        this.setCallAdditional(CallControl.CALLING, callerID);
        if ( answer )
        {
          // to pass the "RINGS" event
          try{Thread.sleep(100);}catch(Exception e){}
          // to answer call
          CCM.setHookState( handle, Hardware.DX_OFFHOOK );
          // to enable disconnect detection events
          Hardware.enableEvt( handle, Reason.CALL.DISCONNECT );
        }
        super.newState(deviceProxy.DS_IDLE);
        return Reason.CALL.ALERTING;
      }
      // try to share the port
      if ( this.isCanUsedInConnect && SCBUS.isResource_SCBUS(handle) )
      { // to share the port for external use
        CCM.sharedDevice( this, 1000);
      }else {
        // simple sleep :-)
        try{Thread.sleep(1000);}catch(InterruptedException e){}
      }
      // to check, is timeout expired
      if ( !everyTime && border <= System.currentTimeMillis() ) break;
    }
    // device is Idle
    super.newState(deviceProxy.DS_IDLE);
    // to disable the events for port
    Hardware.disableEvt( handle );
    // to return the reason of command's completion
    return Hardware.reason( handle, Reason.IO.TIMEOUT );
  }
  /**
   * <accessor>
   * to check is device can make call
   * @return true, if can
   */
  public final boolean canMakeCall() {return this.isCanMakeCall;}
  /**
   * <action>
   * to make the outgoing call
   * @param number destination's address
   * @param timeout maxtime to make
   * @return termination's reason
   */
  public final String makeCall(String number, int timeout)
  {
    if ( !super.isOpened() ) return deviceProxy.DS_CLOSED;
    int handle = this.getHandle();
    // to store origin phone as calling
    String phone = this.getCallAdditional(CallControl.ORIGIN);
    this.setCallAdditional(CallControl.CALLING, phone );
    super.newState(deviceProxy.DS_DIAL);
    // to disable the events for port
    Hardware.disableEvt( handle );
    // to prepare the dial, set hook off
    CCM.setHookState( handle, Hardware.DX_OFFHOOK );
    // to make outgoing call
    String result = CCM.makeCall( handle, number, timeout);
    // to store the number as called
    this.setCallAdditional(CallControl.CALLED, number );
    // to enable disconnect detection events
    Hardware.enableEvt( handle, Reason.CALL.DISCONNECT );
    // the device is Idle
    super.newState(deviceProxy.DS_IDLE);
    // to return the reason of command's completion
    return Hardware.reason(handle,result);
  }
  /**
   * <accessor>
   * to check is device can share itself
   * @return true, if can
   */
  public final boolean canUsedInConnect() {return this.isCanUsedInConnect;}
  /**
   * <action>
   * to connect device with other party
   * @param destination destination's address
   * @param timeout maxtime to make connection
   * @param toPlay sound to play during make
   * @return termination's reason
   */
  public final String connect(String destination, int timeout, Sound toPlay)
  {
    if ( !super.isOpened() ) return deviceProxy.DS_CLOSED;
    if ( !SCBUS.isResource_SCBUS(this.getHandle()) )
    {
      return Reason.CA.NO_DIAL_TONE;
    }
    return CCM.tryConnect(this, destination, timeout, toPlay);
  }
  /**
   * <action>
   * to drop current call
   */
  public final void dropCall()
  {
    int handle = this.getHandle();
    // to change the hook state to on
    CCM.setHookState( handle, Hardware.DX_ONHOOK );
    // to disable the events
    Hardware.disableEvt( handle );
    // to make some delay for PBX's reaction
    try{Thread.sleep(1500);}catch(InterruptedException e){}
  }
}
