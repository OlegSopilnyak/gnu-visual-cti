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

/**
 * Type: Basic properties of the server unit
 */
public interface UnitBasics {
    /**
     * The lifecycle of unit state
     *
     * @see #currentUnitState()
     */
    enum UnitState {
        PASSIVE("passive"),
        ACTIVE("active"),
        BROKEN("broken");
        private final String state;

        UnitState(String state) {
            this.state = state;
        }

        public static UnitState of(Object state) {
            if (state instanceof UnitState) {
                return (UnitState) state;
            } if (state instanceof String) {
                for (UnitState unitState : UnitState.values()) {
                    if (unitState.state.equalsIgnoreCase((String) state))
                        return unitState;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return state;
        }
    }
    /**
     * <accessor>
     * To get body unit's Icon Image (gif | jpeg)
     *
     * @return the value
     */
    byte[] getIcon();

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, services tree, etc.)
     *
     * @return the value
     */
    String getType();

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    String getName();

    /**
     * <accessor>
     * To get Path to unit instance in repository
     *
     * @return the value
     */
    String getPath();

    /**
     * <accessor>
     * To get Current state of unit (active/passive/broken)
     *
     * @return the value
     */
    UnitState currentUnitState();
}
