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
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;

public class UnitMessageAdapterTest {
    @Test
    public void shouldSerializeMessage() throws IOException {
        // preparing test data
        UnitMessageAdapter adapter = new AdapterImpl();
        adapter.setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);

        // acting
        out.writeObject(adapter);
        out.flush();
        out.close();

        // check results
        byte[] bytes = stream.toByteArray();
        assertThat(bytes).isNotNull().isNotEmpty();
        // check behavior
    }

    @Test
    public void shouldDeserializeMessage() throws IOException, ClassNotFoundException {
        // preparing test data
        UnitMessageAdapter adapter = new AdapterImpl();
        adapter.setMessageType(MessageType.ERROR)
                .setDescription("description-2").setDate(new Date().getTime()).setUnitPath("unit-path-2")
        ;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(adapter);
        out.flush();
        out.close();

        // acting
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
        UnitMessageAdapter deserialized = (UnitMessageAdapter) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(adapter);
        // check behavior
    }

    @Test
    public void shouldDeserializeEmptyMessage() throws IOException, ClassNotFoundException {
        // preparing test data
        UnitMessageAdapter adapter = new AdapterImpl();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(adapter);
        out.flush();
        out.close();

        // acting
        ByteArrayInputStream rawStream = new ByteArrayInputStream(stream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(rawStream);
        UnitMessageAdapter deserialized = (UnitMessageAdapter) in.readObject();

        // check results
        assertThat(deserialized).isEqualTo(adapter);
        // check behavior
    }

    @Test
    public void shouldGetXML() {
        // preparing test data
        UnitMessageAdapter adapter = new AdapterImpl();
        adapter.setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;

        // acting
        Element xml = adapter.getXML();

        // check results
        String adapterName = xml.getName();
        assertThat(adapterName).isEqualTo("unit-action-message");
        // check behavior
    }

    @Test
    public void shouldSetXML() throws IOException, DataConversionException {
        // preparing test data
        UnitMessageAdapter adapter = new AdapterImpl();
        adapter.setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        Element xml = adapter.getXML();

        // acting
        UnitMessageAdapter adapter2 = new AdapterImpl();
        adapter2.setXML(xml);

        // check results
        assertThat(adapter2).isEqualTo(adapter);
        // check behavior
    }

    // private classes
    private static class AdapterImpl extends UnitMessageAdapter {
        @Override
        public MessageType getMessageType() {
            return MessageType.UNKNOWN;
        }

        @Override
        public UnitMessage setMessageType(MessageType messageType) {
            return this;
        }
    }
}
