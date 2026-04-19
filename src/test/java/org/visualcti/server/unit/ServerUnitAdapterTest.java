package org.visualcti.server.unit;

import static org.assertj.core.api.Assertions.assertThat;

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
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter(){
            @Override
            protected Class<? extends ServerUnit> getParentUnitClass() {
                return ServerUnit.class;
            }
        };
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
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter(){
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
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapter(){
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
}
