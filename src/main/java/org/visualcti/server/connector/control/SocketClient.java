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
package org.visualcti.server.connector.control;

import org.jdom.*;
import org.visualcti.server.*;
import org.visualcti.util.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * PlainSocket client for the VisualCTI server's control</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public class SocketClient implements Client
{
/**
 * <attribute>
 * The port for make the connection
 */
private int port = 1777;
  /**
   * <mutator>
   * To adjust the client
   * @param xml the configuration
   */
  public void configure(Element xml)
  {
    String portNumber = xml.getAttributeValue("port");
    try{this.port = Integer.parseInt(portNumber);
    }catch(Exception e){}
  }
/**
 * <attribute>
 * The socket to the VisualCTI server
 */
private volatile Socket socket=null;
  /**
   * <action>
   * To login to VisualCTI server
   * @param server the server
   * @param login login
   * @param password password
   * @return true if login is correct
   * @throws IOException if some wrong in the network
   */
  public boolean login(String server, String login, char[] Password) throws IOException
  {
    // try to make the connection
    this.socket = new Socket( server, this.port );

    // connection established make the steams
    InputStream in = socket.getInputStream();
    OutputStream out= socket.getOutputStream();
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    PrintWriter user = new PrintWriter(out,true);

    // The negotiantion with server
    String password = new String(Password),

    // to read a login's prompt
    line = input.readLine();
    if ( line == null ) throw new IOException("Invalid protocol");
    line = line.toLowerCase();

    // to send the user's login
    if ( line.startsWith("login") ) user.println(login);

    // to read a password's prompt
    line = input.readLine();
    if ( line == null ) throw new IOException("Invalid protocol");
    line = line.toLowerCase();
    if ( line.startsWith("password") ) user.println(password);
    try{
      // try to read "Ok" line
      line=input.readLine();
      if ( line == null )
      { // disconnected
        try{this.socket.close();}catch(Exception e){}
        // to free the memory
        in=null; out=null; input=null; user=null;
        this.socket = null; return false;
      }
    }catch(IOException e){
      try{this.socket.close();}catch(Exception ex){}
      // to free the memory
      in=null; out=null; input=null; user=null;
      this.socket = null; return false;
    }

    // to make the connection's streams
    this.server = new DataOutputStream(out);
    this.client = new DataInputStream( in );

    // to start the threads
    new receiver().start();
    new sender().start();

    // to receive the event with Link's name
    unitAction event = this.receive();
    this.linkName = event.getDescription();

    // to free the memory
    in=null; out=null; input=null; user=null; event=null;
    return true;
  }
/**
 * <attribute>
 * The input stream, receive the server's events
 */
//private GZIPInputStream client=null;
private DataInputStream client=null;
/**
 * <attribute>
 * The output stream to sent the server's commands
 */
//private GZIPOutputStream server=null;
private DataOutputStream server=null;
/**
 * <attribute>
 * The name of the server's link for this client
 */
private String linkName = null;
  /**
   * <accessor>
   * To get access to link's name
   * @return the link's name or null, if not login on
   */
  public final String getLinkName() {return this.linkName;}
/***
 * <thread>
 * Thread for receive the packets & place it to events queue
 */
private final class receiver extends Thread{
  receiver(){
    super("Server's events receiver");super.setPriority(Thread.MAX_PRIORITY);
  }
  public final void run(){while(socket!=null) socketReceive();}
}
      /**
       * <receiver>
       * To receive the event from server's transport
       */
      private final void socketReceive(){
        if ( this.socket == null ) return;
        try{
          int length = this.client.readInt(), count = -1;
          byte[] data = new byte[ length ];

          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          while( (count=this.client.read(data,0,length)) != -1) {
            buffer.write(data,0,count);
            if ( count == length ) break; else length -= count;
          }
          ByteArrayInputStream iBuffer = new ByteArrayInputStream(buffer.toByteArray());
          GZIPInputStream zip = new GZIPInputStream(iBuffer);

          //unitAction event = unitAction.Restore(iBuffer);

          unitAction event = unitAction.Restore(zip); zip.close();
          // to free the memory
          buffer=null; data=null; zip=null; iBuffer=null;
          // to store received event to the queue of events
          this.events.push( event );
        }catch(IOException e){
          //e.printStackTrace();
          this.close();
        }catch(OutOfMemoryError e){// invalid exchange's protocol
          //e.printStackTrace();
          this.close();// to close client immediate
        }
      }
/**
 * <queue>
 * The queue of received server's events
 */
private final Queue events = new Queue();
  /**
   * <action>
   * To receive the event from the server
   * @return the server's event
   * @throws IOException if some wrong in the network
   */
  public unitAction receive() throws IOException
  {
    if ( this.socket == null ) throw new IOException("the client not log in");
    unitAction event = (unitAction)this.events.pop();
    if ( event == null ) throw new IOException("Connection to link is lost");
    return event;
  }
/**
 * <queue>
 * The queue of commands to server
 */
private final Queue commands = new Queue();
/***
 * <thread>
 * Thread for get command from commands queue & sent it to server's transport
 */
private final class sender extends Thread{
  sender(){super("Server's commands sender");}
  public final void run(){
    while(socket!=null) {
      unitCommand command = (unitCommand)commands.pop();
      if ( command != null ) socketSend(command); else break;
    }
  }
}
  /**
   * <sender>
   * To sent the command to Server's transport
   * @param command
   */
  private final void socketSend(unitCommand command){
    if ( this.socket == null ) return;
    command.setLink(this.linkName);
    try{
      ByteArrayOutputStream oBuffer = new ByteArrayOutputStream();
      GZIPOutputStream zip = new GZIPOutputStream(oBuffer);
      command.store(zip); zip.finish(); zip.close();
      byte[] data = oBuffer.toByteArray();
      this.server.writeInt(data.length);
      this.server.write(data);
      this.server.flush();
      // to free the memory
      data=null; zip=null; oBuffer=null;
    }catch(IOException e){
      e.printStackTrace();
      this.close();
    }
  }
  /**
   * <action>
   * To transmit the command to the server
   * @param command server's command
   * @throws IOException if some wrong in the network
   */
  public void send(unitCommand command) throws IOException
  {
    if ( this.socket == null ) throw new IOException("the client not log in");
    if (command != null) this.commands.push(command);
  }
  /**
   * <action>
   * To close the client
   */
  public synchronized void close()
  {
    if ( this.socket == null ) return;
    try{this.socket.close();}catch(Exception e){}
    this.socket = null;
    this.events.push(null);
    this.commands.push(null);
    this.server = null;
    this.client = null;
    this.linkName = null;
    System.gc();
  }
}
