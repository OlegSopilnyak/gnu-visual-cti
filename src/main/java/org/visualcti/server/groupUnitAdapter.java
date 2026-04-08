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

import java.util.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The group of serverUnit adapter</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public abstract class groupUnitAdapter extends serverUnitAdapter implements groupUnit
{
/**
 * <pool> The group units store
 */
private final ArrayList group = new ArrayList();
    /**
     * <mutator>
     * To remove the children
     */
    public void removeChildren()
    {
      synchronized( this.group )
      {
        for(Iterator i=this.group.iterator();i.hasNext();)
        {
          try{
            serverUnit unit = (serverUnit)i.next();
            unit.setOwner( null );
          }catch(Exception e){}
        }
        this.group.clear();
      }
    }
    /**
     * <mutator>
     * to add child to the group
    */
    public void addChild(serverUnit child) {
        if (child == null) return;
        synchronized( this.group )
        {
            // to register child in group's hierarchy in UnitRegistry
            child.setOwner(this); this.group.add(child);
        }
    }
    /**
<mutator>
to remove child from group
    */
    public void removeChild(serverUnit child) {
        if (child == null) return;
        synchronized( this.group )
        {
            // to unregister child from UnitRegistry
            if (this.group.remove(child)) child.setOwner(null);
        }
    }
    /**
<accessor>
get list of children
    */
    public List children(){return this.group;}
    /**
<checker>
The unit is child of this group, or from parent group
    */
    public boolean isChild(serverUnit unit) {
        if (unit == null) return false;
        // to iterate the children
        synchronized(this.group) {
            for(Iterator i=this.group.iterator();i.hasNext();) {
                if (unit.equals(i.next())) return true;// finded child
            }
        }
        serverUnit parent = this.getOwner();// try find in owner
        if (parent instanceof groupUnit) return ((groupUnit)parent).isChild(unit);
        return false;
    }
   /**
<mutator>
To set new owner of this unit (null for root unit)
   */
   public void setOwner(serverUnit owner)
   {
        super.setOwner(owner);
        // to update children for change
        // the UnitRegistry path and reregister units
        synchronized(this.group) {
            for(Iterator i=this.group.iterator();i.hasNext();) {
                ((serverUnit)i.next()).setOwner(this);
            }
        }
   }
}
