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

import javax.jms.JMSException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import org.visualcti.server.message.Message;
/**
Base class-implementation of Message interface
*/
class BaseMessage implements Message
{
/** constant for properly serialization/deserialzation */
static final long serialVersionUID = -3393354388245863933L;
    /** empty constructor */
    public BaseMessage(){}
    /** the identifier of the message (ID) */
    transient String messageID="";
   /**
<accessor>
return the identifier of the message (ID)
   */
   public String getJMSMessageID(){return this.messageID;}

   /**
<mutator>
set message ID
   */
   public void setJMSMessageID(String messageID){this.messageID=messageID;}

   /** TimeStamp of the Message */
   transient long timestamp = System.currentTimeMillis();
   /**
<accessor>
return TimeStamp of the Message
   */
   public long getJMSTimestamp(){return this.timestamp;}

   /**
<mutator>
set TimeStamp of the Message
   */
   public void setJMSTimestamp(long timeStamp){this.timestamp=timeStamp;}

   /** correlation ID of this message */
   transient String correlationID = null;
   /**
<accessor>
returns correlation ID of this message
The CorrelationID header field is used for linking one message with another.
It typically links a reply message with its requesting message.

CorrelationID can hold either a provider-specific message ID,
an application-specific String or a provider-native byte[] value.
   */
   public String getJMSCorrelationID(){return this.correlationID;}

   /**
<mutator>
set correlation ID of this message
   */
   public void setJMSCorrelationID(String messageID){this.correlationID=messageID;}
   /** <accessor>
    * Gets the correlation ID as an array of bytes for the message.
    */
   public byte [] getJMSCorrelationIDAsBytes() throws javax.jms.JMSException
   {
        return this.correlationID.getBytes();
   }
   /** <mutator>
    * Sets the correlation ID as an array of bytes for the message.
    */
   public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws javax.jms.JMSException
   {
        this.correlationID = new String(correlationID);
   }

   /** replyto destination */
   transient javax.jms.Destination replyTo = new Message.Destination("");
   /**
<accessor>
returns replyto destination
   */
   public javax.jms.Destination getJMSReplyTo(){return this.replyTo;}

   /**
<mutator>
set replyto destination
   */
   public void setJMSReplyTo(javax.jms.Destination destination){this.replyTo=destination;}

    /** message destination */
    transient javax.jms.Destination destination = new Message.Destination("");
   /**
<accessor>
returns message destination
   */
   public javax.jms.Destination getJMSDestination(){return this.destination;}

   /**
<mutator>
set message destination
   */
   public void setJMSDestination(javax.jms.Destination destination){this.destination=destination;}

    /** deliver mode */
    transient int deliverMode = NON_PERSISTENT;
   /**
<accessor>
get deliver mode
A client marks a message as persistent if it feels
that the application will have problems if the message is lost in transit.
A client marks a message as non-persistent if an occasional lost message is tolerable.
   */
   public int getJMSDeliveryMode(){return this.deliverMode;}

   /**
<mutator>
set deliver mode
A client marks a message as persistent if it feels
that the application will have problems if the message is lost in transit.
A client marks a message as non-persistent if an occasional lost message is tolerable.
   */
   public void setJMSDeliveryMode(int mode)
   {
        if (mode == NON_PERSISTENT || mode == PERSISTENT) this.deliverMode=mode;
   }

   /** the message type */
   transient String type = "";
   /**
<accessor>
Get the message type.
The type header field contains the name of a message's definition.
for selector expression "Type = 'voice'"
   */
   public String getJMSType(){return this.type;}

   /**
<mutator>
Set the message type.
The type header field contains the name of a message's definition.
   */
   public void setJMSType(String type){this.type=type;}

   /** the message's expiration value */
   transient long expiration = -1L;
   /**
<accessor>
Get the message's expiration value.
When a message is sent, expiration is left unassigned.
After completion of the send method, it holds the expiration time of the message.
This is the sum of the time-to-live value specified by the client and the GMT at the time of the send.

If the time-to-live is specified as zero, expiration is set to zero which indicates the message does not expire.

When a message's expiration time is reached, a provider should discard it.
Return the time the message expires. It is the sum of the time-to-live value specified by the client,
and the GMT at the time of the send.
   */
   public long getJMSExpiration(){return this.expiration;}

   /**
<mutator>
Set the message's expiration value.
When a message is sent, expiration is left unassigned.
After completion of the send method, it holds the expiration time of the message.
This is the sum of the time-to-live value specified by the client and the GMT at the time of the send.

If the time-to-live is specified as zero, expiration is set to zero which indicates the message does not expire.

When a message's expiration time is reached, a provider should discard it.
Providers set this field when a message is sent.
This operation can be used to change the value of a message that's been received.
   */
   public void setJMSExpiration(long expiration){this.expiration = expiration;}

    /**
     * Gets the message's delivery time value.
     *
     * <p>
     * When a message is sent, the {@code JMSDeliveryTime} header field is
     * left unassigned. After completion of the {@code send} or
     * {@code publish} method, it holds the delivery time of the message.
     * This is the the difference, measured in milliseconds,
     * between the delivery time and midnight, January 1, 1970 UTC.
     * <p>
     * A message's delivery time is the earliest time when a JMS provider may
     * deliver the message to a consumer. The provider must not deliver messages
     * before the delivery time has been reached.
     *
     * @return the message's delivery time value
     * @throws JMSException if the JMS provider fails to get the delivery time due to
     *                      some internal error.
     * @see javax.jms.Message#setJMSDeliveryTime(long)
     * @since JMS 2.0
     */
    public long getJMSDeliveryTime() throws JMSException {
        return 0;
    }

    /**
     * Sets the message's delivery time value.
     * <p>
     * This method is for use by JMS providers only to set this field when a
     * message is sent. This message cannot be used by clients to configure the
     * delivery time of the message. This method is public to allow a JMS
     * provider to set this field when sending a message whose implementation is
     * not its own.
     *
     * @param deliveryTime the message's delivery time value
     * @throws JMSException if the JMS provider fails to set the delivery time due to
     *                      some internal error.
     * @see javax.jms.Message#getJMSDeliveryTime()
     * @since JMS 2.0
     */
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {

    }

    /** the message priority */
   transient int priority = 1;
   /**
<accessor>
Get the message priority.
JMS defines a ten level priority value with 0 as the lowest priority and 9 as the highest.
In addition, clients should consider priorities 0-4 as gradations of normal priority and
priorities 5-9 as gradations of expedited priority.
   */
   public int getJMSPriority(){return this.priority;}

   /**
<mutator>
Set the message priority.
JMS defines a ten level priority value with 0 as the lowest priority and 9 as the highest.
In addition, clients should consider priorities 0-4 as gradations of normal priority and
priorities 5-9 as gradations of expedited priority.
   */
   public void setJMSPriority(int priority){this.priority=priority;}

   /** is message redelivered */
   transient boolean redelivered;
   /** <accessor>
    * Gets an indication of whether this message is being redelivered.
    */
   public boolean getJMSRedelivered() throws javax.jms.JMSException
   {
        return this.redelivered;
   }
   /** <mutator>
    * Specifies whether this message is being redelivered.
    */
   public void setJMSRedelivered(boolean redelivered) throws javax.jms.JMSException
   {
        this.redelivered=redelivered;
   }

   /**
   message properties and fields container
   */
   transient Hashtable properties = new Hashtable();
   /**
<accessor>
Check if a property value exists.
   */
   public boolean propertyExists(String name){return this.properties.get(name) != null;}

   /**
<accessor>
Return the Java object property value with the given name.
   */
   public Object getObjectProperty(String name){return this.properties.get(name);}

   /**
<mutator>
Set a Java object property value with the given name, into the Message.
Note that this method only works for the objectified primitive
object types (Integer, Double, Long ...) and String's.
   */
   public void setObjectProperty(String name, Object value){this.properties.put(name, value);}

   /** <accessor> get byte property */
   public byte getByteProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Number)this.getObjectProperty(name)).byteValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get byte for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get byte for "+name);
        }
   }
   /** <mutator> set byte property */
   public void setByteProperty(String name, byte value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Byte(value));
   }

   /** <accessor> get short property */
   public short getShortProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Number)this.getObjectProperty(name)).shortValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get short for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get short for "+name);
        }
   }
   /** <mutator> set short property */
   public void setShortProperty(String name, short value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Short(value));
   }

   /** <accessor> get int property */
   public int getIntProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Number)this.getObjectProperty(name)).intValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get int for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get int for "+name);
        }
   }
   /** <mutator> set int property */
   public void setIntProperty(String name, int value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Integer(value));
   }

   /** <accessor> get long property */
   public long getLongProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Number)this.getObjectProperty(name)).longValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get long for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get long for "+name);
        }
   }
   /** <mutator> set long property */
   public void setLongProperty(String name, long value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Long(value));
   }

   /** <accessor> get float property */
   public float getFloatProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Number)this.getObjectProperty(name)).floatValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get float for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get float for "+name);
        }
   }
   /** <mutator> set float property */
   public void setFloatProperty(String name, float value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Float(value));
   }

   /** <accessor> get double property */
   public double getDoubleProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Number)this.getObjectProperty(name)).doubleValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get double for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get double for "+name);
        }
   }
   /** <mutator> set double property */
   public void setDoubleProperty(String name, double value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Double(value));
   }

   /** <accessor> get string property */
   public String getStringProperty(String name) throws javax.jms.JMSException
   {
        try {return this.getObjectProperty(name).toString();
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get String for "+name);
        }
   }
   /** <mutator> set string property */
   public void setStringProperty(String name, String value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, value);
   }

   /** <accessor> get boolean property */
   public boolean getBooleanProperty(String name) throws javax.jms.JMSException
   {
        try {return ((Boolean)this.getObjectProperty(name)).booleanValue();
        }catch(ClassCastException e){
            throw new javax.jms.MessageFormatException("get boolean for "+name);
        }catch(NullPointerException e){
            throw new javax.jms.MessageFormatException("get boolean for "+name);
        }
   }
   /** <mutator> set boolean property */
   public void setBooleanProperty(String name, boolean value) throws javax.jms.JMSException
   {
        this.setObjectProperty(name, new Boolean(value));
   }


   /**
<accessor>
Return an Enumeration of all user property names.
   */
   public Enumeration getPropertyNames()
   {
        Vector props = new Vector();
        for(Enumeration e=this.properties.keys();e.hasMoreElements();)
        {
            String name = (String)e.nextElement();
            if (name.startsWith(A_PREFIX)) continue;// to skip service names
            props.addElement( name );
        }
        return props.elements();
   }

   /** <action>
    * Acknowledges all consumed messages of the session of this consumed message.
    * Used for StreamMessages (maybe) :-)
    */
    public void acknowledge() throws javax.jms.JMSException{}
   /**
<mutator>
Clear a message's properties. The message header fields and body are not cleared.
   */
   public void clearProperties(){this.properties.clear();}
   /**
<mutator>
Clear out the message body. Clearing a message's body does not clear its header values or property entries.
If this message body was read-only, calling this method leaves the message body
is in the same state as an empty body in a newly created message.
   */
   public void clearBody(){this.bodyText = null;this.bodyAttachment=null;}

    /**
     * Returns the message body as an object of the specified type.
     * This method may be called on any type of message except for
     * <tt>StreamMessage</tt>. The message
     * body must be capable of being assigned to the specified type. This means
     * that the specified class or interface must be either the same as, or a
     * superclass or superinterface of, the class of the message body.
     * If the message has no body then any type may be specified and null is returned.
     * <p>
     *
     * @param c The type to which the message body will be assigned. <br>
     *          If the message is a {@code TextMessage} then this parameter must
     *          be set to {@code String.class} or another type to which
     *          a {@code String} is assignable. <br>
     *          If the message is a {@code ObjectMessage} then parameter must
     *          must be set to {@code java.io.Serializable.class} or
     *          another type to which the body is assignable. <br>
     *          If the message is a {@code MapMessage} then this parameter must
     *          be set to {@code java.util.Map.class} (or {@code java.lang.Object.class}). <br>
     *          If the message is a {@code BytesMessage} then this parameter must
     *          be set to {@code byte[].class} (or {@code java.lang.Object.class}). This method
     *          will reset the {@code BytesMessage} before and after use.<br>
     *          If the message is a
     *          {@code TextMessage}, {@code ObjectMessage}, {@code MapMessage}
     *          or {@code BytesMessage} and the message has no body,
     *          then the above does not apply and this parameter may be set to any type;
     *          the returned value will always be null.<br>
     *          If the message is a {@code Message} (but not one of its subtypes)
     *          then this parameter may be set to any type;
     *          the returned value will always be null.
     * @return the message body
     * @throws MessageFormatException <ul>
     *                                <li>if the message is a {@code StreamMessage}
     *                                <li> if the message body cannot be assigned to
     *                                the specified type
     *                                <li> if the message is an {@code ObjectMessage} and object
     *                                deserialization fails.
     *                                </ul>
     * @throws JMSException           if the JMS provider fails to get the message body due to
     *                                some internal error.
     * @since JMS 2.0
     */
    public <T> T getBody(Class<T> c) throws JMSException {
        return null;
    }

    /**
     * Returns whether the message body is capable of being assigned to the
     * specified type. If this method returns true then a subsequent call to the
     * method {@code getBody} on the same message with the same type argument would not throw a
     * MessageFormatException.
     * <p>
     * If the message is a {@code StreamMessage} then false is always returned.
     * If the message is a {@code ObjectMessage} and object deserialization
     * fails then false is returned. If the message has no body then any type may be specified and true is
     * returned.
     *
     * @param c The specified type <br>
     *          If the message is a {@code TextMessage} then this method will
     *          only return true if this parameter is set to
     *          {@code String.class} or another type to which a {@code String}
     *          is assignable. <br>
     *          If the message is a {@code ObjectMessage} then this
     *          method will only return true if this parameter is set to
     *          {@code java.io.Serializable.class} or another class to
     *          which the body is assignable. <br>
     *          If the message is a {@code MapMessage} then this method
     *          will only return true if this parameter is set to
     *          {@code java.util.Map.class} (or {@code java.lang.Object.class}). <br>
     *          If the message is a {@code BytesMessage} then this this
     *          method will only return true if this parameter is set to
     *          {@code byte[].class} (or {@code java.lang.Object.class}). <br>
     *          If the message is a
     *          {@code TextMessage}, {@code ObjectMessage}, {@code MapMessage}
     *          or {@code BytesMessage} and the message has no body,
     *          then the above does not apply and this method will return true
     *          irrespective of the value of this parameter.<br>
     *          If the message is a
     *          {@code Message} (but not one of its subtypes)
     *          then this method will return true
     *          irrespective of the value of this parameter.
     * @return whether the message body is capable of being assigned to the
     * specified type
     * @throws JMSException if the JMS provider fails to return a value due to some
     *                      internal error.
     */
    public boolean isBodyAssignableTo(Class c) throws JMSException {
        return false;
    }

    /** the text part of message body */
   transient String bodyText = "";
   /**
<accessor>
Get the text part of message body
   */
   public String getBodyText(){return this.bodyText;}

   /**
<mutator>
Set the text part of message body
   */
   public void setBodyText(String text){this.bodyText=text;}

   /** the attachment part of message body */
   transient Object bodyAttachment=null;
   /**
<accessor>
Get the attachment part of message body
   */
   public Object getBodyAttachment(){return this.bodyAttachment;}

   /**
<mutator>
Set the attachment part of message body
   */
   public void setBodyAttachment(java.io.File attachmentFile){this.bodyAttachment=attachmentFile;}
   /**
<mutator>
Set the attachment part of message body
   */
   public void setBodyAttachment(Object attachment){this.bodyAttachment=attachment;}
   /**
<action>
To copy message contains to other message
   */
   public void copyTo(Message messageI)
   {
        BaseMessage message;
        try {message = (BaseMessage)messageI;
        }catch(ClassCastException ce){
            return;// not base class
        }
        this.pack();
        message.clearBody(); message.clearProperties();
        Enumeration e = this.properties.keys();
        while( e.hasMoreElements() ) {
            String key = (String)e.nextElement();
            message.setObjectProperty(key, this.properties.get(key));
        }
        message.unpack();// to fill attributes from properties
   }
   /**
    * <action>
    *  to save changes, before store message
    */
   public void saveChanges(){this.pack();}
   /** <action>
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @serialData Overriding methods should use this tag to describe
     *             the data layout of this Externalizable object.
     *             List the sequence of element types and, if possible,
     *             relate the element to a public/protected field and/or
     *             method of this Externalizable class.
     *
     * @exception IOException Includes any I/O exceptions that may occur
     */
   public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException
   {
        this.pack();
        Enumeration e = this.properties.keys();
        while( e.hasMoreElements() ) {
            String key = (String)e.nextElement();
            Object value = this.properties.get(key);
            out.writeObject(new Message.Pair(key,value));
        }
        out.writeObject(null);
   }

   /** <action>
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
   public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException
   {
        Object o = null;
        this.properties = new Hashtable();// made new properties Object
        while( (o=in.readObject()) != null) {
            try {
                Message.Pair pair = (Message.Pair)o;
                this.properties.put(pair.getName(), pair.getValue());// store fields
            }catch(ClassCastException e){
                throw new java.io.IOException("Invalid stored BaseMessage object...");
            }
        }
        this.unpack();
   }
        /**
        <preparing>
        to store all fields values to properties, before serialization
        */
        private void pack()
        {
            this.properties.remove(MESSAGE_ID);
            if (this.messageID != null) this.properties.put(MESSAGE_ID, this.messageID);

            this.properties.put(TIMESTAMP, new Long(this.timestamp));

            this.properties.remove(CORRELATION_ID);
            if (this.correlationID != null) this.properties.put(CORRELATION_ID, this.correlationID);

            this.properties.remove(REPLYTO);
            if (this.replyTo != null) this.properties.put(REPLYTO, this.replyTo);

            this.properties.remove(DESTINATION);
            if (this.destination != null) this.properties.put(DESTINATION, this.destination);

            this.properties.put(DELIVERMODE, new Integer(this.deliverMode));

            this.properties.remove(TYPE);
            if (this.type != null) this.properties.put(TYPE, this.type);

            this.properties.put(EXPIRATION, new Long(this.expiration));

            this.properties.put(PRIORITY, new Integer(this.priority));

            this.properties.put(REDELIVERED, new Boolean(this.redelivered));

            this.properties.remove(BODYTEXT);
            if (this.bodyText != null) this.properties.put(BODYTEXT, this.bodyText);

            this.properties.remove(BODYATTACHMENT);
            if (this.bodyAttachment != null) this.properties.put(BODYATTACHMENT, this.bodyAttachment);
        }
        /**
        <preparing>
        to restore all fields values from properties, after deserialization
        */
        private void unpack()
        {
            Object value;
            value = this.properties.get(MESSAGE_ID);
            this.messageID = value == null ? null:value.toString();

            value = this.properties.get(TIMESTAMP);
            this.timestamp = value == null ? -1L:((Number)value).longValue();

            value = this.properties.get(CORRELATION_ID);
            this.correlationID = value == null ? null:value.toString();

            value = this.properties.get(REPLYTO);
            this.replyTo = new Message.Destination(value == null ? "":value.toString());

            value = this.properties.get(DESTINATION);
            this.destination = new Message.Destination(value == null ? "":value.toString());

            value = this.properties.get(DELIVERMODE);
            this.deliverMode = value == null ? NON_PERSISTENT:((Number)value).intValue();

            value = this.properties.get(TYPE);
            this.type = value == null ? null:value.toString();

            value = this.properties.get(EXPIRATION);
            this.expiration = value == null ? -1L:((Number)value).longValue();

            value = this.properties.get(PRIORITY);
            this.priority = value == null ? 1:((Number)value).intValue();

            value = this.properties.get(REDELIVERED);
            this.redelivered = value == null ? false:((Boolean)value).booleanValue();

            value = this.properties.get(BODYTEXT);
            this.bodyText = value == null ? null:value.toString();

            this.bodyAttachment = this.properties.get(BODYATTACHMENT);
        }
}
