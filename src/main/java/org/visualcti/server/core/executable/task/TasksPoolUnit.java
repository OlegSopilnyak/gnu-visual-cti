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

import org.visualcti.server.core.XmlAware;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.unit.ServerUnit;

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
public interface TasksPoolUnit extends Engine, ServerUnit, XmlAware {
    /**
     * Type: task-pool type
     */
    enum PoolType {
        PUBLIC("public"),
        LOCAL("local");
        private String type;

        PoolType(String type) {
            this.type = type;
        }

        static PoolType of(String type) {
            for (PoolType p : PoolType.values()) {
                if (p.type.equalsIgnoreCase(type)) {
                    return p;
                }
            }
            return null;
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
}
