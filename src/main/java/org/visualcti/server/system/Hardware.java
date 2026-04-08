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
package org.visualcti.server.system;

import java.util.*;
import org.jdom.*;

import org.visualcti.util.Tools;
import org.visualcti.server.Server;
import org.visualcti.server.serverUnitMaker;
import org.visualcti.server.hardware.deviceFactory;
import org.visualcti.server.hardware.Manager;
/**
Class for made the Hardware system
*/
public final class Hardware
{
    /**
    <maker>
    To make the system
    */
    public static void make(Element xml,Server server)
    {
        // Get manager's Element
        Element managerXML = xml.getChild("Manager");
        if (managerXML == null)
        {
            Tools.error("The Hardware's Manager definition, is not found");
            System.exit(1);
        }
        Manager manager=(Manager)serverUnitMaker.make( managerXML, Manager.class );
        // Get the list of device factories
        Iterator list = xml.getChildren("factory").iterator();
        // to iterate the factories
        while( list.hasNext() )
        {
            Element XML = (Element)list.next();
            deviceFactory factory=
              (deviceFactory)serverUnitMaker.make(XML, deviceFactory.class);
            if (factory != null) manager.addDeviceFactory(factory);
        }
        manager.setOwner(server); server.setHardwareManager(manager);
   }
}
