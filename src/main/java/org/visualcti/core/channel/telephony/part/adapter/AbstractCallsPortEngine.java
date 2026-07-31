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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceCore;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.media.Sound;

/**
 * The Part of the Telephony Channel Device: The device part adapter of the telephony call management
 */
@SuppressWarnings({"unchecked"})
public class AbstractCallsPortEngine<H> extends AbstractDevicePart<H> implements CallsPortEngine<H> {
    // predicate for valid session's state for completed operation
    private static final Predicate<DeviceStateValue> operationCompleteState =
            state -> state == Device.State.IDLE || state == Device.State.ERROR;
    // predicate for valid result values of wait for call operation
    private static final Predicate<OperationResultValue>
            waitForCallOperationResultExpected = value -> value == Result.CALL.RINGS
            || value == Result.CALL.ALERTING || value == Result.TIMEOUT;
    // predicate for connected phone call's operation
    private static final Predicate<OperationResultValue>
            connectedCallOperationResult = value -> value == Result.CALL.Analysis.VOICE
            || value == Result.CALL.Analysis.FAX;
    // predicate for valid result values of make call operation
    private static final Predicate<OperationResultValue>
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
                // after possible connect with another phone number
                // breaking the connections with all joint phone call sessions
                session.joint().map(phoneCall -> (PhoneCallSession<H>) phoneCall)
                        .map(Device.Session::getDeviceHandle)
                        .forEach(second -> serviceProvider.breakConnection(second, handle));
                // detaching all possible joint sessions
                session.detachAll();
                // saving last operation result
                session.operationComplete(Result.CALL.DISCONNECT);
                // disable all events producing for the opened handle
                serviceProvider.disableEvents(handle);
                // enable producing incoming call events for the opened handle
                serviceProvider.enableEvents(handle, Result.CALL.RINGS);
            } else {
                // saving last operation result
                session.operationComplete(Result.ERROR);
                // drop call didn't work properly on service provider side
                device.dispatchError("Cannot drop call on the service provider side.");
                return false;
            }
            // operation is finished
            session.setState(Device.State.IDLE);
            // marking session as not alive (disconnected)
            session.alive(false);
            // the operation is successfully completed
            return true;
        } else {
            // device handle has wrong value or session isn't alive yet
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
            final long halfSecondMilliseconds = TimeUnit.MILLISECONDS.toMillis(500);
            do {
                try {
                    // preparing the session for wait for incoming call and
                    // waiting for incoming call 1 second of the timeout's seconds
                    preparingWaitForCall(session, serviceProvider, halfSecondMilliseconds);
                    // checking wait for call operation results
                    if (isThereIncomingCallDetected(session, serviceProvider, answer)) {
                        // incoming call for the telephony device is detected
                        session.getDevice().dispatchEvent("Wait for call operation is completed.");
                        // operation is completed successfully
                        return true;
                        // checking is operation terminated
                    } else if (session.isTerminated()) {
                        // wait for call operation is complete
                        session.setState(Device.State.IDLE);
                        return false;
                        // checking is it possible to share the phone call session during wait for call operation
                    } else if (canBeConnected()) {
                        // waiting for incoming call or make call 1 second of the timeout's seconds
                        if (sharedForConnectWasUsed(session, halfSecondMilliseconds)) {
                            // the operation is completed by any reason
                            return true;
                        } else {
                            // nothing is happened
//                            tryCount--;
                        }
                    }
                } catch (InterruptedException e) {
                    session.getDevice().dispatchError(e, "Cannot wait for call operation complete.");
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                    return false;
                }
            } while (--tryCount > 0);
            // setting up the appropriate operation result
            session.operationComplete(Result.TIMEOUT);
            // wait for call operation is complete
            session.setState(Device.State.IDLE);
            return true;
        } else {
            // handle has wrong value or session isn't disconnected
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
     * @param session     the phone call's session, device is working with
     * @param phoneNumber telephone number for make call to
     * @param timeout     maximal waiting time for the answer (sec) after which session with
     *                    {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER}
     *                    will be returned.
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     * @see #canMakeCall()
     */
    @Override
    public boolean makeCall(final PhoneCallSession<H> session, final PhoneCall.Number phoneNumber, final int timeout) {
        // checking the operation's allowance and session's state
        if (isOpened(session) && canMakeCall() && session.isDisconnected()) {
            // preparing the session for make outgoing call
            preparingCallMaker(session, deviceCore.getProvider(), phoneNumber);
            // start outgoing call making
            if (!startCalling(session, phoneNumber, timeout)) {
                session.getDevice().dispatchError("Cannot start calling phone number");
                session.setState(Device.State.ERROR);
                return false;
            }
            // waiting for an answer from the called number side 'timeout' seconds
            try {
                session.waitForOperationComplete(timeout * 1000L);
                if (isThereOutgoingCallCompleted(session)) {
                    // outgoing call for the session is made
                    session.getDevice().dispatchEvent("Make call operation complete.");
                    // checking is operation terminated
                } else if (session.isTerminated()) {
                    // wait for call operation is complete
                    session.setState(Device.State.IDLE);
                    return false;
                } else {
                    // outgoing call for the session isn't made
                    session.alive(false);
                    session.operationComplete(Result.CALL.Analysis.NO_ANSWER);
                    session.getDevice().dispatchEvent("Make call operation is failed.");
                }
                // make call operation is complete
                session.setState(Device.State.IDLE);
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
        if (!isOpened(session)) {
            // session isn't opened yet
            session.operationComplete(Result.ERROR);
            return false;
        } else if (canBeConnected() && connectedTo(calledPhoneNumber, timeout, toPlay, session)) {
            // device phone call session is linked with another one
            return true;
        } else {
            // device doesn't support linking with another one or not connected to allowed one
            session.operationComplete(Result.CALL.Analysis.NO_DIAL_TONE);
            return false;
        }
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
     * @see TelephonyDevice.State#WAIT
     * @see TelephonyDevice.State#DIAL
     */
    @Override
    public void terminate(PhoneCallSession<H> session) throws IOException {
        final DeviceStateValue state = session.getState();
        if (state == TelephonyDevice.State.WAIT || state == TelephonyDevice.State.DIAL) {
            session.operationComplete(Result.TERMINATED);
        }
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
                                      final TelephonyServiceProvider<H> serviceProvider,
                                      final long milliseconds) throws InterruptedException {
        // getting the device's handle from the session
        final H handle = session.getDeviceHandle();
        // setting up called number for waiting incoming call to
        final Optional<ConfigurationParameter> originNumber = deviceCore.getParameter(Parameter.ORIGIN);
        session.calledNumber(originNumber.isPresent() ? originNumber.get().getValue() : PhoneCall.Number.EMPTY);
        // enabling incoming call events producing for the opened handle
        serviceProvider.enableEvents(handle, Result.CALL.RINGS);
        session.setState(TelephonyDevice.State.WAIT);
        session.operationComplete(Result.NONE);
        // waiting for incoming call 1 second of the timeout's seconds
        session.waitForOperationComplete(milliseconds);
    }

    // checking wait for call operation results
    private static <H> boolean isThereIncomingCallDetected(final PhoneCallSession<H> session,
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
            // setting up the appropriate operation result
            session.operationComplete(Result.CALL.ALERTING);
            // enabling call's disconnect events producing for the opened handle
            serviceProvider.enableEvents(handle, Result.CALL.DISCONNECT);
        } else {
            // setting up the appropriate operation result
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

    // to start building outgoing phone call to the phone number
    private boolean startCalling(PhoneCallSession<H> session, PhoneCall.Number phoneNumber, int timeout) {
        return deviceCore.getProvider().startCalling(session.getDeviceHandle(), phoneNumber, timeout);
    }

    // checking wait for call operation results
    private boolean isThereOutgoingCallCompleted(final PhoneCallSession<H> session) {
        if (makeCallOperationResultExpected.negate().test(session.operationResult())) {
            // noting is happened, meaning there is no any expected operation result
            return false;
        }
        // mark the session as alive depends on operation result
        session.alive(connectedCallOperationResult.test(session.operationResult()));
        // enabling call's disconnect events producing for the opened handle
        deviceCore.getProvider().enableEvents(session.getDeviceHandle(), Result.CALL.DISCONNECT);
        return true;
    }

    // making the connection to the shared session using phone call number
    private boolean connectedTo(final PhoneCall.Number phoneNumber,
                                final int timeout, final Sound toPlay,
                                final PhoneCallSession<H> leadingSession) {
        return leadingSession.getDevice().getFactory().findConnectableFor(phoneNumber).map(connectableSession -> {
            // low-level connections joining if leading session is alive
            if (connectableSession.isAlive() && lowLevelJoin(connectableSession, leadingSession)) {
                // just connecting two alive session by their handles
                connectableSession.join(leadingSession);
                return true;
            } else if (connectableSession.isDisconnected()) {
                // start playing the melody sound during outgoing phone call's making
                startPlaying(leadingSession, toPlay, timeout);
                // making outgoing telephony call using shared session and
                // low-level connections joining if outgoing pone call is made
                if (makeCall(connectableSession, phoneNumber, timeout) && lowLevelJoin(connectableSession, leadingSession)) {
                    // stop playing the melody sound
                    stopPlaying(leadingSession);
                    // just connecting two alive session by their handles
                    connectableSession.join(leadingSession);
                    return true;
                }
                // stop playing the melody sound
                stopPlaying(leadingSession);
            }
            // the connection isn't successful
            return false;
        }).orElse(false);
    }

    // waiting for incoming call or make call 1 second of the timeout's seconds
    private boolean sharedForConnectWasUsed(PhoneCallSession<H> session, long timeout) throws InterruptedException {
        session.getDevice().dispatchEvent("Sharing 'wait for call' operation resource.");
        // waiting for incoming call or make call 1 second of the timeout's seconds
        session.waitForOperationComplete(timeout);
        return operationCompleteState.test(session.getState()) && session.isAlive();
    }

    // low-level telephony call session connections joining by their handles
    private boolean lowLevelJoin(PhoneCallSession<H> connectable, PhoneCallSession<H> leading) {
        return deviceCore.getProvider().makeConnection(connectable.getDeviceHandle(), leading.getDeviceHandle());
    }

    private void startPlaying(PhoneCallSession<H> session, Sound sound, int timeout) {
        final TelephonyDevice<H, ?> device = session.getDevice();
        // doing nothing fore while
        device.playbackAudio(sound, "", timeout);
    }

    private void stopPlaying(PhoneCallSession<H> session) {
        // doing nothing fore while
    }
}
