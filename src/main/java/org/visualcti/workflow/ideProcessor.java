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
package org.visualcti.workflow;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.io.IOException;
import java.lang.reflect.*;

import org.jdom.*;
import org.visualcti.util.Tools;
import org.visualcti.briquette.*;
import org.visualcti.workflow.model.*;
import org.visualcti.workflow.facade.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The processor for execute IDE's commands </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Prominic Inc & Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

class ideProcessor
{
/**
 * <attribute>
 * The reference to GUI
 * */
private final IDE ide;
/**
 * <attribute>
 * The name of current action
 * */
private String action=null;
  /**
   * <constructor>
   * */
  public ideProcessor(IDE ide){this.ide=ide;}
  /**
   * <action>
   * Main entry of the processor
   * */
  public void doAction(String action)
  {
      try {
        this.action = action; action = "execute"+action;
        Method toDo = (Method)Actions.get(action);
        toDo.invoke(this,new Object[]{});
      }catch(Exception e) {
        System.out.println("fault "+e.getMessage());
        e.printStackTrace();
        this.defaultAction();
      }
  }
/**
 * <pool>
 * The pool of actions
 * */
private static final HashMap Actions = new HashMap();
  /**
   * <init>
   * To store methods to pool
   * */
  static {
        Method[] entry = ideProcessor.class.getDeclaredMethods();
        for (int i=0;i < entry.length;i++)
        {
            String name = entry[i].getName();
            if (name.startsWith("execute")){
              ideProcessor.Actions.put(name,entry[i]);
            }
        }
  }
  /**
   * <action>
   * default action of processor's command
   * */
  private final void defaultAction()
  {
      System.out.println("Unimplemented action ["+this.action+"]");
  }
/**
 * <attribute>
 * The name of programm's file
 * */
private volatile String programmFile = null;
  /**
   * <action>
   * to execute "New" action
   * */
  private final void executeNew(){
    this.programmFile = null;
    this.ide.setProgramm( Program.newProgram() );
  }
/**
 * <member>
 * The filename extension for Program
 * */
private final static String EXT = ".briquettes.task.xml";
/**
 * <member>
 * The filter for Program files
 * */
private static FileFilter programmFilter = new FileFilter(){
  public boolean accept(java.io.File file){
    if ( file.isDirectory() ) return true;
    String name = file.getName().toLowerCase();
    return name.endsWith( EXT );
  }
  public String getDescription(){return "VisualCTI briquettes programms";}
};
/**
 * <member>
 * The dialog for choose the file
 * */
private final static JFileChooser fileChooser = new JFileChooser();
static {fileChooser.addChoosableFileFilter(programmFilter);}
/**
 * <member>
 * The name of design's XML Element
 * */
private final static String DESIGN = "design";
/**
 * <attribute>
 * The last directory for work
 */
private java.io.File workDirectory = new java.io.File("./");
  /**
   * <action>
   * to execute "Open" action
   * */
  private final void executeOpen(){
    java.io.File file = new java.io.File(this.ide.getProgramm().getFileName());
    fileChooser.setSelectedFile( file );
    fileChooser.setCurrentDirectory( this.workDirectory );
    int result = fileChooser.showOpenDialog( this.ide );
    if(result == JFileChooser.APPROVE_OPTION) {
      file = fileChooser.getSelectedFile();
      this.workDirectory = fileChooser.getCurrentDirectory();
      Program content = Program.newProgram();
      content.setFileName( file.getAbsolutePath() );
      try {
        Element contentXML = Tools.xmlLoad(file);
        content.setXML( contentXML );
        this.ide.reStoreDesign( contentXML.getChild(DESIGN) );
      }catch(Exception e){
        // some wrong
        e.printStackTrace();
        return;
      }
      // to store the name to global
      this.programmFile = content.getFileName();
      // to assign solved programm to IDE
      this.ide.setProgramm(content);
    }
  }
  /**
   * <action>
   * to execute "Save" action
   * */
  private final void executeSave(){
    if (this.programmFile != null) {
      Tools.xmlSave( this.ide.getProgramm().getXML(), this.programmFile );
      return;
    }
    java.io.File file = new java.io.File("NewProgramm"+EXT);
    fileChooser.setSelectedFile( file );
    fileChooser.setCurrentDirectory( this.workDirectory );
    int result = fileChooser.showSaveDialog( this.ide );
    if(result == JFileChooser.APPROVE_OPTION) {
      file = fileChooser.getSelectedFile();
      this.workDirectory = fileChooser.getCurrentDirectory();
      String name = file.getName();
      if ( !name.toLowerCase().endsWith(EXT) ) {
        file = new java.io.File(name+EXT);
      }
      Program content = this.ide.getProgramm();
      content.setFileName( file.getAbsolutePath() );
      Element contentXML = content.getXML();
      Element designXML = new Element(DESIGN);
      this.ide.storeDesign( designXML );
      contentXML.addContent( new Comment("This tree used only for IDE") );
      contentXML.addContent( designXML );
      Tools.xmlSave( contentXML, file );
      // to store the name to global
      this.programmFile = content.getFileName();
      // to assign solved programm to IDE
      this.ide.setProgramm(content);
    }
  }
  /**
   * <action>
   * to execute "Load" action
   * */
  private final void executeLoad(){
    ServerConnection connection = this.ide.getFacade().getServerConnection();
    Cursor ideCursor = this.ide.getCursor();
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    try{
      this.ide.setCursor(waitCursor);
      String groups[] = connection.getTaskGroups();
      this.tasksGroups.setGroups(groups);
      for(int i=0;i < groups.length;i++){
        String group = groups[i];
        String[]tasks = connection.getProgramms(group);
        this.tasksGroups.putTasks(group,tasks);
      }
      this.tasksGroups.selectGroup(this.currentTasksGroup);
      JScrollPane view = new JScrollPane(this.tasksGroups);
      view.setPreferredSize(new Dimension(400,400));
      Object message[] = new Object[2];
      message[0] = "To load the task from server";
      message[1] = view;
      String options[] = new String[]{"Load","Cancel"};
      this.ide.setCursor(ideCursor);
      int reason=
      JOptionPane.showOptionDialog
                    (
                    JOptionPane.getFrameForComponent(this.ide),
                    message,
                    "VisualCTI server's access",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
                    );
      if ( reason == 0 ) {// load chooseed programm
        if ( this.tasksGroups.selectedUserObject instanceof tasksTree.Task) {
          tasksTree.Task node = (tasksTree.Task)this.tasksGroups.selectedUserObject;
          this.currentTasksGroup = node.group;
          this.ide.setCursor(waitCursor);
          Program programm = connection.load(node.name,node.group);
          if ( programm != null ) {
            this.ide.setProgramm(programm);
            Tools.print("Loaded programm ["+programm.getName()+"]...");
          }
        }
      }
    }catch(IOException e){
    }
    this.ide.setCursor(ideCursor);
  }
/**
 * <attribute>
 * current tasks group
 */
private volatile String currentTasksGroup = null;
  /**
   * <action>
   * to execute "Deploy" action
   * */
  private final void executeDeploy(){
    ServerConnection connection = this.ide.getFacade().getServerConnection();
    Program programm = this.ide.getProgramm();
    Cursor ideCursor = this.ide.getCursor();
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    try{
      this.ide.setCursor(waitCursor);
      String groups[] = connection.getTaskGroups();
      this.tasksGroups.setGroups(groups);
      this.tasksGroups.selectGroup(this.currentTasksGroup);
      JScrollPane view = new JScrollPane(this.tasksGroups);
      view.setPreferredSize(new Dimension(400,400));
      Object message[] = new Object[2];
      message[0] = "To deploy the task ["+programm.getName()+"]";
      message[1] = view;
      String options[] = new String[]{"Deploy","Cancel"};
      this.ide.setCursor(ideCursor);
      int reason=
      JOptionPane.showOptionDialog
                    (
                    JOptionPane.getFrameForComponent(this.ide),
                    message,
                    "VisualCTI server's access",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
                    );
      if ( reason == 0 ) {// deploy chooseed
        if ( this.tasksGroups.selectedUserObject instanceof tasksTree.Path) {
          this.ide.setCursor(waitCursor);
          tasksTree.Path node = (tasksTree.Path)this.tasksGroups.selectedUserObject;
          this.currentTasksGroup = node.path;
          connection.deploy( this.currentTasksGroup, programm );
          Tools.print("Deployed programm ["+programm.getName()+"]...");
        }
      }
    }catch(IOException e){
    }
    this.ide.setCursor(ideCursor);
  }
/**
 * <visual>
 * The component for visualize the programms with groups
 */
private final tasksTree tasksGroups = new tasksTree();
/**
 * <tree>
 * The tree for visualize the programms
 */
private static final class tasksTree extends JTree{
  final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tasks");
  final DefaultTreeModel model = new DefaultTreeModel( root );
  private final Map paths = new HashMap();
  private final TreeSelectionModel selection;
  private volatile Object selectedUserObject = null;
  public tasksTree(){
    super.setModel(this.model);
    super.setShowsRootHandles(true);
    this.selection = super.getSelectionModel();
    this.selection.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.selection.addTreeSelectionListener(new TreeSelectionListener(){
      public final void valueChanged(TreeSelectionEvent e){
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
        selectedUserObject = node != null ? node.getUserObject():null;
      }
    });
  }
  private final void setGroups(String[] groups){
    this.root.removeAllChildren(); this.paths.clear();
    for(int i=0;i < groups.length;i++) this.addGroup(groups[i],groups[i],this.root);
    this.model.reload(this.root);
    super.expandPath( new TreePath(this.root.getPath()) );
  }
  private final void selectGroup(String group){
    TreePath path = (TreePath)this.paths.get(group);
    if ( path != null ) super.setSelectionPath(path);
  }
  private final void addGroup(String group,String path,DefaultMutableTreeNode root){
    int slashIndex = path.indexOf("/");
    if ( slashIndex == -1 ){
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Path(group));
      root.add(node); this.paths.put( group, new TreePath(node.getPath()) );
    }else{
      String groupName = new StringTokenizer(path,"/").nextToken();
      path = path.substring(slashIndex+1);
      for(int i=0;i < root.getChildCount();i++){
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
        if ( groupName.equals(node.getUserObject()) ) {
          this.addGroup(group,path,node); return;
        }
      }
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(groupName);
      root.add(node); this.addGroup(group,path,node);
    }
  }
  private final void putTasks(String group,String[] tasks){
    if ( tasks.length == 0 ) return;
    TreePath path = (TreePath)this.paths.get(group);
    if ( path == null ) return;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
    for(int i=0;i < tasks.length;i++) {
      String task = tasks[i];
      if ( task != null )
        node.add(new DefaultMutableTreeNode(new Task(group,task)));
    }
  }
  /*<wrapper> class-wrapper for the task */
  private final static class Task{
    final String group,name;
    Task(String group,String name){this.group=group;this.name=name;}
    public final String toString(){return "Task:"+this.name;}
    public final boolean equals(Object o){
      try {return equals((Task)o);
      }catch(ClassCastException e){}
      return false;
    }
    public final boolean equals(Task o){
      return this.group.equals(o.group) && this.name.equals(o.name);
    }
  }
  /*<wrapper> the class-wrapper for the tasks group */
  private final static class Path{
    String node,path;
    Path(String path){
      this.node = this.path = path;
      StringTokenizer st = new StringTokenizer(path,"/");
      while(st.hasMoreTokens()) this.node=st.nextToken();
    }
    public final String toString(){return this.node;}
    public final boolean equals(Object o){
      try {return equals((Path)o);
      }catch(ClassCastException e){}
      return false;
    }
    public final boolean equals(Path o){return this.path.equals(o.path);}
  }
}
  /**
   * <action>
   * to execute "Print" action
   * */
  private final void executePrint(){}
  /**
   * <action>
   * to execute "Delete" action
   * */
  private final void executeDelete()
  {
    try {
      this.visualChainModel().eraseSelected();
      this.ide.setEnabled("Copy",false);
    }catch(NullPointerException e){}
  }
/**
 * <attribute>
 * The clipboard of chainModel's Items
 * */
private Element clipboard=null;
  /**
   * <action>
   * to execute "Copy" action
   * */
  private final void executeCopy(){
    try{
      this.clipboard = this.visualChainModel().EditCopy();
      this.ide.setEnabled("Paste",true);
    }catch(NullPointerException e){}
  }
  /**
   * <action>
   * to execute "Paste" action
   * */
  private final void executePaste(){
    try{
      this.ide.setEnabled("Paste",false);
      if (this.clipboard != null)
        this.visualChainModel().EditPaste( this.clipboard );
      // to clear the clipborad
      this.clipboard = null;
    }catch(NullPointerException e){}
  }
  /** to get access to curent Chain's visuals model */
  private final chainModel visualChainModel(){
    return this.ide.getChain().getModel();
  }
  /**
   * <action>
   * to execute "Cut" action
   * */
  private final void executeCut(){
    this.executeCopy();
    this.executeDelete();
  }
  /**
   * <action>
   * to execute "Stop" action
   * */
  private final void executeStop(){this.ide.runtime.Stop();}
  /**
   * <action>
   * to execute "Run" action
   * */
  private final void executeRun(){this.ide.runtime.Run();}
  /**
   * <action>
   * to execute "Pause" action
   * */
  private final void executePause(){this.ide.runtime.Pause();}
  /**
   * <action>
   * to execute "Step" action
   * */
  private final void executeStep(){this.ide.runtime.Step();}
  /**
   * <action>
   * to execute "Find" action
   * */
  private final void executeFind(){this.ide.runtime.Find();}
}
