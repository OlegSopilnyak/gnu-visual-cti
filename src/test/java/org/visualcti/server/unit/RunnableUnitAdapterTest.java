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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.Test;
import org.visualcti.server.core.unit.RunnableServerUnit;

public class RunnableUnitAdapterTest {
    @Test
    public void shouldConfigureServerUnit() throws IOException, DataConversionException {
        // preparing test data
        RunnableUnitAdapter unit = spy(new RunnableUnitAdapterImpl());
        assertThat(unit.unitConfiguration).isNull();
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        Element element = unit.getXML();

        // acting
        unit.configure(element);

        // check the behavior
        verify(unit).setXML(element);
        // check results
        assertThat(unit.unitConfiguration).isSameAs(element);
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
    }

    @Test
    public void shouldNotConfigureServerUnit_SetXmlThrows() throws IOException, DataConversionException {
        // preparing test data
        RunnableUnitAdapter unit = spy(new RunnableUnitAdapterImpl());
        doThrow(IOException.class).when(unit).setXML(any(Element.class));
        assertThat(unit.unitConfiguration).isNull();
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.PASSIVE);
        Element element = unit.getXML();

        // acting
        unit.configure(element);

        // check the behavior
        verify(unit).setXML(element);
        // check results
        assertThat(unit.unitConfiguration).isNull();
        assertThat(unit.currentUnitState()).isSameAs(RunnableServerUnit.UnitState.BROKEN);
    }

    private static class RunnableUnitAdapterImpl extends RunnableUnitAdapter {
        @Override
        public String getType() {
            return "runnable-unit";
        }

        @Override
        public String getName() {
            return "RunnableUnitAdapter";
        }
    }
}
