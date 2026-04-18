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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;

public class UnitMetaDataTest {
    @Mock
    ServerUnit unit;
    byte[] icon = new byte[]{1,2};
    String type = "test-unit-type";
    String name = "unit";
    String path = "unit-path";
    String state = "unit-state";

    private void prepareUnit() {
        doReturn(icon).when(unit).getIcon();
        doReturn(type).when(unit).getType();
        doReturn(name).when(unit).getName();
        doReturn(path).when(unit).getPath();
        doReturn(state).when(unit).getUnitState();
    }
    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldBuildMetaData() {
        // preparing test data
        prepareUnit();

        // acting
        UnitMetaData data = UnitMetaData.of(unit);

        // check results
        assertThat(data.getIcon()).isEqualTo(icon);
        assertThat(data.getType()).isEqualTo(type);
        assertThat(data.getName()).isEqualTo(name);
        assertThat(data.getPath()).isEqualTo(path);
        assertThat(data.getUnitState()).isEqualTo(state);
        assertThat(data.className()).isEqualTo(unit.getClass().getName());
    }

    @Test
    public void shouldTransferToResponseFully() throws Exception {
        // preparing test data
        prepareUnit();
        UnitMetaData data = UnitMetaData.of(unit);
        ServerCommandResponse response = mock(ServerCommandResponse.class);
        doReturn(Stream.empty()).when(response).getParameters();

        // acting
        data.transferTo(response);

        // check results
        ArgumentCaptor<Parameter> captor = ArgumentCaptor.forClass(Parameter.class);
        verify(response, atLeastOnce()).setParameter(captor.capture());
        Parameter[] parameter = captor.getAllValues().toArray(new Parameter[0]);
        assertThat(parameter).hasSize(6);
        assertThat(parameter[0].getName()).isEqualTo("meta.icon");
        assertThat(parameter[0].getBytesValue()).isEqualTo(icon);
        assertThat(parameter[0].isOutput()).isTrue();
        assertThat(parameter[1].getName()).isEqualTo("meta.type");
        assertThat(parameter[1].getStringValue()).isEqualTo(type);
        assertThat(parameter[1].isOutput()).isTrue();
        assertThat(parameter[2].getName()).isEqualTo("meta.class");
        assertThat(parameter[2].getStringValue()).isEqualTo(unit.getClass().getName());
        assertThat(parameter[2].isOutput()).isTrue();
        assertThat(parameter[3].getName()).isEqualTo("meta.name");
        assertThat(parameter[3].getStringValue()).isEqualTo(name);
        assertThat(parameter[3].isOutput()).isTrue();
        assertThat(parameter[4].getName()).isEqualTo("meta.path");
        assertThat(parameter[4].getStringValue()).isEqualTo(path);
        assertThat(parameter[4].isOutput()).isTrue();
        assertThat(parameter[5].getName()).isEqualTo("meta.state");
        assertThat(parameter[5].getStringValue()).isEqualTo(state);
        assertThat(parameter[5].isOutput()).isTrue();
    }

    @Test
    public void shouldTransferToResponseByTemplate() throws Exception {
        // preparing test data
        prepareUnit();
        UnitMetaData data = UnitMetaData.of(unit);
        ServerCommandResponse response = mock(ServerCommandResponse.class);
        Parameter[] templateParameters = new Parameter[]{
                new Parameter("meta.name", true),
                new Parameter("meta.state", true),
                new Parameter("meta.class", true)
        };
        doReturn(Arrays.stream(templateParameters)).when(response).getParameters();

        // acting
        data.transferTo(response);

        // check results
        ArgumentCaptor<Parameter> captor = ArgumentCaptor.forClass(Parameter.class);
        verify(response, atLeastOnce()).setParameter(captor.capture());
        Parameter[] parameter = captor.getAllValues().toArray(new Parameter[0]);
        assertThat(parameter).hasSameSizeAs(templateParameters);
        assertThat(parameter[0].getName()).isEqualTo("meta.name");
        assertThat(parameter[0].getStringValue()).isEqualTo(name);
        assertThat(parameter[0].isOutput()).isTrue();
        assertThat(parameter[1].getName()).isEqualTo("meta.state");
        assertThat(parameter[1].getStringValue()).isEqualTo(state);
        assertThat(parameter[1].isOutput()).isTrue();
        assertThat(parameter[2].getName()).isEqualTo("meta.class");
        assertThat(parameter[2].getStringValue()).isEqualTo(unit.getClass().getName());
        assertThat(parameter[2].isOutput()).isTrue();
    }
}
