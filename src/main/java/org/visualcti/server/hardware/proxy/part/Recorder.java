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
package org.visualcti.server.hardware.proxy.part;

import java.io.*;

import org.visualcti.media.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Interface for record the voice to output stream</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface Recorder
{
/**
<accessor>
*  must return the array of supported audio formats for recording
*  null if recording is not supported
*/
Audio[] canRecord();
/**
 * <accessor>
 * To get access to current format to record
 * @return
 */
Audio getRecordFormat();
/**
<action>
*    Record.
*
*    Parameters:
*
*    target   - the output stream, where is made record of the sound data;
*    termmask - set of symbols, interrupting playback (mask). A mask
*                 is transferred by a string as any combination
*                 of symbols (0-9,*,#), divided by comma,
*                 for example: " 1, 2, #, 0 ".
*    silence  - time of long silence in a line, after which record
*                 the record should be completed.
*    timeout  - maximal time of record.
*    format   - parameter determining type of the coder
*                    for coding of a sound.
*
*    Returned value (reason of completion):
*
*    Reason.IO.TIMEOUT      - the time of record was finished.
*    Reason.IO.DTMF         - the record is interrupted by symbol from a mask.
*                              Symbol, which has caused interruption it is
*                              possible to receive by a method getDigits ();
*    Reason.CALL.DISCONNECT - the record is interrupted owing to break of telephony connection;
*    Reason.IO.SILENCE      - silence in a line;
*    Reason.IO.FORMAT       - the format is not supported.
*    Reason.TERMINATED      - the operation is interrupted by system.
*/
String record(OutputStream target, String termmask,int silence, int timeout);
}
