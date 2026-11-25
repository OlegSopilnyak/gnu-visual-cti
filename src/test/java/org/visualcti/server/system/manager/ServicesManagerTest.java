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
package org.visualcti.server.system.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.service.Service;

public class ServicesManagerTest {
    String configurationXml = "<service>\n" +
            "  <!-- The manager of the server's services -->\n" +
            "  <Manager package=\"org.visualcti.server.system.manager\" class=\"ServerServicesManager\" extends=\"org.visualcti.server.service.ServicesManager\" Prominic=\"signed\">\n" +
            "    <builder package=\"org.visualcti.server.system.manager\" class=\"ServerServicesManager\" method=\"getInstance\" />\n" +
            "  </Manager>\n" +
            "</service>\n\n";
    XmlAware xmlEngine;
    ServerServicesManager servicesManager;
    String devicesSubSystemName = "Services";
    SubSystem devicesSubSystem;

    @Before
    public void setUp() throws Exception {
        xmlEngine = new XmlAware() {
        };
        servicesManager = ServerServicesManager.getInstance();
        servicesManager.resetUnitPath();
        devicesSubSystem = mock(SubSystem.class);
        doReturn(devicesSubSystemName).when(devicesSubSystem).getSystemElementName();
    }

    @After
    public void tearDown() throws Exception {
        servicesManager.Stop();
        servicesManager.setSystem(null);
        servicesManager.setOwner(null);
        servicesManager.unitConfigurationChanged();
        UnitRegistry.clear();
    }

    @Test
    public void shouldGetXML() throws IOException {
        // preparing test data
        ServerServicesManager manager = spy(servicesManager);

        // acting
        Element managerXml = manager.getXML();

        // check the behavior
        verify(manager).getRootElementName();
        verify(manager, times(2)).getUnitDescription();
        // check results
        assertThat(managerXml).isNotNull();
        assertThat(xmlEngine.asString(managerXml)).isEqualTo(configurationXml);
    }

    @Test
    public void shouldGetManagerInstance() {
        // preparing test data

        // acting
        ServerServicesManager manager = ServerServicesManager.getInstance();

        // check results
        assertThat(manager).isSameAs(servicesManager);
    }

    @Test
    public void shouldGetName() {
        // preparing test data

        // acting
        String unitName = servicesManager.getName();

        // check results
        assertThat("Server's Services Manager").isEqualTo(unitName);
    }

    @Test
    public void shouldGetUnitDescription() {
        // preparing test data

        // acting
        String unitDescription = servicesManager.getUnitDescription().trim();

        // check results
        assertThat("The manager of the server's services").isEqualTo(unitDescription);
    }

    @Test
    public void shouldGetRootElementName() {
        // preparing test data

        // acting
        String rootElementName = servicesManager.getRootElementName();

        // check results
        assertThat("Manager").isEqualTo(rootElementName);
    }

    @Test
    public void shouldGetSystem() {
        // preparing test data
        SubSystem hardware = mock(SubSystem.class);
        servicesManager.setSystem(hardware);

        // acting
        SubSystem subSystem = servicesManager.getSystem();

        // check results
        assertThat(subSystem).isEqualTo(hardware);
    }

    @Test
    public void shouldNotGetSystem_NotAssigned() {
        // preparing test data

        // acting
        SubSystem subSystem = servicesManager.getSystem();

        // check results
        assertThat(subSystem).isNull();
    }

    @Test
    public void shouldAddDevicesFactoryUnit() throws IOException {
        // preparing test data
        Service service = mock(Service.class);
        ServerServicesManager manager = spy(servicesManager);

        // acting
        boolean success = manager.add(service);

        // check the behavior
        verify(manager).addService(service);
        verify(manager).services();
        verify(service).setOwner(manager);
        verify(manager).unitConfigurationChanged();
        // check results
        assertThat(success).isTrue();
        assertThat(manager.services().toArray()).contains(service);
        doReturn(manager).when(service).getOwner();
    }

    @Test
    public void shouldNotAddDevicesFactoryUnit_NotService() {
        // preparing test data
        ServerUnit serverUnit = mock(ServerUnit.class);
        ServerServicesManager manager = spy(servicesManager);

        // acting
        boolean success = manager.add(serverUnit);

        // check the behavior
        verify(manager, never()).addService(any(Service.class));
        // check results
        assertThat(success).isFalse();
        assertThat(manager.services().toArray()).isEmpty();
    }

    @Test
    public void shouldRemoveDevicesFactoryUnit() throws IOException {
        // preparing test data
        Service service = mock(Service.class);
        ServerServicesManager manager = spy(servicesManager);
        assertThat(manager.add(service)).isTrue();
        doReturn(manager).when(service).getOwner();
        assertThat(manager.services().toArray()).contains(service);
        reset(manager);

        // acting
        boolean success = manager.remove(service);

        // check the behavior
        verify(manager).removeService(service);
        verify(service).setOwner(null);
        verify(manager).removeBranch(service);
        verify(service).close();
        verify(manager).unitConfigurationChanged();
        // check results
        assertThat(success).isTrue();
        assertThat(manager.services().toArray()).isEmpty();
    }

    @Test
    public void shouldDoBeforeRegisterUnit() throws IOException {
        // preparing test data
        servicesManager.setSystem(devicesSubSystem);
        servicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(servicesManager);
        ServerServicesManager spied = spy(servicesManager);
        UnitRegistry.register(spied);
        spied.resetUnitPath();

        // acting
        spied.beforeRegisterUnit();

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        // check results
        assertThat(spied.getPath()).isEqualTo(servicesManager.defaultManagerUnitPath());
    }

    @Test
    public void shouldBuildDefaultManagerUnitPath() {
        // preparing test data
        servicesManager.setSystem(devicesSubSystem);

        // acting
        String defaultPath = servicesManager.defaultManagerUnitPath();

        // check the behavior
        // check results
        assertThat(defaultPath).isEqualTo("Service");
    }

    @Test
    public void shouldGetSystemName() {
        // preparing test data
        servicesManager.setSystem(devicesSubSystem);
        ServerServicesManager spied = spy(servicesManager);
        assertThat(spied.getPath()).isSameAs(servicesManager.getPath());

        // acting
        String managerSystemName = spied.getSystemName();

        // check the behavior
        verify(spied).getSystem();
        verify(devicesSubSystem, atLeastOnce()).getSystemElementName();
        // check results
        assertThat(managerSystemName).isEqualTo(devicesSubSystem.getSystemElementName());
    }

    @Test
    public void shouldConnectToTheApplicationServer() throws IOException, ServerUnitException {
        // preparing test data
        String serverPath = "TestServer";
        ApplicationServer server = mock(ApplicationServer.class);
        doReturn(serverPath).when(server).getPath();
        servicesManager.setSystem(devicesSubSystem);
        servicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(servicesManager);
        ServerServicesManager spied = spy(servicesManager);
        UnitRegistry.register(spied);
        assertThat(UnitRegistry.lookup(ServerServicesManager.class).getPath()).isEqualTo(servicesManager.getPath());

        // acting
        spied.connectTo(server);

        // check the behavior
        verify(spied).setOwner(server);
        // check results
        String connectedTasksManagerPath = serverPath + "/Service";
        assertThat(UnitRegistry.lookup(connectedTasksManagerPath, ServerServicesManager.class)).isSameAs(spied);
        assertThat(UnitRegistry.lookup(ServerServicesManager.class).getPath()).isEqualTo(connectedTasksManagerPath);
    }

    @Test
    public void shouldStartManager() throws IOException {
        // preparing test data
        servicesManager.setSystem(devicesSubSystem);
        servicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(servicesManager);
        ServerServicesManager manager = spy(servicesManager);
        UnitRegistry.register(manager);
        Service service = mock(Service.class);
        doReturn(true).when(service).isDependsOn(manager);
        manager.add(service);

        // acting
        manager.Start();

        // check the behavior
        verify(manager).canStartUnit();
        verify(manager).startUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).startUnitChild(service);
        verify(service).Start();
        // check results
        assertThat(manager.isStarted()).isTrue();
        doReturn(manager).when(service).getOwner();
    }

    @Test
    public void shouldStopManager() throws IOException {
        // preparing test data
        servicesManager.setSystem(devicesSubSystem);
        servicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(servicesManager);
        ServerServicesManager manager = spy(servicesManager);
        UnitRegistry.register(manager);
        Service service = mock(Service.class);
        doReturn(true).when(service).isDependsOn(manager);
        manager.add(service);
        manager.Start();

        // acting
        manager.Stop();

        // check the behavior
        verify(manager).stopUnitRunnable();
        verify(manager, atLeastOnce()).runnableChildren();
        verify(manager).stopUnitChild(service);
        verify(service).Stop();
        // check results
        assertThat(manager.isStopped()).isTrue();
        doReturn(manager).when(service).getOwner();
    }
}
