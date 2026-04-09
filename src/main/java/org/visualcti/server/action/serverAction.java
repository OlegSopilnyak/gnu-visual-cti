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

import org.jdom.*;
/**
Interface for represent actions of server
Error/Event/Command

Attributes:
int actionClass is readOnly attribute from enumeration {ERROR|EVENT|COMMAND}
int sequenceID is ReadOnly attribute of Action, used for properly make the answer to action
String objectPath is ReadOnly attribute contents path to serverObject
*/
public interface serverAction extends
                                java.lang.Cloneable,
                                java.io.Serializable,
                                org.visualcti.server.action.serverActionID
{
/**
const for errors action
*/
public static final short ERROR = -1;
/**
const for events action
*/
public static final short EVENT = 0;
/**
const for commands action
*/
public static final short COMMAND = 1;
/**
const for command response
*/
public static final short RESPONSE = 2;
////////////// ATTRIBUTES PART ////////
    /**
    <accessor>
    to get access to action's class
    attribute from enumeration {ERROR,EVENT,COMMAND}
    will use for store/restore methods.
    Will by defined in Action's implementation as final method
    */
    short actionClass();
    
    /**
    <accessor>
    The date and time, when happened action
    serverAction producer must setting up this attribute
    */
    java.util.Date getDate();

    /**
    <accessor>
    to get action's sequence ID
    serverAction producer must setting up this attribute
    */
    int sequenceID();
    
    /**
    <accesor>
    to get access to Path to serverObject in UnitRegistry
    when action will constucted, in constructor will
    transferred serverObject reference
    */
    String getUnitPath();
    
    /**
    <converter>
    to represent action as XML element
    */
    Element getXML();

////////// STORE/RESTORE PART ////////
    /**
    <store>
    to store action's attributes to OutputStream
    will be used for transport objects
    */
    void store(OutputStream out) throws IOException;
    
    /**
    <restore>
    to restore action's attributes from InputStream
    will be used for transport objects
    */
    void restore(InputStream in) throws IOException;

    /**
    <Object I/O helper>
    Class for Store/Restore Objects
    */
    public static final class IO
    {
        /**
        to store Object to bytes array
        */
        public static final byte[] store
                                    (
                                    Serializable obj
                                    )
                                    throws IOException
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(obj);oout.flush();oout.close();
            return bout.toByteArray();
        }
        /**
        to restore Object from bytes array
        */
        public static final Object restore
                                    (
                                    byte[] obj
                                    )
                                    throws IOException
        {
            ByteArrayInputStream bin = new ByteArrayInputStream(obj);
            ObjectInputStream in = new ObjectInputStream(bin);
            try {
                Object result = in.readObject(); in.close();
                return result;
            }catch (ClassNotFoundException e){
                return null;
            }
        }
    }
}
