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
package org.visualcti.core.channel.telephony.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.adapter.AbstractDevice;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
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
 * @see MultimediaEngine
 * @see FaxMachineEngine
 */
public class AbstractTelephonyDevice<H, T extends TelephonyDeviceFactory<H, ?>>
        extends AbstractDevice<H, T> implements TelephonyDevice<H, T> {
    //
    // predicate to test whether device handle value is valid or not
    private final Predicate<H> validResourceHandle =
            handle -> !Objects.equals(handle, wrongHandle()) || !Objects.equals(handle, errorHandle());
    //
    // the name of the device in the device factory
    private final String name;
    // device part of the telephony calls management
    protected final CallsPortEngine<H> calls;
    // device part of the telephony signals and tones management
    protected final TonesEngine<H> tones;
    // device part of the telephony multi-medea (playback/record) management
    protected final MultimediaEngine<H> media;
    // device part of the telephony fax-document exchange management
    protected final FaxMachineEngine<H> faxes;

    /**
     * <builder>
     * To build phone calls management part of the telephony device
     *
     * @return built part instance
     * @see AbstractCallsPortEngine
     */
    protected CallsPortEngine<H> callsPart() {
        return new AbstractCallsPortEngine<H>() {
        };
    }

    /**
     * <builder>
     * To build tones management part of the telephony device
     *
     * @return built part instance
     * @see AbstractTonesEngine
     */
    protected TonesEngine<H> tonesPart() {
        return new AbstractTonesEngine<H>() {
        };
    }

    /**
     * <builder>
     * To build media part of the telephony device
     *
     * @return built part instance
     * @see AbstractMultimediaEngine
     */
    protected MultimediaEngine<H> mediaPart() {
        return new AbstractMultimediaEngine<H>() {
        };
    }

    /**
     * <builder>
     * To build fax machine part of the telephony device
     *
     * @return built part instance
     * @see AbstractFaxMachineEngine
     */
    protected FaxMachineEngine<H> faxPart() {
        return new AbstractFaxMachineEngine<H>() {
        };
    }

    /**
     * <contructor>
     * The constructor of the telephony device with prebuilt parts instance
     *
     * @param name     the name of the device in the device factory
     * @param provider the manufacturer's provider of telephony operations
     * @see #callsPart()
     * @see #tonesPart()
     * @see #mediaPart()
     * @see #faxPart()
     */
    protected AbstractTelephonyDevice(final String name, final TelephonyServiceProvider<H> provider) {
        super(provider);
        this.name = name;
        this.calls = callsPart().uses(this);
        this.tones = tonesPart().uses(this);
        this.media = mediaPart().uses(this);
        this.faxes = faxPart().uses(this);
    }

    /**
     * <builder>
     * The constructor of the telephony device with external parts instance
     *
     * @param name     the name of the device in the device factory
     * @param provider the manufacturer's provider of telephony operations
     * @param calls    device part of the telephony calls management
     * @param tones    device part of the telephony signals and tones management
     * @param media    device part of the telephony multi-medea (playback/record) management
     * @param faxes    device part of the telephony fax-document exchange management
     * @see CallsPortEngine
     * @see TonesEngine
     * @see MultimediaEngine
     * @see FaxMachineEngine
     */
    protected AbstractTelephonyDevice(final String name, final TelephonyServiceProvider<H> provider,
                                      final CallsPortEngine<H> calls, final TonesEngine<H> tones,
                                      final MultimediaEngine<H> media, final FaxMachineEngine<H> faxes) {
        super(provider);
        this.name = name;
        this.calls = calls.uses(this);
        this.tones = tones.uses(this);
        this.media = media.uses(this);
        this.faxes = faxes.uses(this);
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
     * <checker>
     * To check the value of opened device handle
     *
     * @param deviceHandle handle after open resource operation
     * @return true if value is invalid
     * @see Device#startSession()
     * @see TelephonyServiceProvider#openResource(String)
     * @see #wrongHandle()
     * @see #errorHandle()
     */
    @Override
    public boolean isInvalidHandle(H deviceHandle) {
        return validResourceHandle.negate().test(deviceHandle);
    }

    /**
     * <accessor>
     * To get access to the wrong value device's low-level handle
     *
     * @return the value for handle of unopened device
     * @see #isInvalidHandle(H)
     */
    protected H wrongHandle() {
        return null;
    }

    /**
     * <accessor>
     * To get access to the error value device's low-level handle
     *
     * @return the value for handle of corrupted device
     * @see #isInvalidHandle(H)
     */
    protected H errorHandle() {
        return null;
    }

    /**
     * <accessor>
     * To get access to the current device's telephony events provider
     *
     * @return the reference to the events provider singleton
     * @see TelephonyServiceProvider
     */
    @Override
    public TelephonyServiceProvider<H> getProvider() {
        return (TelephonyServiceProvider<H>) super.serviceProvider();
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
    public DeviceActivitySession<H> startSession() throws IOException {
        final PhoneCallSession<H> session = (PhoneCallSession<H>) super.startSession();
        // analyzing the opened device session
        if (session != null && session.isOpened()) {
            // to get telephony service provider instance
            final TelephonyServiceProvider<H> provider = getProvider();
            // filling media parameter
            // to get the device's handle from the session
            final H handle = session.getDeviceHandle();
            // stopping and detach the old session (not just created one), if any (only 1 session is allowed for the telephony device)
            findSessionByHandle(handle).filter(s -> s != session).ifPresent(this::detachAndClose);
            // disabling any event for the opened device handle
            provider.disableEvents(handle);
            // trying to open the fax-machine part
            faxes.open(session);
            // checking the fax machine device opening state
            if (faxes.isOpened(session)) {
                dispatchError("Fax Machine features aren't supported...");
            }
            // enabling device's incoming call events producing for particular device andle
            provider.enableEvents(handle, Result.CALL.RINGS);
            // sharing started session if it's possible
            if (canBeConnected()) {
                // sharing the device's session for connection forever
                getFactory().shareDevice(handle);
            }
        }
        return session;
    }

    /**
     * <action>
     * To stop device's session and detach it from device events stream
     *
     * @param session opened device's session
     * @see Device#detachAndClose(DeviceActivitySession)
     */
    @Override
    public void detachAndClose(final DeviceActivitySession<H> session) {
        if (session == null) {
            // nothing to do
            return;
        }
        // analyzing the opened device session
        if (session.isOpened()) {
            // casting the session to the telephone device session type
            final PhoneCallSession<H> phoneCallSession = (PhoneCallSession<H>) session;
            // to get the device's handle from the session
            final H handle = phoneCallSession.getDeviceHandle();
            // unsharing the device's session for connection
            getFactory().unShareDevice(handle);
            // disabling any event for the opened device handle
            getProvider().disableEvents(handle);
            // closing the fax-machine stuff if any
            if (faxes.isOpened(phoneCallSession)) {
                // releasing the fax-machine telephony device session's resources
                faxes.close(phoneCallSession);
            }
        }
        // detaching and closing the session
        super.detachAndClose(session);
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
        calls.terminate(session);
        tones.terminate(session);
        media.terminate(session);
        faxes.terminate(session);
    }

    /**
     * <checker>
     * To check is phone call session opened for this device's part
     *
     * @param session the phone call's session, device is working with
     * @return true if session opened well
     */
    @Override
    public boolean isOpened(PhoneCallSession<H> session) {
        return false;
    }

    /**
     * <action>
     * To end a phone call.
     *
     * @param session the phone call's session, device is working with
     * @return true if operation complete successfully
     * @see PhoneCallSession
     */
    public boolean dropCall(PhoneCallSession<H> session) {
        return calls.dropCall(session);
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
     * @see PhoneCallSession#operationResult()
     */
    public boolean waitForCall(PhoneCallSession<H> session, int rings, int timeout, boolean answer) {
        return calls.waitForCall(session, rings, timeout, answer);
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
     */
    @Override
    public boolean makeCall(PhoneCallSession<H> session, PhoneCall.Number number, int timeout) {
        return calls.makeCall(session, number, timeout);
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
     * In case of result call with {@link PhoneCallSession#operationResult()}
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
     * Possible values of {@link PhoneCallSession#operationResult()}:
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
     * @param session the phone call's session, device is working with
     * @param number  called telephone number
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @param toPlay  The sound which is playing during the connect operation
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     * @see PhoneCall.Number
     */
    @Override
    public boolean connect(PhoneCallSession<H> session, PhoneCall.Number number, int timeout, Sound toPlay) {
        return calls.connect(session, number, timeout, toPlay);
    }

    /**
     * <accessor>
     * To get the quantity of the transferred fax-pages
     *
     * @param session the phone call's session, device is working with
     * @return how many pages transferred
     * @see FaxMachineEngine#getTransferredPages(PhoneCallSession)
     */
    @Override
    public int getTransferredPages(PhoneCallSession<H> session) {
        return faxes.getTransferredPages(session);
    }

    /**
     * <accessor>
     * To get the local ID of the remote fax machine
     *
     * @param session the phone call's session, device is working with
     * @return localId of the remote fax-machine
     * @see FaxMachineEngine#getRemoteID(PhoneCallSession)
     */
    @Override
    public String getRemoteID(PhoneCallSession<H> session) {
        return faxes.getRemoteID(session);
    }

    /**
     * <mutator>
     * To set up the header of the fax-document's pages
     *
     * @param session the phone call's session, device is working with
     * @param header  the new value
     * @see FaxMachineEngine#setFaxHeader(PhoneCallSession, String)
     */
    @Override
    public void setFaxHeader(PhoneCallSession<H> session, String header) {
        faxes.setFaxHeader(session, header);
    }

    /**
     * <mutator>
     * To set up fax local ID for fax machine
     *
     * @param session the phone call's session, device is working with
     * @param localID new value of device's fax-machine localId
     * @see FaxMachineEngine#setFaxLocalID(PhoneCallSession, String)
     */
    @Override
    public void setFaxLocalID(PhoneCallSession<H> session, String localID) {
        faxes.setFaxLocalID(session, localID);
    }

    /**
     * <action>
     * To receive the fax document.
     *
     * @param session           the phone call's session, device is working with
     * @param target            the stream for saving data of the received fax document in a TIFF format
     * @param pollingMode       flag, to initiate receive of a fax in a polling mode;
     * @param issueVoiceRequest upon termination of receive to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * @see FaxMachineEngine#receive(PhoneCallSession, OutputStream, boolean, boolean)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue receive(PhoneCallSession<H> session, OutputStream target, boolean pollingMode, boolean issueVoiceRequest) {
        return isDeviceOpened()
                ? faxes.receive(session, target, pollingMode, issueVoiceRequest)
                : Result.ERROR;
    }

    /**
     * <action>
     * To transmit the fax document.
     *
     * @param session           the phone call's session, device is working with
     * @param source            stream to fax data
     * @param format            format of data in the stream(resolution is a field)
     * @param issueVoiceRequest upon termination of reception to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * @see Fax
     * @see FaxMachineEngine#transmit(PhoneCallSession, InputStream, Fax, boolean)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue transmit(PhoneCallSession<H> session, InputStream source, Fax format, boolean issueVoiceRequest) {
        return isDeviceOpened()
                ? faxes.transmit(session, source, format, issueVoiceRequest)
//                delegateFaxTransmit(source, format, issueVoiceRequest)
                : Result.ERROR;
    }

    // to delegate call to the particular device's part engine
    private OperationResultValue delegateFaxTransmit(InputStream source, Fax format, boolean issueVoiceRequest) {
//        setState(SENDFAX);
        try {
            return faxes.transmit(null, source, format, issueVoiceRequest);
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
     * @see MultimediaEngine#canPlay()
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
     * @see MultimediaEngine#getRawFormat()
     */
    @Override
    public Audio getRawFormat() {
        return isDeviceOpened() ? media.getRawFormat() : null;
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
     * @see MultimediaEngine#canPlay(Audio)
     * @see MultimediaEngine#playbackAudio(PhoneCallSession, InputStream, Audio, String, int)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue playbackAudio(final PhoneCallSession<H> session, final InputStream source,
                                              final Audio format, final String terminationSymbolsMask, final int timeout
    ) {
        return isDeviceOpened()
                ? media.playbackAudio(session, source, format, terminationSymbolsMask, timeout)
                : Result.ERROR;
    }

    /**
     * <action>
     * Playback the audio stream data in asynchronous mode.
     *
     * @param session the phone call's session, device is working with
     * @param sound   the audio sound playing back in a telephone line asynchronously
     * @return true if start playing the sound
     */
    @Override
    public boolean asyncPlaybackAudio(PhoneCallSession<H> session, Sound sound) {
        return isDeviceOpened() && media.asyncPlaybackAudio(session, sound);
    }

    /**
     * <accessor>
     * Returns the array of supported audio formats for recording,
     * null if record is not supported
     *
     * @return the array of the record formats supported by device or null
     * @see Audio
     * @see MultimediaEngine#canRecord()
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
     * @see MultimediaEngine#getRecordFormat()
     */
    @Override
    public Audio getRecordFormat() {
        return isDeviceOpened() ? media.getRecordFormat() : null;
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
     * @see MultimediaEngine#canRecord(Audio)
     * @see MultimediaEngine#recordAudio(PhoneCallSession, OutputStream, Audio, String, int, int)
     * @see Result#ERROR
     */
    @Override
    public OperationResultValue recordAudio(final PhoneCallSession<H> session, final OutputStream target, final Audio format, final String terminationSymbolsMask,
                                            final int silence, final int timeout) {
        return media.canRecord(format)
                ? delegateRecordAudio(session, target, terminationSymbolsMask, silence, timeout, format)
                : Result.ERROR;
    }

    /**
     * <action>
     * To dial DTMF symbols to phone line
     *
     * @param session the phone call's session, device is working with
     * @param toDial  sequence of symbols to dial, like "555#1234*"
     * @see TonesEngine#dial(PhoneCallSession, String)
     */
    @Override
    public void dial(PhoneCallSession<H> session, String toDial) {
        delegateToneAction(TelephonyDevice.State.DIAL, () -> tones.dial(session, toDial));
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
     * @see TonesEngine#playTone(PhoneCallSession, ToneId, float)
     */
    @Override
    public void playTone(PhoneCallSession<H> session, ToneId toneId, float time) {
        delegateToneAction(TelephonyDevice.State.TONE, () -> tones.playTone(session, toneId, time));
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
     * @see TonesEngine#inputDigits(PhoneCallSession, int, int, String)
     */
    @Override
    public OperationResultValue inputDigits(PhoneCallSession<H> session, int digitsCount, int timeout, String terminationSymbolsMask) {
        return isDeviceOpened()
                ? delegateMediaOperation(TelephonyDevice.State.GTDIG, () -> tones.inputDigits(session, digitsCount, timeout, terminationSymbolsMask))
                : Result.ERROR;
    }

    /**
     * <accessor>
     * To take entered symbols.<BR/>
     * The string of the input symbols from the buffer comes back.<BR/>
     * Internal input buffer will be cleaned
     *
     * @param session the phone call's session, device is working with
     * @return digits sequence accepted by user's input
     * @see TonesEngine#getInputSymbols(PhoneCallSession)
     */
    @Override
    public String getInputSymbols(PhoneCallSession<H> session) {
        return isDeviceOpened() ? delegateInputSymbols(session) : "";
    }

    // to delegate call to the particular device's part engine
    private String delegateInputSymbols(PhoneCallSession<H> session) {
//        setState(GTDIG);
        try {
            return tones.getInputSymbols(session);
        } finally {
//            setState(Device.State.IDLE);
        }
    }

    // unified delegation of phone call operation for particular device
//    private PhoneCall delegatePhoneCallOperation(final DeviceStateValue operationInitState,
//                                                 final Supplier<PhoneCall> operation,
//                                                 final Predicate<OperationResultValue> validResults) {
//        // checking device's handle value
//        if (!isDeviceOpened()) {
//            // device isn't opened yet
////            setState(Device.State.CLOSED);
//            return PhoneCall.FAILED;
//        } else {
//            // running the operation's call sequence
////            setState(operationInitState);
//            // waiting for the operation's complete
//            final PhoneCall result = operation.get();
//            // preparing new device state
//            final DeviceStateValue operationResultDeviceState = validResults.test(result.operationResult())
//                    ? Device.State.IDLE
//                    : result.operationResult() == Result.TERMINATED ? Device.State.STOPD : Device.State.ERROR;
//            // setting up the device state according the operation's result
////            setState(operationResultDeviceState);
//            // returning the phone call instance
//            return result;
//        }
//    }

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
    private OperationResultValue delegateRecordAudio(final PhoneCallSession<H> session, final OutputStream target,
                                                     final String terminationSymbolsMask,
                                                     final int silence, final int timeout, final Audio format) {
        return delegateMediaOperation(TelephonyDevice.State.RECORD,
                () -> media.recordAudio(session, target, format, terminationSymbolsMask, silence, timeout)
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
        return isOpened();
    }
}
