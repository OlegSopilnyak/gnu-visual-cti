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
package org.visualcti.server.system;

import static org.visualcti.server.core.executable.task.TasksPoolUnit.TASKS_POOL_ROOT_ELEMENT_NAME;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.EngineUnitBuilder;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.system.manager.TasksManager;
import org.visualcti.server.task.AbstractTaskPoolsManager;
import org.visualcti.server.task.AbstractTasksPool;
import org.visualcti.util.Tools;

/**
 * <System>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * System: The sub-system of the application server which is in charge with application tasks</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see SubSystem
 * @see AbstractTasksPool
 * @see AbstractTaskPoolsManager
 */
@SuppressWarnings("unchecked")
public class TasksSubSystem implements SubSystem {
    /**
     * <accessor>
     * To get the name of the system's element name in the XML
     *
     * @return the name of element inside root one
     * @see #getRootElementName()
     *
     */
    @Override
    public String getSystemElementName() {
        return TASKS_SUB_SYSTEM;
    }

    /**
     * <accessor>
     * To get the reference to main sub-system manager unit
     *
     * @return instance of the manager
     * @see Manager
     */
    @Override
    public TasksManager getSystemManager() {
        try {
            return UnitRegistry.lookup(TasksManager.class);
        } catch (ServerUnitException e) {
            e.printStackTrace(Tools.err);
            return null;
        }
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see XmlAware#getXML()
     * @see #getRootElementName()
     */
    @Override
    public Element getXML() {
        final TasksManager manager = getSystemManager();
        // preparing TasksSubSystem's XML-element
        final Element rootTasksElement = new Element(TASKS_SUB_SYSTEM);
        rootTasksElement.addContent((Element) manager.getXML().clone());
        final TasksPoolUnit publicTasksPool = manager.publicTaskPool();
        if (publicTasksPool != null) {
            // public pool adding
            rootTasksElement.addContent(new Comment(" Pool of public tasks (server's tasks library) "));
            rootTasksElement.addContent((Element) publicTasksPool.getXML().clone());
        }
        // local pools adding
        rootTasksElement.addContent(new Comment(" Pool of concrete device's tasks "));
        manager.localTaskPoolStream().forEach(tasksPoolUnit ->
                rootTasksElement.addContent((Element) tasksPoolUnit.getXML().clone())
        );
        // adding tasks xml-element to the root of sub-system and return it
        return SubSystem.super.getXML()
                .addContent(new Comment(" Server tasks control system "))
                .addContent(rootTasksElement);
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
    @SuppressWarnings("unchecked")
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // getting sub-system's manager's configuration xml-element
        final Element managerElement = extractSystemManagerElement(xml);
        // building and configuring manager's instance from xml and register it
        final Consumer<TasksManager> afterManagerCreate = manager -> manager.setSystem(this);
        final TasksManager manager = EngineUnitBuilder.getInstance().build(managerElement, afterManagerCreate);
        //
        // iterating by xml-elements of the tasks pools
        // building tasks pools units as well
        final List<Element> poolElements = managerElement.getParent().getChildren(TASKS_POOL_ROOT_ELEMENT_NAME);
        Tools.out.print(" : pools .");
        Tools.out.flush();
        for (final Element tasksPoolConfigurationXml : poolElements) {
            // preparing tasks pool server unit and attach it to the manager
            final TasksPoolUnit tasksPool = new TasksManager.Pool();
            // configuring created tasks pool
            tasksPool.configure(tasksPoolConfigurationXml);
            // attach it to the manager
            tasksPool.setOwner(manager);
            // printing the dot after successful pool's configuration
            Tools.out.print(".");
            Tools.out.flush();
        }
        // all task-pools are configured well
        Tools.print("Done!");
    }
}
