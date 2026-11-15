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
package org.visualcti.server.hardware.provider.virtual;


import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.visualcti.core.channel.AbstractTelephonyChannel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.hardware.provider.dialogic.DialogicDevice;
import org.visualcti.server.hardware.provider.dialogic.FactoryImpl;

/**
 * <singleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The factory for the Virtual Telephony devices</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @param <H> the type of device's handle
 * @param <D> the type of device
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see DialogicDevice
 * @see AbstractTelephonyFactory
 */
@SuppressWarnings("unchecked")
public class VirtualTelephonyDevicesFactory<H extends UUID, D extends VirtualTelephonyDevice<H, ?>> extends AbstractTelephonyFactory<H, D> {
    // the instance of the factory (singleton)
    private static VirtualTelephonyDevicesFactory<?, ?> instance = null;

    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return VirtualTelephonyDevicesFactory.class;
    }

    @Override
    public String getUnitBuilderMethodName() {
        return "getInstance";
    }

    /**
     * <builder>
     * To get access to the Dialogic Factory Instance as a singleton
     *
     * @return the singleton of the factory
     */
    public static VirtualTelephonyDevicesFactory<?, ?> getInstance() {
        if (instance == null) {
            synchronized (FactoryImpl.class) {
                if (instance == null) {
                    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                    instance = new VirtualTelephonyDevicesFactory<>(new VirtualTelephonyDeviceProvider<>(scheduler));
                }
            }
        }
        return instance;
    }

    protected VirtualTelephonyDevicesFactory(TelephonyServiceProvider<H> provider) {
        super(provider);
    }

    /**
     * <builder>
     * To build the telephony device's instance
     *
     * @param deviceName the name of device to build
     * @param serviceProvider the provider of the devices factory's services
     * @return built device's instance
     * @throws IOException if it cannot build the device
     * @see Device
     * @see Device.ServiceProvider
     * @see VirtualTelephonyDeviceProvider
     */
    @Override
    public Device<H, ?> buildDevice(
            final String deviceName, final Device.ServiceProvider<H> serviceProvider
    ) throws IOException {
        // trying to build the telephony device's instance
        try {
            return new VirtualTelephonyDevice<>(deviceName, serviceProvider);
        } catch (ClassCastException e) {
            throw new IOException("The service provider must be an instance of VirtualTelephonyDeviceProvider", e);
        }
    }

    /**
     * <builder>
     * To make the channel for the device
     *
     * @param device channel to build for
     * @return built channel
     */
    @Override
    protected TelephonyChannel<D> makeChannelFor(Device<?, ?> device) {
        return new VirtualDeviceChannel((D) device);
    }


    // inner classes
    private class VirtualDeviceChannel extends AbstractTelephonyChannel<D> {
        protected VirtualDeviceChannel(D device) {
            super(device);
        }
    }
}
