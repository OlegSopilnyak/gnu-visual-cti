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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Test;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;

public class ConsoleResponseTest {
    @Test
    public void shouldSerializeResponse() throws IOException {
        // preparing test data
        CommandResponse response = new CommandResponse();
        response
                .setCommandSuccess(false)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(response.getMessageType()).isEqualTo(MessageType.RESPONSE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);

        // acting
        out.writeObject(response);
        out.flush();
        out.close();

        // check results
        byte[] bytes = stream.toByteArray();
        assertThat(bytes).isNotNull().isNotEmpty();
        // check behavior
    }

    @Test
    public void shouldDeserializeResponse() throws IOException, ClassNotFoundException {
        // preparing test data
        CommandResponse response = new CommandResponse();
        response
                .setCommandSuccess(true)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(response.getMessageType()).isEqualTo(MessageType.RESPONSE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(response);
        out.flush();
        out.close();

        // acting
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
        CommandResponse deserialized = (CommandResponse) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(response);
        assertThat(deserialized.getMessageType()).isEqualTo(MessageType.RESPONSE);
        // check behavior
    }

    @Test
    public void shouldDeserializeEmptyResponse() throws IOException, ClassNotFoundException {
        // preparing test data
        CommandResponse response = new CommandResponse();
        assertThat(response.getMessageType()).isEqualTo(MessageType.RESPONSE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(response);
        out.flush();
        out.close();

        // acting
        ByteArrayInputStream rawStream = new ByteArrayInputStream(stream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(rawStream);
        CommandResponse deserialized = (CommandResponse) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(response);
        assertThat(deserialized.getMessageType()).isEqualTo(MessageType.RESPONSE);
        // check behavior
    }

    @Test
    public void shouldGetXML() {
        // preparing test data
        CommandResponse response = new CommandResponse();
        response
                .setCommandSuccess(true)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(response.getMessageType()).isEqualTo(MessageType.RESPONSE);

        // acting
        Element xml = response.getXML();

        // check results
        String requestName = xml.getName();
        assertThat(requestName).isEqualTo("unit-action-message");
        // check behavior
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        CommandResponse response = new CommandResponse();
        response
                .setCommandSuccess(true)
                .setCorrelationID("correlationID").setLinkName("link-name") .setParameter(new Parameter("param1", "value1"))
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(response.getMessageType()).isEqualTo(MessageType.RESPONSE);
        Element xml = response.getXML();

        // acting
        UnitMessageAdapter response2 = new CommandResponse();
        response2.setXML(xml);

        // check results
        assertThat(response2).isEqualTo(response);
        assertThat(response2.getMessageType()).isEqualTo(MessageType.RESPONSE);
        // check behavior
    }

}