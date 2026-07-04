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
package org.visualcti.core.channel.device;


import static org.visualcti.core.channel.device.Device.State.CLOSED;
import static org.visualcti.core.channel.device.Device.State.ERROR;
import static org.visualcti.core.channel.device.Device.State.IDLE;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * Abstract Device of the Channel: The root device through which task communicate with external world
 *
 * @see ServerUnit
 * @see Device
 * @see Factory
 */
public abstract class AbstractDevice<F extends Factory<?>> extends ServerUnitAdapter implements Device<F> {
    // the current status of the device
    protected final AtomicReference<DeviceStateValue> currentState = new AtomicReference<>(CLOSED);

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AbstractDevice)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * <action>
     * Opening and activation of the channel-device.
     *
     * @throws IOException if channel device cannot be opened or activated
     * @see #currentState
     * @see Device.State#IDLE
     */
    @Override
    public void open() throws IOException {
        currentState.getAndSet(IDLE);
    }

    /**
     * <action>
     * Closing the server unit, releasing attached resources and restoring original unitPath
     *
     * @throws IOException if an I/O error occurs
     * @see #currentState
     * @see Device.State#CLOSED
     */
    @Override
    public void close() throws IOException {
        currentState.getAndSet(CLOSED);
    }

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     * @see #currentState
     * @see Device.State#ERROR
     * @see Device.State#CLOSED
     */
    @Override
    public boolean isOpened() {
        // device isn't in error state or already closed
        return currentState.get() != ERROR && currentState.get() != CLOSED;
    }

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @throws IOException If the device can't terminate current operation
     */
    @Override
    public void terminate() throws IOException {
        if (isOpened()) {
            close();
        }
    }

    /**
     * <accessor>
     * To get access to the status of the channel-device
     *
     * @return status' value
     */
    @Override
    public DeviceStateValue getState() {
        return currentState.get();
    }

    /**
     * <mutator>
     * To set up the new state value of the channel-device
     *
     * @param state new value of device state
     * @see DeviceStateValue#getValue()
     */
    @Override
    public void setState(DeviceStateValue state) {
        this.currentState.getAndSet(state);
        // sending event (unit state is changed)
        dispatchEvent(state.getValue());
        // notifying about device state changed
        stateChanged(state);
    }

    /**
     * <notify>
     * To notify, about device state changed
     *
     * @param state the new value of device state
     */
    protected void stateChanged(DeviceStateValue state) {
        // doing nothing by default
    }

    /**
     * <repair>
     * Try to repair device after malfunction
     *
     * @return true if repaired well
     */
    @Override
    public boolean repair() {
        return false;
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
