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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.Parameter;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Server Console Command</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface ServerConsoleRequest extends ServerConsoleExecutable {
    String COMMAND_SUCCESS_PARAMETER_NAME = "@request-succeed";
    String COMMAND_NEED_RESPONSE_PARAMETER_NAME = "@need_response";
    String COMMAND_ERROR_PARAMETER_NAME = "@error";
    String COMMAND_DONE_PARAMETER_NAME = "@request-is-done";

    /**
     * <accessor>
     * To get the type of the message
     *
     * @return the message's type
     * @see MessageType
     */
    @Override
    default MessageType getMessageType() {
        return MessageType.COMMAND;
    }

    /**
     * <accessor>
     * To check is request executed well
     *
     * @return the value
     */
    boolean isSuccess();

    /**
     * <mutator>
     * To set up the success of the request execution
     *
     * @param requestSuccess the value
     * @return reference to the request
     */
    ServerConsoleRequest setSuccess(boolean requestSuccess);

    /**
     * <accessor>
     * To check is request needs response
     *
     * @return the value
     */
    boolean isNeedResponse();

    /**
     * <mutator>
     * To set up is response needed after the request execution
     *
     * @param needResponse the value
     * @return reference to the request
     */
    ServerConsoleRequest setNeedResponse(boolean needResponse);

    /**
     * <accessor>
     * To get the lock of request to provide synchronous request execution
     *
     * @return the lock associated with the request
     * @see Lock
     * @see ServerConsoleRequest#isNeedResponse()
     */
    Lock getLock();

    /**
     * <mutator>
     * To set up the lock of request
     *
     * @see Lock
     * @see #setXML(Element)
     */
    void setLock(Lock lock);

    /**
     * <accessor>
     * To check is request executed
     *
     * @return true if request is executed
     * @see ServerConsoleRequest#assignResponse(ServerConsoleResponse)
     */
    boolean isDone();

    /**
     * <mutator>
     * To set up the value of is request done flag
     *
     * @param done the value
     * @return reference to the request
     */
    ServerConsoleRequest setDone(boolean done);

    /**
     * <action>
     * To assign the response to the request
     *
     * @param response the response to the request
     * @see ServerConsoleRequest#getLock()
     * @see ServerConsoleResponse
     */
    default void assignResponse(ServerConsoleResponse response) {
        final boolean requestSuccess = response.isCommandSuccess();
        // analyzing response according request's response need
        if (!isNeedResponse()) {
            // no needs to response processing
            setSuccess(requestSuccess);
        } else {
            // processing the response
            if (requestSuccess) {
                // copying parameters from response to request as outputs
                response.getParameters().forEach(parameter -> setParameter(parameter.output()));
            } else {
                // prepare the response's error output
                final Parameter error = new Parameter(COMMAND_ERROR_PARAMETER_NAME, response.getDescription());
                setParameter(error.output());
            }
            // end of response's processing
            setSuccess(requestSuccess).setDone(true);
            // To free the request's lock ;)
            getLock().unlock();
        }
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see Parameter
     * @see ServerConsoleRequest#COMMAND_SUCCESS_PARAMETER_NAME
     * @see UnitActionMessage#ROOT_ELEMENT_NAME
     * @see UnitActionMessage#DESCRIPTION_PARAMETER_NAME
     * @see ServerConsoleExecutable#getXML()
     */
    @Override
    default Element getXML() {
        final Parameter isSuccessParameter = new Parameter(COMMAND_SUCCESS_PARAMETER_NAME, isSuccess());
        final Parameter isNeedResponseParameter = new Parameter(COMMAND_NEED_RESPONSE_PARAMETER_NAME, isNeedResponse());
        final Parameter isDoneParameter = new Parameter(COMMAND_DONE_PARAMETER_NAME, isDone());
        return ServerConsoleExecutable.super.getXML()
                .addContent(isSuccessParameter.getXML())
                .addContent(isNeedResponseParameter.getXML())
                .addContent(isDoneParameter.getXML());
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
     * @see ServerConsoleExecutable#setXML(Element)
     */
    @Override
    default void setXML(final Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        setLock(new ReentrantLock());
        ServerConsoleExecutable.super.setXML(xml);
    }

    /**
     * To update the message property by restored from XML parameter
     *
     * @param propertyParameter the value
     * @see ServerConsoleExecutable#updateMessagePropertyBy(Parameter)
     * @see ServerConsoleRequest#COMMAND_SUCCESS_PARAMETER_NAME
     */
    @Override
    default void updateMessagePropertyBy(final Parameter propertyParameter) {
        if (COMMAND_SUCCESS_PARAMETER_NAME.equals(propertyParameter.getName())) {
            setSuccess(propertyParameter.getValue(false));
        } else if (COMMAND_NEED_RESPONSE_PARAMETER_NAME.equals(propertyParameter.getName())) {
            setNeedResponse(propertyParameter.getValue(false));
        } else if (COMMAND_DONE_PARAMETER_NAME.equals(propertyParameter.getName())) {
            setDone(propertyParameter.getValue(false));
        } else {
            ServerConsoleExecutable.super.updateMessagePropertyBy(propertyParameter);
        }
    }
}

