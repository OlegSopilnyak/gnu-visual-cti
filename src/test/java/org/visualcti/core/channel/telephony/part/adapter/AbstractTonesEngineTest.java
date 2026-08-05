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
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractTonesEngineTest<H> {
    ScheduledExecutorService executor;

    AbstractTonesEngine<H> engine;
    PhoneCallSession<H> session;
    TelephonyDevice<H, ?> device;
    TelephonyServiceProvider<H> provider;
    String deviceName = "device-name";
    H deviceHandle = (H) "handle";

    @Before
    public void setUp() throws Exception {
        provider = mock(TelephonyServiceProvider.class);
        device = mock(TelephonyDevice.class);
        doReturn(deviceName).when(device).getName();
        doReturn(provider).when(device).getProvider();
        session = spy(new PhoneCallSession(device, deviceHandle) {
        });
        engine = spy(new AbstractTonesEngine() {
        });
        executor = Executors.newScheduledThreadPool(2);
    }

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    @Test
    public void shouldDialDtmf() {
        // preparing test data
        String toDial = "123#76*5#";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();

        // acting
        engine.dial(session, toDial);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session, times(2)).getDevice();
        verify(device).dispatchEvent("Dialing [" + toDial + "]");
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(provider).dialingDtmf(deviceHandle, toDial);
        verify(device).dispatchEvent("Dialing is completed.");
        verify(session).setState(Device.State.IDLE);
        verify(session).isTerminated();
        verify(session).operationResult(Result.OK);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.OK);
    }

    @Test
    public void shouldDialDtmf_Terminated() {
        // preparing test data
        String toDial = "123#76*5#";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        doReturn(true).when(session).isTerminated();

        // acting
        engine.dial(session, toDial);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session, times(2)).getDevice();
        verify(device).dispatchEvent("Dialing [" + toDial + "]");
        verify(session).setState(TelephonyDevice.State.DIAL);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(provider).dialingDtmf(deviceHandle, toDial);
        verify(device).dispatchEvent("Dialing is completed.");
        verify(session).setState(Device.State.IDLE);
        verify(session).isTerminated();
        verify(session, never()).operationResult(Result.OK);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.NONE);
    }

    @Test
    public void shouldNotDial_NotOpened() {
        // preparing test data
        String toDial = "123#76*5#";
        doReturn(false).when(session).isOpened();

        // acting
        engine.dial(session, toDial);

        // check the behavior
        verify(session).isOpened();
        verify(session, never()).isAlive();
        verify(session, never()).getDevice();
        // check results
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldNotDial_Disconnected() {
        // preparing test data
        String toDial = "123#76*5#";

        // acting
        engine.dial(session, toDial);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session, never()).getDevice();
        // check results
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldPlayTone() throws InterruptedException {
        // preparing test data
        ToneId id = ToneId.BEEP;
        float time = 0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        doReturn(true).when(provider).startToneSending(deviceHandle, id);

        // acting
        engine.playTone(session, id, time);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Sending [" + id + "] tone for '" + time + "' seconds.");
        verify(session).setState(TelephonyDevice.State.TONE);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(provider).startToneSending(deviceHandle, id);
        verify(session).waitForOperationComplete(500L);
        verify(session).operationResult();
        verify(session).isTerminated();
        verify(device).dispatchEvent("Tone sending is completed.");
        verify(provider).stopToneSending(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(Result.OK);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.OK);
    }

    @Test
    public void shouldNotPlayTone_NotOpened() {
        // preparing test data
        ToneId id = ToneId.BEEP;
        float time = 0.5F;
        engine.uses(device);
        doReturn(false).when(session).isOpened();

        // acting
        engine.playTone(session, id, time);

        // check the behavior
        verify(session).isOpened();
        verify(session, never()).isAlive();
        verify(device, never()).dispatchEvent("Sending [" + id + "] tone for '" + time + "' seconds.");
        verify(session).setState(Device.State.ERROR);
        verify(session).operationResult(Result.ERROR);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldNotPlayTone_Disconnected() {
        // preparing test data
        ToneId id = ToneId.BEEP;
        float time = 0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();

        // acting
        engine.playTone(session, id, time);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device, never()).dispatchEvent("Sending [" + id + "] tone for '" + time + "' seconds.");
        verify(session).setState(Device.State.ERROR);
        verify(session).operationResult(Result.ERROR);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldNotPlayTone_DoesNotStartToneSending() throws InterruptedException {
        // preparing test data
        String deviceErrorReason = "Start tone sending is failed.";
        ToneId id = ToneId.BEEP;
        float time = -0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();

        // acting
        engine.playTone(session, id, time);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Sending [" + id + "] tone for '" + time + "' seconds.");
        verify(session).setState(TelephonyDevice.State.TONE);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(provider).startToneSending(deviceHandle, id);
        verify(engine).onDeviceError(session, deviceErrorReason, false);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(deviceErrorReason);
        verify(session).operationResult(Result.ERROR);
        verify(session, never()).waitForOperationComplete(anyLong());
        // check results
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldNotPlayTone_WrongTimeoutValue() throws InterruptedException {
        // preparing test data
        String deviceErrorReason = "Tone sending time is too short.";
        ToneId id = ToneId.BEEP;
        float time = -0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        doReturn(true).when(provider).startToneSending(deviceHandle, id);

        // acting
        engine.playTone(session, id, time);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Sending [" + id + "] tone for '" + time + "' seconds.");
        verify(session).setState(TelephonyDevice.State.TONE);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(provider).startToneSending(deviceHandle, id);
        verify(provider).stopToneSending(deviceHandle);
        verify(engine).onDeviceError(session, deviceErrorReason, false);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(deviceErrorReason);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(Result.ERROR);
        verify(session, never()).waitForOperationComplete(anyLong());
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldNotPlayTone_HardwareError() throws InterruptedException {
        // preparing test data
        String deviceErrorReason = "Tone sending is failed.";
        ToneId id = ToneId.BEEP;
        float time = 0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        doReturn(true).when(provider).startToneSending(deviceHandle, id);
        executor.schedule(() -> session.operationComplete(Result.ERROR), 100, TimeUnit.MILLISECONDS);

        // acting
        Throwable error = assertThrows(Throwable.class, () -> engine.playTone(session, id, time));

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Sending [" + id + "] tone for '" + time + "' seconds.");
        verify(session).setState(TelephonyDevice.State.TONE);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(provider).startToneSending(deviceHandle, id);
        verify(session).waitForOperationComplete(500L);
        verify(session).operationResult();
        verify(provider).stopToneSending(deviceHandle);
        verify(engine).onDeviceError(session, deviceErrorReason);
        verify(engine).onDeviceError(session, deviceErrorReason, true);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(deviceErrorReason);
        verify(session, never()).isTerminated();
        // check results
        assertThat(error).isInstanceOf(DeviceMalfunction.class);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldInputDigits_TimedOut() throws InterruptedException {
        // preparing test data
        int digitsCount = 2;
        int oneSymbolTimeout = 100;
        String terminationSymbolsMask = "";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();

        // acting
        OperationResultValue result = engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Getting the user input.");
        verify(session).setState(TelephonyDevice.State.GTDIG);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).waitForOperationComplete(oneSymbolTimeout);
        verify(session, atLeastOnce()).operationResult();
        verify(session).isTerminated();
        verify(device).dispatchEvent("User input getting is completed.");
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.<String>parameter(Device.Parameter.USER_INPUT)).isEmpty();
        assertThat(result).isSameAs(Result.TIMEOUT);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.TIMEOUT);
    }

    @Test
    public void shouldInputDigits_UserInputPartly() throws InterruptedException {
        // preparing test data
        int digitsCount = 2;
        int oneSymbolTimeout = 500;
        String terminationSymbolsMask = "#";
        String userInput = "1";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        executor.schedule(() -> {
            session.parameter(Device.Parameter.USER_INPUT, userInput);
            session.operationComplete(Result.IO.DTMF);
        }, 100, TimeUnit.MILLISECONDS);

        // acting
        OperationResultValue result = engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Getting the user input.");
        verify(session).setState(TelephonyDevice.State.GTDIG);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(session, atLeastOnce()).waitForOperationComplete(oneSymbolTimeout);
        verify(session, atLeastOnce()).operationResult();
        verify(session, atLeastOnce()).isTerminated();
        verify(device).dispatchEvent("User input getting is completed.");
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.<String>parameter(Device.Parameter.USER_INPUT)).isEqualTo(userInput);
        assertThat(result).isSameAs(Result.TIMEOUT);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.TIMEOUT);
    }

    @Test
    public void shouldInputDigits_UserInputExpectedQuantity() throws InterruptedException {
        // preparing test data
        final int digitsCount = 2;
        final int oneSymbolTimeout = 500;
        final String terminationSymbolsMask = "#";
        String userInput = "19";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        executor.schedule(() -> {
            session.parameter(Device.Parameter.USER_INPUT, userInput);
            session.operationComplete(Result.IO.DTMF);
        }, 100, TimeUnit.MILLISECONDS);

        // acting
        OperationResultValue result = engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Getting the user input.");
        verify(session).setState(TelephonyDevice.State.GTDIG);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(session, atLeastOnce()).waitForOperationComplete(oneSymbolTimeout);
        verify(session, atLeastOnce()).operationResult();
        verify(session, atLeastOnce()).isTerminated();
        verify(device).dispatchEvent("User input getting is completed.");
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.<String>parameter(Device.Parameter.USER_INPUT)).isEqualTo(userInput);
        assertThat(result).isSameAs(Result.IO.DTMF);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.IO.DTMF);
    }

    @Test
    public void shouldInputDigits_UserInputTerminatedBySymbolsMask() throws InterruptedException {
        // preparing test data
        final int digitsCount = 2;
        final int oneSymbolTimeout = 500;
        final String terminationSymbolsMask = "#,0";
        String userInput = "1#";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        executor.schedule(() -> {
            session.parameter(Device.Parameter.USER_INPUT, userInput);
            session.operationComplete(Result.IO.DTMF);
        }, 100, TimeUnit.MILLISECONDS);

        // acting
        OperationResultValue result = engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Getting the user input.");
        verify(session).setState(TelephonyDevice.State.GTDIG);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(session, atLeastOnce()).waitForOperationComplete(oneSymbolTimeout);
        verify(session, atLeastOnce()).operationResult();
        verify(session, atLeastOnce()).isTerminated();
        verify(device).dispatchEvent("User input getting is completed.");
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.<String>parameter(Device.Parameter.USER_INPUT))
                .isEqualTo(userInput.substring(0, userInput.length() - 1));
        assertThat(result).isSameAs(Result.IO.DTMF);
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.IO.DTMF);
    }

    @Test
    public void shouldDoesNotInputDigits_NotOpened() {
        // preparing test data
        int digitsCount = 2;
        int oneSymbolTimeout = 100;
        String terminationSymbolsMask = "";
        doReturn(false).when(session).isOpened();

        // acting
        OperationResultValue result = engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask);

        // check the behavior
        verify(session).isOpened();
        verify(session, never()).isAlive();
        verify(session).operationResult(Result.ERROR);
        verify(session).setState(Device.State.ERROR);
        // check results
        assertThat(session.parameterOrDefault(Device.Parameter.USER_INPUT, "")).isEmpty();
        assertThat(result).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldDoesNotInputDigits_Disconnected() {
        // preparing test data
        int digitsCount = 2;
        int oneSymbolTimeout = 100;
        String terminationSymbolsMask = "";
        doReturn(true).when(session).isOpened();

        // acting
        OperationResultValue result = engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session).operationResult(Result.ERROR);
        verify(session).setState(Device.State.ERROR);
        // check results
        assertThat(session.parameterOrDefault(Device.Parameter.USER_INPUT, "")).isEmpty();
        assertThat(result).isSameAs(Result.ERROR);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
    }

    @Test
    public void shouldDoesNotInputDigits_HardwareError() throws InterruptedException {
        // preparing test data
        int digitsCount = 2;
        int oneSymbolTimeout = 1000;
        String terminationSymbolsMask = "";
        String deviceErrorReason = "Getting the user input is failed.";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        executor.schedule(() -> session.operationComplete(Result.ERROR), 100, TimeUnit.MILLISECONDS);

        // acting
        Throwable error = assertThrows(Throwable.class,
                () -> engine.inputDigits(session, digitsCount, oneSymbolTimeout, terminationSymbolsMask)
        );

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(device).dispatchEvent("Getting the user input.");
        verify(session).setState(TelephonyDevice.State.GTDIG);
        verify(device).getProvider();
        verify(session).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).waitForOperationComplete(oneSymbolTimeout);
        verify(session, atLeastOnce()).operationResult();
        verify(engine).onDeviceError(session, deviceErrorReason);
        verify(engine).onDeviceError(session, deviceErrorReason, true);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(deviceErrorReason);
        verify(session, never()).isTerminated();
        // check results
        assertThat(error).isInstanceOf(DeviceMalfunction.class);
        assertThat(session.getState()).isSameAs(Device.State.ERROR);
        assertThat(session.operationResult()).isSameAs(Result.ERROR);
        assertThat(session.<String>parameter(Device.Parameter.USER_INPUT)).isEmpty();
    }

    @Test
    public void shouldGetInputSymbols_EmptyBuffer() {
        // preparing test data
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();

        // acting
        String result = engine.getInputSymbols(session);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session).parameterOrDefault(Device.Parameter.USER_INPUT, "");
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        // check results
        assertThat(result).isEmpty();
        assertThat(session.<String>parameter(Device.Parameter.USER_INPUT)).isEmpty();
    }

    @Test
    public void shouldGetInputSymbols_WithUserUnput() {
        // preparing test data
        String userInput = "1#";
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        session.parameter(Device.Parameter.USER_INPUT, userInput);

        // acting
        String result = engine.getInputSymbols(session);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session).parameterOrDefault(Device.Parameter.USER_INPUT, "");
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        // check results
        assertThat(result).isEqualTo(userInput);
        assertThat(engine.getInputSymbols(session)).isEmpty();
    }

    @Test
    public void shouldNotGetInputSymbols_NotOpened() {
        // preparing test data
        String userInput = "1#";
        doReturn(false).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        session.parameter(Device.Parameter.USER_INPUT, userInput);

        // acting
        String result = engine.getInputSymbols(session);

        // check the behavior
        verify(session).isOpened();
        verify(session, never()).isAlive();
        verify(session, never()).parameterOrDefault(any(Device.ParameterName.class), anyString());
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        // check results
        assertThat(result).isEmpty();
        assertThat(engine.getInputSymbols(session)).isEmpty();
    }

    @Test
    public void shouldNotGetInputSymbols_Disconnected() {
        // preparing test data
        String userInput = "1#";
        doReturn(true).when(session).isOpened();
        session.parameter(Device.Parameter.USER_INPUT, userInput);

        // acting
        String result = engine.getInputSymbols(session);

        // check the behavior
        verify(session).isOpened();
        verify(session).isAlive();
        verify(session, never()).parameterOrDefault(any(Device.ParameterName.class), anyString());
        verify(session).parameter(Device.Parameter.USER_INPUT, "");
        // check results
        assertThat(result).isEmpty();
        assertThat(engine.getInputSymbols(session)).isEmpty();
    }

    @Test
    public void shouldTerminatePlayTone() throws IOException {
        // preparing test data
        ToneId id = ToneId.BEEP;
        float time = 0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        doReturn(true).when(provider).startToneSending(deviceHandle, id);
        executor.execute(() -> engine.playTone(session, id, time));
        await().until(() -> session.operationIsActive());

        // acting
        engine.terminate(session);
        await().until(() -> session.getState() == Device.State.IDLE);

        // check the behavior
        verify(session).terminate();
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.TERMINATED);
    }

    @Test
    public void shouldTerminateInputDigits() throws IOException {
        // preparing test data
        int digitsCount = 2;
        int oneSymbolTimeout = 500;
        String terminationSymbolsMask = "#";
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        executor.execute(() -> engine.inputDigits(session, digitsCount,oneSymbolTimeout, terminationSymbolsMask));
        await().until(() -> session.operationIsActive());

        // acting
        engine.terminate(session);
        await().until(() -> session.getState() == Device.State.IDLE);

        // check the behavior
        verify(session).terminate();
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(session.operationResult()).isSameAs(Result.TERMINATED);
    }
}