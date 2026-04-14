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
import org.visualcti.server.core.unit.model.MessageFamilyType;
import org.visualcti.server.core.unit.model.MessageType;

public class UnitErrorTest {
    @Test
    public void shouldSerializeError() throws IOException {
        // preparing test data
        UnitError error = new UnitError();
        error.setNestedException(new Exception())
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(error.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(error.getMessageType()).isEqualTo(MessageType.ERROR);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);

        // acting
        out.writeObject(error);
        out.flush();
        out.close();

        // check results
        byte[] bytes = stream.toByteArray();
        assertThat(bytes).isNotNull().isNotEmpty();
        // check behavior
    }

    @Test
    public void shouldDeserializeError() throws IOException, ClassNotFoundException {
        // preparing test data
        UnitError error = new UnitError();
        error.setNestedException(new Exception())
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(error.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(error.getMessageType()).isEqualTo(MessageType.ERROR);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(error);
        out.flush();
        out.close();

        // acting
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
        UnitError deserialized = (UnitError) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(error);
        assertThat(deserialized.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(deserialized.getMessageType()).isEqualTo(MessageType.ERROR);
        // check behavior
    }

    @Test
    public void shouldDeserializeEmptyError() throws IOException, ClassNotFoundException {
        // preparing test data
        UnitError error = new UnitError();
        assertThat(error.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(error.getMessageType()).isEqualTo(MessageType.ERROR);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(error);
        out.flush();
        out.close();

        // acting
        ByteArrayInputStream rawStream = new ByteArrayInputStream(stream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(rawStream);
        UnitError deserialized = (UnitError) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(error);
        assertThat(deserialized.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(deserialized.getMessageType()).isEqualTo(MessageType.ERROR);
        // check behavior
    }

    @Test
    public void shouldGetXML() {
        // preparing test data
        UnitError error = new UnitError();
        error.setNestedException(new Exception())
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.START)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(error.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(error.getMessageType()).isEqualTo(MessageType.ERROR);

        // acting
        Element xml = error.getXML();

        // check results
        String errorName = xml.getName();
        assertThat(errorName).isEqualTo("unit-action-message");
        // check behavior
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        UnitError error = new UnitError();
        error.setNestedException(new Exception())
                .setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        assertThat(error.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(error.getMessageType()).isEqualTo(MessageType.ERROR);
        Element xml = error.getXML();

        // acting
        UnitMessageAdapter error2 = new UnitError();
        error2.setXML(xml);

        // check results
        assertThat(error2).isEqualTo(error);
        assertThat(error2.getFamilyType()).isEqualTo(MessageFamilyType.ERROR);
        assertThat(error2.getMessageType()).isEqualTo(MessageType.ERROR);
        // check behavior
    }
}
