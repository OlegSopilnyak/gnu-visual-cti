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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.Factory;

/**
 * The Factory of the Devices Adapter: The factory of the channel-devices
 *
 * @param <D> the type of factory's devices
 * @see Factory
 * @see AbstractEventProcessor
 */
public class AbstractFactory<D extends Device<?, ?>> extends AbstractEventProcessor implements Factory<D> {
    // the holder of factory's device channels
    private final AtomicReference<Collection<Channel<?>>> channelsHolder = new AtomicReference<>(Collections.emptyList());

    public AbstractFactory(Executor deviceEventExecutor, DeviceEvent.Provider eventsProvider) {
        this(deviceEventExecutor, eventsProvider, new DefaultDeviceEventListenersHub());
    }

    protected AbstractFactory(Executor deviceEventExecutor, DeviceEvent.Provider eventsProvider, DeviceEvent.Listener.Hub eventListenersHub) {
        super(deviceEventExecutor, eventsProvider, eventListenersHub);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractFactory)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * <accessor>
     * get access to factory's vendor name
     *
     * @return vendor's name
     * @see Factory#getName()
     */
    @Override
    public String getVendor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <accessor>
     * get access to factory's version
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     * Grabbing the factory's devices and making channels for them
     *
     * @throws IOException if something went wrong during the internal parts starting
     * @see #Start()
     * @see #makeChannelFor(Device)
     */
    @Override
    public void startUnitRunnable() throws IOException {
        channelsHolder.getAndSet(devices().map(this::makeChannelFor).collect(Collectors.toSet()));
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Clearing built in startUnitRunnable channels array
     *
     * @throws IOException if something went wrong during the internal parts stopping
     * @see #Stop()
     * @see #startUnitRunnable()
     */
    @Override
    public void stopUnitRunnable() throws IOException {
        channelsHolder.getAndSet(Collections.emptyList());
    }

    /**
     * <builder>
     * To make the channel for device
     *
     * @param device channel to build for
     * @return built channel
     */
    protected Channel<D> makeChannelFor(Device<?, ?> device) {
        throw new UnsupportedOperationException("Not supported here. Please implement it in the descendent.");
    }

    /**
     * <aceessor>
     * to get the array of available factory's channels
     *
     * @return the array of available channels
     * @see Channel
     */
    @Override
    public Collection<Channel<?>> channels() {
        return channelsHolder.get();
    }

    /**
     * Events Listeners Hub: The default implementation hub of the native device's event listeners
     *
     * @see DeviceEvent.Listener.Hub
     */
    public static class DefaultDeviceEventListenersHub extends AbstractEventListenersHub {
    }
}
