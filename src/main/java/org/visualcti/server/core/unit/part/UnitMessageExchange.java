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
package org.visualcti.server.core.unit.part;

import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;

/**
 * Unit Part: The part is in charge of server unit messages exchange
 *
 * @see org.visualcti.server.core.unit.ServerUnit
 */
public interface UnitMessageExchange {
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
    void dispatch(UnitMessage message);

    /**
     * <executer>
     * To execute command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     */
    void execute(ServerCommandRequest command) throws Exception;
}
