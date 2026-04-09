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


/**
Abstract unified message
*/
public interface Message
                    extends
                    MessageHeader
{
/**
Prefix of Attribut name
*/
String A_PREFIX = "JMSX_";
/**
<property name>
Property name of the identifier of the message (ID)
*/
String MESSAGE_ID = "JMSX_MESSAGE_ID";
/**
<property name>
Property name of the TimeStamp of the Message
*/
String TIMESTAMP = "JMSX_TIMESTAMP";
/**
<property name>
Property name of the correlation ID of this message
*/
String CORRELATION_ID = "JMSX_CORRELATION_ID";
/**
<property name>
Property name of the replyto destination
*/
String REPLYTO = "JMSX_REPLYTO";
/**
<property name>
Property name of the replyto destination
*/
String DESTINATION = "JMSX_DESTINATION";
/**
<property name>
Property name of the deliver mode
*/
String DELIVERMODE = "JMSX_DELIVERMODE";
/**
<property name>
Property name of the message type
*/
String TYPE = "JMSX_TYPE";
/**
<property name>
Property name of the message's expiration value
*/
String EXPIRATION = "JMSX_EXPIRATION";
/**
<property name>
Property name of the message priority
*/
String PRIORITY = "JMSX_PRIORITY";
/**
<property name>
Property name of the message priority
*/
String REDELIVERED = "JMSX_REDEVIVERED";
/**
<property name>
Property name of the text part of message body
*/
String BODYTEXT = "JMSX_BODYTEXT";
/**
<property name>
Property name of the text part of message body
*/
String BODYATTACHMENT = "JMSX_BODYATTACHMENT";
/**
<property name>
Name of delivery protocol property
*/
String FACTORY = "JMSX_FACTORY";
/**
<property name>
Name of delivery protocol property
*/
String PROTOCOL = "JMSX_PROTOCOL";
/**
<property name>
Name of delivery result property
*/
String RESULT = "JMSX_RESUL";
/**
<property value>
Value of successful delivery result
*/
String RESULT_OK = "OK";
/**
<property value>
Value of error delivery result
*/
String RESULT_ERROR = "ERROR";
/**
<property name>
Name of delivery result description property
*/
String MESSAGE = "JMSX_MESSAGE";
   /**
<mutator>
Clear out the message body. Clearing a message's body does not clear its header values or property entries.
If this message body was read-only, calling this method leaves the message body
is in the same state as an empty body in a newly created message.
   */
   void clearBody();

   /**
<accessor>
Get the text part of message body
   */
   String getBodyText();

   /**
<mutator>
Set the text part of message body
   */
   void setBodyText(String text);

   /**
<accessor>
Get the attachment part of message body
   */
   Object getBodyAttachment();

   /**
<mutator>
Set the attachment part of message body
   */
   void setBodyAttachment(Object attachment);
   /**
<mutator>
Set the attachment part of message body, like file
   */
   void setBodyAttachment(java.io.File attachmentFile);
   /**
<action>
To copy message contains to other message
   */
   void copyTo(Message message);
   /**
   class pair of Property name, Property value
   */
   public static class Pair implements java.io.Externalizable
   {
        /** constant for properly serialization/deserialzation */
        static final long serialVersionUID = -5919312595027392425L;
        /** attributes */
        private transient String name;
        private transient Object value;
        /** Empty constructor */
        public Pair(){}
        /** Constructor */
        public Pair(String name, Object value){this.name=name;this.value=value;}
        /** <accessor> get property name */
        public String getName(){return this.name;}
        /** <accessor> get property value */
        public Object getValue(){return this.value;}
        /** called, when serialize the Object */
        public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
            this.check(); out.writeUTF(this.name); out.writeObject(this.value);
        }
        /** called, when deserialize the Object */
        public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
            this.name = in.readUTF(); this.value = in.readObject(); this.check();
        }
        /** to check Pair integrity */
        private void check() throws java.io.IOException {
            if (this.name == null || this.value == null)
                throw new java.io.IOException("Invalid Pair Object, name or value is null ...");
        }
   }
   /**
   class destination for ct-messaging system
   */
   public static class Destination implements java.io.Externalizable, javax.jms.Destination
   {
        /** constant for properly serialization/deserialzation */
        static final long serialVersionUID = -4685394098387456156L;
        /** attribute */
        private transient String where;
        /** Empty constructor */
        public Destination(){}
        /** Constructor */
        public Destination(String where){this.where=where;}
        /** overrided Object.toString() */
        public String toString(){return this.where;}
        /** called, when serialize the Object */
        public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
            out.writeUTF( this.where );
        }
        /** called, when deserialize the Object */
        public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException{
            this.where = in.readUTF();
        }
   }
}
