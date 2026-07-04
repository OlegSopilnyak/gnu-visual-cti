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
package org.visualcti.core.channel.telephony.part;

import java.io.InputStream;
import java.io.OutputStream;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.ResultValue;
import org.visualcti.media.Audio;

/**
 * The Part of the Telephony Channel Device: The root device part of the telephony multi-medea (playback/record) management
 */
public interface MultiMedeaEngine {
    /**
     * <accessor>
     * Returns the array of supported audio formats for playing back,
     * null if playback is not supported
     *
     * @return the array of the playback formats supported by device
     */
    Audio[] canPlay();

    /**
     * <accessor>
     * To get access to audio format to play raw data (without header)
     *
     * @return the format for the play
     */
    Audio getRawFormat();

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
     * @see ResultValue
     * @see TonesEngine#getInputSymbols()
     */
    ResultValue playbackAudio(InputStream source, String terminationSymbolsMask, int timeout, Audio format);

    /**
     * <accessor>
     * Returns the array of supported audio formats for recording,
     * null if record is not supported
     *
     * @return the array of the record formats supported by device
     */
    Audio[] canRecord();

    /**
     * <accessor>
     * To get access to the default audio format of recording
     *
     * @return the default format for the voice record operation
     */
    Audio getRecordFormat();

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
     * @see ResultValue
     * @see TonesEngine#getInputSymbols()
     */
    ResultValue recordAudio(OutputStream target, String terminationSymbolsMask, int silence, int timeout, Audio format);
}
