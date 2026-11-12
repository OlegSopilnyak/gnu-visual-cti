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
import java.util.List;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.core.channel.device.adapter.AbstractDevicesManager;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.EngineUnitBuilder;
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
        final Element tasksElement = new Element(HARDWARE_SUB_SYSTEM);
        systemElement.addContent(new Comment(" Server devices control system "));
        systemElement.addContent(tasksElement);
        tasksElement.addContent(new Comment(" The manager of channel-devices pools "));
        tasksElement.addContent((Element) manager.getXML().clone());
        manager.factories().forEach(factory -> tasksElement.addContent((Element) factory.getXML().clone()));
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
        // getting manager's configuration XML-Element
        final Element managerElement = extractSystemManagerElement(xml);
        // configuring built manager's instance from xml and register it
        manager.configure(managerElement);
        //
        // building the channel-device-factories
        final Element systemElement = managerElement.getParent();
        final List<Element> factoryConfigurationElements = systemElement.getChildren(Factory.XML_ROOT_NAME);
        Tools.out.print(" : device factories .");
        Tools.out.flush();
        for (final Element configuration : factoryConfigurationElements) {
            // preparing devices factory server-unit and attach it to the manager
            final Factory<?, ?> factory = (Factory<?, ?>) EngineUnitBuilder.getInstance().build(configuration);
            // printing the dot after successful factory creation
            Tools.out.print(".");
            Tools.out.flush();
            manager.addDeviceFactory(factory);
        }
        Tools.out.println();
        // all channel-device-factories are configured well reporting
        Tools.out.println("Done!");
        Tools.out.println("Report ===>");
        manager.factories().forEach(factory -> {
            Tools.out.print("Factory of :");
            Tools.out.print(factory.getVendor());
            Tools.out.print(" has devices:");
            Tools.out.print(factory.devices().count());
            Tools.out.println();
        });
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
