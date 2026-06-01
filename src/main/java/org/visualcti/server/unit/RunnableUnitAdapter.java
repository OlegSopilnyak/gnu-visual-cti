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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.jdom.Element;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;

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
    private Collection<UnitMessage.Listener> listeners = Collections.emptyList();
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
        return listeners;
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
        final List<UnitMessage.Listener> listeners = new LinkedList<>(this.listeners);
        listeners.add(listener);
        // safe updating of listeners list
        this.listeners = Collections.unmodifiableList(listeners);
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
        final List<UnitMessage.Listener> listeners = new LinkedList<>(this.listeners);
        listeners.remove(listener);
        // safe updating of listeners list
        this.listeners = Collections.unmodifiableList(listeners);
    }

    @Deprecated
    @Override
    public boolean isBroken() {
        return RunnableServerUnit.super.isBroken();
    }

    @Deprecated
    @Override
    public boolean isStarted() {
        return RunnableServerUnit.super.isStarted();
    }

    @Deprecated
    @Override
    public boolean isStopped() {
        return RunnableServerUnit.super.isStopped();
    }

    @Deprecated
    @Override
    public void Start() throws IOException {
        RunnableServerUnit.super.Start();
    }

    @Deprecated
    @Override
    public boolean canStartUnit() {
        return RunnableServerUnit.super.canStartUnit();
    }

    @Deprecated
    @Override
    public Stream<RunnableServerUnit> runnableChildren() {
        return RunnableServerUnit.super.runnableChildren();
    }

    @Deprecated
    @Override
    public void startUnitRunnable() {

    }

    @Deprecated
    @Override
    public void startUnitChild(RunnableServerUnit runnable) {
        RunnableServerUnit.super.startUnitChild(runnable);
    }

    @Deprecated
    @Override
    public void Stop() throws IOException {
        RunnableServerUnit.super.Stop();
    }

    @Deprecated
    @Override
    public void stopUnitRunnable() {

    }

    @Deprecated
    @Override
    public void stopUnitChild(RunnableServerUnit runnable) {
        RunnableServerUnit.super.stopUnitChild(runnable);
    }

    @Deprecated
    @Override
    public void dispatchError(Exception exception, String description) {
        RunnableServerUnit.super.dispatchError(exception, description);
    }

    @Deprecated
    @Override
    public void dispatchError(String description) {
        RunnableServerUnit.super.dispatchError(description);
    }

    @Deprecated
    @Override
    public void dispatch(UnitMessage message) {
        RunnableServerUnit.super.dispatch(message);
    }

    @Deprecated
    @Override
    public void processUnitMessage(UnitMessage message) {
        RunnableServerUnit.super.processUnitMessage(message);
    }

    @Deprecated
    @Override
    public void handleUnitMessage(UnitMessage message) {
        RunnableServerUnit.super.handleUnitMessage(message);
    }

    @Deprecated
    @Override
    public void notifyListeners(UnitMessage message) {
        RunnableServerUnit.super.notifyListeners(message);
    }

    @Deprecated
    @Override
    public void notifyListener(UnitMessage.Listener listener, UnitMessage message) {
        RunnableServerUnit.super.notifyListener(listener, message);
    }

    @Deprecated
    @Override
    public void execute(ServerCommandRequest command) throws Exception {
        RunnableServerUnit.super.execute(command);
    }

    /**
     * <config>
     * <notify>
     * To notify system about broken unit configuration
     *
     * @param e the cause of malfunction
     * @see ServerUnitAdapter#configure(Element)
     * @see UnitState#BROKEN
     * @see #dispatchError(Exception, String)
     */
    @Override
    protected void cannotConfigureBecause(Exception e) {
        // mark unit as broken one
        unitState = UnitState.BROKEN;
        // dispatching malfunction cause to the event-listeners
        dispatchError(e, "Cannot restore server unit :" + getName());
    }
}
