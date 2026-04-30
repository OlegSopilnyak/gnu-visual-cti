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
package org.visualcti.server.core.unit.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.XmlAware;
import org.visualcti.util.Tools;

/**
 * <prototype>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Server's Unit activity basic message</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface UnitMessage extends XmlAware, Cloneable {
    // message's root XML element name
    String ROOT_ELEMENT_NAME = "unit-action-message";
    // action's base parameters XML element name (inside root element)
    String BASE_ELEMENT_NAME = "base";
    // the name of description's attribute in serialization parameters
    String DESCRIPTION_PARAMETER_NAME = "@description";
    String BASE_MESSAGE_FAMILY_TYPE_ATTRIBUTE = "action";
    String BASE_MESSAGE_TYPE_ATTRIBUTE = "type";
    String BASE_MESSAGE_WHEN_ATTRIBUTE = "date";
    String BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE = "correlation-id";
    String BASE_MESSAGE_UNIT_PATH_ATTRIBUTE = "unitPath";

    /**
     * <accessor>
     * To get the messages family type of the message
     *
     * @return type of the unit's action message
     * @see MessageFamilyType
     */
    MessageFamilyType getFamilyType();

    /**
     * <mutator>
     * To set up the messages family type of the message
     *
     * @param messageFamilyType new value of message's family type
     * @return reference to the message
     */
    default UnitMessage setFamilyType(MessageFamilyType messageFamilyType) {
        // do nothing by default
        return this;
    }

    /**
     * <accessor>
     * To get the type of the message
     *
     * @return the message's type
     * @see MessageType
     */
    MessageType getMessageType();

    /**
     * <mutator>
     * To set up the type of the message
     *
     * @param messageType new value of message's type
     * @return reference to the message
     */
    default UnitMessage setMessageType(MessageType messageType) {
        // do nothing by default
        return this;
    }

    /**
     * <accessor>
     * To get the description
     *
     * @return description of the unit's action message
     */
    String getDescription();

    /**
     * <mutator>
     * To set up the description
     *
     * @param description new value of the unit's action message
     * @return reference to the message
     */
    UnitMessage setDescription(String description);

    /**
     * <accessor>
     * The date-time, when action has happened
     *
     * @return the value
     */
    Date getDate();

    /**
     * <mutator>
     * To set up the date-time, when action has happened
     *
     * @param dateTime new value of message's date-time
     * @return reference to the message
     */
    UnitMessage setDate(long dateTime);

    /**
     * <accesor>
     * To get access to Path of ServerUnit in UnitRegistry
     *
     * @return the unit-path associated with message
     */
    String getUnitPath();

    /**
     * <mutator>
     * To set up the Path of ServerUnit in UnitRegistry
     *
     * @param unitPath new value of message's unit-path
     * @return reference to the message
     */
    UnitMessage setUnitPath(String unitPath);

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see Parameter
     * @see UnitMessage#ROOT_ELEMENT_NAME
     * @see UnitMessage#DESCRIPTION_PARAMETER_NAME
     * @see XmlAware#store(OutputStream)
     */
    @Override
    default Element getXML() {
        final Element baseMessageXML = new Element(ROOT_ELEMENT_NAME).addContent(baseMessageXML());
        if (!"".equals(getDescription())) {
            baseMessageXML.addContent(Parameter.of(DESCRIPTION_PARAMETER_NAME, getDescription()).input().getXML());
        }
        return baseMessageXML;
    }

    /**
     * <converter>
     * To update the entity's fields from XML
     *
     * @param xml possible XML of te entity
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     * @see XmlAware#restore(InputStream)
     * @see #setupMessageParameter(Parameter)
     */
    @SuppressWarnings("unchecked")
    @Override
    default void setXML(final Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // to restore the base properties
        baseMessageXML(xml.getChild(BASE_ELEMENT_NAME));
        // Set up description of message by default
        setDescription("");
        // iterate message-parameter elements
        final List<Element> parameterElementsList = xml.getChildren(Parameter.ELEMENT);
        for (final Element parameterXml : parameterElementsList) {
            try {
                final Parameter parameter = Parameter.restore(parameterXml);
                if (parameter != null) {
                    // to set up message's parameter
                    setupMessageParameter(parameter);
                }
            } catch (Exception e) {
                Tools.error("Restore parameter error!");
                e.printStackTrace(Tools.err);
            }
        }
    }

    /**
     * <converter>
     * setting up message's parameter
     *
     * @param parameter of the message
     * @see #setXML(Element)
     * @see Parameter
     * @see #setDescription(String)
     * @see #updateMessagePropertyBy(Parameter)
     */
    default void setupMessageParameter(final Parameter parameter) {
        // updating the property of the message by restored parameter
        if (DESCRIPTION_PARAMETER_NAME.equals(parameter.getName())) {
            // description parameter is detected
            // if something went wrong, assign to description "Invalid action's description!"
            setDescription(parameter.getValue("Invalid action's description!"));
        } else {
            // updating not description executable property parameter
            updateMessagePropertyBy(parameter);
        }
    }

    /**
     * To update the message property by restored from XML Parameter instance
     *
     * @param parameter the value
     * @see #setupMessageParameter(Parameter)
     * @see Parameter
     */
    default void updateMessagePropertyBy(final Parameter parameter) {
        // do nothing by default for basic message
    }

    /**
     * <builder>
     * To make the base part of the Unit Action Message XML
     *
     * @return base part of the message XML
     * @see Element
     * @see Attribute
     * @see UnitMessage#getXML()
     * @see UnitMessage#BASE_ELEMENT_NAME
     * @see UnitMessage#BASE_MESSAGE_FAMILY_TYPE_ATTRIBUTE
     * @see UnitMessage#getFamilyType()
     * @see UnitMessage#BASE_MESSAGE_TYPE_ATTRIBUTE
     * @see UnitMessage#getMessageType()
     * @see UnitMessage#BASE_MESSAGE_WHEN_ATTRIBUTE
     * @see UnitMessage#getDate()
     * @see UnitMessage#BASE_MESSAGE_UNIT_PATH_ATTRIBUTE
     * @see UnitMessage#getUnitPath()
     * @see UnitMessage#BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE
     */
    default Element baseMessageXML() {
        final Element xml = new Element(BASE_ELEMENT_NAME);
        xml.setAttribute(messageAttribute(BASE_MESSAGE_FAMILY_TYPE_ATTRIBUTE, getFamilyType()));
        xml.setAttribute(messageAttribute(BASE_MESSAGE_TYPE_ATTRIBUTE, getMessageType()));
        xml.setAttribute(messageAttribute(BASE_MESSAGE_WHEN_ATTRIBUTE, getDate()));
        xml.setAttribute(messageAttribute(BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE, "_"));
        xml.setAttribute(messageAttribute(BASE_MESSAGE_UNIT_PATH_ATTRIBUTE, getUnitPath()));
        return xml;
    }

    static Attribute messageAttribute(String attributeName, MessageFamilyType type) {
        return messageAttribute(attributeName, type == null ? null : type.name());
    }

    static Attribute messageAttribute(String attributeName, MessageType type) {
        return messageAttribute(attributeName, type == null ? null : type.name());
    }

    static Attribute messageAttribute(String attributeName, Date dateTime) {
        return messageAttribute(attributeName, dateTime == null ? null : String.valueOf(dateTime.getTime()));
    }

    static Attribute messageAttribute(String attributeName, String value) {
        return new Attribute(attributeName, value == null ? "" : value);
    }

    /**
     * <builder>
     * To check and update basic attribute values of the message
     *
     * @param xml restored XML of the message
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     * @see UnitMessage#setXML(Element)
     * @see UnitMessage#checkAndUpdateMessageType(Element, MessageType)
     * @see UnitMessage#checkAndUpdateMessageType(Element, MessageFamilyType)
     * @see UnitMessage#checkAndUpdateMessageWhen(Element)
     * @see UnitMessage#BASE_MESSAGE_UNIT_PATH_ATTRIBUTE
     * @see UnitMessage#setUnitPath(String)
     */
    default void baseMessageXML(final Element xml)
            throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        if (xml == null) {
            throw new IOException("Invalid basic part of the message.");
        }
        // checking family and message types and updating them
        checkAndUpdateMessageType(xml, getMessageType());
        checkAndUpdateMessageType(xml, getFamilyType());
        // updating the rest property values of the message from the XML element
        checkAndUpdateMessageWhen(xml);
        setUnitPath(xml.getAttributeValue(BASE_MESSAGE_UNIT_PATH_ATTRIBUTE));

    }

    /**
     * <check-and-update>
     * To check and update message's type from xml
     *
     * @param xml  restored XML of the message
     * @param type expected type of the message
     * @throws IOException if something went wrong
     * @see Element
     * @see MessageType
     * @see #baseMessageXML(Element)
     * @see UnitMessage#BASE_MESSAGE_TYPE_ATTRIBUTE
     * @see #setMessageType(MessageType)
     */
    void checkAndUpdateMessageType(Element xml, MessageType type) throws IOException;

    /**
     * <check-and-update>
     * To check and update message family's type from xml
     *
     * @param xml  restored XML of the message
     * @param type expected type of the message family
     * @throws IOException if something went wrong
     * @see Element
     * @see MessageFamilyType
     * @see #baseMessageXML(Element)
     * @see UnitMessage#BASE_MESSAGE_FAMILY_TYPE_ATTRIBUTE
     * @see #setFamilyType(MessageFamilyType)
     */
    void checkAndUpdateMessageType(final Element xml, final MessageFamilyType type) throws IOException;

    /**
     * <check-and-update>
     * To restore or set up message creation date from xml
     *
     * @param xml restored XML of the message
     * @see Element
     * @see #baseMessageXML(Element)
     * @see UnitMessage#BASE_MESSAGE_WHEN_ATTRIBUTE
     * @see #setDate(long)
     */
    void checkAndUpdateMessageWhen(final Element xml);

    /**
     * <listener>
     * The listener of dispatched unit message
     *
     * @see UnitMessage
     */
    interface Listener {
        /**
         * <action>
         * to handle the server event
         *
         * @param message the message to handle by listener
         */
        void handleUnitMessage(UnitMessage message);
    }
}
