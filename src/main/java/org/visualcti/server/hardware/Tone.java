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
package org.visualcti.server.hardware;

/**
interface for define the Tone names
*/
public interface Tone
{
/**
Tone ID for BEEP before  start record (for example)
*/
String BEEP      = "BEEP";
/**
Tone ID for dial tone signal
*/
String DIAL      = "DIAL";
/**
Tone ID for fax receive request signal
*/
String FAX_RX    = "FAX RX";
/**
Tone ID for fax transmit request signal
*/
String FAX_TX    = "FAX TX";
/**
Tone ID for BUSY signal
*/
String BUSY      = "BUSY";
/**
Tone ID for RINGBACK signal
*/
String RINGBACK1 = "RINGBACK1";
/**
Tone ID for RINGBACK signal
*/
String RINGBACK2 = "RINGBACK2";
/**
Tone ID for REORDER signal
*/
String REORDER   = "REORDER";
/**
Tone ID for INTERCEPT signal
*/
String INTERCEPT = "INTERCEPT";
/**
Tone ID for CALLWAIT signal
*/
String CALLWAIT1 = "CALLWAIT1";
/**
Tone ID for CALLWAIT signal
*/
String CALLWAIT2 = "CALLWAIT2";
/**
 * <set>
 * The set of valid Tone's names for station's Signals
 * */
String STATION[] = new String[]
 {DIAL, BUSY, RINGBACK1, RINGBACK2, REORDER, INTERCEPT, CALLWAIT1, CALLWAIT2};
/**
 * <set>
 * The set of valid Tone's names for FAX's Signals
 * */
String FAXS[] = new String[]{FAX_RX, FAX_TX};
}
