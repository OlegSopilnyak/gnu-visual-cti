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
package org.visualcti.server.hardware.provider;

import java.io.IOException;
import java.util.*;

import org.jdom.*;
import org.visualcti.server.*;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server.
 * The Class-stub, for realizing deviceFactory features</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public class stubDeviceFactory extends groupUnitAdapter implements deviceFactory
{
/**
 * <state>
 * The state of Engine
 */
private Engine.State state = Engine.State.OUT_OF_SERVICE;
   /**
    * <control>
    * To start the factory
    * @throws IOException if some wrong
    */
   public void Start() throws java.io.IOException
   {
    if ( this.isStarted() ) return;
    // to open all devices
    this.openAll();
    // to change the Factory
    this.state = Engine.State.IN_SERVICE;
    // dispatch success start event
    this.dispatch( new unitEvent(this,unitAction.START_ID) );
   }
  /**
   * <action>
   * to start (open) all telephony devices
   * @throws IOException if any device can't open
   */
  private final void openAll() throws java.io.IOException {
    synchronized( this.devices ){// to iterate the devices list
      for(Iterator i=this.devices.iterator();i.hasNext();) {
        generalDeviceProxy device = (generalDeviceProxy)i.next();
        if ( !device.isOpened() ) device.open();// try to open the device
      }
    }
  }
  /**
   * <accessor>
   * To check is factory is started
   * @return true if has started
   */
  public boolean isStarted(){return this.state == Engine.State.IN_SERVICE;}
  /**
   * <control>
   * To stop the factory & all devices (close it)
   * @throws IOException if some wrong during close
   */
  public void Stop() throws java.io.IOException
  {
      if ( this.isStopped() ) return;
      // to close all devices
      this.closeAll();
      // to change Engine's state
      this.state = Engine.State.OUT_OF_SERVICE;
      // dispatch success stop event
      this.dispatch( new unitEvent(this,unitAction.STOP_ID) );
  }
  /**
   * <action>
   * to stop (close) all devices
   * @throws IOException if any device is can't close
   */
  private final void closeAll() throws java.io.IOException{
    synchronized( this.devices ){// to ietrate the devices list
      for(Iterator i=this.devices.iterator();i.hasNext();){
        generalDeviceProxy device = (generalDeviceProxy)i.next();
        if (device.isOpened()) device.close();
      }
    }
  }
  /**
   * <accessor>
   * To check is factory is stopped
   * @return true if has stopped
   */
  public boolean isStopped(){return this.state == Engine.State.OUT_OF_SERVICE;}
  /**
   * <accessor>
   * To get access to state ID
   * @return the state's ID
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
   * <restore>
   * To restore factory's configuration
   * @param xml the factory's configuration
   */
  protected void processConfiguration(org.jdom.Element xml){
      String prefix = "Stub_"; int count = 2;
      for(Iterator i = xml.getChildren("parameter").iterator();i.hasNext();){
          try {
              Element p = (Element)i.next();
              Parameter param = Parameter.restore(p);
              String parName = param.getName();
              if ("device prefix".equals(parName)) {
                  prefix = param.getStringValue();
              } else
              if ("device count".equals(parName))  {
                  count = param.getIntValue();
              }
          }catch(Exception e){
              e.printStackTrace();
          }
      }
      // to make fake devices
      for(int i=1;i <= count;i++) this.appendDevice(new stubDevice(prefix+i, this));
  }
  /**
   * <accessor>
   * To get access to Unit's name
   * @return the name
   */
  public final String getName(){return "provider/"+this.getVendor();}
  /**
   * <accessor>
   * To get access to Unit's type
   * @return the type
   */
  public final String getType(){return "CT-board";}
  /**
   * <accessor>
   * To get access to Unit's icon
   * @return the icon's body
   */
  public byte[] getIcon(){return null;}
  /**
   * <accessor>
   * To get access to Unit's state
   * @return the state
   */
  public final String getUnitState(){return this.isStarted() ? "in service":"out service";}

   // device factory
/**
 * <attribute>
 * The map of devices and device's names
 */
private final HashMap devicesMap = new HashMap(100);
/**
 * <attribute>
 * The pool of factory's devices
 * May adjusted in processConfiguration(org.jdom.Element xml)
 */
private final ArrayList devices = new ArrayList(100);
  /**
   * <mutator>
   * To clear all pools
   */
  protected final void init(){
    this.devices.clear();this.devicesMap.clear();
  }
  /**
   * <accessor>
   * To get access to Factory's vendor name
   * @return the vendor's name
   */
  public String getVendor(){return "VisualCTI";}
  /**
   * <accessor>
   * To get access to Factory's version
   * @return the version
   */
  public String getVersion(){return "3.0 alfa";}
  /**
   * <transform>
   * To represent the factory as string
   * @return the string
   */
  public final String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" Hardware from ").append(this.getVendor());
    sb.append(", version ").append(this.getVersion());
    sb.append(". Contains ").append(devices.size()).append(" devices.");
    return sb.toString();
  }
  /**
   * <accessor>
   * To get access to devices quantity
   * @return devices quantity
   */
  protected final int devicesCount(){return this.devices.size();}
  /**
   * <producer>
   * To make the array of devices from factory
   * @return the array of devices
   */
  public final generalDeviceProxy[] devices()
  {
    synchronized(this.devices)
    { // copy ArrayList to the array
      return (generalDeviceProxy[])this.devices.toArray(new generalDeviceProxy[0]);
    }
  }
  /**
   * <accessor>
   * To get access to devicevia device's name
   * @param name device's name
   * @return the device or null, if invalid name
   */
  public final generalDeviceProxy getDevice(String name)
  {
    if (name == null) return null;
    synchronized(this.devices)
    { // get device from the devices map
      return (generalDeviceProxy)this.devicesMap.get(name);
    }
  }
  /**
   * <mutator>
   * To add the device to factory
   * @param device the device to add
   */
  protected final boolean appendDevice(generalDeviceProxy device)
  {
      if (device == null) return false;
      String name = device.getDeviceName();
      if (this.getDevice(name) != null) return false;// device with this name exists in the pool
      synchronized(this.devices)
      { // to add as child Unit
        this.addChild( device );
        // place it to devices pool
        this.devices.add( device );
        // place it to devices map
        this.devicesMap.put( name, device );
      }
      return true;
  }
}
