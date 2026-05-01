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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;

public class RunnableUnitAdapterTest {
    RunnableUnitAdapter runnableUnitAdapter = spy(new RunnableUnitAdapterImpl());

    @Test
    public void shouldGetCurrentUnitState() {
        // preparing test data

        // acting
        RunnableServerUnit.UnitState state = runnableUnitAdapter.currentUnitState();

        // check results
        assertThat(state).isNotNull().isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        assertThat(runnableUnitAdapter.isStopped()).isTrue();
    }

    @Test
    public void shouldSetCurrentUnitState() {
        // preparing test data
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);

        // acting
        RunnableServerUnit.UnitState state = runnableUnitAdapter.currentUnitState();

        // check results
        assertThat(state).isNotNull().isSameAs(RunnableServerUnit.UnitState.ACTIVE);
        assertThat(runnableUnitAdapter.isStarted()).isTrue();
    }

    @Test
    public void shouldConfigureServerUnit() throws IOException, DataConversionException {
        // preparing test data
        RunnableUnitAdapter unit = spy(new RunnableUnitAdapterImpl());
        assertThat(unit.unitConfiguration).isNull();
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        Element element = unit.getXML();

        // acting
        unit.configure(element);

        // check the behavior
        verify(unit).setXML(element);
        verify(unit, never()).cannotConfigureBecause(any(Exception.class));
        // check results
        assertThat(unit.unitConfiguration).isSameAs(element);
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
    }

    @Test
    public void shouldNotConfigureServerUnit_SetXmlThrows() throws IOException, DataConversionException {
        // preparing test data
        RunnableUnitAdapter unit = spy(new RunnableUnitAdapterImpl());
        Exception error = new IOException("Don't want to configure");
        doThrow(error).when(unit).setXML(any(Element.class));
        assertThat(unit.unitConfiguration).isNull();
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        Element element = unit.getXML();

        // acting
        unit.configure(element);

        // check the behavior
        verify(unit).setXML(element);
        verify(unit).cannotConfigureBecause(error);
        verify(unit).dispatchError(eq(error), anyString());
        // check results
        assertThat(unit.unitConfiguration).isNull();
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.BROKEN);
        assertThat(unit.isBroken()).isTrue();
    }

    @Test
    public void shouldStartUnit_NoChildren() throws IOException {
        // preparing test data
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
        reset(runnableUnitAdapter);

        // acting
        runnableUnitAdapter.Start();

        // check the behavior
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStarted();
        verify(runnableUnitAdapter).startUnitRunnable();
        verify(runnableUnitAdapter, never()).startUnitChild(any(RunnableServerUnit.class));
        verify(runnableUnitAdapter).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(runnableUnitAdapter).dispatch(any(UnitActionEvent.class));
        // check results
        assertThat(runnableUnitAdapter.isStarted()).isTrue();
    }

    @Test
    public void shouldStartUnit_WithChild() throws IOException {
        // preparing test data
        RunnableUnitAdapter unit = mock(RunnableUnitAdapter.class);
        runnableUnitAdapter.add(unit);
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
        reset(runnableUnitAdapter, unit);

        // acting
        runnableUnitAdapter.Start();

        // check the behavior
        verify(unit).Start();
        verify(unit, never()).currentUnitState(any(RunnableServerUnit.UnitState.class));
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStarted();
        verify(runnableUnitAdapter).startUnitRunnable();
        verify(runnableUnitAdapter).startUnitChild(unit);
        verify(runnableUnitAdapter, never()).dispatchError(any(IOException.class), anyString());
        verify(runnableUnitAdapter).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(runnableUnitAdapter).dispatch(any(UnitActionEvent.class));
        // check results
        assertThat(runnableUnitAdapter.isStarted()).isTrue();
    }

    @Test
    public void shouldStartUnit_WithChildThrows() throws IOException {
        // preparing test data
        RunnableServerUnit unit = mock(RunnableServerUnit.class);
        runnableUnitAdapter.add(unit);
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
        reset(runnableUnitAdapter, unit);
        doThrow(IOException.class).when(unit).Start();

        // acting
        runnableUnitAdapter.Start();

        // check the behavior
        verify(unit).Start();
        verify(unit).currentUnitState(RunnableServerUnit.UnitState.BROKEN);
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStarted();
        verify(runnableUnitAdapter).startUnitRunnable();
        verify(runnableUnitAdapter).startUnitChild(unit);
        verify(runnableUnitAdapter).dispatchError(any(IOException.class), anyString());
        verify(runnableUnitAdapter).currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        verify(runnableUnitAdapter, times(2)).dispatch(any(UnitActionEvent.class));
        // check results
        assertThat(runnableUnitAdapter.isStarted()).isTrue();
    }

    @Test
    public void shouldNotStartUnit_AlreadyStarted() throws IOException {
        // preparing test data
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);

        // acting
        runnableUnitAdapter.Start();

        // check the behavior
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStarted();
        verify(runnableUnitAdapter, never()).startUnitRunnable();
        // check results
    }

    @Test
    public void shouldNotStartUnit_UnitBroken() throws IOException {
        // preparing test data
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.BROKEN);

        // acting
        runnableUnitAdapter.Start();

        // check the behavior
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter, never()).isStarted();
        verify(runnableUnitAdapter, never()).startUnitRunnable();
        // check results
    }

    @Test
    public void shouldStopUnit_NoChildren() throws IOException {
        // preparing test data
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(runnableUnitAdapter);

        // acting
        runnableUnitAdapter.Stop();

        // check the behavior
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStopped();
        verify(runnableUnitAdapter).stopUnitRunnable();
        verify(runnableUnitAdapter, never()).stopUnitChild(any(RunnableServerUnit.class));
        verify(runnableUnitAdapter).currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        verify(runnableUnitAdapter).dispatch(any(UnitActionEvent.class));
        // check results
        assertThat(runnableUnitAdapter.isStopped()).isTrue();
    }

    @Test
    public void shouldStopUnit_WitChild() throws IOException {
        // preparing test data
        RunnableUnitAdapter unit = mock(RunnableUnitAdapter.class);
        runnableUnitAdapter.add(unit);
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(runnableUnitAdapter, unit);

        // acting
        runnableUnitAdapter.Stop();

        // check the behavior
        verify(unit).Stop();
        verify(unit, never()).currentUnitState(any(RunnableServerUnit.UnitState.class));
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStopped();
        verify(runnableUnitAdapter).stopUnitRunnable();
        verify(runnableUnitAdapter).stopUnitChild(unit);
        verify(runnableUnitAdapter).currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        verify(runnableUnitAdapter).dispatch(any(UnitActionEvent.class));
        // check results
        assertThat(runnableUnitAdapter.isStopped()).isTrue();
    }

    @Test
    public void shouldStopUnit_WitChildTrows() throws IOException {
        // preparing test data
        RunnableUnitAdapter unit = mock(RunnableUnitAdapter.class);
        runnableUnitAdapter.add(unit);
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(runnableUnitAdapter, unit);
        doThrow(IOException.class).when(unit).Stop();

        // acting
        runnableUnitAdapter.Stop();

        // check the behavior
        verify(unit).Stop();
        verify(unit).currentUnitState(RunnableServerUnit.UnitState.BROKEN);
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStopped();
        verify(runnableUnitAdapter).stopUnitRunnable();
        verify(runnableUnitAdapter).stopUnitChild(unit);
        verify(runnableUnitAdapter).currentUnitState(RunnableServerUnit.UnitState.PASSIVE);
        verify(runnableUnitAdapter, times(2)).dispatch(any(UnitActionEvent.class));
        // check results
        assertThat(runnableUnitAdapter.isStopped()).isTrue();
    }

    @Test
    public void shouldStopUnit_StillStopped() throws IOException {
        // preparing test data

        // acting
        runnableUnitAdapter.Stop();

        // check the behavior
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter).isStopped();
        verify(runnableUnitAdapter, never()).stopUnitRunnable();
        // check results
    }

    @Test
    public void shouldStopUnit_UnitBroken() throws IOException {
        // preparing test data
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.BROKEN);

        // acting
        runnableUnitAdapter.Stop();

        // check the behavior
        verify(runnableUnitAdapter).isBroken();
        verify(runnableUnitAdapter, never()).isStopped();
        verify(runnableUnitAdapter, never()).stopUnitRunnable();
        // check results
    }

    @Test
    public void shouldGetMessagesListeners_Empty() {
        // preparing test data

        // acting
        Collection<UnitMessage.Listener>listeners = runnableUnitAdapter.listeners();

        // check results
        assertThat(listeners).isEmpty();
    }

    @Test
    public void shouldGetMessagesListeners_NotEmpty() {
        // preparing test data
        Collection<UnitMessage.Listener>listenersBefore = runnableUnitAdapter.listeners();
        assertThat(listenersBefore).isEmpty();
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);

        // acting
        Collection<UnitMessage.Listener>listeners = runnableUnitAdapter.listeners();

        // check results
        assertThat(listeners).isNotEmpty().isNotEqualTo(listenersBefore).contains(listener);
    }

    @Test
    public void shouldAddUnitMessageListener() {
        // preparing test data
        Collection<UnitMessage.Listener>listenersBefore = runnableUnitAdapter.listeners();
        assertThat(listenersBefore).isEmpty();
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);

        // acting
        runnableUnitAdapter.addUnitMessageListener(listener);

        // check results
        assertThat(runnableUnitAdapter.listeners()).isNotEmpty().isNotEqualTo(listenersBefore).contains(listener);
    }

    @Test
    public void shouldRemoveUnitMessageListener() {
        // preparing test data
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);
        assertThat(runnableUnitAdapter.listeners()).contains(listener);

        // acting
        runnableUnitAdapter.removeUnitMessageListener(listener);

        // check results
        assertThat(runnableUnitAdapter.listeners()).isEmpty();
    }

    @Test
    public void shouldDispatchExceptionFor_Exception() {
        // preparing test data
        IOException exception = mock(IOException.class);
        String message = "The error message";

        // acting
        runnableUnitAdapter.dispatchError(exception, message);

        // check the behavior
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(runnableUnitAdapter).dispatch(captor.capture());
        UnitMessage errorMessage = captor.getValue();
        assertThat(errorMessage).isNotNull().isInstanceOf(UnitActionError.class);
        assertThat(errorMessage.getDescription()).isEqualTo(message);
        assertThat(((UnitActionError)errorMessage).getNestedException()).isNotNull();
        // check results
    }

    @Test
    public void shouldDispatchExceptionFor_JustMessage() {
        // preparing test data
        String message = "The error message";

        // acting
        runnableUnitAdapter.dispatchError(message);

        // check the behavior
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(runnableUnitAdapter).dispatch(captor.capture());
        UnitMessage errorMessage = captor.getValue();
        assertThat(errorMessage).isNotNull().isInstanceOf(UnitActionError.class);
        assertThat(errorMessage.getDescription()).isEqualTo(message);
        assertThat(((UnitActionError)errorMessage).getNestedException()).isNull();
        // check results
    }

    @Test
    public void shouldDispatchMessage_NoListeners() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);

        // acting
        runnableUnitAdapter.dispatch(event);

        // check the behavior
        verify(runnableUnitAdapter).getOwner();
        verify(runnableUnitAdapter).handleUnitMessage(event);
        verify(runnableUnitAdapter).listeners();
        verify(runnableUnitAdapter).processUnitMessage(event);
    }

    @Test
    public void shouldDispatchMessage_WithListener() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);

        // acting
        runnableUnitAdapter.dispatch(event);

        // check the behavior
        verify(runnableUnitAdapter).getOwner();
        verify(runnableUnitAdapter).handleUnitMessage(event);
        verify(runnableUnitAdapter, times(2)).listeners();
        verify(runnableUnitAdapter, never()).processUnitMessage(any(UnitMessage.class));
        verify(listener).handleUnitMessage(event);
    }

    @Test
    public void shouldDispatchMessage_ThroughOwner() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);
        ServerUnit owner = mock(ServerUnit.class);
        runnableUnitAdapter.setOwner(owner);

        // acting
        runnableUnitAdapter.dispatch(event);

        // check the behavior
        verify(runnableUnitAdapter, times(2)).getOwner();
        verify(owner).dispatch(event);
        verify(runnableUnitAdapter, never()).handleUnitMessage(any(UnitMessage.class));
    }

    @Test
    public void shouldProcessUnitMessage() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);

        // acting
        runnableUnitAdapter.processUnitMessage(event);

        // check the behavior
        verify(runnableUnitAdapter, never()).currentUnitState();
    }

    @Test
    public void shouldHandleUnitMessage_NoListeners() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);

        // acting
        runnableUnitAdapter.handleUnitMessage(event);

        // check the behavior
        verify(runnableUnitAdapter).listeners();
        verify(runnableUnitAdapter).processUnitMessage(event);
        verify(runnableUnitAdapter, never()).notifyListeners(any(UnitMessage.class));
    }

    @Test
    public void shouldHandleUnitMessage_WithListener() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);

        // acting
        runnableUnitAdapter.handleUnitMessage(event);

        // check the behavior
        verify(runnableUnitAdapter, times(2)).listeners();
        verify(runnableUnitAdapter, never()).processUnitMessage(any(UnitMessage.class));
        verify(runnableUnitAdapter).notifyListeners(event);
        verify(runnableUnitAdapter).notifyListener(listener, event);
        verify(listener).handleUnitMessage(event);
    }

    @Test
    public void shouldNotifyListeners_NoListeners() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);

        // acting
        runnableUnitAdapter.notifyListeners(event);

        // check the behavior
        verify(runnableUnitAdapter).listeners();
        verify(runnableUnitAdapter,never()).notifyListener(any(UnitMessage.Listener.class), any(UnitMessage.class));
    }

    @Test
    public void shouldNotifyListeners_WithListeners() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);

        // acting
        runnableUnitAdapter.notifyListeners(event);

        // check the behavior
        verify(runnableUnitAdapter).listeners();
        verify(runnableUnitAdapter).notifyListener(listener, event);
        verify(listener).handleUnitMessage(event);
    }

    @Test
    public void shouldNotifyListener_HappyDay() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);

        // acting
        runnableUnitAdapter.notifyListener(listener, event);

        // check the behavior
        verify(listener).handleUnitMessage(event);
        verify(runnableUnitAdapter, never()).dispatchError(any(Exception.class), anyString());
    }

    @Test
    public void shouldNotNotifyListener_ListenerThrows() throws IOException {
        // preparing test data
        String message = "The stopped message";
        UnitMessage event = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.EVENT, MessageFamilyType.STOP, message);
        UnitMessage.Listener listener = mock(UnitMessage.Listener.class);
        runnableUnitAdapter.addUnitMessageListener(listener);
        Exception error = new RuntimeException("Don't want to handle this.");
        doThrow(error).when(listener).handleUnitMessage(event);

        // acting
        runnableUnitAdapter.notifyListener(listener, event);

        // check the behavior
        verify(listener).handleUnitMessage(event);
        verify(runnableUnitAdapter).dispatchError(eq(error), anyString());
        verify(runnableUnitAdapter).removeUnitMessageListener(listener);
        // check results
        assertThat(runnableUnitAdapter.listeners()).isEmpty();
    }

    @Test
    public void shouldExecuteStartUnit_NoNeedResponse() throws Exception {
        // preparing test data
        ServerCommandRequest request = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.COMMAND, MessageFamilyType.START, "Starting the unit");
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
        reset(runnableUnitAdapter);

        // acting
        runnableUnitAdapter.execute(request);

        // check the behavior
        verify(runnableUnitAdapter).Start();
        verify(runnableUnitAdapter).respondTo(eq(request), any(Consumer.class));
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(runnableUnitAdapter, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(runnableUnitAdapter.isStarted()).isTrue();
    }

    @Test
    public void shouldExecuteStartUnit_WithResponse() throws Exception {
        // preparing test data
        ServerCommandRequest request = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.COMMAND, MessageFamilyType.START, "Starting the unit");
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
        reset(runnableUnitAdapter);

        // acting
        runnableUnitAdapter.execute(request.setNeedResponse(true));

        // check the behavior
        verify(runnableUnitAdapter).Start();
        verify(runnableUnitAdapter).respondTo(eq(request), any(Consumer.class));
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(runnableUnitAdapter, atLeastOnce()).dispatch(captor.capture());
        List<UnitMessage> unitMessages = captor.getAllValues();
        assertThat(unitMessages).hasSize(2);
        assertThat(unitMessages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(unitMessages.get(1)).isInstanceOf(ServerCommandResponse.class);
        // check results
        assertThat(runnableUnitAdapter.isStarted()).isTrue();
    }

    @Test
    public void shouldNotExecuteStartUnit_WrongCommandType() throws Exception {
        // preparing test data
        ServerCommandRequest request = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.COMMAND, MessageFamilyType.GET, "Starting the unit");
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
        reset(runnableUnitAdapter);

        // acting
        Exception e = assertThrows(Exception.class, () -> runnableUnitAdapter.execute(request));

        // check the behavior
        verify(runnableUnitAdapter, never()).Start();
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("GET isn't supported!");
        assertThat(runnableUnitAdapter.isStarted()).isFalse();
    }

    @Test
    public void shouldExecuteStopUnit_NoNeedResponse() throws Exception {
        // preparing test data
        ServerCommandRequest request = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.COMMAND, MessageFamilyType.STOP, "Stopping the unit");
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(runnableUnitAdapter);

        // acting
        runnableUnitAdapter.execute(request);

        // check the behavior
        verify(runnableUnitAdapter).Stop();
        verify(runnableUnitAdapter).respondTo(eq(request), any(Consumer.class));
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(runnableUnitAdapter, atLeastOnce()).dispatch(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isInstanceOf(UnitActionEvent.class);
        // check results
        assertThat(runnableUnitAdapter.isStopped()).isTrue();
    }

    @Test
    public void shouldExecuteStopUnit_WithResponse() throws Exception {
        // preparing test data
        ServerCommandRequest request = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.COMMAND, MessageFamilyType.STOP, "Stopping the unit");
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(runnableUnitAdapter);

        // acting
        runnableUnitAdapter.execute(request.setNeedResponse(true));

        // check the behavior
        verify(runnableUnitAdapter).Stop();
        verify(runnableUnitAdapter).respondTo(eq(request), any(Consumer.class));
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(runnableUnitAdapter, atLeastOnce()).dispatch(captor.capture());
        List<UnitMessage> unitMessages = captor.getAllValues();
        assertThat(unitMessages).hasSize(2);
        assertThat(unitMessages.get(0)).isInstanceOf(UnitActionEvent.class);
        assertThat(unitMessages.get(1)).isInstanceOf(ServerCommandResponse.class);
        // check results
        assertThat(runnableUnitAdapter.isStopped()).isTrue();
    }

    @Test
    public void shouldNotExecuteStopUnit_WrongCommandType() throws Exception {
        // preparing test data
        ServerCommandRequest request = runnableUnitAdapter.getMessageFactory()
                .buildFor(runnableUnitAdapter, MessageType.COMMAND, MessageFamilyType.GET, "Stopping the unit");
        runnableUnitAdapter.currentUnitState(RunnableServerUnit.UnitState.ACTIVE);
        reset(runnableUnitAdapter);

        // acting
        Exception e = assertThrows(Exception.class, () -> runnableUnitAdapter.execute(request));

        // check the behavior
        verify(runnableUnitAdapter, never()).Stop();
        // check results
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("GET isn't supported!");
        assertThat(runnableUnitAdapter.isStopped()).isFalse();
    }

    // inner classes
    private static class RunnableUnitAdapterImpl extends RunnableUnitAdapter {
        @Override
        public String getType() {
            return "runnable-unit";
        }

        @Override
        public String getName() {
            return "RunnableUnitAdapter";
        }

        @Override
        public void startUnitRunnable() {

        }

        @Override
        public void stopUnitRunnable() {

        }
    }
}
