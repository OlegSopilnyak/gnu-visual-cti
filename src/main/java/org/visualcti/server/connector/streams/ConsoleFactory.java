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
package org.visualcti.server.connector.streams;

import java.io.*;

import org.visualcti.server.connector.*;
import org.visualcti.util.SoftInputStream;

/**
<links factory>
Links factory for system console
*/
public final class ConsoleFactory extends LinkFactory
{
   /**
<accessor>
To get body of unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return super.getIcon();}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState(){return super.getUnitState();}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return super.getType()+" console links factory";}
/**
<semaphore>
for wait, while transport has works
*/
private final Object TRANSPORT = new Object();
/**
<flag>
is transport part of processor still work
*/
private volatile boolean transportWorks = false;
   /**
   <accessor>
   to get access to factory's GateKeeper
   */
   protected final GateKeeper getGateKeeper()
   {
        if ( transportWorks )
        {   // To wait, while transport works
            synchronized(TRANSPORT)
            {   // simple Object.wait() :-)
                try{TRANSPORT.wait();}catch(Exception e){}
            }
        }
        return this.isStarted() ? new Session():null;
   }
   /**
<action>
to Stop factory (nothing)
this factory never stop ;-)
   */
    public final void Stop() throws IOException{}
   /**
<accessor>
access to links factory name ("Console")
   */
   public final String getName(){return "Console";}
   /**
   Inner class Session (GateKeeper+Transport)
   */
   private final class Session extends Processor {
        /**
        Constructor
        */
        Session(){super(System.in,System.out,ConsoleFactory.this);}
        /**
        <notify>
        To notify on opening transport
        */
        protected final void transportOpened(){transportWorks = true;}
        /**
        Overrided Object.toString()
        */
        public final String toString()
        {
            return "System console GateKeeper+Transport";
        }
        /**
        To notify on closing transport
        */
        protected final void transportClosed()
        {
            if (transportWorks)
            {
              // to notify the semaphore
              synchronized(TRANSPORT){TRANSPORT.notify();}
            }
            transportWorks = false;
        }
   }
}
