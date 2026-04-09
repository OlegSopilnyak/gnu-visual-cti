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

/**
The interface describes the actions ID and ID constants

Attributes:
int ID is Read/Write attribute of action for define the action's type
String description is ReadOnly attribute for the description of action (related with ID)
*/
public interface serverActionID {
/**
predefined ID for error
*/
public static final short ERROR_ID = -1;
/**
predefined ID for stop action
*/
public static final short STOP_ID = 0;
/**
predefined ID for start action
*/
public static final short START_ID = 1;
/**
predefined ID for state update action
*/
public static final short STATE_ID = 2;
/**
predefined ID for get action
*/
public static final short GET_ID = 3;
/**
predefined ID for set action
*/
public static final short SET_ID = 4;
    
    /**
    <accessor>
    to get action's ID (type)
    */
    short getID();
    
    /**
    <mutator>
    to setting up action's ID (type)
    */
    void setID(short ID);

    /**
    <accessor>
    to get action's description
    */
    String getDescription();
}
