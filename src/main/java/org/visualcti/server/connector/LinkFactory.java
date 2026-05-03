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
package org.visualcti.server.connector;

import java.io.*;
import java.util.*;

import org.jdom.*;

import org.visualcti.server.*;
import org.visualcti.server.service.*;
import org.visualcti.server.security.*;
/**
Link's factory
*/
public abstract class LinkFactory extends ServiceAdapter
{
/**
<accessor>
to get access to factory's GateKeeper
*/
abstract protected GateKeeper getGateKeeper();//{return null;}
   /**
<accessor>
access to service name (abstract)
   */
abstract public String getName();//{return "Links factory";}
/**
To process coniguration
Will copy information from Element configuration
to HashMap properties
*/
    protected void processConfiguration(Element xml)
    {
    }
/**
manager of links factory
*/
private manager mng = null;
   /**
<action>
to Start links factory
   */
   public void Start() throws IOException
   {
        if ( this.isStarted() ) return;// service already started
        this.state = Service.State.START;// setting up new state
        (this.mng = new manager()).start();// to start factory manager
        this.checkSecurity();// to check security reference
        super.dispatch(new unitEvent(this,unitAction.START_ID));
   }

   /**
<action>
to Stop links factory
   */
    public void Stop() throws IOException
    {
        if ( this.isStopped() ) return;
        this.state = Service.State.STOP;// setting up new state
        if ( !this.isStopFromManager )
        {
          this.mng.close();try{this.mng.join();}catch(Exception e){}
        }
        this.mng = null; this.closeAll();
        super.dispatch(new unitEvent(this,unitAction.STOP_ID));
    }


/**
<attribute>
listener of links
*/
private transient volatile LinkListener listener = null;
    /**
    To add Links listeners
    for manage factory's Links
    */
    public void addLinkListener(LinkListener listener) throws TooManyListenersException
    {
        if (this.listener != null) throw new TooManyListenersException();
        if (listener == this) return;// try to add himself
        this.listener=listener;
    }
    /**
    To remove Links listeners
    */
    public void removeLinkListener(LinkListener listener)
    {
        if (this.listener != null && this.listener.equals(listener)) {
            this.listener=null;
        }
    }
/**
<attribute>
Server's security manager
*/
private transient volatile org.visualcti.server.security.Manager security = null;
    /**
    to check reference to factory's security
    */
    private synchronized void checkSecurity()
    {
        if (this.security == null)
        {
            this.security =
                (org.visualcti.server.security.Manager)UnitRegistry.lookupOld
                                                    (
                                                    "/Service/Security",
                                                    org.visualcti.server.security.Manager.class
                                                    );
        }
    }
    /**
    <accessor>
    get client's Filter, called from GateKeeper.login()
    */
    public final Filter getUserFilter(String login,String password)
    {
        if (this.security == null) return null;
        return security.getFilter(login,password);
    }
/**
<counter>
Counter of link names
*/
private int linkCount = 1;
/**
<pool>
The pool of created links
*/
private final ArrayList links = new ArrayList();
    /**
    To close all links
    */
    private final void closeAll()
    {
        Iterator i;
        synchronized(this.links){
            i = ((ArrayList)this.links.clone()).iterator();
        }
        while(i.hasNext()) ((Link)i.next()).close();
    }
    /**
    <producer>
    to create and register new Link
    will call from inner class
    */
    private final boolean createLink
                    (
                    Transport transport,
                    Filter filter
                    )
    {
        if (
            !this.isStarted() ||
            this.listener == null ||
            transport == null ||
            filter == null
            ) return false;// invalid parameters set
        // to create and adjust Link
        Link link = new Link(this,transport).setFilter(filter);
        // adjust and register new Link
        link.setName(this.getName()+"_"+this.linkCount);this.linkCount++;
        link.activate();// to start link's threads
        transport.setLink( link );
        // to dispatch factory's new state event
        dispatch(new unitEvent(this,"Created Link "+link.getName()));
        // transfer link's reference to factory listener
        if (this.listener != null) this.listener.addConnectorLink(link);
        // to store link reference to local Links pool
        synchronized(this.links){this.links.add(link);}
        return true;// link created
    }
    /**
    <cleaner>
    this method will call from Link during Link.close()
    */
    public final void linkClosed(Link link)
    {
        if (this.listener != null) this.listener.removeConnectorLink(link);
        // to remove link reference from local Links pool
        synchronized(this.links){this.links.remove( link );}
        // to dispatch factory's new state event
        dispatch(new unitEvent(this,"Closed Link "+link.getName()));
    }
/**
 * <flag>
 * Flag, is Stop() from manager
 */
private volatile boolean isStopFromManager = false;
    /**
    <processor>
    inner class - processor
    */
    private class manager extends Thread
    {
        /** constructor */
        public manager()
        {
            super(
                 LinkFactory.this.getThreadGroup(),
                 LinkFactory.this.getName()+"_LinksManager"
                 );
            this.setPriority(Thread.MIN_PRIORITY);
        }
        /**
        reference to current GateKeeper
        */
        private volatile GateKeeper keeper=null;
        /**
        <action>
        to close (stop) the manager
        */
        public final void close()
        {
            if (this.keeper != null) this.keeper.abortLogin();
            this.keeper = null;
        }
        /**
        the link factory manager, main loop
        */
        public final void run()
        {
            this.event("Started");
            LinkFactory.this.isStopFromManager = false;
            try {
                while( LinkFactory.this.isStarted() )
                {   // get factory's keeper instance
                    this.keeper = getGateKeeper();
                    try {
                        if (keeper == null)// this is not possible, but ...
                        {
                            LinkFactory.this.isStopFromManager = true;
                            this.error("in factory GateKeeper is null.");
                            LinkFactory.this.Stop(); break;
                        }
                        // blocking operation, wait external Connection
                        // and user Authentication
                        Transport transport = this.keeper.login();
                        // To check received transport
                        if (transport == null) {// user can't login
                            this.keeper = null; continue;
                        }
                        //get user's filter from gate keeper
                        Filter filter = this.keeper.getUserFilter();
                        // to create and register new Link
                        if ( !createLink(transport,filter) ) transport.close();
                        this.keeper = null; transport=null; filter = null;
                        System.gc();// to free the memory
                    }catch(IOException e){}
                }
            }finally {
                this.close(); this.event("Stopped");
            }
        }
        /**
        to dispatch the error
        */
        private void error(String message)
        {
            unitError error = new unitError(LinkFactory.this,message);
            LinkFactory.this.dispatch( error );
        }
        /**
        to dispatch the start/stop event
        */
        private void event(String prefix)
        {
            unitEvent event = new unitEvent(LinkFactory.this,prefix+" manager for "+this.getName());
            LinkFactory.this.dispatch( event );
        }
    }
}
