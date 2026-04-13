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
package org.visualcti.server.action;

import java.io.*;
import java.util.*;
import org.visualcti.server.serverUnit;

/**
Adapter for any severAction

Attributes:
int actionClass is readOnly attribute from enumeration {ERROR|EVENT|COMMAND}
int sequenceID is ReadOnly attribute of Action, used for properly make the answer to action
String unitPath is ReadOnly attribute contents path to serverUnit
int ID is Read/Write attribute of action for define the action's type
String description is ReadOnly attribute for the description of action (related with ID)
*/
public abstract class serverActionAdapter
                            implements serverAction
{
    /** attribute, for support serialization compatibility */
    static final long serialVersionUID = 857368303519352473L;
/**
ReadOnly attribute The action's date/time
*/
private transient long date;
/**
The action type (ID)
*/
private transient short ID;
/**
ReadOnly attribut of Action, used for properly make the answer to action
*/
private transient int sequenceID;
/**
ReadOnly attribute contents path to serverUnit instance in repository
*/
private transient String unitPath;
/**
The action description
*/
private transient String description;
    /**
    Constructor. Empty, for deserialization
    */
    public serverActionAdapter()
    {
        // to init default values
        this.date       = 0L;
        this.sequenceID = -1;
        this.ID         = ID;
        this.unitPath   = null;
        this.description= null;
    }
    /**
    Constructor for serverUnit and ID
    */
    public serverActionAdapter
                            (
                            serverUnit who,
                            short ID
                            )
    {
        this(who,ID,null);
    }
    /**
    Constructor for serverUnit and description
    */
    public serverActionAdapter
                            (
                            serverUnit who,
                            String description
                            )
    {
        this(who,STATE_ID,description);
    }
    /**
    Constructor for serverUnit, ID and description
    */
    public serverActionAdapter
                            (
                            serverUnit who,
                            short ID,
                            String description
                            )
    {
        this.date       = new Date().getTime();
        this.unitPath   = who.getPath();
        this.ID         = ID;
        this.description= description;
    }
////////////// ATTRIBUTES PART ////////
    /**
    <accessor>
    to get access to action's class
    attribute from enumeration {ERROR,EVENT,COMMAND}
    will use for store/restore methods.
    Will by defined in Action's implementation as final method
    */
    public abstract short actionClass();

    /**
    <accessor>
    The date and time, when happened action
    serverAction producer must setting up this attribute
    */
    public Date getDate(){return new Date(this.date);}
    
    /**
    <accessor>
    to get action's sequence ID
    serverAction producer must setting up this attribute
    */
    public final int sequenceID()
    {
        if (this.sequenceID <= 0) this.setNextSequenceID();
        return this.sequenceID;
    }
/** counter for dispached sequence */
private static int sequenceCounter = 1;
        /** to made new sequenceID */
        private final void setNextSequenceID()
        {
            synchronized(serverActionAdapter.class)
            {
                this.sequenceID = sequenceCounter;
                sequenceCounter++;
            }
        }
    
    /**
    <accesor>
    to get access to Path to serverUnit in UnitRegistry
    when action will constucted, in constructor will
    transferred serverUnit reference
    */
    public final String getUnitPath(){return this.unitPath;}
    
    /**
    <accessor>
    to get action's ID (type)
    */
    public final short getID(){return this.ID;}
    
    /**
    <mutator>
    to setting up action's ID (type)
    */
    public final void setID(short ID){this.ID=ID;}

    /**
    <accessor>
    to get action's description
    */
    public String getDescription()
    {
        if (this.description != null) return this.description;
        switch( this.ID ) 
        {
            case START_ID: return "Start";
            case STOP_ID : return "Stop";
            case STATE_ID: return "State";
            case GET_ID  : return "Get";
            case SET_ID  : return "Set";
        }
        return null;
    }

    /**
    Override Object.toString()
    */
    public String toString()
    {
        return  //"Date: "+this.getDate()+
                " {"+this.unitPath+"}"+
                ", SeqID = "+this.sequenceID()+
                ", ID = "+this.getID()+
                ", ["+this.getDescription()+"]";
    }

////////// STORE/RESTORE PART ////////
    /**
    <store>
    to store action's attributes to DataOutput
    will be used for transport objects
    */
    public void store(DataOutput out) throws IOException
    {
        if (this.unitPath == null) throw new IOException("The unitPath is null...");
        out.writeLong(this.date);
        out.writeInt(this.sequenceID);
        out.writeUTF(this.unitPath);
        out.writeShort(this.ID);
        out.writeUTF(this.description == null?"":this.description);
    }
    
    /**
    <restore>
    to restore action's attributes from DataInput
    will be used for transport objects
    */
    public void restore(DataInput in) throws IOException
    {
        this.date           = in.readLong();
        this.sequenceID     = in.readInt();
        this.unitPath       = in.readUTF();
        this.ID             = in.readShort();
        String desc         = in.readUTF();
        this.description    = desc.length() == 0 ? null:desc;
    }

///////////// SERIALIZE /DESERIALIZE PART //////////
    /**
     <store>
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @serialData Overriding methods should use this tag to describe
     *             the data layout of this Externalizable object.
     *             List the sequence of element types and, if possible,
     *             relate the element to a public/protected field and/or
     *             method of this Externalizable class.
     *
     * @exception IOException Includes any I/O exceptions that may occur
     */
    public final void writeExternal(ObjectOutput out) throws IOException
        {this.store(out);}

    /**
     <restore>
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
        {this.restore(in);}
}
