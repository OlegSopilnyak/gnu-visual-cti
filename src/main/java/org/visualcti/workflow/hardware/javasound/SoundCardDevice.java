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
package org.visualcti.workflow.hardware.javasound;

import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractCallsPortEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractFaxMachineEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractMultimediaEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractTonesEngine;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyDevice
 */
public class SoundCardDevice<H extends SoundCardHandle, F extends AbstractTelephonyFactory<H, ?>>
        extends AbstractTelephonyDevice<H, F> {

    protected SoundCardDevice(String name, TelephonyServiceProvider<H> provider) {
        super(name, provider);
    }

    protected SoundCardDevice(String name, TelephonyServiceProvider<H> provider,
                              CallsPortEngine<H> calls, TonesEngine<H> tones, MultimediaEngine<H> media, FaxMachineEngine<H> faxes) {
        super(name, provider, calls, tones, media, faxes);
    }

    @Override
    public DeviceActivitySession<H> createSessionFor(H handle) {
        return new DefaultTelephonyCall<>(this, handle);
    }

    @Override
    public boolean canBeConnected() {
        return false;
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
        return SoundCardHandle.wrong();
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
        return SoundCardHandle.wrong();
    }

    @Override
    protected CallsPortEngine<H> callsPart() {
        return new CallControl();
    }

    @Override
    protected TonesEngine<H> tonesPart() {
        return new Tones();
    }

    @Override
    protected MultimediaEngine<H> mediaPart() {
        return new Media();
    }

    @Override
    protected FaxMachineEngine<H> faxPart() {
        return new Fax();
    }

    // inner classes
    private class CallControl extends AbstractCallsPortEngine<H> {

    }

    private class Tones extends AbstractTonesEngine<H> {

    }

    private class Media extends AbstractMultimediaEngine<H> {

    }

    private class Fax extends AbstractFaxMachineEngine<H> {

    }
}
