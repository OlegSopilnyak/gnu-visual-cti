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

import org.jdom.*;

import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;

import org.visualcti.util.*;
import org.visualcti.server.*;
import org.visualcti.server.service.*;
/**
Class for made the Services system
*/
public final class Services
{
    /**
    <maker>
    To make the system
    */
    public static void make(Element xml,Server server)
    {
         // Get the list of services
        List systems = xml.getChildren("service");
        HashMap services = new HashMap();
        Tools.out.print("Progress .");Tools.out.flush();
        // To iterate service entries
        for(Iterator i = systems.iterator();i.hasNext();) {
            Element service = Config.getPrimaryEntry( (Element)i.next() );
            String serviceName = service.getName();
            Tools.out.print(".");Tools.out.flush();
            Service srv = ServiceMaker.make(service);
            if ( srv == null) Tools.error("Can't install service "+serviceName);
            else {
                Tools.out.print(".");Tools.out.flush();
                services.put(serviceName, srv);
            }
        }
        Tools.out.println(" Done");
        org.visualcti.server.service.Manager srvs = null;
        // to make Services tree
        try {
            // get registered service.Manager from registry
            Class mngClass = org.visualcti.server.service.Manager.class;
            // get registered root Class of services system
            Service service = (Service)UnitRegistry.lookup("/Service", mngClass);
            srvs = (org.visualcti.server.service.Manager)service;
            if (srvs == null) throw new ClassCastException();
            // store services to Server
            server.setServiceManager(srvs);
            services.remove("Manager");// remote root service from Vector
        }catch(ClassCastException e){
            Tools.error("The root Services instance, Manager, is not found");
            System.exit(1);
        }
        // To subordinate all services to master service
        for(Iterator i = services.values().iterator();i.hasNext(); ) {
            srvs.addSlaveService((Service)i.next());
        }
        // to direct services events flow to server
        srvs.setOwner( server );
        // to make logging reference
        org.visualcti.server.log.Log log = null;
        try {
            // get registered log.Log from registry
            Class logClass = org.visualcti.server.log.Log.class;
            // get registered root Class of services system
            Service service = (Service)UnitRegistry.lookup("/Service/Log", logClass);
            log = (org.visualcti.server.log.Log)service;
            if (log == null) throw new ClassCastException();
            server.setLogger(log);
        }catch(ClassCastException e){
            Tools.error("The Logging system not found");
            System.exit(2);
        }
        // to start Log service
        try {log.Start();
        }catch(java.io.IOException e){
            Tools.error("Can't to start The Logging system...");
            e.printStackTrace(Tools.err);
            System.exit(2);
        }
        // to make connectors reference
        org.visualcti.server.connector.Manager connectors = null;
        try {
            // get registered connector.Manager from registry
            Class connectClass = org.visualcti.server.connector.Manager.class;
            // get registered root Class of services system
            Service service = (Service)UnitRegistry.lookup("/Service/Connector", connectClass);
            connectors = (org.visualcti.server.connector.Manager)service;
            if (connectors == null) throw new ClassCastException();
            server.setConnectorsManager(connectors);
        }catch(ClassCastException e){
            Tools.error("The Connectors system not found");
            System.exit(2);
        }
   }
    /**
    <maker>
    Try to make RMI Registry object
    */
    public static void makeRegistry(Element xml, Server server)
    {
        Tools.out.print("Start RMI, using "+xml.getText()+"? Started - ");
        Tools.out.flush();
        // resolve start attribute
        Attribute start = xml.getAttribute("start");
        boolean isStart = true, needSave=false;
        if (start == null) {
            xml.setAttribute(start = new Attribute("start","true"));
            needSave = true;
        }
        try {
            if ( !start.getBooleanValue() ) {
                Tools.print("false!");return;
            }
        }catch(DataConversionException e){
            xml.setAttribute(start = new Attribute("start","true"));
            needSave = true;
        }
        // resolve port attribute
        int rmiHandlePort = 2888;
        Attribute port = xml.getAttribute("port");
        if (port == null) {
            xml.setAttribute(port = new Attribute("port","2888"));
            needSave = true;
        }
        try {rmiHandlePort = port.getIntValue();
        }catch(DataConversionException e){
            xml.setAttribute(new Attribute("port","2888"));
            needSave = true;
        }
        if (needSave) Config.save();// to save changes
        Registry rmi = null;
        try {// try to create and return the RMI registry
                rmi = java.rmi.registry.LocateRegistry.createRegistry( rmiHandlePort );
        } catch(java.rmi.server.ExportException _ex) {
            _ex.printStackTrace();
            // detected another copy of Registry server
            System.out.println("Only one copy of application is Valid\nThis copy will stopped ;)");
            throw new InternalError("Another copy of VisualCTI server is detected");
        } catch (java.rmi.RemoteException e){ // other RemoteException, will throw InternalError
            System.out.println("Application RMI make failed "+e);
            throw new InternalError("Create registry invalid");
        }
        Tools.print((rmi != null)+"!");
        server.setLocalRegistry( rmi );
    }
}
