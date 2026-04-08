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

Contact oleg@visualcti.org or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg@visualcti.org
Home Phone:	380-62-3851086 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server;

import org.jdom.*;
import java.lang.reflect.*;
import org.visualcti.util.Tools;
/**
Class for make serverUnit by XML Element
*/
public class serverUnitMaker
{
    /**
    To create the serverUnit, using XML element
    */
    public static serverUnit make(Element xml,Class target)
    {
        serverUnit unit = null;// result of method's call, in the future
        Attribute pkg = xml.getAttribute("package");
        Attribute cls = xml.getAttribute("class");
        Attribute ext = xml.getAttribute("extends");
        try {
            String name = pkg.getValue()+"."+cls.getValue();
            String extName = ext.getValue();
            if (extName.indexOf(".") < 0)
            {   // only name without package
                extName = pkg.getValue()+"."+extName;
            }
            ClassLoader cl = serverUnitMaker.class.getClassLoader();
            Class extClass = Class.forName( extName, true, cl );// to make class extends
            Class unitClass = Class.forName( name, true, cl );// to make serverUnit's class
            Element parent = xml.getChild( "parent" );// get parent entry
            target = target == null ? serverUnit.class : target;
            unit = parent != null ? createFrom(parent):(serverUnit)unitClass.newInstance();
            // check is valid maked serverUnit instance
            if (
                unitClass.isInstance(unit) && // maked from defined in XML class
                extClass.isInstance(unit)  && // unit's class extends defined int XML class
                target.isInstance(unit)       // unit's class extends *target*
                )
            {
                unit.configure( xml );// to configure factory
                return unit;// to return the valid serverUnit instance
            }
        }catch(Exception e){// mistake
            e.printStackTrace(Tools.err);
        }
        return null;
    }
        /**
        To create factory, using class-helper
        */
        private static serverUnit createFrom(Element xml) throws Exception
        {
            Attribute pkg = xml.getAttribute("package");
            Attribute cls = xml.getAttribute("class");
            Attribute m   = xml.getAttribute("method");
            String name = pkg.getValue()+"."+cls.getValue();
            Class helpClass = Class.forName( name );// to make helper's class
            if (m != null) {
                String method = m.getValue();// make-method name
                Method make = helpClass.getMethod(method,new Class[]{});// to get static method by name
                return (serverUnit)make.invoke(helpClass,new Object[]{});
            } else return (serverUnit)helpClass.newInstance();
        }
}
