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
package org.visualcti.server.hardware;

import java.util.*;

import org.visualcti.server.*;
import org.visualcti.server.core.Engine;

/**
<manager>
Manager of all hardware factories
*/
public final class HardwareManager
                        extends org.visualcti.server.groupUnitAdapter
                        implements Manager
{
    /**
    <constructor>
    for internal use only (private)
    to unregister himself in UnitRegistry
    */
    private HardwareManager(){this.setOwner(null);}
/**
Reference to Manager (for realize Singleton concept)
*/
private static Manager instance = null;
    /**
    <producer>
    To get access to Manager reference
    */
    public static final Manager getManager()
    {
        if (instance == null)
        {
            synchronized(HardwareManager.class)
            {
                if (instance == null) instance = new HardwareManager();
            }
        }
        return HardwareManager.instance;
    }
    /**
    <mutator>
    to add deviceFactory for management
    */
    public final void addDeviceFactory( deviceFactory factory ){this.addChild( factory );}
    /**
    <mutator>
    to remove deviceFactory for management
    */
    public final void removeDeviceFactory( deviceFactory factory ){this.removeChild( factory );}
    /**
    <accessor>
    get access to factories List
    */
    public final java.util.List factories(){return this.children();}
    /**
    <accessor>
    get access to devices List
    */
    public final java.util.List devices()
    {
        ArrayList copy = null;
        synchronized( this.devices ){copy=(ArrayList)this.devices.clone();}
        return copy;
    }
/**
<attribute>
state of engine
*/
private Engine.State state = Engine.State.OUT_OF_SERVICE;
   /**
<action>
to Start engine
if engine can't start, throws IOException
   */
   public final void Start() throws java.io.IOException
   {
        if ( this.isStarted() ) return;
        this.state = Engine.State.IN_SERVICE;
        // to start all factories
        for(Iterator i=this.factories().iterator();i.hasNext();)
        {
            deviceFactory factory = (deviceFactory)i.next();
            factory.Start();
        }
        // dispatch success start event
        this.dispatch( new unitEvent(this,unitAction.START_ID) );
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
        if ( this.isStopped() ) return;
        this.state = Engine.State.OUT_OF_SERVICE;
        // to stop all factories
        for(Iterator i=this.factories().iterator();i.hasNext();)
        {
            deviceFactory factory = (deviceFactory)i.next();
            factory.Stop();
        }
        // dispatch success stop event
        this.dispatch( new unitEvent(this,unitAction.STOP_ID) );
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
   public final Engine.State getState(){return this.state;}
/**
Pool of available devices
*/
private final ArrayList devices = new ArrayList(100);
    /**
<mutator>
to add child to group
    */
    public final void addChild(serverUnit child)
    {
        if (child instanceof deviceFactory)
        {
            super.addChild( child );
            this.addFactoryDevices((deviceFactory)child);
        }
    }
    /**
    <mutator>
    to add devices from factory to devices pool
    */
    private final void addFactoryDevices(deviceFactory factory)
    {
        generalDeviceProxy device[] = factory.devices();
        if ( device == null) super.removeChild(factory);
        else {
            synchronized( this.devices )
            {
                for(int i=0;i < device.length;i++) this.devices.add( device[i] );
                Collections.sort( this.devices, new deviceComparator() );
            }
        }
    }
    /**
    <comparator> for compare 2 devices
    */
    private final class deviceComparator implements Comparator
    {
        public final int compare(Object d1, Object d2)
        {
            generalDeviceProxy device1 = (generalDeviceProxy)d1;
            generalDeviceProxy device2 = (generalDeviceProxy)d2;
            String d1name = device1.getName();
            String d2name = device2.getName();
            return d1name.compareTo( d2name );
        }
    }
    /**
<mutator>
to remove child from group
    */
    public final void removeChild(serverUnit child)
    {
        if (child instanceof deviceFactory)
        {
            super.removeChild( child );
            try {((deviceFactory)child).Stop();}catch (java.io.IOException e){}
            this.removeFactoryDevices((deviceFactory)child);
        }
    }
    /**
    <mutator>
    to remove devices from factory from devices pool
    */
    private final void removeFactoryDevices(deviceFactory factory)
    {
        synchronized( this.devices )
        {
            for (Iterator i= this.devices.iterator();i.hasNext();)
            {
                generalDeviceProxy device = (generalDeviceProxy)i.next();
                if (device.getFactory().equals(factory)) i.remove();
            }
        }
    }
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
   process configuration (nothing)
   */
   protected final void processConfiguration(org.jdom.Element xml) {}
   /**
<accessor>
To get Name of unit
   */
   public final String getName(){return "Hardware Manager";}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return "[manager]";}
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return null;}
   /**
<accessor>
To get Path to object instance
   */
   public final String getPath(){return "/Hardware";}
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
}
