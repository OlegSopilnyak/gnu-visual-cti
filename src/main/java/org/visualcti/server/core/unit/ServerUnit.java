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
package org.visualcti.server.core.unit;

import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Optional;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.util.Tools;

/**
 * <singleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Smallest atomic(indivisible) part of the Application Server(server-unit)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface ServerUnit extends UnitBasics {
    /**
     * <accessor>
     * To get local RMI registry to share access to server objects
     *
     * @return RMI registry instance
     * @see Registry
     */
    default Registry localRegistry() {
        try {
            return getOwner().localRegistry();
        } catch (NullPointerException npe) {
            return null;
        }
    }
//////////////// ACTIONS PART (begin) ///////////////////
    /**
     * <accessor>
     * To get reference to messages factory
     *
     * @return not null reference to the factory
     */
    UnitMessageFactory getMessageFactory();

    /**
     * <dispatcher>
     * To dispatch event, error, or command response from the unit
     * This method will be called inside the activity of unit.
     * Should override for root unit
     *
     * @param message action message to dispatch
     * @see UnitMessage
     */
    default void dispatch(UnitMessage message) {
        try {
            getOwner().dispatch(message);
        } catch (NullPointerException npe) {
            Tools.error("Warning! Lost message in " + getPath() + " msg:" + message);
        }
    }

    /**
     * <executer>
     * To execute command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     */
    default void execute(ServerCommandRequest command) throws Exception {
        if (MessageFamilyType.GET == command.getFamilyType()  && command.isNeedResponse()) {
            // getting parameter with name "target" from the executing command
            final Optional<Parameter> target = command.getParameter("target", Parameter.INPUT_DIRECTION);
            if (target.isPresent() && "meta".equals(target.get().getValue())) {
                // command asks to get the metadata of the unit
                final ServerCommandResponse response = getMessageFactory().build(MessageType.RESPONSE);
                UnitMetaData.of(this).transferTo(response);
                // dispatching the response to the command request
                dispatch(response.setCommandSuccess(true));
                return;
            }
        }
        throw new UnknownCommandException(command.getFamilyType() + " isn't supported!");
    }
//////////////// ACTIONS PART (end) ///////////////////

/////////// SERVER UNIT HIERARCHY PART (begin) ////////////////////
    /**
     * <accessor>
     * To get access to owner of this unit (null for root unit)
     */
    ServerUnit getOwner();

    /**
     * <mutator>
     * To set new owner of this unit (null for the root unit)
     */
    void setOwner(ServerUnit owner);
/////////// SERVER UNIT HIERARCHY PART (end) ////////////////////

///////////// PROPERTIES PART (begin) //////////////
    /**
     * <config>
     * To configure the unit, using information from Element
     */
    void configure(Element configuration);

    /**
     * <accessor>
     * get serverUnit properties
     * may use for visual editing in GUI
     */
    Map getProperties();

    /**
     * <mutator>
     * assign properties set to serverUnit
     * Properties may be changed in GUI
     */
    void setProperties(Map properties);
///////////// PROPERTIES PART (end) //////////////
}
