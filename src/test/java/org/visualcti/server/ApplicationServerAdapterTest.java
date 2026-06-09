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
package org.visualcti.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.server.ExportException;
import java.util.stream.Stream;
import org.jdom.DataConversionException;
import org.junit.After;
import org.junit.Test;
import org.visualcti.server.core.system.SubSystem;

public class ApplicationServerAdapterTest {

    ApplicationServerAdapter application = spy(new ApplicationServerAdapter());

    @After
    public void tearDown() throws Exception {
        UnitRegistry.clear();
        application.close();
    }

    @Test
    public void shouldInitializeServer() throws Exception {
        // preparing test data

        // acting
        application.initialize();

        // check the behavior
        verify(application).loadServerXml();
        // check results
        assertThat(application.isBroken()).isFalse();
    }

    @Test
    public void shouldNotInitializeServer_RmiRegistryDetected() throws Exception {
        // preparing test data
        application.initialize();
        reset(application);

        // acting
        Throwable e = assertThrows(Throwable.class, () -> application.initialize());

        // check the behavior
        verify(application).loadServerXml();
        // check results
        assertThat(application.isBroken()).isTrue();
        assertThat(e).isInstanceOf(InternalError.class);
        assertThat(e.getMessage()).isEqualTo("Another copy of VisualCTI Server is detected");
        assertThat(e.getCause()).isInstanceOf(ExportException.class);
        assertThat(e.getCause().getMessage()).isEqualTo("internal error: ObjID already in use");
    }

    @Test
    public void shouldGetServerParts() throws Exception {
        // preparing test data
        application.initialize();

        // acting
        Stream<SubSystem>partsStream = application.serverParts();

        // check results
        assertThat(partsStream.count()).isNotZero();
    }

    @Test
    public void shouldLoadServerXml() throws IOException, DataConversionException {
        // preparing test data

        // acting
        application.loadServerXml();

        // check the behavior
        verify(application).prepareXmlDocument(any(InputStream.class));
        // check results
        assertThat(application.serverParts().count()).isNotZero();
    }

    @Test
    public void saveServerXml() {
    }

    @Test
    public void handleUnitMessage() {
    }

    @Test
    public void start() {
    }

    @Test
    public void stop() {
    }

    @Test
    public void startingEngine() {
    }

    @Test
    public void stoppingEngine() {
    }

    @Test
    public void execute() {
    }

    @Test
    public void dispatch() {
    }

    @Test
    public void processUnitMessage() {
    }

    @Test
    public void testHandleUnitMessage() {
    }

    @Test
    public void notifyListeners() {
    }

    @Test
    public void notifyListener() {
    }

    @Test
    public void listeners() {
    }

    @Test
    public void addUnitMessageListener() {
    }

    @Test
    public void removeUnitMessageListener() {
    }

    @Test
    public void canStartUnit() {
    }

    @Test
    public void getXML() {
    }

    @Test
    public void setXML() {
    }

    @Test
    public void validateCommand() {
    }

    @Test
    public void respondTo() {
    }

    @Test
    public void testRespondTo() {
    }

    @Test
    public void testSetXML() {
    }
}