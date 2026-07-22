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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.channel.telephony.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.media.Sound;

public class CallsPortEngineTest<H> {
    CallsPortEngine<H> engine;
    PhoneCallSession<H> session;

    @Before
    public void setUp() {
        engine = mock(CallsPortEngine.class);
        session = mock(PhoneCallSession.class);
    }

    @Test
    public void shouldDropCall() {
        // preparing test data
        doReturn(true).when(engine).canAcceptCall();

        // acting
        engine.dropCall(session);

        // check results
        assertThat(engine.canAcceptCall()).isTrue();
    }

    @Test
    public void shouldCanAcceptCall() {
        // preparing test data
        doReturn(true).when(engine).canAcceptCall();

        // acting
        boolean can = engine.canAcceptCall();

        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantAcceptCall() {
        // preparing test data

        // acting
        boolean can = engine.canAcceptCall();

        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldWaitForCall() {
        // preparing test data
        PhoneCall call = mock(PhoneCall.class);
        OperationResultValue resultValue = mock(OperationResultValue.class);
        doReturn(resultValue).when(call).operationResult();
        int rings = 3;
        int timeout = 5;
        boolean answer = true;
        doReturn(call).when(engine).waitForCall(session, anyInt(), anyInt(), anyBoolean());

        // acting
        boolean result = engine.waitForCall(session, rings, timeout, answer);

        // check results
        assertThat(result).isTrue();
//        assertThat(result).isSameAs(call);
//        assertThat(result.operationResult()).isSameAs(resultValue);
    }

    @Test
    public void shouldCanMakeCall() {
        // preparing test data
        doReturn(true).when(engine).canMakeCall();

        // acting
        boolean can = engine.canMakeCall();

        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantMakeCall() {
        // preparing test data

        // acting
        boolean can = engine.canMakeCall();

        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldMakeCall() {
        // preparing test data
        PhoneCall call = mock(PhoneCall.class);
        OperationResultValue resultValue = mock(OperationResultValue.class);
        doReturn(resultValue).when(call).operationResult();
        String phoneNumber = "pone-number";
        int timeout = 5;
        doReturn(call).when(engine).makeCall(session, anyString(), anyInt());

        // acting
        boolean result = engine.makeCall(session, phoneNumber, timeout);

        // check results
//        assertThat(result).isSameAs(call);
//        assertThat(result.operationResult()).isSameAs(resultValue);
    }

    @Test
    public void shouldCanBeConnected() {
        // preparing test data
        doReturn(true).when(engine).canBeConnected();

        // acting
        boolean can = engine.canBeConnected();

        // check results
        assertThat(can).isTrue();
    }

    @Test
    public void shouldCantBeConnected() {
        // preparing test data

        // acting
        boolean can = engine.canBeConnected();

        // check results
        assertThat(can).isFalse();
    }

    @Test
    public void shouldConnect() {
        // preparing test data
        PhoneCall call = mock(PhoneCall.class);
        OperationResultValue resultValue = mock(OperationResultValue.class);
        doReturn(resultValue).when(call).operationResult();
        Sound playBefore = mock(Sound.class);
        String phoneNumber = "phone-number";
        int timeout = 5;
        doReturn(call).when(engine).connect(session, anyString(), anyInt(), any(Sound.class));

        // acting
        boolean result = engine.connect(session, phoneNumber, timeout, playBefore);

        // check results
//        assertThat(result).isSameAs(call);
//        assertThat(result.operationResult()).isSameAs(resultValue);
    }
}