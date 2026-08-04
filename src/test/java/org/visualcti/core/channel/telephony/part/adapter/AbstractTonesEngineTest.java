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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceMalfunction;
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
        String deviceErrorReason = "Tone sending is failed.";
        ToneId id = ToneId.BEEP;
        float time = 0.5F;
        engine.uses(device);
        doReturn(true).when(session).isOpened();
        doReturn(true).when(session).isAlive();
        doReturn(true).when(provider).startToneSending(deviceHandle, id);
        executor.schedule(() -> session.operationComplete(Result.ERROR), 100, TimeUnit.MILLISECONDS);

        // acting
        Throwable error = assertThrows(Throwable.class, ()-> engine.playTone(session, id, time));

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
    public void inputDigits() {
    }

    @Test
    public void terminate() {
    }

    @Test
    public void getInputSymbols() {
    }
}