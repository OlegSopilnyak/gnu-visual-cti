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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.util.Tools;

@SuppressWarnings("unchecked")
public class ApplicationServerAdapterTest {

    ApplicationServerAdapter application = spy(new ApplicationServerAdapter());

    @After
    public void tearDown() throws Exception {
        UnitRegistry.clear();
        application.close();
    }

    @Test
    public void shouldInitializeServer() throws Exception {
        // preparing test data

        // acting
        application.initialize();

        // check the behavior
        verify(application).loadServerXml();
        // check results
        assertThat(application.isBroken()).isFalse();
    }

    @Test
    public void shouldNotInitializeServer_RmiRegistryDetected() throws Exception {
        // preparing test data
        application.initialize();
        reset(application);

        // acting
        Throwable e = assertThrows(Throwable.class, () -> application.initialize());

        // check the behavior
        verify(application).loadServerXml();
        // check results
        assertThat(application.isBroken()).isTrue();
        assertThat(e).isInstanceOf(InternalError.class);
        assertThat(e.getMessage()).isEqualTo("Another copy of VisualCTI Server is detected");
        assertThat(e.getCause()).isInstanceOf(ExportException.class);
        assertThat(e.getCause().getMessage()).isEqualTo("internal error: ObjID already in use");
    }

    @Test
    public void shouldGetServerParts() throws Exception {
        // preparing test data
        application.initialize();

        // acting
        Stream<SubSystem>partsStream = application.serverParts();

        // check results
        assertThat(partsStream.count()).isNotZero();
    }

    @Test
    public void shouldLoadServerXml() throws IOException, DataConversionException {
        // preparing test data

        // acting
        application.loadServerXml();

        // check the behavior
        verify(application).prepareXmlDocument(any(InputStream.class));
        // check results
        assertThat(application.serverParts().count()).isNotZero();
    }

    @Test
    public void shouldSaveServerXml() throws IOException, DataConversionException {
        // preparing test data
        String subSystemName = "Tasks";
        Element serverXml = new Element("Server");
        Element tasksXml = new Element(subSystemName);
        Element systemXml = new Element("system").addContent(tasksXml);
        serverXml.addContent(systemXml);
        Element managerConfigXml = new Element("parameter").setAttribute("name", "directory")
                .setAttribute("type", "string").setAttribute("value", "work/tasks");
        tasksXml.addContent(new Element("Manager").addContent(managerConfigXml));
        application.setXML(serverXml);
        File appCongiFile = new File("./conf/VisualCTI.test.server.xml");
        assertThat(appCongiFile).doesNotExist();
        application.setServerConfigFile(appCongiFile);
        reset(application);

        // acting
        application.saveServerXml();

        // check the behavior
        verify(application).store(any(Document.class), any(OutputStream.class));
        // check results
        assertThat(appCongiFile).exists();
        assertThat(appCongiFile.delete()).isTrue();
    }

    @Test
    public void shouldGetListeners() throws Exception {
        // preparing test data
        application.initialize();

        // acting
        Collection<UnitMessage.Listener> listeners = application.listeners();

        // check the behavior
        // check results
        assertThat(listeners).hasSize(1).contains(application);
    }

    @Test
    public void shouldNotifyListener() {
        // preparing test data
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        application.addUnitMessageListener(listener);
        UnitMessage message = mock(UnitMessage.class);

        // acting
        application.notifyListener(listener, message);

        // check the behavior
        verify(listener).handleUnitMessage(message);
    }

    @Test
    public void shouldNotifyListeners() throws Exception {
        // preparing test data
        application.initialize();
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        application.addUnitMessageListener(listener);
        UnitMessage message = mock(UnitMessage.class);

        // acting
        application.notifyListeners(message);

        // check the behavior
        verify(application).notifyListener(listener, message);
        verify(application).processUnitMessage(message);
    }

    @Test
    public void shouldAddUnitMessageListener() {
        // preparing test data
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        assertThat(application.listeners()).isEmpty();

        // acting
        application.addUnitMessageListener(listener);

        // check results
        assertThat(application.listeners()).contains(listener);
    }

    @Test
    public void shouldNotAddUnitMessageListener_Duplication() {
        // preparing test data
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        assertThat(application.listeners()).isEmpty();
        application.addUnitMessageListener(listener);
        reset(application);

        // acting
        application.addUnitMessageListener(listener);

        // check results
        assertThat(application.listeners()).hasSize(1);
    }

    @Test
    public void shouldRemoveUnitMessageListener() {
        // preparing test data
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        application.addUnitMessageListener(listener);
        assertThat(application.listeners()).contains(listener);
        reset(application);

        // acting
        application.removeUnitMessageListener(listener);

        // check results
        assertThat(application.listeners()).isEmpty();
    }

    @Test
    public void shouldHandleUnitMessage() throws Exception {
        // preparing test data
        application.initialize();
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        application.addUnitMessageListener(listener);
        UnitMessage message = mock(UnitMessage.class);

        // acting
        application.handleUnitMessage(message);

        // check the behavior
        verify(application).notifyListeners(message);
        verify(application).processUnitMessage(message);
        verify(application).notifyListener(listener, message);
        verify(listener).handleUnitMessage(message);
        // check results
    }

    @Test
    public void shouldStartServer() throws Exception {
        // preparing test data
        application.initialize();
        assertThat(application.isStarted()).isFalse();
        assertThat(application.isStopped()).isTrue();
        reset(application);

        // acting
        application.Start();

        // check the behavior
        verify(application).isBroken();
        verify(application).isStarted();
        verify(application).canStartUnit();
        verify(application).startUnitRunnable();
        verify(application).runnableChildren();
        verify(application).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(application).getMessageFactory();
        verify(application, atLeastOnce()).dispatch(any(UnitMessage.class));
        // check results
        assertThat(application.isStarted()).isTrue();
        assertThat(application.isStopped()).isFalse();
    }

    @Test
    public void shouldCanStartApplicationServerUnit() throws Exception {
        // preparing test data
        application.initialize();

        // acting
        boolean canStart = application.canStartUnit();

        // check results
        assertThat(canStart).isTrue();
    }

    @Test
    public void shouldCantStartApplicationServerUnit_NoSubSystemToStart() throws Exception {
        // preparing test data
        application.initialize();
        doReturn(Stream.empty()).when(application).serverParts();

        // acting
        boolean canStart = application.canStartUnit();

        // check results
        assertThat(canStart).isFalse();
    }

    @Test
    public void shouldStartServerSystems() throws Exception {
        // preparing test data
        application.initialize();
//        int children = application.serverParts().map(SubSystem::getSystemManager)
//                .map(unit -> unit.runnableChildren().count()).map(Long::intValue)
//                .reduce(0, Integer::sum);
        application.serverParts().forEach(sys -> assertThat(sys.getSystemManager().isStarted()).isFalse());
        reset(application);

        // acting
        application.startUnitRunnable();

        // check the behavior
        verify(application, atLeastOnce()).startUnitChild(any(RunnableServerUnit.class));
        verify(application, atLeastOnce()).dispatch(any(UnitMessage.class));
        // check results
        application.serverParts().forEach(sys -> assertThat(sys.getSystemManager().isStarted()).isTrue());
    }

    @Test
    public void shouldStopServer() throws Exception {
        // preparing test data
        application.initialize();
        application.Start();
        assertThat(application.isStarted()).isTrue();
        assertThat(application.isStopped()).isFalse();
        reset(application);

        // acting
        application.Stop();

        // check the behavior
        verify(application).isBroken();
        verify(application).isStopped();
        verify(application).stopUnitRunnable();
        verify(application).runnableChildren();
        verify(application).currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        verify(application).getMessageFactory();
        verify(application, atLeastOnce()).dispatch(any(UnitMessage.class));
        // check results
        assertThat(application.isStarted()).isFalse();
        assertThat(application.isStopped()).isTrue();
    }

    @Test
    public void shouldStopServerSystems() throws Exception {
        // preparing test data
        application.initialize();
        application.Start();
        application.serverParts().forEach(sys -> assertThat(sys.getSystemManager().isStarted()).isTrue());
        reset(application);

        // acting
        application.stopUnitRunnable();

        // check the behavior
        verify(application, atLeastOnce()).stopUnitChild(any(RunnableServerUnit.class));
        verify(application, atLeastOnce()).dispatch(any(UnitMessage.class));
        // check results
        application.serverParts().forEach(sys -> assertThat(sys.getSystemManager().isStarted()).isFalse());
    }

    @Test
    public void shouldDontDoStartingEngine_FromEngineStart() {
        // preparing test data

        // acting
        Exception e = assertThrows(Exception.class, () -> application.startingEngine());

        // check results
        assertThat(e).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void shouldDontDoStoppingEngine_FromEngineStop() {
        // preparing test data

        // acting
        Exception e = assertThrows(Exception.class, () -> application.stoppingEngine());

        // check results
        assertThat(e).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void shouldExecuteStartCommand() throws Exception {
        // preparing test data
        application.initialize();
        ServerCommandRequest startCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.START, "Start the server");
        reset(application);

        // acting
        application.execute(startCommand);

        // check the behavior
        verify(application).Start();
        verify(application).startUnitRunnable();
        // check results
        assertThat(application.isStarted()).isTrue();
    }

    @Test
    public void shouldExecuteStopCommand() throws Exception {
        // preparing test data
        application.initialize();
        ServerCommandRequest stopCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.STOP, "Stop the server");
        application.Start();
        reset(application);

        // acting
        application.execute(stopCommand);

        // check the behavior
        verify(application).Stop();
        verify(application).stopUnitRunnable();
        // check results
        assertThat(application.isStopped()).isTrue();
    }

    @Test
    public void shouldUpdateSeverSystemXml() throws IOException, DataConversionException {
        // preparing test data
        application.initialize();
        SubSystem tasks = application.serverParts()
                .filter(sys -> sys.getSystemElementName().equals("Tasks")).findFirst()
                .orElse(null);
        assertThat(tasks).isNotNull();
        File appCongiFile = new File("./conf/VisualCTI.test.server.xml");
        assertThat(appCongiFile).doesNotExist();
        application.setServerConfigFile(appCongiFile);
        reset(application);

        // acting
        application.updateSeverSystemXml(tasks.getXML());

        // check the behavior
        verify(application).saveServerXml();
        // check results
        assertThat(appCongiFile).exists();
        assertThat(appCongiFile.delete()).isTrue();
    }

    @Test
    public void shouldExecuteUpdateSystemCommand() throws Exception {
        // preparing test data
        application.initialize();
        SubSystem tasks = application.serverParts()
                .filter(sys -> sys.getSystemElementName().equals("Tasks")).findFirst()
                .orElse(null);
        assertThat(tasks).isNotNull();
        Element tasksXml = tasks.getXML();
        File appCongiFile = new File("./conf/VisualCTI.test.server.xml");
        assertThat(appCongiFile).doesNotExist();
        application.setServerConfigFile(appCongiFile);
        ServerCommandRequest updateSystemCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.SET, "Updating the system of server");
        updateSystemCommand.setNeedResponse(true)
                .setParameter(Parameter.of("type", "update-server-configuration"))
                .setParameter(Parameter.of("system", tasksXml))
        ;
        reset(application);

        // acting
        application.execute(updateSystemCommand);

        // check the behavior
        verify(application).manageServerStuff(updateSystemCommand);
        verify(application).updateSeverSystemXml(tasksXml);
        verify(application).saveServerXml();
        verify(application).successfulResponseTo(eq(updateSystemCommand), any(Consumer.class));
        verify(application).respondTo(eq(updateSystemCommand), eq(true), any(Consumer.class));
        verify(application).getMessageFactory();
        ArgumentCaptor<UnitMessage>  messageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(application).dispatch(messageCaptor.capture());
        // check results
        UnitMessage message = messageCaptor.getValue();
        assertThat(message).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) message;
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getCorrelationID()).isEqualTo(updateSystemCommand.getCorrelationID());
        assertThat(response.getDescription()).isEqualTo(updateSystemCommand.getDescription());
        assertThat(appCongiFile).exists();
        assertThat(appCongiFile.delete()).isTrue();
    }

    @Test
    public void shouldNotExecuteUpdateSystemCommand_WrongRequestTypeParameter() throws Exception {
        // preparing test data
        application.initialize();
        SubSystem tasks = application.serverParts()
                .filter(sys -> sys.getSystemElementName().equals("Tasks")).findFirst()
                .orElse(null);
        assertThat(tasks).isNotNull();
        Element tasksXml = tasks.getXML();
        File appCongiFile = new File("./conf/VisualCTI.test.server.xml");
        assertThat(appCongiFile).doesNotExist();
        application.setServerConfigFile(appCongiFile);
        ServerCommandRequest updateSystemCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.SET, "Updating the system of server");
        String wrongRequestType = "update-server-configuration-";
        updateSystemCommand.setNeedResponse(true)
                .setParameter(Parameter.of("type", wrongRequestType))
                .setParameter(Parameter.of("system", tasksXml))
        ;
        reset(application);

        // acting
        Exception e = assertThrows(Exception.class, () -> application.execute(updateSystemCommand));

        // check the behavior
        verify(application).manageServerStuff(updateSystemCommand);
        verify(application, never()).updateSeverSystemXml(tasksXml);
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).startsWith("Invalid SET's command type").contains(wrongRequestType);
        assertThat(appCongiFile).doesNotExist();
    }

    @Test
    public void shouldNotExecuteUpdateSystemCommand_WrongSystemXmlParameter() throws Exception {
        // preparing test data
        application.initialize();
        SubSystem tasks = application.serverParts()
                .filter(sys -> sys.getSystemElementName().equals("Tasks")).findFirst()
                .orElse(null);
        assertThat(tasks).isNotNull();
        Element tasksXml = tasks.getXML();
        File appCongiFile = new File("./conf/VisualCTI.test.server.xml");
        assertThat(appCongiFile).doesNotExist();
        application.setServerConfigFile(appCongiFile);
        ServerCommandRequest updateSystemCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.SET, "Updating the system of server");
        updateSystemCommand.setNeedResponse(true)
                .setParameter(Parameter.of("type", "update-server-configuration"))
                .setParameter(Parameter.of("system", Tools.emptyXML))
        ;
        reset(application);

        // acting
        application.execute(updateSystemCommand);

        // check the behavior
        verify(application).manageServerStuff(updateSystemCommand);
        verify(application, never()).updateSeverSystemXml(tasksXml);
        ArgumentCaptor<Exception>  exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(application).failedResponseTo(eq(updateSystemCommand),anyString(), exceptionCaptor.capture());
        ArgumentCaptor<UnitMessage>  messageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(application, atLeastOnce()).dispatch(messageCaptor.capture());
        // check results
        Exception e = exceptionCaptor.getValue();
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).startsWith("Expected element with name: system not");
        assertThat(appCongiFile).doesNotExist();
        List<UnitMessage> messageList = messageCaptor.getAllValues();
        assertThat(messageList).hasSize(2);
        UnitMessage message = messageList.get(0);
        assertThat(message.getMessageType()).isSameAs(MessageType.ERROR);
        assertThat(message.getFamilyType()).isSameAs(MessageFamilyType.ERROR);
        assertThat(message.getUnitPath()).isEqualTo("{Server}");
        assertThat(messageList.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messageList.get(1);
        assertThat(response.isCommandSuccess()).isFalse();
        assertThat(response.getMessageType()).isSameAs(MessageType.RESPONSE);
        assertThat(response.getFamilyType()).isSameAs(MessageFamilyType.SET);
        assertThat(response.getUnitPath()).isEqualTo("{Server}");
    }

    @Test
    public void shouldNotExecuteUpdateSystemCommand_WrongSystemXmlParameter2() throws Exception {
        // preparing test data
        application.initialize();
        SubSystem tasks = application.serverParts()
                .filter(sys -> sys.getSystemElementName().equals("Tasks")).findFirst()
                .orElse(null);
        assertThat(tasks).isNotNull();
        Element tasksXml = tasks.getXML();
        File appCongiFile = new File("./conf/VisualCTI.test.server.xml");
        assertThat(appCongiFile).doesNotExist();
        application.setServerConfigFile(appCongiFile);
        ServerCommandRequest updateSystemCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.SET, "Updating the system of server");
        updateSystemCommand.setNeedResponse(true)
                .setParameter(Parameter.of("type", "update-server-configuration"))
                .setParameter(Parameter.of("system", new Element("system")))
        ;
        reset(application);

        // acting
        application.execute(updateSystemCommand);

        // check the behavior
        verify(application).manageServerStuff(updateSystemCommand);
        verify(application, never()).updateSeverSystemXml(tasksXml);
        ArgumentCaptor<Exception>  exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(application).failedResponseTo(eq(updateSystemCommand),anyString(), exceptionCaptor.capture());
        ArgumentCaptor<UnitMessage>  messageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(application, atLeastOnce()).dispatch(messageCaptor.capture());
        // check results
        Exception e = exceptionCaptor.getValue();
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("Expected exactly 1 child element");
        assertThat(appCongiFile).doesNotExist();
        List<UnitMessage> messageList = messageCaptor.getAllValues();
        assertThat(messageList).hasSize(2);
        UnitMessage message = messageList.get(0);
        assertThat(message.getMessageType()).isSameAs(MessageType.ERROR);
        assertThat(message.getFamilyType()).isSameAs(MessageFamilyType.ERROR);
        assertThat(message.getUnitPath()).isEqualTo("{Server}");
        assertThat(messageList.get(1)).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) messageList.get(1);
        assertThat(response.isCommandSuccess()).isFalse();
        assertThat(response.getMessageType()).isSameAs(MessageType.RESPONSE);
        assertThat(response.getFamilyType()).isSameAs(MessageFamilyType.SET);
        assertThat(response.getUnitPath()).isEqualTo("{Server}");
    }

    @Test
    public void shouldExecuteShutdownSystemCommand() throws Exception {
        // preparing test data
        application.initialize();
        ServerCommandRequest shutdownSystemCommand = application.getMessageFactory()
                .buildFor(application, MessageType.COMMAND, MessageFamilyType.SET, "Shutting down the server");
        shutdownSystemCommand.setNeedResponse(true)
                .setParameter(Parameter.of("type", "shutdown-server"))
        ;
        reset(application);

        // acting
        application.execute(shutdownSystemCommand);

        // check the behavior
        verify(application).stopAndExitServer();
        verify(application).Stop();
        verify(application).successfulResponseTo(eq(shutdownSystemCommand), any(Consumer.class));
        verify(application).respondTo(eq(shutdownSystemCommand), eq(true), any(Consumer.class));
        verify(application).getMessageFactory();
        ArgumentCaptor<UnitMessage>  messageCaptor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(application).dispatch(messageCaptor.capture());
        // check results
        UnitMessage message = messageCaptor.getValue();
        assertThat(message).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) message;
        assertThat(response.isCommandSuccess()).isTrue();
        assertThat(response.getCorrelationID()).isEqualTo(shutdownSystemCommand.getCorrelationID());
        assertThat(response.getDescription()).isEqualTo(shutdownSystemCommand.getDescription());
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        String subSystemName = "Tasks";
        Element serverXml = new Element("Server");
        Element tasksXml = new Element(subSystemName);
        Element systemXml = new Element("system").addContent(tasksXml);
        serverXml.addContent(systemXml);
        Element managerConfigXml = new Element("parameter").setAttribute("name", "directory")
                .setAttribute("type", "string").setAttribute("value", "work/tasks");
        tasksXml.addContent(new Element("Manager").addContent(managerConfigXml));

        // acting
        application.setXML(serverXml);

        // check the behavior
        ArgumentCaptor<Element> xmlElementCaptor = ArgumentCaptor.forClass(Element.class);
        verify(application).buildSubSystem(xmlElementCaptor.capture());
        verify(application).getSubSystem(subSystemName);
        verify(application).createSubSystem(subSystemName);
        // check results
        assertThat(xmlElementCaptor.getValue()).isSameAs(systemXml);
        assertThat(application.getSubSystem(subSystemName).getSystemElementName()).isEqualTo(subSystemName);
        assertThat(application.getSubSystem(subSystemName).getSystemManager()).isNotNull();
    }
}