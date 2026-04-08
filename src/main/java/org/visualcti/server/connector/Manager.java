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
package org.visualcti.server.connector;

import org.jdom.*;

import java.util.*;
import java.io.*;

import org.visualcti.util.*;
import org.visualcti.server.*;
import org.visualcti.server.service.*;
import org.visualcti.util.Queue;

/**
Manager of all Link factories
*/
public final class Manager extends MasterServiceAdapter implements LinkListener
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
   public final String getType(){return super.getType()+" connectors manager";}
    /**
    <constructor>
    To start proxy daemons
    */
    public Manager() {super();
        new deliver().start(); new commander().start();
    }
   /**
<action>
to Start connectors manager
   */
   public final void Start() throws IOException
   {
        if ( !this.isStarted() ) {super.Start();
            this.dispatch(new unitEvent(this,unitEvent.START_ID));
        }
   }

   /**
<action>
to Stop connectors manager
   */
    public void Stop() throws IOException
    {
        if ( !this.isStopped() ) {super.Stop();
            this.dispatch(new unitEvent(this,unitEvent.STOP_ID));
        }
    }
/**
To process coniguration
Will copy information from Element configuration
to HashMap properties
*/
    protected void processConfiguration(Element xml)
    {
        // to process "factory" entries
        for(Iterator i = xml.getChildren("factory").iterator();i.hasNext();)
        {
            Element e = (Element)i.next();
//Config.out.println("Install links factory: "+e.getTextNormalize()+"...");
            Service service = ServiceMaker.make( e );// to make factory, as service and configure(xml)
            if (service instanceof LinkFactory)
            {
                LinkFactory factory = (LinkFactory)service;
//Config.out.println("Installed links factory "+factory.getName()+" ("+e.getTextNormalize()+")");
                this.addSlaveService(factory);
                try {factory.addLinkListener(this);}catch(Exception ue){}
            }else {
                Tools.error("??? Can't install "+e.getTextNormalize()+"...");
            }
        }
    }
   /**
<accessor>
access to service name ("Connector")
   */
    public final String getName(){return "Connector";}
    /**
    to add Link factory
    */
    public void addLinkFactory(LinkFactory factory)
    {
        super.addSlaveService(factory);
    }
    /**
    to remove LinkFactory
    */
    public void removeLinkFactory(LinkFactory factory)
    {
        super.removeSlaveService(factory);
    }
    /**
    to iterate factories list
    */
    public Iterator factories(){return super.slaveServices();}
/**
<attribute>
pool of Links
*/
private final transient ArrayList links = new ArrayList();
    /**
    <action>
    to add new Link
    This event will translated to listener,
    if no listener, Link will be closed()
    */
    public void addConnectorLink(Link link) {
        synchronized(this.links){this.links.add(link);}
    }
    /**
    <action>
    to remove Link
    This event will translated to listener
    */
    public void removeConnectorLink(Link link){
        synchronized(this.links){this.links.remove(link);}
    }
/**
thread group for Connectors system
*/
private final ThreadGroup group = new ThreadGroup("Connector");
   /**
   <accessor>
   current service's ThreadGroup
   */
   public final ThreadGroup getThreadGroup(){return this.group;}
/**
<queue>
All events queue
*/
private final Queue commands = new Queue();
   /**
   <delivery>
   To delivery command to Server
   */
   public final unitCommand command(){return (unitCommand)this.commands.pop();}
    /**
     * <thread>
     * Class for delivery events to Links
     */
    private final class commander extends Thread
    {   /** constructor */
        public commander()
        {
            super
                (
                Manager.this.getThreadGroup(),
                "/Service/Connector (commands preparer)"
                );
            this.setDaemon(true);
            this.setPriority(Thread.MIN_PRIORITY);
        }
        /** main delivery loop */
        public final void run()
        {
            while( true )
            {
              if ( pushLinksCommands() ) continue;
              // no commands pushed to the queue of commands, will sleep :-)
              try{this.sleep(100);}catch(Exception e){}
            }
        }
    }
    /* To get the command from the link & place it to the queue of commands */
    private final boolean pushLinksCommands(){
      boolean pushed = false;
      for(Iterator i= safeLinksIterator();i.hasNext();) {
        unitCommand command = ((Link)i.next()).getCommand();
        if ( command != null ) {commands.push(command);pushed=true;}
      }
      return pushed;
    }
/**
<queue>
All events queue
*/
private final Queue events = new Queue();
    /**
    <deilvery>
    Delivery the event to owner
    */
    public final void delivery(unitAction event){events.push(event);}
    /**
     * <thread>
     * Class to deliver the events to active Links
     */
    private final class deliver extends Thread
    {   /* constructor */
        public deliver()
        {
            super
                (
                Manager.this.getThreadGroup(),
                "/Service/Connector (the events deliver)"
                );
            this.setDaemon(true);
            this.setPriority(Thread.MIN_PRIORITY);
        }
        /* main delivery loop */
        public final void run()
        { // the event to delivery
          unitAction event = null;
          while( true )
          { // to get the event from events queue (may block)
            if ( (event=(unitAction)events.pop()) == null ) continue;
            // to deliver the event by event's class
            switch( event.actionClass() )
            {
              case unitAction.EVENT:
              case unitAction.ERROR:
                  Manager.this.deliveryEvent(event);
                  break;
              case unitAction.RESPONSE:
                  Manager.this.deliveryResponse( event );
                  break;
            }
          }
        }
    }
    /*
    To deliver the event or error to All active links
    */
    private final void deliveryEvent(unitAction event) {
      // to check the event's class
      if ( !(event instanceof unitEvent || event instanceof unitError) )return;
      // to dispath the event to all connector's Links
      for(Iterator i=this.safeLinksIterator();i.hasNext();){
        // to dispatch the event to the Link
        ((Link)i.next()).dispatch(event);
      }
    }
    /*
    To deliver the response to the Link-source of related command
    */
    private final void deliveryResponse(unitAction event) {
      // to check the event's class
      if ( !(event instanceof unitResponse) ) return;
      // to get the target link's name from the response
      String linkName = ((unitResponse)event).getLink();
      // to iterate all links
      for(Iterator i=this.safeLinksIterator();i.hasNext();){
        Link link = (Link)i.next();
        if ( link.getName().equals(linkName) ) {
          // this is target link,
          // dispatch the response & finish the iterations
          link.dispatch(event); break;
        }
      }
    }
    /*
    To get the safed Iterator to links
    */
    private final Iterator safeLinksIterator(){ArrayList theCopy;
      synchronized( this.links ){theCopy = (ArrayList)this.links.clone();}
      return theCopy.iterator();
    }
}
