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
package org.visualcti.workflow.environment;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

import org.visualcti.workflow.facade.*;
import org.visualcti.workflow.server.*;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * To edit the Server's properties</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class serverPartEditor extends partEditor
{
/**
 * <attribute>
 * The model of server's types combobox
 */
private final DefaultComboBoxModel model = new DefaultComboBoxModel();
private final JComboBox serverType = new JComboBox( model );
private final JTextField server =new JTextField(12);
private final JTextField login  =new JTextField(12);
private final JPasswordField password=new JPasswordField(12);
private final JButton Connect = new JButton("Connect");


  public serverPartEditor()
  {
    super.setLayout(new BorderLayout());super.setDoubleBuffered(false);
    JPanel editor = new JPanel( false );
    Border border = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(142, 142, 142)),"Connection");
    editor.setBorder( border );
    editor.setLayout(new BoxLayout(editor,BoxLayout.Y_AXIS));
    TitledBorder serverTypeBorder = new TitledBorder("Server type");
    serverTypeBorder.setTitleJustification(TitledBorder.CENTER);
    this.serverType.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){switchServer();}
    });
    this.serverType.setBorder(serverTypeBorder);
    this.serverType.setToolTipText("To change the server's type.");
    editor.add(this.serverType); editor.add(Box.createVerticalStrut(2));
    this.model.addElement("Visual CTI");
    ServerConnection nativeServer = new nativeServerConnection();
    this.servers.put( "Visual CTI", nativeServer );
    JPanel properties = new JPanel(false);
    TitledBorder connectBorder = new TitledBorder("Properties");
    connectBorder.setTitleJustification(TitledBorder.CENTER);
    properties.setBorder(connectBorder);
    properties.setLayout(new BoxLayout(properties,BoxLayout.Y_AXIS));
    this.server.setToolTipText("The server's address");
    this.login.setToolTipText("The login to server");
    this.password.setToolTipText("The login's password");
    addLine("[S]",this.server,properties); properties.add(Box.createVerticalStrut(2));
    addLine("[L]",this.login,properties); properties.add(Box.createVerticalStrut(2));
    addLine("[P]",this.password,properties); properties.add(Box.createVerticalStrut(2));
    editor.add(properties);
    JPanel connect = new JPanel(new FlowLayout(), false);
    connect.add(this.Connect);
    editor.add(connect);
    this.Connect.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        connectAction();
      }
    });

    super.add( new JScrollPane(editor), BorderLayout.NORTH );
  }
/**
 * <pool>
 * The pool of connectors to server
 */
private final Map servers = new HashMap();
    /**
     * <action>
     * To switch the server's type
     */
    private final void switchServer(){
      String server = (String)this.serverType.getSelectedItem();
      try{
        ServerConnection connection = (ServerConnection)this.servers.get(server);
        if ( connection != null ) {
          super.owner.getFacade().setServerConnection(connection);
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }
/**
 * <flag>
 * The flag, is last connection is successful?
 */
private volatile boolean connected = false;
/**
 * <attribute>
 * The listener of connection's disconnect
 */
private final ServerConnection.disconnectListener disconnect =
  new ServerConnection.disconnectListener(){
    public final void disconnected(){
      if ( connected ) {
        connected = false;
        adjustPartEditor();
      }
    }
  };
    /**
     * <action>
     * Try to connect/disconnect the server
     */
    private final void connectAction(){
      ServerConnection connection = super.owner.getFacade().getServerConnection();
      if ( connection == null ) {
        String server = (String)this.serverType.getSelectedItem();
        try{
          connection = (ServerConnection)this.servers.get(server);
          if ( connection != null ) {
            super.owner.getFacade().setServerConnection(connection);
          }
        }catch(Exception e){
          e.printStackTrace();
          return;
        }
      }
      this.Connect.setEnabled(false);
      this.properties(false);
      if ( !this.connected ) {
        String Server = this.server.getText();
        String Login = this.login.getText();
        char[] Password = this.password.getPassword();
        this.connected=connection.connect(Server,Login,Password);
      }else {
        connection.disconnect();
        this.connected = false;
      }
      this.adjustPartEditor();
    }
    /**
     * <action>
     * To adjust the visual components of the part's editor
     */
    private final void adjustPartEditor(){
      ServerConnection connection =
          super.owner.getFacade().getServerConnection();
      if ( this.connected ) {
        try{connection.addDisconnecteListener(this.disconnect);
        }catch(Exception e){}
        this.Connect.setText("Disconnect");
        this.properties(false);
      }else {
        connection.removeDisconnectListener(this.disconnect);
        this.Connect.setText("Connect");
        this.properties(true);
      }
      this.Connect.setEnabled(true);
      this.Connect.getParent().invalidate();
    }
    private final void properties(boolean enable){
      this.server.setEditable(enable);
      this.login.setEnabled(enable);
      this.password.setEnabled(enable);
      this.serverType.setEnabled(enable);
      super.owner.getFacade().setEnabled("Load",!enable);
      super.owner.getFacade().setEnabled("Deploy",!enable);
    }
    /*
     * <mutator>
     * To add text's field
     */
    private final void addLine(final String title,final JTextField editor,JPanel place){
      JPanel line = new JPanel(false);line.setLayout(new BoxLayout(line,BoxLayout.X_AXIS));
      line.add(new JLabel(title));
      line.add(Box.createHorizontalStrut(2));
      line.add(editor);
      place.add(line);
    }
  /**
   * <accessor>
   * To get the name of environment's part
   * */
  public final String getName() {return "Server";}
  /**
   * <refresh>
   * To reload visuals from env part
   * Nothing to do (not a Chain's oriented editor)
   * */
  public final void reload() {}
  /**
   * <cleaner>
   * To clean all references to Chain
   * Nothing to do (not a Chain's oriented editor)
   * */
  public final void clean() {}
}
