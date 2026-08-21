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
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
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
import org.visualcti.media.Audio;
import org.visualcti.media.Fax;
import org.visualcti.media.Sound;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractTelephonyDeviceTest<H> {
    final static Device.ParameterName ALLOWED_CODECS = MultimediaEngine.Parameter.ALLOWED_CODECS;
    final static Device.ParameterName PLAYBACK_CODEC = MultimediaEngine.Parameter.PLAYBACK_CODEC;
    final static Device.ParameterName RECORD_CODEC = MultimediaEngine.Parameter.RECORD_CODEC;
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
    ScheduledExecutorService shadowExecutor;
    DeviceEvent.Provider<?> eventsProvider;
    AbstractTelephonyDeviceFactory<H, ?> factory;
    AbstractTelephonyDevice<H, ?> device;
    PhoneCallSession<H> session;
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
            public DeviceActivitySession createSessionFor(Object openedDeviceHandle) {
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
            public DeviceActivitySession createSessionFor(Object openedDeviceHandle) {
                return spy(super.createSessionFor(openedDeviceHandle));
            }
        });
        deviceEventExecutor = mock(Executor.class);
        shadowExecutor = Executors.newScheduledThreadPool(2);
        doAnswer(invocation -> {
            shadowExecutor.execute(invocation.getArgument(0, Runnable.class));
            return null;
        }).when(deviceEventExecutor).execute(any(Runnable.class));
        eventsProvider = mock(DeviceEvent.Provider.class);
        factory = spy(new TestFactory<>(deviceEventExecutor, eventsProvider));
        factory.add(device);
        factory.add(mockedDevice);
        session = (PhoneCallSession<H>) device.startSession();
    }

    @Test
    public void shouldStartSession_NoDeviceSharing() throws IOException {
        // preparing test data
        device.detachAndClose(session);
        reset(session, provider, device, factory);
        doReturn(deviceHandle).when(provider).openResource(telephonyDeviceName);

        // acting
        PhoneCallSession<H> startedSession = (PhoneCallSession<H>) device.startSession();

        // check the behavior
        verify(provider).openResource(telephonyDeviceName);
        verify(device).createSessionFor(deviceHandle);
        verify(startedSession).isOpened();
        verify(startedSession, atLeastOnce()).getDeviceHandle();
        verify(faxes).open(startedSession);
        verify(provider).disableEvents(deviceHandle);
        verify(provider).enableEvents(eq(deviceHandle), any(OperationResultValue.class));
        verify(device).canBeConnected();
        // sharing device part
        verify(factory, never()).devices();
        verify(factory, never()).shareDevice((H) any());
        verify(factory, never()).shareDevice(any(PhoneCallSession.class));
        // check results
        assertThat(startedSession).isInstanceOf(DeviceActivitySession.class).isInstanceOf(PhoneCallSession.class);
        assertThat(startedSession.isOpened()).isTrue();
        assertThat(startedSession.isAlive()).isFalse();
        assertThat(startedSession.isTerminated()).isFalse();
        assertThat(startedSession.getDevice()).isSameAs(device);
        assertThat(startedSession.getDeviceName()).isSameAs(telephonyDeviceName);
        assertThat(startedSession.getDeviceHandle()).isSameAs(deviceHandle);
        assertThat(startedSession.getState()).isSameAs(Device.State.IDLE);
        assertThat(startedSession.operationResult()).isSameAs(Result.NONE);
    }

    @Test
    public void shouldStartSession_WithDeviceSharing() throws IOException {
        // preparing test data
        device.detachAndClose(session);
        reset(session, provider, device, factory);
        doReturn(deviceHandle).when(provider).openResource(telephonyDeviceName);
        doReturn(true).when(device).canBeConnected();

        // acting
        PhoneCallSession<H> startedSession = (PhoneCallSession<H>) device.startSession();

        // check the behavior
        verify(provider).openResource(telephonyDeviceName);
        verify(device).createSessionFor(deviceHandle);
        verify(startedSession).isOpened();
        verify(startedSession, atLeastOnce()).getDeviceHandle();
        verify(faxes).open(startedSession);
        verify(provider).disableEvents(deviceHandle);
        verify(provider).enableEvents(eq(deviceHandle), any(OperationResultValue.class));
        verify(device, times(2)).canBeConnected();
        // sharing device part
        verify(factory).devices();
        verify(factory).shareDevice(deviceHandle);
        verify(factory).shareDevice(startedSession);
        // check results
        assertThat(startedSession).isInstanceOf(DeviceActivitySession.class).isInstanceOf(PhoneCallSession.class);
        assertThat(startedSession.isOpened()).isTrue();
        assertThat(startedSession.isAlive()).isFalse();
        assertThat(startedSession.isTerminated()).isFalse();
        assertThat(startedSession.getDevice()).isSameAs(device);
        assertThat(startedSession.getDeviceName()).isSameAs(telephonyDeviceName);
        assertThat(startedSession.getDeviceHandle()).isSameAs(deviceHandle);
        assertThat(startedSession.getState()).isSameAs(Device.State.IDLE);
        assertThat(startedSession.operationResult()).isSameAs(Result.CALL.Analysis.NO_DIAL_TONE);
    }

    @Test
    public void shouldDetachAndCloseSession() throws IOException {
        // preparing test data
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
        verify(faxes, times(2)).isOpened(session);
        verify(session).close();
        verify(session).detachAll();
        // check results
        assertThat(session.isOpened()).isFalse();
        assertThat(session.getDeviceHandle()).isNull();
        assertThat(session.getDevice()).isNull();
    }

    @Test
    public void shouldDropCall_Mocked() {
        // preparing test data
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);

        // acting
        mockedDevice.dropCall(mocked);

        // check the behavior
        verify(mockedCalls).dropCall(mocked);
        // check results
    }

    @Test
    public void shouldDropCall_Regular() throws IOException {
        // preparing test data
        session.alive(true);
        doReturn(true).when(provider).handsetOff(deviceHandle);

        // acting
        device.dropCall(session);

        // check the behavior
        verify(calls).dropCall(session);
        verify(provider).handsetOff(deviceHandle);
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
        session.alive(true);

        // acting
        device.dropCall(session);

        // check the behavior
        verify(calls).dropCall(session);
        verify(provider).handsetOff(deviceHandle);
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
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);
        int rings = 1;
        int timeout = 10;
        boolean answer = true;
        doReturn(true).when(mockedCalls).waitForCall(mocked, rings, timeout, answer);

        // acting
        boolean success = mockedDevice.waitForCall(mocked, rings, timeout, answer);

        // check the behavior
        verify(mockedCalls).waitForCall(mocked, rings, timeout, answer);
        // check results
        assertThat(success).isTrue();
    }

    @Test
    public void shouldWaitForCall_Regular() throws InterruptedException {
        // preparing test data
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
        verify(session).waitingForOperationComplete(anyLong());
        // check results
        assertThat(success).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.TIMEOUT);
    }

    @Test
    public void shouldNotWaitForCall_Regular_CannotAcceptCall() {
        // preparing test data
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
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        doReturn(true).when(mockedCalls).makeCall(mocked, target, timeout);

        // acting
        boolean success = mockedDevice.makeCall(mocked, target, timeout);

        // check the behavior
        verify(mockedCalls).makeCall(mocked, target, timeout);
        // check results
        assertThat(success).isTrue();
    }

    @Test
    public void shouldMakeCall_Regular() throws InterruptedException {
        // preparing test data
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
        verify(session).waitingForOperationComplete(timeout * 1000L);
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
    public void shouldNotMakeCall_Regular_CannotMakeCall() {
        // preparing test data
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
    public void shouldNotMakeCall_Regular_CannotStartCalling() throws InterruptedException {
        // preparing test data
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
        Throwable error = assertThrows(Throwable.class, () -> device.makeCall(session, target, timeout));

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
        verify(session, never()).waitingForOperationComplete(anyLong());
        // check results
        assertThat(error).isInstanceOf(DeviceMalfunction.class);
        assertThat(error.getMessage()).endsWith("Cannot start call on the device side.");
        assertThat(session.isAlive()).isFalse();
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
    }

    @Test
    public void shouldConnect_Mocked() {
        // preparing test data
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        doReturn(true).when(mockedCalls).connect(mocked, target, timeout, toPlay);

        // acting
        boolean success = mockedDevice.connect(mocked, target, timeout, toPlay);

        // check the behavior
        verify(mockedCalls).connect(mocked, target, timeout, toPlay);
        // check results
        assertThat(success).isTrue();
    }

    @Test
    public void shouldConnect_Alive() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        sharedSession.parameter(PhoneCallSession.Parameter.CALLED, target);
        sharedSession.alive(true);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        factory.shareDevice(sharedSession);
        doReturn(true).when(provider).makeConnection(sharedHandle, primaryHandle);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(sharedSession).hasNumber(target);
        verify(provider).makeConnection(sharedHandle, primaryHandle);
        verify(session).join(sharedSession);
        verify(sharedSession, times(2)).join(session);
        // check results
        assertThat(success).isTrue();
        assertThat(session.joint().collect(Collectors.toSet())).contains(sharedSession);
        assertThat(sharedSession.joint().collect(Collectors.toSet())).contains(session);
    }

    @Test
    public void shouldNotConnect_Regular_NoAliveSessionWithPhoneNumber() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        sharedSession.alive(true);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        factory.shareDevice(sharedSession);
        doReturn(true).when(provider).makeConnection(sharedHandle, primaryHandle);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(sharedSession).hasNumber(target);
        verify(provider, never()).makeConnection(sharedHandle, primaryHandle);
        verify(session, never()).join(sharedSession);
        // check results
        assertThat(success).isFalse();
        assertThat(sharedSession.isCaptive()).isFalse();
        assertThat(session.joint().collect(Collectors.toSet())).isEmpty();
        assertThat(sharedSession.joint().collect(Collectors.toSet())).isEmpty();
    }

    @Test
    public void shouldNotConnect_Alive_ProviderDidNotMakeConnection() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        sharedSession.parameter(PhoneCallSession.Parameter.CALLED, target);
        sharedSession.alive(true);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        factory.shareDevice(sharedSession);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(sharedSession).hasNumber(target);
        verify(provider).makeConnection(sharedHandle, primaryHandle);
        verify(session, never()).join(sharedSession);
        // check results
        assertThat(success).isFalse();
        assertThat(sharedSession.isCaptive()).isFalse();
        assertThat(session.joint().collect(Collectors.toSet())).isEmpty();
        assertThat(sharedSession.joint().collect(Collectors.toSet())).isEmpty();
    }

    @Test
    public void shouldConnect_Disconnected() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        String primaryDeviceName = "primary-device-name";
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // substituting device name for the session
        session.parameter(Device.Parameter.NAME, primaryDeviceName);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        factory.shareDevice(sharedSession);
        doReturn(true).when(provider).makeConnection(sharedHandle, primaryHandle);
        doReturn(true).when(calls).makeCall(sharedSession, target, timeout);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(session, atLeastOnce()).isAlive();
        verify(session, never()).hasNumber(target);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(sharedSession, never()).hasNumber(target);
        verify(device).asyncPlaybackAudio(session, toPlay);
        verify(calls).makeCall(sharedSession, target, timeout);
        verify(provider).makeConnection(sharedHandle, primaryHandle);
        verify(provider).stopAudioPlaying(primaryHandle);
        verify(sharedSession).release(sharedSession);
        verify(sharedSession).capture(session);
        verify(session).join(sharedSession);
        verify(sharedSession, times(2)).join(session);
        // check results
        assertThat(success).isTrue();
        assertThat(session.captiveBy()).isEmpty();
        assertThat(sharedSession.isCaptive()).isTrue();
        assertThat(sharedSession.captiveBy()).contains(primaryDeviceName);
        assertThat(session.joint().collect(Collectors.toSet())).contains(sharedSession);
        assertThat(sharedSession.joint().collect(Collectors.toSet())).contains(session);
    }

    @Test
    public void shouldNotConnect_Disconnected_NoFreeSessions() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        String primaryDeviceName = "primary-device-name";
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // substituting device name for the session
        session.parameter(Device.Parameter.NAME, primaryDeviceName);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        doReturn(true).when(provider).makeConnection(sharedHandle, primaryHandle);
        doReturn(true).when(calls).makeCall(sharedSession, target, timeout);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(session, atLeastOnce()).isAlive();
        verify(session, never()).hasNumber(target);
        verify(device, never()).asyncPlaybackAudio(session, toPlay);
        verify(sharedSession, never()).capture(any(PhoneCallSession.class));
        verify(session, never()).join(sharedSession);
        // check results
        assertThat(success).isFalse();
        assertThat(sharedSession.isCaptive()).isFalse();
        assertThat(session.joint().collect(Collectors.toSet())).isEmpty();
        assertThat(sharedSession.joint().collect(Collectors.toSet())).isEmpty();
    }

    @Test
    public void shouldNotConnect_Disconnected_WrongDelegateMakeCall() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        String primaryDeviceName = "primary-device-name";
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // substituting device name for the session
        session.parameter(Device.Parameter.NAME, primaryDeviceName);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        factory.shareDevice(sharedSession);
        doReturn(true).when(provider).makeConnection(sharedHandle, primaryHandle);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(session, atLeastOnce()).isAlive();
        verify(session, never()).hasNumber(target);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(sharedSession, never()).hasNumber(target);
        verify(device).asyncPlaybackAudio(session, toPlay);
        verify(calls).makeCall(sharedSession, target, timeout);
        verify(provider, never()).makeConnection(sharedHandle, primaryHandle);
        verify(provider).stopAudioPlaying(primaryHandle);
        // check results
        assertThat(success).isFalse();
        assertThat(session.captiveBy()).isEmpty();
        assertThat(sharedSession.isCaptive()).isFalse();
        assertThat(session.joint().collect(Collectors.toSet())).isEmpty();
        assertThat(sharedSession.joint().collect(Collectors.toSet())).isEmpty();
    }

    @Test
    public void shouldNotConnect_Disconnected_ProviderDidNotMakeConnection() throws IOException {
        // preparing test data
        doReturn(true).when(device).canBeConnected();
        String primaryDeviceName = "primary-device-name";
        H primaryHandle = (H) "leader";
        H sharedHandle = (H) "shared";
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        session.parameter(Device.Parameter.DEVICE_HANDLE, primaryHandle);
        // substituting device name for the session
        session.parameter(Device.Parameter.NAME, primaryDeviceName);
        // placing the spy of the session instead created previously
        factory.unShareDevice(session);
        factory.shareDevice(session);
        Sound toPlay = mock(Sound.class);
        PhoneCall.Number target = PhoneNumber.of(1, 2, 3, 4);
        int timeout = 10;
        PhoneCallSession<H> sharedSession = spy((PhoneCallSession<H>) device.startSession());
        // substituting device handle for the session to avoid session's closing by next device.startSession()
        sharedSession.parameter(Device.Parameter.DEVICE_HANDLE, sharedHandle);
        // placing the spy of the session instead created previously
        factory.unShareDevice(sharedSession);
        factory.shareDevice(sharedSession);
        doReturn(true).when(calls).makeCall(sharedSession, target, timeout);

        // acting
        boolean success = device.connect(session, target, timeout, toPlay);

        // check the behavior
        verify(calls).connect(session, target, timeout, toPlay);
        verify(factory).findConnectableFor(target, session);
        verify(session, atLeastOnce()).isAlive();
        verify(session, never()).hasNumber(target);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(sharedSession, never()).hasNumber(target);
        verify(device).asyncPlaybackAudio(session, toPlay);
        verify(calls).makeCall(sharedSession, target, timeout);
        verify(provider).makeConnection(sharedHandle, primaryHandle);
        verify(provider).stopAudioPlaying(primaryHandle);
        // check results
        assertThat(success).isFalse();
        assertThat(session.captiveBy()).isEmpty();
        assertThat(sharedSession.isCaptive()).isFalse();
        assertThat(session.joint().collect(Collectors.toSet())).isEmpty();
        assertThat(sharedSession.joint().collect(Collectors.toSet())).isEmpty();
    }

    @Test
    public void shouldGetTransferredPages_Mocked() {
        // preparing test data
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);
        int pages = 20;
        doReturn(pages).when(mockedFaxes).getTransferredPages(mocked);

        // acting
        int result = mockedDevice.getTransferredPages(mocked);

        // check the behavior
        verify(mockedFaxes).getTransferredPages(mocked);
        // check results
        assertThat(result).isEqualTo(pages);
    }

    @Test
    public void shouldGetTransferredPages() throws IOException {
        // preparing test data
        int pages = -20;
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        session.parameter(FaxMachineEngine.Parameter.TRANSFERRED_FAX_PAGES, pages);
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        int result = device.getTransferredPages(session);

        // check the behavior
        verify(faxes).getTransferredPages(session);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session).parameterOrDefault(FaxMachineEngine.Parameter.TRANSFERRED_FAX_PAGES, 0);
        // check results
        assertThat(result).isEqualTo(pages);
    }

    @Test
    public void shouldDontGetTransferredPages_NotAllowedForDevice() {
        // preparing test data
        reset(faxes);
        int pages = -20;

        // acting
        int result = device.getTransferredPages(session);

        // check the behavior
        verify(faxes).getTransferredPages(session);
        verify(faxes).canFax();
        verify(session, never()).isAlive();
        // check results
        assertThat(result).isNotEqualTo(pages).isZero();
    }

    @Test
    public void shouldDontGetTransferredPages_NotOpened() throws IOException {
        // preparing test data
        int pages = -20;
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        session.alive(true);
        reset(faxes, session);

        // acting
        int result = device.getTransferredPages(session);

        // check the behavior
        verify(faxes).getTransferredPages(session);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session, never()).parameterOrDefault(eq(FaxMachineEngine.Parameter.TRANSFERRED_FAX_PAGES), any());
        // check results
        assertThat(result).isNotEqualTo(pages).isZero();
    }

    @Test
    public void shouldDontGetTransferredPages_NothingInSessionParameter() throws IOException {
        // preparing test data
        int pages = -20;
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        int result = device.getTransferredPages(session);

        // check the behavior
        verify(faxes).getTransferredPages(session);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session).parameterOrDefault(FaxMachineEngine.Parameter.TRANSFERRED_FAX_PAGES, 0);
        // check results
        assertThat(result).isNotEqualTo(pages).isZero();
    }

    @Test
    public void shouldDontGetTransferredPages_DisconnectedSession() {
        // preparing test data
        int pages = -20;
        switchFaxPartOn();
        reset(faxes, session);

        // acting
        int result = device.getTransferredPages(session);

        // check the behavior
        verify(faxes).getTransferredPages(session);
        verify(faxes).canFax();
        verify(session).isAlive();
        verify(session, never()).getState();
        // check results
        assertThat(result).isNotEqualTo(pages).isZero();
    }

    @Test
    public void shouldGetRemoteID_Mocked() {
        // preparing test data
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);
        String ID = "remote-id";
        doReturn(ID).when(mockedFaxes).getRemoteID(mocked);

        // acting
        String result = mockedDevice.getRemoteID(mocked);

        // check the behavior
        verify(mockedFaxes).getRemoteID(mocked);
        // check results
        assertThat(result).isSameAs(ID);
    }

    @Test
    public void shouldGetRemoteID() throws IOException {
        // preparing test data
        String ID = "remote-id";
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        session.parameter(FaxMachineEngine.Parameter.REMOTE_FAX_ID, ID);
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        String result = device.getRemoteID(session);

        // check the behavior
        verify(faxes).getRemoteID(session);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session).parameterOrDefault(FaxMachineEngine.Parameter.REMOTE_FAX_ID, "");
        // check results
        assertThat(result).isSameAs(ID);
    }

    @Test
    public void shouldDontGetRemoteID_NotAllowedForDevice() {
        // preparing test data
        reset(faxes);
        String ID = "remote-id";

        // acting
        String result = device.getRemoteID(session);

        // check the behavior
        verify(faxes).getRemoteID(session);
        verify(faxes).canFax();
        verify(session, never()).isAlive();
        // check results
        assertThat(result).isNotEqualTo(ID).isEmpty();
    }

    @Test
    public void shouldDontGetRemoteID_NotOpened() throws IOException {
        // preparing test data
        String ID = "remote-id";
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        session.alive(true);
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        reset(faxes, session);

        // acting
        String result = device.getRemoteID(session);

        // check the behavior
        verify(faxes).getRemoteID(session);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session, never()).parameterOrDefault(eq(FaxMachineEngine.Parameter.REMOTE_FAX_ID), any());
        // check results
        assertThat(result).isNotEqualTo(ID).isEmpty();
    }

    @Test
    public void shouldDontGetRemoteID_NothingInSessionParameter() throws IOException {
        // preparing test data
        String ID = "remote-id";
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        session.alive(true);
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        faxes.open(session);
        reset(faxes, session);

        // acting
        String result = device.getRemoteID(session);

        // check the behavior
        verify(faxes).getRemoteID(session);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session).parameterOrDefault(FaxMachineEngine.Parameter.REMOTE_FAX_ID, "");
        // check results
        assertThat(result).isNotEqualTo(ID).isEmpty();
    }

    @Test
    public void shouldDontGetRemoteID_DisconnectedSession() {
        // preparing test data
        String ID = "remote-id";
        switchFaxPartOn();
        reset(faxes, session);

        // acting
        String result = device.getRemoteID(session);

        // check the behavior
        verify(faxes).getRemoteID(session);
        verify(faxes).canFax();
        verify(session).isAlive();
        verify(session, never()).getState();
        // check results
        assertThat(result).isNotEqualTo(ID).isEmpty();
    }

    @Test
    public void shouldSetFaxHeader_Mocked() {
        // preparing test data
        String faxHeader = "fax-document-header";
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);

        // acting
        mockedDevice.setFaxHeader(mocked, faxHeader);

        // check the behavior
        verify(mockedFaxes).setFaxHeader(mocked, faxHeader);
        // check results
    }

    @Test
    public void shouldSetFaxHeader() throws IOException {
        // preparing test data
        String faxHeader = "fax-document-header";
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        device.setFaxHeader(session, faxHeader);

        // check the behavior
        verify(faxes).setFaxHeader(session, faxHeader);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session).parameter(FaxMachineEngine.Parameter.FAX_PAGE_HEADER, faxHeader);
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.FAX_PAGE_HEADER)).isSameAs(faxHeader);
    }

    @Test
    public void shouldDontSetFaxHeader_NotAllowedForDevice() {
        // preparing test data
        String faxHeader = "fax-document-header";
        reset(faxes, session);

        // acting
        device.setFaxHeader(session, faxHeader);

        // check the behavior
        verify(faxes).setFaxHeader(session, faxHeader);
        verify(faxes).canFax();
        verify(session, never()).isAlive();
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.FAX_PAGE_HEADER)).isNull();
    }

    @Test
    public void shouldDontSetFaxHeader_NotOpened() {
        // preparing test data
        String faxHeader = "fax-document-header";
        switchFaxPartOn();
        session.alive(true);
        reset(faxes, session);

        // acting
        device.setFaxHeader(session, faxHeader);

        // check the behavior
        verify(faxes).setFaxHeader(session, faxHeader);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session, never()).parameter(eq(FaxMachineEngine.Parameter.FAX_PAGE_HEADER), any());
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.FAX_PAGE_HEADER)).isNull();
    }

    @Test
    public void shouldDontSetFaxHeader_Disconnected() {
        // preparing test data
        String faxHeader = "fax-document-header";
        switchFaxPartOn();
        reset(faxes, session);

        // acting
        device.setFaxHeader(session, faxHeader);

        // check the behavior
        verify(faxes).setFaxHeader(session, faxHeader);
        verify(faxes).canFax();
        verify(session).isAlive();
        verify(session, never()).getState();
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.FAX_PAGE_HEADER)).isNull();
    }

    @Test
    public void shouldSetFaxLocalID_Mocked() {
        // preparing test data
        String faxLocalID = "fax-local-id";
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);

        // acting
        mockedDevice.setFaxLocalID(mocked, faxLocalID);

        // check the behavior
        verify(mockedFaxes).setFaxLocalID(mocked, faxLocalID);
        // check results
    }

    @Test
    public void shouldSetFaxLocalID() throws IOException {
        // preparing test data
        String faxLocalID = "fax-local-id";
        H faxHandle = (H) "fax-handle";
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        device.setFaxLocalID(session, faxLocalID);

        // check the behavior
        verify(faxes).setFaxLocalID(session, faxLocalID);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session).parameter(FaxMachineEngine.Parameter.LOCAL_FAX_ID, faxLocalID);
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.LOCAL_FAX_ID)).isSameAs(faxLocalID);
    }

    @Test
    public void shouldDontSetFaxLocalID_NotAllowedForDevice() {
        // preparing test data
        String faxLocalID = "fax-local-id";
        reset(faxes, session);

        // acting
        device.setFaxLocalID(session, faxLocalID);

        // check the behavior
        verify(faxes).setFaxLocalID(session, faxLocalID);
        verify(faxes).canFax();
        verify(session, never()).isAlive();
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.LOCAL_FAX_ID)).isNull();
    }

    @Test
    public void shouldDontSetFaxLocalID_NotOpened() {
        // preparing test data
        String faxLocalID = "fax-local-id";
        switchFaxPartOn();
        session.alive(true);
        reset(faxes, session);

        // acting
        device.setFaxLocalID(session, faxLocalID);

        // check the behavior
        verify(faxes).setFaxLocalID(session, faxLocalID);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(session, never()).parameter(eq(FaxMachineEngine.Parameter.LOCAL_FAX_ID), any());
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.LOCAL_FAX_ID)).isNull();
    }

    @Test
    public void shouldDontSetFaxLocalID_Disconnected() {
        // preparing test data
        String faxLocalID = "fax-local-id";
        switchFaxPartOn();
        reset(faxes, session);

        // acting
        device.setFaxLocalID(session, faxLocalID);

        // check the behavior
        verify(faxes).setFaxLocalID(session, faxLocalID);
        verify(faxes).canFax();
        verify(session).isAlive();
        verify(session, never()).getState();
        // check results
        assertThat(session.<String>parameter(FaxMachineEngine.Parameter.LOCAL_FAX_ID)).isNull();
    }

    @Test
    public void shouldReceiveFaxDocument_Mocked() {
        // preparing test data
        PhoneCallSession<H> mocked = mock(PhoneCallSession.class);
        OutputStream target = mock(OutputStream.class);
        boolean pollingMode = true;
        boolean issueVoiceRequest = true;
        doReturn(true).when(mockedDevice).isOpened();
        doReturn(Result.OK).when(mockedFaxes).receive(mocked, target, pollingMode, issueVoiceRequest);

        // acting
        OperationResultValue result = mockedDevice.receive(mocked, target, pollingMode, issueVoiceRequest);

        // check the behavior
        verify(mockedDevice).isOpened();
        verify(mockedFaxes).receive(mocked, target, pollingMode, issueVoiceRequest);
        // check results
        assertThat(result).isSameAs(Result.OK);
    }

    @Test
    public void shouldReceiveFaxDocument_EOF() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        H faxHandle = (H) "fax-handle";
        OutputStream target = mock(OutputStream.class);
        boolean pollingMode = true;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);
        Runnable completeRunnable = () -> {
            File tempFaxFile = session.parameter(FaxMachineEngine.Parameter.FAX_TEMPORARY);
            try (OutputStream out = new FileOutputStream(tempFaxFile)) {
                out.write(faxContent.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing fax document transmitting operation
            session.operationComplete(Result.IO.EOF);
        };

        // acting
        Future<OperationResultValue> action = shadowExecutor.submit(
                () -> device.receive(session, target, pollingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(completeRunnable, 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).receive(session, target, pollingMode, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyReceiveFaxEventsManagement(provider, faxHandle);
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        verify(session).waitingForOperationComplete(1000L);
        verify(provider).stopFaxReceiving(faxHandle);
        verify(session).operationResult(Result.IO.EOF);
        verify(session).setState(Device.State.IDLE);
        // final IO result checking
        ArgumentCaptor<byte[]> receivedDataCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<Integer> receivedDataSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(target).write(receivedDataCaptor.capture(), anyInt(), receivedDataSizeCaptor.capture());
        // check results
        assertThat(result).isSameAs(Result.IO.EOF);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        byte[] receivedData = Arrays.copyOf(receivedDataCaptor.getValue(), receivedDataSizeCaptor.getValue());
        assertThat(receivedData).isEqualTo(faxContent.getBytes());
    }

    @Test
    public void shouldNotReceiveFaxDocument_DeviceError() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String errorReason = "Receive fax document is failed.";
        H faxHandle = (H) "fax-handle";
        OutputStream target = mock(OutputStream.class);
        boolean pollingMode = true;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<Throwable> action = shadowExecutor.submit(() ->
                assertThrows(Throwable.class, () -> device.receive(session, target, pollingMode, issueVoiceRequest))
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(() -> session.operationComplete(Result.ERROR), 50, TimeUnit.MILLISECONDS);
        Throwable error = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).receive(session, target, pollingMode, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyReceiveFaxEventsManagement(provider, faxHandle);
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        verify(session).waitingForOperationComplete(1000L);
        verify(provider).stopFaxReceiving(faxHandle);
        verify(device).dispatchError(errorReason);
        verify(session, times(2)).operationResult(Result.ERROR);
        verify(session).setState(Device.State.ERROR);
        // final IO result checking
        verify(target, never()).write(any(byte[].class), anyInt(), anyInt());
        // check results
        assertThat(error).isInstanceOf(DeviceMalfunction.class);
        assertThat(error.getMessage()).endsWith(errorReason);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_WrongResult() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        H faxHandle = (H) "fax-handle";
        OutputStream target = mock(OutputStream.class);
        boolean pollingMode = true;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<OperationResultValue> action = shadowExecutor.submit(
                () -> device.receive(session, target, pollingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(() -> session.operationResult(Result.FAX.COMMUNICATION_ERROR), 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).receive(session, target, pollingMode, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyReceiveFaxEventsManagement(provider, faxHandle);
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        verify(session).waitingForOperationComplete(1000L);
        verify(provider).stopFaxReceiving(faxHandle);
        verify(session).operationResult(Result.FAX.COMMUNICATION_ERROR);
        verify(session).setState(Device.State.ERROR);
        // final IO result checking
        verify(target, never()).write(any(byte[].class), anyInt(), anyInt());
        // check results
        assertThat(result).isSameAs(Result.FAX.COMMUNICATION_ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
    }

    @Test
    public void shouldTransmitFaxDocument_Mocked() {
        // preparing test data
        InputStream source = mock(InputStream.class);
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        doReturn(Result.OK).when(mockedFaxes).transmit(session, source, format, issueVoiceRequest);

        // acting
        OperationResultValue result = mockedDevice.transmit(session, source, format, issueVoiceRequest);

        // check the behavior
        verify(mockedDevice).isOpened();
        verify(mockedFaxes).transmit(session, source, format, issueVoiceRequest);
        // check results
        assertThat(result).isSameAs(Result.OK);
    }

    @Test
    public void shouldTransmitFaxDocument_EOF() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        H faxHandle = (H) "fax-handle";
        InputStream source = prepareFaxSource(faxContent);
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<OperationResultValue> action = shadowExecutor.submit(
                () -> device.transmit(session, source, format, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(() -> session.operationComplete(Result.IO.EOF), 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).transmit(session, source, format, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyTransmitFaxEventsManagement(provider, faxHandle);
        verify(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).waitingForOperationComplete(1000L);
        verify(provider).stopFaxTransmitting(faxHandle);
        // check results
        assertThat(result).isSameAs(Result.IO.EOF);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
    }

    @Test
    public void shouldNotTransmitFaxDocument_DeviceError() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String errorReason = "Send fax document is failed.";
        String faxContent = "Fax Document Content";
        H faxHandle = (H) "fax-handle";
        InputStream source = prepareFaxSource(faxContent);
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<Throwable> action = shadowExecutor.submit(() ->
                assertThrows(Throwable.class, () -> device.transmit(session, source, format, issueVoiceRequest))
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(() -> session.operationComplete(Result.ERROR), 50, TimeUnit.MILLISECONDS);
        Throwable error = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).transmit(session, source, format, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyTransmitFaxEventsManagement(provider, faxHandle);
        verify(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).waitingForOperationComplete(1000L);
        verify(provider, never()).stopFaxTransmitting(any());
        // check results
        assertThat(error).isInstanceOf(DeviceMalfunction.class);
        assertThat(error.getMessage()).endsWith(errorReason);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_WrongResult() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        H faxHandle = (H) "fax-handle";
        InputStream source = prepareFaxSource(faxContent);
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<OperationResultValue> action = shadowExecutor.submit(
                () -> device.transmit(session, source, format, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(() -> session.operationResult(Result.FAX.COMPATIBILITY), 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).transmit(session, source, format, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyTransmitFaxEventsManagement(provider, faxHandle);
        verify(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).waitingForOperationComplete(1000L);
        verify(provider).stopFaxTransmitting(faxHandle);
        // check results
        assertThat(result).isSameAs(Result.FAX.COMPATIBILITY);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldCanPlay_AvailableCodecs_Mocked() {
        // preparing test data
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        doReturn(audios).when(mockedMedia).canPlay();

        // acting
        Audio[] all = mockedDevice.canPlay();

        // check the behavior
        verify(mockedMedia).canPlay();
        // check results
        assertThat(all).isEqualTo(audios);
    }

    @Test
    public void shouldCanPlayParticularAudio() {
        // preparing test data
        Audio audio = Audio.LINEAR_8;
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of(ALLOWED_CODECS.value(), Arrays.asList(audios)));
        device.setParameter(ALLOWED_CODECS, allAudios);

        // acting
        boolean can = device.canPlay(audio);

        // check the behavior
        verify(device).canPlay();
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCannotPlayParticularAudio_UnsupportedFormat() {
        // preparing test data
        Audio audio = Audio.ALAW_8;
        Device.ParameterName name = ALLOWED_CODECS;
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of(name.value(), Arrays.asList(audios)));
        device.setParameter(name, allAudios);

        // acting
        boolean can = device.canPlay(audio);

        // check the behavior
        verify(device).canPlay();
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldCanPlay_AvailableCodecs_Regular() {
        // preparing test data
        Device.ParameterName name = ALLOWED_CODECS;
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of(name.value(), Arrays.asList(audios)));
        device.setParameter(ALLOWED_CODECS, allAudios);

        // acting
        Audio[] all = device.canPlay();

        // check the behavior
        verify(media).canPlay();
        verify(device).getParameter(name);
        verify(allAudios).getValue();
        // check results
        assertThat(all).isEqualTo(audios);
    }

    @Test
    public void shouldCannotPlay_NoAvailableCodecs() {
        // preparing test data
        Device.ParameterName name = ALLOWED_CODECS;
        assertThat(device.getParameter(name)).isEmpty();
        media.uses(device);
        reset(device);

        // acting
        Audio[] all = device.canPlay();

        // check the behavior
        verify(media).canPlay();
        verify(device).getParameter(name);
        // check results
        assertThat(device.getParameter(name)).isEmpty();
        assertThat(all).isEmpty();
    }

    @Test
    public void shouldGetRawPlayingFormat_Mocked() {
        // preparing test data
        doReturn(Audio.LINEAR).when(mockedMedia).getRawFormat();

        // acting
        Audio rawAudio = mockedDevice.getRawFormat();

        // check the behavior
        verify(mockedMedia).getRawFormat();
        // check results
        assertThat(rawAudio).isEqualTo(Audio.LINEAR);
    }

    @Test
    public void shouldGetRawPlayingFormat_Regular() {
        // preparing test data
        media.uses(device);
        Audio audio = Audio.LINEAR;
        Device.ParameterName name = PLAYBACK_CODEC;
        ConfigurationParameter playbackCodec = spy(ConfigurationParameter.of(name.value(), audio));
        device.setParameter(name, playbackCodec);

        // acting
        Audio rawAudio = device.getRawFormat();

        // check the behavior
        verify(media).getRawFormat();
        verify(device).getParameter(name);
        verify(playbackCodec).getValue();
        // check results
        assertThat(rawAudio).isEqualTo(audio);
    }

    @Test
    public void shouldNotGetRawPlayingFormat_NoAvailableCodec() {
        // preparing test data
        media.uses(device);
        Device.ParameterName name = PLAYBACK_CODEC;

        // acting
        Audio rawAudio = device.getRawFormat();

        // check the behavior
        verify(media).getRawFormat();
        verify(device).getParameter(name);
        // check results
        assertThat(rawAudio).isNull();
        assertThat(device.getParameter(name)).isEmpty();
    }

    @Test
    public void playbackAudio() {
    }

    @Test
    public void asyncPlaybackAudio() {
    }

    @Test
    public void shouldCanRecord() {
        // preparing test data
        media.uses(device);
        Audio audio = Audio.LINEAR;
        Device.ParameterName name = RECORD_CODEC;
        device.setParameter(name, spy(ConfigurationParameter.of(name.value(), audio)));

        // acting
        Audio[] all = device.canRecord();

        // check the behavior
        verify(media).canRecord();
        verify(media).getRecordFormat();
        verify(device).getParameter(name);
        // check results
        assertThat(all).containsExactly(audio);
    }

    @Test
    public void shouldCannotRecord_NoAvailableCodecs() {
        // preparing test data
        media.uses(device);

        // acting
        Audio[] all = device.canRecord();

        // check the behavior
        verify(media).canRecord();
        verify(media).getRecordFormat();
        verify(device).getParameter(RECORD_CODEC);
        // check results
        assertThat(all).isEmpty();
    }

    @Test
    public void shouldGetRecordFormat_Mocked() {
        // preparing test data
        doReturn(Audio.LINEAR).when(mockedMedia).getRecordFormat();

        // acting
        Audio rawAudio = mockedDevice.getRecordFormat();

        // check the behavior
        verify(mockedMedia).getRecordFormat();
        // check results
        assertThat(rawAudio).isEqualTo(Audio.LINEAR);
    }

    @Test
    public void shouldCanRecordParticularAudio() {
        // preparing test data
        media.uses(device);
        Audio audio = Audio.LINEAR;
        Device.ParameterName name = RECORD_CODEC;
        device.setParameter(name, spy(ConfigurationParameter.of(name.value(), audio)));

        // acting
        boolean can = device.canRecord(audio);

        // check the behavior
        verify(device).canRecord();
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCannotRecordParticularAudio_UnsupportedFormat() {
        // preparing test data
        media.uses(device);
        Audio audio = Audio.LINEAR;
        Device.ParameterName name = RECORD_CODEC;
        device.setParameter(name, spy(ConfigurationParameter.of(name.value(), audio)));

        // acting
        boolean can = device.canRecord(Audio.LINEAR_8);

        // check the behavior
        verify(device).canRecord();
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldGetRecordFormat_Regular() {
        // preparing test data
        media.uses(device);
        Audio audio = Audio.LINEAR;
        Device.ParameterName name = RECORD_CODEC;
        ConfigurationParameter recordCodec = spy(ConfigurationParameter.of(name.value(), audio));
        device.setParameter(name, recordCodec);

        // acting
        Audio rawAudio = device.getRecordFormat();

        // check the behavior
        verify(media).getRecordFormat();
        verify(device).getParameter(name);
        verify(recordCodec).getValue();
        // check results
        assertThat(rawAudio).isEqualTo(Audio.LINEAR);
    }

    @Test
    public void shouldNotGetRecordFormat_NoAvailableCodec() {
        // preparing test data
        media.uses(device);
        Device.ParameterName name = RECORD_CODEC;

        // acting
        Audio rawAudio = device.getRecordFormat();

        // check the behavior
        verify(media).getRecordFormat();
        verify(device).getParameter(name);
        // check results
        assertThat(rawAudio).isNull();
        assertThat(device.getParameter(name)).isEmpty();
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
    public void shouldTerminateReceiveFax() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        H faxHandle = (H) "fax-handle";
        OutputStream target = mock(OutputStream.class);
        boolean pollingMode = true;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<OperationResultValue> action = shadowExecutor.submit(
                () -> device.receive(session, target, pollingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(this::terminateActivity, 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).receive(session, target, pollingMode, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verifyReceiveFaxEventsManagement(provider, faxHandle);
        verify(provider).startFaxReceiving(eq(faxHandle), anyString(), eq(issueVoiceRequest));
        verify(session).waitingForOperationComplete(1000L);
        verify(provider, atLeastOnce()).stopFaxReceiving(faxHandle);
        // final IO result checking
        verify(target, never()).write(any(byte[].class), anyInt(), anyInt());
        // check results
        assertThat(result).isSameAs(Result.TERMINATED);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
    }


    @Test
    public void shouldTerminateTransmitFax() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        H faxHandle = (H) "fax-handle";
        InputStream source = prepareFaxSource(faxContent);
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        switchFaxPartOn();
        doReturn(faxHandle).when(provider).openFaxResource(telephonyDeviceName);
        doReturn(true).when(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        faxes.open(session);
        session.alive(true);
        reset(faxes, session);

        // acting
        Future<OperationResultValue> action = shadowExecutor.submit(
                () -> device.transmit(session, source, format, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        shadowExecutor.schedule(this::terminateActivity, 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = action.get();

        // check the behavior
        verify(device).isOpened();
        verify(faxes).transmit(session, source, format, issueVoiceRequest);
        verifyEngineSessionProceedingAbility(faxes, session);
        verify(device).getProvider();
        verifyTransmitFaxEventsManagement(provider, faxHandle);
        verify(provider).startFaxTransmitting(eq(faxHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).waitingForOperationComplete(1000L);
        verify(provider, atLeastOnce()).stopFaxTransmitting(faxHandle);
        // check results
        assertThat(result).isSameAs(Result.TERMINATED);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
    }

    /// private methods
    // allowing fax feature
    private void switchFaxPartOn() {
        FaxMachineEngine.Parameter allowed = FaxMachineEngine.Parameter.FAX_ALLOWED;
        device.setParameter(allowed, ConfigurationParameter.of(allowed.value(), true));
    }

    // to prepare the input stream for te fax transmit operation
    private static InputStream prepareFaxSource(String faxContent) throws IOException {
        InputStream source = mock(InputStream.class);
        AtomicBoolean firstRead = new AtomicBoolean(true);
        doAnswer((Answer<Integer>) invocation -> {
            if (firstRead.get()) {
                byte[] b = invocation.getArgument(0);
                byte[] payload = faxContent.getBytes();
                System.arraycopy(payload, 0, b, 0, payload.length);
                firstRead.getAndSet(false);
                return payload.length;
            } else {
                return -1;
            }
        }).when(source).read(any(byte[].class), anyInt(), anyInt());
        return source;
    }

    // validating proceeding engine and session behavior
    private static <H> void verifyEngineSessionProceedingAbility(FaxMachineEngine<H> engine, PhoneCallSession<H> session) {
        verify(engine).canFax();
        verify(session, atLeastOnce()).isAlive();
        verify(engine, atLeastOnce()).isOpened(session);
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
    }

    // validating receive fax events management
    private static <H> void verifyReceiveFaxEventsManagement(TelephonyServiceProvider<H> provider, H deviceHandle) {
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
    }

    // validating receive fax events management
    private static <H> void verifyTransmitFaxEventsManagement(TelephonyServiceProvider<H> provider, H deviceHandle) {
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
    }

    // to terminate device's current activity
    private void terminateActivity() {
        try {
            device.terminate(session);
        } catch (IOException e) {
            // nothing to do
        }
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