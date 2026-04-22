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
import java.util.Collection;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.core.XmlAware;
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
public interface TasksPoolUnit extends ServerUnit, XmlAware {
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
     * To get the group name of the pool
     *
     * @return group name of the pool
     */
    String getGroup();

    /**
     * <mutator>
     * To set up the group name of the pool
     *
     * @param group new value of pool's group
     * @return reference to tasks pool
     */
    TasksPoolUnit setGroup(String group);

    /**
     * <mutator>
     * To set up the tasks list file name of the pool
     *
     * @param poolFile new value of pool's file name
     * @return reference to tasks pool
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
     * To add the task to a pool.
     *
     * @param task   instance to add
     * @param notify flag is need notification after
     * @return true if added successfully
     * @see Task
     */
    boolean add(Task task, boolean notify);

    /**
     * <mutator>
     * To update the task in a pool.
     *
     * @param task instance to update
     * @return true if updated successfully
     * @see Task
     */
    boolean update(Task task);

    /**
     * <mutator>
     * To remove the task from a pool.
     *
     * @param task instance to remove
     * @return true if removed successfully
     * @see Task
     */
    boolean remove(Task task);

    /**
     * <order>
     * To move task up in the tasks list.
     *
     * @param task to move up
     * @return true if moved successfully
     * @see Task
     */
    boolean up(Task task);

    /**
     * <order>
     * To move task down in the tasks list.
     *
     * @param task to move down
     * @return true if moved successfully
     * @see Task
     */
    boolean down(Task task);

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
    default void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // here we update unit from element of main server configuration
        setPoolType(PoolType.of(xml.getAttributeValue("type")));
        String[] name = xml.getAttributeValue("name").split("/");
        if (name.length > 1) {
            setGroup(name[1]);
        }
        setPoolFile(xml.getAttributeValue("file"));
        // the call loading of XML from pool-file
        loadingTasksList();
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
     */
    void loadingTasksList() throws IOException, DataConversionException, NumberFormatException, NullPointerException;
}
