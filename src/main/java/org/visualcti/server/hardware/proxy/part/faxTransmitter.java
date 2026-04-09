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
 * Interface for transmit the fax from the input stream</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface faxTransmitter
{
/**
* <action>
*    To transmit the fax
 * @param source stream to fax data
 * @param format format of data in the stream(resolution is a field)
 * @param issueVoiceRequest upon termination of reception to give out a
*                            sound signal on the remote fax-device
 * @return reason of completion<br>
*    Reason.IO.EOF              - normal end of transfer<br>
*    Reason.CALL.DISCONNECT     - having dug telephony connection<br>
*    Reason.IO.TIMEOUT          - the remote fax does not answer<br>
*                                   (there is no signal of reception or transfer)<br>
*    Reason.IO.FORMAT           - the format of the data in a transmitted file is<br>
*                                   not supported by fax-device<br>
*    Reason.FAX.COMMUNICATION_ERROR - mistake of communication<br>
*                                       at reception or transfer of a fax<br>
*    Reason.FAX.USER_STOP       - on the remote fax-device the key STOP is pressed<br>
*    Reason.FAX.COMPATIBILITY   - the remote fax-device is not compatible or<br>
*                                   can not accept a fax with the given resolution<br>
  * @see Fax
 */
String transmit(InputStream source, Fax format, boolean issueVoiceRequest );
/**
* <action>
*    To transmit the fax
 * @param doc  The pair (Stream & format)
 * @param issueVoiceRequest upon termination of reception to give out a
 *                            sound signal on the remote fax-device
 * @return reason of completion<br>
*    Reason.IO.EOF              - normal end of transfer<br>
*    Reason.CALL.DISCONNECT     - having dug telephony connection<br>
*    Reason.IO.TIMEOUT          - the remote fax does not answer<br>
*                                   (there is no signal of reception or transfer)<br>
*    Reason.IO.FORMAT           - the format of the data in a transmitted file is<br>
*                                   not supported by fax-device<br>
*    Reason.FAX.COMMUNICATION_ERROR - mistake of communication<br>
*                                       at reception or transfer of a fax<br>
*    Reason.FAX.USER_STOP       - on the remote fax-device the key STOP is pressed<br>
*    Reason.FAX.COMPATIBILITY   - the remote fax-device is not compatible or<br>
*                                   can not accept a fax with the given resolution<br>
 * @see org.visualcti.media.Document
 */
String transmit(Document doc, boolean issueVoiceRequest );
}
