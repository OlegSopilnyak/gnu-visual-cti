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
package org.visualcti.server.hardware.proxy;

import org.visualcti.server.hardware.proxy.part.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Interface of telephony device (proxy) for use it in the Task's level</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface deviceProxy extends
                                  CallControl,
                                  ToneDetector,ToneGenerator,
                                  Player,Recorder,
                                  faxMachine
{
/**
 * <environment's entry>
 * The name of selected devivce in runtime environment
 */
String SELECTED_DEVICE = "telephony <:current device:>";
/**
 * <const>
 * The name of root XML's Element
 */
String ELEMENT = "device";
/**
<device_status>
hardware error on the device
*/
String DS_ERROR     ="ERROR";
/**
<device_status>
The device is closed
*/
String DS_CLOSED    ="CLOSED";
/**
<device_status>
The device is wait to call
*/
String DS_WAIT      ="WAIT";
/**
<device_status>
The device is idle
*/
String DS_IDLE      ="IDLE";
/**
<device_status>
The device is playing back
*/
String DS_PLAY      ="PLAY";
/**
<device_status>
The device is recording
*/
String DS_RECD      ="RECORD";
/**
<device_status>
The device is dialing
*/
String DS_DIAL      ="DIAL";
/**
<device_status>
The device is getting digits
*/
String DS_GTDIG     ="GET DIGITS";
/**
<device_status>
The device is generating a tone
*/
String DS_TONE      ="TONE SEND";
/**
<device_status>
Operation has terminated
*/
String DS_STOPD     ="STOPED";
/**
<device_status>
The device is sending a fax
*/
String DS_SENDFAX   ="FAX SEND";
/**
<device_status>
The device is receiving a fax
*/
String DS_RECVFAX   ="FAX RECV";
/**
 * <accessor>
 * To get access to the current device's status
 * @return status
 */
String getStatus();
/**
<accessor>
    The name of device. The name is unique and
    is established by the driver of the equipment.
 * @return the name
 */
String getDeviceName();
/**
<action>
 The unconditional termination anyone current CTI of operation:
 	1. operations with calls (waiting, make call, connect)
 	2. exchanges of the data (voice or fax)
 * @throws IOException If the device can't terminate current operation
 */
void terminate() throws java.io.IOException;
}
