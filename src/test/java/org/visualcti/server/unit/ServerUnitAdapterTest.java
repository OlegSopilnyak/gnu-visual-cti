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
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.Parameter;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.ConfigurationParameter;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;

public class ServerUnitAdapterTest {

    ServerUnitAdapter serverUnitAdapter;

    @Before
    public void setUp() {
        // no builder class
        serverUnitAdapter = spy(new ServerUnitAdapterImpl() {
            @Override
            public Class<?> getUnitBuilderClass() {
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        UnitRegistry.unRegister(serverUnitAdapter);
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
        ServerUnitAdapter unit = spy(new ServerUnitAdapterImpl() {
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
        });

        // acting
        Element element = unit.getXML();

        // check the behavior
        verify(unit).buildUnitRootElement();
        verify(unit).getUnitDescription();
        verify(unit).prepareBaseUnitXML(element);
        verify(unit).prepareUnitXML(element);
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
    public void shouldSetServerUnitXML_CheckResults() throws IOException, DataConversionException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapterImpl();
        assertThat(unit.iconBodyPath).isNull();
        Element element = unit.getXML().addContent(ConfigurationParameter.of("icon", iconPath).getXml());

        // acting
        unit.setXML(element);

        // check results
        assertThat(unit.getPath()).isEqualTo(unit.getName());
        assertThat(unit.iconBodyPath).isEqualTo(iconPath);
        assertThat(unit.iconBody).isNotEmpty();
        try (InputStream in = unit.getClass().getClassLoader().getResourceAsStream(unit.iconBodyPath)) {
            assertThat(in).isNotNull();
            byte[] buffer = new byte[in.available()];
            assertThat(in.read(buffer)).isEqualTo(buffer.length);
            assertThat(buffer).isEqualTo(unit.iconBody);
        }
    }

    @Test
    public void shouldSetServerUnitXML_CheckBehavior() throws IOException, DataConversionException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapterImpl();
        assertThat(unit.iconBodyPath).isNull();
        Element element = serverUnitAdapter.getXML().addContent(ConfigurationParameter.of("icon", iconPath).getXml());

        // acting
        serverUnitAdapter.setXML(element);

        // check behavior
        verify(serverUnitAdapter).settingUpBasePart(element);
        verify(serverUnitAdapter).settingUpMainPart(element);
        // check results
    }

    @Test
    public void shouldGetUnitProperties() {
        // preparing test data

        // acting
        Map<String, Object> properties = serverUnitAdapter.getProperties();
        Exception error = assertThrows(Exception.class, () -> properties.put("package", "org.visualcti.server.unit"));

        // check results
        assertThat(error).isInstanceOf(UnsupportedOperationException.class);
        assertThat(properties).isNotNull().isEmpty();
    }

    @Test
    public void shouldSetUnitProperties() {
        // preparing test data
        Map<String, Object> properties = new HashMap<>();
        properties.put("package", "org.visualcti.server.unit");
        assertThat(serverUnitAdapter.getProperties()).isNotNull().isEmpty();

        // acting
        serverUnitAdapter.setProperties(properties);

        // check results
        assertThat(serverUnitAdapter.getProperties())
                .isNotSameAs(properties)
                .isEqualTo(properties)
                .containsKey("package")
                .containsValue("org.visualcti.server.unit");
    }

    @Test
    public void shouldGetUnitOwnerFromScratch() {
        // preparing test data

        // acting
        ServerUnit unit = serverUnitAdapter.getOwner();

        // check results
        assertThat(unit).isNull();
    }

    @Test
    public void shouldGetUnitOwnerWithExistOwner() {
        // preparing test data
        ServerUnit unitOwner = mock(ServerUnit.class);
        ServerUnitAdapter adapter = new ServerUnitAdapterImpl() {
            {
                this.owner = unitOwner;
            }
        };

        // acting
        ServerUnit unit = adapter.getOwner();

        // check results
        assertThat(unit).isSameAs(unitOwner);
    }

    @Test
    public void shouldSetUnitOwnerValidInstance() throws IOException {
        // preparing test data
        ServerUnit unitOwner = mock(ServerUnit.class);
        doReturn("Owner").when(unitOwner).getPath();

        // acting
        serverUnitAdapter.setOwner(unitOwner);

        // check the behavior
        verify(serverUnitAdapter).getName();
        verify(serverUnitAdapter, never()).removeAll();
        // check results
        assertThat(serverUnitAdapter.getOwner()).isSameAs(unitOwner);
        assertThat(serverUnitAdapter.unitPath).isEqualTo("Owner/" + serverUnitAdapter.getName());
    }

    @Test
    public void shouldSetUnitOwnerNull() throws IOException {
        // preparing test data
        ServerUnit unitOwner = mock(ServerUnit.class);
        doReturn("Owner").when(unitOwner).getPath();
        serverUnitAdapter.setOwner(unitOwner);
        reset(serverUnitAdapter);
        assertThat(serverUnitAdapter.getOwner()).isSameAs(unitOwner);

        // acting
        serverUnitAdapter.setOwner(null);

        // check the behavior
        verify(serverUnitAdapter).getName();
        verify(serverUnitAdapter).removeAll();
        // check results
        assertThat(serverUnitAdapter.getOwner()).isNull();
        assertThat(serverUnitAdapter.unitPath).isEqualTo(serverUnitAdapter.getName());
    }

    @Test
    public void shouldAddServerUnitChild() throws IOException {
        // preparing test data
        ServerUnit unitChild = mock(ServerUnit.class);
        ServerUnit unitOwner = mock(ServerUnit.class);
        doReturn("Owner").when(unitOwner).getPath();
        serverUnitAdapter.setOwner(unitOwner);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.add(unitChild);

        // check the behavior
        verify(serverUnitAdapter).isChild(unitChild);
        verify(unitChild).setOwner(serverUnitAdapter);
        verify(serverUnitAdapter).addBranch(unitChild);
        // check results
        assertThat(serverUnitAdapter.getOwner()).isSameAs(unitOwner);
        assertThat(serverUnitAdapter.unitPath).isEqualTo("Owner/" + serverUnitAdapter.getName());
        assertThat(serverUnitAdapter.children().toArray()).containsExactly(unitChild);
    }

    @Test
    public void shouldNotAddServerUnitChild_ChildAdded() throws IOException {
        // preparing test data
        ServerUnit unitChild = mock(ServerUnit.class);
        doReturn(serverUnitAdapter).when(unitChild).getOwner();
        ServerUnit unitOwner = mock(ServerUnit.class);
        doReturn("Owner").when(unitOwner).getPath();
        serverUnitAdapter.setOwner(unitOwner);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.add(unitChild);

        // check the behavior
        verify(serverUnitAdapter).isChild(unitChild);
        verify(unitChild, never()).setOwner(any(ServerUnit.class));
        verify(serverUnitAdapter, never()).addBranch(any(ServerUnit.class));
        // check results
        assertThat(serverUnitAdapter.getOwner()).isSameAs(unitOwner);
        assertThat(serverUnitAdapter.unitPath).isEqualTo("Owner/" + serverUnitAdapter.getName());
        assertThat(serverUnitAdapter.children().toArray()).isEmpty();
    }

    @Test
    public void shouldNotAddServerUnitChild_ChildIsNull() throws IOException {
        // preparing test data
        ServerUnit unitOwner = mock(ServerUnit.class);
        doReturn("Owner").when(unitOwner).getPath();
        serverUnitAdapter.setOwner(unitOwner);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.add(null);

        // check the behavior
        verify(serverUnitAdapter, never()).isChild(any(ServerUnit.class));
        // check results
        assertThat(serverUnitAdapter.getOwner()).isSameAs(unitOwner);
        assertThat(serverUnitAdapter.unitPath).isEqualTo("Owner/" + serverUnitAdapter.getName());
        assertThat(serverUnitAdapter.children().toArray()).isEmpty();
    }

    @Test
    public void shouldNotAddServerUnitChild_ChildSetOwnerThrows() throws IOException {
        // preparing test data
        ServerUnit unitChild = mock(ServerUnit.class);
        doThrow(IOException.class).when(unitChild).setOwner(any(ServerUnit.class));
        ServerUnit unitOwner = mock(ServerUnit.class);
        doReturn("Owner").when(unitOwner).getPath();
        serverUnitAdapter.setOwner(unitOwner);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.add(unitChild);

        // check the behavior
        verify(serverUnitAdapter).isChild(unitChild);
        verify(unitChild).setOwner(serverUnitAdapter);
        verify(serverUnitAdapter, never()).addBranch(any(ServerUnit.class));
        // check results
        assertThat(serverUnitAdapter.getOwner()).isSameAs(unitOwner);
        assertThat(serverUnitAdapter.unitPath).isEqualTo("Owner/" + serverUnitAdapter.getName());
        assertThat(serverUnitAdapter.children().toArray()).isEmpty();
    }

    @Test
    public void shouldRemoveServerUnitChild() throws IOException {
        // preparing test data
        ServerUnit unitChild = mock(ServerUnit.class);
        serverUnitAdapter.add(unitChild);
        doReturn(serverUnitAdapter).when(unitChild).getOwner();
        assertThat(serverUnitAdapter.children().toArray()).containsExactly(unitChild);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.remove(unitChild);

        // check the behavior
        verify(serverUnitAdapter).isChild(unitChild);
        verify(unitChild).setOwner(null);
        verify(serverUnitAdapter).removeBranch(unitChild);
        // check results
        assertThat(serverUnitAdapter.children().toArray()).isEmpty();
    }

    @Test
    public void shouldNotRemoveServerUnitChild_NotChild() throws IOException {
        // preparing test data
        ServerUnit unitChild = mock(ServerUnit.class);
        serverUnitAdapter.add(unitChild);
        assertThat(serverUnitAdapter.children().toArray()).containsExactly(unitChild);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.remove(unitChild);

        // check the behavior
        verify(serverUnitAdapter).isChild(unitChild);
        verify(unitChild, never()).setOwner(null);
        verify(serverUnitAdapter, never()).removeBranch(any(ServerUnit.class));
        // check results
        assertThat(serverUnitAdapter.children().toArray()).containsExactly(unitChild);
    }

    @Test
    public void shouldNotRemoveServerUnitChild_ChildSetOwnerThrows() throws IOException {
        // preparing test data
        ServerUnit unitChild = mock(ServerUnit.class);
        serverUnitAdapter.add(unitChild);
        doReturn(serverUnitAdapter).when(unitChild).getOwner();
        doThrow(IOException.class).when(unitChild).setOwner(any());
        assertThat(serverUnitAdapter.children().toArray()).containsExactly(unitChild);
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.remove(unitChild);

        // check the behavior
        verify(serverUnitAdapter).isChild(unitChild);
        verify(unitChild).setOwner(null);
        verify(serverUnitAdapter, never()).removeBranch(any(ServerUnit.class));
        // check results
        assertThat(serverUnitAdapter.children().toArray()).containsExactly(unitChild);
    }

    @Test
    public void shouldExecuteCommand_GetMeta() throws Exception {
        // preparing test data
        ServerCommandRequest request = serverUnitAdapter.getMessageFactory()
                .buildFor(serverUnitAdapter, MessageType.COMMAND, MessageFamilyType.GET, "Getting meta-info");
        request.setNeedResponse(true).setParameter(Parameter.of("target", "meta").input());
        reset(serverUnitAdapter);

        // acting
        serverUnitAdapter.execute(request);

        // check the behavior
        verify(serverUnitAdapter).getMessageFactory();
        ArgumentCaptor<UnitMessage> captor = ArgumentCaptor.forClass(UnitMessage.class);
        verify(serverUnitAdapter).dispatch(captor.capture());
        UnitMessage message = captor.getValue();
        // check results
        assertThat(message).isInstanceOf(ServerCommandResponse.class);
        ServerCommandResponse response = (ServerCommandResponse) message;
        assertThat(response.getFamilyType()).isSameAs(request.getFamilyType()).isSameAs(MessageFamilyType.GET);
        assertThat(response.getCorrelationID()).isEqualTo(request.getCorrelationID());
        assertThat(response.getLinkName()).isEqualTo(request.getLinkName());
        assertThat(response.isCommandSuccess()).isTrue();
    }

    @Test
    public void shouldNotExecuteCommand_NotGet() throws Exception {
        // preparing test data
        ServerCommandRequest request = serverUnitAdapter.getMessageFactory()
                .buildFor(serverUnitAdapter, MessageType.COMMAND, MessageFamilyType.SET, "Getting meta-info");
        request.setNeedResponse(true).setParameter(Parameter.of("target", "meta").input());
        reset(serverUnitAdapter);

        // acting
        Exception e = assertThrows(Exception.class, () -> serverUnitAdapter.execute(request));

        // check the behavior
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("SET isn't supported!");
        verify(serverUnitAdapter, never()).getMessageFactory();
        // check results
    }

    @Test
    public void shouldNotExecuteCommand_NoNeedsResponse() throws Exception {
        // preparing test data
        ServerCommandRequest request = serverUnitAdapter.getMessageFactory()
                .buildFor(serverUnitAdapter, MessageType.COMMAND, MessageFamilyType.GET, "Getting meta-info");
        request.setNeedResponse(false).setParameter(Parameter.of("target", "meta").input());
        reset(serverUnitAdapter);

        // acting
        Exception e = assertThrows(Exception.class, () -> serverUnitAdapter.execute(request));

        // check the behavior
        assertThat(e).isInstanceOf(UnknownCommandException.class);
        assertThat(e.getMessage()).isEqualTo("GET isn't supported! Asynchronous execution isn't supported.");
        verify(serverUnitAdapter, never()).getMessageFactory();
        // check results
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
