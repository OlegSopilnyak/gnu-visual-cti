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
 * Interface for tones generator, to generate some tones to the phone's line</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface ToneGenerator
{
/**
 * <action>
 *    To dial DTMF to line
 *
 *    Parameter:
 *
 *    toDial - sequence of symbols to dial, like "555#1234*"
 */
void dial(String toDial);

/**
 * <action>
 *    To give out in a line a sound signal.
 *    The parameters of a signal should be preset in properties
 *    port under the appropriate identifier of a signal.
 *
 *    Parameter:
 *
 *    toneID - identifier of a signal (see Tone).
 */
void playTone(String toneID);

/**
<action>
*    To give out in a line a sound signal.
*    The parameters of a signal should be preset in properties
*    port under the appropriate identifier of a signal.
*
*    Parameter:
*
*    toneID - identifier of a signal (see Tone).
*    time   - duration in seconds
*/
void playTone(String toneID, float time);
}
