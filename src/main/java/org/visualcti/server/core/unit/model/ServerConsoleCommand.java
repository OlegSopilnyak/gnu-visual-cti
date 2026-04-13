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

import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.XmlAware;

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
public interface ServerConsoleCommand extends ServerConsoleExecutable {
    String COMMAND_SUCCESS_PARAMETER_NAME = "@command-succeed";
    String COMMAND_NEED_RESPONSE_PARAMETER_NAME = "@need_response";
    String COMMAND_ERROR_PARAMETER_NAME = "@error";
    String COMMAND_DONE_PARAMETER_NAME = "@command-is-done";

    /**
     * <accessor>
     * To get the type of action
     *
     * @return the action's type
     * @see MessageType
     */
    @Override
    default MessageType getMessageType() {
        return MessageType.COMMAND;
    }

    /**
     * <accessor>
     * To check is command executed well
     *
     * @return the value
     */
    boolean isSuccess();

    /**
     * <mutator>
     * To set up the success of the command execution
     *
     * @param commandSuccess the value
     * @return reference to the command
     */
    ServerConsoleCommand setSuccess(boolean commandSuccess);

    /**
     * <accessor>
     * To check is command needs response
     *
     * @return the value
     */
    boolean isNeedResponse();

    /**
     * <mutator>
     * To set up is response needed after the command execution
     *
     * @param needResponse the value
     * @return reference to the command
     */
    ServerConsoleCommand setNeedResponse(boolean needResponse);

    /**
     * <accessor>
     * To get the lock of command to provide synchronous command execution
     *
     * @return the lock associated with the command
     * @see Lock
     * @see ServerConsoleCommand#isNeedResponse()
     */
    Lock getLock();

    /**
     * <accessor>
     * To check is command executed
     *
     * @return true if command is executed
     * @see ServerConsoleCommand#assignResponse(ServerConsoleResponse)
     */
    boolean isDone();

    /**
     * <mutator>
     * To set up the value of is command done flag
     *
     * @param done the value
     * @return reference to the command
     */
    ServerConsoleCommand setDone(boolean done);

    /**
     * <action>
     * To assign the response to the command
     *
     * @param response the response to the command
     * @see ServerConsoleCommand#getLock()
     * @see ServerConsoleResponse
     */
    void assignResponse(ServerConsoleResponse response);

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see Parameter
     * @see ServerConsoleCommand#COMMAND_SUCCESS_PARAMETER_NAME
     * @see UnitActionMessage#ROOT_ELEMENT_NAME
     * @see UnitActionMessage#DESCRIPTION_PARAMETER_NAME
     * @see XmlAware#store(OutputStream)
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
     * To update the message property by restored from XML parameter
     *
     * @param propertyParameter the value
     * @see ServerConsoleExecutable#updateMessagePropertyBy(Parameter)
     * @see ServerConsoleCommand#COMMAND_SUCCESS_PARAMETER_NAME
     */
    @Override
    default void updateMessagePropertyBy(final Parameter propertyParameter) {
        if (COMMAND_SUCCESS_PARAMETER_NAME.equals(propertyParameter.getName())) {
            setSuccess(propertyParameter.getValue(false));
        } else if (COMMAND_NEED_RESPONSE_PARAMETER_NAME.equals(propertyParameter.getName())) {
            setNeedResponse(propertyParameter.getValue(false));
        } else if (COMMAND_DONE_PARAMETER_NAME.equals(propertyParameter.getName())) {
            setDone(propertyParameter.getValue(false));
        }
    }
}

