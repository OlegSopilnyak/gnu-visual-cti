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

import java.util.*;
/**
<registry>
Class for manage the serverObjects
*/
public final class ObjectRegistry
{
/**
The store of registered objacts
*/
private static final HashMap objects = new HashMap();
    /**
    <mutator>
    to register serverObject
    */
    public static void register(serverObject object) throws Exception
    {
        String path = object.getPath();
        if (path == null) throw new Exception("Object have invalid path");
        if ( getInstance(path) != null) throw new Exception("Path ["+path+"] already registered");
//System.out.println("[ObjectRegistry] Register "+object+" to "+path);
        synchronized (objects){objects.put(path, object);}
    }
    /**
    <mutator>
    to reregister server object
    */
    public static void reRegister(serverObject object) throws Exception
    {
        String path = object.getPath();
        if (path == null) throw new Exception("Object have invalid path");
        synchronized (objects){objects.put(object.getPath(), object);}
    }
    /**
    <mutator>
    to unregister server object
    */
    public static void unRegister(serverObject object)
    {
//System.out.println("[ObjectRegistry] unRegister "+object);
        synchronized (objects){objects.remove(object.getPath());}
    }
    /**
    <accessor>
    to get instance of serverObject by Path
    */
    public static serverObject getInstance(String objectPath)
    {
        synchronized (objects){ return (serverObject)objects.get(objectPath);}
    }
    /**
    <accessor>
    get list of paths of all registered objects
    */
    public static String[] list()
    {
        Vector set = new Vector();
        synchronized(objects)
        {
            for(Iterator keys = objects.keySet().iterator(); keys.hasNext(); ){
                set.addElement(keys.next());
            }
        }
        return result(set);
    }
    /**
    <accessor>
    get list of paths of some registered objects
    */
    public static String[] list(String prefix)
    {
        if (prefix == null || prefix.length() == 0) return list();
        Vector set = new Vector();
        synchronized(objects)
        {
            for(Iterator keys = objects.keySet().iterator(); keys.hasNext(); )  {
                String key = (String)keys.next();
                if ( key.startsWith(prefix) ) set.addElement( key );
            }
        }
        return result(set);
    }
        /** to convert Vector to String[] */
        private static String[] result(Vector set)
        {
            int length = set.size();
            if (length == 0) return null;
            String [] list = new String[ length ];
            set.copyInto( list );
            return list;
        }
}
