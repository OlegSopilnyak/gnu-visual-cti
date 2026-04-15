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
import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;
import org.visualcti.server.core.unit.model.MessageType;
import org.visualcti.server.core.unit.model.ServerConsoleRequest;
import org.visualcti.server.core.unit.model.ServerConsoleResponse;
import org.visualcti.server.core.unit.model.UnitActionError;
import org.visualcti.server.core.unit.model.UnitActionEvent;
import org.visualcti.server.core.unit.model.UnitActionMessage;

public class UnitMessageFactoryAdapterTest {

    UnitMessageFactoryAdapter factory = new UnitMessageFactoryAdapter() {
    };

    @Test
    public void shouldBuildError() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.ERROR;

        // acting
        UnitActionError message = factory.build(messageType, UnitActionError.class);

        // check results
        assertThat(message).isNotNull().isInstanceOf(UnitError.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldBuildEvent() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.EVENT;

        // acting
        UnitActionEvent message = factory.build(messageType, UnitActionEvent.class);

        // check results
        assertThat(message).isNotNull().isInstanceOf(UnitEvent.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldBuildConsoleRequest() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.COMMAND;

        // acting
        ServerConsoleRequest message = factory.build(messageType, ServerConsoleRequest.class);

        // check results
        assertThat(message).isNotNull().isInstanceOf(ConsoleRequest.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldBuildConsoleResponse() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.RESPONSE;

        // acting
        ServerConsoleResponse message = factory.build(messageType, ServerConsoleResponse.class);

        // check results
        assertThat(message).isNotNull().isInstanceOf(ConsoleResponse.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldNotBuildUnknownType() {
        // preparing test data

        // acting
        Exception error = assertThrows(IOException.class, () -> factory.build(MessageType.UNKNOWN, UnitActionMessage.class));

        // check results
        assertThat(error).isNotNull().isInstanceOf(IOException.class);
        assertThat(error.getMessage()).isEqualTo("Unknown message type");
        // check behavior
    }

    @Test
    public void shouldNotBuildNullType() {
        // preparing test data

        // acting
        Exception error = assertThrows(IOException.class, () -> factory.build(null, UnitActionMessage.class));

        // check results
        assertThat(error).isNotNull().isInstanceOf(IOException.class);
        assertThat(error.getMessage()).isEqualTo("Message type is not supported");
        // check behavior
    }

    @Test
    public void shouldNotBuildWrongMessageType() {
        // preparing test data

        // acting
        Exception error = assertThrows(IOException.class, () -> factory.build(MessageType.COMMAND, UnitActionEvent.class));

        // check results
        assertThat(error).isNotNull().isInstanceOf(IOException.class);
        assertThat(error.getMessage()).isEqualTo("ConsoleRequest is not an instance of UnitActionEvent");
        // check behavior
    }


    @Test
    public void shouldBuildErrorTypeOnly() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.ERROR;

        // acting
        UnitActionError message = factory.build(messageType);

        // check results
        assertThat(message).isNotNull().isInstanceOf(UnitError.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldBuildEventTypeOnly() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.EVENT;

        // acting
        UnitActionEvent message = factory.build(messageType);

        // check results
        assertThat(message).isNotNull().isInstanceOf(UnitEvent.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldBuildConsoleRequestTypeOnly() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.COMMAND;

        // acting
        ServerConsoleRequest message = factory.build(messageType);

        // check results
        assertThat(message).isNotNull().isInstanceOf(ConsoleRequest.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldBuildConsoleResponseTypeOnly() throws IOException {
        // preparing test data
        MessageType messageType = MessageType.RESPONSE;

        // acting
        ServerConsoleResponse message = factory.build(messageType);

        // check results
        assertThat(message).isNotNull().isInstanceOf(ConsoleResponse.class);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        // check behavior
    }

    @Test
    public void shouldNotBuildUnknownTypeTypeOnly() {
        // preparing test data

        // acting
        Exception error = assertThrows(IOException.class, () -> factory.build(MessageType.UNKNOWN));

        // check results
        assertThat(error).isNotNull().isInstanceOf(IOException.class);
        assertThat(error.getMessage()).isEqualTo("Unknown message type");
        // check behavior
    }

    @Test
    public void shouldNotBuildNullTypeTypeOnly() {
        // preparing test data

        // acting
        Exception error = assertThrows(IOException.class, () -> factory.build(null));

        // check results
        assertThat(error).isNotNull().isInstanceOf(IOException.class);
        assertThat(error.getMessage()).isEqualTo("Message type is not supported");
        // check behavior
    }
}