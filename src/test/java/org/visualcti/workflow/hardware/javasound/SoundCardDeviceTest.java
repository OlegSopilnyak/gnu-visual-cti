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
package org.visualcti.workflow.hardware.javasound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TelephonyDevicePart;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractCallsPortEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractFaxMachineEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractMultimediaEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractTonesEngine;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SoundCardDeviceTest<H extends SoundCardHandle> {
    CallsPortEngine<H> calls;
    TonesEngine<H> tones;
    MultimediaEngine<H> media;
    FaxMachineEngine<H> faxes;

    Executor deviceEventExecutor;
    ScheduledExecutorService shadowExecutor;
    H deviceHandle;
    SoundCardServiceProvider<H> provider;
    SoundCardDevicesFactory<H, ?> factory;
    SoundCardDevice<H, ?> device;
    PhoneCallSession<H> session;

    @Before
    public void setUp() throws Exception {
        deviceEventExecutor = mock(Executor.class);
        shadowExecutor = Executors.newScheduledThreadPool(2);
        doAnswer(invocation -> {
            shadowExecutor.execute(invocation.getArgument(0, Runnable.class));
            return null;
        }).when(deviceEventExecutor).execute(any(Runnable.class));
        provider = spy(new SoundCardServiceProvider<>(shadowExecutor));
        factory = spy(new SoundCardDevicesFactory<>(deviceEventExecutor, provider));
        calls = spy(new AbstractCallsPortEngine() {
        });
        tones = spy(new AbstractTonesEngine() {
        });
        media = spy(new AbstractMultimediaEngine() {
        });
        faxes = spy(new AbstractFaxMachineEngine() {
        });
        device = spy(new SoundCardDevice<>(SoundCardServiceProvider.SOUND_DEVICE, provider, calls, tones, media, faxes));
        factory.addDevice(device);
        session = (PhoneCallSession<H>) spy(device.startSession());
        deviceHandle = session.getDeviceHandle();
        reset(device, session, factory, provider);
    }

    @Test
    public void shouldStartSession() throws IOException {
        // preparing test data
        device.close();
        reset(device);

        // acting
        DeviceActivitySession<H> started = spy(device.startSession());

        // check the behavior
        verify(device).getProvider();
        verify(provider).openResource(device.getName());
        H handle = started.getDeviceHandle();
        verify(device).findInitiatedSession();
        verify(device, never()).detachAndClose(any(DeviceActivitySession.class));
        verify(device).fillingDeviceSpecific(handle);
        verify(device).createSessionFor(handle);
        ArgumentCaptor<PhoneCallSession<H>> sessionCaptor = ArgumentCaptor.forClass(PhoneCallSession.class);
        verify(device).stateChangedFor(sessionCaptor.capture());
        PhoneCallSession<H> changedSession = sessionCaptor.getValue();
        verify(started).getDeviceHandle();
        verify(device).findSessionByHandle(handle);
        verify(faxes).open(changedSession);
        verify(faxes).isOpened(changedSession);
        verify(provider).enableEvents(handle, Result.CALL.RINGS);
        verify(device).canBeConnected();
        verify(factory, never()).shareDevice(handle);
        // check results
        assertThat(started).isNotNull();
        assertThat(started.isOpened()).isTrue();
        assertThat(started.isAlive()).isFalse();
        assertThat(changedSession).isEqualTo(started);
        assertThat(device.isInvalidHandle(started.getDeviceHandle())).isFalse();
    }

    @Test
    public void shouldDoNotStartSession_ResourceIsOpened() throws IOException {
        // preparing test data
        assertThat(device.isOpened()).isTrue();
        assertThat(session.isOpened()).isTrue();
        reset(device, session);

        // acting
        Exception e = assertThrows(Exception.class, () -> device.startSession());

        // check the behavior
        verify(device).serviceProvider();
        verify(provider).openResource(device.getName());
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).endsWith("is already opened");
    }

    @Test
    public void shouldDetachAndCloseDeviceSession() throws IOException {
        // preparing test data
        assertThat(session.isOpened()).isTrue();
        reset(session);

        // acting
        device.detachAndClose(session);

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device, atLeastOnce()).getFactory();
        verify(factory).unShareDevice(deviceHandle);
        verify(provider).disableEvents(deviceHandle);
        verify(session).terminate();
        verify(device, atLeastOnce()).findSessionByHandle(deviceHandle);
        verify(provider, never()).closeResource(deviceHandle);
        verify(session).close();
        // check results
        assertThat(session.isOpened()).isFalse();
        assertThat(session.getDevice()).isNull();
    }

    @Test
    public void shouldOpenDevice() throws IOException {
        // preparing test data
        device.close();
        assertThat(device.isOpened()).isFalse();
        assertThat(session.isOpened()).isFalse();
        reset(device);

        // acting
        device.open();

        // check the behavior
        verify(device).startSession();
        // check results
        assertThat(device.isOpened()).isTrue();
    }

    @Test
    public void shouldCloseDevice() throws IOException {
        // preparing test data
        assertThat(device.isOpened()).isTrue();
        assertThat(session.isOpened()).isTrue();
        reset(device);

        // acting
        device.close();

        // check the behavior
        verify(device, atLeastOnce()).sessions();
        ArgumentCaptor<DeviceActivitySession<H>> sessionCaptor = ArgumentCaptor.forClass(DeviceActivitySession.class);
        verify(device).detachAndClose(sessionCaptor.capture());
        // check results
        assertThat(device.isOpened()).isFalse();
        DeviceActivitySession<H> detached = sessionCaptor.getValue();
        assertThat(detached.isOpened()).isFalse();
        assertThat(detached.getDevice()).isNull();
    }

    @Test
    public void shouldCallDevicePartUses() {
        // preparing test data

        // acting
        TelephonyDevicePart<H> part = faxes.uses(device);

        // check the behavior
        // check results
        assertThat(part).isSameAs(faxes);
    }

    @Test
    public void shouldDisconnectCall() {
        // preparing test data
        session.alive(true);
        assertThat(device.isOpened()).isTrue();
        assertThat(session.isDisconnected()).isFalse();
        reset(device, session);

        // acting
        device.disconnect(session);

        // check the behavior
        verify(calls).disconnect(session);
        verify(provider).handsetOff(deviceHandle);
        verify(session).detachAll();
        verify(session).accept(any(DeviceEvent.class));
        verify(tones, never()).disconnect(any(PhoneCallSession.class));
        verify(media, never()).disconnect(any(PhoneCallSession.class));
        verify(faxes, never()).disconnect(any(PhoneCallSession.class));
        // check results
        assertThat(session.isDisconnected()).isTrue();
    }

    @Test
    public void shouldTerminateDeviceParts() throws IOException {
        // preparing test data
        assertThat(device.isOpened()).isTrue();
        assertThat(session.isTerminated()).isFalse();
        reset(device, session);

        // acting
        device.terminate(session);

        // check the behavior
        verify(calls).terminate(session);
        verify(tones).terminate(session);
        verify(media).terminate(session);
        verify(faxes).terminate(session);
        // check results
        assertThat(session.isTerminated()).isTrue();
    }
}