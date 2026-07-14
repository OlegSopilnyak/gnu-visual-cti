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
package org.visualcti.core.channel.device.adapter;


import static org.visualcti.core.channel.device.Device.State.IDLE;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceStateValue;

/**
 * Abstract Device's Context: The context of device's activity
 *
 * @param <H> the type of the device's low-level operations handle
 * @see Device.Session
 * @see Device#findSessionByHandle(Object)
 * @see Device#createSessionFor(Object)
 */
public class AbstractDeviceSession<H> implements Device.Session<H> {
    // the device-owner reference
    protected final Device<H, ?> device;
    // the holder of device's handle
    protected final AtomicReference<H> deviceHandle = new AtomicReference<>();
    // the flag is device resource opened
    private final AtomicBoolean opened = new AtomicBoolean();
    // the flag is context terminated
    private final AtomicBoolean terminated = new AtomicBoolean();
    // the flag is context connected with external resource
    private final AtomicBoolean alive = new AtomicBoolean();
    // the current status of the device's context
    protected final AtomicReference<DeviceStateValue> sessionState = new AtomicReference<>(IDLE);

    /**
     * <builder>
     * Constructor of the device activities context
     *
     * @param deviceOwner  the reference to the device-owner of the context
     * @param deviceHandle the handle to the opened device's resource
     * @see Device.ServiceProvider#openResource(String)
     */
    protected AbstractDeviceSession(Device<H, ?> deviceOwner, H deviceHandle) {
        this.device = deviceOwner;
        this.deviceHandle.getAndSet(deviceHandle);
        opened.getAndSet(true);
        terminated.getAndSet(false);
        alive.getAndSet(false);
    }

    /**
     * <accessor>
     * To get access to device-owner of the context
     *
     * @return the device-owner reference
     */
    @Override
    public Device<H, ?> getDevice() {
        return device;
    }

    /**
     * <accessor>
     * To get access to opened device's internal handle
     *
     * @return device's handle
     * @see Device.ServiceProvider#openResource(String)
     */
    @Override
    public H getDeviceHandle() {
        return deviceHandle.get();
    }

    /**
     * <checker>
     * Whether context's device handle valid or not
     *
     * @return true if device handle value is valid
     * @see #deviceHandle
     * @see #isOpened()
     */
    protected boolean isValidDeviceHandle() {
        return true;
    }

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     */
    @Override
    public boolean isOpened() {
        return isValidDeviceHandle() && opened.get();
    }

    /**
     * <accessor>
     * To get access to device's internal name
     *
     * @return device's name
     * @see Device#getName()
     */
    @Override
    public String getDeviceName() {
        return device.getName();
    }

    /**
     * <accessor>
     * To get access to context's termination flag
     *
     * @return the flag's value
     */
    @Override
    public boolean isTerminated() {
        return terminated.get();
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
        terminated.getAndSet(true);
    }

    /**
     * <accessor>
     * To get access to the state of the channel-device context
     *
     * @return value of device state
     * @see DeviceStateValue#getValue()
     * @see Device.State
     */
    @Override
    public DeviceStateValue getState() {
        return sessionState.get();
    }

    /**
     * <mutator>
     * To set up the new state value of the channel-device context
     *
     * @param state new value of device state
     * @see DeviceStateValue#getValue()
     */
    @Override
    public void setState(DeviceStateValue state) {
        if (state != sessionState.get()) {
            // updating state value
            sessionState.getAndSet(state);
            // sending event (unit state is changed)
            device.dispatchEvent(state.getValue());
            // notifying about device state changed
            device.stateChangedFor(this);
        }
    }

    /**
     * <accssor>
     * To check up the condition of the channel-device context
     *
     * @return true if the device context is in service (connected)
     */
    @Override
    public boolean isAlive() {
        return alive.get();
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        opened.getAndSet(false);
        alive.getAndSet(false);
        terminated.getAndSet(false);
    }

    /**
     * <action>
     * Whether the given event is accepted by this listener.
     *
     * @param event the fired Event
     * @return true if the event accepted for the processing
     */
    @Override
    public boolean accept(DeviceEvent event) {
        return true;
    }

    /**
     * <action>
     * Whether the given event is accepted by the context
     *
     * @param event the fired Event
     * @return true if the event accepted for the processing
     */
    protected boolean acceptEvent(DeviceEvent event) {
        return true;
    }
}
