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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.ServerUnitException;

@SuppressWarnings("unchecked")
public class HardwareManagerTest<H> {
    String configurationXml = "<Manager package=\"org.visualcti.server.system.manager\" class=\"HardwareManager\" extends=\"org.visualcti.core.channel.device.DevicesManager\">\n" +
            "  <!-- The manager of channel-devices pools -->\n" +
            "  <builder package=\"org.visualcti.server.system.manager\" class=\"HardwareManager\" method=\"getInstance\" />\n" +
            "</Manager>\n\n";
    XmlAware xmlEngine;
    HardwareManager devicesManager;
    String devicesSubSystemName = "Hardware";
    SubSystem devicesSubSystem;

    @Before
    public void setUp() throws Exception {
        xmlEngine = new XmlAware() {
        };
        devicesManager = HardwareManager.getInstance();
        devicesManager.resetUnitPath();
        devicesSubSystem = mock(SubSystem.class);
        doReturn(devicesSubSystemName).when(devicesSubSystem).getSystemElementName();
    }

    @After
    public void tearDown() throws Exception {
        devicesManager.Stop();
        devicesManager.setSystem(null);
        devicesManager.setOwner(null);
        UnitRegistry.clear();
    }

    @Test
    public void shouldGetXML() throws IOException {
        // preparing test data
        HardwareManager manager = spy(devicesManager);

        // acting
        Element managerXml = manager.getXML();

        // check the behavior
        verify(manager).getRootElementName();
        verify(manager).getUnitDescription();
        // check results
        assertThat(managerXml).isNotNull();
        assertThat(xmlEngine.asString(managerXml)).isEqualTo(configurationXml);
    }

    @Test
    public void shouldGetManagerInstance() {
        // preparing test data

        // acting
        HardwareManager manager = HardwareManager.getInstance();

        // check results
        assertThat(manager).isSameAs(devicesManager);
    }

    @Test
    public void shouldGetName() {
        // preparing test data

        // acting
        String unitName = devicesManager.getName();

        // check results
        assertThat("Hardware Manager").isEqualTo(unitName);
    }

    @Test
    public void shouldGetUnitDescription() {
        // preparing test data

        // acting
        String unitDescription = devicesManager.getUnitDescription().trim();

        // check results
        assertThat("The manager of channel-devices pools").isEqualTo(unitDescription);
    }

    @Test
    public void shouldGetRootElementName() {
        // preparing test data

        // acting
        String rootElementName = devicesManager.getRootElementName();

        // check results
        assertThat("Manager").isEqualTo(rootElementName);
    }

    @Test
    public void shouldGetSystem() {
        // preparing test data
        SubSystem hardware = mock(SubSystem.class);
        devicesManager.setSystem(hardware);

        // acting
        SubSystem subSystem = devicesManager.getSystem();

        // check results
        assertThat(subSystem).isEqualTo(hardware);
    }

    @Test
    public void shouldNotGetSystem_NotAssigned() {
        // preparing test data

        // acting
        SubSystem subSystem = devicesManager.getSystem();

        // check results
        assertThat(subSystem).isNull();
    }

    @Test
    public void shouldAddDevicesFactoryUnit() throws IOException {
        // preparing test data
        Factory<H, ?> factory = mock(Factory.class);
        HardwareManager manager = spy(devicesManager);

        // acting
        boolean success = manager.add(factory);

        // check the behavior
        verify(manager).addDeviceFactory(factory);
        verify(manager).factories();
        verify(factory).setOwner(manager);
        verify(manager).unitConfigurationChanged();
        // check results
        assertThat(success).isTrue();
        assertThat(manager.factories().toArray()).contains(factory);
        doReturn(manager).when(factory).getOwner();
    }

    @Test
    public void shouldNotAddDevicesFactoryUnit_NotFactory() {
        // preparing test data
        ServerUnit factory = mock(ServerUnit.class);
        HardwareManager manager = spy(devicesManager);

        // acting
        boolean success = manager.add(factory);

        // check the behavior
        verify(manager, never()).addDeviceFactory(any(Factory.class));
        // check results
        assertThat(success).isFalse();
        assertThat(manager.factories().toArray()).isEmpty();
    }

    @Test
    public void shouldRemoveDevicesFactoryUnit() throws IOException {
        // preparing test data
        Factory<H, ?> factory = mock(Factory.class);
        HardwareManager manager = spy(devicesManager);
        assertThat(manager.add(factory)).isTrue();
        doReturn(manager).when(factory).getOwner();
        assertThat(manager.factories().toArray()).contains(factory);
        reset(manager);

        // acting
        boolean success = manager.remove(factory);

        // check the behavior
        verify(manager).removeDeviceFactory(factory);
        verify(factory).setOwner(null);
        verify(manager).removeBranch(factory);
        verify(factory).close();
        verify(manager).unitConfigurationChanged();
        // check results
        assertThat(success).isTrue();
        assertThat(manager.factories().toArray()).isEmpty();
    }

    @Test
    public void shouldDoBeforeRegisterUnit() throws IOException {
        // preparing test data
        devicesManager.setSystem(devicesSubSystem);
        devicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(devicesManager);
        HardwareManager spied = spy(devicesManager);
        UnitRegistry.register(spied);
        spied.resetUnitPath();
        // acting
        spied.beforeRegisterUnit();

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(devicesSubSystem, atLeastOnce()).getSystemElementName();
        // check results
        assertThat(spied.getPath()).isEqualTo(devicesSubSystem.getSystemElementName() + "Manager");
    }

    @Test
    public void shouldDoNotBeforeRegisterUnit_NoSystemAssociated() {
        // preparing test data
        SubSystem hardware = mock(SubSystem.class);
        HardwareManager spied = spy(devicesManager);
        assertThat(spied.getPath()).isSameAs(devicesManager.getName());

        // acting
        Exception e = assertThrows(Exception.class, spied::beforeRegisterUnit);

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(hardware, never()).getSystemElementName();
        // check results
        assertThat(e).isInstanceOf(NullPointerException.class);
        assertThat(spied.getPath()).isSameAs(devicesManager.getName());
    }

    @Test
    public void shouldBuildDefaultManagerUnitPath() {
        // preparing test data
        devicesManager.setSystem(devicesSubSystem);
        HardwareManager spied = spy(devicesManager);

        // acting
        String defaultPath = spied.defaultManagerUnitPath();

        // check the behavior
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(devicesSubSystem, atLeastOnce()).getSystemElementName();
        // check results
        assertThat(defaultPath).isEqualTo(devicesSubSystem.getSystemElementName() + "Manager");
    }

    @Test
    public void shouldGetSystemName() {
        // preparing test data
        devicesManager.setSystem(devicesSubSystem);
        HardwareManager spied = spy(devicesManager);
        assertThat(spied.getPath()).isSameAs(devicesManager.getPath());

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
        devicesManager.setSystem(devicesSubSystem);
        devicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(devicesManager);
        HardwareManager spied = spy(devicesManager);
        UnitRegistry.register(spied);
        assertThat(UnitRegistry.lookup(HardwareManager.class).getPath()).isEqualTo(devicesManager.getPath());

        // acting
        spied.connectTo(server);

        // check the behavior
        verify(spied).setOwner(server);
        // check results
        String connectedTasksManagerPath = serverPath + "/" + devicesSubSystem.getSystemElementName() + "Manager";
        assertThat(UnitRegistry.lookup(connectedTasksManagerPath, HardwareManager.class)).isSameAs(spied);
        assertThat(UnitRegistry.lookup(HardwareManager.class).getPath()).isEqualTo(connectedTasksManagerPath);
    }

    @Test
    public void shouldStartManager() throws IOException {
        // preparing test data
        devicesManager.setSystem(devicesSubSystem);
        devicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(devicesManager);
        HardwareManager manager = spy(devicesManager);
        UnitRegistry.register(manager);
        Factory<H, ?> factory = mock(Factory.class);
        manager.add(factory);

        // acting
        manager.Start();

        // check the behavior
        verify(manager).canStartUnit();
        verify(manager).startUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).startUnitChild(factory);
        verify(factory).Start();
        // check results
        assertThat(manager.isStarted()).isTrue();
        doReturn(manager).when(factory).getOwner();
    }

    @Test
    public void shouldStopManager() throws IOException {
        // preparing test data
        devicesManager.setSystem(devicesSubSystem);
        devicesManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(devicesManager);
        HardwareManager manager = spy(devicesManager);
        UnitRegistry.register(manager);
        Factory<H, ?> factory = mock(Factory.class);
        manager.add(factory);
        manager.Start();

        // acting
        manager.Stop();

        // check the behavior
        verify(manager).stopUnitRunnable();
        verify(manager, atLeastOnce()).runnableChildren();
        verify(manager).stopUnitChild(factory);
        verify(factory).Stop();
        // check results
        assertThat(manager.isStopped()).isTrue();
        doReturn(manager).when(factory).getOwner();
    }
}
