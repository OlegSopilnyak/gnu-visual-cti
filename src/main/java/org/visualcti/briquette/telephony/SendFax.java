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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.core.FaxSource;
import org.visualcti.briquette.core.Media;
import org.visualcti.media.Fax;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To transmit a fax document</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class SendFax extends Basis
{
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
public final String get_ID_prefix(){return "SendFax.";}
/**
 * <const>
 * The fine quality of transmition
 * */
public static final int FINE = 0;
/**
 * <const>
 * The standart quality of transmition
 * */
public static final int STANDART = 1;
    /**
     * <constructor>
     * */
    public SendFax(){
      super.setAbout("To transmit a fax document");
      this.initRuntime();
    }
        /**
         * <init>
         * To init run-time properties
         * */
        private final void initRuntime() {
          this.source = new FaxSource();
          this.issueVoiceRequest = false;
          this.quality = STANDART;
          this.header = Symbol.newConst("VisualCTI's fax");
          this.localID= Symbol.newConst("VisualCTI server");
          this.startPage = Symbol.newConst(new Integer(1));
        }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( this.source.getXML() );
      xml.addContent( new Property("issueVoiceRequest",this.issueVoiceRequest).getXML() );
      xml.addContent( new Property("quality",this.quality).getXML() );
      xml.addContent( new Property("header",this.header).getXML() );
      xml.addContent( new Property("localID",this.localID).getXML() );
      xml.addContent( new Property("startPage",this.startPage).getXML() );
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
      // to restore the source
      this.source.setXML( xml.getChild(Media.ELEMENT) );
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 5 );
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
          if ( "quality".equals(name) )    {
              this.quality = property.getValue(this.quality); names.add(name);
          }else
          if ( "header".equals(name) )    {
              this.header = property.getValue(this.header); names.add(name);
          }else
          if ( "localID".equals(name) )    {
              this.localID = property.getValue(this.localID); names.add(name);
          }else
          if ( "startPage".equals(name) )     {
              this.startPage = property.getValue(this.startPage); names.add(name);
          }
      }
    }
  /**
   * <main>
   * Main method of Operation
   * */
  protected void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_SENDFAX);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");

    // to make the input stream to document's source
    InputStream in = null;
    try{in=this.source.getInputStream(caller);
    }catch(IOException e){
      caller.set(Basis.system_cti_Operation_Result,Reason.IO.FORMAT);
      return;
    }

    // to make flag, is call operator after
    boolean issueVoiceRequest = this.issueVoiceRequest;

    // quality's setup
    int quality = this.quality;

    // to header's setup
    String header = "VisualCTI's fax";
    try{header = caller.get(this.header).toString();
    }catch(Exception e){}

    // local ID's setup
    String localID = "VisualCTI server";
    try{localID = caller.get(this.localID).toString();
    }catch(Exception e){}

    // start page's setup
    int startPage = 1;
    try{startPage = ((Number)caller.get(this.startPage)).intValue();
    }catch(Exception e){}

    // to init the result
    String result = Reason.IO.EOF;

    // to prepare the device
    device.setFaxHeader(header);
    device.setFaxLocalID(localID);

    // to setup a stream's format
    Fax format = this.source.getFormat();
    if (quality == STANDART) format.normal();
    if (quality == FINE) format.high();

    // the main call (to transmit the fax-document)
    result = device.transmit( in, format, issueVoiceRequest );

    // to store result to Subroutine's pool
    caller.set(Basis.system_cti_Operation_Result,result);
  }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
 * <attribute>
 * The source of document
 * */
private FaxSource source;
  /**
   * <accessor>
   * To get access to document's source
   * @return the source
   */
  public final FaxSource getSource() {return source;}
/**
 * <flag>
 * Is needs to call operator after transmition
 * */
private boolean issueVoiceRequest;
  /**
   * <mutator>
   * To setup flag's value
   * @param issueVoiceRequest new value
   */
  public final void setIssueVoiceRequest(boolean issueVoiceRequest) {this.issueVoiceRequest = issueVoiceRequest;}
  /**
   * <accessor>
   * To get access to flag's value
   * @return the value
   */
  public final boolean isIssueVoiceRequest() {return issueVoiceRequest;}
/**
 * <attributr>
 * The quality of transmition
 * */
private int quality;
  /**
   * <mutator>
   * To setup quantity's value
   * @param quality new value
   */
  public final void setQuality(int quality) {this.quality = quality;}
  /**
   * <accessor>
   * To get access to quantity's value
   * @return the value
   */
  public final int getQuality() {return quality;}
/**
 * <attribute>
 * The header of the document
 * */
private Symbol header;
  /**
   * <mutator>
   * To setup document header's value
   * @param header new value
   */
  public final void setHeader(Symbol header) {this.header = header;}
  /**
   * <accessor>
   * To get access to document header's value
   * @return the value
   */
  public final Symbol getHeader() {return header;}
/**
 * <attribute>
 * The FaxMachine's local ID
 * */
private Symbol localID;
  /**
   * <mutator>
   * To setup localID's value
   * @param localID new value
   */
  public final void setLocalID(Symbol localID) {this.localID = localID;}
  /**
   * <accessor>
   * To get access to localID's value
   * @return the value
   */
  public final Symbol getLocalID() {return localID;}
/**
 * <attribute>
 * Starting page of document to transmit
 * */
private Symbol startPage;
  /**
   * <mutator>
   * To setup start page's value
   * @param startPage new value
   */
  public final void setStartPage(Symbol startPage) {this.startPage = startPage;}
  /**
   * <accessor>
   * To get access to start page's value
   * @return the value
   */
  public final Symbol getStartPage() {return startPage;}
}
