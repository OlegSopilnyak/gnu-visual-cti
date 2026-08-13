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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractTelephonyServiceProviderTest<H> {

    AbstractTelephonyServiceProvider<H> provider;

    @Before
    public void setUp() {
        provider = spy(new AbstractTelephonyServiceProvider() {});
    }

    @Test
    public void shouldOpenResource() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);

        // acting
        H resourceHandle = provider.openResource(resourceName);

        // check the behavior
        verify(provider).nativeResourceOpen(resourceName);
        // check results
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        assertThat(resourceHandle).isSameAs(handle);
    }

    @Test
    public void shouldNotOpenResource_NativeReturnedNull() throws IOException {
        // preparing test data
        String resourceName = "resourceName";

        // acting
        H resourceHandle = provider.openResource(resourceName);

        // check the behavior
        verify(provider).nativeResourceOpen(resourceName);
        // check results
        assertThat(provider.isOpened(resourceHandle)).isFalse();
        assertThat(resourceHandle).isNull();
    }

    @Test
    public void shouldCloseResource() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();

        // acting
        provider.closeResource(resourceHandle);

        // check the behavior
        verify(provider).nativeResourceClose(resourceHandle);
        // check results
        assertThat(provider.isOpened(resourceHandle)).isFalse();
    }

    @Test
    public void shouldNotCloseResource_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);

        // acting
        provider.closeResource(resourceHandle);

        // check the behavior
        verify(provider, never()).nativeResourceClose(any());
        // check results
        assertThat(provider.isOpened(resourceHandle)).isFalse();
    }

    @Test
    public void shouldDropCall() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        doReturn(false).when(provider).isHandsetOff(resourceHandle);
        doReturn(true).when(provider).nativeHandsetOff(resourceHandle);

        // acting
        boolean done = provider.handsetOff(resourceHandle);

        // check the behavior
        verify(provider).isHandsetOff(resourceHandle);
        verify(provider).nativeHandsetOff(resourceHandle);
        // check results
        assertThat(done).isTrue();
    }

    @Test
    public void shouldNotDropCall_NativeDoesNotReturn() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        doReturn(false).when(provider).isHandsetOff(resourceHandle);

        // acting
        boolean done = provider.handsetOff(resourceHandle);

        // check the behavior
        verify(provider).isHandsetOff(resourceHandle);
        verify(provider).nativeHandsetOff(resourceHandle);
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldNotDropCall_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);

        // acting
        boolean done = provider.handsetOff(resourceHandle);

        // check the behavior
        verify(provider, never()).nativeHandsetOff(any());
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldAnswerCall() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        doReturn(true).when(provider).nativeAnswerCall(resourceHandle);

        // acting
        boolean done = provider.answerCall(resourceHandle);

        // check the behavior
        verify(provider).nativeAnswerCall(resourceHandle);
        // check results
        assertThat(done).isTrue();
    }

    @Test
    public void shouldNotAnswerCall_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);

        // acting
        boolean done = provider.answerCall(resourceHandle);

        // check the behavior
        verify(provider, never()).nativeAnswerCall(any());
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldNotAnswerCall_NativeDoesNotReturn() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();

        // acting
        boolean done = provider.answerCall(resourceHandle);

        // check the behavior
        verify(provider).nativeAnswerCall(resourceHandle);
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldGetCallerID() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        PhoneCall.Number callerID = mock(PhoneCall.Number.class);
        doReturn(callerID).when(provider).nativeCallerID(resourceHandle);

        // acting
        PhoneCall.Number result = provider.getCallerID(resourceHandle);

        // check the behavior
        verify(provider).nativeCallerID(resourceHandle);
        // check results
        assertThat(result).isSameAs(callerID);
    }

    @Test
    public void shouldNotGetCallerID_NativeDoesNotReturn() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();

        // acting
        PhoneCall.Number result = provider.getCallerID(resourceHandle);

        // check the behavior
        verify(provider).nativeCallerID(resourceHandle);
        // check results
        assertThat(result).isSameAs(PhoneCall.Number.EMPTY);
    }

    @Test
    public void shouldNotGetCallerID_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isFalse();

        // acting
        PhoneCall.Number result = provider.getCallerID(resourceHandle);

        // check the behavior
        verify(provider, never()).nativeCallerID(any());
        // check results
        assertThat(result).isSameAs(PhoneCall.Number.EMPTY);
    }

    @Test
    public void shouldStartCalling() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 1;
        doReturn(true).when(provider).nativeStartCalling(resourceHandle, number, timeout);

        // acting
        boolean done = provider.startCalling(resourceHandle, number, timeout);

        // check the behavior
        verify(provider).nativeStartCalling(resourceHandle, number, timeout);
        // check results
        assertThat(done).isTrue();
    }

    @Test
    public void shouldNotStartCalling_NativeDoesNotReturn() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 1;

        // acting
        boolean done = provider.startCalling(resourceHandle, number, timeout);

        // check the behavior
        verify(provider).nativeStartCalling(resourceHandle, number, timeout);
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldNotStartCalling_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isFalse();
        PhoneCall.Number number = mock(PhoneCall.Number.class);
        int timeout = 1;

        // acting
        boolean done = provider.startCalling(resourceHandle, number, timeout);

        // check the behavior
        verify(provider, never()).nativeStartCalling(any(), any(PhoneCall.Number.class), anyInt());
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldGetEvent() {
        // preparing test data
        DeviceEvent<H> event = mock(DeviceEvent.class);
        int timeout = 1;
        doReturn(event).when(provider).nativeGetEvent(timeout);

        // acting
        Optional<DeviceEvent<H>> result = provider.getEvent(timeout);

        // check the behavior
        verify(provider).nativeGetEvent(timeout);
        // check results
        assertThat(result).contains(event);
    }

    @Test
    public void shouldNotGetEvent_NativeDoesNotReturn() {
        // preparing test data
        int timeout = 1;

        // acting
        Optional<DeviceEvent<H>> result = provider.getEvent(timeout);

        // check the behavior
        verify(provider).nativeGetEvent(timeout);
        // check results
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldEnableEvents() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();

        // acting
        provider.enableEvents(resourceHandle, eventType);

        // check the behavior
        verify(provider).nativeEnableEvents(resourceHandle, eventTypeName);
        // check results
        assertThat(provider.enabledEventTypes(resourceHandle)).contains(eventType);
    }

    @Test
    public void shouldNotEnableEvents_AlreadyThere() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();
        provider.enableEvents(resourceHandle, eventType);
        assertThat(provider.enabledEventTypes(resourceHandle)).hasSize(1).contains(eventType);
        reset(provider);

        // acting
        provider.enableEvents(resourceHandle, eventType);

        // check the behavior
        verify(provider, never()).nativeEnableEvents(any(), anyString());
        // check results
        assertThat(provider.enabledEventTypes(resourceHandle)).hasSize(1).contains(eventType);
    }

    @Test
    public void shouldNotEnableEvents_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isFalse();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();

        // acting
        provider.enableEvents(resourceHandle, eventType);

        // check the behavior
        verify(provider, never()).nativeEnableEvents(any(), anyString());
        // check results
        assertThat(provider.enabledEventTypes(resourceHandle)).isEmpty();
    }

    @Test
    public void shouldDisableEvents() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();
        provider.enableEvents(resourceHandle, eventType);
        assertThat(provider.enabledEventTypes(resourceHandle)).hasSize(1).contains(eventType);
        reset(provider);

        // acting
        provider.disableEvents(resourceHandle, eventType);

        // check the behavior
        verify(provider).nativeDisableEvents(resourceHandle, eventType.getValue());
        // check results
        assertThat(provider.enabledEventTypes(resourceHandle)).isEmpty();
    }

    @Test
    public void shouldNotDisableEvents_NoEnabledTypes() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();
        reset(provider);

        // acting
        provider.disableEvents(resourceHandle, eventType);

        // check the behavior
        verify(provider, never()).nativeDisableEvents(any(), anyString());
        // check results
    }

    @Test
    public void shouldNotDisableEvents_ResourceIsNotOpened() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isFalse();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();
        provider.enableEvents(resourceHandle, eventType);
        reset(provider);

        // acting
        provider.disableEvents(resourceHandle, eventType);

        // check the behavior
        verify(provider, never()).nativeDisableEvents(any(), anyString());
        // check results
    }

    @Test
    public void shouldDisableAllEvents() throws IOException {
        // preparing test data
        String eventTypeName = "eventTypeName";
        String resourceName = "resourceName";
        H handle = (H) "handle";
        doReturn(handle).when(provider).nativeResourceOpen(resourceName);
        H resourceHandle = provider.openResource(resourceName);
        assertThat(provider.isOpened(resourceHandle)).isTrue();
        OperationResultValue eventType = mock(OperationResultValue.class);
        doReturn(eventTypeName).when(eventType).getValue();
        provider.enableEvents(resourceHandle, eventType);
        assertThat(provider.enabledEventTypes(resourceHandle)).hasSize(1).contains(eventType);
        reset(provider);

        // acting
        provider.disableEvents(resourceHandle);

        // check the behavior
        verify(provider).disableEvents(resourceHandle, TelephonyServiceProvider.EventType.ALL);
        verify(provider).nativeDisableEvents(resourceHandle, "ALL");
        // check results
        assertThat(provider.enabledEventTypes(resourceHandle)).isEmpty();
    }
}