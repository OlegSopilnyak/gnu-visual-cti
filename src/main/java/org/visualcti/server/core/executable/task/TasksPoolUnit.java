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
package org.visualcti.server.core.executable.task;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.util.Tools;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The parent of the tasks' pool (CTI-applications)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface TasksPoolUnit extends RunnableServerUnit {

    String TASKS_POOL_TYPE_ATTRIBUTE_NAME = "type";
    String TASKS_POOL_NAME_ATTRIBUTE_NAME = "name";
    String TASKS_POOL_EXTERNAL_FILE_ATTRIBUTE_NAME = "file";
    String TASKS_LIST_ROOT_ELEMENT_NAME = "TasksPool";
    String TASKS_LIST_ABOUT_TEMPLATE = "The tasks pool for [%s]";
    String TASKS_POOL_ROOT_ELEMENT_NAME = "pool";
    // target types of GET command
    String GET_POOL_INFO_TARGET = "info";
    String GET_POOL_EDIT_TARGET = "edit";
    // types of SET command
    String SET_DEPLOY_TASK_TYPE = "deploy";
    String SET_INSTALL_TASK_TYPE = "install";
    String SET_DELETE_TASK_TYPE = "delete";
    String SET_MOVE_TASK_TYPE = "move";
    // task moving constants
    String SET_MOVE_TASK_DIRECTION = "direction";
    String SET_MOVE_TASK_UP = "up";
    String SET_MOVE_TASK_DOWN = "down";

    /**
     * Type: task-pool type
     */
    enum PoolType {
        PUBLIC("public"),
        LOCAL("local");
        private final String type;

        PoolType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        static PoolType of(String type) {
            return Arrays.stream(PoolType.values())
                    .filter(poolType -> poolType.getType().equalsIgnoreCase(type))
                    .findFirst().orElse(null);
        }
    }

    /**
     * <accessor>
     * To get the type of task-pool
     *
     * @return the value
     * @see PoolType
     */
    PoolType getPoolType();

    /**
     * <mutator>
     * To set up the type of task-pool
     *
     * @param poolType new value of task-pool type
     * @return reference to the pool
     * @see PoolType
     * @see #setXML(Element)
     */
    TasksPoolUnit setPoolType(PoolType poolType);

    /**
     * <accessor>
     * To get the type of task-pool is public flag
     *
     * @return true if pool is public
     * @see PoolType#PUBLIC
     */
    default boolean isPublic() {
        return getPoolType() == PoolType.PUBLIC;
    }

    /**
     * <accessor>
     * To get the group name of the pool
     *
     * @return group name of the pool
     */
    String getPoolGroup();

    /**
     * <mutator>
     * To set up the group name of the pool
     *
     * @param group new value of pool's group
     * @return reference to tasks pool
     * @see #setXML(Element)
     */
    TasksPoolUnit setPoolGroup(String group);

    /**
     * <accessor>
     * To get the name of the pool
     *
     * @return the name of the pool
     */
    String getPoolName();

    /**
     * <mutator>
     * To set up the name of the pool
     *
     * @param name new value of pool's group
     * @return reference to tasks pool
     * @see #setXML(Element)
     */
    TasksPoolUnit setPoolName(String name);

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    default String getName() {
        return isEmpty(getPoolGroup()) ? getPoolName() : getPoolGroup() + "/" + getPoolName();
    }

    /**
     * <mutator>
     * To set up the tasks list file name of the pool
     *
     * @param poolFile new value of pool's file name
     * @return reference to tasks pool
     * @see #setXML(Element)
     */
    TasksPoolUnit setPoolFile(String poolFile);

    /**
     * <accessor>
     * To get the current(executed) task. (returned last next() call)
     * This method works only when engine isStarted()
     *
     * @return current server task
     * @see Task
     */
    Task current();

    /**
     * <action>
     * To get next task.
     * This method works only when engine isStarted()
     * otherwise will return null
     *
     * @return next server task from started tasks pool
     * @see Task
     */
    Task next();

    /**
     * <accessor>
     * To get the installed pool's tasks list
     *
     * @return list of installed tasks
     * @see Task
     */
    Collection<Task> tasks();

    /**
     * <mutator>
     * To addTask the task to a pool.
     *
     * @param task   instance to addTask
     * @param notify flag is need notification after (used when tasks are loading)
     * @return true if added successfully
     * @see Task
     * @see #loadTasksList()
     */
    boolean addTask(Task task, boolean notify);

    /**
     * <mutator>
     * To updateTask the task in a pool.
     *
     * @param task instance to updateTask
     * @return true if updated successfully
     * @see Task
     */
    boolean updateTask(Task task);

    /**
     * <mutator>
     * To remove the task from a pool.
     *
     * @param task instance to remove
     * @return true if removed successfully
     * @see Task
     */
    boolean removeTask(Task task);

    /**
     * <order>
     * To move task up in the tasks list.
     *
     * @param task to move up
     * @return true if moved successfully
     * @see Task
     */
    boolean moveTaskUp(Task task);

    /**
     * <order>
     * To move task down in the tasks list.
     *
     * @param task to move down
     * @return true if moved successfully
     * @see Task
     */
    boolean moveTaskDown(Task task);

    /**
     * <accessor>
     * To get pool's task by task name
     *
     * @param taskName the name of the task to get
     * @return exists task or empty
     * @see Task
     * @see Task#getName()
     * @see Optional
     */
    default Optional<Task> getTask(String taskName) {
        return tasks().stream().filter(task -> task.getName().equals(taskName)).findFirst();
    }

    /**
     * <converter>
     * To updateTask the entity's fields from XML
     *
     * @param xml possible entity's XML
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     * @see #configure(Element)
     */
    @Override
    default void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // here we updateTask unit from element of main server configuration
        setPoolType(PoolType.of(xml.getAttributeValue(TASKS_POOL_TYPE_ATTRIBUTE_NAME)));
        final String combinedPoolName = xml.getAttributeValue(TASKS_POOL_NAME_ATTRIBUTE_NAME);
        if (isEmpty(combinedPoolName)) {
            // empty value of pool name
            throw new IOException("Pool name is empty");
        }
        // resolving pool-name from XML
        final String[] nameParts = combinedPoolName.split("/");
        if (nameParts.length > 1) {
            setPoolGroup(nameParts[1]).setPoolName(nameParts[0])
                    .setPoolFile(xml.getAttributeValue(TASKS_POOL_EXTERNAL_FILE_ATTRIBUTE_NAME));
        } else {
            setPoolName(nameParts[0])
                    .setPoolFile(xml.getAttributeValue(TASKS_POOL_EXTERNAL_FILE_ATTRIBUTE_NAME));
        }
        // loading tasks list of the pool from the external pool-file
        loadTasksList();
    }

    /**
     * <tasks-keeper>
     * To load tasks list from external XML file
     *
     * @throws IOException             if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see #setXML(Element)
     */
    void loadTasksList() throws IOException, NumberFormatException, NullPointerException;

    /**
     * <tasks-keeper>
     * To save tasks list to the external XML file
     *
     * @throws IOException             if something went wrong
     */
    void saveTasksList() throws IOException;

    /**
     * <executer>
     * To execute command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     * @see RunnableServerUnit#execute(ServerCommandRequest)
     * @see ServerCommandRequest#getFamilyType()
     * @see #Start()
     * @see #Stop()
     */
    @Override
    default void execute(ServerCommandRequest command) throws Exception {
        try {
            // trying to execute the command in the parent unit
            RunnableServerUnit.super.execute(command);
            // the command has been done there.
            // no needs to process it further.
            return;
        } catch (UnknownCommandException e) {
            // doing nothing just trying to execute command further
        }
        // checking command to execute (is it needs response)
        ServerUnit.validateCommand(command);
        //
        final MessageFamilyType commandType = command.getFamilyType();
        // processing command request
        switch (commandType) {
            case GET:
                // execute GET command
                executePoolGet(command);
                break;
            case SET:
                // execute SET command
                executePoolSet(command);
                break;
            default:
                // the command isn't processed here
                throw new UnknownCommandException(commandType + " isn't supported!");
        }
    }

    /**
     * <action>
     * To execute GET tasks-pool command
     *
     * @param command GET command to execute
     * @throws UnknownCommandException if command target is unknown
     * @throws IOException if response preparing went wrong
     */
    void executePoolGet(ServerCommandRequest command) throws UnknownCommandException, IOException;

    /**
     * <action>
     * To execute SET tasks-pool command
     *
     * @param command SET command to execute
     * @throws UnknownCommandException if command target is unknown
     * @throws IOException if response preparing went wrong
     */
    void executePoolSet(ServerCommandRequest command) throws UnknownCommandException, IOException;

    static Element taskParameterAsXml(ServerCommandRequest command, String actionName) throws UnknownCommandException {
        return taskParameter(command, actionName).getValue(Tools.emptyXML);
    }

    static String taskParameterAsString(ServerCommandRequest command, String actionName) throws UnknownCommandException {
        return taskParameter(command, actionName).getValue("????:-P");
    }

    static Parameter taskParameter(ServerCommandRequest command, String actionName) throws UnknownCommandException {
        return inputParameter(command, Task.ROOT_ELEMENT, actionName);
    }

    static Parameter inputParameter(ServerCommandRequest command, String parameterName, String actionName) throws UnknownCommandException {
        return command.getParameter(parameterName, Parameter.INPUT_DIRECTION)
                .orElseThrow(() -> new UnknownCommandException(command.getFamilyType() + " isn't supported! Nothing to " + actionName + "."));
    }

    /**
     * <accessor>
     * To get the installed pool's tasks list as a string
     *
     * @return installed task list names
     */
    default String tasksList() {
        final StringBuilder builder = new StringBuilder();
        tasks().forEach(task -> builder.append(task.getName()).append("\n"));
        return builder.toString();
    }
}
