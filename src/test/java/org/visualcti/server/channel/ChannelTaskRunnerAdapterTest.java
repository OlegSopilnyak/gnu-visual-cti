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
package org.visualcti.server.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.awaitility.Duration;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.server.core.channel.ChannelTasksRuntime;
import org.visualcti.server.core.channel.TaskRunnerStream;
import org.visualcti.server.core.channel.device.DeviceEvent;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.task.Environment;

@SuppressWarnings("unchecked")
public class ChannelTaskRunnerAdapterTest {
    ChannelTasksRuntime group;
    Executor executor;
    ChannelTaskRunnerAdapter runner;
    Environment environment;
    Channel channel;
    Device device;
    Factory factory;
    TasksPoolUnit tasksPoolUnit;
    Task task;

    @Before
    public void setUp() throws IOException {
        channel = mock(Channel.class);
        doCallRealMethod().when(channel).getName();
        device = mock(Device.class);
        factory = mock(Factory.class);
        doReturn(factory).when(device).getFactory();
        doReturn(device).when(channel).getDevice();
        environment = spy(new Environment());
        tasksPoolUnit = mock(TasksPoolUnit.class);
        task = mock(Task.class);
        doReturn(task).when(tasksPoolUnit).next();
        doReturn(task).when(tasksPoolUnit).current();
        runner = spy(new ChannelTaskRunnerAdapter(environment, channel, tasksPoolUnit){});
        group = mock(ChannelTasksRuntime.class);
        runner.setOwner(group);
        executor = mock(Executor.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(executor).execute(any(Runnable.class));
        doReturn(executor).when(group).getExecutor();
    }

    @Test
    public void shouldAcceptIncomingDeviceEvent() throws IOException {
        // preparing test data
        doReturn("task-name").when(task).getName();
        DeviceEvent.Type deviceEventType = DeviceEvent.Type.INCOMING;
        String deviceName = "device-name";
        String deviceFactoryName = "device-factory-name";
        doReturn(deviceName).when(device).getName();
        doReturn(deviceFactoryName).when(factory).getVendor();
        // event
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(deviceFactoryName).when(event).getVendor();
        doReturn(deviceEventType).when(event).getEventType();
        // starting runner
        runner.Start();
        doReturn("task-name").when(task).getName();
        verify(tasksPoolUnit).Start();
        reset(runner);

        // acting
        boolean accepted = runner.accept(event);

        // check the behavior
        verify(runner, atLeastOnce()).isStarted();
        verify(runner, atLeastOnce()).getChannel();
        verify(channel, atLeastOnce()).getDevice();
        verify(channel).isBusy();
        verify(runner, atLeastOnce()).getGroup();
        verify(group).getExecutor();
        verify(executor).execute(any(Runnable.class));
        verify(runner).runChannelTask();
        verify(runner).getTasksPool();
        verify(tasksPoolUnit).next();
        verify(runner).getExclusiveAccessLock();
        verify(device, atLeastOnce()).open();
        verify(runner).attachTask(task);
        verify(runner).getEnvironment();
        verify(task).setEnv(environment);
        verify(channel).beforeStart(task);
        verify(task).execute();
        verify(runner).detachTask(task);
        verify(task).setEnv(null);
        verify(channel).afterStop(task);
        // check results
        assertThat(accepted).isTrue();
    }

    @Test
    public void shouldAcceptMalfunctionDeviceEvent() throws IOException {
        // preparing test data
        DeviceEvent.Type deviceEventType = DeviceEvent.Type.MALFUNCTION;
        String deviceName = "broken-device-name";
        String deviceFactoryName = "broken-device-factory-name";
        String eventDescription = "broken-device-description";
        doReturn(deviceName).when(device).getName();
        doReturn(deviceFactoryName).when(factory).getVendor();
        // event
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(deviceFactoryName).when(event).getVendor();
        doReturn(deviceEventType).when(event).getEventType();
        doReturn(eventDescription).when(event).getDescription();
        // starting runner
        runner.Start();
        verify(tasksPoolUnit).Start();
        reset(runner);

        // acting
        boolean accepted = runner.accept(event);

        // check the behavior
        verify(runner, atLeastOnce()).isStarted();
        verify(runner, atLeastOnce()).getChannel();
        verify(channel, atLeastOnce()).getDevice();
        verify(runner).dispatchError(eventDescription);
        verify(device).terminate();
        verify(device).repair();
        verify(runner).Stop();
        // check results
        assertThat(accepted).isTrue();
        assertThat(runner.isBroken()).isTrue();
    }

    @Test
    public void shouldNotAcceptAnyDeviceEvent_RunnerNotStarted() throws IOException {
        // preparing test data
        String deviceName = "device-name";
        String deviceFactoryName = "device-factory-name";
        doReturn(deviceName).when(device).getName();
        doReturn(deviceFactoryName).when(factory).getVendor();
        // event
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(deviceFactoryName).when(event).getVendor();

        // acting
        boolean accepted = runner.accept(event);

        // check the behavior
        verify(runner, atLeastOnce()).isStarted();
        verify(runner, atLeastOnce()).getChannel();
        verify(channel, atLeastOnce()).getDevice();
        verify(channel, never()).isBusy();
        verify(runner, never()).runChannelTask();
        // check results
        assertThat(accepted).isFalse();
    }

    @Test
    public void shouldNotAcceptIncomingDeviceEvent_ChannelIsBusy() throws IOException {
        // preparing test data
        DeviceEvent.Type deviceEventType = DeviceEvent.Type.INCOMING;
        String deviceName = "device-name";
        String deviceFactoryName = "device-factory-name";
        doReturn(deviceName).when(device).getName();
        doReturn(deviceFactoryName).when(factory).getVendor();
        // event
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(deviceFactoryName).when(event).getVendor();
        doReturn(deviceEventType).when(event).getEventType();
        // starting runner
        runner.Start();
        verify(tasksPoolUnit).Start();
        reset(runner);
        doReturn(true).when(channel).isBusy();

        // acting
        boolean accepted = runner.accept(event);

        // check the behavior
        verify(runner, atLeastOnce()).isStarted();
        verify(runner, atLeastOnce()).getChannel();
        verify(channel, atLeastOnce()).getDevice();
        verify(channel).isBusy();
        verify(runner, never()).getGroup();
        // check results
        assertThat(accepted).isFalse();
    }

    @Test
    public void shouldNotAcceptDeviceEvent_WrongEventType() throws IOException {
        // preparing test data
        DeviceEvent.Type deviceEventType = DeviceEvent.Type.DEVICE_SPECIFIC;
        String deviceName = "device-name";
        String deviceFactoryName = "device-factory-name";
        doReturn(deviceName).when(device).getName();
        doReturn(deviceFactoryName).when(factory).getVendor();
        // event
        DeviceEvent event = mock(DeviceEvent.class);
        doReturn(deviceName).when(event).getDeviceName();
        doReturn(deviceFactoryName).when(event).getVendor();
        doReturn(deviceEventType).when(event).getEventType();
        // starting runner
        runner.Start();
        verify(tasksPoolUnit).Start();
        reset(runner);

        // acting
        boolean accepted = runner.accept(event);

        // check the behavior
        verify(runner).isStarted();
        verify(runner).getChannel();
        verify(channel, atLeastOnce()).getDevice();
        verify(channel, never()).isBusy();
        verify(runner).dispatchError(anyString());
        // check results
        assertThat(accepted).isFalse();
    }

    @Test
    public void shouldRunChannelTask() throws IOException {
        // preparing test data
        doReturn("task-name").when(task).getName();
        // starting runner
        runner.Start();
        verify(tasksPoolUnit).Start();
        reset(runner, device);

        // acting
        runner.runChannelTask();

        // check the behavior
        verify(runner, times(2)).isStarted();
        verify(tasksPoolUnit).next();
        verify(runner, never()).Stop();
        verify(runner).getExclusiveAccessLock();
        verify(runner, times(3)).getChannel();
        verify(channel, times(5)).getDevice();
        verify(device).open();
        verify(runner).attachTask(task);
        verify(task).execute();
        verify(runner).detachTask(task);
        verify(channel).onlineTasksCount();
    }

    @Test
    public void shouldNotRunChannelTask_RunnerIsNotStarted() throws IOException {
        // preparing test data

        // acting
        runner.runChannelTask();

        // check the behavior
        verify(runner, times(2)).isStarted();
        verify(tasksPoolUnit, never()).next();
    }

    @Test
    public void shouldAttachTask() {
        // preparing test data
        String taskName = "task-name";
        Task taskToAttach = mock(Task.class);
        doReturn(taskName).when(taskToAttach).getName();
        assertThat(runner.getChannel().onlineTasksCount()).isZero();
        reset(runner);

        // acting
        runner.attachTask(taskToAttach);

        // check the behavior
        verify(runner).getEnvironment();
        verify(runner).getGroup();
        verify(taskToAttach).setEnv(environment);
        verify(taskToAttach, times(2)).getName();
        verify(runner).dispatchEvent(anyString());
        verify(channel).beforeStart(taskToAttach);
        // check results
        assertThat(runner.getChannel().onlineTasksCount()).isEqualTo(1);
    }

    @Test
    public void shouldDetachTask() {
        // preparing test data
        String taskName = "task-name";
        Task taskToDetach = mock(Task.class);
        doReturn(taskName).when(taskToDetach).getName();

        // acting
        runner.detachTask(taskToDetach);

        // check the behavior
        verify(taskToDetach).setEnv(null);
        verify(runner).dispatchEvent(anyString());
        verify(channel).afterStop(taskToDetach);
        // check results
        assertThat(runner.getChannel().onlineTasksCount()).isZero();
    }

    @Test
    public void shouldGetExecutingTaskCount() {
        // preparing test data
        int taskQuantity = 10;
        String taskName = "task-name";
        Task taskToAttach = mock(Task.class);
        doReturn(taskName).when(taskToAttach).getName();
        assertThat(runner.getChannel().onlineTasksCount()).isZero();
        reset(runner);

        // acting
        IntStream.range(0, taskQuantity).forEach(i -> runner.attachTask(taskToAttach));

        // check results
        assertThat(runner.getChannel().onlineTasksCount()).isEqualTo(taskQuantity);
        assertThat(channel.getOnlineTasks()).containsEntry(taskName, taskQuantity);

        // acting
        IntStream.range(0, taskQuantity).forEach(i -> runner.detachTask(taskToAttach));

        // check results
        assertThat(runner.getChannel().onlineTasksCount()).isZero();
        assertThat(channel.getOnlineTasks()).containsEntry(taskName, 0);
    }

    @Test
    public void shouldStartUnitRunnable() throws IOException {
        // preparing test data
        String deviceName = "device-name";
        String deviceFactoryVendorName = "vendor-name";
        String channelDeviceName = "/channel/device/" + deviceFactoryVendorName + "/" + deviceName;
        doReturn(deviceName).when(device).getName();
        doReturn(deviceFactoryVendorName).when(factory).getVendor();
        doCallRealMethod().when(device).getDeviceName();

        // acting
        runner.startUnitRunnable();

        // check the behavior
        verify(runner, times(2)).getChannel();
        verify(channel, times(3)).getDevice();
        verify(device).isOpened();
        verify(device).open();
        verify(tasksPoolUnit).Start();
        // check default environment building sequence
        verify(runner).getEnvironment();
        verify(environment).clear();
        verify(device).getDeviceName();
        verify(factory).getVendor();
        verify(device, times(2)).getName();
        verify(environment).setPart(anyString(), eq(channelDeviceName));
        verify(environment).setPart(channelDeviceName, device);
        verify(environment, times(2)).setPart(anyString(), any(TaskRunnerStream.class));
    }

    @Test
    public void shouldStopUnitRunnable_ChannelDeviceOpened() throws IOException {
        // preparing test data
        doReturn(true).when(device).isOpened();

        // acting
        runner.stopUnitRunnable();

        // check the behavior
        verify(runner).getChannel();
        verify(channel, times(2)).getDevice();
        verify(device).isOpened();
        verify(device).close();
        verify(tasksPoolUnit).Stop();
        verify(runner).getEnvironment();
        verify(environment).clear();
    }

    @Test
    public void shouldStopUnitRunnable_ChannelDeviceIsNotOpened() throws IOException {
        // preparing test data

        // acting
        runner.stopUnitRunnable();

        // check the behavior
        verify(runner).getChannel();
        verify(channel, times(2)).getDevice();
        verify(device).isOpened();
        verify(device, never()).close();
        verify(tasksPoolUnit).Stop();
        verify(runner).getEnvironment();
        verify(environment).clear();
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        Element element = new Element("xml");

        // acting
        runner.setXML(element);

        // check the behavior
        verify(runner).settingUpBasePart(element);
        verify(runner).settingUpMainPart(element);
    }

    @Test
    public void shouldGetXML() {
        // preparing test data
        String service = "ChannelTaskRunner";

        // acting
        Element xml = runner.getXML();

        // check the behavior
        // check results
        assertThat(xml).isNotNull();
        assertThat(xml.getName()).isEqualTo(service);
        assertThat(xml.getAttributeValue("class")).contains(service);
        assertThat(xml.getAttributeValue("extends")).endsWith(service);
    }

    @Test
    public void shouldStartChannelTaskRunner() throws IOException {
        // preparing test data
        doReturn("task-name").when(task).getName();
        assertThat(runner.getNextRunnerStepExecutor()).isNull();
        AtomicBoolean acceptRunning = new AtomicBoolean(false);
        doAnswer(invocation -> {
            acceptRunning.getAndSet(true);
            return invocation.callRealMethod();
        }).when(runner).accept(any(DeviceEvent.class));

        // acting
        runner.Start();

        // check the behavior
        verify(runner).startUnitRunnable();
        verify(channel).onlineTasksCount();
        verify(runner).dispatchEvent("Pushing the next iteration event");
        await().atMost(Duration.ONE_SECOND).until(acceptRunning::get);
        verify(runner).accept(any(DeviceEvent.class));
        verify(runner).runChannelTask();
        // check results
        assertThat(runner.getNextRunnerStepExecutor()).isNotNull();
    }

    @Test
    public void shouldStopChannelTaskRunner() throws IOException {
        // preparing test data
        doReturn("task-name").when(task).getName();
        runner.Start();
        assertThat(runner.getNextRunnerStepExecutor()).isNotNull();

        // acting
        runner.Stop();

        // check the behavior
        verify(runner).stopUnitRunnable();
        // check results
        assertThat(runner.getNextRunnerStepExecutor()).isNull();
    }
}