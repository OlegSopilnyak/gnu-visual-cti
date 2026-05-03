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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;

public class TasksPoolUnitAdapterTest {
    TasksPoolUnitAdapter tasksPool = spy(new TasksPoolUnitAdapter());

    @After
    public void tearDown() throws Exception {
        tasksPool.Stop();
    }

    @Test
    public void shouldGetName_WithGroup() {
        // preparing test data
        tasksPool.setPoolGroup("group");
        tasksPool.setPoolName("name");

        // acting
        String name = tasksPool.getName();

        // check the behavior
        verify(tasksPool, times(2)).getPoolGroup();
        verify(tasksPool).getPoolName();
        // check results
        assertThat("group/name").isEqualTo(name);
    }

    @Test
    public void shouldGetName_WithoutGroup() {
        // preparing test data
        tasksPool.setPoolName("name");

        // acting
        String name = tasksPool.getName();

        // check the behavior
        verify(tasksPool).getPoolGroup();
        verify(tasksPool).getPoolName();
        // check results
        assertThat("name").isEqualTo(name);
    }

    @Test
    public void shouldGetCurrent() throws IOException {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(current).when(current).clone();
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
    public void shouldNotGetCurrent_NotStarted() {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(current).when(current).clone();
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
    public void shouldNotGetNext_NotStarted() {
        // preparing test data
        Task current = mock(Task.class);
        doReturn("test").when(current).getName();
        doReturn(current).when(current).clone();
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
        tasksPool.addTask(task, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task);

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
        boolean modifiedNull = tasksPool.removeTask((Task) null);

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
    public void shouldUpdateTask_TaskAlreadyThere() {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        tasksPool.addTask(task, true);
        reset(tasksPool);
        assertThat(tasksPool.tasks()).containsExactly(task);

        // acting
        boolean modified = tasksPool.updateTask(task);

        // check the behavior
        verify(tasksPool).removeTask(task);
        verify(tasksPool).addTask(task, true);
        // check results
        assertThat(tasksPool.tasks()).containsExactly(task);
        assertThat(modified).isTrue();
    }

    @Test
    public void shouldNotUpdateTask_TaskNotThere() {
        // preparing test data
        Task task = mock(Task.class);
        doReturn("test").when(task).getName();
        doReturn(task).when(task).clone();
        assertThat(tasksPool.tasks()).isEmpty();

        // acting
        boolean modified = tasksPool.updateTask(task);

        // check the behavior
        verify(tasksPool).removeTask(task);
        verify(tasksPool, never()).addTask(any(Task.class), anyBoolean());
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
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        reset(tasksPool);

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
        doReturn(task1).when(task1).clone();
        Task task2 = mock(Task.class);
        doReturn("test-2").when(task2).getName();
        doReturn(task2).when(task2).clone();
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        reset(tasksPool);

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
        tasksPool.addTask(task1, true);
        tasksPool.addTask(task2, true);
        assertThat(tasksPool.tasks()).containsExactly(task1, task2);
        reset(tasksPool);

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
    public void shouldLoadTasksList_ExistsFile() throws IOException, DataConversionException {
        // preparing test data
        String managerPath = "Tasks/Manager";
        String managerDirectory = "work/tasks";
        TaskPoolsManager poolsManager = mock(TaskPoolsManager.class);
//        doCallRealMethod().when(poolsManager).add(any(ServerUnit.class));
        doReturn(managerPath).when(poolsManager).getPath();
        doReturn(new File(managerDirectory)).when(poolsManager).getRoot();
        UnitRegistry.register(poolsManager);


        // acting
        Element pool = new Element("pool")
                .setAttribute("type", "public")
                .setAttribute("name", "public")
                .setAttribute("file", "public.tasks.pool");
        TasksPoolUnit tasksPoolUnit = spy(new TasksPoolUnitAdapter());
        tasksPoolUnit.configure(pool);

        // check the behavior
        verify(tasksPoolUnit).setXML(pool);
        verify(tasksPoolUnit).loadTasksList();
        ArgumentCaptor<InputStream> loadCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(tasksPoolUnit).load(loadCaptor.capture());
        verify(tasksPoolUnit).prepareXmlDocument(loadCaptor.getValue());
        verify(tasksPoolUnit, times(3)).addTask(any(Task.class), Matchers.eq(false));
        verify(poolsManager).add(tasksPoolUnit);
        // check results
        assertThat(tasksPoolUnit.tasks()).hasSize(3);
    }
}
