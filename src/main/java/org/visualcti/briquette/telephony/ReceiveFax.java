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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.core.FaxTarget;
import org.visualcti.briquette.core.Media;
import org.visualcti.media.Fax;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To receive a fax document</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class ReceiveFax extends Basis
{
/**
<value>
The system symbol
the result of fax receiving (the document's image)
*/
public static final Symbol system_cti_Fax = Symbol.newSystem("cti.Fax", Symbol.FAX);
/**
<value>
The system symbol
the number of pages in fax document
*/
public static final Symbol system_cti_Fax_Pages = Symbol.newSystem("cti.Fax.Pages", Symbol.NUMBER);
/**
<value>
The system symbol
the number of transferred pages
*/
public static final Symbol system_cti_Fax_TransferredPages = Symbol.newSystem("cti.Fax.TransferredPages", Symbol.NUMBER);
/**
<value>
The system symbol
the local ID of remote fax-machine
*/
public static final Symbol system_cti_Fax_RemoteID = Symbol.newSystem("cti.Fax.RemoteID", Symbol.STRING);
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
  predefined.add(system_cti_Fax);
  predefined.add(system_cti_Fax_Pages);
  predefined.add(system_cti_Fax_TransferredPages);
  predefined.add(system_cti_Fax_RemoteID);
  // fax operations resluts
  predefined.add(Symbol.newConst( Reason.FAX.COMPATIBILITY ));
  predefined.add(Symbol.newConst( Reason.FAX.COMMUNICATION_ERROR ));
  predefined.add(Symbol.newConst( Reason.FAX.USER_STOP ));
  predefined.add(Symbol.newConst( Reason.FAX.POLLING ));
  predefined.add(Symbol.newConst( Reason.FAX.NOPOLL ));
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
public final String get_ID_prefix(){return "ReceiveFax.";}
    /**
     * <constructor>
     * */
    public ReceiveFax(){
      super.setAbout("To receive the fax");
      this.initRuntime();
    }
    /** init runtime's properties */
    private final void initRuntime(){
      this.target.setContent(ReceiveFax.system_cti_Fax);
      this.target.setType(Symbol.FAX);
      this.target.setFormat( Fax.TIFF.normal() );
      this.issueVoiceRequest = false;
      this.pooling = false;
    }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( this.target.getXML() );
      xml.addContent( new Property("issueVoiceRequest",this.issueVoiceRequest).getXML() );
      xml.addContent( new Property("pooling",this.pooling).getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.initRuntime(); if ( xml == null ) return;
      // to restore the target
      this.target.setXML( xml.getChild(Media.ELEMENT) );
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
          if ( "issueVoiceRequest".equals(name) )    {
              this.issueVoiceRequest = property.getValue(this.issueVoiceRequest); names.add(name);
          }else
          if ( "pooling".equals(name) )     {
              this.pooling = property.getValue(this.pooling); names.add(name);
          }
      }
    }
  /**
   * <main>
   * Main method of Operation
   * */
  protected void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_RECVFAX);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");
    caller.set(system_cti_Fax,null);

    // to make the output stream from the target
    OutputStream out = null;
    try{out=this.target.getOutputStream(caller);
    }catch(IOException e){
      caller.set(Basis.system_cti_Operation_Result,Reason.IO.FORMAT);
      return;
    }

    // to make the flag, is call operator after
    boolean issueVoiceRequest = this.issueVoiceRequest;

    // to make the flag, is needs receive in pooling mode
    boolean pooling = this.pooling;

    // the main call (to receive the fax-document)
    String result = device.receive( out, pooling, issueVoiceRequest );

    // to store result to Subroutine's pool
    caller.set(Basis.system_cti_Operation_Result,result);
  }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
 * <attribute>
 * The container for fax-data
 * */
private FaxTarget target = new FaxTarget();
  /**
   * <accessor>
   * To get access to fax's target
   * @return the target
   */
  public final FaxTarget getTarget() {return target;}
/**
 * <flag>
 * Is needs to call operator after transmition
 * */
private boolean issueVoiceRequest;
  /**
   * <mutator>
   * To setup the flag's value
   * @param flag new value
   */
  public final void setIssueVoiceRequest(boolean flag) {this.issueVoiceRequest = flag;}
  /**
   * <accessor>
   * To get access to flag's value
   * @return the value
   */
  public final boolean isIssueVoiceRequest() {return issueVoiceRequest;}
/**
 * <flag>
 * Is use a pooling mode
 * */
private boolean pooling;
  /**
   * <mutator>
   * To setup the flag's value
   * @param pooling new value
   */
  public final void setPooling(boolean pooling) {this.pooling = pooling;}
  /**
   * <accessor>
   * To get access to flag's value
   * @return
   */
  public final boolean isPooling() {return pooling;}
}
