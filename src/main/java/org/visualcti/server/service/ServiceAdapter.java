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
import java.util.*;

import org.visualcti.server.*;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.server.action.*;
import org.visualcti.server.action.event.*;
/**
class-adapter for Service
*/
public abstract class ServiceAdapter
                extends serverUnitAdapter
                implements Service
                {
    /**
    Empty constructor
    To add itself as events listener
    */
    public ServiceAdapter(){this.addUnitEventListener(this);}

/**
<attribut>
State of service
The descendant may have access to this attribute
*/
protected volatile short state = Service.State.STOP;
   /**
<action>
to Start service (abstract)
   */
abstract public void Start() throws IOException;
   /**
   <accessor>
   is Service have START state
   */
   public final boolean isStarted(){return this.state != Service.State.STOP;}

   /**
<action>
to Stop service (abstract)
   */
abstract public void Stop() throws IOException;
   /**
   <accessor>
   is Service have STOP state
   */
   public final boolean isStopped(){return this.state == Service.State.STOP;}

   /**
<accessor>
access to service name (abstract)
   */
abstract public String getName();//{return "Adapter";}
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public byte[] getIcon(){return null;}

   /**
<accessor>
current service state
   */
   public final short getState(){return this.state;}
    /**
<accessor>
Get access to Service state String
    */
    public String getUnitState()
    {
        switch( this.state )
        {
            case Service.State.START:
                return "Started";
            case Service.State.STOP:
                return "Stopped";
            default:
                return null;
        }
    }
    /**
<accessor>
Get access to Service state type
    */
    public String getType(){return "[service]";}

   /**
<accessor>
Access to service owner
The one whom this service is subordinate
default no owner (null)
   */
   public final Service getServiceOwner()
   {
        try {// to return owner as Service
            return (Service)super.getOwner();
        }catch(ClassCastException ce){
            return null;// owner not service
        }
   }
    /**
<mutator>
to setting up service owner
    */
    public void setServiceOwner(Service owner)
    {
        super.setOwner( owner );// store and register owner
        this.removeUnitEventListener(this);// remove itself from listeners
    }
   /**
<accessor>
The check, is service depended from master
   */
   public final boolean isDependsFrom(Service master)
   {
        if ( this.equals(master) ) return true;
        for(
            Service service = this.getServiceOwner();
            service != null;
            service = service.getServiceOwner()
            )
        {
            if ( service.equals(master) ) return true;

        }
        return false;
   }
///////////// EVENTABLE PART /////////
    public final void dispatchEvent(unitAction event){this.dispatch(event);}
    /**
    <dispatcher>
    To dispatch event, error, or command response from unit
    The method will call inside of the unit.
    for Services we will use the handleEvent(...) method
    */
    public final void dispatch(unitAction event)
    {   // try to dispatch event to owner of service
        try {this.getOwner().dispatch( event );
        }catch(NullPointerException e){// owner not found
            this.handleEvent(event);// standart event processing
        }
    }
    /**
    <observer>
    to handle the server event
    unitEventListener realization
    */
    public final void handleEvent(serverAction event)
    {
        if (this.listeners.size() > 0)  this.notifyListeners(event);
        else                            this.processServiceEvent(event);
    }
    /**
    To process event, maybe redefined in extended classes
    */
    protected void processServiceEvent(serverAction event)
    {
        System.err.println("\tWarning! in Service "+this.getName());
        System.err.println("\tUnprocessed event ["+event+"]\n\tThe event will be lost...");
    }
        /**
        to notify all events(Event/Error/Response) listeners
        or to remove listener, where error occur
        */
        private final void notifyListeners(serverAction event) {
            Enumeration list;// enumeration of listeners
            synchronized (this.listeners)
            {
                list = ((Vector)this.listeners.clone()).elements();
            }
            while( list.hasMoreElements() )
            {
                unitEventListener listener = (unitEventListener)list.nextElement();
                if (listener == this) {// listener is itself
                    this.processServiceEvent(event); continue;
                }
                try {// try to notify listener
                    listener.handleEvent(event);
                }catch(Throwable t){// problems, when notify listener
                    this.removeUnitEventListener(listener);// remove bad listener
                }
            }
        }
/**
<attribut>
list of event listeners
*/
private final Vector listeners = new Vector();
   /**
<mutator>
to add unitEventListener
   */
   public void addUnitEventListener(unitEventListener listener)
   {
        synchronized (this.listeners){this.listeners.addElement(listener);}
   }
   /**
<mutator>
to remove unitEventListener
   */
   public void removeUnitEventListener(unitEventListener listener)
   {
        synchronized (this.listeners){this.listeners.removeElement(listener);}
   }
/////////// COMMANDS PART //////
    /**
    <executer>
    To execute command for this unit.
    The method will call from the outside of unit.
    If command invalid, the exception will be occurred.
    */
    public void execute(unitCommand command) throws Exception
    {
        try {super.execute(command); return;// try to parent's execute
        }catch(UnknownCommandException e){}
        switch( command.getID() )
        {
            case unitCommand.START_ID: this.Start(); return;
            case unitCommand.STOP_ID:  this.Stop(); return;
        }
        throw new UnknownCommandException();
    }
   /**
<accessor>
current service's ThreadGroup
   */
   public ThreadGroup getThreadGroup()
   {
        try {return this.getServiceOwner().getThreadGroup();
        }catch(NullPointerException e){
            return null;
        }
   }

}
