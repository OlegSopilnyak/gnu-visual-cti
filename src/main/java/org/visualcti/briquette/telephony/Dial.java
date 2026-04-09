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

import java.util.*;
import java.util.Iterator;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: Dialing a DTMF's digits or make the call</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Dial extends Basis
{
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
  // make call results
  predefined.add(Symbol.newConst( Reason.CA.VOICE ));
  predefined.add(Symbol.newConst( Reason.CA.FAX ));
  predefined.add(Symbol.newConst( Reason.CA.BUSY ));
  predefined.add(Symbol.newConst( Reason.CA.NO_ANSWER ));
  predefined.add(Symbol.newConst( Reason.CA.NO_RESPONDING ));
  predefined.add(Symbol.newConst(Reason.OK));
}
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * @return predefined symbols
   */
  public final List getPredefinedSymbols()
  {
    ArrayList list = new ArrayList(super.getPredefinedSymbols());
    list.addAll(predefined);
    return list;
  }
/**
 * <accessor>
 * To get access to new ID's prefix
 * */
public final String get_ID_prefix(){return "Dial.";}
    /**
     * <constructor>
     * */
    public Dial(){
      super.setAbout("To dial digits");
      this.initRuntime();
    }
    private final void initRuntime()
    {
      this.toDial = Symbol.newConst("");
      this.makeCall = false;
      this.timeout = Symbol.newConst(new Integer(30));
    }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("toDial",this.toDial).getXML() );
      xml.addContent( new Property("makeCall",this.makeCall).getXML() );
      xml.addContent( new Property("timeout",this.timeout).getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      // to clear a runtime properties
      this.initRuntime();
      // to check of XML's integrity
      if (xml == null) return;
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 3 );
      // to iterate the properties of operation
      while( i.hasNext() )
      {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          // check the property's name
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of runtime properties!");
          // to solve the property by name
          if ( "toDial".equals(name) )    {
              this.toDial = property.getValue(this.toDial); names.add(name);
          }else
          if ( "makeCall".equals(name) )     {
              this.makeCall = property.getValue(this.makeCall); names.add(name);
          }else
          if ( "timeout".equals(name) )     {
              this.timeout = property.getValue(this.timeout); names.add(name);
          }
      }
    }
  /**
   * <main>
   * Main method of Operation
   * */
  protected final void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_DIAL);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");
    if ( this.makeCall ) makeCall(device,caller); else dial(device,caller);
  }
  /**
   * <action>
   * To make outgoing call
   * */
  private final void makeCall(deviceProxy device, Subroutine caller) {
    // to prepare the call
    // to get values from the Subroutine's Pool

    // to make the addresse's string
    String address = "";
    try {address = caller.get(this.toDial).toString();
    }catch(Exception e){}
    // to make the timeout for make call
    int timeout = 30;
    try{timeout = ((Number)caller.get(this.timeout)).intValue();
    }catch(Exception e){}
    // to make the outgoing phone's call
    String reason = Reason.CA.NO_RESPONDING;
    if ( !"".equals(address) ) reason=device.makeCall(address,timeout);
    // to store the completion's reason
    caller.set(Basis.system_cti_Operation_Result,reason);
  }
  /**
   * <action>
   * To dial the string
   * */
  private final void dial(deviceProxy device, Subroutine caller) {
    // to prepare the call
    // to get values from the Subroutine's Pool
    String toDial="";
    // to make to dial string
    try{toDial = (String)caller.get(this.toDial);
    }catch(Exception e){}
    // making the telephony call
    device.dial(toDial);
    // to store the reason
    caller.set(Basis.system_cti_Operation_Result,Reason.OK);
  }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
  /**
   * <attribute>
   * The string to dial
   * */
  private Symbol toDial;
  /**
   * <mutator>
   * To setup phonenumber's value
   * @param toDial new phonenumber's value
   */
  public final void setToDial(Symbol toDial) {this.toDial = toDial;}
  /**
   * <accessor>
   * To get access to phonenumber's value
   * @return phonenumber's value
   */
  public final Symbol getToDial() {return toDial;}
  /**
   * <attribute>
   * Flag, is needs a make call, using toDial string
   * */
  private boolean makeCall;
  /**
   * <mutatot>
   * To get access to makeCall's flag
   * @param makeCall new flag's value
   */
  public final void setMakeCall(boolean makeCall) {this.makeCall = makeCall;}
  /**
   * <accessor>
   * To get access to makeCall's flag
   * @return flag's value
   */
  public final boolean isMakeCall() {return makeCall;}
  /**
   * <attribute>
   * The timeout for make call
   * */
  private Symbol timeout;
  /**
   * <mutator>
   * To setup the timeout's value
   * @param timeout new value
   */
  public final void setTimeout(Symbol timeout) {this.timeout = timeout;}
  /**
   * <accessor>
   * To get access to timeout's value
   * @return the value
   */
  public final Symbol getTimeout() {return timeout;}
}
