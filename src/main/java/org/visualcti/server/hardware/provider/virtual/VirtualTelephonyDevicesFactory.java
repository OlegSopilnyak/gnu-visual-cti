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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.AbstractTelephonyChannel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.adapter.AbstractGeneralFactory;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.hardware.provider.dialogic.DialogicDevice;
import org.visualcti.server.hardware.provider.dialogic.FactoryImpl;
import org.visualcti.server.unit.ServerUnitAdapter;

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
public class VirtualTelephonyDevicesFactory<H extends UUID, D extends TelephonyDevice<H, ?>>
        extends AbstractTelephonyFactory<H, D> {
    // the value of type of the factory as a server unit
    public static final String DEVICES_FACTORY_UNIT_TYPE = "[virtual:telephony-channel-devices-board]";
    // devices count configuration parameter
    private static final String DEVICE_COUNT = "device count";
    // device names prefix configuration parameter
    private static final String DEVICE_PREFIX = "device prefix";
    // the instance of the factory (singleton)
    private static VirtualTelephonyDevicesFactory<?, ?> instance = null;

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     * @see #DEVICES_FACTORY_UNIT_TYPE
     */
    @Override
    public String getType() {
        return DEVICES_FACTORY_UNIT_TYPE;
    }

    /**
     * <accessor>
     * To get main class of the unit
     *
     * @return the type of the unit value
     * @see VirtualTelephonyDevicesFactory
     */
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

    /**
     * <builder>
     * To build the telephony device's instance
     *
     * @param deviceName      the name of device to build
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
            return new TelephonyDevice<>(deviceName, serviceProvider);
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
        return new DeviceChannel((D) device);
    }

    /**
     * <builder>
     * To prepare the network xml-section of the telephony device
     *
     * @return prepared telephony device network configuration-xml-section
     * @see super#defaultNetworkXml()
     * @see Element#addContent(Element)
     */
    @Override
    protected Element defaultNetworkXml() {
        return super.defaultNetworkXml().addContent(
                ConfigurationParameter.of(FaxMachineEngine.Parameter.FAX_ALLOWED.value(), true).getXml()
        );
    }

    /**
     * <applier>
     * To apply not processed configuration parameter of the devices factory
     *
     * @param parameter the factory parameter to apply
     * @see ConfigurationParameter
     * @see AbstractGeneralFactory#setXML(Element)
     * @see ServerUnitAdapter#settingUpMainPart(Element)
     * @see ServerUnitAdapter#processParameter(ConfigurationParameter)
     * @see AbstractTelephonyFactory#applyFactoryParameter(ConfigurationParameter)
     */
    @Override
    protected void applyFactoryParameter(final ConfigurationParameter parameter) {
        switch (parameter.getName()) {
            case DEVICE_COUNT:
                // the configuration parameter of the virtual devices count
                getDeviceProvider().ifPresent(provider ->
                        provider.setDevicesCount(parameter.<Number>getValue().intValue())
                );
                break;
            case DEVICE_PREFIX:
                // the configuration parameter of the virtual device name prefix
                getDeviceProvider().ifPresent(provider ->
                        provider.setDeviceNamePrefix(parameter.getValue())
                );
                break;
            default:
                break;
        }
    }

    /**
     * <converter>
     * To update the factory-provider's fields from XML
     *
     * @param devicesFactoryXml possible factory-unit's XML
     * @see super#beforeDevicesAdding(Element)
     * @see #applyFactoryParameter(ConfigurationParameter)
     */
    @Override
    protected void beforeDevicesAdding(final Element devicesFactoryXml) {
        // the container for parsed from XML parameter
        final List<Element> parameters = devicesFactoryXml.getChildren(ConfigurationParameter.ELEMENT);
        // processing the parameters of the root unit XML elements
        parameters.stream()
                .map(ConfigurationParameter::of).filter(Objects::nonNull)
                .forEach(this::applyFactoryParameter);
    }

    @Deprecated
    @Override
    protected void applyUnitParameter(ConfigurationParameter parameter) {
        super.applyUnitParameter(parameter);
    }

    /**
     * <converter>
     * To represent the parameters of the unit as an XML element
     * Here managed the icon of the server unit
     *
     * @param rootElement building from unit XML Element
     * @see AbstractTelephonyFactory#getXML()
     * @see AbstractTelephonyFactory#prepareUnitParametersXML(Element)
     */
    @Override
    protected void prepareUnitParametersXML(final Element rootElement) {
        // doing preparation by default
        super.prepareUnitParametersXML(rootElement);
        // preparing add to root configured parameter's xml-element consumer
        final Consumer<ConfigurationParameter> saving = parameter -> rootElement.addContent(parameter.getXml());
        // representing virtual devices factory parameters
        getDeviceProvider().ifPresent(provider -> {
            // saving the virtual devices count
            saving.accept(ConfigurationParameter.of(DEVICE_COUNT, provider.getDevicesCount()));
            // saving the virtual device name prefix
            saving.accept(ConfigurationParameter.of(DEVICE_PREFIX, provider.getDeviceNamePrefix()));
        });
    }

    /**
     * <accessor>
     * To get virtual telephony device provider or empty
     *
     * @return device provider
     * @see VirtualTelephonyDeviceProvider
     * @see #getProvider()
     * @see Optional
     */
    protected Optional<VirtualTelephonyDeviceProvider<H>> getDeviceProvider() {
        return Optional.ofNullable(getProvider())
                .filter(VirtualTelephonyDeviceProvider.class::isInstance)
                .map(VirtualTelephonyDeviceProvider.class::cast);
    }

    /**
     * <accessor>
     * To get the description of the unit
     *
     * @see ServerUnitAdapter#buildUnitRootElement()
     */
    @Override
    protected String getUnitDescription() {
        return " The implementation of Virtual telephony devices factory features ";
    }

    /// private methods
    ///
    // private constructor
    VirtualTelephonyDevicesFactory(TelephonyServiceProvider<H> provider) {
        super(provider);
        vendorName = VirtualTelephonyDeviceProvider.DEVICE_FACTORY_VENDOR;
        unitPath = VirtualTelephonyDeviceProvider.DEVICE_FACTORY_VENDOR;
    }

    /// inner classes
    ///
    // the virtual channel-device implementation
    class TelephonyDevice<F extends TelephonyFactory<H, ?>> extends AbstractTelephonyDevice<H, F> {
        protected TelephonyDevice(String deviceName, ServiceProvider<H> serviceProvider) {
            super(deviceName, (VirtualTelephonyDeviceProvider<H>) serviceProvider);
        }

        @Override
        protected H wrongHandle() {
            return null;
        }

        @Override
        protected H errorHandle() {
            return wrongHandle();
        }
    }

    // the virtual device's channel implementation
    class DeviceChannel extends AbstractTelephonyChannel<D> {
        protected DeviceChannel(D device) {
            super(device);
        }
    }
}
