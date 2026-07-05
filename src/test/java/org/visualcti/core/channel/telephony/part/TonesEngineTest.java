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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.visualcti.core.channel.telephony.operation.ResultValue;
import org.visualcti.core.channel.telephony.operation.ToneId;

public class TonesEngineTest {
    TonesEngine engine;

    @Before
    public void setUp() {
        engine = mock(TonesEngine.class);
    }

    @Test
    public void shouldDial() {
        // preparing test data
        String phoneNumber = "123456";

        // acting
        engine.dial(phoneNumber);

        // check results
        verify(engine).dial(phoneNumber);
    }

    @Test
    public void shouldPlayToneWithDuration() {
        // preparing test data
        ToneId toneId = ToneId.BEEP;
        float duration = 0.5F;

        // acting
        engine.playTone(toneId , duration);

        // check results
        verify(engine).playTone(toneId , duration);
    }

    @Test
    public void shouldPlayToneWithDefaultDuration() {
        // preparing test data
        ToneId toneId = ToneId.BEEP;
        doCallRealMethod().when(engine).playTone(any(ToneId.class));

        // acting
        engine.playTone(toneId);

        // check results
        ArgumentCaptor<Float> argumentCaptor = ArgumentCaptor.forClass(Float.class);
        verify(engine).playTone(eq(toneId) , argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(0.5F);
    }

    @Test
    public void shouldInputDigits() {
        // preparing test data
        ResultValue resultValue = mock(ResultValue.class);
        int count = 1;
        int timeout = 5;
        String mask = "#";
        doReturn(resultValue).when(engine).inputDigits(anyInt(), anyInt(), anyString());

        // acting
        ResultValue result = engine.inputDigits(count, timeout, mask);

        // check results
        assertThat(result).isSameAs(resultValue);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(engine).inputDigits(eq(count), eq(timeout), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isSameAs(mask);
    }

    @Test
    public void shouldGetInputSymbols() {
        // preparing test data
        String digitsBuffer = "1234567890";
        doReturn(digitsBuffer).when(engine).getInputSymbols();

        // acting
        String inputSymbols = engine.getInputSymbols();

        // check results
        assertThat(inputSymbols).isSameAs(digitsBuffer);
    }
}