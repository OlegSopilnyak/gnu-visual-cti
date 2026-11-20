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
package org.visualcti.server.service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.visualcti.server.core.unit.ServerUnit;

/**
 * <services-manager-facade>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Facade: The manager of all Services of the Server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see Service
 */
public interface ServicesManager extends Service {
    // The unit's path of the manager by default
    String UNIT_PATH = "Service";
    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    default String getType() {
        return "[manager]";
    }

    /**
     * <mutator>
     * To add the subordinated service.
     * At the start of this service all subordinated will start as well.
     * The same way at the service stop.
     *
     * @param service the service to add
     * @see Service
     * @see ServerUnit#add(ServerUnit)
     */
    default void addService(final Service service) {
        add(service);
    }

    /**
     * <mutator>
     * To remove the subordinated service.
     *
     * @param service the service to remove
     * @see Service
     * @see ServerUnit#remove(ServerUnit)
     */
    default void removeService(final Service service) {
        remove(service);
    }

    /**
     * <accessor>
     * To get the stream of subordinated services
     *
     * @return the stream of subordinated services
     * @see Service
     * @see Stream
     * @see ServerUnit#children()
     */
    default Stream<Service> services() {
        return children().filter(Service.class::isInstance).map(Service.class::cast);
    }

    /**
     * <accessor>
     * To look for the service's reference by service name
     *
     * @param name the name of the service to find
     * @return service's reference or empty
     * @see Service
     * @see Optional
     * @see #services()
     */
    default Optional<Service> findService(final String name) {
        return services().filter(service -> Objects.equals(service.getName(), name)).findFirst();
    }
}
