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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceSession;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
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

    /**
     * Configured Parameter Names Enumeration: The parameter names of call parts of the telephony device
     */
    enum Parameter implements Device.ParameterName {
        // the latch of wait for call operation
        WAIT_CALL_LATCH("WAIT FOR CALL LATCH"),
        MAKE_CALL_QUEUE("MAKE CALL RESULTS QUEUE")
        ;

        private final String name;

        Parameter(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name.toLowerCase();
        }
    }

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
     * @see Device#startSession()
     */
    @Override
    public PhoneCallSession<H> startSession() throws IOException {
        final PhoneCallSession<H> session = super.startSession();
        final SoundCardUI<H> former = uiHolder.getAndSet(new SoundCardUI<>(session));
        if (former != null && former.getParent() != null) {
            former.getParent().remove(former);
            former.setVisible(false);
        }
        return session;
    }

    /**
     * <action>
     * Getting the device hardware parameters from service provider by device's handle
     * and store them to the device's basic parameters
     *
     * @param handle valid opened device handle value
     * @see #startSession()
     */
    @Override
    public void fillingDeviceSpecific(final H handle) {
        super.fillingDeviceSpecific(handle);
        // adjusting playback/record volumes
        handle.setSourceVolume(floatValueOf(MultimediaEngine.Parameter.PLAYBACK_VOLUME, 0.0f));
        handle.setTargetVolume(floatValueOf(MultimediaEngine.Parameter.RECORD_VOLUME, 0.0f));
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
     * @see CallsPortEngine#canBeConnected()
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
     * @see TelephonyDevice#dropCall(PhoneCallSession)
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
     * @see TelephonyDevice#waitForCall(PhoneCallSession, int, int, boolean)
     */
    @Override
    public boolean waitForCall(final PhoneCallSession<H> session, final int rings,
                               final int timeout, final boolean answer) {
        if (isOpened(session) && canAcceptCall() && session.isDisconnected()) {
            // preparing UI for wait for call operation
            final SoundCardUI<H> ui = uiHolder.get();
            ui.waitingForCall();
            final CountDownLatch operationLatch = new CountDownLatch(rings);
            session.parameter(Parameter.WAIT_CALL_LATCH, operationLatch);
            session.setState(TelephonyDevice.State.WAIT);
            final H handle = session.getDeviceHandle();
            try {
                session.remove(Device.Parameter.TERMINATE);
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
                        // activating dial pad
                        ui.activateDialPad();
                    } else {
                        // setting up the appropriate operation result
                        session.operationResult(Result.CALL.RINGS);
                    }
                } else {
                    // operation timeout (there wasn't notification of operation's latch)
                    // setting up the appropriate operation result
                    session.operationResult(Result.TIMEOUT);
                }
                // updating UI
                ui.setHandset(session.isAlive());
            } catch (InterruptedException e) {
                ui.setHandset(false);
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
            session.operationResult(Result.ERROR);
            return false;
        }
    }

    /**
     * <notify>
     * The notify from UI "Ring" button
     *
     * @see #waitForCall(PhoneCallSession, int, int, boolean)
     * @see org.visualcti.workflow.hardware.javasound.ui.panel.PhoneCallsPane
     * @see #waitForCall(PhoneCallSession, int, int, boolean)
     */
    public void callAlerted(final PhoneCallSession<H> session) {
        final CountDownLatch operationLatch = session.parameter(Parameter.WAIT_CALL_LATCH);
        if (operationLatch == null) {
            // notification of rings without active wait for call operation
            return;
        }
        // processing the call-ring-alert
        operationLatch.countDown();
        if (operationLatch.getCount() == 0) {
            // all incoming call rings were received
            // waiting for end of 'wait for call' operation
            waitUntil(() -> session.getState() == TelephonyDevice.State.WAIT, "wait for call");
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
     * @see TelephonyDevice#makeCall(PhoneCallSession, PhoneCall.Number, int)
     */
    @Override
    public boolean makeCall(final PhoneCallSession<H> session, final PhoneCall.Number number, final int timeout) {
        if (isOpened(session) && canMakeCall() && session.isDisconnected()) {
            final BlockingQueue<OperationResultValue> answeredResultsQueue = new ArrayBlockingQueue<>(1);
            session.parameter(Parameter.MAKE_CALL_QUEUE, answeredResultsQueue);
            final SoundCardServiceProvider<H> provider = (SoundCardServiceProvider<H>) getProvider();
            // preparing UI for make call operation
            final SoundCardUI<H> ui = uiHolder.get();
            ui.makingCallFor(number);
            // playing dial-tone
            playTone(session, ToneId.DIAL, 0.7F);
            // dialing the number
            dial(session, number.toString());
            // getting device handle
            final H handle = session.getDeviceHandle();
            // starting ringback tone playing
            if (provider.startToneSending(handle, findToneById(ToneId.RINGBACK))) {
                // waiting for ringback tone playing is started
                waitUntil(() -> !handle.isOperationInProgress(), "ringback tone playing start");
                // setting up make the call device's state
                session.setState(TelephonyDevice.State.DIAL);
                session.remove(Device.Parameter.TERMINATE);
                // getting operation result for auto-answer feature
                ui.makingCallResult(answeredResultsQueue);
            } else {
                session.setState(Device.State.ERROR);
                session.operationResult(Result.ERROR);
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
                    ui.setHandset(false);
                    return false;
                }
                // is answered result means line is connected?
                final boolean isConnected = isOnline.test(answered);
                ui.setHandset(isConnected);
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
            session.operationCompletingWithResult(Result.ERROR);
            // it's impossible to make outgoing call
            return false;
        }
    }

    /**
     * <notify>
     * To notify from UI "Answered" button
     *
     * @param analyzeResult CallAnalyze result
     * @see #makeCall(PhoneCallSession, PhoneCall.Number, int)
     * @see Result.CALL.Analysis
     * @see #makeCall(PhoneCallSession, PhoneCall.Number, int)
     */
    public final void answered(final PhoneCallSession<H> session, final OperationResultValue analyzeResult) {
        final BlockingQueue<OperationResultValue> answeredResultsQueue = session.parameter(Parameter.MAKE_CALL_QUEUE);
        if (answeredResultsQueue != null && !answeredResultsQueue.offer(analyzeResult)) {
            throw new AssertionError("Not enough room for make call answered result.");
        }
    }

    /**
     * <action>
     * To receive the user input from the telephony line.
     * <p>
     * Possible values of the user input operation result:
     * <p>
     * {@link Result.IO#DTMF} - the sequence of symbols is accepted it's in the digits buffer of the detector.<BR/>
     * For reception of value from buffer, it is necessary to call {@link #getInputSymbols(PhoneCallSession)}.<BR/>
     * {@link Result#TIMEOUT} - in time of timeout there is no any symbol accepted.<BR/>
     * {@link Result.CALL#DISCONNECT} - the operation is interrupted owing to break of telephony connection;<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.<BR/>
     * {@link Result.CALL.Analysis#FAX} - signal of a fax-machine is in the line.
     * <p>
     * At reception of symbol from an array determined by a mask input
     * interrupts and come back symbols which are entered up to
     * interruptions by a symbol from a mask
     *
     * @param session                the phone call's session, device is working with
     * @param digitsCount            quantity of expected symbols
     * @param timeout                maximal waiting time (seconds) of input of next symbol
     * @param terminationSymbolsMask set of symbols finishing up the user input (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".<BR/>
     *                               The symbol finished up the input from the <b>terminationSymbolsMask</b>
     *                               will not be placed to the buffer of input symbols
     * @return the operation's result
     * @see OperationResultValue
     * @see TelephonyDevice#inputDigits(PhoneCallSession, int, int, String)
     */
    @Override
    public OperationResultValue inputDigits(final PhoneCallSession<H> session, final int digitsCount, final int timeout,
                                            final String terminationSymbolsMask) {
        return doWithUI(session, () -> super.inputDigits(session, digitsCount, timeout, terminationSymbolsMask));
    }

    /**
     * <accessor>
     * To take entered symbols.<BR/>
     * The string of the input symbols from the buffer comes back.<BR/>
     * Internal input buffer will be cleaned
     *
     * @param session the phone call's session, device is working with
     * @return digits sequence accepted by user's input
     * @see TelephonyDevice#getInputSymbols(PhoneCallSession)
     */
    @Override
    public String getInputSymbols(final PhoneCallSession<H> session) {
        // clearing the dial pad UI for get input symbols operation
        uiHolder.get().clearDialPad();
        // doing super action
        return super.getInputSymbols(session);
    }

    /**
     * <notify>
     * From UI notification when pressed button in dial pad
     *
     * @param activePhoneCallSession active device's session
     * @param inputCharacter         pressed symbol
     * @see org.visualcti.workflow.hardware.javasound.ui.panel.DtmfInputPane.DialPadButton
     */
    public void userInput(final PhoneCallSession<H> activePhoneCallSession, final String inputCharacter) {
        // playing dtmf tone of the character
        dial(activePhoneCallSession, inputCharacter);
        // sending DTMF device event
        ((SoundCardServiceProvider<H>) getProvider()).putEvent(AbstractDeviceEvent.<H>of(DeviceEvent.Type.DEVICE_SPECIFIC)
                .deviceHandle(activePhoneCallSession.getDeviceHandle()).description("User Input")
                .deviceName(SoundCardServiceProvider.SOUND_DEVICE).vendor(SoundCardServiceProvider.DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, Result.IO.DTMF).option(DeviceEvent.Option.INPUT, inputCharacter)
        );
    }

    /**
     * <action>
     * Playback the audio data stream.
     *
     * @param session                the phone call's session, device is working with
     * @param source                 the input stream, from which undertake sound data for playback to the telephone line
     * @param format                 parameter determining type of the decoder for transformation the sound data
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return the operation's result
     * @see TelephonyDevice#playbackAudio(PhoneCallSession, InputStream, Audio, String, int)
     */
    @Override
    public OperationResultValue playbackAudio(
            final PhoneCallSession<H> session, final InputStream source, final Audio format,
            final String terminationSymbolsMask, final int timeout) {
        return doWithUI(session, () -> super.playbackAudio(session, source, format, terminationSymbolsMask, timeout));
    }

    /**
     * <action>
     * Recording the audio data from the telephone line.
     *
     * @param session                the phone call's session, device is working with
     * @param target                 the output stream where recorded data will be placed
     * @param format                 parameter determining type of the record audio data
     * @param terminationSymbolsMask set of symbols finishing up the recording (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param silence                time (seconds) how long silence in a line is allowed, after which the record operation be finished.
     * @param timeout                maximum time of recording in seconds
     * @return the operation's result
     * @see TelephonyDevice#recordAudio(PhoneCallSession, OutputStream, Audio, String, int, int)
     */
    @Override
    public OperationResultValue recordAudio(PhoneCallSession<H> session, OutputStream target, Audio format, String terminationSymbolsMask, int silence, int timeout) {
        return doWithUI(session, () -> super.recordAudio(session, target, format, terminationSymbolsMask, silence, timeout));
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

    /**
     * <notify>
     * To notify, about device's context state changed
     *
     * @param session the context with new value of the state
     * @see AbstractDeviceSession#setState(DeviceStateValue)
     * @see Device#stateChangedFor(DeviceActivitySession)
     */
    @Override
    public void stateChangedFor(final DeviceActivitySession<H> session) {
        final SoundCardUI<H> ui = uiHolder.get();
        if (isInvalid.negate().test(session) && ui != null) {
            ui.stateChanged(session.getState().toString());
        }
        super.stateChangedFor(session);
    }

    /// private methods
    // to transfer keyboard focus to dial-pad of UI
    private void activateDialPadUI(PhoneCallSession<H> session) {
        if (session.isAlive()) {
            // adjusting the device's UI
            uiHolder.get().activateDialPad();
        }
    }

    // doing the action returning result with UI
    private OperationResultValue doWithUI(
            PhoneCallSession<H> session, Supplier<OperationResultValue> actionResultValue
    ) {
        try {
            activateDialPadUI(session);
            return actionResultValue.get();
        } finally {
            activateDialPadUI(session);
        }
    }

    // to wait until condition is true
    private void waitUntil(final BooleanSupplier condition, final String operation) {
        while (condition.getAsBoolean()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Tools.error("Waiting the '" + operation + "' operation completion is failed");
                e.printStackTrace(Tools.err);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            }
        }
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
}
