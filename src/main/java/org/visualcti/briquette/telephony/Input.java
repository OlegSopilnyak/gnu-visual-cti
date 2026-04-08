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
package org.visualcti.briquette.telephony;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.core.termination;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.Tone;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To accept an user's input</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Input extends Basis
{
/**
<value>
The system symbol
the result of user's input
*/
public static final Symbol system_cti_Input = Symbol.newSystem("cti.Input", Symbol.STRING);
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
  predefined.add(system_cti_Input);
  // voice IO operations resluts
  predefined.add(Symbol.newConst( Reason.IO.TIMEOUT ));
  predefined.add(Symbol.newConst( Reason.IO.DTMF ));
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
     * <constructor>
     * */
    public Input(){
      super.setAbout("To accept an user's input");
      this.initRuntime();
    }
    private final void initRuntime()
    {
      this.beepBefore = true;
      this.quantity = Symbol.newConst(new Integer(1));
      this.timeout=Symbol.newConst(new Integer(5));
      this.DTMF.restore("#");
    }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("beepBefore",this.beepBefore).getXML() );
      xml.addContent( new Property("quantity",this.quantity).getXML() );
      xml.addContent( new Property("timeout",this.timeout).getXML() );
      xml.addContent( this.DTMF.getXML() );
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
          if ( "beepBefore".equals(name) )    {
              this.beepBefore = property.getValue(this.beepBefore); names.add(name);
          }else
          if ( "quantity".equals(name) )     {
              this.quantity = property.getValue(this.quantity); names.add(name);
          }else
          if ( "timeout".equals(name) )     {
              this.timeout = property.getValue(this.timeout); names.add(name);
          }
      }
      this.DTMF.setXML( xml.getChild(termination.ELEMENT) );
    }
  /**
   * <main>
   * Main method of the Operation
   * */
  protected final void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_GTDIG);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");
    // to prepare the call
    // to get values from the Subroutine's Pool

    // to make the quantity of symbol to input
    int quantity = 1;
    try{quantity = ((Number)caller.get(this.quantity)).intValue();
    }catch(Exception e){}

    // to make the one symbol input's timeout
    int timeout = 5;
    try{timeout = ((Number)caller.get(this.timeout)).intValue();
    }catch(Exception e){}

    // to make the termination's mask
    String dtmfSet = this.DTMF.toString();

    // to execuet the beep before input
    if ( this.beepBefore )  device.playTone(Tone.BEEP);

    // making the telephony call
    String reason = device.getDigits(quantity,timeout,dtmfSet);

    // to store the reason
    caller.set(Basis.system_cti_Operation_Result,reason);

    // to store the user's input
    String input = device.getDigitsBuffer();
    caller.set(Input.system_cti_Input,input);

    // the debug's print
    caller.info("Input have finished. Reason is "+reason+" data ["+input+"]");
  }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
 * <attribute>
 * Flag is needs beep before
 * */
private boolean beepBefore;
  /**
   * <mutator>
   * To setup the flag's value
   * @param beepBefore new value
   */
  public final void setBeepBefore(boolean beepBefore) {this.beepBefore = beepBefore;}
  /**
   * <accessor>
   * To get access to flag's value
   * @return the value
   */
  public final boolean isBeepBefore() {return beepBefore;}
/**
 * <attribute>
 * the quantity of symbol for input
 * */
private Symbol quantity;
  /**
   * <mutator>
   * To setup the max digits count value
   * @param quantity new value
   */
  public final void setQuantity(Symbol quantity) {this.quantity = quantity;}
  /**
   * <accessor>
   * To get access to max digits count value
   * @return the value
   */
  public final Symbol getQuantity() {return quantity;}
/**
 * <attribute>
 * The timeout for One symbol
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
/**
 * <attribute>
 * The set of DTMF symbols for finish the input
 * */
private final termination DTMF=new termination("");
  /**
   * <accessor>
   * To get access to DTMF's termination
   * @return the termination
   */
  public final termination getDTMF() {return DTMF;}
}
