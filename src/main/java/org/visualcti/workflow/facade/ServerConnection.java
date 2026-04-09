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
package org.visualcti.workflow.facade;

import java.io.*;
import java.util.*;

import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow,<br>
 * The skeleton of connection to telephony server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public interface ServerConnection
{
/**
 * <mutator>
 * To add the disconnect's listener
 * @param listener the listener to add
 * @throws TooManyListenersException if can't add
 */
void addDisconnecteListener(disconnectListener listener) throws TooManyListenersException;
/**
 * <mutator>
 * To remove the disconnect's listener
 * @param listener listener to remove
 */
void removeDisconnectListener(disconnectListener listener);
/**
 * <accessor>
 * To get access to connection's state
 * @return true if connected
 */
boolean isConnected();
/**
 * <action>
 * To login to the server
 * @param server server's address
 * @param login server's login
 * @param password login's password
 * @return true if connected
 */
boolean connect(String server,String login,char[] password);
/**
 * <action>
 * To disconnect from server
 */
void disconnect();
/**
 * <accessor>
 * To get access to array of tasks groups names
 * @return the groups names
 * @throws IOException if not login
 */
String[] getTaskGroups() throws IOException;
/**
 * <accessor>
 * To get access to programms list for tasks pool
 * @param taskPool the name of tasks pool
 * @return the array of names
 * @throws IOException if not login
 */
String[] getProgramms(String taskPool) throws IOException;
/**
 * <action>
 * To load the programm from the server
 * @param name programm's name
 * @param taskPool pool's name
 * @return the programm to edit
 * @throws IOException if not login
 */
Program load(String name, String taskPool) throws IOException;
/**
 * <action>
 * To deploy the programm to the server
 * @param taskPool the name of a pool
 * @param programm the programm to deploy
 * @throws IOException if not login
 */
void deploy(String taskPool, Program programm) throws IOException;
/**
 * <listner>
 * The listener of disconnect
 */
public interface disconnectListener{void disconnected();}
}
