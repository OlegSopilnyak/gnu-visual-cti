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

public class UnitMessageAdapterTest {
    @Test
    public void shouldSerializeMessage() throws IOException {
        // preparing test data
        UnitMessageAdapter adapter = new UnitMessageAdapter();
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
        UnitMessageAdapter adapter = new UnitMessageAdapter();
        adapter.setMessageType(MessageType.ERROR)
                .setDescription("description-2").setDate(new Date().getTime()).setUnitPath("unit-path-2")
        ;
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
    public void shouldDeserializeEmptyMessage() throws IOException, ClassNotFoundException {
        // preparing test data
        UnitMessageAdapter adapter = new UnitMessageAdapter();
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
        UnitMessageAdapter adapter = new UnitMessageAdapter();
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
        UnitMessageAdapter adapter = new UnitMessageAdapter();
        adapter.setMessageType(MessageType.EVENT).setFamilyType(MessageFamilyType.ERROR)
                .setDescription("description").setDate(new Date().getTime()).setUnitPath("unit-path")
        ;
        Element xml = adapter.getXML();

        // acting
        UnitMessageAdapter adapter2 = new UnitMessageAdapter();
        adapter2.setXML(xml);

        // check results
        assertThat(adapter2).isEqualTo(adapter);
        // check behavior
    }
}
