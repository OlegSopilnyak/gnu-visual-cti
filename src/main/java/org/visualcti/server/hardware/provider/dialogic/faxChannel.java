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
package org.visualcti.server.hardware.provider.dialogic;

import java.io.*;

import org.visualcti.media.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.proxy.*;
import org.visualcti.server.hardware.proxy.part.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Fax's part of the device</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

abstract class faxChannel extends voxChannel implements faxMachine
{
/**
 * <attribute>
 * The fax-device's handle
 */
private int handle = Hardware.DX_ERROR;
    private final boolean valid(){return this.handle != Hardware.DX_ERROR;}
  /**
   * <constructor>
   * To make fax part of device
   * @param name the name of device
   * @param owner the factory of the device
   */
  faxChannel(String name, Factory owner)
  {
    super(name,owner);
  }
    /* called from Garbage Collector before free the memory */
    protected void finalize() throws Throwable {
      if ( this.handle != Hardware.DX_ERROR ) {
        Hardware.closeChannel(this.handle);
        this.handle = Hardware.DX_ERROR;
      }
      super.finalize();
    }
  /**
   * <action>
   * To open and activate the device.
   * If the device can't open, device may throw the exception
   * @throws IOException if some wrong
   * @see generalDevice
   */
  public void open() throws java.io.IOException{
    this.handle = Hardware.openFaxChannel(super.getName(),this);
    super.open();// to open vox
  }
  /**
   * <action>
   *  To close the device, if there are no active operations and
   *  the expectation of end of the current operation, if still execute
   * @throws IOException if the device can't close
   * @see generalDevice
   */
  public void close() throws java.io.IOException{
    super.close();// to close the vox's part
  }
  /**
   * <notify>
   * Notify, the device have the new state
   * @param state new state's value
   */
  protected void stateChanged(String state){super.stateChanged(state);
    Context context = Context.get( this.getHandle() );
    if ( context != null && this.isActive() ) {
      context.setTerminated(false);
    }
  }
  /**
  <action>
   The unconditional termination anyone current CTI of operation:
          1. operations with calls (waiting, make call, connect)
          2. exchanges of the data (voice or fax)
   * @throws IOException If the device can't terminate current operation
   */
  public void terminate() throws java.io.IOException
  {
    if ( this.isActive() )
    {
      // to terminate the current active operation
      Hardware.terminate(this.handle);
      // to wait a termination's completion
      while( this.isActive() ) { Thread.yield();
        try{Thread.sleep(100);}catch(Exception e){}
      }
      super.newState(deviceProxy.DS_STOPD);
    }else
      super.terminate();// to terminate the VOX's part of device
  }
    /* to check, is device's part is active */
    private final boolean isActive() {
      if ( !valid() ) return false;
      String state = super.getStatus();
      return
        deviceProxy.DS_RECVFAX.equals(state) ||
        deviceProxy.DS_SENDFAX.equals(state);
    }
  /**
   * <accessor>
   * To get access to quantity of transfered pages
   * @return the value
   */
  public int getTransferredPages()
  {
    return !this.valid() ? 0:FAX.getTransferredPages(this.handle);
  }
  /**
   * <accessor>
   * To get access to localID of remote fax-machine
   * @return the value
   */
  public String getRemoteID()
  {
    return !this.valid() ? "":"";
  }
  /**
   * <mutator>
   * to setup the Document's header
   * @param header the value
   */
  public void setFaxHeader(String header)
  {
    if ( this.valid() ) FAX.setHeaderInfo(this.handle,header);
  }
  /**
   * <mutator>
   * to setup the localID of device
   * @param localID the value
   */
  public void setFaxLocalID(String localID)
  {
    if ( this.valid() ) FAX.setLocalID(this.handle,localID);
  }
  /* to receive the fax-document */
  /**
   * <action>
   * to receive the fax-document
   * @param target the OuputStream to target
   * @param pollingMode flag
   * @param issueVoiceRequest flag
   * @return termination's reason
   */
  public String receive(OutputStream target, boolean pollingMode, boolean issueVoiceRequest)
  {
    if (!this.valid() ) return Reason.ERROR;
    // to start fax's receive
    super.newState(deviceProxy.DS_RECVFAX);
    File file = new File(super.getName()+".FAX");
    String reason = FAX.recieve(this.handle,file.getAbsolutePath(),issueVoiceRequest);
    // to copy data from file to target
    if ( file.exists() )
    {
      try{
        byte[] buffer = new byte[1024];int count = 0;
        FileInputStream in = new FileInputStream(file);
        while ( (count=in.read(buffer)) > 0 ) target.write(buffer,0,count);
        target.flush(); in.close();
      }catch(IOException e){
      }finally{
        file.delete();
      }
    }
    // operation complete
    super.newState(deviceProxy.DS_IDLE);
    return reason;
  }
  /**
   * <action>
   * to transmit the fax-document
   * @param source the InputStream to source
   * @param format document's format
   * @param issueVoiceRequest flag
   * @return termination's reason
   */
  public String transmit(InputStream source, Fax format, boolean issueVoiceRequest)
  {
    if (!this.valid() ) return Reason.ERROR;
    // to start fax's transmit
    super.newState(deviceProxy.DS_SENDFAX);
    String reason = Reason.IO.FORMAT;
    File file = new File(super.getName()+".FAX");
    try {
        byte[] buffer = new byte[1024];int count = 0;
        FileOutputStream out = new FileOutputStream(file);
        while ( (count=source.read(buffer)) > 0 ) out.write(buffer,0,count);
        out.flush(); out.close();
        reason=FAX.send(
                      this.handle,
                      format.isTIFF(),
                      file.getAbsolutePath(),
                      format.isHighResolution(),
                      issueVoiceRequest,
                      0,
                      -1
                      );
      }catch(IOException e){
      }finally{
        file.delete();
      }
    // operation complete
    super.newState(deviceProxy.DS_IDLE);
    return reason;
  }
  /**
   * <action>
   * to transmit the fax-document
   * @param doc document to sent
   * @param issueVoiceRequest flag
   * @return termination's reason
   */
  public String transmit(org.visualcti.media.Document doc, boolean issueVoiceRequest)
  {
    try{
      return this.transmit(doc.getInputStream(),doc.getFormat(),issueVoiceRequest);
    }catch(IOException e){
      super.dispatch( new org.visualcti.server.unitError(this,e) );
    }
    return Reason.IO.FORMAT;
  }
}
