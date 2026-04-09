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

import org.visualcti.server.*;
import org.visualcti.server.hardware.proxy.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Interface of telephony device (proxy) for use it in the System level</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface generalDeviceProxy extends serverUnit, deviceProxy
{
/**
<accessor>
Get reference to owner of this device
*/
deviceFactory getFactory();
/**
<action>
 Opening and activization of the device.
 If the device can't open, device may throw the exception
*/
void open() throws java.io.IOException;
/**
<accessor>
 Check, is device already opened
*/
boolean isOpened();
/**
<action>
 Closing of the device, if there are no active operations and
 the expectation of end of the current operation, if still execute
 If the device can't close, device may throw the exception
*/
void close() throws java.io.IOException;
/**
<action>
 Attempt to restore serviceability of the device.
 Must return the success of device restoring.
*/
boolean restore();
/**
<mutator>
    To attach a output stream to the device,
    in a stream the information on a new status is written
    port, driver of port before transition from one
    condition in another should write the information
    about a new status in this stream
    -------------------------------------------
    This function, will realized in the part of serverUnit, method:
    dispatch(new unitEvent(this,unitEvent.STATE_ID,this.getStatus()))
*/
//void setPortStatusOutputStream(java.io.OutputStream stream);
}
