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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Executor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.device.Factory;

public class AbstractDevicesManagerTest {
    AbstractDevicesManager manager;

    @Before
    public void setUp() throws Exception {
        manager = spy(new AbstractDevicesManager());
    }

    @After
    public void tearDown() throws Exception {
        manager.Stop();
    }

    @Test
    public void shouldGetDeviceEventExecutor() throws IOException {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);
        manager.Start();
        reset(manager);

        // acting
        Executor executor = manager.deviceEventExecutor();

        // check the behavior
        // check results
        assertThat(executor).isNotNull();
    }

    @Test
    public void shouldNotGetDeviceEventExecutor_NoFactoriesAdded() throws IOException {
        // preparing test data
        manager.Start();
        reset(manager);

        // acting
        Executor executor = manager.deviceEventExecutor();

        // check the behavior
        // check results
        assertThat(executor).isNull();
    }

    @Test
    public void shouldNotGetDeviceEventExecutor_ManagerNotStarted() {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);
        reset(manager);

        // acting
        Executor executor = manager.deviceEventExecutor();

        // check the behavior
        // check results
        assertThat(executor).isNull();
    }

    @Test
    public void shouldStartUnitRunnable() throws IOException {
        // preparing test data
        assertThat(manager.deviceEventExecutor()).isNull();
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);

        // acting
        manager.Start();

        // check the behavior
        verify(manager).startUnitRunnable();
        // check results
        assertThat(manager.deviceEventExecutor()).isNotNull();
    }

    @Test
    public void shouldStopUnitRunnable() throws IOException {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);
        manager.Start();
        assertThat(manager.deviceEventExecutor()).isNotNull();
        reset(manager);

        // acting
        manager.Stop();

        // check the behavior
        verify(manager).stopUnitRunnable();
        // check results
        assertThat(manager.deviceEventExecutor()).isNull();
    }

    @Test
    public void shouldGetFactories() {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);

        // acting
        Object[] factories = manager.factories().toArray();

        // check the behavior
        verify(manager).children();
        // check results
        assertThat(factories).contains(factory);
    }

    @Test
    public void shouldAddDeviceFactory() throws IOException {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        assertThat(manager.factories().findAny()).isEmpty();

        // acting
        manager.addDeviceFactory(factory);

        // check the behavior
        verify(manager).add(factory);
        verify(manager).isChild(factory);
        verify(factory).setOwner(manager);
        verify(manager).addBranch(factory);
        // check results
        assertThat(manager.factories().findAny()).isPresent().contains(factory);
    }

    @Test
    public void shouldRemoveDeviceFactory() throws IOException {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);
        assertThat(manager.factories().findAny()).isPresent().contains(factory);
        reset(manager);
        doReturn(manager).when(factory).getOwner();

        // acting
        manager.removeDeviceFactory(factory);

        // check the behavior
        verify(manager).remove(factory);
        verify(manager).isChild(factory);
        verify(factory).setOwner(null);
        verify(manager).removeBranch(factory);
        verify(factory).close();
        // check results
        assertThat(manager.factories().findAny()).isEmpty();
    }

    @Test
    public void shouldStartManager() throws IOException {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);
        assertThat(manager.factories().findAny()).isPresent().contains(factory);

        // acting
        manager.Start();

        // check the behavior
        verify(manager).isBroken();
        verify(manager).isStarted();
        verify(manager).canStartUnit();
        verify(manager).startUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).startUnitChild(factory);
        verify(factory).Start();
        // check results
        assertThat(manager.isStarted()).isTrue();
        assertThat(manager.deviceEventExecutor()).isNotNull();
    }

    @Test
    public void shouldStopManager() throws IOException {
        // preparing test data
        Factory<?,?> factory = mock(Factory.class);
        manager.addDeviceFactory(factory);
        assertThat(manager.factories().findAny()).isPresent().contains(factory);
        manager.Start();
        assertThat(manager.isStopped()).isFalse();
        assertThat(manager.deviceEventExecutor()).isNotNull();
        reset(manager);

        // acting
        manager.Stop();

        // check the behavior
        verify(manager).isBroken();
        verify(manager).isStopped();
        verify(manager).stopUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).stopUnitChild(factory);
        verify(factory).Stop();
        // check results
        assertThat(manager.isStopped()).isTrue();
        assertThat(manager.deviceEventExecutor()).isNull();
    }
}
