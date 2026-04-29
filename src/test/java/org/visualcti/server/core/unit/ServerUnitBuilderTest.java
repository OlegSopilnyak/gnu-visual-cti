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
package org.visualcti.server.core.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jdom.Element;
import org.junit.Test;
import org.visualcti.server.core.ConfigurationParameter;
import org.visualcti.server.unit.ServerUnitAdapter;

public class ServerUnitBuilderTest {

    ServerUnitBuilder serverUnitBuilder = ServerUnitBuilder.getInstance();
    final String currentPackage = ServerUnitBuilderTest.class.getPackage().getName();

    @Test
    public void shouldGetInstance() {
        assertThat(ServerUnitBuilder.getInstance()).isSameAs(serverUnitBuilder);
    }

    @Test
    public void shouldBuildServerUnitAdapter_UnitBuilderClassIsEmpty() throws IOException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapter() {
            @Override
            public String getName() {
                return "Adapter";
            }

            @Override
            public String getType() {
                return "adapter";
            }
        };
        assertThat(unit.getIcon()).isNull();
        Element xml = unit.getXML();
        assertThat(xml).isNotNull();
        // add builder
        xml.addContent(new Element("builder")
                .setAttribute("package", currentPackage)
                .setAttribute("class", "ServerUnitBuilderTest$ServerUnitAdapterBuilder")
                .setAttribute("method", "buildNoBuilder")
        );
        // add icon for the unit
        xml.addContent(ConfigurationParameter.of("icon", iconPath).getXml());
        // making xml as string from server unit
        String xmlString;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()){
            unit.store(output, false);
            xmlString = output.toString();
        }
        assertThat(xmlString).isNotNull().isNotEmpty();

        // acting
        ServerUnit built = serverUnitBuilder.build(xml);

        // check results
        assertThat(built).isInstanceOf(ServerUnitAdapter.class);
        assertThat(built.getIcon()).isNotEmpty();
        // getting XML from built unit
        Element element = ((ServerUnitAdapter)built).getXML();
        // check server unit classes
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        //check icon parameter
        Element iconParameter = element.getChild("parameter");
        assertThat(iconParameter).isNotNull();
        String unitIconPath = ConfigurationParameter.of(iconParameter).getValue();
        assertThat(unitIconPath).isEqualTo(iconPath);
        // check builder element
        assertThat(element.getChild("builder")).isNull();
    }

    @Test
    public void shouldBuildServerUnitAdapter_UnitBuilderClassIsDefault() throws IOException {
        // preparing test data
        String iconPath = "icon/icon_body.gif";
        ServerUnitAdapter unit = new ServerUnitAdapter() {
            @Override
            public String getName() {
                return "Adapter";
            }

            @Override
            public String getType() {
                return "adapter";
            }
        };
        assertThat(unit.getIcon()).isNull();
        Element xml = unit.getXML();
        assertThat(xml).isNotNull();
        // add builder
        xml.addContent(new Element("builder")
                .setAttribute("package", currentPackage)
                .setAttribute("class", "ServerUnitBuilderTest$ServerUnitAdapterBuilder")
                .setAttribute("method", "buildStandardBuilder")
        );
        // add icon for the unit
        xml.addContent(ConfigurationParameter.of("icon", iconPath).getXml());
        // making xml as string from server unit
        String xmlString;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()){
            unit.store(output, false);
            xmlString = output.toString();
        }
        assertThat(xmlString).isNotNull().isNotEmpty();

        // acting
        ServerUnit built = serverUnitBuilder.build(xml);

        // check results
        assertThat(built).isInstanceOf(ServerUnitAdapter.class);
        assertThat(built.getIcon()).isNotEmpty();
        // getting XML from built unit
        Element element = ((ServerUnitAdapter)built).getXML();
        // check server unit classes
        assertThat(element.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(element.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(element.getAttributeValue("extends")).isEqualTo("org.visualcti.server.core.unit.ServerUnit");
        //check icon parameter
        Element iconParameter = element.getChild("parameter");
        assertThat(iconParameter).isNotNull();
        String unitIconPath = ConfigurationParameter.of(iconParameter).getValue();
        assertThat(unitIconPath).isEqualTo(iconPath);
        // check builder element
        Element builder = element.getChild("builder");
        assertThat(builder).isNotNull();
        assertThat(builder.getAttributeValue("class")).isEqualTo("ServerUnitAdapter");
        assertThat(builder.getAttributeValue("package")).isEqualTo("org.visualcti.server.unit");
        assertThat(builder.getAttributeValue("method")).isNull();
    }

    // inner classes
    private static class ServerUnitAdapterBuilder extends ServerUnitAdapter {
        public static ServerUnit buildNoBuilder() {
            return new ServerUnitAdapterBuilder() {
                @Override
                public Class<?> getUnitBuilderClass() {
                    return null;
                }
            };
        }

        public static ServerUnit buildStandardBuilder() {
            return new ServerUnitAdapterBuilder();
        }

        @Override
        public String getName() {
            return "Builder";
        }

        @Override
        public String getType() {
            return "builder";
        }
    }
}
