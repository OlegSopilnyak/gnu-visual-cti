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
package org.visualcti.workflow.hardware.javasound.io.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.media.Audio;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;

public class CaptureUtilsTest {
    ScheduledExecutorService executor;

    @Before
    public void setUp() throws Exception {
        executor = Executors.newScheduledThreadPool(2);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdown();
    }

    @Test
    public void shouldCapturingAudioToFile() throws IOException {
        // preparing test data
        SoundCardHandle handle = SoundCardHandle.WRONG_HANDLE;
        Path filePath = Files.createTempFile("test", ".audio");
        Audio format = Audio.LINEAR;
        Runnable afterCapturing = mock(Runnable.class);
        filePath.toFile().deleteOnExit();
        executor.schedule(() -> CaptureUtils.completeCapturing(handle, afterCapturing), 500L, TimeUnit.MILLISECONDS);

        // acting
        CaptureUtils.capturingAudioToFile(handle, filePath, format);

        // check the behavior
        verify(afterCapturing).run();
        // check results
        assertThat(Files.size(filePath)).isGreaterThan(1);
        Files.delete(filePath);
    }

    @Test
    public void shouldCompleteCapturing() throws IOException {
        // preparing test data
        SoundCardHandle handle = SoundCardHandle.WRONG_HANDLE;
        Path filePath = Files.createTempFile("test", ".audio");
        Audio format = Audio.LINEAR;
        Runnable afterCapturing = mock(Runnable.class);
        filePath.toFile().deleteOnExit();
        executor.schedule(() -> CaptureUtils.capturingAudioToFile(handle, filePath, format), 0L, TimeUnit.MILLISECONDS);

        // acting
        await().until(handle::isOperationInProgress);
        CaptureUtils.completeCapturing(handle, afterCapturing);

        // check the behavior
        verify(afterCapturing).run();
        // check results
        assertThat(handle.isOperationInProgress()).isFalse();
        assertThat(Files.size(filePath)).isGreaterThan(1);
        Files.delete(filePath);
    }

    @Test
    public void shouldWaitForOperationComplete() throws IOException {
        // preparing test data
        SoundCardHandle handle = SoundCardHandle.WRONG_HANDLE;
        Path filePath = Files.createTempFile("test", ".audio");
        Audio format = Audio.LINEAR;
        Runnable afterCapturing = mock(Runnable.class);
        filePath.toFile().deleteOnExit();
        executor.schedule(() -> CaptureUtils.capturingAudioToFile(handle, filePath, format), 0L, TimeUnit.MILLISECONDS);
        await().until(handle::isOperationInProgress);
        executor.schedule(() -> CaptureUtils.completeCapturing(handle, afterCapturing), 500L, TimeUnit.MILLISECONDS);

        // acting
        assertThat(handle.isOperationInProgress()).isTrue();
        CaptureUtils.waitForOperationComplete(handle);

        // check the behavior
        verify(afterCapturing).run();
        // check results
        assertThat(handle.isOperationInProgress()).isFalse();
        assertThat(Files.size(filePath)).isGreaterThan(1);
        Files.delete(filePath);
    }
}