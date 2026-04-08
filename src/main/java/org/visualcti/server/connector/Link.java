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
package org.visualcti.server.connector;

import java.io.*;
import java.net.*;

import org.visualcti.server.*;
import org.visualcti.server.security.*;
import org.visualcti.util.*;

/**
   Link to external coonection to server
 */
public final class Link
{
    /**
    <constructor>
    with factory and transport
    other properties may be setup,
    using mutators
    */
    public Link
            (
            LinkFactory factory,
            Transport transport
            )
    {
        this.closed = true;
        this.factory = factory;
        this.transport = transport;
        this.transport.setLink( this );
    }
    /**
    <action>
    To activate the link
    */
    public final void activate()
    {
        if ( this.transport.ready() )
        {
            new receiver().start();
            new sender().start();
            this.closed = false;
        }else {
            Tools.error("Transport not ready");
        }
    }
/**
<attribute>
Transport of this link
*/
private transient volatile Transport transport;
    /**
       <accessor>
       Check is this Link is alive
       @roseuid 3BC2AFC002CE
     */
    public final boolean isAlive(){return this.transport.ready();}

/**
<attribute>
The name of link
*/
private transient String name = "Link";
    /**
       <accessor>
       get Link name
       @roseuid 3BC2AFC002D8
     */
    public final String getName(){return this.name;}
    /**
        <mutator>
        setting up Link name
    */
    public final Link setName(String name){this.name=name;return this;}

/**
<attribute>
factory - woner of this Link
*/
private transient LinkFactory factory;
    /**
       <accessor>
       get Link's factory
       @roseuid 3BC2AFC002D9
     */
    public final LinkFactory getLinkFactory(){return this.factory;}
/**
<attribute>
received commands
*/
private final Queue commands = new Queue();

    /**
       <accessor>
       To get unitCommand from external connection
       if no command in link, the call will return null
       @roseuid 3BC2AFC002DA
     */
    public final unitCommand getCommand()
    {
      if ( !this.isAlive() ) return null;
      unitCommand command = this.commands.empty() ? null:(unitCommand)this.commands.pop();
      if (command != null)
      {
        command.setLink( this.getName() );
        if ( !command.isNeedResponse() ) command.setSuccessful(true);
      }
      return command;
    }

/**
<attribute>
events to send
*/
private final Queue events = new Queue();
    /**
       <transfer>
       To dispatch event from server
       to connected user
       @roseuid 3BC2AFC002E2
     */
    public final synchronized void dispatch(unitAction event)
    {
      // validate & check permissions using filter & transport
//System.out.println("Link ["+this.getName()+"] try to dispath\n"+event);
      boolean valid = this.isValid(event);
      boolean allowed = this.isAllowed(event);
//System.out.println("\tevent is: valid "+valid+"\n\t allowed "+allowed);
//      if ( isValid(event) && isAllowed(event) ) this.events.push(event);
      if ( valid && allowed ) {
//System.out.println("Event pushed to transport.");
        this.events.push(event);
      }
    }
    /**
<validator>
is unitAction valid for this Link
    */
    private final boolean isValid(unitAction action){
        return action != null && this.filter != null && isAlive();
    }
    /**
<validator>
is unitAction allowed for this Link
    */
	private final boolean isAllowed(unitAction action){
	    return this.filter.isAllowed(action);
	}
	/**
	   <control>
	   Will is call when the Transport has received the command
	   @roseuid 3BC2AFC002E4
	 */
	private final void commandReceived(unitCommand command){
          if ( this.isValid(command)){// command and link valid
              if ( command.getUnitPath().equals(this.name) ){
                // command to this link
                this.executeLinkCommand(command);return;
              }
              if ( this.isAllowed(command) ){
                // command to server's unit
                this.commands.push(command);return;
              }
          }
          // not a valid command
          if ( command.isNeedResponse() ) {// need response
              unitResponse response =
                new unitResponse(command,"You have no permission for this");
              this.events.push( response.bad() );// place it back to transport
          }else {// simple error return
              unitError error =
                new unitError
                      (
                      "user's permissions",
                      null,
                      "You have no permission for this"
                      );
              this.events.push( error );// place it back to transport
          }
	}
	/**
	<execute>
	To execute link's command
	(get allowed units list)
	*/
	private final void executeLinkCommand(unitCommand command) {
          if (command.getID() == unitCommand.GET_ID) {
            // to check the parameter, named "target"
            Parameter target = command.getParameter("target");
            if ( target == null){
                unitResponse answer = new unitResponse(command, "Invalid target");
                this.events.push( answer.bad() );// place it to transport
            }else
            if ( "units".equals(target.getValue())) {
              // to get an allowed units set
              String units[] = UnitRegistry.list();
              StringBuffer data = new StringBuffer();
              // to collect allowed unit's paths
              for(int i=0;i < units.length;i++){
                  if ( this.filter.isAllowed(units[i]) ) data.append(units[i]).append(";");
              }
              // make response
              unitResponse answer = new unitResponse(command).well();
              // to store the list as parameter
              answer.set( new Parameter("units",data.toString()).output() );
              this.events.push( answer );// place it to transport to client
            }else
            if ( "class".equals(target.getValue()) ){
              unitResponse answer = new unitResponse(command,"The result").well();
              Parameter par = command.getParameter("resource.name");
              if ( par == null || par.getValue() == null) {
              }
              ClassLoader loader = Link.class.getClassLoader();
              URL url = loader.getResource(par.getValue("nothing.com"));
              if ( url == null ){
                this.events.push( answer.bad() );// place it to transport to client
                return;
              }
              try{
                InputStream in = url.openStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte buffer[] = new byte[4096]; int count=-1;
                while( (count = in.read(buffer)) != -1) out.write(buffer,0,count);
                in.close();out.close();
                answer.set(new Parameter("resource.body",out.toByteArray()));
              }catch(IOException e){
                this.events.push( answer.bad() );// place it to transport to client
              }
              this.events.push( answer );// place it to transport to client
            }else
            {
                unitResponse answer = new unitResponse(command, "Invalid target");
                this.events.push( answer.bad() );// place it to transport
            }
          }else {// command not "get"
            unitAction error; String message = "Invalid command's ID";
            if (command.isNeedResponse()){
                error = new unitResponse(command, message).bad();
            }else {
                error = new unitError("Your Link", null,  message);
            }
            this.events.push( error );
          }
	}
	/**
	   <control>
	   Will called when the Transport has sent the event
	   @roseuid 3BC2AFC002EC
	 */
	private final void eventDispatched(unitAction event)
	{   // to notify waiting thread (if need)
          synchronized(event){event.notify();}
	}

/**
<attribute>
filter for server's flows
*/
private Filter filter = null;
	/**
	   <accessor>
	   Get the filter for this connection
	   @roseuid 3BC2AFC002EE
	 */
	public final Filter getFilter(){return this.filter;}
	/**
	   <mutator>
	   To Set the filter for this connection
	 */
	public final Link setFilter(Filter filter){this.filter = filter;return this;}
/**
<flag>
is this Link is closed
*/
private volatile boolean closed = true;
        public final boolean isClosed(){return this.closed;}
	/**
	   <action>
	   To close the Link
	   @roseuid 3BC2AFC002EF
	 */
	public final void close()
	{
          if ( this.closed ) return;
          synchronized( this )
          {
            if ( this.closed ) return;
            this.transport.close();
            this.transport.setLink( null );
            // check events queue, free it, if need
            if ( this.events.isWait() ) this.events.push(null);
            // clear reference to link in LinkFactory
            this.factory.linkClosed( this );
            this.closed=true;
          }
	}
/**
<inner-class>
Thread for receive the commands from
connected client via link's transport
*/
private final class receiver extends Thread{
    private receiver() {
        super(// parent's threads group
              getLinkFactory().getThreadGroup(),
              Link.this.getName()+" commands receiver"
              );
        super.setPriority(Thread.MIN_PRIORITY);
    }
    /** main loop of receiver */
    public final void run()
    {
        while ( Link.this.isAlive() )
        {
            try{// to receive & process command
              if ( !transport.ready() ) break;// transport closed
              // to receive the command (blocked call)
              // this call can throw the Exception
              unitCommand command = transport.receive();
              // to process received command
              commandReceived( command );
            }catch(IOException e){
                break;// transport's exception
            }
        }
        Link.this.close();// to close the link
    }
}
/**
<inner-class>
Thread for sent the events to
connected client via link's transport
*/
private final class sender extends Thread {
  private sender(){
    super(// parent's threads group
          getLinkFactory().getThreadGroup(),
          Link.this.getName()+" events sender"
          );
    super.setPriority(Thread.MIN_PRIORITY);
  }
  /** main loop of sender */
  public final void run()
  {
    while ( Link.this.isAlive() )
    {
      try{// get event from events's queue
        unitAction event = (unitAction)events.pop();// blocked operation
        if (event == null)  break;// link closed
        // to sent the event to client via transport
        transport.send( event );
        // To notify, the event dispatched to client
        eventDispatched( event );
      }catch(IOException e){
        break;// transport's exception
      }
    }
    Link.this.close();// to close the link
  }
}
}
