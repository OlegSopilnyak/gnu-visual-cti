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
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.task.TaskPoolsManagerAdapter;
import org.visualcti.server.task.TasksPoolUnitAdapter;

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
//    private final List<PoolReflection> localTaskPools = new LinkedList<>();
//    private PoolReflection publicTasksPool;

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
        tasksElement.addContent(manager.getXML());
        final TasksPoolUnit publicTasksPool = manager.publicTaskPool();
        if (publicTasksPool != null) {
            // public pool adding
            tasksElement.addContent(new Comment(" Pool of public tasks "));
            tasksElement.addContent(publicTasksPool.getXML());
        }
        // local pools adding
        tasksElement.addContent(new Comment(" Pool of concrete device's tasks "));
        manager.localTaskPoolStream().forEach(poolUnit -> tasksElement.addContent(poolUnit.getXML()));
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
        // building manager's instance from xml and register it
        this.manager.configure(managerElement);
        //
        // building task pools units
        final List<Element> poolElements = systemElement.getChildren(TASKS_POOL_ROOT_ELEMENT_NAME);
        for(final Element poolElement : poolElements) {
            // preparing tasks pool server unit
//            final TasksPoolUnit poolUnit = new PoolServerUnit();
            // preparing tasks pool server unit and attach it to the manager
            new PoolServerUnit().configure(poolElement);
//            // preparing tasks pool reference
//            final PoolReflection reflection = new PoolReflection(poolElement);
//            if (reflection.isPublic()) {
//                // public pool storing to sub-system's field
//                publicTasksPool = reflection;
//            } else {
//                // local pool storing adding to sub-system's field
//                localTaskPools.add(reflection);
//            }
        }
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

//    public List<PoolReflection> getTaskPools() {
//        return localTaskPools;
//    }
//
//    public PoolReflection getPublicTasksPool() {
//        return publicTasksPool;
//    }

    // inner classes
    // implementation of sub-system's tasks manager, builder feature of server unit isn't used
    private static class MainTaskManager extends TaskPoolsManagerAdapter implements Manager {

        @Override
        public String getName() {
            return TASKS_MANAGER_UNIT_NAME;
        }

        /**
         * <builder>
         * To build local tasks pool
         *
         * @param name    the name of the tasks channel
         * @param factory the name of factory(group) of the tasks channel
         * @return built not registered instance
         */
        @Override
        protected TasksPoolUnit createTaskPool(String name, String factory) {
            final TasksPoolUnit poolUnit = new TasksSubSystem.PoolServerUnit();
            poolUnit.setPoolName(name);
            poolUnit.setPoolGroup(factory);
            poolUnit.setPoolType(TasksPoolUnit.PoolType.LOCAL);
            poolUnit.setPoolFile(name+".tasks.pool");
            try {
                poolUnit.loadTasksList();
            } catch (IOException e) {
                return null;
            }
            return poolUnit;
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

    // tasks pool refection
//    private static class PoolReflection implements XmlAware {
//        // the type of the pool
//        private String type;
//        // the name of the pool (<factory>/local name)
//        private String name;
//        // the tasks file
//        private String file;
//
//        private boolean isPublic() {
//            return TasksPoolUnit.PoolType.PUBLIC.getType().equals(type);
//        }
//
//        private boolean isLocal() {
//            return !isPublic();
//        }
//
//        public PoolReflection(Element element) throws IOException {
//            setXML(element);
//        }
//
//        /**
//         * <converter>
//         * To represent entity as an XML element
//         *
//         * @return entity's XML
//         * @see Element
//         */
//        @Override
//        public Element getXML() {
//            return new Element(TASKS_POOL_ROOT_ELEMENT_NAME)
//                    .setAttribute(TASKS_POOL_TYPE_ATTRIBUTE_NAME, type)
//                    .setAttribute(TASKS_POOL_NAME_ATTRIBUTE_NAME, name)
//                    .setAttribute(TASKS_POOL_EXTERNAL_FILE_ATTRIBUTE_NAME, file);
//        }
//
//        /**
//         * <converter>
//         * To update the entity's fields from XML
//         *
//         * @param xml possible entity's XML
//         * @throws IOException if root element is wrong
//         * @see Element
//         */
//        @Override
//        public void setXML(Element xml) throws IOException, NumberFormatException, NullPointerException {
//            final String rootName = xml.getName();
//            if (!TASKS_POOL_ROOT_ELEMENT_NAME.equals(rootName)) {
//                throw new IOException("Wrong root element name [" + rootName + "]");
//            }
//            this.type = validAttribute(xml, TASKS_POOL_TYPE_ATTRIBUTE_NAME);
//            this.name = validAttribute(xml, TASKS_POOL_NAME_ATTRIBUTE_NAME);
//            this.file = validAttribute(xml, TASKS_POOL_EXTERNAL_FILE_ATTRIBUTE_NAME);
//        }
//
//        private static String validAttribute(Element element, String attributeName) throws IOException {
//            return Optional.ofNullable(element.getAttributeValue(attributeName))
//                    .orElseThrow(() -> new IOException("Wrong value of attribute [" + attributeName + "]"));
//        }
//    }
}
