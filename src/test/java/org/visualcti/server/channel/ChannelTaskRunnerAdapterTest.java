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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.core.channel.Channel;
import org.visualcti.server.core.channel.TaskRunnerGroup;
import org.visualcti.server.core.channel.device.Device;
import org.visualcti.server.core.channel.device.DeviceEvent;
import org.visualcti.server.core.channel.device.Factory;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.task.Environment;

public class ChannelTaskRunnerAdapterTest {
    TaskRunnerGroup group;
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
        runner = spy(new ChannelTaskRunnerAdapter(environment, channel, tasksPoolUnit));
        group = mock(TaskRunnerGroup.class);
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
    public void runChannelTask() {
    }

    @Test
    public void attachTask() {
    }

    @Test
    public void detachTask() {
    }

    @Test
    public void eventCompliesDevice() {
    }

    @Test
    public void setXML() {
    }
}