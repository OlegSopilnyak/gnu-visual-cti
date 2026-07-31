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
import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractFaxMachineEngineTest<H> {

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
        engine = spy(new AbstractFaxMachineEngine());
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
    public void receive() {
    }

    @Test
    public void transmit() {
    }

    @Test
    public void terminate() {
    }
}