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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.service.Service;
import org.visualcti.server.service.ServicesManager;
import org.visualcti.server.system.manager.ServerServicesManager;

public class ServiceSubSystemTest {
    final String testXml = "services.xml";
    String configurationXmlString;
    final XmlAware xmlEngine = new XmlAware() {
    };
    final ServiceSubSystem system = new ServiceSubSystem();

    @Before
    public void setUp() throws Exception {
        Element xml = xmlEngine.load(ServiceSubSystem.class.getResourceAsStream(testXml));
        // preparing default sub-system Xml configuration text
        configurationXmlString = xmlEngine.asString(xml);
        UnitRegistry.clear();
    }

    @After
    public void tearDown() throws Exception {
        if (system.getSystemManager() != null) {
            system.getSystemManager().Stop();
            system.getSystemManager().cleanUnitsTree();
        }
    }

    @Test
    public void shouldGetBasicsXml() throws IOException {
        // preparing test data
        String basicsXmlString = "<base>\n" +
                "  <!--This is the main interface of the services manager-->\n" +
                "  <ServicesManager package=\"org.visualcti.server.service\" class=\"ServicesManager\" extends=\"Service\" />\n" +
                "  <!--This is the main interface of any service-->\n" +
                "  <Service package=\"org.visualcti.server.service\" class=\"Service\" extends=\"org.visualcti.server.core.unit.RunnableServerUnit\" />\n" +
                "</base>\n\n";
        ServiceSubSystem.Basics basics = new ServiceSubSystem.Basics();

        // acting
        Element xml = basics.getXML();

        // check results
        assertThat(xml).isNotNull();
        assertThat(xmlEngine.asString(xml)).isEqualTo(basicsXmlString);
    }

    @Test
    public void shouldSetupBasicsXml() throws IOException, DataConversionException {
        // preparing test data
        ServiceSubSystem.Basics basics = new ServiceSubSystem.Basics();
        Element baseXml = xmlEngine.load(ServiceSubSystem.class.getResourceAsStream(testXml))
                .getChild("Services").getChild("base");

        // acting
        basics.setXML(baseXml);

        // check results
        assertThat(basics.master.isValid()).isTrue();
        assertThat(basics.basic.isValid()).isTrue();
        assertThat(basics.isValid()).isTrue();
        assertThat(xmlEngine.asString(basics.getXML())).isEqualTo(xmlEngine.asString(baseXml));
    }

    @Test
    public void shouldSetServiceSubSystemXML() throws IOException, DataConversionException, ServerUnitException {
        // preparing test data
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
        assertThat(system.asString(xml)).isEqualTo(configurationXmlString);

        // acting
        system.setXML(xml);

        // check results
        assertThat(system.getSystemManager()).isInstanceOf(SubSystem.Manager.class).isInstanceOf(ServicesManager.class);
        assertThat(UnitRegistry.lookup(SubSystem.Manager.class)).isInstanceOf(ServicesManager.class);
        ServicesManager servicesManager = (ServicesManager) UnitRegistry.lookup("Service", ServicesManager.class);
        assertThat(UnitRegistry.lookup("Service", ServicesManager.class))
                .isInstanceOf(ServerServicesManager.class);
//        assertThat(servicesManager.services().count()).isNotZero();
        servicesManager.services().forEach(service -> {
                    assertThat(service).isInstanceOf(Service.class);
                    assertThat(service.canStartUnit()).isTrue();
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
        Element servicesXml = systemXml.getChild("Services");
        assertThat(servicesXml).isNotNull();
        assertThat(servicesXml.getChild("base")).isNotNull();
        Element managerXml = servicesXml.getChild("service").getChild("Manager");
        assertThat(managerXml).isNotNull();
        assertThat(managerXml.getAttributeValue("class")).isNotNull();
        assertThat(managerXml.getAttributeValue("package")).isNotNull();
        assertThat(managerXml.getAttributeValue("extends")).isNotNull();
        assertThat(xmlEngine.asString(systemXml)).isNotEmpty();
    }

    @Test
    public void shouldStartSystem() throws IOException, DataConversionException {
        // preparing test data
        system.setXML(xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes())));
        Service fakeService = mock(Service.class);
        doReturn("FakeService").when(fakeService).getName();
        assertThat(system.getSystemManager()).isInstanceOf(ServicesManager.class);
        assertThat(system.getSystemManager().add(fakeService)).isTrue();
        doReturn(true).when(fakeService).isDependsOn(system.getSystemManager());
        assertThat(system.getSystemManager().canStartUnit()).isTrue();

        // acting
        system.getSystemManager().Start();

        // check the behavior
        verify(fakeService).Start();
        // check results
        assertThat(system.getSystemManager().isStarted()).isTrue();
        ServicesManager manager = system.getSystemManager();
        manager.services().forEach(service -> {
            if (service.canStartUnit()) {
                assertThat(service.isStarted()).isTrue();
            } else {
                System.out.println("=== Service :[" + service.getName() + "] can't start");
            }
        });
    }

    @Test
    public void shouldStopSystem() throws IOException, DataConversionException {
        // preparing test data
        system.setXML(xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes())));
        Service fakeService = mock(Service.class);
        doReturn("FakeService").when(fakeService).getName();
        assertThat(system.getSystemManager()).isInstanceOf(ServicesManager.class);
        assertThat(system.getSystemManager().add(fakeService)).isTrue();
        doReturn(true).when(fakeService).isDependsOn(system.getSystemManager());
        assertThat(system.getSystemManager().canStartUnit()).isTrue();
        system.getSystemManager().Start();

        // acting
        doReturn(true).when(fakeService).isStopped();
        system.getSystemManager().Stop();

        // check the behavior
        verify(fakeService).Stop();
        // check results
        assertThat(system.getSystemManager().isStopped()).isTrue();
        ServicesManager manager = system.getSystemManager();
        manager.services().forEach(service -> {
            assertThat(service.isStopped()).isTrue();
        });
    }
}
