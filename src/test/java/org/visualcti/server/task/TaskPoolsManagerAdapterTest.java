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
package org.visualcti.server.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;

@SuppressWarnings("unchecked")
public class TaskPoolsManagerAdapterTest {
    TaskPoolsManagerAdapter manager;

    @Before
    public void setUp() throws IOException {
        UnitRegistry.clear();
        manager = spy(new TheManager());
        manager.setRootDirectoryName("work/tasks");
        UnitRegistry.register(manager);
    }

    @After
    public void tearDown() {
        UnitRegistry.clear();
    }

    @Test
    public void shouldAddCreatedTasksPool() throws IOException {
        // preparing test data
        String poolName = "pool1";
        String poolGroup = "poolGroup1";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        checkTasksListFileFor(poolName);

        // acting
        manager.add(poolUnit);

        // check the behavior
        verify(manager).isChild(poolUnit);
        verify(poolUnit).setOwner(manager);
        verify(manager).addBranch(poolUnit);
        // check results
        assertThat(manager.children().anyMatch(child -> child == poolUnit)).isTrue();
    }

    @Test
    public void shouldNotAddCreatedTasksPool_PoolExists() throws IOException {
        // preparing test data
        String poolName = "pool2";
        String poolGroup = "poolGroup2";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.children().anyMatch(child -> child == poolUnit)).isTrue();
        checkTasksListFileFor(poolName);
        reset(manager, poolUnit);

        // acting
        manager.add(poolUnit);

        // check the behavior
        verify(manager).isChild(poolUnit);
        verify(poolUnit, never()).setOwner(any(TaskPoolsManager.class));
        verify(manager, never()).addBranch(any(TasksPoolUnit.class));
    }

    @Test
    public void shouldDetachTaskPool() throws IOException {
        // preparing test data
        String poolName = "pool3";
        String poolGroup = "poolGroup3";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
        checkTasksListFileFor(poolName);
        reset(poolUnit, manager);

        // acting
        TasksPoolUnit detached = manager.detachTaskPool(poolName, poolGroup);

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(poolUnit, times(2)).close();
        verify(poolUnit, times(3)).getPoolName();
        verify(poolUnit, times(5)).getPoolGroup();
        verify(manager).isChild(poolUnit);
        verify(poolUnit).Stop();
        verify(manager).remove(poolUnit);
        verify(poolUnit).setOwner(null);
        verify(manager).removeBranch(poolUnit);
        // check results
        assertThat(detached).isSameAs(poolUnit);
        assertThat(manager.children().count()).isZero();
    }

    @Test
    public void shouldNotDetachTaskPool_NoPools() {
        // preparing test data
        String poolName = "pool4";
        String poolGroup = "poolGroup4";

        // acting
        TasksPoolUnit detached = manager.detachTaskPool(poolName, poolGroup);

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(manager, never()).isChild(any(TasksPoolUnit.class));
        // check results
        assertThat(detached).isNull();
    }

    @Test
    public void shouldGetPublicTaskPool() {
        // preparing test data
        String poolName = "pool5";
        String poolGroup = "poolGroup5";
        TasksPoolUnitAdapter poolUnit = (TasksPoolUnitAdapter) manager.createTaskPool("poolName", "poolGroup");
        checkTasksListFileFor("poolName");
        poolUnit.applyPoolNameFor(poolName, poolGroup, TasksPoolUnit.PoolType.PUBLIC);
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
        reset(poolUnit, manager);

        // acting
        TasksPoolUnit publicPool = manager.publicTaskPool();

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(poolUnit).isPublic();
        // check results
        assertThat(publicPool).isSameAs(poolUnit);
    }

    @Test
    public void shouldNotGetPublicTaskPool_NoPublicPools() {
        // preparing test data
        String poolName = "pool6";
        String poolGroup = "poolGroup6";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
        checkTasksListFileFor(poolName);
        reset(poolUnit, manager);

        // acting
        TasksPoolUnit publicPool = manager.publicTaskPool();

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(poolUnit).isPublic();
        // check results
        assertThat(publicPool).isNull();
    }

    @Test
    public void shouldGetTaskPool() {
        // preparing test data
        String poolName = "pool7";
        String poolGroup = "poolGroup7";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
        checkTasksListFileFor(poolName);
        reset(poolUnit, manager);

        // acting
        TasksPoolUnit taskPool = manager.getTaskPool(poolName, poolGroup);

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(poolUnit).getPoolName();
        verify(poolUnit).getPoolGroup();
        // check results
        assertThat(taskPool).isSameAs(poolUnit);
    }

    @Test
    public void shouldNotGetTaskPool_ButCreateOne() {
        // preparing test data
        String poolName = "pool8";
        String anotherPoolName = "poolName";
        String poolGroup = "poolGroup8";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.children().count()).isGreaterThan(0L);
        checkTasksListFileFor(poolName);
        reset(poolUnit, manager);

        // acting
        TasksPoolUnit taskPool = manager.getTaskPool(anotherPoolName, poolGroup);

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(poolUnit).getPoolName();
        verify(poolUnit, never()).getPoolGroup();
        verify(manager).createTaskPool(anotherPoolName, poolGroup);
        verify(manager).isChild(taskPool);
        verify(manager).getPath();
        verify(manager).addBranch(taskPool);
        // check results
        assertThat(taskPool).isNotSameAs(poolUnit);
        assertThat(taskPool.getPoolName()).isEqualTo(anotherPoolName);
        assertThat(taskPool.getPoolGroup()).isEqualTo(poolGroup);
        assertThat(taskPool.getOwner()).isSameAs(manager);
    }

    @Test
    public void shouldCreateTaskPool_TasksFileNotExist() throws IOException {
        // preparing test data
        String poolName = "pool9";
        String poolGroup = "poolGroup9";

        // acting
        TasksPoolUnitAdapter taskPool = (TasksPoolUnitAdapter) manager.createTaskPool(poolName, poolGroup);

        // check the behavior
        verify(taskPool).applyPoolNameFor(poolName, poolGroup, TasksPoolUnit.PoolType.LOCAL);
        verify(taskPool).loadOrCreateTasksList();
        verify(manager).getRoot();
        verify(taskPool, never()).restoreDocumentFrom(any(InputStream.class));
        verify(taskPool).store(any(Document.class), any(OutputStream.class));
        checkTasksListFileFor(poolName);
        // check results
        assertThat(taskPool).isNotNull();
        assertThat(taskPool.getPoolName()).isEqualTo(poolName);
        assertThat(taskPool.getPoolGroup()).isEqualTo(poolGroup);
        assertThat(taskPool.tasks()).isEmpty();
        assertThat(taskPool.getOwner()).isNull();
    }

    @Test
    public void shouldCreateTaskPool_TasksFileExists() throws IOException {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup10";
        File poolFile = new File(manager.getRoot(), poolName + ".tasks.pool");
        assertThat(poolFile).exists();
        reset(manager);

        // acting
        TasksPoolUnitAdapter taskPool = (TasksPoolUnitAdapter) manager.createTaskPool(poolName, poolGroup);

        // check the behavior
        verify(taskPool).applyPoolNameFor(poolName, poolGroup, TasksPoolUnit.PoolType.LOCAL);
        verify(taskPool).loadOrCreateTasksList();
        verify(manager).getRoot();
        verify(taskPool).restoreDocumentFrom(any(InputStream.class));
        verify(taskPool, never()).store(any(Document.class), any(OutputStream.class));
        // check results
        assertThat(taskPool).isNotNull();
        assertThat(taskPool.getPoolName()).isEqualTo(poolName);
        assertThat(taskPool.getPoolGroup()).isEqualTo(poolGroup);
        assertThat(taskPool.tasks()).hasSize(1);
        assertThat(taskPool.getOwner()).isNull();
    }

    @Test
    public void shouldNotCreateTaskPool_WrongPoolName() {
        // preparing test data
        String poolName = "dxxx??C1";
        String poolGroup = "poolGroup11";

        // acting
        TasksPoolUnit taskPool = manager.createTaskPool(poolName, poolGroup);

        // check the behavior
        verify(manager).getRoot();
        // check results
        assertThat(taskPool).isNull();
    }

    @Test
    public void shouldPrepareUnitParametersXML() {
        // preparing test data
        String rootDirectory = "root-directory";
        Element xml = new Element("TasksManager");
        manager.setRootDirectoryName(rootDirectory);

        // acting
        manager.prepareUnitParametersXML(xml);

        // check results
        assertThat(xml.getChild("parameter")).isNotNull();
        assertThat(xml.getChild("parameter").getAttributeValue("name")).isEqualTo("directory");
        assertThat(xml.getChild("parameter").getAttributeValue("type")).isEqualTo("string");
        assertThat(xml.getChild("parameter").getAttributeValue("value")).isEqualTo(rootDirectory);
    }

    @Test
    public void shouldSetXML_SetManagerRootDirectory() throws IOException, DataConversionException {
        // preparing test data
        String rootDirectory = "root-directory";
        File rootDirectoryFile = new File(rootDirectory);
        if (!rootDirectoryFile.exists()) {
            assertThat(rootDirectoryFile.mkdir()).isTrue();
        }
        Element parameterXml = new Element("parameter")
                .setAttribute("name", "directory")
                .setAttribute("type", "string")
                .setAttribute("value", rootDirectory);
        Element xml = new Element("TasksManager").addContent(parameterXml);
        assertThat(manager.getRoot()).hasName("tasks");

        // acting
        manager.setXML(xml);

        // check the behavior
        assertThat(rootDirectoryFile.delete()).isTrue();
        ArgumentCaptor<ConfigurationParameter> parameterCaptor = ArgumentCaptor.forClass(ConfigurationParameter.class);
        verify(manager).applyUnitParameter(parameterCaptor.capture());
        ConfigurationParameter parameter = parameterCaptor.getValue();
        // check results
        assertThat(manager.getRoot()).hasName(rootDirectory);
        String managerRootDirectory = parameter.getValue();
        assertThat(managerRootDirectory).isEqualTo(rootDirectory);
        assertThat(manager.getRoot()).isEqualTo(rootDirectoryFile);
        assertThat(xml.getChild("builder")).isNotNull();
    }

    @Test
    public void shouldNotSetXML_WrongRootDirectory() throws IOException, DataConversionException {
        // preparing test data
        String rootDirectory = "root-directory";
        Element parameterXml = new Element("parameter")
                .setAttribute("name", "directory")
                .setAttribute("type", "string")
                .setAttribute("value", rootDirectory);
        Element xml = new Element("TasksManager").addContent(parameterXml);
        assertThat(manager.getRoot()).hasName("tasks");

        // acting
        manager.setXML(xml);

        // check the behavior
        ArgumentCaptor<ConfigurationParameter> parameterCaptor = ArgumentCaptor.forClass(ConfigurationParameter.class);
        verify(manager).applyUnitParameter(parameterCaptor.capture());
        ConfigurationParameter parameter = parameterCaptor.getValue();
        // check results
        assertThat(manager.getRoot()).hasName("tasks");
        String managerRootDirectory = parameter.getValue();
        assertThat(managerRootDirectory).isEqualTo(rootDirectory);
        assertThat(xml.getChild("builder")).isNotNull();
    }

    @Test
    public void shouldApplyUnitParameter() {
        // preparing test data
        String rootDirectory = "root-directory";
        File rootDirectoryFile = new File(rootDirectory);
        if (!rootDirectoryFile.exists()) {
            assertThat(rootDirectoryFile.mkdir()).isTrue();
        }
        ConfigurationParameter parameter = ConfigurationParameter.of("directory", rootDirectory);
        assertThat(manager.getRoot()).hasName("tasks");

        // acting
        manager.applyUnitParameter(parameter);

        // check the behavior
        assertThat(rootDirectoryFile.delete()).isTrue();
        // check results
        assertThat(manager.getRoot()).hasName(rootDirectory);
    }

    @Test
    public void shouldNotApplyUnitParameter_ManagerRootDirectoryNotExists() {
        // preparing test data
        String rootDirectory = "root-directory";
        ConfigurationParameter parameter = ConfigurationParameter.of("directory", rootDirectory);
        assertThat(manager.getRoot()).hasName("tasks");

        // acting
        manager.applyUnitParameter(parameter);

        // check results
        assertThat(manager.getRoot()).hasName("tasks");
    }

    @Test
    public void shouldStartManagerAndPools() throws IOException {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup12";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        reset(poolUnit, manager);

        // acting
        manager.Start();

        // check the behavior
        verify(manager).isBroken();
        verify(manager).isStarted();
        verify(manager).canStartUnit();
        verify(manager).startUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).startUnitChild(poolUnit);
        verify(manager).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(manager).getMessageFactory();
        ArgumentCaptor<UnitMessage> managerMessageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(manager, times(2)).dispatch(managerMessageCaptor.capture());
        verify(poolUnit).isBroken();
        verify(poolUnit).isStarted();
        verify(poolUnit).canStartUnit();
        verify(poolUnit).startUnitRunnable();
        verify(poolUnit).runnableChildren();
        verify(poolUnit).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(poolUnit).getMessageFactory();
        ArgumentCaptor<UnitMessage> poolMessageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(poolUnit).dispatch(poolMessageCaptor.capture());
        // check results
        assertThat(manager.isStarted()).isTrue();
        assertThat(poolUnit.isStarted()).isTrue();
        List<UnitMessage> dispatched = managerMessageCaptor.getAllValues();
        assertThat(dispatched).hasSize(2);
        // dispatched from pool's dispatch to the owner
        assertThat(dispatched.get(0).getUnitPath()).isEqualTo(poolUnit.getPath());
        assertThat(dispatched.get(1).getUnitPath()).isEqualTo(manager.getPath());
        assertThat(managerMessageCaptor.getValue().getUnitPath()).isEqualTo(manager.getPath());
        assertThat(poolMessageCaptor.getValue().getUnitPath()).isEqualTo(poolUnit.getPath());
    }

    @Test
    public void shouldNotStartManagerAndPools_EmptyPoolIsNotStarted() throws IOException {
        // preparing test data
        String poolName = "pool13";
        String poolGroup = "poolGroup13";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        checkTasksListFileFor(poolName);
        reset(poolUnit, manager);

        // acting
        manager.Start();

        // check the behavior
        verify(manager).isBroken();
        verify(manager).isStarted();
        verify(manager).canStartUnit();
        verify(manager).startUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).startUnitChild(poolUnit);
        verify(manager).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(poolUnit).isBroken();
        verify(poolUnit).isStarted();
        verify(poolUnit).canStartUnit();
        verify(poolUnit, never()).startUnitRunnable();
        // check results
        assertThat(manager.isStarted()).isTrue();
        assertThat(poolUnit.isStarted()).isFalse();
    }

    @Test
    public void shouldNotStartManagerAndPools_NoPools() throws IOException {
        // preparing test data

        // acting
        manager.Start();

        // check the behavior
        verify(manager).isBroken();
        verify(manager).isStarted();
        verify(manager).canStartUnit();
        verify(manager, never()).startUnitRunnable();
        // check results
        assertThat(manager.isStarted()).isFalse();
    }

    @Test
    public void shouldStopManagerAndPools() throws IOException {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup14";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        manager.Start();
        assertThat(manager.isStarted()).isTrue();
        assertThat(poolUnit.isStarted()).isTrue();
        reset(poolUnit, manager);

        // acting
        manager.Stop();

        // check the behavior
        verify(manager).isBroken();
        verify(manager).isStopped();
        verify(manager).stopUnitRunnable();
        verify(manager).runnableChildren();
        verify(manager).stopUnitChild(poolUnit);
        verify(manager).currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        verify(manager).getMessageFactory();
        ArgumentCaptor<UnitMessage> managerMessageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(manager, times(2)).dispatch(managerMessageCaptor.capture());
        verify(poolUnit).isBroken();
        verify(poolUnit).isStopped();
        verify(poolUnit).stopUnitRunnable();
        verify(poolUnit).runnableChildren();
        verify(poolUnit).currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        verify(poolUnit).getMessageFactory();
        ArgumentCaptor<UnitMessage> poolMessageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(poolUnit).dispatch(poolMessageCaptor.capture());
        // check results
        assertThat(manager.isStopped()).isTrue();
        assertThat(poolUnit.isStopped()).isTrue();
        List<UnitMessage> dispatched = managerMessageCaptor.getAllValues();
        assertThat(dispatched).hasSize(2);
        // dispatched from pool's dispatch to the owner
        assertThat(dispatched.get(0).getUnitPath()).isEqualTo(poolUnit.getPath());
        assertThat(dispatched.get(1).getUnitPath()).isEqualTo(manager.getPath());
        assertThat(managerMessageCaptor.getValue().getUnitPath()).isEqualTo(manager.getPath());
        assertThat(poolMessageCaptor.getValue().getUnitPath()).isEqualTo(poolUnit.getPath());
    }

    @Test
    public void shouldExecuteStartManagerAndPools_NoResponseNeeded() throws Exception {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup15";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.isStarted()).isFalse();
        assertThat(poolUnit.isStarted()).isFalse();
        ServerCommandRequest request = manager.getMessageFactory()
                .buildFor(manager, MessageType.COMMAND, MessageFamilyType.START, "Starting the tasks manager");
        reset(poolUnit, manager);

        // acting
        manager.execute(request);

        // check the behavior
        verify(manager).Start();
        verify(manager).successfulResponseTo(eq(request), any(Consumer.class));
        verify(manager, atLeastOnce()).dispatch(any(UnitMessage.class));
        verify(poolUnit).Start();
        // check results
        assertThat(manager.isStarted()).isTrue();
        assertThat(poolUnit.isStarted()).isTrue();
    }

    @Test
    public void shouldExecuteStartManagerAndPools_WithResponse() throws Exception {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup16";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        assertThat(manager.isStarted()).isFalse();
        assertThat(poolUnit.isStarted()).isFalse();
        ServerCommandRequest request = manager.getMessageFactory()
                .buildFor(manager, MessageType.COMMAND, MessageFamilyType.START, "Starting the tasks manager");
        request.setNeedResponse(true);
        reset(poolUnit, manager);

        // acting
        manager.execute(request);

        // check the behavior
        verify(manager).Start();
        verify(manager).successfulResponseTo(eq(request), any(Consumer.class));
        ArgumentCaptor<UnitMessage> responseArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(manager, atLeastOnce()).dispatch(responseArgumentCaptor.capture());
        verify(poolUnit).Start();
        // check results
        assertThat(manager.isStarted()).isTrue();
        assertThat(poolUnit.isStarted()).isTrue();
        Optional<ServerCommandResponse> found = responseArgumentCaptor.getAllValues().stream()
                .filter(ServerCommandResponse.class::isInstance)
                .map(ServerCommandResponse.class::cast)
                .findFirst();
        assertThat(found).isPresent();
        ServerCommandResponse response = found.get();
        assertThat(response.getCorrelationID()).isEqualTo(request.getCorrelationID());
        assertThat(response.getUnitPath()).isEqualTo(manager.getPath());
    }

    @Test
    public void shouldExecuteStopManagerAndPools_NoResponseNeeded() throws Exception {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup17";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        manager.Start();
        assertThat(manager.isStopped()).isFalse();
        assertThat(poolUnit.isStopped()).isFalse();
        ServerCommandRequest request = manager.getMessageFactory()
                .buildFor(manager, MessageType.COMMAND, MessageFamilyType.STOP, "Stopping the tasks manager");
        reset(poolUnit, manager);

        // acting
        manager.execute(request);

        // check the behavior
        verify(manager).Stop();
        verify(manager).successfulResponseTo(eq(request), any(Consumer.class));
        verify(manager, atLeastOnce()).dispatch(any(UnitMessage.class));
        verify(poolUnit).Stop();
        // check results
        assertThat(manager.isStopped()).isTrue();
        assertThat(poolUnit.isStopped()).isTrue();
    }

    @Test
    public void shouldExecuteStopManagerAndPools_WithResponse() throws Exception {
        // preparing test data
        String poolName = "dxxxB1C1";
        String poolGroup = "poolGroup18";
        TasksPoolUnit poolUnit = manager.createTaskPool(poolName, poolGroup);
        manager.add(poolUnit);
        manager.Start();
        assertThat(manager.isStopped()).isFalse();
        assertThat(poolUnit.isStopped()).isFalse();
        ServerCommandRequest request = manager.getMessageFactory()
                .buildFor(manager, MessageType.COMMAND, MessageFamilyType.STOP, "Stopping the tasks manager");
        request.setNeedResponse(true);
        reset(poolUnit, manager);

        // acting
        manager.execute(request);

        // check the behavior
        verify(manager).Stop();
        verify(manager).successfulResponseTo(eq(request), any(Consumer.class));
        ArgumentCaptor<UnitMessage> responseArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(manager, atLeastOnce()).dispatch(responseArgumentCaptor.capture());
        verify(poolUnit).Stop();
        // check results
        assertThat(manager.isStopped()).isTrue();
        assertThat(poolUnit.isStopped()).isTrue();
        Optional<ServerCommandResponse> found = responseArgumentCaptor.getAllValues().stream()
                .filter(ServerCommandResponse.class::isInstance)
                .map(ServerCommandResponse.class::cast)
                .findFirst();
        assertThat(found).isPresent();
        ServerCommandResponse response = found.get();
        assertThat(response.getCorrelationID()).isEqualTo(request.getCorrelationID());
        assertThat(response.getUnitPath()).isEqualTo(manager.getPath());
    }

    // inner classes
    private static class TheManager extends TaskPoolsManagerAdapter {
        public TheManager() {
            this.unitPath = "Manager";
        }

        @Override
        public TasksPoolUnit createTaskPool(String name, String factory) {
            TasksPoolUnitAdapter pool = spy(new TasksPoolUnitAdapter() {
            });
            pool.localPoolFor(name, factory);
            try {
                pool.loadOrCreateTasksList();
            } catch (IOException e) {
                dispatchError(e, "Could not load or create tasks list");
                return null;
            }
            return pool;
        }
    }

    //private methods
    private void checkTasksListFileFor(String poolName) {
        File poolFile = new File(manager.getRoot(), poolName + ".tasks.pool");
        assertThat(poolFile).exists();
        poolFile.deleteOnExit();
    }
}
