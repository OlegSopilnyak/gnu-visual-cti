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

import javax.jms.JMSException;
import org.visualcti.util.Queue;
import org.visualcti.server.*;
import org.visualcti.server.message.*;
/**
MessageFactory for external messaging, like e-mail, Domino, JMS, etc.
*/
public abstract class ExternalMessageFactory extends MessageFactoryImpl
{
////////////////////// SEND MESSAGE begin //////////////////////////
   /**
<action>
To send the message to the consumer by factory depended protocol.
If not success message place to MessageFactory.this.sentMessages
must be synchronized
   */
protected abstract void sendByProtocol(Message message) throws Exception;
   /**
<queue>
Queue of sent messages
   */
private final Queue sentMessagesQueue = new Queue();
   /**
Message factory deliver
    */
private Deliver deliver = null;
   /**
<action>
To start messages deliver process
   */
   protected final void startDeliver()
   {
        (this.deliver = new Deliver()).start();// to start message deliver
   }
   /**
<action>
To stop messages deliver process
   */
   protected final void stopDeliver(){
       if (this.sentMessagesQueue.isWait()) this.sentMessagesQueue.push(null);
       try{this.deliver.join();}catch(Exception e){}// to wait while deliver-thread work
       this.deliver = null; // clear refer to messages deliver-thread
   }
   /**
<transfer>
To prepare Message for sending by protocol
   */
   protected void prepareToSend(Message message) throws MessageException
   {
        if ( !this.isStarted() ) throw new MessageException("Message factory "+this.getName()+" not started.");
        this.sentMessagesQueue.push(message); Thread.yield();
   }
   /**
   <accessor>
   Get list of the not sent messages
   */
   protected final Iterator notSentMessages()
   {
        ArrayList notSent = new ArrayList();
        if ( this.isStarted() ) return notSent.iterator();
        while( !this.sentMessagesQueue.empty() )
        {
            Message message = (Message)this.sentMessagesQueue.pop();
            if (message == null) continue;
            notSent.add(message);
        }
        return notSent.iterator();
   }
        /**
        <action>
        to remove delivered persistent message from disk
        */
        private void removePersistent(Message message) throws IOException {
            if ( this.isPersistent(message) ) 
                try {File file = new File(message.getStringProperty(MessageFactory.PERSISTENT_FILE));
                    synchronized(MessageFactory_SendingPart.class) {file.delete();}
                    dispatch( new unitEvent(this,"Removed persistent message "+file.getName()) );
                }catch(Exception e){}
        }
   /**
class for deliviring message to consumer
by protocol, use MessageFactory.sendByProtocol
message will get from sentMessages queue
   */
   final private class Deliver extends Thread {
      /** constructor */
      public Deliver() {
        super(ExternalMessageFactory.this.getThreadGroup(),"Deliver");
        this.setName("Messages deliver for "+ExternalMessageFactory.this.getName());
        this.setPriority(Thread.MIN_PRIORITY);
      }
      /**
main loop of Deliver thread
1. try to get message from MessageFactory.this.sentMessage queue
2. call MessageFactory.this.sendByProtocol for send
3. If send is not succesful then place back message to the queue
      */
      public final void run()
      {
        this.event("Start");
        while( ExternalMessageFactory.this.isStarted() ) 
        {
            Message message = (Message)sentMessagesQueue.pop();
            if ( isStarted() ) // is factory started
            {
                if (message == null) continue;// null message placed by mistake
                try {// try to delivery message using factory's protocol
                    sendByProtocol( message );
                    removePersistent( message );// remove from directory
                }catch (SendMessageException e){// place message back to sent queue
                    // message will be redelivered
                    try {message.setJMSRedelivered( true );}catch(javax.jms.JMSException je){}
                    sentMessagesQueue.push(message);
                    String description = "Can's send message from "+this.getName()+" e:"+e.getMessage();
                    unitEvent event = new unitEvent(ExternalMessageFactory.this,description);
                    dispatch(event);
                    try{this.sleep(100);}catch(Exception se){}// to sleep 0.1 seconds
                }catch (Exception e){// other exception
                    String description = this.getName()+" can't delivery message because "+e.getMessage();
                    unitError error = new unitError(ExternalMessageFactory.this,description);
                    dispatch( error );
                }
            } else break;// MessageFactory_SendingPart has stopped
        }
        this.event("Stop");
      }
        /** to dispatch Deliver event */
        private void event(String what)
        {
            String description = what+" Deliver for >"+ExternalMessageFactory.this.getName();
            unitEvent event = new unitEvent(ExternalMessageFactory.this,description);
            dispatch( event );
        }
   }
////////////////////// SEND MESSAGE end //////////////////////////
////////////////////// RECEIVE MESSAGE begin //////////////////////////
/**
Pool of receiver message headers threads
threads will call this.messageReceived(String protocol,Message message),
when receive the new message header
*/
private final HashMap threads = new HashMap();
    /**
<action>
To start thread for background receive the message headers
Thread can use the getPool(protocol) for get pool of received headers
    */
    protected void startReceiverThread(String protocol)
    {
        Vector pool = this.getPool(protocol);
    }
    /**
<action>
To stop thread for background receive the message headers
    */
    protected void stopReceiverThread(String protocol)
    {
    }
    /**
To add receiving entity for receiving threads
    */
    protected void addReceivingEntity(Properties factory)
    {
    }
////////////////////// RECEIVE MESSAGE end //////////////////////////
}
