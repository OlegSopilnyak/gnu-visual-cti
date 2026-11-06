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
package org.visualcti.server.hardware.provider.dialogic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.jdom.DataConversionException;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Device;

@SuppressWarnings("unchecked")
public class AbstractDialogicDeviceFactoryTest<H extends Integer> {
    String configurationXml =
            "<factory package=\"org.visualcti.server.hardware.provider.dialogic\" class=\"Factory\" extends=\"org.visualcti.server.hardware.deviceFactory\">\n" +
            "  <parameter name=\"vendor\" type=\"string\" value=\"Dialogic\" />\n" +
            "  <parameter name=\"url\" type=\"string\" value=\"file:./conf/dialogic.configuration.xml\" />\n" +
            "</factory>\n";
    AbstractDialogicDeviceFactory<H, ?> factory;
    DialogicServiceProvider<H> provider;
    Executor executor;

    @Before
    public void setUp() throws Exception {
        executor = Executors.newSingleThreadExecutor();
        provider = spy(new DialogicServiceProvider<>());
        factory = spy(new AbstractDialogicDeviceFactory<>(executor, provider));
    }

    @Test
    public void shouldStartDeviceEventProcessing() throws IOException, DataConversionException {
        // preparing test data
        factory.setXML(factory.load(new ByteArrayInputStream(configurationXml.getBytes())));
        reset(factory);

        // acting
        factory.Start();

        // check the behavior
        verify(provider).allowedDevices();
        verify(factory, never()).addDevice(any(Device.class));
        verify(factory, never()).buildDevice(anyString(), any(Device.ServiceProvider.class));
        verify(factory, atLeastOnce()).devices();
        verify(factory).startingDeviceEventProcessing();
        // check results
        assertThat(factory.isStarted()).isTrue();
    }

    @Test
    public void shouldStopDeviceEventProcessing() throws IOException, DataConversionException {
        // preparing test data
        factory.setXML(factory.load(new ByteArrayInputStream(configurationXml.getBytes())));
        factory.Start();
        assertThat(factory.isStarted()).isTrue();
        reset(factory);

        // acting
        factory.Stop();

        // check the behavior
        verify(factory).stoppingDeviceEventProcessing();
        // check results
        assertThat(factory.isStopped()).isTrue();
    }
}