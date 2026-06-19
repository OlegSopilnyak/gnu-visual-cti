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
package org.visualcti.server.core.system;

import java.io.IOException;
import org.visualcti.core.XmlAware;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.core.unit.RunnableServerUnit;

/**
 * <SubSystem>
 * The sub-system of the application server
 *
 * @see XmlAware
 */
public interface SubSystem extends XmlAware {
    String SYSTEM_ROOT_ELEMENT_NAME = "system";
    // the name of tasks sub-system
    String TASKS_SUB_SYSTEM = "Tasks";
    String SERVICES_SUB_SYSTEM = "Services";
    String HARDWARE_SUB_SYSTEM = "Hardware";
    // the root element name of the manager definition
    String MANAGER_ROOT_ELEMENT = "Manager";

    /**
     * <accessor>
     * To get the name of the root element name in XML result
     *
     * @return the name of root element
     * @see XmlAware#getXML()
     */
    @Override
    default String getRootElementName() {
        return SYSTEM_ROOT_ELEMENT_NAME;
    }

    /**
     * <accessor>
     * To get the name of the system's element name in the XML
     *
     * @return the name of element inside root one
     * @see #getRootElementName()
     *
     */
    String getSystemElementName();

    /**
     * <accessor>
     * To get the reference to main sub-system manager unit
     *
     * @return instance of the manager
     * @see Manager
     */
    Manager getSystemManager();

    /**
     * <SystemManager>
     * The sub-system's main manager unit to register in the server (root) unit
     *
     * @see RunnableServerUnit
     */
    interface Manager extends RunnableServerUnit {
        /**
         * <accessor>
         * To get the name of sub-system manage is belong to
         *
         * @return sub-system name
         */
        String getSystemName();

        /**
         * <connect>
         * To connect the manager to the applications server
         *
         * @param server instance to connect
         * @throws IOException if the manager can't connect
         */
        void connectTo(ApplicationServer server) throws IOException;
    }
}
