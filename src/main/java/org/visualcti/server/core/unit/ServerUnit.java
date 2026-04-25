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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.server.core.unit.part.UnitBasics;
import org.visualcti.server.core.unit.part.UnitMessageExchange;
import org.visualcti.server.core.unit.part.UnitsComposite;
import org.visualcti.util.Tools;

/**
 * <singleton>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Smallest atomic(indivisible) part of the Application Server(server-unit)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.02
 * @see UnitBasics
 * @see UnitsComposite
 * @see UnitMessageExchange
 */
public interface ServerUnit extends UnitMessageExchange, UnitsComposite, UnitBasics {
    String UNIT_TYPE_PACKAGE = "package";
    String UNIT_TYPE_CLASS = "class";
    String UNIT_TYPE_EXTENDS_CLASS = "extends";
    String UNIT_BUILDER_ELEMENT_NAME = "builder";
    String UNIT_BUILDER_METHOD_ATTRIBUTE = "method";
    // predicate to test is string empty
    Predicate<String> isEmptyString = string -> string == null || string.trim().isEmpty();

    /**
     * <accessor>
     * To get local RMI registry to share access to server objects
     *
     * @return RMI registry instance
     * @see Registry
     */
    default Registry localRegistry() {
        try {
            return getOwner().localRegistry();
        } catch (NullPointerException npe) {
            return null;
        }
    }

//////////////// ACTIONS PART (begin) ///////////////////
    /**
     * <dispatcher>
     * To dispatch event, error, or command response from the unit
     * This method will be called inside the activity of unit.
     * Should override for root unit
     *
     * @param message action message to dispatch
     * @see UnitMessageExchange#dispatch(UnitMessage)
     * @see UnitMessage
     * @see #getOwner()
     */
    @Override
    default void dispatch(UnitMessage message) {
        try {
            getOwner().dispatch(message);
        } catch (NullPointerException npe) {
            Tools.error("Warning! Lost message in " + getPath() + " msg:" + message);
        }
    }

    /**
     * <executer>
     * To execute command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     * @see UnitMessageExchange#execute(ServerCommandRequest)
     * @see ServerCommandRequest
     * @see MessageFamilyType#GET
     * @see Parameter
     * @see Parameter#INPUT_DIRECTION
     * @see org.visualcti.server.core.unit.message.UnitMessageFactory
     * @see #getMessageFactory()
     * @see org.visualcti.server.core.unit.message.UnitMessageFactory#build(MessageType)
     * @see MessageType#RESPONSE
     * @see ServerCommandResponse
     * @see UnitMetaData#transferTo(ServerCommandResponse)
     * @see #dispatch(UnitMessage)
     */
    @Override
    default void execute(ServerCommandRequest command) throws Exception {
        if (MessageFamilyType.GET == command.getFamilyType() && command.isNeedResponse()) {
            // getting parameter with name "target" from the executing command
            final Optional<Parameter> target = command.getParameter("target", Parameter.INPUT_DIRECTION);
            if (target.isPresent() && "meta".equals(target.get().getValue())) {
                // command asks to get the metadata of the unit
                final ServerCommandResponse response = getMessageFactory().build(MessageType.RESPONSE);
                UnitMetaData.of(this).transferTo(response);
                // dispatching the response to the command request
                dispatch(response.setCommandSuccess(true));
                return;
            }
        }
        throw new UnknownCommandException(command.getFamilyType() + " isn't supported!");
    }
//////////////// ACTIONS PART (end) ///////////////////

/////////// SERVER UNIT HIERARCHY PART (begin) ////////////////////
    /**
     * <mutator>
     * to add child to the composite units tree<BR/>
     * set up the owner for the child unit this unit
     *
     * @param child the unit to add
     * @see UnitsComposite#add(ServerUnit)
     * @see ServerUnit#setOwner(ServerUnit)
     * @see #addBranch(ServerUnit)
     */
    @Override
    default void add(ServerUnit child) {
        if (child != null && child.getOwner() != this) {
            try {
                // attaching child to units registry
                child.setOwner(this);
                // adding child to unit's tree
                addBranch(child);
            } catch (IOException e) {
                e.printStackTrace(Tools.err);
            }
        }
    }

    /**
     * <mutator>
     * to remove child from the composite units tree
     *
     * @param child the unit to remove
     * @see ServerUnit
     * @see #removeBranch(ServerUnit)
     * @see #setOwner(ServerUnit)
     */
    @Override
    default void remove(ServerUnit child) {
        if (child.getOwner() == this) {
            try {
                // detaching child unit from units registry
                child.setOwner(null);
                // removing child from unit's tree
                removeBranch(child);
            } catch (IOException e) {
                e.printStackTrace(Tools.err);
            }
        }
    }
/////////// SERVER UNIT HIERARCHY PART (end) ////////////////////

///////////// PROPERTIES PART (begin) //////////////
    /**
     * <config>
     * To configure the unit, using information from XML Element
     *
     * @param configuration new configuration value of the unit
     * @see Element
     */
    void configure(Element configuration);

    /**
     * <accessor>
     * To get ServerUnit instance properties
     * may use for visual editing in GUI
     *
     * @return server unit properties
     */
    Map<String, Object> getProperties();

    /**
     * <mutator>
     * To assign properties to ServerUnit instance
     * Properties may be changed in GUI
     *
     * @param properties server unit properties
     */
    void setProperties(Map<String, Object> properties);
///////////// PROPERTIES PART (end) //////////////

///////////// UNIT BUILDER PART (begin) //////////////
/// BASIC UNIT CLASSES (begin) methods ///
    /**
     * <accessor>
     * To get main class of the unit
     */
    default Class<? extends ServerUnit> getUnitClass() {
        throw new UnsupportedOperationException("Not supported yet. Should be override in child.");
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @see #getUnitClass()
     */
    default Class<? extends ServerUnit> getUnitExtendsClass() {
        return ServerUnit.class;
    }
/// BASIC UNIT CLASSES (end) methods ///
///
/// UNIT BUILDER CLASSES (begin) methods ///
    /**
     * <accessor>
     * To get class-builder of the unit instance
     */
    default Class<?> getUnitBuilderClass() {
        return null;
    }

    /**
     * <accessor>
     * To get the method name in class-builder to build the unit instance
     *
     * @see #getUnitBuilderClass()
     */
    default String getUnitBuilderMethodName() {
        return null;
    }
/// UNIT BUILDER CLASSES (end) methods ///
///
///////////// UNIT BUILDER PART (end) //////////////

    /**
     * <builder>
     * Function: The builder of the instance of the server unit, unsing XML Element from scratch
     *
     * @param <T> the type of built server unit
     */
    interface Builder<T extends ServerUnit> {
        // function to calculate canonical java class name
        BiFunction<String, String, String> className = (packageName, className) ->
                className.contains(".") ? className : packageName + "." + className;
        /**
         * <builder method>
         * To build the instance of server unit from XML Element
         *
         * @param configuration XML configuration of the server unit
         * @return built server unit instance
         */
        @SuppressWarnings("unchecked")
        default T build(Element configuration) throws IOException {
            final String unitPackage = configuration.getAttributeValue(UNIT_TYPE_PACKAGE);
            final String unitClassName = className.apply(unitPackage, configuration.getAttributeValue(UNIT_TYPE_CLASS));
            final String unitExtendsClassName =
                    className.apply(unitPackage, configuration.getAttributeValue(UNIT_TYPE_EXTENDS_CLASS));
            try {
                final ClassLoader unitClassloader = ServerUnit.class.getClassLoader();
                // to make the class extends server unit
                final Class<?> unitExtendsClass = Class.forName(unitExtendsClassName, true, unitClassloader);
                // to make the class of server unit
                final Class<T> unitClass = (Class<T>) Class.forName(unitClassName, true, unitClassloader);
                // to check is class extends declared correct
                if (!unitExtendsClass.isAssignableFrom(unitClass)) {
                    // wrong declared inheritance
                    throw new IOException("Class " + unitExtendsClassName + " is not extending " + unitClassName);
                }
                // creating the instance of server unit
                final Element unitBuilderXML = getBuilderElement(configuration);
                final T unit = unitBuilderXML == null ? unitClass.newInstance() : build(unitBuilderXML, unitExtendsClass);
                // configuring created server unit instance
                unit.configure(configuration);
                // returns built and configured server unit
                return unit;
            } catch (ClassNotFoundException e) {
                throw new IOException("Class not found", e);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new IOException("Cannot make unit instance", e);
            }
        }

        @SuppressWarnings("unchecked")
        default T build(Element xml, Class<?> extendsClass) throws
                ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException,
                InvocationTargetException, IOException
        {
            final String builderClassName =
                    className.apply(xml.getAttributeValue(UNIT_TYPE_PACKAGE), xml.getAttributeValue(UNIT_TYPE_CLASS));
            // creating the class of the builder
            final Class<T> builderClass = (Class<T>) Class.forName(builderClassName, true, extendsClass.getClassLoader());
            // build-instance-method name
            final String builderMethod = xml.getAttributeValue(UNIT_BUILDER_METHOD_ATTRIBUTE);
            final T unit;
            if (isEmptyString.test(builderMethod)) {
                // just create
                unit = builderClass.newInstance();
            } else {
                final Method theMethod = builderClass.getMethod(builderMethod);
                unit = (T) theMethod.invoke(builderClass);
            }
            if (extendsClass.isInstance(unit)) {
                return unit;
            }
            throw new IOException("Built unit is not an instance of " + extendsClass.getName());
        }
    }

    static Element getBuilderElement(Element configuration) {
        final Element element = configuration.getChild(UNIT_BUILDER_ELEMENT_NAME);
        // for backward compatibility of old sever XML format
        return element != null ? element : configuration.getChild("parent");
    }
}
