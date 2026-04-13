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
import org.jdom.Element;
import org.visualcti.server.core.unit.model.ServerConsoleCommand;
import org.visualcti.server.core.unit.model.UnitActionEvent;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Smallest atomic(indivisible) part of the Server(server-unit)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface BasicServerUnit {
    /**
     * <accessor>
     * To get local RMI registry to share access to server objects
     *
     * @return RMI registry instance
     * @see Registry
     */
    Registry localRegistry();
//////////////// ACTIONS PART (begin) ///////////////////
    /**
     * <dispatcher>
     * To dispatch event, error, or command response from unit
     * This method will be called inside the unit.
     *
     * @param action action event to dispatch
     * @see UnitActionEvent
     */
    void dispatch(UnitActionEvent action);

    /**
     * <executer>
     * To execute console command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     */
    void execute(ServerConsoleCommand command) throws Exception;
//////////////// ACTIONS PART (end) ///////////////////

/////////// INHERITING PART (begin) ////////////////////
    /**
     * <accessor>
     * To get access to owner of this unit (null for root unit)
     */
    BasicServerUnit getOwner();

    /**
     * <mutator>
     * To set new owner of this unit (null for the root unit)
     */
    void setOwner(BasicServerUnit owner);
/////////// INHERITING PART (end) ////////////////////

/////////////////// CORE PART (begin) //////////////////
    /**
     * <accessor>
     * To get body unit's Icon (gif | jpeg)
     */
    byte[] getIcon();

    /**
     * <accessor>
     * To get Type of unit
     */
    String getType();

    /**
     * <accessor>
     * To get Name of unit
     */
    String getName();

    /**
     * <accessor>
     * To get Path to unit instance in repository
     */
    String getPath();

    /**
     * <accessor>
     * To get Current state of unit
     */
    String getUnitState();
/////////////////// CORE PART (end) //////////////////

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
