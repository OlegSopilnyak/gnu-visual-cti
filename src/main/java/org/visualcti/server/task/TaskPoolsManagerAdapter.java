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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.unit.ServerUnitAdapter;
import org.visualcti.util.Tools;

/**
 * <manager>
 * Implementation: The manager of task pools
 *
 * @see TaskPoolsManager
 */
public class TaskPoolsManagerAdapter extends ServerUnitAdapter implements TaskPoolsManager {
    // current state of the engine
    private transient volatile State state = State.OUT_OF_SERVICE;
    // the reference in FileSystem to the tasks directory
    private transient File rootDirectory = new File("./TASKS");
    // the storage of the task pools under manager's control
    private final Map<String, TasksPoolUnit> poolStore = new HashMap<>(100);
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
     * <accessor>
     * get access to public TaskPool(all tasks pool)
     *
     * @return public pool instance
     * @see TasksPoolUnit
     */
    @Override
    public TasksPoolUnit publicTaskPool() {
        final Callable<TasksPoolUnit> action = () -> poolStore.values().stream()
                .filter(TasksPoolUnit::isPublic)
                .findFirst().orElse(null);
        return safeAction(action);
    }

    /**
     * <accessor>
     * get access to TaskPool for scheduler by CT-device name & device's factory
     *
     * @param name    the name of tasks pool
     * @param factory the factory-owner group name of the task pool
     * @return local pool instance
     */
    @Override
    public TasksPoolUnit getTaskPool(String name, String factory) {
        return safeAction(() -> getOrCreateTaskPool(name, factory));
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

    }

    // private methods
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

    // to get or create tasks pool by name and factory
    private TasksPoolUnit getOrCreateTaskPool(String name, String factory) {
        TasksPoolUnit pool = poolStore.get(poolKey(SYSTEM_GROUP, name));
        if (pool != null) {
            // tasks pool is not attached to any server tasks channel
            return pool;
        } else {
            // tasks pool is attached to some server tasks channel
            return poolStore.computeIfAbsent(poolKey(factory, name), key -> {
                // creating new tasks pool
                final TasksPoolUnit poolUnit = createTaskPool(name, factory);
                // NPE will be thrown if pool creation was failed
                Tools.print("Created tasks pool :" + poolUnit.getName());
                // add created pool to the units composite
                super.add(poolUnit);
                // returns valid tasks pool
                return poolUnit;
            });
        }
    }

    // creating new tasks pool
    private TasksPoolUnit createTaskPool(String name, String factory) {
        return null;
    }

    private static String poolKey(String name, String factory) {
        return factory + "/" + name;
    }

    /**
     * <accessor>
     * To get current state value
     *
     * @return the current state ID of the engine
     */
    @Override
    public short getState() {
        return state.getCode();
    }

    /**
     * <mutator>
     * To set up the new state value
     *
     * @param state new state ID of the engine
     */
    @Override
    public void setState(short state) {
        this.state = State.of(state);
    }
}
