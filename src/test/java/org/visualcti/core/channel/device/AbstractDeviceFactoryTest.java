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
package org.visualcti.core.channel.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.AbstractChannel;
import org.visualcti.core.channel.Channel;

public class AbstractDeviceFactoryTest {
    static String deviceName = "device-name";
    static String deviceVendor = "device-vendor";
    static String deviceVendorVersion = "device-vendor-version";
    Device<?> device;
    AbstractDeviceFactory<?> factory;

    @Before
    public void setUp() {
        device = mock(Device.class);
        doReturn(deviceName).when(device).getName();
        factory = spy(new TestFactory());
    }

    @Test
    public void shouldGetName() {
        // preparing test data

        // acting
        String factoryUnitName = factory.getName();

        // check the behavior
        verify(factory).getVendor();
        // check results
        assertThat(factoryUnitName).isEqualTo("provider/" + deviceVendor);
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String factoryUnitType = factory.getType();

        // check results
        assertThat(factoryUnitType).isEqualTo("[channel-devices-board]");
    }

    @Test
    public void shouldGetVendor() {
        // preparing test data

        // acting
        String vendorName = factory.getVendor();

        // check results
        assertThat(vendorName).isSameAs(deviceVendor);
    }

    @Test
    public void shouldGetVersion() {
        // preparing test data

        // acting
        String vendorVersion = factory.getVersion();

        // check results
        assertThat(vendorVersion).isSameAs(deviceVendorVersion);
    }

    @Test
    public void shouldGetFactoryDevices() {
        // preparing test data
        factory.add(device);

        // acting
        Stream<Device<?>> available = factory.devices();

        // check the behavior
        verify(factory).children();
        // check results
        assertThat(available.findAny()).isPresent().contains(device);
    }

    @Test
    public void shouldNotGetFactoryDevices_NoDevicesInFactory() {
        // preparing test data

        // acting
        Stream<Device<?>> available = factory.devices();

        // check the behavior
        verify(factory).children();
        // check results
        assertThat(available.count()).isZero();
    }

    @Test
    public void shouldGetChannelsArray() throws IOException {
        // preparing test data
        factory.add(device);
        factory.Start();
        verify(factory, times(2)).children();

        // acting
        Channel<?>[] factoryChannels = factory.channels();

        // check the behavior
        // check results
        assertThat(factoryChannels).isNotEmpty();
        assertThat(factoryChannels[0].getDevice()).isSameAs(device);
    }

    @Test
    public void shouldNotGetChannelsArray_FactoryNotStarted() {
        // preparing test data
        factory.add(device);

        // acting
        Channel<?>[] factoryChannels = factory.channels();

        // check results
        assertThat(factoryChannels).isEmpty();
    }

    @Test
    public void shouldGetEventListenersForDeviceName() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        factory.addDeviceEventListenerFor(deviceName, eventListener);

        // acting
        Stream<DeviceEvent.Listener> stream = factory.eventListeners(deviceName);

        // check results
        assertThat(stream.toArray()).hasSize(1).contains(eventListener);
    }

    @Test
    public void shouldNotGetEventListenersForDeviceName_NoAddedOnes() {
        // preparing test data

        // acting
        Stream<DeviceEvent.Listener> stream = factory.eventListeners(deviceName);

        // check results
        assertThat(stream.toArray()).isEmpty();
    }

    @Test
    public void shouldAddDeviceEventListenerFor() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        assertThat(factory.eventListeners(deviceName).toArray()).isEmpty();

        // acting
        factory.addDeviceEventListenerFor(deviceName, eventListener);

        // check results
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(1).contains(eventListener);
    }

    @Test
    public void shouldNotAddDeviceEventListenerFor_AlreadyAddedOne() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        factory.addDeviceEventListenerFor(deviceName, eventListener);
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(1).contains(eventListener);

        // acting
        factory.addDeviceEventListenerFor(deviceName, eventListener);

        // check results
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(1);
    }

    @Test
    public void shouldRemoveDeviceEventListenerFor() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        factory.addDeviceEventListenerFor(deviceName, eventListener);
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(1).contains(eventListener);

        // acting
        factory.removeDeviceEventListenerFor(deviceName, eventListener);

        // check results
        assertThat(factory.eventListeners(deviceName).toArray()).isEmpty();
    }

    @Test
    public void shouldNotifyListeners() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        DeviceEvent.Listener eventListener2 = mock(DeviceEvent.Listener.class);
        factory.addDeviceEventListenerFor(deviceName, eventListener);
        factory.addDeviceEventListenerFor(deviceName, eventListener2);
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(2).contains(eventListener);
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(2).contains(eventListener2);
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(true).when(eventListener).accept(event);
        doReturn(true).when(eventListener2).accept(event);

        // acting
        factory.notifyListeners(event);

        // check the behavior
        verify(eventListener).accept(event);
        verify(eventListener2).accept(event);
        verify(factory, never()).reject(event);
        // check results
    }

    @Test
    public void shouldNotifyListeners_WithRejectEvent() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        factory.addDeviceEventListenerFor(deviceName, eventListener);
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(1).contains(eventListener);
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();

        // acting
        factory.notifyListeners(event);

        // check the behavior
        verify(eventListener).accept(event);
        verify(factory).reject(event);
        // check results
    }

    @Test
    public void shouldNotifyListeners_WithAcceptEvent() {
        // preparing test data
        DeviceEvent.Listener eventListener = mock(DeviceEvent.Listener.class);
        factory.addDeviceEventListenerFor(deviceName, eventListener);
        assertThat(factory.eventListeners(deviceName).toArray()).hasSize(1).contains(eventListener);
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(true).when(eventListener).accept(event);

        // acting
        factory.notifyListeners(event);

        // check the behavior
        verify(eventListener).accept(event);
        verify(factory, never()).reject(event);
        // check results
    }

    @Test
    public void shouldRejectEvent() {
        // preparing test data
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        AtomicBoolean rejected = new AtomicBoolean(false);
        doAnswer(invocationOnMock -> {
            rejected.getAndSet(true);
            return invocationOnMock.callRealMethod();
        }).when(factory).reject(event);
        assertThat(rejected.get()).isFalse();

        // acting
        factory.reject(event);

        // check results
        assertThat(rejected.get()).isTrue();
    }

    @Test
    public void shouldNotGetDevice_NoANyDevice() {
        // preparing test data

        // acting
        Optional<Device<?>> factoryDevice = factory.getDevice(deviceName);

        // check the behavior
        verify(factory).devices();
        // check results
        assertThat(factoryDevice).isEmpty();
    }

    @Test
    public void shouldGetDevice_DeviceAdded() {
        // preparing test data
        factory.add(device);

        // acting
        Optional<Device<?>> factoryDevice = factory.getDevice(deviceName);

        // check the behavior
        verify(factory).devices();
        // check results
        assertThat(factoryDevice).isPresent().contains(device);
    }

    @Test
    public void shouldMakeChannelFor() {
        // preparing test data

        // acting
        Channel<?> madeDeviceChannel = factory.makeChannelFor(device);

        // check results
        assertThat(madeDeviceChannel.getDevice()).isSameAs(device);
    }

    @Test
    public void shouldStartUnitRunnable_WithoutDevices() throws IOException {
        // preparing test data
        assertThat(factory.channels()).isEmpty();

        // acting
        factory.startUnitRunnable();

        // check the behavior
        verify(factory).devices();
        verify(factory, never()).makeChannelFor(any(Device.class));
        // check results
        assertThat(factory.channels()).isEmpty();
    }

    @Test
    public void shouldStartUnitRunnable_WithDevice() throws IOException {
        // preparing test data
        factory.add(device);
        assertThat(factory.channels()).isEmpty();

        // acting
        factory.startUnitRunnable();

        // check the behavior
        verify(factory).devices();
        verify(factory).makeChannelFor(device);
        // check results
        assertThat(factory.channels()[0].getDevice()).isSameAs(device);
    }

    @Test
    public void shouldStopUnitRunnable() throws IOException {
        // preparing test data
        factory.add(device);
        factory.startUnitRunnable();
        assertThat(factory.channels()[0].getDevice()).isSameAs(device);
        reset(factory);

        // acting
        factory.stopUnitRunnable();

        // check the behavior
        verify(factory, never()).devices();
        // check results
        assertThat(factory.channels()).isEmpty();
    }

    /// / inner classes
    private static class TestFactory extends AbstractDeviceFactory<Device<?>> {
        @Override
        public String getVendor() {
            return deviceVendor;
        }

        @Override
        public String getVersion() {
            return deviceVendorVersion;
        }

        @Override
        protected void reject(DeviceEvent event) {
            // doing nothing here
        }

        @Override
        protected Channel<Device<?>> makeChannelFor(Device<?> device) {
            return new AbstractChannel<Device<?>>(device) {
            };
        }
    }
}