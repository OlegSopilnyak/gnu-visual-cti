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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.task.AbstractTasksPool;

@SuppressWarnings("unchecked")
public class TasksManagerTest {
    TasksManager tasksManager;

    @Before
    public void setUp() throws Exception {
        tasksManager = TasksManager.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        tasksManager.Stop();
        tasksManager.setSystem(null);
    }

    @Test
    public void shouldGetManagerInstance() {
        // preparing test data

        // acting
        TasksManager manager = TasksManager.getInstance();

        // check results
        assertThat(manager).isSameAs(tasksManager);
    }

    @Test
    public void shouldGetName() {
        // preparing test data

        // acting
        String unitName = tasksManager.getName();

        // check results
        assertThat("Tasks Manager").isEqualTo(unitName);
    }

    @Test
    public void shouldGetUnitDescription() {
        // preparing test data

        // acting
        String unitDescription = tasksManager.getUnitDescription().trim();

        // check results
        assertThat("The manager of tasks pools").isEqualTo(unitDescription);
    }

    @Test
    public void shouldGetRootElementName() {
        // preparing test data

        // acting
        String rootElementName = tasksManager.getRootElementName();

        // check results
        assertThat("Manager").isEqualTo(rootElementName);
    }

    @Test
    public void shouldGetSystem() {
        // preparing test data
        SubSystem tasks = mock(SubSystem.class);
        tasksManager.setSystem(tasks);

        // acting
        SubSystem subSystem = tasksManager.getSystem();

        // check results
        assertThat(subSystem).isEqualTo(tasks);
    }

    @Test
    public void shouldNotGetSystem_NotAssigned() {
        // preparing test data

        // acting
        SubSystem subSystem = tasksManager.getSystem();

        // check results
        assertThat(subSystem).isNull();
    }

    @Test
    public void shouldCreateTaskPool() throws IOException, ServerUnitException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        String managerUnitPath = "TestTasksManager";
        File managerRoot = new File(".");
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        doReturn(managerUnitPath).when(taskPoolsManager).getPath();
        doReturn(managerRoot).when(taskPoolsManager).getRoot();
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();


        // acting
        TasksPoolUnit pool = tasksManager.createTaskPool(poolName, factoryName);

        // check results
        assertThat(pool).isNotNull();
        assertThat(pool.getPoolName()).isEqualTo(poolName);
        assertThat(pool.getPoolGroup()).isEqualTo(factoryName);
        assertThat(pool.getName()).isEqualTo(factoryName + "/" + poolName);
        assertThat(tasksPoolFile.delete()).isTrue();
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
    }

    @Test
    public void shouldDoBeforeRegisterUnit() {
        // preparing test data
        String systemName = "TasksSystem";
        SubSystem tasks = mock(SubSystem.class);
        doReturn(systemName).when(tasks).getSystemElementName();
        tasksManager.setSystem(tasks);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isEmpty();

        // acting
        spied.beforeRegisterUnit();

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(tasks).getSystemElementName();
        // check results
        assertThat(spied.getPath()).isEqualTo(systemName + "Manager");
    }

    @Test
    public void shouldDoNotBeforeRegisterUnit_NoSystemAssociated() {
        // preparing test data
        SubSystem tasks = mock(SubSystem.class);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isEmpty();

        // acting
        Exception e = assertThrows(Exception.class, spied::beforeRegisterUnit);

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(tasks, never()).getSystemElementName();
        // check results
        assertThat(e).isInstanceOf(NullPointerException.class);
        assertThat(spied.getPath()).isEmpty();
    }

    @Test
    public void shouldBuildDefaultManagerUnitPath() {
        // preparing test data
        String systemName = "TasksSystem";
        SubSystem tasks = mock(SubSystem.class);
        doReturn(systemName).when(tasks).getSystemElementName();
        tasksManager.setSystem(tasks);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isEmpty();

        // acting
        String defaultPath = spied.defaultManagerUnitPath();

        // check the behavior
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(tasks).getSystemElementName();
        // check results
        assertThat(defaultPath).isEqualTo(systemName + "Manager");
    }

    @Test
    public void shouldGetSystemName() {
        // preparing test data
        String systemName = "TasksSystem";
        SubSystem tasks = mock(SubSystem.class);
        doReturn(systemName).when(tasks).getSystemElementName();
        tasksManager.setSystem(tasks);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isEmpty();

        // acting
        String managerSystemName = spied.getSystemName();

        // check the behavior
        verify(spied).getSystem();
        verify(tasks).getSystemElementName();
        // check results
        assertThat(managerSystemName).isEqualTo(systemName);
    }

    @Test
    public void shouldConnectToTheApplicationServer() throws IOException, ServerUnitException {
        // preparing test data
        String serverPath = "TestServer";
        ApplicationServer server = mock(ApplicationServer.class);
        doReturn(serverPath).when(server).getPath();
        String systemName = "TasksSystem";
        SubSystem tasks = mock(SubSystem.class);
        doReturn(systemName).when(tasks).getSystemElementName();
        tasksManager.setSystem(tasks);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isEmpty();
        UnitRegistry.register(spied);
        assertThat(UnitRegistry.lookup(TasksManager.class).getPath()).isEmpty();

        // acting
        spied.connectTo(server);

        // check the behavior
        verify(spied).setOwner(server);
        // check results
        String connectedTasksManagerPath = serverPath + "/" + systemName + "Manager";
        assertThat(UnitRegistry.lookup(connectedTasksManagerPath, TaskPoolsManager.class)).isSameAs(spied);
        assertThat(UnitRegistry.lookup(TaskPoolsManager.class).getPath()).isEqualTo(connectedTasksManagerPath);
    }

    @Test
    public void shouldGetRoot() {
        // preparing test data

        // acting
        File poolsRootFile = tasksManager.getRoot();

        // check results
        assertThat(poolsRootFile).isNotNull();
    }

    @Test
    public void shouldGetPublicTaskPool() throws IOException, ServerUnitException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        String managerUnitPath = "TestTasksManager";
        File managerRoot = new File(".");
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        doReturn(managerUnitPath).when(taskPoolsManager).getPath();
        doReturn(managerRoot).when(taskPoolsManager).getRoot();
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        doReturn(TasksPoolUnit.PoolType.PUBLIC).when(pool).getPoolType();
        taskPoolsManager.add(pool);

        // acting
        TasksPoolUnit publicPool = taskPoolsManager.publicTaskPool();

        // check the behavior
        verify(taskPoolsManager).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager).children();
        // check results
        assertThat(publicPool).isNotNull();
        assertThat(publicPool.isPublic()).isTrue();
        assertThat(publicPool.getPoolName()).isEqualTo(poolName);
        assertThat(publicPool.getPoolGroup()).isEqualTo(factoryName);
        assertThat(publicPool.getName()).isEqualTo(factoryName + "/" + poolName);
        assertThat(tasksPoolFile.delete()).isTrue();
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
        assertThat(UnitRegistry.lookup(publicPool.getPath(), TasksPoolUnit.class)).isSameAs(publicPool);
    }

    @Test
    public void shouldGetLocalTaskPoolStream() throws ServerUnitException, IOException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        String managerUnitPath = "TestTasksManager";
        File managerRoot = new File(".");
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        doReturn(managerUnitPath).when(taskPoolsManager).getPath();
        doReturn(managerRoot).when(taskPoolsManager).getRoot();
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);

        // acting
        Stream<TasksPoolUnit> localPools = taskPoolsManager.localTaskPoolStream();

        // check the behavior
        verify(taskPoolsManager).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager).children();
        // check results
        assertThat(localPools).isNotNull();
        TasksPoolUnit localPool = localPools.findFirst().orElse(null);
        assertThat(localPool).isNotNull();
        assertThat(localPool.isPublic()).isFalse();
        assertThat(localPool.getPoolName()).isEqualTo(poolName);
        assertThat(localPool.getPoolGroup()).isEqualTo(factoryName);
        assertThat(localPool.getName()).isEqualTo(factoryName + "/" + poolName);
        assertThat(tasksPoolFile.delete()).isTrue();
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
        assertThat(UnitRegistry.lookup(localPool.getPath(), TasksPoolUnit.class)).isSameAs(localPool);
    }

    @Test
    public void shouldGetTaskPool() throws IOException, ServerUnitException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        String managerUnitPath = "TestTasksManager";
        File managerRoot = new File(".");
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        doReturn(managerUnitPath).when(taskPoolsManager).getPath();
        doReturn(managerRoot).when(taskPoolsManager).getRoot();
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);

        // acting
        TasksPoolUnit localPool = taskPoolsManager.getTaskPool(poolName, factoryName);

        // check the behavior
        verify(taskPoolsManager).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager).children();
        // check results
        assertThat(localPool).isNotNull();
        assertThat(localPool.isPublic()).isFalse();
        assertThat(localPool.getPoolName()).isEqualTo(poolName);
        assertThat(localPool.getPoolGroup()).isEqualTo(factoryName);
        assertThat(localPool.getName()).isEqualTo(factoryName + "/" + poolName);
        assertThat(tasksPoolFile.delete()).isTrue();
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
        assertThat(UnitRegistry.lookup(localPool.getPath(), TasksPoolUnit.class)).isSameAs(localPool);
    }

    @Test
    public void shouldDetachTaskPool() throws IOException, ServerUnitException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        String managerUnitPath = "TestTasksManager";
        File managerRoot = new File(".");
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        doReturn(managerUnitPath).when(taskPoolsManager).getPath();
        doReturn(managerRoot).when(taskPoolsManager).getRoot();
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);
        assertThat(taskPoolsManager.taskPoolStreamBy(unit -> true).toArray()).contains(pool);

        // acting
        TasksPoolUnit detachedPool = taskPoolsManager.detachTaskPool(poolName, factoryName);

        // check the behavior
        verify(taskPoolsManager, atLeastOnce()).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager, atLeastOnce()).children();
        verify(pool).Stop();
        verify(taskPoolsManager).remove(pool);
        // check results
        assertThat(taskPoolsManager.taskPoolStreamBy(unit -> true).toArray()).isEmpty();
        assertThat(detachedPool).isNotNull();
        assertThat(detachedPool.isPublic()).isFalse();
        assertThat(detachedPool.getPoolName()).isEqualTo(poolName);
        assertThat(detachedPool.getPoolGroup()).isEqualTo(factoryName);
        assertThat(detachedPool.getName()).isEqualTo(factoryName + "/" + poolName);
        assertThat(tasksPoolFile.delete()).isTrue();
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
    }

    @Test
    public void start() {
    }

    @Test
    public void stop() {
    }
}