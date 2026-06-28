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
package org.visualcti.server.channel.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.server.core.executable.task.Task;

@SuppressWarnings("unchecked")
public class AbstractChannelTest {
    String deviceName = "device-name";
    String deviceVendor = "device-vendor";
    Factory<?> factory;
    Device<?> device;
    AbstractChannel<?> channel;

    @Before
    public void setUp() {
        device = mock(Device.class);
        doReturn(deviceName).when(device).getName();
        factory = mock(Factory.class);
        doReturn(deviceVendor).when(factory).getVendor();
        channel = spy(new TestChannel<>(device));
        doReturn(factory).when(device).getFactory();
    }

    @Test
    public void shouldGetTheChannelDevice() {
        // preparing test data

        // acting
        Device<?> channelDevice = channel.getDevice();

        // check results
        assertThat(channelDevice).isSameAs(device);
    }

    @Test
    public void shouldGetTheChannelDeviceFactory() {
        // preparing test data

        // acting
        Factory<?> channelDeviceFactory = channel.getDeviceFactory();

        // check the behavior
        verify(channel).getDevice();
        verify(device).getFactory();
        // check results
        assertThat(channelDeviceFactory).isSameAs(factory);
    }

    @Test
    public void shouldGetChannelStatus() {
        // preparing test data
        String status = "status";
        doReturn(status).when(device).getStatus();

        // acting
        String channelStatus = channel.getStatus();

        // check the behavior
        verify(channel).getDevice();
        // check results
        assertThat(channelStatus).isEqualTo(status);
    }

    @Test
    public void shouldCheckIsOpenedChannel() {
        // preparing test data

        // acting
        boolean channelOpened = channel.isOpened();

        // check the behavior
        verify(channel).getDevice();
        verify(device).isOpened();
        // check results
        assertThat(channelOpened).isEqualTo(device.isOpened());
    }

    @Test
    public void shouldDoBeforeStart() {
        // preparing test data
        String taskName = "task-name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        assertThat(channel.onlineTasksCount()).isZero();

        // acting
        channel.beforeStart(task);

        // check results
        assertThat(channel.onlineTasksCount()).isEqualTo(1);
        assertThat(channel.getOnlineTasks()).containsEntry(taskName, 1);
    }

    @Test
    public void shouldDoAfterStop() {
        // preparing test data
        String taskName = "task-name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        channel.beforeStart(task);
        assertThat(channel.onlineTasksCount()).isEqualTo(1);
        reset(channel);

        // acting
        channel.afterStop(task);

        // check results
        assertThat(channel.onlineTasksCount()).isZero();
        assertThat(channel.getOnlineTasks()).containsEntry(taskName, 0);
    }

    @Test
    public void shouldGetOnlineTasks() {
        // preparing test data
        int onlineTasksCount = 10;
        String taskName1 = "task-name-1";
        String taskName2 = "task-name-2";
        Task task = mock(Task.class);
        doAnswer(new Answer<String>() {
            int count = 0;
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                count++;
                return count % 2 == 0 ? taskName1 : taskName2;
            }
        }).when(task).getName();
        reset(channel);

        // acting
        IntStream.range(0, onlineTasksCount).forEach(i -> channel.beforeStart(task));

        // check results
        assertThat(channel.onlineTasksCount()).isEqualTo(onlineTasksCount);
        assertThat(channel.getOnlineTasks()).containsEntry(taskName1, onlineTasksCount / 2);
        assertThat(channel.getOnlineTasks()).containsEntry(taskName2, onlineTasksCount / 2);
    }

    @Test
    public void shouldGetDeviceVendor() {
        // preparing test data

        // acting
        String vendorName = channel.getDeviceVendor();

        // check the behavior
        verify(channel).getDevice();
        verify(device).getFactory();
        verify(factory).getVendor();
        // check results
        assertThat(vendorName).isEqualTo(deviceVendor);
    }

    @Test
    public void shouldAddDeviceEventListenerFor() {
        // preparing test data
        DeviceEvent.Listener listener = mock(DeviceEvent.Listener.class);

        // acting
        channel.addDeviceEventListenerFor(listener);

        // check the behavior
        verify(channel, times(2)).getDevice();
        verify(device).getFactory();
        verify(factory).addDeviceEventListenerFor(deviceName, listener);
    }

    @Test
    public void shouldRemoveDeviceEventListenerFor() {
        // preparing test data
        DeviceEvent.Listener listener = mock(DeviceEvent.Listener.class);

        // acting
        channel.removeDeviceEventListenerFor(listener);

        // check the behavior
        verify(channel, times(2)).getDevice();
        verify(device).getFactory();
        verify(factory).removeDeviceEventListenerFor(deviceName, listener);
    }

    //// inner classes
    static class TestChannel<F extends Factory<?>> extends AbstractChannel<Device<F>> {
        protected TestChannel(Device<F> device) {
            super(device);
        }
    }
}