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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.channel.Channel;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.server.core.channel.device.Device;
import org.visualcti.server.core.channel.device.DeviceEvent;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.NoSuchUnitException;
import org.visualcti.server.task.TaskPoolsManagerAdapter;

public class ServerTaskRuntimeAdapterTest {
    ChannelTasksRuntimeAdapter runtime;
    Executor executor;

    @Before
    public void setUp() {
        executor = mock(Executor.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(executor).execute(any(Runnable.class));
        runtime = spy(new ChannelTasksRuntimeAdapter(executor){});
    }

    @After
    public void tearDown() {
        UnitRegistry.clear();
    }

    @Test
    public void shouldAddRunnerForTheChannel() throws IOException {
        // preparing test data
        String channelName = "testChannel";
        String deviceVendor = "testDeviceVendor";
        String managerUnitPath = "manager";
        TasksPoolUnit tasksPool = mock(TasksPoolUnit.class);
        TaskPoolsManager manager = spy(new TaskPoolsManagerAdapter() {
            @Override
            public TasksPoolUnit createTaskPool(String name, String factory) {
                return tasksPool;
            }
        });
        doReturn(managerUnitPath).when(manager).getPath();
        UnitRegistry.register(manager);
        doReturn(tasksPool).when(manager).getTaskPool(channelName, deviceVendor);
        Channel channel = mock(Channel.class);
        doReturn(channelName).when(channel).getName();
        doReturn(deviceVendor).when(channel).getDeviceVendor();

        // acting
        boolean added = runtime.addRunnerFor(channel);

        // check the behavior
        verify(manager).getTaskPool(channelName, deviceVendor);
        ArgumentCaptor<DeviceEvent.Listener> captor = ArgumentCaptor.forClass(DeviceEvent.Listener.class);
        verify(channel).addDeviceEventListenerFor(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ChannelTaskRunner.class);
        ChannelTaskRunner runner = (ChannelTaskRunner) captor.getValue();
        verify(runtime).add(runner);
        // check results
        assertThat(added).isTrue();
        assertThat(runtime.runners().collect(Collectors.toList())).hasSize(1).contains(runner);
        assertThat(runner.getEnvironment()).isNotNull();
        assertThat(runner.getChannel()).isSameAs(channel);
        assertThat(runner.getTasksPool()).isSameAs(tasksPool);
        assertThat(runner.getGroup()).isSameAs(runtime);
    }

    @Test
    public void shouldNotAddRunnerForTheChannel_NoRegisteredTaskPoolsManager() {
        // preparing test data
        String channelName = "testChannel";
        String deviceVendor = "testDeviceVendor";
        Channel channel = mock(Channel.class);
        doReturn(channelName).when(channel).getName();
        doReturn(deviceVendor).when(channel).getDeviceVendor();

        // acting
        boolean added = runtime.addRunnerFor(channel);

        // check the behavior
        verify(channel, never()).addDeviceEventListenerFor(any(DeviceEvent.Listener.class));
        verify(runtime, never()).add(any(ServerUnit.class));
        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(runtime).dispatchError(captor.capture(), anyString());
        // check results
        assertThat(added).isFalse();
        assertThat(captor.getValue()).isInstanceOf(NoSuchUnitException.class);
        assertThat(runtime.runners().toArray()).isEmpty();
    }

    @Test
    public void shouldGetRunners_NoRunners() {
        // preparing test data

        // acting
        ChannelTaskRunner[] taskRunners = runtime.runners().toArray(ChannelTaskRunner[]::new);

        // check the behavior
        verify(runtime).runnableChildren();
        // check results
        assertThat(taskRunners).isEmpty();
    }

    @Test
    public void shouldGetRunners_WithRunner() throws IOException {
        // preparing test data
        String channelName = "testChannel";
        String deviceVendor = "testDeviceVendor";
        String managerUnitPath = "manager";
        TasksPoolUnit tasksPool = mock(TasksPoolUnit.class);
        TaskPoolsManager manager = spy(new TaskPoolsManagerAdapter() {
            @Override
            public TasksPoolUnit createTaskPool(String name, String factory) {
                return tasksPool;
            }
        });
        doReturn(managerUnitPath).when(manager).getPath();
        UnitRegistry.register(manager);
        doReturn(tasksPool).when(manager).getTaskPool(channelName, deviceVendor);
        Channel channel = mock(Channel.class);
        doReturn(channelName).when(channel).getName();
        doReturn(deviceVendor).when(channel).getDeviceVendor();
        assertThat(runtime.addRunnerFor(channel)).isTrue();
        ArgumentCaptor<DeviceEvent.Listener> captor = ArgumentCaptor.forClass(DeviceEvent.Listener.class);
        verify(channel).addDeviceEventListenerFor(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ChannelTaskRunner.class);
        ChannelTaskRunner runner = (ChannelTaskRunner) captor.getValue();
        reset(runtime);

        // acting
        ChannelTaskRunner[] taskRunners = runtime.runners().toArray(ChannelTaskRunner[]::new);

        // check the behavior
        verify(runtime).runnableChildren();
        // check results
        assertThat(taskRunners).hasSize(1).contains(runner);
    }

    @Test
    public void shouldAddRunner() {
        // preparing test data
        ChannelTaskRunner runner = mock(ChannelTaskRunner.class);
        assertThat(runtime.runners().toArray()).isEmpty();

        // acting
        boolean added = runtime.add(runner);

        // check results
        assertThat(added).isTrue();
        assertThat(runtime.runners().toArray()).hasSize(1).contains(runner);
    }

    @Test
    public void shouldNotAddRunner_WrongUnitType() {
        // preparing test data
        ServerUnit runner = mock(ServerUnit.class);
        assertThat(runtime.runners().toArray()).isEmpty();

        // acting
        boolean added = runtime.add(runner);

        // check results
        assertThat(added).isFalse();
        assertThat(runtime.runners().toArray()).isEmpty();
    }

    @Test
    public void shouldStartWithRunners() throws IOException {
        // preparing test data
        String channelName = "testChannel";
        String deviceName = "testDeviceName";
        String deviceVendor = "testDeviceVendor";
        String managerUnitPath = "manager";
        TasksPoolUnit tasksPool = mock(TasksPoolUnit.class);
        TaskPoolsManager manager = spy(new TaskPoolsManagerAdapter() {
            @Override
            public TasksPoolUnit createTaskPool(String name, String factory) {
                return tasksPool;
            }
        });
        doReturn(managerUnitPath).when(manager).getPath();
        UnitRegistry.register(manager);
        doReturn(tasksPool).when(manager).getTaskPool(channelName, deviceVendor);
        Channel channel = mock(Channel.class);
        doReturn(channelName).when(channel).getName();
        doReturn(deviceVendor).when(channel).getDeviceVendor();
        assertThat(runtime.addRunnerFor(channel)).isTrue();
        ArgumentCaptor<DeviceEvent.Listener> captor = ArgumentCaptor.forClass(DeviceEvent.Listener.class);
        verify(channel).addDeviceEventListenerFor(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ChannelTaskRunner.class);
        ChannelTaskRunner runner = (ChannelTaskRunner) captor.getValue();
        assertThat(runtime.runners().toArray()).hasSize(1).contains(runner);
        assertThat(runtime.isStarted()).isFalse();
        Device device = mock(Device.class);
        doReturn(device).when(channel).getDevice();
        doReturn(deviceName).when(device).getDeviceName();
        reset(runtime);

        // acting
        runtime.Start();

        // check the behavior
        verify(runtime).startUnitChild(runner);
        verify(device).isOpened();
        verify(device).open();
        verify(device).close();
        verify(tasksPool).Start();
        // check results
        assertThat(runtime.isStarted()).isTrue();
        assertThat(runner.isStarted()).isTrue();
        String devicePartKey = "/channel/device/" + deviceName.toLowerCase();
        assertThat(runner.getEnvironment().getPart(devicePartKey, Device.class)).isSameAs(device);
        assertThat(runner.getEnvironment().getPart("stdout", OutputStream.class)).isNotNull();
        assertThat(runner.getEnvironment().getPart("stderr", OutputStream.class)).isNotNull();
    }

    @Test
    public void shouldGetXML() {
        // preparing test data
        String service = "ChannelTasksRuntime";

        // acting
        Element xml = runtime.getXML();

        // check the behavior
        // check results
        assertThat(xml).isNotNull();
        assertThat(xml.getName()).isEqualTo(service);
        assertThat(xml.getAttributeValue("class")).contains(service);
        assertThat(xml.getAttributeValue("extends")).endsWith(service);
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        Element element = new Element("no-xml");

        // acting
        runtime.setXML(element);

        // check the behavior
        verify(runtime).settingUpBasePart(element);
        verify(runtime).settingUpMainPart(element);
    }
}