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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractCallsPortEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractFaxMachineEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractMultimediaEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractTonesEngine;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractTelephonyDeviceTest<H> {
    String telephonyDeviceName = "telephony-device";
    TelephonyServiceProvider<H> provider;
    CallsPortEngine<H> calls;
    TonesEngine<H> tones;
    MultimediaEngine<H> media;
    FaxMachineEngine<H> faxes;
    CallsPortEngine<H> mockedCalls;
    TonesEngine<H> mockedTones;
    MultimediaEngine<H> mockedMedia;
    FaxMachineEngine<H> mockedFaxes;

    static String deviceVendor = "device-vendor";
    static String deviceVendorVersion = "device-vendor-version";
    H deviceHandle = (H) "mock()";
    Executor deviceEventExecutor;
    ExecutorService shadowExecutor;
    DeviceEvent.Provider<?> eventsProvider;
    AbstractTelephonyDeviceFactory<H, ?> factory;
    AbstractTelephonyDevice<H, ?> device;
    AbstractTelephonyDevice<H, ?> mockedDevice;

    @Before
    public void setUp() throws Exception {
        provider = mock(TelephonyServiceProvider.class);
        doReturn(deviceHandle).when(provider).openResource(telephonyDeviceName);
        calls = spy(new AbstractCallsPortEngine() {
        });
        tones = spy(new AbstractTonesEngine() {
        });
        media = spy(new AbstractMultimediaEngine() {
        });
        faxes = spy(new AbstractFaxMachineEngine() {
        });
        device = spy(new AbstractTelephonyDevice(telephonyDeviceName, provider, calls, tones, media, faxes) {
            @Override
            public Session createSessionFor(Object openedDeviceHandle) {
                return spy(super.createSessionFor(openedDeviceHandle));
            }
        });
        mockedCalls = mock(CallsPortEngine.class);
        mockedTones = mock(TonesEngine.class);
        mockedMedia = mock(MultimediaEngine.class);
        mockedFaxes = mock(FaxMachineEngine.class);
        doReturn(mockedCalls).when(mockedCalls).uses(any(TelephonyDevice.class));
        doReturn(mockedTones).when(mockedTones).uses(any(TelephonyDevice.class));
        doReturn(mockedMedia).when(mockedMedia).uses(any(TelephonyDevice.class));
        doReturn(mockedFaxes).when(mockedFaxes).uses(any(TelephonyDevice.class));
        mockedDevice = spy(new AbstractTelephonyDevice(telephonyDeviceName, provider, mockedCalls, mockedTones, mockedMedia, mockedFaxes) {
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
    public void shouldStartSession_NoDeviceSharing() throws IOException {
        // preparing test data

        // acting
        PhoneCallSession<H> session = (PhoneCallSession<H>) device.startSession();

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
        verify(factory, never()).devices();
        verify(factory, never()).shareDevice((H) any(), anyLong());
        verify(factory, never()).shareDevice(any(PhoneCallSession.class), anyLong());
        // check results
        assertThat(session).isInstanceOf(Device.Session.class).isInstanceOf(PhoneCallSession.class);
        assertThat(session.isOpened()).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.isTerminated()).isFalse();
        assertThat(session.getDevice()).isSameAs(device);
        assertThat(session.getDeviceName()).isSameAs(telephonyDeviceName);
        assertThat(session.getDeviceHandle()).isSameAs(deviceHandle);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.NONE);
    }

    @Test
    public void shouldStartSession_WithDeviceSharing() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();

        // acting
        PhoneCallSession<H> session = (PhoneCallSession<H>) device.startSession();

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
        verify(factory).devices();
        verify(factory).shareDevice(deviceHandle, -1L);
        verify(factory).shareDevice(session, -1L);
        // check results
        assertThat(session).isInstanceOf(Device.Session.class).isInstanceOf(PhoneCallSession.class);
        assertThat(session.isOpened()).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.isTerminated()).isFalse();
        assertThat(session.getDevice()).isSameAs(device);
        assertThat(session.getDeviceName()).isSameAs(telephonyDeviceName);
        assertThat(session.getDeviceHandle()).isSameAs(deviceHandle);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.CALL.Analysis.NO_DIAL_TONE);
    }

    @Test
    public void shouldDetachAndCloseSession() throws IOException {
        // preparing test data
        PhoneCallSession<H> session = (PhoneCallSession<H>) device.startSession();
        assertThat(session.isOpened()).isTrue();
        reset(device, session, provider);

        // acting
        device.detachAndClose(session);

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(factory).unShareDevice(deviceHandle);
        verify(factory).unShareDevice(session);
        verify(provider).disableEvents(deviceHandle);
        verify(faxes).close(session);
        // check results
        assertThat(session.isOpened()).isFalse();
    }

    @Test
    public void shouldDropCall_Mocked() {
        // preparing test data
        PhoneCallSession<H> session = mock(PhoneCallSession.class);

        // acting
        mockedDevice.dropCall(session);

        // check the behavior
        verify(mockedCalls).dropCall(session);
        // check results
    }

    @Test
    public void shouldDropCall_Regular() throws IOException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        session.alive(true);
        doReturn(true).when(provider).dropCall(deviceHandle);

        // acting
        device.dropCall(session);

        // check the behavior
        verify(calls).dropCall(session);
        verify(provider).dropCall(deviceHandle);
        verify(device).terminate(session);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session).detachAll();
        verify(session).alive(false);
        verify(session).operationResult(Result.CALL.DISCONNECT);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider, atLeastOnce()).enableEvents(deviceHandle, Result.CALL.RINGS);
        // check results
        assertThat(session.isAlive()).isFalse();
    }

    @Test
    public void shouldNotDropCall_Regular_Provider() throws IOException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        session.alive(true);

        // acting
        device.dropCall(session);

        // check the behavior
        verify(calls).dropCall(session);
        verify(provider).dropCall(deviceHandle);
        verify(device).terminate(session);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, never()).detachAll();
        verify(device).dispatchError(anyString());
        verify(session).setState(Device.State.ERROR);
        verify(session).operationResult(Result.ERROR);
        // check results
        assertThat(session.isAlive()).isTrue();
    }

    @Test
    public void shouldWaitForCall_Mocked() {
        // preparing test data
        PhoneCallSession<H> session = mock(PhoneCallSession.class);
        int rings = 1;
        int timeout = 10;
        boolean answer = true;
        doReturn(true).when(mockedCalls).waitForCall(session, rings, timeout, answer);

        // acting
        boolean success = mockedDevice.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(mockedCalls).waitForCall(session, rings, timeout, answer);
        // check results
        assertThat(success).isTrue();
    }

    @Test
    public void shouldWaitForCall_Regular() throws IOException, InterruptedException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        doReturn(true).when(provider).canAcceptCall(telephonyDeviceName);
        Device.ParameterName parameterName = CallsPortEngine.Parameter.ACCEPT_CALL_ALLOWED;
        device.setParameter(parameterName, ConfigurationParameter.of(parameterName.value(), true));
        int rings = 1;
        int timeout = 1;
        boolean answer = true;

        // acting
        boolean success = device.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(calls).waitForCall(session, rings, timeout, answer);
        verify(calls).canAcceptCall();
        verify(session).isDisconnected();
        verify(calls).canBeConnected();
        verify(session).setState(TelephonyDevice.State.WAIT);
        verify(session).waitingForTheOperationComplete(anyLong());
        // check results
        assertThat(success).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.TIMEOUT);
    }

    @Test
    public void shouldNotWaitForCall_Regular_CannotAcceptCall() throws IOException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        int rings = 1;
        int timeout = 10;
        boolean answer = true;

        // acting
        boolean success = device.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(calls).waitForCall(session, rings, timeout, answer);
        verify(calls).canAcceptCall();
        verify(session, never()).isDisconnected();
        verify(calls, never()).canBeConnected();
        // check results
        assertThat(success).isFalse();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.NONE);
    }

    @Test
    public void shouldMakeCall_Mocked() {
        // preparing test data
        PhoneCallSession<H> session = mock(PhoneCallSession.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        doReturn(true).when(mockedCalls).makeCall(session, target, timeout);

        // acting
        boolean success = mockedDevice.makeCall(session, target, timeout);

        // check the behavior
        verify(mockedCalls).makeCall(session, target, timeout);
        // check results
        assertThat(success).isTrue();
    }

    @Test
    public void shouldMakeCall_Regular() throws IOException, InterruptedException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        {
            Device.ParameterName parameterName = CallsPortEngine.Parameter.MAKE_CALL_ALLOWED;
            device.setParameter(parameterName, ConfigurationParameter.of(parameterName.value(), true));
        }
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        PhoneCall.Number original = PhoneNumber.of(4, 3, 2, 1);
        {
            Device.ParameterName parameterName = CallsPortEngine.Parameter.ORIGIN;
            device.setParameter(parameterName, ConfigurationParameter.of(parameterName.value(), original));
        }
        int timeout = 1;
        doReturn(true).when(provider).canMakeCall(telephonyDeviceName);
        doReturn(true).when(provider).startCalling(deviceHandle, target, timeout);

        // acting
        boolean success = device.makeCall(session, target, timeout);

        // check the behavior
        verify(calls).makeCall(session, target, timeout);
        verify(calls).canMakeCall();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session).isDisconnected();
        verify(session).calledNumber(target);
        verify(session).callingNumber(original);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(session).operationResult(Result.NONE);
        verify(provider).startCalling(deviceHandle, target, timeout);
        verify(session).waitingForTheOperationComplete(timeout * 1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(session).isTerminated();
        verify(session).alive(false);
        // check results
        assertThat(success).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.operationResult()).isSameAs(Result.CALL.Analysis.NO_ANSWER);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
    }

    @Test
    public void shouldNotMakeCall_Regular_CannotMakeCall() throws IOException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        {
            Device.ParameterName parameterName = CallsPortEngine.Parameter.MAKE_CALL_ALLOWED;
            device.setParameter(parameterName, ConfigurationParameter.of(parameterName.value(), true));
        }
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 1;

        // acting
        boolean success = device.makeCall(session, target, timeout);

        // check the behavior
        verify(calls).makeCall(session, target, timeout);
        verify(calls).canMakeCall();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, never()).isDisconnected();
        // check results
        assertThat(success).isFalse();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
    }

    @Test
    public void shouldNotMakeCall_Regular_CannotStartCalling() throws IOException, InterruptedException {
        // preparing test data
        PhoneCallSession<H> session = spy((PhoneCallSession<H>) device.startSession());
        {
            Device.ParameterName parameterName = CallsPortEngine.Parameter.MAKE_CALL_ALLOWED;
            device.setParameter(parameterName, ConfigurationParameter.of(parameterName.value(), true));
        }
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        PhoneCall.Number original = PhoneNumber.of(4, 3, 2, 1);
        {
            Device.ParameterName parameterName = CallsPortEngine.Parameter.ORIGIN;
            device.setParameter(parameterName, ConfigurationParameter.of(parameterName.value(), original));
        }
        int timeout = 1;
        doReturn(true).when(provider).canMakeCall(telephonyDeviceName);

        // acting
        Throwable error = assertThrows(Throwable.class, () ->device.makeCall(session, target, timeout));

        // check the behavior
        verify(calls).makeCall(session, target, timeout);
        verify(calls).canMakeCall();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session).isDisconnected();
        verify(session).calledNumber(target);
        verify(session).callingNumber(original);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(session).operationResult(Result.NONE);
        verify(provider).startCalling(deviceHandle, target, timeout);
        verify(session, never()).waitingForTheOperationComplete(anyLong());
        // check results
        assertThat(error).isInstanceOf(DeviceMalfunction.class);
        assertThat(error.getMessage()).endsWith("Cannot start call on the device side.");
        assertThat(session.isAlive()).isFalse();
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
    }

    @Test
    public void connect() {
    }

    @Test
    public void getTransferredPages() {
    }

    @Test
    public void getRemoteID() {
    }

    @Test
    public void setFaxHeader() {
    }

    @Test
    public void setFaxLocalID() {
    }

    @Test
    public void receive() {
    }

    @Test
    public void transmit() {
    }

    @Test
    public void canPlay() {
    }

    @Test
    public void getRawFormat() {
    }

    @Test
    public void playbackAudio() {
    }

    @Test
    public void asyncPlaybackAudio() {
    }

    @Test
    public void canRecord() {
    }

    @Test
    public void getRecordFormat() {
    }

    @Test
    public void recordAudio() {
    }

    @Test
    public void dial() {
    }

    @Test
    public void playTone() {
    }

    @Test
    public void inputDigits() {
    }

    @Test
    public void getInputSymbols() {
    }

    @Test
    public void terminate() {
    }

    /// inner classes
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