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
package org.visualcti.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.exception.NoSuchUnitException;
import org.visualcti.server.core.unit.exception.NoUniqueUnitException;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.util.Tools;

/**
 * <registry>
 * Class for manage the serverUnits
 */
public final class UnitRegistry {
    // the storage of registered server-units
    private static final Map<String, Object> units = new HashMap<>();
    // the lock for safe updating units map
    private static final Lock lock = new ReentrantLock();

    /**
     * <mutator>
     * to register serverUnit
     *
     * @see serverUnit#getPath()
     */
    @Deprecated
    public static void register(serverUnit unit) throws IOException {
        String path = unit.getPath();
        if (path == null) throw new IOException("Unit have invalid path");
        if (lookupOld(path, serverUnit.class) != null) throw new IOException("Path [" + path + "] already registered");
        try {
            safeAction(() -> units.put(path, unit));
        } catch (ServerUnitException e) {
            // noway to throw here
        }
    }

    /**
     * <mutator>
     * to register serverUnit
     *
     * @see ServerUnit#getPath()
     */
    public static void register(ServerUnit unit) throws IOException {
        final String path = unit.getPath();
        if (path == null) {
            throw new IOException("Unit have invalid path");
        } else try {
            try {
                if (lookup(path, ServerUnit.class) != null) {
                    throw new IOException("Path [" + path + "] already registered");
                }
            } catch (NoSuchUnitException e) {
                safeAction(() -> units.put(path, unit));
            }
        } catch (ServerUnitException e) {
            throw new IOException("Path [" + path + "] invalid", e);
        }
    }

    /**
     * <mutator>
     * to unregister server unit
     *
     * @see serverUnit#getPath()
     */
    @Deprecated
    public static void unRegister(serverUnit unit) {
        try {
            safeAction(() -> units.remove(unit.getPath()));
        } catch (NullPointerException | ServerUnitException e) {
            // do nothing (simple operation never throws)
        }
//System.out.println("[UnitRegistry] unRegister "+unit);
    }

    /**
     * <mutator>
     * to unregister server unit
     *
     * @see ServerUnit#getPath()
     */
    public static void unRegister(ServerUnit unit) {
        try {
            safeAction(() -> units.remove(unit.getPath()));
        } catch (NullPointerException | ServerUnitException e) {
            // do nothing (simple operation never throws)
        }
//System.out.println("[UnitRegistry] unRegister "+unit);
    }

    /**
     * <accessor>
     * to get instance of serverUnit by Path
     *
     * @see #lookup(String, Class)
     * @see serverUnit
     */
    @Deprecated
    public static Object lookupOld(String unitPath) {
        return UnitRegistry.lookupOld(unitPath, serverUnit.class);
    }

    /**
     * <accessor>
     * to get instance of serverUnit by Path
     *
     * @see #lookup(String, Class)
     * @see serverUnit
     */
    @Deprecated
    public static Object lookupOld(String unitPath, Class<?> unitClass) {
        try {
            return safeAction(() -> {
                final Object unit = units.get(unitPath);
                // check is instance class, have unitClass as parent
                if (!unitClass.isInstance(unit)) {
                    throw new NoSuchUnitException("No registered units for " + unitPath);
                }
                return unit;
            });
        } catch (ServerUnitException e) {
            e.printStackTrace(Tools.err);
            return null;
        }
    }

    /**
     * <accessor>
     * to get instance of ServerUnit by unit path and parent type
     */
    public static <T extends ServerUnit> Object lookup(String unitPath, Class<T> unitClass) throws ServerUnitException {
        return safeAction(() -> {
            final Object unit = units.get(unitPath);
            // check is instance class, have unitClass as parent
            if (!unitClass.isInstance(unit)) {
                throw new NoSuchUnitException("No registered units for " + unitPath);
            }
            return unit;
        });
    }

    /**
     * <accessor>
     * to get instance of serverUnit by unit type
     */
    @SuppressWarnings("unchecked")
    public static <T extends ServerUnit> T lookup(Class<T> unitType) throws ServerUnitException {
        return (T) safeAction(() -> {
            // to look for all units which extends required unit type
            final List<ServerUnit> result = units.values().stream().filter(unitType::isInstance)
                    .map(ServerUnit.class::cast).collect(Collectors.toList());
            // processing selected units
            if (result.isEmpty()) {
                throw new NoSuchUnitException("No registered units for " + unitType.getName());
            } else if (result.size() > 1) {
                throw new NoUniqueUnitException("Too many registered units for " + unitType.getName());
            } else {
                return result.get(0);
            }
        });
    }

    /**
     * <accessor>
     * get list of paths of all registered units
     *
     * @see #list(String)
     */
    public static String[] list() {
        return list(null);
    }

    /**
     * <accessor>
     * get list of paths of some registered units
     */
    public static String[] list(String prefix) {
        try {
            return safeAction(() -> units.keySet().stream()
                    .filter(path -> prefix == null || path.startsWith(prefix))
                    .toArray(String[]::new)
            );
        } catch (ServerUnitException e) {
            return new String[0];
        }
    }

    // private methods
    private static <T> T safeAction(Callable<T> action) throws ServerUnitException {
        lock.lock();
        try {
            return action.call();
        } catch (ServerUnitException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(Tools.err);
            return null;
        } finally {
            lock.unlock();
        }

    }
}
