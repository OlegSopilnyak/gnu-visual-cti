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

import static org.mockito.Mockito.spy;

import java.io.IOException;
import org.jdom.DataConversionException;
import org.junit.Test;

public class ApplicationServerAdapterTest {

    ApplicationServerAdapter application = spy(new ApplicationServerAdapter());

    @Test
    public void serverParts() {
    }

    @Test
    public void shouldLoadServerXml() throws IOException, DataConversionException {
        // preparing test data

        // acting
        application.loadServerXml();

        // check results
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