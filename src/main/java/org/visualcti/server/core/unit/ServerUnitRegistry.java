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
package org.visualcti.server.core.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.visualcti.server.core.unit.exception.CannotRegisterUnitException;
import org.visualcti.server.core.unit.exception.InvalidUnitException;
import org.visualcti.server.core.unit.exception.ServerUnitException;

/**
 * <registry>
 * Class Utility: for manage server units instances
 */
public final class ServerUnitRegistry {
    // The storage of registered server-units
    private static final Map<String, ServerUnit> units = new HashMap<>();
    // The lock for access to the units
    private static final Lock lock = new ReentrantLock();

    /**
     * <mutator>
     * to register server unit
     *
     * @param unit instance to register
     * @throws ServerUnitException if something went wrong
     * @see ServerUnit#getPath()
     * @see #register(String, ServerUnit)
     */
    public static void register(final ServerUnit unit) throws ServerUnitException {
        register(unit.getPath(), unit);
    }


    /**
     * <mutator>
     * to register server unit
     *
     * @param unitPath the path of the unit to register
     * @param unit     instance to register
     * @throws ServerUnitException if something went wrong
     * @see InvalidUnitException
     * @see CannotRegisterUnitException
     * @see #register(ServerUnit)
     */
    public static void register(final String unitPath, final ServerUnit unit) throws ServerUnitException {
        if (unitPath == null || unitPath.trim().isEmpty()) {
            // unit path value of the server unit
            throw new InvalidUnitException("The ServerUnit has invalid path");
        }
        // defending units map start
        lock.lock();
        try {
            // put unit to the map if not exists
            if (units.putIfAbsent(unitPath, unit) != null) {
                // the unit with the path already registered
                throw new CannotRegisterUnitException("Path [" + unitPath + "] already registered");
            }
        } finally {
            // defending units map end
            lock.unlock();
        }
    }

    /**
     * <mutator>
     * to unregister the server unit
     *
     * @param unit the unit to unregister
     * @throws Exception if something went wrong
     * @see InvalidUnitException
     * @see CannotRegisterUnitException
     * @see ServerUnit#getPath()
     * @see #unRegister(String)
     */
    public static void unRegister(final ServerUnit unit) throws Exception {
        unRegister(unit.getPath());
    }

    /**
     * <mutator>
     * to unregister the server unit
     *
     * @param unitPath the path of the unit to unregister
     * @throws Exception if something went wrong
     * @see InvalidUnitException
     */
    public static void unRegister(final String unitPath) throws Exception {
        if (unitPath == null || unitPath.trim().isEmpty()) {
            throw new InvalidUnitException("The ServerUnit has invalid path");
        }
        safeUnitAction(() -> units.remove(unitPath));
    }

    /**
     * <accessor>
     * to look for the instance of server unit by unit path
     *
     * @param unitPath the path of the unit to look for
     * @return found server unit or empty
     * @throws Exception if something went wrong
     * @see InvalidUnitException
     * @see Optional
     */
    public static Optional<ServerUnit> lookup(final String unitPath) throws Exception {
        if (unitPath == null || unitPath.trim().isEmpty()) {
            throw new InvalidUnitException("The ServerUnit has invalid path");
        }
        // returns the result of searching
        return Optional.ofNullable(safeUnitAction(() -> units.get(unitPath)));
    }

    /**
     * <accessor>
     * to look for the instance of server unit by unit path
     *
     * @param unit the unit to look for
     * @return found server unit or empty
     * @throws Exception if something went wrong
     * @see Optional
     * @see Optional#empty()
     * @see ServerUnit#getPath()
     * @see #lookup(String)
     */
    public static Optional<ServerUnit> lookup(final ServerUnit unit) throws Exception {
        return lookup(unit.getPath());
    }

    /**
     * Clear registry, only for tests purposes!
     * @deprecated
     */
    @Deprecated
    public static void clearForTesting() {
        units.clear();
    }


    // private methods
    private ServerUnitRegistry() {
    }

    private static ServerUnit safeUnitAction(Callable<ServerUnit> action) throws Exception {
        // defending units map start
        lock.lock();
        try {
            return action.call();
        } finally {
            // defending units map end
            lock.unlock();
        }
    }
}
