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
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.task.TaskPoolsManagerAdapter;
import org.visualcti.server.task.TasksPoolUnitAdapter;
import org.visualcti.util.Tools;

/**
 * <SubSystem>
 * The sub-system of the application server which in charge with application tasks
 *
 * @see SubSystem
 */
public class TasksSubSystem implements SubSystem {
    private static final String TASKS_MANAGER_ROOT_ELEMENT_NAME = "Manager";
    private static final String TASKS_MANAGER_UNIT_NAME = "Tasks Manager";
    private final MainTaskManager manager = new MainTaskManager();

    /**
     * <accessor>
     * To get the reference to main sub-system manager unit
     *
     * @return instance of the manager
     * @see Manager
     */
    @Override
    public Manager getSystemManager() {
        return manager;
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
        final Element systemElement = SubSystem.super.getXML();
        systemElement.addContent(new Comment(" Server tasks control system "));
        final Element tasksElement = new Element(TASKS_SUB_SYSTEM);
        systemElement.addContent(tasksElement);
        tasksElement.addContent(new Comment(" The manager of tasks pools "));
        tasksElement.addContent((Element) manager.getXML().clone());
        final TasksPoolUnit publicTasksPool = manager.publicTaskPool();
        if (publicTasksPool != null) {
            // public pool adding
            tasksElement.addContent(new Comment(" Pool of public tasks "));
            tasksElement.addContent((Element)publicTasksPool.getXML().clone());
        }
        // local pools adding
        tasksElement.addContent(new Comment(" Pool of concrete device's tasks "));
        manager.localTaskPoolStream()
                .forEach(poolUnit -> tasksElement.addContent((Element)poolUnit.getXML().clone()));
        return systemElement;
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
        final Element systemElement = xml.getChild(TASKS_SUB_SYSTEM);
        if (systemElement == null) {
            throw new IOException("Wrong root element of [" + TASKS_SUB_SYSTEM + "]");
        }
        final Element managerElement = systemElement.getChild(MANAGER_ROOT_ELEMENT);
        if (managerElement == null) {
            throw new IOException("Wrong root element of [" + MANAGER_ROOT_ELEMENT + "]");
        }
        Tools.out.print(">> " + TASKS_SUB_SYSTEM);
        Tools.out.print(": manager .");
        Tools.out.flush();
        // configuring built manager's instance from xml and register it
        manager.configure(managerElement);
        //
        // building task pools units
        final List<Element> poolElements = systemElement.getChildren(TASKS_POOL_ROOT_ELEMENT_NAME);
        Tools.out.print(" : pools .");
        Tools.out.flush();
        for (final Element poolElement : poolElements) {
            // preparing tasks pool server unit and attach it to the manager
            new PoolServerUnit().configure(poolElement);
            // printing the dot after successful pool's configuration
            Tools.out.print(".");
            Tools.out.flush();
        }
        // all task-pools are configured well
        Tools.print("Done!");
    }

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

    // inner classes
    // implementation of sub-system's tasks manager, builder feature of server unit isn't used
    private static class MainTaskManager extends TaskPoolsManagerAdapter implements Manager {
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
            return TASKS_MANAGER_ROOT_ELEMENT_NAME;
        }

        @Override
        public String getSystemName() {
            return TASKS_SUB_SYSTEM;
        }

        @Override
        public void connectTo(final ApplicationServer server) throws IOException {
            // unregistering manager from the registry
            UnitRegistry.unRegister(this);
            // attach manager to the server for correct unit-messages processing
            // UnitRegistry.register(this) doing under the hood
            this.setOwner(server);
        }

        @Override
        public void close() {
            this.owner = null;
            this.unitPath = getSystemName();
        }

        @Override
        public TasksPoolUnit createTaskPool(String name, String factory) {
            final TasksPoolUnit poolUnit = new TasksSubSystem.PoolServerUnit().localPoolFor(name, factory);
            try {
                poolUnit.loadOrCreateTasksList();
            } catch (IOException e) {
                dispatchError(e, "Could not load or create tasks list");
                return null;
            }
            return poolUnit;
        }

        @Override
        protected void settingUpBasePart(Element xml) {
            // doing nothing, keeps original XML
            // Here we don't use builder stuff of the server unit
        }

        @Override
        protected void beforeRegisterUnit() {
            if (isEmptyString.test(this.unitPath) || TASKS_MANAGER_UNIT_NAME.equals(this.unitPath)) {
                this.unitPath = TASKS_SUB_SYSTEM;
            }
        }

        @Override
        protected boolean updatedUnitConfiguration() {
            // keeping loaded configuration Element as is after changed branches
            return true;
        }
    }

    // implementation of sub-system's tasks pool, builder feature of server unit isn't used
    private static class PoolServerUnit extends TasksPoolUnitAdapter {
        @Override
        protected void settingUpBasePart(Element xml) {
            // doing nothing, keeps original XML
            // Here we don't use builder stuff of the server unit
        }
    }
}
