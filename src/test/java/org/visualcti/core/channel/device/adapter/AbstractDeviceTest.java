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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.Factory;

@SuppressWarnings("unchecked")
public class AbstractDeviceTest {
    String vendorVersion = "device-vendor-version";
    String deviceVendor = "device-vendor";
    String deviceName = "device-name";
    Factory<?> factory;
    AbstractDevice<String, ?> device;
    Device.ServiceProvider<?> serviceProvider;
    Device.Session<?> session;
    Executor deviceEventExecutor;
    DeviceEvent.Provider eventsProvider;

    @Before
    public void setUp() throws IOException {
        deviceEventExecutor = mock(Executor.class);
        eventsProvider = mock(DeviceEvent.Provider.class);
        factory = spy(new AbstractFactory(deviceEventExecutor, eventsProvider) {
            @Override
            public String getVendor() {
                return deviceVendor;
            }

            @Override
            public String getVersion() {
                return vendorVersion;
            }
        });
        serviceProvider = mock(Device.ServiceProvider.class);
        session = mock(Device.Session.class);
        device = spy(new AbstractDevice(serviceProvider) {
            @Override
            public String getName() {
                return deviceName;
            }

            @Override
            public Session createSessionFor(Object openedDeviceHandle) {
                return session;
            }
        });
        device.setOwner(factory);
    }

    @Test
    public void shouldGetFactory_DeviceInFactory() {
        // preparing test data

        // acting
        Factory<?> deviceFactory = device.getFactory();

        // check the behavior
        verify(device).getOwner();
        // check results
        assertThat(deviceFactory).isSameAs(factory);
    }

    @Test
    public void shouldNoGetFactory_DeviceNotInFactory() throws IOException {
        // preparing test data
        device.setOwner(null);

        // acting
        Optional<?> deviceFactory = device.getFactoryOptional();

        // check the behavior
        verify(device).getOwner();
        // check results
        assertThat(deviceFactory).isEmpty();
    }

    @Test
    public void shouldOpenDevice() throws IOException {
        // preparing test data
        doReturn(true).when(session).isOpened();
        assertThat(device.isOpened()).isFalse();

        // acting
        device.open();

        // check the behavior
        verify(device).startSession();
        // check results
        assertThat(device.isOpened()).isTrue();
    }

    @Test
    public void shouldNotOpenDevice_ProviderThrowsDuringOpenResource() throws IOException {
        // preparing test data
        doReturn(true).when(session).isOpened();
        assertThat(device.isOpened()).isFalse();
        doThrow(IOException.class).when(serviceProvider).openResource(deviceName);

        // acting
        Exception e = assertThrows(Exception.class, () -> device.open());

        // check the behavior
        verify(device).startSession();
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(device.isOpened()).isFalse();
    }

    @Test
    public void shouldNotOpenDevice_NoFactory() throws IOException {
        // preparing test data
        device.setOwner(null);

        // acting
        Error e = assertThrows(Error.class, () -> device.open());

        // check the behavior
        verify(device).startSession();
        // check results
        assertThat(e).isInstanceOf(DeviceMalfunction.class);
        assertThat(e.getMessage()).contains(deviceName).endsWith("No Factory for the Device!");
    }

    @Test
    public void shouldNotOpenDevice_SessionNotOpened() throws IOException {
        // preparing test data
        assertThat(device.isOpened()).isFalse();

        // acting
        Exception e = assertThrows(Exception.class, () -> device.open());

        // check the behavior
        verify(device).startSession();
        verify(session).isOpened();
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("Device Session could not be opened!");
        assertThat(device.isOpened()).isFalse();
    }

    @Test
    public void shouldCloseDevice() throws IOException {
        // preparing test data
        doReturn(true).when(session).isOpened();
        device.open();
        assertThat(device.isOpened()).isTrue();

        // acting
        device.close();

        // check the behavior
        verify(device, atLeastOnce()).sessions();
        // check results
        assertThat(device.isOpened()).isFalse();
    }

    @Test
    public void shouldDeviceBeOpened() throws IOException {
        // preparing test data
        doReturn(true).when(session).isOpened();
        device.open();

        // acting
        boolean opened = device.isOpened();

        // check results
        assertThat(opened).isTrue();
    }

    @Test
    public void shouldDeviceNotBeOpened_NoDidOpen() {
        // preparing test data
        assertThat(device.isOpened()).isFalse();

        // acting
        boolean opened = device.isOpened();

        // check results
        assertThat(opened).isFalse();
    }

    @Test
    public void shouldRepairDevice() throws IOException {
        // preparing test data
        doReturn(true).when(session).isOpened();

        // acting
        boolean repaired = device.repair();

        // check the behavior
        verify(device).close();
        verify(device).open();
        verify(device).isOpened();
        // check results
        assertThat(repaired).isTrue();
    }

    @Test
    public void shouldNotRepairDevice_SessionNotOpened() throws IOException {
        // preparing test data
        int attemptsCount = 2;
        long attemptsTimeout = 100;
        ConfigurationParameter attempts = ConfigurationParameter.of(Device.REPAIR_ATTEMPT.value(), attemptsCount);
        ConfigurationParameter timeouts = ConfigurationParameter.of(Device.REPAIR_TIMEOUT.value(), attemptsTimeout);
        doReturn(Optional.of(attempts)).when(device).getParameter(Device.REPAIR_ATTEMPT);
        doReturn(Optional.of(timeouts)).when(device).getParameter(Device.REPAIR_TIMEOUT);

        // acting
        boolean repaired = device.repair();

        // check the behavior
        verify(device).close();
        verify(device, times(attemptsCount)).open();
        verify(device, times(attemptsCount)).isOpened();
        // check results
        assertThat(repaired).isFalse();
    }

    @Test
    public void shouldGetName() {
        // preparing test data

        // acting
        String name = device.getName();

        // check results
        assertThat(name).isSameAs(deviceName);
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String type = device.getType();

        // check results
        assertThat(type).isEqualTo("[channel-device]");
    }

    @Test
    public void shouldGetDeviceName() {
        // preparing test data

        // acting
        String name = device.getDeviceName();

        // check the behavior
        verify(device).getName();
        verify(device).getOwner();
        verify(device).getFactory();
        verify(factory).getVendor();
        // check results
        assertThat(name).isEqualTo(deviceVendor + "/" + deviceName);
    }

    @Test
    public void shouldBeStateChangedForDevice() throws IOException {
        // preparing test data
        String handle = "100";
        Device.Session<String> deviceSession = spy(new AbstractDeviceSession(device, handle));
        assertThat(deviceSession.getState()).isSameAs(Device.State.IDLE);
        doReturn(deviceSession).when(device).createSessionFor(handle);

        // acting
        deviceSession.setState(Device.State.STOPD);

        // check the behavior
        verify(device).dispatchEvent("STOPED");
        verify(device).stateChangedFor(deviceSession);
    }

    @Test
    public void shouldNotBeStateChangedForDevice_TheSameState() throws IOException {
        // preparing test data
        String handle = "100";
        Device.Session<String> deviceSession = spy(new AbstractDeviceSession(device, handle));
        assertThat(deviceSession.getState()).isSameAs(Device.State.IDLE);
        doReturn(deviceSession).when(device).createSessionFor(handle);

        // acting
        deviceSession.setState(Device.State.IDLE);

        // check the behavior
        verify(device, never()).dispatchEvent(anyString());
        verify(device, never()).stateChangedFor(deviceSession);
    }

    @Test
    public void shouldGetParameter() {
        // preparing test data
        String parameterName = "parameter-name";
        String parameterValue = "parameter-value";
        Device.ParameterName name = mock(Device.ParameterName.class);
        ConfigurationParameter parameter = ConfigurationParameter.of(parameterName, parameterValue);
        doReturn(Optional.of(parameter)).when(device).getParameter(name);

        // acting
        Object value = device.getParameter(name).map(ConfigurationParameter::getValue).orElse(null);

        // check results
        assertThat(value).isEqualTo(parameterValue);
    }

    @Test
    public void shouldNotGetParameter_Empty() {
        // preparing test data
        Device.ParameterName name = mock(Device.ParameterName.class);

        // acting
        Object value = device.getParameter(name);

        // check results
        assertThat(value).isInstanceOf(Optional.class);
        assertThat((Optional<?>) value).isEmpty();
    }

    @Test
    public void shouldCreateSessionFor() throws IOException {
        // preparing test data
        String handle = "100";
        doReturn(true).when(session).isOpened();

        // acting
        Device.Session<String> deviceSessionFor = device.createSessionFor(handle);

        // check the behavior
        // check results
        assertThat(session).isSameAs(deviceSessionFor);
    }

    @Test
    public void shouldGetStates() throws IOException {
        // preparing test data
        String handle = "101";
        DeviceStateValue sessionStateInitValue = Device.State.IDLE;
        Device.Session<String> deviceSession = spy(new AbstractDeviceSession(device, handle));
        assertThat(deviceSession.getState()).isSameAs(sessionStateInitValue);
        doReturn(deviceSession).when(device).createSessionFor(handle);
        doReturn(handle).when(serviceProvider).openResource(deviceName);
        device.open();

        // acting
        Stream<DeviceStateValue> deviceStates = device.getStates();

        // check results
        assertThat(deviceStates.toArray(DeviceStateValue[]::new)).isNotEmpty().contains(sessionStateInitValue);
    }

    @Test
    public void shouldNotGetStates_DeviceNotOpened() {
        // preparing test data
        assertThat(device.isOpened()).isFalse();

        // acting
        Stream<DeviceStateValue> deviceStates = device.getStates();

        // check results
        assertThat(deviceStates.toArray(DeviceStateValue[]::new)).isEmpty();
    }

    @Test
    public void shouldStartSession() throws IOException {
        // preparing test data
        String handle = "102";
        Device.Session<String> deviceSession = spy(new AbstractDeviceSession(device, handle));
        assertThat(deviceSession.getState()).isSameAs(Device.State.IDLE);
        doReturn(deviceSession).when(device).createSessionFor(handle);
        doReturn(handle).when(serviceProvider).openResource(deviceName);

        // acting
        Device.Session<?> startedSession = device.startSession();

        // check the behavior
        verify(serviceProvider).openResource(deviceName);
        verify(device).findSessionByHandle(handle);
        verify(device).sessions();
        verify(device).createSessionFor(handle);
        verify(factory).addDeviceEventListenerFor(deviceName, deviceSession);
        verify(device).stateChangedFor(deviceSession);
        // check results
        assertThat(startedSession).isSameAs(deviceSession);
    }
}
