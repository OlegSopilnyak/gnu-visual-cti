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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TelephonyDevicePart;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Fax;
import org.visualcti.media.Sound;
import org.visualcti.server.core.unit.ServerUnit;


/**
 * Device of the Telephony Channel: The root device through which task communicate with computer telephony equipment
 *
 * @param <H> the type of the telephony device's low-level operations handle
 * @param <F> the type of the devices factory
 * @see TelephonyDeviceFactory
 * @see Device
 * @see CallsPortEngine
 * @see TonesEngine
 */
public interface TelephonyDevice<H, F extends TelephonyDeviceFactory<H, ?>> extends Device<H, F>,
        // core stuff of the telephony device
        TelephonyDeviceCore<H>,
        // phone calls control engine
        CallsPortEngine<H>,
        // phone line's the tones generator and the user input getter
        TonesEngine<H>,
        // phone line's playback record features engine
        MultimediaEngine<H>,
        // phone line's fax-machine features engine
        FaxMachineEngine<H> {
    //
    // the value of type the device as the server unit
    String UNIT_TYPE = "[telephony-channel-device]";
    // the array of the hardware parameter names to get from telephony service provider
    ParameterName[] HARDWARE = new ParameterName[]{
            // the name for the list of audio formats supported by telephony device
            MultimediaEngine.Parameter.ALLOWED_CODECS
    };

    /**
     * <accessor>
     * To get the body of unit's Icon Image (GIF | JPEG)
     *
     * @return the value
     */
    @Override
    default byte[] getIcon() {
        return new byte[0];
    }

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     * @see ServerUnit#getType()
     */
    @Override
    default String getType() {
        return UNIT_TYPE;
    }

    /**
     * <accessor>
     * To get access to device's name
     *
     * @return the value
     * @see ServerUnit#getName()
     */
    @Override
    String getName();

    /**
     * <accessor>
     * To get Path to unit instance in repository
     *
     * @return the value
     */
    @Override
    default String getPath() {
        return "";
    }

    /**
     * <accessor>
     * To get the hardware parameters used in the device
     *
     * @return stream to parameter names
     * @see ParameterName
     * @see Collection
     * @see Device#hardwareParameterNames()
     * @see Arrays#asList(Object[])
     */
    @Override
    default Collection<ParameterName> hardwareParameterNames() {
        return Arrays.asList(HARDWARE);
    }

    /**
     * <builder>
     * To create the session for the opened device resource handle
     *
     * @param openedDeviceHandle the handle of the opened device resource
     * @return built device session
     * @see DefaultTelephonyCall
     */
    @Override
    default Session<H> createSessionFor(H openedDeviceHandle) {
        return new DefaultTelephonyCall<>(this, openedDeviceHandle);
    }

    /**
     * <notify>
     * To notify, about device's session state changed
     *
     * @param session the session with new value of the state
     * @see Session#getState()
     */
    @Override
    default void stateChangedFor(Session<H> session) {

    }

    /**
     * <accessor>
     * To get access to the current device's telephony events provider
     *
     * @return the reference to the events provider singleton
     * @see TelephonyServiceProvider
     */
    @Override
    default TelephonyServiceProvider<H> getProvider() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    boolean waitForCall(PhoneCallSession<H> session, int rings, int timeout, boolean answer);

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
     * @param session the phone call's session, device is working with
     * @param number  the telephone number is calling to
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     * @see PhoneCall.Number
     */
    boolean makeCall(PhoneCallSession<H> session, PhoneCall.Number number, int timeout);

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
     * @param session the phone call's session, device is working with
     * @param number  the telephone number is calling to
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCallSession#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @param toPlay  The sound which is playing during the connect operation
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see PhoneCallSession#operationResult()
     * @see Result.CALL.Analysis
     * @see PhoneCall.Number
     */
    boolean connect(PhoneCallSession<H> session, PhoneCall.Number number, int timeout, Sound toPlay);

    /**
     * <accessor>
     * To check, whether device can operate with fax-machines
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can accept the incoming phone call
     * @see FaxMachineEngine#canFax()
     * @see TelephonyServiceProvider#canFax(String)
     * @see #getProvider()
     * @see #getName()
     * @see #getParameter(Device.ParameterName)
     * @see Device.ParameterName
     * @see ConfigurationParameter#getValue()
     * @see FaxMachineEngine.Parameter#FAX_ALLOWED
     */
    @Override
    default boolean canFax() {
        return getProvider().canFax(getName())
                && getParameter(FaxMachineEngine.Parameter.FAX_ALLOWED).<Boolean>map(ConfigurationParameter::getValue)
                .orElse(false);
    }

    /**
     * <accessor>
     * To get the quantity of the transferred fax-pages
     *
     * @param session the phone call's session, device is working with
     * @return how many pages transferred
     */
    @Override
    default int getTransferredPages(PhoneCallSession<H> session) {
        return 0;
    }

    /**
     * <accessor>
     * To get the local ID of the remote fax machine
     *
     * @param session the phone call's session, device is working with
     * @return localId of the remote fax-machine
     */
    @Override
    default String getRemoteID(PhoneCallSession<H> session) {
        return "";
    }

    /**
     * <mutator>
     * To set up the heading of page of the fax-document
     *
     * @param session the phone call's session, device is working with
     * @param header  the new value
     */
    @Override
    default void setFaxHeader(PhoneCallSession<H> session, String header) {

    }

    /**
     * <mutator>
     * To set up fax local ID for fax machine
     *
     * @param session the phone call's session, device is working with
     * @param localID new value of device's fax-machine localId
     */
    @Override
    default void setFaxLocalID(PhoneCallSession<H> session, String localID) {

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
     * @return the operation's result<p>
     * {@link Result.IO#EOF} - normal end of document transferring<br>
     * {@link Result.CALL#DISCONNECT} - the receiving is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of transfer starting)<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document receiving<br>
     * {@link Result.FAX#POLLING} - the inquiry on polling from the remote fax-device is received<br>
     * {@link Result.FAX#NO_POLL} - the remote fax-device has not accepted inquiry on polling<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-machine is not compatible with device's one
     * @see OperationResultValue
     */
    @Override
    default OperationResultValue receive(PhoneCallSession<H> session, OutputStream target, boolean pollingMode, boolean issueVoiceRequest) {
        return Result.ERROR;
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
     * <p>
     * {@link Result.IO#EOF} - normal end of the transmitted document<br>
     * {@link Result.CALL#DISCONNECT} - the transmitting is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of reception or transfer)<br>
     * {@link Result.IO#FORMAT} - the format of the data in the transmitted file is not supported by fax-device<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document transmitting<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-device is not compatible or can't accept a fax with the given resolution<br>
     * @see Fax
     * @see OperationResultValue
     */
    @Override
    default OperationResultValue transmit(PhoneCallSession<H> session, InputStream source, Fax format, boolean issueVoiceRequest) {
        return Result.ERROR;
    }

    /**
     * <action>
     * To create and dispatch the error-type message from the device
     *
     * @param exception   the cause of the error
     * @param description the description of the error
     */
    @Override
    default void dispatchError(Throwable exception, String description) {
        Device.super.dispatchError(exception, description);
    }
//
//    /**
//     * <mutator>
//     * to add unit to the server unit composite units tree as a branch
//     *
//     * @param branch the unit to add as a branch
//     * @see ServerUnit
//     * @see #add(ServerUnit)
//     */
//    @Override
//    default void addBranch(ServerUnit branch) {
//
//    }
//
//    /**
//     * <mutator>
//     * to remove the branch from the server unit's units tree
//     *
//     * @param branch the unit to remove from composite tree
//     * @see ServerUnit
//     * @see #remove(ServerUnit)
//     */
//    @Override
//    default void removeBranch(ServerUnit branch) {
//
//    }
//
//    /**
//     * <accessor>
//     * To get access to the owner of this composite (null for root unit)
//     *
//     * @return the reference to server composite's owner or null if it isn't exists
//     * @see ServerUnit
//     */
//    @Override
//    default ServerUnit getOwner() {
//        return null;
//    }
//
//    /**
//     * <mutator>
//     * To set new owner of this composite (null for the root unit)
//     *
//     * @param owner new value of composite's owner
//     * @throws IOException if cannot reregister unit (or children) in units registry
//     * @see ServerUnit
//     * @see UnitRegistry#register(ServerUnit)
//     */
//    @Override
//    default void setOwner(ServerUnit owner) throws IOException {
//
//    }

    /**
     * <config>
     * To configure the unit, using information from XML Element
     *
     * @param configuration new configuration value of the unit
     * @see Element
     */
    @Override
    default void configure(Element configuration) {

    }

    /**
     * <accessor>
     * Returns the array of supported audio formats for playing back,
     * null if playback is not supported
     *
     * @return the array of the supported playback formats supported by device or null if device can't play back
     */
    @Override
    default Audio[] canPlay() {
        return null;
    }

    /**
     * <accessor>
     * To get access to audio format to play raw data (without header)
     *
     * @return the format for the play or null if device can't play back
     */
    @Override
    default Audio getRawFormat() {
        return null;
    }

    /**
     * <action>
     * Playback the audio stream data.
     *
     * @param session                the phone call's session, device is working with
     * @param source                 the input stream, from which undertake sound data for playback in a telephone line
     * @param format                 parameter determining type of the decoder for transformation the sound data
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return the operation's result<p>
     * {@link Result.IO#EOF} - the playback reached end of stream;
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols(PhoneCallSession)};<BR/>
     * {@link Result#TIMEOUT} - the time of playback was exceeded.<BR/>
     * {@link Result.CALL#DISCONNECT} - the playback is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#FORMAT} - the format of audio does not support by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     * @see OperationResultValue
     */
    @Override
    default OperationResultValue playbackAudio(
            PhoneCallSession<H> session, InputStream source, Audio format, String terminationSymbolsMask, int timeout
    ) {
        return Result.ERROR;
    }

    /**
     * <action>
     * Playback the audio stream.
     * <p>
     * Possible values of the playing back operation result:
     * <p>
     * {@link Result.IO#EOF} - the playback reached end of stream;
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols(PhoneCallSession)};<BR/>
     * {@link Result#TIMEOUT} - the time of playback was exceeded.<BR/>
     * {@link Result.CALL#DISCONNECT} - the playback is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#FORMAT} - the format of audio does not support by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     *
     * @param session                the phone call's session, device is working with
     * @param sound                  the audio sound which contains format and input stream to the media data
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @return the operation's result
     * @see OperationResultValue
     * @see Sound#getInputStream()
     * @see Sound#getFormat()
     * @see MultimediaEngine#playbackAudio(PhoneCallSession, InputStream, Audio, String, int)
     */
    default OperationResultValue playbackAudio(
            final PhoneCallSession<H> session, final Sound sound, final String terminationSymbolsMask,
            final int timeout) {
        try {
            return playbackAudio(session, sound.getInputStream(), sound.getFormat(), terminationSymbolsMask, timeout);
        } catch (IOException e) {
            dispatchError(e, "Cannot play audio sound");
            return Result.ERROR;
        }
    }

    /**
     * <accessor>
     * To get access to the default audio format of recording
     *
     * @return the default format for the voice record operation or null if device can't record
     */
    @Override
    default Audio getRecordFormat() {
        return null;
    }

    /**
     * <action>
     * Record the audio data from telephone line.
     *
     * @param session                the phone call's session, device is working with
     * @param target                 the output stream where recorded data will be placed
     * @param format                 parameter determining type of the record audio data
     * @param terminationSymbolsMask set of symbols finishing up the recording (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param silence                time (seconds) how long silence in a line is allowed, after which the record operation be finished.
     * @param timeout                maximum time of recording in seconds
     * @return the operation's result
     * <p>
     * {@link Result#TIMEOUT} - the time of audio record was exceeded.<BR/>
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols(PhoneCallSession)};<BR/>
     * {@link Result.CALL#DISCONNECT} - the record is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#SILENCE} - silence exceeded in a line;<BR/>
     * {@link Result.IO#FORMAT} - the format is not supported by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     * @see OperationResultValue
     */
    @Override
    default OperationResultValue recordAudio(
            PhoneCallSession<H> session, OutputStream target, Audio format, String terminationSymbolsMask,
            int silence, int timeout) {
        return Result.ERROR;
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
    default void dial(PhoneCallSession<H> session, String toDial) {
        TonesEngine.super.dial(session, toDial);
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
    default void playTone(PhoneCallSession<H> session, ToneId toneId, float time) {
        TonesEngine.super.playTone(session, toneId, time);
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
    default OperationResultValue inputDigits(PhoneCallSession<H> session, int digitsCount, int timeout, String terminationSymbolsMask) {
        return TonesEngine.super.inputDigits(session, digitsCount, timeout, terminationSymbolsMask);
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
    default String getInputSymbols(PhoneCallSession<H> session) {
        return TonesEngine.super.getInputSymbols(session);
    }

    /**
     * <mutator>
     * To assign device core which will be used in the device part
     *
     * @param deviceCore device core which will be used in the part's activities
     * @return concrete instance of device part
     * @see TelephonyDeviceCore
     */
    @Override
    default <P extends TelephonyDevicePart<?>> P uses(TelephonyDeviceCore<H> deviceCore) {
        return null;
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
    default void terminate(PhoneCallSession<H> session) throws IOException {

    }

    /**
     * Telephony Device States Enumeration: The states of the device
     *
     * @see Device.Session#getState()
     * @see DeviceStateValue
     */
    enum State implements DeviceStateValue {
        // device is waiting for an incoming call
        WAIT("WAIT"),
        // device is playing back outgoing media-stream
        PLAY("PLAY"),
        // device is recording incoming media-stream
        RECORD("RECORD"),
        // device is dialing the phone number in order to build outgoing phone call
        DIAL("DIAL"),
        // device is getting user input (getting digits)
        GTDIG("GET DIGITS"),
        // device is generating a tone
        TONE("TONE SEND"),
        // device's operation was terminated
        STOPD("STOPPED"),
        // device is sending a fax document
        SENDFAX("FAX SEND"),
        // device is receiving a fax document
        RECVFAX("FAX RECV");

        private final String value;

        State(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }


    /**
     * Default Implementation: Phone Call Sessions: Keeps all information about phone call
     *
     * @param <H> the type of the device's low-level operations handle
     * @see PhoneCallSession
     */
    class DefaultTelephonyCall<H> extends PhoneCallSession<H> {

        public DefaultTelephonyCall(TelephonyDevice<H, ?> deviceOwner, H deviceHandle) {
            super(deviceOwner, deviceHandle);
        }
    }
}
