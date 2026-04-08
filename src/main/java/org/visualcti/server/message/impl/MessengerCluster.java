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
package org.visualcti.server.message.impl;

import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.DataConversionException;

import org.visualcti.util.remoteLink;
//import org.visualcti.util.Config;
import org.visualcti.server.*;
import org.visualcti.server.service.MasterServiceAdapter;

import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageFactory;
import org.visualcti.server.message.MessengerHandle;
import org.visualcti.server.message.MessageException;
/**
Messenger part for support remote features
*/
abstract
class MessengerCluster
        extends MasterServiceAdapter
{
/**
list of the remote objects,
implements MessengerHandle, from other Messenger
*/
private final Vector messengers = new Vector ();
/**
Name of RMI resource (Messenger Handle)
*/
private String rmiHandleName = "Messenger/Handle";
/**
remote links to other server Messengers (contains remoteLink objects)
*/
private final Vector links = new Vector();
    /**
    Constructor
    */
    public MessengerCluster(){super();}
    /**
    to start Cluster part of Messenger
    */
    public void Start() throws IOException
    {
        super.Start();// to start the slave services(message factories)
        //this.listen();// to start Messenger handle sharing
        this.startCluster(); // to start messages cluster support
    }
    /**
    to stop Cluster part of Messenger
    */
    public void Stop() throws IOException
    {
        super.Stop();// to stop the slave services(message factories)
        //this.unlisten(); // to stop sharing of Messenger handle
        this.stopCluster(); // to stop messages cluster support
    }
/** is need to save updated configuration Element */
protected boolean needSaveXML = false;
    /**
    process configuration from XML element
    */
    protected void processConfiguration(Element xml) {
        this.needSaveXML = false;
        // try to make RMI properties
        Element RMI = xml.getChild("rmi");
        if (RMI == null) {
            RMI = new Element("rmi").setText("Handle for other severs");
            RMI.setAttribute(new Attribute("handleName","Messenger/Handle"));
            RMI.setAttribute(new Attribute("class","org.visualcti.server.message.Messenger$Handle"));
            RMI.setAttribute(new Attribute("extends","org.visualcti.server.message.MessengerHandle"));
            xml.addContent( RMI ); this.needSaveXML = true;
        }
        Attribute handle = RMI.getAttribute("handleName");
        this.rmiHandleName = handle.getValue();
        // to process "remote" entries
        for(Iterator i = xml.getChildren("remote").iterator();i.hasNext();){
            remoteLink link = new remoteLink((Element)i.next(),this.rmiHandleName);
            this.needSaveXML = this.needSaveXML || link.needSaveXML;
            if ( link.isValid() ) this.links.addElement(link);
        }
    }
/////////////// REMOTE RMI /////////////////////////
        /**
        to add remote messenger handle to messengers list when connection established
        */
        private void addMessengerHanlde(MessengerHandle handle) {
            synchronized( this.messengers ){this.messengers.addElement(handle);}
        }
        /**
        to remove remote messenger handle to messengers list when connection is lost
        */
        private void removeMessengerHanlde(MessengerHandle handle) {
            synchronized( this.messengers ){this.messengers.removeElement(handle);}
        }
        /**
        to start messenger cluster support
        */
        private void startCluster() throws IOException {
            for(Enumeration e = this.links.elements();e.hasMoreElements();)
            {
                remoteLink link = (remoteLink)e.nextElement();
                new ClusterSupport(link.port,link.host,link.name).start();
            }
        }
        /**
        to stop cluster support
        */
        private void stopCluster() throws IOException  {
            synchronized(this.messengers){this.messengers.removeAllElements();}
            this.getThreadGroup().stop();// to kill all ClusterSupport threads
        }
//////////////// LOCAL RMI MANAGEMENT ///////////////
        /**
        To share Messenger handler
        calling from Start() method
        */
        private void listen() throws IOException
        {
            if (this.localRegistry() == null) throw new IOException("RMI registry not started...");
            dispatch(new unitEvent(this,"Try to bind handle to "+rmiHandleName));
            try {// to share Handle with RMI name...
		        this.localRegistry().rebind(this.rmiHandleName, new Handle());
            }catch(RemoteException re){
                dispatch( new unitError(this,re) );
                throw new IOException("Remote exception "+re.getMessage());
            }
            dispatch(new unitEvent(this,"Bind handle to "+rmiHandleName+" Success!"));
        }
        /**
        to unshare Messenger handler,
        calling from Stop() method
        */
        private void unlisten() throws IOException
        {
            if (this.localRegistry() == null) throw new IOException("RMI registry not started...");
		    try {// to destroy RMI name
		        this.localRegistry().unbind( rmiHandleName );
		    } catch (Exception e) {
                dispatch( new unitError(this,e) );
                throw new IOException("Remote exception "+e.getMessage());
		    }
            dispatch(new unitEvent(this,"Unbind handle to "+rmiHandleName+" Success!"));
        }
    /**
<accessor>
To get access to factory, using the name of factory
    */
    public abstract MessageFactory getFactory(String name);
    /**
<transfer>
to receive message from remote message factory
    */
    protected Message receiveRemote
                            (
                            String factoryName,
                            String selector,
                            Properties factoryProperties
                            )
                            throws MessageException
    {
        Message message=null;// reference to resulting Message
        Enumeration e;
        synchronized(this.messengers)
        {
            e = ((Vector)this.messengers.clone()).elements();
        }
        while ( e.hasMoreElements() ) {// to iterate the remote messengers list
            MessengerHandle handle = (MessengerHandle)e.nextElement();
           try {// to process handle
                message = handle.receive(factoryName, selector, factoryProperties);
                if (message != null) break;// the message received :-)
            }catch( RemoteException re){ // invalid reference to remote messenger
                dispatch( new unitError(this,re) );
                this.removeMessengerHanlde( handle );// to remove broken handle from list
            }catch ( MessageException me) {// message exception in remote messenger
                dispatch( new unitError(this,me) );
            } catch(ClassCastException ce) {// invalide object in messengers list
                dispatch( new unitError(this,ce) );
            } catch(NullPointerException ne){// I have no idea why this is occured
                dispatch( new unitError(this,ne) );
            }
        }
        return message;
    }
//////////////// INNNER CLASSES //////////
   /**
Object bind with name Messenger\Handle via RMI
Handle for remote acces to local messages list outside
   */
   final class Handle extends UnicastRemoteObject  implements MessengerHandle
   {
      public Handle() throws RemoteException {super();}
      /** the invocation of remote method receive(..) */
      public final Message receive
                            (
                            String factoryName,
                            String selector,
                            Properties properties
                            )
                            throws  MessageException,
                                    RemoteException
      {
            MessageFactory factory = getFactory( factoryName );
            if (factory == null) throw new MessageException("Invalide factory name "+factoryName);
            return factory.receive(selector,properties);
      }
    /**
    To check, is remote object availability?
    */
     public final void check() throws RemoteException {}
   }
   /**
    class for support connection to remote Messenger handle
    This thing need for made messengers Cluster
   */
   final class ClusterSupport extends Thread
   {
      String host,name; int port;
      /**
      Constructor
      */
      public ClusterSupport(int port,String host,String name)
      {
        super(MessengerCluster.this.getThreadGroup(),"ClusterSupport");
        this.port = port; this.host=host; this.name=name;
        this.setName("C-link to "+host+":"+this.port+"/"+this.name);
        this.setPriority(Thread.MIN_PRIORITY);
      }
      /**
        handle to remote messenger
      */
      private MessengerHandle handle = null;

      /**
        main loop of thread
        while (true){
        1. Try to connect to remote messenger
        2. If successed then call Messenger.this.addMessengerHandle(this.handle) .
        3. check connection.
        If connection not present call Messenger.this.removeMessengerHandle(this.handle).
        }
      */
      public void run() {
        if ( !this.stillWorks() ) return;
        dispatch( new unitEvent(MessengerCluster.this,this.getName()+" Started!"));
        try{
            do {
                if (this.handle == null) {// not connected
                    // try to connect
                    if ( this.connect() ) {// connected :-)
                        addMessengerHanlde(this.handle);// add for remote receive
                        dispatch(new unitEvent(MessengerCluster.this,this.getName()+" Connected..."));
                        continue;// to check
                    }
                } else {// still connected
                    // check connection integrity
                    if ( !this.check() ) {// check remote object validation
                        removeMessengerHanlde(this.handle);// remove for remote receive
                        this.handle = null;// setting up to not connected state
                        dispatch(new unitEvent(MessengerCluster.this,this.getName()+" Disconnected..."));
                        continue;// to connect
                    }
                }
                if ( this.stillWorks() )
                {   // delay 0.5 seconds, if Messenger still works
                    try{this.sleep(500);}catch(InterruptedException e){}
                }
            }while( this.stillWorks() );
        }catch(ThreadDeath death){
            throw death;
        }finally {
            dispatch(new unitEvent(MessengerCluster.this,this.getName()+" Stopped..."));
        }
      }
      /**
      to check is Messenger still started
      */
      private boolean stillWorks(){return isStarted();}
      /**
      Try, is it possible, to make connection to the remote Messenger's handle
      */
      private boolean connect(){
          try {// to get access to remote RMI registry
              checkNetworkSocket();
              Registry registry = LocateRegistry.getRegistry(this.host,this.port);
              // get and store remote reference to Messenger's handle
              this.handle = (MessengerHandle)registry.lookup(this.name);return true;
          }catch (Exception e){
              dispatch(new unitError(MessengerCluster.this,"Not available the RMI on :"+this.host));
              return false;
          }// bad luck :-(
      }
      /**
      To check availability of remote host by name and port
      */
      private void checkNetworkSocket() throws Exception
      {
        InetAddress server = InetAddress.getByName(this.host);
        Socket sock = new Socket(server,this.port);
        sock.close();
      }
      /**
      to check, is remote Messenger's handle accessible
      */
      private boolean check(){
          try{
              this.handle.check();
              return true;// handle is accessible now :-)
          }catch(RemoteException e){
              return false;
          }
      }
   }
}
