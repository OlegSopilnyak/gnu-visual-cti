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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.unit.RunnableUnitAdapter;

/**
 * Implementation Adapter: server tasks pool unit engine
 *
 * @see Task
 * @see TasksPoolUnit
 */
public class TasksPoolUnitAdapter extends RunnableUnitAdapter implements TasksPoolUnit {
    // The list of tasks in the pool
    private final List<Task> tasksPool = Collections.synchronizedList(new LinkedList<>());
    // working ring of the tasks (used in the in-service engine state)
    private final LinkedList<Task> inServiceTasksRing = new LinkedList<>();
    // locker for started tasks ring
    private final Lock tasksRingLock = new ReentrantLock();
    // Current task from the tasks ring
    private transient volatile Task currentTask = null;
    // type of tasks pool
    private PoolType poolType;
    // The name of the unit
    private String poolName = "";
    // the group of tasks pool
    private String poolGroup;
    // the name of file of the pool's XML
    private String poolFile;
    // the file of the pool's XML
    private File poolLocation;
    // the state of engine (tasks pool)
    private Engine.State unitEngineState = Engine.State.OUT_OF_SERVICE;
    //
    // to do action in safe, for tasks ring, way
    private final Consumer<Runnable> safeTasksRingAction = action -> {
        tasksRingLock.lock();
        try {
            action.run();
        } finally {
            tasksRingLock.unlock();
        }
    };

    /**
     * <accessor>
     * To get the type of task-pool
     *
     * @return the value
     * @see PoolType
     */
    @Override
    public PoolType getPoolType() {
        return poolType;
    }

    /**
     * <mutator>
     * To set up the type of task-pool
     *
     * @param poolType new value of task-pool type
     * @return reference to the pool
     * @see PoolType
     */
    @Override
    public TasksPoolUnit setPoolType(PoolType poolType) {
        this.poolType = poolType;
        return this;
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        return isEmpty(poolGroup) ? poolName : poolGroup + "/" + poolName;
    }

    /**
     * <mutator>
     * To set up the name of the pool
     *
     * @param poolName new value of pool's group
     * @return reference to tasks pool
     */
    @Override
    public TasksPoolUnit setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    public String getType() {
        return getPoolType().getType() + "/tasks/pool";
    }

    /**
     * <accessor>
     * To get the group name of the pool
     *
     * @return group name of the pool
     */
    @Override
    public String getPoolGroup() {
        return poolGroup;
    }

    /**
     * <mutator>
     * To set up the group name of the pool
     *
     * @param group new value of pool's group
     * @return reference to tasks pool
     */
    @Override
    public TasksPoolUnit setPoolGroup(String group) {
        this.poolGroup = group;
        return this;
    }

    /**
     * <mutator>
     * To set up the tasks list file name of the pool
     *
     * @param poolFile new value of pool's file name
     * @return reference to tasks pool
     */
    @Override
    public TasksPoolUnit setPoolFile(String poolFile) {
        this.poolFile = poolFile;
        this.poolLocation = new File(poolFile);
        return this;
    }

    /**
     * <accessor>
     * To get current state value
     *
     * @return the current state ID of the engine
     */
    @Override
    public short getState() {
        return unitEngineState.getCode();
    }

    /**
     * <mutator>
     * To set up the new state value
     *
     * @param state new state ID of the engine
     */
    @Override
    public void setState(short state) {
        this.unitEngineState = State.of(state);
    }

    /**
     * <action>
     * to Start the engine
     *
     * @throws IOException if engine can't start
     * @see #isStarted()
     */
    @Override
    public void Start() throws IOException {
//        if (unitState != UnitState.BROKEN && !isStarted() && !tasksPool.isEmpty()) {
//            safeTasksRingAction.accept(() -> {
//                // to copying tasks from pool to the tasks ring
//                inServiceTasksRing.addAll(tasksPool);
//                // updating the unit state
//                unitState = UnitState.ACTIVE;
//                // update the engine state
//                unitEngineState = Engine.State.IN_SERVICE;
//            });
//            // preparing engine started successfully event
//            final UnitActionEvent event = getMessageFactory().build(MessageType.EVENT, UnitActionEvent.class);
//            // dispatching success engine started event
//            dispatch(event.setFamilyType(MessageFamilyType.START).setUnitPath(getPath()));
//        }
    }

    /**
     * <action>
     * to Stop the engine
     *
     * @throws IOException if engine can't stop
     */
    @Override
    public void Stop() throws IOException {
//        if (unitState != UnitState.BROKEN && !isStopped()) {
//            try {
//                // to stop execution of the current task
//                current().stopExecute();
//            } catch (NullPointerException e) {
//                // do nothing here, just ignore if current task is null
//            }
//            safeTasksRingAction.accept(() -> {
//                // clear the tasks ring
//                inServiceTasksRing.clear();
//                currentTask = null;
//                // updating the unit state
//                unitState = UnitState.PASSIVE;
//                // update the state
//                unitEngineState = Engine.State.OUT_OF_SERVICE;
//            });
//            // preparing engine stopped successfully event
//            final UnitMessage event = getMessageFactory().build(MessageType.EVENT, UnitActionEvent.class);
//            // dispatching engine stopped successfully event
//            dispatch(event.setFamilyType(MessageFamilyType.STOP).setUnitPath(getPath()));
//        }
    }

    /**
     * <accessor>
     * To check is Engine is working (in service)
     *
     * @return true if Engine is in service
     * @see State#IN_SERVICE
     */
    @Override
    public boolean isStarted() {
        return unitEngineState == Engine.State.IN_SERVICE;
    }

    /**
     * <accessor>
     * To check is Engine is stopped (out of service)
     *
     * @return true if Engine is out of service
     * @see State#OUT_OF_SERVICE
     */
    @Override
    public boolean isStopped() {
        return unitEngineState == Engine.State.OUT_OF_SERVICE;
    }

    /**
     * <accessor>
     * To get the current(executed) task. (returned last next() call)
     * This method works only when engine isStarted()
     *
     * @return current server task
     * @see Task
     */
    @Override
    public Task current() {
        return isStarted() ? currentTask : null;
    }

    /**
     * <action>
     * To get next active task.
     * This method works only when engine isStarted()
     * otherwise will return null
     *
     * @return next server task from started tasks pool
     * @see Task
     * @see LinkedList#removeFirst()
     * @see LinkedList#addLast(Object)
     * @see #current()
     */
    @Override
    public Task next() {
        if (!this.isStarted() || inServiceTasksRing.isEmpty()) {
            // engine isn't started or tasks ring is empty
            return null;
        } else {
            // rotate tasks ring and setup current one
            safeTasksRingAction.accept(() -> {
                // get first Task in the linked list
                final Task taskFromTheTop = inServiceTasksRing.removeFirst();
                // set current pool's task
                currentTask = taskFromTheTop.clone();
                // place to tail of list
                inServiceTasksRing.addLast(taskFromTheTop);
            });
            return currentTask;
        }
    }

    /**
     * <accessor>
     * To get the installed pool's tasks list
     *
     * @return list of installed tasks
     * @see Task
     */
    @Override
    public Collection<Task> tasks() {
        return new ArrayList<>(tasksPool);
    }

    /**
     * <mutator>
     * To add the task to a pool.
     *
     * @param task   instance to add
     * @param notify flag is need notification after
     * @return true if added successfully
     * @see Task
     */
    @Override
    public boolean add(Task task, boolean notify) {
        return false;
    }

    /**
     * <mutator>
     * To update the task in a pool.
     *
     * @param task instance to update
     * @return true if updated successfully
     * @see Task
     */
    @Override
    public boolean update(Task task) {
        return false;
    }

    /**
     * <mutator>
     * To remove the task from a pool.
     *
     * @param task instance to remove
     * @return true if removed successfully
     * @see Task
     */
    @Override
    public boolean remove(Task task) {
        return false;
    }

    /**
     * <order>
     * To move task up in the tasks list.
     *
     * @param task to move up
     * @return true if moved successfully
     * @see Task
     */
    @Override
    public boolean up(Task task) {
        return false;
    }

    /**
     * <order>
     * To move task down in the tasks list.
     *
     * @param task to move down
     * @return true if moved successfully
     * @see Task
     */
    @Override
    public boolean down(Task task) {
        return false;
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     */
    @Override
    public Element getXML() {
        return null;
    }

    /**
     * <converter>
     * To load tasks list from external XML file
     *
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see #setPoolFile(String)
     * @see #setXML(Element)
     * @see Element
     * @see java.io.InputStream
     */
    @Override
    public void loadingTasksList() throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // preparing tasks file InputStream
    }

    /**
     * <executer>
     * To execute console command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     */
    @Override
    public void execute(ServerCommandRequest command) throws Exception {

    }

    // private methods
    //To get the installed pool's tasks list as a string
    private String tasksList() {
        final StringBuilder builder = new StringBuilder();
        tasks().forEach(task -> builder.append(task.getName()).append("\n"));
        return builder.toString();
    }
}
