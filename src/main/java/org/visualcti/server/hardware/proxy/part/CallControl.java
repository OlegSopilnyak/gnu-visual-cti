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

import org.visualcti.media.*;
import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Interface -  telephone call control</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface CallControl
{
/**
 * <accessor>
 * To get access to device's factory
 * @return
 */
deviceFactory getFactory();
/**
 * <accessor>
 * To get access to device's name
 */
String getName();
/**
 * <accessor>
 * To get access to device's handle
 */
int getHandle();
/**
<parameter name>
Using this parameter name you can get
the additional information about telephone number of this device
*/
String ORIGIN = "ORIGIN";
/**
<parameter name>
Using this parameter name you can get
the additional information about telephone number of the caused subscriber
*/
String CALLED = "DNIS";
/**
<parameter name>
Using this parameter name you can get
the additional information about telephone number of initiator of a call
*/
String CALLING= "ANI";
/**
<accessor>
    The additional information on a telephone call.
*/
String getCallAdditional(String parameter);
/**
<accessor>
    The phone number of the caused subscriber (DNIS or DID). For analog
    channels (without DID of service) returns an own telephone number of port,
    established in properties of port.
*/
String getCalledNumber();
/**
<accessor>
    The phone number calling (ANI) (initiator of a call).
*/
String getCallingNumber();
/**
<accessor>
    To check up a condition of a telephone call

    Return:

    true  - "The call is not served any more"
    false - "The call is served"
*/
boolean isDisconnected();
/**
<accessor>
Check, whether device can accept the incoming call
This flag, the factory may set in properties of the device
*/
boolean canWaitForCall();
/**
<action>
    The entering call expects. For a user's telephone line a call
    is deemed accepted after receipt rings of bells. For connecting
    interstation line, after receipt of a call, in a line is reproduced
    (rings-1) of time a signal Tone.RINGBACK1 and then the method returns Reason.CALL.ALERTING.

    If the port is authorized for using as a resource for outgoing calls,
    (is established in properties of port (only for Telco Edition)),
    The system can interrupt expectation of an entering call and to execute outgoing
    call, using resources of this port. If the connection was unsuccessful,
    the port returns Reason.CALL.DISCONNECT.
    The information on a call can be received by methods
    getCalledNumber (), getCallingNumber ().

    Parameters:

    rings - quantity of rings
    answer - to answer an incoming call
    timeout - waiting time.

    Returned values (reason of completion):

    Reason.IO.TIMEOUT - the waiting time has expired,
    Reason.CALL.ALERTING - the incoming call (entering ring) has arrived.
  ??????????????????????????????????? need to finish the method's call
    TERM_CONNECT - (only for Telco Edition) the port was involved by system
                    for performance of an outgoing call also is in a mode
                    switching. The given value comes back after the analysis
                    result of a outgoing call, in case of successful connection with
                    by the subscriber.
  ??????????????????????????????????? need to finish the method's call
    Reason.CALL.DISCONNECT - unsuccessful incoming or outgoing call,
                or disconnect detected during simple waiting (rings==0).
    Reason.TERMINATED - the operation is interrupted by system.
*/
String waitForCall(int rings , int timeout, boolean answer);
/**
<accessor>
Check, whether device can make the outgoing call
This flag, the factory may set in properties of the device
*/
boolean canMakeCall();
/**
<action>
    To make a outgoing call. A mode of a set (pulse or tone) and others
    the necessary parameters are set by installations of port.

    Parameters:

    number - telephone number,
    timeout - maximal waiting time of the answer (sec), after which
              the reason Reason.CA.NO_ANSWER comes back.

    Returned value (reason of completion):

    Reason.CA.VOICE   - the man has answered
    Reason.CA.FAX     - the fax - device has answered
    Reason.CA.BUSY    - number is engaged
    Reason.CA.NO_ANSWER - the telephone number does not answer
    Reason.CA.NO_DIAL_TONE - port be not capable to execute a outgoing call
                                because of a condition of a line
    Reason.CA.SIT     - special information signal on a line
    Reason.CA.NO_RESPONDING - there is no signal after a set of number
    Reason.CA.BAN     - number is forbidden
*/
String makeCall(String number, int timeout);
/**
<accessor>
The check, whether device can be used in operations of connections
This flag, the factory may set in properties of the device
*/
boolean canUsedInConnect();
/**
<action>
    Inquiry about connection.

    Inquiry to system on performance of connection with the specified
    telephone number. Having received inquiry, the system chooses free
    port and makes outgoing call on the given telephone number.
    (For a choice of port the table of routing can be used.)
    On the chosen port operation makeCall (number, timeout)
    automatically is carried out. The result of this operation also
    will be returned result of operation connect (). In case of result
    VOICE or FAX the switching of two ports is made.

    If the telephone number coincides with internal number of one of ports
    systems (internal number of port is established in properties of port):
    1) If the port is in a condition offhook, the connection is made
        and the operation returns VOICE;
    2) If the port is in a condition onhook and type of port - POTS,
        on connected to him the telephone device the signals of a call
        are sent. If hook on the telephone device will be lifted,
        the connection is made and the operation returns VOICE.
        If in time timeout hook will not be removed(taken off), the operation
        returns NO_ANSWER.
    3) If port is in condition onhook and type of port - PSTN, it
        is translated in the condition offhook also is checked presence
        of a signal from telephone station (DIAL TONE).
        At presence of a signal the operation returns VOICE,
        otherwise - NO_DIAL_TONE.

    Parameter:

    number  - telephone number or channel's name;
    timeout - maximal waiting time of the answer (sec), after which
              the NO_ANSWER comes back.
    toPaly  - The sound to play during connect's operation

    Returned value (reason of completion):

    Reason.CA.VOICE   - the man has answered
    Reason.CA.FAX     - the fax - device has answered
    Reason.CA.BUSY    - number is engaged
    Reason.CA.NO_ANSWER - the telephone number does not answer
    Reason.CA.NO_DIAL_TONE - system be not capable to execute a outgoing call
                    (There is no free line for performance of an outgoing call)
    Reason.CA.SIT     - special information signal on a line
    Reason.CA.NO_RESPONDING - there is no signal after a set of number
    Reason.CA.BAN     - number is forbidden
*/
String connect(String number, int timeout, Sound toPlay);
/**
<action>
    To break off telephone connection.
*/
void dropCall();
}
