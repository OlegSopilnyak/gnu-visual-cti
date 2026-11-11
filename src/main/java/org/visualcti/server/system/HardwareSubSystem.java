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

import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.channel.device.adapter.AbstractDevicesManager;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.util.Tools;

/**
 * <SubSystem>
 * The sub-system of the application server which is in charge with hardware channel-devices
 *
 * @see SubSystem
 */
public class HardwareSubSystem implements SubSystem {
    private static final String HARDWARE_MANAGER_UNIT_NAME = "Hardware Manager";
    private final MainDevicesManager manager;

    public HardwareSubSystem() {
        manager = new MainDevicesManager(this);
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
        return HARDWARE_SUB_SYSTEM;
    }

    /**
     * <accessor>
     * To get the reference to main sub-system manager unit
     *
     * @return instance of the manager
     * @see Manager
     */
    @Override
    public SubSystem.Manager getSystemManager() {
        return manager;
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
        final Element systemElement = xml.getChild(getSystemElementName());
        if (systemElement == null) {
            throw new IOException("Wrong root element of [" + getSystemElementName() + "]");
        }
        final Element managerElement = systemElement.getChild(MANAGER_ROOT_ELEMENT);
        if (managerElement == null) {
            throw new IOException("Wrong root element of [" + MANAGER_ROOT_ELEMENT + "]");
        }
//        final ServerUnit mngr;
//        try {
//            mngr = EngineUnitBuilder.getInstance().build(managerElement, MainTaskManager.class);
//        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
//                 InvocationTargetException e) {
//            throw new IOException("Wrong root element of [" + MANAGER_ROOT_ELEMENT + "]", e);
//        }
        Tools.out.print(">> " + getSystemElementName());
        Tools.out.print(": manager .");
        Tools.out.flush();
        // configuring built manager's instance from xml and register it
        manager.configure(managerElement);
        //
        // building task channel-device-factory units
//        final List<Element> poolElements = systemElement.getChildren(TASKS_POOL_ROOT_ELEMENT_NAME);
//        Tools.out.print(" : pools .");
//        Tools.out.flush();
//        for (final Element poolElement : poolElements) {
//            // preparing tasks pool server unit and attach it to the manager
//            new TasksSubSystem.PoolServerUnit().configure(poolElement);
//            // printing the dot after successful pool's configuration
//            Tools.out.print(".");
//            Tools.out.flush();
//        }
        // all task-pools are configured well
        Tools.print("Done!");
    }

    // inner classes
    // implementation of sub-system's channel-devices manager, builder feature of server unit isn't used here at the moment
    private static class MainDevicesManager extends AbstractDevicesManager implements SubSystem.Manager {
        private final SubSystem owner;

        private MainDevicesManager(SubSystem owner) {
            this.owner = owner;
        }

        @Override
        public String getName() {
            return HARDWARE_MANAGER_UNIT_NAME;
        }

        @Override
        protected String getUnitDescription() {
            return " The manager of channel-devices ";
        }

        @Override
        public String getRootElementName() {
            return MANAGER_ROOT_ELEMENT;
        }

        @Override
        public SubSystem getSystem() {
            return owner;
        }

        @Override
        public void close() {
            this.unitPath = defaultManagerUnitPath();
        }

        @Override
        protected void settingUpBasePart(Element xml) {
            // doing nothing, keeps original XML
            // Here we don't use builder stuff of the server unit
        }

        @Override
        protected void beforeRegisterUnit() {
            if (isEmptyString.test(this.unitPath) || HARDWARE_MANAGER_UNIT_NAME.equals(this.unitPath)) {
                this.unitPath = defaultManagerUnitPath();
            }
        }

        @Override
        protected boolean updatedUnitConfiguration() {
            // keeping loaded configuration Element as is after changed branches
            return true;
        }
    }
}
