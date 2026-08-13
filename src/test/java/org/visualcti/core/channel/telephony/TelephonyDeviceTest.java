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
package org.visualcti.core.channel.telephony;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.action.UnitActionError;

public class TelephonyDeviceTest {
    TelephonyDevice<?, ?> device;
    String deviceName = "device-name";
    TelephonyServiceProvider<?> provider;

    @Before
    public void setUp() {
        device = mock(TelephonyDevice.class);
        doReturn(deviceName).when(device).getName();
        provider = mock(TelephonyServiceProvider.class);
        doReturn(provider).when(device).getProvider();
    }

    @Test
    public void shouldGetType() {
        // preparing test data
        doCallRealMethod().when(device).getType();

        // acting
        String type = device.getType();

        // check results
        assertThat(type).isEqualTo("[telephony-channel-device]");
    }

    @Test
    public void shouldGetName() {
        // preparing test data
        String telephonyDeviceName = "telephony-device";
        doReturn(telephonyDeviceName).when(device).getName();

        // acting
        String type = device.getName();

        // check results
        assertThat(type).isEqualTo(telephonyDeviceName);
    }

    @Test
    public void shouldGetParameter() {
        // preparing test data
        Device.ParameterName parameterName = mock(Device.ParameterName.class);
        ConfigurationParameter parameter = mock(ConfigurationParameter.class);
        doReturn(Optional.of(parameter)).when(device).getParameter(parameterName);

        // acting
        Optional<ConfigurationParameter> deviceParameter = device.getParameter(parameterName);

        // check results
        assertThat(deviceParameter).contains(parameter);
    }

    @Test
    public void shouldNotGetParameter() {
        // preparing test data
        Device.ParameterName parameterName = mock(Device.ParameterName.class);

        // acting
        Optional<ConfigurationParameter> deviceParameter = device.getParameter(parameterName);

        // check results
        assertThat(deviceParameter).isEmpty();
    }

    @Test
    public void shouldGetStates() {
        // preparing test data
        DeviceStateValue state = mock(DeviceStateValue.class);
        doReturn(Stream.of(state)).when(device).getStates();

        // acting
        Stream<DeviceStateValue> states = device.getStates();

        // check results
        assertThat(states.toArray()).contains(state);
    }

    @Test
    public void shouldNotGetStates() {
        // preparing test data

        // acting
        Stream<DeviceStateValue> states = device.getStates();

        // check results
        assertThat(states.toArray()).isEmpty();
    }

    @Test
    public void shouldCanFax() {
        // preparing test data
        Device.ParameterName parameter = FaxMachineEngine.Parameter.FAX_ALLOWED;
        ConfigurationParameter configurationParameter = ConfigurationParameter.of("fax", true);
        doCallRealMethod().when(device).canFax();
        doReturn(true).when(provider).canFax(deviceName);
        doReturn(Optional.of(configurationParameter)).when(device).getParameter(parameter);

        // acting
        boolean can = device.canFax();

        // check the behavior
        verify(device).getProvider();
        verify(provider).canFax(deviceName);
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantFax_NotConfiguredFeature() {
        // preparing test data
        Device.ParameterName parameter = FaxMachineEngine.Parameter.FAX_ALLOWED;
        doReturn(true).when(provider).canFax(deviceName);
        doCallRealMethod().when(device).canFax();

        // acting
        boolean can = device.canFax();

        // check the behavior
        verify(device).getProvider();
        verify(provider).canFax(deviceName);
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldCantFax_ProviderIssue() {
        // preparing test data
        doCallRealMethod().when(device).canFax();

        // acting
        boolean can = device.canFax();

        // check the behavior
        verify(device).getProvider();
        verify(provider).canFax(deviceName);
        verify(device, never()).getParameter(any(Device.ParameterName.class));
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldDispatchError() throws IOException {
        // preparing test data
        UnitMessageFactory factory = mock(UnitMessageFactory.class);
        UnitActionError unitError = mock(UnitActionError.class);
        Throwable error = mock(Throwable.class);
        String description = "description";
        doCallRealMethod().when(device).dispatchError(any(Throwable.class), anyString());
        doReturn(factory).when(device).getMessageFactory();
        doReturn(unitError).when(factory).buildFor(device, MessageType.ERROR, MessageFamilyType.ERROR, description);

        // acting
        device.dispatchError(error, description);

        // check the behavior
        verify(device).dispatch(unitError);
        // check results
    }
}