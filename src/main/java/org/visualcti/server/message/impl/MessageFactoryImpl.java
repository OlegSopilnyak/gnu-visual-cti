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

import java.lang.reflect.*;
import java.util.*;
import java.io.*;


import org.jdom.*;
import org.visualcti.server.service.ServiceAdapter;
import org.visualcti.util.*;
import org.visualcti.server.*;

import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.SendMessageException;
import org.visualcti.server.message.MessageFactory;

/**
<Singleton>
factory of messages
features: create, send, receive
*/
public abstract 
class MessageFactoryImpl 
            extends MessageFactory_ReceivingPart
            implements MessageFactory
{
   /**
<action> to Start message factory
   */
   public final void Start() throws IOException
   {
        if ( this.isStarted() ) return;
        super.Start();
        this.dispatch(new unitEvent(this,unitEvent.START_ID));
   }
   /**
<action> to Stop message factory
   */
   public final void Stop() throws IOException
   {
        if ( this.isStopped() ) return;
        super.Stop();
        this.dispatch(new unitEvent(this,unitEvent.STOP_ID));
   }
   /**
<produce>
to create message for this factory, must be synchronized
   */
   public Message createMessage() throws MessageException
   {
        if (this.isStopped()){// factory stopped
            MessageException e = new MessageException("MessageFactory "+this.getName()+" has stopped...");
            dispatch( new unitError(this, e) ); throw e;
        }
        Message message = new BaseMessage();// make new message
        try {// to store factory name to message
            message.setStringProperty(Message.FACTORY, this.getName());
        }catch(javax.jms.JMSException e){
            throw new MessageException("Can't create message using "+this.getName()+" factory...");
        }
        return message;
   }
   /**
<produce>
to create message as reply to message for this factory, must be synchronized
   */
   public Message createReplyToMessage(Message source) throws MessageException
   {
        Message message = this.createMessage();
        source.copyTo( message );
        try {
            message.setStringProperty( Message.FACTORY, this.getName());
            message.setStringProperty( Message.PROTOCOL, this.outProtocol());
            message.setJMSDestination( source.getJMSReplyTo() );
            message.setJMSCorrelationID( source.getJMSMessageID() );
            message.clearBody();
        }catch (javax.jms.JMSException e){
            throw new MessageException("Can't create reply message using "+this.getName()+" factory...");
        }
        return message;
   }
   
   /**
   <checker>
   check, Is whether there corresponds the message to criteria of selection
   If, durint messages iteration, call returns true, then iteration must be
   have finished and return thie message
   */
   protected boolean isSelected(String selection, Message message) {
     try {
       Stack reversPolishRecord = SelectorParser.parse(selection);
       Stack data = new Stack();
       Object[] params = {data};
       while (!reversPolishRecord.empty()) {
         Object obj = reversPolishRecord.pop();
         if (obj instanceof Method) {
           ((Method)obj).invoke(null, params);
		 } else {
           String temp = (String)obj;
           if (Operators.isJavaIdentifier(temp)) {
             if (message.propertyExists(temp)) {data.push(message.getObjectProperty(temp));}
             else {throw new SelectorParseException("no such property: " + temp);}	 
           } else {
             if (Operators.hasFaildQuota(temp)) {
               throw new SelectorParseException("not valid argument: " + temp);
             } else if (Operators.isQuoted(temp)) {
               data.push(Operators.delQuotas(temp));
             } else if (temp.equalsIgnoreCase("true")||temp.equalsIgnoreCase("false")) {
               data.push(new Boolean(temp));
             } else {
               try {
                 data.push(new Double(temp));
               } catch (NumberFormatException e) {
                 throw new SelectorParseException("not valid argument: " + temp);
               }
             }
           }
         }
       }
     return ((Boolean)data.pop()).booleanValue();
     } catch (SelectorParseException spe) { return false;
     } catch (ClassCastException cce) { return false;
     } catch (EmptyStackException ese) { return false;
     } catch (IllegalAccessException iae) { return false;
     } catch (InvocationTargetException ite) { return false;
     } catch (javax.jms.JMSException jmse) { return false;
     }      
   }
}
