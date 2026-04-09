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
package org.visualcti.server.hardware.provider.dialogic;

import java.util.*;
import org.visualcti.media.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.proxy.part.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Call Control Manager class.</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
class CCM
{
    /**
     * <check>
     * To check hook-state ID
     * @param state hook-state ID
     * @return true if ID is valid
     */
    final static boolean isValidHookState(int state)
    {
      return state==Hardware.DX_ONHOOK || state==Hardware.DX_OFFHOOK;
    }
    /**
     * <action>
     * To change the hook's state
     * @param handle device's handle
     * @param hookState new state
     */
    final static void setHookState(int handle, int hookState)
    {
      if ( !isValidHookState(hookState) ) return;
      try {
        // try to get the device's owner
        Object owner = Context.get(handle).getOwner();
        // try start the hook state change
        synchronized(owner)
        {
          // try to start to set new hook's state (native)
          if (Hardware.setHook(handle, hookState) == Hardware.DX_ERROR )
          {
            // Error, To throws the HardwareError
            Hardware.malfunction(handle, "set hook state to "+hookState);
          }
          Hardware.debug("Hardware:start set hook to "+hookState+" for handle "+handle);
          Hardware.debug("Hardware:wait event for "+handle);
          // to wait for notify from dialogic's event
          owner.wait();
        }
        Hardware.debug("Hardware:event occur for "+handle);
      }catch(Exception e){}
    }
    /**
     * <action>
     * To check, is telephony call not served more
     * @param handle device's handle
     * @return true if no active telephony call
     */
    final static boolean isDisconnected(int handle)
    {
      try{
        return Context.get(handle).isDisconnected();
      }catch(NullPointerException e){}
      return true;
    }
    /**
     * <action>
     * To detect the incoming call alert
     * @param handle device's handle
     * @return true, if the call is alerted
     */
    final static boolean isIncomingCall(int handle)
    {
      // native
      return Context.get(handle) == null ? false:Hardware.isCallAlerted(handle);
    }
    /**
     * <action>
     * to make outgoing telephony call
     * @param handle device's handle
     * @param number desination's number
     * @param timeout maxtime to answer
     * @return termination's reason
     */
    final static String makeCall(int handle, String number, int timeout)
    {
      try{
        // to get device's owner
        Object owner = Context.get(handle).getOwner();
        // to stat the make call (native)
        if (Hardware.call(handle,number,timeout) != Hardware.DX_ERROR)
        {
          // to wait the dialogic's event
          synchronized(owner){owner.wait();}
          // to solve the termination reason
          return Hardware.termReason( handle );
        }else {
          // hardware's malfunction
          Hardware.malfunction(handle, "make call to "+number);
        }
      }catch(Exception e){
        throw new HardwareError( e.getMessage() );
      }
      return Reason.ERROR;
    }
/**
 * <pool> The container of free resources (CallControl)
 */
private static final ArrayList freeChannels = new ArrayList();
/**
 * <pool> The list of alive telephony connections
 */
private static final ArrayList aliveConnects = new ArrayList();

    /**
     * <action>
     * The port distributes itself for switching
     * this call will block...
     * @param port the device for connection
     * @param timeout maxtime for alloc
     */
    final static void sharedDevice(CallControl port, long timeout)
    {
        // to check is port valid for use
        if ( !port.canUsedInConnect() || !port.canMakeCall() ) return;
        // to make the wrapper for the port
        Wrapper resource = new Wrapper(port,timeout);
        // push new resource to container
        CCM.push(resource);
        try{
          // to wait while resource expired, or someone make outgoing call
          synchronized(resource){resource.wait();}
        }catch(InterruptedException e){}
    }
    /**
     * <action>
     * to terminate connection for distributed port
     * @param port distributed port
     */
    final static void terminateConnection(CallControl port)
    {
        Connect connect = null;// Prospective connection
        synchronized (CCM.aliveConnects)
        {   // to iterate the connections
          for(Iterator i=CCM.aliveConnects.iterator();i.hasNext();) {
            connect = (Connect)i.next();
            if( CCM.isPortConnect(port,connect) ) break;
            else connect = null;// clear reference to connection
          }
        }
        if (connect == null) return;
        connect.Stop();// say to destroy connection
        // wait, while connection will be destroyed
        try{connect.join();}catch(InterruptedException e){}
    }
      /* Is the connection use the port */
      private final static boolean isPortConnect(CallControl port, Connect connect) {
          return connect.Master == port || connect.Slave == port;
      }
    /**
     * <action>
     * Try to connect the port with destination,
     * this method will call from CallControl.connect(...)
     * @param port port to connect
     * @param target the connect's destination
     * @param timeout maxtime to make connection
     * @return the reason of termination
     */
    final static String tryConnect(CallControl port,String target,int timeout,Sound toPlay)
    {
        CCM.terminateConnection( port );// stop connection for the port
        // only destroy previous connection
        if ("".equals(target) || timeout <= 0) return Reason.CA.NO_DIAL_TONE;
        // try to make the connection
        String result = Reason.CA.NO_DIAL_TONE;
        Connect connect = null;
        // try to get the destination port from factory by name
        CallControl slave = (CallControl)port.getFactory().getDevice(target);
        if ( slave != null && !slave.isDisconnected() )
        {   // request to direct connection
            try {// try to make Connection object
                connect = new Connect(port,slave);
                result = Reason.CA.VOICE;// object maked
            }catch(ClassCastException e){
                result = Reason.CA.NO_DIAL_TONE;// can't make Connection object, invalid type
            }
        }else{
            // try to get free port's wrapper
            Wrapper resource = CCM.pop("SC Bus");
            if (resource == null) return Reason.CA.NO_DIAL_TONE;// no free port found
            // have a free port
            try {// try to make outgoing call via free port
                result = resource.getDevice().makeCall(target, timeout);
            }catch(Throwable t) {   // some mistake
                result = Reason.ERROR;
            }
            // if positive answer in slave channel
            if ( CCM.isPositiveAnswer(result) )
            {
                try {
                  connect = new Connect( port, resource );
                }catch(ClassCastException e){
                    // notify for finish Java call - CCM.freePort()
                    synchronized(resource){resource.notify();}
                    result = Reason.CA.NO_DIAL_TONE;// can't make Connection object, invalid type
                }
            }else
            {
              resource.getDevice().dropCall();
            }
        }
        // to start routed connection
        CCM.startRoutedConnection( connect );
        return result;
    }
        /* Check, is it positive answer to make outgoing call */
        private final static boolean isPositiveAnswer(String answer){
            return
                Reason.CA.VOICE.equalsIgnoreCase (answer) ||
                Reason.CA.FAX.equalsIgnoreCase   (answer);
        }
        /* to start routed connection */
        private final static void startRoutedConnection(Connect connect){
            if (connect == null) return;// invalid connect
            synchronized(CCM.aliveConnects){
              // to start connection and observe for ports disconnect
              connect.start();
              // to add started connection to connections list, for observer
              CCM.aliveConnects.add( connect );
            }
        }
    /**
     * Class to server the connection between channels
     * The thread alive, while connection between ports exists
     */
    private final static class Connect extends Thread
    {
    private final CallControl Master;// initiator of connection
    private final CallControl Slave; // other port
    private final Object resource;// the wrapper of the Slave port
        /* constructor (connect with phone) */
        Connect(CallControl master, Wrapper resource){
            this( master, resource.getDevice(), resource );
        }
        /* constructor (connect with other channel) */
        Connect (CallControl master, CallControl slave){this(master, slave, null);}
        /* constructor */
        private Connect (CallControl master, CallControl slave, Object resource){
            this.Master = master;
            /*
            if ( !SCBUS.isResource_SCBUS(Master.Handle) )// channel not suuported SC Bus
                throw new ClassCastException("SC Bus for "+Master.getPortName()+" channel not supported");
            */
            this.Slave = slave;
            /*
            if ( !SCBUS.isResource_SCBUS(Slave.Handle) ) // channel not suuported SC Bus
                throw new ClassCastException("SC Bus for "+Slave.getPortName()+" channel not supported");
            */
            // save for notify after destroy connection (for free resource)
            // if null notify not needs
            this.resource = resource;
            super.setName( "Connection between "+Master.getName()+" and "+Slave.getName() );
        }
        /* access to name of master port */
        public final String getMasterName(){return this.Master.getName();}
        /* to start the connect */
        public final void start()
        {
            // to make SC Bus routing
            int result = SCBUS.route
                                  (
                                  this.Master.getHandle(),
                                  SCBUS.SC_LSI,
                                  this.Slave.getHandle(),
                                  SCBUS.SC_LSI
                                  );
            // if mistake, throw the HardwareErro to the thread's context
            // who call CallControl.connect(String, int);
            if (result == Hardware.DX_ERROR) throw new HardwareError("invalid route result "+result);
            super.start();// to start the Java's thread
        }
        /* to stop the connect's thread */
        final void Stop(){this.continueObserve=false; super.interrupt();}
    /* flag for process the execution of main loop */
    private volatile boolean continueObserve = false;
        /* to check any port's disconnection */
        private final boolean isDisconnected() {
            return this.Master.isDisconnected() || this.Slave.isDisconnected();
        }
        /* main loop's method */
        public final void run()
        {
          this.continueObserve = true;
          try {
            // main loop of the thread
            while ( this.continueObserve )
            {   // to check the disconnect
              if ( this.isDisconnected() ) break;// to leave a main loop
              if ( this.continueObserve ) super.sleep(1000);// to sleep one second
            }
          }catch (InterruptedException e){
          }finally{
            // to unroute the SCBus's connection
            SCBUS.unroute
                      (
                      this.Master.getHandle(),
                      SCBUS.SC_LSI,
                      this.Slave.getHandle(),
                      SCBUS.SC_LSI
                      );
            if (this.resource != null)
            { // if the connection is maked on waiting port
              this.Slave.dropCall();// to drop call in the slave
              // notify for finish Java call - CCM.freePort()
              synchronized(this.resource){this.resource.notify();}
            }
            // remove itself from connection pool
            synchronized(CCM.aliveConnects)
            {
              CCM.aliveConnects.remove( this );
            }
          }
        }
    }
    /**
     * <mutator> To push free resource to the pool
     * @param resource resource to push
     */
    private final static void push(Wrapper resource){
      synchronized( CCM.freeChannels ) {CCM.freeChannels.add(resource);}
    }
    /**
     * <accessor>
     * To get access to first free Resource that support the Bus
     * @param bus the bus
     * @return the resource or null if no resources with this bus
     */
    private static Wrapper pop(Object bus){Wrapper resource = null;// result
      synchronized( CCM.freeChannels ) {
        ListIterator i = CCM.freeChannels.listIterator();
        if ( i.hasNext() ) {
          resource = (Wrapper)i.next(); i.remove();
        }
      }
      return resource;
    }
    /* class-wrapper for free port */
    private final static class Wrapper {
      /* time to expired */
      private long toExpired=-1;
      /* the free device */
      private CallControl device;
      /* constructor */
      Wrapper(CallControl device, long timeout){
        this.device=device; this.toExpired=timeout+System.currentTimeMillis();
      }
      /* get access to wrapped port*/
      public CallControl getDevice(){return this.device;}
      /* Is resource expired */
      final boolean isExpired(long current){return this.toExpired <= current;}
      /* to translate the object to String */
      public final String toString(){return "wrapper for ["+this.device+"] expired "+this.toExpired;}
    }

/**
 * <thread> thread for free the expired resources
 */
private static toFreeExpired collector=null;
/**
 * <action>To initialize Call Control Manager
 */
final static void init()
{
  synchronized(CCM.class)
  {
    if (CCM.collector == null)
    {
      // to start thread to free the expired shared resources
      (CCM.collector = new CCM.toFreeExpired()).start();
    }
  }
}
    /**
     * class for free expired ports
     */
    private final static class toFreeExpired extends Thread{
        /* constructor */
        toFreeExpired() {
            super.setDaemon( true );
            super.setName("to kill expired free ports");
            super.setPriority( Thread.MIN_PRIORITY );
        }
        public final void run()
        {
          final ArrayList expired = new ArrayList(),valid = new ArrayList();
          // Infinite loop
          while (true)
          {
              expired.clear(); valid.clear();
              long current = System.currentTimeMillis();
              // scan CCM container about expired ports
              synchronized (CCM.freeChannels)
              {   // to iterate resource Container
                  boolean haveExpired = false;
                  for (Iterator i = CCM.freeChannels.iterator();i.hasNext();)
                  {
                      Wrapper port = (Wrapper)i.next();
                      // check port expiration
                      if ( port.isExpired(current) ) {
                        expired.add( port );haveExpired = true;
                      }else valid.add( port );
                      // to transfer context to other threads
                      super.yield();
                  }
                  // have expired resources?
                  if (haveExpired)
                  {   // to made new container body
                      CCM.freeChannels.clear();CCM.freeChannels.addAll(valid);
                  }
              }
              // to free expired resources
              for (Iterator i= expired.iterator();i.hasNext();)
              {
                  Object resource = i.next();
                  // to notify for finish Java call - CCM.freePort()
                  synchronized(resource){resource.notify();}
                  // to transfer context to other threads
                  super.yield();
              }
              // to sleep 0.1 second, for observer's thread
              try{this.sleep(100);}catch(InterruptedException e){}
          }
        }
    }
}
