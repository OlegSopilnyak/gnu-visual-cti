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
import org.visualcti.server.service.*;
import org.visualcti.util.Queue;
import org.visualcti.server.unitEvent;
import org.visualcti.server.unitError;

import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.SendMessageException;
import org.visualcti.server.message.MessageFactory;

/**
factory of messages,
part for sending message
*/
abstract class MessageFactory_SendingPart  extends ServiceAdapter
{
   /**
<action> To send the message to the consumer.
message placed to sentMessage queue for Deliver thread
   */
   public void send(Message message) throws MessageException
   {
        try{ this.savePersistent( message ); }catch(Exception e){}
        this.prepareToSend(message);
        dispatch( new unitEvent(this,"Message "+message+" prepared to send.") );
   }
   /**
<action>
to Start factory's sending
   */
   public void Start() throws IOException
   {
        if (this.outProtocol() == null) {
            String message =
                    "For message factory "+
                    this.getName()+
                    " sending protocol not defined";
            IOException error =  new IOException(message);
            dispatch(new unitError(this,error));
            throw error;
        }
        this.state = Service.State.START;
        this.restoreQueue();// to restore persistent messages
        this.startDeliver();// to start messages deliver process
   }
   /**
<action>
to Stop factory's sending
   */
   public void Stop() throws IOException
   {
        this.state = Service.State.STOP;
        this.stopDeliver();// to stop message deliver process
        this.storeQueue();// to store persistent messages
   }
/**
<accessor>
Get list of the not sent messages
*/
protected abstract Iterator notSentMessages();
/**
<home_directory>
File to Home Directory of this factory
*/
private File home = null;
/**
Outgoing messages factory protocol
*/
private String protocol = null;
   /**
<accessor> To get access to outgoing message protocol name
   */
   protected final String outProtocol(){return this.protocol;}
    /**
    process configuration
    */
    protected final void processConfiguration(Element xml)
    {
        Attribute persistent = xml.getAttribute("persistent");
        if (persistent == null) {
            xml.setAttribute(new Attribute("persistent","./persistent.messages"));
            persistent = xml.getAttribute("persistent");
        }
        File mainDir = new File(persistent.getValue());
        this.home = new File(mainDir,this.getName());
        if ( !this.home.exists() ) this.home.mkdirs();
        Element protocol = xml.getChild("protocol");
        this.solveProtocol(protocol);
    }
    /** to process protocol Element */
    protected void solveProtocol(Element xml)
    {
        if (xml == null) return;
        Attribute out = xml.getAttribute("out");
        if (out == null) return;
        this.protocol = out.getValue();
    }
    /**
    <accessor>
    to get access to persistent directory File
    */
    protected synchronized File getHome()
    {
        if (this.home != null) return this.home;
        synchronized(MessageFactory_SendingPart.class)
        {
            if (this.home != null) return this.home;
            Element xml = this.configuration;
            Attribute persistent = xml.getAttribute("persistent");
            if (persistent == null) {
                xml.setAttribute(new Attribute("persistent","./persistent.messages"));
                persistent = xml.getAttribute("persistent");
            }
            File mainDir = new File(persistent.getValue());
            this.home = new File(mainDir,this.getName());
            if ( !this.home.exists() ) this.home.mkdirs();
        }
        dispatch( new unitEvent(this,"Persistent directory is "+this.home.getAbsolutePath()) );
        return this.home;
    }

   /**
<action>
To start messages deliver process
   */
   protected void startDeliver(){}
   /**
<action>
To stop messages deliver process
   */
   protected void stopDeliver(){}
   /**
<transfer>
To prepare Message for sending by protocol
   */
   protected void prepareToSend(Message message) throws MessageException {}
        /**
        <action>
        to restore sentMessages queue
        */
        private void restoreQueue()
        {
            File messagesDir = this.getHome();
            String []list = messagesDir.list();
            if (list == null || list.length <= 0) return;
            // to iterate files list in directory
            for(int i=0; i < list.length; i++) {
                if ( !list[i].endsWith(".serialized") ) continue;
                File messageFile = new File(messagesDir,list[i]);
                try {// to read messages from file and push to sent queue
                    FileInputStream fin = new FileInputStream( messageFile );
                    ObjectInputStream in = new ObjectInputStream( fin );
                    Message message=(Message)in.readObject();
                    // prepare restored message to send
                    in.close(); this.prepareToSend(message);
                }catch(Exception e){}
            }
        }
        /**
        <action>
        to store the not sent Messages
        */
        private void storeQueue() throws IOException
        {
            for(Iterator i=this.notSentMessages();i.hasNext();)
            {
                Message message = (Message)i.next();
                this.savePersistent( message );
            }
        }
    /**
    <checker>
    is message need temporary storage before sending
    */
    protected final boolean isPersistent(Message message)
    {
        int mode = Message.NON_PERSISTENT;
        try {mode = message.getJMSDeliveryMode();
        }catch (javax.jms.JMSException e){}
        return mode == Message.PERSISTENT;
    }
        /**
        <action>
        to save persintent message to disk
        */
        private final void savePersistent(Message message) throws IOException {
            if ( !this.isPersistent(message) ) return;
            // to save persistent message to file
            File file = File.createTempFile("toSent", ".serialized", this.getHome());
            FileOutputStream o = new FileOutputStream( file );
            ObjectOutputStream out = new ObjectOutputStream( o );
            try {// store to message the file name
                message.setStringProperty(MessageFactory.PERSISTENT_FILE, file.getAbsolutePath());
                message.saveChanges();
            }catch(javax.jms.JMSException e) {}
            out.writeObject( message ); out.flush(); out.close();
            dispatch( new unitEvent(this,"Saved persistent message to temp "+file.getName()) );
        }
}
