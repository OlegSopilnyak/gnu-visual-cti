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
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;

/**
 * Builder: Unit Action Message Builder (the messages factory)
 */
public interface UnitMessageFactory {
    /**
     * <builder>
     * To build or get the instance of action message
     *
     * @param type         the type of the message
     * @param messageClass type(class) of the message
     * @param <T>          concrete type of built message
     * @return built or got instance of the unit action message
     * @throws IOException throws if it cannot build the message
     * @see UnitMessage
     * @see MessageType
     */
    <T extends UnitMessage> T build(MessageType type, Class<T> messageClass) throws IOException;

    /**
     * <builder>
     * To build or get the instance of action message
     *
     * @param type the type of the message
     * @param <T>  concrete type of built message
     * @return built or got instance of the unit action message
     * @throws IOException if it cannot build the message
     * @see UnitMessage
     * @see MessageType
     */
    <T extends UnitMessage> T build(MessageType type) throws IOException;

    /**
     * <builder>
     * To build or get the instance of an action message
     *
     * @param unit the sourcer of the message
     * @param type the type of the message
     * @param familyType the concrete type of message family
     * @param description the description of built message
     * @return built or got instance of the unit action message
     * @param <T>  concrete type of built message
     * @throws IOException if it cannot build the message
     * @see ServerUnit
     * @see UnitMessage
     * @see MessageType
     * @see MessageFamilyType
     */
    <T extends UnitMessage> T buildFor(ServerUnit unit, MessageType type, MessageFamilyType familyType, String description) throws IOException ;

    /**
     * <builder>
     * To build the instance of command's response to the request
     *
     * @param request the request the response is building for
     * @return instance of response for the request
     * @throws IOException if it cannot build the message
     * @see ServerCommandResponse
     * @see ServerCommandRequest
     */
    ServerCommandResponse responseTo(ServerCommandRequest request) throws IOException;
}
