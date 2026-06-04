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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * Implementation Adapter: server tasks pool unit engine
 *
 * @see Task
 * @see TasksPoolUnit
 */
public abstract class TasksPoolUnitAdapter extends RunnableUnitAdapter implements TasksPoolUnit {
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
    private final Predicate<Task> nonNullTask = Objects::nonNull;
    private final Predicate<Task> taskHasValidName = task -> isEmptyString.negate().test(task.getName());
    private final Predicate<Task> validTask = nonNullTask.and(taskHasValidName);
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

    /**
     * To apply parameters for pool creation flow
     *
     * @param name the name of the task pool (hardware channel name)
     * @param factory hardware factory of the channel name
     * @return adjusted by parameters instance
     * @see #applyFor(String, String, PoolType)
     */
    public TasksPoolUnit localPoolFor(String name, String factory) {
        return applyFor(name, factory, PoolType.LOCAL);
    }

    /**
     * To adjust the parameters of the pool before pool's tasks list operations
     *
     * @param name     the name of the pool
     * @param factory  the name of factory(group) of the pool
     * @param poolType the type of the pool
     * @return adjusted pool's instance
     */
    @Override
    public TasksPoolUnit applyFor(String name, String factory, PoolType poolType) {
        // adjusting basic pool's parameters
        this.poolName = name;
        this.poolGroup = factory;
        this.poolType = poolType;
        this.poolFile = name + ".tasks.pool";
        // adjusting tasks-list xml document for further serialization/deserialization
        adjustingTaskListXml();
        // returns adjusted pool's instance
        return this;
    }


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
     * <accessor>
     * To get the group name of the pool
     *
     * @return group name of the pool
     */
    @Override
    public String getPoolGroup() {
        return poolGroup;
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
     */
    @Override
    public void applyTasksFile(String poolFile) {
        if (isEmptyString.negate().test(poolFile) || !Objects.equals(poolFile, this.poolFile)) {
            this.poolFile = poolFile;
        }
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    public String getType() {
        return "[tasks pool]";
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
     * To add the task to the tasks list.
     *
     * @param task instance to add
     * @return true if added successfully
     * @see Task
     * @see #addTask(Task, boolean)
     */
    protected boolean addTask(final Task task) {
        return addTask(task, true);
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

    @Deprecated
    @Override
    public String tasksList() {
        return TasksPoolUnit.super.tasksList();
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

    @Deprecated
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        TasksPoolUnit.super.setXML(xml);
    }

    /**
     * <tasks-keeper>
     * To load tasks list from external XML file
     *
     * @throws IOException           if something went wrong
     * @throws NumberFormatException if something went wrong
     * @throws NullPointerException  if something went wrong
     * @see #setXML(Element)
     * @see Element
     * @see ServerUnitException
     * @see #checkTaskListXml()
     * @see #adjustingTaskListXml()
     * @see #loadTasksList(File)
     * @see TaskPoolsManager#add(ServerUnit)
     */
    @Override
    public void loadTasksList() throws IOException, NumberFormatException, NullPointerException {
        // to check task-list-xml document integrity
        checkTaskListXml();
        // preparing root element of the task list XML document
        adjustingTaskListXml();
        // loading task list and add pool to the pools manager
        try {
            // getting instance of task pool manager
            final TaskPoolsManager poolsManager = UnitRegistry.lookup(TaskPoolsManager.class);
            // preparing tasks-list external file
            // loading tasks-list XML from tasks-list-file and restore the tasks
            loadTasksList(new File(poolsManager.getRoot(), this.poolFile));
            // adding task pool to the pool manager
            poolsManager.add(this);
        } catch (ServerUnitException e) {
            throw new IOException("Wrong tasks manager in registry", e);
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
            // saving tasks list to the external file
            try (final FileOutputStream out = new FileOutputStream(tasksListFile)) {
                store(this.tasksListConfigurationDocument, out);
            }
        } catch (ServerUnitException e) {
            throw new IOException("Wrong manager in registry", e);
        }
    }

    /**
     * <tasks-keeper>
     * To load exists tasks list or create and save the new one
     *
     * @throws IOException if something went wrong
     */
    @Override
    public void loadOrCreateTasksList() throws IOException, NumberFormatException, NullPointerException {
        try {
            // getting instance of task pools manager
            final TaskPoolsManager poolsManager = UnitRegistry.lookup(TaskPoolsManager.class);
            // preparing tasks-list external file
            final File tasksListFile = new File(poolsManager.getRoot(), this.poolFile);
            if (tasksListFile.exists()) {
                // tasks list file exists just load it
                loadTasksList(tasksListFile);
            } else {
                // creating empty tasks list xml document
                adjustingTaskListXml();
                // saving tasks list to the external file
                try (final FileOutputStream out = new FileOutputStream(tasksListFile)) {
                    store(this.tasksListConfigurationDocument, out);
                }
                // preparing the unit path for first time
                this.unitPath = getName();
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

    @Deprecated
    @Override
    public void execute(ServerCommandRequest command) throws Exception {
        TasksPoolUnit.super.execute(command);
    }

    /**
     * <checker>
     * To check is unit can start according the internal state
     * Task Pool cannot start if tasks list is empty
     *
     * @return true if tasks list isn't empty
     */
    @Override
    public boolean canStartUnit() {
        return !tasksPool.isEmpty();
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     * Should be implemented in the children classes
     *
     * @see TasksPoolUnit#Start()
     * @see TasksPoolUnit#startUnitRunnable()
     */
    @Override
    public void startUnitRunnable() {
        if (!isPublic()) {
            // public pool doesn't need to prepare tasks ring
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
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Should be implemented in the children classes
     *
     * @see TasksPoolUnit#Stop()
     * @see TasksPoolUnit#stopUnitRunnable() \
     */
    @Override
    public void stopUnitRunnable() {
        safeForTasksRing(() -> {
                    // stopping current task execution
                    if (currentTask != null) {
                        currentTask.stopExecute();
                    }
                    // to clean the tasks ring
                    inServiceTasksRing.clear();
                    // set moveTaskUp current-task as null
                    currentTask = null;
                    // doesn't matter
                    return null;
                }
        );
    }

    /**
     * <action>
     * To execute GET tasks-pool command
     *
     * @param command GET command to execute
     * @throws UnknownCommandException if command target is unknown
     * @throws IOException             if response preparing went wrong
     * @see TasksPoolUnit#execute(ServerCommandRequest)
     * @see #responsePoolSimpleInfo(ServerCommandRequest)
     * @see #responsePoolXmlInfo(ServerCommandRequest)
     */
    @Override
    public void executePoolGet(ServerCommandRequest command) throws UnknownCommandException, IOException {
        // getting parameter with name "target" from the executing command
        final String target = ServerUnit.targetValueOf(command);
        switch (target) {
            case GET_POOL_INFO_TARGET:
                // target is "info" responding to it
                responsePoolSimpleInfo(command);
                break;
            case GET_POOL_TASK_INFO_TARGET:
                // target is "edit" responding to it
                responsePoolXmlInfo(command);
                break;
            default:
                throw new UnknownCommandException("Invalid GET's command target [" + target + "]");
        }
    }

    /**
     * <action>
     * To execute SET tasks-pool command
     *
     * @param command SET command to execute
     * @throws UnknownCommandException if command target is unknown
     * @throws IOException             if response preparing went wrong
     * @see #deployingTask(ServerCommandRequest)
     * @see #installingTask(ServerCommandRequest)
     * @see #deletingTask(ServerCommandRequest)
     * @see #movingTask(ServerCommandRequest)
     */
    @Override
    public void executePoolSet(ServerCommandRequest command) throws UnknownCommandException, IOException {
        final String commandSetType = ServerUnit.typeValueOf(command);
        switch (commandSetType) {
            case SET_DEPLOY_TASK_TYPE:
                // deploying (updating) task in tasks list
                deployingTask(command);
                break;
            case SET_INSTALL_TASK_TYPE:
                // installing (adding or updating) task in tasks list
                installingTask(command);
                break;
            case SET_DELETE_TASK_TYPE:
                // deleting the task from tasks list
                deletingTask(command);
                break;
            case SET_MOVE_TASK_TYPE:
                // moving the task inside tasks list
                movingTask(command);
                break;
            default:
                throw new UnknownCommandException("Invalid SET's command type [" + commandSetType + "]");
        }
    }

    /**
     * <action>
     * Closing the server unit, releasing attached resources and restoring original unitPath
     *
     * @throws IOException if an I/O error occurs
     * @see #unitPath
     */
    @Override
    public void close() throws IOException {
        this.unitPath = getName();
    }


    @Deprecated
    @Override
    public Optional<Task> getTask(String taskName) {
        return TasksPoolUnit.super.getTask(taskName);
    }

    // private methods

    // to check task-list-xml document integrity
    private void checkTaskListXml() throws IOException {
        // checking the task-list-xml document integrity
        final List<?> taskListDocumentContent = this.tasksListConfigurationDocument.getContent();
        if (taskListDocumentContent == null || taskListDocumentContent.size() < 2) {
            throw new IOException("Tasks list configuration document is invalid.");
        }
        // checking the task-list-xml document license
        if (!(taskListDocumentContent.get(0) instanceof Comment)) {
            throw new IOException("Tasks list licence is invalid.");
        }
        // checking the task-list-xml document root element properties
        final Element taskListXml = this.tasksListConfigurationDocument.getRootElement();
        if(taskListXml == null || !TASKS_LIST_ROOT_ELEMENT_NAME.equals(taskListXml.getName())) {
            throw new IOException("Wrong tasks list root element name.");
        }
    }

    // preparing root element of the task list XML document
    private void adjustingTaskListXml() {
        final Element tasksListRootElement = new Element(TASKS_LIST_ROOT_ELEMENT_NAME);
        final String tasksListPoolType = this.poolType.getType();
        tasksListRootElement.setAttribute(TASKS_POOL_TYPE_ATTRIBUTE_NAME, tasksListPoolType);
        tasksListRootElement.addContent(new Comment(String.format(TASKS_LIST_ABOUT_TEMPLATE, this.poolName)));
        // adjusting XML document of the task-list
        this.tasksListConfigurationDocument.detachRootElement().detach();
        this.tasksListConfigurationDocument.setRootElement(tasksListRootElement);
    }

    // loading tasks-list xml and restore the tasks
    @SuppressWarnings("unchecked")
    private void loadTasksList(final File tasksListFile) throws IOException {
        try (final FileInputStream in = new FileInputStream(tasksListFile)) {
            final List<Element> xmlsList = restoreDocumentFrom(in).getRootElement().getChildren(Task.ROOT_ELEMENT);
            xmlsList.stream().map(TaskMaker::restore)
                    // only tasks restored successfully
                    .filter(Objects::nonNull)
                    // adding restored tasks to the tasks pool
                    .forEach(task -> addTask(task, false))
            ;
            // preparing unit path before register
            if (isEmptyString.test(this.unitPath)) {
                this.unitPath = getName();
            }
        }
    }

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
        tasks.forEach(task -> tasksListXml.addContent(task.getXML().detach()));
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
            final String eventMessage = action + ":\n\t- tasks.list -\n" + this.tasksList();
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

    // to install the task to the tasks pool depends on is it inside or not
    private boolean installing(final Task task) {
        return hasTaskInside(task) ? updateTask(task) : addTask(task);
    }

    // making changes in tasks pool using "modify" function if the task isn't empty
    private void modifyTask(final ServerCommandRequest command, final Task task, final String modifyType,
                            final Function<Task, Boolean> modify, final String reasonMessage) throws IOException {
        if (task != null) {
            // valid task value, modifying
            respondTo(command, true,
                    response -> response.setParameter(Parameter.of(modifyType, modify.apply(task)).output())
            );
        } else {
            // invalid task value, reporting
            commandFailed(command, reasonMessage);
        }
    }

    // prepare and send error message
    private void commandFailed(final ServerCommandRequest command, final String reasonMessage) throws IOException {
        respondTo(command, false,
                response -> response.setParameter(Parameter.of("reason", reasonMessage).output())
        );
    }

    // getting pool info for target "edit"
    private void responsePoolXmlInfo(ServerCommandRequest command) throws UnknownCommandException, IOException {
        // getting task name from request's parameter "task"
        final String taskName = TasksPoolUnit.taskParameterAsString(command, GET_POOL_TASK_INFO_TARGET);
        // getting pool's task by name
        final Task task = getTask(taskName)
                .orElseThrow(() -> new UnknownCommandException("Invalid get task by name [" + taskName + "]"));
        // preparing and sending response to the command
        respondTo(command, response -> {
            // preparing response of the task's parameters for the task editing action
            response
                    .setParameter(Parameter.of("edit.class", "nothing :-(").output())
                    .setParameter(Parameter.of(Task.ROOT_ELEMENT, task.getXML()).output())
            ;
        });
    }

    // getting pool info for target "info"
    private void responsePoolSimpleInfo(ServerCommandRequest command) {
        safeForTasksRing(() -> {
            respondTo(command, response -> {
                // returning common pool's parameters
                response.setParameter(Parameter.of("unit.state", currentUnitState().toString()).output())
                        .setParameter(Parameter.of("tasks.list", tasksList()).output())
                ;
                // getting current task in the running pool
                final Task task = this.current();
                if (task != null) {
                    response.setParameter(Parameter.of("current", task.getName()).output());
                }
            });
            // return doesn't matter
            return null;
        });
    }

    // moving the task inside tasks list
    private void movingTask(ServerCommandRequest command) throws UnknownCommandException, IOException {
        final String commandSetType = SET_DELETE_TASK_TYPE;
        final String taskNameToMove = TasksPoolUnit.taskParameterAsString(command, commandSetType);
        final String moveDirection =
                TasksPoolUnit.inputParameter(command, SET_MOVE_TASK_DIRECTION, commandSetType).getValue("????:-P");
        final String action = SET_MOVE_TASK_TYPE + "." + moveDirection;
        switch (moveDirection) {
            case SET_MOVE_TASK_UP:
                // moving task up in tasks pool
                modifyTask(
                        command, getTask(taskNameToMove).orElse(null),
                        action, this::moveTaskUp, "invalid move's direction up"
                );
                break;
            case SET_MOVE_TASK_DOWN:
                // moving task down in tasks pool
                modifyTask(
                        command, getTask(taskNameToMove).orElse(null),
                        action, this::moveTaskDown, "invalid move's direction down"
                );
                break;
            default:
                // unknown moving task direction
                throw new UnknownCommandException("invalid move's direction " + moveDirection);
        }
    }

    // deleting the task from tasks list
    private void deletingTask(ServerCommandRequest command) throws UnknownCommandException, IOException {
        final String commandSetType = SET_DELETE_TASK_TYPE;
        final String taskNameToDelete = TasksPoolUnit.taskParameterAsString(command, commandSetType);
        modifyTask(
                command, getTask(taskNameToDelete).orElse(null),
                commandSetType, this::removeTask, "invalid task to delete in the pool"
        );
    }

    // installing (adding or updating) task in tasks pool
    private void installingTask(ServerCommandRequest command) throws UnknownCommandException, IOException {
        final String commandSetType = SET_INSTALL_TASK_TYPE;
        final Parameter taskParameter = TasksPoolUnit.taskParameter(command, commandSetType);
        switch (taskParameter.getType()) {
            case Parameter.STRING:
                // input task as task name
                // installing (adding or updating) task from public tasks pool to the tasks pool directly
                final TaskPoolsManager manager = (TaskPoolsManager) this.getOwner();
                final TasksPoolUnit publicTaskPool = manager.publicTaskPool();
                modifyTask(
                        command, publicTaskPool.getTask(taskParameter.getValue("????:-P")).orElse(null),
                        commandSetType, this::installing, "no task to install in the public pool"
                );
                break;
            case Parameter.XML:
                // input task as task XML
                // installing(adding or updating) task to the pool directly
                modifyTask(
                        command, TaskMaker.restore(taskParameter.getValue(Tools.emptyXML)),
                        commandSetType, this::installing, "invalid task to install XML");
                break;
            default:
                commandFailed(command, "invalid type of the install task input parameter");
                break;
        }
    }

    // deploying (updating) task in tasks pool
    private void deployingTask(ServerCommandRequest command) throws IOException, UnknownCommandException {
        final String commandSetType = SET_DEPLOY_TASK_TYPE;
        modifyTask(
                command, TaskMaker.restore(TasksPoolUnit.taskParameterAsXml(command, commandSetType)),
                commandSetType, this::updateTask, "invalid task to deploy XML"
        );
    }
}
