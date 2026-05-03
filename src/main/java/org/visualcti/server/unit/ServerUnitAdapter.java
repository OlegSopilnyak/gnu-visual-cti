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

import static org.visualcti.server.core.unit.ServerUnit.Builder.className;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.rmi.registry.Registry;
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
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.part.UnitBasics;
import org.visualcti.server.core.unit.part.UnitMessageExchange;
import org.visualcti.server.core.unit.part.UnitsComposite;
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
    // loaded or built unit XML element
    protected volatile Element unitConfiguration = null;
    // the body unit's Icon Image (GIF | JPEG)
    protected volatile byte[] iconBody = null;
    // the unit's path to the Icon content
    protected volatile String iconBodyPath = null;
    // The path to unit instance in repository
    protected String unitPath = "";
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

    @Deprecated
    @Override
    public Registry localRegistry() {
        return ServerUnit.super.localRegistry();
    }

    @Deprecated
    @Override
    public void dispatch(UnitMessage message) {
        ServerUnit.super.dispatch(message);
    }

    @Deprecated
    @Override
    public void execute(ServerCommandRequest command) throws Exception {
        ServerUnit.super.execute(command);
    }

    @Deprecated
    @Override
    public void respondTo(ServerCommandRequest command, Consumer<ServerCommandResponse> beforeDispatch) throws IOException {
        ServerUnit.super.respondTo(command, beforeDispatch);
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
     *
     * @return the value
     */
    @Override
    public String getPath() {
        return unitPath;
    }

    /**
     * <accessor>
     * To get main class of the unit
     *
     * @return class-implementation of base server unit type
     * @see ServerUnit#getUnitClass()
     * @see #getUnitBuilderClass()
     * @see #prepareUnitClassPart(Element)
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
     * @see ServerUnit#getUnitExtendsClass()
     * @see #prepareUnitClassPart(Element)
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
     * @see #prepareUnitBuilderClassPart(Element, Class)
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
        if (unitConfiguration != null) {
            // unit configuration is actual
            return unitConfiguration;
        }
        final Element element = buildUnitRootElement();
        // preparing XML for base part of the unit
        prepareBaseUnitXML(element);
        // preparing XML for the parameters and other parts of unit
        prepareUnitXML(element);
        return element;
    }

    @Deprecated
    @Override
    public void store(OutputStream out, boolean compact) throws IOException {
        XmlAware.super.store(out, compact);
    }

    @Deprecated
    @Override
    public void store(Element XML, OutputStream out, boolean compact) throws IOException {
        XmlAware.super.store(XML, out, compact);
    }

    @Deprecated
    @Override
    public XMLOutputter documentXmlOutputter() {
        return XmlAware.super.documentXmlOutputter();
    }

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
     * To prepare base parameters of the unit using XML Element and correct xml if it's needed
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #setXML(Element)
     * @see #getUnitBuilderClass()
     * @see #getUnitBuilderMethodName()
     */
    protected void settingUpBasePart(final Element xml) {
        Element builder = xml.getChild(UNIT_BUILDER_ELEMENT_NAME);
        final Class<?> unitBuilderClass = this.getUnitBuilderClass();
        if (builder == null && unitBuilderClass == null) {
            // nothing to check and update
            return;
        }
        // check builder class
        if (unitBuilderClass == null) {
            // removing unit builder xml element from the incoming unit's XML
            xml.removeChild(UNIT_BUILDER_ELEMENT_NAME);
            // nothing to check more
            return;
        }
        //
        // adjusting builder XML element for builder method
        if (builder == null) {
            // creating new builder XML element for current unit
            builder = new Element(UNIT_BUILDER_ELEMENT_NAME)
                    .setAttribute(UNIT_TYPE_PACKAGE, unitBuilderClass.getPackage().getName())
                    .setAttribute(UNIT_TYPE_CLASS, simpleName.apply(unitBuilderClass));
            xml.addContent(builder);
        }
        // check data from incoming unit builder xml
        final String builderClassName = builder == null ? null : className.apply(
                builder.getAttributeValue(UNIT_TYPE_PACKAGE), builder.getAttributeValue(UNIT_TYPE_CLASS)
        );
        // check builder class name
        if (!unitBuilderClass.getName().equals(builderClassName)) {
            // updating unit builder xml-element in the incoming unit's XML
            builder
                    .setAttribute(UNIT_TYPE_PACKAGE, unitBuilderClass.getPackage().getName())
                    .setAttribute(UNIT_TYPE_CLASS, simpleName.apply(unitBuilderClass));
        }
        // check builder method name
        final String unitBuilderMethodName = this.getUnitBuilderMethodName();
        if (isEmpty(unitBuilderMethodName)) {
            // removing unit builder method xml attribute from the builder XML
            builder.removeAttribute(UNIT_BUILDER_METHOD_ATTRIBUTE);
        } else if (!unitBuilderMethodName.equals(builder.getAttributeValue(UNIT_BUILDER_METHOD_ATTRIBUTE))) {
            // updating unit builder method xml attribute in the builder XML
            builder.setAttribute(UNIT_BUILDER_METHOD_ATTRIBUTE, unitBuilderMethodName);
        }
    }

    /**
     * <converter>
     * To prepare main parameters of the unit using XML Element
     *
     * @param xml the XML Element of the unit
     * @see Element
     * @see #setXML(Element)
     * @see #processParameter(ConfigurationParameter)
     * @see #applyUnitParameter(ConfigurationParameter)
     * @see #unitPath
     */
    @SuppressWarnings("unchecked")
    protected void settingUpMainPart(Element xml) {
        // initiating unit path value
        this.unitPath = getName();
        // the container for parsed from XML parameter
        final List<Element> parameters = xml.getChildren(ConfigurationParameter.ELEMENT);
        // processing the parameters of the root unit XML elements
        parameters.stream()
                .map(ConfigurationParameter::of).filter(Objects::nonNull)
                .forEach(this::processParameter);
    }

    /**
     * <converter>
     * <applier>
     * To apply configuration parameter of the server unit
     *
     * @param parameter the unit parameter to apply
     * @see ConfigurationParameter
     * @see #processParameter(ConfigurationParameter)
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
        // thread safe properties map updating
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
            // removing unit's tree branches as well
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
     * <checker>
     * To check is the unit managing by the composite unit (in units tree), or from parent group
     * Just for current version of Mockito
     *
     * @param unit unit to test
     * @return true if group contains the unit
     * @see ServerUnit
     * @see UnitsComposite#isChild(ServerUnit)
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
     * @return true if it's succeeded
     * @see UnitsComposite#add(ServerUnit)
     * @see UnitsComposite#isChild(ServerUnit)
     * @see UnitsComposite#setOwner(ServerUnit)
     * @see #addBranch(ServerUnit)
     */
    @Override
    public boolean add(ServerUnit unit) {
        if (ServerUnit.super.add(unit)) {
            // loaded configuration is not actual after units tree activity
            this.unitConfiguration = null;
            // rebuilding and storing configuration as JDOM element
            this.unitConfiguration = getXML();
            return true;
        } else {
            return false;
        }
    }

    /**
     * <mutator>
     * to add unit to the composite units tree as a branch
     *
     * @param branch the unit to add
     * @see ServerUnit
     * @see ServerUnit#addBranch(ServerUnit)
     */
    @Override
    public void addBranch(ServerUnit branch) {
        branches.add(branch);
    }

    /**
     * <mutator>
     * to remove child from the server unit composite units tree
     *
     * @param unit the unit to remove
     * @return true if it's succeeded
     * @see ServerUnit
     * @see UnitsComposite#remove(ServerUnit)
     * @see UnitsComposite#isChild(ServerUnit)
     * @see UnitsComposite#setOwner(ServerUnit)
     * @see #removeBranch(ServerUnit)
     */
    @Override
    public boolean remove(ServerUnit unit) {
        if (ServerUnit.super.remove(unit)) {
            // loaded configuration is not actual after units tree activity
            this.unitConfiguration = null;
            // rebuilding and storing configuration as JDOM element
            this.unitConfiguration = getXML();
            return true;
        } else {
            return false;
        }
    }

    /**
     * <mutator>
     * to remove the branch from the composite units tree
     *
     * @param branch the unit to remove from composite tree
     * @see ServerUnit
     * @see ServerUnit#removeBranch(ServerUnit)
     */
    @Override
    public void removeBranch(ServerUnit branch) {
        branches.remove(branch);
    }

    /**
     * <mutator>
     * To remove all units from the composite units tree
     * Just for current version of Mockito
     *
     * @see #children()
     * @see #remove(ServerUnit)
     * @see UnitsComposite#removeAll()
     */
    @Deprecated
    @Override
    public boolean removeAll() {
        return ServerUnit.super.removeAll();
    }

    /**
     * <accessor>
     * To get access to the list of group's units as Stream
     *
     * @return the stream to the units list managed by composite
     * @see Stream
     * @see ServerUnit
     * @see UnitsComposite#children()
     */
    @Override
    public Stream<ServerUnit> children() {
        return branches.stream();
    }

    /**
     * <config>
     * To configure the unit, using information from XML Element
     *
     * @param configuration new configuration value of the unit
     * @see Element
     */
    @Override
    public void configure(Element configuration) {
        try {
            setXML(configuration);
            // saving successful configuration to the unit's field
            this.unitConfiguration = configuration;
            // registering unit in the registry
            UnitRegistry.register(this);
        } catch (IOException | DataConversionException e) {
            // delegate exception processing to children
            cannotConfigureBecause(e);
        }
    }

    /**
     * <config>
     * <notify>
     * To notify system about broken unit configuration
     *
     * @param e the cause of malfunction
     */
    protected void cannotConfigureBecause(Exception e) {
        // ignore exception on this level
    }

    // private methods
    // building server unit main classes part

    /**
     * @see #buildUnitRootElement()
     */
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

    /**
     * @see #buildUnitRootElement()
     */
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

    /**
     * @see #setOwner(ServerUnit)
     */
    private void updateChildrenUnitOwner(ServerUnit owner) throws IOException {
        // assign new value of the unit-owner
        this.owner = owner;
        // iterating tree's branches, changing the unit-owner
        for (final ServerUnit branch : branches) {
            branch.setOwner(this);
        }
    }

    //to process the xml configuration parameter of the unit

    /**
     * @see #settingUpMainPart(Element)
     * @see #iconBodyPath
     * @see #applyUnitParameter(ConfigurationParameter)
     */
    private void processParameter(ConfigurationParameter parameter) {
        if (isUnitIconParameter.test(parameter)) {
            // found icon parameter
            final String iconResourcePath = parameter.getValue();
            this.iconBodyPath = iconResourcePath;
            loadingIconFrom(iconResourcePath);
        } else {
            // process another parameter
            applyUnitParameter(parameter);
        }
    }

    // to load icon body from the path in the classloader

    /**
     * @see #processParameter(ConfigurationParameter)
     */
    private void loadingIconFrom(final String path) {
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
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
    }
}
