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
package org.visualcti.core.channel.device.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.message.UnitMessage;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractEventProcessorTest {
    String processorType = "abstract-event-processor-test";
    String processorName = "AbstractEventProcessorTest";
    AbstractEventProcessor processor;
    Executor deviceEventExecutor;
    ExecutorService shadowExecutor;
    DeviceEvent.Provider eventsProvider;
    DeviceEvent.Listener.Hub eventListenersHub;

    @Before
    public void setUp() {
        deviceEventExecutor = mock(Executor.class);
        shadowExecutor = Executors.newFixedThreadPool(10);
        doAnswer(invocation -> {
            shadowExecutor.execute(invocation.getArgument(0, Runnable.class));
            return null;
        }).when(deviceEventExecutor).execute(any(Runnable.class));
        eventsProvider = mock(DeviceEvent.Provider.class);
        eventListenersHub = spy(new AbstractEventListenersHub() {
        });
        processor = spy(new AbstractEventProcessor(deviceEventExecutor, eventsProvider, eventListenersHub) {
            {
                unitState.getAndSet(UnitState.ACTIVE);
            }

            @Override
            public String getName() {
                return processorName;
            }

            @Override
            public String getType() {
                return processorType;
            }
        });
    }

    @Test
    public void shouldSetupEventsGrabberThread() {
        // preparing test data
        Thread thread = mock(Thread.class);
        assertThat(processor.providerEventsThread.get()).isNull();

        // acting
        processor.eventsGrabberThread(thread);

        // check the behavior
        // check results
        assertThat(processor.providerEventsThread.get()).isSameAs(thread);
    }

    @Test
    public void shouldGetHowLongWaitForDeviceEvent() {
        // preparing test data
        long howLong = 200L;
        processor.howLongWaitForDeviceEvent = howLong;

        // acting
        long actual = processor.getHowLongWaitForDeviceEvent();

        // check the behavior
        // check results
        assertThat(actual).isEqualTo(howLong);
    }

    @Test
    public void shouldBeNoUnprocessedEventsThere() {
        // preparing test data

        // acting
        boolean isThere = processor.isThereNoUnprocessedEvents();

        // check the behavior
        // check results
        assertThat(isThere).isTrue();
    }

    @Test
    public void shouldBeUnprocessedEventsThere() {
        // preparing test data
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        processor.onDeviceEvent(deviceEvent);

        // acting
        boolean isThere = processor.isThereNoUnprocessedEvents();

        // check the behavior
        // check results
        assertThat(isThere).isFalse();
    }

    @Test
    public void shouldSendForProcessing() {
        // preparing test data
        assertThat(processor.isThereNoUnprocessedEvents()).isTrue();
        DeviceEvent deviceEvent = mock(DeviceEvent.class);

        // acting
        processor.onDeviceEvent(deviceEvent);

        // check the behavior
        // check results
        assertThat(processor.isThereNoUnprocessedEvents()).isFalse();
    }

    @Test
    public void shouldTakeSentEvent_ByTimeout() throws InterruptedException {
        // preparing test data
        long howLong = 200L;
        processor.howLongWaitForDeviceEvent = howLong;

        // acting
        DeviceEvent deviceEvent = processor.takeDeviceEvent();

        // check the behavior
        // check results
        assertThat(deviceEvent).isNull();
    }

    @Test
    public void shouldTakeSentEvent_EventSent() throws InterruptedException {
        // preparing test data
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        processor.onDeviceEvent(deviceEvent);

        // acting
        DeviceEvent takenEvent = processor.takeDeviceEvent();

        // check the behavior
        // check results
        assertThat(takenEvent).isEqualTo(deviceEvent);
    }

    @Test
    public void shouldBeNotTakenTheBreak() throws InterruptedException, ExecutionException {
        // preparing test data
        long howLong = 5000L;
        processor.howLongWaitForDeviceEvent = howLong;
        AtomicReference<Thread> takenThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Future<Boolean> takenBreak = shadowExecutor.submit(() -> {
            takenThread.set(Thread.currentThread());
            latch.countDown();
            return processor.isNotTakenTheBreak();
        });
        latch.await();
        takenThread.get().interrupt();

        // acting
        boolean done = takenBreak.get();

        // check the behavior
        verify(processor).dispatch(any(UnitMessage.class));
        // check results
        assertThat(done).isTrue();
    }

    @Test
    public void shouldBeTakenTheBreak() {
        // preparing test data
        long howLong = 50L;
        processor.howLongWaitForDeviceEvent = howLong;

        // acting
        boolean done = processor.isNotTakenTheBreak();

        // check the behavior
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldGrabProviderEvents() throws InterruptedException, ExecutionException {
        // preparing test data
        assertThat(processor.isThereNoUnprocessedEvents()).isTrue();
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        doReturn(Optional.of(deviceEvent)).when(eventsProvider).getEvent(anyLong());
        processor.onDeviceEvent(deviceEvent);
        processor.onDeviceEvent(DeviceEvent.EMPTY);
        AtomicReference<Thread> grabbingThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Future<?> grabEvents = shadowExecutor.submit(() -> {
            grabbingThread.getAndSet(Thread.currentThread());
            latch.countDown();
            processor.grabProviderEvents();
        });
        latch.await();

        // acting
        Thread.sleep(50L);
        processor.currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        grabEvents.get();

        // check the behavior
        verify(processor, atLeastOnce()).isStarted();
        verify(processor, atLeastOnce()).getProvider();
        verify(eventsProvider, atLeastOnce()).getEvent(anyLong());
        verify(processor, atLeastOnce()).onDeviceEvent(deviceEvent);
        // check results
        assertThat(processor.isThereNoUnprocessedEvents()).isFalse();
    }

    @Test
    public void shouldNotifyListeners_NoEventRejection() {
        // preparing test data
        String deviceName = "device-name";
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        doReturn(deviceName).when(deviceEvent).getDeviceName();
        DeviceEvent.Listener deviceEventListener = mock(DeviceEvent.Listener.class);
        doReturn(true).when(deviceEventListener).accept(deviceEvent);
        processor.getHub().addDeviceEventListenerFor(deviceName, deviceEventListener);
        reset(processor, eventListenersHub);

        // acting
        processor.notifyListeners(deviceEvent);

        // check the behavior
        verify(processor).getHub();
        verify(eventListenersHub).eventListeners(deviceName);
        verify(deviceEventListener).accept(deviceEvent);
        verify(processor, never()).dispatchError(anyString());
        verify(eventsProvider, never()).reject(any(DeviceEvent.class));
        // check results
    }

    @Test
    public void shouldNotifyListeners_WithEventRejection() {
        // preparing test data
        String deviceName = "device-name";
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        doReturn(deviceName).when(deviceEvent).getDeviceName();
        DeviceEvent.Listener deviceEventListener = mock(DeviceEvent.Listener.class);
        processor.getHub().addDeviceEventListenerFor(deviceName, deviceEventListener);
        reset(processor, eventListenersHub);

        // acting
        processor.notifyListeners(deviceEvent);

        // check the behavior
        verify(processor).getHub();
        verify(eventListenersHub).eventListeners(deviceName);
        verify(deviceEventListener).accept(deviceEvent);
        verify(processor).dispatchError(anyString());
        verify(eventsProvider).reject(any(DeviceEvent.class));
        // check results
    }

    @Test
    public void shouldDoProcessingDeviceEvents() throws InterruptedException {
        // preparing test data
        String deviceName = "device-name";
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        doReturn(deviceName).when(deviceEvent).getDeviceName();
        DeviceEvent.Listener deviceEventListener = mock(DeviceEvent.Listener.class);
        doReturn(true).when(deviceEventListener).accept(deviceEvent);
        processor.getHub().addDeviceEventListenerFor(deviceName, deviceEventListener);
        doReturn(Optional.of(deviceEvent)).when(eventsProvider).getEvent(anyLong());
        processor.currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        processor.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(processor);
        shadowExecutor.execute(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // doing nothing here
            }
            processor.currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        });

        // acting
        processor.processingDeviceEvents();

        // check the behavior
        verify(processor, atLeastOnce()).isStarted();
        verify(processor, atLeastOnce()).takeDeviceEvent();
        verify(deviceEvent, atLeastOnce()).getDeviceName();
        verify(processor, atLeastOnce()).notifyListeners(deviceEvent);
        verify(processor, never()).isNotTakenTheBreak();
        // check results
    }

    @Test
    public void shouldDoNotProcessingDeviceEvents_PassiveState() throws InterruptedException {
        // preparing test data
        processor.currentUnitState(RunnableServerUnit.UnitState.PASSIVE);

        // acting
        processor.processingDeviceEvents();

        // check the behavior
        verify(processor).isStarted();
        // grabbed events processing
        verify(processor, never()).takeDeviceEvent();
        // grabbing device events provider events and put for processing
        verify(processor, never()).eventsGrabberThread(any(Thread.class));
        // check results
    }

    @Test
    public void shouldSetupActiveCurrentUnitState() throws InterruptedException {
        // preparing test data
        AbstractEventProcessor testProcessor = spy(
                new AbstractEventProcessor(deviceEventExecutor, eventsProvider, eventListenersHub) {
                    @Override
                    public String getName() {
                        return processorName;
                    }

                    @Override
                    public String getType() {
                        return processorType;
                    }
                }
        );
        String deviceName = "device-name";
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        doReturn(deviceName).when(deviceEvent).getDeviceName();
        DeviceEvent.Listener deviceEventListener = mock(DeviceEvent.Listener.class);
        doReturn(true).when(deviceEventListener).accept(deviceEvent);
        doReturn(Optional.of(deviceEvent)).when(eventsProvider).getEvent(anyLong());
        testProcessor.getHub().addDeviceEventListenerFor(deviceName, deviceEventListener);

        // acting
        testProcessor.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Thread.sleep(50);
        // check results
        assertThat(testProcessor.providerEventsThread.get()).isNotNull();
        testProcessor.currentUnitState(RunnableServerUnit.UnitState.PASSIVE);

        // check the behavior
        verify(deviceEventExecutor, times(2)).execute(any(Runnable.class));
        // working threads activities check
        verify(testProcessor, atLeastOnce()).isStarted();
        // grabbed events processing
        verify(testProcessor, atLeastOnce()).takeDeviceEvent();
        verify(deviceEvent, atLeastOnce()).getDeviceName();
        verify(testProcessor, atLeastOnce()).notifyListeners(deviceEvent);
        verify(testProcessor, never()).isNotTakenTheBreak();
        // grabbing device events provider events and put for processing
        verify(testProcessor).eventsGrabberThread(any(Thread.class));
        verify(testProcessor, atLeastOnce()).getProvider();
        verify(testProcessor, atLeastOnce()).getHowLongWaitForDeviceEvent();
        verify(eventsProvider, atLeastOnce()).getEvent(anyLong());
        verify(testProcessor, atLeastOnce()).onDeviceEvent(deviceEvent);
        // check results
    }

    @Test
    public void shouldSetupPassiveCurrentUnitState() throws InterruptedException {
        // preparing test data
        AbstractEventProcessor testProcessor = spy(
                new AbstractEventProcessor(deviceEventExecutor, eventsProvider, eventListenersHub) {
                    @Override
                    public String getName() {
                        return processorName;
                    }

                    @Override
                    public String getType() {
                        return processorType;
                    }
                }
        );
        String deviceName = "device-name";
        DeviceEvent deviceEvent = mock(DeviceEvent.class);
        doReturn(deviceName).when(deviceEvent).getDeviceName();
        DeviceEvent.Listener deviceEventListener = mock(DeviceEvent.Listener.class);
        doReturn(true).when(deviceEventListener).accept(deviceEvent);
        testProcessor.getHub().addDeviceEventListenerFor(deviceName, deviceEventListener);
        doReturn(Optional.of(deviceEvent)).when(eventsProvider).getEvent(anyLong());
        testProcessor.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Thread.sleep(50);
        assertThat(testProcessor.providerEventsThread.get()).isNotNull();
        reset(testProcessor);

        // acting
        testProcessor.currentUnitState(RunnableServerUnit.UnitState.PASSIVE);

        // check the behavior
        verify(testProcessor, atLeastOnce()).isStarted();
        verify(testProcessor).eventsGrabberThread(null);
        // check results
        assertThat(testProcessor.providerEventsThread.get()).isNull();
    }
}
