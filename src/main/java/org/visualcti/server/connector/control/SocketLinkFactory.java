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
package org.visualcti.server.connector.control;

import org.visualcti.util.SoftInputStream;
import org.visualcti.server.connector.*;
import org.visualcti.server.*;
import org.visualcti.server.security.Filter;
import org.jdom.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br></p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public class SocketLinkFactory extends LinkFactory
{
protected ServerSocket makeServerSocket(int port) throws IOException
{return new ServerSocket(port);}
/**
 * <attribute>
 * The port for listen
 */
private int port = 1777;
/**
 * <attribute>
 * The network socket
 */
private ServerSocket listener=null;
  /**
  <action>
  to Start links factory
  */
  public final void Start() throws IOException
  {
    if ( super.isStarted() ) return;// service already started
    this.listener = this.makeServerSocket(this.port);
    super.Start();
  }
  /**
  <action>
  to Stop links factory
  */
  public final void Stop() throws IOException
  {
      if ( super.isStopped() ) return;
      this.listener.close(); this.listener=null;
      super.Stop();
  }
  /**
   * <producer>
   * To make the gate keeper
   * @return the instance
   */
  protected final GateKeeper getGateKeeper()
  {
    if ( !super.isStarted() || this.listener == null ) return new gateKeeper( null );
    Socket socket = null;
    try {
      socket = this.listener.accept();
    }catch(IOException e){}
    // return the gate keeper
    return new gateKeeper( socket );
  }
  /**
   * <accessor>
   * To get access to LinkFactory's name
   * @return
   */
  public final String getName() {return "Socket";}
  /**
  <accessor>
  To get Type of unit
  */
  public final String getType(){return super.getType()+" socket links factory";}
/**
To process coniguration
Will copy information from Element configuration
to HashMap properties
*/
    protected final void processConfiguration(Element xml)
    {
      String port = xml.getAttributeValue("port");
      try{this.port = Integer.parseInt(port);}catch(Exception e){}
    }
    private final Filter getFilter(String login,String password){
      return super.getUserFilter(login,password);
    }
private static int counter = 1;
/***
 * <stream>
 * The soft input stream, for detect the socket's disconnect
 */
private final class inputStream extends SoftInputStream {
  String name = "Factory's SoftInputStream ";
  private inputStream(InputStream in){super(in);
    synchronized(SocketLinkFactory.class){this.name += counter; counter++;}
    super.start();
  }
  protected final String getThreadName(){return name;}
}
/**
 * <gatekeeper>
 * Realize the login & get the user's fliter
 */
private final class gateKeeper implements GateKeeper {
  private final Socket socket;
  private volatile boolean abort = false;
  /**<constructor> Constructor */
  private gateKeeper(Socket socket){this.socket=socket;}
  /**<attribute> user's request */
  private String login="",password="";
  /**<action> to do a login's sequence */
  public final Transport login() throws java.io.IOException
  {
    if ( this.socket == null ) throw new IOException("Invalid network socket");
    InputStream in = this.socket.getInputStream();
    OutputStream out = this.socket.getOutputStream();
    // to make the wrappers
    PrintStream print = new PrintStream(out,true);
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    print.println("Login:"); this.login = input.readLine();
    print.println("Password:"); this.password = input.readLine();
    // to get the user's filter
    Filter user = getFilter(this.login,this.password);
    if ( user == null )
    {
      this.socket.close();
      throw new IOException("Invalid user");
    }
    print.println("Ok");
    // to clear the memory
    print = null; input=null; user = null; System.gc();
    return new SocketTransport( in, out, this.socket );
  }
  /**<accessor> to get access to user's filter */
  public final Filter getUserFilter()
  {
    return getFilter(this.login,this.password);
  }
  /**<action> to abort a login's sequence*/
  public final void abortLogin()
  {
    try{this.socket.close();}catch(Exception e){}
    this.abort=true;
  }
}

}
