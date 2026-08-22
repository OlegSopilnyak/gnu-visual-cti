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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceMalfunction;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Sound;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractMultimediaEngineTest<H> {
    ScheduledExecutorService executor;

    AbstractMultimediaEngine<H> engine;
    PhoneCallSession<H> session;
    TelephonyDevice<H, ?> device;
    TelephonyServiceProvider<H> provider;
    String deviceName = "device-name";
    H deviceHandle = (H) "handle";
    final static Device.ParameterName ALLOWED_CODECS = MultimediaEngine.Parameter.ALLOWED_CODECS;
    final static Device.ParameterName PLAYBACK_CODEC = MultimediaEngine.Parameter.PLAYBACK_CODEC;
    final static Device.ParameterName RECORD_CODEC = MultimediaEngine.Parameter.RECORD_CODEC;

    @Before
    public void setUp() throws Exception {
        provider = mock(TelephonyServiceProvider.class);
        device = mock(TelephonyDevice.class);
        doReturn(deviceName).when(device).getName();
        doReturn(provider).when(device).getProvider();
        session = spy(new PhoneCallSession(device, deviceHandle) {
        });
        engine = spy(new AbstractMultimediaEngine() {
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
    public void shouldCanPlay_AllFormats() {
        // preparing test data
        engine.uses(device);
        Device.ParameterName allowedCodecs = ALLOWED_CODECS;
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of("media-codecs", Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(allowedCodecs);

        // acting
        Audio[] all = engine.canPlay();

        // check the behavior
        verify(device).getParameter(allowedCodecs);
        verify(allAudios).getValue();
        // check results
        assertThat(all).isEqualTo(audios);
    }

    @Test
    public void shouldCannotPlay_NoFormats() {
        // preparing test data
        engine.uses(device);

        // acting
        Audio[] all = engine.canPlay();

        // check the behavior
        verify(device).getParameter(ALLOWED_CODECS);
        // check results
        assertThat(all).isEmpty();
    }

    @Test
    public void shouldCanPlay_ParticularFormat() {
        // preparing test data
        engine.uses(device);
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of("media-codecs", Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(ALLOWED_CODECS);

        // acting
        boolean can = engine.canPlay(Audio.LINEAR);

        // check the behavior
        verify(engine).canPlay();
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCannotPlay_ParticularFormat() {
        // preparing test data
        engine.uses(device);
        Audio[] audios = new Audio[]{Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of("media-codecs", Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(ALLOWED_CODECS);

        // acting
        boolean can = engine.canPlay(Audio.ULAW_8);

        // check the behavior
        verify(engine).canPlay();
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldGetRawFormat() {
        // preparing test data
        engine.uses(device);
        Audio rawFormat = Audio.ALAW_8;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("play", rawFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(PLAYBACK_CODEC);

        // acting
        Audio audio = engine.getRawFormat();

        // check the behavior
        verify(device).getParameter(PLAYBACK_CODEC);
        verify(rawAudio).getValue();
        // check results
        assertThat(audio).isSameAs(rawFormat);
    }

    @Test
    public void shouldGetRecordFormat() {
        // preparing test data
        engine.uses(device);
        Audio recordFormat = Audio.ADPCM_8;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);

        // acting
        Audio audio = engine.getRecordFormat();

        // check the behavior
        verify(device).getParameter(RECORD_CODEC);
        verify(rawAudio).getValue();
        // check results
        assertThat(audio).isSameAs(recordFormat);
    }

    @Test
    public void shouldCanRecord_AllFormats() {
        // preparing test data
        engine.uses(device);
        Audio recordFormat = Audio.ADPCM_8;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);

        // acting
        Audio[] all = engine.canRecord();

        // check the behavior
        verify(engine).getRecordFormat();
        // check results
        assertThat(all).isEqualTo(new Audio[]{recordFormat});
    }

    @Test
    public void shouldCanRecord_ParticularFormats() {
        // preparing test data
        engine.uses(device);
        Audio recordFormat = Audio.ADPCM_8;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);

        // acting
        boolean can = engine.canRecord(recordFormat);

        // check the behavior
        verify(engine).getRecordFormat();
        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCannotRecord_NoFormats() {
        // preparing test data
        engine.uses(device);

        // acting
        Audio[] all = engine.canRecord();

        // check the behavior
        verify(engine).getRecordFormat();
        // check results
        assertThat(all).isEmpty();
    }

    @Test
    public void shouldCannotRecord_ParticularFormats() {
        // preparing test data
        engine.uses(device);
        Audio recordFormat = Audio.ADPCM_8;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);

        // acting
        boolean can = engine.canRecord(Audio.ADPCM_6);

        // check the behavior
        verify(engine).getRecordFormat();
        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldStartPlaybackAudioAsynchronously() throws IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String audio = "Testing audio content";
        InputStream audioStream = spy(new ByteArrayInputStream(audio.getBytes()));
        Audio[] audios = new Audio[]{playbackFormat, Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of("media-codecs", Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(ALLOWED_CODECS);
        Sound sound = mock(Sound.class);
        doReturn(playbackFormat).when(sound).getFormat();
        doReturn(audioStream).when(sound).getInputStream();
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(-1));

        // acting
        boolean can = engine.asyncPlaybackAudio(session, sound);

        // check the behavior
        verify(sound).getFormat();
        verify(session).isOpened();
        verify(session).isAlive();
        verify(engine).canPlay(playbackFormat);
        verify(engine).canPlay();
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(-1));
        // check results
        assertThat(can).isTrue();
        assertThat(session.getState()).isEqualTo(TelephonyDevice.State.PLAY);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
        File tempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
        assertThat(tempFile).exists();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(audio);
        }
    }

    @Test
    public void shouldNotStartPlaybackAudioAsynchronously_DisconnectedBefore() throws IOException {
        // preparing test data
        engine.uses(device);
        Audio playbackFormat = Audio.ADPCM_8;
        String audio = "Testing audio content";
        InputStream audioStream = spy(new ByteArrayInputStream(audio.getBytes()));
        Audio[] audios = new Audio[]{playbackFormat, Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of("media-codecs", Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(ALLOWED_CODECS);
        Sound sound = mock(Sound.class);
        doReturn(playbackFormat).when(sound).getFormat();
        doReturn(audioStream).when(sound).getInputStream();

        // acting
        boolean can = engine.asyncPlaybackAudio(session, sound);

        // check the behavior
        verify(sound).getFormat();
        verify(session).isOpened();
        verify(session).isAlive();
        verify(engine, never()).canPlay(playbackFormat);
        // check results
        assertThat(can).isFalse();
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
        assertThat(session.operationResult()).isEqualTo(Result.ERROR);
        assertThat(session.<File>parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY)).isNull();
    }

    @Test
    public void shouldNotStartPlaybackAudioAsynchronously_ProviderDidntStartAudioPlaying() throws IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String audio = "Testing audio content";
        InputStream audioStream = spy(new ByteArrayInputStream(audio.getBytes()));
        Audio[] audios = new Audio[]{playbackFormat, Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of("media-codecs", Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(ALLOWED_CODECS);
        Sound sound = mock(Sound.class);
        doReturn(playbackFormat).when(sound).getFormat();
        doReturn(audioStream).when(sound).getInputStream();

        // acting
        boolean can = engine.asyncPlaybackAudio(session, sound);

        // check the behavior
        verify(sound).getFormat();
        verify(session).isOpened();
        verify(session).isAlive();
        verify(engine).canPlay(playbackFormat);
        verify(engine).canPlay();
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(-1));
        // check results
        assertThat(can).isFalse();
        assertThat(session.getState()).isEqualTo(TelephonyDevice.State.PLAY);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
        File tempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
        assertThat(tempFile).exists();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(audio);
        }
    }

    @Test
    public void shouldPlaybackAudio_EOF() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "*";
        int timeout = 2;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.IO.EOF), 50, TimeUnit.MILLISECONDS);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verifyPlaybackEventsAdjusting();
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).operationResult(Result.NONE);
        verify(session).waitingForOperationComplete(timeout * 1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Playback audio is completed.");
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(result).isSameAs(Result.IO.EOF);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.IO.EOF);
    }

    @Test
    public void shouldPlaybackAudio_DTMF() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        session.parameter(Device.Parameter.USER_INPUT, terminationSymbolsMask);

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.IO.DTMF), 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verifyPlaybackEventsAdjusting();
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).waitingForOperationComplete(timeout * 1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(session).parameter(Device.Parameter.USER_INPUT);
        verify(device).dispatchEvent("Playback audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(result).isSameAs(Result.IO.DTMF);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.IO.DTMF);
    }

    @Test
    public void shouldPlaybackAudio_DTMF_ButEmptyMask() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "";
        int timeout = 1;
        long waitForMills = timeout * 1000L;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        session.parameter(Device.Parameter.USER_INPUT, terminationSymbolsMask);

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.IO.DTMF), 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verifyPlaybackEventsAdjusting(false);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).waitingForOperationComplete(waitForMills);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Playback audio is completed.");
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(Result.TIMEOUT);
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(session, atLeastOnce()).waitingForOperationComplete(captor.capture());
        List<Long> captured = captor.getAllValues();
        // check results
        assertThat(result).isSameAs(Result.TIMEOUT);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.TIMEOUT);
        assertThat(captured.get(0)).isEqualTo(waitForMills);
        assertThat(captured.get(1)).isLessThan(waitForMills);
    }

    @Test
    public void shouldPlaybackAudio_DTMF_ButNotFromMask() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 1;
        long waitForMills = timeout * 1000L;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        session.parameter(Device.Parameter.USER_INPUT, "*");

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.IO.DTMF), 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verifyPlaybackEventsAdjusting();
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).waitingForOperationComplete(waitForMills);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Playback audio is completed.");
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(Result.TIMEOUT);
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(session, atLeastOnce()).waitingForOperationComplete(captor.capture());
        List<Long> captured = captor.getAllValues();
        // check results
        assertThat(result).isSameAs(Result.TIMEOUT);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.TIMEOUT);
        assertThat(captured.get(0)).isEqualTo(waitForMills);
        assertThat(captured.get(1)).isLessThan(waitForMills);
    }

    @Test
    public void shouldPlaybackAudio_DTMF_EmptyUserInput() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 1;
        long waitForMills = timeout * 1000L;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        session.parameter(Device.Parameter.USER_INPUT, "");

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.IO.DTMF), 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device).getProvider();
        verifyPlaybackEventsAdjusting();
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).waitingForOperationComplete(waitForMills);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Playback audio is completed.");
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(Result.TIMEOUT);
        // check results
        assertThat(result).isSameAs(Result.TIMEOUT);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.TIMEOUT);
    }

    @Test
    public void shouldNotPlaybackAudio_HardwareError() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "";
        String malfunctionReason = "Playback audio is failed.";
        int timeout = 2;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));

        // acting
        Future<Throwable> playback = executor.submit(() ->
                assertThrows(Throwable.class,
                        () -> engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
                )
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.ERROR), 100, TimeUnit.MILLISECONDS);
        Throwable result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session).operationResult(Result.NONE);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device, atLeastOnce()).getProvider();
        verifyPlaybackEventsAdjusting(false);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).operationResult(Result.NONE);
        verify(session).waitingForOperationComplete(timeout * 1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(engine).onDeviceError(session, malfunctionReason);
        verify(engine).onDeviceError(session, malfunctionReason, true);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(malfunctionReason);
        verify(device, never()).dispatchEvent("Playback audio is completed.");
        // check results
        assertThat(result).isInstanceOf(DeviceMalfunction.class);
        assertThat(result.getMessage()).endsWith(malfunctionReason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
        assertThat(session.operationResult()).isEqualTo(Result.ERROR);
    }

    @Test
    public void shouldNotPlaybackAudio_ProviderDidntStartAudioPlaying() throws InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "";
        String malfunctionReason = "Cannot start playing the audio file.";
        int timeout = 2;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);

        // acting
        Throwable result = assertThrows(Throwable.class,
                () -> engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device, atLeastOnce()).getProvider();
        verifyPlaybackEventsAdjusting(false);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session, never()).waitingForOperationComplete(anyLong());
        verifyPlaybackEventsAdjusting(false);
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(engine).onDeviceError(session, malfunctionReason);
        verify(engine).onDeviceError(session, malfunctionReason, true);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(malfunctionReason);
        // check results
        assertThat(result).isInstanceOf(DeviceMalfunction.class);
        assertThat(result.getMessage()).endsWith(malfunctionReason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotPlaybackAudio_DisconnectedInAction() throws InterruptedException, ExecutionException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "*";
        String malfunctionReason = "Playback audio is failed. The connection is lost.";
        int timeout = 2;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            session.alive(false);
            session.operationComplete(Result.CALL.DISCONNECT);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device, atLeastOnce()).getProvider();
        verifyPlaybackEventsAdjusting();
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).waitingForOperationComplete(timeout * 1000L);
        verify(session).isTerminated();
        verify(session).isDisconnected();
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(malfunctionReason);
        // check results
        assertThat(result).isSameAs(Result.CALL.DISCONNECT);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.DISCONNECT);
    }

    @Test
    public void shouldRecordAudio_EOF() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        String audio = "Testing audio content";
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.IO.EOF;
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
                    try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                        return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                    }
                }
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            final File audioTempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
            // saving audio content to the temporary media file of the record operation
            try (OutputStream out = new FileOutputStream(audioTempFile)) {
                out.write(audio.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing media-data transmitting operation (end of media data)
            session.operationComplete(recordingResult);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session).operationResult(Result.NONE);
        verify(device).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Record audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(recordingResult);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(recordingResult);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(recordingResult);
        assertThat(tempFile.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(audio);
        }
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldRecordAudio_Silence() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        String audio = "Testing audio content";
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.IO.SILENCE;
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
                    try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                        return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                    }
                }
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            final File audioTempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
            // saving audio content to the temporary media file of the record operation
            try (OutputStream out = new FileOutputStream(audioTempFile)) {
                out.write(audio.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing media-data transmitting operation (end of media data)
            session.operationComplete(recordingResult);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session).operationResult(Result.NONE);
        verify(device).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Record audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(recordingResult);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(recordingResult);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(recordingResult);
        assertThat(tempFile.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(audio);
        }
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldRecordAudio_DTMF() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        String audio = "Testing audio content";
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.IO.DTMF;
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );
        session.parameter(Device.Parameter.USER_INPUT, terminationSymbolsMask);

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
                    try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                        return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                    }
                }
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            final File audioTempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
            // saving audio content to the temporary media file of the record operation
            try (OutputStream out = new FileOutputStream(audioTempFile)) {
                out.write(audio.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing media-data transmitting operation (end of media data)
            session.operationComplete(recordingResult);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session).operationResult(Result.NONE);
        verify(device).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session).parameter(Device.Parameter.USER_INPUT);
        verify(session).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Record audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(recordingResult);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(recordingResult);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(recordingResult);
        assertThat(tempFile.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(audio);
        }
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldRecordAudio_DTMF_ButEmptyMask() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        String audio = "Testing audio content";
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.IO.DTMF;
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );
        session.parameter(Device.Parameter.USER_INPUT, "*");

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
                    try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                        return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                    }
                }
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            final File audioTempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
            // saving audio content to the temporary media file of the record operation
            try (OutputStream out = new FileOutputStream(audioTempFile)) {
                out.write(audio.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing media-data transmitting operation (end of media data)
            session.operationComplete(recordingResult);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(device).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session, never()).parameter(Device.Parameter.USER_INPUT);
        verify(session, atLeastOnce()).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Record audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(recordingResult);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(Result.NONE);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldRecordAudio_DTMF_ButNotFromMask() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        String audio = "Testing audio content";
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.IO.DTMF;
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );
        session.parameter(Device.Parameter.USER_INPUT, "*");

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
                    try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                        return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                    }
                }
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            final File audioTempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
            // saving audio content to the temporary media file of the record operation
            try (OutputStream out = new FileOutputStream(audioTempFile)) {
                out.write(audio.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing media-data transmitting operation (end of media data)
            session.operationComplete(recordingResult);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(device).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session).parameter(Device.Parameter.USER_INPUT);
        verify(session, atLeastOnce()).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Record audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(recordingResult);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(Result.NONE);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldRecordAudio_DTMF_ButEmptyUserInput() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        String audio = "Testing audio content";
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.IO.DTMF;
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
                    try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                        return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                    }
                }
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            final File audioTempFile = session.parameter(MultimediaEngine.Parameter.AUDIO_TEMPORARY);
            // saving audio content to the temporary media file of the record operation
            try (OutputStream out = new FileOutputStream(audioTempFile)) {
                out.write(audio.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing media-data transmitting operation (end of media data)
            session.operationComplete(recordingResult);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(device).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session).parameter(Device.Parameter.USER_INPUT);
        verify(session, atLeastOnce()).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(device).dispatchEvent("Record audio is completed.");
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        verify(session).operationResult(recordingResult);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(Result.NONE);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.NONE);
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldNotRecordAudio_HardwareError() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        OperationResultValue recordingResult = Result.ERROR;
        String malfunctionReason = "Record audio is failed.";
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );

        // acting
        Future<Throwable> recording = executor.submit(() ->
                assertThrows(Throwable.class, () -> {
                            try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                                engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
                            }
                        }
                )
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(Result.ERROR), 100, TimeUnit.MILLISECONDS);
        Throwable result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session).operationResult(Result.NONE);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, atLeastOnce()).operationResult(Result.NONE);
        verify(session).waitingForOperationComplete(1000L);
        verify(session, atLeastOnce()).operationResult();
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(engine).onDeviceError(session, malfunctionReason);
        verify(engine).onDeviceError(session, malfunctionReason, true);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(malfunctionReason);
        verify(device, never()).dispatchEvent("Record audio is completed.");
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isInstanceOf(DeviceMalfunction.class);
        assertThat(result.getMessage()).endsWith(malfunctionReason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
        assertThat(session.operationResult()).isEqualTo(recordingResult);
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldNotRecordAudio_DidNotStartRecording() throws InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        String malfunctionReason = "Cannot start recording the audio file.";

        // acting
        Throwable result = assertThrows(Throwable.class, () -> {
            try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
            }
        });

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session, never()).operationResult(Result.NONE);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session, never()).waitingForOperationComplete(anyLong());
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(engine).onDeviceError(session, malfunctionReason);
        verify(engine).onDeviceError(session, malfunctionReason, true);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(malfunctionReason);
        verify(device, never()).dispatchEvent("Record audio is completed.");
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isInstanceOf(DeviceMalfunction.class);
        assertThat(result.getMessage()).endsWith(malfunctionReason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldNotRecordAudio_DisconnectedInAction() throws ExecutionException, InterruptedException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        String malfunctionReason = "Recording audio is failed. The connection is lost.";
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
            try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            session.alive(false);
            session.operationComplete(Result.CALL.DISCONNECT);
        }, 100, TimeUnit.MILLISECONDS);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session).operationResult(Result.NONE);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session).waitingForOperationComplete(1000L);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError(malfunctionReason);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(Result.CALL.DISCONNECT);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
        assertThat(session.operationResult()).isEqualTo(Result.CALL.DISCONNECT);
        assertThat(tempFile.delete()).isTrue();
    }

    @Test
    public void shouldTerminatePlayback() throws InterruptedException, ExecutionException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio playbackFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "*";
        int timeout = 10;
        String audio = "Testing audio content";
        InputStream source = prepareMultiMediaSource(audio);
        preparePlaybackCodecs(playbackFormat);
        doReturn(true).when(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));

        // acting
        Future<OperationResultValue> playback = executor.submit(() ->
                engine.playbackAudio(session, source, playbackFormat, terminationSymbolsMask, timeout)
        );
        await().until(() -> session.operationIsActive());
        engine.terminate(session);
        OperationResultValue result = playback.get();

        // check the behavior
        verifyPlaybackInputVerification(playbackFormat);
        verify(device).dispatchEvent("Playback audio is starting...");
        verify(session).setState(TelephonyDevice.State.PLAY);
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(device, atLeastOnce()).getProvider();
        verifyPlaybackEventsAdjusting();
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).startAudioPlaying(eq(deviceHandle), anyString(), eq(playbackFormat), eq(timeout));
        verify(session).waitingForOperationComplete(timeout * 1000L);
        verify(session).isTerminated();
        verify(session, never()).isDisconnected();
        verify(provider, atLeastOnce()).stopAudioPlaying(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(result).isSameAs(Result.TERMINATED);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.TERMINATED);
    }

    @Test
    public void shouldTerminateRecord() throws InterruptedException, ExecutionException, IOException {
        // preparing test data
        engine.uses(device);
        session.alive(true);
        Audio recordFormat = Audio.ADPCM_8;
        String terminationSymbolsMask = "#";
        int timeout = 2;
        int silence = 1;
        ConfigurationParameter rawAudio = spy(ConfigurationParameter.of("record", recordFormat));
        doReturn(Optional.of(rawAudio)).when(device).getParameter(RECORD_CODEC);
        File tempFile = File.createTempFile("media-data", ".audio");
        tempFile.deleteOnExit();
        doReturn(true).when(provider).startAudioRecording(
                eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout)
        );

        // acting
        Future<OperationResultValue> recording = executor.submit(() -> {
            try (OutputStream audioStream = new FileOutputStream(tempFile)) {
                return engine.recordAudio(session, audioStream, recordFormat, terminationSymbolsMask, silence, timeout);
            }
        });
        await().until(() -> session.operationIsActive());
        engine.terminate(session);
        OperationResultValue result = recording.get();

        // check the behavior
        verify(session).isOpened();
        verify(session, atLeastOnce()).isAlive();
        verify(engine).canRecord(recordFormat);
        verify(engine).canRecord();
        verify(device).dispatchEvent("Audio record is starting...");
        verify(session).setState(TelephonyDevice.State.RECORD);
        verify(session).operationResult(Result.NONE);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).getDeviceHandle();
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(session).parameter(eq(MultimediaEngine.Parameter.AUDIO_TEMPORARY), any(File.class));
        verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider).startAudioRecording(eq(deviceHandle), anyString(), eq(recordFormat), eq(silence), eq(timeout));
        verify(session).waitingForOperationComplete(1000L);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle, Result.IO.DTMF);
        verify(provider, atLeastOnce()).stopAudioRecording(deviceHandle);
        verify(session).setState(Device.State.IDLE);
        // check results
        assertThat(session.isTerminated()).isTrue();
        assertThat(result).isSameAs(Result.TERMINATED);
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
        assertThat(session.operationResult()).isEqualTo(Result.TERMINATED);
        assertThat(tempFile.delete()).isTrue();
    }

    /// private methods
    // verifying playback input parameters
    private void verifyPlaybackInputVerification(Audio audioFormat) {
        verify(session, atLeastOnce()).isAlive();
        verify(engine).isOpened(session);
        verify(engine).canPlay(audioFormat);
        verify(session, atLeastOnce()).parameter(Device.Parameter.DEVICE_HANDLE);
        verify(engine).canPlay();
    }

    // preparing available device's codecs
    private void preparePlaybackCodecs(Audio playbackFormat) {
        Device.ParameterName parameterName = ALLOWED_CODECS;
        Audio[] audios = new Audio[]{playbackFormat, Audio.LINEAR, Audio.LINEAR_8, Audio.LINEAR_11};
        ConfigurationParameter allAudios = spy(ConfigurationParameter.of(parameterName.value(), Arrays.asList(audios)));
        doReturn(Optional.of(allAudios)).when(device).getParameter(parameterName);
    }

    // verifying playback events management
    private void verifyPlaybackEventsAdjusting() {
        verifyPlaybackEventsAdjusting(true);
    }
    private void verifyPlaybackEventsAdjusting(boolean isMaskExists) {
        verify(provider).disableEvents(deviceHandle);
        if(isMaskExists) {
            verify(provider).enableEvents(deviceHandle, Result.IO.DTMF);
        }
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(provider).disableEvents(deviceHandle, Result.IO.DTMF);
    }

    // to prepare the input stream for te fax transmit operation
    private static InputStream prepareMultiMediaSource(String multiMediaContent) throws IOException {
        InputStream source = mock(InputStream.class);
        AtomicBoolean firstRead = new AtomicBoolean(true);
        doAnswer((Answer<Integer>) invocation -> {
            if (firstRead.get()) {
                byte[] b = invocation.getArgument(0);
                byte[] payload = multiMediaContent.getBytes();
                System.arraycopy(payload, 0, b, 0, payload.length);
                firstRead.getAndSet(false);
                return payload.length;
            } else {
                return -1;
            }
        }).when(source).read(any(byte[].class), anyInt(), anyInt());
        return source;
    }
}
