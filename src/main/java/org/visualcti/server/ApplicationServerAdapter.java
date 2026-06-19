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
import org.visualcti.core.XmlAware;
import org.visualcti.server.core.ApplicationServer;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.system.TasksSubSystem;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.util.Tools;

/**
 * Facade Implementation: The root unit of the application
 *
 * @see ApplicationServer
 * @see RunnableUnitAdapter
 */
public class ApplicationServerAdapter extends RunnableUnitAdapter implements ApplicationServer {
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

    /**
     * <server-updater>
     * To update system in server's xml-document and save changes
     *
     * @param systemXml new value of server's system-xml
     * @throws IOException if it cannot update
     * @see #manageServerStuff(ServerCommandRequest)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void updateSeverSystemXml(final Element systemXml) throws IOException {
        // getting sub-system to update name
        final String subSystemToUpdateName = ApplicationServer.serverSystemName(systemXml);
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
                    subSystemToUpdateName.equals(ApplicationServer.serverSystemName(xml)) ? systemXml : xml;
            serverConfigurationElement.addContent(serverSystemXml);
        }
        // saving updated configuration
        saveServerXml();
    }

    /**
     * <server-manager>
     * To stop running server and exit the application
     *
     * @throws IOException if it cannot stop and exit
     * @see #manageServerStuff(ServerCommandRequest)
     */
    @Override
    public void stopAndExitServer() throws IOException {
        Stop();
        if (ServerUnit.isUnderJUnit()) {
            Tools.print("===== Under JUnit tests, skipping real application's shutdown =====");
            return;
        }
        // shutting down the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Tools.print("\tShutting down server in 5 secs.");
            try {
                Thread.sleep(5000);
                Tools.print("\t=== Server work is completed ===");
            } catch (InterruptedException e) {
                // doing noting
            }
        }));
        new Thread(() -> Runtime.getRuntime().exit(0)).start();
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
     * <converter>
     * To represent server as an XML element
     *
     * @return server's XML
     * @see Element
     * @see XmlAware#getXML()
     * @see #serverConfigurationElement
     */
    @Override
    public Element getXML() {
        return this.serverConfigurationElement;
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
            // loading server configuration xml-file as Document
            final Document serverConfigurationXmlDocument = prepareXmlDocument(in);
            // to check and prepare loaded server root xml-element
            final Element serverXml = loadAndCheckConfigurationXmlDocument(serverConfigurationXmlDocument);
            // setting up server parts
            setXML(serverXml);
        }
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
     * @see #configure(Element)
     * @see #settingUpBasePart(Element)
     * @see #settingUpMainPart(Element)
     */
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // apply header values to initial document and server's properties
        boolean needToSave = applyServerHeader(xml);
        // load and configure server's sub-systems
        needToSave = configureServerSystems(xml) || needToSave;
        if (needToSave) {
            // saving updated server's configuration
            saveServerXml();
        }
    }

    /**
     * <server-system-builder>
     * To build and configure server sub-system instance from xml-configuration element
     *
     * @param systemElement the server's system xml-configuration element
     * @return ready to use server sub-system instance
     * @throws IOException if there is wrong xml-element structure
     * @throws DataConversionException if there is xml-conversion problem
     * @see SubSystem
     */
    @Override
    public SubSystem buildSubSystem(final Element systemElement) throws IOException, DataConversionException {
        // preparing the sub-system instance
        final String systemName = ApplicationServer.serverSystemName(systemElement);
        Tools.print("Resolving system :" + systemName);
        // get or create server's sub-system instance
        final SubSystem subSystem = getSubSystem(systemName);
        if (subSystem != null) {
            // configuring it by XML
            subSystem.setXML(systemElement);
        }
        return subSystem;
    }

    /**
     * <accessor-builder>
     * To get or build server's sub-system instance by the name of one
     *
     * @param subSystemName  server sub-system name
     * @return got or built clean instance of the server's sub-system
     * @see #createSubSystem(String)
     * @see #buildSubSystem(Element)
     */
    public SubSystem getSubSystem(String subSystemName) {
        return safeServerSystemsAction(() -> system.computeIfAbsent(subSystemName, this::createSubSystem));
    }


    /**
     * <server-system-builder>
     * To build empty server sub-system instance by its name
     *
     * @param systemName server sub-system name
     * @return built clean instance of the server's sub-system
     * @see SubSystem
     */
    @Override
    public SubSystem createSubSystem(String systemName) {
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
        for (final Element systemElement : systems) {
            final SubSystem subSystem = buildSubSystem(systemElement);
            if (subSystem != null) {
                system.put(subSystem.getSystemElementName(), subSystem);
                // connecting the manager with server
                subSystem.getSystemManager().connectTo(this);
            }
            serverConfigurationElement.addContent((Element) systemElement.clone());
        }
        return false;
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
    private boolean applyServerHeader(final Element updatedServerRoot) {
        final Element configuredServerRoot = serverConfigurationDocument.getRootElement().getChild(SERVER_ROOT_ELEMENT_NAME);
        boolean needToSave = false;
        // dealing with date-time format
        final String dateFormat = updatedServerRoot.getAttributeValue(SERVER_DATE_FORMAT_ATTRIBUTE_NAME);
        if (isEmptyString.negate().test(dateFormat)) {
            configuredServerRoot.setAttribute(SERVER_DATE_FORMAT_ATTRIBUTE_NAME, dateFormat);
            dateTimeFormat = new SimpleDateFormat(dateFormat);
            needToSave = true;
        }
        // dealing with RMI properties
        final Element updatedRmiElement = updatedServerRoot.getChild(SERVER_RMI_ELEMENT_NAME);
        if (updatedRmiElement != null) {
            // getting configured RMI root xml-element
            final Element configuredRmiElement = configuredServerRoot.getChild(SERVER_RMI_ELEMENT_NAME);
            // dealing with RMI port value
            needToSave = configureRmiPort(updatedRmiElement, configuredRmiElement, needToSave);
            // dealing with RMI startup value
            needToSave = configureRmiStartup(updatedRmiElement, configuredRmiElement, needToSave);
        }
        return needToSave;
    }

    // configuring the RMI port value
    private boolean configureRmiPort(Element updatedRmiElement, Element configuredRmiElement, boolean needToSave) {
        final String rmiPortValue = updatedRmiElement.getAttributeValue(SERVER_RMI_PORT_ATTRIBUTE_NAME);
        final String defaultRmiPortValue = String.valueOf(SERVER_RMI_PORT_DEFAULT_VALUE);
        if (isEmptyString.negate().test(rmiPortValue) && !Objects.equals(rmiPortValue, defaultRmiPortValue)) {
            try {
                rmi.port = Integer.parseInt(rmiPortValue);
                configuredRmiElement.setAttribute(SERVER_RMI_PORT_ATTRIBUTE_NAME, rmiPortValue);
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
