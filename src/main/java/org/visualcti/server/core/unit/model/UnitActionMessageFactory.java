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

/**
 * Builder: Unit Action Message Builder (the messages factory)
 */
public interface UnitActionMessageFactory {
    /**
     * <builder>
     * To guild or get the instance of action message
     *
     * @param type the type of the message
     * @param messageClass type(class) of the message
     * @return built or got instance of the unit action message
     * @param <T> concrete type of built message
     * @throws IOException throws if it cannot build the message
     * @see UnitActionMessage
     * @see MessageType
     */
    <T extends UnitActionMessage> T build(MessageType type, Class<T> messageClass) throws IOException;

    /**
     * <builder>
     * To guild or get the instance of action message
     *
     * @param type the type of the message
     * @return built or got instance of the unit action message
     * @param <T> concrete type of built message
     * @throws IOException throws if cannot build the message
     * @see UnitActionMessage
     * @see MessageType
     */
    <T extends UnitActionMessage> T build(MessageType type) throws IOException;
}
