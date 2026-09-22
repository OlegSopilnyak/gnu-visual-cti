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
package org.visualcti.workflow.hardware.javasound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.exception.NoSuchUnitException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SoundCardDevicesFactoryTest<H extends SoundCardHandle> {

    SoundCardDevicesFactory<H, ?> factory;
    Executor deviceEventExecutor;
    ScheduledExecutorService shadowExecutor;
    SoundCardServiceProvider<H> serviceProvider;

    @Before
    public void setUp() {
        deviceEventExecutor = mock(Executor.class);
        shadowExecutor = Executors.newScheduledThreadPool(2);
        doAnswer(invocation -> {
            shadowExecutor.execute(invocation.getArgument(0, Runnable.class));
            return null;
        }).when(deviceEventExecutor).execute(any(Runnable.class));
        serviceProvider = spy(new SoundCardServiceProvider<>(shadowExecutor));
        factory = spy(new SoundCardDevicesFactory(deviceEventExecutor, serviceProvider) {
            @Override
            public Device buildDevice(String deviceName, Device.ServiceProvider serviceProvider) {
                return spy(super.buildDevice(deviceName, serviceProvider));
            }
        });
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String factoryUnitType = factory.getType();

        // check results
        assertThat(factoryUnitType).isEqualTo("[SoundCard:telephony-channel-devices-board]");
    }

    @Test
    public void shouldGetVendor() {
        // preparing test data

        // acting
        String factoryUnitType = factory.getVendor();

        // check results
        assertThat(factoryUnitType).isEqualTo("JavaSound");
    }

    @Test
    public void shouldBuildDevice() {
        // preparing test data
        String deviceName = "device-name";
        TelephonyServiceProvider<H> mockedServiceProvider = mock(TelephonyServiceProvider.class);

        // acting
        Device<?, ?> device = factory.buildDevice(deviceName, mockedServiceProvider);

        // check results
        assertThat(device).isNotNull();
        assertThat(device.getName()).isNotEqualTo(deviceName).isEqualTo("SoundCard");
        assertThat(device.serviceProvider()).isNotEqualTo(mockedServiceProvider).isEqualTo(serviceProvider);
    }

    @Test
    public void shouldMakeChannelFor() {
        // preparing test data
        String deviceName = "device-name";
        TelephonyServiceProvider<H> mockedServiceProvider = mock(TelephonyServiceProvider.class);
        Device<?, ?> device = factory.buildDevice(deviceName, mockedServiceProvider);

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
        assertThat(factory.getVendor()).isEqualTo("JavaSound");
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
    public void shouldGetFactoryXml() {
        // preparing test data

        // acting
        Element factoryXml = factory.getXML();

        // check the behavior
        verify(factory, atLeastOnce()).getVendor();
        verify(factory).getVersion();
        reset(factory);
        // repeating xml-getting
        factory.getXML();
        // no proves of the interactions
        verify(factory, never()).getVendor();
        verify(factory, never()).getVersion();
        // check results
        assertThat(factoryXml).isNotNull();
        List<Element> children = factoryXml.getChildren("parameter");
        assertThat(children).hasSize(2);
        assertThat(children.get(0).getAttributeValue("value")).isEqualTo("JavaSound");
        assertThat(children.get(1).getAttributeValue("value")).endsWith("javasound.configuration.xml");
    }

    @Test
    public void shouldOpenFactory() throws IOException, DataConversionException {
        // preparing test data
        assertThat(factory.getVendor()).isEqualTo("JavaSound");
        assertThat(factory.getVersion()).isEqualTo("0.0.0");

        // acting
        factory.open();

        // check the behavior
        ArgumentCaptor<InputStream> configCapture = ArgumentCaptor.forClass(InputStream.class);
        verify(factory).load(configCapture.capture());
        InputStream configIn = configCapture.getValue();
        verify(factory).restoreDocumentFrom(configIn);
        verify(factory).prepareXmlDocument(configIn);
        ArgumentCaptor<Element> xmlCapture = ArgumentCaptor.forClass(Element.class);
        verify(factory).configure(xmlCapture.capture());
        Element xmlConfig = xmlCapture.getValue();
        verify(factory).setXML(xmlConfig);
        verify(factory, times(2)).devices();
        verify(factory).cleanUnitsTree();
        verify(factory, times(2)).getProvider();
        verify(serviceProvider).allowedDevices();
        verify(factory).buildDevice(anyString(), eq(serviceProvider));
        ArgumentCaptor<Device<H, ?>> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(factory).addDevice(deviceArgumentCaptor.capture());
        Device<H, ?> device = deviceArgumentCaptor.getValue();
        verify(factory).settingUpBasePart(xmlConfig);
        verify(factory).settingUpMainPart(xmlConfig);
        verify(factory, atLeastOnce()).applyUnitParameter(any(ConfigurationParameter.class));
        verify(factory).setVendor(anyString());
        verify(factory).loadFactoryConfiguration();
        ArgumentCaptor<Element> xmlDeviceCapture = ArgumentCaptor.forClass(Element.class);
        verify(device).setXML(xmlDeviceCapture.capture());
        assertThat(xmlDeviceCapture.getValue().getName()).isEqualTo("JavaSound");
        verify(factory).isNeedRegistration();
        // check results
        assertThat(factory.devices().toArray()).isNotEmpty();
        assertThat(factory.channels().toArray()).isEmpty();
        assertThat(factory.getVersion()).isEqualTo("3.2");
        assertThat(device.getName()).isEqualTo("SoundCard");
        assertThat(device.isOpened()).isFalse();
        Exception e = assertThrows(Exception.class, () -> UnitRegistry.lookup(SoundCardDevicesFactory.class));
        assertThat(e).isInstanceOf(NoSuchUnitException.class);
    }

    @Test
    public void shouldStartFactory() throws IOException, DataConversionException {
        // preparing test data
        factory.open();
        assertThat(factory.getVendor()).isEqualTo("JavaSound");
        assertThat(factory.getVersion()).isEqualTo("3.2");
        assertThat(factory.isStarted()).isFalse();
        reset(factory);

        // acting
        factory.Start();

        // check the behavior
        verify(factory).isBroken();
        verify(factory, atLeastOnce()).isStarted();
        verify(factory).canStartUnit();
        verify(factory).startUnitRunnable();
        verify(factory).devices();
        ArgumentCaptor<Device<H, ?>> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(factory).makeChannelFor(deviceArgumentCaptor.capture());
        verify(factory, never()).startUnitChild(any(RunnableServerUnit.class));
        // check results
        assertThat(factory.isStarted()).isTrue();
    }
}
