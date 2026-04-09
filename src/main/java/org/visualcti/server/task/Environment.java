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
package org.visualcti.server.task;
import java.io.*;
import java.util.*;
/**
 * <environment>
 * The environment for Server's tasks
 * */
public final class Environment
{
/**
 * <pool>
 * The pool of Environment's parts
 * */
private final HashMap env = new HashMap();
  /**
   * <mutator>
   * To clear all parts of the environment
   */
  public final void clear(){this.env.clear();}
  /**
   * <mutator>
   * To setup the environment's part
   * @param name the name of environment's part
   * @param part the part of the environment
   */
  public final void setPart(String name, Object part)
  {
    if ( name != null && part != null) this.env.put(name.toLowerCase(),part);
  }
  /**
   * <accessor>
   * To get access to value of environment part
   * @param name the name of environment's part
   * @return the value
   */
  public final Object getPart(String name,Class source)
  {
    Object part = this.env.get(name.toLowerCase());
    return source.isInstance(part) ? part:null;
  }
  /**
   * To get access to environment's part by class
   * @param parent
   * @return
   */
  public final Object getPart(Class parent)
  {
    for(Iterator i=this.env.entrySet().iterator();i.hasNext();)
    {
      Object value = ((Map.Entry)i.next()).getValue();
      if ( parent.isInstance(value) ) return value;
    }
    return null;
  }
  /**
   * <mutator>
   * To remove the part of the environment
   * @param name the name of removed part
   */
  public final void removePart(String name)
  {
    if (name != null) this.env.remove(name);
  }
  /**
   * <accessor>
   * To get access to list of the parts of the environment with prefix in name
   * For design mode only!
   * @param prefix the prefix of the names
   * @return the list
   */
  public final List getPartsList(String prefix)
  {
    ArrayList list = new ArrayList();
    prefix = prefix.toLowerCase();
    for(Iterator i=this.env.keySet().iterator();i.hasNext();)
    {
      String name = (String)i.next();
      if ( name.startsWith(prefix) ) list.add( this.env.get(name) );
    }
    return list;
  }
  /**
   * <accessor>
   * To get access to list of all parts of the environment
   * For design mode only!
   * @return the list
   */
  public final List getPartsList(){return this.getPartsList("");}
}
