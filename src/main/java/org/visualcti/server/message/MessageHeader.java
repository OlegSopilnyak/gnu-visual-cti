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

import java.io.Externalizable;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Destination;
import javax.jms.DeliveryMode;
/**
The abstract unified message header
*/
public interface MessageHeader 
                    extends 
                    java.io.Externalizable,
                    javax.jms.Message 
{
   
   /**
the persistent delivery mode
(need to save message to file)
   */
   int PERSISTENT = DeliveryMode.PERSISTENT;
   
   /**
the non persistent delivery mode
(not need message saving)
   */
   int NON_PERSISTENT = DeliveryMode.NON_PERSISTENT;
   
    /** <accessor>
      * Gets the message ID.
      *
      * <P>The <CODE>JMSMessageID</CODE> header field contains a value that 
      * uniquely identifies each message sent by a provider.
      *  
      * <P>When a message is sent, <CODE>JMSMessageID</CODE> can be ignored. 
      * When the <CODE>send</CODE> or <CODE>publish</CODE> method returns, it 
      * contains a provider-assigned value.
      *
      * <P>A <CODE>JMSMessageID</CODE> is a <CODE>String</CODE> value that 
      * should function as a 
      * unique key for identifying messages in a historical repository. 
      * The exact scope of uniqueness is provider-defined. It should at 
      * least cover all messages for a specific installation of a 
      * provider, where an installation is some connected set of message 
      * routers.
      *
      * <P>All <CODE>JMSMessageID</CODE> values must start with the prefix 
      * <CODE>'ID:'</CODE>. 
      * Uniqueness of message ID values across different providers is 
      * not required.
      *
      * <P>Since message IDs take some effort to create and increase a
      * message's size, some JMS providers may be able to optimize message
      * overhead if they are given a hint that the message ID is not used by
      * an application. By calling the 
      * <CODE>MessageProducer.setDisableMessageID</CODE> method, a JMS client 
      * enables this potential optimization for all messages sent by that 
      * message producer. If the JMS provider accepts this
      * hint, these messages must have the message ID set to null; if the 
      * provider ignores the hint, the message ID must be set to its normal 
      * unique value.
      *
      * @return the message ID
      *
      * @exception JMSException if the JMS provider fails to get the message ID 
      *                         due to some internal error.
      * @see javax.jms.Message#setJMSMessageID(String)
      * @see javax.jms.MessageProducer#setDisableMessageID(boolean)
      */ 
    String getJMSMessageID() throws JMSException;


    /** <mutator>
      * Sets the message ID.
      *  
      * <P>JMS providers set this field when a message is sent. This method
      * can be used to change the value for a message that has been received.
      *
      * @param id the ID of the message
      *
      * @exception JMSException if the JMS provider fails to set the message ID 
      *                         due to some internal error.
      *
      * @see javax.jms.Message#getJMSMessageID()
      */ 
    void setJMSMessageID(String id) throws JMSException;


    /** <accesor>
      * Gets the message timestamp.
      *  
      * @return the message timestamp
      *
      * @exception JMSException if the JMS provider fails to get the timestamp
      *                         due to some internal error.
      *
      * @see javax.jms.Message#setJMSTimestamp(long)
      * @see javax.jms.MessageProducer#setDisableMessageTimestamp(boolean)
      */
    long getJMSTimestamp() throws JMSException;


    /** <mutator>
      * Sets the message timestamp.
      *  
      * <P>JMS providers set this field when a message is sent. This method
      * can be used to change the value for a message that has been received.
      *
      * @param timestamp the timestamp for this message
      *  
      * @exception JMSException if the JMS provider fails to set the timestamp
      *                         due to some internal error.
      *
      * @see javax.jms.Message#getJMSTimestamp()
      */
    void setJMSTimestamp(long timestamp) throws JMSException;


    /** <accessor>
      * Gets the correlation ID as an array of bytes for the message.
      *  
      * <P>The use of a <CODE>byte[]</CODE> value for 
      * <CODE>JMSCorrelationID</CODE> is non-portable.
      *
      * @return the correlation ID of a message as an array of bytes
      *
      * @exception JMSException if the JMS provider fails to get the correlation
      *                         ID due to some internal error.
      *  
      * @see javax.jms.Message#setJMSCorrelationID(String)
      * @see javax.jms.Message#getJMSCorrelationID()
      * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
      */
    byte [] getJMSCorrelationIDAsBytes() throws JMSException;


    /** <mutator>
      * Sets the correlation ID as an array of bytes for the message.
      * 
      * <P>The array is copied before the method returns, so
      * future modifications to the array will not alter this message header.
      *  
      * <P>If a provider supports the native concept of correlation ID, a 
      * JMS client may need to assign specific <CODE>JMSCorrelationID</CODE> 
      * values to match those expected by native messaging clients. 
      * JMS providers without native correlation ID values are not required to 
      * support this method and its corresponding get method; their 
      * implementation may throw a
      * <CODE>java.lang.UnsupportedOperationException</CODE>. 
      *
      * <P>The use of a <CODE>byte[]</CODE> value for 
      * <CODE>JMSCorrelationID</CODE> is non-portable.
      *
      * @param correlationID the correlation ID value as an array of bytes
      *  
      * @exception JMSException if the JMS provider fails to set the correlation
      *                         ID due to some internal error.
      *  
      * @see javax.jms.Message#setJMSCorrelationID(String)
      * @see javax.jms.Message#getJMSCorrelationID()
      * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
      */
    void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException;


    /** <mutator>
      * Sets the correlation ID for the message.
      *  
      * <P>A client can use the <CODE>JMSCorrelationID</CODE> header field to 
      * link one message with another. A typical use is to link a response 
      * message with its request message.
      *  
      * <P><CODE>JMSCorrelationID</CODE> can hold one of the following:
      *    <UL>
      *      <LI>A provider-specific message ID
      *      <LI>An application-specific <CODE>String</CODE>
      *      <LI>A provider-native <CODE>byte[]</CODE> value
      *    </UL>
      *  
      * <P>Since each message sent by a JMS provider is assigned a message ID
      * value, it is convenient to link messages via message ID. All message ID
      * values must start with the <CODE>'ID:'</CODE> prefix.
      *  
      * <P>In some cases, an application (made up of several clients) needs to
      * use an application-specific value for linking messages. For instance,
      * an application may use <CODE>JMSCorrelationID</CODE> to hold a value 
      * referencing some external information. Application-specified values 
      * must not start with the <CODE>'ID:'</CODE> prefix; this is reserved for 
      * provider-generated message ID values.
      *  
      * <P>If a provider supports the native concept of correlation ID, a JMS
      * client may need to assign specific <CODE>JMSCorrelationID</CODE> values 
      * to match those expected by clients that do not use the JMS API. A 
      * <CODE>byte[]</CODE> value is used for this
      * purpose. JMS providers without native correlation ID values are not
      * required to support <CODE>byte[]</CODE> values. The use of a 
      * <CODE>byte[]</CODE> value for <CODE>JMSCorrelationID</CODE> is 
      * non-portable.
      *  
      * @param correlationID the message ID of a message being referred to
      *  
      * @exception JMSException if the JMS provider fails to set the correlation
      *                         ID due to some internal error.
      *  
      * @see javax.jms.Message#getJMSCorrelationID()
      * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
      * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
      */ 
    void setJMSCorrelationID(String correlationID) throws JMSException;


    /** <accessor> 
      * Gets the correlation ID for the message.
      *
      * <P>This method is used to return correlation ID values that are 
      * either provider-specific message IDs or application-specific 
      * <CODE>String</CODE> values.
      *
      * @return the correlation ID of a message as a <CODE>String</CODE>
      *
      * @exception JMSException if the JMS provider fails to get the correlation
      *                         ID due to some internal error.
      *
      * @see javax.jms.Message#setJMSCorrelationID(String)
      * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
      * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
      */ 
    String getJMSCorrelationID() throws JMSException;


    /** <accessor>
      * Gets the <CODE>Destination</CODE> object to which a reply to this 
      * message should be sent.
      *  
      * @return <CODE>Destination</CODE> to which to send a response to this 
      *         message
      *
      * @exception JMSException if the JMS provider fails to get the  
      *                         <CODE>JMSReplyTo</CODE> destination due to some 
      *                         internal error.
      *
      * @see javax.jms.Message#setJMSReplyTo(Destination)
      */ 
    Destination getJMSReplyTo() throws JMSException;


    /** <mutator>
      * Sets the <CODE>Destination</CODE> object to which a reply to this 
      * message should be sent.
      *  
      * <P>The <CODE>JMSReplyTo</CODE> header field contains the destination 
      * where a reply 
      * to the current message should be sent. If it is null, no reply is 
      * expected. The destination may be either a <CODE>Queue</CODE> object or
      * a <CODE>Topic</CODE> object.
      *
      * <P>Messages sent with a null <CODE>JMSReplyTo</CODE> value may be a 
      * notification of some event, or they may just be some data the sender 
      * thinks is of interest.
      *
      * <P>Messages with a <CODE>JMSReplyTo</CODE> value typically expect a 
      * response. A response is optional; it is up to the client to decide.  
      * These messages are called requests. A message sent in response to a 
      * request is called a reply.
      *
      * <P>In some cases a client may wish to match a request it sent earlier 
      * with a reply it has just received. The client can use the 
      * <CODE>JMSCorrelationID</CODE> header field for this purpose.
      *
      * @param replyTo <CODE>Destination</CODE> to which to send a response to 
      *                this message
      *
      * @exception JMSException if the JMS provider fails to set the  
      *                         <CODE>JMSReplyTo</CODE> destination due to some 
      *                         internal error.
      *
      * @see javax.jms.Message#getJMSReplyTo()
      */ 
    void setJMSReplyTo(Destination replyTo) throws JMSException;


    /** <accessor>
      * Gets the <CODE>Destination</CODE> object for this message.
      *  
      * <P>The <CODE>JMSDestination</CODE> header field contains the 
      * destination to which the message is being sent.
      *  
      * <P>When a message is sent, this field is ignored. After completion
      * of the <CODE>send</CODE> or <CODE>publish</CODE> method, the field 
      * holds the destination specified by the method.
      *  
      * <P>When a message is received, its <CODE>JMSDestination</CODE> value 
      * must be equivalent to the value assigned when it was sent.
      *
      * @return the destination of this message
      *  
      * @exception JMSException if the JMS provider fails to get the destination
      *                         due to some internal error.
      *  
      * @see javax.jms.Message#setJMSDestination(Destination)
      */ 
    Destination getJMSDestination() throws JMSException;


    /** <mutator>
      * Sets the <CODE>Destination</CODE> object for this message.
      *  
      * <P>JMS providers set this field when a message is sent. This method 
      * can be used to change the value for a message that has been received.
      *
      * @param destination the destination for this message
      *  
      * @exception JMSException if the JMS provider fails to set the destination
      *                         due to some internal error.
      *  
      * @see javax.jms.Message#getJMSDestination()
      */ 
    void setJMSDestination(Destination destination) throws JMSException;


    /** <accessor>
      * Gets the <CODE>DeliveryMode</CODE> value specified for this message.
      *  
      * @return the delivery mode for this message
      *  
      * @exception JMSException if the JMS provider fails to get the 
      *                         delivery mode due to some internal error.
      *  
      * @see javax.jms.Message#setJMSDeliveryMode(int)
      * @see javax.jms.DeliveryMode
      */ 
    int getJMSDeliveryMode() throws JMSException;
 
 
    /** <mutator>
      * Sets the <CODE>DeliveryMode</CODE> value for this message.
      *  
      * <P>JMS providers set this field when a message is sent. This method 
      * can be used to change the value for a message that has been received.
      *
      * @param deliveryMode the delivery mode for this message
      *  
      * @exception JMSException if the JMS provider fails to set the 
      *                         delivery mode due to some internal error.
      *  
      * @see javax.jms.Message#getJMSDeliveryMode()
      * @see javax.jms.DeliveryMode
      */ 
    void setJMSDeliveryMode(int deliveryMode) throws JMSException;


    /** <accessor>
      * Gets an indication of whether this message is being redelivered.
      *
      * <P>If a client receives a message with the <CODE>JMSRedelivered</CODE> 
      * field set,
      * it is likely, but not guaranteed, that this message was delivered
      * earlier but that its receipt was not acknowledged
      * at that time.
      *
      * @return true if this message is being redelivered
      *  
      * @exception JMSException if the JMS provider fails to get the redelivered
      *                         state due to some internal error.
      *
      * @see javax.jms.Message#setJMSRedelivered(boolean)
      */ 
    boolean getJMSRedelivered() throws JMSException;
 
 
    /** <mutator>
      * Specifies whether this message is being redelivered.
      *  
      * <P>This field is set at the time the message is delivered. This
      * method can be used to change the value for a message that has
      * been received.
      *
      * @param redelivered an indication of whether this message is being
      * redelivered
      *  
      * @exception JMSException if the JMS provider fails to set the redelivered
      *                         state due to some internal error.
      *
      * @see javax.jms.Message#getJMSRedelivered()
      */ 
    void setJMSRedelivered(boolean redelivered) throws JMSException;


    /** <accessor>
      * Gets the message type identifier supplied by the client when the
      * message was sent.
      *
      * @return the message type
      *  
      * @exception JMSException if the JMS provider fails to get the message 
      *                         type due to some internal error.
      *
      * @see javax.jms.Message#setJMSType(String)
      */
    String getJMSType() throws JMSException;


    /** <mutator>
      * Sets the message type.
      *
      * <P>Some JMS providers use a message repository that contains the 
      * definitions of messages sent by applications. The <CODE>JMSType</CODE> 
      * header field may reference a message's definition in the provider's
      * repository.
      *
      * <P>The JMS API does not define a standard message definition repository,
      * nor does it define a naming policy for the definitions it contains. 
      *
      * <P>Some messaging systems require that a message type definition for 
      * each application message be created and that each message specify its 
      * type. In order to work with such JMS providers, JMS clients should 
      * assign a value to <CODE>JMSType</CODE>, whether the application makes 
      * use of it or not. This ensures that the field is properly set for those 
      * providers that require it.
      *
      * <P>To ensure portability, JMS clients should use symbolic values for 
      * <CODE>JMSType</CODE> that can be configured at installation time to the 
      * values defined in the current provider's message repository. If string 
      * literals are used, they may not be valid type names for some JMS 
      * providers.
      *
      * @param type the message type
      *  
      * @exception JMSException if the JMS provider fails to set the message 
      *                         type due to some internal error.
      *
      * @see javax.jms.Message#getJMSType()
      */
    void setJMSType(String type) throws JMSException;


    /** <accessor>
      * Gets the message's expiration value.
      *  
      * <P>When a message is sent, the <CODE>JMSExpiration</CODE> header field 
      * is left unassigned. After completion of the <CODE>send</CODE> or 
      * <CODE>publish</CODE> method, it holds the expiration time of the
      * message. This is the sum of the time-to-live value specified by the
      * client and the GMT at the time of the <CODE>send</CODE> or 
      * <CODE>publish</CODE>.
      *
      * <P>If the time-to-live is specified as zero, <CODE>JMSExpiration</CODE> 
      * is set to zero to indicate that the message does not expire.
      *
      * <P>When a message's expiration time is reached, a provider should
      * discard it. The JMS API does not define any form of notification of 
      * message expiration.
      *
      * <P>Clients should not receive messages that have expired; however,
      * the JMS API does not guarantee that this will not happen.
      *
      * @return the time the message expires, which is the sum of the
      * time-to-live value specified by the client and the GMT at the
      * time of the send
      *  
      * @exception JMSException if the JMS provider fails to get the message 
      *                         expiration due to some internal error.
      *
      * @see javax.jms.Message#setJMSExpiration(long)
      */ 
    long getJMSExpiration() throws JMSException;
 
 
    /** <mutator>
      * Sets the message's expiration value.
      *
      * <P>JMS providers set this field when a message is sent. This method 
      * can be used to change the value for a message that has been received.
      *  
      * @param expiration the message's expiration time
      *  
      * @exception JMSException if the JMS provider fails to set the message 
      *                         expiration due to some internal error.
      *
      * @see javax.jms.Message#getJMSExpiration() 
      */
    void setJMSExpiration(long expiration) throws JMSException;


    /** <accessor>
      * Gets the message priority level.
      *  
      * <P>The JMS API defines ten levels of priority value, with 0 as the 
      * lowest
      * priority and 9 as the highest. In addition, clients should consider
      * priorities 0-4 as gradations of normal priority and priorities 5-9
      * as gradations of expedited priority.
      *  
      * <P>The JMS API does not require that a provider strictly implement 
      * priority 
      * ordering of messages; however, it should do its best to deliver 
      * expedited messages ahead of normal messages.
      *  
      * @return the default message priority
      *  
      * @exception JMSException if the JMS provider fails to get the message 
      *                         priority due to some internal error.
      *
      * @see javax.jms.Message#setJMSPriority(int) 
      */ 
    int getJMSPriority() throws JMSException;


    /** <mutator>
      * Sets the priority level for this message.
      *  
      * <P>JMS providers set this field when a message is sent. This method 
      * can be used to change the value for a message that has been received.
      *
      * @param priority the priority of this message
      *  
      * @exception JMSException if the JMS provider fails to set the message 
      *                         priority due to some internal error.
      *
      * @see javax.jms.Message#getJMSPriority() 
      */ 
    void setJMSPriority(int priority) throws JMSException;


    /** <mutator>
      * Clears a message's properties.
      *
      * <P>The message's header fields and body are not cleared.
      *
      * @exception JMSException if the JMS provider fails to clear the message 
      *                         properties due to some internal error.
      */ 
    void clearProperties() throws JMSException;


    /** <accessor>
      * Indicates whether a property value exists.
      *
      * @param name the name of the property to test
      *
      * @return true if the property exists
      *  
      * @exception JMSException if the JMS provider fails to determine if the 
      *                         property exists due to some internal error.
      */
    boolean propertyExists(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>boolean</CODE> property with the  
      * specified name.
      *  
      * @param name the name of the <CODE>boolean</CODE> property
      *  
      * @return the <CODE>boolean</CODE> property value for the specified name
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid. 
      */ 
    boolean getBooleanProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>byte</CODE> property with the specified 
      * name.
      *  
      * @param name the name of the <CODE>byte</CODE> property
      *  
      * @return the <CODE>byte</CODE> property value for the specified name
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid. 
      */ 
    byte getByteProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>short</CODE> property with the specified 
      * name.
      *
      * @param name the name of the <CODE>short</CODE> property
      *
      * @return the <CODE>short</CODE> property value for the specified name
      *
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */
    short getShortProperty(String name) throws JMSException;
 
 
    /** <accessor>
      * Returns the value of the <CODE>int</CODE> property with the specified 
      * name.
      *  
      * @param name the name of the <CODE>int</CODE> property
      *  
      * @return the <CODE>int</CODE> property value for the specified name
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */ 
    int getIntProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>long</CODE> property with the specified 
      * name.
      *  
      * @param name the name of the <CODE>long</CODE> property
      *  
      * @return the <CODE>long</CODE> property value for the specified name
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */ 
    long getLongProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>float</CODE> property with the specified 
      * name.
      *  
      * @param name the name of the <CODE>float</CODE> property
      *  
      * @return the <CODE>float</CODE> property value for the specified name
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */ 
    float getFloatProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>double</CODE> property with the specified
      * name.
      *  
      * @param name the name of the <CODE>double</CODE> property
      *  
      * @return the <CODE>double</CODE> property value for the specified name
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */ 
    double getDoubleProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the <CODE>String</CODE> property with the specified
      * name.
      *  
      * @param name the name of the <CODE>String</CODE> property
      *  
      * @return the <CODE>String</CODE> property value for the specified name;
      * if there is no property by this name, a null value is returned
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */ 
    String getStringProperty(String name) throws JMSException;


    /** <accessor>
      * Returns the value of the Java object property with the specified name.
      *  
      * <P>This method can be used to return, in objectified format,
      * an object that has been stored as a property in the message with the 
      * equivalent <CODE>setObjectProperty</CODE> method call, or its equivalent
      * primitive <CODE>set<I>type</I>Property</CODE> method.
      *  
      * @param name the name of the Java object property
      *  
      * @return the Java object property value with the specified name, in 
      * objectified format (for example, if the property was set as an 
      * <CODE>int</CODE>, an <CODE>Integer</CODE> is 
      * returned); if there is no property by this name, a null value 
      * is returned
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                         value due to some internal error.
      */ 
    Object getObjectProperty(String name) throws JMSException;


    /** <accessor>
      * Returns an <CODE>Enumeration</CODE> of all the property names.
      *
      * <P>Note that JMS standard header fields are not considered
      * properties and are not returned in this enumeration.
      *  
      * @return an enumeration of all the names of property values
      *  
      * @exception JMSException if the JMS provider fails to get the property
      *                          names due to some internal error.
      */ 
    java.util.Enumeration getPropertyNames() throws JMSException;


    /** <mutator>
      * Sets a <CODE>boolean</CODE> property value with the specified name into 
      * the message.
      *
      * @param name the name of the <CODE>boolean</CODE> property
      * @param value the <CODE>boolean</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setBooleanProperty(String name, boolean value) throws JMSException;


    /** <mutator>
      * Sets a <CODE>byte</CODE> property value with the specified name into 
      * the message.
      *  
      * @param name the name of the <CODE>byte</CODE> property
      * @param value the <CODE>byte</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setByteProperty(String name, byte value) throws JMSException;


    /** <mutator>
      * Sets a <CODE>short</CODE> property value with the specified name into
      * the message.
      *  
      * @param name the name of the <CODE>short</CODE> property
      * @param value the <CODE>short</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setShortProperty(String name, short value) throws JMSException;


    /** <mutator>
      * Sets an <CODE>int</CODE> property value with the specified name into
      * the message.
      *  
      * @param name the name of the <CODE>int</CODE> property
      * @param value the <CODE>int</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setIntProperty(String name, int value) throws JMSException;


    /** <mutator>
      * Sets a <CODE>long</CODE> property value with the specified name into 
      * the message.
      *  
      * @param name the name of the <CODE>long</CODE> property
      * @param value the <CODE>long</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setLongProperty(String name, long value) throws JMSException;


    /** <mutator>
      * Sets a <CODE>float</CODE> property value with the specified name into 
      * the message.
      *  
      * @param name the name of the <CODE>float</CODE> property
      * @param value the <CODE>float</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setFloatProperty(String name, float value) throws JMSException;


    /** <mutator>
      * Sets a <CODE>double</CODE> property value with the specified name into 
      * the message.
      *  
      * @param name the name of the <CODE>double</CODE> property
      * @param value the <CODE>double</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setDoubleProperty(String name, double value) throws JMSException;


    /** <mutator>
      * Sets a <CODE>String</CODE> property value with the specified name into 
      * the message.
      *
      * @param name the name of the <CODE>String</CODE> property
      * @param value the <CODE>String</CODE> property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setStringProperty(String name, String value) throws JMSException;


    /** <mutator>
      * Sets a Java object property value with the specified name into the 
      * message.
      *  
      * <P>Note that this method works only for the objectified primitive
      * object types (<CODE>Integer</CODE>, <CODE>Double</CODE>, 
      * <CODE>Long</CODE> ...) and <CODE>String</CODE> objects.
      *  
      * @param name the name of the Java object property
      * @param value the Java object property value to set
      *  
      * @exception JMSException if the JMS provider fails to set the property
      *                          due to some internal error.
      * @exception MessageFormatException if the object is invalid
      * @exception MessageNotWriteableException if properties are read-only
      */ 
    void setObjectProperty(String name, Object value) throws JMSException;


    /** <action>
      * Acknowledges all consumed messages of the session of this consumed 
      * message.
      *  
      * <P>All consumed JMS messages support the <CODE>acknowledge</CODE> 
      * method for use when a client has specified that its JMS session's 
      * consumed messages are to be explicitly acknowledged.  By invoking 
      * <CODE>acknowledge</CODE> on a consumed message, a client acknowledges 
      * all messages consumed by the session that the message was delivered to.
      * 
      * <P>Calls to <CODE>acknowledge</CODE> are ignored for both transacted 
      * sessions and sessions specified to use implicit acknowledgement modes.
      *
      * <P>A client may individually acknowledge each message as it is consumed,
      * or it may choose to acknowledge messages as an application-defined group 
      * (which is done by calling acknowledge on the last received message of the group,
      *  thereby acknowledging all messages consumed by the session.)
      *
      * <P>Messages that have been received but not acknowledged may be 
      * redelivered.
      *
      * @exception JMSException if the JMS provider fails to acknowledge the
      *                         messages due to some internal error.
      * @exception IllegalStateException if this method is called on a closed
      *                         session.
      *
      * @see javax.jms.Session#CLIENT_ACKNOWLEDGE
      */ 
    void acknowledge() throws JMSException;


    /** <mutator>
      * Clears out the message body. Clearing a message's body does not clear 
      * its header values or property entries.
      *
      * <P>If this message body was read-only, calling this method leaves
      * the message body in the same state as an empty body in a newly
      * created message.
      *
      * @exception JMSException if the JMS provider fails to clear the message
      *                         body due to some internal error.
      */
    void clearBody() throws JMSException;
    
    /**
    * <action>
    *  to save changes, before store message
    */
    void saveChanges();
    
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
    void writeExternal(java.io.ObjectOutput out) throws java.io.IOException;

    /** <action>
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException;
}
