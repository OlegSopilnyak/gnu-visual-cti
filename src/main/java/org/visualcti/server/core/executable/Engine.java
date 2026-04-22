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
package org.visualcti.server.core.executable;

import java.io.IOException;
import org.visualcti.server.core.XmlAware;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
import org.visualcti.server.core.unit.part.UnitMessageExchange;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Engine, high level subsystem interface</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface Engine extends XmlAware, UnitMessageExchange {
    /**
     * <enum>
     * engine states
     */
    enum State {
        // started engine state
        IN_SERVICE ((short) 1),
        // stopped engine state
        OUT_OF_SERVICE ((short) 0);
        private final short code;

        State(short code) {
            this.code = code;
        }

        public static State of(short code) {
            for(State state : State.values()) {
                if (state.code == code) {
                    return state;
                }
            }
            return null;
        }

        public short getCode() {
            return code;
        }
    }

    /**
     * <accessor>
     * To get current state value
     *
     * @return  the current state ID of the engine
     */
    short getState();

    /**
     * <accessor>
     * To get current state value
     *
     * @return  the current state of the engine
     */
    default State getEngineState() {
        return State.of(getState());
    }

    /**
     * <mutator>
     * To set up the new state value
     *
     * @param state new state ID of the engine
     */
    void setState(short state);

    /**
     * <mutator>
     * To set up the new state value
     *
     * @param state new state of the engine
     */
    default void setEngineState(State state) {
        setState(state.getCode());
    }

    /**
     * <action>
     * To start the engine
     *
     * @throws IOException if engine can't start
     */
    default void Start() throws IOException {
        if (isStarted()) {
            // engine already started
            return;
        }
        // starting internal parts of the engine
        startingEngine();
        // update engine state
        setEngineState(State.IN_SERVICE);
        // sending engine started event
        final UnitActionEvent event = getMessageFactory().build(MessageType.EVENT);
        dispatch(event.setFamilyType(MessageFamilyType.START));
    }

    /**
     * <action>
     * To stop the engine
     *
     * @throws IOException if engine can't stop
     */
    default void Stop() throws IOException {
        if (isStopped()) {
            // engine already stopped
            return;
        }
        // stopping internal parts of the engine
        stoppingEngine();
        // update engine state
        setEngineState(State.OUT_OF_SERVICE);
        // sending engine stopped event
        final UnitActionEvent event = getMessageFactory().build(MessageType.EVENT);
        dispatch(event.setFamilyType(MessageFamilyType.STOP));
    }

    /**
     * <action>
     * Starting the internal parts of the engine<BR/>
     * Doing the actions according engine implementation start logic
     *
     * @throws IOException if engine can't start
     */
    default void startingEngine() throws IOException {
        throw new UnsupportedOperationException("Not supported yet. Should be override in child.");
    }

    /**
     * <action>
     * Stopping the internal parts of the engine<BR/>
     * Doing the actions according engine implementation stop logic
     *
     * @throws IOException if engine can't stop
     */
    default void stoppingEngine() throws IOException {
        throw new UnsupportedOperationException("Not supported yet. Should be override in child.");
    }

    /**
     * <accessor>
     * To check is Engine is working (in service)
     *
     * @return true if Engine is in service
     * @see State#IN_SERVICE
     */
    default boolean isStarted() {
        return getEngineState() == State.IN_SERVICE;
    }

    /**
     * <accessor>
     * To check is Engine is stopped (out of service)
     *
     * @return true if Engine is out of service
     * @see State#OUT_OF_SERVICE
     */
    default boolean isStopped() {
        return getEngineState() == State.OUT_OF_SERVICE;
    }
}
