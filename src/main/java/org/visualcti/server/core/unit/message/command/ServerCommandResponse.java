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
package org.visualcti.server.core.unit.message.command;

import java.io.OutputStream;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.XmlAware;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;

/**
 * <prototype>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Server Console Response to the Command</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface ServerCommandResponse extends ServerCommandExecutable {
    String RESPONSE_SUCCESS_PARAMETER_NAME = "@execution-result-succeed";

    /**
     * <accessor>
     * To get the type of action
     *
     * @return the action's type
     * @see MessageType
     */
    @Override
    default MessageType getMessageType() {
        return MessageType.RESPONSE;
    }

    /**
     * <accessor>
     * To get is command executed well?
     *
     * @return the value
     */
    boolean isCommandSuccess();

    /**
     * <mutator>
     * To set up the success of the command execution
     *
     * @param commandSuccess the value
     * @return reference to the response
     */
    ServerCommandResponse setCommandSuccess(boolean commandSuccess);

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see Parameter
     * @see ServerCommandResponse#RESPONSE_SUCCESS_PARAMETER_NAME
     * @see UnitMessage#ROOT_ELEMENT_NAME
     * @see UnitMessage#DESCRIPTION_PARAMETER_NAME
     * @see XmlAware#store(OutputStream)
     */
    @Override
    default Element getXML() {
        return ServerCommandExecutable.super.getXML()
                .addContent(Parameter.of(RESPONSE_SUCCESS_PARAMETER_NAME, isCommandSuccess()).getXML());
    }

    /**
     * To update the message property by restored parameter
     *
     * @param parameter the value
     * @see ServerCommandExecutable#updateMessagePropertyBy(Parameter)
     * @see ServerCommandResponse#RESPONSE_SUCCESS_PARAMETER_NAME
     */
    @Override
    default void updateMessagePropertyBy(final Parameter parameter) {
        if (RESPONSE_SUCCESS_PARAMETER_NAME.equals(parameter.getName())) {
            setCommandSuccess(parameter.getValue(false));
        } else {
            ServerCommandExecutable.super.updateMessagePropertyBy(parameter);
        }
    }

    /**
     * To adjust response as a response to the command
     *
     * @param command command request
     * @return built instance of command response to the command request
     */
    default ServerCommandResponse of(ServerCommandRequest command) {
        this
                .setCorrelationID(command.getCorrelationID()).setLinkName(command.getLinkName())
                .setFamilyType(command.getFamilyType()).setUnitPath(command.getUnitPath())
                .setDescription(command.getDescription());
        return this;
    }
}

