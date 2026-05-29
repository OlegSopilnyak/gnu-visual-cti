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

import java.io.File;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.visualcti.server.core.unit.RunnableServerUnit;

/**
 * <manager>
 * UnitFacade: The parent of the tasks manager (CTI-applications)
 *
 * @see RunnableServerUnit
 */
public interface TaskPoolsManager extends RunnableServerUnit {
    // predicate to check is manager's pool public
    Predicate<TasksPoolUnit> isPublicPool = TasksPoolUnit::isPublic;

    /**
     * <accessor>
     * To get access to tasks root directory
     *
     * @return the reference to the directory where tasks are living
     * @see File#exists()
     * @see File#isDirectory()
     */
    File getRoot();

    /**
     * <accessor>
     * get access to public TaskPool(all tasks pool)
     *
     * @return public pool instance
     * @see TasksPoolUnit
     * @see #isPublicPool
     * @see #taskPoolStreamBy(Predicate)
     */
    default TasksPoolUnit publicTaskPool() {
        return taskPoolStreamBy(isPublicPool).findFirst().orElse(null);
    }

    /**
     * <accessor>
     * get access to local TaskPool stream
     *
     * @return local task pools stream
     * @see TasksPoolUnit
     * @see Stream
     * @see #isPublicPool
     * @see #taskPoolStreamBy(Predicate)
     */
    default Stream<TasksPoolUnit> localTaskPoolStream() {
        return taskPoolStreamBy(isPublicPool.negate());
    }

    /**
     * <accessor>
     * To get access to manager's TaskPool stream by condition
     *
     * @return task pools stream
     * @see TasksPoolUnit
     * @see Stream
     * @see Predicate
     */
    default Stream<TasksPoolUnit> taskPoolStreamBy(Predicate<TasksPoolUnit> condition) {
        return children()
                .filter(TasksPoolUnit.class::isInstance).map(TasksPoolUnit.class::cast)
                .filter(condition);
    }

    /**
     * <accessor>
     * get access to TaskPool for scheduler by CT-device name & device's factory
     *
     * @param name    the name of tasks pool
     * @param factory the name of factory-owner group name of the task pool
     * @return local pool instance
     */
    TasksPoolUnit getTaskPool(String name, String factory);

    /**
     * <mutator>
     * To detach the tasks pool from the manager
     *
     * @param name    the name of tasks pool
     * @param factory the name of factory-owner group name of the task pool
     * @return detached pool instance
     */
    TasksPoolUnit detachTaskPool(String name, String factory);
}
