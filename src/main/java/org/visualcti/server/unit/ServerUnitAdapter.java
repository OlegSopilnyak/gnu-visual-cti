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
package org.visualcti.server.unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.Text;
import org.visualcti.server.core.XmlAware;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.event.model.UnitMessages;

/**
 * <singleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Basic Implementation: Smallest atomic(indivisible) part of the Application Server(server-unit)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.02
 * @see ServerUnit
 * @see XmlAware
 */
public class ServerUnitAdapter implements ServerUnit, XmlAware {
    public static final String UNIT_TYPE_PACKAGE = "package";
    public static final String UNIT_TYPE_CLASS = "class";
    public static final String UNIT_BUILDER_ELEMENT_NAME = "parent";
    public static final String UNIT_BUILDER_METHOD_ATTRIBUTE = "method";
    public static final String UNIT_PARAMETER_ELEMENT_NAME = "parameter";
    public static final String UNIT_ICON_ATTRIBUTE = "icon";
    // the body unit's Icon Image (GIF | JPEG)
    protected byte[] iconBody = null;
    // the unit's path to the Icon content
    protected String iconBodyPath = null;
    // the type of the unit
    protected String unitType = "";
    // The name of the unit
    protected String unitName = "";
    // The path to unit instance in repository
    protected String unitPath = "";
    // The current state of the unit
    protected UnitState unitState = UnitState.PASSIVE;
    // The to the owner of this unit
    protected ServerUnit owner;
    private final Collection<ServerUnit> branches = new ArrayList<>();
    // the factory of server action messages
    protected UnitMessageFactory actionMessageFactory = UnitMessages.factorySingleton();

    /**
     * <accessor>
     * To get the body unit's Icon Image (GIF | JPEG)
     */
    @Override
    public byte[] getIcon() {
        return iconBody;
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, services tree, etc.)
     */
    @Override
    public String getType() {
        return unitType;
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     */
    @Override
    public String getName() {
        return unitName;
    }

    /**
     * <accessor>
     * To get Path to unit instance in repository
     */
    @Override
    public String getPath() {
        return unitPath;
    }

    /**
     * <accessor>
     * To get Current state of unit (active/passive/broken)
     */
    @Override
    public UnitState getUnitState() {
        return unitState;
    }

    /**
     * <accessor>
     * To get root element of the unit
     *
     * @see #getXML()
     */
    protected String getRootElementName() {
        return "AbstractServerUnit";
    }

    /**
     * <accessor>
     * To get description of the unit
     *
     * @see #getXML()
     */
    protected String getUnitDescription() {
        return "Adapter of server unit";
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     */
    @Override
    public Element getXML() {
        final Element element = new Element(getRootElementName());
        adjustRoot(element).addContent(baseXML());
        prepareUnitXML(element);
        return element;
    }

    /**
     * <accessor>
     * To get main class of the unit
     *
     * @see #adjustRoot(Element)
     */
    protected Class<? extends ServerUnit> getUnitClass() {
        return ServerUnitAdapter.class;
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @see #adjustRoot(Element)
     */
    protected Class<? extends ServerUnit> getParentUnitClass() {
        return ServerUnit.class;
    }

    /**
     * <converter>
     * To adjust parameters of root XML element of the unit
     *
     * @param rootElement of the unit
     * @see #getXML()
     */
    protected Element adjustRoot(Element rootElement) {
        final Class<? extends ServerUnit> unitClass = getUnitClass();
        final Class<? extends ServerUnit> parentUnitClass = getParentUnitClass();
        final String unitPackage = unitClass.getPackage().getName();
        final String parentPackage = parentUnitClass.getPackage().getName();
        rootElement.setAttribute(UNIT_TYPE_PACKAGE, unitPackage).setAttribute(UNIT_TYPE_CLASS, unitClass.getSimpleName());
        if (!unitClass.equals(parentUnitClass)) {
            rootElement.setAttribute("extends",
                    unitPackage.equals(parentPackage) ? parentUnitClass.getSimpleName() : parentUnitClass.getName()
            );
        }
        return rootElement;
    }

    /**
     * <accessor>
     * To get class-builder of the unit instance
     *
     * @see #baseXML()
     */
    protected Class<?> getUnitBuilderClass() {
        return getUnitClass();
    }

    /**
     * <accessor>
     * To get the method name in class-builder to build the unit instance
     *
     * @see #baseXML()
     */
    protected String getUnitBuilderMethodName() {
        return null;
    }

    /**
     * <converter>
     * To represent base parameters of unit as an XML element
     *
     * @return entity's XML
     * @see Element
     */
    protected Element baseXML() {
        final Element element = new Element(UNIT_BUILDER_ELEMENT_NAME);
        final String description = getUnitDescription();
        if (description != null && !description.isEmpty()) {
            element.addContent(new Text(description));
        }
        final Class<?> builderClass = getUnitBuilderClass();
        element
                .setAttribute(UNIT_TYPE_PACKAGE, builderClass.getPackage().getName())
                .setAttribute(UNIT_TYPE_CLASS, builderClass.getSimpleName());
        final String builderMethodName = getUnitBuilderMethodName();
        if (builderMethodName != null && !builderMethodName.trim().isEmpty()) {
            element.setAttribute(UNIT_BUILDER_METHOD_ATTRIBUTE, builderMethodName);
        }
        return element;
    }

    /**
     * <converter>
     * To represent the parameters of unit as an XML element
     *
     * @param rootElement  building from unit XML Element
     * @see Element
     */
    protected void prepareUnitXML(Element rootElement) {
        if (!isEmpty(iconBodyPath)) {
            rootElement.addContent(
                    new Element(UNIT_PARAMETER_ELEMENT_NAME).setAttribute(UNIT_ICON_ATTRIBUTE, iconBodyPath)
            );
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
     */
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // preparing base part of the server unit
        prepareBasePart(xml);
        // prepare main part of the server unit
        prepareMainPart(xml);
        // prepare properties part of the server unit
        preparePropertiesPart(xml);
    }

    /**
     * <converter>
     * To prepare base parameters of the unit using XML Element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #baseXML()
     */
    protected void prepareBasePart(Element xml) {
        // doing nothing because unit already created
    }

    /**
     * <converter>
     * To prepare main parameters of the unit using XML Element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #prepareUnitXML(Element)
     */
    @SuppressWarnings("unchecked")
    protected void prepareMainPart(Element xml) {
        final List<Element> parameters = xml.getChildren(UNIT_PARAMETER_ELEMENT_NAME);
        for (Element parameter : parameters) {
            if(parameter.getAttribute(UNIT_ICON_ATTRIBUTE) != null) {
                // found icon parameter
                iconBodyPath = parameter.getAttributeValue(UNIT_ICON_ATTRIBUTE);
                loadIconBodyFrom(iconBodyPath);
            } else {
                // process rest parameters
                applyUnitParameter(parameter);
            }
        }
    }

    /**
     * <converter>
     * To apply parameter of the unit using XML Element
     *
     * @param parameter the XML Element of the unit parameter
     * @see Element
     * @see #prepareMainPart(Element)
     */
    protected void applyUnitParameter(Element parameter) {
        // doing nothing here we're restoring only icon parameter
    }

    /**
     * <converter>
     * To prepare properties of the unit using XML Element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #prepareMainPart(Element)
     */
    private void preparePropertiesPart(Element xml) {
        // doing nothing because unit already created
    }

    /**
     * <config>
     * To configure the unit, using information from XML Element
     *
     * @param configuration new configuration of the unit
     * @see Element
     * @see #setXML(Element)
     * @see UnitActionError
     * @see UnitMessageFactory#build(MessageType)
     * @see #getMessageFactory()
     */
    @Override
    public void configure(Element configuration) {
        try {
            setXML(configuration);
        } catch (IOException | DataConversionException e) {
            unitState = UnitState.BROKEN;
            try {
                final UnitActionError error = getMessageFactory().build(MessageType.ERROR);
                dispatch(error.setNestedException(e).setDescription("Cannot restore server unit " + getName()));
            } catch (IOException ex) {
                // doing nothing, server unit is already in broken state
            }
        }
    }

    /**
     * <accessor>
     * To get ServerUnit instance properties
     * may use for visual editing in GUI
     *
     * @return server unit properties
     */
    @Override
    public Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

    /**
     * <mutator>
     * To assign properties to ServerUnit instance
     * Properties may be changed in GUI
     *
     * @param properties server unit properties
     */
    @Override
    public void setProperties(Map<String, Object> properties) {

    }

    /**
     * <accessor>
     * To get reference to messages factory
     *
     * @return not null reference to the factory
     */
    @Override
    public UnitMessageFactory getMessageFactory() {
        return actionMessageFactory;
    }

    /**
     * <accessor>
     * To get access to the owner of this unit (null for root unit)
     */
    @Override
    public ServerUnit getOwner() {
        return owner;
    }

    /**
     * <mutator>
     * To set new owner of this unit (null for the root unit)
     *
     * @param owner the owner of the unit
     */
    @Override
    public void setOwner(ServerUnit owner) {
        this.owner = owner;
    }

    /**
     * <mutator>
     * to add unit to the composite units tree as a branch
     *
     * @param branch the unit to add as a branch
     * @see ServerUnit
     * @see #add(ServerUnit)
     */
    @Override
    public void addBranch(ServerUnit branch) {
        branches.add(branch);
    }

    /**
     * <mutator>
     * to remove the branch from the composite units tree
     *
     * @param branch the unit to remove from composite tree
     * @see ServerUnit
     * @see #remove(ServerUnit)
     */
    @Override
    public void removeBranch(ServerUnit branch) {
        branches.remove(branch);
    }

    /**
     * <accessor>
     * To get access to the list of group's units as Stream
     *
     * @return the stream to the units list managed by composite
     * @see Stream
     * @see ServerUnit
     */
    @Override
    public Stream<ServerUnit> children() {
        return branches.stream();
    }

    // private methods
    // to load icon body from the path in the classloader
    private void loadIconBodyFrom(String path) {
        try(InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in != null) {
                final int bodySize = in.available();
                iconBody = new byte[bodySize];
                final int read = in.read(iconBody);
                assert read == bodySize : "Read data different of excepted";
            }
        } catch (IOException e) {
            // do nothing in case of error
        }
    }

}
