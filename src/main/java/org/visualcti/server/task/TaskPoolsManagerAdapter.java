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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * <manager>
 * Implementation: The manager of task pools
 *
 * @see TaskPoolsManager
 */
public abstract class TaskPoolsManagerAdapter extends RunnableUnitAdapter implements TaskPoolsManager {
    // the name of tasks' parameter
    private static final String TASKS_DIRECTORY_PARAMETER = "directory";
    // testing is parameter unit's icon
    private static final Predicate<ConfigurationParameter> isTasksDirectoryParameter =
            parameter -> TASKS_DIRECTORY_PARAMETER.equals(parameter.getName());
    // current state of the engine
    private transient volatile State state = State.OUT_OF_SERVICE;
    // the reference in FileSystem to the tasks directory
    private transient String rootDirectoryName = null;
    private transient File rootDirectory = new File("./TASKS");
    // the lock to protect pool storage
    private final Lock poolLock = new ReentrantLock();

    /**
     * <accessor>
     * To get access to tasks root directory
     *
     * @return the reference to the directory where tasks are living
     * @see File#exists()
     * @see File#isDirectory()
     */
    @Override
    public File getRoot() {
        return rootDirectory;
    }

    /**
     * <mutator>
     * To change the value of the root tasks pool files directory
     * For tests purposes only
     *
     * @param rootDirectoryName nwe value
     */
    public void setRootDirectoryName(String rootDirectoryName) {
        this.rootDirectoryName = rootDirectoryName;
        this.rootDirectory = new File(rootDirectoryName);
    }

    /**
     * <mutator>
     * to add child to the server unit composite units tree<BR/>
     * set up the owner for the child unit current unit
     *
     * @param unit the unit to add
     * @return true if it's succeeded
     * @see ServerUnit#add(ServerUnit)
     * @see TasksPoolUnit
     */
    @Override
    public boolean add(ServerUnit unit) {
        return safeAction(() ->
                unit instanceof TasksPoolUnit && super.add(unit) ? (TasksPoolUnit) unit : null
        ) != null;
    }

    /**
     * <mutator>
     * To detach the tasks pool from the manager
     *
     * @param name    the name of tasks pool
     * @param factory the name of factory-owner group name of the task pool
     * @return detached pool instance
     */
    @Override
    public TasksPoolUnit detachTaskPool(String name, String factory) {
        return safeAction(() -> findTaskPoolBy(name, factory).map(gotPool -> {
                    // unregistering detached pool from th registry
                    UnitRegistry.unRegister(gotPool);
                    try {
                        // stopping detached pool
                        gotPool.Stop();
                    } catch (IOException e) {
                        dispatchError(e, "Cannot stop detached task pool: " + name);
                        return null;
                    }
                    // removing detached pool from unit's tree branches
                    return super.remove(gotPool) ? gotPool : null;
                }).orElse(null)
        );
    }

    /**
     * <accessor>
     * get access to public TaskPool(all tasks pool)
     *
     * @return public pool instance
     * @see TasksPoolUnit
     */
    @Override
    public TasksPoolUnit publicTaskPool() {
        return safeAction(TaskPoolsManager.super::publicTaskPool);
    }

    /**
     * <accessor>
     * get access to TaskPool for scheduler by CT-device name & device's factory
     *
     * @param name    the name of tasks pool
     * @param factory the factory-owner group name of the task pool
     * @return local pool instance
     * @see #findTaskPoolBy(String, String)
     * @see #createTaskPool(String, String)
     */
    @Override
    public TasksPoolUnit getTaskPool(String name, String factory) {
        return safeAction(() -> {
            // found a task pool is attached to the manager, returning it
            return findTaskPoolBy(name, factory)
                    // task pool is not attached to the manager, creating it
                    .orElseGet(() -> {
                        final TasksPoolUnit createdPool = createTaskPool(name, factory);
                        // NPE will be thrown if pool creation was failed
                        Tools.print("Created tasks pool :" + createdPool.getName());
                        // attach created pool to the manager as a unit branch
                        return super.add(createdPool) ? createdPool : null;
                    });
        });
    }

    /**
     * <builder>
     * To build new instance of the local tasks pool
     *
     * @param name    the name of the tasks channel
     * @param factory the name of factory(group) of the tasks channel
     * @return built not registered instance
     * @see #getTaskPool(String, String)
     */
    protected abstract TasksPoolUnit createTaskPool(String name, String factory);

    /**
     * <converter>
     * To represent the parameters of unit as an XML element
     * Here managed the icon of the server unit
     *
     * @param rootElement building from unit XML Element
     * @see Element
     * @see org.visualcti.server.unit.ServerUnitAdapter#getXML()
     */
    @Override
    protected void prepareUnitXML(Element rootElement) {
        super.prepareUnitXML(rootElement);
        if (isEmptyString.negate().test(rootDirectoryName)) {
            rootElement.addContent(
                    ConfigurationParameter.of(TASKS_DIRECTORY_PARAMETER, rootDirectoryName).getXml()
            );
        }
    }

    /**
     * <converter>
     * To update the entity's fields from XML
     *
     * @param xml possible entity's XML
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     */
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        super.setXML(xml);
        if (isEmptyString.test(rootDirectoryName)) {
            throw new IOException("Wrong tasks directory name parameter.");
        }
    }

    /**
     * <converter>
     * <applier>
     * To apply configuration parameter of the server unit
     *
     * @param parameter the unit's parameter to apply
     * @see ConfigurationParameter
     * @see org.visualcti.server.unit.ServerUnitAdapter#processParameter(ConfigurationParameter)
     */
    @Override
    protected void applyUnitParameter(ConfigurationParameter parameter) {
        if (isTasksDirectoryParameter.test(parameter)) {
            // found tasks directory parameter
            final String tasksDirectoryName = parameter.getValue();
            final File tasksDirectory = new File(tasksDirectoryName);
            if (tasksDirectory.exists() && tasksDirectory.isDirectory()) {
                // directory exists and is a directory FS entry
                this.rootDirectory = tasksDirectory;
                this.rootDirectoryName = tasksDirectoryName;
            }
        }
    }

    @Deprecated
    @Override
    public short getState() {
        return state.getCode();
    }

    @Deprecated
    @Override
    public void setState(short state) {
        this.state = State.of(state);
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        return "Tasks Manager";
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    public String getType() {
        return "[manager]";
    }

    // private methods
    // to do children manipulation in safest way
    private TasksPoolUnit safeAction(final Callable<TasksPoolUnit> action) {
        poolLock.lock();
        try {
            return action.call();
        } catch (Exception e) {
            e.printStackTrace(Tools.err);
            return null;
        } finally {
            poolLock.unlock();
        }
    }

    // to get the task pool, added to the manager, in not safe way by name and factory name (group)
    private Optional<TasksPoolUnit> findTaskPoolBy(String name, String factory) {
        final Predicate<TasksPoolUnit> condition = pool ->
                Objects.equals(pool.getPoolName(), name) && Objects.equals(pool.getPoolGroup(), factory);
        return TaskPoolsManager.super.taskPoolStreamBy(condition).findFirst();
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     * Should be implemented in the children classes
     *
     */
    @Override
    public void startUnitRunnable() {

    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Should be implemented in the children classes
     *
     */
    @Override
    public void stopUnitRunnable() {

    }
}
