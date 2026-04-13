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

/**
Event occured inside of serverUnit
*/
public final class unitEvent extends unitAction
{
    static final long serialVersionUID = 7494252216923130152L;
    /**
    <accessor>
    to get access to action class's ID
    attribute from enumeration {ERROR,EVENT,COMMAND}
    will use for store/restore methods.
    Will by defined in Action's implementation as final method
    */
    final public short actionClass(){return unitAction.EVENT;}
    /**
    <accessor>
    get type of action
    */
    final protected String getType(){return "event";}
/** counter for dispached sequence */
private static int sequenceCounter = 1;
    /**
    <producer>
    to made new sequenceID
    */
    final protected int nextSequenceID()
    {
        int nextID=-1;
        synchronized(unitEvent.class){nextID = sequenceCounter++;}
        return nextID;
    }
    /**
    Overrided Object.toString()
    */
    final public String toString()
    {
        String desc = this.getDescription();
        return "Event: ["+
                this.actionByID()+
                ":"+this.sequenceID()+
                "] from "+this.getUnitPath()+
                ("".equals(desc)? "":" ("+desc+")");
    }
   /**
   <constructor>
   empty constructor for deserialization
   */
   public unitEvent(){super();}
   /**
   <constructor>
   make event from serverUnit, with description
   */
   public unitEvent(serverUnit unit,String desc){this(unit,STATE_ID,desc);}
   /**
   <constructor>
   make event from serverUnit, with actionID
   */
   public unitEvent(serverUnit unit,short ID){this(unit,ID,"");}
   /**
   <constructor>
   make event from serverUnit, with actionID and description
   */
   public unitEvent
                (
                serverUnit unit,
                short ID,
                String description
                )
    {   this();
        this.setUnitPath(unit.getPath());
        this.setDescription(description);
        this.setID(ID);
    }
}
