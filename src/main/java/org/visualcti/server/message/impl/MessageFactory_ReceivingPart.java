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

import org.jdom.*;
import org.visualcti.server.*;
import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.MessageFactory;
/**
factory of messages,
part for receiving message
*/
abstract
class MessageFactory_ReceivingPart
            extends MessageFactory_SendingPart
{
   /**
<action>
to Start factory's receiving
   */
   public void Start() throws IOException
   {
        if (this.protocol == null)
        {
            String message = 
                    "For message factory "+
                    this.getName()+
                    " receiving protocol not defined";
            IOException error =  new IOException(message);
            dispatch(new unitError(this,error));
            throw error;
        }
        super.Start();// to start sender part
        this.startReceivers();
   }
   /**
<action>
to Stop factory's receiving
   */
   public void Stop() throws IOException
   {
        super.Stop();// to stop sender part
        this.stopReceivers();
   }
/**
Incoming messages factory's protocols
*/
private String protocol = null;
   /**
<accessor>
To get access to incoming message protocol name
   */ 
   protected final String inProtocol(){return this.protocol;}
   /**
   to process protocol Element
   */
   protected final void solveProtocol(Element xml)
   {
       super.solveProtocol( xml );
       if (xml == null) return;
       Attribute protocol = xml.getAttribute("in");
       if (protocol == null) return;
       this.protocol = protocol.getValue();
   }
   /**
<transfer>
To receive the message satisfying to a condition in selector, must be synchronized
(in selector expression LIKE SQL 92)
   */
   public Message receive
                   (
                   String selector,
                   Properties factoryProperties
                   )
                   throws MessageException
   {
        String protocol = factoryProperties.getProperty(Message.PROTOCOL);
        if ( !this.isValid(protocol) ) throw new MessageException("Invalid receiving protocol "+protocol);
        String server   = factoryProperties.getProperty(MessageFactory.SERVER);
        String login    = factoryProperties.getProperty(MessageFactory.LOGIN);
        String password = factoryProperties.getProperty(MessageFactory.PASSWORD);
        if (server == null || login == null || password == null)
        {
            throw new MessageException("Invalid factory properties "+factoryProperties);
        }
        this.addReceivingEntity(factoryProperties);
        return this.receivedLocal(selector, factoryProperties);
   }
   /**
   To add receiving entity for receiver process
   */
   protected void addReceivingEntity(Properties factory)
   {
   }
        /**
        To get the local received messages
        */
        private Message receivedLocal(String selector,Properties factory) throws MessageException
        {
            String protocol = factory.getProperty(Message.PROTOCOL);
            Vector pool = this.getPool(protocol);
            Enumeration e;
            synchronized(pool){e=((Vector)pool.clone()).elements();}
            while( e.hasMoreElements() ) {
                Message message = (Message)e.nextElement();
                if (this.isSelected(selector,message))
                {
                    return this.getFull(message,factory);
                }
            }
            return null;
        }
/**
<checker>
check, Is whether there corresponds the message to criteria of selection
If, durint messages iteration, call returns true, then iteration must be
have finished and return thie message
*/
protected abstract boolean isSelected(String selection, Message message);
/**
<transfer>
To receive the message  by factory depended protocol.
*/
protected abstract Message receiveByProtocol
                                    (
                                    Properties factoryProps,
                                    boolean withBody
                                    )
                                    throws MessageException;
////////////////// PRIVATE METHODS /////////////
        /**
        get body of message, using message header
        */
        private Message getFull
                            (
                            Message message,
                            Properties factory
                            )
                            throws MessageException
        {
            String protocol = factory.getProperty(Message.PROTOCOL);
            try {
                factory.put(MessageFactory.FOR_RECEIVE,message.getJMSMessageID());
                String fileName = message.getStringProperty(MessageFactory.PERSISTENT_FILE);
                if (fileName != null) new File(fileName).delete();
            }catch(javax.jms.JMSException e){}
            Vector pool = this.getPool(protocol);
            // to remove message header from message headers pool
            synchronized( pool ) 
            {   // maybe this message was received from other Thread
                if ( pool.contains(message) ) pool.removeElement(message);
                else return null;
            }
            // to receive message with body from factory
            return this.receiveByProtocol(factory, true);
        }
        /**
        check is valid protocol defined
        */
        private boolean isValid(String protocol)throws MessageException
        {
            if (protocol == null) return false;
            String valid = this.inProtocol();
            if (valid == null) throw new MessageException("Invalid factory inProtocol()");
            StringTokenizer st = new StringTokenizer(valid," ,");
            while( st.hasMoreTokens() ) {
                if (st.nextToken().equalsIgnoreCase(protocol)) return true;
            }
            return false;
        }
/**
Pool of received message headers
*/
private final HashMap messages = new HashMap();
    /**
    Call-back method from receiver threads
    */
    protected void messageReceived(String protocol,Message message)
    {
        Vector pool = this.getPool(protocol);
        synchronized(pool){pool.addElement(message);}
        File dir = new File(super.getHome(),protocol);
        if ( !dir.exists() ){dir.mkdirs();return;}
        this.saveMessage(dir, message);
    }
    /**
    to get messages pool for protocol
    */
    protected Vector getPool(String protocol)
    {
        Vector pool = (Vector)this.messages.get(protocol);
        if (pool != null) return pool;
        synchronized(this.messages)
        {
            if ((pool = (Vector)this.messages.get(protocol)) != null) return pool;
            pool = new Vector();
            this.messages.put(protocol, pool);
        }
        return pool;
    }
        /**
        To start receivers threads
        */
        private final void startReceivers()
        {
            StringTokenizer st = new StringTokenizer(this.protocol," ,");
            while( st.hasMoreTokens() )
            {
                String protocol = st.nextToken();
                restore( protocol );// to restore saved messages
                startReceiverThread( protocol );
            }
        }
    /**
<action>
To start thread for background receive the message headers
    */
    protected void startReceiverThread(String protocol)
    {
    }
        /**
        To stop receivers threads
        */
        private final void stopReceivers()
        {
            StringTokenizer st = new StringTokenizer(this.protocol," ,");
            while( st.hasMoreTokens() )
            {
                String protocol = st.nextToken();
                store( protocol );// to store received message headers
                stopReceiverThread(protocol);
            }
        }
    /**
<action>
To stop thread for background receive the message headers
    */
    protected void stopReceiverThread(String protocol)
    {
    }
        /**
        To store the message headers for protocol
        */
        private final void store(String protocol)
        {
            File dir = new File(super.getHome(),protocol);
            if ( !dir.exists() ){dir.mkdirs();return;}
            Vector pool = this.getPool(protocol);
            synchronized(pool)
            {
                for(Enumeration e = pool.elements();e.hasMoreElements();){
                    Message message = (Message)e.nextElement();
                    this.saveMessage(dir, message);
                }
                pool.clear();// clear protocol messages
            }
        }
            /**
            to save message to disk
            */
            private void saveMessage(File dir,Message message)
            {
                try {
                    String fileName = message.getStringProperty(MessageFactory.PERSISTENT_FILE);
                    if (fileName != null && new File(fileName).exists()) return;// message already stored
                }catch(javax.jms.JMSException e){}
                try {// to save received message header to file
                    File file = File.createTempFile("Received_", ".serialized", dir);
                    FileOutputStream o = new FileOutputStream( file );
                    ObjectOutputStream out = new ObjectOutputStream( o );
                    try {// store to message rpoperties the file name
                        message.setStringProperty(MessageFactory.PERSISTENT_FILE, file.getAbsolutePath());
                        message.saveChanges();
                    }catch(javax.jms.JMSException jmse) {}
                    out.writeObject( message ); out.flush(); out.close();
                }catch(IOException ioe){}
            }
        /**
        To restore saved message headers for protocol
        */
        private final void restore(String protocol)
        {
            File dir = new File(super.getHome(),protocol);
            if ( !dir.exists() ){dir.mkdirs();return;}
            // to clear old protocol pool
            Vector pool = this.getPool(protocol);
            synchronized(pool){pool.clear();}
            // get file names from directory
            String []names = dir.list();
            for(int i=0;i < names.length;i++){
                if ( !names[i].endsWith(".serialized") ) continue;
                File file = new File(dir,names[i]);
                try{
                    FileInputStream fin = new FileInputStream(file);
                    ObjectInputStream in = new ObjectInputStream(fin);
                    Message message = (Message)in.readObject(); in.close();
                    this.messageReceived(protocol, message);
                }catch(Exception e){}
            }
        }
}
