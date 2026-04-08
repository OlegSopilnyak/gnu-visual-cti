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
package org.visualcti.server.service;

import java.util.*;
import java.io.*;

import org.visualcti.util.Queue;
import org.visualcti.server.*;
import org.visualcti.server.action.serverAction;
import org.visualcti.server.action.serverActionAdapter;
/**
class-adapter for MasterService
*/
public abstract class MasterServiceAdapter
                            extends ServiceAdapter
                            implements MasterService
{
    /**
<accessor>
Get access to Service state type
    */
    public String getType(){return "[services tree]";}
   /**
<action>
to Start master service
   */
   public void Start() throws IOException
   {
        if ( this.isStarted() ) return;// service already started
        // to start children
        for (Iterator i = this.slaveServices(); i.hasNext(); )
        {
            ((Service)i.next()).Start();
        }
        this.state = Service.State.START;// setting up new state
        // to start event and command processors
        this.startProcessors();
   }
   
   /**
<action>
to Stop master service
   */
    public void Stop() throws IOException
    {
            if ( this.isStopped() ) return;
            // to stop children
            for (Iterator i = this.slaveServices(); i.hasNext(); )
            {
                ((Service)i.next()).Stop();
            }
            this.state = Service.State.STOP;// setting up new state
            // to stop event and command processors
            this.stopProcessors();
    }
   
   /**
<mutator>
To set new owner of this unit (null for root unit)
   */
   public final void setOwner(serverUnit owner)
   {
        super.setOwner( owner );
        this.removeUnitEventListener(this);// remove itself from listeners
        for (Iterator i = this.slaveServices();i.hasNext();)
        {
            Service srv = (Service)i.next();
            srv.setServiceOwner( this );
        }
   }
    /**
<mutator>
to setting up service owner
    */
    public final void setServiceOwner(Service owner)
    {
        super.setServiceOwner( owner );
        for (Iterator i = this.slaveServices();i.hasNext();)
        {
            Service srv = (Service)i.next();
            srv.setServiceOwner( this );
        }
    }
/**
map of services
*/
protected final HashMap services = new HashMap();
   /**
<accessor>
To get access to slave service reference, by service name
   */
   public final Service getService(String name)
   {
        if (name == null) return null;
        synchronized( this.services ){// to iterate services list
            for(Iterator i=this.services.values().iterator();i.hasNext();) {
                Service service = (Service)i.next();
                if ( name.equalsIgnoreCase(service.getName()) ) return service;
            }
        }
        return null;
   }
   
   /**
<mutator>
To add the subordinated service. At start of this service, will start all subordinated
   */
   public void addSlaveService(Service service)
   {
        try {// to setup itself as owner of service (prepare to integration)
            service.setServiceOwner( this );
            // check the service state, service can throw exception :-\
            if (this.isStarted() && !service.isStarted()) service.Start();
            else
            if (this.isStopped() && !service.isStopped()) service.Stop();
            // to intergate service
            synchronized(this.services)
            {   // save the new service to services map
                this.services.put(service.getName(), service);
            }
        }catch(IOException e){// bad service, need to unintegrate
            service.setServiceOwner( null );
        }
   }
   
   /**
<mutator>
To remove the subordinated service.
   */
   public void removeSlaveService(Service service)
   {
        if (service == null) return;
        String serviceName = service.getName();
        Service old_service = this.getService(serviceName);
        if ( old_service != null && old_service.equals(service) )
        {   
            synchronized(this.services)
            {   // to remove the old service from services map
                this.services.remove(serviceName);
            }
            // Stop removed service
            try{service.Stop();}catch(IOException e){}// stop removed service
            service.setServiceOwner( null );// setup, no owner for service
        }
   }
   /**
<accessor>
get enumeration of the slave services
Listing subordinate services
   */
   public final Iterator slaveServices() {Iterator iterator;
        synchronized(this.services) {
            iterator = ((HashMap)this.services.clone()).values().iterator();
        }
        return iterator;
   }
////////////// PROCESSORS PART ///////
   /**
   to start processors threads
   */
   private void startProcessors(){
        // to start event processor
        if (this.events != null && !this.isOwnerPresent())
        {
            new eventsProcessor().start();// to start the thread
        }
   }
   /**
   to stop processors threads
   */
   private void stopProcessors() {
        // to stop events processor
        synchronized(this.EVENTS) 
        {
            if (this.events != null && this.events.isWait()) this.events.push(null);
            this.events = null;
        }
   }
////////////////// DISPATCHERS PART //////////////////////
/**
to transport event to High Level from queue (abstract)
Transport for not Service unit
*/
protected void processEvent(unitEvent event)
{
    unitEvent copy = (unitEvent)copy(event);
    System.out.println("#["+this.getName()+"]:"+copy);
}
/**
to transport error to High Level from queue (abstract)
Transport for not Service unit
*/
protected void processError(unitError error)
{
    unitError copy = (unitError)this.copy(error);
    if (copy.nested() != null) {
        copy.nested().fillInStackTrace();
        copy.nested().printStackTrace();
    }
    System.out.println("!["+this.getName()+"]:"+copy);
}
/**
to transport response to High Level from queue (abstract)
Transport for not Service unit
*/
protected void processResponse(unitCommand response)
{
    unitCommand copy = (unitCommand)copy(response);
    System.out.println("?["+this.getName()+"]:"+copy);
}
        /**
        to make copy of serverAction, using serialization/deserialization
        */
        private serverAction copy(serverAction action)
        {
            serverAction copy=null;
            try{
                long mark = System.currentTimeMillis();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(buffer);
                out.writeObject(action);out.close();
                ByteArrayInputStream bin = new ByteArrayInputStream(buffer.toByteArray());
                ObjectInputStream in = new ObjectInputStream(bin);
                copy = (serverAction)in.readObject(); in.close();
                mark = System.currentTimeMillis()-mark;
                System.out.println("Content serialize/deserialize "+mark+" miliseconds.");
            }catch(Exception e){
                e.printStackTrace();
            }
            return copy;
        }
/**
events queue
*/
private volatile Queue events = null;
/**
semaphore to new events queue creations
*/
private final Object EVENTS = new Object();
   /**
   to store event to events queue for processing
   */
   protected final void processServiceEvent(serverAction action)
   {
        synchronized(this.EVENTS)
        {
            if (this.events == null) this.events = new Queue();
        }
        this.events.push( action );
   }
        /**
        to process action by event Class
        */
        private final void processAction(serverAction action)
        {
            switch(action.actionClass())
            {
                case serverAction.EVENT:
                        this.processEvent((unitEvent)action);
                        break;
                case serverAction.ERROR:
                        this.processError((unitError)action);
                        break;
                case serverAction.RESPONSE:
                        this.processResponse((unitCommand)action);
                        break;
                default:
                    System.err.println("Unknown action! In events queue "+action);
            }
        }
   /**
   Class for process Events
   */
   final class eventsProcessor extends Thread
   {
        public eventsProcessor(){
            super( MasterServiceAdapter.this.getThreadGroup(),"Processor" );
            this.setName("Sevice events processor");
            this.setPriority(Thread.MIN_PRIORITY);
        }
        public void run()
        {
            while(MasterServiceAdapter.this.events != null){
                serverAction action = (serverAction)MasterServiceAdapter.this.events.pop();
                if ( action == null ) break;
                MasterServiceAdapter.this.processAction(action);
                if (MasterServiceAdapter.this.isOwnerPresent()) break;

            }
        }
   }
        /**
        check is present service owner for events processing
        */
        private final boolean isOwnerPresent()
        {   // get service owner, only service
            serverUnit owner = this.getOwner();
            if ( owner == null) return false;
            // to dispatch events from local queue to serviceOwner
            while ( !this.events.empty() ) {
                unitAction event = (unitAction)this.events.pop();
                owner.dispatch( event );// to owner
            }
            // clear local events queue
            synchronized(this.EVENTS) {this.events = null;}
            return true;
        }
}
