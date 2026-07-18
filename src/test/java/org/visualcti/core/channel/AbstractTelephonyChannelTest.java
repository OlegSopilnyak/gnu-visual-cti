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
package org.visualcti.core.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;

public class AbstractTelephonyChannelTest {
    String deviceName = "telephony-device-name";
    TelephonyDevice<?, ?> device;
    AbstractTelephonyChannel<?> channel;

    @Before
    public void setUp() {
        device = mock(TelephonyDevice.class);
        doReturn(deviceName).when(device).getName();
        channel = spy(new AbstractTelephonyChannel(device){});
    }

    @Test
    public void shouldGetTelephonyDevice() {
        // preparing test data

        // acting
        TelephonyDevice<?, ?> channelDevice = channel.getDevice();

        // check results
        assertThat(channelDevice).isSameAs(device);
    }

    @Test
    public void shouldGetType() {
        // preparing test data

        // acting
        String channelUnitType = channel.getType();

        // check results
        assertThat(channelUnitType).isEqualTo("[telephony-channel]");
    }

    @Test
    public void shouldBeBusy() {
        // preparing test data
        doReturn(true).when(device).isOpened();

        // acting
        boolean channelIsBusy = channel.isBusy();

        // check the behavior
        verify(channel).getDevice();
        verify(device).isOpened();
        // check results
        assertThat(channelIsBusy).isTrue();
    }

    @Test
    public void shouldNotBeBusy_DeviceIsClosed() {
        // preparing test data

        // acting
        boolean channelIsBusy = channel.isBusy();

        // check the behavior
        verify(channel).getDevice();
        verify(device).isOpened();
        // check results
        assertThat(channelIsBusy).isFalse();
    }

    @Test
    public void shouldGetDeviceFactory() {
        // preparing test data
        TelephonyDeviceFactory<?, ?> factory = mock(TelephonyDeviceFactory.class);
        doReturn(factory).when(device).getFactory();

        // acting
        TelephonyDeviceFactory<?, ?> channelDeviceFactory = channel.getDeviceFactory();

        // check the behavior
        verify(channel).getDevice();
        verify(device).getFactory();
        // check results
        assertThat(channelDeviceFactory).isSameAs(factory);
    }
}