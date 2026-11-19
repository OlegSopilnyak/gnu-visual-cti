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

import java.util.Optional;
import org.visualcti.server.core.unit.RunnableServerUnit;

/**
 * <service-facade>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Facade: The parent of any Service of the Server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @see RunnableServerUnit
 */
public interface Service extends RunnableServerUnit {
    // The name of root configuration xml-element
    String XML_ROOT_NAME = "service";

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    default String getType() {
        return "[service]";
    }

    /**
     * <accessor>
     * To get the service's owner
     * The one whom this service is subordinate
     *
     * @return the service-owner of the service
     */
    default Service getServiceOwner() {
        return Optional.ofNullable(getOwner()).filter(Service.class::isInstance)
                .map(Service.class::cast)
                .orElse(null);
    }

    /**
     * <accessor>
     * To check is service depends on the master-service
     *
     * @param master potential owner of the service
     * @return true if service depends on the master
     */
    default boolean isDependsOn(final Service master) {
        return master != null && (this.equals(master) || isServiceOwner(getServiceOwner(), master));
    }

    /**
     * <walker>
     * To check is service depends on the master-service
     *
     * @param owner  potential owner of the service
     * @param master the owner of the service to check
     * @return true if service depends on the master
     */
    static boolean isServiceOwner(final Service owner, final Service master) {
        return owner != null && (master.equals(owner) || isServiceOwner(owner.getServiceOwner(), master));
    }
}
