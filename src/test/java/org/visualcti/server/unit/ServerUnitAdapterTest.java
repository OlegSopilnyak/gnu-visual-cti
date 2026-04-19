package org.visualcti.server.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
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
        String iconPath = "icons/icon_body.png";
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter() {
            {iconBodyPath = iconPath;}
            @Override
            protected String getUnitBuilderMethodName() {
                return "build";
            }
        };

        // acting
        Element element = serverUnitAdapter.getXML();

        // check results
        File out = new File("./unit.xml");
        out.deleteOnExit();
        FileOutputStream output = new FileOutputStream(out);
        serverUnitAdapter.store(output, false);
        output.close();
        List<String> lines = Files.readAllLines(out.toPath());
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).startsWith("<?xml version=");
        assertThat(lines.get(1)).startsWith("<" + serverUnitAdapter.getRootElementName());
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
        assertThat(parameter.getAttributeValue("icon")).isEqualTo(iconPath);
    }
}
