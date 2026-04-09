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
package org.visualcti.server.connector;

import org.visualcti.server.connector.control.*;
import org.visualcti.server.*;
import org.visualcti.util.*;
import org.visualcti.util.Queue;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.io.*;
import java.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The controls panel for manage the server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public class ControlPanel extends JFrame
{
private static final Dimension size = new Dimension(640,480);
private final JButton disconnect = new JButton();
  /**
   * <constructor>
   */
  public ControlPanel()
  {
    super.setTitle("VisualCTI Server's Controls Panel");
    super.getContentPane().add(makeServerConnect(),BorderLayout.NORTH);
    JTabbedPane mainPanel = new JTabbedPane();
    super.getContentPane().add(mainPanel,BorderLayout.CENTER);
    mainPanel.setTabPlacement(JTabbedPane.TOP);
    mainPanel.addTab("Schedulers",null,makeSchedulers(),"The managed telephony channels");
    mainPanel.addTab("Tasks",null,makeTasks(),"The tasks pools");
    mainPanel.addTab("Services",null,makeServices(),"The server's services");
    // to adjust the geometry & location
    super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    super.setSize( ControlPanel.size );
    // To center the frame on the screen
    Dimension screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    int X = (screen.width - ControlPanel.size.width)/2;
    int Y = (screen.height-ControlPanel.size.height)/2;
    super.setLocation(X, Y);
    // to add listener for Window closing's event
    super.addWindowListener(new WindowAdapter(){
        public final void windowClosing(WindowEvent e){ControlPanel.this.exit();}
    });
    // to start the connection's servers
    new commandsDispatcher().start();
    new eventsDispatcher().start();
  }
    /*<producer> to make the server connect panel */
    private final JPanel makeServerConnect(){
      JPanel panel = new JPanel(true);
      panel.setBorder(new BevelBorder(BevelBorder.RAISED));
      panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
      panel.add(Box.createHorizontalStrut(8));
      final DefaultComboBoxModel serverModel =
         new DefaultComboBoxModel(new String[]{"localhost"});
      final JComboBox server = new JComboBox( serverModel );
      server.setEditable(true);
      panel.add(new JLabel("Server:"));
      panel.add(server);panel.add(Box.createHorizontalStrut(3));
      final JTextField login = new JTextField(8);
      panel.add(new JLabel("Login:"));
      panel.add(login);panel.add(Box.createHorizontalStrut(3));
      final JPasswordField password = new JPasswordField(8);
      panel.add(new JLabel("Password:"));
      panel.add(password);panel.add(Box.createHorizontalStrut(13));

      final JButton connect = new JButton("connect");
      panel.add(connect);panel.add(Box.createHorizontalStrut(3));
      disconnect.setText("disconnect");
      panel.add(disconnect);panel.add(Box.createHorizontalStrut(3));
      disconnect.setEnabled(false);
      // adjust the connect
      connect.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          setEnabledProp(false);
          Cursor old = getCursor();
          setCursor(new Cursor(Cursor.WAIT_CURSOR));
          if ( !connect(
                  (String)server.getSelectedItem(),
                  login.getText(),
                  password.getPassword()
                      ) )
            setEnabledProp( true );
          else {
            online();
            String srv = (String)server.getSelectedItem();
            if ( serverModel.getIndexOf(srv) == -1) serverModel.addElement(srv);
            disconnect.setEnabled(true);
          }
          setCursor(old);
        }
        private final void setEnabledProp(boolean flag){
          connect.setEnabled(flag);
          server.setEnabled(flag);
          login.setEnabled(flag);
          password.setEnabled(flag);
        }
      });
      // adjust the disconnect
      disconnect.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          server.setEnabled(true);
          login.setEnabled(true);
          password.setEnabled(true);
          disconnect(
                  (String)server.getSelectedItem(),
                  login.getText(),
                  password.getPassword()
                      );
          disconnect.setEnabled(false);
          offline();
          connect.setEnabled(true);
        }
      });
      return panel;
    }
      /* to disconnect the controls */
      private final void disconnect(){this.disconnect.doClick();}
/**
 * <pool>
 * The pool of server's parts
 */
private final ArrayList parts = new ArrayList();
    /**<action> to set all parts to offline mode */
    private final void offline(){
      for(Iterator i=this.parts.iterator();i.hasNext();){
        serverPart part = (serverPart)i.next();
        part.offline();
      }
    }
    /**<action> to set all parts to online mode */
    private final void online(){
      for(Iterator i=this.parts.iterator();i.hasNext();){
        serverPart part = (serverPart)i.next();
        part.online();
      }
    }
    private final void setUnits(ArrayList units){
      for(Iterator i=this.parts.iterator();i.hasNext();){
        serverPart part = (serverPart)i.next();
        GUI control = part.getControl();
        if ( control != null ) control.setUnits(units);
      }
    }
    private final void processEvent(unitAction event){
      if (event instanceof unitError) System.err.println(event);
      for(Iterator i=this.parts.iterator();i.hasNext();){
        serverPart part = (serverPart)i.next();
        GUI control = part.getControl();
        if ( control != null && control.processEvent(event)) break;
      }
    }
    /*<producer>to make the schedulers panel */
    private final JPanel makeSchedulers(){
      serverPart part = new serverPart(this);
      part.setControl( new SchedulersPanel(part) );
      part.offline();
      this.parts.add(part);
      return part;
    }
    /*<producer>to make the tasks panel */
    private final JPanel makeTasks(){
      serverPart part = new serverPart(this);
      part.setControl( new TasksPanel(part) );
      part.offline();
      this.parts.add(part);
      return part;
    }
    /*<producer>to make the services panel */
    private final JPanel makeServices(){
      serverPart part = new serverPart(this);
      //part.setControl( new JPanel() );
      part.offline();
      this.parts.add(part);
      return part;
    }
    /* to exit the application */
    private final void exit(){
      System.exit(0);
    }
private volatile Client connection=null;
    /* to connect to the server */
    private final boolean connect(String server,String login,char[] password){
      if ( "".equals(login) || password==null || password.length==0 ) return false;
      this.connection = ClientsFactory.makeClient();
      if ( this.connection == null ) return false;
      try {
        if ( this.connection.login(server,login,password) ) {
          String link = this.connection.getLinkName();
          unitCommand request = new unitCommand(link,unitAction.GET_ID,"allowed units");
          request.set(new Parameter("target", "units") );
          request.setNeedResponse(true);
          this.connection.send(request);
          while( true )
          {
            unitAction event = this.connection.receive();
            if ( event instanceof unitResponse) {
              unitResponse response = (unitResponse)event;
              if ( response.getCorrelationID() == request.sequenceID() )
              {
                request.setResponse(response);
                if ( request.isSuccessful() )
                {
                  Parameter par = request.getParameter("units");
                  StringTokenizer st = new StringTokenizer(par.getValue().toString(),";");
                  ArrayList units = new ArrayList();
                  while(st.hasMoreTokens()) units.add( st.nextToken() );
                  Collections.sort( units );
                  this.setUnits( units );
                  this.startProcessors();
                }else {
                  System.err.println("Error to get an units list:");
                  Parameter par = request.getParameter( "@error" );
                  if ( par != null) System.err.println(par.getValue());
                  else System.err.println(" Unknown error :(");
                  this.disconnect(server,login,password);
                  return false;
                }
                break;
              }
            }
          }
          return true;
        }
      }catch(IOException e){
//        e.printStackTrace();
      }
      this.disconnect(server,login,password);
      return false;
    }
    private final void waitSemaphore(Object semaphore){
      try{
        synchronized( semaphore ){ semaphore.wait();}
      }catch(InterruptedException e){}
    }
    private final void notifySemaphore(Object semaphore){
      synchronized( semaphore ){ semaphore.notify();}
    }
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
      if ( event == null ) waitSemaphore(eventsSemaphore);
      else processEvent(event);
    }
  }
}
/***
 * <thread>
 * Class to dispath the GUI's commands
 */
private final class commandsDispatcher extends Thread{
  commandsDispatcher(){super("To transmit the commands to the server");}
  public final void run(){
    while( true ) { unitCommand command = null;boolean empty=false;
      synchronized( commands ){
        if ( !(empty=commands.empty()) ) command=(unitCommand)commands.pop();
      }
      if ( empty ) try{super.sleep(100);}catch(Exception e){}
      else
      if (command != null) sendTheCommand(command);
    }
  }
}
    /* to start/resume the threads for process the connection */
    private final void startProcessors(){
      if ( this.connection != null ) this.notifySemaphore(this.eventsSemaphore);
    }
    /* try to send the command via connection */
    private final void sendTheCommand(unitCommand command){
      try{
        this.connection.send(command);
      }catch(NullPointerException e){
        this.processEvent( new unitResponse(command,"no connection").bad() );
      }catch(IOException e){
        this.disconnect();
        this.processEvent( new unitResponse(command,"no connection").bad() );
      }
    }
    /* to receive the event from the connection */
    private final unitAction receiveEvent(){
      if ( this.connection == null ) return null;
      try{
        if ( this.connection.getLinkName() == null ) return null;
        return this.connection.receive();
      }catch(Exception e){
        this.disconnect();
        return null;
      }
    }
    /* to disconnect from the server */
    private final void disconnect(String server,String login,char[] password){
      if ( this.connection != null ){
        this.connection.close();this.connection=null;
      }
    }
private final Queue commands = new Queue();
  /**
   * <GUI>
   * The panel for place the GUI os server's Part
   */
  public static abstract class GUI extends JPanel{
    private final serverPart control;
    public GUI(serverPart control){this.control=control;}
    public abstract void online();
    public void offline()
    { // to free all command's threads
      synchronized( this.commands )
      {
        for(Iterator i=this.commands.values().iterator();i.hasNext();){
          Object command = i.next();
          synchronized( command ){command.notify();}
        }
        this.commands.clear();
      }
    }
    public abstract void setUnits(ArrayList units);
    public abstract boolean processEvent(unitAction event);
    /**
     * <pool>
     * The pool of commands that wait a execution's finish
     */
    private final HashMap commands = new HashMap();
    /**
     * <processor>
     * To execute the command & wait a response if needs
     * @param command server's command
     */
    protected final void executeCommand(unitCommand command){
      // sent the command to execute
      this.control.execute(command);
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
  }
  /**
   * <part>
   * The part of server's control
   */
  public final static class serverPart extends JPanel{
    private final CardLayout layout = new CardLayout(0,0);
    private GUI control=null;
    private final ControlPanel owner;
    public serverPart(ControlPanel owner){
      super(true);this.owner=owner;
      super.setLayout(this.layout);
      super.add( "offline", new JLabel("O F F L I N E !",JLabel.CENTER) );
    }
    public final GUI getControl(){return this.control;}
    public final void setControl(GUI control){
      if ( this.control != null )
      {
        this.control.offline();
        super.remove(this.control);
      }
      if ( control != null ) super.add("online",this.control=control);
    }
    public final void execute(unitCommand command)
    {
      synchronized(owner.commands){owner.commands.push(command);}
    }
    public final void online(){
      this.layout.show(this,"online");
      if (this.control != null)this.control.online();
    }
    public final void offline(){
      this.layout.show(this,"offline");
      if (this.control != null)this.control.offline();
    }
  }
  /**
   * <start>
   * To start the controls panel
   * @param args command-line arguments
   */
  public static void main(String[] args)
  {
    ControlPanel controlPanel = new ControlPanel();
    controlPanel.setVisible(true);
  }
}
