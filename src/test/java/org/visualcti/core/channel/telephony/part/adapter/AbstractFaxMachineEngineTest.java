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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.media.Fax;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractFaxMachineEngineTest<H> {
    ScheduledExecutorService executor;

    PhoneCallSession<H> session;
    AbstractFaxMachineEngine<H> engine;
    TelephonyDevice<H, ?> device;
    TelephonyServiceProvider<H> provider;
    String deviceName = "device-name";
    H deviceHandle = (H) "handle";

    @Before
    public void setUp() throws Exception {
        provider = mock(TelephonyServiceProvider.class);
        device = mock(TelephonyDevice.class);
        doReturn(deviceName).when(device).getName();
        doReturn(provider).when(device).getProvider();
        session = spy(new PhoneCallSession(device, deviceHandle) {
        });
        engine = spy(new AbstractFaxMachineEngine() {
        });
        executor = Executors.newScheduledThreadPool(2);
    }

    //    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    @Test
    public void shouldOpen() throws IOException {
        // preparing test data
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);

        // acting
        engine.open(session);

        // check the behavior
        verify(engine).canFax();
        verify(engine).isOpened(session);
        verify(device).getProvider();
        verify(provider).openFaxResource(deviceName);
        verify(session).parameter(Device.Parameter.FAX_DEVICE_HANDLE, deviceHandle);
        // check results
        assertThat(engine.isOpened(session)).isTrue();
    }

    @Test
    public void shouldNotOpen_AlreadyOpened() throws IOException {
        // preparing test data
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        reset(engine, provider);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);

        // acting
        Exception e = assertThrows(Exception.class, () -> engine.open(session));

        // check the behavior
        verify(engine).canFax();
        verify(engine).isOpened(session);
        verify(device).getProvider();
        verify(provider, never()).openFaxResource(deviceName);
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("Cannot open FAX device part! Already opened!");
        assertThat(engine.isOpened(session)).isTrue();
    }

    @Test
    public void shouldClose() throws IOException {
        // preparing test data
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        reset(engine, provider);

        // acting
        engine.close(session);

        // check the behavior
        verify(engine, never()).canFax();
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(provider).closeFaxResource(deviceHandle);
        verify(session).parameter(Device.Parameter.FAX_DEVICE_HANDLE, null);
        // check results
        assertThat(engine.isOpened(session)).isFalse();
    }

    @Test
    public void shouldReceiveFaxDocument() throws IOException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OperationResultValue result;
        File output = File.createTempFile("fax-document", ".fax");
        output.deleteOnExit();
        Runnable completeRunnable = () -> {
            File tempFaxFile = session.parameter(FaxMachineEngine.Parameter.FAX_TEMPORARY);
            try (OutputStream out = new FileOutputStream(tempFaxFile)) {
                out.write(faxContent.getBytes());
            } catch (IOException e) {
                // doing nothing here
            }
            // completing fax document transmitting operation
            session.operationComplete(Result.IO.EOF);
        };
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        executor.schedule(completeRunnable, 100, TimeUnit.MILLISECONDS);
        try (OutputStream out = new FileOutputStream(output)) {
            result = engine.receive(session, out, poolingMode, issueVoiceRequest);
        }

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(session).operationComplete(Result.IO.EOF);
        verify(session).setState(Device.State.IDLE);
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(Result.IO.EOF);
        assertThat(output.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(output)))) {
            assertThat(in.readLine()).isEqualTo(faxContent);
        }
        assertThat(output.delete()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
    }

    @Test
    public void shouldNotReceiveFaxDocument_SessionIsNotOpened() {
        // preparing test data
        OutputStream out = mock(OutputStream.class);

        // acting
        OperationResultValue result = engine.receive(session, out, true, false);

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, never()).getProvider();
        // check results
        assertThat(result).isSameAs(Result.ERROR);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_DidNotStartFaxOperation() throws IOException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);

        // acting
        result = engine.receive(session, out, poolingMode, issueVoiceRequest);

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(device).dispatchError(exceptionCaptor.capture(), eq("Cannot  receive fax file"));
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(provider, never()).stopFaxReceiving(deviceHandle);
        // check results
        Exception exception = exceptionCaptor.getValue();
        assertThat(exception).isInstanceOf(IOException.class);
        assertThat(exception.getMessage()).isEqualTo("Cannot start FAX receiving");
        assertThat(result).isSameAs(Result.ERROR);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_Disconnected() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue reason = Result.CALL.DISCONNECT;
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        Future<OperationResultValue> acting = executor.submit(
                () -> engine.receive(session, out, poolingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            session.alive(false);
            session.operationComplete(Result.CALL.DISCONNECT);
        }, 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(session).alive(false);
        verify(session).operationComplete(Result.CALL.DISCONNECT);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Receive fax document is failed.");
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(result).isSameAs(reason);
        assertThat(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_DeviceError() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue reason = Result.ERROR;
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        Future<OperationResultValue> acting = executor.submit(
                () -> engine.receive(session, out, poolingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Receive fax document is failed.");
        // check results
        assertThat(result).isSameAs(reason);
        assertThat(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_FaxTransmittingTimeout() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue reason = Result.TIMEOUT;
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        Future<OperationResultValue> acting = executor.submit(
                () -> engine.receive(session, out, poolingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Receive fax document is failed.");
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(result).isSameAs(reason);
        assertThat(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_FailedCommunicationError() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue reason = Result.FAX.COMMUNICATION_ERROR;
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        Future<OperationResultValue> acting = executor.submit(
                () -> engine.receive(session, out, poolingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Receive fax document is failed.");
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(result).isSameAs(reason);
        assertThat(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_FailedNoPoll() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue reason = Result.FAX.NO_POLL;
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        Future<OperationResultValue> acting = executor.submit(
                () -> engine.receive(session, out, poolingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Receive fax document is failed.");
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(result).isSameAs(reason);
        assertThat(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotReceiveFaxDocument_FailedUserStop() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OutputStream out = mock(OutputStream.class);
        OperationResultValue reason = Result.FAX.USER_STOP;
        OperationResultValue result;
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        Future<OperationResultValue> acting = executor.submit(
                () -> engine.receive(session, out, poolingMode, issueVoiceRequest)
        );
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(out, never()).write(any(), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Receive fax document is failed.");
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(result).isSameAs(reason);
        assertThat(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldTerminateFaxReceiving() throws InterruptedException, IOException {
        // preparing test data
        String faxContent = "Fax Document Content";
        boolean poolingMode = true;
        boolean issueVoiceRequest = true;
        OperationResultValue result;
        File output = File.createTempFile("fax-document", ".fax");
        output.deleteOnExit();
        Runnable completeRunnable = () -> {
            File tempFaxFile = session.parameter(FaxMachineEngine.Parameter.FAX_TEMPORARY);
            try (OutputStream out = new FileOutputStream(tempFaxFile)) {
                out.write(faxContent.getBytes());
                // terminating fax document transmitting operation
                engine.terminate(session);
            } catch (IOException e) {
                // doing nothing here
            }
        };
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));

        // acting
        executor.schedule(completeRunnable, 100, TimeUnit.MILLISECONDS);
        try (OutputStream out = new FileOutputStream(output)) {
            result = engine.receive(session, out, poolingMode, issueVoiceRequest);
        }

        // check the behavior
        verify(engine, atLeastOnce()).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        if (poolingMode) {
            verify(provider).enableEvents(deviceHandle, Result.FAX.POLLING);
        }
        verify(session).setState(TelephonyDevice.State.RECVFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxReceiving(eq(deviceHandle), anyString(), eq(issueVoiceRequest));
        verify(session).waitForOperationComplete(anyLong());
        verify(engine).terminate(session);
        verify(session).operationComplete(Result.TERMINATED);
        verify(session).terminate();
        verify(session).setState(Device.State.IDLE);
        verify(provider).stopFaxReceiving(deviceHandle);
        // check results
        assertThat(session.isTerminated()).isTrue();
        assertThat(result).isSameAs(Result.TERMINATED);
        assertThat(output.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(output)))) {
            assertThat(in.readLine()).isNull();
        }
        assertThat(output.delete()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
    }

    @Test
    public void shouldTransmitFaxDocument() throws IOException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        Runnable completeRunnable = () -> {
            // completing fax document transmitting operation
            session.operationComplete(Result.IO.EOF);
        };
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        executor.schedule(completeRunnable, 100, TimeUnit.MILLISECONDS);
        try (InputStream in = new FileInputStream(tempFile)) {
            result = engine.transmit(session, in, format, issueVoiceRequest);
        }

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(session).operationComplete(Result.IO.EOF);
        verify(session).setState(Device.State.IDLE);
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(session.isTerminated()).isFalse();
        assertThat(result).isSameAs(Result.IO.EOF);
        assertThat(tempFile.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(faxContent);
        }
        assertThat(tempFile.delete()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
    }

    @Test
    public void shouldNotTransmitFaxDocument_SessionIsNotOpened() {
        // preparing test data
        InputStream in = mock(InputStream.class);

        // acting
        OperationResultValue result = engine.transmit(session, in, null, false);

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, never()).getProvider();
        // check results
        assertThat(result).isSameAs(Result.ERROR);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_DidNotStartFaxOperation() throws IOException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);

        // acting
        try (InputStream in = new FileInputStream(tempFile)) {
            result = engine.transmit(session, in, format, issueVoiceRequest);
        }

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(device).dispatchError(exceptionCaptor.capture(), eq("Cannot send fax file"));
        verify(provider, never()).stopFaxTransmitting(deviceHandle);
        verify(session, never()).operationComplete(any(OperationResultValue.class));
        // check results
        Exception exception = exceptionCaptor.getValue();
        assertThat(exception).isInstanceOf(IOException.class);
        assertThat(exception.getMessage()).isEqualTo("Cannot start FAX sending");
        assertThat(result).isSameAs(Result.ERROR);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_Disconnected() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.CALL.DISCONNECT;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> {
            session.alive(false);
            session.operationComplete(reason);
        }, 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, reason);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).alive(false);
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_FaxTransmittingTimeout() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.TIMEOUT;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_FaxDeviceError() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.ERROR;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_FailedCommunicationError() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.FAX.COMMUNICATION_ERROR;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_FailedCompatibility() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.FAX.COMPATIBILITY;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_FailedNoPoll() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.FAX.NO_POLL;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldNotTransmitFaxDocument_FailedUserStop() throws IOException, ExecutionException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue reason = Result.FAX.USER_STOP;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        Future<OperationResultValue> acting = executor.submit(() -> {
            try (InputStream in = new FileInputStream(tempFile)) {
                return engine.transmit(session, in, format, issueVoiceRequest);
            } catch (IOException e) {
                return Result.ERROR;
            }
        });
        await().until(() -> session.operationIsActive());
        executor.schedule(() -> session.operationComplete(reason), 50, TimeUnit.MILLISECONDS);
        result = acting.get();

        // check the behavior
        verify(engine).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(reason);
        verify(session).setState(Device.State.ERROR);
        verify(device).dispatchError("Send fax document is failed.");
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(result).isSameAs(session.operationResult()).isSameAs(reason);
        assertThat(session.getState()).isEqualTo(Device.State.ERROR);
    }

    @Test
    public void shouldTerminateFaxTransmitting() throws IOException, InterruptedException {
        // preparing test data
        String faxContent = "Fax Document Content";
        Fax format = Fax.TEXT;
        boolean issueVoiceRequest = true;
        OperationResultValue result;
        File tempFile = File.createTempFile("fax-document", ".tiff");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            out.write(faxContent.getBytes());
        }
        tempFile.deleteOnExit();
        Runnable completeRunnable = () -> {
            try {
                // terminating fax document transmitting operation
                engine.terminate(session);
            } catch (IOException e) {
                // doing nothing here
            }
        };
        engine.uses(device);
        doReturn(true).when(engine).canFax();
        doReturn(deviceHandle).when(provider).openFaxResource(deviceName);
        engine.open(session);
        assertThat(engine.isOpened(session)).isTrue();
        session.alive(true);
        reset(engine, provider);
        doReturn(true).when(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());

        // acting
        executor.schedule(completeRunnable, 100, TimeUnit.MILLISECONDS);
        try (InputStream in = new FileInputStream(tempFile)) {
            result = engine.transmit(session, in, format, issueVoiceRequest);
        }

        // check the behavior
        verify(engine, atLeastOnce()).isOpened(session);
        verify(device, atLeastOnce()).getProvider();
        verify(session, atLeastOnce()).parameter(Device.Parameter.FAX_DEVICE_HANDLE);
        verify(provider, atLeastOnce()).disableEvents(deviceHandle);
        verify(provider).enableEvents(deviceHandle, Result.CALL.DISCONNECT);
        verify(session).setState(TelephonyDevice.State.SENDFAX);
        verify(session).parameter(eq(FaxMachineEngine.Parameter.FAX_TEMPORARY), any(File.class));
        verify(provider).startFaxTransmitting(eq(deviceHandle), anyString(), eq(issueVoiceRequest),
                eq(format.isTIFF()), eq(format.isHighResolution()), anyInt(), anyInt());
        verify(session).operationComplete(Result.NONE);
        verify(session).waitForOperationComplete(anyLong());
        verify(engine).terminate(session);
        verify(session).operationComplete(Result.TERMINATED);
        verify(session).terminate();
        verify(session).setState(Device.State.IDLE);
        verify(provider).stopFaxTransmitting(deviceHandle);
        // check results
        assertThat(session.isTerminated()).isTrue();
        assertThat(result).isSameAs(Result.TERMINATED);
        assertThat(tempFile.exists()).isTrue();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)))) {
            assertThat(in.readLine()).isEqualTo(faxContent);
        }
        assertThat(tempFile.delete()).isTrue();
        assertThat(session.getState()).isEqualTo(Device.State.IDLE);
    }
}