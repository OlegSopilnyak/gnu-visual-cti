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
package org.visualcti.server.service;

import org.visualcti.server.serverUnit;
import org.visualcti.server.action.event.unitEventListener;

import java.io.*;

/**
Parent of any Service of Server
*/
public interface Service extends 
                            serverUnit,
                            unitEventListener
{
   
   /**
   <enum>
   class for sevice state constants
   */
   public static final class State
   {
        public State(){}// constructor
        /**
        const for started service state
        */
        public static final short START = 1;
           
        /**
        const for stopped service state
        */
        public static final short STOP = 0;
   }
   
   /**
<action>
to Start service
if service can't start, throws IOException
   */
   void Start() throws IOException;
   /**
<accessor>
is Service have START state
   */
   boolean isStarted();
   
   /**
<action>
to Stop service
if service can't stop, throws IOException
   */
   void Stop() throws IOException;
   /**
<accessor>
is Service have STOP state
   */
   boolean isStopped();
   
   /**
<accessor>
access to service name
   */
   String getName();
   
   /**
<accessor>
current service state
   */
   short getState();
   
   /**
<accessor>
current service's ThreadGroup
   */
   ThreadGroup getThreadGroup();
   
////////////////// INHERITING PART /////////////////
   /**
<accessor>
Access to service owner
The one whom this service is subordinate
   */
   Service getServiceOwner();

   /**
<mutator>
Setting up service owner
The one whom this service is subordinate
   */
   void setServiceOwner(Service owner);
   
   /**
<accessor>
The check, is service depended from master
   */
   boolean isDependsFrom(Service master);
   
///////// UNIT EVENTS PART //////////////////
   /**
<mutator>
to add unitEventListener
   */
   void addUnitEventListener(unitEventListener listener);
   /**
<mutator>
to remove unitEventListener
   */
   void removeUnitEventListener(unitEventListener listener);
}
