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
package org.visualcti.server.unit;

import org.jdom.Element;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;

/**
 * <singleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Basic Implementation: Smallest atomic(indivisible) runnable part of the Application Server(server-unit)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.02
 * @see ServerUnit
 * @see Engine
 */
public abstract class RunnableUnitAdapter extends ServerUnitAdapter implements RunnableServerUnit {
    // The current state of the unit
    protected UnitState unitState = UnitState.PASSIVE;

    /**
     * <accessor>
     * To get Current state of unit (active/passive/broken)
     */
    @Override
    public UnitState currentUnitState() {
        return unitState;
    }

    /**
     * <mutator>
     * To set up the current state of unit (active/passive/broken)
     *
     * @param unitState new value of unit state
     * @see UnitState
     */
    @Override
    public void currentUnitState(UnitState unitState) {
        this.unitState = unitState;
    }

    /**
     * <config>
     * <notify>
     * To notify system about broken unit configuration
     *
     * @param e the cause of malfunction
     * @see ServerUnitAdapter#configure(Element)
     * @see UnitState#BROKEN
     * @see #dispatchExceptionFor(Exception, String)
     */
    @Override
    protected void cannotConfigureBecause(Exception e) {
        // mark unit as broken one
        unitState = UnitState.BROKEN;
        // dispatching malfunction cause to the event-listeners
        dispatchExceptionFor(e, "Cannot restore server unit :" + getName());
    }

    /**
     * <action>
     * To create and dispatch the error message from the unit
     * Just for current version of Mockito
     *
     * @param exception   the cause of the error
     * @param description the description of the error
     * @deprecated
     */
    @Deprecated
    @Override
    public void dispatchExceptionFor(Exception exception, String description) {
        RunnableServerUnit.super.dispatchExceptionFor(exception, description);
    }
}
