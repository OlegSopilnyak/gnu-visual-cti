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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.EngineUnitBuilder;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.task.AbstractTasksPool;

@SuppressWarnings("unchecked")
public class TasksManagerTest {
    String configurationXml = "<Manager package=\"org.visualcti.server.system.manager\" class=\"TasksManager\" extends=\"org.visualcti.server.core.executable.task.TaskPoolsManager\">\n" +
            "  <!-- The manager of tasks pools -->\n" +
            "  <builder package=\"org.visualcti.server.system.manager\" class=\"TasksManager\" method=\"getInstance\" />\n" +
            "  <parameter name=\"directory\" type=\"string\" value=\"work/tasks\" />\n" +
            "</Manager>\n\n";
    XmlAware xmlEngine;
    TasksManager tasksManager;
    String tasksSubSystemName = "Tasks";
    SubSystem tasksSubSystem;

    @Before
    public void setUp() throws Exception {
        xmlEngine = new XmlAware() {
        };
        tasksManager = TasksManager.getInstance();
        tasksManager.resetUnitPath();
        tasksSubSystem = mock(SubSystem.class);
        doReturn(tasksSubSystemName).when(tasksSubSystem).getSystemElementName();
    }

    @After
    public void tearDown() throws Exception {
        tasksManager.Stop();
        tasksManager.setSystem(null);
        tasksManager.setOwner(null);
        UnitRegistry.clear();
    }

    @Test
    public void shouldGetXML() throws IOException {
        // preparing test data
        tasksManager.setRootDirectoryName("work/tasks");
        TasksManager manager = spy(tasksManager);

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
    public void shouldBuildManagerUnit() throws IOException {
        // preparing test data
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes()));

        // acting
        TasksManager manager = EngineUnitBuilder.getInstance().build(xml, mng -> mng.setSystem(tasksSubSystem));

        // check results
        assertThat(manager).isSameAs(tasksManager);
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
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
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
    public void shouldDoBeforeRegisterUnit() throws IOException {
        // preparing test data
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(tasksManager);
        TasksManager spied = spy(tasksManager);
        UnitRegistry.register(spied);
        spied.resetUnitPath();
        // acting
        spied.beforeRegisterUnit();

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(tasksSubSystem, atLeastOnce()).getSystemElementName();
        // check results
        assertThat(spied.getPath()).isEqualTo(tasksSubSystem.getSystemElementName() + "Manager");
    }

    @Test
    public void shouldDoNotBeforeRegisterUnit_NoSystemAssociated() {
        // preparing test data
        SubSystem tasks = mock(SubSystem.class);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isSameAs(tasksManager.getName());

        // acting
        Exception e = assertThrows(Exception.class, spied::beforeRegisterUnit);

        // check the behavior
        verify(spied).defaultManagerUnitPath();
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(tasks, never()).getSystemElementName();
        // check results
        assertThat(e).isInstanceOf(NullPointerException.class);
        assertThat(spied.getPath()).isSameAs(tasksManager.getName());
    }

    @Test
    public void shouldBuildDefaultManagerUnitPath() {
        // preparing test data
        tasksManager.setSystem(tasksSubSystem);
        TasksManager spied = spy(tasksManager);

        // acting
        String defaultPath = spied.defaultManagerUnitPath();

        // check the behavior
        verify(spied).getSystemName();
        verify(spied).getSystem();
        verify(tasksSubSystem, atLeastOnce()).getSystemElementName();
        // check results
        assertThat(defaultPath).isEqualTo(tasksSubSystem.getSystemElementName() + "Manager");
    }

    @Test
    public void shouldGetSystemName() {
        // preparing test data
        tasksManager.setSystem(tasksSubSystem);
        TasksManager spied = spy(tasksManager);
        assertThat(spied.getPath()).isSameAs(tasksManager.getPath());

        // acting
        String managerSystemName = spied.getSystemName();

        // check the behavior
        verify(spied).getSystem();
        verify(tasksSubSystem, atLeastOnce()).getSystemElementName();
        // check results
        assertThat(managerSystemName).isEqualTo(tasksSubSystem.getSystemElementName());
    }

    @Test
    public void shouldConnectToTheApplicationServer() throws IOException, ServerUnitException {
        // preparing test data
        String serverPath = "TestServer";
        ApplicationServer server = mock(ApplicationServer.class);
        doReturn(serverPath).when(server).getPath();
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        UnitRegistry.unRegister(tasksManager);
        TasksManager spied = spy(tasksManager);
        UnitRegistry.register(spied);
        assertThat(UnitRegistry.lookup(TasksManager.class).getPath()).isEqualTo(tasksManager.getPath());

        // acting
        spied.connectTo(server);

        // check the behavior
        verify(spied).setOwner(server);
        // check results
        String connectedTasksManagerPath = serverPath + "/" + tasksSubSystem.getSystemElementName() + "Manager";
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
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        doReturn(TasksPoolUnit.PoolType.PUBLIC).when(pool).getPoolType();
        taskPoolsManager.add(pool);

        // acting
        TasksPoolUnit publicPool = taskPoolsManager.publicTaskPool();

        // check the behavior
        verify(taskPoolsManager, times(2)).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager, times(2)).children();
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
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);

        // acting
        Stream<TasksPoolUnit> localPools = taskPoolsManager.localTaskPoolStream();

        // check the behavior
        verify(taskPoolsManager, times(2)).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager, times(2)).children();
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
        assertThat(UnitRegistry.lookup(localPool.getPath(), TasksPoolUnit.class)).isSameAs(pool);
    }

    @Test
    public void shouldGetTaskPool() throws IOException, ServerUnitException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);

        // acting
        TasksPoolUnit localPool = taskPoolsManager.getTaskPool(poolName, factoryName);

        // check the behavior
        verify(taskPoolsManager, times(2)).taskPoolStreamBy(any(Predicate.class));
        verify(taskPoolsManager, times(2)).children();
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
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
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
    public void shouldStartManager() throws IOException, ServerUnitException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);
        assertThat(taskPoolsManager.taskPoolStreamBy(unit -> true).toArray()).contains(pool);
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
        assertThat(UnitRegistry.lookup(pool.getPath(), TasksPoolUnit.class)).isSameAs(pool);

        // acting
        taskPoolsManager.Start();

        // check the behavior
        verify(taskPoolsManager).canStartUnit();
        verify(taskPoolsManager).startUnitRunnable();
        verify(taskPoolsManager).runnableChildren();
        verify(taskPoolsManager).startUnitChild(pool);
        verify(pool).Start();
        verify(pool).canStartUnit();
        verify(pool, never()).startUnitRunnable();
        // check results
        assertThat(taskPoolsManager.isStarted()).isTrue();
        assertThat(pool.isStarted()).isFalse();
    }

    @Test
    public void shouldStopManager() throws ServerUnitException, IOException {
        // preparing test data
        String poolName = "testPool";
        String factoryName = "testFactory";
        tasksManager.setSystem(tasksSubSystem);
        tasksManager.configure(xmlEngine.load(new ByteArrayInputStream(configurationXml.getBytes())));
        String managerUnitPath = tasksManager.getPath();
        File managerRoot = tasksManager.getRoot();
        UnitRegistry.unRegister(tasksManager);
        TaskPoolsManager taskPoolsManager = spy(tasksManager);
        UnitRegistry.register(taskPoolsManager);
        File tasksPoolFile = new File(managerRoot, poolName + ".tasks.pool");
        tasksPoolFile.deleteOnExit();
        AbstractTasksPool pool = spy((AbstractTasksPool) tasksManager.createTaskPool(poolName, factoryName));
        taskPoolsManager.add(pool);
        assertThat(taskPoolsManager.taskPoolStreamBy(unit -> true).toArray()).contains(pool);
        assertThat(UnitRegistry.lookup(managerUnitPath, TaskPoolsManager.class)).isSameAs(taskPoolsManager);
        assertThat(UnitRegistry.lookup(pool.getPath(), TasksPoolUnit.class)).isSameAs(pool);
        taskPoolsManager.Start();

        // acting
        taskPoolsManager.Stop();

        // check the behavior
        verify(taskPoolsManager).stopUnitRunnable();
        verify(taskPoolsManager, atLeastOnce()).runnableChildren();
        verify(taskPoolsManager).stopUnitChild(pool);
        verify(pool).Stop();
        verify(pool).isStopped();
        verify(pool, never()).stopUnitRunnable();
        // check results
        assertThat(taskPoolsManager.isStopped()).isTrue();
        assertThat(pool.isStopped()).isTrue();
    }
}