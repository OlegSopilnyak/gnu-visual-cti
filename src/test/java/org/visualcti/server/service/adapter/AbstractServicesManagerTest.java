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
package org.visualcti.server.service.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.service.Service;
import org.visualcti.server.service.ServicesManager;

public class AbstractServicesManagerTest {
    ServicesManager manager;

    @Before
    public void setUp() throws Exception {
        manager = spy(new AbstractServicesManager() {
        });
    }

    @After
    public void tearDown() throws Exception {
        manager.Stop();
    }

    @Test
    public void shouldGetUnitType() {
        // preparing test data

        // acting
        String type = manager.getType();

        // check the behavior
        // check results
        assertThat("[manager]").isSameAs(type);
    }

    @Test
    public void shouldAddService() {
        // preparing test data
        Service service = mock(Service.class);

        // acting
        boolean added = manager.addService(service);

        // check the behavior
        verify(manager).add(service);
        // check results
        assertThat(added).isTrue();
        assertThat(manager.services().count()).isEqualTo(1);
    }

    @Test
    public void shouldAddServiceUnit() throws IOException {
        // preparing test data
        Service service = mock(Service.class);

        // acting
        boolean added = manager.add(service);

        // check the behavior
        verify(manager).isChild(service);
        verify(manager).isChildrenEnabled();
        verify(service).setOwner(manager);
        verify(manager).addBranch(service);
        // check results
        assertThat(added).isTrue();
        assertThat(manager.services().count()).isEqualTo(1);
    }

    @Test
    public void shouldNotAddJustServerUnit() {
        // preparing test data
        ServerUnit service = mock(ServerUnit.class);

        // acting
        boolean added = manager.add(service);

        // check the behavior
        verify(manager, never()).isChild(service);
        // check results
        assertThat(added).isFalse();
        assertThat(manager.services().count()).isZero();
    }

    @Test
    public void shouldRemoveService() throws IOException {
        // preparing test data
        Service service = mock(Service.class);
        assertThat(manager.addService(service)).isTrue();
        doReturn(manager).when(service).getOwner();
        reset(manager);

        // acting
        boolean removed = manager.removeService(service);

        // check the behavior
        verify(manager).remove(service);
        verify(manager).isChild(service);
        verify(manager).isChildrenEnabled();
        verify(service).setOwner(null);
        verify(manager).removeBranch(service);
        // check results
        assertThat(removed).isTrue();
        assertThat(manager.services().count()).isZero();
    }

    @Test
    public void shouldNotRemoveService_Empty() {
        // preparing test data
        Service service = mock(Service.class);

        // acting
        boolean removed = manager.removeService(service);

        // check the behavior
        verify(manager).remove(service);
        verify(manager).isChild(service);
        verify(manager, never()).isChildrenEnabled();
        // check results
        assertThat(removed).isFalse();
    }

    @Test
    public void shouldGetServices() {
        // preparing test data
        Service service = mock(Service.class);
        assertThat(manager.addService(service)).isTrue();
        doReturn(manager).when(service).getOwner();

        // acting
        Stream<Service> services = manager.services();

        // check the behavior
        verify(manager).children();
        // check results
        assertThat(services.toArray()).hasSize(1).contains(service);
    }

    @Test
    public void shouldFoundService() {
        // preparing test data
        String serviceName = "test-service";
        Service service = mock(Service.class);
        doReturn(serviceName).when(service).getName();
        assertThat(manager.addService(service)).isTrue();
        doReturn(manager).when(service).getOwner();
        reset(manager);

        // acting
        Optional<Service> found = manager.findService(serviceName);

        // check the behavior
        verify(manager).services();
        verify(service).getName();
        // check results
        assertThat(found).isPresent().contains(service);
    }

    @Test
    public void shouldNotFoundService_WrongServiceName() {
        // preparing test data
        String serviceName = "test-service";
        Service service = mock(Service.class);
        doReturn(serviceName).when(service).getName();
        assertThat(manager.addService(service)).isTrue();
        doReturn(manager).when(service).getOwner();
        reset(manager);

        // acting
        Optional<Service> found = manager.findService("serviceName");

        // check the behavior
        verify(manager).services();
        verify(service).getName();
        // check results
        assertThat(found).isEmpty();
    }

    @Test
    public void shouldStartServices() throws IOException {
        // preparing test data
        Service service = mock(Service.class);
        assertThat(manager.addService(service)).isTrue();
        assertThat(manager.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        assertThat(manager.isStarted()).isFalse();

        // acting
        manager.Start();

        // check the behavior
        verify(manager).canStartUnit();
        verify(manager).startUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).startUnitChild(service);
        verify(service).Start();
        // check results
        assertThat(manager.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.ACTIVE);
        assertThat(manager.isStarted()).isTrue();
    }

    @Test
    public void shouldStopServices() throws IOException {
        // preparing test data
        Service service = mock(Service.class);
        assertThat(manager.addService(service)).isTrue();
        manager.Start();
        assertThat(manager.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.ACTIVE);
        assertThat(manager.isStarted()).isTrue();
        reset(manager);

        // acting
        manager.Stop();

        // check the behavior
        verify(manager).stopUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).stopUnitChild(service);
        verify(service).Stop();
        // check results
        assertThat(manager.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        assertThat(manager.isStarted()).isFalse();
    }

    @Test
    public void shouldGetManagerXML() {
    }

    @Test
    public void shouldSetXManagerML() {
    }
}