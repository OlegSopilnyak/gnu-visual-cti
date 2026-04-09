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
Constants set
Reason of completion of the operation
*/
public interface Reason
{
String ERROR = "ERROR";
/**
The result of the operation, which is not required a reason of completion
*/
String OK = "OK";
/**
The reason - operation was interrupted by manager
*/
String TERMINATED  = "TERMINATION";
/**
Reason of completion of the Call operations
*/
public interface CALL
{
    /**
    Reason - incoming call alerted
    */
    String ALERTING = "RINGS";
    /**
    Reason - incoming call alerted
    */
    String RINGS = "RINGS";
    /**
    Reason - the telephone call disconnect is detected
    */
    String DISCONNECT  = "DISCONNECT";
}
/**
Reason of completion of the Call Analyze operation
for makeCall(...) method
*/
public interface CA
{
    /**
    Reason - voice detected
    */
    String VOICE = "VOICE";
    /**
    Reason - faxmachine detected
    */
    String FAX = "FAX";
    /**
    Reason - modem detected
    */
    String MODEM = "MODEM";
    /**
    Reason - answering machine detected
    */
    String AUTOANSWER = "AUTOANSWER";
    /**
    Reason - BUSY signal detected
    */
    String BUSY = "BUSY";
    /**
    Reason - abonent does not answered
    */
    String NO_ANSWER = "NO ANSWER";
    /**
    Reason - no ringback detected
    */
    String NO_RESPONDING = "NO RINGBACK";
    /**
    Reason - telephony line not connected or not working properly
    */
    String NO_DIAL_TONE  = "NO DIAL TONE";
    /**
    Reason - Special information signal on a line
    */
    String SIT = "SIT";
    /**
    Reason - PhoneNumber is forbidden
    */
    String BAN = "BAN";
}
/**
Reason of completion of the Input/Output operations
*/
public interface IO
{
    /**
    Reason - expiration of a timeout
    */
    String TIMEOUT = "TIMEOUT";
    /**
    Reason - incompatible format
    */
    String FORMAT = "FORMAT";
    /**
    Reason - the dataStream was completed
    */
    String EOF = "EOF";
    /**
    Reason - the defined DTMF is detected
    */
    String DTMF = "DTMF";
    /**
    The reason - during voice recording, is detected a silence
    */
    String SILENCE = "SILENCE";
}
/**
Reason of completion of the Fax operations
*/
public interface FAX
{
    /**
    Reason - the remote fax-device is not COMPATIBLE
    or can not accept or to transfer a fax with the given sanction
    */
    String COMPATIBILITY = "COMPATIBILITY";
    /**
    Reason - mistake of COMMUNICATION at reception or transfer of a fax
    */
    String COMMUNICATION_ERROR = "COMMUNICATION ERROR";
    /**
    Reason - on the remote fax-device the key STOP is pressed
    */
    String USER_STOP = "USER STOP";
    /**
    Reason - the inquiry to POLLING from the remote fax-device is received
    */
    String POLLING = "POLLING";
    /**
    Reason - the remote fax-device has not accepted inquiry on POLLING
    */
    String NOPOLL = "NO POLL";
}
}
