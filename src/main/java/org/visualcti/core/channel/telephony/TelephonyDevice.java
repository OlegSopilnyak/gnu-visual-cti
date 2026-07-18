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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultiMedeaEngine;
import org.visualcti.core.channel.telephony.part.TelephonyDevicePart;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Fax;
import org.visualcti.media.Sound;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.unit.ServerUnit;


/**
 * Device of the Telephony Channel: The root device through which task communicate with computer telephony equipment
 *
 * @param <H> the type of the device's low-level operations handle
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
        MultiMedeaEngine<H>,
        // phone line's fax-machine features engine
        FaxMachineEngine<H>
{
    //
    // the value of type the device as the server unit
    String UNIT_TYPE = "[telephony-channel-device]";

    /**
     * <accessor>
     * To get body unit's Icon Image (gif | jpeg)
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
     * To get reference to the channel-devices service provider to do this channel-device low-level operations
     *
     * @return the service provider associated with the channel-device
     * @see ServiceProvider
     */
    @Override
    default ServiceProvider<H> serviceProvider() {
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
     * @see Device#getParameter(ParameterName)
     * @see CallsPortEngine.CallParameter
     */
    @Override
    Optional<ConfigurationParameter> getParameter(ParameterName name);

    /**
     * <builder>
     * To create the session for the opened device resource handle
     *
     * @param openedDeviceHandle the handle of the opened device resource
     * @return built device session
     * @throws IOException if device cannot create the session for device handle
     * @see Session
     */
    @Override
    default Session<H> createSessionFor(H openedDeviceHandle) throws IOException {
        return null;
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
        return null;
    }

    /**
     * <action>
     * To break off telephone connection.
     */
    @Override
    default void dropCall() {

    }

    /**
     * <accessor>
     * To check, whether device can accept incoming calls
     * This flag, the factory may set up it in the properties of the device
     *
     * @return true if device can accept incoming phone calls
     * @see CallsPortEngine#canAcceptCall()
     */
    @Override
    default boolean canAcceptCall() {
        return (boolean) getParameter(CallParameter.ACCEPT_CALL_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
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
     * @param rings   the quantity of ring signals before answering the call
     * @param timeout waiting time (seconds) how many seconds wait before timeout status returned
     * @param answer  flag is needed answer to an incoming call
     * @return the phone call with appropriate operation result
     * @see PhoneCall
     * @see PhoneCall#operationResult()
     */
    @Override
    default PhoneCall waitForCall(int rings, int timeout, boolean answer) {
        return null;
    }

    /**
     * <accessor>
     * To check, whether device can make the outgoing call
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can make outgoing calls
     * @see CallsPortEngine#canMakeCall()
     */
    @Override
    default boolean canMakeCall() {
        return (boolean) getParameter(CallParameter.MAKE_CALL_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
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
     * @param number  telephone number
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCall#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @return the phone call with appropriate operation result
     * @see PhoneCall
     * @see PhoneCall#operationResult()
     * @see Result.CALL.Analysis
     */
    @Override
    default PhoneCall makeCall(String number, int timeout) {
        return null;
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
    default boolean canBeConnected() {
        return (boolean) getParameter(CallParameter.SHARE_CALL_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
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
     * @param number  telephone number
     * @param timeout maximal waiting time for the answer (sec) after which call with
     *                {@link PhoneCall#operationResult()} equals {@link Result.CALL.Analysis#NO_ANSWER} will be returned.
     * @param toPlay  The sound which is playing during the connect operation
     * @return the phone call with appropriate operation result
     * @see PhoneCall
     * @see PhoneCall#operationResult()
     * @see Result.CALL.Analysis
     */
    @Override
    default PhoneCall connect(String number, int timeout, Sound toPlay) {
        return null;
    }

    /**
     * <accessor>
     * To check, whether device can operate with fax-machhines
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can accept the incoming phone call
     * @see FaxMachineEngine#canFax()
     */
    @Override
    default boolean canFax() {
        dispatchError(null, "");
        return (boolean) getParameter(CallParameter.FAX_ALLOWED)
                .map(ConfigurationParameter::getValue).orElse(false);
    }

    /**
     * <accessor>
     * To get the quantity of the transferred fax-pages
     *
     * @return how many pages transferred
     */
    @Override
    default int getTransferredPages() {
        return 0;
    }

    /**
     * <accessor>
     * To get the local ID of the remote fax machine
     *
     * @return localId of the remote fax-machine
     */
    @Override
    default String getRemoteID() {
        return "";
    }

    /**
     * <mutator>
     * To set up the heading of page of the fax-document
     *
     * @param header the new value
     */
    @Override
    default void setFaxHeader(String header) {

    }

    /**
     * <mutator>
     * To set up fax local ID for fax machine
     *
     * @param localID new value of device's fax-machine localId
     */
    @Override
    default void setFaxLocalID(String localID) {

    }

    /**
     * <action>
     * To receive the fax document.
     *
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
    default OperationResultValue receive(OutputStream target, boolean pollingMode, boolean issueVoiceRequest) {
        return null;
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
    default OperationResultValue transmit(InputStream source, Fax format, boolean issueVoiceRequest) {
        return null;
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

    /**
     * <mutator>
     * to add unit to the server unit composite units tree as a branch
     *
     * @param branch the unit to add as a branch
     * @see ServerUnit
     * @see #add(ServerUnit)
     */
    @Override
    default void addBranch(ServerUnit branch) {

    }

    /**
     * <mutator>
     * to remove the branch from the server unit's units tree
     *
     * @param branch the unit to remove from composite tree
     * @see ServerUnit
     * @see #remove(ServerUnit)
     */
    @Override
    default void removeBranch(ServerUnit branch) {

    }

    /**
     * <accessor>
     * To get access to the owner of this composite (null for root unit)
     *
     * @return the reference to server composite's owner or null if it isn't exists
     * @see ServerUnit
     */
    @Override
    default ServerUnit getOwner() {
        return null;
    }

    /**
     * <mutator>
     * To set new owner of this composite (null for the root unit)
     *
     * @param owner new value of composite's owner
     * @throws IOException if cannot reregister unit (or children) in units registry
     * @see ServerUnit
     * @see UnitRegistry#register(ServerUnit)
     */
    @Override
    default void setOwner(ServerUnit owner) throws IOException {

    }

    /**
     * <accessor>
     * To get access to the composite units tree as Stream
     *
     * @return the stream to the units list managed by composite
     * @see Stream
     * @see ServerUnit
     */
    @Override
    default Stream<ServerUnit> children() {
        return Stream.empty();
    }

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
     * To get ServerUnit instance properties
     * may use for visual editing in GUI
     *
     * @return server unit properties
     */
    @Override
    default Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

    /**
     * <mutator>
     * To assign properties to ServerUnit instance
     * Properties may be changed in GUI
     *
     * @param properties server unit properties
     */
    @Override
    default void setProperties(Map<String, Object> properties) {

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
        return new Audio[0];
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
     * Playback the audio stream.
     * <p>
     * Possible values of the playing back operation result:
     * <p>
     * {@link Result.IO#EOF} - the playback reached end of stream;
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols()};<BR/>
     * {@link Result#TIMEOUT} - the time of playback was exceeded.<BR/>
     * {@link Result.CALL#DISCONNECT} - the playback is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#FORMAT} - the format of audio does not support by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     *
     * @param source                 the input stream, from which undertake sound data for playback in a telephone line
     * @param terminationSymbolsMask set of symbols finishing up the playing (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param timeout                maximum time of playing back in seconds (-1 for unlimited, waiting for end of stream)
     * @param format                 parameter determining type of the decoder for transformation the sound data
     * @return the operation's result
     * @see OperationResultValue
     * @see TonesEngine#getInputSymbols()
     */
    @Override
    default OperationResultValue playbackAudio(InputStream source, String terminationSymbolsMask, int timeout, Audio format) {
        return null;
    }

    /**
     * <accessor>
     * Returns the array of supported audio formats for recording,
     * null if record is not supported
     *
     * @return the array of the record formats supported by device or null if device can't record
     */
    @Override
    default Audio[] canRecord() {
        return new Audio[0];
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
     * Record the audio from telephone line.
     * <p>
     * Possible values of the playing back operation result:
     * <p>
     * {@link Result#TIMEOUT} - the time of audio record was exceeded.<BR/>
     * {@link Result.IO#DTMF} - the playback is interrupted by symbol from the termination mask.<BR/>
     * The symbol, which cause the playback interruption can be got by the {@link TonesEngine#getInputSymbols()};<BR/>
     * {@link Result.CALL#DISCONNECT} - the record is interrupted by telephony line disconnection;<BR/>
     * {@link Result.IO#SILENCE} - silence exceeded in a line;<BR/>
     * {@link Result.IO#FORMAT} - the format is not supported by device.<BR/>
     * {@link Result#TERMINATED} - the operation is interrupted by system.
     *
     * @param target                 the output stream where recorded data will be placed
     * @param terminationSymbolsMask set of symbols finishing up the recording (mask). The mask is passed to the method
     *                               as any combination of comma separated symbols<BR/>(0-9,*,#), for example: " 1, 2, #, 0 ".
     * @param silence                time (seconds) how long silence in a line is allowed, after which the record operation be finished.
     * @param timeout                maximum time of recording in seconds
     * @param format                 parameter determining type of the record audio data
     * @return the operation's result
     * @see OperationResultValue
     * @see TonesEngine#getInputSymbols()
     */
    @Override
    default OperationResultValue recordAudio(OutputStream target, String terminationSymbolsMask, int silence, int timeout, Audio format) {
        return null;
    }

    /**
     * <action>
     * To dial DTMF symbols to phone line
     *
     * @param toDial sequence of symbols to dial, like "555#1234*"
     */
    @Override
    default void dial(String toDial) {

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
    @Override
    default void playTone(ToneId toneId, float time) {

    }

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
    @Override
    default OperationResultValue inputDigits(int digitsCount, int timeout, String terminationSymbolsMask) {
        return null;
    }

    /**
     * <accessor>
     * To take entered symbols.<BR/>
     * The string of the input symbols from the buffer comes back.<BR/>
     * Internal input buffer will be cleaned
     *
     * @return digits sequence accepted by user's input
     * @see #inputDigits(int, int, String)
     */
    @Override
    default String getInputSymbols() {
        return "";
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
    default <P extends TelephonyDevicePart<?>> P use(TelephonyDeviceCore<H> deviceCore) {
        return null;
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
    default void terminate() throws IOException {

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
}
