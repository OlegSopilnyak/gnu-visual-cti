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
package org.visualcti.server.task;

import java.io.*;
import java.util.*;

import org.jdom.*;

import org.visualcti.server.*;
//import org.visualcti.util.Config;
/**
<manager>
The manager of tasks (implementation)
*/
public final class TaskManager
                    extends
                        groupUnitAdapter
                    implements
                        Manager
{
/**
<instance>
One instance of the Manager (Singleton)
*/
private static Manager instance=null;
    /**
    <producer>
    To get access to tasks manager instance
    */
    public static Manager getManager() {
        if (instance != null) return instance;
        synchronized(TaskManager.class){
            if (instance == null) instance = new TaskManager();
        }
        return instance;
    }
    /**
    <constructor>
    */
    private TaskManager(){}
/**
<attribute>
state of engine
*/
private short state = Engine.State.OUT_SERVICE;
   /**
<action>
to Start engine
if engine can't start, throws IOException
   */
   public final void Start() throws java.io.IOException
   {
        if ( this.isStarted() ) return;
        // to start pools
        this.startPools();
        // to update the state
        this.state = Engine.State.IN_SERVICE;
        // dispatch success start event
        this.dispatch( new unitEvent(this,unitAction.START_ID) );
   }
   /**
   <action>
   To start all children tasks pools
   */
   private final void startPools() throws java.io.IOException
   {
        if ( this.main_Dir == null) throw new IOException("not defined base directory");
        if ( !this.main_Dir.exists() && !this.main_Dir.mkdirs())
        {
            throw new IOException("in XML inavid name of base directory");
        }
        // to start all children tasks pools
        for(Iterator i=this.children().iterator();i.hasNext();)
            {((TaskPool)i.next()).Start();}
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
        // to stop pools
        this.stopPools();
        // to update the state
        this.state = Engine.State.OUT_SERVICE;
        // dispatch success stop event
        this.dispatch( new unitEvent(this,unitAction.STOP_ID) );
   }
   /**
   <action>
   To start all children tasks pools
   */
   private final void stopPools() throws java.io.IOException
   {
        for(Iterator i=this.children().iterator();i.hasNext();)
            {((TaskPool)i.next()).Stop();}
   }
   /**
<accessor>
is Engine have State.OUT_SERVICE state
   */
   public final boolean isStopped(){return this.state == Engine.State.OUT_SERVICE;}

   /**
<accessor>
current engine state
   */
   public final short getState(){return this.state;}
   /**
<accessor>
To get Name of unit (abstract)
   */
    public final String getName(){return "Tasks Manager";}
    /**
<accessor>
To get Path to unit instance in repository
   */
    public final String getPath(){return "/Tasks";}
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
    protected final void processConfiguration(Element xml)
    {
        Element par = xml.getChild("parameter");
        try {
            Parameter dir = Parameter.restore(par);
            if (dir != null) this.main_Dir = new File(dir.getStringValue());
        }catch (Exception e){}
    }
/**
<attribute>
Base directory of tasks system
*/
private File main_Dir = new File("./TASKS");
/**
<accessor>
To get access to tasks root directory
*/
public File getRoot(){return this.main_Dir;}
    /**
<mutator>
to add child to a group
    */
    public final void addChild(serverUnit child) {
        if (child instanceof TaskPool) {
            TaskPool pool = (TaskPool)child;
            String name = pool.getName();
            synchronized(this.poolStore)
            {
                if (this.poolStore.get(name) != null) return;// pool in store
                if (pool.isPublic()) this.publicPool = pool;// this is public pool
                else this.poolStore.put( name, pool );// to store pool for get access by name
                super.addChild(child);// To attach the child pool
            }
        }
    }
    /**
<mutator>
to remove child from group
    */
    public final void removeChild(serverUnit child) {
        if (child instanceof TaskPool) {
            super.removeChild(child);// to remove from children list
            TaskPool pool = (TaskPool)child;
            if (child == this.publicPool) this.publicPool=null;
            synchronized(this.poolStore)
            {   // to remove from pool store
                this.poolStore.remove( pool.getName() );
            }
        }
    }
    /**
     * <mutator>
     * To remove the children
     * Only device's pools
     */
    public final void removeChildren(){
      synchronized( this.poolStore ){
        for(Iterator i=this.poolStore.values().iterator();i.hasNext();){
          super.removeChild( (TaskPool)i.next() );
        }
        this.poolStore.clear();
      }
    }
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
           case unitAction.START_ID: this.Start(); return;
           case unitAction.STOP_ID:  this.Stop(); return;
           case unitAction.GET_ID:   this.processGet(command); return;
       }
       throw new UnknownCommandException();
   }
    private final void processGet(unitCommand get){
      unitResponse answer = new unitResponse(get).well();
      super.dispatch(answer);
    }
/**
<attribute>
The task pools store
*/
private final HashMap poolStore = new HashMap(100);
/**
<attribute>
The public tasks pool
*/
private TaskPool publicPool=null;
/**
<accessor>
get access to public TaskPool
*/
    public TaskPool publicTaskPool(){return this.publicPool;}
/**
<accessor>
get access to TaskPool for scheduler by CT-device name
*/
    public TaskPool getTaskPool(String name, String factory)
    {
      String key = factory+"/"+name;
      synchronized(this.poolStore)
      {
        TaskPool pool = (TaskPool)this.poolStore.get( "System/"+name );
        if (pool == null)pool=(TaskPool)this.poolStore.get( key );
        else pool.setGroup(factory);
        if (pool == null)
        {   // The pool not created, will make it
          Element xml = new Element("pool");
          xml.setAttribute(new Attribute("type", "local"));
          xml.setAttribute(new Attribute("file", name+".tasks.pool"));
          pool = new TaskPool(name, factory);
          this.addChild( pool );
          pool.configure(xml); xml = pool.getXML();
          if ( pool.getOwner() != null ) {
            this.poolStore.put(key,pool);
//            super.configuration.getParent().addContent(xml);// add element to main config
            Config.save();// to save updates
          } else return null;
        }
        return pool;
      }
    }
}
