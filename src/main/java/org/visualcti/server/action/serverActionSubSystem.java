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
package org.visualcti.server.action;
/**
The interface describes subsystem attributes of action and constants
*/
public interface serverActionSubSystem
{
///////////// SERVICE_SYSTEM subsystem begin //////////////
/**
Const, name for messaging service (Service subsystem)
public final static String SERVICE_SYSTEM = "Service";
*/
public static final String MESSAGE_SUB_SYSTEM = "Messenger";
/**
Const, name for messaging service (Service subsystem)
public final static String SERVICE_SYSTEM = "Service";
*/
public static final String DATABASE_SUB_SYSTEM = "Database";
/**
Const, ID for messaging service (Service subsystem)
public final static short SERVICE_SYSTEM_ID = 400;
*/
public static final short MESSAGE_SUB_SYSTEM_ID = serverActionSystem.SERVICE_SYSTEM_ID + 10;
/**
Const, ID for database service (Service subsystem)
public final static short SERVICE_SYSTEM_ID = 400;
*/
public static final short DATABASE_SUB_SYSTEM_ID = serverActionSystem.SERVICE_SYSTEM_ID + 20;
///////////// SERVICE_SYSTEM subsystem end //////////////
    /**
    <accessor>
    method, to get action Sub System ID
    */
    short getSubSystemID();
    /**
    <accessor>
    method, to get action Sub System name
    */
    String getSubSystemName();
}
