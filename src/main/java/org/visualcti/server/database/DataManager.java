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
package org.visualcti.server.database;

import java.io.*;
import java.sql.*;

import org.jdom.*;

import org.visualcti.server.*;
import org.visualcti.server.service.*;
/**
Security manager for server
*/
public final class DataManager extends ServiceAdapter implements Database
{
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
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
   public final String getType(){return super.getType()+" database";}
   /**
<accessor>
access to service name
   */
   public final String getName(){return "Database";}
   /**
   process configuration
   */
   protected final void processConfiguration(Element xml)
   {
   }
   /**
<action>
to Start database service
   */
   public final void Start() throws IOException
   {
        if ( this.isStarted() ) return;
        this.state = Service.State.START;// setting up new state
        dispatch(new unitEvent(this,unitEvent.START_ID));
   }

   /**
<action>
to Stop database service
   */
    public final void Stop() throws IOException
    {
        if ( this.isStopped() ) return;
        this.state = Service.State.STOP;// setting up new state
        dispatch(new unitEvent(this,unitEvent.STOP_ID));
    }
    /**
    <producer>
    to make database connection by request
    */
    public final Connection getConnection(connectionRequest  request)
    {
        try {
            Class drvClass = Class.forName( request.getDriverClass(),true,DataManager.class.getClassLoader() );
            Driver driver = (Driver)drvClass.newInstance();
            String url = request.getURL();
            String login = request.getLogin();
            String password = request.getPassword();
            return DriverManager.getConnection(url,login,password);
        }catch(Exception e){
            dispatch( new unitError(this,e,"can't process "+request) );
        }
        return null;
    }
}
