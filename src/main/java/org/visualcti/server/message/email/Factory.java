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
package org.visualcti.server.message.email;

import org.visualcti.server.*;
import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageFactory;
import org.visualcti.server.message.impl.ExternalMessageFactory;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.InvalidMessageException;

import org.jdom.*;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;

/**
Factory realize the e-mail messages processing
*/
public class Factory extends ExternalMessageFactory implements MessageFactory {
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
   public final String getType(){return super.getType()+" e-mail messages factory";}
   /**
<accessor> to get access to service name
   */
   public final String getName(){return "EMAIL";}

   /**
<produce>
To receive the message satisfying to a condition in selector, must be synchronized
(in selector expression LIKE SQL 92)
   */
   public final Message receive
                            (
                            String selector,
                            Properties factoryProperties
                            )
                            throws MessageException
   {
        if ( !existsMessage(selector,factoryProperties) ) return null;
        return this.receiveByProtocol(factoryProperties,true);
   }
private static final String FOR_RECEIVE = "JMSX_FOR_DELETE_MESSAGE_ID";
            /**
        <checker> is message exists
        In this method:
        Get the messages Enumeration for all received messages, iterate it
        If message is selected the save it message ID to factory properties
        and let finish iteration
            */
            private boolean existsMessage(String selector, Properties factoryProperties) {
                Enumeration e = this.receivedMessages(factoryProperties);
                factoryProperties.remove( FOR_RECEIVE );// remove value of selected message ID
                while( e.hasMoreElements() ) {
                    try{
                        Message message = (Message)e.nextElement();
                        if ( this.isSelected(selector,message) ) {
                            // to store message ID to properties
                            factoryProperties.put(FOR_RECEIVE,message.getJMSMessageID());
                            return true;// found
                        }
                    }catch(Exception ex){}
                }
                return false;// not found
            }
            /**
        <accessor>
        To get access to set of received messages for Properties set
            */
            private Enumeration receivedMessages(Properties factoryProperties) {
                Vector mess = new Vector();
                return mess.elements();
            }
   /**
<action>
To receive the message  by factory depended protocol.
must be synchronized
   */
   protected Message receiveByProtocol
                        (
                        Properties factoryProperties,
                        boolean withBody
                        )
                        throws MessageException
   {
        this.removeFromReceived(factoryProperties);
        throw new MessageException("Receive not realized...");
   }
        /**
        <action> to remove selected message from factoryProperties pool
        */
        private void removeFromReceived(Properties factoryProperties) {
                String messageID = factoryProperties.getProperty(FOR_RECEIVE);
                if (messageID == null) return;
        }
   /**
<action>
To send the message to the consumer by factory depended protocol.
   */
   protected void sendByProtocol(Message message) throws Exception
   {
        String protocol = message.getStringProperty(Message.PROTOCOL);
        if ( "SMTP".equalsIgnoreCase(protocol) ) {
            String server = message.getStringProperty(MessageFactory.SERVER);
            // to send MIME message
            Mailer.sendMessage(this, message, server);
        }else {
            String invalid = "Invalid delivery protocol "+protocol;
            InvalidMessageException e = new InvalidMessageException(invalid);
            dispatchEvent( new unitError(this,e) ); throw e;
        }
   }
}
