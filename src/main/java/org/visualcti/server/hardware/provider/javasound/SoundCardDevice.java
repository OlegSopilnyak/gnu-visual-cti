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
package org.visualcti.server.hardware.provider.javasound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Port;

import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyServiceProvider
 */
public class SoundCardDevice<H extends SoundCardHandle, F extends AbstractTelephonyFactory<H, ?>>
        extends AbstractTelephonyDevice<H, F> {

    protected SoundCardDevice(String name, TelephonyServiceProvider<H> provider) {
        super(name, provider);
    }

    /**
     * <accessor>
     * To get access to the wrong value device's low-level handle
     *
     * @return the value for a handle of an unopened device
     * @see #isInvalidHandle(H)
     */
    @Override
    protected H wrongHandle() {
        return super.wrongHandle();
    }

    /**
     * <accessor>
     * To get access to the error value device's low-level handle
     *
     * @return the value for handle of corrupted device
     * @see #isInvalidHandle(H)
     */
    @Override
    protected H errorHandle() {
        return super.errorHandle();
    }

    public static void main(String[] args) {
        System.out.println("----------- Source Lines");
        Line.Info[] source = AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE);
        for (int i = 0; i < source.length; i++) {
            System.out.println("Source Line Info:" + i + ": " + source[i].toString());
            System.out.println("Source Line Info:" + i + ": " + source[i].getLineClass().toString());
            System.out.println("Source Line Info:" + i + ": " + source[i].getLineClass().getName());
        }
        System.out.println("----------- Target Lines");
        Line.Info[] target = AudioSystem.getTargetLineInfo(Port.Info.SPEAKER);
        for (int i = 0; i < target.length; i++) {
            System.out.println("Target Line Info:" + i + ": " + target[i].toString());
            System.out.println("Target Line Info:" + i + ": " + target[i].getLineClass().toString());
            System.out.println("Target Line Info:" + i + ": " + target[i].getLineClass().getName());
        }
    }
}
