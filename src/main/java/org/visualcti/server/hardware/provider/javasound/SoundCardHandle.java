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

import javax.sound.sampled.Line;

import java.util.function.Predicate;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device handle for the telephony device operations</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see org.visualcti.core.channel.device.Device.ServiceProvider#openResource(String)
 */
@SuppressWarnings("unchecked")
public class SoundCardHandle {
    // predicate to test is it impossible to use this handle
    private static final Predicate<SoundCardHandle> isWrong = handle ->
            handle.source == null && handle.target == null;
    // the source line info
    private final Line.Info source;
    private final Line.Info target;

    /**
     * <builder>
     * To create a new instance of the valid SoundCardHandle
     *
     * @param source info about the source line
     * @param target info about the target line
     * @return built instance of SoundCardHandle
     */
    public static SoundCardHandle of(Line.Info source, Line.Info target) {
        return new SoundCardHandle(source, target);
    }

    /**
     * <builder>
     * To create a new instance of the invalid (wrong) SoundCardHandle
     *
     * @return built instance of SoundCardHandle
     */
    public static <H extends SoundCardHandle> H wrong() {
        return (H) of(null, null);
    }

    public Line.Info getSource() {
        return source;
    }

    public Line.Info getTarget() {
        return target;
    }

    /**
     * <accessor>
     * To test is it possible to use this handle
     *
     * @return true if it is possible to use this handle
     */
    public boolean canUse() {
        return isWrong.negate().test(this);
    }

    private SoundCardHandle(Line.Info source, Line.Info target) {
        this.source = source;
        this.target = target;
    }
}
