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
package org.visualcti.server.connector.control;

import org.visualcti.util.*;
import org.jdom.*;
import java.io.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The factory for make the Client</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public final class ClientsFactory
{
private static final String config = "./conf/VisualCTI.control.xml";
public static final String ELEMENT = "control";
  /**
   * <producer>
   * To make the control's client
   * @return the client's instance or null if can't make
   */
  public final static Client makeClient(){
    Element xml = Tools.xmlLoad(new File(config));
    if ( xml == Tools.emptyXML ) {
      xml = new Element("VisualCTI");
      Element controlXML = new Element(ELEMENT);
      controlXML.setAttribute(
          new Attribute(
              "class",
              "org.visualcti.server.connector.control.SocketClient"
                        )
                              );
      controlXML.setAttribute(new Attribute("port","1777"));
      xml.addContent(new Comment("PlainSocket client for the server"));
      xml.addContent(controlXML);
      Tools.xmlSave(xml,new File(config));
    }
    return makeClient(xml.getChild(ELEMENT));
  }
  /**
   * <producer>
   * To make the client, using XML's definition
   * @param xml definition
   * @return the client's instance or null if can't make
   */
  public final static Client makeClient(Element xml)
  {
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return null;
    try {
      String className = xml.getAttributeValue("class");
      Client client = (Client)Class.forName(className).newInstance();
      client.configure( xml );
      return client;
    }catch(Exception e){
      e.printStackTrace(Tools.err);
    }
    return null;
  }
}
