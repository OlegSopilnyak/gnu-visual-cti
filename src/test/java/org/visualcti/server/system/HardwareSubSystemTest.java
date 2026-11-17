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
package org.visualcti.server.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DevicesManager;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.exception.ServerUnitException;

@SuppressWarnings("unchecked")
public class HardwareSubSystemTest<H> {
    final String testXml = "hardware.xml";
    String configurationXmlString;
    final XmlAware xmlEngine = new XmlAware() {
    };
    final HardwareSubSystem system = new HardwareSubSystem();

    @Before
    public void setUp() throws Exception {
        Element xml = xmlEngine.load(HardwareSubSystem.class.getResourceAsStream(testXml));
        // preparing default sub-system Xml configuration text
        configurationXmlString = xmlEngine.asString(xml);
        UnitRegistry.clear();
    }

    @After
    public void tearDown() throws Exception {
        system.getSystemManager().Stop();
    }

    @Test
    public void shouldSetHardwareSubSystemXML() throws IOException, DataConversionException, ServerUnitException {
        // preparing test data
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
        assertThat(system.asString(xml)).isEqualTo(configurationXmlString);

        // acting
        system.setXML(xml);

        // check results
        assertThat(system.getSystemManager()).isInstanceOf(SubSystem.Manager.class).isInstanceOf(DevicesManager.class);
        assertThat(UnitRegistry.lookup(SubSystem.Manager.class)).isInstanceOf(DevicesManager.class);
        DevicesManager hardwareManager = (DevicesManager) UnitRegistry.lookup("HardwareManager", DevicesManager.class);
        assertThat(UnitRegistry.lookup("HardwareManager/provider/Dialogic", TelephonyFactory.class))
                .isInstanceOf(org.visualcti.server.hardware.provider.dialogic.FactoryImpl.class);
        assertThat(UnitRegistry.lookup("HardwareManager/provider/VirtualTelephony", TelephonyFactory.class))
                .isInstanceOf(org.visualcti.server.hardware.provider.virtual.VirtualTelephonyDevicesFactory.class);
        assertThat(hardwareManager.factories().count()).isNotZero();
        hardwareManager.devices().forEach(device -> {
                    assertThat(device).isInstanceOf(Device.class);
                    try {
                        DeviceActivitySession<H> session = (DeviceActivitySession<H>) device.startSession();
                        assertThat(session).isNotNull();
                        assertThat(session.isOpened()).isTrue();
                        assertThat(device.isOpened()).isTrue();
                        ((Device<H, ?>)device).detachAndClose(session);
                        assertThat(device.isOpened()).isFalse();
                        assertThat(session.isOpened()).isFalse();
                    } catch (IOException e) {
                        fail("Cannot start the session", e);
                    }
                }
        );
    }

    @Test
    public void shouldGetHardwareSubSystemXML() throws IOException, DataConversionException {
        // preparing test data
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
        system.setXML(xml);

        // acting
        Element systemXml = system.getXML();

        // check results
        assertThat(systemXml).isNotNull();
        assertThat(systemXml.getName()).isEqualTo("system");
        Element hardware = systemXml.getChild("Hardware");
        assertThat(hardware).isNotNull();
        Element manager = hardware.getChild("Manager");
        assertThat(manager).isNotNull();
        String textXml = xmlEngine.asString(systemXml);
        assertThat(textXml).isEqualTo(configurationXmlString);
    }

    @Test
    public void shouldStartSystem() throws IOException, DataConversionException {
        // preparing test data
        assertThat(system.getSystemManager()).isInstanceOf(DevicesManager.class);
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
        assertThat(system.getSystemManager().canStartUnit()).isFalse();
        system.setXML(xml);
        assertThat(system.getSystemManager().canStartUnit()).isTrue();

        // acting
        system.getSystemManager().Start();

        // check results
        assertThat(system.getSystemManager().isStarted()).isTrue();
        DevicesManager manager = (DevicesManager) system.getSystemManager();
        manager.factories().forEach(factory -> {
            if (factory.canStartUnit()) {
                assertThat(factory.isStarted()).isTrue();
            } else {
                System.out.println("=== Factory :[" + factory.getName() + "] can't start");
            }
        });
    }
}
