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
package org.visualcti.server.event.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.model.MessageFamilyType;
import org.visualcti.server.core.unit.model.MessageType;
import org.visualcti.server.core.unit.model.ServerConsoleResponse;

public class ConsoleRequestTest {
    @Test
    public void shouldSerializeRequest() throws IOException {
        // preparing test data
        ConsoleRequest request = new ConsoleRequest();
        request
                .setDone(false).setSuccess(false).setNeedResponse(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(request.getMessageType()).isEqualTo(MessageType.COMMAND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);

        // acting
        out.writeObject(request);
        out.flush();
        out.close();

        // check results
        byte[] bytes = stream.toByteArray();
        assertThat(bytes).isNotNull().isNotEmpty();
        // check behavior
    }

    @Test
    public void shouldDeserializeRequest() throws IOException, ClassNotFoundException {
        // preparing test data
        ConsoleRequest request = new ConsoleRequest();
        request
                .setDone(false).setSuccess(false).setNeedResponse(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(request.getMessageType()).isEqualTo(MessageType.COMMAND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(request);
        out.flush();
        out.close();

        // acting
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
        ConsoleRequest deserialized = (ConsoleRequest) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(request);
        assertThat(deserialized.getMessageType()).isEqualTo(MessageType.COMMAND);
        // check behavior
    }

    @Test
    public void shouldDeserializeEmptyRequest() throws IOException, ClassNotFoundException {
        // preparing test data
        ConsoleRequest request = new ConsoleRequest();
        assertThat(request.getMessageType()).isEqualTo(MessageType.COMMAND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(request);
        out.flush();
        out.close();

        // acting
        ByteArrayInputStream rawStream = new ByteArrayInputStream(stream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(rawStream);
        ConsoleRequest deserialized = (ConsoleRequest) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(request);
        assertThat(deserialized.getMessageType()).isEqualTo(MessageType.COMMAND);
        // check behavior
    }

    @Test
    public void shouldGetXML() {
        // preparing test data
        ConsoleRequest request = new ConsoleRequest();
        request
                .setDone(false).setSuccess(false).setNeedResponse(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(request.getMessageType()).isEqualTo(MessageType.COMMAND);

        // acting
        Element xml = request.getXML();

        // check results
        String requestName = xml.getName();
        assertThat(requestName).isEqualTo("unit-action-message");
        // check behavior
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        ConsoleRequest request = new ConsoleRequest();
        request
                .setDone(false).setSuccess(false).setNeedResponse(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(request.getMessageType()).isEqualTo(MessageType.COMMAND);
        Element xml = request.getXML();

        // acting
        UnitMessageAdapter request2 = new ConsoleRequest();
        request2.setXML(xml);

        // check results
        assertThat(request2).isEqualTo(request);
        assertThat(request2.getMessageType()).isEqualTo(MessageType.COMMAND);
        // check behavior
    }

    @Test
    public void shouldAssignTheResponse_Success_NoNeedResponse() {
        // preparing test data
        boolean success = true;
        ServerConsoleResponse response = mock(ServerConsoleResponse.class);
        doReturn(success).when(response).isCommandSuccess();
        ConsoleRequest request = spy(new ConsoleRequest());
        doCallRealMethod().when(request).assignResponse(any(ServerConsoleResponse.class));

        // acting
        request.assignResponse(response);

        // check results
        // check behavior
        verify(request).setSuccess(success);
        verify(request, never()).setDone(anyBoolean());
    }

    @Test
    public void shouldAssignTheResponse_Success_NeedResponse() {
        // preparing test data
        boolean success = true;
        Lock lock = spy(new ReentrantLock());
        ServerConsoleResponse response = mock(ServerConsoleResponse.class);
        doReturn(success).when(response).isCommandSuccess();
        doReturn(Stream.empty()).when(response).getParameters();
        ConsoleRequest request = spy(new ConsoleRequest());
        request
                .setDone(false).setSuccess(false).setNeedResponse(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        request.setNeedResponse(true);
        request.setLock(lock);

        // acting
        lock.lock();
        request.assignResponse(response);

        // check results
        Parameter in = request.getParameters(Parameter.INPUT_DIRECTION).findAny().orElseThrow(AssertionError::new);
        assertThat(in.getName()).isEqualTo("param1");
        assertThat(in.getValue()).isEqualTo("value1");
        assertThat(request.getParameters(Parameter.OUTPUT_DIRECTION).findAny()).isEmpty();
        // check behavior
        verify(response).getParameters();
        verify(request).setSuccess(success);
        verify(request).setDone(true);
        verify(request).getLock();
        verify(lock).unlock();
    }

    @Test
    public void shouldAssignTheResponse_NotSuccess_NoNeedResponse() {
        // preparing test data
        boolean success = false;
        ServerConsoleResponse response = mock(ServerConsoleResponse.class);
        doReturn(success).when(response).isCommandSuccess();
        ConsoleRequest request = spy(new ConsoleRequest());
        doCallRealMethod().when(request).assignResponse(any(ServerConsoleResponse.class));

        // acting
        request.assignResponse(response);

        // check results
        // check behavior
        verify(request).setSuccess(success);
        verify(request, never()).setDone(anyBoolean());
    }

    @Test
    public void shouldAssignTheResponse_NotSuccess_NeedResponse() throws Exception {
        // preparing test data
        boolean success = false;
        Lock lock = spy(new ReentrantLock());
        ServerConsoleResponse response = mock(ServerConsoleResponse.class);
        String errorMessage = "<<<error>>> !";
        doReturn(errorMessage).when(response).getDescription();
        doReturn(success).when(response).isCommandSuccess();
        doReturn(Stream.empty()).when(response).getParameters();
        ConsoleRequest request = spy(new ConsoleRequest());
        request
                .setDone(false).setSuccess(false).setNeedResponse(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        request.setNeedResponse(true);
        request.setLock(lock);
        reset(request);

        // acting
        lock.lock();
        request.assignResponse(response);

        // check results
        Parameter in = request.getParameters(Parameter.INPUT_DIRECTION).findAny().orElseThrow(AssertionError::new);
        assertThat(in.getName()).isEqualTo("param1");
        assertThat(in.getValue()).isEqualTo("value1");
        Parameter out = request.getParameters(Parameter.OUTPUT_DIRECTION).findAny().orElseThrow(AssertionError::new);
        assertThat(out.getName()).isEqualTo("@error");
        assertThat(out.getValue()).isEqualTo(errorMessage);
        // check behavior
        verify(response, never()).getParameters();
        ArgumentCaptor<Parameter> captor = ArgumentCaptor.forClass(Parameter.class);
        verify(request).setParameter(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("@error");
        assertThat(captor.getValue().getStringValue()).isEqualTo(errorMessage);
        verify(request).setSuccess(success);
        verify(request).setDone(true);
        verify(request).getLock();
        verify(lock).unlock();
    }
}
