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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jdom.Element;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
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
 * @see Closeable
 */
public interface ServerUnit extends UnitMessageExchange, UnitsComposite, UnitBasics, Closeable {
    String UNIT_TYPE_PACKAGE = "package";
    String UNIT_TYPE_CLASS = "class";
    String UNIT_TYPE_EXTENDS_CLASS = "extends";
    String UNIT_BUILDER_ELEMENT_NAME = "builder";
    String UNIT_BUILDER_METHOD_ATTRIBUTE = "method";
    // commands constants
    String COMMAND_TYPE_PARAMETER = "type";
    String COMMAND_TARGET_PARAMETER = "target";
    String COMMAND_FAILED_REASON = "reason";
    // command's target for execute by default
    String GET_UNIT_META_TARGET = "meta";
    // predicate to test is string empty
    Predicate<String> isEmptyString = string -> string == null || string.trim().isEmpty();
    // the consumer for command's response before dispatch successful result in successfulResponseTo(...)
    Consumer<ServerCommandResponse> COMMAND_NOT_NEEDED_RESPONSE = response -> {
    };

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
            Tools.error("Warning! Lost message in unit [" + getPath() + "] message:" + message);
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
        // checking command to execute
        validateCommand(command);
        // processing the command with response
        if (command.getFamilyType() == MessageFamilyType.GET) {
            // getting parameter with name "target" from the executing command
            if (GET_UNIT_META_TARGET.equals(targetValueOf(command))) {
                // target is "meta" responding to it
                successfulResponseTo(command, response -> UnitMetaData.of(this).transferTo(response));
            } else {
                // command's target isn't "meta"
                final String reason = command.getFamilyType() + " isn't supported! Wrong GET's request target parameter";
                throw new UnknownCommandException(reason);
            }
        } else {
            throw new UnknownCommandException(command.getFamilyType() + " isn't supported!");
        }
    }

    /**
     * <responser>
     * To do action before dispatching the command response
     *
     * @param command        command which requires response
     * @param beforeDispatch to do something before response dispatching
     * @throws IOException if something went wrong
     * @see ServerCommandRequest
     * @see Consumer
     * @see ServerCommandResponse
     * @see #respondTo(ServerCommandRequest, boolean, Consumer)
     */
    default void successfulResponseTo(ServerCommandRequest command, Consumer<ServerCommandResponse> beforeDispatch) throws IOException {
        respondTo(command, true, beforeDispatch);
    }

    /**
     * <responser>
     * Prepare and send failed response to the command request with thrown exception
     *
     * @param command   the response for
     * @param reason    why it was wrong
     * @param exception the exception
     * @throws IOException if it can't send the response to the command request
     * @see #dispatchError(Throwable, String)
     * @see ServerCommandResponse#setParameter(Parameter)
     * @see Parameter#of(String, Object)
     * @see #respondTo(ServerCommandRequest, boolean, Consumer)
     */
    default void failedResponseTo(ServerCommandRequest command, String reason, Exception exception) throws IOException {
        // dispatch detected exception message
        dispatchError(exception, reason);
        // prepare command response consumer
        final Consumer<ServerCommandResponse> failedResponseConsumer = response ->
                response.setParameter(Parameter.of(COMMAND_FAILED_REASON, reason).output());
        // send failed response to the command
        respondTo(command, false, failedResponseConsumer);
    }

    /**
     * <responser>
     * To do action before dispatching the command response
     *
     * @param command        command which requires response
     * @param commandSuccess command execution result
     * @param beforeDispatch to do something before response dispatching
     * @throws IOException if something went wrong
     * @see ServerCommandRequest
     * @see Consumer#accept(Object)
     * @see ServerCommandResponse#setCommandSuccess(boolean)
     * @see #getMessageFactory()
     * @see UnitMessageFactory#responseTo(ServerCommandRequest)
     * @see #dispatch(UnitMessage)
     */
    default void respondTo(ServerCommandRequest command,
                           boolean commandSuccess,
                           Consumer<ServerCommandResponse> beforeDispatch
    ) throws IOException {
        if (command.isNeedResponse()) {
            // creating the response to the command
            final ServerCommandResponse response = getMessageFactory().responseTo(command);
            // to do action before response dispatch
            beforeDispatch.accept(response);
            // dispatching the response to the command request
            dispatch(response.setCommandSuccess(commandSuccess));
        }
    }

    /**
     * <action>
     * To create and dispatch the event-type message from the unit
     *
     * @param eventType   type of the event
     * @param description the description of the event
     * @see UnitActionEvent
     * @see #getMessageFactory()
     * @see UnitMessageFactory#buildFor(ServerUnit, MessageType, MessageFamilyType, String)
     * @see #dispatch(UnitMessage)
     */
    default void dispatchEvent(MessageFamilyType eventType, String description) {
        try {
            final UnitActionEvent event =
                    getMessageFactory().buildFor(
                            this,
                            MessageType.EVENT,
                            eventType == null ? MessageFamilyType.STATE : eventType,
                            description);
            dispatch(event);
        } catch (IOException ex) {
            // doing nothing, server unit is already in broken state
        }
    }

    /**
     * <action>
     * To create and dispatch the event-type (state) message from the unit
     *
     * @param description the description of the event
     * @see #dispatchEvent(MessageFamilyType, String)
     * @see MessageFamilyType#STATE
     */
    default void dispatchEvent(String description) {
        dispatchEvent(MessageFamilyType.STATE, description);
    }

    /**
     * <action>
     * To create and dispatch the error-type message from the unit
     *
     * @param exception   the cause of the error
     * @param description the description of the error
     * @see UnitActionError
     * @see UnitActionError#setNestedException(Throwable)
     * @see #getMessageFactory()
     * @see UnitMessageFactory#buildFor(ServerUnit, MessageType, MessageFamilyType, String)
     * @see #dispatch(UnitMessage)
     */
    default void dispatchError(Throwable exception, String description) {
        try {
            final UnitActionError error = getMessageFactory()
                    .buildFor(this, MessageType.ERROR, MessageFamilyType.ERROR, description);
            if (exception != null) {
                error.setNestedException(exception);
            }
            dispatch(error);
        } catch (IOException ex) {
            // doing nothing, server unit is already in broken state
        }
    }

    /**
     * <action>
     * To create and dispatch the error-type message from the unit
     *
     * @param description the description of the error
     * @see #dispatchError(Throwable, String)
     */
    default void dispatchError(String description) {
        dispatchError(null, description);
    }
//////////////// ACTIONS PART (end) ///////////////////

/////////// SERVER UNIT HIERARCHY PART (begin) ////////////////////
    /**
     * <mutator>
     * to add child to the server unit composite units tree<BR/>
     * set up the owner for the child unit current unit
     *
     * @param unit the unit to add
     * @return true if it's succeeded
     * @see UnitsComposite#add(ServerUnit)
     * @see UnitsComposite#isChild(ServerUnit)
     * @see UnitsComposite#setOwner(ServerUnit)
     * @see #addBranch(ServerUnit)
     */
    @Override
    default boolean add(ServerUnit unit) {
        if (unit != null && !isChild(unit)) {
            try {
                // attaching child to units registry
                unit.setOwner(this);
                // adding child to unit's tree
                addBranch(unit);
            } catch (IOException e) {
                e.printStackTrace(Tools.err);
            }
            return true;
        }
        return false;
    }

    /**
     * <mutator>
     * to add unit to the server unit composite units tree as a branch
     *
     * @param branch the unit to add as a branch
     * @see ServerUnit
     * @see #add(ServerUnit)
     */
    void addBranch(ServerUnit branch);

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
    default boolean remove(ServerUnit unit) {
        if (isChild(unit)) {
            try {
                // detaching child unit from units composite units tree
                unit.setOwner(null);
                // removing child from unit's tree
                removeBranch(unit);
                // closing detached unit
                unit.close();
            } catch (IOException e) {
                e.printStackTrace(Tools.err);
            }
            return true;
        }
        return false;
    }

    /**
     * <mutator>
     * to remove the branch from the server unit's units tree
     *
     * @param branch the unit to remove from composite tree
     * @see ServerUnit
     * @see #remove(ServerUnit)
     */
    void removeBranch(ServerUnit branch);

    /**
     * <mutator>
     * To set new owner of this composite (null for the root unit)
     *
     * @param owner new value of composite's owner
     * @throws IOException if cannot reregister unit (or children) in units registry
     * @see ServerUnit
     * @see org.visualcti.server.UnitRegistry#register(ServerUnit)
     */
    @Override
    void setOwner(ServerUnit owner) throws IOException;

    /**
     * <accessor>
     * To check is unit needs to be registered in units registry
     *
     * @return true if unit needed registration
     * @see org.visualcti.server.UnitRegistry#register(ServerUnit)
     */
    boolean isNeedRegistration();
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

    /// ////////// PROPERTIES PART (end) //////////////
// to check is method called from JUnit test
    static boolean isUnderJUnit() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(e -> e.getClassName().startsWith("org.junit."));
    }

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
/// ////////// UNIT BUILDER PART (begin) //////////////
    /**
     * <builder>
     * Function: The builder of the instance of the server unit, unsing XML Element from scratch
     *
     * @param <T> the type of built server unit
     */
    interface Builder<T extends ServerUnit> {
        // function to calculate canonical java class name
        BinaryOperator<String> className = (packageName, classname) ->
                classname.contains(".") ? classname : packageName + "." + classname;

        /**
         * <builder method>
         * To build the instance of server unit from XML Element
         *
         * @param configuration XML configuration of the server unit
         * @return built server unit instance
         * @throws IOException if something wrong it te XML-document
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
                final T unit = unitBuilderXML == null
                        ? unitClass.getDeclaredConstructor().newInstance()
                        : build(unitBuilderXML, unitExtendsClass);
                // configuring created server unit instance
                unit.configure(configuration);
                // returns built and configured server unit
                return unit;
            } catch (ClassNotFoundException e) {
                throw new IOException("Class not found", e);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new IOException("Cannot make unit instance", e);
            }
        }

        /**
         * <builder method>
         * To build the instance of server unit using builder XML Element
         *
         * @param xml          builder XML-element
         * @param extendsClass the unit-class should be extended
         * @return built server instance
         * @throws ClassNotFoundException    if something went wrong
         * @throws IllegalAccessException    if something went wrong
         * @throws InstantiationException    if something went wrong
         * @throws NoSuchMethodException     if something went wrong
         * @throws InvocationTargetException if something went wrong
         * @throws IOException               if something went wrong (wrong server's type)
         * @see #build(Element)
         */
        @SuppressWarnings("unchecked")
        default T build(Element xml, Class<?> extendsClass) throws
                ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException,
                InvocationTargetException, IOException {
            final String builderClassName =
                    className.apply(xml.getAttributeValue(UNIT_TYPE_PACKAGE), xml.getAttributeValue(UNIT_TYPE_CLASS));
            // creating the class of the builder
            final Class<T> builderClass = (Class<T>) Class.forName(builderClassName, true, extendsClass.getClassLoader());
            // build-instance-method name
            final String builderMethod = xml.getAttributeValue(UNIT_BUILDER_METHOD_ATTRIBUTE);
            final T unit;
            if (isEmptyString.test(builderMethod)) {
                // just create
                unit = builderClass.getDeclaredConstructor().newInstance();
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

/// ////////// UNIT BUILDER PART (end) //////////////

    /////// private methods
    /**
     * <accessor>
     * To get builder XML-element from unit's configuration XML-element
     *
     * @param configuration unit's configuration XML-element
     * @return unit's builder XML-element
     */
    static Element getBuilderElement(Element configuration) {
        final Element element = configuration.getChild(UNIT_BUILDER_ELEMENT_NAME);
        // for backward compatibility of old sever XML format
        return element != null ? element : configuration.getChild("parent");
    }

    /**
     * <validator>
     * To check the command to execute
     * Is command needs response
     *
     * @param command command to validate
     * @throws UnknownCommandException if command doesn't the response
     * @see ServerCommandRequest#isNeedResponse()
     * @see UnknownCommandException
     */
    static void validateCommand(ServerCommandRequest command) throws UnknownCommandException {
        if (!command.isNeedResponse()) {
            // Asynchronous execution isn't supported yet
            throw new UnknownCommandException("Asynchronous execution isn't supported yet!");
        }
    }

    /**
     * <accessor>
     * getting parameter with name "target" from the executing GET command
     *
     * @param command request for GET command
     * @return the value of target parameter
     * @throws UnknownCommandException if something wrong in target parameter
     * @see ServerCommandRequest#getFamilyType()
     * @see MessageFamilyType#GET
     */
    static String targetValueOf(ServerCommandRequest command) throws UnknownCommandException {
        if (command.getFamilyType() == MessageFamilyType.GET) {
            return command.getParameter(COMMAND_TARGET_PARAMETER, Parameter.INPUT_DIRECTION)
                    .map(parameter -> parameter.getValue("invalid GET target"))
                    .orElseThrow(() -> new UnknownCommandException(command.getFamilyType() + " isn't supported! Wrong GET target"));
        } else {
            throw new UnknownCommandException(command.getFamilyType() + " isn't supported!");
        }
    }

    /**
     * <accessor>
     * getting parameter with name "type" from the executing SET command
     *
     * @param command request for SET command
     * @return the value of target parameter
     * @throws UnknownCommandException if something wrong in target parameter
     * @see ServerCommandRequest#getFamilyType()
     * @see MessageFamilyType#SET
     */
    static String typeValueOf(ServerCommandRequest command) throws UnknownCommandException {
        if (command.getFamilyType() == MessageFamilyType.SET) {
            return command.getParameter(COMMAND_TYPE_PARAMETER, Parameter.INPUT_DIRECTION)
                    .map(parameter -> parameter.getValue("invalid SET type"))
                    .orElseThrow(() -> new UnknownCommandException(command.getFamilyType() + " isn't supported! Wrong SET type"));
        } else {
            throw new UnknownCommandException(command.getFamilyType() + " isn't supported!");
        }
    }

}
