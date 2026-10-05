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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;

@SuppressWarnings("unchecked")
public class PlaybackUtilsTest {
    ScheduledExecutorService executor;

    @Before
    public void setUp() {
        executor = Executors.newScheduledThreadPool(2);
    }

    @After
    public void tearDown() {
        executor.shutdown();
    }

    @Test
    public void shouldPlayingBackAudioFile() throws IOException {
        // preparing test data
        SoundCardHandle handle = SoundCardHandle.WRONG_HANDLE;
        Path filePath = Files.createTempFile("test", ".audio");
        InputStream resource = PlaybackUtils.class.getResourceAsStream("/VM/prompts/PLAY_GREETING_PHONESOFT.WAV");
        assertThat(resource).isNotNull();
        Files.copy(resource, filePath, StandardCopyOption.REPLACE_EXISTING);
        Consumer<OperationResultValue> resultUpdater = mock(Consumer.class);
        File audioFile = filePath.toFile();
        audioFile.deleteOnExit();
        executor.schedule(() -> {
            handle.setSourceLine(null);
            CommonUtils.waitForOperationComplete(handle);
        }, 300L, TimeUnit.MILLISECONDS);

        // acting
        PlaybackUtils.playingBackAudioFile(handle, audioFile, resultUpdater);

        // check the behavior
        verify(resultUpdater).accept(Result.TIMEOUT);
        // check results
        assertThat(handle.isOperationInProgress()).isFalse();
        assertThat(Files.deleteIfExists(filePath)).isTrue();
    }
}