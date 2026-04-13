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
package org.visualcti.server.core;

import java.io.IOException;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Engine, high level subsystem interface</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface Engine {
    /**
     * <enum>
     * engine states
     */
    enum State {
        // started engine state
        IN_SERVICE,
        // stopped engine state
        OUT_OF_SERVICE
    }

    /**
     * <action>
     * to Start the engine
     *
     * @throws IOException if engine can't start
     */
    void Start() throws IOException;

    /**
     * <action>
     * to Stop the engine
     *
     * @throws IOException if engine can't stop
     */
    void Stop() throws IOException;

    /**
     * <accessor>
     * To check is Engine is working (in service)
     *
     * @return true if Engine is in service
     * @see State#IN_SERVICE
     */
    boolean isStarted();

    /**
     * <accessor>
     * To check is Engine is stopped (out of service)
     *
     * @return true if Engine is out of service
     * @see State#OUT_OF_SERVICE
     */
    boolean isStopped();
}
