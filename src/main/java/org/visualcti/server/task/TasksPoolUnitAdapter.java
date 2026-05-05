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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * Implementation Adapter: server tasks pool unit engine
 *
 * @see Task
 * @see TasksPoolUnit
 */
public class TasksPoolUnitAdapter extends RunnableUnitAdapter implements TasksPoolUnit {
    // The list of tasks in the pool
    private volatile List<Task> tasksPool = Collections.emptyList();
    // XML-Document of the list of tasks in the pool
    private final Document tasksListConfigurationDocument = new Document().setContent(Arrays.asList(
            new Comment(Tools.getLicenceHeader()),
            new Element(TASKS_LIST_ROOT_ELEMENT_NAME)
                    .setAttribute(TASKS_POOL_TYPE_ATTRIBUTE_NAME, "unknown")
                    .addContent(new Comment(TASKS_LIST_ABOUT_TEMPLATE))
    ));
    // predicate checks is task valid
    private final Predicate<Task> validTask = task -> task != null && !isEmpty(task.getName());
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

    @Deprecated
    @Override
    public boolean isPublic() {
        return TasksPoolUnit.super.isPublic();
    }

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
     * To set moveTaskUp the type of task-pool
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
     * To get the name of the pool
     *
     * @return the name of the pool
     */
    @Override
    public String getPoolName() {
        return poolName;
    }

    /**
     * <mutator>
     * To set moveTaskUp the name of the pool
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
     * To set moveTaskUp the group name of the pool
     *
     * @param group new value of pool's group
     * @return reference to tasks pool
     */
    @Override
    public TasksPoolUnit setPoolGroup(String group) {
        this.poolGroup = group;
        return this;
    }

    @Deprecated
    @Override
    public String getName() {
        return TasksPoolUnit.super.getName();
    }

    /**
     * <mutator>
     * To set moveTaskUp the tasks list file name of the pool
     *
     * @param poolFile new value of pool's file name
     * @return reference to tasks pool
     */
    @Override
    public TasksPoolUnit setPoolFile(String poolFile) {
        this.poolFile = poolFile;
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
     * To get the current(executed) task. (returned last next() call)
     * This method works only when engine isStarted()
     *
     * @return current server task
     * @see Task
     */
    @Override
    public Task current() {
        return safeForTasksRing(() -> isStarted() ? this.currentTask : null);
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
            return safeForTasksRing(() -> {
                // set current pool's task using task from the tasks ring top
                currentTask = topTask();
                // returns current task
                return currentTask;
            });
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
     * To add the task to the tasks list.
     *
     * @param task   instance to add
     * @param notify flag is need notification after (used when tasks are loading)
     * @return true if added successfully
     * @see Task
     * @see #loadTasksList()
     */
    @Override
    public boolean addTask(final Task task, final boolean notify) {
        if (validTask.negate().test(task)) {
            // incoming task is invalid
            dispatchTasksError("Add:Invalid task to add");
            return false;
        }
        // to check is task in the tasks list
        if (hasTaskInside(task)) {
            // incoming task already in pool
            dispatchTasksError("Add:Task already exists");
            // incoming task is in the pool already
            return false;
        }
        // adding task to the tasks list of the pool and update tasks list of the pool
        final List<Task> tasks = new LinkedList<>(tasksPool);
        tasks.add(task);
        return updateTasksList(tasks, !notify, "Add");
    }

    /**
     * <mutator>
     * To update the task in the tasks list.
     *
     * @param task instance to update
     * @return true if updated successfully
     * @see Task
     * @see #removeTask(Task)
     * @see #addTask(Task, boolean)
     */
    @Override
    public boolean updateTask(final Task task) {
        if (validTask.negate().test(task)) {
            // incoming task is invalid
            dispatchTasksError("Update:Invalid task to update");
            return false;
        } else {
            final int taskIndex = taskIndexOf(task);
            if (taskIndex == -1) {
                // incoming task is not in the tasks list
                dispatchTasksError("Update:Not found task to update.");
                return false;
            } else {
                // substitute old task instance by the new one in the tasks list
                final List<Task> tasks = new LinkedList<>(tasksPool);
                tasks.set(taskIndex, task);
                // updating tasks list and store it
                return updateTasksList(tasks, false, "Update");
            }
        }
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
    public boolean removeTask(Task task) {
        if (validTask.negate().test(task)) {
            // incoming task is invalid
            dispatchTasksError("Remove:Invalid task to remove");
            return false;
        }
        // to check is task isn't inside the tasks list
        if (!hasTaskInside(task)) {
            // incoming task already in pool
            dispatchTasksError("Remove:Task isn't exists");
            // incoming task is in the pool already
            return false;
        }
        // removing task by task name and returning updated tasks list
        return updateTasksList(tasksPool.stream()
                        .filter(t -> !Objects.equals(task.getName(), t.getName()))
                        .collect(Collectors.toList()),
                false,
                "Remove");
    }

    /**
     * <mutator>
     * <order>
     * To move task up in the tasks list.
     *
     * @param task to move up
     * @return true if moved successfully
     * @see Task
     */
    @Override
    public boolean moveTaskUp(Task task) {
        if (validTask.negate().test(task)) {
            // incoming task is invalid
            dispatchTasksError("Up:Invalid task to move.");
            return false;
        }
        final int taskIndex = taskIndexOf(task);
        if (taskIndex == -1) {
            // incoming task is not in the tasks list
            dispatchTasksError("Up:Not found task to move.");
            return false;
        } else if (taskIndex == 0) {
            // incoming task already occupied the top of tasks list
            dispatchTasksError("Up:The task to move is on the top.");
            return false;
        }
        // moving task moveTaskUp in tasks list and updateTask the tasks pool
        return updateTasksList(exchangeTasks(taskIndex, -1), false, "Up");
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
    public boolean moveTaskDown(final Task task) {
        if (validTask.negate().test(task)) {
            // incoming task is invalid
            dispatchTasksError("Down:Invalid task to move");
            return false;
        }
        final int taskIndex = taskIndexOf(task);
        if (taskIndex == -1) {
            // incoming task is not in the tasks list
            dispatchTasksError("Down:Not found task to move.");
            return false;
        } else if (taskIndex >= tasksPool.size() - 1) {
            // incoming task already occupied the top of tasks list
            dispatchTasksError("Down:The task to move is on the bottom.");
            return false;
        }
        // moving task down in tasks list and updateTask the tasks pool
        return updateTasksList(exchangeTasks(taskIndex, 1), false, "Down");
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
        return unitConfiguration;
    }

    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        TasksPoolUnit.super.setXML(xml);
        this.unitPath = this.getName();
    }

    /**
     * <tasks-keeper>
     * To load tasks list from external XML file
     *
     * @throws IOException           if something went wrong
     * @throws NumberFormatException if something went wrong
     * @throws NullPointerException  if something went wrong
     * @see #setPoolFile(String)
     * @see #setXML(Element)
     * @see Element
     * @see java.io.InputStream
     */
    @SuppressWarnings("unchecked")
    @Override
    public void loadTasksList() throws IOException, NumberFormatException, NullPointerException {
        // preparing tasks list configuration xml-document
        final String tasksListPoolType = this.poolType.getType();
        final List<?> rootContent = this.tasksListConfigurationDocument.getContent();
        if (rootContent == null || rootContent.size() < 2) {
            throw new IOException("Tasks list configuration document is invalid.");
        }
        final Object licence = rootContent.get(0);
        if (!(licence instanceof Comment)) {
            throw new IOException("Tasks list licence is invalid.");
        }
        final Element tasksListElement = this.tasksListConfigurationDocument.getRootElement();
        tasksListElement.getContent().clear();
        tasksListElement.setAttribute(TASKS_POOL_TYPE_ATTRIBUTE_NAME, tasksListPoolType);
        final String tasksListAbout = String.format(TASKS_LIST_ABOUT_TEMPLATE, tasksListPoolType);
        tasksListElement.addContent(new Comment(tasksListAbout));
        try {
            // getting instance of task pools manager
            final TaskPoolsManager poolsManager = UnitRegistry.lookup(TaskPoolsManager.class);
            // preparing tasks-list external file
            final File tasksListFile = new File(poolsManager.getRoot(), this.poolFile);
            // loading tasks-list xml and restore the tasks
            try (final FileInputStream in = new FileInputStream(tasksListFile)) {
                final List<Element> xmlsList = restoreDocumentFrom(in).getRootElement().getChildren(Task.ROOT_ELEMENT);
                xmlsList.stream().map(TaskMaker::restore).filter(Objects::nonNull)
                        // adding restored tasks to the tasks pool
                        .forEach(task -> addTask(task, false))
                ;
                // adding tasks pool to the pools manager
                poolsManager.add(this);
            }
        } catch (ServerUnitException e) {
            throw new IOException("Wrong manager in registry", e);
        }
    }

    /**
     * <tasks-keeper>
     * To save tasks list to the external XML file
     *
     * @throws IOException if something went wrong
     * @see #updateTasksList(List, boolean, String)
     */
    @Override
    public void saveTasksList() throws IOException {
        try {
            // getting instance of task pools manager
            final TaskPoolsManager poolsManager = UnitRegistry.lookup(TaskPoolsManager.class);
            // preparing tasks-list external file
            final File tasksListFile = new File(poolsManager.getRoot(), this.poolFile);
            // saving tasks to the external file
            try (final FileOutputStream out = new FileOutputStream(tasksListFile)) {
                store(this.tasksListConfigurationDocument, out);
            }
        } catch (ServerUnitException e) {
            throw new IOException("Wrong manager in registry", e);
        }
    }

    @Deprecated
    @Override
    public Element load(InputStream in) throws IOException {
        return TasksPoolUnit.super.load(in);
    }

    @Deprecated
    @Override
    public Document restoreDocumentFrom(InputStream in) throws IOException {
        return TasksPoolUnit.super.restoreDocumentFrom(in);
    }

    @Deprecated
    @Override
    public Document prepareXmlDocument(InputStream in) throws IOException {
        return TasksPoolUnit.super.prepareXmlDocument(in);
    }

    @Deprecated
    @Override
    public void store(Document document, OutputStream out) throws IOException {
        TasksPoolUnit.super.store(document, out);
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     * Should be implemented in the children classes
     *
     * @see TasksPoolUnit#Start()
     */
    @Override
    public void startUnitRunnable() {
        safeForTasksRing(() -> {
                    // to copying tasks from pool to the tasks ring
                    inServiceTasksRing.addAll(tasksPool);
                    // get the task from the top of tasks ring
                    currentTask = topTask();
                    // doesn't matter
                    return null;
                }
        );
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Should be implemented in the children classes
     *
     * @see TasksPoolUnit#Stop()
     */
    @Override
    public void stopUnitRunnable() {
        safeForTasksRing(() -> {
                    // to clean the tasks ring
                    inServiceTasksRing.clear();
                    // set moveTaskUp current-task as null
                    currentTask = topTask();
                    // doesn't matter
                    return null;
                }
        );
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
        super.execute(command);
    }

    // private methods
    // to get the task from the top of tasks ring's list and move it to the bottom of the one
    private Task topTask() {
        if (inServiceTasksRing.isEmpty()) {
            // tasks ring is empty
            return null;
        }
        // get first Task in the linked list
        final Task taskFromTheTop = inServiceTasksRing.removeFirst();
        // place it to tail of the linked list
        inServiceTasksRing.addLast(taskFromTheTop);
        // return task from the top moved to the bottom
        return taskFromTheTop.clone();
    }

    // exchanging tasks in tasks list
    private List<Task> exchangeTasks(final int taskIndex, final int offset) {
        final Task[] tasks = tasksPool.toArray(new Task[0]);
        final Task targetTask = tasks[taskIndex];
        final Task toMoveTask = tasks[taskIndex + offset];
        tasks[taskIndex] = toMoveTask;
        tasks[taskIndex + offset] = targetTask;
        return Arrays.asList(tasks);
    }

    // updating tasks list of the pool and save changes
    private boolean updateTasksList(final List<Task> tasks, final boolean dontNotify, String action) {
        final Element tasksListXml = tasksListConfigurationDocument.getRootElement();
        tasksListXml.removeChildren(Task.ROOT_ELEMENT);
        // copying task-xmls to the tasks list xml-element
        tasks.forEach(task -> {
            final Element taskXml = task.getXML().detach();
            tasksListXml.addContent(taskXml);
        });
        this.tasksPool = tasks;
        if (!dontNotify) {
            try {
                // saving updated tasks list of the pool
                this.saveTasksList();
            } catch (IOException e) {
                // tasks list updating failed
                e.printStackTrace(Tools.err);
                return false;
            }
            // notify about tasks list modification event
            this.dispatchTasksModifiedEvent(action);
        }
        return true;
    }

    // to look for the index of task in the tasks list
    private int taskIndexOf(final Task task) {
        final Task[] tasks = tasksPool.toArray(new Task[0]);
        return IntStream.range(0, tasks.length)
                .filter(i -> Objects.equals(tasks[i].getName(), task.getName())).findFirst().orElse(-1);
    }

    // dispatching tasks list updated event
    private void dispatchTasksModifiedEvent(final String action) {
        try {
            final String eventMessage = action + ":tasks.list\n" + this.tasksList();
            final UnitActionEvent event = getMessageFactory()
                    .buildFor(this, MessageType.EVENT, MessageFamilyType.STATE, "");
            dispatch(event.setDescription(eventMessage));
        } catch (IOException e) {
            e.printStackTrace(Tools.err);
            // ignoring the exception
        }
    }

    // dispatching tasks list updating error
    private void dispatchTasksError(final String errorMessage) {
        try {
            final UnitActionError error =
                    getMessageFactory().buildFor(this, MessageType.ERROR, MessageFamilyType.ERROR, "");
            dispatch(error.setDescription(errorMessage));
        } catch (IOException e) {
            e.printStackTrace(Tools.err);
            // ignoring the exception
        }

    }

    // to do action in safe, for tasks ring, way
    private Task safeForTasksRing(Callable<Task> action) {
        tasksRingLock.lock();
        try {
            return action.call();
        } catch (Exception e) {
            e.printStackTrace(Tools.err);
            return null;
        } finally {
            tasksRingLock.unlock();
        }
    }

    // to check is task inside tasks list
    private boolean hasTaskInside(final Task task) {
        return tasksPool.stream().anyMatch(t -> Objects.equals(t.getName(), task.getName()));
    }

    //To get the installed pool's tasks list as a string
    private String tasksList() {
        final StringBuilder builder = new StringBuilder();
        tasks().forEach(task -> builder.append(task.getName()).append("\n"));
        return builder.toString();
    }
}
