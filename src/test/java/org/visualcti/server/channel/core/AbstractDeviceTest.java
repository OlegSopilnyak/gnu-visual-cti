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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Factory;

public class AbstractDeviceTest {
    String deviceVendor = "deviceVendor";
    String deviceName = "deviceName";
    Factory<?> factory;
    AbstractDevice<?> device;

    @Before
    public void setUp() {
        factory = mock(Factory.class);
        device = spy(new AbstractDevice(){
            @Override
            public String getName() {
                return deviceName;
            }
        });
    }

    @Test
    public void shouldGetFactory_DeviceInFactory() throws IOException {
        // preparing test data
        device.setOwner(factory);

        // acting
        Factory<?> deviceFactory = device.getFactory();

        // check the behavior
        verify(device).getOwner();
        // check results
        assertThat(deviceFactory).isSameAs(factory);
    }

    @Test
    public void shouldNoGetFactory_DeviceNotInFactory() {
        // preparing test data

        // acting
        Optional<?> deviceFactory = device.getFactoryOptional();

        // check the behavior
        verify(device).getOwner();
        // check results
        assertThat(deviceFactory).isEmpty();
    }

    @Test
    public void shouldOpenDevice() throws IOException {
        // preparing test data
        assertThat(device.getStatus()).isEqualTo("CLOSED");

        // acting
        device.open();

        // check results
        assertThat(device.getStatus()).isEqualTo("IDLE");
    }

    @Test
    public void shouldCloseDevice() throws IOException {
        // preparing test data
        device.open();
        assertThat(device.getStatus()).isEqualTo("IDLE");

        // acting
        device.close();

        // check results
        assertThat(device.getStatus()).isEqualTo("CLOSED");
    }

    @Test
    public void shouldDeviceBeOpened() throws IOException {
        // preparing test data
        device.open();
        assertThat(device.getStatus()).isEqualTo("IDLE");

        // acting
        boolean opened = device.isOpened();

        // check results
        assertThat(opened).isTrue();
    }

    @Test
    public void shouldDeviceNotBeOpened() {
        // preparing test data
        assertThat(device.getStatus()).isEqualTo("CLOSED");

        // acting
        boolean opened = device.isOpened();

        // check results
        assertThat(opened).isFalse();
    }

    @Test
    public void shouldTerminateDevice() throws IOException {
        // preparing test data
        device.open();
        assertThat(device.getStatus()).isEqualTo("IDLE");

        // acting
        device.terminate();

        // check the behavior
        verify(device).isOpened();
        verify(device).close();
    }

    @Test
    public void shouldNotTerminateDevice_Closed() throws IOException {
        // preparing test data

        // acting
        device.terminate();

        // check the behavior
        verify(device).isOpened();
        verify(device, never()).close();
    }

    @Test
    public void shouldGetStatus() {
        // preparing test data
        String deviceStatus = "WAIT";
        device.currentStatus = deviceStatus;

        // acting
        String status = device.getStatus();

        // check results
        assertThat(status).isSameAs(deviceStatus);
    }

    @Test
    public void shouldRepairDevice() {
        // preparing test data
        doReturn(true).when(device).repair();

        // acting
        boolean repaired = device.repair();

        // check results
        assertThat(repaired).isTrue();
    }

    @Test
    public void shouldNotRepairDevice_ByDefault() {
        // preparing test data

        // acting
        boolean repaired = device.repair();

        // check results
        assertThat(repaired).isFalse();
    }

    @Test
    public void shouldGetName() {
        // preparing test data

        // acting
        String name = device.getName();

        // check results
        assertThat(name).isSameAs(deviceName);
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String type = device.getType();

        // check results
        assertThat(type).isEqualTo("[channel-device]");
    }

    @Test
    public void shouldGetDeviceName() throws IOException {
        // preparing test data
        doReturn(deviceVendor).when(factory).getVendor();
        device.setOwner(factory);

        // acting
        String name = device.getDeviceName();

        // check the behavior
        verify(device).getName();
        verify(device).getOwner();
        verify(device).getFactory();
        verify(factory).getVendor();
        // check results
        assertThat(name).isEqualTo(deviceVendor + "/" + deviceName);
    }
}