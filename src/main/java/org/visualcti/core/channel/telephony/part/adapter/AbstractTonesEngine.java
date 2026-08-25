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
package org.visualcti.core.channel.telephony.part.adapter;

import java.io.IOException;
import java.util.function.Predicate;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.TonesEngine;

/**
 * Adapter: The Part of the Telephony Channel Device: The root device part of the telephony signals and tones management
 *
 * @param <H> the type of the telephony device's low-level operations handle
 * @see TonesEngine
 * @see AbstractDevicePart
 */
public abstract class AbstractTonesEngine<H> extends AbstractDevicePart<H> implements TonesEngine<H> {
    // predicate to check is operation in progress
    private static final Predicate<DeviceStateValue> isOperationInProgress =
            state -> state == TelephonyDevice.State.DIAL
                    || state == TelephonyDevice.State.TONE
                    || state == TelephonyDevice.State.GTDIG;

    /**
     * <action>
     * To dial DTMF symbols to phone line
     *
     * @param session the phone call's session, device is working with
     * @param toDial  sequence of symbols to dial, like "555#1234*"
     */
    @Override
    public void dial(PhoneCallSession<H> session, String toDial) {
        if (canProceed(session)) {
            // staring audio data transmitting
            session.getDevice().dispatchEvent("Dialing [" + toDial + "]");
            session.setState(TelephonyDevice.State.DIAL);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.parameter(Device.Parameter.DEVICE_HANDLE);
            // dialing DTMF symbols sequence
            serviceProvider.dialingDtmf(deviceHandle, toDial);
            // operation is complete
            session.getDevice().dispatchEvent("Dialing is completed.");
            // making dial DTMF operation completed
            session.setState(Device.State.IDLE);
            if (!session.isTerminated() && session.isAlive()) {
                // session wasn't terminated
                session.operationResult(Result.OK);
            }
        } else {
            session.setState(Device.State.ERROR);
            session.operationResult(Result.ERROR);
        }
    }

    /**
     * <action>
     * To play out a sound signal to the phone line.<BR/>
     * The parameters of a signal should be present in the properties port<BR/>
     * under the appropriate identifier of a signal.
     *
     * @param session the phone call's session, device is working with
     * @param toneId  identifier of the signal
     * @param time    duration in seconds
     * @see ToneId
     */
    @Override
    public void playTone(PhoneCallSession<H> session, ToneId toneId, float time) {
        if (canProceed(session)) {
            // staring tone sending
            session.getDevice().dispatchEvent("Sending [" + toneId + "] tone for '" + time + "' seconds.");
            session.setState(TelephonyDevice.State.TONE);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.parameter(Device.Parameter.DEVICE_HANDLE);
            if (serviceProvider.startToneSending(deviceHandle, toneId)) {
                // calculating the timeout time
                final long timeout = (long) (time * 1000L);
                if (timeout <= 0) {
                    // stopping tone generation
                    serviceProvider.stopToneSending(deviceHandle);
                    // start the operation is failed (wrong tone sending timeout) Exception won't throw
                    onDeviceError(session, "Tone sending time is too short.", false);
                    // make send tone operation is complete
                    session.setState(Device.State.IDLE);
                    session.operationResult(Result.ERROR);
                    return;
                }
                // waiting for the timeout time
                try {
                    session.waitingForOperationComplete(timeout);
                    // checking the operation result value after waiting operation complete
                    if (session.operationResult() == Result.ERROR) {
                        // stopping tone generation
                        serviceProvider.stopToneSending(deviceHandle);
                        // device error is detected
                        onDeviceError(session, "Tone sending is failed.");
                        // unreachable code place
                        return;
                        // checking for the termination of the operation
                    } else if (session.isTerminated()) {
                        // stopping tone generation
                        serviceProvider.stopToneSending(deviceHandle);
                        // tone send operation is terminated
                        session.setState(Device.State.IDLE);
                        return;
                        // checking for the disconnection during the operation
                    } else if (session.isDisconnected()) {
                        // stopping tone generation
                        serviceProvider.stopToneSending(deviceHandle);
                        session.setState(Device.State.ERROR);
                        breakingTheSession(session, "Tone sending is failed. The connection is lost.");
                        session.operationResult(Result.CALL.DISCONNECT);
                        return;
                    }
                } catch (InterruptedException e) {
                    session.getDevice().dispatchError(e, "Cannot wait for tone send operation complete.");
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                }
            } else {
                // start the operation is failed Exception won't throw
                onDeviceError(session, "Start tone sending is failed.", false);
                session.operationResult(Result.ERROR);
                return;
            }
            // operation is complete
            session.getDevice().dispatchEvent("Tone sending is completed.");
            // stopping tone generation
            serviceProvider.stopToneSending(deviceHandle);
            // make send tone operation is complete
            session.setState(Device.State.IDLE);
            session.operationResult(Result.OK);
        } else {
            session.setState(Device.State.ERROR);
            session.operationResult(Result.ERROR);
        }
    }

    /**
     * <action>
     * To receive the user input from the telephony line.
     * <p>
     * At reception of symbol from an array determined by a mask input
     * interrupts and come back symbols which are entered up to
     * interruptions by a symbol from a mask
     *
     * @param session                the phone call's session, device is working with
     * @param digitsCount            quantity of expected symbols
     * @param oneSymbolTimeout       maximal waiting time (seconds) of input of next symbol
     * @param terminationSymbolsMask set of symbols finishing up the user input (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".<BR/>
     *                               The symbol finished up the input from the <b>terminationSymbolsMask</b>
     *                               will not be placed to the buffer of input symbols
     * @return the operation's result
     * <p>
     * {@link Result.IO#DTMF} - the sequence of symbols is accepted it's in the digits buffer of the detector.<BR/>
     * For reception of value from buffer, it is necessary to call {@link #getInputSymbols(PhoneCallSession)}.<BR/>
     * {@link Result#TIMEOUT} - in time of timeout there is no any symbol accepted.<BR/>
     * {@link Result.CALL#DISCONNECT} - the operation is interrupted owing to break of telephony connection;<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.<BR/>
     * {@link Result.CALL.Analysis#FAX} - signal of a fax-machine is in the line.
     * @see OperationResultValue
     * @see #getInputSymbols(PhoneCallSession)
     */
    @Override
    public OperationResultValue inputDigits(final PhoneCallSession<H> session, final int digitsCount,
                                            final int oneSymbolTimeout, final String terminationSymbolsMask) {
        if (canProceed(session)) {
            // staring tone sending
            session.getDevice().dispatchEvent("Getting the user input.");
            session.setState(TelephonyDevice.State.GTDIG);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H deviceHandle = session.parameter(Device.Parameter.DEVICE_HANDLE);
            // cleaning the user's input for the current session
            session.parameter(Device.Parameter.USER_INPUT, "");
            //
            // enabling DTMF events producing for the opened handle
            serviceProvider.enableEvents(deviceHandle, Result.IO.DTMF);
            // getting user input (digitsCount symbols)
            for (int i = 1; i <= digitsCount; i++) {
                // waiting for the timeout time
                try {
                    session.waitingForOperationComplete(oneSymbolTimeout);
                    final OperationResultValue operationResult = session.operationResult();
                    // checking the operation result value after waiting operation complete
                    if (operationResult == Result.ERROR) {
                        // stopping DTMF events producing for the opened handle
                        serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
                        // device error is detected
                        onDeviceError(session, "Getting the user input is failed.");
                        return Result.ERROR;
                    } else if (session.isTerminated()) {
                        // stopping DTMF events producing for the opened handle
                        serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
                        // tone send operation is terminated
                        session.setState(Device.State.IDLE);
                        return Result.TERMINATED;
                    } else if (operationResult == Result.IO.DTMF) {
                        // user input detected
                        final String userInput = session.parameter(Device.Parameter.USER_INPUT);
                        if (isTerminationMaskSymbol(session, userInput, terminationSymbolsMask)
                                || userInput.length() >= digitsCount) {
                            // is reached the end of user input condition
                            break;
                        } else {
                            // cleaning processed operation result for the next iteration
                            session.operationResult(Result.NONE);
                            // the next iteration of the loop
                        }
                    } else {
                        // operation is timed out
                        session.operationResult(Result.TIMEOUT);
                        // leaving the loop
                        break;
                    }
                } catch (InterruptedException e) {
                    session.getDevice().dispatchError(e, "Cannot wait for user input operation complete.");
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                    return Result.ERROR;
                }
            }
            // operation is complete
            session.getDevice().dispatchEvent("User input getting is completed.");
            // stopping DTMF events producing for the opened handle
            serviceProvider.disableEvents(deviceHandle, Result.IO.DTMF);
            // make send tone operation is complete
            session.setState(Device.State.IDLE);
        } else {
            session.setState(Device.State.ERROR);
            session.operationResult(Result.ERROR);
        }
        return session.operationResult();
    }

    /**
     * <accessor>
     * To take entered symbols.<BR/>
     * The string of the input symbols from the buffer comes back.<BR/>
     * Internal input buffer will be cleaned
     *
     * @param session the phone call's session, device is working with
     * @return digits sequence accepted by user's input
     * @see #inputDigits(PhoneCallSession, int, int, String)
     */
    @Override
    public String getInputSymbols(PhoneCallSession<H> session) {
        return canProceed(session) ? TonesEngine.super.getInputSymbols(session) : "";
    }

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @param session the phone call's session, device is working with
     * @throws IOException If the device can't terminate current operation
     * @see PhoneCallSession
     */
    @Override
    public void terminate(PhoneCallSession<H> session) throws IOException {
        if (isOperationInProgress.test(session.getState())) {
            session.operationComplete(Result.TERMINATED);
        }
        session.terminate();
    }

    /// private methods
    // to check input session's state
    private boolean canProceed(final PhoneCallSession<H> session) {
        return isOpened(session) && session.isAlive();
    }
    // to check is user's input contains the symbol from the termination mask
    private boolean isTerminationMaskSymbol(PhoneCallSession<H> session, String userInput, String terminationSymbolsMask) {
        if (!isEmpty(userInput)) {
            // getting the last symbol of the user input in the context
            final String lastSymbol = userInput.substring(userInput.length() - 1);
            // analyzing the last user input symbol
            if (terminationSymbolsMask.contains(lastSymbol)) {
                // the last symbol of user input is from the termination symbols mask
                session.parameter(
                        Device.Parameter.USER_INPUT,
                        // the last symbol should be removed from the session's user input result
                        userInput.substring(0, userInput.length() - 1)
                );
                // the symbol from the termination mask
                return true;
            }
        }
        return false;
    }
}
