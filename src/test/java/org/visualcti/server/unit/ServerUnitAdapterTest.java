package org.visualcti.server.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.ServerUnit;

public class ServerUnitAdapterTest {

    ServerUnitAdapter serverUnitAdapter;

    @Before
    public void setUp() {
        serverUnitAdapter = new ServerUnitAdapter();
    }

    @Test
    public void shouldAdjustServerUnitRootTheSame() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter() {
            @Override
            protected Class<? extends ServerUnit> getParentUnitClass() {
                return ServerUnitAdapter.class;
            }
        };
        Element element = new Element(serverUnitAdapter.getRootElementName());

        // acting
        serverUnitAdapter.adjustRoot(element);

        // check results
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isNull();
    }

    @Test
    public void shouldAdjustServerUnitRootDifferent() {
        // preparing test data
        Element element = new Element(serverUnitAdapter.getRootElementName());

        // acting
        serverUnitAdapter.adjustRoot(element);

        // check results
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
    }

    @Test
    public void shouldUpdateBaseXML_NoBuilderMethod() {
        // preparing test data

        // acting
        Element element = serverUnitAdapter.baseXML();

        // check results
        assertThat(element.getText()).isEqualTo("Adapter of server unit");
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttribute("method")).isNull();
    }

    @Test
    public void shouldUpdateBaseXML_WithBuilderMethod() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter() {
            @Override
            protected String getUnitBuilderMethodName() {
                return "build";
            }
        };

        // acting
        Element element = serverUnitAdapter.baseXML();

        // check results
        assertThat(element.getText()).isEqualTo("Adapter of server unit");
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("method")).isEqualTo("build");
    }

    @Test
    public void shouldUpdateBaseXML_NoDescription() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter() {
            @Override
            protected String getUnitDescription() {
                return "";
            }
        };

        // acting
        Element element = serverUnitAdapter.baseXML();

        // check results
        assertThat(element.getText()).isEmpty();
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttribute("method")).isNull();
    }

    @Test
    public void shouldAdjustUnitRootElement() {
        // preparing test data
        Element element = new Element("unit-root");

        // acting
        assertThat(serverUnitAdapter.adjustRoot(element)).isSameAs(element);

        // check results
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttribute("method")).isNull();
    }

    @Test
    public void shouldGetServerUnitXML() throws IOException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapter() {
            {iconBodyPath = iconPath;}
            @Override
            protected String getUnitBuilderMethodName() {
                return "build";
            }
        };

        // acting
        Element element = unit.getXML();

        // check results
        final BufferedReader reader;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()){
            unit.store(output, false);
            reader = new BufferedReader(new StringReader(output.toString()));
        }
        assertThat(reader).isNotNull();
        List<String> lines = readAllLines(reader);
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).startsWith("<?xml version=");
        assertThat(lines.get(1)).startsWith("<" + unit.getRootElementName());
        // checking content
        assertThat(element).isNotNull();
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        Element parent = element.getChild("parent");
        assertThat(parent).isNotNull();
        assertThat(parent.getText()).isEqualTo("Adapter of server unit");
        assertThat(parent.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(parent.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(parent.getAttributeValue("method")).isEqualTo("build");
        Element parameter = element.getChild("parameter");
        assertThat(parameter).isNotNull();
        Parameter icon = Parameter.of("x","").setXML(parameter);
        assertThat(icon.getName()).isEqualTo("icon");
        assertThat(icon.getValue()).isEqualTo(iconPath);
    }

    @Test
    public void shouldSetServerUnitXML() throws IOException, DataConversionException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapter();
        assertThat(unit.iconBodyPath).isNull();
        Element element = unit.getXML().addContent(Parameter.of("icon", iconPath).getXML());

        // acting
        unit.setXML(element);

        // check results
        assertThat(unit.iconBodyPath).isEqualTo(iconPath);
        assertThat(unit.iconBody).isNotEmpty();
        try(InputStream in = unit.getClass().getClassLoader().getResourceAsStream(unit.iconBodyPath)) {
            assertThat(in).isNotNull();
            byte[] buffer = new byte[in.available()];
            assertThat(in.read(buffer)).isEqualTo(buffer.length);
            assertThat(buffer).isEqualTo(unit.iconBody);
        }
    }

    // private methods
    private List<String> readAllLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        reader.close();
        return lines;
    }
}
