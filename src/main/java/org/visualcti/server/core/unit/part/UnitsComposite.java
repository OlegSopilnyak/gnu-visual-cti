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

import java.util.stream.Stream;
import org.visualcti.server.core.unit.ServerUnit;

/**
 * <composite>
 * Unit: Composite part of server unit. The tree of server units
 *
 * @see ServerUnit
 */
public interface UnitsComposite {
    /**
     * <accessor>
     * To get access to the owner of this unit (null for root unit)
     */
    ServerUnit getOwner();

    /**
     * <mutator>
     * To set new owner of this unit (null for the root unit)
     */
    void setOwner(ServerUnit owner);

    /**
     * <mutator>
     * to add child to the composite units tree
     *
     * @param child the unit to add
     * @see ServerUnit
     * @see #addBranch(ServerUnit)
     */
    void  add(ServerUnit child);

    /**
     * <mutator>
     * to add unit to the composite units tree as a branch
     *
     * @param branch the unit to add as a branch
     * @see ServerUnit
     * @see #add(ServerUnit)
     */
    void addBranch(ServerUnit branch);

    /**
     * <mutator>
     * to remove child from the composite units tree
     *
     * @param child the unit to remove
     * @see ServerUnit
     * @see #removeBranch(ServerUnit)
     */
    default void remove(ServerUnit child) {
        if (child.getOwner() == this) {
            child.setOwner(null);
            removeBranch(child);
        }
    }

    /**
     * <mutator>
     * to remove the branch from the composite units tree
     *
     * @param branch the unit to remove from composite tree
     * @see ServerUnit
     * @see #remove(ServerUnit)
     */
    void removeBranch(ServerUnit branch);

    /**
     * <mutator>
     * To remove all units from the composite units tree
     *
     * @see #children()
     * @see #remove(ServerUnit)
     */
    default void removeAll() {
        children().forEach(this::remove);
    }

    /**
     * <accessor>
     * To get access to the composite units tree as Stream
     *
     * @return the stream to the units list managed by composite
     * @see Stream
     * @see ServerUnit
     */
    Stream<ServerUnit> children();

    /**
     * <checker>
     * To check is the unit managing by the composite unit (in units tree), or from parent group
     *
     * @param unit unit to test
     * @return true if group contains the unit
     * @see ServerUnit
     */
    default boolean isChild(ServerUnit unit) {
        return unit.getOwner() == this || unit.getOwner() != null && unit.getOwner().isChild(unit);
    }
}
