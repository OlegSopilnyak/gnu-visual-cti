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
package org.visualcti.server.system.manager;

import java.io.IOException;
import org.jdom.Element;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.task.AbstractTaskPoolsManager;
import org.visualcti.server.task.AbstractTasksPool;

/**
 * <SystemManagerSingleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Manager: The manager of all Server's Application Tasks</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTaskPoolsManager
 * @see SubSystem.Manager
 */
public class TasksManager extends AbstractTaskPoolsManager implements SubSystem.Manager {
    private static final String TASKS_MANAGER_UNIT_NAME = "Tasks Manager";
    private static TasksManager instance;
    private SubSystem subSystem;

    /**
     * <builder>
     * To get access to the Tasks Manager instance as a singleton
     *
     * @return the singleton of the manager
     */
    public static TasksManager getInstance() {
        if (instance == null) {
            synchronized (TasksManager.class) {
                if (instance == null) {
                    instance = new TasksManager();
                }
            }
        }
        return instance;
    }

    @Override
    public String getName() {
        return TASKS_MANAGER_UNIT_NAME;
    }

    @Override
    protected String getUnitDescription() {
        return " The manager of tasks pools ";
    }

    @Override
    public String getRootElementName() {
        return SubSystem.MANAGER_ROOT_ELEMENT;
    }

    @Override
    public SubSystem getSystem() {
        return subSystem;
    }

    public void setSystem(SubSystem subSystem) {
        this.subSystem = subSystem;
    }

    @Override
    public void close() {
        this.unitPath = defaultManagerUnitPath();
    }

    @Override
    public TasksPoolUnit createTaskPool(final String name, final String factory) {
        try {
            // creating new tasks pool
            final TasksPoolUnit tasksPool = new Pool().localPoolFor(name, factory);
            // loading or creating tasks list of the new tasks pool
            tasksPool.loadOrCreateTasksList();
            // returning created tasks pool with the tasks list
            return tasksPool;
        } catch (IOException e) {
            dispatchError(e, "Could not load or create tasks list");
            return null;
        }
    }

    @Override
    protected void settingUpBasePart(Element xml) {
        // doing nothing, keeps original XML
        // Here we don't use builder stuff of the server unit
    }

    @Override
    protected void beforeRegisterUnit() {
        if (isEmptyString.test(this.unitPath) || TASKS_MANAGER_UNIT_NAME.equals(this.unitPath)) {
            this.unitPath = defaultManagerUnitPath();
        }
    }

    @Override
    protected boolean updatedUnitConfiguration() {
        // keeping loaded configuration Element as is after changed branches
        return true;
    }

    /// private methods
    private TasksManager() {
    }

    /// inner classes
    /**
     * <SystemManager>
     * <p>Title: Visual CTI Java Telephony Server</p>
     * <p>Description: VisualCTI Applications Server,<br>
     * Pool: The Pool of Server's Application Tasks</p>
     *
     * @author Sopilnyak Oleg
     * @version 3.2
     * @see AbstractTasksPool
     */
    public static class Pool extends AbstractTasksPool {
        @Override
        protected void settingUpBasePart(Element xml) {
            // doing nothing, keeps original XML
            // Here we don't use builder stuff of the server unit
        }
    }
}
