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
package org.visualcti.workflow.hardware.javasound;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractCallsPortEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractFaxMachineEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractMultimediaEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractTonesEngine;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.ui.SoundCardUI;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyDevice
 */
@SuppressWarnings("unchecked")
public class SoundCardDevice<H extends SoundCardHandle, F extends AbstractTelephonyFactory<H, ?>>
        extends AbstractTelephonyDevice<H, F> {
    private final AtomicReference<SoundCardUI<H>> uiHolder = new AtomicReference<>(null);
    // to test the value of make call operation result
    private static final Predicate<OperationResultValue> isOnline = value ->
            value == Result.CALL.Analysis.VOICE || value == Result.CALL.Analysis.FAX;

    public SoundCardDevice(String name, SoundCardServiceProvider<H> provider) {
        super(name, provider);
    }

    protected SoundCardDevice(String name, SoundCardServiceProvider<H> provider,
                              CallsPortEngine<H> calls, TonesEngine<H> tones, MultimediaEngine<H> media, FaxMachineEngine<H> faxes) {
        super(name, provider, calls, tones, media, faxes);
    }

    /**
     * <accessor>
     * To get the UI of the device
     *
     * @return the value
     * @see SoundCardUI
     */
    public SoundCardUI<H> getUI() {
        return uiHolder.get();
    }

    /**
     * <action>
     * To create and start device's session
     *
     * @return opened device's session
     * @throws IOException if device cannot start the session
     * @see Device#open()
     * @see #createSessionFor(Object)
     * @see #getProvider()
     */
    @Override
    public PhoneCallSession<H> startSession() throws IOException {
        final PhoneCallSession<H> session = super.startSession();
        final SoundCardUI<H> former = uiHolder.getAndSet(new SoundCardUI<>(session));
        if (former != null) {
            former.getParent().remove(former);
            former.setVisible(false);
        }
        return session;
    }

    /**
     * <checker>
     * To check is phone call session opened for this device's part
     *
     * @param session the phone call's session, device is working with
     * @return true if session opened well
     */
    @Override
    public boolean isOpened(final PhoneCallSession<H> session) {
        return session.getState() != Device.State.CLOSED && session.getDeviceHandle() != wrongHandle();
    }

    /**
     * <accessor>
     * To check, whether device can be used in operations of connections (conference)
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can be shared for another device
     * @see TelephonyFactory
     * @see CallsPortEngine.Parameter#SHARE_CALL_PORT_ALLOWED
     */
    @Override
    public boolean canBeConnected() {
        // cannot no doubts
        return false;
    }

    @Override
    protected H wrongHandle() {
        return SoundCardHandle.wrong();
    }

    @Override
    protected H errorHandle() {
        return wrongHandle();
    }

    @Override
    protected CallsPortEngine<H> callsPart() {
        return new CallControl();
    }

    @Override
    protected TonesEngine<H> tonesPart() {
        return new Tones();
    }

    @Override
    protected MultimediaEngine<H> mediaPart() {
        return new Media();
    }

    @Override
    protected FaxMachineEngine<H> faxPart() {
        return new Fax();
    }

    /**
     * <action>
     * To end a phone call.
     *
     * @param session the phone call's session, device is working with
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see #isOpened()
     */
    @Override
    public boolean dropCall(final PhoneCallSession<H> session) {
        final boolean dropped = super.dropCall(session);
        uiHolder.get().setHandset(!dropped);
        return dropped;
    }

    /**
     * <action>
     * The incoming call is expected. For a user's telephone line a call is deemed accepted after
     * receipt rings of bells.
     *
     * @param session the phone call's session, device is working with
     * @param rings   the quantity of ring signals before answering the call
     * @param timeout waiting time (seconds) how many seconds wait before timeout status returned
     * @param answer  flag is needed answer to an incoming call
     * @return true if operation complete successfully
     * @see AbstractTelephonyDevice#waitForCall(PhoneCallSession, int, int, boolean)
     */
    @Override
    public boolean waitForCall(
            final PhoneCallSession<H> session, final int rings, final int timeout, final boolean answer
    ) {
        if (isOpened(session) && canAcceptCall() && session.isDisconnected()) {
            uiHolder.get().waitingForCall();
            final CountDownLatch operationLatch = new CountDownLatch(rings);
            session.parameter(Parameter.WAIT_CALL_LATCH, operationLatch);
            session.setState(TelephonyDevice.State.WAIT);
            final H handle = session.getDeviceHandle();
            try {
                handle.inProgress(true);
                if (operationLatch.await(timeout, TimeUnit.SECONDS)) {
                    // all necessary rings are detected
                    if (answer) {
                        // answering to the incoming call and mark the session as alive
                        session.alive(true);
                        // setting up the appropriate operation result
                        session.operationResult(Result.CALL.ALERTING);
                        // enabling call's disconnect events producing for the opened handle
                        getProvider().enableEvents(session.getDeviceHandle(), Result.CALL.DISCONNECT);
                    } else {
                        // setting up the appropriate operation result
                        session.operationResult(Result.CALL.RINGS);
                    }
                } else {
                    // operation timeout
                    // setting up the appropriate operation result
                    session.operationResult(Result.TIMEOUT);
                }
            } catch (InterruptedException e) {
                session.setState(Device.State.ERROR);
                session.operationResult(Result.ERROR);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                return false;
            } finally {
                // removing wait for call operation latch
                session.remove(Parameter.WAIT_CALL_LATCH);
                // operation for handle is finished
                handle.inProgress(false);
            }
            // wait for call operation is complete
            session.setState(Device.State.IDLE);
            return true;
        } else {
            session.setState(Device.State.ERROR);
            return false;
        }
    }

    /**
     * <notify>
     * The notify from UI "Ring" button
     *
     * @see #waitForCall(PhoneCallSession, int, int, boolean)
     * @see org.visualcti.workflow.hardware.javasound.ui.panel.PhoneCallsPane
     */
    public void callAlerted(final PhoneCallSession<H> session) {
        final CountDownLatch operationLatch = session.parameter(Parameter.WAIT_CALL_LATCH);
        if (operationLatch == null) {
            // rings without active wait for call operation
            return;
        }
        // processing the call-ring-alert
        operationLatch.countDown();
        final long ringsRemaining = operationLatch.getCount();
        if (ringsRemaining > 0) {
            // not all incoming call rings were received
            return;
        }
        // waiting for end of 'wait for call' operation
        while (session.getState() == TelephonyDevice.State.WAIT) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Tools.error("Waiting the 'wait for call' operation completion is failed");
                e.printStackTrace(Tools.err);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * <action>
     * To make the outgoing call. A mode of a set (pulse or tone) and others
     * the necessary parameters are set by installations of port.
     * <p>
     * Possible values of {@link PhoneCallSession#operationResult()}:
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
     * @param session the phone call's session, device is working with
     * @param number  called telephone number
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     * @see PhoneCall.Number
     * @see #isOpened()
     */
    @Override
    public boolean makeCall(PhoneCallSession<H> session, PhoneCall.Number number, int timeout) {
        if (isOpened(session) && canMakeCall() && session.isDisconnected()) {
            final BlockingQueue<OperationResultValue> answeredResultsQueue = new ArrayBlockingQueue<>(1);
            session.parameter(Parameter.MAKE_CALL_QUEUE, answeredResultsQueue);
            final SoundCardServiceProvider<H> provider = (SoundCardServiceProvider<H>) getProvider();
            session.setState(TelephonyDevice.State.DIAL);
            // preparing UI for make call operation
            uiHolder.get().makingCallFor(number);
            // playing dial-tone
            playTone(session, ToneId.DIAL, 0.7F);
            // dialing the number
            dial(session, number.toString());
            // getting device handle
            final H handle = session.getDeviceHandle();
            // starting ringback tone playing
            if (provider.startToneSending(handle, findToneById(ToneId.RINGBACK))) {
                // waiting for ringback tone plying is started
                waitingForToneStart(handle);
                // getting operation result for auto-answer feature
                uiHolder.get().makingCallResult(answeredResultsQueue);
            } else {
                // ringback tone playing isn't started
                return false;
            }
            try {
                final OperationResultValue answered = answeredResultsQueue.poll(timeout, TimeUnit.SECONDS);
                // stopping ringback tone playing
                provider.stopToneSending(handle);
                //
                // analyzing the make call's answer
                if (answered == null) {
                    // operation timeout
                    // setting up the appropriate operation result
                    session.operationResult(Result.CALL.Analysis.NO_ANSWER);
                    session.setState(Device.State.IDLE);
                    session.alive(false);
                    uiHolder.get().setHandset(false);
                    return false;
                }
                // is answered result means line is connected?
                final boolean isConnected = isOnline.test(answered);
                uiHolder.get().setHandset(isConnected);
                session.alive(isConnected);
                // setting up the state and operation result values
                session.operationResult(answered);
                session.setState(Device.State.IDLE);
                return true;
            } catch (InterruptedException e) {
                session.setState(Device.State.ERROR);
                session.operationResult(Result.ERROR);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                return false;
            } finally {
                // removing make call operation queue
                session.remove(Parameter.MAKE_CALL_QUEUE);
            }
        } else {
            session.setState(Device.State.ERROR);
            session.operationComplete(Result.ERROR);
            // it's impossible to make outgoing call
            return false;
        }
    }

    private void waitingForToneStart(H handle) {
        while (!handle.isOperationInProgress()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * <notify>
     * To notify from UI "Answer" button
     *
     * @param analyzeResult CallAnalyze result
     * @see #makeCall(PhoneCallSession, PhoneCall.Number, int)
     * @see Result.CALL.Analysis
     */
    public final void answered(final PhoneCallSession<H> session, final OperationResultValue analyzeResult) {
        final BlockingQueue<OperationResultValue> answeredResultsQueue = session.parameter(Parameter.MAKE_CALL_QUEUE);
        if (answeredResultsQueue != null && !answeredResultsQueue.offer(analyzeResult)) {
            throw new AssertionError("Not enough room for make call answered result.");
        }
    }

    /**
     * <notify>
     * From UI notification when pressed button in dial pad
     *
     * @param activePhoneCallSession active device's session
     * @param inputCharacter         pressed symbol
     */
    public void userInput(final PhoneCallSession<H> activePhoneCallSession, final String inputCharacter) {
        // playing dtmf tone of the character
        dial(activePhoneCallSession, inputCharacter);
    }

    public void setRecordFormat(Audio selected) {
        // TODO add communication with sound card device
    }

    public void setTransferredPages(PhoneCallSession<H> activePhoneCallSession, int newValue) {
        // TODO add communication with sound card device
    }

    public void setRemoteID(PhoneCallSession<H> activePhoneCallSession, String str) {
        // TODO add communication with sound card device
    }

    public String getLocalID(PhoneCallSession<H> activePhoneCallSession) {
        return "";
    }

    public void setLocalID(PhoneCallSession<H> activePhoneCallSession, String str) {
        // TODO add communication with sound card device
    }

    public String getFaxDocumentHeader(PhoneCallSession<H> activePhoneCallSession) {
        return "";
    }

    public void setFaxDocumentHeader(PhoneCallSession<H> activePhoneCallSession, String str) {
        // TODO add communication with sound card device
    }

    /// inner classes
    // PhoneCall management
    private class CallControl extends AbstractCallsPortEngine<H> {
    }

    // Telephony tones/user's input management
    private class Tones extends AbstractTonesEngine<H> {
    }

    // Audio Play/Record management
    private class Media extends AbstractMultimediaEngine<H> {
    }

    // Fax transmitting/receiving management
    private class Fax extends AbstractFaxMachineEngine<H> {
    }

    /**
     * Configured Parameter Names Enumeration: The parameter names of call parts of the telephony device
     */
    enum Parameter implements Device.ParameterName {
        // the latch of wait for call operation
        WAIT_CALL_LATCH("WAIT FOR CALL LATCH"),
        MAKE_CALL_QUEUE("MAKE CALL RESULTS QUEUE");

        private final String name;

        Parameter(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name.toLowerCase();
        }
    }
}
