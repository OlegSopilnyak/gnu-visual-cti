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

import org.jdom.*;

import java.util.*;
/**
Class serverunit's Meta Information
*/
public final class MetaData
{
/**
<parameter_name>
Parameter, path to icon file body
*/
public static final String ICON = "meta.icon";
/**
<parameter_name>
Parameter, type of unit
*/
public static final String TYPE = "meta.type";
/**
<parameter_name>
Parameter, class name of unit
*/
public static final String CLASS = "meta.class";
/**
<parameter_name>
Parameter, name of unit instance
*/
public static final String NAME = "meta.name";
/**
<parameter_name>
Parameter, the path to unit instance
*/
public static final String PATH = "meta.path";
/**
<parameter_name>
Parameter, the state of unit instance
*/
public static final String STATE = "meta.state";
/**
<pool>
Map of unit meta-data
*/
private final transient HashMap meta = new HashMap();
    /**
    to make meta-data from unit instance
    */
    public MetaData(serverUnit unit)
    {
        this.meta.put(MetaData.ICON, unit.getIcon() );
        this.meta.put(MetaData.TYPE, unit.getType() );
        this.meta.put(MetaData.CLASS, unit.getClass().getName() );
        this.meta.put(MetaData.NAME, unit.getName() );
        this.meta.put(MetaData.PATH, unit.getPath() );
        this.meta.put(MetaData.STATE, unit.getUnitState() );
    }
    /**
    <accessor>
    Get the Icon's body for this serverUnit
    */
    public final byte[] getIcon(){return (byte[])this.meta.get(MetaData.ICON);}
    /**
    <accessor>
    To get unitType
    */
    public final String getType(){return (String)this.meta.get(MetaData.TYPE);}
    /**
    <acessor>
    Get class name for which comes back the information
    */
    public final String className(){return (String)this.meta.get(MetaData.CLASS);}
    /**
    <acessor>
    Get access to name of unit
    */
    public final String getName(){return (String)this.meta.get(MetaData.NAME);}
    /**
    <acessor>
    Get access the path to instance in UnitRegistry
    */
    public final String getPath(){return (String)this.meta.get(MetaData.PATH);}
    /**
    <acessor>
    Get access to unit state String
    */
    public final String getState(){return (String)this.meta.get(MetaData.STATE);}
    /**
    <acessor>
    Object.toString() overrided
    */
    public final String toString()
    {
        return "MetaData of "+this.getName()+
                "\n\tClass:"+this.className()+
                "\n\tState:"+this.getState()+
                "\n\tRegitry path:"+this.getPath()+"\n";
    }
    /**
    <fill>
    to fill information to response
    */
    public final void fill(unitResponse response)
    {
        Parameter par;
        if ( (par = response.getResult("meta.*")) != null) this.fullFill(response);
        else {
            ArrayList info = new ArrayList();
            for(Iterator i=response.results();i.hasNext();){
                String name = ((Parameter)i.next()).getName();
                if (name.startsWith("meta.")) info.add(name);
            }
            if (info.size()==0)this.fullFill(response);
            else {
                for (Iterator i=info.iterator();i.hasNext();)
                {
                    String name = (String)i.next();
                    if ( name.equals(ICON) ){
                        par = new Parameter(ICON, this.getIcon()).output();
                    }else {
                        Object value = this.meta.get(name);
                        if (value == null) continue;
                        par = new Parameter(name, (String)value).output();
                    }
                    response.set(par);
                }
            }
        }
    }
    /**
    <fill>
    to fill full meta information to response
    */
    public final void fullFill(unitResponse response)
    {
        Parameter par;
        if (this.getIcon() != null) {
            par = new Parameter(ICON, this.getIcon());
            response.set(par);
        }
        par = new Parameter(TYPE, this.getType());
        response.set(par);
        par = new Parameter(CLASS, this.className());
        response.set(par);
        par = new Parameter(NAME, this.getName());
        response.set(par);
        par = new Parameter(PATH, this.getPath());
        response.set(par);
        par = new Parameter(STATE, this.getState());
        response.set(par);
    }
}
