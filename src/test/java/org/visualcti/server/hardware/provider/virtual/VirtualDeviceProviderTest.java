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
package org.visualcti.server.hardware.provider.virtual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.media.Audio;

@SuppressWarnings("unchecked")
public class VirtualDeviceProviderTest {
    ScheduledExecutorService scheduler;
    VirtualDeviceProvider<UUID> provider;

    @Before
    public void setUp() throws Exception {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        provider = spy(new VirtualDeviceProvider<>(scheduler));
    }

    @After
    public void tearDown() throws Exception {
        scheduler.shutdown();
    }

    @Test
    public void shouldSetDeviceNamePrefix() {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device";
        provider.setDevicesCount(1);

        // acting
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);

        // check the behavior
        // check results
        assertThat(provider.allowedDevices().iterator().next()).startsWith(telephonyDeviceNamePrefix);
    }

    @Test
    public void shouldSetDevicesCount() {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device";
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        int devicesCount = 2;

        // acting
        provider.setDevicesCount(devicesCount);

        // check the behavior
        // check results
        assertThat(provider.allowedDevices()).hasSize(devicesCount);
        provider.allowedDevices().forEach(deviceName -> assertThat(deviceName).startsWith(telephonyDeviceNamePrefix));
    }

    @Test
    public void shouldGetNativeAllowedDevices() {
        // preparing test data
        assertThat(provider.allowedDevices()).isEmpty();
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        reset(provider);

        // acting
        Collection<String> devices = provider.allowedDevices();

        // check the behavior
        verify(provider).nativeAllowedDevices();
        // check results
        assertThat(devices).hasSize(devicesCount);
        devices.forEach(deviceName -> assertThat(deviceName).startsWith(telephonyDeviceNamePrefix));
    }

    @Test
    public void shouldDoNativeResourceOpen() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";

        // acting
        UUID handle = provider.openResource(resourceName);

        // check the behavior
        verify(provider).nativeResourceOpen(resourceName);
        // check results
        assertThat(provider.isValid(handle)).isTrue();
    }

    @Test
    public void shouldDoNotNativeResourceOpen_WrongDeviceName() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);

        // acting
        Exception e = assertThrows(Exception.class, () -> provider.openResource("resourceName"));

        // check the behavior
        verify(provider).nativeResourceOpen("resourceName");
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).endsWith("is already opened or has invalid name");
    }

    @Test
    public void shouldDoNativeAnswerCall() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);

        // acting
        boolean answered = provider.answerCall(handle);

        // check the behavior
        verify(provider).nativeAnswerCall(handle);
        // check results
        assertThat(answered).isTrue();
    }

    @Test
    public void shouldGetNativeCallerID() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);

        // acting
        PhoneCall.Number callerID = provider.getCallerID(handle);

        // check the behavior
        verify(provider).nativeCallerID(handle);
        // check results
        assertThat(callerID).isEqualTo(PhoneNumber.of("1234567890"));
    }

    @Test
    public void shouldBeReadyToPlay() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        Path path = Paths.get("src/test/resources/audio.wav");
        Audio format = Audio.LINEAR;
        int timeout = 10;

        // acting
        boolean ready = provider.startAudioPlaying(handle, path, format, timeout);

        // check the behavior
        verify(provider).isReadyToPlay(handle);
        verify(provider, never()).asyncAudioFilePlaying(any(File.class), any(Audio.class), any());
        // check results
        assertThat(ready).isFalse();
    }

    @Test
    public void shouldStartAsyncAudioFilePlaying() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        File audio = File.createTempFile("audio", ".wav");
        audio.deleteOnExit();
        Audio format = Audio.LINEAR;
        int timeout = 10;

        // acting
        boolean ready = provider.startAudioPlaying(handle, audio.toPath(), format, timeout);

        // check the behavior
        verify(provider).isReadyToPlay(handle);
        verify(provider).asyncAudioFilePlaying(audio, format, handle);
        // check results
        assertThat(ready).isTrue();
        assertThat(audio.delete()).isTrue();
    }

    @Test
    public void shouldBeReadyToRecord() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        File audio = File.createTempFile("audio", ".wav");
        audio.deleteOnExit();
        Audio format = Audio.LINEAR;
        int timeout = 10;
        int silence = 1;

        // acting
        boolean ready = provider.startAudioRecording(handle, audio.toPath(), format, silence, timeout);

        // check the behavior
        verify(provider).isReadyToRecord(handle);
        // check results
        assertThat(ready).isTrue();
        assertThat(audio.delete()).isTrue();
    }

    @Test
    public void shouldStartAsyncAudioFileRecording() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        File audio = File.createTempFile("audio", ".wav");
        Path path = audio.toPath();
        audio.deleteOnExit();
        Audio format = Audio.LINEAR;
        int timeout = 10;
        int silence = 1;

        // acting
        boolean ready = provider.startAudioRecording(handle, path, format, silence, timeout);

        // check the behavior
        verify(provider).isReadyToRecord(handle);
        verify(provider).asyncAudioFileRecording(path, format, handle, silence);
        // check results
        assertThat(ready).isTrue();
        assertThat(audio.delete()).isTrue();
    }

    @Test
    public void shouldSendTimeoutEventFor() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        String action = "Test Action";
        long after = 100L;
        TimeUnit unit = TimeUnit.MILLISECONDS;

        // acting
        provider.timeoutEventIn(handle, action, after, unit);

        // check the behavior
        verify(provider).sendTimeoutEventFor(handle, action);
        // check results
    }

    @Test
    public void shouldSchedulePostponedAction() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        String action = "Test Action";
        long after = 100L;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        provider.enableEventsGetting();

        // acting
        provider.timeoutEventIn(handle, action, after, unit);

        // check the behavior
        verify(provider).sendTimeoutEventFor(handle, action);
        verify(provider).schedulePostponedAction(eq(handle), any(Runnable.class), eq(after), eq(unit));
        // check results
    }

    @Test
    public void shouldScheduleHandleTimeoutEvent() throws IOException {
        // preparing test data
        String telephonyDeviceNamePrefix = "virtual_telephony_device_";
        int devicesCount = 2;
        provider.setDeviceNamePrefix(telephonyDeviceNamePrefix);
        provider.setDevicesCount(devicesCount);
        String resourceName = telephonyDeviceNamePrefix + "1";
        UUID handle = provider.openResource(resourceName);
        String action = "Test Action";
        long after = 100L;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        provider.enableEventsGetting();

        // acting
        provider.timeoutEventIn(handle, action, after, unit);
        await().until(() -> provider.getEvent(50).isPresent());

        // check the behavior
        ArgumentCaptor<DeviceEvent<UUID>> eventArgumentCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(provider).sendTimeoutEventFor(handle, action);
        verify(provider).schedulePostponedAction(eq(handle), any(Runnable.class), eq(after), eq(unit));
        verify(provider).putEvent(eventArgumentCaptor.capture());
        // check results
        DeviceEvent<UUID> event = eventArgumentCaptor.getValue();
        assertThat(event.getDeviceName()).isEqualTo(resourceName);
        assertThat(event.getDeviceHandle()).isEqualTo(handle);
        assertThat(event.getEventType()).isEqualTo(DeviceEvent.Type.DEVICE_SPECIFIC);
        assertThat(event.getOption(DeviceEvent.Option.REASON)).contains(Result.TIMEOUT);
    }
}
