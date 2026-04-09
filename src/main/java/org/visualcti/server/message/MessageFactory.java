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
package org.visualcti.server.message;

import java.io.IOException;
import java.util.Properties;


import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;


/**
<Singleton>
factory of messages
features: create, send, receive
*/
public interface MessageFactory  {
/**
<property name>
Property name of the e-mail server name
*/
public static final String SERVER = Message.A_PREFIX + "SERVER";
/**
<property name>
Property name of the e-mail server database
*/
public static final String DATABASE = Message.A_PREFIX + "DATABASE";
/**
<property name>
Property name of the e-mail server's login
*/
public static final String LOGIN = Message.A_PREFIX + "LOGIN";
/**
<property name>
Property name of the e-mail server's password
*/
public static final String PASSWORD = Message.A_PREFIX + "PASSWORD";
/**
<property name>
Property name of the persistent file name
*/
public static final String PERSISTENT_FILE = Message.A_PREFIX + "PERSISTENT_FILE";
/**
<property name>
Property name of ID of receuved message
*/
public static final String FOR_RECEIVE = Message.A_PREFIX + "FOR_RECEIVE_MESSAGE_ID";
   /**
<action> to Start factory
if factory can't start, throws IOException
   */
   void Start() throws IOException;
   /**
<action> to Stop factory
if factory can't stop, throws IOException
   */
   void Stop() throws IOException;
   /**
<produce>
to create message for this factory
   */
   Message createMessage() throws MessageException;
   /**
<produce>
to create message as reply to message for this factory
   */
   Message createReplyToMessage(Message source) throws MessageException;
   /**
<transfer> To send the message to the consumer.
message placed to sentMessage queue for Deliver thread
If message is persistent, factory must store it
   */
   void send(Message message) throws MessageException;
   /**
<transfer>
To receive the message satisfying to a condition in selector, must be synchronized
(in selector expression LIKE SQL 92)
   */
   Message receive(String selector, Properties factoryProperties) throws MessageException;
}
