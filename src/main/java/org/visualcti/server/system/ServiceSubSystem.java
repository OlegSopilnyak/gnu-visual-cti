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

import static org.visualcti.server.core.unit.ServerUnit.UNIT_TYPE_CLASS;
import static org.visualcti.server.core.unit.ServerUnit.UNIT_TYPE_EXTENDS_CLASS;
import static org.visualcti.server.core.unit.ServerUnit.UNIT_TYPE_PACKAGE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.EngineUnitBuilder;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.service.Service;
import org.visualcti.server.system.manager.ServerServicesManager;
import org.visualcti.util.Tools;

/**
 * <System>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * System: The sub-system of the application server which is in charge of the server's services</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see SubSystem
 * @see ServerServicesManager
 */
@SuppressWarnings("unchecked")
public class ServiceSubSystem implements SubSystem {
    // The basics of the services system
    private final Basics base = new Basics();
    // The services registered in the system
    private final List<Service> services = new ArrayList<>();

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
        return SERVICES_SUB_SYSTEM;
    }

    /**
     * <accessor>
     * To get the reference to main sub-system manager unit
     *
     * @return instance of the manager
     * @see ServerServicesManager
     */
    @Override
    public ServerServicesManager getSystemManager() {
        try {
            return UnitRegistry.lookup(ServerServicesManager.class);
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
     * @see SubSystem#getXML()
     * @see #getSystemElementName()
     */
    @Override
    public Element getXML() {
        final Element systemRootXml = SubSystem.super.getXML();
        final Element rootServicesXml = new Element(getSystemElementName());
        systemRootXml.addContent(rootServicesXml);
        // base part of Services System configuration
        basePartFor(rootServicesXml);
        // services part of Services System configuration
        servicesPartFor(rootServicesXml);
//        final ServerServicesManager manager = getSystemManager();
//        rootServicesXml.addContent((Element) manager.getXML().clone());
//        manager.services().forEach(service -> rootServicesXml.addContent((Element) service.getXML().clone()));
        return systemRootXml;
    }

    private void basePartFor(final Element rootServicesXml) {
        rootServicesXml
                .addContent(new Comment("Base components of any service"))
                .addContent(base.getXML());
    }

    private void servicesPartFor(final Element rootServicesXml) {
        services.forEach(service -> rootServicesXml.addContent((Element) service.getXML().clone()));
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
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // getting root services configuration XML-Element
        final Element servicesXml = xml.getChild(SERVICES_SUB_SYSTEM);
        // base part of Services System configuration
        configureBasePart(servicesXml);
        // services part of Services System configuration
        configureServicesPart(servicesXml);
        // finalization
        adjustingSystemManager();
    }

    /// private methods
    private void configureBasePart(final Element servicesXml) throws IOException, DataConversionException {
        // getting base system's configuration XML-Element and update it
        base.setXML(servicesXml.getChild(Basics.BASIC_ROOT_NAME));
        // unregistering all services
        services.forEach(UnitRegistry::unRegister);
        services.clear();
        Tools.out.print(">> " + getSystemElementName() +" : services .");
        Tools.out.flush();
    }

    private void configureServicesPart(final Element servicesXml) throws IOException {
        // building the services from services' configuration XML-Element
        final List<Element> serviceConfigurationElements = servicesXml.getChildren(Service.XML_ROOT_NAME);
        //
        // preparing the engine unit builder
        final EngineUnitBuilder<ServerUnit> unitBuilder = EngineUnitBuilder.getInstance();
        //
        // iterating through the services configurations
        for (final Element configurationXml : serviceConfigurationElements) {
            //
            // preparing server's service server-unit and attach it to the services list
            final Element serviceXml = base.extractEntityFrom(configurationXml, Service.XML_ROOT_NAME);
            // making service using engine unit builder
            final Service service = unitBuilder.build(serviceXml);
            // checking built service instance
            if (service == null || !base.basic.serviceType().isAssignableFrom(service.getClass())) {
                // service is null or has wrong type ¯\_(ツ)_/¯
                throw new IOException("Wrong type of built service " + service);
            }
            //
            // printing the dot after successful service's creation
            Tools.out.print(".");
            Tools.out.flush();
            //
            // adding built and registered service to the services list
            services.add(service);
        }
        // services creation is done
        Tools.out.println("Done!");
        Tools.out.println();
    }

    private void adjustingSystemManager() throws IOException {
        // getting registered services manager
        final ServerServicesManager manager = getSystemManager();
        // checking built services manager instance
        if (manager != null) {
            // the system manager is registered
            if (base.master.serviceType().isAssignableFrom(manager.getClass())) {
                // configuring services manager
                manager.setSystem(this);
                services.stream().filter(service -> service != manager).forEach(manager::add);
                // all services are configured well, reporting about it
                Tools.out.println(manager.getName() + ":Report ===>");
                manager.services().forEach(service -> {
                    Tools.out.print("Service :[");
                    Tools.out.print(service.getName());
                    Tools.out.println("]");
                });
            } else {
                // manager has wrong type ¯\_(ツ)_/¯
                throw new IOException("Wrong type of built services manager");
            }
        } else {
            // manager is not registered yet ¯\_(ツ)_/¯
            throw new IOException("Cannot get services manager");
        }
    }

    // inner classes
    // The basics of the services system
    static class Basics implements XmlAware {
        final static String BASIC_ROOT_NAME = "base";
        final static String MASTER_SERVICE_ROOT_NAME = "ServicesManager";
        final static String BASIC_SERVICE_ROOT_NAME = "Service";
        final ServiceMeta master = new ServiceMeta("org.visualcti.server.service",
                "ServicesManager",
                "Service");
        final ServiceMeta basic = new ServiceMeta("org.visualcti.server.service",
                "Service",
                "org.visualcti.server.core.unit.RunnableServerUnit");

        @Override
        public String getRootElementName() {
            return BASIC_ROOT_NAME;
        }

        @Override
        public Element getXML() {
            final Element rootElement = XmlAware.super.getXML();
            rootElement.addContent(new Comment("This is the main interface of the services manager"));
            rootElement.addContent(masterXml());
            rootElement.addContent(new Comment("This is the main interface of any service"));
            rootElement.addContent(basicXml());
            return rootElement;
        }

        @Override
        public void setXML(final Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
            master.fromXml(xml.getChild(MASTER_SERVICE_ROOT_NAME));
            basic.fromXml(xml.getChild(BASIC_SERVICE_ROOT_NAME));
        }

        boolean isValid() {
            return master.isValid() && basic.isValid() && basic.serviceType().isAssignableFrom(master.serviceType());
        }

        // private methods
        private Element masterXml() {
            final Element rootElement = new Element(MASTER_SERVICE_ROOT_NAME);
            master.toXml(rootElement);
            return rootElement;
        }

        private Element basicXml() {
            final Element rootElement = new Element(BASIC_SERVICE_ROOT_NAME);
            basic.toXml(rootElement);
            return rootElement;
        }
    }

    // The meta-info of the service
    static class ServiceMeta {
        static final String PACKAGE_NAME = UNIT_TYPE_PACKAGE;
        static final String CLASS_NAME = UNIT_TYPE_CLASS;
        static final String INHERITANCE_NAME = UNIT_TYPE_EXTENDS_CLASS;
        String packageValue;
        String classValue;
        String inheritanceValue;

        ServiceMeta(String packageValue, String classValue, String inheritanceValue) {
            this.packageValue = packageValue;
            this.classValue = classValue;
            this.inheritanceValue = inheritanceValue;
        }

        @Override
        public String toString() {
            return "ServiceMeta{" +
                    "packageValue='" + packageValue + '\'' +
                    ", classValue='" + classValue + '\'' +
                    ", inheritanceValue='" + inheritanceValue + '\'' +
                    '}';
        }

        boolean isValid() {
            final Class<?> serviceType = serviceType();
            if (serviceType == null) {
                return false;
            }
            final Class<?> extendsType = extendsType();
            if (extendsType == null) {
                return false;
            }
            return extendsType.isAssignableFrom(serviceType);
        }

        Class<?> serviceType() {
            final String className = packageValue + "." + classValue;
            try {
                return Class.forName(className, true, ServiceSubSystem.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace(Tools.err);
                return null;
            }
        }

        Class<?> extendsType() {
            final String className = inheritanceValue.contains(".") ? inheritanceValue : packageValue + "." + inheritanceValue;
            try {
                return Class.forName(className, true, ServiceSubSystem.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace(Tools.err);
                return null;
            }
        }

        void toXml(final Element rootElement) {
            rootElement
                    .setAttribute(PACKAGE_NAME, packageValue)
                    .setAttribute(CLASS_NAME, classValue)
                    .setAttribute(INHERITANCE_NAME, inheritanceValue);
        }

        void fromXml(final Element rootElement) {
            if (rootElement == null)
                return;
            if (rootElement.getAttributeValue(PACKAGE_NAME) != null) {
                packageValue = rootElement.getAttributeValue(PACKAGE_NAME);
            }
            if (rootElement.getAttributeValue(CLASS_NAME) != null) {
                classValue = rootElement.getAttributeValue(CLASS_NAME);
            }
            if (rootElement.getAttributeValue(INHERITANCE_NAME) != null) {
                inheritanceValue = rootElement.getAttributeValue(INHERITANCE_NAME);
            }
        }
    }
}
