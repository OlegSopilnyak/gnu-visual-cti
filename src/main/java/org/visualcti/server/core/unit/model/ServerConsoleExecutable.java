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
package org.visualcti.server.core.unit.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.XmlAware;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Server Console Executable Entities</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface ServerConsoleExecutable extends UnitActionMessage {
    String LINK_NAME_PARAMETER_NAME = "@link-name";

    /**
     * <accessor>
     * To get the correlation ID of the executable message
     * Uses, in general, for correlation request/response pair
     *
     * @return current message correlation ID
     */
    String getCorrelationID();

    /**
     * <mutator>
     * To set up the correlation ID of the executable message
     *
     * @param correlationId new value of message's correlation ID
     * @return reference to the message
     */
    ServerConsoleExecutable setCorrelationID(String correlationId);

    /**
     * To prepare parameters container for updates
     *
     * @see #setParameter(Parameter)
     */
    void initializeParameters();

    /**
     * <accessor>
     * To get the stream to executable parameters
     *
     * @return stream of available parameters
     * @see Parameter
     */
    Stream<Parameter> getParameters();

    /**
     * <accessor>
     * To get the parameter by name
     *
     * @param name the name of the parameter
     * @return parameter value or empty
     * @see Parameter
     * @see Optional
     */
    Optional<Parameter> getParameter(String name);

    /**
     * <mutator>
     * To set up the parameter of executable (parameter instance shouldn't be null)
     *
     * @param parameter the parameter value to set for executable
     * @return reference to the executable
     * @see Parameter
     */
    ServerConsoleExecutable setParameter(Parameter parameter);

    /**
     * <accessor>
     * To get the name of the link to the console shell
     *
     * @return the name of the link
     */
    String getLinkName();

    /**
     * <mutator>
     * To set up the name of the link to the console shell
     *
     * @param linkName the name of the link
     * @return reference to the executable
     */
    ServerConsoleExecutable setLinkName(String linkName);

    /**
     * <builder>
     * To make the base part of the Server Console Executable XML
     *
     * @return base part of the message XML
     * @see Element
     * @see Attribute
     * @see UnitActionMessage#baseMessageXML()
     * @see ServerConsoleExecutable#getCorrelationID()
     * @see UnitActionMessage#BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE
     */
    @Override
    default Element baseMessageXML() {
        final Element baseMessageXML = UnitActionMessage.super.baseMessageXML();
        baseMessageXML.setAttribute(new Attribute(BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE, getCorrelationID()));
        return baseMessageXML;
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see Parameter
     * @see ServerConsoleExecutable#LINK_NAME_PARAMETER_NAME
     * @see UnitActionMessage#ROOT_ELEMENT_NAME
     * @see XmlAware#store(OutputStream)
     */
    @Override
    default Element getXML() {
        final Element xml = UnitActionMessage.super.getXML();
        xml.addContent(new Parameter(LINK_NAME_PARAMETER_NAME, getLinkName()).getXML());
        getParameters().filter(Objects::nonNull).filter(parameter -> !parameter.getName().startsWith("@"))
                .forEach(parameter -> xml.addContent(parameter.getXML()));
        return xml;
    }

    /**
     * <builder>
     * To check and update basic attribute values of the message with correlation
     *
     * @param xml restored XML of the message
     * @see Element
     * @see Attribute
     * @see UnitActionMessage#baseMessageXML(Element)
     * @see ServerConsoleExecutable#setCorrelationID(String)
     * @see UnitActionMessage#BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE
     */
    @Override
    default void baseMessageXML(final Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        UnitActionMessage.super.baseMessageXML(xml);
        setCorrelationID(xml.getAttributeValue(BASE_MESSAGE_CORRELATION_ID_ATTRIBUTE));
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
     * @see UnitActionMessage#setXML(Element)
     */
    @Override
    default void setXML(final Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        initializeParameters();
        UnitActionMessage.super.setXML(xml);
    }

    /**
     * To update the message property by restored from XML Parameter instance
     *
     * @param parameter the value
     * @see UnitActionMessage#updateMessagePropertyBy(Parameter)
     * @see ServerConsoleExecutable#setLinkName(String)
     * @see ServerConsoleExecutable#LINK_NAME_PARAMETER_NAME
     * @see ServerConsoleExecutable#setParameter(Parameter)
     */
    @Override
    default void updateMessagePropertyBy(final Parameter parameter) {
        final String parameterName = parameter.getName();
        if (LINK_NAME_PARAMETER_NAME.equals(parameterName)) {
            setLinkName(parameter.getValue("Invalid LinkName!"));
        } else if (!parameterName.startsWith("@")) {
            setParameter(parameter);
        }
    }
}

