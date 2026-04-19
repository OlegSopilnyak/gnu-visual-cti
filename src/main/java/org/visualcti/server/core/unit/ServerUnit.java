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
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.server.core.unit.part.UnitBasics;
import org.visualcti.server.core.unit.part.UnitMessageExchange;
import org.visualcti.server.core.unit.part.UnitsComposite;
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
 * @version 3.02
 * @see UnitBasics
 * @see UnitsComposite
 * @see UnitMessageExchange
 */
public interface ServerUnit extends UnitMessageExchange, UnitsComposite, UnitBasics {
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
     * <dispatcher>
     * To dispatch event, error, or command response from the unit
     * This method will be called inside the activity of unit.
     * Should override for root unit
     *
     * @param message action message to dispatch
     * @see UnitMessage
     * @see #getOwner()
     */
    @Override
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
     * @see ServerCommandRequest
     * @see MessageFamilyType#GET
     * @see Parameter
     * @see Parameter#INPUT_DIRECTION
     * @see org.visualcti.server.core.unit.message.UnitMessageFactory
     * @see #getMessageFactory()
     * @see org.visualcti.server.core.unit.message.UnitMessageFactory#build(MessageType)
     * @see MessageType#RESPONSE
     * @see ServerCommandResponse
     * @see UnitMetaData#transferTo(ServerCommandResponse)
     * @see #dispatch(UnitMessage)
     */
    @Override
    default void execute(ServerCommandRequest command) throws Exception {
        if (MessageFamilyType.GET == command.getFamilyType() && command.isNeedResponse()) {
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
     * <mutator>
     * to add child to the composite units tree
     *
     * @param child the unit to add
     * @see ServerUnit
     * @see #addBranch(ServerUnit)
     */
    @Override
    default void add(ServerUnit child) {
        if (child.getOwner() != this) {
            child.setOwner(this);
            addBranch(child);
        }
    }
/////////// SERVER UNIT HIERARCHY PART (end) ////////////////////

///////////// PROPERTIES PART (begin) //////////////
    /**
     * <config>
     * To configure the unit, using information from XML Element
     *
     * @param configuration new configuration of the unit
     * @see Element
     */
    void configure(Element configuration);

    /**
     * <accessor>
     * To get ServerUnit instance properties
     * may use for visual editing in GUI
     *
     * @return server unit properties
     */
    Map<String, Object> getProperties();

    /**
     * <mutator>
     * To assign properties to ServerUnit instance
     * Properties may be changed in GUI
     *
     * @param properties server unit properties
     */
    void setProperties(Map<String, Object> properties);
///////////// PROPERTIES PART (end) //////////////
}
