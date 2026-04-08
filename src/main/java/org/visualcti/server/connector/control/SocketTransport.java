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

import org.visualcti.server.connector.*;
import org.visualcti.server.*;
import org.jdom.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The transport for XML's sockets</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public final class SocketTransport implements Transport
{
/**
 * <attribute>
 * The input stream to client
 */
private DataInputStream input;
/**
 * <attribute>
 * The outpur stream to client
 */
private DataOutputStream output;
/**
 * <attribute>
 * The network's socket to the client
 */
private Socket socket;

/**
 * <flag>
 * Is transport is ready
 */
private volatile boolean isReady;
  /**
   * <constructor>
   * Constructor
   * @param in socket's input stream
   * @param out socket's output stream
   * @param socket network socket
   * @throws IOException if some wrong
   */
  public SocketTransport(InputStream in,OutputStream out,Socket socket)
      throws IOException
  {
    this.isReady = (this.socket = socket) != null;
    if ( this.isReady ) {
      this.output= new DataOutputStream(out);
      this.input = new DataInputStream(in);
    }else{
      this.input = null;
      this.output= null;
    }
  }
/**
 * <attribute>
 * The owner of the transport
 */
private Link owner=null;
  /**
   * <accessor>
   * To get access to transport's owner
   * @return the owner
   */
  public final Link getLink() {return this.owner;}
  /**
   * <mutator>
   * To change the owner of the transport
   * @param link the owner
   */
  public final void setLink(Link link)
  {
    if ( (this.owner=link) == null || owner.isClosed() ) return;
    // to notify the client
    unitEvent event = new unitEvent(link.getLinkFactory(),unitAction.SET_ID,link.getName());
    try {
      this.send( event );
    }catch(IOException e){
      this.isReady = false;
    }
  }
  /**
   * <accessor>
   * To check is transport is ready
   * @return true if transport is ready
   */
  public final boolean ready() {return this.isReady;}
  /**
   * <action>
   * To close the transport:
   * close the socket and clear the ready's flag
   */
  public final void close()
  {
    if ( this.isReady )
    { // to close the socket
      try{this.socket.close();}catch(IOException e){}
      this.input = null; this.output=null;
      System.gc();
      // to clear the ready's flag
      this.isReady = false;
    }
  }
  /**
   * <action>
   * To send the event to the client
   * @param event evetn to send
   * @throws IOException if some wrong
   */
  public final void send(unitAction event) throws IOException
  { // to check the ready's flag
    if ( !this.isReady ) throw new IOException("Transport not ready");
    // to send the event
    try {
      // convert to XML and send it to the client
      synchronized ( this.socket )
      {
        // to transform the event to XML & send it to the buffer
        ByteArrayOutputStream oBuffer = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(oBuffer);
        event.store( zip ); zip.finish(); zip.close();
        byte[] data = oBuffer.toByteArray();
        this.output.writeInt( data.length );
        this.output.write( data );
        this.output.flush();
        // to free the memory
        data=null; zip=null; oBuffer=null;
      }
    }catch(IOException exception){
      this.isReady = false;
      throw exception;
    }
  }
  /**
   * <action>
   * To receive the command from the client
   * @return client's command
   * @throws IOException if some wrong
   */
  public final unitCommand receive() throws IOException
  { // to check the ready's flag
    if ( !this.isReady ) throw new IOException("Transport not ready");
    // to receive the COMMAND :-) (not error,or event, or responce)
    try {
      while ( true )
      {
        int length = this.input.readInt(), count = -1;
        byte[] data = new byte[ length ];

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while( (count=this.input.read(data,0,length)) != -1) {
          buffer.write(data,0,count);
          if ( count == length ) break; else length -= count;
        }
        ByteArrayInputStream iBuffer = new ByteArrayInputStream(buffer.toByteArray());
        GZIPInputStream zip = new GZIPInputStream(iBuffer);
        // to restore the action from XML
        unitAction command = unitAction.Restore(zip);zip.close();
        // to free the memory
        buffer=null; data=null; zip=null; iBuffer=null;
        // if action is command, returns it
        if ( command instanceof unitCommand)
        {
          unitCommand request = (unitCommand)command;
          // to store the Link's name to the command
          // for solve the request
          request.setLink( this.getLink().getName() );
          return request;
        }
      }
    }catch(IOException exception){
      this.isReady = false;
      throw exception;
    }catch(OutOfMemoryError e){
      this.close();// to close the transport immediate
      throw new IOException("Fucken hacker >:-(");
    }
  }
}
