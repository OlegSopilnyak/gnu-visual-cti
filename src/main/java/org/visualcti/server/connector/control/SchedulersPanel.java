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
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The panel to manage the shedulers </p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public final class SchedulersPanel extends ControlPanel.GUI
{
private static final String SCHEDULER = "/Scheduler";
private static final String HARDWARE = "/Hardware";
private static final String TASKS = "/Tasks";
/**
 * <pool>
 * The pool of valid unitPath's prefixs
 */
private static final String units[] = new String[]{SCHEDULER,HARDWARE,TASKS};

private static  final String IDLE_HTML = "<html>[<font color=\"#FFFF00\">o</font>]</html>";
private static  final String STOPD_HTML = "<html>[<font color=\"#FF0000\">x</font>]</html>";
private static  final String CLOSED_HTML = "<html>[<font color=\"#FF0000\">o</font>]</html>";
private static  final String WORKS_HTML = "<html>[<font color=\"#009900\">o</font>]</html>";
/**
 * <check>
 * To check is event served here
 * @param event the event to check
 * @return true if event will serverd here
 */
private static final boolean isOwner(unitAction event){
  return isOwner(event.getUnitPath());
}
/* to check is valid prefix */
private static final boolean isOwner(String path){
  for(int i=0;i < units.length;i++)
    if ( path.startsWith(units[i]) ) return true;
  return false;// not owned path
}
/**
 * <pool>
 * The pool of unit's paths
 */
private final HashMap unitPaths = new HashMap();
  /**
   * <action>
   * To do online status
   */
  public final void online()
  {
    // list of command's threads
    ArrayList threads = new ArrayList();
    // list of availabled paths
    ArrayList paths = new ArrayList();
    paths.addAll(this.unitPaths.keySet());
    Collections.sort(paths);
    // to make the proxys and prepare to adjust it
    for( Iterator i=paths.iterator();i.hasNext();)
    {
      final String path = (String)i.next();
      if ( path.startsWith(SCHEDULER) && !ALL.equals(path) ) {
        String device = device(path);
        if ( device == null ) continue;
        // to place the proxy to unitPaths
        this.place( new Scheduler(path), device );
        // to make the thread to configure the proxy
        Thread thread = new Thread(){
          public final void run(){
            SchedulersPanel.this.requestSchedulerInfo(path);
          }
        };
        threads.add(thread);
      }
    }
    // to start the threads
    for(Iterator i=threads.iterator();i.hasNext();) ((Thread)i.next()).start();
  }
    /* to calculate the device's name from Scheduler's unitPath */
    private static final String device(String path){
      StringTokenizer st = new StringTokenizer(path,"/");
      try{
        // to skip the scheduler's prefix
        st.nextToken();
        return new StringBuffer(st.nextToken()).append("/")
                              .append(st.nextToken())
                              .toString();
      }catch(NoSuchElementException e){
        return null;
      }
    }
    /* to assign all path with the device to Scheduler's proxy */
    private final void place(Scheduler proxy,String device){
      ArrayList paths = new ArrayList( this.unitPaths.keySet() );
      for(Iterator i=paths.iterator();i.hasNext();)
      {
        String path = (String)i.next();
        if ( path.endsWith(device) ) this.unitPaths.put(path,proxy);
      }
    }
    /* to request from server the Scheduler's info and place it to the proxy */
    private final void requestSchedulerInfo(String path){
      // to make the command
      unitCommand command = new unitCommand(path,unitAction.GET_ID,"info");
      command.set(new Parameter("target","info")).setNeedResponse(true);
      super.executeCommand(command);
      // to solve the answer
      if ( command.isSuccessful() ){
        Scheduler proxy = (Scheduler)this.unitPaths.get(path);
        String value;
        Parameter
        param = command.getParameter("hardware");
        if ( param != null ) proxy.hardware=param.getValue().toString();
        param = command.getParameter("hardware.state");
        if ( param != null ) proxy.hardwareState=param.getValue().toString();
        param = command.getParameter("started");
        if ( param != null ) proxy.started=new Boolean(param.getValue().toString()).booleanValue();
        param = command.getParameter("task");
        if ( param != null ) proxy.task=param.getValue().toString();
        // to configure the Scheduler's button in main panel
        proxy.showButton(proxy.hardwareState);proxy.button.setEnabled(true);
      }
    }
  /**
   * <action>
   * To go the GUI to offline
   */
  public final void offline()
  {
    this.unitPaths.clear();
    super.offline();
    // to clear the main panel
    this.main.removeAll();
    // to clear the right panel
    this.clearScheduler();
  }
  /**
   * <action>
   * To assign the units set to the GUI
   * @param units
   */
  public final void setUnits(ArrayList units)
  {
    this.offline();
    for(Iterator i=units.iterator();i.hasNext();)
    {
      String path = (String)i.next();
      if ( isOwner(path) ) this.unitPaths.put(path,new Object());
    }
  }
  /**
   * <action>
   * To process received event
   * @param event server's event
   * @return true if this GUI was process it
   */
  public final boolean processEvent(unitAction event)
  {
    if ( !isOwner(event) ) return false;
    // to solve the event
    if ( event instanceof unitResponse)
    {
      if ( event.getUnitPath().startsWith(SCHEDULER) )
        super.processResponse( (unitResponse)event );
      else return false;
    } else {
      String path = event.getUnitPath();
      Scheduler proxy = null;
      try{
        proxy = (Scheduler)unitPaths.get(path);
      }catch(ClassCastException e){
        System.err.println("Interested unit's path "+path);
        e.printStackTrace();
        return false;
      }
      if ( proxy != null ) {
        if ( path.startsWith(SCHEDULER) ) proxy.schedulerEvent(event);
        else
        if ( path.startsWith(HARDWARE) ) proxy.hardwareEvent(event);
        else
        if ( path.startsWith(TASKS) ) {
          proxy.tasksEvent(event); return false;// must have finished in tasks GUI
        }
      }
    }
    return true;
  }
  /**
   * <constructor>
   * The contructor of panel
   * @param part part for execute the commands
   */
  public SchedulersPanel(ControlPanel.serverPart part)
  {
    super(part);
    super.setLayout(new BorderLayout());
    super.setDoubleBuffered(true);
    this.makeControl();
    this.makeMain();
    this.makeScheduler();
  }
    /* to make panel with detail scheduler's information */
    private final void makeControl(){
      JPanel control = new JPanel(new FlowLayout(FlowLayout.CENTER));
      super.add(control,BorderLayout.SOUTH);
      control.setBorder(new BevelBorder(BevelBorder.RAISED));
      JButton start = new JButton("start ALL");
      start.setToolTipText("To start all Schedulers");
      start.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){startALL();}
      });
      control.add(start);
      JButton stop = new JButton("stop ALL");
      stop.setToolTipText("To stop all Schedulers");
      stop.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){stopALL();}
      });
      control.add(stop);
    }
/**
 * <panel>
 * The channel-buttons panel
 */
private final JPanel main = new JPanel(new GridLayout(0,16),true);
    /* to make buttons panel */
    private final void makeMain(){
      JPanel canvas = new JPanel(new BorderLayout());
      canvas.setBorder(new BevelBorder(BevelBorder.LOWERED));
      super.add(canvas,BorderLayout.CENTER);
      canvas.add(main,BorderLayout.NORTH);
    }
private final JButton start = new JButton("start");
private final JButton stop = new JButton("stop");
private final JTextField hardware = new JTextField(8);
private final JTextField hardwareState = new JTextField(8);
private final JButton hardwareControl = new JButton("...");
private final JTextField task = new JTextField(8);
private final JButton taskControl = new JButton("...");
private final JTextArea taskLog = new JTextArea(0,15);
    /* to clear the right panel of the main panel */
    private final void clearScheduler(){
      synchronized( this.currentSemaphore ){
        this.start.setEnabled(false); this.stop.setEnabled(false);
        this.hardware.setText("");this.hardwareState.setText("");
        this.hardwareControl.setEnabled(false);
        this.task.setText(""); this.taskLog.setText("");
        this.taskControl.setEnabled(false);
        this.current=null;
      }
    }
private Scheduler current = null;
private final Object currentSemaphore = new Object();
    /* to open the scheduler */
    private final void setScheduler(Scheduler proxy){
      synchronized( this.currentSemaphore ){
        this.current = proxy;
        this.start.setEnabled( !proxy.started );
        this.stop.setEnabled( proxy.started );
        this.text(this.hardware,proxy.hardware);
        this.text(this.hardwareState,proxy.hardwareState);
        this.text(this.task,proxy.task);
        this.text(this.taskLog, proxy.taskLog.toString());
      }
    }
      /* to place the text to text's component */
      private static final void text(javax.swing.text.JTextComponent field,String text){
        synchronized( field ) {
          field.setText(text); field.setCaretPosition(0);
        }
      }
    /* to make the right panel */
    private final void makeScheduler(){
      Insets margin = new Insets(1,1,1,1);
      JPanel panel = new JPanel(new BorderLayout(),true);
      super.add(panel,BorderLayout.EAST);
      panel.setBorder(new BevelBorder(BevelBorder.RAISED));
      JPanel top = new JPanel(false);
      top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
      panel.add(top,BorderLayout.NORTH);
      JPanel control = new JPanel();
      top.add(control);
      top.add(Box.createVerticalStrut(3));
      this.start.setMargin(margin);
      this.start.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          start.setEnabled(false);
          try{
            unitCommand command = new unitCommand(current.path,unitAction.START_ID,"");
            command.setNeedResponse(false);
            SchedulersPanel.super.executeCommand(command);
          }catch(NullPointerException ne){}
        }
      });
      this.stop.setMargin(margin);
      this.stop.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          stop.setEnabled(false);
          try{
            unitCommand command = new unitCommand(current.path,unitAction.STOP_ID,"");
            command.setNeedResponse(false);
            SchedulersPanel.super.executeCommand(command);
          }catch(NullPointerException ne){}
        }
      });
      control.add(this.start);control.add(this.stop);
      top.add(new JLabel("Hardware"));
      JPanel hPane = new JPanel(new BorderLayout());
      hPane.add(this.hardwareState,BorderLayout.CENTER);
      hPane.add(this.hardware,BorderLayout.WEST);
      hPane.add(this.hardwareControl,BorderLayout.EAST);
      this.hardware.setText("dxxxB1C1");
      this.hardwareState.setText("IDLE");
      top.add(hPane);
      this.hardware.setEditable(false);
      this.hardwareControl.setMargin(margin);
      this.hardwareState.setEditable(false);
      top.add(Box.createVerticalStrut(3));
      top.add(new JLabel("Task"));
      JPanel tPane = new JPanel(new BorderLayout());
      tPane.add(this.task,BorderLayout.CENTER);
      tPane.add(this.taskControl,BorderLayout.EAST);
      this.task.setText("VoiceMail System");
      top.add(tPane);
      this.task.setEditable(false);
      this.taskControl.setMargin(margin);
      top.add(Box.createVerticalStrut(3));
      top.add(new JLabel("Task's log"));
      panel.add(new JScrollPane(this.taskLog),BorderLayout.CENTER);
      JPanel viewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JButton viewTaskLog = new JButton("View Log");
      viewPanel.add(viewTaskLog);
      panel.add(viewPanel,BorderLayout.SOUTH);
      viewTaskLog.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){showTaskLog();}
      });
    }
    private final void showTaskLog(){
      JTextArea view = new JTextArea(20,50);
      Object message[] = new Object[2];
      message[0] = "Tasks Log of "+this.hardware.getText();
      message[1] = new JScrollPane(view);
      this.text( view, this.taskLog.getText() );
      String options[] = new String[]{"Close","Clear Log"};
      int reason=
      JOptionPane.showOptionDialog
                    (
                    JOptionPane.getFrameForComponent(this.taskLog),
                    message,
                    "VisualCTI tasks Log view",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
                    );
      if ( reason == 1) {// to clear the log
        this.text(this.taskLog,""); current.taskLog.setLength(0);
      }
    }
/**
 * <const>
 * The name of Schedulers group
 */
private static final String ALL = "/Scheduler/all";
    /* to start all schedulers */
    private final void startALL(){
      if ( this.unitPaths.get(ALL) == null ) return;
      unitCommand command = new unitCommand(ALL,unitAction.START_ID,"To start all");
      command.setNeedResponse(false);
      super.executeCommand(command);
    }
    /* to stop all schedulers */
    private final void stopALL(){
      if ( this.unitPaths.get(ALL) == null ) return;
      unitCommand command = new unitCommand(ALL,unitAction.STOP_ID,"To stop all");
      command.setNeedResponse(false);
      super.executeCommand(command);
    }
/***
 * <proxy>
 * To represent's the information about the VisualCTI's Scheduler
 */
private final class Scheduler {
  private final JButton button = new JButton("[o]");
  private final String path;
  private boolean started = false;
  private String task="";
  private final StringBuffer taskLog = new StringBuffer();
  private String hardware="";
  private String hardwareState="";
  /**
   * <constructor>
   * Constructor
   * @param path the Scheduler's unitPath
   */
  Scheduler(String path) {
    this.path = path;
    this.button.setEnabled(false);
    SchedulersPanel.this.main.add( this.button );
    this.button.setMargin(new Insets(1,1,1,1));
    this.button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        SchedulersPanel.this.setScheduler( Scheduler.this );
      }
    });
    this.button.setToolTipText("To control the "+path);
  }
  /**
   * <mutator>
   * To change the button's view, in dependence of hardware's state
   * @param state hardware's state
   */
  void showButton(String state){
    if ("IDLE".equalsIgnoreCase(state) ) {
      this.button.setText(IDLE_HTML);
    } else
    if ("STOPED".equalsIgnoreCase(state) ) {
      this.button.setText(STOPD_HTML);
    } else
    if ("CLOSED".equalsIgnoreCase(state) ) {
      this.button.setText(CLOSED_HTML);
    } else
    this.button.setText(WORKS_HTML);
  }
  /**
   * <action>
   * To hadle the event from the hardware
   * @param event the event
   */
  void hardwareEvent(unitAction event){
    if ( event.getID() != unitAction.STATE_ID ) return;
    this.hardwareState = event.getDescription();
    synchronized( currentSemaphore ) {
      if ( this == current ) {
        text(SchedulersPanel.this.hardwareState,this.hardwareState);
      }
    }
    this.showButton(this.hardwareState);
  }
  /**
   * <action>
   * To handle the event from the Scheduler
   * @param event the event
   */
  void schedulerEvent(unitAction event){
    switch (event.getID()){
      case unitAction.ERROR_ID:
        this.taskLog.append(event);
        synchronized( currentSemaphore ) {
          if ( this == current ) {
            SchedulersPanel.this.taskLog.append("\n");
            SchedulersPanel.this.taskLog.append(event.toString());
          }
        }
        this.taskLog.append("\n");
        this.taskLog.append(event.toString());
        break;
      case unitAction.START_ID:
        this.started = true;
        synchronized( currentSemaphore ) {
          if ( this == current ) {
            SchedulersPanel.this.start.setEnabled(false);
            SchedulersPanel.this.stop.setEnabled(true);
          }
        }
        break;
      case unitAction.STOP_ID:
        this.started = false;
        synchronized( currentSemaphore ) {
          if ( this == current ) {
            SchedulersPanel.this.start.setEnabled(true);
            SchedulersPanel.this.stop.setEnabled(false);
            SchedulersPanel.this.taskLog.setText("");
          }
        }
        this.taskLog.setLength(0);
        break;
      case unitAction.STATE_ID:
        String message = event.getDescription();
        synchronized( currentSemaphore ) {
          if ( this == current ) {
            SchedulersPanel.this.taskLog.append("\n");
            SchedulersPanel.this.taskLog.append(message);
          }
        }
        this.taskLog.append("\n");
        this.taskLog.append(message);
        break;
    }
  }
  /**
   * <action>
   * To handle the event from Tasks poll
   * @param event the event
   */
  void tasksEvent(unitAction event){
    if ( event.actionClass() == unitAction.EVENT && event.getID() == unitAction.STATE_ID ){
      String message = event.getDescription();
      if ( message.startsWith("current") ){
        StringTokenizer st = new StringTokenizer(message,"\n\r");
        try{
          // to skip the type
          st.nextToken();
          // to assign the name
          this.task = st.nextToken();
        }catch(NoSuchElementException e){
          e.printStackTrace();
          this.task = "";
        }
        // to update the textfield
        synchronized( currentSemaphore ) {
          if ( this == current ) text(SchedulersPanel.this.task,this.task);
        }
      }
    }
  }
}
}
