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
package org.visualcti.server.service;

import java.io.*;

import org.visualcti.server.*;
import org.visualcti.server.action.serverAction;

/**
<Singleton>
Manager of services of VisualCTI Server
*/
public final class ServiceManager 
                        extends MasterServiceAdapter
                        implements Manager
{
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return super.getIcon();}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState(){return super.getUnitState();}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return "[manager]";}
    /** process configuration (nothing) */
    protected final void processConfiguration(org.jdom.Element xml) {}
   /**
   <action>
   to Start service manager
   */
   public final void Start() throws IOException {
        if ( this.isStarted() ) return;
        this.checkUnitRegistry(); super.Start();
        this.dispatch(new unitEvent(this,serverAction.START_ID));
        //this.dispatch(new Service.Error(this,"Started sevice manager"));
   }
        /**
        to check services in registry
        */
        private final void checkUnitRegistry() throws IOException 
        {
            // get list unit, with "\Service" prefix
            String [] list = UnitRegistry.list( this.getPath() );
            if (list == null || list.length == 0)
            {
                IOException mistake = new IOException("In registry not found serices");
                dispatchEvent(new unitError(this,mistake)); throw mistake;
            }
            // to iterate entry names
            for (int i=0;i < list.length;i++)
            {
                Service service = (Service)UnitRegistry.lookup(list[i],Service.class);
                if ( !service.isDependsFrom( this ) ) {
                    // Service Manager not owner of this service
                    String mistake = "Service ["+
                                     service.getName()+
                                     "] does not depend from Service Manager, will unregistered";
                    dispatchEvent(new unitError(this,mistake));
                    UnitRegistry.unRegister( service );
                }
            }
        }
   /**
   <action>
   to Stop service manager
   */
   public final void Stop() throws IOException {
        if (this.isStopped()) return;
        super.Stop();
        this.dispatchEvent(new unitEvent(this,serverAction.STOP_ID));
   }
/**
thread group for processes
*/
private final ThreadGroup group = new ThreadGroup("Service Manager");
   /**
   <accessor>
   current service's ThreadGroup
   (Service Manager not owner of any thread)
   */
   public final ThreadGroup getThreadGroup(){return this.group;}
   
/**
refer to one copy of ServiceManager
*/
private static volatile Manager manager = null;
   /**
   <accessor>
   to get reference to Service Manager
   */
   public static Manager getManager(){
        if (manager == null) {
            synchronized (ServiceManager.class) {
                if (manager == null) manager = new ServiceManager();
            }
        }
        return manager;
   }
            /**
            Constructor for realize Singleton
            */
            private ServiceManager()
            {
                this.setServiceOwner(null);// no parent services
                UnitRegistry.unRegister( this );
                try {UnitRegistry.register( this );}catch(Exception e){}
            }
   /**
<accessor>
To get Path to object instance
   */
   public final String getPath(){return "/Service";}
   
   /**
<accessor>
name of service, anytime "Service Manager"
   */
   public final String getName() {return "Service Manager";}
//////////////////////// DISPATCHERS PART //////////////////////
    /**
    to transport event to High Level from queue (abstract)
    Transport for not Service unit
    */
    protected final void processEvent(unitEvent event)
    {
        try {this.getOwner().dispatch(event);
        }catch (NullPointerException e){
            super.processEvent(event);
        }
    }
    /**
    to transport error to High Level from queue (abstract)
    Transport for not Service unit
    */
    protected final void processError(unitError error)
    {
        try {this.getOwner().dispatch(error);
        }catch (NullPointerException e){
            super.processError(error);
        }
    }
    /**
    to transport response to High Level from queue (abstract)
    Transport for not Service unit
    */
    protected final void processResponse(unitCommand response)
    {
        try {this.getOwner().dispatch(response);
        }catch (NullPointerException e){
            super.processResponse(response);
        }
    }
}
