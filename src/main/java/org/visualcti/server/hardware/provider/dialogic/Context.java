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
package org.visualcti.server.hardware.provider.dialogic;

import java.io.*;
import java.util.*;
import org.visualcti.media.*;
//import com.smile.sound.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Context of opened dialogic channel. To serve the sreams of channel</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
final class Context
{
//// STATIC section of the class
/**
 * <pool>
 * The array of contexts
 * array's index = channel's handle
 */
private static Context entry[] = new Context[0];
    /**
     * <accessor>
     * To get access to the context by handle
     * @param handle the handle to opened device
     * @return the context or null if invalid handle
     */
    static final Context get(int handle) {
      if (handle == Hardware.DX_ERROR) return null;
      try {
        return entry[ handle ];// the context by handle
      }catch(ArrayIndexOutOfBoundsException e){}
      return null;// invalid entry index
    }
    /**
     * <accessor>
     * To get access to the context by channel's name
     * @param name the channel's name
     * @return the context or null if invalid name
     */
    static final Context get(String name){
      if ( name == null ) return null;
      // to iterate Context's array
      for(int index=0; index < entry.length;index++) {
        Context context = entry[ index ];
        try{
          if ( context.handleName.equals(name) ) return context;
        }catch(NullPointerException e){}
      }
      return null;// not found
    }
    /**
     * <mutator>
     * To free the context by handle
     * Close entry and clear the reference to it
     * @param handle channel's handle
     */
    static final void free(int handle){
      try {entry[ handle ].close(); entry[ handle ] = null;
      }catch(ArrayIndexOutOfBoundsException e){}
    }
    /**
     * <producer>
     * To make the context for handle and owner
     * @param handle channel's handle
     * @param owner the owner of the handle
     * @param name the name of channel
     * @return the context
     * @throws Exception if invalid parameters
     */
    static final Context init(int handle,Object owner,String name) throws Exception {
      if (handle <= 0 || owner == null) throw new Exception("Invaid parameters!");
      if (Context.get( handle ) != null) throw new Exception("handle for Context in use!");
      // to check entries size
      Context.checkEntrySize( handle );
      // make new entry for handle and put it to entries array
      return entry[ handle ] = new Context( handle, owner , name);
    }
        /*
        To check the size of the contexts pool
        */

        private static void checkEntrySize(int handle)
        {
          if (handle < Context.entry.length) return;
          // increase the size of entries array
          synchronized( Context.class ){
              // There is no size of a array, will expand it
              int oldLength = Context.entry.length;
              Context[] newStore = new Context[ handle+1 ];
              System.arraycopy(Context.entry, 0, newStore, 0, oldLength);
              Context.entry = newStore;
          }
        }
///////////// NOT STATIC part of the class !!!!!!!!!
    /*
    Constructor
    */
    private Context(int handle, Object owner, String name)
    {
      this.handle = handle;
      this.owner = owner;
      this.handleName = name;
    }
/**
 * <attribute>
 * The handle to opened dialogic resource
 */
private final int handle;
    /**
     * <accessor>
     * To get access to handle of dialogic resource
     * @return the handle
     */
    public final int getHandle(){return this.handle;}

/**
 * <attribute>
 * The owner of opened handle
 */
private final Object owner;
    /**
     * <accessor>
     * To get access to handle's owner
     * @return the owner (used for wait/notify operations)
     */
    public final Object getOwner(){return this.owner;}
/**
 * <attribute>
 * The name of opened handle
 */
private final String handleName;
  /**
   * <accessor>
   * To get access to device's internal name
   * @return device's name
   */
  public final String getHandleName(){return this.handleName;}
/**
 * <flag>
 * Is the device is terminated
 */
private volatile boolean terminated = false;
  /**
   * <accessor>
   * To get access to terminated flag
   * @return the flag's value
   */
  public final boolean isTerminated() {return terminated;}
  /**
   * <mutator>
   * To change the value of termintaed flag
   * @param terminated ne value
   */
  public final void setTerminated(boolean terminated) {this.terminated = terminated;}
/**
 * <flag>
 * flag is port disconnected (call not served)
 * */
private volatile boolean disconnected = false;
    /**
     * <accessor>
     * To get access to disconnected flag
     * @return the flag's value
     */
    public final boolean isDisconnected(){return this.disconnected;}
    /**
     * <mutator>
     * To change the disconnected flag
     */
    public final void setDisconnected(){this.disconnected=true;}
    /**
     * <mutator>
     * To use hook's state for setup disconnected flag
     * @param hookState new hook's state
     */
    public final void setHookState(int hookState)
    {
        if ( isValidHookState(hookState) )
          this.disconnected = (hookState==Hardware.DX_ONHOOK);
    }
      private static final boolean isValidHookState(int state){
        return state==Hardware.DX_ONHOOK || state==Hardware.DX_OFFHOOK;
      }
/**
 * <attribute>
 * The last event for that context
 */
private volatile dialogicEvent lastEvent=null;
    /**
     * <accessor>
     * To get access to last event of the context
     * @return the event
     */
    public dialogicEvent getLastEvent(){return this.lastEvent;}
    /**
     * <mutator>
     * To setting up the last event for context
     * @param event last event
     */
    public void setLastEvent(dialogicEvent event){this.lastEvent = event;}
/**
 * <pool>
 * The pool of available codecs
 */
private final List codecs = Collections.synchronizedList( new ArrayList() );
  /**
   * <accessor>
   * To get access to the array of codecs
   * @return the array (not null)
   */
  final Audio[] getAvailabledCodecs()
  {
    return (Audio[])this.codecs.toArray( new Audio[0] );
  }
  /**
   * <mutator>
   * To update the codecs pool
   * @param codecs new codecs pool
   */
  final void setAvailableCodecs(List codecs)
  {
    this.codecs.clear();this.codecs.addAll(codecs);
  }
  public final String toString(){return "context for Dialogic channel "+handleName;}
  /**
   * <action>
   * To close the context
   */
  final void close()
  {
    synchronized(this.owner){this.owner.notify();}/* unblock wait() */
    this.setInputStream( null ); this.setOutputStream( null );
  }
/**
 * <attribute>
 * The InputStream for data (voice[play]/fax[send])
 * will use in C++ callback calls
 */
private InputStream input = null;
    /**
     * <mutator>
     * To store the InputStream with media-data
     * @param source the InputStream
     */
    final void setInputStream(InputStream source){this.input = source;}
    /**
     * <reader>
     * To read data-block from stored stream
     * @param data I/O buffer
     * @param off offset in buffer
     * @param len bytes quantity
     * @return really readed bytes
     * @throws IOException if invalid InputStream
     */
    final int read(byte[] data,int off,int len) throws java.io.IOException {
      try {
        return this.input.read(data, off, len);
      }catch (NullPointerException e){
          throw new java.io.IOException("not defined source stream");
      }
    }
/**
 * <attribute>
 * The OutputStream for store the media-data (voice[record]/fax[receive])
 * will use in C++ callback calls
 */
private OutputStream output = null;
    /**
     * <mutator>
     * To store the OutputStream for the media-data
     * @param target the OutputStream
     */
    final void setOutputStream(OutputStream target)
    {
      try {this.buffer.flush(this.output);} catch(java.io.IOException e){}
      this.output = target;
    }
/**
 * <attribute>
 * The buffer for realize a Seek feature
 */
private final Buffer buffer = new Buffer();
    /**
     * <writer>
     * To write the media-data to store OutputStream
     * @param data the buffer
     * @param off offset in buffer
     * @param len bytes quantity
     * @throws IOException if invalid OutputStream
     */
    final void write(byte[] data, int off, int len) throws java.io.IOException{
      if (this.output != null) {
        this.buffer.flush(this.output);
        this.buffer.write(data, off, len);
      } else  throw new java.io.IOException("no such target stream");
    }
    /**
     * <mutator>
     * To seek writed cti-data position for backward ("cut" DTMF)
     * @param position new position
     */
    final void seek(int position){
      if (position < 0) {
          int volume = this.buffer.getVolume();
          int newpos = volume+position; newpos = newpos < 0 ? 0:newpos;
          if (volume > 0) this.buffer.seek(newpos);
      }
    }
/**
 * <attribute>
 * The buffer for I/O operations
 */
private final byte[] IO_buffer = new byte[ Short.MAX_VALUE * 2 + 1  ];
    /**
     * <accessor>
     * To get access to I/O buffer
     * @return I/O buffer
     */
    final byte[] getBuffer(){return this.IO_buffer;}

/***
 * <buffer>
 * The class for realize buffered write & seek features
 */
private static class Buffer {
    /** byte array for low-level operations */
    private byte[] body = new byte[ Short.MAX_VALUE ];
    /** access to buffer body */
    public byte[] getBody(){return this.body;}
    /** Volume of bytes in buffer body */
    private int volume = 0;
    /** access to volume of buffer body */
    public int getVolume(){return this.volume;}
    /** to write data to the buffer body */
    public void write(byte[] data,int off, int len){
        int max = Short.MAX_VALUE-this.volume;
        len = Math.min(len, max);
        System.arraycopy(data, off, this.body, this.volume, len);
        this.volume += len;
    }
    /** to flush body to output stream */
    public void flush(OutputStream target) throws java.io.IOException
    {
      int length = this.volume; this.volume = 0;
      if (target == null || length <= 0) return;
      target.write(this.body, 0, length);/* can throw */
    }
    /** to seek pointer to position */
    public void seek(int pos) {this.volume = pos;}
}

/**
 * <attribute>
 * The content of user's input
 */
private final StringBuffer userInput = new StringBuffer();
    /**
    * To add user input for context
    */
    final void addDigits(String digits) {
      if ( digits == null) return;// invalid parameter
      synchronized( this.userInput ) {
          this.userInput.append(digits);// to increase internal buffer
      }
    }
    /**
    * To get user input buffer.
    * After this call internal buffer will be clear
    */

    public String getDigitsBuffer()
    {
        String result = "";
        synchronized ( this.userInput )
        {
            result = this.userInput.toString();// copy internal buffer for result
            // to clear the buffer
            this.userInput.setLength(0);
        }
        return result;
    }
}
