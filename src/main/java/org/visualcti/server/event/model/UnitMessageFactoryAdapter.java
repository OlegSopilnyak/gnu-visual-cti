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
package org.visualcti.server.event.model;

import java.io.IOException;
import org.visualcti.server.core.unit.model.MessageType;
import org.visualcti.server.core.unit.model.UnitActionMessage;
import org.visualcti.server.core.unit.model.UnitActionMessageFactory;

/**
 * Builder Adapter: Unit Action Message Builder (the messages factory)
 * for multi-inheritance feature
 *
 * @see UnitActionMessageFactory
 */
public interface UnitMessageFactoryAdapter extends UnitActionMessageFactory {
    /**
     * <builder>
     * To guild or get the instance of action message
     *
     * @param type         the type of the message
     * @param messageClass type(class) of the message
     * @param <T>          concrete type of built message
     * @return built or got instance of the unit action message
     * @throws IOException throws if it cannot build the message
     * @see UnitActionMessage
     * @see MessageType
     */
    @SuppressWarnings("unchecked")
    @Override
    default <T extends UnitActionMessage> T build(MessageType type, Class<T> messageClass) throws IOException {
        if (type != null) {
            switch (type) {
                case ERROR:
                    return (T) checked(new UnitError(), messageClass);
                case EVENT:
                    return (T) checked(new UnitEvent(), messageClass);
                case COMMAND:
                    return (T) checked(new ConsoleRequest(), messageClass);
                case RESPONSE:
                    return (T) checked(new ConsoleResponse(), messageClass);
                case UNKNOWN:
                    throw new IOException("Unknown message type");
            }
        }
        throw new IOException("Message type is not supported");
    }

    /**
     * <builder>
     * To guild or get the instance of action message
     *
     * @param type the type of the message
     * @param <T>  concrete type of built message
     * @return built or got instance of the unit action message
     * @throws IOException throws if it cannot build the message
     * @see UnitActionMessage
     * @see MessageType
     */
    @SuppressWarnings("unchecked")
    @Override
    default <T extends UnitActionMessage> T build(MessageType type) throws IOException {
        if (type != null) {
            switch (type) {
                case ERROR:
                    return (T) new UnitError();
                case EVENT:
                    return (T) new UnitEvent();
                case COMMAND:
                    return (T) new ConsoleRequest();
                case RESPONSE:
                    return (T) new ConsoleResponse();
                case UNKNOWN:
                    throw new IOException("Unknown message type");
            }
        }
        throw new IOException("Message type is not supported");
    }

    default UnitActionMessage checked(UnitActionMessage message, Class<?> messageClass) throws IOException {
        if (!messageClass.isInstance(message)) {
            throw new IOException(message.getClass().getSimpleName() + " is not an instance of " + messageClass.getSimpleName());
        } else {
            return message;
        }
    }
}
