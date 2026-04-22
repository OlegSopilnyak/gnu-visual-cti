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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.visualcti.server.core.unit.ServerUnit;

/**
<registry>
Class for manage the serverUnits
*/
public final class UnitRegistry
{
/**
The storage of registered server-units
*/
private static final Map<String, Object> units = new HashMap<>();
// the lock for safe updating units map
private static final Lock lock = new ReentrantLock();
    /**
     <mutator>
     to register serverUnit

     @see serverUnit#getPath()
     */
    public static void register(serverUnit unit) throws IOException
    {
        String path = unit.getPath();
        if (path == null) throw new IOException("Unit have invalid path");
        if (lookup(path, serverUnit.class) != null) throw new IOException("Path [" + path + "] already registered");
        safeAction(() -> units.put(path, unit));
    }
    /**
     <mutator>
     to register serverUnit

     @see ServerUnit#getPath()
     */
    public static void register(ServerUnit unit) throws IOException
    {
        String path = unit.getPath();
        if (path == null) throw new IOException("Unit have invalid path");
        if (lookup(path, ServerUnit.class) != null) throw new IOException("Path [" + path + "] already registered");
        safeAction(() -> units.put(path, unit));
    }
    /**
     <mutator>
     to unregister server unit

     @see serverUnit#getPath()
     */
    public static void unRegister(serverUnit unit)
    {
        try {safeAction(() -> units.remove(unit.getPath()));
        }catch(NullPointerException e){}
//System.out.println("[UnitRegistry] unRegister "+unit);
    }
    /**
     <mutator>
     to unregister server unit

     @see ServerUnit#getPath()
     */
    public static void unRegister(ServerUnit unit)
    {
        try {safeAction(() -> units.remove(unit.getPath()));
        }catch(NullPointerException e){}
//System.out.println("[UnitRegistry] unRegister "+unit);
    }
    /**
    <accessor>
    to get instance of serverUnit by Path

     @see #lookup(String, Class)
     @see serverUnit
    */
    public static Object lookupOld(String unitPath)
    {
        return UnitRegistry.lookup(unitPath, serverUnit.class);
    }
    /**
    <accessor>
    to get instance of serverUnit by Path
    */
    public static Object lookup( String unitPath, Class unitClass)
    {
        return safeAction(() -> {
            final Object unit = units.get(unitPath);
            // check is instance class, have unitClass as parent
            return unitClass.isInstance(unit) ? unit : null;
//        return unit.getClass().isAssignableFrom( unitClass ) ? unit:null;
        });
    }
    /**
    <accessor>
    get list of paths of all registered units

     @see #list(String)
    */
    public static String[] list() {return list(null);}
    
    /**
    <accessor>
    get list of paths of some registered units
    */
    public static String[] list(String prefix)
    {
        return safeAction(() ->
                units.keySet().stream()
                        .filter(path -> prefix == null || path.startsWith(prefix))
                        .toArray(String[]::new)
        );
    }

    // private methods
    private static <T> T safeAction(Callable<T> action) {
        lock.lock();
        try {
            return action.call();
        } catch (Exception e) {
            return null;
        } finally {
            lock.unlock();
        }

    }
}
