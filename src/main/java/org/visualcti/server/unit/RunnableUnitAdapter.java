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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.Element;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.message.UnitMessage;

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
 * @see ServerUnitAdapter
 * @see RunnableServerUnit
 */
public abstract class RunnableUnitAdapter extends ServerUnitAdapter implements RunnableServerUnit {
    // the listeners of unit messages
    private transient Collection<UnitMessage.Listener> listeners = Collections.emptyList();
    // the lock for listeners
    private final Lock listenersLock = new ReentrantLock();
    // The current state of the unit
    protected UnitState unitState = UnitState.PASSIVE;

    /**
     * <accessor>
     * To get Current state of unit (active/passive/broken)
     *
     * @return current state value
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
     * <accessor>
     * To get message-listeners associated with the unit
     *
     * @return message-listeners
     */
    @Override
    public Collection<UnitMessage.Listener> listeners() {
        return safeListenersAction(() -> listeners);
    }

    /**
     * <mutator>
     * To add messages listener to the unit
     *
     * @param listener messages listener to add
     * @see #listeners()
     */
    @Override
    public void addUnitMessageListener(UnitMessage.Listener listener) {
        safeListenersAction(() -> {
            final List<UnitMessage.Listener> tempListeners = new LinkedList<>(this.listeners);
            if (tempListeners.stream().noneMatch(lsnr -> listener == lsnr)) {
                // adding not exists listener
                tempListeners.add(listener);
                // safe updating of listeners list
                this.listeners = Collections.unmodifiableList(tempListeners);
            }
            return listeners;
        });
    }

    /**
     * <mutator>
     * To remove messages listener from the unit
     *
     * @param listener messages listener to remove
     * @see #listeners()
     */
    @Override
    public void removeUnitMessageListener(UnitMessage.Listener listener) {
        safeListenersAction(() -> {
            final List<UnitMessage.Listener> tempListeners = new LinkedList<>(this.listeners);
            if (tempListeners.stream().anyMatch(lsnr -> listener == lsnr)) {
                // removing exists listener
                tempListeners.remove(listener);
                // safe updating of listeners list
                this.listeners = Collections.unmodifiableList(tempListeners);
            }
            return listeners;
        });
    }

    /**
     * <action>
     * to notify unit's message listeners
     *
     * @param message the message to handle by listener
     * @see #handleUnitMessage(UnitMessage)
     * @see UnitMessage
     * @see #listeners()
     * @see #processUnitMessage(UnitMessage)
     * @see #notifyListener(UnitMessage.Listener, UnitMessage)
     */
    @Override
    public void notifyListeners(UnitMessage message) {
        safeListenersAction(() -> {
            RunnableServerUnit.super.notifyListeners(message);
            return listeners;
        });
    }

    /**
     * <config>
     * <notify>
     * To notify system about broken unit configuration
     *
     * @param e the cause of malfunction
     * @see ServerUnitAdapter#configure(Element)
     * @see UnitState#BROKEN
     * @see #dispatchError(Throwable, String)
     */
    @Override
    protected void cannotConfigureBecause(Exception e) {
        // mark unit as broken one
        unitState = UnitState.BROKEN;
        // dispatching malfunction cause to the event-listeners
        dispatchError(e, "Cannot restore server unit :" + getName());
    }

    // private methods
    // to do action with protected messages listeners container
    private Collection<UnitMessage.Listener> safeListenersAction(final Callable<Collection<UnitMessage.Listener>> action) {
        listenersLock.lock();
        try {
            return action.call();
        } catch (Exception e) {
            dispatchError(e, "Error while calling messages listeners action!");
            return Collections.emptySet();
        } finally {
            listenersLock.unlock();
        }
    }
}
