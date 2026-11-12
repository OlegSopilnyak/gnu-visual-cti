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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.core.channel.device.DevicesManager;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.exception.ServerUnitException;

public class HardwareSubSystemTest {
    // preparing default sub-system Xml configuration
    final static String configurationXmlString = "<system>\n" +
            "  <!-- Server devices control system -->\n" +
            "  <Hardware>\n" +
            "    <!-- The manager of channel-devices pools -->\n" +
            "    <Manager package=\"org.visualcti.server.hardware\" class=\"Manager\" extends=\"org.visualcti.server.core.executable.Engine\">\n" +
            "      <!-- Not used for unit's building here only parameters applying -->\n" +
            "      <parent package=\"org.visualcti.server.hardware\" class=\"HardwareManager\" method=\"getManager\">The implementation of hardware manager's features</parent>\n" +
            "    </Manager>\n" +
            "    <factory package=\"org.visualcti.server.hardware.provider.dialogic\" class=\"FactoryImpl\" extends=\"org.visualcti.core.channel.telephony.TelephonyFactory\">\n" +
            "      <builder package=\"org.visualcti.server.hardware.provider.dialogic\" class=\"FactoryImpl\" method=\"getInstance\">The implementation of Dialogic telephony hardware factory features</builder>\n" +
            "      <parameter name=\"vendor\" type=\"string\" value=\"Dialogic\" />\n" +
            "      <parameter name=\"url\" type=\"string\" value=\"file:./conf/dialogic.configuration.xml\" />\n" +
            "    </factory>\n" +
            "  </Hardware>\n" +
            "</system>\n\n";
    HardwareSubSystem system = new HardwareSubSystem();

    @Before
    public void setUp() throws Exception {
        UnitRegistry.clear();
    }

    @Test
    public void shouldSetHardwareSubSystemXML() throws IOException, DataConversionException, ServerUnitException {
        // preparing test data
        Element xml = new XmlAware() {
        }.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
        assertThat(system.asString(xml)).isEqualTo(configurationXmlString);

        // acting
        system.setXML(xml);

        // check results
        assertThat(system.getSystemManager()).isInstanceOf(SubSystem.Manager.class)
                .isInstanceOf(DevicesManager.class);
        assertThat(UnitRegistry.lookup(SubSystem.Manager.class)).isNotNull().isInstanceOf(DevicesManager.class);
        assertThat(UnitRegistry.lookup(DevicesManager.class).factories().count()).isNotZero();
        UnitRegistry.lookup(DevicesManager.class).factories()
                .forEach(factory -> assertThat(factory).isInstanceOf(Factory.class));
    }

    @Test
    public void shouldGetHardwareSubSystemXML() throws IOException, DataConversionException {
        // preparing test data
        Element xml = new XmlAware() {
        }.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
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
        String textXml = system.asString(systemXml);
        assertThat(textXml).isEqualTo(configurationXmlString);
    }
}
