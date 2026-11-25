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
package org.visualcti.server.system.manager;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jdom.Comment;
import org.jdom.Element;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.service.Service;
import org.visualcti.server.service.ServicesManager;
import org.visualcti.server.service.adapter.AbstractServicesManager;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * <SystemManager>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Singleton:Manager: The manager of all Server's Services</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractServicesManager
 * @see SubSystem.Manager
 */
@SuppressWarnings("unchecked")
public class ServerServicesManager extends AbstractServicesManager implements SubSystem.Manager {
    private static final String SERVICES_MANAGER_UNIT_NAME = "Server's Services Manager";
    // is unit's path is current te unit's name
    private static final Predicate<String> isCurrentUnitName =
            path -> Objects.equals(path, SERVICES_MANAGER_UNIT_NAME);
    private static ServerServicesManager instance;
    private SubSystem subSystem;

    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return ServerServicesManager.class;
    }

    @Override
    public Class<? extends ServerUnit> getUnitExtendsClass() {
        return ServicesManager.class;
    }

    @Override
    public String getUnitBuilderMethodName() {
        return "getInstance";
    }

    @Override
    public SubSystem getSystem() {
        return subSystem;
    }

    public void setSystem(SubSystem subSystem) {
        this.subSystem = subSystem;
        this.unitPath = subSystem == null ? getName() : defaultManagerUnitPath();
    }

    /**
     * <accessor>
     * To get the unit path fot the manager by default
     *
     * @return fixed path as master service (the owner of any service in the server)
     * @see SubSystem.Manager#defaultManagerUnitPath()
     * @see #setSystem(SubSystem)
     * @see #beforeRegisterUnit()
     * @see #close()
     */
    @Override
    public String defaultManagerUnitPath() {
        return "Service";
    }

    @Override
    public String getName() {
        return SERVICES_MANAGER_UNIT_NAME;
    }

    @Override
    protected String getUnitDescription() {
        return " The manager of the server's services ";
    }

    @Override
    public String getRootElementName() {
        return SubSystem.MANAGER_ROOT_ELEMENT;
    }

    /**
     * <builder>
     * To get access to the Tasks Manager instance as a singleton
     *
     * @return the singleton of the manager
     */
    public static ServerServicesManager getInstance() {
        if (instance == null) {
            synchronized (ServerServicesManager.class) {
                if (instance == null) {
                    instance = new ServerServicesManager();
                    instance.unitPath = instance.getName();
                }
            }
        }
        return instance;
    }

    /**
     * <action>
     * To reset the unit's path value to default one<BR>
     * Used only for tests purposes
     *
     * @see #unitPath
     * @see #getName()
     */
    public void resetUnitPath() {
        this.unitPath = getName();
    }

    /**
     * <action>
     * To do something before register unit in the units registry<BR>
     * updating the unit path value by default unit path of the manager if it needs
     *
     * @see #unitPath
     * @see #defaultManagerUnitPath()
     */
    @Override
    protected void beforeRegisterUnit() {
        if (isEmptyString.or(isCurrentUnitName).test(unitPath)) {
            // updating the unit path value by default manager path
            unitPath = defaultManagerUnitPath();
        }
    }

    @Override
    public void close() {
        this.unitPath = defaultManagerUnitPath();
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see ServerUnitAdapter#getXML()
     * @see Service#XML_ROOT_NAME
     */
    @Override
    public Element getXML() {
        return new Element(Service.XML_ROOT_NAME)
                .addContent(new Comment(getUnitDescription()))
                .addContent(super.getXML());
    }

    /**
     * <converter>
     * To build parameters of the root XML element of the unit (for unit building)
     *
     * @return built the root unit's XML element
     * @see #getXML()
     */
    @Override
    protected Element buildUnitRootElement() {
        final Element rootElement = super.buildUnitRootElement();
        final List<Object> content = rootElement.getContent();
        final Predicate<Object> isComment = Comment.class::isInstance;
        // removing description comment from root element
        final List<Object> detachedContent = content.stream()
                // ignoring comments
                .filter(isComment.negate())
                // only elements will be there
                .filter(Element.class::isInstance).map(Element.class::cast).map(Element::clone)
                // new content's list (only elements)
                .collect(Collectors.toList());
        return rootElement.setContent(detachedContent).setAttribute("Prominic", "signed");
    }

    @Override
    protected void settingUpBasePart(Element xml) {
        // doing nothing, keeps original XML
        // Here we don't use builder stuff of the server unit
    }

    @Override
    protected boolean updatedUnitConfiguration() {
        // keeping loaded configuration Element as is after changed branches
        return true;
    }

    /// private methods
    private ServerServicesManager() {
    }
}
