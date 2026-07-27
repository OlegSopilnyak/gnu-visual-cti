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
package org.visualcti.core.channel.telephony.part.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.media.Sound;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractCallsPortEngineTest<H> {
    ScheduledExecutorService executor;

    PhoneCallSession<H> session;
    AbstractCallsPortEngine<H> engine;
    TelephonyDevice<H, ?> device;
    TelephonyServiceProvider<H> provider;
    String deviceName = "device-name";
    H deviceHandle = (H) "handle";
    TelephonyDeviceFactory<H, ?> factory;
    Executor deviceEventExecutor;
    DeviceEvent.Provider<H> eventsProvider;

    @Before
    public void setUp() {
        provider = mock(TelephonyServiceProvider.class);
        device = mock(TelephonyDevice.class);
        doReturn(deviceName).when(device).getName();
        doReturn(provider).when(device).getProvider();
        session = spy(new PhoneCallSession(device, deviceHandle) {
        });
        engine = spy(new AbstractCallsPortEngine());
        executor = Executors.newSingleThreadScheduledExecutor();
        deviceEventExecutor = Executors.newSingleThreadExecutor();
        eventsProvider = mock(DeviceEvent.Provider.class);
        factory = spy(new AbstractTelephonyDeviceFactory(executor, deviceEventExecutor, eventsProvider) {
            @Override
            protected TelephonyChannel makeChannelFor(Device device) {
                return null;
            }
        });
        doReturn(factory).when(device).getFactory();
    }

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdown();
        }
        executor = null;
    }

    @Test
    public void shouldDropCall() throws IOException {
        // preparing test data
        session.alive(true);
        doReturn(true).when(provider).dropCall(deviceHandle);

        // acting
        boolean done = engine.dropCall(session);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session).isAlive();
        verify(session).getDevice();
        verify(device).terminate(session);
        verify(device).getProvider();
        verify(provider).dropCall(deviceHandle);
        verify(session).operationComplete(Result.CALL.DISCONNECT);
        verify(provider).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.RINGS);
        verify(session).setState(Device.State.IDLE);
        verify(session).alive(false);
        // check results
        assertThat(done).isTrue();
    }

    @Test
    public void shouldNotDropCall_Disconnected() {
        // preparing test data

        // acting
        boolean done = engine.dropCall(session);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session).isAlive();
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldNotDropCall_ProviderDidntWork() throws IOException {
        // preparing test data
        session.alive(true);
        reset(session);

        // acting
        boolean done = engine.dropCall(session);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session).isAlive();
        verify(session).getDevice();
        verify(device).terminate(session);
        verify(device).getProvider();
        verify(provider).dropCall(deviceHandle);
        verify(session).operationComplete(Result.ERROR);
        verify(device).dispatchError(anyString());
        verify(session, never()).setState(any(DeviceStateValue.class));
        verify(session, never()).alive(anyBoolean());
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldWaitForCall_Timeout() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canAcceptCall();
        engine.uses(device);
        int rings = 2;
        int timeout = 1;
        boolean answer = true;

        // acting
        boolean done = engine.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canAcceptCall();
        verify(session).isAlive();
        verify(device).getProvider();
        verify(device, times(timeout)).getParameter(any(Device.ParameterName.class));
        verify(provider, times(timeout)).enableEvents(deviceHandle, Result.CALL.RINGS);
        verify(session, times(timeout)).setState(TelephonyDevice.State.WAIT);
        verify(session, times(timeout)).operationComplete(Result.NONE);
        verify(session, times(timeout)).waitForOperationComplete(1000L);
        verify(session, times(timeout)).operationResult();
        verify(session).operationComplete(Result.TIMEOUT);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.TIMEOUT);
    }

    @Test
    public void shouldWaitForCall_RingsEvent_WithAnswer() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canAcceptCall();
        PhoneCall.Number callingNumber = mock(PhoneCall.Number.class);
        doReturn(callingNumber).when(provider).getCallerID(deviceHandle);
        doReturn(true).when(provider).answerCall(deviceHandle);
        engine.uses(device);
        int rings = 2;
        int timeout = 10;
        boolean answer = true;
        executor.schedule(() -> session.operationComplete(Result.CALL.RINGS), 50, TimeUnit.MILLISECONDS);

        // acting
        boolean done = engine.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canAcceptCall();
        verify(session).isAlive();
        verify(device).getProvider();
        verify(device).getParameter(any(Device.ParameterName.class));
        verify(provider).enableEvents(deviceHandle, Result.CALL.RINGS);
        verify(session).setState(TelephonyDevice.State.WAIT);
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(1000L);
        verify(session).operationResult();
        verify(provider).disableEvents(deviceHandle, Result.CALL.RINGS);
        verify(provider).getCallerID(deviceHandle);
        verify(session).callingNumber(callingNumber);
        verify(provider).answerCall(deviceHandle);
        verify(session).alive(true);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).operationComplete(Result.CALL.ALERTING);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.ALERTING);
    }

    @Test
    public void shouldWaitForCall_RingsEvent_WithoutAnswer() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canAcceptCall();
        PhoneCall.Number callingNumber = mock(PhoneCall.Number.class);
        doReturn(callingNumber).when(provider).getCallerID(deviceHandle);
        doReturn(true).when(provider).answerCall(deviceHandle);
        engine.uses(device);
        int rings = 2;
        int timeout = 10;
        boolean answer = false;
        executor.schedule(() -> session.operationComplete(Result.CALL.RINGS), 50, TimeUnit.MILLISECONDS);


        // acting
        boolean done = engine.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canAcceptCall();
        verify(session).isAlive();
        verify(device).getProvider();
        verify(device).getParameter(any(Device.ParameterName.class));
        verify(provider).enableEvents(deviceHandle, Result.CALL.RINGS);
        verify(session).setState(TelephonyDevice.State.WAIT);
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(1000L);
        verify(session).operationResult();
        verify(provider).disableEvents(deviceHandle, Result.CALL.RINGS);
        verify(provider).getCallerID(deviceHandle);
        verify(session).callingNumber(callingNumber);
        verify(provider, never()).answerCall(any());
        verify(session, never()).alive(anyBoolean());
        verify(provider, never()).enableEvents(any(), eq(Result.CALL.DISCONNECT));
        verify(session, times(2)).operationComplete(Result.CALL.RINGS);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.RINGS);
    }

    @Test
    public void shouldNotWaitForCall_CannotAcceptCall() {
        // preparing test data
        int rings = 2;
        int timeout = 1;
        boolean answer = true;

        // acting
        boolean done = engine.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canAcceptCall();
        verify(session, never()).isAlive();
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldNotWaitForCall_IsAlive() {
        // preparing test data
        doReturn(true).when(engine).canAcceptCall();
        session.alive(true);
        int rings = 2;
        int timeout = 1;
        boolean answer = true;

        // acting
        boolean done = engine.waitForCall(session, rings, timeout, answer);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canAcceptCall();
        verify(session).isAlive();
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldMakeCall_NoAnswer() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canMakeCall();
        engine.uses(device);
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 1;
        doReturn(true).when(provider).startCalling(deviceHandle, number, timeout);

        // acting
        boolean done = engine.makeCall(session, number, timeout);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canMakeCall();
        verify(session).isAlive();
        verify(device, atLeastOnce()).getProvider();
        verify(session).calledNumber(number);
        verify(device).getParameter(any(Device.ParameterName.class));
        verify(provider).disableEvents(deviceHandle);
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(session).operationComplete(Result.NONE);
        verify(provider).startCalling(deviceHandle, number, timeout);
        verify(session).waitForOperationComplete(timeout * 1000L);
        verify(session).operationResult();
        verify(session).alive(anyBoolean());
        verify(provider, never()).enableEvents(any(), eq(Result.CALL.DISCONNECT));
        verify(device, atLeastOnce()).dispatchEvent(anyString());
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.Analysis.NO_ANSWER);
    }

    @Test
    public void shouldMakeCall_VoiceAnswer() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canMakeCall();
        engine.uses(device);
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 10;
        doReturn(true).when(provider).startCalling(deviceHandle, number, timeout);
        executor.schedule(() -> session.operationComplete(Result.CALL.Analysis.VOICE), 50, TimeUnit.MILLISECONDS);

        // acting
        boolean done = engine.makeCall(session, number, timeout);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canMakeCall();
        verify(session).isAlive();
        verify(device, atLeastOnce()).getProvider();
        verify(session).calledNumber(number);
        verify(device).getParameter(any(Device.ParameterName.class));
        verify(provider).disableEvents(deviceHandle);
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(session).operationComplete(Result.NONE);
        verify(provider).startCalling(deviceHandle, number, timeout);
        verify(session).waitForOperationComplete(timeout * 1000L);
        verify(session,atLeastOnce()).operationResult();
        verify(session).alive(true);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(device, atLeastOnce()).dispatchEvent(anyString());
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.Analysis.VOICE);
    }

    @Test
    public void shouldMakeCall_FaxAnswer() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canMakeCall();
        engine.uses(device);
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 10;
        doReturn(true).when(provider).startCalling(deviceHandle, number, timeout);
        executor.schedule(() -> session.operationComplete(Result.CALL.Analysis.FAX), 50, TimeUnit.MILLISECONDS);

        // acting
        boolean done = engine.makeCall(session, number, timeout);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canMakeCall();
        verify(session).isAlive();
        verify(device, atLeastOnce()).getProvider();
        verify(session).calledNumber(number);
        verify(device).getParameter(any(Device.ParameterName.class));
        verify(provider).disableEvents(deviceHandle);
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(session).operationComplete(Result.NONE);
        verify(provider).startCalling(deviceHandle, number, timeout);
        verify(session).waitForOperationComplete(timeout * 1000L);
        verify(session,atLeastOnce()).operationResult();
        verify(session).alive(true);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(device, atLeastOnce()).dispatchEvent(anyString());
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.Analysis.FAX);
    }

    @Test
    public void shouldNotMakeCall_InvalidAnswer() throws InterruptedException {
        // preparing test data
        doReturn(true).when(engine).canMakeCall();
        engine.uses(device);
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 10;
        doReturn(true).when(provider).startCalling(deviceHandle, number, timeout);
        executor.schedule(() -> session.operationComplete(Result.CALL.Analysis.BUSY), 50, TimeUnit.MILLISECONDS);

        // acting
        boolean done = engine.makeCall(session, number, timeout);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canMakeCall();
        verify(session).isAlive();
        verify(device, atLeastOnce()).getProvider();
        verify(session).calledNumber(number);
        verify(device).getParameter(any(Device.ParameterName.class));
        verify(provider).disableEvents(deviceHandle);
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(session).operationComplete(Result.NONE);
        verify(provider).startCalling(deviceHandle, number, timeout);
        verify(session).waitForOperationComplete(timeout * 1000L);
        verify(session,atLeastOnce()).operationResult();
        verify(session).alive(false);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(device, atLeastOnce()).dispatchEvent(anyString());
        // check results
        assertThat(done).isTrue();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.Analysis.BUSY);
    }

    @Test
    public void shouldNotMakeCall_CannotMakeCall() {
        // preparing test data
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 10;

        // acting
        boolean done = engine.makeCall(session, number, timeout);

        // check the behavior
        verify(session).getDeviceHandle();
        verify(engine).canMakeCall();
        verify(session, never()).isAlive();
        // check results
        assertThat(done).isFalse();
        assertThat(session.isAlive()).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
    }

    @Test
    public void shouldNotMakeCall_IsAlive() {
        // preparing test data
        doReturn(true).when(engine).canMakeCall();
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 10;
        session.alive(true);

        // acting
        boolean done = engine.makeCall(session, number, timeout);

        // check the behavior
        verify(session).getDeviceHandle();
        verify(engine).canMakeCall();
        verify(session).isAlive();
        // check results
        assertThat(done).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
    }

    @Test
    public void shouldConnect_ConnectableIsAlive() {
        // preparing test data
        doReturn(true).when(engine).canBeConnected();
        engine.uses(device);
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 1;
        Sound sound = mock(Sound.class);
        H sharedHandle = (H) "mock()";
        TelephonyDevice sharedDevice = mock(TelephonyDevice.class);
        doReturn(true).when(sharedDevice).canBeConnected();
        PhoneCallSession<H> sharedSession = mock(PhoneCallSession.class);
        doReturn(sharedDevice).when(sharedSession).getDevice();
        doReturn(sharedHandle).when(sharedSession).getDeviceHandle();
        doReturn(true).when(sharedSession).isAlive();
        doReturn(true).when(sharedSession).hasNumber(number);
        factory.shareDevice(sharedSession, -1L);
        doReturn(true).when(provider).joinResources(sharedHandle, deviceHandle);

        // acting
        boolean done = engine.connect(session, number, timeout, sound);

        // check the behavior
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(engine).canBeConnected();
        verify(session).getDevice();
        verify(device).getFactory();
        verify(factory).findConnectableFor(number);
        verify(sharedSession, atLeastOnce()).isAlive();
        verify(provider).joinResources(sharedHandle, deviceHandle);
        verify(sharedSession).join(session);
        // check results
        assertThat(done).isTrue();
    }

    @Test
    public void terminate() {
    }
}