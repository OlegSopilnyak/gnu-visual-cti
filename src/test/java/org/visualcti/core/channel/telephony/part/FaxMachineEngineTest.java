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
package org.visualcti.core.channel.telephony.part;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ResultValue;
import org.visualcti.media.Document;
import org.visualcti.media.Fax;

public class FaxMachineEngineTest {
    FaxMachineEngine engine;

    @Before
    public void setUp() {
        engine = mock(FaxMachineEngine.class);
    }

    @Test
    public void shouldCanFax() {
        // preparing test data
        doReturn(true).when(engine).canFax();

        // acting
        boolean can = engine.canFax();

        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantFax() {
        // preparing test data

        // acting
        boolean can = engine.canFax();

        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldGetTransferredPages() {
        // preparing test data
        int pages = 10;
        doReturn(pages).when(engine).getTransferredPages();

        // acting
        int transferred = engine.getTransferredPages();

        // check results
        assertThat(transferred).isEqualTo(pages);
    }

    @Test
    public void shouldGetRemoteID() {
        // preparing test data
        String faxId = "fax-id";
        doReturn(faxId).when(engine).getRemoteID();

        // acting
        String remoteFaxId = engine.getRemoteID();

        // check results
        assertThat(remoteFaxId).isEqualTo(faxId);
    }

    @Test
    public void shouldSetFaxHeader() {
        // preparing test data
        String faxHeader = "fax-header";

        // acting
        engine.setFaxHeader(faxHeader);

        // check results
        verify(engine).setFaxHeader(faxHeader);
    }

    @Test
    public void shouldSetFaxLocalID() {
        // preparing test data
        String faxId = "fax-id";

        // acting
        engine.setFaxLocalID(faxId);

        // check results
        verify(engine).setFaxLocalID(faxId);
    }

    @Test
    public void shouldReceiveFax() {
        // preparing test data
        ResultValue resultValue = mock(ResultValue.class);
        OutputStream stream = mock(OutputStream.class);
        boolean pollingMode = true;
        boolean issueVoiceRequest = true;
        doReturn(resultValue).when(engine).receive(any(OutputStream.class), eq(true), anyBoolean());

        // acting
        ResultValue result = engine.receive(stream, pollingMode, issueVoiceRequest);

        // check the behavior
        ArgumentCaptor<OutputStream> captor = ArgumentCaptor.forClass(OutputStream.class);
        verify(engine).receive(captor.capture(), anyBoolean(), anyBoolean());
        // check results
        assertThat(captor.getValue()).isSameAs(stream);
        assertThat(result).isSameAs(resultValue);
    }

    @Test
    public void shouldTransmitFaxStream() {
        // preparing test data
        ResultValue resultValue = mock(ResultValue.class);
        InputStream stream = mock(InputStream.class);
        Fax faxFormat = Fax.TIFF;
        boolean issueVoiceRequest = true;
        doReturn(resultValue).when(engine).transmit(eq(stream), eq(faxFormat), anyBoolean());

        // acting
        ResultValue result = engine.transmit(stream, faxFormat, issueVoiceRequest);

        // check the behavior
        ArgumentCaptor<Fax> captor = ArgumentCaptor.forClass(Fax.class);
        verify(engine).transmit(eq(stream), captor.capture(), anyBoolean());
        // check results
        assertThat(captor.getValue()).isSameAs(faxFormat);
        assertThat(result).isSameAs(resultValue);
    }

    @Test
    public void shouldTransmitFaxDocument() throws IOException {
        // preparing test data
        ResultValue resultValue = mock(ResultValue.class);
        InputStream stream = mock(InputStream.class);
        Fax faxFormat = Fax.TIFF;
        Document document = mock(Document.class);
        doReturn(stream).when(document).getInputStream();
        doReturn(faxFormat).when(document).getFormat();
        boolean issueVoiceRequest = true;
        doCallRealMethod().when(engine).transmit(eq(document), anyBoolean());
        doReturn(resultValue).when(engine).transmit(stream, faxFormat, true);

        // acting
        ResultValue result = engine.transmit(document, issueVoiceRequest);

        // check the behavior
        ArgumentCaptor<Fax> captor = ArgumentCaptor.forClass(Fax.class);
        verify(engine).transmit(eq(stream), captor.capture(), anyBoolean());
        // check results
        assertThat(captor.getValue()).isSameAs(faxFormat);
        assertThat(result).isSameAs(resultValue);
    }

    @Test
    public void shouldNotTransmitFaxDocument_GetStreamThrows() throws IOException {
        // preparing test data
        Fax faxFormat = Fax.TIFF;
        Document document = mock(Document.class);
        IOException exception = mock(IOException.class);
        doThrow(exception).when(document).getInputStream();
        doReturn(faxFormat).when(document).getFormat();
        boolean issueVoiceRequest = true;
        doCallRealMethod().when(engine).transmit(eq(document), anyBoolean());

        // acting
        ResultValue result = engine.transmit(document, issueVoiceRequest);

        // check the behavior
        verify(document, never()).getFormat();
        verify(engine, never()).transmit(any(InputStream.class), any(Fax.class), anyBoolean());
        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(engine).dispatchError(captor.capture(), anyString());
        // check results
        assertThat(captor.getValue()).isSameAs(exception);
        assertThat(result).isSameAs(Result.TERMINATED);
    }
}