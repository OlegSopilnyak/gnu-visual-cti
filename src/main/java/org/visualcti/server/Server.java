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
package org.visualcti.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.*;
import java.rmi.RMISecurityManager;
import java.rmi.registry.Registry;
import org.jdom.*;
import org.visualcti.util.*;
import org.visualcti.server.system.*;

import org.visualcti.server.service.Manager;
import org.visualcti.server.service.ServiceMaker;
import org.visualcti.server.log.Log;

import org.visualcti.server.hardware.generalDeviceProxy;
import org.visualcti.server.task.TaskPool;
import org.visualcti.server.message.Messenger;

/**
Main class of VisualCTI Server
*/
public final class Server extends serverUnitAdapter
{
    /** Constructor */
    public Server() throws Exception
    {
        // to register server in UnitRegistry
        UnitRegistry.register( this );
        // get configuration set for Server
        Element server = Config.getMainPart("Server");
        if (server == null) throw new Exception("Invalid Server section in XML");
        // try to make RMI registry
        Element RMI = server.getChild("rmi");
        if (RMI == null) throw new Exception("Invalid Server rmi section");
        Services.makeRegistry( RMI, this );
        // make server, using Server part from XML
        this.configure( server );
        // to start commands proxy
        new commander().start();
    }
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return null;}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState(){return "Active";}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return "[kernel]";}
/**
<attrubute>
RMI registry
*/
private Registry registry = null;
    /**
    <accessor>
    To assign local RMI registry for share objects
    */
    public final void setLocalRegistry
            (
            java.rmi.registry.Registry registry
            )
    {
        this.registry = registry;
    }
/**
<system>
Reference to Services system
*/
private org.visualcti.server.service.Manager services = null;
/**
<mutator>
To setting up the Server's Service manager
*/
public final void setServiceManager
        (
        org.visualcti.server.service.Manager manager
        )
{
    if (this.services == null) this.services=manager;
}
/**
<system>
Reference to the Logging system
*/
private org.visualcti.server.log.Log log = null;
/**
<mutator>
To setting up the Server's logger
*/
public final void setLogger
        (
        org.visualcti.server.log.Log log
        )
{
    if (this.log == null) this.log=log;
}
/**
<system>
Reference to Connector system
*/
private org.visualcti.server.connector.Manager connectors = null;
/**
<mutator>
To setting up the Server's logger
*/
public final void setConnectorsManager
        (
        org.visualcti.server.connector.Manager connectors
        )
{
    if (this.connectors == null) this.connectors=connectors;
}
/**
<system>
Reference to hardware manager
*/
private org.visualcti.server.hardware.Manager hardware = null;
/**
<mutator>
To setting up the Hardware manager
*/
public final void setHardwareManager
        (
        org.visualcti.server.hardware.Manager hardware
        )
{
    if (this.hardware == null) this.hardware=hardware;
}
/**
<system>
Reference to hardware manager
*/
private org.visualcti.server.task.Manager tasks = null;
/**
<mutator>
To setting up the tasks manager
*/
public final void setTaskManager
        (
        org.visualcti.server.task.Manager tasks
        )
{
    if (this.tasks == null) this.tasks=tasks;
}
/**
<system>
Reference to schedulers group
*/
private org.visualcti.server.SchedulerGroup schedulers;
    /**
    To process coniguration
    Will copy information from Element configuration
    to HashMap properties
    call inside configure(...) method
    */
    protected final void processConfiguration(Element xml)
    {
        Attribute date = xml.getAttribute( "date" );
        if (date != null) this.dateFormat = new SimpleDateFormat( date.getValue() );
        // Get the list of systems
        List systems = xml.getChildren("system");
        // To iterate system entries
        for(Iterator i = systems.iterator();i.hasNext();) {
            Element system = Config.getPrimaryEntry( (Element)i.next() );
            String systemName = system.getName();
            Tools.print("Resolving system :"+systemName);
            this.processSystem(system);
        }
        // to make and initialize schedullers
        this.makeSchedulers();
    }
    /**
    <producer>
    Using devices Manager and tasks Manager, make Schedulers
    */
    private final void makeSchedulers()
    {
        List list = this.hardware.devices();
        Tools.print("Make system :Schedulers");
        Tools.out.print("Progress ");Tools.out.flush();
        this.schedulers = new SchedulerGroup();
        List valid = new ArrayList();
        for(Iterator i=list.iterator();i.hasNext();)
        {
            Object device = i.next();
            if ( device instanceof generalDeviceProxy ){
              this.makeScheduler((generalDeviceProxy)device,valid);
            }
        }
        this.schedulers.setOwner(this);
        // to adjust the tasks pools
        TaskPool publicPool = this.tasks.publicTaskPool();
        this.tasks.removeChildren();
        this.tasks.addChild(publicPool);
        for(Iterator i=valid.iterator();i.hasNext();)
          this.tasks.addChild((TaskPool)i.next());
        Tools.print(" Done");
    }
    /**
    To make and register the Scheduler
    */
    private final String makeScheduler(generalDeviceProxy dev,List valid)
    {
        Tools.out.print("*");Tools.out.flush();
        // to get the device's name
        String name = dev.getDeviceName();
        String factory = dev.getFactory().getVendor();
        // to get the device's tasks pool
        TaskPool pool = this.tasks.getTaskPool( name, factory );
        valid.add( pool );
        Tools.out.print("*");Tools.out.flush();
        // to make the scheduler
        Scheduler instance = new Scheduler(name,dev,pool);
        // to register scheduler in SchedlureGroup
        this.schedulers.addChild( instance );
        Tools.out.print("*");Tools.out.flush();
        return name;
    }
/**
methods pool of Server.class
with "process" pefix and "XML" suffix
in method name
*/
private static Method[] action;
    /**
    to make the action array
    */
    static
    {
        Config.init();
        Vector processors = new Vector();
        Method[] entry = Server.class.getDeclaredMethods();
        for (int i=0;i < entry.length;i++) {
            String name = entry[i].getName();
            if (name.startsWith("process") && name.endsWith("XML")) {
                processors.addElement( entry[i] );
            }
        }
        Server.action = new Method[ processors.size() ];
        processors.copyInto(Server.action);
    }
    /** get processor for system */
    private final Method processor(String systemName)
    {
        String name = "process"+systemName+"XML";// name of method
        for (int i=0;i < this.action.length;i++) {
            if (Server.action[i].getName().equals(name)) return action[i];
        }
        return null;
    }
    /**
    to process xml element usign name for get the method
    */
    private final void processSystem(Element xml)
    {
        Method process = this.processor( xml.getName() );
        if (process == null) return;
        // to process the system configuration
        try {process.invoke(this,new Object[]{xml});
        }catch(Exception e){e.printStackTrace( Tools.err );}

    }
    /**
    To process system/Services XML entry
    */
    private void processServicesXML(Element xml){Services.make(xml, this);}
    /**
    To process system/Hardware XML entry
    */
    private void processHardwareXML(Element xml){Hardware.make(xml, this);}
    /**
    To process system/Tasks XML entry
    */
    private void processTasksXML(Element xml){Tasks.make(xml, this);}
    /**
<accessor>
To get Path to object instance
   */
   public final String getPath(){return "{Server}";}
   /**
<accessor>
To get Name of unit {Server}
   */
   public final String getName(){return "Server";}
    /**
<accessor>
To get local RMI registry for share objects
    */
    public final Registry localRegistry() {return this.registry;}
    /**
    <dispatcher>
    To dispatch event, error, or command response from unit
    The method will call inside of the unit.
    */
    public final void dispatch(unitAction event)
    {
        this.connectors.delivery( event );
        this.log.save( this.dateFormat, event );
    }
/** Format of Date formating */
private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    /**
    <accessor>
    Get format for Date to String conversion
    */
    public final DateFormat getDateFormat(){return this.dateFormat;}
    /**
    <executer>
    to execute server command
    */
    public final void execute(unitCommand command) throws Exception
    {
        this.log.save(this.dateFormat,command);
        try {super.execute(command); return;// try to parent's execute
        }catch(UnknownCommandException e){}
        switch( command.getID() ) {
            // to start server
            case unitCommand.START_ID:
                    this.services.Start();
                    this.hardware.Start();
                    this.tasks.Start();
                    this.schedulers.Start();
                    return;
            // to stop server
            case unitCommand.STOP_ID:
                    this.services.Stop();
                    this.schedulers.Stop();
                    this.hardware.Stop();
                    this.tasks.Stop();
                    // to stop JVM normal
                    System.exit(0);
                    return;
        }
        throw new UnknownCommandException();
    }
    /* to dispatch & execute the command */
    private final void dispath_and_execute(unitCommand command){
      String unitPath = command.getUnitPath();
      serverUnit unit = (serverUnit)UnitRegistry.lookup(unitPath);
      if ( unit == null) {
          // invalid unit path reaction
          unitAction answer=null;
          String message = "Invalid unit path ["+unitPath+"]";
          if ( command.isNeedResponse() ){
              answer = new unitResponse(command,message).bad();
          }else {
              answer = new unitError(this.connectors,null,message);
          }
          this.connectors.delivery(answer);
      } else {
          // to execute command
          try {
            this.log.save(this.dateFormat, command);
            unit.execute( command );
          }catch(Exception e){
            unitError error = new unitError(unit, e);
            this.dispatch( error );
            if ( command.isNeedResponse() )
            {
              unitResponse answer = new unitResponse(command,e.getMessage());
              this.connectors.delivery( answer.bad() );
            }
          }
      }
    }
    /**
    <action>
    to get & execute connector's command
    */
    private final void processCommand(){
        unitCommand command = this.connectors.command();
        if (command != null) new commandDispatcher(command).start();
    }
    /***
     * <thread>
     * thread for serve the command's dispatch & execution
     */
    private final class commandDispatcher extends Thread
    {
      private final unitCommand command;
      commandDispatcher(unitCommand command)
      {
        this.command=command;
        super.setName("To dispatch the Command "+command.sequenceID()+" from "+command.getLink());
        super.setPriority(Thread.NORM_PRIORITY);
      }
      public final void run(){dispath_and_execute(this.command);}
    }
    /**
    Class for get & execute commands
    */
    private final class commander extends Thread
    {
        public commander()
        {
            super("Server. commands proxy");
            this.setDaemon(true);
            this.setPriority(Thread.NORM_PRIORITY);
        }
        public final void run(){while(true) processCommand();}
    }
}
