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
package org.visualcti.server.hardware.provider.dialogic;

import static org.visualcti.server.hardware.provider.dialogic.Hardware.DX_ERROR;

import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * generic class of the device</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractDialogicDeviceFactory
 * @see AbstractTelephonyDevice
 */
@SuppressWarnings("unchecked")
public class DialogicDevice<H extends Integer, F extends AbstractDialogicDeviceFactory<H, ?>>
        extends AbstractTelephonyDevice<H, F> {

    public DialogicDevice(String name, DialogicServiceProvider<H> provider) {
        super(name, provider);
    }

    protected DialogicDevice(String name, TelephonyServiceProvider<H> provider,
                             CallsPortEngine<H> calls, TonesEngine<H> tones,
                             MultimediaEngine<H> media, FaxMachineEngine<H> faxes) {
        super(name, provider, calls, tones, media, faxes);
    }

    @Override
    public boolean isInvalidHandle(H deviceHandle) {
        return deviceHandle == null || deviceHandle.intValue() == DX_ERROR;
    }

   @Override
    protected H wrongHandle() {
        return (H) Integer.valueOf(DX_ERROR);
    }

    @Override
    protected H errorHandle() {
        return (H) Integer.valueOf(DX_ERROR);
    }
}
