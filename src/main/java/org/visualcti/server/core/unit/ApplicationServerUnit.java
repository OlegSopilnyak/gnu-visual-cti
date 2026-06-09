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
package org.visualcti.server.core.unit;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.jdom.DataConversionException;
import org.visualcti.server.core.system.SubSystem;

/**
 * Facade: The root unit of the application
 *
 * @see RunnableServerUnit
 */
public interface ApplicationServerUnit extends RunnableServerUnit {
    // the file which contains primary information about application server parameters
    File CONFIGURATION_XML_FILE = new File("./conf/VisualCTI.server.xml");
    String BRAND_ROOT_ELEMENT_NAME = "VisualCTI";
    String SERVER_ROOT_ELEMENT_NAME = "Server";
    String SERVER_DATE_FORMAT_ATTRIBUTE_NAME = "date";
    String SERVER_DATE_FORMAT_DEFAULT_VALUE = "dd MMMM yyyy HH:mm:ss.SSS";
    String SERVER_BASE_UNIT_ELEMENT_NAME = "base";
    String SERVER_BASE_UNIT_PACKAGE_ATTRIBUTE_NAME = "package";
    String SERVER_RMI_ELEMENT_NAME = "rmi";
    String SERVER_RMI_DESCRIPTION = "local RMI Registry properties";
    String SERVER_RMI_PORT_ATTRIBUTE_NAME = "port";
    int SERVER_RMI_PORT_DEFAULT_VALUE = 2888;
    String SERVER_RMI_STARTUP_ATTRIBUTE_NAME = "start";
    boolean SERVER_RMI_STARTUP_DEFAULT_VALUE = false;
    String SERVER_UNIT_PATH_IN_REGISTRY = "{Server}";

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    default String getName() {
        return "Server";
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    default String getType() {
        return "[kernel]";
    }

    /**
     * <accessor>
     * To get access to the owner of this composite (null for root unit)
     *
     * @return the reference to server composite's owner or null if it isn't exists
     * @see ServerUnit
     */
    @Override
    default ServerUnit getOwner() {
        // no owners for the server instance
        return null;
    }

    /**
     * <accessor>
     * To get server's part
     *
     * @return stream to registered in the server sub-systems
     */
    Stream<SubSystem> serverParts();

    /**
     * <server-configuration-keeper>
     * To load server configuration from the external XML file
     *
     * @throws IOException             if something went wrong
     */
    void loadServerXml() throws IOException, DataConversionException;

    /**
     * <server-configuration-keeper>
     * To save server configuration to the external XML file
     *
     * @throws IOException             if something went wrong
     */
    void saveServerXml() throws IOException;
}
