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
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To wait an incoming call or timeout</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Wait extends Basis
{
/**
<value>
The system symbol
Call's target phone-number
(The telephone number, where is made a Call)
*/
public static final Symbol system_cti_CalledNumber = Symbol.newSystem("cti.CalledNumber", Symbol.STRING);
/**
<value>
The system symbol
Call's source phone-number
(The telephone number, whence is made a Call)
*/
public static final Symbol system_cti_CallingNumber = Symbol.newSystem("cti.CallingNumber", Symbol.STRING);
/**
<value>
The system symbol
The originate phone-number of telephone device
*/
public static final Symbol system_cti_OriginateNumber = Symbol.newSystem("cti.OriginateNumber", Symbol.STRING);
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
  predefined.add(system_cti_CalledNumber);
  predefined.add(system_cti_CallingNumber);
  predefined.add(system_cti_OriginateNumber);

  predefined.add(Symbol.newConst( Reason.CALL.RINGS ));
  predefined.add(Symbol.newConst( Reason.IO.TIMEOUT ));
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
    public final String get_ID_prefix(){return "Wait.";}


    /**
     * <constructor>
     * */
    public Wait(){
      super.setAbout("To wait an event");
      this.initRuntime();
    }
    private final void initRuntime()
    {
      this.rings = Symbol.newConst(new Integer(0));
      this.timeout=Symbol.newConst(new Integer(-1));
      this.answer = false;
    }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("rings",this.rings).getXML() );
      xml.addContent( new Property("timeout",this.timeout).getXML() );
      xml.addContent( new Property("answer",this.answer).getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      if (xml == null) return;
      // to clear a runtime properties
      this.initRuntime();
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
          if ( "rings".equals(name) )    {
              this.rings = property.getValue(this.rings); names.add(name);
          }else
          if ( "timeout".equals(name) )     {
              this.timeout = property.getValue(this.timeout); names.add(name);
          }else
          if ( "answer".equals(name)){
              this.answer = property.getValue(this.answer); names.add(name);
          }
      }
    }
    /**
     * <action>
     * Main method
     * */
    protected final void telephonyMethod(deviceProxy device, Subroutine caller)
    {
      caller.set(Basis.system_cti_Operation,deviceProxy.DS_WAIT);
      caller.set(Basis.system_cti_Operation_Result,"Progress...");
      // to get values from the Subroutine's Pool

      //to make the rings count
      int rings = 0;
      try{rings = ((Number)caller.get(this.rings)).intValue();
      }catch(Exception e){}

      // to make the timeout
      int timeout = -1;
      try{timeout = ((Number)caller.get(this.timeout)).intValue();
      }catch(Exception e){}

      // debug's print
      caller.info("Will wait "+rings+" rings during "+timeout+" secs, is answer = "+this.answer);

      // to execute a telephony method
      String result = device.waitForCall(rings,timeout,this.answer);

      if ( Reason.CALL.RINGS.equals(result) )
      {// to store the parametres of the call
        caller.set(system_cti_CalledNumber,device.getCalledNumber());
        caller.set(system_cti_OriginateNumber,device.getCalledNumber());
        caller.set(system_cti_CallingNumber,device.getCallingNumber());
      }
      // debug's print
      caller.info("Wait result:"+result);

      // to store result to Subroutine's pool
      caller.set(Basis.system_cti_Operation_Result,result);
    }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
 * <attribute>
 * The rings count to wait completion
 */
private Symbol rings;
  /**
   * <mutator>
   * To setup rings count's value
   * @param rings new value
   */
  public final void setRings(Symbol rings){this.rings = rings;}
  /**
   * <accessor>
   * To get access to rings count's value
   * @return the value
   */
  public final Symbol getRings(){return rings;}
/**
 * <attribute>
 * Max time to wait
 */
private Symbol timeout;
  /**
   * <mutator>
   * To setup timeout's value
   * @param timeout new value
   */
  public final void setTimeout(Symbol timeout){this.timeout = timeout;}
  /**
   * <accessor>
   * To get access to timeout's value
   * @return the value
   */
  public final Symbol getTimeout(){return timeout;}
/**
 * <attribute>
 * The flag, is need to answer to incoming call or only registration...
 */
private boolean answer;
  /**
   * <mutator>
   * To setup the flag's value
   * @param answer new value
   */
  public final void setAnswer(boolean answer){this.answer = answer;}
  /**
   * <accessor>
   * To get access to flag's value
   * @return the value
   */
  public final boolean isAnswer(){return answer;}
}
