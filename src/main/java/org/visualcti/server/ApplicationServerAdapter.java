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
package org.visualcti.server;

import static org.visualcti.server.core.system.SubSystem.SYSTEM_ROOT_ELEMENT_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.ApplicationServerUnit;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.server.system.TasksSubSystem;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * Facade Implementation: The root unit of the application
 *
 * @see ApplicationServerUnit
 * @see RunnableUnitAdapter
 */
public class ApplicationServerAdapter extends RunnableUnitAdapter implements ApplicationServerUnit {
    private final Lock subSystemLock = new ReentrantLock();
    // the configuration for RMI
    private final transient RmiConfig rmi = new RmiConfig();
    // the server configuration file
    private File serverConfigFile = CONFIGURATION_XML_FILE;
    // the registry of RMI
    private transient Registry localRegistry;
    // the container of server's sub-systems
    private final Map<String, SubSystem> system = new ConcurrentHashMap<>();
    // XML-Document of the server's configuration
    private final Document serverConfigurationDocument = initialConfigurationDocument();
    // XML-Element the root of server configuration
    private final Element serverConfigurationElement = serverConfigurationDocument.getRootElement().getChild(SERVER_ROOT_ELEMENT_NAME);
    // The format of DateTime using for to String transformation
    private Format dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Deprecated
    @Override
    public String getName() {
        return ApplicationServerUnit.super.getName();
    }

    @Deprecated
    @Override
    public String getType() {
        return ApplicationServerUnit.super.getType();
    }

    /**
     * <action>
     * Closing the server unit, releasing attached resources and restoring original unitPath
     *
     * @throws IOException if an I/O error occurs
     * @see #unitPath
     */
    @Override
    public void close() throws IOException {
        this.unitPath = SERVER_UNIT_PATH_IN_REGISTRY;
        if (localRegistry != null) {
            UnicastRemoteObject.unexportObject(localRegistry,true);
            localRegistry = null;
        }
        removeUnitMessageListener(this);
        system.clear();
    }

    /**
     * To change the server's configuration file
     * Using only for tests purposes
     *
     * @param serverConfigFile new file value
     * @see #loadServerXml()
     * @see #saveServerXml()
     */
    public void setServerConfigFile(File serverConfigFile) {
        this.serverConfigFile = serverConfigFile;
    }


    /**
     * <action>
     * Processing message in this unit
     *
     * @param message the message to process
     * @see UnitMessage
     * @see #handleUnitMessage(UnitMessage)
     */
    @Override
    public void processUnitMessage(UnitMessage message) {
        Tools.print("\tProcessing message [" + message + "]\n\tThe message will be lost...");
    }

    /**
     * <accessor>
     * To get local RMI registry to share RMI access of the server objects
     *
     * @return RMI registry instance
     * @see Registry
     */
    @Override
    public Registry localRegistry() {
        return localRegistry;
    }

    /**
     * <initializer>
     * To initialize the server and prepare all parts for starting
     *
     * @throws IOException if something went wrong
     * @throws DataConversionException if something went wrong with xml-configuration processing
     */
    public void initialize() throws IOException, DataConversionException {
        // initializing server core stuff
        initializeCore();
        // loading configuration from config-file
        loadServerXml();
        // launching rmi registry
        exposeRmiRegistry();
        // setup server's unit-path to the root unit
        this.unitPath = SERVER_UNIT_PATH_IN_REGISTRY;
        // adding as server unit message listener
        addUnitMessageListener(this);
        // registering server as server-unit in the units registry
        UnitRegistry.register(this);
    }

    @Deprecated
    @Override
    public boolean canStartUnit() {
        return ApplicationServerUnit.super.canStartUnit();
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     * Starting server's sub-systems
     *
     * @see RunnableServerUnit#Start()
     * @see RunnableServerUnit#startUnitChild(RunnableServerUnit)
     * @see SubSystem#getSystemManager()
     */
    @Override
    public void startUnitRunnable() {
        safeServerSystemsAction(() -> system.values().stream()
                .peek(subSystem -> startUnitChild(subSystem.getSystemManager()))
                .reduce((first, second) -> second).orElse(null)
        );
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Stopping server's sub-systems
     *
     * @see RunnableServerUnit#Stop()
     * @see RunnableServerUnit#stopUnitChild(RunnableServerUnit)
     * @see SubSystem#getSystemManager()
     */
    @Override
    public void stopUnitRunnable() {
        safeServerSystemsAction(() -> system.values().stream()
                .peek(subSystem -> stopUnitChild(subSystem.getSystemManager()))
                .reduce((first, second) -> second).orElse(null)
        );
    }

    @Deprecated
    @Override
    public void execute(ServerCommandRequest command) throws Exception {
        ApplicationServerUnit.super.execute(command);
    }

    @Deprecated
    @Override
    public void setupServerStuff(ServerCommandRequest command) throws UnknownCommandException, IOException {
        ApplicationServerUnit.super.setupServerStuff(command);
    }

    /**
     * <server-updater>
     * To update system in server's xml-document and save changes
     *
     * @param systemXml new value of server's system-xml
     * @throws IOException if it cannot update
     * @see #setupServerStuff(ServerCommandRequest)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void updateSeverSystem(final Element systemXml) throws IOException {
        // getting sub-system to update name
        final String subSystemToUpdateName = ApplicationServerUnit.serverSystemName(systemXml);
        if (isEmptyString.test(subSystemToUpdateName)) {
            throw new IOException("Empty subsystem name.");
        }
        // copying the server system elements to temporary container
        final List<Element> systems = (List<Element>) serverConfigurationElement.getChildren(SYSTEM_ROOT_ELEMENT_NAME)
                .stream().map(sys -> ((Element) sys).clone()).collect(Collectors.toList());
        // removing system entries from the configuration root element
        serverConfigurationElement.removeChildren(SYSTEM_ROOT_ELEMENT_NAME);
        // repairing system entries in the configuration root element
        for (final Element xml : systems) {
            final Element serverSystemXml =
                    subSystemToUpdateName.equals(ApplicationServerUnit.serverSystemName(xml)) ? systemXml : xml;
            serverConfigurationElement.addContent(serverSystemXml);
        }
        // saving updated configuration
        saveServerXml();
    }

    /**
     * <accessor>
     * To get server's parts
     *
     * @return stream to registered in the server sub-systems
     */
    @Override
    public Stream<SubSystem> serverParts() {
        return new ArrayList<>(system.values()).stream();
    }

    /**
     * <server-configuration-keeper>
     * To load server configuration from the external XML file
     *
     * @throws IOException if something went wrong
     * @throws DataConversionException if something went wrong with XML stuff
     * @see #serverConfigFile
     */
    @Override
    public void loadServerXml() throws IOException, DataConversionException {
        try(final FileInputStream in = new FileInputStream(serverConfigFile)) {
            // to check and prepare loaded server root xml-element
            final Element serverXml = loadAndCheckConfigurationXmlDocument(prepareXmlDocument(in));
            boolean needToSave = false;
            // apply header values to initial document and server's properties
            needToSave = applyServerHeader(serverXml);
            // load and configure server's sub-systems
            needToSave = configureServerSystems(serverXml) || needToSave;
            if (needToSave) {
                // saving updated server's configuration
                saveServerXml();
            }
        }
    }

    @Deprecated
    @Override
    public Document prepareXmlDocument(InputStream in) throws IOException {
        return super.prepareXmlDocument(in);
    }

    /**
     * <server-configuration-keeper>
     * To save server configuration to the external XML file
     *
     * @throws IOException if something went wrong
     * @see #serverConfigFile
     */
    @Override
    public void saveServerXml() throws IOException {
        try(final FileOutputStream out = new FileOutputStream(serverConfigFile)) {
            store(serverConfigurationDocument, out);
        }
    }

    @Deprecated
    @Override
    public void store(Document document, OutputStream out) throws IOException {
        super.store(document, out);
    }

    // inner classes
    private static class RmiConfig {
        int port;
        boolean startup;

        public RmiConfig() {
            this.port = SERVER_RMI_PORT_DEFAULT_VALUE;
            this.startup = SERVER_RMI_STARTUP_DEFAULT_VALUE;
        }
    }

    // private methods
    // initializing server core stuff
    private void initializeCore() {
        // cleaning server units registry
        UnitRegistry.clear();
        // setup server's unit-path to the root unit
        this.unitPath = "";
        // removing systems from server configuration XML document
        this.serverConfigurationElement.removeChildren(SYSTEM_ROOT_ELEMENT_NAME);
    }

    // to load and configure server's sub-systems
    @SuppressWarnings("unchecked")
    private boolean configureServerSystems(final Element serverXml) throws IOException, DataConversionException {
        final List<Element> systems = serverXml.getChildren(SYSTEM_ROOT_ELEMENT_NAME);
        for (final Element element : systems) {
            serverConfigurationElement.addContent((Element) element.clone());
            final SubSystem subSystem = buildSubSystem(element);
            if (subSystem != null) {
                system.put(subSystem.getSystemElementName(), subSystem);
                // connecting the manager with server
                subSystem.getSystemManager().connectTo(this);
            }
        }
        return false;
    }

    // to build server's sub-system instance from XML-element
    @SuppressWarnings("unchecked")
    private SubSystem buildSubSystem(final Element subSystemElement) throws IOException, DataConversionException {
        final List<Element> children = subSystemElement.getChildren();
        if (children.size() != 1) {
            throw new IOException("Expected exactly 1 child element");
        }
        // building the sub-system instance
        final String systemName = children.get(0).getName();
        Tools.print("Resolving system :" + systemName);
        final SubSystem subSystem = getSubSystem(systemName);
        if (subSystem != null) {
            // configuring it by XML
            subSystem.setXML(subSystemElement);
        }
        return subSystem;
    }

    // to get subsystem by its name
    private SubSystem getSubSystem(String subSystemName) {
        return safeServerSystemsAction(() -> system.computeIfAbsent(subSystemName, this::createSubSystem));
    }

    // to do action with protected server systems container
    private SubSystem safeServerSystemsAction(final Callable<SubSystem> action) {
        subSystemLock.lock();
        try {
            return action.call();
        } catch (Exception e) {
            dispatchError(e, "Error while calling server action!");
            return null;
        } finally {
            subSystemLock.unlock();
        }
    }

    // to create subsystem by its name
    private SubSystem createSubSystem(String systemName) {
        switch (systemName) {
            case SubSystem.TASKS_SUB_SYSTEM:
                // tasks sub system creation
                return new TasksSubSystem();
            case SubSystem.SERVICES_SUB_SYSTEM:
                // services sub system creation
                return null;
            case SubSystem.HARDWARE_SUB_SYSTEM:
                // external channels sub system creation
                return null;
            default:
                return null;
        }
    }

    // initiating configuration xml-document
    private static Document initialConfigurationDocument() {
        final Element serverXmlElement = new Element(SERVER_ROOT_ELEMENT_NAME)
                .setAttribute(SERVER_DATE_FORMAT_ATTRIBUTE_NAME, SERVER_DATE_FORMAT_DEFAULT_VALUE)
                .addContent(new Comment("Base interface of any Server's parts"))
                .addContent(
                        new Element(SERVER_BASE_UNIT_ELEMENT_NAME)
                                .setAttribute(SERVER_BASE_UNIT_PACKAGE_ATTRIBUTE_NAME, ServerUnit.class.getPackage().getName())
                                .setText(ServerUnit.class.getSimpleName())
                ).addContent(new Comment("Properties of main RMI registry"))
                .addContent(new Element(SERVER_RMI_ELEMENT_NAME)
                        .setAttribute(SERVER_RMI_PORT_ATTRIBUTE_NAME, String.valueOf(SERVER_RMI_PORT_DEFAULT_VALUE))
                        .setAttribute(SERVER_RMI_STARTUP_ATTRIBUTE_NAME, String.valueOf(SERVER_RMI_STARTUP_DEFAULT_VALUE))
                        .setText(SERVER_RMI_DESCRIPTION)
                );
        return new Document(Collections.singletonList(new Comment(Tools.getLicenceHeader())))
                .setRootElement(new Element(BRAND_ROOT_ELEMENT_NAME).addContent(serverXmlElement));
    }

    // to check loaded xml-document and prepare server root xml-element
    private Element loadAndCheckConfigurationXmlDocument(final Document configurationDocument) throws IOException {
        // checking the configuration-xml document integrity
        final List<?> configurationDocumentContent = configurationDocument.getContent();
        if (configurationDocumentContent == null || configurationDocumentContent.size() < 2) {
            throw new IOException("Invalid configuration file XML");
        }
        // checking the server configuration-xml document
        final Element serverRoot = validServerRootElement(configurationDocument, configurationDocumentContent);
        if (serverRoot == null) {
            throw new IOException("Wrong server's configuration root element.");
        }
        return serverRoot;
    }

    // checking the server configuration-xml document
    private Element validServerRootElement(
            final Document configurationDocument, final List<?> configurationDocumentContent
    ) throws IOException {
        // checking the configuration-xml document license
        if (!(configurationDocumentContent.get(0) instanceof Comment)) {
            throw new IOException("VisualCTI configuration licence is invalid.");
        }
        // checking the configuration-xml document root element
        final Element brandRoot = configurationDocument.getRootElement();
        if(brandRoot == null || !BRAND_ROOT_ELEMENT_NAME.equals(brandRoot.getName())) {
            throw new IOException("Wrong brand's configuration root element name.");
        }
        // getting nested brand's element (Server)
        return brandRoot.getChild(SERVER_ROOT_ELEMENT_NAME);
    }

    // apply header values to initial document and server's properties
    private boolean applyServerHeader(final Element serverXml) {
        final Element attachedServerRoot = serverConfigurationDocument.getRootElement().getChild(SERVER_ROOT_ELEMENT_NAME);
        boolean needToSave = false;
        // dealing with date-time format
        final String dateFormat = serverXml.getAttributeValue(SERVER_DATE_FORMAT_ATTRIBUTE_NAME);
        if (isEmptyString.negate().test(dateFormat)) {
            attachedServerRoot.setAttribute(SERVER_DATE_FORMAT_ATTRIBUTE_NAME, dateFormat);
            dateTimeFormat = new SimpleDateFormat(dateFormat);
            needToSave = true;
        }
        // dealing with RMI properties
        final Element attacedRmiElement = attachedServerRoot.getChild(SERVER_RMI_ELEMENT_NAME);
        final Element loadedRmiElement = serverXml.getChild(SERVER_RMI_ELEMENT_NAME);
        // dealing with RMI port value
        needToSave = configureRmiPort(loadedRmiElement, attacedRmiElement, needToSave);
        // dealing with RMI startup value
        needToSave = configureRmiStartup(loadedRmiElement, attacedRmiElement, needToSave);
        return needToSave;
    }

    // configuring the RMI port value
    private boolean configureRmiPort(Element loadedRmiElement, Element attacedRmiElement, boolean needToSave) {
        final String rmiPortValue = loadedRmiElement.getAttributeValue(SERVER_RMI_PORT_ATTRIBUTE_NAME);
        final String defaultRmiPortValue = String.valueOf(SERVER_RMI_PORT_DEFAULT_VALUE);
        if (isEmptyString.negate().test(rmiPortValue) && !Objects.equals(rmiPortValue, defaultRmiPortValue)) {
            try {
                rmi.port = Integer.parseInt(rmiPortValue);
                attacedRmiElement.setAttribute(SERVER_RMI_PORT_ATTRIBUTE_NAME, rmiPortValue);
                needToSave = true;
            }catch (NumberFormatException e){
                Tools.error("Invalid RMI port value.");
                e.printStackTrace(Tools.err);
            }
        }
        return needToSave;
    }

    // configuring the RMI startup value
    private boolean configureRmiStartup(Element loadedRmiElement, Element attacedRmiElement, boolean needToSave) {
        final String rmiStartupValue = loadedRmiElement.getAttributeValue(SERVER_RMI_STARTUP_ATTRIBUTE_NAME);
        final String defaultRmiStartupValue = String.valueOf(SERVER_RMI_STARTUP_DEFAULT_VALUE);
        if (isEmptyString.negate().test(rmiStartupValue) && !Objects.equals(rmiStartupValue, defaultRmiStartupValue)) {
            rmi.startup = Boolean.parseBoolean(rmiStartupValue);
            attacedRmiElement.setAttribute(SERVER_RMI_STARTUP_ATTRIBUTE_NAME, rmiStartupValue);
            needToSave = true;
        }
        return needToSave;
    }

    // launching RMI registry
    private void exposeRmiRegistry() {
        if (rmi.startup) {
            try {
                localRegistry = LocateRegistry.createRegistry(rmi.port);
            } catch(java.rmi.server.ExportException e) {
                this.unitState = UnitState.BROKEN;
                e.printStackTrace(Tools.err);
                // detected another copy of RMI Registry server
                Tools.error("Only one copy of running Server is Valid\nThis copy will be stopped.)");
                throw new InternalError("Another copy of VisualCTI Server is detected", e);
            } catch (java.rmi.RemoteException e) {
                this.unitState = UnitState.BROKEN;
                Tools.error("Application RMI make failed " + e.getMessage());
                throw new InternalError("Create registry invalid", e);
            }
            Tools.print("Server's RMI Registry has been initialized just now.");
        }
    }
}
