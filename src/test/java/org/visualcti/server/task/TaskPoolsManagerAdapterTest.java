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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;

public class TaskPoolsManagerAdapterTest {
    TaskPoolsManagerAdapter manager;

    @Before
    public void setUp() {
        manager = spy(new TheManager());
    }


    @Test
    public void shouldAddCreatedTasksPool() throws IOException {
        // preparing test data
        String poolName = "pool1";
        String poolGroup = "poolGroup1";
        TasksPoolUnit poolUnit = spy(manager.createTaskPool(poolName, poolGroup));

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
        TasksPoolUnit poolUnit = spy(manager.createTaskPool(poolName, poolGroup));
        manager.add(poolUnit);
        assertThat(manager.children().anyMatch(child -> child == poolUnit)).isTrue();
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
        TasksPoolUnit poolUnit = spy(manager.createTaskPool(poolName, poolGroup));
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
        reset(poolUnit, manager);

        // acting
        TasksPoolUnit detached = manager.detachTaskPool(poolName, poolGroup);

        // check the behavior
        verify(manager).taskPoolStreamBy(any(Predicate.class));
        verify(poolUnit).close();
        verify(poolUnit, times(2)).getPoolName();
        verify(poolUnit, times(3)).getPoolGroup();
        verify(manager).isChild(poolUnit);
        verify(poolUnit).Stop();
        verify(manager).remove(poolUnit);
        verify(poolUnit).setOwner(null);
        verify(manager).removeBranch(poolUnit);
        verify(poolUnit).close();
        // check results
        assertThat(detached).isSameAs(poolUnit);
        assertThat(manager.children().count()).isZero();
    }

    @Test
    public void shouldNotDetachTaskPool_NoPools() throws IOException {
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
        TasksPoolUnit poolUnit = spy(manager.createTaskPool("poolName", "poolGroup"));
        poolUnit.applyFor(poolName, poolGroup, TasksPoolUnit.PoolType.PUBLIC);
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
    public void shouldNotGetPublicTaskPool_NoPublicPools() throws IOException {
        // preparing test data
        String poolName = "pool6";
        String poolGroup = "poolGroup6";
        TasksPoolUnit poolUnit = spy(manager.createTaskPool(poolName, poolGroup));
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
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
        TasksPoolUnit poolUnit = spy(manager.createTaskPool(poolName, poolGroup));
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
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
    public void shouldNotGetTaskPool_CreateOne() {
        // preparing test data
        String poolName = "pool8";
        String anotherPoolName = "poolName";
        String poolGroup = "poolGroup8";
        TasksPoolUnit poolUnit = spy(manager.createTaskPool(poolName, poolGroup));
        manager.add(poolUnit);
        assertThat(manager.children().count()).isNotZero();
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
    public void createTaskPool() {
    }

    @Test
    public void prepareUnitXML() {
    }

    @Test
    public void setXML() {
    }

    @Test
    public void applyUnitParameter() {
    }

    @Test
    public void start() {
    }

    @Test
    public void stop() {
    }

    @Test
    public void execute() {
    }

    private static class TheManager extends TaskPoolsManagerAdapter {
        public TheManager() {
            this.unitPath = "Manager";
        }

        @Override
        protected TasksPoolUnit createTaskPool(String name, String factory) {
            return new TasksPoolUnitAdapter() {
            }.localPoolFor(name, factory);
        }
    }
}