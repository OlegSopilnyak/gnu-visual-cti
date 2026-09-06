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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device service provider of the telephony device features</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see org.visualcti.core.channel.telephony.TelephonyServiceProvider#openResource(String)
 * @see AbstractTelephonyServiceProvider
 */
@SuppressWarnings("unchecked")
public class SoundCardServiceProvider<H extends SoundCardHandle> extends AbstractTelephonyServiceProvider<H> {
    public static final String SOUND_DEVICE = "SoundCard";
    // reference to the sound-card handle as singleton
    private static final AtomicReference<SoundCardHandle> handle = new AtomicReference<>(null);
    // the state of handset true = handset is off false = handset is on
    private static final AtomicBoolean handsetOff = new AtomicBoolean(true);

    @Override
    public Collection<String> allowedDevices() {
        return Collections.singleton(SOUND_DEVICE);
    }

    @Override
    protected H nativeResourceOpen(String name) throws IOException {
        if (isOpened(name)) {
            throw new IOException("Device :" + name + ": is already opened");
        } else if (SOUND_DEVICE.equals(name)) {
            return (H) soundCardHandle();
        } else {
            throw new IOException("Unsupported device: " + name);
        }
    }

    @Override
    protected boolean isHandsetOff(H handle) {
        return isOpened(handle) && handsetOff.get();
    }

    @Override
    protected boolean nativeHandsetOff(H handle) {
        if (isOpened(handle)) {
            handsetOff.set(true);
            return true;
        }
        return false;
    }

    @Override
    protected boolean nativeAnswerCall(H handle) {
        if (isOpened(handle)) {
            handsetOff.set(false);
            return true;
        }
        return false;
    }

    /// private methods
    // Returns the singleton instance of SoundCardHandle.
    private static SoundCardHandle soundCardHandle() {
        if (handle.get() != null) {
            return handle.get();
        }
        synchronized (SoundCardHandle.class) {
            if (handle.get() == null) {
                handle.getAndSet(createHandle());
            }
        }
        return handle.get();
    }

    private static SoundCardHandle createHandle() {
        final Line.Info[] microphones = AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE);
        final Line.Info source = microphones.length > 0 ? microphones[0] : null;
        final Line.Info[] speakers = AudioSystem.getTargetLineInfo(Port.Info.SPEAKER);
        final Line.Info target = speakers.length > 0 ? speakers[0] : null;
        return SoundCardHandle.of(source, target);
    }
}
