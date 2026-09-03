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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.jdom.DataConversionException;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;

@SuppressWarnings({"unchecked"})
public class AbstractTelephonyFactoryTest {
    AbstractTelephonyFactory<?, ?> factory;
    Executor deviceEventExecutor;
    TelephonyServiceProvider<?> serviceProvider;

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

    /// / inner classes
    private static class TestFactory<H, T extends TelephonyDevice<H, ?>> extends AbstractTelephonyFactory<H, T> {
        public TestFactory(Executor deviceEventExecutor, TelephonyServiceProvider<H> serviceProvider) {
            super(deviceEventExecutor, serviceProvider);
        }

        @Override
        protected TelephonyChannel<T> makeChannelFor(Device<?, ?> device) {
            TelephonyChannel<T> deviceChannel = mock(TelephonyChannel.class);
            doReturn(device).when(deviceChannel).getDevice();
            return deviceChannel;
        }
    }
}