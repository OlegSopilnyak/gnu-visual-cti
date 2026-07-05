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
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.action.UnitActionError;

public class TelephonyDeviceTest {
    TelephonyDevice<?> device;

    @Before
    public void setUp() {
        device = mock(TelephonyDevice.class);
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
        assertThat(deviceParameter).isPresent().contains(parameter);
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
    public void shouldGetState() {
        // preparing test data
        DeviceStateValue state = mock(DeviceStateValue.class);
        doReturn(state).when(device).getState();

        // acting
        DeviceStateValue deviceState = device.getState();

        // check results
        assertThat(deviceState).isSameAs(state);
    }

    @Test
    public void shouldCanAcceptCall() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.ACCEPT_CALL_ALLOWED;
        ConfigurationParameter configurationParameter = ConfigurationParameter.of("in", true);
        doCallRealMethod().when(device).canAcceptCall();
        doReturn(Optional.of(configurationParameter)).when(device).getParameter(parameter);

        // acting
        boolean can = device.canAcceptCall();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantAcceptCall() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.ACCEPT_CALL_ALLOWED;
        doCallRealMethod().when(device).canAcceptCall();

        // acting
        boolean can = device.canAcceptCall();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldCanMakeCall() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.MAKE_CALL_ALLOWED;
        ConfigurationParameter configurationParameter = ConfigurationParameter.of("out", true);
        doCallRealMethod().when(device).canMakeCall();
        doReturn(Optional.of(configurationParameter)).when(device).getParameter(parameter);

        // acting
        boolean can = device.canMakeCall();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantMakeCall() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.MAKE_CALL_ALLOWED;
        doCallRealMethod().when(device).canMakeCall();

        // acting
        boolean can = device.canMakeCall();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldCanBeConnected() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.SHARE_CALL_ALLOWED;
        ConfigurationParameter configurationParameter = ConfigurationParameter.of("share", true);
        doCallRealMethod().when(device).canBeConnected();
        doReturn(Optional.of(configurationParameter)).when(device).getParameter(parameter);

        // acting
        boolean can = device.canBeConnected();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantBeConnected() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.SHARE_CALL_ALLOWED;
        doCallRealMethod().when(device).canBeConnected();

        // acting
        boolean can = device.canBeConnected();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldCanFax() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.FAX_ALLOWED;
        ConfigurationParameter configurationParameter = ConfigurationParameter.of("fax", true);
        doCallRealMethod().when(device).canFax();
        doReturn(Optional.of(configurationParameter)).when(device).getParameter(parameter);

        // acting
        boolean can = device.canFax();

        // check the behavior
        verify(device).getParameter(parameter);
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantFax() {
        // preparing test data
        CallsPortEngine.CallParameter parameter = CallsPortEngine.CallParameter.FAX_ALLOWED;
        doCallRealMethod().when(device).canFax();

        // acting
        boolean can = device.canFax();

        // check the behavior
        verify(device).getParameter(parameter);
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

    @Test
    public void shouldGetHandle() {
        // preparing test data
        int handle = 10;
        doReturn(handle).when(device).getHandle();
        doCallRealMethod().when(device).getType();

        // acting
        int deviceHandle = device.getHandle();

        // check results
        assertThat(deviceHandle).isEqualTo(handle);
    }
}