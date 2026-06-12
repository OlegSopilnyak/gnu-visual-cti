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

import static org.visualcti.server.core.system.SubSystem.SYSTEM_ROOT_ELEMENT_NAME;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.util.Tools;

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
    // types of SET application command
    String UPDATE_CONFIGURATION_SET_TYPE = "update-server-configuration";
    String SERVER_SYSTEM_PARAMETER_NAME = "system";

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
     * <checker>
     * To check is unit can start according the internal state
     *
     * @return true if unit can start
     */
    @Override
    default boolean canStartUnit() {
        return serverParts().findAny().isPresent();
    }

    /**
     * <command-executor>
     * To execute command for the server.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     * @see RunnableServerUnit#execute(ServerCommandRequest)
     * @see ServerCommandRequest#getFamilyType()
     * @see MessageFamilyType#SET
     * @see #setupServerStuff(ServerCommandRequest)
     */
    @Override
    default void execute(ServerCommandRequest command) throws Exception {
        try {
            // trying to execute the command in the parent unit
            RunnableServerUnit.super.execute(command);
            // the command has been done there.
            // no needs to process it further.
            return;
        } catch (UnknownCommandException e) {
            // doing nothing just trying to execute command further
        }
        //
        final MessageFamilyType commandType = command.getFamilyType();
        // processing command request
        if (commandType == MessageFamilyType.SET) {
            // updating the server
            setupServerStuff(command);
        } else {
            // the command isn't processed here
            throw new UnknownCommandException(commandType + " isn't supported!");
        }
    }

    /**
     * <command-executor>
     * To update the parts of the server
     *
     * @see #updateSeverSystem(Element)
     * @see #UPDATE_CONFIGURATION_SET_TYPE
     */
    default void setupServerStuff(ServerCommandRequest command) throws UnknownCommandException, IOException {
        final String commandSetType = ServerUnit.typeValueOf(command);
        switch (commandSetType) {
            case UPDATE_CONFIGURATION_SET_TYPE:
                final Element systemXml = systemAsXml(command, commandSetType);
                try {
                    // updating the server system in configuration xml file
                    updateSeverSystem(systemXml);
                    // send successful response to the command
                    successfulResponseTo(command, COMMAND_NOT_NEEDED_RESPONSE);
                } catch (IOException e) {
                    // detected the exception
                    final String reasonMessage = "Failed to update server configuration!";
                    failedResponseTo(command, reasonMessage, e);
                }
                break;
            default:
                throw new UnknownCommandException("Invalid SET's command type [" + commandSetType + "]");
        }
    }

    /**
     * <server-updater>
     * To update system in server's xml-document and save changes
     *
     * @param systemXml new value of server's system-xml
     * @throws IOException if it cannot update
     * @see #setupServerStuff(ServerCommandRequest)
     */
    void updateSeverSystem(Element systemXml) throws IOException;

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

    /**
     * <accessor>
     * To get sub-system name from the xml-element
     *
     * @param xml element to get the name from
     * @return the value
     * @throws IOException if xml-element isn't correct
     */
    @SuppressWarnings("unchecked")
    static String serverSystemName(final Element xml) throws IOException {
        if (!Objects.equals(SYSTEM_ROOT_ELEMENT_NAME, xml.getName())) {
            throw new IOException("Expected element with name: " + SYSTEM_ROOT_ELEMENT_NAME);
        }
        final List<Element> children = xml.getChildren();
        if (children.size() != 1) {
            throw new IOException("Expected exactly 1 child element");
        }
        // getting sub-system to update name
        return children.get(0).getName();
    }

    // private methods
    static Element systemAsXml(
            final ServerCommandRequest command, final String actionName
    ) throws UnknownCommandException {
        return inputParameter(command, SERVER_SYSTEM_PARAMETER_NAME, actionName).getValue(Tools.emptyXML);
    }

    static Parameter inputParameter(
            final ServerCommandRequest command, final String parameterName, final String actionName
    ) throws UnknownCommandException {
        return command.getParameter(parameterName, Parameter.INPUT_DIRECTION)
                .orElseThrow(() -> new UnknownCommandException(command.getFamilyType() + " isn't supported! Nothing to " + actionName + "."));
    }
}
