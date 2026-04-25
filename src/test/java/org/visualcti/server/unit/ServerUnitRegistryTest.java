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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.CannotRegisterUnitException;
import org.visualcti.server.core.unit.exception.NoSuchUnitException;
import org.visualcti.server.core.unit.exception.NoUniqueUnitException;
import org.visualcti.server.core.unit.exception.ServerUnitException;

public class ServerUnitRegistryTest {
    @Before
    public void setUp() {
        ServerUnitRegistry.clearForTesting();
    }

    @After
    public void tearDown() {
        ServerUnitRegistry.clearForTesting();
    }

    @Test
    public void shouldRegisterServerUnit() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();

        // acting
        ServerUnitRegistry.register(unit);

        // check results
        assertThat(ServerUnitRegistry.lookup(unit)).isPresent();
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(unitPath);
        assertThat(found).isPresent();
        assertThat(found.get()).isSameAs(unit);
    }

    @Test
    public void shouldNotRegisterServerUnit_WrongPath() {
        // preparing test data
        ServerUnit unit = mock(ServerUnit.class);

        // acting
        Exception error = assertThrows(Exception.class, () -> ServerUnitRegistry.register(unit));

        // check results
        assertThat(error).isInstanceOf(NoSuchUnitException.class);
        assertThat(error.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }

    @Test
    public void shouldNotRegisterServerUnit_UnitRegistered() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();
        ServerUnitRegistry.register(unit);

        // acting
        Exception error = assertThrows(Exception.class, () -> ServerUnitRegistry.register(unit));

        // check results
        assertThat(error).isInstanceOf(ServerUnitException.class);
        assertThat(error.getMessage()).isEqualTo("Cannot do safe action!");
        Throwable cause = error.getCause();
        assertThat(cause).isNotEqualTo(error);
        assertThat(cause).isInstanceOf(CannotRegisterUnitException.class);
        assertThat(cause.getMessage()).isEqualTo("Path [" + unitPath + "] already registered");
    }

    @Test
    public void shouldUnregisterServerUnit() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();
        ServerUnitRegistry.register(unit);
        assertThat(ServerUnitRegistry.lookup(unit)).isPresent();
        assertThat(ServerUnitRegistry.lookup(unitPath)).isPresent();

        // acting
        ServerUnitRegistry.unRegister(unit);

        // check results
        assertThat(ServerUnitRegistry.lookup(unit)).isEmpty();
        assertThat(ServerUnitRegistry.lookup(unitPath)).isEmpty();
    }

    @Test
    public void shouldNotUnregisterServerUnit_WrongPath() {
        // preparing test data
        ServerUnit unit = mock(ServerUnit.class);

        // acting
        Exception error = assertThrows(Exception.class, () -> ServerUnitRegistry.unRegister(unit));

        // check results
        assertThat(error).isInstanceOf(NoSuchUnitException.class);
        assertThat(error.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }

    @Test
    public void shouldLookupServerUnitByInstance() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();
        ServerUnitRegistry.register(unit);

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(unit);

        // check results
        assertThat(found).isPresent();
        assertThat(found.get()).isSameAs(unit);
        assertThat(ServerUnitRegistry.lookup(unitPath)).isEqualTo(found);
    }

    @Test
    public void shouldNotLookupServerUnitByInstance_WrongPath() {
        // preparing test data
        ServerUnit unit = mock(ServerUnit.class);

        // acting
        Exception error = assertThrows(Exception.class, () -> ServerUnitRegistry.lookup(unit));

        // check results
        assertThat(error).isInstanceOf(NoSuchUnitException.class);
        assertThat(error.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }

    @Test
    public void shouldLookupServerUnitByPath() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();
        ServerUnitRegistry.register(unit);

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(unitPath);

        // check results
        assertThat(found).isPresent();
        assertThat(found.get()).isSameAs(unit);
        assertThat(ServerUnitRegistry.lookup(unit)).isEqualTo(found);
    }

    @Test
    public void shouldNotLookupServerUnitByPath_WrongPath() {
        // preparing test data

        // acting
        Exception error = assertThrows(Exception.class, () -> ServerUnitRegistry.lookup("   "));

        // check results
        assertThat(error).isInstanceOf(NoSuchUnitException.class);
        assertThat(error.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }

    @Test
    public void shouldLookupServerUnitByType() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();
        ServerUnitRegistry.register(unit);

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(ServerUnit.class);

        // check results
        assertThat(found).isPresent();
        assertThat(found.get()).isSameAs(unit);
        assertThat(ServerUnitRegistry.lookup(unit)).isEqualTo(found);
    }

    @Test
    public void shouldNotLookupServerUnitByType_NotExistsTypeToSearch() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path";
        ServerUnit unit = mock(ServerUnit.class);
        doReturn(unitPath).when(unit).getPath();
        ServerUnitRegistry.register(unit);

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(TasksPoolUnit.class);

        // check results
        assertThat(found).isEmpty();
    }

    @Test
    public void shouldNotLookupServerUnitByType_MultipleInstances() throws ServerUnitException {
        // preparing test data
        String unitPath = "unit-path-1";
        String unitPath2 = "unit-path-2";
        ServerUnit unit = mock(ServerUnit.class);
        when(unit.getPath()).thenReturn(unitPath).thenReturn(unitPath2);
        ServerUnitRegistry.register(unit);
        ServerUnitRegistry.register(unit);

        // acting
        Exception error = assertThrows(Exception.class, () -> ServerUnitRegistry.lookup(ServerUnit.class));

        // check results
        assertThat(error).isInstanceOf(ServerUnitException.class);
        assertThat(error.getMessage()).isEqualTo("Cannot do safe action!");
        Throwable cause = error.getCause();
        assertThat(cause).isNotEqualTo(error);
        assertThat(cause).isInstanceOf(NoUniqueUnitException.class);
        assertThat(cause.getMessage()).isEqualTo("Non unique registered ServerUnit found");
    }
}
