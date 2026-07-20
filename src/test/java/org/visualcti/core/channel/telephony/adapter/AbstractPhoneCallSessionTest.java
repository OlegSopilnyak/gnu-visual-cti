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
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractPhoneCallSessionTest {
    PhoneCallSession<String> session;
    TelephonyDevice<String, ?> device;
    String deviceName = "device-name";
    String handle = "device-handle";
    ExecutorService executor;

    @Before
    public void setUp() throws Exception {
        device = mock(TelephonyDevice.class);
        session = spy(new PhoneCallSession(device, handle) {
        });
        executor = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() {
        executor.shutdown();
        executor = null;
    }

    @Test
    public void shouldGetDeviceName() {
        // preparing test data
        doReturn(deviceName).when(device).getName();

        // acting
        String sessionDeviceName = session.getDeviceName();

        // check results
        assertThat(sessionDeviceName).isSameAs(deviceName);
    }

    @Test
    public void shouldBeAlive() {
        // preparing test data
        assertThat(session.isAlive()).isFalse();

        // acting
        session.alive = true;

        // check results
        assertThat(session.isAlive()).isTrue();
    }

    @Test
    public void shouldGetOperationResult() {
        // preparing test data
        OperationResultValue result = mock(OperationResultValue.class);
        session.operationResult.getAndSet(result);

        // acting
        OperationResultValue operationResultValue = session.operationResult();

        // check results
        assertThat(operationResultValue).isSameAs(result);
    }

    @Test
    public void shouldSetOperationResult() {
        // preparing test data
        OperationResultValue result = mock(OperationResultValue.class);
        assertThat(session.operationResult()).isSameAs(Result.NONE);

        // acting
        PhoneCallSession<String> updated = session.operationResult(result);

        // check results
        assertThat(updated).isSameAs(session);
        assertThat(session.operationResult()).isSameAs(result);
    }

    @Test
    public void shouldGetCalledNumber() {
        // preparing test data

        // acting
        PhoneCall.Number number = session.getCalledNumber();

        // check results
        assertThat(number).isSameAs(PhoneCall.Number.EMPTY);
    }


    @Test
    public void shouldSetCalledNumber() {
        // preparing test data
        PhoneCall.Number result = mock(PhoneCall.Number.class);
        assertThat(session.getCalledNumber()).isSameAs(PhoneCall.Number.EMPTY);

        // acting
        PhoneCallSession<String> updated = session.calledNumber(result);

        // check results
        assertThat(updated).isSameAs(session);
        assertThat(updated.getCalledNumber()).isSameAs(result);
    }

    @Test
    public void shouldGetCallingNumber() {
        // preparing test data

        // acting
        PhoneCall.Number number = session.getCallingNumber();

        // check results
        assertThat(number).isSameAs(PhoneCall.Number.EMPTY);
    }

    @Test
    public void shouldSetCallingNumber() {
        // preparing test data
        PhoneCall.Number result = mock(PhoneCall.Number.class);
        assertThat(session.getCallingNumber()).isSameAs(PhoneCall.Number.EMPTY);

        // acting
        PhoneCallSession<String> updated = session.callingNumber(result);

        // check results
        assertThat(updated).isSameAs(session);
        assertThat(updated.getCallingNumber()).isSameAs(result);
    }

    @Test
    public void shouldWaitForOperationComplete_Interrupted() throws InterruptedException {
        // preparing test data
        session.operationResult.getAndSet(Result.ERROR);
        Runnable runnable = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // doing nothing
            }
            session.operationComplete(Result.OK);
        };
        executor.execute(runnable);

        // acting
        session.waitForOperationComplete(200L);

        // check results
        assertThat(session.operationResult()).isSameAs(Result.OK);
    }

    @Test
    public void shouldWaitForOperationComplete_TimedOut() throws InterruptedException {
        // preparing test data
        session.operationResult.getAndSet(Result.ERROR);

        // acting
        session.waitForOperationComplete(200L);

        // check results
        assertThat(session.operationResult()).isSameAs(Result.NONE);
    }

    @Test
    public void shouldCompleteTheOperationAndUpdateLastOperationResult() throws InterruptedException {
        // preparing test data
        session.operationResult.getAndSet(Result.ERROR);
        executor.execute(() -> {
            try {
                session.waitForOperationComplete(-1L);
            } catch (InterruptedException e) {
                // doing nothing here
            }
        });
        await().until(() -> session.operationResult() == Result.NONE);

        // acting
        session.operationComplete(Result.TERMINATED);

        // check results
        assertThat(session.operationResult()).isSameAs(Result.TERMINATED);
    }

    @Test
    public void shouldStreamJoint_AnotherJoint() {
        // preparing test data
        PhoneCall anotherCall = mock(PhoneCall.class);
        session.join(anotherCall);

        // acting
        Stream<PhoneCall> joint = session.joint();

        // check results
        assertThat(joint.toArray()).containsExactly(anotherCall);
    }

    @Test
    public void shouldStreamJoint_NoJoint() {
        // preparing test data

        // acting
        Stream<PhoneCall> joint = session.joint();

        // check results
        assertThat(joint.toArray()).isEmpty();
    }

    @Test
    public void shouldJoin() {
        // preparing test data
        PhoneCall anotherCall = mock(PhoneCall.class);
        assertThat(session.joint().toArray()).isEmpty();

        // acting
        session.join(anotherCall);

        // check results
        assertThat(session.joint().toArray()).containsExactly(anotherCall);
    }

    @Test
    public void shouldClose() throws IOException {
        // preparing test data
        PhoneCall anotherCall = mock(PhoneCall.class);
        session.join(anotherCall);

        // acting
        session.close();

        // check the behavior
        verify(anotherCall).close();
        // check results
        assertThat(session.joint().toArray()).isEmpty();
        assertThat(session.operationResult()).isSameAs(Result.TERMINATED);
    }

    @Test
    public void shouldAccept_DeviceSpecificEvent() {
        // preparing test data
        doReturn(deviceName).when(device).getName();
        TelephonyDeviceFactory<String, ?> factory = mock(TelephonyDeviceFactory.class);
        doReturn(factory).when(device).getFactory();
        DeviceEvent<String> event = spy(AbstractDeviceEvent.<String>of(DeviceEvent.Type.DEVICE_SPECIFIC)
                .deviceName(deviceName).deviceHandle(handle)
                .option(DeviceEvent.Option.REASON, Result.CALL.RINGS)
        );

        // acting
        boolean succeed = session.accept(event);

        // check the behavior
        verify(session).proceedDeviceSpecificEvent(event);
        // check results
        assertThat(succeed).isTrue();
    }

    @Test
    public void shouldAccept_NotDeviceSpecificEvent() {
        // preparing test data
        doReturn(deviceName).when(device).getName();
        TelephonyDeviceFactory<String, ?> factory = mock(TelephonyDeviceFactory.class);
        doReturn(factory).when(device).getFactory();
        DeviceEvent<String> event = spy(AbstractDeviceEvent.<String>of(DeviceEvent.Type.INCOMING)
                .deviceName(deviceName).deviceHandle(handle)
                .option(DeviceEvent.Option.REASON, Result.CALL.RINGS)
        );

        // acting
        boolean succeed = session.accept(event);

        // check the behavior
        verify(session, never()).proceedDeviceSpecificEvent(event);
        // check results
        assertThat(succeed).isTrue();
    }

    @Test
    public void shouldProceedDeviceSpecificEvent_RingsReason_Idle() {
        // preparing test data
        doReturn(deviceName).when(device).getName();
        TelephonyDeviceFactory<String, ?> factory = mock(TelephonyDeviceFactory.class);
        doReturn(factory).when(device).getFactory();
        DeviceEvent<String> event = spy(AbstractDeviceEvent.<String>of(DeviceEvent.Type.DEVICE_SPECIFIC)
                .deviceName(deviceName).deviceHandle(handle)
                .option(DeviceEvent.Option.REASON, Result.CALL.RINGS)
        );

        // acting
        session.proceedDeviceSpecificEvent(event);

        // check the behavior
        ArgumentCaptor<DeviceEvent<String>> captor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(factory).onDeviceEvent(captor.capture());
        DeviceEvent<?> sentEvent = captor.getValue();
        // check results
        assertThat(session.getState()).isSameAs(Device.State.IDLE);
        assertThat(sentEvent.getEventType()).isSameAs(DeviceEvent.Type.INCOMING);
        assertThat(sentEvent.getDeviceName()).isEqualTo(event.getDeviceName());
        assertThat(sentEvent.getDeviceHandle()).isEqualTo(event.getDeviceHandle());
        assertThat(sentEvent.getOption(DeviceEvent.Option.REASON))
                .isEqualTo(event.getOption(DeviceEvent.Option.REASON))
                .contains(Result.CALL.RINGS);
    }

    @Test
    public void shouldProceedDeviceSpecificEvent_RingsReason_Waiting() {
        // preparing test data
        session.setState(TelephonyDevice.State.WAIT);
        doReturn(deviceName).when(device).getName();
        TelephonyDeviceFactory<String, ?> factory = mock(TelephonyDeviceFactory.class);
        doReturn(factory).when(device).getFactory();
        DeviceEvent<String> event = spy(AbstractDeviceEvent.<String>of(DeviceEvent.Type.DEVICE_SPECIFIC)
                .deviceName(deviceName).deviceHandle(handle)
                .option(DeviceEvent.Option.REASON, Result.CALL.RINGS)
        );

        // acting
        session.proceedDeviceSpecificEvent(event);

        // check the behavior
        verify(session).operationComplete(Result.CALL.RINGS);
        verify(factory, never()).onDeviceEvent(any(DeviceEvent.class));
        // check results
        assertThat(session.getState()).isSameAs(TelephonyDevice.State.WAIT);
    }
}
