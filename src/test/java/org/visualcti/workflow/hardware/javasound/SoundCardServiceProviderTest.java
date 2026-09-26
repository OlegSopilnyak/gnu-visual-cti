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
import static org.awaitility.Awaitility.await;
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
import static org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider.SOUND_DEVICE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractEventListenersHub;
import org.visualcti.core.channel.device.adapter.AbstractEventProcessor;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.TelephonyTone;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.media.Audio;

@SuppressWarnings("unchecked")
public class SoundCardServiceProviderTest {
    static final Device.ParameterName ALLOWED_CODECS = MultimediaEngine.Parameter.ALLOWED_CODECS;

    AbstractEventProcessor<SoundCardHandle> eventProcessor;
    SoundCardServiceProvider<SoundCardHandle> provider;
    ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    ScheduledExecutorService shadowScheduler;

    @Before
    public void setUp() throws IOException {
        provider = spy(new SoundCardServiceProvider<>(scheduler));
        shadowScheduler = Executors.newScheduledThreadPool(10);
        eventProcessor = new EventsProcessor(shadowScheduler, provider);
        eventProcessor.Start();
    }

    @After
    public void tearDown() throws IOException {
        eventProcessor.Stop();
        await().until(() -> eventProcessor.isStopped());
        shadowScheduler.shutdown();
        shadowScheduler = null;
    }

    @Test
    public void shouldOpenResource() throws IOException {
        // preparing test data
        String deviceName = SOUND_DEVICE;

        // acting
        SoundCardHandle handle = provider.openResource(deviceName);

        // check the behavior
        verify(provider).nativeResourceOpen(deviceName);
        verify(provider).availableFormatsFor(handle);
        // check results
        assertThat(handle).isNotNull();
        assertThat(handle.canUse()).isTrue();
        Optional<ConfigurationParameter> codecs = provider.findResourceParameter(handle, ALLOWED_CODECS);
        assertThat(codecs).isPresent();
        assertThat(codecs.get().<List<?>>getValue()).isNotEmpty();
    }

    @Test
    public void shouldNotOpenResource_WrongResourceName() throws IOException {
        // preparing test data
        String deviceName = "SoundCardServiceProvider.SOUND_DEVICE";

        // acting
        SoundCardHandle handle = provider.openResource(deviceName);

        // check the behavior
        verify(provider).nativeResourceOpen(deviceName);
        verify(provider).availableFormatsFor(any());
        // check results
        assertThat(handle).isNotNull();
        assertThat(handle.canUse()).isFalse();
        assertThat(provider.findResourceParameter(handle, ALLOWED_CODECS)).isEmpty();
    }

    @Test
    public void shouldNotOpenResource_AlreadyOpened() throws IOException {
        // preparing test data
        String deviceName = SOUND_DEVICE;
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        String deviceName = SOUND_DEVICE;
        SoundCardHandle handle = provider.openResource(deviceName);
        assertThat(provider.isOpened(handle)).isTrue();

        // acting
        Optional<SoundCardHandle> byName = provider.openedHandleByName(deviceName);

        // check the behavior
        // check results
        assertThat(byName).contains(handle);
    }

    @Test
    public void shouldNotGetHandleByName_NotOpened() {
        // preparing test data

        // acting
        Optional<SoundCardHandle> byName = provider.openedHandleByName(SOUND_DEVICE);

        // check the behavior
        // check results
        assertThat(byName).isEmpty();
    }

    @Test
    public void shouldBeHandsetOff() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);

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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);

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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
    public void shouldGetEvent_FromNative() throws IOException {
        // preparing test data
        eventProcessor.Stop();
        await().until(() -> eventProcessor.isStopped());
        provider.enableEventsGetting();
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
    public void shouldGetEvent_FromEventsQueue() throws IOException {
        // preparing test data
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(mocked).when(provider).allowedEvent(any(DeviceEvent.class));
        eventProcessor.Stop();
        await().until(() -> eventProcessor.isStopped());
        provider.enableEventsGetting();
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
    public void shouldPutEvent() throws IOException {
        // preparing test data
        DeviceEvent<SoundCardHandle> mocked = mock(DeviceEvent.class);
        doReturn(mocked).when(provider).allowedEvent(any(DeviceEvent.class));
        eventProcessor.Stop();
        await().until(() -> eventProcessor.isStopped());
        provider.enableEventsGetting();
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        AbstractDeviceEvent<SoundCardHandle> event = spy(SoundCardEvent.of(DeviceEvent.Type.DEVICE_SPECIFIC));
        event.deviceHandle(handle).option(DeviceEvent.Option.REASON, eventReason);
        eventProcessor.Stop();
        await().until(() -> eventProcessor.isStopped());
        provider.enableEventsGetting();
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        AbstractDeviceEvent<SoundCardHandle> event = spy(SoundCardEvent.of(DeviceEvent.Type.DEVICE_SPECIFIC));
        event.deviceHandle(handle).option(DeviceEvent.Option.REASON, eventReason);
        eventProcessor.Stop();
        await().until(() -> eventProcessor.isStopped());
        provider.enableEventsGetting();
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
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
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
        String deviceName = SOUND_DEVICE;

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
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        assertThat(provider.isOpened(handle)).isTrue();

        // acting
        provider.closeFaxResource(handle);

        // check the behavior
        verify(provider).nativeFaxResourceClose(handle);
        // check results
        assertThat(provider.isOpened(handle)).isFalse();
    }

    @Test
    public void shouldStartFaxReceiving() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";
        File faxFile = new File(file);
        assertThat(faxFile.createNewFile()).isTrue();
        faxFile.deleteOnExit();
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));

        // acting
        boolean starts = provider.startFaxReceiving(handle, file, false);
        assertThat(provider.hasShadowActivity(handle)).isTrue();
        await().until(() -> !provider.hasShadowActivity(handle));

        // check the behavior
        verify(provider).nativeStartFaxReceiving(handle, file, false);
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(starts).isTrue();
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.MALFUNCTION);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.FAX.COMPATIBILITY);
        assertThat(faxFile.delete()).isTrue();
    }

    @Test
    public void shouldDoNotStartFaxReceiving_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";
        provider.closeFaxResource(handle);

        // acting
        boolean starts = provider.startFaxReceiving(handle, file, false);

        // check the behavior
        verify(provider).nativeStartFaxReceiving(handle, file, false);
        verify(provider).isOpened(handle);
        verify(scheduler, never()).schedule(any(Callable.class), anyLong(), any(TimeUnit.class));
        verify(provider, never()).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(starts).isFalse();
    }

    @Test
    public void shouldDoNotStartFaxReceiving_WrongFileName() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";

        // acting
        boolean starts = provider.startFaxReceiving(handle, file, false);

        // check the behavior
        verify(provider).nativeStartFaxReceiving(handle, file, false);
        verify(provider).isOpened(handle);
        verify(scheduler, never()).schedule(any(Callable.class), anyLong(), any(TimeUnit.class));
        verify(provider, never()).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(starts).isFalse();
    }

    @Test
    public void shouldStopFaxReceiving() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";
        File faxFile = new File(file);
        assertThat(faxFile.createNewFile()).isTrue();
        faxFile.deleteOnExit();
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return mock(ScheduledFuture.class);
        }).when(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
        assertThat(provider.startFaxReceiving(handle, file, false)).isTrue();
        assertThat(provider.hasShadowActivity(handle)).isTrue();
        reset(provider);

        // acting
        provider.stopFaxReceiving(handle);

        // check the behavior
        verify(provider).nativeStopFaxReceiving(handle);
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).hasShadowActivity(handle);
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.IO.EOF);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(faxFile.delete()).isTrue();
    }

    @Test
    public void shouldDoNotStopFaxReceiving_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        provider.closeFaxResource(handle);
        String file = "fax-file";
        File faxFile = new File(file);
        assertThat(faxFile.createNewFile()).isTrue();
        faxFile.deleteOnExit();
        assertThat(provider.startFaxReceiving(handle, file, false)).isFalse();
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        reset(provider);

        // acting
        provider.stopFaxReceiving(handle);

        // check the behavior
        verify(provider).nativeStopFaxReceiving(handle);
        verify(provider).isOpened(handle);
        verify(provider, never()).hasShadowActivity(any());
        // check results
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(faxFile.delete()).isTrue();
    }

    @Test
    public void shouldDoNotStopFaxReceiving_NotStarted() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        reset(provider);

        // acting
        provider.stopFaxReceiving(handle);

        // check the behavior
        verify(provider).nativeStopFaxReceiving(handle);
        verify(provider).isOpened(handle);
        verify(provider).hasShadowActivity(handle);
        // check results
        assertThat(provider.hasShadowActivity(handle)).isFalse();
    }

    @Test
    public void shouldStartFaxTransmitting() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        File faxFile = Files.createTempFile("fax-document", ".fax").toFile();
        assertThat(faxFile).isNotNull();
        faxFile.deleteOnExit();
        String file = faxFile.getCanonicalPath();
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));

        // acting
        boolean starts = provider.startFaxTransmitting(handle, file, false, true, true, 1, 10);
        assertThat(provider.hasShadowActivity(handle)).isTrue();
        await().until(() -> !provider.hasShadowActivity(handle));

        // check the behavior
        verify(provider).nativeStartFaxTransmitting(handle, file, false, true, true, 1, 10);
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(starts).isTrue();
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.MALFUNCTION);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.FAX.COMPATIBILITY);
        assertThat(faxFile.delete()).isTrue();
    }

    @Test
    public void shouldDoNotStartFaxTransmitting_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";
        provider.closeFaxResource(handle);

        // acting
        boolean starts = provider.startFaxTransmitting(handle, file, false, true, true, 1, 10);

        // check the behavior
        verify(provider).nativeStartFaxTransmitting(handle, file, false, true, true, 1, 10);
        verify(provider).isOpened(handle);
        verify(scheduler, never()).schedule(any(Callable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
        verify(provider, never()).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(starts).isFalse();
    }

    @Test
    public void shouldDoNotStartFaxTransmitting_WrongFilename() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";

        // acting
        boolean starts = provider.startFaxTransmitting(handle, file, false, true, true, 1, 10);

        // check the behavior
        verify(provider).nativeStartFaxTransmitting(handle, file, false, true, true, 1, 10);
        verify(provider).isOpened(handle);
        verify(scheduler, never()).schedule(any(Callable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
        verify(provider, never()).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(starts).isFalse();
    }

    @Test
    public void shouldStopFaxTransmitting() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        String file = "fax-file";
        File faxFile = new File(file);
        assertThat(faxFile.createNewFile()).isTrue();
        faxFile.deleteOnExit();
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return mock(ScheduledFuture.class);
        }).when(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
        assertThat(provider.startFaxTransmitting(handle, file, false, true, true, 1, 10)).isTrue();
        assertThat(provider.hasShadowActivity(handle)).isTrue();
        reset(provider);

        // acting
        provider.stopFaxTransmitting(handle);

        // check the behavior
        verify(provider).nativeStopFaxTransmitting(handle);
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).hasShadowActivity(handle);
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.IO.EOF);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(faxFile.delete()).isTrue();
    }

    @Test
    public void shouldDoNotStopFaxTransmitting_Closed() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        provider.closeFaxResource(handle);
        String file = "fax-file";
        File faxFile = new File(file);
        assertThat(faxFile.createNewFile()).isTrue();
        faxFile.deleteOnExit();
        assertThat(provider.startFaxTransmitting(handle, file, false, true, true, 1, 10)).isFalse();
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        reset(provider);

        // acting
        provider.stopFaxTransmitting(handle);

        // check the behavior
        verify(provider).nativeStopFaxTransmitting(handle);
        verify(provider).isOpened(handle);
        verify(provider, never()).hasShadowActivity(any());
        // check results
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(faxFile.delete()).isTrue();
    }

    @Test
    public void shouldDoNotStopFaxTransmitting_NotStarted() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openFaxResource(SOUND_DEVICE);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        reset(provider);

        // acting
        provider.stopFaxTransmitting(handle);

        // check the behavior
        verify(provider).nativeStopFaxTransmitting(handle);
        verify(provider).isOpened(handle);
        verify(provider).hasShadowActivity(handle);
        // check results
        assertThat(provider.hasShadowActivity(handle)).isFalse();
    }

    @Test
    public void shouldStartAudioPlaying() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        Audio format = Audio.LINEAR;
        InputStream in = provider.getClass().getResourceAsStream("/VM/prompts/MAIN_MENU1.WAV");
        assertThat(in).isNotNull();
        File tempFile = File.createTempFile("audio", ".WAV");
        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        tempFile.deleteOnExit();
        int timeout = 1;
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        // acting
        boolean started = provider.startAudioPlaying(handle, tempFile.getCanonicalPath(), format, timeout);
        await().until(handle::isOperationInProgress);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.TIMEOUT);
        assertThat(started).isTrue();
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(handle.isSourceActive()).isFalse();
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldStopAudioPlaying() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        Audio format = Audio.LINEAR;
        InputStream in = provider.getClass().getResourceAsStream("/VM/prompts/MAIN_MENU1.WAV");
        assertThat(in).isNotNull();
        File tempFile = File.createTempFile("audio", ".WAV");
        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        tempFile.deleteOnExit();
        int timeout = 10;
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        assertThat(provider.startAudioPlaying(handle, tempFile.getCanonicalPath(), format, timeout)).isTrue();
        await().until(handle::isOperationInProgress);
        reset(provider);

        // acting
        provider.stopAudioPlaying(handle);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.IO.EOF);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(handle.isSourceActive()).isFalse();
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldStartAudioRecording() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        Audio format = Audio.LINEAR_11;
        File tempFile = File.createTempFile("audio", ".WAV");
        tempFile.deleteOnExit();
        int silence = 2;
        int timeout = 1;
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        // acting
        boolean started = provider.startAudioRecording(handle, tempFile.getCanonicalPath(), format, silence, timeout);
        await().until(handle::isOperationInProgress);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).nativeStartAudioRecording(eq(handle), anyString(), eq(format), eq(silence), eq(timeout));
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider, atLeastOnce()).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.TIMEOUT);
        assertThat(started).isTrue();
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(handle.isTargetActive()).isFalse();
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldStopAudioRecording() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        Audio format = Audio.LINEAR;
        File tempFile = File.createTempFile("audio", ".WAV");
        tempFile.deleteOnExit();
        int silence = 2;
        int timeout = 10;
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        boolean started = provider.startAudioRecording(handle, tempFile.getCanonicalPath(), format, silence, timeout);
        await().until(handle::isOperationInProgress);
        assertThat(started).isTrue();
        reset(provider);

        // acting
        provider.stopAudioRecording(handle);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).nativeStopAudioRecording(handle);
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.IO.EOF);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(handle.isTargetActive()).isFalse();
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldGetResourceParameter() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        Device.ParameterName parameterName = mock(Device.ParameterName.class);
        ConfigurationParameter parameterValue = mock(ConfigurationParameter.class);
        provider.nativeSetResourceParameter(handle, parameterName, parameterValue);

        // acting
        Optional<ConfigurationParameter> parameter = provider.findResourceParameter(handle, parameterName);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).availableFormatsFor(handle);
        verify(provider).nativeFindResourceParameter(handle, parameterName);
        // check results
        assertThat(parameter).isNotNull().isPresent().contains(parameterValue);
    }

    @Test
    public void shouldGetAllowedDevices() {
        // preparing test data

        // acting
        Collection<String> allowedDevices = provider.allowedDevices();

        // check the behavior
        verify(provider).nativeAllowedDevices();
        // check results
        assertThat(allowedDevices).containsExactly(SOUND_DEVICE);
    }

    @Test
    public void shouldDialDtmfString() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        String dtmf = "1234-555(#*)";
        assertThat(provider.isOpened(handle)).isTrue();
        reset(provider);

        // acting
        provider.dialingDtmf(handle, dtmf);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).nativeDialingDtmf(handle, dtmf);
        // check results
    }

    @Test
    public void shouldStartPlayingTone_Beep() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        TelephonyTone tone = new TelephonyTone(ToneId.BEEP, "-1,1000,0,0,0,0,0,0,0,0");
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        shadowScheduler.schedule(() -> provider.stopToneSending(handle), 500, TimeUnit.MILLISECONDS);

        // acting
        boolean started = provider.startToneSending(handle, tone);
        await().until(handle::isOperationInProgress);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).nativeStartToneSending(handle, tone);
        verify(provider).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(started).isTrue();
        assertThat(handle.isSourceActive()).isFalse();
    }

    @Test
    public void shouldStartPlayingTone_Busy() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        TelephonyTone tone = new TelephonyTone(ToneId.BUSY, "2,500,200,0,0,55,40,55,40,4");
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        shadowScheduler.schedule(() -> provider.stopToneSending(handle), 800, TimeUnit.MILLISECONDS);

        // acting
        boolean started = provider.startToneSending(handle, tone);
        await().until(handle::isOperationInProgress);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).nativeStartToneSending(handle, tone);
        verify(provider).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(started).isTrue();
        assertThat(handle.isSourceActive()).isFalse();
    }

    @Test
    public void shouldStartPlayingTone_Ringback() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        TelephonyTone tone = new TelephonyTone(ToneId.RINGBACK, "3,450,150,0,0,150,100,550,400,0");
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        shadowScheduler.schedule(() -> provider.stopToneSending(handle), 1200, TimeUnit.MILLISECONDS);

        // acting
        boolean started = provider.startToneSending(handle, tone);
        await().until(handle::isOperationInProgress);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).nativeStartToneSending(handle, tone);
        verify(provider).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(started).isTrue();
        assertThat(handle.isSourceActive()).isFalse();
    }

    @Test
    public void shouldStartPlayingTone_Disconnect() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        TelephonyTone tone = new TelephonyTone(ToneId.DISCONNECT, "4,900,700,0,0,90,70,90,70,2");
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        shadowScheduler.schedule(() -> provider.stopToneSending(handle), 500, TimeUnit.MILLISECONDS);

        // acting
        boolean started = provider.startToneSending(handle, tone);
        await().until(handle::isOperationInProgress);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider).nativeStartToneSending(handle, tone);
        verify(provider).putEvent(any(DeviceEvent.class));
        // check results
        assertThat(started).isTrue();
        assertThat(handle.isSourceActive()).isFalse();
    }

    @Test
    public void shouldStopPlayingTone() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        TelephonyTone tone = new TelephonyTone(ToneId.BEEP, "-1,1000,0,0,0,0,0,0,0,0");
        doAnswer(invocation -> shadowScheduler.schedule(
                invocation.getArgument(0, Runnable.class),
                invocation.getArgument(1, Long.class),
                invocation.getArgument(2, TimeUnit.class)
        )).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        assertThat(provider.startToneSending(handle, tone)).isTrue();
        await().until(handle::isOperationInProgress);
        reset(provider);

        // acting
        shadowScheduler.schedule(() -> provider.stopToneSending(handle), 300, TimeUnit.MILLISECONDS);
        await().until(() -> !handle.isOperationInProgress());

        // check the behavior
        verify(provider, atLeastOnce()).isOpened(handle);
        verify(provider, never()).nativeStartToneSending(any(), any(TelephonyTone.class));
        ArgumentCaptor<DeviceEvent<SoundCardHandle>> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).putEvent(eventCaptor.capture());
        // check results
        DeviceEvent<SoundCardHandle> event = eventCaptor.getValue();
        assertThat(event.getEventType()).isSameAs(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getDeviceHandle()).isSameAs(handle);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).isPresent().contains(Result.IO.EOF);
        assertThat(provider.hasShadowActivity(handle)).isFalse();
        assertThat(handle.isSourceActive()).isFalse();
    }

    @Test
    public void shouldBeginToneRegistering() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);

        // acting
        provider.beginToneRegistering(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).nativeBeginToneRegistering(handle);
        // check results
    }

    @Test
    public void shouldRegisterTone() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);
        TelephonyTone tone = mock(TelephonyTone.class);

        // acting
        provider.registerTone(handle, tone);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).nativeRegisterTone(handle, tone);
        // check results
    }

    @Test
    public void shouldCommitToneRegistering() throws IOException {
        // preparing test data
        SoundCardHandle handle = provider.openResource(SOUND_DEVICE);

        // acting
        provider.commitToneRegistering(handle);

        // check the behavior
        verify(provider).isOpened(handle);
        verify(provider).nativeCommitToneRegistering(handle);
        // check results
    }

    /// inner classes
    private static class EventsProcessor extends AbstractEventProcessor<SoundCardHandle> {
        protected EventsProcessor(Executor deviceEventExecutor, DeviceEvent.Provider<SoundCardHandle> eventsProvider) {
            super(deviceEventExecutor, eventsProvider, new AbstractEventListenersHub() {
            });
        }

        @Override
        public String getType() {
            return "device-events-processor";
        }

        @Override
        public String getName() {
            return "TestEeventsProcessor";
        }
    }
}
