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
package org.visualcti.server.hardware.provider.virtual;


import java.util.UUID;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Virtual Telephony device</p>
 *
 * @param <H> the type of device's handle
 * @param <F> the type of device's factory
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyDevice
 * @see VirtualTelephonyDevicesFactory
 * @see VirtualTelephonyDeviceProvider
 * @see UUID
 */
public class VirtualTelephonyDevice<H extends UUID, F extends VirtualTelephonyDevicesFactory<H, ?>>
        extends AbstractTelephonyDevice<H, F> {

    public VirtualTelephonyDevice(String deviceName, ServiceProvider<H> serviceProvider) {
        super(deviceName, (VirtualTelephonyDeviceProvider<H>)serviceProvider);
    }
}
