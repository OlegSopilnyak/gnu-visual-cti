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

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Interface for detect the tones</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface ToneDetector
{
/**
* <action>
*    To receive the user input from a line.
*
*    Parameters:
*
*    digitsCount    - quantity of expected symbols
*    timeout        - maximal waiting time of input of next symbol
*    termMask       - set of symbols interrupting input (mask). The mask is transferred by a line
*                     as any combination of symbols (0-9,*,#), divided by comma,
*                     For example: " 1, 2, #, 0 ".
*                     Symbol from termMask will not placed to detector's buffer
*
*    Returned value (reason of completion):
*
*    Reason.IO.DTMF         - the Accepted sequence of symbols is in the buffer
*                              of the detector.
*                              For reception of value from buffer, it is necessary
*                              to call getDigitsBuffer ().
*    Reason.IO.TIMEOUT      - in time timeout it is not accepted of any symbol.
*    Reason.CALL.DISCONNECT - the operation is interrupted owing to break of telephony connection;
*    Reason.TERMINATED      - the operation is interrupted by system.
*    Reason.CA.FAX          - signal of a fax on a line.
*
*    At reception of symbol from a array determined by a mask input
*    interrupts and come back symbols which are entered up to
*    interruptions by a symbol from a mask
*/
String getDigits(int digitsCount, int timeout, String termMask);

/**
<accessor>
*    To receive entered symbols.
*    The string from symbols from buffers of detector comes back.
*/
String getDigitsBuffer();
}
