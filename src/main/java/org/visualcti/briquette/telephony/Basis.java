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
package org.visualcti.briquette.telephony;

import java.util.ArrayList;
import java.util.List;
import org.visualcti.briquette.Operation;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.server.hardware.generalDeviceProxy;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The basis briquette for telephony's group</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class Basis extends Operation
{
/**
<value>
The system symbol
the class-name of telepehony device
*/
public static final Symbol system_cti_Driver = Symbol.newSystem("cti.Driver", Symbol.STRING);
/**
<value>
The system symbol
the name of telepehony device
*/
public static final Symbol system_cti_portName = Symbol.newSystem("cti.portName", Symbol.STRING);
/**
<value>
The system symbol
the name of current telepehony operation
*/
public static final Symbol system_cti_Operation = Symbol.newSystem("cti.Operation", Symbol.STRING);
/**
<value>
The system symbol
the result of last telepehony operation
*/
public static final Symbol system_cti_Operation_Result = Symbol.newSystem("cti.Operation.Result", Symbol.STRING);
/**
 * <pool>
 * The pool of predefined symbols
 */
private static final List predefined = new ArrayList();
/**
 * <init>
 * To initialize predefined Symbols
 */
static{
  Basis.predefined.add(Basis.system_cti_Driver);
  Basis.predefined.add(Basis.system_cti_portName);
  Basis.predefined.add(Basis.system_cti_Operation);
  Basis.predefined.add(Basis.system_cti_Operation_Result);

  // terminated by admin
  Basis.predefined.add(Symbol.newConst(Reason.TERMINATED));
  Basis.predefined.add(Symbol.newConst(Reason.CALL.DISCONNECT));
}
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * It may be overrided in children
   * @return predefined symbols
   */
  public List getPredefinedSymbols(){return predefined;}
/**
 * <attribute>
 * The reference to Telephony Device
 * */
private volatile deviceProxy device=null;
/**
 * <action>
 * To execute concrete telephony call
 * parameters:
 * device - the reference to telephony device
 * caller - the owner of Operation, for get/set the values
 * */
protected abstract void telephonyMethod(deviceProxy device,Subroutine caller);
    /**
     * <action>
     * To execute this operator
     * After execution, method returns the reference
     * to the next executable operator.
     * If null has returned, the Subroutine (caller) must finish
     * */
    public final Operation doIt(Subroutine caller)
    {
      // to check the telephony device
      if ( !this.deviceReady(caller) ) return null;
      // to execute concrete telephony's method
      this.telephonyMethod( this.device, caller );
      // to setup current operation name to IDLE
      caller.set(system_cti_Operation,deviceProxy.DS_IDLE);
      // clear the reference to telephony device
      this.device = null;
      // return reference to the next Operation
      return this.getLink(Operation.DEFAULT_LINK);
    }
      private final boolean deviceReady(Subroutine caller){
        // to store the telephony device
        try{
          org.visualcti.server.task.Environment env=caller.getProgramm().getEnv();
          String device_name = (String)env.getPart(deviceProxy.SELECTED_DEVICE, String.class);
          this.device = (deviceProxy)env.getPart(device_name, deviceProxy.class);
          env=null;
        }catch(Exception e){
          caller.error("Task's Environment troubles...");
          return false;
        }
        if ( this.device == null ) {
          caller.error("Telephony device is not available...");
          return false;
        }else
        if( !((generalDeviceProxy)this.device).isOpened() ){
          caller.error("Telephony device is not opened...");
          return false;
        }
        // setup the system values
        caller.set(system_cti_Driver,this.device.getClass().getName());
        caller.set(system_cti_portName,this.device.getDeviceName());
        caller.set(system_cti_Operation_Result,null);
        return true;
      }
    /**
    <action>
    To stop Operator executing
    */
    public final void stopExecute()
    {
        try{this.device.terminate();}catch(Exception e){}
    }
}
