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
package org.visualcti.core.channel.telephony.part;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.visualcti.media.Audio.ULAW_8;

import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.telephony.operation.ResultValue;
import org.visualcti.media.Audio;

public class MultiMedeaEngineTest {
    MultiMedeaEngine engine;

    @Before
    public void setUp() {
        engine = mock(MultiMedeaEngine.class);
    }

    @Test
    public void shouldCanPlay() {
        // preparing test data
        Audio [] audios = {Audio.ULAW_8, Audio.LINEAR};
        doReturn(audios).when(engine).canPlay();

        // acting
        Audio [] canPlay = engine.canPlay();

        // check results
        assertThat(canPlay).isSameAs(audios);
    }

    @Test
    public void shouldCantPlay() {
        // preparing test data

        // acting
        Audio [] canPlay = engine.canPlay();

        // check results
        assertThat(canPlay).isNull();
    }

    @Test
    public void shouldGetRawFormat() {
        // preparing test data
        Audio rawAudio = Audio.ULAW_8;
        doReturn(rawAudio).when(engine).getRawFormat();

        // acting
        Audio canPlay = engine.getRawFormat();

        // check results
        assertThat(canPlay).isSameAs(rawAudio);
    }

    @Test
    public void shouldNotGetRawFormat() {
        // preparing test data

        // acting
        Audio canPlay = engine.getRawFormat();

        // check results
        assertThat(canPlay).isNull();
    }

    @Test
    public void shouldPlaybackAudio() {
        ResultValue resultValue = mock(ResultValue.class);
        InputStream stream = mock(InputStream.class);
        String mask = "*,#";
        Audio audioFormat = Audio.LINEAR_11;
        int timeout = 5;
        doReturn(resultValue).when(engine).playbackAudio(eq(stream), anyString(), anyInt(), eq(audioFormat));

        // acting
        ResultValue result = engine.playbackAudio(stream, mask, timeout, audioFormat);

        // check the behavior
        ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
        verify(engine).playbackAudio(captor.capture(), anyString(), anyInt(), any(Audio.class));
        // check results
        assertThat(captor.getValue()).isSameAs(stream);
        assertThat(result).isSameAs(resultValue);
    }

    @Test
    public void shouldCanRecord() {
        // preparing test data
        Audio [] audios = {Audio.ULAW_8, Audio.LINEAR};
        doReturn(audios).when(engine).canRecord();

        // acting
        Audio [] canPlay = engine.canRecord();

        // check results
        assertThat(canPlay).isSameAs(audios);
    }

    @Test
    public void shouldCantRecord() {
        // preparing test data

        // acting
        Audio [] canPlay = engine.canRecord();

        // check results
        assertThat(canPlay).isNull();
    }

    @Test
    public void shouldGetRecordFormat() {
        // preparing test data
        Audio recordAudio = Audio.ULAW_8;
        doReturn(recordAudio).when(engine).getRecordFormat();

        // acting
        Audio canPlay = engine.getRecordFormat();

        // check results
        assertThat(canPlay).isSameAs(recordAudio);
    }

    @Test
    public void shouldNotGetRecordFormat() {
        // preparing test data

        // acting
        Audio canPlay = engine.getRecordFormat();

        // check results
        assertThat(canPlay).isNull();
    }

    @Test
    public void shouldRecordAudio() {
        ResultValue resultValue = mock(ResultValue.class);
        OutputStream stream = mock(OutputStream.class);
        String mask = "*,#";
        Audio audioFormat = ULAW_8;
        int silence = 2;
        int timeout = 5;
        doReturn(resultValue).when(engine).recordAudio(eq(stream), anyString(), anyInt(), anyInt(), eq(audioFormat));

        // acting
        ResultValue result = engine.recordAudio(stream, mask, silence, timeout, audioFormat);

        // check the behavior
        ArgumentCaptor<OutputStream> captor = ArgumentCaptor.forClass(OutputStream.class);
        verify(engine).recordAudio(captor.capture(), anyString(), anyInt(), anyInt(), any(Audio.class));
        // check results
        assertThat(captor.getValue()).isSameAs(stream);
        assertThat(result).isSameAs(resultValue);
    }
}