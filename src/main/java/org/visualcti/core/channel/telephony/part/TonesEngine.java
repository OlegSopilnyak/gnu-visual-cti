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

import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;

/**
 * The Part of the Telephony Channel Device: The root device part of the telephony signals and tones management
 *
 * @param <H> the type of low-level telephony operations handle
 * @see TelephonyDevicePart
 */
public interface TonesEngine<H> extends TelephonyDevicePart<H> {
    /**
     * <action>
     * To dial DTMF symbols to phone line
     *
     * @param toDial sequence of symbols to dial, like "555#1234*"
     */
    void dial(String toDial);

    /**
     * <action>
     * To play out a sound signal to the phone line.<BR/>
     * The parameters of a signal should be present in the properties port<BR/>
     * under the appropriate identifier of a signal.
     *
     * @param toneId identifier of the signal
     * @see ToneId
     * @see #playTone(ToneId, float)
     */
    default void playTone(ToneId toneId) {
        playTone(toneId, 0.5F);
    }

    /**
     * <action>
     * To play out a sound signal to the phone line.<BR/>
     * The parameters of a signal should be present in the properties port<BR/>
     * under the appropriate identifier of a signal.
     *
     * @param toneId identifier of the signal
     * @param time   duration in seconds
     * @see ToneId
     */
    void playTone(ToneId toneId, float time);

    /**
     * <action>
     * To receive the user input from the telephony line.
     * <p>
     * Possible values of the user input operation result:
     * <p>
     * {@link Result.IO#DTMF} - the sequence of symbols is accepted it's in the digits buffer of the detector.<BR/>
     * For reception of value from buffer, it is necessary to call {@link #getInputSymbols()}.<BR/>
     * {@link Result#TIMEOUT} - in time of timeout there is no any symbol accepted.<BR/>
     * {@link Result.CALL#DISCONNECT} - the operation is interrupted owing to break of telephony connection;<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.<BR/>
     * {@link Result.CALL.Analysis#FAX} - signal of a fax-machine is in the line.
     * <p>
     * At reception of symbol from an array determined by a mask input
     * interrupts and come back symbols which are entered up to
     * interruptions by a symbol from a mask
     *
     * @param digitsCount            quantity of expected symbols
     * @param timeout                maximal waiting time (seconds) of input of next symbol
     * @param terminationSymbolsMask set of symbols finishing up the user input (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".<BR/>
     *                               The symbol finished up the input from the <b>terminationSymbolsMask</b>
     *                               will not be placed to the buffer of input symbols
     * @return the operation's result
     * @see OperationResultValue
     * @see #getInputSymbols()
     */
    OperationResultValue inputDigits(int digitsCount, int timeout, String terminationSymbolsMask);

    /**
     * <accessor>
     * To take entered symbols.<BR/>
     * The string of the input symbols from the buffer comes back.<BR/>
     * Internal input buffer will be cleaned
     *
     * @return digits sequence accepted by user's input
     * @see #inputDigits(int, int, String)
     */
    String getInputSymbols();
}
