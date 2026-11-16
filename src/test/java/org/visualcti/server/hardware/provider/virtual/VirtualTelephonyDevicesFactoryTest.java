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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.XmlAware;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DevicesManager;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.server.UnitRegistry;

public class VirtualTelephonyDevicesFactoryTest<H extends UUID> {
    String configurationXml = "<factory package=\"org.visualcti.server.hardware.provider.virtual\" class=\"VirtualTelephonyDevicesFactory\" extends=\"org.visualcti.core.channel.telephony.TelephonyFactory\">\n" +
            "  <!-- The implementation of Virtual telephony devices factory features -->\n" +
            "  <builder package=\"org.visualcti.server.hardware.provider.virtual\" class=\"VirtualTelephonyDevicesFactory\" method=\"getInstance\" />\n" +
            "  <parameter name=\"vendor\" type=\"string\" value=\"VirtualTelephony\" />\n" +
            "  <parameter name=\"url\" type=\"string\" value=\"file:./conf/virtualtelephony.configuration.xml\" />\n" +
            "  <parameter name=\"device count\" type=\"number\" value=\"2\" />\n" +
            "  <parameter name=\"device prefix\" type=\"string\" value=\"stub_\" />\n" +
            "</factory>\n\n";

    DevicesManager manager;
    VirtualTelephonyDeviceProvider<H> serviceProvider;
    VirtualTelephonyDevicesFactory<H, TelephonyDevice<H, ?>> factory;

    @Before
    public void setUp() throws Exception {
        manager = mock(DevicesManager.class);
        doReturn("Manager").when(manager).getPath();
        doReturn(Executors.newScheduledThreadPool(2)).when(manager).eventsServiceExecutor();
        serviceProvider = spy(new VirtualTelephonyDeviceProvider<>(Executors.newSingleThreadScheduledExecutor()));
        factory = spy(new VirtualTelephonyDevicesFactory<>(serviceProvider));
        factory.setOwner(manager);
        assertThat(UnitRegistry.lookup("Manager/VisualCTI", VirtualTelephonyDevicesFactory.class)).isNotNull();
    }

    @After
    public void tearDown() throws Exception {
        UnitRegistry.unRegister(factory);
        factory.Stop();
    }

    @Test
    public void shouldGetVendor() {
        // preparing test data

        // acting
        String factoryUnitType = factory.getVendor();

        // check results
        assertThat(factoryUnitType).isEqualTo("VisualCTI");
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String factoryUnitType = factory.getType();

        // check results
        assertThat(factoryUnitType).isEqualTo("[virtual:telephony-channel-devices-board]");
    }

    @Test
    public void shouldGetInstance() {
        // preparing test data

        // acting
        VirtualTelephonyDevicesFactory<?, ?> factoryInstance = VirtualTelephonyDevicesFactory.getInstance();

        // check results
        assertThat(factoryInstance).isNotNull();
    }

    @Test
    public void shouldBuildDevice() throws IOException {
        // preparing test data
        String deviceName = "test-device";

        // acting
        Device<?, ?> device = factory.buildDevice(deviceName, serviceProvider);

        // check results
        assertThat(device).isInstanceOf(VirtualTelephonyDevicesFactory.TelephonyDevice.class);
        assertThat(device.getName()).isEqualTo(deviceName);
    }

    @Test
    public void shouldMakeChannelForDevices() throws IOException {
        // preparing test data
        String deviceName = "test-device";
        Device<H, ?> device = factory.buildDevice(deviceName, serviceProvider);

        // acting
        TelephonyChannel<?> channel = factory.makeChannelFor(device);

        // check results
        assertThat(channel).isInstanceOf(VirtualTelephonyDevicesFactory.DeviceChannel.class);
    }

    @Test
    public void shouldApply_DeviceCount_FactoryParameter() {
        // preparing test data
        int devices = 2;
        ConfigurationParameter parameter = ConfigurationParameter.of("device count", devices);
        assertThat(serviceProvider.getDevicesCount()).isZero();

        // acting
        factory.applyFactoryParameter(parameter);

        // check the behavior
        verify(factory).getDeviceProvider();
        verify(serviceProvider).setDevicesCount(devices);
        // check results
        assertThat(serviceProvider.getDevicesCount()).isEqualTo(devices);
    }

    @Test
    public void shouldApply_DevicePrefix_FactoryParameter() {
        // preparing test data
        String prefix = "stub_";
        ConfigurationParameter parameter = ConfigurationParameter.of("device prefix", prefix);
        assertThat(serviceProvider.getDeviceNamePrefix()).isEqualTo("virtual_");

        // acting
        factory.applyFactoryParameter(parameter);

        // check the behavior
        verify(factory).getDeviceProvider();
        verify(serviceProvider).setDeviceNamePrefix(prefix);
        // check results
        assertThat(serviceProvider.getDeviceNamePrefix()).isEqualTo(prefix);
    }

    @Test
    public void shouldGetFactoryXml() throws IOException {
        // preparing test data
        int devices = 2;
        String prefix = "stub_";
        factory.applyUnitParameter(ConfigurationParameter.of("vendor", "VirtualTelephony"));
        factory.applyFactoryParameter(ConfigurationParameter.of("device count", devices));
        factory.applyFactoryParameter(ConfigurationParameter.of("device prefix", prefix));

        // acting
        Element xml = factory.getXML();

        // check the behavior
        verify(factory).prepareUnitParametersXML(xml);
        // check results
        assertThat(xml).isNotNull();
        assertThat(xml.getName()).isEqualTo("factory");
        String xmlText = new XmlAware() {
        }.asString(xml);
        assertThat(xmlText).isEqualTo(configurationXml);
    }

    @Test
    public void shouldSetFactoryXml() throws IOException, DataConversionException {
        // preparing test data
        int devices = 2;
        String prefix = "stub_";
        Element xml = new XmlAware() {
        }.load(new ByteArrayInputStream(configurationXml.getBytes()));
        factory.setOwner(null);

        // acting
        factory.configure(xml);

        // check the behavior
        verify(factory).setXML(xml);
        verify(factory, times(4)).applyFactoryParameter(any(ConfigurationParameter.class));
        verify(serviceProvider, atLeastOnce()).setDevicesCount(devices);
        verify(serviceProvider, atLeastOnce()).setDeviceNamePrefix(prefix);
        // check results
        assertThat(factory.devices().count()).isEqualTo(devices);
        assertThat(serviceProvider.getDeviceNamePrefix()).isEqualTo(prefix);
        assertThat(serviceProvider.getDevicesCount()).isEqualTo(devices);
    }

    @Test
    public void shouldStartFactory() throws IOException, DataConversionException {
        // preparing test data
        int devices = 2;
        Element xml = new XmlAware() {
        }.load(new ByteArrayInputStream(configurationXml.getBytes()));
        factory.setOwner(null);
        factory.configure(xml);
        assertThat(factory.devices().count()).isEqualTo(devices);
        assertThat(factory.channels()).isEmpty();

        // acting
        factory.Start();

        // check the behavior
        verify(factory).canStartUnit();
        verify(factory, times(devices)).makeChannelFor(any(Device.class));
        // check results
        assertThat(factory.isStarted()).isTrue();
        assertThat(factory.channels()).hasSize(devices);
    }
}