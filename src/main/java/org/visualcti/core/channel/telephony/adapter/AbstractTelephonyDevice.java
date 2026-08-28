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
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.adapter.AbstractDevice;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.core.channel.telephony.operation.adapter.TelephonyTone;
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
@SuppressWarnings("unchecked")
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
        // install the parameters of device by default before updates from XML
        initializeDefaultParameters();
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
        // install the parameters of device by default before updates from XML
        initializeDefaultParameters();
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
     * Not used here
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
     * @see #isOpened()
     */
    public boolean dropCall(PhoneCallSession<H> session) {
        return isOpened() && calls.dropCall(session);
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
     * @see #isOpened()
     */
    public boolean waitForCall(PhoneCallSession<H> session, int rings, int timeout, boolean answer) {
        return isOpened() && calls.waitForCall(session, rings, timeout, answer);
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
        return isOpened() && calls.makeCall(session, number, timeout);
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
     * @see #isOpened()
     */
    @Override
    public boolean connect(PhoneCallSession<H> session, PhoneCall.Number number, int timeout, Sound toPlay) {
        return isOpened() && calls.connect(session, number, timeout, toPlay);
    }

    /**
     * <accessor>
     * To get the quantity of the transferred fax-pages
     *
     * @param session the phone call's session, device is working with
     * @return how many pages transferred
     * @see FaxMachineEngine#getTransferredPages(PhoneCallSession)
     * @see #isOpened()
     */
    @Override
    public int getTransferredPages(PhoneCallSession<H> session) {
        return isOpened() ? faxes.getTransferredPages(session) : -1;
    }

    /**
     * <accessor>
     * To get the local ID of the remote fax machine
     *
     * @param session the phone call's session, device is working with
     * @return localId of the remote fax-machine
     * @see FaxMachineEngine#getRemoteID(PhoneCallSession)
     * @see #isOpened()
     */
    @Override
    public String getRemoteID(PhoneCallSession<H> session) {
        return isOpened() ? faxes.getRemoteID(session) : "";
    }

    /**
     * <mutator>
     * To set up the header of the fax-document's pages
     *
     * @param session the phone call's session, device is working with
     * @param header  the new value
     * @see FaxMachineEngine#setFaxHeader(PhoneCallSession, String)
     * @see #isOpened()
     */
    @Override
    public void setFaxHeader(PhoneCallSession<H> session, String header) {
        if (isOpened()) {
            faxes.setFaxHeader(session, header);
        }
    }

    /**
     * <mutator>
     * To set up fax local ID for fax machine
     *
     * @param session the phone call's session, device is working with
     * @param localID new value of device's fax-machine localId
     * @see FaxMachineEngine#setFaxLocalID(PhoneCallSession, String)
     * @see #isOpened()
     */
    @Override
    public void setFaxLocalID(PhoneCallSession<H> session, String localID) {
        if (isOpened()) {
            faxes.setFaxLocalID(session, localID);
        }
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
     * @see #isOpened()
     */
    @Override
    public OperationResultValue receive(PhoneCallSession<H> session, OutputStream target, boolean pollingMode, boolean issueVoiceRequest) {
        return isOpened()
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
     * @see #isOpened()
     */
    @Override
    public OperationResultValue transmit(PhoneCallSession<H> session, InputStream source, Fax format, boolean issueVoiceRequest) {
        return isOpened()
                ? faxes.transmit(session, source, format, issueVoiceRequest)
                : Result.ERROR;
    }

    /**
     * <accessor>
     * Returns the array of supported audio formats for playing back,
     * null if playback is not supported
     *
     * @return the array of the formats supported by device or null
     * @see Audio
     * @see MultimediaEngine#canPlay()
     * @see #isOpened()
     */
    @Override
    public Audio[] canPlay() {
        return isOpened() ? media.canPlay() : null;
    }

    /**
     * <accessor>
     * To get access to audio format to play raw data (without header)
     *
     * @return the format for the play or null if device can't play back
     * @see Audio
     * @see MultimediaEngine#getRawFormat()
     * @see #isOpened()
     */
    @Override
    public Audio getRawFormat() {
        return isOpened() ? media.getRawFormat() : null;
    }

    /**
     * <accessor>
     * To get the device's default codec for playing back depends on the vendor
     *
     * @return default codec instance for recording
     */
    protected Audio defaultPlaybackCodec() {
        return Audio.LINEAR_16;
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
     * @see #isOpened()
     */
    @Override
    public OperationResultValue playbackAudio(final PhoneCallSession<H> session, final InputStream source,
                                              final Audio format, final String terminationSymbolsMask, final int timeout
    ) {
        return isOpened()
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
     * @see #isOpened()
     */
    @Override
    public boolean asyncPlaybackAudio(PhoneCallSession<H> session, Sound sound) {
        return isOpened() && media.asyncPlaybackAudio(session, sound);
    }

    /**
     * <accessor>
     * Returns the array of supported audio formats for recording,
     * null if record is not supported
     *
     * @return the array of the record formats supported by device or null
     * @see Audio
     * @see MultimediaEngine#canRecord()
     * @see #isOpened()
     */
    @Override
    public Audio[] canRecord() {
        return isOpened() ? media.canRecord() : null;
    }

    /**
     * <accessor>
     * To get access to the default audio format of recording
     *
     * @return the default format for the voice record operation or null if device can't record
     * @see Audio
     * @see MultimediaEngine#getRecordFormat()
     * @see #isOpened()
     */
    @Override
    public Audio getRecordFormat() {
        return isOpened() ? media.getRecordFormat() : null;
    }

    /**
     * <accessor>
     * To get the device's default codec for recording depends on the vendor
     *
     * @return default codec instance for recording
     */
    protected Audio defaultRecordCodec() {
        return Audio.LINEAR_16;
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
     * @see #isOpened()
     */
    @Override
    public OperationResultValue recordAudio(final PhoneCallSession<H> session, final OutputStream target, final Audio format, final String terminationSymbolsMask,
                                            final int silence, final int timeout) {
        return isOpened()
                ? media.recordAudio(session, target, format, terminationSymbolsMask, silence, timeout)
                : Result.ERROR;
    }

    /**
     * <action>
     * To dial DTMF symbols to phone line
     *
     * @param session the phone call's session, device is working with
     * @param toDial  sequence of symbols to dial, like "555#1234*"
     * @see TonesEngine#dial(PhoneCallSession, String)
     * @see #isOpened()
     */
    @Override
    public void dial(PhoneCallSession<H> session, String toDial) {
        if (isOpened()) {
            tones.dial(session, toDial);
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
     * @see TonesEngine#playTone(PhoneCallSession, ToneId, float)
     * @see #isOpened()
     */
    @Override
    public void playTone(PhoneCallSession<H> session, ToneId toneId, float time) {
        if (isOpened()) {
            tones.playTone(session, toneId, time);
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
     * @see TonesEngine#inputDigits(PhoneCallSession, int, int, String)
     * @see #isOpened()
     */
    @Override
    public OperationResultValue inputDigits(PhoneCallSession<H> session, int digitsCount, int timeout, String terminationSymbolsMask) {
        return isOpened()
                ? tones.inputDigits(session, digitsCount, timeout * 1000, terminationSymbolsMask)
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
     * @see #isOpened()
     */
    @Override
    public String getInputSymbols(PhoneCallSession<H> session) {
        return isOpened() ? tones.getInputSymbols(session) : "";
    }

    /**
     * <converter>
     * To update the entity's fields from XML
     *
     * @param xml possible entity's XML
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     * @see #configure(Element)
     * @see #settingUpBasePart(Element)
     * @see #settingUpMainPart(Element)
     */
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        // install the parameters of device by default before updates from XML
        initializeDefaultParameters();
        // applying default parameters fot the device
        applyDeviceParameters(xml.getChild(DEFAULT_ROOT));
        final String deviceName = getName();
        final List<Element> devices = xml.getChildren(DEVICE_ROOT);
        for (final Element deviceXml : devices) {
            if (deviceName.equals(deviceXml.getAttributeValue(DEVICE_NAME_ATTRIBUTE))) {
                applyDeviceParameters(deviceXml);
            }
        }
    }

    /// private methods
    // applying device's parameters from xml-element of the configuration
    private void applyDeviceParameters(final Element parametersXml) throws IOException {
        if (parametersXml != null) {
            applyDeviceNetworkParameters(parametersXml.getChild(DEVICE_NETWORK_ROOT));
            applyDeviceMediaParameters(parametersXml.getChild(DEVICE_MEDIA_ROOT));
        }
    }

    // initializing default device's parameters
    private void initializeDefaultParameters() {
        initializeDefaultNetworkParameters();
        initializeDefaultMediaParameters();
    }

    // applying device's media parameters from xml-element of the configuration
    private void applyDeviceMediaParameters(final Element parametersXml) throws IOException {
        if (parametersXml != null) {
            // processing the parameters of the device's media codecs XML elements
            final List<Element> codecs = parametersXml.getChildren(DEVICE_MEDIA_CODEC_ROOT);
            codecs.forEach(this::applyDeviceMediaCodecParameters);
            // processing the parameters of the device's media tones XML elements
            final List<Element> tones = parametersXml.getChildren(DEVICE_MEDIA_TONE_ROOT);
            tones.forEach(this::applyDeviceMediaToneParameters);
            // registering configured tones in the service provider for the current device
            registerDeviceTones();
        }
    }

    // registering configured tones in the service provider for the further device tones detection
    private void registerDeviceTones() throws IOException {
        final TelephonyServiceProvider<H> provider = getProvider();
        final H deviceHandle = provider.openResource(getName());
        provider.beginToneRegistering(deviceHandle);
        getParameter(TonesEngine.Parameter.TONES_TABLE).ifPresent(parameter ->
                parameter.<EnumMap<ToneId, TelephonyTone>>getValue()
                        .values().stream().filter(tone -> tone.getToneId() > 0)
                        .sorted(Comparator.comparingInt(TelephonyTone::getToneId))
                        .forEach(tone -> provider.registerTone(deviceHandle, tone))
        );
        provider.commitToneRegistering(deviceHandle);
        provider.closeResource(deviceHandle);
    }

    // initializing default device's media parameters
    private void initializeDefaultMediaParameters() {
        initializeDefaultMediaToneParameters();
        initializeDefaultMediaCodecParameters();
    }

    // applying device's media tones parameters table from xml-element of the configuration
    private void applyDeviceMediaToneParameters(final Element toneXml) {
        final EnumMap<ToneId, TelephonyTone> tonesTable = getParameter(TonesEngine.Parameter.TONES_TABLE)
                .<EnumMap<ToneId, TelephonyTone>>map(ConfigurationParameter::getValue)
                .orElse(null);
        if (tonesTable == null) {
            // initializing media tones parameters table
            initializeDefaultMediaToneParameters();
            // recursive method's call
            applyDeviceMediaToneParameters(toneXml);
        } else {
            final String toneType = toneXml.getAttributeValue(DEVICE_MEDIA_TONE_NAME_ATTRIBUTE);
            final String toneDefinition = toneXml.getAttributeValue(DEVICE_MEDIA_TONE_VALUE_ATTRIBUTE);
            ToneId.of(toneType).ifPresent(id -> storeTone(tonesTable, id, toneDefinition));
        }
    }

    // initializing default device's media tones parameters table
    private void initializeDefaultMediaToneParameters() {
        final EnumMap<ToneId, TelephonyTone> tones = new EnumMap<>(ToneId.class);
        storeTone(tones, ToneId.BEEP, "-1,900,0,0,0,0,0,0,0,0");
        storeTone(tones, ToneId.DIAL, "1,400,125,400,125,0,0,0,0,0");
        storeTone(tones, ToneId.BUSY, "2,500,200,0,0,55,40,55,40,4");
        storeTone(tones, ToneId.RINGBACK, "3,450,150,0,0,150,100,550,400,0");
        storeTone(tones, ToneId.DISCONNECT, "4,900,700,0,0,90,70,90,70,2");
        final Device.ParameterName tonesTable = TonesEngine.Parameter.TONES_TABLE;
        setParameter(tonesTable, ConfigurationParameter.of(tonesTable.value(), tones));
    }

    // setting up media tone into device's tones table
    private static void storeTone(final EnumMap<ToneId, TelephonyTone> tones, final ToneId toneId, final String toneAsString) {
        tones.put(toneId, new TelephonyTone(toneId, toneAsString));
    }

    // applying device's media codecs parameters from xml-element of the configuration
    private void applyDeviceMediaCodecParameters(final Element codecXml) {
        // setting up the codec parameters
        final String codecType = codecXml.getAttributeValue(DEVICE_MEDIA_CODEC_TYPE_ATTRIBUTE);
        final String codecValue = codecXml.getAttributeValue(DEVICE_MEDIA_CODEC_VALUE_ATTRIBUTE);
        if (codecType == null || codecValue == null) {
            // wrong codec XML
            return;
        }
        // getting codec from string
        final Audio codec = Audio.fromString(codecValue);
        if (codec == null) {
            dispatchError("Unknown codec value: " + codecValue);
        } else if (MultimediaEngine.Parameter.PLAYBACK_CODEC.value().equalsIgnoreCase(codecType)) {
            setupMediaCodecParameterFor(MultimediaEngine.Parameter.PLAYBACK_CODEC, codec);
        } else if (MultimediaEngine.Parameter.RECORD_CODEC.value().equalsIgnoreCase(codecType)) {
            setupMediaCodecParameterFor(MultimediaEngine.Parameter.RECORD_CODEC, codec);
        } else {
            dispatchError("Unknown codec type: " + codecType);
        }
    }

    // initializing default media codecs parameters
    private void initializeDefaultMediaCodecParameters() {
        setupMediaCodecParameterFor(MultimediaEngine.Parameter.PLAYBACK_CODEC, defaultPlaybackCodec());
        setupMediaCodecParameterFor(MultimediaEngine.Parameter.RECORD_CODEC, defaultRecordCodec());
    }

    // setting up particular codec for the device
    private void setupMediaCodecParameterFor(final Device.ParameterName codecParameter, final Audio value) {
        setParameter(codecParameter, ConfigurationParameter.of(codecParameter.value(), value));
    }

    // applying device's network parameters from xml-element of the configuration
    private void applyDeviceNetworkParameters(final Element xml) {
        if (xml != null) {
            final List<Element> parameters = xml.getChildren(ConfigurationParameter.ELEMENT);
            // processing the parameters of the device's network XML-elements
            parameters.stream().map(ConfigurationParameter::of).filter(Objects::nonNull)
                    .forEach(this::applyNetworkParameter);
        }
    }

    // applying device's network parameters by network parameter type
    private void applyNetworkParameter(final ConfigurationParameter parameter) {
        final String parameterName = parameter.getName();
        if (CallsPortEngine.Parameter.ACCEPT_CALL_ALLOWED.value().equalsIgnoreCase(parameterName)) {
            // updating can accept call device's parameter ("in")
            setParameter(CallsPortEngine.Parameter.ACCEPT_CALL_ALLOWED, parameter);
        } else if (CallsPortEngine.Parameter.MAKE_CALL_ALLOWED.value().equalsIgnoreCase(parameterName)) {
            // updating can make call device's parameter ("out")
            setParameter(CallsPortEngine.Parameter.MAKE_CALL_ALLOWED, parameter);
        } else if (CallsPortEngine.Parameter.SHARE_CALL_PORT_ALLOWED.value().equalsIgnoreCase(parameterName)) {
            // updating can share port device's parameter ("share")
            setParameter(CallsPortEngine.Parameter.SHARE_CALL_PORT_ALLOWED, parameter);
        } else if (CallsPortEngine.Parameter.ORIGIN.value().equalsIgnoreCase(parameterName)) {
            final PhoneCall.Number origin = PhoneNumber.of(parameter.getValue());
            // updating origin phone number
            setParameter(CallsPortEngine.Parameter.ORIGIN, ConfigurationParameter.of(parameter.getName(), origin));
        } else if (FaxMachineEngine.Parameter.FAX_ALLOWED.value().equalsIgnoreCase(parameterName)) {
            // updating can fax port device's parameter ("fax")
            setParameter(FaxMachineEngine.Parameter.FAX_ALLOWED, parameter);
        } else {
            dispatchError("Unknown parameter: " + parameterName);
        }
    }

    // initializing default device's network parameters
    private void initializeDefaultNetworkParameters() {
        setupNetworkParameterFor(CallsPortEngine.Parameter.ACCEPT_CALL_ALLOWED, true);
        setupNetworkParameterFor(CallsPortEngine.Parameter.MAKE_CALL_ALLOWED, true);
        setupNetworkParameterFor(CallsPortEngine.Parameter.SHARE_CALL_PORT_ALLOWED, false);
        setupNetworkParameterFor(CallsPortEngine.Parameter.ORIGIN, PhoneNumber.domesticOf(123));
    }

    // setting up particular network parameters for the device
    private void setupNetworkParameterFor(final Device.ParameterName networkParameter, final Boolean value) {
        setParameter(networkParameter, ConfigurationParameter.of(networkParameter.value(), value));
    }
    // setting up particular network parameters for the device
    private void setupNetworkParameterFor(final Device.ParameterName networkParameter, final Object value) {
        setParameter(networkParameter, ConfigurationParameter.of(networkParameter.value(), value));
    }
}
