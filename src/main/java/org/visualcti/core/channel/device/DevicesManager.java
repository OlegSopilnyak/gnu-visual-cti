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
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.unit.RunnableUnitAdapter;

/**
 * Manager: The facade to manage server's channel devices.
 * The manager is registered in the units registry and owns the Server.
 *
 * @see Device
 * @see Device#getFactory()
 * @see Factory
 * @see RunnableUnitAdapter
 */
@SuppressWarnings("unchecked")
public interface DevicesManager extends RunnableServerUnit {
    /**
     * <accessor>
     * To get access to the managed devices factories Stream
     *
     * @see Stream
     * @see Factory
     * @see RunnableServerUnit#children()
     */
    default Stream<Factory<?, ?>> factories() {
        return children().filter(Factory.class::isInstance).map(Factory.class::cast);
    }

    /**
     * <accessor>
     * To get access to the managed devices Stream
     *
     * @see Stream
     * @see Device
     * @see Factory#devices()
     */
    default Stream<Device<?, ?>> devices() {
        return factories().flatMap(Factory::devices);
    }

    /**
     * <mutator>
     * to add the devices factory to manage it further
     *
     * @see Factory
     * @see RunnableServerUnit#add(ServerUnit)
     * @see #factories()
     */
    default void addDeviceFactory(Factory<?, ?> factory) {
        add(factory);
    }

    /**
     * <mutator>
     * to remove devices factory from the manager
     *
     * @param factory the instance to add for management
     * @see RunnableServerUnit#remove(ServerUnit)
     * @see Factory
     * @see #addDeviceFactory(Factory)
     */
    default void removeDeviceFactory(Factory<?, ?> factory) {
        remove(factory);
    }

    /**
     * <aceessor>
     * The executor of device factories' events management
     *
     * @return threads pool for device events processing
     * @see DeviceEventsProcessor#grabProviderEvents()
     */
    Executor deviceEventExecutor();
}
