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
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ConfigurationParameter;
import org.visualcti.server.core.XmlAware;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.part.UnitBasics;
import org.visualcti.server.core.unit.part.UnitMessageExchange;
import org.visualcti.server.core.unit.part.UnitsComposite;
import org.visualcti.server.event.model.UnitMessages;
import org.visualcti.util.Tools;

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
public abstract class ServerUnitAdapter implements ServerUnit, XmlAware {
    // the name of icon's attribute
    private static final String UNIT_ICON_ATTRIBUTE = "icon";
    //
    // unit activity execution part
    // testing is parameter unit's icon
    private static final Predicate<ConfigurationParameter> isUnitIconParameter =
            parameter -> UNIT_ICON_ATTRIBUTE.equals(parameter.getName());
    // correct getting the simple name of the class from class.getName()
    private static final Function<Class<?>, String> simpleName = clazz -> {
        final String[] parts = clazz.getName().split("\\.");
        return parts[parts.length - 1];
    };
    // correct getting the public method for the class by name
    private static final BiFunction<Class<?>, String, Method> publicMethodOf = (clazz, methodName) -> {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            // doing nothing just returns null
            return null;
        }
    };
    // making builder's XML Element
    private static final BiFunction<Class<?>, Method, Element> builderXmlOf = (builderType, builderMethod) -> {
        // preparing server unit builder XML Element
        final Element builderElement = new Element(UNIT_BUILDER_ELEMENT_NAME)
                .setAttribute(UNIT_TYPE_PACKAGE, builderType.getPackage().getName())
                .setAttribute(UNIT_TYPE_CLASS, simpleName.apply(builderType));
        return builderMethod != null
                // setting up declared name of the method in builder class
                ? builderElement.setAttribute(UNIT_BUILDER_METHOD_ATTRIBUTE, builderMethod.getName())
                : builderElement
                ;
    };
    //
    // main properties of the unit
    // the body unit's Icon Image (GIF | JPEG)
    protected volatile byte[] iconBody = null;
    // the unit's path to the Icon content
    protected volatile String iconBodyPath = null;
    // The path to unit instance in repository
    protected String unitPath = "";
    // The current state of the unit
    protected UnitState unitState = UnitState.PASSIVE;
    // The to the owner of this unit
    protected ServerUnit owner;
    // the branches of server units tree
    private final Collection<ServerUnit> branches = new ArrayList<>();
    // the factory of server action messages
    private final UnitMessageFactory actionMessageFactory = UnitMessages.factorySingleton();
    // the properties of the unit
    private Map<String, Object> properties = new ConcurrentHashMap<>();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerUnitAdapter)) return false;
        ServerUnitAdapter that = (ServerUnitAdapter) o;
        return Objects.equals(iconBodyPath, that.iconBodyPath)
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getType(), that.getType())
                && Objects.equals(getPath(), that.getPath())
                && Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconBodyPath, getType(), getName(), getPath(), getProperties());
    }

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
     * To get main class of the unit
     *
     * @return class-implementation of base server unit type
     * @see ServerUnit
     * @see ServerUnit#getUnitClass()
     * @see #prepareUnitClassPart(Element)
     * @see #buildUnitRootElement()
     */
    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return ServerUnitAdapter.class;
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @return the instance of class which extends server unit main class
     * @see ServerUnit
     * @see ServerUnit#getUnitExtendsClass()
     * @see #prepareUnitClassPart(Element)
     * @see #buildUnitRootElement()
     * @see #getUnitClass()
     */
    @Override
    public Class<? extends ServerUnit> getUnitExtendsClass() {
        return ServerUnit.super.getUnitExtendsClass();
    }

    /**
     * <accessor>
     * To get class-builder of the unit instance
     *
     * @return the instance of class-builder or null if it isn't used
     * @see ServerUnit#getUnitBuilderClass()
     * @see #prepareUnitBuilderClassPart(Element, Class)
     * @see #buildUnitRootElement()
     */
    @Override
    public Class<?> getUnitBuilderClass() {
        return getUnitClass();
    }

    /**
     * <accessor>
     * To get the method name in class-builder to build the unit instance
     *
     * @return the name of method-builder or null if it isn't used
     * @see ServerUnit#getUnitBuilderMethodName()
     * @see #getUnitBuilderClass()
     * @see #prepareUnitBuilderClassPart(Element, Class)
     * @see #buildUnitRootElement()
     */
    @Override
    public String getUnitBuilderMethodName() {
        return null;
    }

    /**
     * <accessor>
     * To get the name of the root element name in XML result
     *
     * @return the name of root element
     * @see XmlAware#getRootElementName()
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
     * @see #buildUnitRootElement()
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
     * @see XmlAware#getXML()
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
     * <transport>
     * to store entity's content as XML to the OutputStream
     * will be used for transport objects in serialization flow or for store to file
     * Just for current version of Mockito
     *
     * @param out     target output stream
     * @param compact flag which output we are expecting
     * @throws IOException if it cannot write to output stream
     * @see XmlAware#store(OutputStream, boolean)
     */
    @Deprecated
    @Override
    public void store(OutputStream out, boolean compact) throws IOException {
        XmlAware.super.store(out, compact);
    }

    /**
     * <builder>
     * Preparing human-readable XML outputter to an output stream
     * Just for current version of Mockito
     *
     * @return prepared XML outputter
     * @see XMLOutputter
     * @see XmlAware#documentXmlOutputter()
     */
    @Deprecated
    @Override
    public XMLOutputter documentXmlOutputter() {
        return XmlAware.super.documentXmlOutputter();
    }

    /**
     * <builder>
     * Preparing the document from entity's XML
     * Just for current version of Mockito
     *
     * @param xml XML element
     * @return prepared XML Document
     * @see Document
     * @see XmlAware#prepareXmlDocument(Element)
     */
    @Deprecated
    @Override
    public Document prepareXmlDocument(Element xml) {
        return XmlAware.super.prepareXmlDocument(xml);
    }

    /**
     * <converter>
     * To build parameters of root XML element of the unit (for unit building)
     *
     * @return built main unit XML element
     * @see #getXML()
     */
    protected Element buildUnitRootElement() {
        final Element rootElement = createRootElement();
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

    private Element createRootElement() {
        return XmlAware.super.getXML();
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
     * @param rootElement building from unit XML Element
     * @see Element
     * @see #getXML()
     */
    protected void prepareUnitXML(Element rootElement) {
        if (!isEmpty(iconBodyPath)) {
            rootElement.addContent(ConfigurationParameter.of(UNIT_ICON_ATTRIBUTE, iconBodyPath).getXml());
        }
    }

    /**
     * <tester>
     * To check is string empty
     * Just for current version of Mockito
     *
     * @param value string to test
     * @return true if value is empty
     */
    @Deprecated
    @Override
    public boolean isEmpty(String value) {
        return XmlAware.super.isEmpty(value);
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
        // preparing base part (for builder) of the server unit by passed unit's root xml element
        settingUpBasePart(xml);
        // prepare main part of the server unit by passed unit's root xml element
        settingUpMainPart(xml);
        // prepare properties part of the server unit by passed unit's root xml element
        settingUpPropertiesPart(xml);
    }

    /**
     * <converter>
     * To prepare base parameters of the unit using XML Element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #setXML(Element)
     * @deprecated
     */
    @Deprecated
    protected void settingUpBasePart(Element xml) {
        // doing nothing because unit already created
    }

    /**
     * <converter>
     * To prepare main parameters of the unit using XML Element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #setXML(Element)
     * @see #iconBodyPath
     * @see #applyUnitParameter(ConfigurationParameter)
     */
    @SuppressWarnings("unchecked")
    protected void settingUpMainPart(Element xml) {
        // the container for parsed from XML parameter
        final List<Element> parameters = xml.getChildren(ConfigurationParameter.ELEMENT);
        // processing the parameters of the root unit XML elements
        parameters.stream().map(ConfigurationParameter::of).filter(Objects::nonNull).forEach(processUnitConfigurationParameter);
        // check the icon
        Tools.print("Icon Path = " + this.iconBodyPath);
    }

    // to load icon body from the path in the classloader
    private final Consumer<String> loadIconBody = path-> {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in != null) {
                final int bodySize = in.available();
                final byte[] body = new byte[bodySize];
                final int read = in.read(body);
                assert read == bodySize : "Read data different of excepted";
                iconBody = body;
            }
        } catch (IOException e) {
            // do nothing in case of error
        }
    };

    // each unit's xml parameter processor ConfigurationParameter
    private final Consumer<ConfigurationParameter> processUnitConfigurationParameter = parameter -> {
        if (isUnitIconParameter.test(parameter)) {
            // found icon parameter
            final String iconResourcePath = parameter.getValue();
            this.iconBodyPath = iconResourcePath;
            loadIconBody.accept(iconResourcePath);
        } else {
            // process another parameter
            applyUnitParameter(parameter);
        }
    };

    /**
     * <applier>
     * To apply configuration parameter of the server unit
     *
     * @param parameter the unit parameter to apply
     * @see ConfigurationParameter
     * @see #processUnitConfigurationParameter
     * @see #settingUpMainPart(Element)
     */
    protected void applyUnitParameter(ConfigurationParameter parameter) {
        // doing nothing here because here we're restoring only icon parameter
    }

    /**
     * <converter>
     * To prepare properties of the unit using XML Element
     *
     * @param xml the root XML Element of the unit
     * @see Element
     * @see #setXML(Element)
     * @see #setProperties(Map)
     */
    protected void settingUpPropertiesPart(Element xml) {
        // doing nothing because unit already created
    }

    /**
     * <config>
     * To configure the unit, using information from XML Element
     *
     * @param configuration new configuration of the unit
     * @see Element
     * @see ServerUnit#configure(Element)
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
     * may be used for visual editing in GUI
     *
     * @return server unit properties
     */
    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * <mutator>
     * To assign new properties to ServerUnit instance
     * the properties may be changed in GUI
     *
     * @param properties server unit properties
     */
    @Override
    public void setProperties(Map<String, Object> properties) {
        // thread safe properties updating
        this.properties = new ConcurrentHashMap<>(properties);
    }

    /**
     * <accessor>
     * To get reference to messages factory
     *
     * @return not null reference to the factory
     * @see UnitMessageFactory
     * @see UnitMessageExchange#getMessageFactory()
     */
    @Override
    public UnitMessageFactory getMessageFactory() {
        return actionMessageFactory;
    }

    /**
     * <accessor>
     * To get access to the owner of this unit (null for root unit)
     *
     * @return the reference to server unit's owner or null if it isn't exists
     * @see ServerUnit
     * @see UnitsComposite#getOwner()
     */
    @Override
    public ServerUnit getOwner() {
        return owner;
    }

    /**
     * <mutator>
     * To set new owner of this unit (null for the root unit)
     *
     * @param owner the owner of the server unit
     * @see UnitRegistry#unRegister(ServerUnit)
     * @see UnitRegistry#register(ServerUnit)
     * @see UnitBasics#getName()
     * @see UnitsComposite#removeAll()
     * @see UnitsComposite#setOwner(ServerUnit)
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
            updateChildrenUnitOwner(owner);
        }
    }

    /**
     * <mutator>
     * to add unit to the composite units tree as a branch
     *
     * @param branch the unit to add
     * @see ServerUnit
     * @see ServerUnit#add(ServerUnit)
     */
    @Override
    public void addBranch(ServerUnit branch) {
        branches.add(branch);
    }

    /**
     * <checker>
     * To check is the unit managing by the composite unit (in units tree), or from parent group
     * Just for current version of Mockito
     *
     * @param unit unit to test
     * @return true if group contains the unit
     * @see ServerUnit
     */
    @Deprecated
    @Override
    public boolean isChild(ServerUnit unit) {
        return ServerUnit.super.isChild(unit);
    }

    /**
     * <mutator>
     * to add child to the server unit composite units tree<BR/>
     * set up the owner for the child unit current unit
     * Just for current version of Mockito
     *
     * @param unit the unit to add
     * @see UnitsComposite#add(ServerUnit)
     * @see UnitsComposite#isChild(ServerUnit)
     * @see UnitsComposite#setOwner(ServerUnit)
     * @see #addBranch(ServerUnit)
     */
    @Deprecated
    @Override
    public void add(ServerUnit unit) {
        ServerUnit.super.add(unit);
    }

    /**
     * <mutator>
     * To remove all units from the composite units tree
     * Just for current version of Mockito
     *
     * @see #children()
     * @see #remove(ServerUnit)
     */
    @Deprecated
    @Override
    public boolean removeAll() {
        return ServerUnit.super.removeAll();
    }

    /**
     * <mutator>
     * to remove child from the server unit composite units tree
     *
     * @param unit the unit to remove
     * @see ServerUnit
     * @see UnitsComposite#remove(ServerUnit)
     * @see UnitsComposite#isChild(ServerUnit)
     * @see UnitsComposite#setOwner(ServerUnit)
     * @see #removeBranch(ServerUnit)
     */
    @Deprecated
    @Override
    public void remove(ServerUnit unit) {
        ServerUnit.super.remove(unit);
    }

    /**
     * <mutator>
     * to remove the branch from the composite units tree
     *
     * @param branch the unit to remove from composite tree
     * @see ServerUnit
     * @see ServerUnit#remove(ServerUnit)
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
                .setAttribute(UNIT_TYPE_CLASS, simpleName.apply(unitClass));
        //
        if (!unitClass.equals(parentUnitClass)) {
            // attributes in unit's root element (server unit extends class)
            final String parentPackage = parentUnitClass.getPackage().getName();
            final String unitExtendsClassName =
                    unitPackage.equals(parentPackage) ? simpleName.apply(parentUnitClass) : parentUnitClass.getName();
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
            final Method builderMethod = publicMethodOf.apply(builderClass, builderMethodName);
            // got unit builder method
            if (builderMethod != null && !unitClass.isAssignableFrom(builderMethod.getReturnType())) {
                // builder method return type is not compatible with unit class
                return;
            }
            // preparing separate builder XML Element
            // adding builder element to the root unit element XML
            rootElement.addContent(builderXmlOf.apply(builderClass, builderMethod));
        } else if (unitClass.isAssignableFrom(builderClass)) {
            // builder class is a child of server unit class
            // adding builder element to the root unit XML element
            rootElement.addContent(builderXmlOf.apply(builderClass, null));
        }
    }

    // updating unit paths for unit's branches
    private void updateChildrenUnitOwner(ServerUnit owner) throws IOException {
        // assign new value of the unit-owner
        this.owner = owner;
        // iterating tree's branches, changing the unit-owner
        for (final ServerUnit branch : branches) {
            branch.setOwner(this);
        }
    }
}
