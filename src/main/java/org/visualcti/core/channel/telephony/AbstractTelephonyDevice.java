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
package org.visualcti.core.channel.telephony;

import static org.visualcti.core.channel.telephony.TelephonyDevice.State.DIAL;
import static org.visualcti.core.channel.telephony.TelephonyDevice.State.GTDIG;
import static org.visualcti.core.channel.telephony.TelephonyDevice.State.PLAY;
import static org.visualcti.core.channel.telephony.TelephonyDevice.State.RECORD;
import static org.visualcti.core.channel.telephony.TelephonyDevice.State.TONE;
import static org.visualcti.core.channel.telephony.TelephonyDevice.State.WAIT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.adapter.AbstractDevice;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultiMedeaEngine;
import org.visualcti.core.channel.telephony.part.TelephonyDevicePart;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Fax;
import org.visualcti.media.Sound;

/**
 * Abstract Device of the Channel: The root device through which task communicate with
 * <p>
 * <b>Computer Telephony Equipment</b>
 *
 * @param <H> the type of the device's low-level operations handle
 * @param <T> the type of the devices factory
 * @see AbstractDevice
 * @see TelephonyDevice
 * @see TelephonyDeviceFactory
 * @see CallsPortEngine
 * @see TonesEngine
 * @see MultiMedeaEngine
 * @see FaxMachineEngine
 */
public class AbstractTelephonyDevice<H, T extends TelephonyDeviceFactory<?>>
        extends AbstractDevice<H, T> implements TelephonyDevice<H, T> {
    // the channel-device configured parameters map
    private final Map<ParameterName, ConfigurationParameter> parameters = new ConcurrentHashMap<>();
    // the name of the device in the device factory
    private final String name;
    // The opened device handle for the low level telephony operations
    private final AtomicReference<H> handleHolder = new AtomicReference<>(wrongHandle());
    private final Predicate<H> validResourceHandle =
            handle -> !Objects.equals(handle, wrongHandle()) || !Objects.equals(handle, errorHandle());
    // predicate for valid result values of wait for call operation
    private static Predicate<OperationResultValue> waitForCallExpected = value -> value == Result.CALL.RINGS
            || value == Result.CALL.ALERTING || value == Result.TIMEOUT;
    // predicate for valid result values of make call operation
    private static Predicate<OperationResultValue> makeCallExpected = value -> value == Result.CALL.Analysis.VOICE
            || value == Result.CALL.Analysis.FAX
            || value == Result.CALL.Analysis.BUSY
            || value == Result.CALL.Analysis.NO_ANSWER
            || value == Result.CALL.Analysis.NO_RESPONDING
            || value == Result.CALL.Analysis.NO_DIAL_TONE;
    // the provider of telephony operations
//    private final TelephonyServiceProvider<H> provider;
    // device part of the telephony calls management
    private final CallsPortEngine<H> calls;
    // device part of the telephony signals and tones management
    private final TonesEngine<H> tones;
    // device part of the telephony multi-medea (playback/record) management
    private final MultiMedeaEngine<H> media;
    // device part of the telephony fax-document exchange management
    private final FaxMachineEngine<H> faxes;

    /**
     * <builder>
     * The constructor of the telephony device with parts instance
     *
     * @param name     the name of the device in the device factory
     * @param provider the manufacturer's provider of telephony operations
     * @param calls    device part of the telephony calls management
     * @param tones    device part of the telephony signals and tones management
     * @param media    device part of the telephony multi-medea (playback/record) management
     * @param faxes    device part of the telephony fax-document exchange management
     */
    protected AbstractTelephonyDevice(
            final String name, final TelephonyServiceProvider<H> provider,
            final CallsPortEngine<H> calls, final TonesEngine<H> tones,
            final MultiMedeaEngine<H> media, final FaxMachineEngine<H> faxes
    ) {
        super(provider);
        this.name = name;
//        this.provider = provider;
        this.calls = calls.use(this);
        this.tones = tones.use(this);
        this.media = media.use(this);
        this.faxes = faxes.use(this);
    }

    @Override
    public <P extends TelephonyDevicePart<?>> P use(TelephonyDeviceCore<H> deviceCore) {
        throw new UnsupportedOperationException("Not applicable here");
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <accessor>
     * To get access to device's low-level handle
     *
     * @return the handle to manipulate the device features
     */
//    @Override
//    public H getHandle() {
//        return handleHolder.get();
//    }

    /**
     * <accessor>
     * To get access to the wrong value device's low-level handle
     *
     * @return the value for handle of unopened device
     */
    protected H wrongHandle() {
        return null;
    }

    /**
     * <accessor>
     * To get access to the error value device's low-level handle
     *
     * @return the value for handle of corrupted device
     */
    protected H errorHandle() {
        return null;
    }

    /**
     * <accessor>
     * To get access to the channel-device configured parameter value
     *
     * @param name the name of configured parameter
     * @return the parameter value or empty
     * @see ConfigurationParameter
     * @see ParameterName
     * @see Optional
     */
//    @Override
//    public Optional<ConfigurationParameter> getParameter(ParameterName name) {
//        return Optional.ofNullable(parameters.get(name));
//    }

    /**
     * <accessor>
     * To get access to the current device's telephony events provider
     *
     * @return the reference to the events provider singleton
     * @see TelephonyServiceProvider
     */
    @Override
    public TelephonyServiceProvider<H> getProvider() {
        return (TelephonyServiceProvider<H>) super.serviceProvider;
    }

    /**
     * <action>
     * Opening and activation of the channel-device.
     *
     * @throws IOException if channel device cannot be opened or activated
     * @see #isOpened()
     * @see #close()
     * @see #getName()
     * @see TelephonyServiceProvider#openResource(String)
     * @see AbstractDevice#open()
     * @see #dropCall()
     * @see TelephonyServiceProvider#enableEvents(Object, OperationResultValue)
     */
    @Override
    public void open() throws IOException {
        if (isOpened()) {
            // device is opened already, closing it
            close();
        }
        // trying to open the telephony resource by the name and get the handle to the resource
        final H handle = getProvider().openResource(getName());
        // checking the resource's handle
        if (validResourceHandle.test(handle)) {
            // setting up the state of te device
            super.open();
            // storing resource's handle
            handleHolder.getAndSet(handle);
            // dropping the call if any
            dropCall();
            // enabling device's incoming call events producing
//            getProvider().enableEvents(getHandle(), Result.CALL.RINGS);
//            // opening fax-machine engine
//            try {
//                faxes.open();
//            } catch (IOException e) {
//                dispatchError(e, "Failed to open fax-machine for: " + getName());
//            }
        }
    }

    /**
     * <action>
     * Closing the device
     *
     * @throws IOException if an I/O error occurs
//     * @see #getHandle()
     * @see #terminate()
     * @see #dropCall()
     * @see AbstractDevice#close()
     * @see TelephonyServiceProvider#disableEvents(Object)
     */
    @Override
    public void close() throws IOException {
//        if (isDeviceOpened()) {
//            // to terminate the current device's operation
//            terminate();
//            // dropping the call if any
//            dropCall();
//            // setting up the state of the device
//            super.close();
//            // disabling any events producing
//            provider.disableEvents(getHandle());
//            // closing the resource
//            provider.closeResource(getHandle());
//            // closing fax-machine engine
//            try {
//                faxes.close();
//            } catch (IOException e) {
//                dispatchError(e, "Failed to close fax-machine for: " + getName());
//            }
//        }
    }

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @throws IOException If the device can't terminate current operation
     */
    @Override
    public void terminate() throws IOException {
//        if (isDeviceOpened()) {
//            final DeviceStateValue deviceState = getState();
//            if (deviceState == PLAY || deviceState == RECORD) {
//                media.terminate();
//            } else if (deviceState == SENDFAX || deviceState == RECVFAX) {
//                faxes.terminate();
//            } else if (deviceState == DIAL || deviceState == TONE || deviceState == GTDIG) {
//                tones.terminate();
//            } else if (calls != null) {
//                calls.terminate();
//            }
//            setState(Device.State.STOPD);
//        }
    }

    /**
     * <action>
     * To break off telephony connection using delegation.
     *
     * @see CallsPortEngine#dropCall()
     */
    @Override
    public void dropCall() {
        calls.dropCall();
//        currentState.getAndSet(Device.State.IDLE);
    }

    /**
     * <action>
     * The incoming call is expected. For a user's telephone line a call is deemed accepted after
     * receipt rings of bells.
     *
     * @param rings   the quantity of ring signals before answering the call
     * @param timeout waiting time (seconds) how many seconds wait before timeout status returned
     * @param answer  flag is needed answer to an incoming call
     * @return the phone call with appropriate operation result
     * @see PhoneCall
     * @see PhoneCall#operationResult()
     * @see PhoneCall#FAILED
     * @see CallsPortEngine#waitForCall(int, int, boolean)
     */
    @Override
    public PhoneCall waitForCall(int rings, int timeout, boolean answer) {
        return calls.canAcceptCall()
                // to delegate call to the particular device's part engine
                ? delegatePhoneCallOperation(WAIT, () -> calls.waitForCall(rings, timeout, answer), waitForCallExpected)
                // failed answer
                : PhoneCall.FAILED;
    }

    // to delegate call to the particular device's part engine
    private PhoneCall delegateWaitForCall(int rings, int timeout, boolean answer) {
        return delegatePhoneCallOperation(WAIT, () -> calls.waitForCall(rings, timeout, answer), waitForCallExpected);
    }

    /**
     * <action>
     * To make the outgoing call. A mode of a set (pulse or tone) and others
     * the necessary parameters are set by installations of port.
     *
     * @param number  telephone number
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCall#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @return the phone call with appropriate operation result
     * @see PhoneCall
     * @see PhoneCall#operationResult()
     * @see PhoneCall#FAILED
     * @see CallsPortEngine#makeCall(String, int)
     */
    @Override
    public PhoneCall makeCall(String number, int timeout) {
        return calls.canMakeCall()
                // to delegate call to the particular device's part engine
                ? delegatePhoneCallOperation(DIAL, () -> calls.makeCall(number, timeout), makeCallExpected)
                // failed answer
                : PhoneCall.FAILED;
    }


    /**
     * <action>
     * Inquiry connection to another phone number (conference).
     *
     * @param number  telephone number
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCall#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @param toPlay  The sound which is playing during the connect operation
     * @return the phone call with appropriate operation result
     * @see PhoneCall
     * @see PhoneCall#FAILED
     * @see CallsPortEngine#connect(String, int, Sound)
     */
    @Override
    public PhoneCall connect(String number, int timeout, Sound toPlay) {
        return calls.canBeConnected()
                // to delegate call to the particular device's part engine
                ? delegatePhoneCallOperation(DIAL, () -> calls.connect(number, timeout, toPlay), makeCallExpected)
                // failed answer
                : PhoneCall.FAILED;
    }

    /**
     * <accessor>
     * To get the quantity of the transferred fax-pages
     *
     * @return how many pages transferred
     * @see FaxMachineEngine#getTransferredPages()
     */
    @Override
    public int getTransferredPages() {
        return  0;
//        return isDeviceOpened() && faxes.isOpened() ? faxes.getTransferredPages() : 0;
    }

    /**
     * <accessor>
     * To get the local ID of the remote fax machine
     *
     * @return localId of the remote fax-machine
     * @see FaxMachineEngine#getRemoteID()
     */
    @Override
    public String getRemoteID() {
        return "";
//        return isDeviceOpened() && faxes.isOpened() ? faxes.getRemoteID() : "";
    }

    /**
     * <mutator>
     * To set up the header of the fax-document's pages
     *
     * @param header the new value
     * @see FaxMachineEngine#setFaxHeader(String)
     */
    @Override
    public void setFaxHeader(String header) {
        faxes.setFaxHeader(header);
    }

    /**
     * <mutator>
     * To set up fax local ID for fax machine
     *
     * @param localID new value of device's fax-machine localId
     * @see FaxMachineEngine#setFaxLocalID(String)
     */
    @Override
    public void setFaxLocalID(String localID) {
        faxes.setFaxLocalID(localID);
    }

    /**
     * <action>
     * To receive the fax document.
     *
     * @param target            the stream for saving data of the received fax document in a TIFF format
     * @param pollingMode       flag, to initiate receive of a fax in a polling mode;
     * @param issueVoiceRequest upon termination of receive to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * @see FaxMachineEngine#receive(OutputStream, boolean, boolean)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue receive(OutputStream target, boolean pollingMode, boolean issueVoiceRequest) {
        return isDeviceOpened() && faxes.canFax()
                ? delegateFaxReceive(target, pollingMode, issueVoiceRequest)
                : Result.ERROR;
    }

    // to delegate call to the particular device's part engine
    private OperationResultValue delegateFaxReceive(OutputStream target, boolean pollingMode, boolean issueVoiceRequest) {
//        setState(RECVFAX);
        try {
            return faxes.receive(target, pollingMode, issueVoiceRequest);
        } finally {
//            setState(Device.State.IDLE);
        }
    }

    /**
     * <action>
     * To transmit the fax document.
     *
     * @param source            stream to fax data
     * @param format            format of data in the stream(resolution is a field)
     * @param issueVoiceRequest upon termination of reception to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * @see Fax
     * @see FaxMachineEngine#transmit(InputStream, Fax, boolean)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue transmit(InputStream source, Fax format, boolean issueVoiceRequest) {
        return isDeviceOpened() && faxes.canFax()
                ? delegateFaxTransmit(source, format, issueVoiceRequest)
                : Result.ERROR;
    }

    // to delegate call to the particular device's part engine
    private OperationResultValue delegateFaxTransmit(InputStream source, Fax format, boolean issueVoiceRequest) {
//        setState(SENDFAX);
        try {
            return faxes.transmit(source, format, issueVoiceRequest);
        } finally {
//            setState(Device.State.IDLE);
        }
    }

    /**
     * <accessor>
     * Returns the array of supported audio formats for playing back,
     * null if playback is not supported
     *
     * @return the array of the formats supported by device or null
     * @see Audio
     * @see MultiMedeaEngine#canPlay()
     */
    @Override
    public Audio[] canPlay() {
        return isDeviceOpened() ? media.canPlay() : null;
    }

    /**
     * <accessor>
     * To get access to audio format to play raw data (without header)
     *
     * @return the format for the play or null if device can't play back
     * @see Audio
     * @see MultiMedeaEngine#getRawFormat()
     */
    @Override
    public Audio getRawFormat() {
        return isDeviceOpened() ? media.getRawFormat() : null;
    }

    /**
     * <action>
     * Playback the audio stream.
     *
     * @param source                 the input stream, from which undertake sound data for playback in a telephone line
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @param format                 parameter determining type of the decoder for transformation the sound data
     * @return the operation's result
     * @see MultiMedeaEngine#playbackAudio(InputStream, String, int, Audio)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue playbackAudio(final InputStream source, final String terminationSymbolsMask,
                                              final int timeout, final Audio format) {
        return media.canPlay(format)
                ? delegateMediaOperation(PLAY, () -> media.playbackAudio(source, terminationSymbolsMask, timeout, format))
                : Result.ERROR;
    }


    /**
     * <accessor>
     * Returns the array of supported audio formats for recording,
     * null if record is not supported
     *
     * @return the array of the record formats supported by device or null
     * @see Audio
     * @see MultiMedeaEngine#canRecord()
     */
    @Override
    public Audio[] canRecord() {
        return isDeviceOpened() ? media.canRecord() : null;
    }

    /**
     * <accessor>
     * To get access to the default audio format of recording
     *
     * @return the default format for the voice record operation or null if device can't record
     * @see Audio
     * @see MultiMedeaEngine#getRecordFormat()
     */
    @Override
    public Audio getRecordFormat() {
        return isDeviceOpened() ? media.getRecordFormat() : null;
    }

    /**
     * <action>
     * Recording the audio from telephone line.
     *
     * @param target                 the output stream where recorded data will be placed
     * @param terminationSymbolsMask set of symbols finishing up the recording (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param silence                time (seconds) how long silence in a line is allowed, after which the record operation be finished.
     * @param timeout                maximum time of recording in seconds
     * @param format                 parameter determining type of the record audio data
     * @return the operation's result
     * @see MultiMedeaEngine#recordAudio(OutputStream, String, int, int, Audio)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue recordAudio(final OutputStream target, final String terminationSymbolsMask,
                                            final int silence, final int timeout, final Audio format) {
        return media.canRecord(format)
                ? delegateRecordAudio(target, terminationSymbolsMask, silence, timeout, format)
                : Result.ERROR;
    }

    /**
     * <action>
     * To dial DTMF symbols to phone line
     *
     * @param toDial sequence of symbols to dial, like "555#1234*"
     * @see TonesEngine#dial(String)
     */
    @Override
    public void dial(String toDial) {
        delegateToneAction(DIAL, () -> tones.dial(toDial));
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
     * @see TonesEngine#playTone(ToneId, float)
     */
    @Override
    public void playTone(ToneId toneId, float time) {
        delegateToneAction(TONE, () -> tones.playTone(toneId, time));
    }

    /**
     * <action>
     * To receive the user input from the telephony line.
     *
     * @param digitsCount            quantity of expected symbols
     * @param timeout                maximal waiting time (seconds) of input of next symbol
     * @param terminationSymbolsMask set of symbols finishing up the user input (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".<BR/>
     *                               The symbol finished up the input from the <b>terminationSymbolsMask</b>
     *                               will not be placed to the buffer of input symbols
     * @return the operation's result
     * @see TonesEngine#inputDigits(int, int, String)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue inputDigits(int digitsCount, int timeout, String terminationSymbolsMask) {
        return isDeviceOpened()
                ? delegateMediaOperation(GTDIG, () -> tones.inputDigits(digitsCount, timeout, terminationSymbolsMask))
                : Result.ERROR;
    }


    /**
     * <accessor>
     * To take entered symbols.<BR/>
     * The string of the input symbols from the buffer comes back.<BR/>
     * Internal input buffer will be cleaned
     *
     * @return digits sequence accepted by user's input
     * @see TonesEngine#inputDigits(int, int, String)
     */
    @Override
    public String getInputSymbols() {
        return isDeviceOpened() ? delegateInputSymbols() : "";
    }

    // to delegate call to the particular device's part engine
    private String delegateInputSymbols() {
//        setState(GTDIG);
        try {
            return tones.getInputSymbols();
        } finally {
//            setState(Device.State.IDLE);
        }
    }

    // unified delegation of phone call operation for particular device
    private PhoneCall delegatePhoneCallOperation(final DeviceStateValue operationInitState,
                                                 final Supplier<PhoneCall> operation,
                                                 final Predicate<OperationResultValue> validResults) {
        // checking device's handle value
        if (!isDeviceOpened()) {
            // device isn't opened yet
//            setState(Device.State.CLOSED);
            return PhoneCall.FAILED;
        } else {
            // running the operation's call sequence
//            setState(operationInitState);
            // waiting for the operation's complete
            final PhoneCall result = operation.get();
            // preparing new device state
            final DeviceStateValue operationResultDeviceState = validResults.test(result.operationResult())
                    ? Device.State.IDLE
                    : result.operationResult() == Result.TERMINATED ? Device.State.STOPD : Device.State.ERROR;
            // setting up the device state according the operation's result
//            setState(operationResultDeviceState);
            // returning the phone call instance
            return result;
        }
    }

    // unified delegation to the proper tone-engine related action
    private void delegateToneAction(final DeviceStateValue actionState, final Runnable action) {
        if (isDeviceOpened()) {
            // running the action's call sequence
//            setState(actionState);
            // waiting for the action's complete
            action.run();
            // setting up the device state according the action's result
//            setState(Device.State.IDLE);
        }
    }

    // to delegate call to the particular device's part engine
    private OperationResultValue delegateRecordAudio(final OutputStream target, final String terminationSymbolsMask,
                                                     final int silence, final int timeout, final Audio format) {
        return delegateMediaOperation(RECORD,
                () -> media.recordAudio(target, terminationSymbolsMask, silence, timeout, format)
        );
    }

    // unified delegation of media operation for particular device
    private OperationResultValue delegateMediaOperation(final DeviceStateValue operationInitState,
                                                        final Supplier<OperationResultValue> operation) {
        // checking device's handle value
        if (!isDeviceOpened()) {
            // device isn't opened yet
//            setState(Device.State.CLOSED);
            return Result.ERROR;
        } else {
            // running the operation's call sequence
//            setState(operationInitState);
            try {
                // waiting for the operation's complete
                // returning the phone call instance
                return operation.get();
            } finally {
                // setting up the device state according the operation's result
//                setState(Device.State.IDLE);
            }
        }
    }

    // to check is device has valid handle
    private boolean isDeviceOpened() {
        return true;
//        return validResourceHandle.test(getHandle());
    }
}
