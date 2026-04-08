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
import org.visualcti.server.*;
import org.visualcti.server.task.TaskPool;
import org.visualcti.server.task.Manager;
/**
Class for made the Tasks system
*/
public final class Tasks
{
    public static final void adjustPools(List validNames)
    {
      Manager manager=(Manager)UnitRegistry.lookup("/Tasks",Manager.class);
      if ( manager != null) System.out.println("manager "+manager);
    }
    /**
    <maker>
    To make the system
    */
    public final static void make(Element xml,Server server)
    {
        // Get manager's Element
        Element managerXML = xml.getChild("Manager");
        if (managerXML == null)
        {
            Tools.error("The Tasks's Manager definition, is not found");
            System.exit(1);
        }
        Manager manager=
          (Manager)serverUnitMaker.make(managerXML, Manager.class );
        // Get the list of tasks pools
        Iterator list = xml.getChildren("pool").iterator();
Tools.out.print("Progress .");
Tools.out.flush();
        // to iterate the pools
        while( list.hasNext() )
        {
            Element xmlPool = (Element)list.next();
            String name = xmlPool.getAttributeValue("name");
Tools.out.print(".");
Tools.out.flush();
            TaskPool pool = new TaskPool( name );
//            pool.configure(xmlPool);
            manager.addChild(pool);
            pool.configure(xmlPool);
Tools.out.print(".");
Tools.out.flush();
        }
        // to assign the public TaskPool
        for(Iterator i=manager.children().iterator();i.hasNext();)
        {
          TaskPool pool = (TaskPool)i.next();
          if ( pool.isPublic() ) {
            manager.removeChild(pool);manager.addChild(pool);
            break;
          }
        }
Tools.out.println(" Done");
        // to connect manager to server
        manager.setOwner(server); server.setTaskManager(manager);
   }
}
