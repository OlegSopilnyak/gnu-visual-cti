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
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;

@SuppressWarnings("unchecked")
public class CommonUtilsTest {
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
    public void shouldWaitForOperationComplete() {
        // preparing test data
        SoundCardHandle handle = spy(SoundCardHandle.WRONG_HANDLE);
        handle.inProgress(true);
        executor.schedule(() -> handle.inProgress(false), 300L, TimeUnit.MILLISECONDS);

        // acting
        CommonUtils.waitForOperationComplete(handle);

        // check the behavior
        verify(handle, atLeastOnce()).isOperationInProgress();
        // check results
        assertThat(handle.isOperationInProgress()).isFalse();
    }

    @Test
    public void shouldCompleteOperation() throws IOException {
        // preparing test data
        SoundCardHandle handle = spy(SoundCardHandle.WRONG_HANDLE);
        handle.inProgress(true);
        CommonUtils.startOperation(handle);
        Runnable completeOperation = mock(Runnable.class);
        executor.schedule(() -> handle.inProgress(false), 300L, TimeUnit.MILLISECONDS);

        // acting
        CommonUtils.completeOperation(handle, completeOperation);

        // check the behavior
        verify(handle, atLeastOnce()).isOperationInProgress();
        // check results
        assertThat(CommonUtils.getAfterPartyQueue(handle)).hasSize(1);
        assertThat(handle.isOperationInProgress()).isFalse();
    }

    @Test
    public void shouldGetAfterPartyQueue() throws IOException {
        // preparing test data
        SoundCardHandle handle = spy(SoundCardHandle.WRONG_HANDLE);
        CommonUtils.startOperation(handle);

        // acting
        BlockingQueue<?> afterParty = CommonUtils.getAfterPartyQueue(handle);

        // check the behavior
        // check results
        assertThat(afterParty).isEmpty();
    }

    @Test
    public void shouldNotGetAfterPartyQueue_OperationDidNotStart() {
        // preparing test data
        SoundCardHandle handle = spy(SoundCardHandle.WRONG_HANDLE);

        // acting
        Exception e = assertThrows(Exception.class, () -> CommonUtils.getAfterPartyQueue(handle));

        // check the behavior
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("Sound capturing is started in a wrong way.");
    }

    @Test
    public void shouldDoPostOperationAction_EmptyQueue() throws IOException {
        // preparing test data
        BlockingQueue<Runnable> afterParty = mock(BlockingDeque.class);
        doReturn(true).when(afterParty).isEmpty();

        // acting
        CommonUtils.postOperation(afterParty);

        // check the behavior
        verify(afterParty).isEmpty();
        // check results
    }

    @Test
    public void shouldDoPostOperationAction_QueueWithRunnable() throws IOException, InterruptedException {
        // preparing test data
        Runnable after = mock(Runnable.class);
        BlockingQueue<Runnable> afterParty = mock(BlockingDeque.class);
        doReturn(after).when(afterParty).take();

        // acting
        CommonUtils.postOperation(afterParty);

        // check the behavior
        verify(after).run();
        // check results
    }

    @Test
    public void shouldStartOperation() throws IOException {
        // preparing test data
        SoundCardHandle handle = spy(SoundCardHandle.WRONG_HANDLE);
        assertThat(assertThrows(Exception.class, () -> CommonUtils.getAfterPartyQueue(handle))).isInstanceOf(IOException.class);

        // acting
        CommonUtils.startOperation(handle);

        // check the behavior
        // check results
        assertThat(CommonUtils.getAfterPartyQueue(handle)).isEmpty();
    }

    @Test
    public void shouldSetOperationComplete() throws IOException {
        // preparing test data
        SoundCardHandle handle = spy(SoundCardHandle.WRONG_HANDLE);
        handle.inProgress(true);
        CommonUtils.startOperation(handle);
        assertThat(CommonUtils.getAfterPartyQueue(handle)).isEmpty();

        // acting
        CommonUtils.operationComplete(handle);

        // check the behavior
        // check results
        assertThat(assertThrows(Exception.class, () -> CommonUtils.getAfterPartyQueue(handle))).isInstanceOf(IOException.class);
        assertThat(handle.isOperationInProgress()).isFalse();
    }
}