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
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;
import org.visualcti.util.Tools;

/**
<pool>
The pool of tasks associated with some name (device, or public)
*/
public final class TaskPool
                    extends
                        serverUnitAdapter
                    implements
        Engine
{
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
        if ( this.isStarted() || this.pool.size() == 0) return;
        // to copy pool to theRing
        synchronized(this.pool){this.theRing.addAll( this.pool );}
        // update the state
        this.state = Engine.State.IN_SERVICE;
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
        // to stop a current task
        try{ this.current().stopExecute();
        }catch(NullPointerException e){}
        // update the state
        this.state = Engine.State.OUT_OF_SERVICE;
        // clear the tasks ring
        synchronized(this.theRing){this.theRing.clear();this.current=null;}
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
   public short getState(){return this.state.getCode();}

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
To get Current state of unit
   */
   public final String getUnitState(){return this.isStarted() ? "active":"passive";}
/**
 * <const> The name of root XML's element
 */
public static final String ELEMENT = "pool";
   /**
   <transfer>
   To transfer the TaskPool to XML
   */
   public final Element getXML()
   {
        if (this.name == null) return null;// invalid TaskPool
        // to make XML for tasks pool
        Element xml = new Element(ELEMENT);
        xml.setAttribute(new Attribute("type", this.poolType));
        xml.setAttribute(new Attribute("name", this.group+"/"+this.name));
        xml.setAttribute(new Attribute("file", this.poolFile));
        this.saveContent();// to save pool to file
        // to return the XML representation of TaskPool
        return xml;
   }
   /**
   to save tasks to pool's file
   */
   private void saveContent(){
        if ( this.content == null ) return;
        Element xml = new Element("TasksPool");
        xml.setAttribute(new Attribute("type", this.poolType));
        xml.addContent(new Comment("The tasks pool for ["+this.name+"]"));
        for (Iterator i=this.pool.iterator();i.hasNext();) {
          Task task = (Task)i.next();
          xml.addContent( task.getXML() );
        }
        Tools.xmlSave( xml, this.content );
   }
   /**
   <processor>
   process configuration
   To restore tasks for this pool
   */
   protected final void processConfiguration(Element xml)
   {
        if ( !ELEMENT.equals(xml.getName()) ) return;
        this.poolType = xml.getAttributeValue("type");
        StringTokenizer st = new StringTokenizer(this.name,"/");
        if ( st.countTokens() > 1 ){
          this.group = st.nextToken(); this.name = st.nextToken();
        }
        this.loadContent( xml );
   }
   /**
   <loader>
   To load tasks pool file
   */
   private final void loadContent(Element xml)
   {
        String file = this.poolFile = xml.getAttributeValue("file");
        if (file == null){this.removeMy(); return;}

        if (file.indexOf("/") != -1 || file.indexOf("\\") != -1)
            this.content = new File(file);
        else {
            Manager mng = (Manager)this.getOwner();
            if (mng == null) return;
            this.content = new File(mng.getRoot(),file);
        }
        if ( !this.content.exists() ) this.saveContent();
        else {
            Element e = Tools.xmlLoad(this.content);
            for (Iterator i=e.getChildren("task").iterator();i.hasNext();)
            {
                Element task = (Element)i.next();
                this.add( TaskMaker.restore(task) , false );
            }
        }
   }
   private final void removeMy() {
       serverUnit owner = this.getOwner();
       if (owner instanceof groupUnit)((groupUnit)owner).removeChild(this);
   }
/**
<attribute>
The file name
*/
private String poolFile = null;
/**
<attribute>
File with content of tasks pool
*/
private File content = null;
/**
<attribute>
The type of tasks pool (local/public)
*/
private String poolType = "local";
   /**
   <accessor>
   If pool is public type
   */
   public final boolean isPublic(){return "public".equals(this.poolType);}
/**
<attribute>
The name of pool owner (ct-device)
*/
private volatile String name;
/**
 * <attribute>
 * The name of telephony factory (for group the pools)
 */
private volatile String group;
   /**
    * <mutator>
    * To assign the group's name to the pool
    * @param group group's name
    */
   public final void setGroup(String group)
   {
      if ( this.group.equals(group) ) return;
      this.group=group;
      Attribute name = new Attribute("name", this.group+"/"+this.name);
      super.configuration.setAttribute(name);
      Config.save();
   }
   /**
   <constructor>
   */
   public TaskPool(String name){
      this(name,"System");
      StringTokenizer st = new StringTokenizer(this.name,"/");
      if ( st.countTokens() > 1 ){
        this.group = st.nextToken(); this.name = st.nextToken();
      }
   }
   /**
   <constructor>
   */
   public TaskPool(String name,String group)
   {
    this.name=name; this.group=group;
   }
   /**
<accessor>
To get Name of unit
   */
   public final String getName(){return group+"/"+name;}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return "[tasks pool]";}
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return null;}
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
           case unitAction.GET_ID:  this.processGet(command); return;
           case unitAction.SET_ID:  this.processSet(command); return;
       }
       throw new UnknownCommandException("not supported");
   }
      private final void processGet(unitCommand command) throws Exception{
        Parameter param = command.getParameter("target");
        if ( param == null ) throw new UnknownCommandException("no target's parameter");
        String target = param.getValue("invalid target");
        if ( target.equals("info") ) processGetInfo(command);
        else
        if ( target.equals("edit") ) processGetEdit(command);
        else
          throw new UnknownCommandException("Invalid target ["+target+"]");
      }
      private final void processGetInfo(unitCommand command) throws Exception{
        unitResponse answer = new unitResponse(command).well();
        synchronized( this.theRing ) {
          answer.set(new Parameter("unit.state",this.getUnitState()));
          Task task = this.current();
          if ( current != null )
            answer.set(new Parameter("current",task.getName()));
          answer.set(new Parameter("tasks.list",this.tasksList()));
        }
        super.dispatch(answer);
      }
      private final void processGetEdit(unitCommand command)throws Exception{
        Parameter param = command.getParameter("task");
        if ( param == null ) throw new UnknownCommandException("nothing to edit");
        String name = param.getValue("????:-P");
        Task task = this.getTask(name);
        if ( task == null ) throw new UnknownCommandException("invalid task's name ["+name+"]");
        unitResponse answer = new unitResponse(command).well();
        answer.set(new Parameter("task",task.getXML()));
        answer.set(new Parameter("edit.class","nothing :-("));
        // to dispath the answer
        super.dispatch(answer);
      }
      private final void processSet(unitCommand command)throws Exception{
        Parameter param = command.getParameter("type");
        if ( param == null ) throw new UnknownCommandException("no type's parameter");
        String type = param.getValue("invalid target");
        if ( type.equals("deploy") ) processSetDeploy(command);
        else
        if ( type.equals("install") ) processSetInstall(command);
        else
        if ( type.equals("delete") ) processSetDelete(command);
        else
        if ( type.equals("move") ) processSetMove(command);
        else
          throw new UnknownCommandException("Invalid set type ["+type+"]");
      }
      private final void processSetDeploy(unitCommand command)throws Exception{
        Parameter param = command.getParameter("deploy");
        if ( param == null ) throw new UnknownCommandException("nothing to deploy");
        unitResponse answer = new unitResponse(command).well();
        Element xml = param.getValue(Tools.emptyXML);
        Task task = TaskMaker.restore( xml );
        if ( task == null )answer.bad().set(new Parameter("reason","invalid task in XML"));
        else answer.set(new Parameter( "deploy", this.update(task) ));
        // to dispath the answer
        super.dispatch(answer);
      }
      private final void processSetInstall(unitCommand command)throws Exception{
        Parameter param = command.getParameter("install");
        if ( param == null ) throw new UnknownCommandException("nothing to install");
        unitResponse answer = new unitResponse(command).well();
        if ( param.getType().equals(Parameter.STRING) ) {
          TaskManager manager = (TaskManager)super.getOwner();
          TaskPool pPool = manager.publicTaskPool();
          String name = param.getValue("????:-P");
          Task task = pPool.getTask(name);
          if ( task == null )answer.bad().set(new Parameter("reason","no task in public"));
          else answer.set(new Parameter( "install", this.add(task,true) ));
        }else
        if ( param.getType().equals(Parameter.XML) ) {
          Element xml = param.getValue(Tools.emptyXML);
          Task task = TaskMaker.restore(xml);
          if ( task == null )answer.bad().set(new Parameter("reason","invalid task in XML"));
          else answer.set(new Parameter( "install", this.add(task,true) ));
        }else answer.bad().set(new Parameter("reason","invalid type of install in XML"));
        // to dispath the answer
        super.dispatch(answer);
      }
      private final void processSetDelete(unitCommand command) throws Exception{
        Parameter param = command.getParameter("task");
        if ( param == null ) throw new UnknownCommandException("nothing to delete");
        String name = param.getValue("????:-P");
        Task task = this.getTask(name);
        unitResponse answer = new unitResponse(command).well();
        if ( !this.remove(task) ) answer.bad();
        // to dispath the answer
        super.dispatch(answer);
      }
      private final void processSetMove(unitCommand command) throws Exception{
        Parameter param = command.getParameter("task");
        if ( param == null ) throw new UnknownCommandException("nothing to move");
        String name = param.getValue("????:-P");
        param = command.getParameter("direction");
        String direction = param.getValue("nowhere");
        Task task = this.getTask(name);
        unitResponse answer = new unitResponse(command).well();
        if ( "up".equalsIgnoreCase(direction) ){
          if ( !this.up(task) ) answer.bad();
        }else
        if ( "down".equalsIgnoreCase(direction) ){
          if ( !this.down(task) ) answer.bad();
        }else throw new UnknownCommandException("invalid move's direction "+direction);
        // to dispath the answer
        super.dispatch(answer);
      }
      private final String tasksList() {
        StringBuffer buf = new StringBuffer();
        for(Iterator i=this.tasks().iterator();i.hasNext();){
          Task task = (Task)i.next();
          buf.append(task.getName()).append("\n");
        }
        return buf.toString();
      }
/**
<attribute>
The tasks pool
*/
private final LinkedList pool = new LinkedList();
/**
<accessor>
The pool size
*/
    public final int size(){
        synchronized(this.pool){return this.pool.size();}
    }
      /**
       * <accessor>
       * To get access to task, by name
       * @param name task's name
       * @return task with the name or null if not found
       */
      private final Task getTask(String name) {
        if ( name == null ) return null;
        synchronized( this.pool ){
          for(Iterator i=this.pool.iterator();i.hasNext();){
            Task task = (Task)i.next();
            if ( name.equals(task.getName()) ) return task;
          }
        }
        return null;
      }

/**
<attribute>
Current task
*/
private transient volatile Task current=null;
/**
<accessor>
To get the current(executed) task. (returned last next() call)
This method works only when isStarted()
*/
    public final Task current(){
        if ( !this.isStarted() ) return null;
        synchronized(this.theRing){return this.current;}
    }

private final LinkedList theRing = new LinkedList();
/**
<action>
To get next task.
This method works only when isStarted()
otherwise will return null
*/
    public final Task next()
    {
        if ( !this.isStarted() ) return null;
        // try to get next Task
        synchronized(this.theRing){
            if (this.theRing.size() == 0) return null;// empty ring
            // get first Task in list
            Object top = this.theRing.removeFirst();
            if (top instanceof Task)
            {
                this.theRing.addLast(top);//place to tail of list
                return this.current = (Task)((Task)top).clone();
            }
            else return null;
        }
    }

/**
<accessor>
To get the tasks list.
*/
    public final List tasks()
    {
      synchronized(this.pool){return (List)this.pool.clone();}
    }

/**
<mutator>
To add the task to a pool.
*/
    public final boolean add(Task task, boolean notify)
    {
      unitAction event = null; boolean success = true;
      if (task == null || task.getName() == null) {
        event = new unitError(this,null,"Add:Invalid task to add");
        success = false;
      } else
      if ( this.getTask(task.getName()) != null) {
        event = new unitError(this,null,"Add:Task already exists");
        success = false;
      }else {
        synchronized(this.pool){this.pool.add(task);}
        if ( notify )this.saveContent();
        String message = "tasks.list\n"+this.tasksList();
        event = new unitEvent(this,message);
      }
      if (notify) super.dispatch(event);
      return success;
    }

/**
<mutator>
To remove the task from a pool.
*/
    public final boolean remove(Task task)
    {
      if (task == null || task.getName() == null){
        super.dispatch(new unitError(this,null,"Remove:Invalid task to remove"));
        return false;
      }
      unitAction event = new unitError(this,null,"Remove:No such Task");
      boolean success = false;
      synchronized(this.pool)
      {
        String name = task.getName();
        for(ListIterator i=this.pool.listIterator();i.hasNext();)
        {
          if ( name.equals(((Task)i.next()).getName()) ){
              i.remove(); success = true; break;
          }
        }
      }
      if ( success ) {
        this.saveContent();
        String message = "tasks.list\n"+this.tasksList();
        event = new unitEvent(this,message);
      }
      super.dispatch(event); return success;
    }

/**
<mutator>
To update the task in a pool.
*/
    public final boolean update(Task task)
    {
        if (task == null || task.getName() == null) return false;
        synchronized(this.pool)
        {
            String name = task.getName();
            for(ListIterator i=this.pool.listIterator();i.hasNext();)
            {
                Task tsk = (Task)i.next();
                if ( name.equals(tsk.getName()) )
                {// entry found
                    i.set(task); this.saveContent(); return true;
                }
            }
        }
        return this.add(task,true);
    }

/**
<order>
To move up the task in the tasks list.
*/
    public final boolean up(Task task)
    {
      if (task == null || task.getName() == null){
        super.dispatch(new unitError(this,null,"Up:Invalid task to remove"));
        return false;
      }
      unitAction event = new unitError(this,null,"Up:No such Task");
      boolean success = false;
      synchronized(this.pool)
      {
        if (this.pool.size() <= 1) return true;
        String name = task.getName();
        for(ListIterator i=this.pool.listIterator();i.hasNext();)
        {
          Task tsk = (Task)i.next();
          if ( name.equals(tsk.getName()) )
          {
            int index = i.previousIndex();
            if (index != -1)
            {
              Task older = (Task)this.pool.get( index );
              this.pool.set(index, task);i.set(older);
            }
            success = true; break;
          }
        }
      }
      if ( success ) {
        this.saveContent();
        String message = "tasks.list\n"+this.tasksList();
        event = new unitEvent(this,message);
      }
      super.dispatch(event); return success;
    }

/**
<order>
To move down the task in the tasks list.
*/
    public final boolean down(Task task)
    {
      if (task == null || task.getName() == null){
        super.dispatch(new unitError(this,null,"Down:Invalid task to remove"));
        return false;
      }
      unitAction event = new unitError(this,null,"Down:No such Task");
      boolean success = false;
      synchronized(this.pool)
      {
        if (this.pool.size() <= 1) return true;
        String name = task.getName();
        for(ListIterator i=this.pool.listIterator();i.hasNext();)
        {
          if ( name.equals(((Task)i.next()).getName()) ){
            int index = i.nextIndex();
            if (index != this.pool.size())
            {
                Task older = (Task)this.pool.get( index );
                this.pool.set(index, task);i.set(older);
            }
            success = true; break;
          }
        }
      }
      if ( success ) {
        this.saveContent();
        String message = "tasks.list\n"+this.tasksList();
        event = new unitEvent(this,message);
      }
      super.dispatch(event); return success;
    }
}
