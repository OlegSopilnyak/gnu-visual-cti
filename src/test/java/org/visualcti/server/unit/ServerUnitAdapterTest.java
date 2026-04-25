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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.core.ConfigurationParameter;
import org.visualcti.server.core.unit.ServerUnit;

public class ServerUnitAdapterTest {

    ServerUnitAdapter serverUnitAdapter;

    @Before
    public void setUp() {
        // no builder class
        serverUnitAdapter = new ServerUnitAdapterImpl() {
            @Override
            public Class<?> getUnitBuilderClass() {
                return null;
            }
        };
    }

    @Test
    public void shouldAdjustServerUnit_ExtendsClassTheSameAsUnit() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapterImpl() {
            @Override
            public Class<? extends ServerUnit> getUnitExtendsClass() {
                return ServerUnitAdapter.class;
            }
        };

        // acting
        Element element = serverUnitAdapter.buildUnitRootElement();

        // check results
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isNull();
        // check builder element
        assertThat(element.getChild("builder")).isNull();
    }

    @Test
    public void shouldAdjustServerUnit_ExtendsClassDifferent_BuilderClassAbsent() {
        // preparing test data

        // acting
        Element element = serverUnitAdapter.buildUnitRootElement();

        // check results
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        // check builder element
        assertThat(element.getChild("builder")).isNull();
    }

    @Test
    public void shouldUpdatePrepareBaseUnitXML_BuilderClassCorrect_NoBuilderMethod() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapterImpl() {
            @Override
            public Class<?> getUnitBuilderClass() {
                return ServerUnitAdapterBuilder.class;
            }
        };

        // acting
        Element element = serverUnitAdapter.buildUnitRootElement();

        // check results
        Object content = element.getContent().get(0);
        assertThat(content).isInstanceOf(Comment.class);
        assertThat(((Comment) content).getText()).isEqualTo("Adapter of server unit");
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        // check builder element
        Element builder = element.getChild("builder");
        assertThat(builder).isNotNull();
        assertThat(builder.getAttributeValue("class")).isEqualTo("ServerUnitAdapterTest$ServerUnitAdapterBuilder");
        assertThat(builder.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(builder.getAttributeValue("method")).isNull();
    }

    @Test
    public void shouldUpdatePrepareBaseUnitXML_BuilderClassCorrect_BuilderMethodCorrect() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapterImpl() {
            @Override
            public Class<?> getUnitBuilderClass() {
                return ServerUnitAdapterBuilder.class;
            }

            @Override
            public String getUnitBuilderMethodName() {
                return "build";
            }
        };

        // acting
        Element element = serverUnitAdapter.buildUnitRootElement();

        // check results
        Object content = element.getContent().get(0);
        assertThat(content).isInstanceOf(Comment.class);
        assertThat(((Comment) content).getText()).isEqualTo("Adapter of server unit");
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        // check builder element
        Element builder = element.getChild("builder");
        assertThat(builder.getAttributeValue("class")).isEqualTo("ServerUnitAdapterTest$ServerUnitAdapterBuilder");
        assertThat(builder.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(builder.getAttributeValue("method")).isEqualTo("build");
    }

    @Test
    public void shouldUpdatePrepareBaseUnitXML_BuilderClassCorrect_BuilderMethodIncorrect() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapterImpl() {
            @Override
            public Class<?> getUnitBuilderClass() {
                return ServerUnitAdapterBuilder.class;
            }

            @Override
            public String getUnitBuilderMethodName() {
                return "make";
            }
        };

        // acting
        Element element = serverUnitAdapter.buildUnitRootElement();

        // check results
        Object content = element.getContent().get(0);
        assertThat(content).isInstanceOf(Comment.class);
        assertThat(((Comment) content).getText()).isEqualTo("Adapter of server unit");
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        // check builder element
        Element builder = element.getChild("builder");
        assertThat(builder.getAttributeValue("class")).isEqualTo("ServerUnitAdapterTest$ServerUnitAdapterBuilder");
        assertThat(builder.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(builder.getAttributeValue("method")).isNull();
    }

    @Test
    public void shouldUpdatePrepareBaseUnitXML_BuilderClassCorrect_NoDescription() {
        // preparing test data
        ServerUnitAdapter serverUnitAdapter = new ServerUnitAdapterImpl() {
            @Override
            protected String getUnitDescription() {
                return "";
            }

            @Override
            public Class<?> getUnitBuilderClass() {
                return ServerUnitAdapterBuilder.class;
            }
        };

        // acting
        Element element = serverUnitAdapter.buildUnitRootElement();

        // check results
        Object content = element.getContent().get(0);
        assertThat(content).isInstanceOf(Element.class);
        // check server unit classes
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        // check builder element
        Element builder = element.getChild("builder");
        assertThat(builder.getAttributeValue("class")).isEqualTo("ServerUnitAdapterTest$ServerUnitAdapterBuilder");
        assertThat(builder.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(builder.getAttributeValue("method")).isNull();
    }

    @Test
    public void shouldGetServerUnitXML() throws IOException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapterImpl() {
            {
                iconBodyPath = iconPath;
            }

            @Override
            public Class<?> getUnitBuilderClass() {
                return ServerUnitAdapterBuilder.class;
            }

            @Override
            public String getUnitBuilderMethodName() {
                return "build";
            }
        };

        // acting
        Element element = unit.getXML();

        // check results
        final BufferedReader reader;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            unit.store(output, false);
            reader = new BufferedReader(new StringReader(output.toString()));
        }
        assertThat(reader).isNotNull();
        List<String> lines = readAllLines(reader);
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).startsWith("<?xml version=");
        assertThat(lines.get(1)).startsWith("<" + unit.getRootElementName());
        // checking content
        Object content = element.getContent().get(0);
        assertThat(content).isInstanceOf(Comment.class);
        assertThat(((Comment) content).getText()).isEqualTo("Adapter of server unit");
        assertThat(element).isNotNull();
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        // check builder element
        Element builder = element.getChild("builder");
        assertThat(builder).isNotNull();
        assertThat(builder.getAttributeValue("class")).isEqualTo("ServerUnitAdapterTest$ServerUnitAdapterBuilder");
        assertThat(builder.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(builder.getAttributeValue("method")).isEqualTo("build");
        Element parameter = element.getChild("parameter");
        assertThat(parameter).isNotNull();
        ConfigurationParameter icon = ConfigurationParameter.of(parameter);
        assertThat(icon).isNotNull();
        assertThat(icon.getName()).isEqualTo("icon");
        assertThat((String) icon.getValue()).isEqualTo(iconPath);
    }

    @Test
    public void shouldSetServerUnitXML() throws IOException, DataConversionException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapterImpl();
        assertThat(unit.iconBodyPath).isNull();
        Element element = unit.getXML().addContent(ConfigurationParameter.of("icon", iconPath).getXml());

        // acting
        unit.setXML(element);

        // check results
        assertThat(unit.iconBodyPath).isEqualTo(iconPath);
        assertThat(unit.iconBody).isNotEmpty();
        try (InputStream in = unit.getClass().getClassLoader().getResourceAsStream(unit.iconBodyPath)) {
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

    // inner classes
    private static class ServerUnitAdapterBuilder extends ServerUnitAdapterImpl {
        public ServerUnitAdapter build() {
            return new ServerUnitAdapterImpl();
        }
    }

    private static class ServerUnitAdapterImpl extends ServerUnitAdapter {
        public ServerUnitAdapterImpl() {
        }

        @Override
        public String getName() {
            return "Adapter";
        }

        @Override
        public String getType() {
            return "adapter";
        }
    }
}
