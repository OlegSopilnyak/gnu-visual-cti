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

import java.util.*;

import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.server.database.Database;
import org.visualcti.server.message.Messenger;
/**
Class of schedulers group
*/
final class SchedulerGroup extends groupUnitAdapter implements Engine
{
    /**
    <accessor>
    To get access to database connections manager
    */
    public final Database getDatabase()
    {
        return (Database)UnitRegistry.lookup("/Service/Database", Database.class);
    }
    /**
    <accessor>
    To get access to system's messenger
    */
    public final Messenger getMessenger()
    {
        return (Messenger)UnitRegistry.lookup("/Service/Messenger", Messenger.class);
    }
/**
<attribute>
Timer for tasks, started as daemon
*/
private static final Timer timer = new Timer( true );
    /**
    <accessor>
    To get access to Timer
    */
    public Timer getTimer(){return timer;}
/**
<attribute>
state of engine
*/
private volatile Engine.State state = Engine.State.OUT_OF_SERVICE;
   /**
<action>
to Start engine
if engine can't start, throws IOException
   */
   public final void Start() throws java.io.IOException
   {
        // to start all schedulers
        for(Iterator i = this.children().iterator();i.hasNext();)
        {
            ((Scheduler)i.next()).Start();
        }
   }
   /**
<accessor>
is Engine have State.IN_SERVICE state
   */
   public final boolean isStarted(){return this.state == Engine.State.IN_SERVICE;}
   
   /**
<action>
to Stop engine
if engine can't stop, throws IOException
   */
   public final void Stop() throws java.io.IOException
   {
        // to stop all schedulers
        for(Iterator i = this.children().iterator();i.hasNext();)
        {
            ((Scheduler)i.next()).Stop();
        }
   }

   /**
<accessor>
is Engine have State.OUT_SERVICE state
   */
   public final boolean isStopped(){return this.state == Engine.State.OUT_OF_SERVICE;}
   
   /**
<accessor>
current engine state
   */
   public final short getState(){return this.state.getCode();}

    /**
     * <mutator>
     * To set up the new state value
     *
     * @param state new state ID of the engine
     */
    @Override
    public void setState(short state) {
        this.state = State.of(state);
    }

    /**
<accessor>
To get Name of unit (abstract)
   */
    public final String getName(){return "All schedulers";}
    /**
<accessor>
To get Path to unit instance in repository
   */
    public final String getPath(){return "/Scheduler/all";}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return "[schedulers tree]";}
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return null;}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState()
   {
        return this.isStarted() ? "in service":"out service";
   }
/**
<processor>
To process coniguration task manager configuration
*/
    protected final void processConfiguration(org.jdom.Element xml){}
   /**
   <executer>
   To execute command for this unit. 
   The method will call from the outside of unit.
   If command invalid, the exception will be occurred.
   */
   public final void execute(unitCommand command) throws Exception
   {
       try {super.execute(command); return;// try to parent's execute
       }catch(UnknownCommandException e){}
       switch( command.getID() )
       {
           case unitCommand.START_ID: this.Start(); return;
           case unitCommand.STOP_ID:  this.Stop(); return;
       }
       throw new UnknownCommandException();
   }
    /**
<mutator>
to add child to a group
    */
    public final void addChild(serverUnit child)
    {
        if (child instanceof Scheduler) super.addChild(child);// To attach the child pool
    }
/**
<counter>
Quantity of the stated Schedulers,
for calculation of a state of the Engine
*/
private int started = 0;
    /**
    <dispatcher>
    To dispatch event, error, or command response from unit
    The method will call inside of the unit.
    */
    public final void dispatch(unitAction action)
    {
        if (
            action instanceof unitEvent &&
            !action.getUnitPath().equals(this.getPath())
            )
        {
            synchronized(SchedulerGroup.class)
            {
                switch( action.getID() )
                {
                    case unitEvent.START_ID:
                                this.started++;
                                this.state = Engine.State.IN_SERVICE;
                                break;
                    case unitEvent.STOP_ID:
                                this.started--;
                                this.state = this.started > 0 ? 
                                        Engine.State.IN_SERVICE:Engine.State.OUT_OF_SERVICE;
                                break;
                }
            }
        }
        super.dispatch( action );
    }
}
