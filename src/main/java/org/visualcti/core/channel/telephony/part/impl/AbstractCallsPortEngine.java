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
package org.visualcti.core.channel.telephony.part.impl;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceCore;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.media.Sound;

/**
 * The Part of the Telephony Channel Device: The device part adapter of the telephony call management
 */
public class AbstractCallsPortEngine<H> extends AbstractDevicePart<H> implements CallsPortEngine<H> {
    // predicate for valid result values of wait for call operation
    private static Predicate<OperationResultValue>
            waitForCallOperationResultExpected = value -> value == Result.CALL.RINGS
            || value == Result.CALL.ALERTING || value == Result.TIMEOUT;
    // predicate for connected phone call's operation
    private static Predicate<OperationResultValue>
            connectedCallOperationResult =
            value -> value == Result.CALL.Analysis.VOICE || value == Result.CALL.Analysis.FAX;
    // predicate for valid result values of make call operation
    private static Predicate<OperationResultValue>
            makeCallOperationResultExpected = value -> connectedCallOperationResult.test(value)
            || value == Result.CALL.Analysis.BUSY
            || value == Result.CALL.Analysis.NO_RESPONDING
            || value == Result.CALL.Analysis.NO_DIAL_TONE;

    /**
     * <action>
     * To end a phone call.
     *
     * @param session the phone call's session, device is working with
     * @return true if operation complete successfully
     * @see PhoneCallSession#getDeviceHandle()
     * @see PhoneCallSession#getDevice()
     * @see TelephonyDevice#getProvider()
     * @see TelephonyDevice#terminate(PhoneCallSession)
     * @see TelephonyServiceProvider#dropCall(Object)
     * @see PhoneCallSession#setState(DeviceStateValue)
     * @see Device.State#IDLE
     * @see PhoneCallSession#operationResult(OperationResultValue)
     * @see Result.CALL#DISCONNECT
     */
    @Override
    public boolean dropCall(final PhoneCallSession<H> session) {
        // checking the session's state
        if (isOpened(session) && session.isAlive()) {
            // device's handle is valid and session is alive
            final TelephonyDevice<H, ?> device = session.getDevice();
            try {
                // terminating current device activities related to the session
                device.terminate(session);
            } catch (IOException e) {
                // something went wrong in the termination current operation
                device.dispatchError(e, "Cannot terminate current phone call activities.");
            }
            //
            // getting the device's handle
            final H handle = session.getDeviceHandle();
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = device.getProvider();
            // dropping telephony call on the device service provider site
            if (serviceProvider.dropCall(handle)) {
                // operation is finished well
                session.setState(Device.State.IDLE);
                // saving last operation result
                session.operationComplete(Result.CALL.DISCONNECT);
                // disable all events producing for the opened handle
                serviceProvider.disableEvents(handle);
                // enable producing incoming call events for the opened handle
                serviceProvider.enableEvents(handle, Result.CALL.RINGS);
            }
            // marking session as not alive (disconnected)
            session.alive(false);
            // the operation is successfully completed
            return true;
        } else {
            // handle had wrong value or session isn't alive
            return false;
        }
    }

    /**
     * <action>
     * The incoming call is expected. For a user's telephone line a call is deemed accepted after
     * receipt rings of bells.
     * For connecting interstation line, after receipt of a call, in a line is reproduced
     * (rings-1) of time a signal {@link ToneId#RINGBACK1} and then the method returns call with {@link Result.CALL#ALERTING}
     * <p>
     * If the telephony device is authorized to use it for outgoing calls, (is established in properties
     * of device (only for Telco Edition)), the system can interrupt expectation of the incoming call and
     * can execute outgoing call, using it. If the connection was unsuccessful, the method returns
     * {@link Result.CALL#DISCONNECT}.
     * <p>
     * The information on a call can be received by methods getCalledNumber (), getCallingNumber ().
     * Returned values (operation result):
     * <p>
     * {@link Result#TIMEOUT} - the waiting time was expired,<BR/>
     * {@link Result.CALL#ALERTING} - the incoming call (entering ring) has arrived.
     * <p>
     * <p>
     * ??????????????????????????????????? need to finish the method's call
     * TERM_CONNECT - (only for Telco Edition) the port was involved by system
     * for performance of an outgoing call also is in a mode
     * switching. The given value comes back after the analysis
     * result of an outgoing call, in case of successful connection with the subscriber.
     * ??????????????????????????????????? need to finish the method's call
     * <p>
     * <p>
     * {@link Result.CALL#DISCONNECT} - unsuccessful incoming or outgoing call,
     * or disconnect detected during simple waiting (rings==0).<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     *
     * @param session the phone call's session, device is working with
     * @param rings   the quantity of ring signals before answering the call
     * @param timeout waiting time (seconds) how many seconds wait before timeout status returned
     * @param answer  flag is needed answer to an incoming call
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#getDeviceHandle()
     * @see PhoneCallSession#operationResult()
     * @see TelephonyDeviceCore#getProvider()
     * @see #canAcceptCall()
     */
    @Override
    public boolean waitForCall(PhoneCallSession<H> session, int rings, int timeout, boolean answer) {
        // checking the operation's allowance and session's state
        if (isOpened(session) && this.canAcceptCall() && session.isDisconnected()) {
            //
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            // waiting for incoming call during the timeout value
            int tryCount = timeout;
            do {
                try {
                    // un-sharing the device session
                    session.getDevice().getFactory().unShareDevice(session);
                    // preparing the session for wait for incoming call
                    preparingWaitForCall(session, serviceProvider);
                    // waiting for incoming call 1 second of the timeout's seconds
                    session.waitForOperationComplete(1000L);
                    // checking wait for call operation results
                    if (isThereIncomingCall(session, serviceProvider, answer)) {
                        // incoming call for the session is detected
                        session.getDevice().dispatchEvent("Wait for call operation is completed.");
                        // to check is it possible to share the phone call session
                        if (canBeConnected()) {
                            // sharing the device's session for connection forever if it's possible
                            session.getDevice().getFactory().shareDevice(session, -1L);
                        }
                        return true;
                    }
                } catch (InterruptedException e) {
                    session.getDevice().dispatchError(e, "Cannot wait for call operation complete.");
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                    return false;
                }
                //
                // trying to share the device session for the connect feature if it's possible
                if (canBeConnected() && canMakeCall()) {
                    // sharing the device for 0.5 second
                    session.getDevice().getFactory().shareDevice(session.getDeviceHandle(), 500L);
                }
            } while (--tryCount > 0);
            return true;
        } else {
            // handle had wrong value or session isn't disconnected
            return false;
        }
    }

    /**
     * <action>
     * To make the outgoing call. A mode of a set (pulse or tone) and others
     * the necessary parameters are set by installations of port.
     * <p>
     * Possible values of {@link PhoneCall#operationResult()}:
     * <p>
     * {@link Result.CALL.Analysis#VOICE}         - the Man's voice is answered<BR/>
     * {@link Result.CALL.Analysis#FAX}           - the fax - device has answered<BR/>
     * {@link Result.CALL.Analysis#BUSY}          - calling number is engaged<BR/>
     * {@link Result.CALL.Analysis#NO_ANSWER}     - the telephone number does not answer<BR/>
     * {@link Result.CALL.Analysis#NO_DIAL_TONE}  - phone line is not capable to execute an outgoing call<BR/>
     * because of the line's condition<BR/>
     * {@link Result.CALL.Analysis#SIT}           - special information signal on a line<BR/>
     * {@link Result.CALL.Analysis#NO_RESPONDING} - there is no signal after a phone number dialing up<BR/>
     * {@link Result.CALL.Analysis#BAN}           - the dialing phone number is forbidden
     *
     * @param session           the phone call's session, device is working with
     * @param calledPhoneNumber telephone number for make call to
     * @param timeout           maximal waiting time for the answer (sec) after which session with
     *                          {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER}
     *                          will be returned.
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     * @see #canMakeCall()
     */
    @Override
    public boolean makeCall(PhoneCallSession<H> session, PhoneCall.Number calledPhoneNumber, int timeout) {
        // checking the operation's allowance and session's state
        if (isOpened(session) && canMakeCall() && session.isDisconnected()) {
            //
            // un-sharing the device session
            session.getDevice().getFactory().unShareDevice(session);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            // preparing the session for make outgoing call
            preparingCallMaker(session, serviceProvider, calledPhoneNumber);
            // start outgoing call making
            if (!serviceProvider.startCalling(session.getDeviceHandle(), calledPhoneNumber, timeout)) {
                session.getDevice().dispatchError("Cannot start calling phone number");
                return false;
            }
            // waiting for an answer from the called number side 'timeout' seconds
            try {
                session.waitForOperationComplete(timeout * 1000L);
                if (isThereOutgoingCallCompleted(session, serviceProvider)) {
                    // outgoing call for the session is made
                    session.getDevice().dispatchEvent("Make call operation complete.");
                    // to check is it possible to share the phone call session
                    if (canBeConnected()) {
                        // sharing the device's session for connection forever if it's possible
                        session.getDevice().getFactory().shareDevice(session, -1L);
                    }
                } else {
                    // outgoing call for the session isn't made
                    session.alive(false);
                    session.operationComplete(Result.CALL.Analysis.NO_ANSWER);
                    session.getDevice().dispatchEvent("Make call operation is failed.");
                }
                // operation finished well
                return true;
            } catch (InterruptedException e) {
                session.getDevice().dispatchError(e, "Cannot wait for make call operation complete.");
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * <action>
     * Inquiry connection to another phone number (conference).
     * <p>
     * Inquiry to system for performing the connection with the specified
     * telephone number. Having received inquiry, the system chooses free
     * telephony port and makes outgoing call on the given telephone number.
     * (For a choice of port the table of routing can be used.)
     * <p>
     * On the chosen phone port operation <b>makeCall (number, timeout)</b>
     * automatically is carried out. The result of this operation also
     * will be returned result of operation <b>connect (...)</b>.
     * In case of result call with {@link PhoneCall#operationResult()}
     * {@link Result.CALL.Analysis#VOICE} or {@link Result.CALL.Analysis#FAX} the joining of two ports is made.
     * <p>
     * If the telephone number coincides with internal number of one of ports
     * systems (internal number of port is established in properties of port):
     * <p>
     * 1) If the port is in a condition <b>offhook</b>, the connection is made
     * and the operation returns {@link Result.CALL.Analysis#VOICE};<BR/>
     * 2) If the port is in a condition <b>onhook</b> and type of port - <b>POTS</b>,
     * on connected to him the telephone device the signals of a call
     * are sent. If hook on the telephone device will be lifted,
     * the connection is made and the operation returns {@link Result.CALL.Analysis#VOICE}.
     * If in time of timeout hook will not be removed(taken off), the operation
     * returns {@link Result.CALL.Analysis#NO_ANSWER}.<BR/>
     * 3) If port is in condition <b>onhook</b> and type of port - <b>PSTN</b>, it
     * is translated in the condition <b>offhook</b> also is checked presence
     * of a signal from telephone station ({@link ToneId#DIAL}).
     * At presence of a signal the operation returns VOICE,
     * otherwise - {@link Result.CALL.Analysis#NO_DIAL_TONE}.<BR/>
     * <p>
     * Possible values of {@link PhoneCall#operationResult()}:
     * <p>
     * {@link Result.CALL.Analysis#VOICE}         - the Man's voice is answered<BR/>
     * {@link Result.CALL.Analysis#FAX}           - the fax - device has answered<BR/>
     * {@link Result.CALL.Analysis#BUSY}          - calling number is engaged<BR/>
     * {@link Result.CALL.Analysis#NO_ANSWER}     - the telephone number does not answer<BR/>
     * {@link Result.CALL.Analysis#NO_DIAL_TONE}  - system is not capable to execute an outgoing call<BR/>
     * (There is no free port to perform an outgoing call)<BR/>
     * {@link Result.CALL.Analysis#SIT}           - special information signal on a line<BR/>
     * {@link Result.CALL.Analysis#NO_RESPONDING} - there is no signal after a phone number dialing up<BR/>
     * {@link Result.CALL.Analysis#BAN}           - the calling phone number is forbidden
     *
     * @param session           the phone call's session, device is working with
     * @param calledPhoneNumber telephone number
     * @param timeout           maximal waiting time for the answer (sec) after which call with
     *                          {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @param toPlay            The sound which is playing during the connect operation
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     */
    @Override
    public boolean connect(PhoneCallSession<H> session, PhoneCall.Number calledPhoneNumber, int timeout, Sound toPlay) {
        // getting the device's handle
        final H handle = session.getDeviceHandle();
        if (!isOpened(session)) {
            // session isn't opened yet
            return false;
        } else if (this.canBeConnected() && connectedTo(calledPhoneNumber, timeout, toPlay, session)) {
            // device can be linked with another one
            return true;
        } else {
            // device doesn't support linking with another one
            session.operationComplete(Result.CALL.Analysis.NO_DIAL_TONE);
            return false;
        }
    }

    private boolean connectedTo(PhoneCall.Number calledPhoneNumber, int timeout, Sound toPlay, PhoneCallSession<H> session) {
        return false;
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
        session.terminate();
    }

    /// private methods
    //checking is session opened
    private boolean isOpened(final PhoneCallSession<H> session) {
        if (super.validResourceHandle.negate().test(session.getDeviceHandle())) {
            session.setState(Device.State.CLOSED);
            return false;
        } else {
            return true;
        }
    }

    // preparing the session for wait for incoming call
    private void preparingWaitForCall(final PhoneCallSession<H> session,
                                      final TelephonyServiceProvider<H> serviceProvider) {
        // getting the device's handle from the session
        final H handle = session.getDeviceHandle();
        // setting up called number for waiting incoming call to
        final Optional<ConfigurationParameter> originNumber = deviceCore.getParameter(Parameter.ORIGIN);
        session.calledNumber(originNumber.isPresent() ? originNumber.get().getValue() : PhoneCall.Number.EMPTY);
        // enabling incoming call events producing for the opened handle
        serviceProvider.enableEvents(handle, Result.CALL.RINGS);
        session.setState(TelephonyDevice.State.WAIT);
        session.operationComplete(Result.NONE);
    }

    // checking wait for call operation results
    private static <H> boolean isThereIncomingCall(final PhoneCallSession<H> session,
                                                   final TelephonyServiceProvider<H> serviceProvider,
                                                   final boolean answer) {
        if (waitForCallOperationResultExpected.negate().test(session.operationResult())) {
            // noting is happened, meaning there is no any expected operation result
            return false;
        }
        // getting the device's handle from the session
        final H handle = session.getDeviceHandle();
        // stop producing events for incoming call
        serviceProvider.disableEvents(handle, Result.CALL.RINGS);
        // getting caller phone number and storing it to the session
        session.callingNumber(serviceProvider.getCallerID(handle));
        // it's caught the incoming call event
        if (answer) {
            // to answer to the incoming call and mark the session as alive
            session.alive(serviceProvider.answerCall(handle));
            // setting up the appropriate session's state
            session.operationComplete(Result.CALL.ALERTING);
            // enabling call's disconnect events producing for the opened handle
            serviceProvider.enableEvents(handle, Result.CALL.DISCONNECT);
        } else {
            // setting up the appropriate session's state
            session.operationComplete(Result.CALL.RINGS);
        }
        // wait for call operation is complete
        session.setState(Device.State.IDLE);
        return true;
    }

    // preparing the session for make the outgoing call
    private void preparingCallMaker(final PhoneCallSession<H> session,
                                    final TelephonyServiceProvider<H> serviceProvider,
                                    final PhoneCall.Number number) {
        // getting the device's handle from the session
        final H handle = session.getDeviceHandle();
        // setting up called number for making outgoing call to
        session.calledNumber(number);
        // setting up calling number for making outgoing call from
        final Optional<ConfigurationParameter> originNumber = deviceCore.getParameter(Parameter.ORIGIN);
        session.callingNumber(originNumber.isPresent() ? originNumber.get().getValue() : PhoneCall.Number.EMPTY);
        // disabling any events producing for the opened handle
        serviceProvider.disableEvents(handle);
        // preparing the session for the outgoing call making
        session.setState(TelephonyDevice.State.DIAL);
        session.operationComplete(Result.NONE);
    }

    // checking wait for call operation results
    private static <H> boolean isThereOutgoingCallCompleted(final PhoneCallSession<H> session,
                                                            final TelephonyServiceProvider<H> serviceProvider) {
        if (makeCallOperationResultExpected.negate().test(session.operationResult())) {
            // noting is happened, meaning there is no any expected operation result
            return false;
        }
        // mark the session as alive depends on operation result
        session.alive(connectedCallOperationResult.test(session.operationResult()));
        // enabling call's disconnect events producing for the opened handle
        serviceProvider.enableEvents(session.getDeviceHandle(), Result.CALL.DISCONNECT);
        return true;
    }
}
