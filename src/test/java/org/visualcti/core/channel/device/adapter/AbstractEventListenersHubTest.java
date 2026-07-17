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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.DeviceEvent;

public class AbstractEventListenersHubTest {

    AbstractEventListenersHub hub;
    DeviceEvent.Listener listener;

    @Before
    public void setUp() {
        hub = spy(new AbstractEventListenersHub() {
        });
        listener = mock(DeviceEvent.Listener.class);
    }

    @Test
    public void shouldGetEventListeners() {
        // preparing test data
        String deviceName = "device-name";
        hub.addDeviceEventListenerFor(deviceName, listener);

        // acting
        Stream<DeviceEvent.Listener> stream = hub.eventListeners(deviceName);

        // check the behavior
        // check results
        assertThat(stream.toArray()).contains(listener);
    }

    @Test
    public void shouldNotGetEventListeners_NoAddedOnes() {
        // preparing test data
        String deviceName = "device-name";

        // acting
        Stream<DeviceEvent.Listener> stream = hub.eventListeners(deviceName);

        // check the behavior
        // check results
        assertThat(stream.toArray()).isEmpty();
    }

    @Test
    public void shouldAddDeviceEventListenerFor() {
        // preparing test data
        String deviceName = "device-name";
        assertThat(hub.eventListeners(deviceName).toArray()).isEmpty();

        // acting
        boolean done = hub.addDeviceEventListenerFor(deviceName, listener);

        // check the behavior
        // check results
        assertThat(done).isTrue();
        assertThat(hub.eventListeners(deviceName).toArray()).contains(listener);
    }

    @Test
    public void shouldNotAddDeviceEventListenerFor_AlreadyAdded() {
        // preparing test data
        String deviceName = "device-name";
        hub.addDeviceEventListenerFor(deviceName, listener);
        assertThat(hub.eventListeners(deviceName).toArray()).contains(listener);

        // acting
        boolean done = hub.addDeviceEventListenerFor(deviceName, listener);

        // check the behavior
        // check results
        assertThat(done).isFalse();
        assertThat(hub.eventListeners(deviceName).toArray()).contains(listener);
    }

    @Test
    public void shouldNotAddDeviceEventListenerFor_Null() {
        // preparing test data
        String deviceName = "device-name";

        // acting
        boolean done = hub.addDeviceEventListenerFor(deviceName, null);

        // check the behavior
        // check results
        assertThat(done).isFalse();
    }

    @Test
    public void shouldRemoveDeviceEventListenerFor() {
        // preparing test data
        String deviceName = "device-name";
        hub.addDeviceEventListenerFor(deviceName, listener);
        assertThat(hub.eventListeners(deviceName).toArray()).contains(listener);

        // acting
        boolean done = hub.removeDeviceEventListenerFor(deviceName, listener);

        // check the behavior
        // check results
        assertThat(done).isTrue();
        assertThat(hub.eventListeners(deviceName).toArray()).isEmpty();
    }

    @Test
    public void shouldNotRemoveDeviceEventListenerFor_NoAddedOnes() {
        // preparing test data
        String deviceName = "device-name";
        assertThat(hub.eventListeners(deviceName).toArray()).isEmpty();

        // acting
        boolean done = hub.removeDeviceEventListenerFor(deviceName, listener);

        // check the behavior
        // check results
        assertThat(done).isFalse();
    }
}