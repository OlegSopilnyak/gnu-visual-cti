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
import org.visualcti.briquette.core.Media;
import org.visualcti.briquette.core.VoxSource;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To make a parallel outgoing call</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Connect extends Basis
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
public final String get_ID_prefix(){return "Connect.";}
    /**
     * <constructor>
     * */
    public Connect()
    {
      super.setAbout("To make a parallel outgoing call");
      this.initRuntime();
    }
        /**
         * <init>
         * To init run-time properties
         * */
        private final void initRuntime() {
          this.target = Symbol.newConst("");
          this.timeout= Symbol.newConst(new Integer(30));
          this.toPlay.setType( Media.FILE );
          this.toPlay.setContent( Symbol.newConst("") );
        }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("target",this.target).getXML() );
      xml.addContent( new Property("timeout",this.timeout).getXML() );
      xml.addContent( this.toPlay.getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.initRuntime(); if (xml == null) return;
      // to restore the source
      this.toPlay.setXML( xml.getChild(Media.ELEMENT) );
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 2 );
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
          if ( "target".equals(name) )    {
              this.target = property.getValue(this.target); names.add(name);
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
  protected void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_DIAL);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");
    // to prepare the call
    // to get values from the Subroutine's Pool

    // to make the target's address
    String target = "";
    try {target = caller.get(this.target).toString();
    }catch(Exception e){}

    // to make the timeout for make connection
    int timeout = 30;
    try{timeout = ((Number)caller.get(this.timeout)).intValue();
    }catch(Exception e){}

    // to adjust the play's source
    this.toPlay.setOwner( caller );

    // making the telephony call
    String reason = Reason.CA.NO_RESPONDING;
    if ( !"".equals(target) ) reason=device.connect(target,timeout, this.toPlay);

    // to store the reason
    caller.set(Basis.system_cti_Operation_Result,reason);
  }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
 * <attribute>
 * The target to connect, Phone's number or channel's name
 * */
private Symbol target;
  /**
   * <mutator>
   * To setup value of the taget's address
   * @param target new target's value
   */
  public final void setTarget(Symbol target) {this.target = target;}
  /**
   * <accessor>
   * To get access to target's value
   * @return target's value
   */
  public final Symbol getTarget() {return target;}
/**
 * <attribute>
 * The telephony operation's timeout
 * */
private Symbol timeout;
  /**
   * <mutator>
   * To setup timeout's value
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
 * The source to play during the telephony operation
 * */
private VoxSource toPlay = new VoxSource();
  /**
   * <mutator>
   * To setup value of play's source
   * @param toPlay new source
   */
  public final void setToPlay(VoxSource toPlay) {this.toPlay = toPlay;}
  /**
   * <accessor>
   * To get access to play's source
   * @return the source
   */
  public final VoxSource getToPlay() {return toPlay;}
}
