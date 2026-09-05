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
package org.visualcti.core.channel.telephony.adapter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;

@SuppressWarnings({"unchecked"})
public class AbstractTelephonyFactoryTest<H> {
    AbstractTelephonyFactory<H, ?> factory;
    Executor deviceEventExecutor;
    TelephonyServiceProvider<H> serviceProvider;

    @Before
    public void setUp() {
        deviceEventExecutor = mock(Executor.class);
        serviceProvider = mock(TelephonyServiceProvider.class);
        factory = spy(new TestFactory<>(deviceEventExecutor, serviceProvider));
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String factoryUnitType = factory.getType();

        // check results
        assertThat(factoryUnitType).isEqualTo("[telephony-channel-devices-board]");
    }

    @Test
    public void shouldMakeChannelFor() {
        // preparing test data
        TelephonyDevice<?, ?> device = mock(TelephonyDevice.class);

        // acting
        TelephonyChannel<?> madeDeviceChannel = factory.makeChannelFor(device);

        // check results
        assertThat(madeDeviceChannel.getDevice()).isSameAs(device);
    }

    @Test
    public void shouldApplyUnitParameter_VendorName() {
        // preparing test data
        String vendorName = "vendor-name";
        ConfigurationParameter parameter = ConfigurationParameter.of("vendor", vendorName);
        assertThat(factory.getConfigurationDocument().getRootElement().getName()).isNotEqualTo(vendorName);
        assertThat(factory.getVendor()).isNotEqualTo(vendorName);

        // acting
        factory.applyUnitParameter(parameter);

        // check results
        assertThat(factory.getVendor()).isEqualTo(vendorName);
        assertThat(factory.getConfigurationDocument().getRootElement().getName()).isEqualTo(vendorName);
    }

    @Test
    public void shouldApplyUnitParameter_ConfigFileUrl() throws IOException, DataConversionException {
        // preparing test data
        String value = "file:./conf/dialogic.configuration.xml";
        ConfigurationParameter parameter = ConfigurationParameter.of("url", value);
        File defaultConfigFile = factory.configurationFile();

        // acting
        factory.applyUnitParameter(parameter);

        // check the behavior
        verify(factory).loadFactoryConfiguration();
        // check results
        assertThat(factory.configurationFile()).isNotEqualTo(defaultConfigFile);
    }

    @Test
    public void shouldApplyUnitParameter_VendorVersion() {
        // preparing test data
        String vendorVersion = "vendor-version";
        ConfigurationParameter parameter = ConfigurationParameter.of("version", vendorVersion);
        assertThat(factory.getVersion()).isNotEqualTo(vendorVersion);

        // acting
        factory.applyUnitParameter(parameter);

        // check results
        assertThat(factory.getVersion()).isEqualTo(vendorVersion);
    }

    @Test
    public void shouldGetFactoryXml() throws IOException, DataConversionException {
        // preparing test data
        String vendorName = "vendor-name";
        String vendorVersion = "vendor-version";
        String configurationPath = "./conf/test-factory.configuration.xml";
        Element xml = new Element(factory.getRootElementName())
                .addContent(ConfigurationParameter.of("vendor", vendorName).getXml())
                .addContent(ConfigurationParameter.of("version", vendorVersion).getXml())
                .addContent(ConfigurationParameter.of("url", "file:" + configurationPath).getXml());
        Element defaultXml = factory.getXML();
        List<Element> children = defaultXml.getChildren("parameter");
        assertThat(children).hasSize(2);
        assertThat(children.get(0).getAttributeValue("value")).isNotEqualTo(vendorName);
        assertThat(children.get(1).getAttributeValue("value").endsWith(configurationPath)).isFalse();
        factory.setXML(xml);
        factory.configurationFile().deleteOnExit();
        reset(factory);

        // acting
        Element factoryXml = factory.getXML();

        // check the behavior
        verify(factory).getVendor();
        verify(factory).getVersion();
        reset(factory);
        // repeating xml-getting
        factory.getXML();
        // no proves of the interactions
        verify(factory, never()).getVendor();
        verify(factory, never()).getVersion();
        // check results
        assertThat(factoryXml).isNotNull();
        children = factoryXml.getChildren("parameter");
        assertThat(children).hasSize(3);
        assertThat(children.get(0).getAttributeValue("value")).isEqualTo(vendorName);
        assertThat(children.get(1).getAttributeValue("value").endsWith(configurationPath)).isTrue();
        assertThat(children.get(2).getAttributeValue("value")).isEqualTo(vendorVersion);
    }

    @Test
    public void shouldSetupFactoryXml() throws IOException, DataConversionException {
        // preparing test data
        String[] allowed = new String[]{"device1", "device2", "dxxxB1C1"};
        doReturn(Arrays.asList(allowed)).when(serviceProvider).allowedDevices();
        String vendorName = "vendor-name";
        String vendorVersion = "vendor-version";
        Path configurationPath = Paths.get("./conf/test-factory.configuration.xml");
        Element xml = new Element(factory.getRootElementName())
                .addContent(ConfigurationParameter.of("vendor", vendorName).getXml())
                .addContent(ConfigurationParameter.of("version", vendorVersion).getXml())
                .addContent(ConfigurationParameter.of("url", "file:" + configurationPath).getXml());
        assertThat(factory.getVendor()).isNotEqualTo(vendorName);
        assertThat(factory.getVersion()).isNotEqualTo(vendorVersion);
        Files.copy(Paths.get("./conf/dialogic.configuration.xml"), configurationPath, REPLACE_EXISTING);
        File configFile = configurationPath.toFile();
        configFile.deleteOnExit();
        String defaultConfigRoot = factory.getConfigurationDocument().getRootElement().getName();
        reset(factory);

        // acting
        factory.setXML(xml);

        // check the behavior
        ArgumentCaptor<ConfigurationParameter> unitParametersCaptor = ArgumentCaptor.forClass(ConfigurationParameter.class);
        verify(factory, atLeastOnce()).applyUnitParameter(unitParametersCaptor.capture());
        List<Device<H, ?>> devices = factory.devices().collect(Collectors.toList());
        verify(factory, atLeastOnce()).defaultDeviceXml();
        for (Device<H, ?> device : devices) {
            verify(factory).addDevice(device);
            verify(device, atLeastOnce()).applyDeviceParameters(any(Element.class));
        }
        verify(factory).loadFactoryConfiguration();
        verify(factory, times(2)).saveFactoryConfiguration();
        verify(factory, atLeastOnce()).configurationFile();
        verify(factory).restoreDocumentFrom(any(InputStream.class));
        // check results
        assertThat(factory.getVendor()).isEqualTo(vendorName);
        assertThat(factory.getConfigurationDocument().getRootElement().getName())
                .isNotEqualTo(defaultConfigRoot).isEqualTo(vendorName);
        assertThat(factory.getVersion()).isEqualTo(vendorVersion);
        assertThat(factory.devices().count()).isEqualTo(allowed.length);
        List<ConfigurationParameter> factoryConfigurationParameters = unitParametersCaptor.getAllValues();
        assertThat(factoryConfigurationParameters.get(0).getName()).isEqualTo("vendor");
        assertThat(factoryConfigurationParameters.get(1).getName()).isEqualTo("version");
        assertThat(factoryConfigurationParameters.get(2).getName()).isEqualTo("url");
        IntStream.range(0, allowed.length).forEach(i ->
                assertThat(devices.get(i).getName()).isEqualTo(allowed[i])
        );
        assertThat(configFile).exists();
        assertThat(configFile.delete()).isTrue();
    }

    /// / inner classes
    private static class TestFactory<H, T extends TelephonyDevice<H, ?>> extends AbstractTelephonyFactory<H, T> {
        public TestFactory(Executor deviceEventExecutor, TelephonyServiceProvider<H> serviceProvider) {
            super(deviceEventExecutor, serviceProvider);
        }

        @Override
        public Device<H, ?> buildDevice(String deviceName, Device.ServiceProvider<H> serviceProvider) {
            return spy(new TestDevice<>(deviceName, (TelephonyServiceProvider<H>) serviceProvider));
        }

        @Override
        protected TelephonyChannel<T> makeChannelFor(Device<?, ?> device) {
            TelephonyChannel<T> deviceChannel = mock(TelephonyChannel.class);
            doReturn(device).when(deviceChannel).getDevice();
            return deviceChannel;
        }
    }

    private static class TestDevice<H, T extends TelephonyFactory<H, ?>> extends AbstractTelephonyDevice<H, T> {
        public TestDevice(String name, TelephonyServiceProvider<H> provider) {
            super(name, provider);
        }
    }
}