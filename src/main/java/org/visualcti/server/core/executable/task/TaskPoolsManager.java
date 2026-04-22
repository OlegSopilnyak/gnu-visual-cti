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
import org.visualcti.server.core.XmlAware;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.unit.part.UnitsComposite;

/**
 * <manager>
 * The manager of task pools
 *
 * @see UnitsComposite
 * @see Engine
 * @see XmlAware
 */
public interface TaskPoolsManager extends UnitsComposite, Engine, XmlAware {
    // factory-owner group name of the task pool (by default)
    String SYSTEM_GROUP = "System";

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
     */
    TasksPoolUnit publicTaskPool();

    /**
     * <accessor>
     * get access to TaskPool for scheduler by CT-device name & device's factory
     *
     * @param name the name of tasks pool
     * @param factory the name of factory-owner group name of the task pool
     * @return local pool instance
     */
    TasksPoolUnit getTaskPool(String name, String factory);
}
