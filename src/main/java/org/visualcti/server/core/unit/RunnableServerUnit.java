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

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.part.UnitMessageExchange;

/**
 * <singleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Smallest atomic(indivisible) runnable part of the Application Server(server-unit)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.02
 * @see ServerUnit
 * @see Engine
 */
public interface RunnableServerUnit extends ServerUnit, Engine {
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
            return state instanceof UnitState ? (UnitState) state :
                    Arrays.stream(UnitState.values())
                    .filter(unitState -> unitState.state.equalsIgnoreCase((String) state))
                    .findFirst().orElse(null);
        }

        @Override
        public String toString() {
            return state;
        }
    }

    /**
     * <accessor>
     * To get Current state of unit (active/passive/broken)
     *
     * @return the value
     */
    UnitState currentUnitState();

    /**
     * <accessor>
     * To get current state value
     *
     * @return the current state ID of the engine
     * @see Engine#getState()
     * @see #currentUnitState()
     */
    @Override
    default short getState() {
        switch (currentUnitState()) {
            case ACTIVE:
                return State.IN_SERVICE.getCode();
            case PASSIVE:
            case BROKEN:
            default:
                return State.OUT_OF_SERVICE.getCode();
        }
    }

    /**
     * <mutator>
     * To set up the new state value
     *
     * @param state new state ID of the engine
     * @see Engine#setState(short)
     */
    @Override
    default void setState(short state) {
        // doing noting in this unit
    }

    /**
     * <accessor>
     * To check is unit had the problems during building and can't be started or stopped
     *
     * @return true if runnable unit is broken
     * @see #currentUnitState()
     * @see UnitState#BROKEN
     */
    default boolean isBroken() {
        return currentUnitState() == UnitState.BROKEN;
    }

    /**
     * <accessor>
     * To check is Engine is working (in service)
     *
     * @return true if runnable unit is started
     * @see #currentUnitState()
     * @see UnitState#ACTIVE
     */
    @Override
    default boolean isStarted() {
        return currentUnitState() == UnitState.ACTIVE;
    }

    /**
     * <accessor>
     * To check is Engine is stopped (out of service)
     *
     * @return true if runnable unit is stopped
     * @see #currentUnitState()
     * @see UnitState#PASSIVE
     */
    @Override
    default boolean isStopped() {
        return currentUnitState() == UnitState.PASSIVE;
    }

    /**
     * <mutator>
     * To set up the current state of unit (active/passive/broken)
     *
     * @see UnitState
     */
    void currentUnitState(UnitState unitState);

    /**
     * <action>
     * To start the runnable unit
     *
     * @throws IOException if the unit can't be started
     * @see #isBroken()
     * @see #isStarted()
     * @see #startUnitRunnable()
     * @see #startUnitChild(RunnableServerUnit)
     * @see #doForUnitChildren(ServerUnit, Consumer)
     * @see #currentUnitState(UnitState)
     * @see UnitMessageFactory#buildFor(ServerUnit, MessageType, MessageFamilyType, String)
     * @see UnitMessageExchange#dispatch(UnitMessage)
     */
    @Override
    default void Start() throws IOException {
        if (isBroken() || isStarted()) {
            // unit is broken or already started
            return;
        }
        // starting internal parts of the runnable unit
        startUnitRunnable();
        // starting runnable children of the unit
        doForUnitChildren(this, this::startUnitChild);
        // setting up new runnable unit state
        currentUnitState(UnitState.ACTIVE);
        // dispatch unit started message
        final String messageDescription = "Started server unit :" + getPath();
        dispatch(getMessageFactory().buildFor(this, MessageType.EVENT, MessageFamilyType.START, messageDescription));
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     *
     * @throws IOException if them can't be started
     */
    default void startUnitRunnable() throws IOException {
        // do nothing by default for internal running
    }

    /**
     * <action>
     * To start the runnable child of the unit
     *
     * @param runnable the child of the unit to start
     */
    default void startUnitChild(RunnableServerUnit runnable) {
        try {
            runnable.Start();
        } catch (IOException e) {
            runnable.currentUnitState(UnitState.BROKEN);
            dispatchExceptionFor(e, "Cannot start server unit :" + runnable.getPath());
        }
    }

    /**
     * <action>
     * To stop the runnable unit
     *
     * @throws IOException if the unit can't be started
     * @see #isBroken()
     * @see #isStopped()
     * @see #stopUnitRunnable()
     * @see #doForUnitChildren(ServerUnit, Consumer)
     * @see #currentUnitState(UnitState)
     * @see UnitMessageFactory#buildFor(ServerUnit, MessageType, MessageFamilyType, String)
     * @see UnitMessageExchange#dispatch(UnitMessage)
     */
    @Override
    default void Stop() throws IOException {
        if (isBroken() || isStopped()) {
            // unit is broken or already stopped
            return;
        }
        // stopping internal parts of the runnable unit
        stopUnitRunnable();
        // stopping runnable children of the unit
        doForUnitChildren(this, this::stopUnitChild);
        // setting up new runnable unit state
        currentUnitState(UnitState.PASSIVE);
        // dispatch unit started message
        final String messageDescription = "Stopped server unit :" + getPath();
        dispatch(getMessageFactory().buildFor(this, MessageType.EVENT, MessageFamilyType.STOP, messageDescription));
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     *
     * @throws IOException if them can't be stopped
     */
    default void stopUnitRunnable() throws IOException {
        // do nothing by default for internal running
    }

    /**
     * <action>
     * To stop the runnable child of the unit
     *
     * @param runnable the child of the unit to stop
     */
    default void stopUnitChild(RunnableServerUnit runnable) {
        try {
            runnable.Stop();
        } catch (IOException e) {
            runnable.currentUnitState(UnitState.BROKEN);
            dispatchExceptionFor(e, "Cannot stop server unit :" + runnable.getPath());
        }
    }

    /**
     * <action>
     * To create and dispatch the error message from the unit
     *
     * @param exception       the cause of the error
     * @param description the description of the error
     */
    default void dispatchExceptionFor(Exception exception, String description) {
        try {
            final UnitActionError  error =
                    getMessageFactory().buildFor(this, MessageType.ERROR, MessageFamilyType.ERROR, description);
            dispatch(error.setNestedException(exception));
        } catch (IOException ex) {
            // doing nothing, server unit is already in broken state
        }
    }

    /**
     * <action>
     * To do action for all children of the unit
     *
     * @param unit   the unit to do action for it's children
     * @param action action to apply for each unit's child (if it's runnable)
     */
    static void doForUnitChildren(final ServerUnit unit, final Consumer<? super RunnableServerUnit> action) {
        unit.children().filter(RunnableServerUnit.class::isInstance).map(RunnableServerUnit.class::cast).forEach(action);
    }
}
