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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.Parameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.util.Tools;

@SuppressWarnings("unchecked")
public class TasksPoolUnitAdapterTest {
    TasksPoolUnitAdapter tasksPool;
    TaskPoolsManager poolsManager;

    @Before
    public void setUp() throws Exception {
        poolsManager = createTaskPoolsManager();
        tasksPool = spy(new TasksPoolUnitAdapter(){});
    }

    @After
    public void tearDown() throws Exception {
        tasksPool.Stop();
        UnitRegistry.clear();
    }

    @Test
    public void shouldGetName_WithGroup() {
        // preparing test data
        tasksPool.localPoolFor("name", "group");

        // acting
        String name = tasksPool.getName();

        // check the behavior
        verify(tasksPool, times(2)).getPoolGroup();
        verify(tasksPool).getPoolName();
        // check results
        assertThat(name).isEqualTo("group/name");
    }

    @Test
    public void shouldGetName_WithoutGroup() {
        // preparing test data
        tasksPool.localPoolFor("name", null);

        // acting
        String name = tasksPool.getName();

        // check the behavior
        verify(tasksPool).getPoolGroup();
        verify(tasksPool).getPoolName();
        // check results
        assertThat(name).isEqualTo("name");
    }

    @Test
    public void shouldGetCurrent() throws IOException {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(current).when(current).clone();
        doReturn(new Element("task")).when(current).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(current, true);
        tasksPool.Start();

        // acting
        Task task = tasksPool.current();

        // check results
        assertThat(task).isSameAs(current);
    }

    @Test
    public void shouldNotGetCurrent_EmptyTasksList() throws IOException {
        // preparing test data
        tasksPool.Start();

        // acting
        Task task = tasksPool.current();

        // check results
        assertThat(task).isNull();
    }

    @Test
    public void shouldNotGetCurrent_NotStarted() throws IOException {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(current).when(current).clone();
        doReturn(new Element("task")).when(current).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(current, true);

        // acting
        Task task = tasksPool.current();

        // check results
        assertThat(task).isNull();
    }

    @Test
    public void shouldGetNext_OneTask() throws IOException {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(current).when(current).clone();
        doReturn(new Element("task-1")).when(current).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(current, true);
        tasksPool.Start();

        // acting
        Task task = tasksPool.current();

        // check results
        assertThat(task).isSameAs(current);
        assertThat(tasksPool.next()).isSameAs(current);
        assertThat(tasksPool.next()).isSameAs(current);
    }

    @Test
    public void shouldGetNext_TwoTasks() throws IOException {
        // preparing test data
        Task task1 = mock(Task.class);
        doReturn("test-1").when(task1).getName();
        doReturn(task1).when(task1).clone();
        Task task2 = mock(Task.class);
        doReturn("test-2").when(task2).getName();
        doReturn(task2).when(task2).clone();
        doReturn(new Element("task-1")).when(task1).getXML();
        doReturn(new Element("task-2")).when(task2).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        tasksPool.Start();

        // acting
        Task task = tasksPool.current();

        // check results
        assertThat(task).isSameAs(task1);
        assertThat(tasksPool.next()).isSameAs(task2);
        assertThat(tasksPool.next()).isSameAs(task1);
    }

    @Test
    public void shouldNotGetNext_NotStarted() throws IOException {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(new Element("task")).when(current).getXML();
        doReturn(current).when(current).clone();
        doReturn(new Element("task")).when(current).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(current, true);

        // acting
        Task task = tasksPool.current();

        // check results
        assertThat(task).isNull();
        assertThat(tasksPool.next()).isNull();
    }

    @Test
    public void shouldAddTask() throws IOException {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        doReturn(new Element("task")).when(task).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        assertThat(tasksPool.tasks()).isEmpty();

        // acting
        boolean modified = tasksPool.addTask(task, true);

        // check the behavior
        verify(tasksPool).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(modified).isTrue();
        assertThat(tasksPool.tasks()).containsExactly(task);
    }

    @Test
    public void shouldNotAddTask_WrongTask() throws IOException {
        // preparing test data
        assertThat(tasksPool.tasks()).isEmpty();

        // acting
        boolean modifiedMock = tasksPool.addTask(mock(Task.class), true);
        boolean modifiedNull = tasksPool.addTask(null, true);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool, times(2)).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues().get(0)).isInstanceOf(UnitActionError.class);
        assertThat(captor.getAllValues().get(1)).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).isEmpty();
        assertThat(modifiedMock).isFalse();
        assertThat(modifiedNull).isFalse();
    }

    @Test
    public void shouldNotAddTask_TaskAlreadyThere() throws IOException {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        doReturn(new Element("task")).when(task).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task);

        // acting
        boolean modified = tasksPool.addTask(task, true);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getAllValues().get(0)).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task);
        assertThat(modified).isFalse();
    }

    @Test
    public void shouldRemoveTask_TaskAlreadyThere() throws IOException {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        doReturn(new Element("task")).when(task).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        boolean modified = tasksPool.removeTask(task);

        // check the behavior
        verify(tasksPool).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(tasksPool.tasks()).isEmpty();
        assertThat(modified).isTrue();
    }

    @Test
    public void shouldNotRemoveTask_TaskNotThere() throws IOException {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();

        // acting
        boolean modified = tasksPool.removeTask(task);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).isEmpty();
        assertThat(modified).isFalse();
    }

    @Test
    public void shouldNotRemoveTask_WrongTask() throws IOException {
        // preparing test data
        assertThat(tasksPool.tasks()).isEmpty();

        // acting
        boolean modifiedMock = tasksPool.removeTask(mock(Task.class));
        boolean modifiedNull = tasksPool.removeTask(null);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool, times(2)).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues().get(0)).isInstanceOf(UnitActionError.class);
        assertThat(captor.getAllValues().get(1)).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).isEmpty();
        assertThat(modifiedMock).isFalse();
        assertThat(modifiedNull).isFalse();
    }

    @Test
    public void shouldUpdateTask_TaskAlreadyThere() throws IOException {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        doReturn(new Element("task")).when(task).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        boolean modified = tasksPool.updateTask(task);

        // check the behavior
        verify(tasksPool).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task);
        assertThat(modified).isTrue();
    }

    @Test
    public void shouldNotUpdateTask_TaskNotThere() throws IOException {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        assertThat(tasksPool.tasks()).isEmpty();

        // acting
        boolean modified = tasksPool.updateTask(task);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).isEmpty();
        assertThat(modified).isFalse();
    }

    @Test
    public void shouldMoveTaskUp() throws IOException {
        // preparing test data
        Task task1 = mock(Task.class);
        doReturn("test-1").when(task1).getName();
        doReturn(task1).when(task1).clone();
        Task task2 = mock(Task.class);
        doReturn("test-2").when(task2).getName();
        doReturn(task2).when(task2).clone();
        doReturn(new Element("task-1")).when(task1).getXML();
        doReturn(new Element("task-2")).when(task2).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        boolean modified = tasksPool.moveTaskUp(task2);

        // check the behavior
        verify(tasksPool).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task2, task1);
        assertThat(modified).isTrue();
    }

    @Test
    public void shouldNotMoveTaskUp_OnTopAlready() throws IOException {
        // preparing test data
        Task task1 = mock(Task.class);
        doReturn("test-1").when(task1).getName();
        Task task2 = mock(Task.class);
        doReturn("test-2").when(task2).getName();
        doReturn(task1).when(task1).clone();
        doReturn(task2).when(task2).clone();
        doReturn(new Element("task-1")).when(task1).getXML();
        doReturn(new Element("task-2")).when(task2).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        boolean modified = tasksPool.moveTaskUp(task1);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        assertThat(modified).isFalse();
    }

    @Test
    public void shouldMoveTaskDown() throws IOException {
        // preparing test data
        Task task1 = mock(Task.class);
        doReturn("test-1").when(task1).getName();
        doReturn(task1).when(task1).clone();
        Task task2 = mock(Task.class);
        doReturn("test-2").when(task2).getName();
        doReturn(task2).when(task2).clone();
        doReturn(new Element("task-1")).when(task1).getXML();
        doReturn(new Element("task-2")).when(task2).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        boolean modified = tasksPool.moveTaskDown(task1);

        // check the behavior
        verify(tasksPool).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task2, task1);
        assertThat(modified).isTrue();
    }

    @Test
    public void shouldNotMoveTaskDown_OnBottomAlready() throws IOException {
        // preparing test data
        Task task1 = mock(Task.class);
        doReturn("test-1").when(task1).getName();
        doReturn(task1).when(task1).clone();
        Task task2 = mock(Task.class);
        doReturn("test-2").when(task2).getName();
        doReturn(task2).when(task2).clone();
        doReturn(new Element("task-1")).when(task1).getXML();
        doReturn(new Element("task-2")).when(task2).getXML();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        reset(tasksPool);

        // acting
        boolean modified = tasksPool.moveTaskDown(task2);

        // check the behavior
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionError.class);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        assertThat(modified).isFalse();
    }

    @Test
    public void shouldLoadTaskList() throws IOException {
        // preparing test data
        String poolName = "public";
        tasksPool.localPoolFor(poolName, null);

        // acting
        tasksPool.loadTasksList();

        // check the behavior
        ArgumentCaptor<InputStream> loadCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(tasksPool).restoreDocumentFrom(loadCaptor.capture());
        verify(tasksPool).prepareXmlDocument(loadCaptor.getValue());
        verify(tasksPool, times(3)).addTask(any(Task.class), eq(false));
        verify(poolsManager).add(tasksPool);
        // check results
        assertThat(tasksPool.tasks()).hasSize(3);
    }

    @Test
    public void shouldNotLoadTaskList_WrongPoolName() throws IOException {
        // preparing test data
        String poolName = "library";
        tasksPool.localPoolFor(poolName, null);

        // acting
        Exception e = assertThrows(Exception.class, () -> tasksPool.loadTasksList());

        // check the behavior
        verify(tasksPool, never()).restoreDocumentFrom(any(InputStream.class));
        // check results
        assertThat(e).isInstanceOf(FileNotFoundException.class);
        assertThat(tasksPool.tasks()).isEmpty();
    }

    @Test
    public void shouldLoadExistsTasksListFile() throws IOException {
        // preparing test data
        String poolName = "public";
        tasksPool.localPoolFor(poolName, null);

        // acting
        tasksPool.loadOrCreateTasksList();

        // check the behavior
        ArgumentCaptor<InputStream> loadCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(tasksPool).restoreDocumentFrom(loadCaptor.capture());
        verify(tasksPool).prepareXmlDocument(loadCaptor.getValue());
        verify(tasksPool, times(3)).addTask(any(Task.class), eq(false));
        // check results
        assertThat(tasksPool.tasks()).hasSize(3);

    }

    @Test
    public void shouldCreateNewTasksListFile() throws IOException {
        // preparing test data
        String poolName = "library";
        tasksPool.localPoolFor(poolName, null);

        // acting
        tasksPool.loadOrCreateTasksList();

        // check the behavior
        verify(tasksPool).store(any(Document.class), any(OutputStream.class));
        // check results
        assertThat(tasksPool.tasks()).isEmpty();
        String poolFile = poolName+".tasks.pool";
        final File tasksListFile = new File(poolsManager.getRoot(), poolFile);
        assertThat(tasksListFile).exists();
        tasksListFile.delete();

    }

    @Test
    public void shouldConfigureLoadingTasksList_ExistsFile() throws IOException, DataConversionException {
        // preparing test data
        String poolType = "public";
        String poolName = "public";
        String poolFileName = "public.tasks.pool";
        TasksPoolUnit tasksPoolUnit = tasksPool;
        Element poolXml = new Element("pool")
                .setAttribute("type", poolType).setAttribute("name", poolName).setAttribute("file", poolFileName);


        // acting
        tasksPoolUnit.configure(poolXml);

        // check the behavior
        verify(tasksPoolUnit).setXML(poolXml);
        verify(tasksPoolUnit).loadTasksList();
        ArgumentCaptor<InputStream> loadCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(tasksPoolUnit).restoreDocumentFrom(loadCaptor.capture());
        verify(tasksPoolUnit).prepareXmlDocument(loadCaptor.getValue());
        verify(tasksPoolUnit, times(3)).addTask(any(Task.class), eq(false));
        verify(poolsManager).add(tasksPoolUnit);
        // check results
        assertThat(tasksPoolUnit.tasks()).hasSize(3);
    }

    @Test
    public void shouldSaveTaskList_TemporalFile() throws IOException {
        // preparing test data
        tasksPool.configure(new Element("pool")
                .setAttribute("type", "public")
                .setAttribute("name", "public")
                .setAttribute("file", "public.tasks.pool")
        );
        String temporalFileName = "temporal.tasks.pool";
        tasksPool.applyTasksFile(temporalFileName);

        // acting
        tasksPool.saveTasksList();

        // check results
        File tasksListFile = new File(poolsManager.getRoot(), temporalFileName);
        assertThat(tasksListFile).exists();
        tasksListFile.deleteOnExit();
    }

    @Test
    public void shouldExecuteStartPool() throws Exception {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test-1").when(task).getName();
        doReturn(new Element("task")).when(task).getXML();
        doReturn(task).when(task).clone();
        tasksPool.addTask(task, false);
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.START, "Starting the pool");
        assertThat(tasksPool.isStarted()).isFalse();
        reset(tasksPool);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).canStartUnit();
        verify(tasksPool).startUnitRunnable();
        // check results
        assertThat(tasksPool.isStarted()).isTrue();
        assertThat(tasksPool.current()).isSameAs(task);
    }

    @Test
    public void shouldNotExecuteStartPool_EmptyTasksList() throws Exception {
        // preparing test data
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.START, "Starting the pool");
        assertThat(tasksPool.isStarted()).isFalse();
        reset(tasksPool);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).canStartUnit();
        verify(tasksPool, never()).startUnitRunnable();
        // check results
        assertThat(tasksPool.isStarted()).isFalse();
    }

    @Test
    public void shouldExecuteStopPool() throws Exception {
        // preparing test data
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.STOP, "Stopping the pool");
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(tasksPool);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).stopUnitRunnable();
        // check results
        assertThat(tasksPool.isStopped()).isTrue();
        assertThat(tasksPool.current()).isNull();
    }

    @Test
    public void shouldExecuteGetPoolInfo() throws Exception {
        // preparing test data
        String targetValue = "info";
        String currentTaskName = "currentTask";
        String tasksListNames = "task 1\ntask 2";
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.GET, "Getting the pool info");
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true).setParameter(Parameter.of("target", targetValue).input());
        Task currentTask = mock(Task.class);
        doReturn(currentTaskName).when(currentTask).getName();
        doReturn(currentTask).when(tasksPool).current();
        doReturn(tasksListNames).when(tasksPool).tasksList();

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolGet(request);
        verify(tasksPool).successfulResponseTo(eq(request), any(Consumer.class));
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        verify(tasksPool).currentUnitState();
        verify(tasksPool).tasksList();
        verify(tasksPool).current();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        List<UnitMessage> unitMessages = captor.getAllValues();
        // check results
        assertThat(unitMessages).hasSize(1);
        assertThat(unitMessages.get(0)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) unitMessages.get(0);
        assertThat(response.getParameters().toArray()).hasSize(3);
        assertThat(response.getParameter("current", "output").get().getValue("wrong")).isEqualTo(currentTaskName);
        assertThat(response.getParameter("tasks.list", "output").get().getValue("wrong")).isEqualTo(tasksListNames);
        assertThat(response.getParameter("unit.state", "output").get().getValue("wrong")).isEqualTo("active");
    }

    @Test
    public void shouldExecuteGetPoolTaskInfo() throws Exception {
        // preparing test data
        String targetValue = "edit";
        String currentTaskName = "currentTask";
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.GET, "Getting the pool info");
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true)
                .setParameter(Parameter.of("target", targetValue).input())
                .setParameter(Parameter.of("task", currentTaskName).input());
        Task currentTask = mock(Task.class);
        Element taskXml = new Element("task");
        doReturn(currentTaskName).when(currentTask).getName();
        doReturn(taskXml).when(currentTask).getXML();
        doReturn(Optional.of(currentTask)).when(tasksPool).getTask(currentTaskName);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolGet(request);
        verify(tasksPool).getTask(currentTaskName);
        verify(tasksPool).successfulResponseTo(eq(request), any(Consumer.class));
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        verify(currentTask).getXML();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(captor.capture());
        List<UnitMessage> unitMessages = captor.getAllValues();
        // check results
        assertThat(unitMessages).hasSize(1);
        assertThat(unitMessages.get(0)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) unitMessages.get(0);
        assertThat(response.getParameters().toArray()).hasSize(2);
        assertThat(response.getParameter("edit.class", "output").get().getValue("wrong")).isEqualTo("nothing :-(");
        assertThat(response.getParameter("task", "output").get().getValue(Tools.emptyXML)).isEqualTo(taskXml);
    }

    @Test
    public void shouldNotExecuteGet_WrongTarget() throws Exception {
        // preparing test data
        String targetValue = "wrong";
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.GET, "Getting the pool info");
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true).setParameter(Parameter.of("target", targetValue).input());

        // acting
        Exception e = assertThrows(Exception.class, () -> tasksPool.execute(request));

        // check the behavior
        verify(tasksPool).executePoolGet(request);
        verify(tasksPool, never()).successfulResponseTo(any(ServerCommandRequest.class), any(Consumer.class));
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("Invalid GET's command target [" + targetValue + "]");
    }

    @Test
    public void shouldNotExecuteGetPoolTaskInfo_NoTaskParameter() throws Exception {
        // preparing test data
        String targetValue = "edit";
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.GET, "Getting the pool info");
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true).setParameter(Parameter.of("target", targetValue).input());

        // acting
        Exception e = assertThrows(Exception.class, () -> tasksPool.execute(request));

        // check the behavior
        verify(tasksPool).executePoolGet(request);
        verify(tasksPool, never()).getTask(anyString());
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("GET isn't supported! Nothing to edit.");
    }

    @Test
    public void shouldNotExecuteGetPoolTaskInfo_NoTaskByName() throws Exception {
        // preparing test data
        String targetValue = "edit";
        String currentTaskName = "currentTask";
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.GET, "Getting the pool info");
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true)
                .setParameter(Parameter.of("target", targetValue).input())
                .setParameter(Parameter.of("task", currentTaskName).input());

        // acting
        Exception e = assertThrows(Exception.class, () -> tasksPool.execute(request));

        // check the behavior
        verify(tasksPool).executePoolGet(request);
        verify(tasksPool).getTask(currentTaskName);
        verify(tasksPool, never()).successfulResponseTo(any(ServerCommandRequest.class), any(Consumer.class));
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("Invalid get task by name [" + currentTaskName + "]");
    }

    @Test
    public void shouldNotExecuteSet_WrongSetModifyType() throws Exception {
        // preparing test data
        String modifyType = "fail";
        String requestDescription = modifyType + "ing the task to the pool";
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true).setParameter(Parameter.of("type", modifyType).input());

        // acting
        Exception e = assertThrows(Exception.class, () -> tasksPool.execute(request));

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("Invalid SET's command type [" + modifyType + "]");
    }

    @Test
    public void shouldExecuteSetDeploy() throws Exception {
        // preparing test data
        String modifyType = "Deploy";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task").setAttribute("class", TestTask.class.getName());
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskXml).input())
        ;
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(new TestTask(), false);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).updateTask(any(Task.class));
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter(modifyType, "output").get().getValue(false)).isTrue();
    }

    @Test
    public void shouldNotExecuteSetInstall_WrongTaskParameterType() throws Exception {
        // preparing test data
        String modifyType = "Install";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", Boolean.TRUE).input())
        ;

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool, never()).addTask(any(Task.class));
        verify(tasksPool).respondTo(eq(request), eq(false), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(0);
        assertThat(response.isCommandSuccess()).isFalse();
        assertThat(response.getParameter("reason", "output").get().getValue("Good"))
                .isEqualTo("invalid type of the install task input parameter");
    }

    @Test
    public void shouldExecuteSetInstallRawTaskAdd() throws Exception {
        // preparing test data
        String modifyType = "Install";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task").setAttribute("class", TestTask.class.getName());
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskXml).input())
        ;
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        assertThat(tasksPool.tasks()).isEmpty();

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).addTask(any(Task.class));
        verify(tasksPool, never()).updateTask(any(Task.class));
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter(modifyType, "output").get().getValue(false)).isTrue();
    }

    @Test
    public void shouldExecuteSetInstallRawTaskUpdate() throws Exception {
        // preparing test data
        String modifyType = "Install";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        tasksPool.addTask(new TestTask(), false);
        reset(tasksPool);
        Element taskXml = new Element("task").setAttribute("class", TestTask.class.getName());
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskXml).input())
        ;
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool, never()).addTask(any(Task.class));
        verify(tasksPool).updateTask(any(Task.class));
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter(modifyType, "output").get().getValue(false)).isTrue();
    }

    @Test
    public void shouldExecuteSetInstallPublicTaskAdd() throws Exception {
        // preparing test data
        String modifyType = "Install";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
        ;
        TasksPoolUnit publicPool = spy(new TasksPoolUnitAdapter(){});
        publicPool.addTask(task, false);
        TaskPoolsManager manager = mock(TaskPoolsManager.class);
        doReturn(publicPool).when(manager).publicTaskPool();
        doReturn(manager).when(tasksPool).getOwner();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).addTask(task);
        verify(tasksPool, never()).updateTask(any(Task.class));
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter(modifyType, "output").get().getValue(false)).isTrue();
    }

    @Test
    public void shouldExecuteSetInstallPublicTaskUpdate() throws Exception {
        // preparing test data
        String modifyType = "Install";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        tasksPool.addTask(task, false);
        reset(tasksPool);
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
        ;
        TasksPoolUnit publicPool = spy(new TasksPoolUnitAdapter(){});
        publicPool.addTask(task, false);
        TaskPoolsManager manager = mock(TaskPoolsManager.class);
        doReturn(publicPool).when(manager).publicTaskPool();
        doReturn(manager).when(tasksPool).getOwner();
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(publicPool).getTask(taskName);
        verify(tasksPool, never()).addTask(any(Task.class));
        verify(tasksPool).updateTask(task);
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter(modifyType, "output").get().getValue(false)).isTrue();
    }

    @Test
    public void shouldNotExecuteSetDeleteTask_NoTasksInPool() throws Exception {
        // preparing test data
        String modifyType = "Delete";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        String taskName = "task name";
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
        ;

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).getTask(taskName);
        verify(tasksPool, never()).removeTask(any(Task.class));
        verify(tasksPool).respondTo(eq(request), eq(false), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(0);
        assertThat(response.getParameter("reason", "output").get().getValue("Good"))
                .isEqualTo("invalid task to delete in the pool");
    }

    @Test
    public void shouldExecuteSetDeleteTask_TaskInPool() throws Exception {
        // preparing test data
        String modifyType = "Delete";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
        ;
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        tasksPool.addTask(task, false);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).getTask(taskName);
        verify(tasksPool).removeTask(task);
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter(modifyType, "output").get().getValue(false)).isTrue();
    }

    @Test
    public void shouldNotExecuteSetMoveTask_WrongDirection() throws Exception {
        // preparing test data
        String modifyType = "Move";
        String moveDirection = "wrong";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        String taskName = "task name";
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("direction", moveDirection).input())
                .setParameter(Parameter.of("task", taskName).input())
        ;

        // acting
        Exception e = assertThrows(Exception.class, () -> tasksPool.execute(request));

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("invalid move's direction " + moveDirection);
    }

    @Test
    public void shouldExecuteSetMoveTaskUp() throws Exception {
        // preparing test data
        String modifyType = "Move";
        String moveDirection = "up";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        for (int i = 0; i < 5; i++) {
            addTestTask(i);
        }
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
                .setParameter(Parameter.of("direction", moveDirection).input())
        ;
        tasksPool.addTask(task, false);
        int position = new ArrayList<>(tasksPool.tasks()).indexOf(task);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).moveTaskUp(task);
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter("move." + moveDirection, "output").get().getValue(false)).isTrue();
        assertThat(new ArrayList<>(tasksPool.tasks()).indexOf(task)).isLessThan(position);
    }

    @Test
    public void shouldNotExecuteSetMoveTaskUp_NowhereToMove() throws Exception {
        // preparing test data
        String modifyType = "Move";
        String moveDirection = "up";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
                .setParameter(Parameter.of("direction", moveDirection).input())
        ;
        tasksPool.addTask(task, false);
        int position = new ArrayList<>(tasksPool.tasks()).indexOf(task);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).moveTaskUp(task);
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionError.class);
        UnitActionError error = (UnitActionError) messages.get(0);
        assertThat(error.getDescription()).isEqualTo("Up:The task to move is on the top.");
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter("move." + moveDirection, "output").get().getValue(true)).isFalse();
        assertThat(new ArrayList<>(tasksPool.tasks()).indexOf(task)).isSameAs(position);
    }

    @Test
    public void shouldExecuteSetMoveTaskDown() throws Exception {
        // preparing test data
        String modifyType = "Move";
        String moveDirection = "down";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        doAnswer(invocation -> null).when(tasksPool).saveTasksList();
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        tasksPool.addTask(task, false);
        for (int i = 0; i < 5; i++) {
            addTestTask(i);
        }
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
                .setParameter(Parameter.of("direction", moveDirection).input())
        ;
        int position = new ArrayList<>(tasksPool.tasks()).indexOf(task);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).moveTaskDown(task);
        verify(tasksPool).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter("move." + moveDirection, "output").get().getValue(false)).isTrue();
        assertThat(new ArrayList<>(tasksPool.tasks()).indexOf(task)).isGreaterThan(position);
    }

    @Test
    public void shouldNotExecuteSetMoveTaskDown_NowhereToMove() throws Exception {
        // preparing test data
        String modifyType = "Move";
        String moveDirection = "down";
        String requestDescription = modifyType + "ing the task to the pool";
        modifyType = modifyType.toLowerCase();
        ServerCommandRequest request = tasksPool.getMessageFactory()
                .buildFor(tasksPool, MessageType.COMMAND, MessageFamilyType.SET, requestDescription);
        tasksPool.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        Element taskXml = new Element("task");
        String taskName = "task name";
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        tasksPool.addTask(task, false);
        request.setNeedResponse(true)
                .setParameter(Parameter.of("type", modifyType).input())
                .setParameter(Parameter.of("task", taskName).input())
                .setParameter(Parameter.of("direction", moveDirection).input())
        ;
        int position = new ArrayList<>(tasksPool.tasks()).indexOf(task);

        // acting
        tasksPool.execute(request);

        // check the behavior
        verify(tasksPool).executePoolSet(request);
        verify(tasksPool).moveTaskDown(task);
        verify(tasksPool, never()).saveTasksList();
        verify(tasksPool).respondTo(eq(request), eq(true), any(Consumer.class));
        ArgumentCaptor<UnitMessage> messageArgumentCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(tasksPool, atLeastOnce()).dispatch(messageArgumentCaptor.capture());
        // check results
        List<UnitMessage> messages = messageArgumentCaptor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UnitActionError.class);
        UnitActionError error = (UnitActionError) messages.get(0);
        assertThat(error.getDescription()).isEqualTo("Down:The task to move is on the bottom.");
        assertThat(messages.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messages.get(1);
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getParameter("move." + moveDirection, "output").get().getValue(true)).isFalse();
        assertThat(new ArrayList<>(tasksPool.tasks()).indexOf(task)).isSameAs(position);
    }

    // private methods
    private TaskPoolsManager createTaskPoolsManager() throws IOException {
        String managerPath = "Tasks/Manager";
        String managerDirectory = "work/tasks";
        TaskPoolsManager mockedPoolsManager = mock(TaskPoolsManager.class);
        doReturn(managerPath).when(mockedPoolsManager).getPath();
        doReturn(new File(managerDirectory)).when(mockedPoolsManager).getRoot();
        UnitRegistry.register(mockedPoolsManager);
        return mockedPoolsManager;
    }

    private void addTestTask(int i) {
        Element taskXml = new Element("task");
        String taskName = "task name " + (i + 1);
        Task task = mock(Task.class);
        doReturn(taskName).when(task).getName();
        doReturn(taskXml).when(task).getXML();
        tasksPool.addTask(task, false);
    }

    // inner classes
    static class TestTask extends TaskAdapter {

        @Override
        public void execute() {
            // it's testing task
        }

        @Override
        public void stopExecute() {
            // it's testing task
        }

        @Override
        protected void clockEvent() {
            // it's testing task
        }
    }
}
