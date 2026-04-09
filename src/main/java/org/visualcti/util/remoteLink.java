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
package org.visualcti.util;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.DataConversionException;
/**
Class Link description to other Server resource
In XML Element need to presents Attributes:
host - remote RMI registry host name
port - remote RMI registry port
name - remote RMI registry name of Remote object instance
check - is need to check object accessibility (for scan Thread)
*/
public class remoteLink  {
    /** constructor */
    public remoteLink(Element xml, String defaultName)
    {
        this.name = defaultName;
        // to solve Element's attributes
        Attribute Host = xml.getAttribute("host");
        Attribute Port = xml.getAttribute("port");
        Attribute Name = xml.getAttribute("name");
        Attribute Start= xml.getAttribute("check");
        if (Host != null) {
            this.host = Host.getValue();// assign the host name of remote RMI registry
            this.needSaveXML = Port==null || Name==null || Start==null;
            // to fix attributes mistake, if need
            if (Port == null) xml.setAttribute(Port=new Attribute("port","2888"));
            if (Name == null) xml.setAttribute(Name=new Attribute("name",defaultName));
            if (Start == null) xml.setAttribute(Start=new Attribute("check","true"));
            try {
                this.port = Port.getIntValue();// assign the port of remote RMI registry
            }catch(DataConversionException e){
                xml.setAttribute(new Attribute("port","2888")); this.needSaveXML = true;
            }
            try{
                if ( !Start.getBooleanValue() ) this.host = null;// to invalidate link
            }catch(DataConversionException e){
                xml.setAttribute(new Attribute("check","true"));this.needSaveXML = true;
            }
            this.name = Name.getValue();
        }
    }
    /** flag is need to change XML */
    public boolean needSaveXML = false;
    /** Is object contains valid link information */
    public boolean isValid(){return this.host != null;}
    /** Host name of remote registry */
    public String host = null;
    /** Remote RMI port */
    public int port = 2888;
    /** The name of MessengerHandler instance in remote RMI registry */
    public String name = "Messenger/Handle";
}
