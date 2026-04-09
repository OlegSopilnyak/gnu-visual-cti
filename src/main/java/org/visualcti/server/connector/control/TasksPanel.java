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
package org.visualcti.server.connector.control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import org.visualcti.util.*;
import org.visualcti.server.connector.*;
import org.visualcti.server.*;
import org.jdom.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The panel for manage the tasks pools in the server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public class TasksPanel extends ControlPanel.GUI
{
private static final String TASKS = "/Tasks";
private static final boolean isOwner(unitAction event){
  return isOwner(event.getUnitPath());
}
private static final boolean isOwner(String path){
  return path.startsWith(TASKS);
}
/**
 * <pool>
 * The pool of unit's paths
 */
private final HashMap unitPaths = new HashMap();
    /**
     * <action>
     * To activate the GUI
     */
    public final void online()
    {
      for( Iterator i=this.unitPaths.keySet().iterator();i.hasNext();)
      {
        final String path = (String)i.next();
        // to make & start the thread to configure the proxy
        new Thread(){
          public final void run(){
            TasksPanel.this.requestTasksPoolInfo(path);
          }
        }.start();
      }
      // to adjust the tree
      this.tree.expandPath(new TreePath(this.root.getPath()));
    }
      /* to request from server the Scheduler's info and place it to the proxy */
      private final void requestTasksPoolInfo(String path){
        // to make the command
        unitCommand command = new unitCommand(path,unitAction.GET_ID,"info");
        command.set(new Parameter("target","info")).setNeedResponse(true);
        // to sent command for execute & wait the answer
        super.executeCommand( command );
        // to solve the answer
        if ( command.isSuccessful() ){
          TaskPool proxy = (TaskPool)this.unitPaths.get(path);
          String value;
          Parameter
          param = command.getParameter("unit.state");
          if ( param != null ) proxy.state=param.getValue("???");
          param = command.getParameter("tasks.list");
          if ( param != null ) {
            StringTokenizer st = new StringTokenizer(param.getValue(""),"\n\r");
            proxy.tasks.clear();
            while( st.hasMoreTokens() )proxy.tasks.add(st.nextToken());
          }
          // to configure the tree
          TreeNode node = (TreeNode)proxy.treePath.getLastPathComponent();
          this.model.reload(node);
          synchronized( this.currentSemaphore ) {
            if ( proxy == this.current ) updateControl();
          }
        }
      }
    /**
     * <action>
     * To made the GUI is offline
     */
    public final void offline(){super.offline();this.clearAll();}
    public final void setUnits(ArrayList units)
    {
      this.clearAll();
      TaskPool pool=new TaskPool("public","System");
      pool.unitPath = "/Tasks/System/public";
      for(Iterator i=units.iterator();i.hasNext();)
      {
        String path = (String)i.next();
        if ( isOwner( path ) ) addPath(path);
      }
      if ( this.publicPool == null ) this.add(pool);
    }
      private final void addPath(String path){
        String factory=null,device=null;
        StringTokenizer st = new StringTokenizer(path,"/");
        try{st.nextToken();// skip prefix
          factory=st.nextToken();// may throw
          device=st.nextToken();// may throw
        }catch(Exception e){}
        if ( factory==null || device==null ) return;
        TaskPool pool = new TaskPool(device,factory);
        pool.unitPath = path;
        this.add(pool);
      }
    public final boolean processEvent(unitAction event)
    {
      if ( !isOwner(event) ) return false;
      if ( event instanceof unitResponse) {
        super.processResponse((unitResponse)event);
      } else {
        String path = event.getUnitPath();
        TaskPool proxy = null;
        try{
          proxy = (TaskPool)unitPaths.get(path);
        }catch(ClassCastException e){
          System.err.println("Interested unit's path "+path);
          e.printStackTrace();
          return false;
        }
        if ( proxy != null ) proxy.handleEvent(event);
        else System.out.println("Unregistered tasks event:\n"+event);
      }
      return true;
    }

private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tasks Pools>");
private final DefaultTreeModel model = new DefaultTreeModel(root);
private final JTree tree = new JTree( model );
  public TasksPanel(ControlPanel.serverPart part)
  {
    super(part);
    super.setLayout(new BorderLayout());
    super.setDoubleBuffered(true);
    JScrollPane left = new JScrollPane( this.tree );
    this.tree.setShowsRootHandles(true);
    TreeSelectionModel selection = this.tree.getSelectionModel();
    selection.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    selection.addTreeSelectionListener(new TreeSelectionListener(){
      public final void valueChanged(TreeSelectionEvent e){
        TreePath  path = e.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        if ( node != null ) selected( node.getUserObject() );
      }
    });
    left.setBorder(new TitledBorder("The tree of Tasks Pools"));
    super.add(left,BorderLayout.CENTER);
    this.makeControl();
  }
/**
 * <attribute>
 * Selected, current pool
 */
private volatile TaskPool current=null;
/**
 * <semaphore>
 * The semaphore for sinchronize the access to this.current
 */
private final Object currentSemaphore = new Object();
    /*
     * <notify>
     * The tree notify, when node is selected
     */
    private final void selected(Object nodeData){
      if (nodeData instanceof TaskPool){
        TaskPool pool = (TaskPool)nodeData;
        if ( pool.isPublic() )
          this.showPublic(); else this.showLocal();
        this.poolSelected(pool);
      }else this.showNone();
    }
    private final void poolSelected(final TaskPool pool){
      synchronized(this.currentSemaphore){this.current = pool;}
      new Thread(){
        public void run(){requestTasksPoolInfo(pool.unitPath);}
      }.start();
    }
private static final String NONE = "nothing";
private static final String LOCAL = "local";
private static final String PUBLIC = "public";
private final CardLayout control = new CardLayout();
private final JPanel controlPanel = new JPanel(control,true);
private final JTextField description = new JTextField(20);
private final HashMap controls = new HashMap();
    /*<builder> to make the control panels */
    private final void makeControl(){
      Border bevel = new BevelBorder(BevelBorder.RAISED);
      TitledBorder border = new TitledBorder(bevel,"Tasks Control");
      border.setTitleJustification(TitledBorder.CENTER);
      controlPanel.setBorder( border );
      controlPanel.add(NONE,new JLabel("No selected Pool",JLabel.CENTER));
      JPanel
      poolCtrl = new poolControlPanel();
      this.controls.put(LOCAL,poolCtrl);
      controlPanel.add(LOCAL,poolCtrl);
      poolCtrl = new poolControlPanel();
      this.controls.put(PUBLIC,poolCtrl);
      controlPanel.add(PUBLIC,poolCtrl);
//      controlPanel.add(PUBLIC,new JLabel("The public pool",JLabel.CENTER));
//      controlPanel.add(LOCAL,new JLabel("The local pool",JLabel.CENTER));
      JPanel ctrl = new JPanel(new BorderLayout());
      description.setEditable(false);
      description.setBorder(null);
      super.add(ctrl,BorderLayout.EAST);
      ctrl.add(controlPanel,BorderLayout.CENTER);
      ctrl.add(description,BorderLayout.SOUTH);
      this.showNone();
    }
    private final void updateControl(){
      try {
        this.currentPoolControl.updateContent(this.current);
      }catch(NullPointerException e){}
    }
    private final void about(String text){
      this.description.setText(text);
      this.description.setCaretPosition(0);
    }
/**
 * <attribute>
 * visible control panel of the Pool
 */
private volatile poolControlPanel currentPoolControl=null;
    /* nothing */
    private final void showNone(){
      this.control.show(this.controlPanel,NONE);
      this.currentPoolControl = (poolControlPanel)this.controls.get(NONE);
      this.about("Nothing to do...");
    }
    /* local pool selected */
    private final void showLocal(){
      this.control.show(this.controlPanel,LOCAL);
      this.currentPoolControl = (poolControlPanel)this.controls.get(LOCAL);
      this.about("Local tasks pool...");
    }
    private final void showPublic(){
      this.control.show(this.controlPanel,PUBLIC);
      this.currentPoolControl = (poolControlPanel)this.controls.get(PUBLIC);
      this.about("Public tasks pool...");
    }
/**
 * <attribute>
 * The public tasks pool
 */
private volatile TaskPool publicPool = null;
    /*<mutator> to add the pool to panel's visualization */
    private final void add(TaskPool pool){
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(pool);
      if (pool.isPublic() ) {
        this.publicPool = pool;
        this.model.insertNodeInto(node,this.root,0);
      }else {
        poolsGroup group = this.getGroup(pool); group.add(pool);
        group.node.add(node);
        this.model.reload(group.node);
      }
      pool.treePath = new TreePath( node.getPath() );
      this.unitPaths.put(pool.unitPath,pool);
      this.treePaths.put(pool.treePath,pool);
    }
    private final poolsGroup getGroup(TaskPool pool){
      synchronized( this.groups ){
        String owner = pool.owner;
        poolsGroup group = (poolsGroup)this.groups.get(owner);
        if ( group == null ) {
          group = new poolsGroup( owner );
          group.node = new DefaultMutableTreeNode(group);
          this.root.add(group.node);
          this.groups.put(owner,group);
        }
        return group;
      }
    }
    private final void clearAll(){
      this.publicPool = null;
      this.root.removeAllChildren();
      this.model.reload(this.root);
      this.treePaths.clear();
      this.unitPaths.clear();
      this.groups.clear();
      this.showNone();
      this.scanExternal();
    }
    private final void scanExternal(){
      this.external.clear();
      File directory = new File("./");
//System.out.println("Scan the directory for external tasks");
      File[] taskFiles = directory.listFiles(new FilenameFilter(){
        public final boolean accept(File directory,String name){
          name = name.toLowerCase();
          return name.endsWith(".task.xml");
        }
      });
      if ( taskFiles == null || taskFiles.length == 0) return;
//System.out.println("found "+taskFiles.length+" files. Try to solve it");
      for( int i=0;i < taskFiles.length;i++ ) this.addExternal( taskFiles[i] );
    }
    private final void addExternal(File file){
      if ( Tools.emptyXML==Tools.xmlLoad(file) ){
//System.out.println("Invalid XML in "+file.getName());
      }else{
//System.out.println("Added external file:"+file.getName());
        this.external.add( file );
      }
      Collections.sort( this.external );
    }
private final ArrayList external = new ArrayList();
private final HashMap treePaths = new HashMap();
private final HashMap groups = new HashMap();
/** the group of tasks pool */
private final class poolsGroup{
  poolsGroup(String owner){this.owner=owner;}
  final void add(TaskPool pool){this.group.add(pool);}
  private String owner;
  private DefaultMutableTreeNode node;
  private final ArrayList group=new ArrayList();
  public final String toString(){return this.owner;}
}
/** the task pool for the scheduler */
private final class TaskPool{
  TaskPool(String name,String owner){this.name=name;this.owner=owner;}
  final boolean isPublic(){return "public".equalsIgnoreCase(this.name);}
  private String name;
  private String owner;
  private TreePath treePath=null;
  private String unitPath=null;
  private String state = "unknown";
  private String current = "";
  private final ArrayList tasks = new ArrayList();
  public final String toString(){return this.name+"("+this.state+")";}
  public final void handleEvent(unitAction event){
    if (event instanceof unitError) System.err.println(event);
    else
    if ( event.getID() == unitAction.START_ID ){
      this.state = "active";
    }else
    if ( event.getID() == unitAction.STOP_ID ){
      this.state = "passive";
    }
    else{
//System.out.println("TaskPool:state's event:"+event);
      String message = event.getDescription();
//System.out.println("TaskPool:state's event description["+message+"]");
      try{
        StringTokenizer st = new StringTokenizer(message,"\n\r");
        String type = st.nextToken();
//System.out.println("To process type >"+type);
        if ( type.equals("tasks.list") ){
          this.tasks.clear();
          while( st.hasMoreTokens() ) this.tasks.add(st.nextToken());
        }else
        if ( type.equals("current") ){
          this.current = st.nextToken();
//System.out.println("TaskPool:now current is ["+current+"]");
        }else this.state = type;
      }catch(NoSuchElementException e){
        this.current = "";
        this.state = message;
      }
    }
    // to configure the tree
    TreeNode node = (TreeNode)this.treePath.getLastPathComponent();
    model.reload(node);
    synchronized( currentSemaphore ) {
      if ( this == TasksPanel.this.current ) updateControl();
    }
  }
}
private class listControlPanel extends controlPanel{
  private final JList list;
  listControlPanel(JList list){this.list=list;}
  protected void Add(){}
  protected void Del(){}
  protected void Up(){}
  protected void Down(){}
  protected void Edit(){}
}
private class poolControlPanel extends JPanel{
  final DefaultListModel contentModel = new DefaultListModel();
  final DefaultListModel availableModel = new DefaultListModel();
  final JList content = new JList(contentModel);
  final JList available = new JList(availableModel);
  final listControlPanel contentControl = new listControlPanel(content);
  final listControlPanel availableControl = new listControlPanel(available);
  private TaskPool owner=null;
  private TaskPool aOwner=null;
  poolControlPanel(){
    super(new BorderLayout(),true);
    JPanel control = new JPanel(false);
    this.makeControlPanels();
    control.setLayout(new BoxLayout(control,BoxLayout.Y_AXIS));
    control.add(new JScrollPane(this.content));
    control.add(this.contentControl);
    control.add(Box.createVerticalStrut(6));
    control.add(new JScrollPane(this.available));
    control.add(this.availableControl);
    super.add(control,BorderLayout.CENTER);
  }
  private final void makeControlPanels(){
    this.contentControl.addOnly();
    JButton
    button = this.contentControl.getButton("add");
    this.contentControl.remove(button);
    button = this.contentControl.getButton("del");
    button.setToolTipText("To unistall selected task");
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){deleteTask();}
    });
    button = this.contentControl.getButton("up");
    button.setToolTipText("To move up selected task");
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){moveUpTask();}
    });
    button = this.contentControl.getButton("down");
    button.setToolTipText("To move down selected task");
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){moveDownTask();}
    });
    button = this.contentControl.getButton("edit");
    button.setToolTipText("To edit selected task");
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){editTask();}
    });
    ListSelectionModel
    sModel = this.content.getSelectionModel();
    sModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sModel.addListSelectionListener(new ListSelectionListener(){
      public final void valueChanged(ListSelectionEvent e){
        if (content.getSelectedValue() != null) contentControl.allFeatures();
      }
    });
    this.availableControl.addOnly();
    button = this.availableControl.getButton("add");
    button.setToolTipText("To add external task-file");
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){addExternal();}
    });
    button = this.availableControl.getButton("del");
    this.availableControl.remove(button);
    button = this.availableControl.getButton("up");
    button.setToolTipText("To install the selected task");
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){installTask();}
    });
    button = this.availableControl.getButton("down");
    this.availableControl.remove(button);
    button = this.availableControl.getButton("edit");
    this.availableControl.remove(button);
    sModel = this.available.getSelectionModel();
    sModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sModel.addListSelectionListener(new ListSelectionListener(){
      public final void valueChanged(ListSelectionEvent e){
        if (available.getSelectedValue() != null) availableControl.allFeatures();
      }
    });
  }
  /*<button> to add filename to external files list */
  private final void addExternal() {
    fileChooser.setCurrentDirectory(currentDirectory);
    int result = fileChooser.showOpenDialog( this );
    if(result == JFileChooser.APPROVE_OPTION) {
      currentDirectory = fileChooser.getCurrentDirectory();
      File file = fileChooser.getSelectedFile();
      TasksPanel.this.external.add( file );
      this.updateAvailable(this.aOwner);
    }
  }
  /*<button> To install selected task to current tasks pool */
  private final void installTask(){
    proxyTask item = (proxyTask)this.available.getSelectedValue();
    String protocol=null;
    if ( item.name != null ) protocol = "Task";
    else
    if ( item.file != null ) protocol = "File";
    Parameter install = null;
    if ( protocol.equals("File") ) {
      Element xml = Tools.xmlLoad(item.file);
      if (xml != Tools.emptyXML) install = new Parameter("install",xml);
    } else
    if ( protocol.equals("Task") ) {
      install = new Parameter("install",item.name);
    }
    if ( install != null) {
      unitCommand command = new unitCommand(current.unitPath,unitAction.SET_ID,"install");
      command.set( new Parameter("type","install") ).set( install );
      command.setNeedResponse( true );
      executeCommand( command );
    }
  }
  /*<button> To uninstall selected task to current tasks pool */
  private final void deleteTask(){
    String item = (String)this.content.getSelectedValue();
    if ( item != null ){
      unitCommand command = new unitCommand(current.unitPath,unitAction.SET_ID,"delete");
      command.set( new Parameter("type","delete") ).set(new Parameter("task",item));
      command.setNeedResponse( true );
      executeCommand( command );
    }
  }
  /*<button> To move up selected task */
  private final void moveUpTask(){
    String item = (String)this.content.getSelectedValue();
    if ( item != null ){
      unitCommand command = new unitCommand(current.unitPath,unitAction.SET_ID,"move up");
      command.set( new Parameter("type","move") ).set(new Parameter("task",item));
      command.set(new Parameter("direction","up")).setNeedResponse( true );
      executeCommand( command );
    }
  }
  /*<button> To move down selected task */
  private final void moveDownTask(){
    String item = (String)this.content.getSelectedValue();
    if ( item != null ){
      unitCommand command = new unitCommand(current.unitPath,unitAction.SET_ID,"move down");
      command.set( new Parameter("type","move") ).set(new Parameter("task",item));
      command.set(new Parameter("direction","down")).setNeedResponse( true );
      executeCommand( command );
    }
  }
  /*<button> To edit selected task */
  private final void editTask(){
    String item = (String)this.content.getSelectedValue();
    if ( item == null ) return;
    unitCommand command = new unitCommand(current.unitPath,unitAction.GET_ID,"get task to edit");
    command.set(new Parameter("task",item)).set(new Parameter("target","edit"));
    command.setNeedResponse( true );
    executeCommand( command );
    if ( command.isSuccessful() ) {
      Parameter par = command.getParameter("task");
      Element xml = par.getValue(Tools.emptyXML);
      Tools.xmlSave(xml,"task_toedit.task.xml");
      par = command.getParameter("editor.class");
      if ( par == null ) return;
    }
  }
  /*<refresh> to refresh the data in content's list */
  synchronized void updateContent(TaskPool pool){
    this.contentControl.addOnly();
    Object selected = null;
    if ( this.owner == pool ) selected = this.content.getSelectedValue();
    else {
      if ( (this.owner=pool) != publicPool ) updateAvailable(publicPool);
      else updateAvailable( null );
    }
    this.contentModel.removeAllElements();
    for(Iterator i=pool.tasks.iterator();i.hasNext();){
      this.contentModel.addElement(i.next());
    }
    if ( selected != null ) this.content.setSelectedValue(selected,true);
  }
  private final class proxyTask{
    public final boolean equals(Object o){
      if (o == this) return true;
      try{
        return this.equals((proxyTask)o);
      }catch(ClassCastException e){
        return false;
      }
    }
    public final boolean equals(proxyTask o){
      if ( this.name != null ) return this.name.equals(o.name);
      else
      if ( this.file != null ) return this.file.equals(o.file);
      return false;
    }
    String name=null;
    File file = null;
    proxyTask(String name){this.name=name;}
    proxyTask(File file){this.file=file;}
    public final String getName(){
      if ( name != null) return name;
      else
      if ( file != null) return file.getName();
      else return "???????";
    }
    public final String toString(){
      if ( name != null) return "Task::"+name;
      else
      if ( file != null) return "File::"+file.getName();
      else return "???????";
    }
  }
  synchronized void updateAvailable(TaskPool pool){
    this.availableControl.addOnly();
    Object selected = null;
    if ( this.aOwner == pool ) selected = this.available.getSelectedValue();
    this.availableModel.removeAllElements();
    if ( pool != null ) {
      for(Iterator i=pool.tasks.iterator();i.hasNext();){
        this.availableModel.addElement( new proxyTask((String)i.next()) );
      }
    }
    for(Iterator i=external.iterator();i.hasNext();){
        this.availableModel.addElement( new proxyTask((File)i.next()) );
    }
    if ( selected != null ) this.available.setSelectedValue(selected,true);
  }
}
/**
 * <member>
 * The filename extension for Program
 * */
private final static String EXT = ".task.xml";
/**
 * <member>
 * The filter for Program files
 * */
private static javax.swing.filechooser.FileFilter programmFilter =
new javax.swing.filechooser.FileFilter(){
  public boolean accept(java.io.File file){
    if ( file.isDirectory() ) return true;
    String name = file.getName().toLowerCase();
    return name.endsWith( EXT );
  }
  public String getDescription(){return "VisualCTI tasks files";}
};
/**
 * <member>
 * The dialog for choose the file
 * */
private final static JFileChooser fileChooser = new JFileChooser();
static {fileChooser.addChoosableFileFilter(programmFilter);}
private static File currentDirectory = new File("./");
}
