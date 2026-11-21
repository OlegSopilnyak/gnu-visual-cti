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
package org.visualcti.server.service.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.service.Service;

public class AbstractServiceTest {
    private static final String abstractConfigurationXml = "<service>\n" +
            "  <!-- Testing abstract service -->\n" +
            "  <TestService package=\"org.visualcti.server.service.adapter\" class=\"AbstractService\" extends=\"org.visualcti.server.service.Service\" />\n" +
            "</service>\n";
    AbstractService service;

    @Before
    public void setUp() throws Exception {
        service = spy(new AbstractService() {
            @Override
            protected void prepareBaseUnitXML(Element rootElement) {

            }

            @Override
            protected String getUnitDescription() {
                return "Testing abstract service";
            }

            @Override
            public String getName() {
                return "TestService";
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        service.Stop();
    }

    @Test
    public void shouldGetServiceOwner() {
        // preparing test data
        Service serviceOwner = mock(Service.class);
        doReturn(serviceOwner).when(service).getOwner();

        // acting
        Service owner = service.getServiceOwner();

        // check the behavior
        verify(service).getOwner();
        // check results
        assertThat(owner).isSameAs(serviceOwner);
    }

    @Test
    public void shouldNotGetServiceOwner_NoUnitOwner() {
        // preparing test data

        // acting
        Service owner = service.getServiceOwner();

        // check the behavior
        verify(service).getOwner();
        // check results
        assertThat(owner).isNull();
    }

    @Test
    public void shouldBeDependsOn_TheOwner() {
        // preparing test data
        Service serviceOwner = mock(Service.class);
        doReturn(serviceOwner).when(service).getOwner();

        // acting
        boolean dependsOn = service.isDependsOn(serviceOwner);

        // check the behavior
        verify(service).getServiceOwner();
        verify(service).getOwner();
        // check results
        assertThat(dependsOn).isTrue();
    }

    @Test
    public void shouldBeDependsOn_ItSelf() {
        // preparing test data

        // acting
        boolean dependsOn = service.isDependsOn(service);

        // check the behavior
        verify(service, never()).getServiceOwner();
        // check results
        assertThat(dependsOn).isTrue();
    }

    @Test
    public void shouldNotBeDependsOn_NoOwner() {
        // preparing test data
        Service serviceOwner = mock(Service.class);

        // acting
        boolean dependsOn = service.isDependsOn(serviceOwner);

        // check the behavior
        verify(service).getServiceOwner();
        verify(service).getOwner();
        // check results
        assertThat(dependsOn).isFalse();
    }

    @Test
    public void shouldNotBeDependsOn_WrongOwner() {
        // preparing test data
        Service serviceOwner = mock(Service.class);
        doReturn(serviceOwner).when(service).getOwner();

        // acting
        boolean dependsOn = service.isDependsOn(mock(Service.class));

        // check the behavior
        verify(service).getServiceOwner();
        verify(service).getOwner();
        verify(serviceOwner).getServiceOwner();
        // check results
        assertThat(dependsOn).isFalse();
    }

    @Test
    public void shouldNotAddUnit_NotChild() {
        // preparing test data
        Service extraService = mock(Service.class);

        // acting
        boolean updated = service.add(extraService);

        // check the behavior
        verify(service).isChild(extraService);
        verify(service).isChildrenEnabled();
        // check results
        assertThat(updated).isFalse();
    }

    @Test
    public void shouldNotAddUnit_IsChild() {
        // preparing test data
        Service extraService = mock(Service.class);
        doReturn(true).when(service).isChild(extraService);

        // acting
        boolean updated = service.add(extraService);

        // check the behavior
        verify(service).isChild(extraService);
        verify(service, never()).isChildrenEnabled();
        // check results
        assertThat(updated).isFalse();
    }

    @Test
    public void shouldNotRemoveUnit_NotChild() {
        // preparing test data
        Service extraService = mock(Service.class);

        // acting
        boolean updated = service.remove(extraService);

        // check the behavior
        verify(service).isChild(extraService);
        verify(service, never()).isChildrenEnabled();
        // check results
        assertThat(updated).isFalse();
    }

    @Test
    public void shouldNotRemoveUnit_IsChild() {
        // preparing test data
        Service extraService = mock(Service.class);
        doReturn(true).when(service).isChild(extraService);

        // acting
        boolean updated = service.remove(extraService);

        // check the behavior
        verify(service).isChild(extraService);
        verify(service).isChildrenEnabled();
        // check results
        assertThat(updated).isFalse();
    }

    @Test
    public void shouldStartService() throws IOException {
        // preparing test data
        assertThat(service.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        assertThat(service.isStarted()).isFalse();

        // acting
        service.Start();

        // check the behavior
        verify(service).canStartUnit();
        verify(service).startUnitRunnable();
        // check results
        assertThat(service.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.ACTIVE);
        assertThat(service.isStarted()).isTrue();
    }

    @Test
    public void shouldStopService() throws IOException {
        // preparing test data
        service.Start();
        assertThat(service.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.ACTIVE);
        assertThat(service.isStarted()).isTrue();
        assertThat(service.isStopped()).isFalse();

        // acting
        service.Stop();

        // check the behavior
        verify(service).stopUnitRunnable();
        // check results
        assertThat(service.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        assertThat(service.isStopped()).isTrue();
    }

    @Test
    public void shouldGetXml() throws IOException {
        // preparing test data

        // acting
        Element xml = service.getXML();

        // check the behavior
        verify(service, atLeastOnce()).getRootElementName();
        verify(service).buildUnitRootElement();
        verify(service).prepareBaseUnitXML(any(Element.class));
        // check results
        assertThat(xml).isNotNull();
        assertThat(xml.getName()).isEqualTo("service");
        assertThat(xml.getChild(service.getName())).isNotNull();
        String xmlString = service.asString(xml);
        assertThat(xmlString).isNotEmpty();
    }

    @Test
    public void shouldExtractServiceFrom() throws IOException {
        // preparing test data
        Element xml = service.load(new ByteArrayInputStream(abstractConfigurationXml.getBytes()));

        // acting
        Element serviceXml = service.extractServiceFrom(xml);

        // check the behavior
        // check results
        assertThat(serviceXml).isNotNull();
        assertThat(serviceXml.getName()).isEqualTo(service.getName());
    }

    @Test
    public void shouldNotExtractServiceFrom_NotService() {
        // preparing test data
        Element xml = new Element("notService");

        // acting
        Exception e = assertThrows(Exception.class, () -> service.extractServiceFrom(xml));

        // check the behavior
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("The configuration xml-element is not a service");
    }

    @Test
    public void shouldNotExtractServiceFrom_WrongStructure() {
        // preparing test data
        Element xml = new Element("service")
                .addContent(new Element("FirstService"))
                .addContent(new Element("SecondService"));

        // acting
        Exception e = assertThrows(Exception.class, () -> service.extractServiceFrom(xml));

        // check the behavior
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("The service's configuration xml-element has wrong structure");
    }

    @Test
    public void shouldSetXml() throws IOException, DataConversionException {
        // preparing test data
        Element xml = service.load(new ByteArrayInputStream(abstractConfigurationXml.getBytes()));

        // acting
        service.setXML(xml);

        // check the behavior
        verify(service).extractServiceFrom(xml);
        ArgumentCaptor<Element> xmlCaptor = ArgumentCaptor.forClass(Element.class);
        verify(service).settingUpMainPart(xmlCaptor.capture());
        Element serviceXml = xmlCaptor.getValue();
        verify(service).settingUpPropertiesPart(serviceXml);
        // check results
        assertThat(serviceXml.getName()).isEqualTo(service.getName());
    }

    @Test
    public void shouldNotSetXml_WrongConfigurationXml() throws IOException {
        // preparing test data
        Element xml = new Element("notService");

        // acting
        Exception e = assertThrows(Exception.class, () -> service.setXML(xml));

        // check the behavior
        verify(service).extractServiceFrom(xml);
        verify(service, never()).settingUpMainPart(any(Element.class));
        verify(service, never()).settingUpPropertiesPart(any(Element.class));
        // check results
        assertThat(e).isInstanceOf(IOException.class);
        assertThat(e.getMessage()).isEqualTo("The configuration xml-element is not a service");
    }
}
