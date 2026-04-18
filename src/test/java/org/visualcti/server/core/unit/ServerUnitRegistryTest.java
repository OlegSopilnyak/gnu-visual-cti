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
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.visualcti.server.core.unit.exception.CannotRegisterUnitException;
import org.visualcti.server.core.unit.exception.InvalidUnitException;
import org.visualcti.server.core.unit.exception.ServerUnitException;

public class ServerUnitRegistryTest {
    @Mock
    ServerUnit unit;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void tearDown() {
        try {
            ServerUnitRegistry.clearForTesting();
        } catch (Exception e) {
            // ignoring it
        }
    }

    @Test
    public void shouldRegisterServerUnit() throws Exception {
        // preparing test data
        String path = "path";
        doReturn(path).when(unit).getPath();

        // acting
        ServerUnitRegistry.register(unit);

        // check results
        assertThat(ServerUnitRegistry.lookup(path)).contains(unit);
    }

    @Test
    public void shouldRegisterServerUnitWithPath() throws Exception {
        // preparing test data
        String path = "path";

        // acting
        ServerUnitRegistry.register(path, unit);

        // check results
        assertThat(ServerUnitRegistry.lookup(path)).contains(unit);
    }

    @Test
    public void shouldNotRegisterServerUnitWithPath_WrongPath() {
        // preparing test data
        String path = "";

        // acting
        Exception e = assertThrows(Exception.class, () -> ServerUnitRegistry.register(path, unit));

        // check results
        assertThat(e).isInstanceOf(InvalidUnitException.class);
        assertThat(e.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }

    @Test
    public void shouldNotRegisterServerUnitWithPath_AlreadyRegistered() throws ServerUnitException {
        // preparing test data
        String path = "unit-path";
        doReturn(path).when(unit).getPath();
        ServerUnitRegistry.register(unit);

        // acting
        Exception e = assertThrows(Exception.class, () -> ServerUnitRegistry.register(path, unit));

        // check results
        assertThat(e).isInstanceOf(CannotRegisterUnitException.class);
        assertThat(e.getMessage()).isEqualTo("Path [" + path + "] already registered");
    }

    @Test
    public void shouldUnRegisterServerUnitByInstance() throws Exception {
        // preparing test data
        String path = "path";
        doReturn(path).when(unit).getPath();
        ServerUnitRegistry.register(unit);
        assertThat(ServerUnitRegistry.lookup(path)).contains(unit);

        // acting
        ServerUnitRegistry.unRegister(unit);

        // check results
        assertThat(ServerUnitRegistry.lookup(path)).isEmpty();
    }

    @Test
    public void shouldUnRegisterServerUnitByUnitPath() throws Exception {
        // preparing test data
        String path = "unit-path";
        ServerUnitRegistry.register(path, unit);
        assertThat(ServerUnitRegistry.lookup(path)).contains(unit);

        // acting
        ServerUnitRegistry.unRegister(path);

        // check results
        assertThat(ServerUnitRegistry.lookup(path)).isEmpty();
    }

    @Test
    public void shouldNotUnRegisterServerUnitByUnitPath_WrongPath() {
        // preparing test data
        String path = "";

        // acting
        Exception e = assertThrows(Exception.class, () -> ServerUnitRegistry.unRegister(path));

        // check results
        assertThat(e).isInstanceOf(InvalidUnitException.class);
        assertThat(e.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }

    @Test
    public void shouldLookupServerUnitByInstance() throws Exception {
        // preparing test data
        String path = "path";
        doReturn(path).when(unit).getPath();
        ServerUnitRegistry.register(unit);
        assertThat(ServerUnitRegistry.lookup(path)).contains(unit);

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(unit);

        // check results
        assertThat(found).contains(unit);
    }

    @Test
    public void shouldLookupServerUnitByUnitPath() throws Exception {
        // preparing test data
        String path = "unit-path";
        ServerUnitRegistry.register(path, unit);
        assertThat(ServerUnitRegistry.lookup(path)).contains(unit);

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(path);

        // check results
        assertThat(found).contains(unit);
    }

    @Test
    public void shouldNotLookupServerUnitByUnitPath_NotRegistered() throws Exception {
        // preparing test data
        String path = "unit-path";

        // acting
        Optional<ServerUnit> found = ServerUnitRegistry.lookup(path);

        // check results
        assertThat(found).isEmpty();
    }

    @Test
    public void shouldNotLookupServerUnitByUnitPath_WrongPath() {
        // preparing test data
        String path = "";

        // acting
        Exception e = assertThrows(Exception.class, () -> ServerUnitRegistry.lookup(path));

        // check results
        assertThat(e).isInstanceOf(InvalidUnitException.class);
        assertThat(e.getMessage()).isEqualTo("The ServerUnit has invalid path");
    }
}