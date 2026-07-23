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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultiMedeaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;

public class AbstractTelephonyDeviceTest<H> {
    String telephonyDeviceName = "telephony-device";
    TelephonyServiceProvider<H> provider;
    CallsPortEngine<H> calls;
    TonesEngine<H> tones;
    MultiMedeaEngine<H> media;
    FaxMachineEngine<H> faxes;

    static String deviceVendor = "device-vendor";
    static String deviceVendorVersion = "device-vendor-version";
    H deviceHandle = (H) "mock()";
    Executor deviceEventExecutor;
    ExecutorService shadowExecutor;
    DeviceEvent.Provider<?> eventsProvider;
    AbstractTelephonyDeviceFactory<H, ?> factory;
    AbstractTelephonyDevice<H ,?> device;

    @Before
    public void setUp() throws Exception {
        provider = mock(TelephonyServiceProvider.class);
        doReturn(deviceHandle).when(provider).openResource(telephonyDeviceName);
        calls = mock(CallsPortEngine.class);
        doReturn(calls).when(calls).uses(any(TelephonyDeviceCore.class));
        tones = mock(TonesEngine.class);
        doReturn(tones).when(tones).uses(any(TelephonyDeviceCore.class));
        media = mock(MultiMedeaEngine.class);
        doReturn(media).when(media).uses(any(TelephonyDeviceCore.class));
        faxes = mock(FaxMachineEngine.class);
        doReturn(faxes).when(faxes).uses(any(TelephonyDeviceCore.class));
        device = spy(new AbstractTelephonyDevice(telephonyDeviceName, provider, calls, tones, media, faxes){
            @Override
            public Session createSessionFor(Object openedDeviceHandle) {
                return spy(super.createSessionFor(openedDeviceHandle));
            }
        });
        deviceEventExecutor = mock(Executor.class);
        shadowExecutor = Executors.newFixedThreadPool(2);
        doAnswer(invocation -> {
            shadowExecutor.execute(invocation.getArgument(0, Runnable.class));
            return null;
        }).when(deviceEventExecutor).execute(any(Runnable.class));
        eventsProvider = mock(DeviceEvent.Provider.class);
        factory = spy(new TestFactory<>(deviceEventExecutor, eventsProvider));
        factory.add(device);
    }

    @Test
    public void shouldStartSession_WithoutDeviceSharing() throws IOException {
        // preparing test data

        // acting
        Device.Session<H> session = device.startSession();

        // check the behavior
        verify(provider).openResource(telephonyDeviceName);
        verify(device).createSessionFor(deviceHandle);
        verify(session).isOpened();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(faxes).open(session);
        verify(provider).disableEvents(deviceHandle);
        verify(provider).enableEvents(eq(deviceHandle), any(OperationResultValue.class));
        verify(device).canBeConnected();
        // sharing device part
        verify(factory, never()).shareDevice((H) any(), anyLong());
        verify(factory, never()).shareDevice(any(PhoneCallSession.class), anyLong());
        // check results
        assertThat(session).isInstanceOf(TelephonyDevice.Session.class).isInstanceOf(PhoneCallSession.class);
        assertThat(session.isOpened()).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.isTerminated()).isFalse();
        assertThat(session.getDevice()).isSameAs(device);
        assertThat(session.getDeviceName()).isSameAs(telephonyDeviceName);
        assertThat(session.getDeviceHandle()).isSameAs(deviceHandle);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(((PhoneCallSession)session).operationResult()).isSameAs(Result.NONE);
    }

    @Test
    public void stopAndDetach() {
    }

    /// / inner classes
    private static class TestFactory<H, T extends TelephonyDevice<H, ?>> extends AbstractTelephonyDeviceFactory<H, T> {
        public TestFactory(Executor deviceEventExecutor, DeviceEvent.Provider eventsProvider) {
            super(deviceEventExecutor, eventsProvider);
        }

        @Override
        public String getVendor() {
            return deviceVendor;
        }

        @Override
        public String getVersion() {
            return deviceVendorVersion;
        }

        @Override
        protected TelephonyChannel<T> makeChannelFor(Device<?, ?> device) {
            TelephonyChannel<T> deviceChannel = mock(TelephonyChannel.class);
            doReturn(device).when(deviceChannel).getDevice();
            return deviceChannel;
        }
    }
}