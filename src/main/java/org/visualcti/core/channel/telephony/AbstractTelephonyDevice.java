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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.AbstractDevice;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultiMedeaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Fax;
import org.visualcti.media.Sound;

/**
 * Abstract Device of the Channel: The root device through which task communicate with
 * <p>
 * <b>Computer Telephony Equipment</b>
 *
 * @see AbstractDevice
 * @see TelephonyDevice
 * @see TelephonyDeviceFactory
 * @see CallsPortEngine
 * @see TonesEngine
 * @see MultiMedeaEngine
 * @see FaxMachineEngine
 */
public class AbstractTelephonyDevice<T extends TelephonyDeviceFactory<?>> extends AbstractDevice<T> implements TelephonyDevice<T> {
    // the channel-device configured parameters map
    private final Map<ParameterName, ConfigurationParameter> parameters = new ConcurrentHashMap<>();
    // the name of the device in the device factory
    private final String name;
    // device part of the telephony calls management
    private final CallsPortEngine calls;
    // device part of the telephony signals and tones management
    private final TonesEngine tones;
    // device part of the telephony multi-medea (playback/record) management
    private final MultiMedeaEngine media;
    // device part of the telephony fax-document exchange management
    private final FaxMachineEngine faxes;

    /**
     * <builder>
     * The constructor of the telephony device with parts instance
     *
     * @param name the name of the device in the device factory
     * @param calls device part of the telephony calls management
     * @param tones device part of the telephony signals and tones management
     * @param media device part of the telephony multi-medea (playback/record) management
     * @param faxes device part of the telephony fax-document exchange management
     */
    protected AbstractTelephonyDevice(
            final String name,
            final CallsPortEngine calls, final TonesEngine tones,
            final MultiMedeaEngine media, final FaxMachineEngine faxes
    ) {
        this.name = name;
        this.calls = calls;
        this.tones = tones;
        this.media = media;
        this.faxes = faxes;
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
    @Override
    public <H> H getHandle() {
        throw new UnsupportedOperationException("Not supported here.");
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
    @Override
    public Optional<ConfigurationParameter> getParameter(ParameterName name) {
        return Optional.ofNullable(parameters.get(name));
    }

    /**
     * <action>
     * To break off telephony connection using delegation.
     *
     * @see CallsPortEngine#dropCall()
     */
    @Override
    public void dropCall() {
        if (calls != null) {
            calls.dropCall();
        }
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
        return calls != null ? calls.waitForCall(rings, timeout, answer) : PhoneCall.FAILED;
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
        return calls != null ? calls.makeCall(number, timeout) : PhoneCall.FAILED;
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
        return calls != null ? calls.connect(number, timeout, toPlay) : PhoneCall.FAILED;
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
        return faxes != null ? faxes.getTransferredPages() : 0;
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
        return faxes != null ? faxes.getRemoteID() : "";
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
        if (faxes != null) {
            faxes.setFaxHeader(header);
        }
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
        if (faxes != null) {
            faxes.setFaxLocalID(localID);
        }
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
        return faxes != null ? faxes.receive(target, pollingMode, issueVoiceRequest) : Result.ERROR;
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
        return faxes != null ? faxes.transmit(source, format, issueVoiceRequest) : Result.ERROR;
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
        return media != null ? media.canPlay() : null;
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
        return media != null ? media.getRawFormat() : null;
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
    public OperationResultValue playbackAudio(InputStream source, String terminationSymbolsMask, int timeout, Audio format) {
        return media != null ? media.playbackAudio(source, terminationSymbolsMask, timeout, format) : Result.ERROR;
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
        return media != null ? media.canRecord() : null;
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
        return media != null ? media.getRecordFormat() : null;
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
    public OperationResultValue recordAudio(OutputStream target, String terminationSymbolsMask, int silence, int timeout, Audio format) {
        return media != null ? media.recordAudio(target, terminationSymbolsMask, silence, timeout, format) : Result.ERROR;
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
        if (tones != null) {
            tones.dial(toDial);
        }
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
        if (tones != null) {
            tones.playTone(toneId, time);
        }
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
        return tones != null ? tones.inputDigits(digitsCount, timeout, terminationSymbolsMask) : Result.ERROR;
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
        return tones != null ? tones.getInputSymbols() : "";
    }
}
