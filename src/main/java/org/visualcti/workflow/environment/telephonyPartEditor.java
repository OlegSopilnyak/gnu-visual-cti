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
package org.visualcti.workflow.environment;

import org.visualcti.workflow.*;
import org.visualcti.workflow.facade.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.visualcti.server.task.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.provider.soundcard.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br></p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class telephonyPartEditor extends partEditor
{
  /**
   * <accessor>
   * To get the name of environment's part
   * @return the name of the environment's part
   */
  public final String getName(){return "Telephony";}
/**
 * The container of visul components of devices
 */
private final JTabbedPane devicesPlace = new JTabbedPane(JTabbedPane.TOP);
  /**
   * <constructor>
   * Making the editor of telephony device
   */
  public telephonyPartEditor()
  {
    super.setLayout(new BorderLayout(0,0));super.setDoubleBuffered(false);
    super.add(this.devicesPlace,BorderLayout.CENTER);
    this.devicesPlace.setFont(new Font("dialog",Font.BOLD,10));
    this.devicesPlace.getModel().addChangeListener(new ChangeListener(){
      public final void stateChanged(ChangeEvent e) {
        int index = devicesPlace.getSelectedIndex();
        if (index != -1) selectDevice( index );
      }
    });
    this.placeSoundCard();
    this.devicesPlace.setSelectedIndex( 0 );
  }
/**
 * The container of devices
 */
private final Map devices = new HashMap();
private final ArrayList tabs = new ArrayList();
/**
 * To attach soundcard telephony device
 */
  private final void placeSoundCard(){
    soundDeviceFactory factory = new soundDeviceFactory();
    generalDeviceProxy[] proxy = factory.devices();
    if ( proxy.length == 0 ) return;
    soundDevice device = (soundDevice)proxy[0];
    this.devices.put(device.getDeviceName(),device);
    this.tabs.add( device );
    try{device.open();}catch(Exception e){}
    devicesPlace.addTab("emulator", new JScrollPane(device.getUI()) );
  }
  public final void clean(){}
  public final void reload()
  {
    Environment env = this.getEnvironment();
    if ( env == null ) return;
    for(Iterator i=devices.entrySet().iterator();i.hasNext();){
      Map.Entry entry = (Map.Entry)i.next();
      String name = "telephony/"+entry.getKey();
      env.setPart( name, entry.getValue() );
    }
    this.selectDevice( this.devicesPlace.getSelectedIndex() );
  }
  private final Environment getEnvironment(){
    try{
      return super.owner.getFacade().getEnvironment();
    }catch(NullPointerException e){
      return null;
    }
  }
  private final void selectDevice(int index){
    Environment env = this.getEnvironment();
    if ( env == null ) return;
    generalDeviceProxy device = (generalDeviceProxy)this.tabs.get(index);
    if ( device == null ) return;
    String name = "telephony/" + device.getDeviceName();
    env.setPart( generalDeviceProxy.SELECTED_DEVICE, name );
  }
}
