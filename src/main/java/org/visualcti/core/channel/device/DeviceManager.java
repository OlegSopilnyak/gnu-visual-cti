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
package org.visualcti.core.channel.device;


import java.util.concurrent.Executor;
import java.util.stream.Stream;
import org.visualcti.server.core.unit.RunnableServerUnit;
import org.visualcti.server.unit.RunnableUnitAdapter;

/**
 * Manager: The facade to deal with server devices, registered in server kernel
 *
 * @see Device
 * @see Factory
 * @see RunnableUnitAdapter
 */
public interface DeviceManager extends RunnableServerUnit {
    /**
     * <aceessor>
     * The stream to factories managed by the manager
     *
     * @return factories stream
     * @see Factory
     */
    Stream<Factory<?, ?>> factories();

    /**
     * <aceessor>
     * The stream to devices managed by the manager's factories
     *
     * @return devices stream
     * @see Factory#devices()
     * @see Device
     */
    Stream<Device<?, ?>> devices();

    /**
     * <mutator>
     * to add devices factory for management
     *
     * @param factory the instance to add for management
     * @see Factory
     */
    void addFactory(Factory<?, ?> factory);

    /**
     * <mutator>
     * to remove devices factory from the manager
     *
     * @param factory the instance to add for management
     * @see Factory
     */
    void removeFactory(Factory<?, ?> factory);

    /**
     * <aceessor>
     * The executor of device factories' events management
     *
     * @return threads pool for device events processing
     * @see DeviceEventsProcessor#grabProviderEvents()
     */
    Executor deviceEventExecutor();
}
