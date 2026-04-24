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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ConfigurationParameter;
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
    // the branches of server units tree
    private final Collection<ServerUnit> branches = new ArrayList<>();
    // the factory of server action messages
    protected final UnitMessageFactory actionMessageFactory = UnitMessages.factorySingleton();

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
    public UnitState currentUnitState() {
        return unitState;
    }

    /**
     * <accessor>
     * To get the name of the root element name in XML result
     *
     * @return the name of root element
     * @see XmlAware#getXML()
     */
    @Override
    public String getRootElementName() {
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
        final Element element = buildUnitRootElement();
        // preparing XML for base part of the unit
        prepareBaseUnitXML(element);
        // preparing XML for the parameters and other parts of unit
        prepareUnitXML(element);
        return element;
    }

    /**
     * <accessor>
     * To get main class of the unit
     *
     * @see #buildUnitRootElement()
     */
    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return ServerUnitAdapter.class;
    }

    /**
     * <converter>
     * To build parameters of root XML element of the unit (for unit building)
     *
     * @see #getXML()
     * @see #getRootElementName()
     */
    protected Element buildUnitRootElement() {
        final Element rootElement = XmlAware.super.getXML();
        //
        // building server unit main classes part
        final Class<? extends ServerUnit> unitClass = prepareUnitClassPart(rootElement);
        //
        // adding unit description as XML comment
        final String description = getUnitDescription();
        // to check the value of the unit's description
        if (isEmptyString.negate().test(description)) {
            rootElement.addContent(new Comment(description));
        }
        //
        // building server unit builder class part
        prepareUnitBuilderClassPart(rootElement, unitClass);
        // returns prepared root element
        return rootElement;
    }

    /**
     * <accessor>
     * To get class-builder of the unit instance
     *
     * @see ServerUnit#getUnitBuilderMethodName()
     */
    @Override
    public Class<?> getUnitBuilderClass() {
        return getUnitClass();
    }

    /**
     * <converter>
     * To represent base parameters of unit as an XML element
     *
     * @see Element
     * @see #getXML()
     */
    protected void prepareBaseUnitXML(Element rootElement) {
        // doing noting by default
    }

    /**
     * <converter>
     * To represent the parameters of unit as an XML element
     *
     * @param rootElement  building from unit XML Element
     * @see Element
     * @see #getXML()
     */
    protected void prepareUnitXML(Element rootElement) {
        if (!isEmpty(iconBodyPath)) {
            rootElement.addContent(ConfigurationParameter.of(UNIT_ICON_ATTRIBUTE, iconBodyPath).getXml());
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
     * @see #setXML(Element)
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
        // the container for parsed from XML parameter
        final List<Element> parameters = xml.getChildren(ConfigurationParameter.ELEMENT);
        parameters.stream().map(ConfigurationParameter::of).filter(Objects::nonNull)
                .forEach(parameter -> {
                    if (UNIT_ICON_ATTRIBUTE.equals(parameter.getName())) {
                        // found icon parameter
                        iconBodyPath = parameter.getValue();
                        loadIconBodyFrom(iconBodyPath);
                    } else {
                        // process another parameter
                        applyUnitParameter(parameter);
                    }
                });
    }

    /**
     * <converter>
     * To apply parameter of the unit using XML Element
     *
     * @param parameter the unit parameter
     * @see ConfigurationParameter
     * @see #prepareMainPart(Element)
     */
    protected void applyUnitParameter(ConfigurationParameter parameter) {
        // doing nothing here because here we're restoring only icon parameter
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
     * @param owner the owner of the unit (or null if unit removed from units tree)
     * @see UnitRegistry#unRegister(ServerUnit)
     * @see UnitRegistry#register(ServerUnit)
     * @see #getName()
     * @see #removeAll()
     * @see #setOwner(ServerUnit)
     */
    @Override
    public void setOwner(ServerUnit owner) throws IOException {
        final String unitName = getName();
        // unregistering unit from the registry
        UnitRegistry.unRegister(this);
        // unit detached from the units  registry
        if (owner == null) {
            // unit kept detached from the registry
            this.owner = null;
            this.unitPath = unitName;
            // removing unit's branches as well
            this.removeAll();
        } else {
            // preparing new value of unit path
            this.unitPath = owner.getPath() + "/" + unitName;
            // registering unit with new value of the path
            UnitRegistry.register(this);
            this.owner = owner;
            // updating unit paths for unit's branches
            for (final ServerUnit branch : branches) {
                branch.setOwner(this);
            }
        }
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
    // building server unit main classes part
    private Class<? extends ServerUnit> prepareUnitClassPart(Element rootElement) {
        final Class<? extends ServerUnit> unitClass = getUnitClass();
        final Class<? extends ServerUnit> parentUnitClass = getUnitExtendsClass();
        final String unitPackage = unitClass.getPackage().getName();
        //
        // attributes in unit's root element (server unit class)
        rootElement
                .setAttribute(UNIT_TYPE_PACKAGE, unitPackage)
                .setAttribute(UNIT_TYPE_CLASS, simpleName(unitClass));
        //
        if (!unitClass.equals(parentUnitClass)) {
            // attributes in unit's root element (server unit extends class)
            final String parentPackage = parentUnitClass.getPackage().getName();
            final String unitExtendsClassName =
                    unitPackage.equals(parentPackage) ? simpleName(parentUnitClass) : parentUnitClass.getName();
            rootElement.setAttribute(UNIT_TYPE_EXTENDS_CLASS, unitExtendsClassName);
        }
        return unitClass;
    }

    // building server unit builder class part
    private void prepareUnitBuilderClassPart(Element rootElement, Class<? extends ServerUnit> unitClass) {
        final Class<?> builderClass = getUnitBuilderClass();
        if (builderClass == null) {
            // in unit class not declared builder class
            return;
        }
        // checking builder stuff before builder XML Element creation
        final String builderMethodName = getUnitBuilderMethodName();
        final boolean isEmptyBuilderMethod = isEmptyString.test(builderMethodName);
        // checking builder class stuff
        if (builderClass.equals(unitClass) && isEmptyBuilderMethod) {
            // builder class the same as unit-class and builder method is empty
        } else if (!isEmptyBuilderMethod) {
            // checking unit builder method
            final Method builderMethod = getPublicMethodInstanceFor(builderClass, builderMethodName);
            // got unit builder method
            if (builderMethod != null && !unitClass.isAssignableFrom(builderMethod.getReturnType())) {
                // builder method return type is not compatible with unit class
                return;
            }
            // preparing separate builder XML Element
            // adding builder element to the root unit element XML
            rootElement.addContent(builderXML(builderClass, builderMethod));
        } else if (unitClass.isAssignableFrom(builderClass)) {
            // builder class is a child of server unit class
            // adding builder element to the root unit XML element
            rootElement.addContent(builderXML(builderClass, null));
        }
    }

    // to make builder's XML Element
    private static Element builderXML(final Class<?> builderClass, final Method builderMethod) {
        // preparing basic builder XML Element
        final Element builderElement = new Element(UNIT_BUILDER_ELEMENT_NAME)
                .setAttribute(UNIT_TYPE_PACKAGE, builderClass.getPackage().getName())
                .setAttribute(UNIT_TYPE_CLASS, simpleName(builderClass));
        if (builderMethod == null) {
            // not declared or wrong name of the method in builder class
        } else {
            builderElement.setAttribute(UNIT_BUILDER_METHOD_ATTRIBUTE, builderMethod.getName());
        }
        return builderElement;
    }

    // getting the public method for the class by name
    private static String simpleName(Class<?> clazz) {
        final String[] parts = clazz.getName().split("\\.");
        return parts[parts.length - 1];
    }
    private static Method getPublicMethodInstanceFor(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            // doing nothing just returns null
            return null;
        }
    }

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
