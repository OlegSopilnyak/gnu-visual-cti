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
package org.visualcti.workflow.server;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.visualcti.briquette.Program;
import org.visualcti.util.*;
import org.visualcti.server.*;
import org.visualcti.server.connector.control.*;
import org.visualcti.workflow.facade.ServerConnection;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * The connection to native (VisaulCTI) server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class nativeServerConnection implements ServerConnection
{
/**
 * <client>
 * The client for communicate with server
 */
private final Client client;
  /**
   * <constructor>
   * To make the connector
   */
  public nativeServerConnection()
  {
    client = ClientsFactory.makeClient();
    // to start the event's dispather
    new eventsDispatcher().start();
  }
/**
 * <attribute>
 * The listener of the disconnect
 */
private disconnectListener listener=null;
  /**
   * <mutator>
   * To add the disconnect's listener
   * @param listener the listener to add
   * @throws TooManyListenersException if can't add
   */
  public final synchronized void addDisconnecteListener
                                    (
                                    disconnectListener listener
                                    )
                                    throws TooManyListenersException
  {
    if ( this.listener != null ) {
      throw new TooManyListenersException("only one listener supported");
    }else this.listener = listener;
  }
  /**
   * <mutator>
   * To remove the disconnect's listener
   * @param listener listener to remove
   */
  public final synchronized void removeDisconnectListener
                                    (
                                    disconnectListener listener
                                    )
  {
    if ( this.listener != null && this.listener.equals(listener) ) this.listener=null;
  }
  /**
   * <accessor>
   * To get access to connection's state
   * @return true if connected
   */
  public final boolean isConnected()
  {
    return this.client.getLinkName() != null;
  }
  /**
   * <action>
   * To login to the server
   * @param server server's address
   * @param login server's login
   * @param password login's password
   * @return true if connected
   */
  public final boolean connect(String server, String login, char[] password)
  {
    try{
      boolean connected = this.client.login(server,login,password);
      if (connected) this.notifySemaphore();
      return connected;
    }catch(IOException e){
      return false;
    }
  }
  /**
   * <action>
   * To disconnect from server
   */
  public synchronized void disconnect()
  {
    this.client.close();
    if ( this.listener != null ) this.listener.disconnected();
  }
  /**
   * <accessor>
   * To get access to array of tasks groups names
   * @return the groups names
   */
  public final String[] getTaskGroups() throws IOException
  {
    if ( !this.isConnected() ) throw new IOException("not connected");
    this.groups.clear();
    // to get from connector all allowed units
    String link = this.client.getLinkName();
    unitCommand command = new unitCommand(link,unitAction.GET_ID,"allowed units");
    command.set(new Parameter("target", "units") ).setNeedResponse(true);
    // received groups list
    ArrayList groupsList = new ArrayList();
    // to sent command for execute & wait the answer
    this.executeCommand( command );
    // to solve the answer
    if ( command.isSuccessful() )
    {
      Parameter par = command.getParameter("units");
      StringTokenizer st = new StringTokenizer(par.getValue().toString(),";");
      String prefix = "/Tasks/";
      int prefixLength = prefix.length();
      while(st.hasMoreTokens())
      {
        String path = st.nextToken();
        if ( !path.startsWith(prefix) ) continue;
        String group = path.substring( prefixLength );
        if ( group.endsWith("public") ) group = "public";
        this.groups.put(group,path);
        groupsList.add(group);
      }
    }
    Collections.sort(groupsList);
    return (String[])groupsList.toArray(new String[]{""});
  }
/**
 * <pool>
 * The pool of tasksPools
 */
private final Map groups = new HashMap();
  /**
   * <accessor>
   * To get access to programms list for tasks pool
   * @param taskPool the name of tasks pool
   * @return the array of names
   * @throws IOException if not login
   */
  public final String[] getProgramms(String taskPool) throws IOException
  {
    if ( !this.isConnected() ) throw new IOException("not connected");
    taskPool = (String)this.groups.get(taskPool);
    // to make the command
    unitCommand command = new unitCommand(taskPool,unitAction.GET_ID,"info");
    command.set(new Parameter("target","info")).setNeedResponse(true);
    // to sent command for execute & wait the answer
    this.executeCommand( command );
    // received tasks list
    ArrayList tasksList = new ArrayList();
    // to solve the answer
    if ( command.isSuccessful() )
    {
      Parameter tasks = command.getParameter("tasks.list");
      if ( tasks != null )
      {
        StringTokenizer st = new StringTokenizer(tasks.getValue(""),"\n\r");
        while( st.hasMoreTokens() ) tasksList.add(st.nextToken());
      }
    }
    return (String[])tasksList.toArray(new String[]{""});
  }
  /**
   * <action>
   * To load the programm from the server
   * @param name programm's name
   * @param taskPool pool's name
   * @return the programm to edit
   * @throws IOException if not login
   */
  public Program load(String name, String taskPool) throws IOException {
    if ( !this.isConnected() ) throw new IOException("not connected");
    taskPool = (String)this.groups.get(taskPool);
    unitCommand command = new unitCommand(taskPool,unitAction.GET_ID,"get task to edit");
    command.set(new Parameter("task",name)).set(new Parameter("target","edit"));
    command.setNeedResponse( true );
    executeCommand( command );
    if ( command.isSuccessful() ) {
      Parameter par = command.getParameter("task");
      Element xml = par.getValue(Tools.emptyXML);
      if ( xml == Tools.emptyXML ) throw new IOException("Invalid task's XML");
      String progClass = Program.class.getName();
      if ( progClass.equals(xml.getAttributeValue("class")) )
      {
        File file = new File("programm.from.the.server.briquettes.task.xml");
        Program content = Program.newProrgamm();
        content.setFileName( file.getAbsolutePath() );
        try{content.setXML( xml );
        }catch(Exception e){
          throw new IOException("Can't restore from XML because "+e.getMessage());
        }
        return content;
      }
    }
    throw new IOException("Can't get the task "+name+" from the server");
  }
  /**
   * <action>
   * To deploy the programm to the server
   * @param taskPool the name of a pool
   * @param programm the programm to deploy
   * @throws IOException if not login
   */
  public void deploy(String taskPool, Program programm) throws IOException {
    if ( !this.isConnected() ) throw new IOException("not connected");
    taskPool = (String)this.groups.get(taskPool);
    unitCommand command = new unitCommand(taskPool,unitAction.SET_ID,"deploy");
    command
    .set( new Parameter("type","deploy") )
    .set( new Parameter("deploy",programm.getXML()) )
    .setNeedResponse( true );
    this.executeCommand( command );
  }
/**
 * <pool>
 * The pool of commands in progress
 */
private final Map commands = new HashMap();
    /**
     * <action>
     * To send the command & wait the result, if needs
     * @param command comand to execute
     */
    private final void executeCommand( unitCommand command ){
      try{
        // to sent the command to the server
        this.client.send(command);
      }catch(IOException e){
        this.disconnect();
        return;
      }
      if ( !command.isNeedResponse() ) return;
      // to make the key for commands pool
      Object key = new Integer( command.sequenceID() );
      if ( !command.isDone() ) {
        // to place the command to commands pool
        synchronized( this.commands ) {this.commands.put(key,command);}
        // to wait the response to the command
        try{synchronized(command){command.wait();}}catch(Exception e){}
      }
      // to remove the command from commands pool
      synchronized( this.commands ) {this.commands.remove(key);}
    }
    /**
     * <processor>
     * To solve the response
     * @param response the response to the command
     */
    protected final void processResponse(unitResponse response){
      // to make the key
      Object key = new Integer(response.getCorrelationID());
      // try to get the command by key
      unitCommand command = null;
      synchronized(this.commands){command = (unitCommand)this.commands.get(key);}
      // debug print
      if ( !response.isCommandSuccess() ) System.err.println(response);
      // to store the response to the command
      if ( command != null ) command.setResponse(response);
    }
    /**
     * <processor>
     * To solve the server's event
     * @param event event to solve
     */
    private final void processEvent(unitAction event){
      if ( event instanceof unitResponse ) {
        this.processResponse((unitResponse)event);
      }else {
        System.out.println("Server say\n"+event);
      }
    }
    /* to receive the event from the connection */
    private final unitAction receiveEvent(){
      if ( this.client == null ) return null;
      try{
        if ( this.client.getLinkName() == null ) return null;
        return this.client.receive();
      }catch(Exception e){
        this.disconnect();
        return null;
      }
    }
    private final void waitSemaphore(){
      try{
        synchronized( this.eventsSemaphore ){ this.eventsSemaphore.wait();}
      }catch(InterruptedException e){}
    }
    private final void notifySemaphore(){
      synchronized( this.eventsSemaphore ){ this.eventsSemaphore.notify();}
    }
/**
 * <semaphore>
 * The semaphore of the events receiving
 */
private final Object eventsSemaphore = new Object();
/***
 * <thread>
 * Class to dispath the events to GUIs
 */
private final class eventsDispatcher extends Thread{
  eventsDispatcher(){super("To dispatch the server's events");}
  public final void run(){
    while( true ) {
      unitAction event = receiveEvent();
      if ( event == null ) waitSemaphore(); else processEvent(event);
    }
  }
}
}
