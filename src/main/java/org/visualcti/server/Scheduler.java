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

import org.visualcti.server.core.Engine;
import org.visualcti.server.hardware.HardwareError;
import org.visualcti.server.hardware.generalDeviceProxy;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.server.task.Environment;
import org.visualcti.server.core.Task;
import org.visualcti.server.task.TaskPool;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Scheduler of the tasks for device channel</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
*/
public final class Scheduler
                    extends
                        serverUnitAdapter
                    implements
                        Runnable,
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
        if ( this.isStarted() ) return;

        // check, is thread started
        if ( !this.inService ) this.statThread();

        // to activate the device and tasks pool
        if ( !this.telephonyDevice.isOpened() ) this.telephonyDevice.open();
        this.tasksRing.Start();

        // to update the state
        this.state = Engine.State.IN_SERVICE;

        synchronized( this ) {// To construct a correct sequence of events
            // to resume suspended thread
            synchronized(this.semaphore){this.semaphore.notify();}
            // dispatch success start event
            this.dispatch( new unitEvent(this,unitAction.START_ID) );
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
        if ( this.isStopped() ) return;
        // to update the state
        this.state = Engine.State.OUT_OF_SERVICE;
        // to stop the tasks pool
        this.tasksRing.Stop();
        // to close the telephony's device
        if ( this.telephonyDevice.isOpened() ) this.telephonyDevice.close();
        // to clear the task's environment
        this.clearEnv();
        // notify about Scheduler's STOP
        synchronized( this ) {// To construct a correct sequence of events
            // dispatch success stop event
            this.dispatch( new unitEvent(this,unitAction.STOP_ID) );
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
   public final Engine.State getState(){return this.state;}
/**
<attribute>
The name of Scheduler
*/
private String name;
   /**
<accessor>
To get Name of unit (abstract)
   */
    public final String getName(){return this.name;}
    /**
<accessor>
To get Path to unit instance in repository
   */
    public final String getPath()
    {
      return new StringBuffer("/Scheduler/")
                      .append(this.telephonyDevice.getFactory().getVendor())
                      .append("/").append(this.name)
                      .toString();
    }
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return "[scheduler]";}
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return this.telephonyDevice.getIcon();}
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
   public final synchronized void execute(unitCommand command) throws Exception
   {
       try {super.execute(command); return;// try to parent's execute
       }catch(UnknownCommandException e){}
       switch( command.getID() )
       {
           case unitCommand.START_ID: this.Start(); return;
           case unitCommand.STOP_ID:  this.Stop(); return;
           case unitCommand.GET_ID:  this.getInfo(command); return;
       }
       throw new UnknownCommandException();
   }
      private final void getInfo(unitCommand command) throws Exception{
        Parameter param = command.getParameter("target");
        if ( param == null || !"info".equals(param.getValue()))
          throw new UnknownCommandException();
        unitResponse answer = new unitResponse(command);
        answer.set(new Parameter("hardware",this.telephonyDevice.getDeviceName()));
        answer.set(new Parameter("hardware.state",this.telephonyDevice.getStatus()));
        answer.set(new Parameter("started",this.isStarted()));
        try{answer.set(new Parameter("task",this.tasksRing.current().getName()));
        }catch(NullPointerException e){}
        super.dispatch(answer);
      }
/**
<resources>
The environment of this scheduler
*/
private final Environment env = new Environment();
/**
<attribute>
CT-device for system operations
*/
private generalDeviceProxy telephonyDevice;
/**
<attribute>
The tasks pool
*/
private TaskPool tasksRing;

    /**
    <constructor>
    */
    public Scheduler
                (
                String name,
                generalDeviceProxy device,
                TaskPool tasks
                )
    {
        this.name = name;
        this.telephonyDevice = device;
        this.current_device_name = "telephony/"+device.getDeviceName();
        // setting up the runtime environment for a tasks
        this.clearEnv();
        // to assign the Tasks pool
        this.tasksRing = tasks;
        // to start the main thread of the Scheduler
        this.statThread();
    }
/**
 * <name>
 * The entry's name in environment
 */
private final String current_device_name;
      /* to clear a scheduler's environment */
      private final void clearEnv(){
        env.clear();
        env.setPart(deviceProxy.SELECTED_DEVICE, this.current_device_name );
        env.setPart(this.current_device_name, this.telephonyDevice);
        env.setPart("stdout", new stdStream());
        env.setPart("stderr", new errStream());
      }
    /**
    to start scheduler's thread
    */
    private final void statThread()
    {
        Thread engine = new Thread(this,"<"+this.name+"> Scheduler thread");
        engine.setPriority( Thread.MAX_PRIORITY );
        engine.start();
        while ( !this.inService ) Thread.yield();
    }
private final Object semaphore = new Object();
private volatile boolean inService = false;
    /**
    to wait semaphore notification
    */
    private final void waitSemaphore(){
        try {synchronized(this.semaphore){this.semaphore.wait();}
        }catch(Exception e){}
    }
    /**
    <action>
    to suspend main thread
    */
    private final void stop(){
      try {this.Stop();}catch(java.io.IOException e){}
    }
    /**
    Main method
    */
    public final void run()
    {
        this.inService = true;
        try {
            while( true )
            {
                if ( !this.isStarted() ) this.waitSemaphore();
                Task task = this.tasksRing.next();
                try {
                    if (task == null) {this.stop();continue;}
                    // to open the telephony device
                    this.telephonyDevice.open();
                    // to attach the next task
                    this.attachTask( task ); Thread.yield();
                    // to execute attached task
                    task.execute(); Thread.yield();
                    // to detach the task
                    this.detachTask( task );Thread.yield();
                    // to close the telephony device
                    this.telephonyDevice.close();
                    // to free the resources
                    task=null; System.gc();
                    // to sleep 1 sec.
                    if ( this.isStarted() ) Thread.sleep( 1000 );
                }catch(Exception e){
//                    e.printStackTrace();
                    super.dispatch( new unitError(this,e) );
                }catch(HardwareError e){
                    if ( !this.telephonyDevice.restore() ) this.stop();
                }
            }
        }finally{
            this.inService = false;
        }
    }
/**
<attribute>
The context of current task
*/
private volatile TaskContext context=null;
    /**
    <connector>
    to connect task to scheduler
    */
    private final void attachTask(Task task)
    {
        if (this.context != null) this.context.destroy();
        this.context = new TaskContext( task );
        this.context.init();
    }
    /**
    <connector>
    to detach task from scheduler
    */
    private final void detachTask(Task task)
    {
        if (this.context != null) this.context.destroy();
        this.context = null;
    }
    /**
    <context>
    Class for store task's context
    */
    private final class TaskContext
    {
        private Task task;
        public TaskContext(Task task){this.task=task;}
        /**
        <action>
        to prepare task to execute
        */
        public void init(){
            if (this.task != null) Scheduler.this.setupTask( this.task );
        }
        /**
        <action>
        to detach environment from task
        */
        public synchronized void destroy(){
            if (this.task == null) return;
            task.stopExecute(); task.setEnv( null );
            try{task.finalize();}catch(Throwable t){}
            this.task = null;
            // to notify about new current task
            String message = "current\n \n";
            unitEvent event = new unitEvent(tasksRing,message);
            Scheduler.super.dispatch(event);
        }
    }
        /**
        <action>
        to adjust Environment for task
        */
        private final void setupTask(Task task)
        {
            // to get source of services
            SchedulerGroup group = (SchedulerGroup)this.getOwner();
            // to prepare the runtime environment for the task
            this.env.setPart( "timer",     group.getTimer()    );
            this.env.setPart( "database",  group.getDatabase() );
            this.env.setPart( "messenger", group.getMessenger());
            // to assign the environment with task
            task.setEnv( this.env );
            // to notify about new current task
            String message = "current\n"+task.getName();
            unitEvent event = new unitEvent(this.tasksRing,message);
            super.dispatch(event);
        }
/**
<task's output>
class Stream for standart output
*/
private final class stdStream extends SchedulerStream {
    public void notifyOwner(String message){// to sent debug message
        dispatch( new unitEvent(Scheduler.this, unitEvent.STATE_ID, message) );
    }
}
/**
<task's output>
class Stream for standart output
*/
private final class errStream extends SchedulerStream {
    public void notifyOwner(String message){// to sent error message
        dispatch( new unitError(Scheduler.this,message) );
    }
}
}
