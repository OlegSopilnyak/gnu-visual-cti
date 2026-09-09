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
package org.visualcti.server.hardware.provider.javasound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;

@SuppressWarnings("unchecked")
public class SoundCardServiceProviderTest {

    SoundCardServiceProvider<SoundCardHandle> provider;

    @Before
    public void setUp() throws Exception {
        provider = spy(new SoundCardServiceProvider<>());
    }

    @Test
    public void shouldOpenResource() throws IOException {
        // preparing test data
        String deviceName = SoundCardServiceProvider.SOUND_DEVICE;

        // acting
        SoundCardHandle handle = provider.openResource(deviceName);

        // check the behavior
        verify(provider).nativeResourceOpen(deviceName);
        // check results
        assertThat(handle).isNotNull();
    }

    @Test
    public void shouldNotOpenResource_WrongResourceName() throws IOException {
        // preparing test data
        String deviceName = "SoundCardServiceProvider.SOUND_DEVICE";

        // acting
        SoundCardHandle handle = provider.openResource(deviceName);

        // check the behavior
        verify(provider).nativeResourceOpen(deviceName);
        // check results
        assertThat(handle).isNotNull();
        assertThat(handle.canUse()).isFalse();
    }

    @Test
    public void shouldNotOpenResource_AlreadyOpened() throws IOException {
        // preparing test data
        String deviceName = SoundCardServiceProvider.SOUND_DEVICE;
        provider.openResource(deviceName);
        reset(provider);

        // acting
        Exception error = assertThrows(Exception.class, () -> provider.openResource(deviceName));

        // check the behavior
        verify(provider).nativeResourceOpen(deviceName);
        // check results
        assertThat(error).isInstanceOf(IOException.class);
        assertThat(error.getMessage()).endsWith("is already opened");
    }

    @Test
    public void shouldCloseResource() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isOpened(handle)).isTrue();

        // acting
        provider.closeResource(handle);

        // check the behavior
        verify(provider).nativeResourceClose(handle);
        // check results
        assertThat(provider.isOpened(handle)).isFalse();
    }

    @Test
    public void shouldNotCloseResource_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isOpened(handle)).isTrue();
        provider.closeResource(handle);
        reset(provider);

        // acting
        provider.closeResource(handle);

        // check the behavior
        verify(provider, never()).nativeResourceClose(any());
        // check results
        assertThat(provider.isOpened(handle)).isFalse();
    }

    @Test
    public void shouldGetHandleByName() throws IOException {
        // preparing test data
        String deviceName = SoundCardServiceProvider.SOUND_DEVICE;
        SoundCardHandle handle = provider.openResource(deviceName);
        assertThat(provider.isOpened(handle)).isTrue();

        // acting
        Optional<SoundCardHandle> byName = provider.handleByName(deviceName);

        // check the behavior
        // check results
        assertThat(byName).contains(handle);
    }

    @Test
    public void shouldNotGetHandleByName_NotOpened() {
        // preparing test data
        String deviceName = SoundCardServiceProvider.SOUND_DEVICE;

        // acting
        Optional<SoundCardHandle> byName = provider.handleByName(deviceName);

        // check the behavior
        // check results
        assertThat(byName).isEmpty();
    }

    @Test
    public void shouldBeHandsetOff() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        reset(provider);

        // acting
        boolean handsetOff = provider.isHandsetOff(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(handsetOff).isTrue();
    }

    @Test
    public void shouldNotBeHandsetOff_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        provider.closeResource(handle);
        reset(provider);

        // acting
        boolean handsetOff = provider.isHandsetOff(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(handsetOff).isFalse();
    }

    @Test
    public void shouldNotBeHandsetOff_AnsweredCall() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        provider.answerCall(handle);
        reset(provider);

        // acting
        boolean handsetOff = provider.isHandsetOff(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(handsetOff).isFalse();
    }

    @Test
    public void shouldSetUpHandsetOff() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        provider.answerCall(handle);
        reset(provider);

        // acting
        boolean handsetOff = provider.handsetOff(handle);

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).isHandsetOff(handle);
        verify(provider).nativeHandsetOff(handle);
        // check results
        assertThat(handsetOff).isTrue();
    }

    @Test
    public void shouldNotSetUpHandsetOff_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        provider.closeResource(handle);
        reset(provider);

        // acting
        boolean handsetOff = provider.handsetOff(handle);

        // check the behavior
        verify(provider, never()).isOpened(any());
        // check results
        assertThat(handsetOff).isFalse();
    }

    @Test
    public void shouldNotSetUpHandsetOff_StillOff() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        reset(provider);

        // acting
        boolean handsetOff = provider.handsetOff(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).isHandsetOff(handle);
        verify(provider, never()).nativeHandsetOff(any());
        // check results
        assertThat(handsetOff).isTrue();
    }

    @Test
    public void shouldCanAnswerCall() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);

        // acting
        boolean canAcceptCall = provider.canAcceptCall(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(canAcceptCall).isTrue();
    }

    @Test
    public void shouldCannotAnswerCall_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        provider.closeResource(handle);

        // acting
        boolean canAcceptCall = provider.canAcceptCall(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(canAcceptCall).isFalse();
    }

    @Test
    public void shouldAnswerCall() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        reset(provider);

        // acting
        boolean answered = provider.answerCall(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).nativeAnswerCall(handle);
        // check results
        assertThat(answered).isTrue();
        assertThat(provider.isHandsetOff(handle)).isFalse();
    }

    @Test
    public void shouldNotAnswerCall_HandsetOn() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isHandsetOff(handle)).isTrue();
        assertThat(provider.isOpened(handle)).isTrue();
        provider.nativeAnswerCall(handle);
        reset(provider);

        // acting
        boolean answered = provider.answerCall(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).nativeAnswerCall(handle);
        // check results
        assertThat(answered).isFalse();
        assertThat(provider.isHandsetOff(handle)).isFalse();
    }

    @Test
    public void shouldSetupCallerID() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number callerId = mock(PhoneCall.Number.class);
        assertThat(provider.getCallerID(handle)).isSameAs(PhoneCall.Number.EMPTY);

        // acting
        provider.callerID(callerId);

        // check results
        assertThat(provider.getCallerID(handle)).isSameAs(callerId);
    }

    @Test
    public void shouldGetCallerID() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number mockedCallerId = mock(PhoneCall.Number.class);
        assertThat(provider.getCallerID(handle)).isSameAs(PhoneCall.Number.EMPTY);
        provider.callerID(mockedCallerId);
        reset(provider);

        // acting
        PhoneCall.Number callerID = provider.getCallerID(handle);

        // check the behavior
        verify(provider).nativeCallerID(handle);
        // check results
        assertThat(callerID).isSameAs(mockedCallerId);
    }

    @Test
    public void shouldNotGetCallerID_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number mockedCallerId = mock(PhoneCall.Number.class);
        assertThat(provider.getCallerID(handle)).isSameAs(PhoneCall.Number.EMPTY);
        provider.callerID(mockedCallerId);
        assertThat(provider.getCallerID(handle)).isSameAs(mockedCallerId);
        provider.closeResource(handle);
        reset(provider);

        // acting
        PhoneCall.Number callerID = provider.getCallerID(handle);

        // check the behavior
        verify(provider, never()).nativeCallerID(handle);
        // check results
        assertThat(callerID).isSameAs(PhoneCall.Number.EMPTY);
    }

    @Test
    public void shouldCanMakeCall() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);

        // acting
        boolean canMakeCall = provider.canMakeCall(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(canMakeCall).isTrue();
    }

    @Test
    public void shouldCannotMakeCall_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        provider.closeResource(handle);

        // acting
        boolean canMakeCall = provider.canMakeCall(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        // check results
        assertThat(canMakeCall).isFalse();
    }

    @Test
    public void shouldStartCalling() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number mockedPhoneNumber = mock(PhoneCall.Number.class);
        int timeout = 5;

        // acting
        boolean started = provider.startCalling(handle, mockedPhoneNumber, timeout);

        // check the behavior
        verify(provider).nativeStartCalling(handle, mockedPhoneNumber, timeout);
        // check results
        assertThat(started).isTrue();
    }

    @Test
    public void shouldNotStartCalling_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number mockedPhoneNumber = mock(PhoneCall.Number.class);
        int timeout = 5;
        provider.closeResource(handle);

        // acting
        boolean started = provider.startCalling(handle, mockedPhoneNumber, timeout);

        // check the behavior
        verify(provider, never()).nativeStartCalling(handle, mockedPhoneNumber, timeout);
        // check results
        assertThat(started).isFalse();
    }

    @Test
    public void shouldNotStartCalling_WrongNumber() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number mockedPhoneNumber = PhoneCall.Number.EMPTY;
        int timeout = 5;

        // acting
        boolean started = provider.startCalling(handle, mockedPhoneNumber, timeout);

        // check the behavior
        verify(provider).nativeStartCalling(handle, mockedPhoneNumber, timeout);
        // check results
        assertThat(started).isFalse();
    }

    @Test
    public void shouldNotStartCalling_WrongTimeout() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        PhoneCall.Number mockedPhoneNumber = mock(PhoneCall.Number.class);
        int timeout = -5;

        // acting
        boolean started = provider.startCalling(handle, mockedPhoneNumber, timeout);

        // check the behavior
        verify(provider).nativeStartCalling(handle, mockedPhoneNumber, timeout);
        // check results
        assertThat(started).isFalse();
    }

    @Test
    public void shouldGetEvent_FromNative() {
        // preparing test data
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(mocked).when(provider).allowedEvent(any(DeviceEvent.class));
        doReturn(mocked).when(provider).nativeGetEvent(anyLong());
        int timeout = 50;

        // acting
        Optional<DeviceEvent<SoundCardHandle>> event = provider.getEvent(timeout);

        // check the behavior
        verify(provider).nativeGetEvent(timeout / 2);
        // check results
        assertThat(event).isNotNull().contains(mocked);
    }

    @Test
    public void shouldGetEvent_FromEventsQueue() {
        // preparing test data
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(mocked).when(provider).allowedEvent(any(DeviceEvent.class));
        assertThat(provider.putEvent(mocked)).isTrue();
        int timeout = 50;

        // acting
        Optional<DeviceEvent<SoundCardHandle>> event = provider.getEvent(timeout);

        // check the behavior
        verify(provider, never()).nativeGetEvent(anyLong());
        // check results
        assertThat(event).isNotNull().contains(mocked);
    }

    @Test
    public void shouldNotGetEvent_NoEventsThere() {
        // preparing test data
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(mocked).when(provider).allowedEvent(any(DeviceEvent.class));
        int timeout = 50;

        // acting
        Optional<DeviceEvent<SoundCardHandle>> event = provider.getEvent(timeout);

        // check the behavior
        verify(provider).nativeGetEvent(timeout / 2);
        // check results
        assertThat(event).isNotNull().isEmpty();
    }

    @Test
    public void shouldPutEvent() {
        // preparing test data
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(mocked).when(provider).allowedEvent(any(DeviceEvent.class));
        int timeout = 50;
        assertThat(provider.getEvent(timeout)).isNotNull().isEmpty();

        // acting
        boolean put = provider.putEvent(mocked);

        // check the behavior
        // check results
        assertThat(put).isTrue();
        assertThat(provider.getEvent(timeout)).isNotNull().contains(mocked);
    }

    @Test
    public void shouldEnableEvents() throws IOException {
        // preparing test data
        OperationResultValue eventReason = Result.IO.EOF;
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        AbstractDeviceEvent<SoundCardHandle> event = spy(SoundCardEvent.of(DeviceEvent.Type.DEVICE_SPECIFIC));
        event.deviceHandle(handle).option(DeviceEvent.Option.REASON, eventReason);
        provider.putEvent(event);
        assertThat(provider.getEvent(50)).isNotNull().isEmpty();
        reset(event);

        // acting
        provider.enableEvents(handle, eventReason);
        provider.putEvent(event);
        Optional<DeviceEvent<SoundCardHandle>> receivedEvent = provider.getEvent(50);

        // check the behavior
        verify(provider).nativeEnableEvents(handle, eventReason.getValue());
        verify(event).getDeviceHandle();
        verify(event).getOption(DeviceEvent.Option.REASON);
        // check results
        assertThat(receivedEvent).isNotNull().contains(event);
    }

    @Test
    public void shouldDisableEvents() throws IOException {
        // preparing test data
        OperationResultValue eventReason = Result.IO.EOF;
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        AbstractDeviceEvent<SoundCardHandle> event = spy(SoundCardEvent.of(DeviceEvent.Type.DEVICE_SPECIFIC));
        event.deviceHandle(handle).option(DeviceEvent.Option.REASON, eventReason);
        provider.enableEvents(handle, eventReason);
        provider.putEvent(event);
        assertThat(provider.getEvent(50)).isNotNull().contains(event);
        reset(event);

        // acting
        provider.disableEvents(handle, eventReason);
        provider.putEvent(event);
        Optional<DeviceEvent<SoundCardHandle>> receivedEvent = provider.getEvent(50);

        // check the behavior
        verify(provider).nativeDisableEvents(handle, eventReason.getValue());
        verify(event).getDeviceHandle();
        verify(event).getOption(DeviceEvent.Option.REASON);
        // check results
        assertThat(receivedEvent).isNotNull().isEmpty();
    }

    @Test
    public void shouldRejectEvent() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SoundCardServiceProvider.SOUND_DEVICE);
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(handle).when(mocked).getDeviceHandle();

        // acting
        provider.reject(mocked);

        // check the behavior
        verify(mocked).getDeviceHandle();
        verify(provider).nativeEventRejected(handle, mocked);
        // check results
    }

    @Test
    public void shouldOpenFaxResource() throws IOException {
        // preparing test data
        String deviceName = SoundCardServiceProvider.SOUND_DEVICE;

        // acting
        SoundCardHandle handle = provider.openFaxResource(deviceName);

        // check the behavior
        verify(provider).nativeFaxResourceOpen(deviceName);
        // check results
        assertThat(handle).isNotNull();
    }

    @Test
    public void shouldCloseFaxResource() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SoundCardServiceProvider.SOUND_DEVICE);
        assertThat(provider.isOpened(handle)).isTrue();

        // acting
        provider.closeFaxResource(handle);

        // check the behavior
        verify(provider).nativeFaxResourceClose(handle);
        // check results
        assertThat(provider.isOpened(handle)).isFalse();
    }
}
