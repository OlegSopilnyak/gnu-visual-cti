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

Contact oleg@visualcti.org or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg@visualcti.org
Home Phone:	380-62-3851086 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server.hardware.proxy.part;

import java.io.*;

import org.visualcti.media.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Interface for play audio stream</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface Player
{
/**
<accessor>
*  must return the array of supported audio formats for playback
*  null if playback is not supported
*/
Audio[] canPlay();
/**
 * <accessor>
 * To get access to default format to play raw data (without header)
 * @return the format for the play
 */
Audio getRawFormat();
/**
<action>
*    Playback.
*
*    Parameters:
*
*    source   - the input stream, from which undertake sound data for
*                 playback in a telephone line;
*    termmask - set of symbols, interrupting playback (mask). A mask
*                 is transferred by a string as any combination
*                 of symbols (0-9,*,#), divided by comma,
*                 for example: " 1, 2, #, 0 ".
*    timeout  - maximal time of playback (-1 for unlimited) seconds
*    format   - parameter determining type of the decoder for
*                    transformation the sound data.
*
*    Returned value (reason of completion):
*
*    Reason.IO.EOF     - the playback is completed;
*    Reason.IO.DTMF    - the playback is interrupted by symbol from a mask.
*                           symbol, which has caused interruption it is
*                           possible to receive by a method getDigits ();
*    Reason.IO.TIMEOUT      - the time of playback was finished.
*    Reason.CALL.DISCONNECT - the playback is interrupted owing to break of telephony connection;
*    Reason.IO.FORMAT       - the format is not supported.
*    Reason.TERMINATED      - the operation is interrupted by system.
*/
String play(InputStream source, String termmask, int timeout, Audio format);
/**
<action>
*    Playback.
*    this method may call parallel with connect(...)
*
*    Parameters:
*
*    sound    - the Pair (input stream & format) for playback in a telephone line;
*    termmask - set of symbols, interrupting playback (mask). A mask
*                 is transferred by a string as any combination
*                 of symbols (0-9,*,#), divided by comma,
*                 for example: " 1, 2, #, 0 ".
*    timeout  - maximal time of playback (-1 for unlimited) seconds
*
*    Returned value (reason of completion):
*
*    Reason.IO.EOF     - the playback is completed;
*    Reason.IO.DTMF    - the playback is interrupted by symbol from a mask.
*                           symbol, which has caused interruption it is
*                           possible to receive by a method getDigits ();
*    Reason.IO.TIMEOUT      - the time of playback was finished.
*    Reason.CALL.DISCONNECT - the playback is interrupted owing to break of telephony connection;
*    Reason.IO.FORMAT       - the format is not supported.
*    Reason.TERMINATED      - the operation is interrupted by system.
*/
String play(Sound sound, String termmask, int timeout);
/**
const value for play timeout parameter
*/
int NOTIMEOUT = -1;
}
