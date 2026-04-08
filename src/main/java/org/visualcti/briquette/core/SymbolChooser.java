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
package org.visualcti.briquette.core;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.event.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow Dialog for choose the Symbol</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class SymbolChooser extends JDialog {
/**
 * <producer>
 * To get one instance of the Dialog
 * */
public static final SymbolChooser getInstance(Component component)
{
  if (SymbolChooser.dialog != null) return SymbolChooser.dialog;
  synchronized(TITLE){
    if (SymbolChooser.dialog == null) {
      Frame frame = JOptionPane.getFrameForComponent(component);
      SymbolChooser.dialog = new SymbolChooser(frame);
    }
  }
  return SymbolChooser.dialog;
}
private static SymbolChooser dialog = null;
  /**
   * overrided method for register ESCAPE key
   * */
 protected final JRootPane createRootPane()
 {
  JRootPane rootPane = super.createRootPane();
  KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
  ActionListener hide = new ActionListener() {
    public final void actionPerformed(ActionEvent e){setVisible(false);}
  };
  rootPane.registerKeyboardAction(hide, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
  return rootPane;
 }
/**
 * <const>
 * The title of the Dialog
 * */
public final static String TITLE = "Choose the Symbol";
/**
 * <attribute>
 * The selected Symbol
 * */
private Symbol choosed = null;
  /**
   * <accessor>
   * To get access to choosed Symbol
   * */
  public Symbol getChoosed() {
    return choosed;
  }
  /**
   * <accessor>
   * To setup the choosed Symbol
   * */
  public final void setChoosed(Symbol choosed) {this.choosed = choosed;}
/**
 * <attribute>
 * The root's Node of the tree
 * */
private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("symbols");
/**
 * <attribute>
 * The tree of symbols
 * */
private final JTree tree;
/**
 * <attribute>
 * The model of the tree
 * */
private final DefaultTreeModel model;
  /**
   * <Constructor>
   * */
  private SymbolChooser(Frame frame) {
    super(frame, TITLE, true);
    JPanel content = new JPanel(new BorderLayout());
    JLabel title = new JLabel(" Symbols set...",JLabel.LEFT);
    title.setForeground(UI.titleColor);
    title.setFont(UI.titleFont);
    content.add(title,BorderLayout.NORTH);
    super.getContentPane().add(content);
    this.model = new DefaultTreeModel(this.root);
    tree = new JTree( this.model );
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles( true );
    tree.setCellRenderer( new SymbolCellRenderer() );
    JScrollPane scroll = new JScrollPane(tree);
    scroll.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    content.add(scroll,BorderLayout.CENTER);
    JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    control.setBackground(Color.gray);
    control.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
    content.add(control,BorderLayout.SOUTH);
    JButton ok = new JButton("OK");
    JButton cancel = new JButton("Cancel");
    control.add(ok);control.add(cancel);

    tree.setNextFocusableComponent( ok );
    ok.setNextFocusableComponent(cancel);
    cancel.setNextFocusableComponent(tree);
    super.setSize(300,350); super.setLocation(100,100);
    super.setResizable(false);
    // to make the stroke for register KeyBoard's Action
    KeyStroke Enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    // action by Ok
    ActionListener okAction = new ActionListener(){
      public final void actionPerformed(ActionEvent e){saveSelectedSymbol();}
    };
    ok.registerKeyboardAction(okAction, Enter, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ok.addActionListener(okAction);
    // action by Cancel
    ActionListener cancelAction = new ActionListener(){
      public final void actionPerformed(ActionEvent e){setVisible( false );}
    };
    cancel.registerKeyboardAction(cancelAction, Enter, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    cancel.addActionListener(cancelAction);
    // listener of cell clicks
    MouseListener ml = new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
           int selRow = tree.getRowForLocation(e.getX(), e.getY());
           TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
           if(selRow != -1 && e.getClickCount() == 2) {
              Object last = selPath.getLastPathComponent();
              Object data = ((DefaultMutableTreeNode)last).getUserObject();
              if (data instanceof Symbol) saveSelectedSymbol();
           }
       }
    };
    this.tree.addMouseListener(ml);
    this.tree.registerKeyboardAction(okAction, Enter, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }
  /**
   * <visualization>
   * Overrided method
   * */
  public final void setVisible(boolean flag)
  {
    if (flag) {
      this.selectChoosed(this.choosed);
      this.tree.requestFocus();
      this.choosed=null;
    }
    super.setVisible(flag);
  }
  /**
   * <saver>
   * To save selected Symbol and close dialog
   * */
  private final void saveSelectedSymbol()
  {
    boolean selected = true;
    DefaultMutableTreeNode node = this.selected();
    if (node == null) return;
    Object data = node.getUserObject();
    selected = data instanceof Symbol;
    if (selected) {
      this.choosed = (Symbol)data;
      super.setVisible( false );
    }
  }
private final java.util.List symbols = new ArrayList();
private final Object symSemaphore = new Object();
  /**
   * To get selected node
   * */
  private final DefaultMutableTreeNode selected()
  {
    return (DefaultMutableTreeNode)this.tree.getLastSelectedPathComponent();
  }
  /**
   * <select>
   * To select choosed symbol
   * */
  private final void selectChoosed(Symbol choosed){
    if (choosed == null) return;
    DefaultMutableTreeNode entry = this.getEntry(choosed),node;
    if (entry == null) return;
    for(Enumeration e = entry.children();e.hasMoreElements();)
    {
      node=(DefaultMutableTreeNode)e.nextElement();
      if ( node.getUserObject().equals(choosed) ) {
        TreePath path = new TreePath(node.getPath());
        this.tree.setSelectionPath(path);
        break;
      }
    }
  }
  /**
   * <accessor>
   * To get access to root of symbol by type
   * */
  private final DefaultMutableTreeNode getEntry(Symbol symbol)
  {
    String key = "??";
    if (choosed.isConst()) key = CONST;
    else {
      switch(choosed.getGroupID()) {
        case Symbol.USER:
          key = USER;
          break;

        case Symbol.DATABASE:
          key = DATABASE;
          break;

        case Symbol.DBCOLUMN:
          key = DBCOLUMN;
          DefaultMutableTreeNode root=(DefaultMutableTreeNode)this.roots.get(key);
          if ( root == null ) return null;
          String table = null;
          try{
            table = new StringTokenizer(symbol.getName(),".").nextToken();
          }catch(NoSuchElementException e){
            return null;
          }
          int count=root.getChildCount();
          for(int i=0;i < count;i++){
            DefaultMutableTreeNode node=(DefaultMutableTreeNode)root.getChildAt(i);
            if ( table.equals(node.getUserObject()) ) return node;
          }
          return null;

        case Symbol.SYSTEM:
          key = SYSTEM;
          break;
      }
    }
    return (DefaultMutableTreeNode)this.roots.get(key);
  }
  /**
   * <mutator>
   * To assign available Symbols
   * */
  public final void setSymbols(java.util.List symbols)
  {
    synchronized(this.symSemaphore)
    {
      this.symbols.clear();
      this.symbols.addAll( symbols );
    }
    this.prepare();
  }
  private final void prepareGroup(int group){this.prepareGroupType(group,Symbol.ANY);}
  private final void prepareType(int type){this.prepareGroupType(Symbol.ANY,type);}
  private final void prepareGroupType(int group,int type)
  {
    this.groupFlag=group; this.typeFlag=type;
    this.prepare();
  }
  private final void prepareAll()
  {
    this.groupFlag=this.typeFlag=Symbol.ANY;
    this.prepare();
  }
private volatile int groupFlag = Symbol.ANY;
private volatile int typeFlag = Symbol.ANY;
  private final boolean isCanAdd(Symbol symbol)
  {
    if (
        this.groupFlag != Symbol.ANY &&
        this.groupFlag != symbol.getGroupID()
        ) return false;
    if (this.typeFlag == Symbol.ANY) return true;
    int symbolType = symbol.getTypeID();
    if (this.typeFlag == Symbol.STRING && symbolType == Symbol.NUMBER) return true;
    return this.typeFlag == symbolType;
  }
/**
 * <pool>
 * The pool of roots
 * */
private final HashMap roots = new HashMap( 5 );
/**
 * <const>
 * The name of CONST node
 * */
private final static String CONST = "const";
/**
 * <const>
 * The name of USER node
 * */
private final static String USER = "local";
/**
 * <const>
 * The name of DATABASE node
 * */
private final static String DATABASE = "db properties";
/**
 * <const>
 * The name of DBCOLUMN node
 * */
private final static String DBCOLUMN = "db content";
/**
 * <const>
 * The name of SYSTEM node
 * */
private final static String SYSTEM = "system";
  private final void prepare()
  {
    this.root.removeAllChildren();
    this.roots.clear();
    DefaultMutableTreeNode consts = new DefaultMutableTreeNode(CONST);
    DefaultMutableTreeNode locals = new DefaultMutableTreeNode(USER);
    DefaultMutableTreeNode dbs = new DefaultMutableTreeNode(DATABASE);
    DefaultMutableTreeNode dbcols = new DefaultMutableTreeNode(DBCOLUMN);
    DefaultMutableTreeNode systems = new DefaultMutableTreeNode(SYSTEM);
    for(Iterator i=symbols.iterator();i.hasNext();)
    {
      Symbol symbol = (Symbol)i.next();
      if (symbol == null) continue;
      // to solve the symbol
      if (symbol.isConst()){this.add(consts,symbol);continue;}
      // to solve not constant's symbol
      switch( symbol.getGroupID() )
      {
        case Symbol.USER:
          this.add(locals,symbol);
          break;
        case Symbol.DATABASE:
          this.add(dbs,symbol);
          break;
        case Symbol.DBCOLUMN:
          this.addDbColumn(dbcols,symbol);
          break;
        case Symbol.SYSTEM:
          this.add(systems,symbol);
          break;
      }
    }
    this.add(consts);
    this.add(locals);
    this.add(dbs);
    this.add(dbcols);
    this.add(systems);
    this.model.reload(this.root);
  }
  private final void add(DefaultMutableTreeNode root, Symbol symbol){
    if ( this.isCanAdd(symbol) ){
      root.add(new DefaultMutableTreeNode(symbol));
    }
  }
  private final void addDbColumn(DefaultMutableTreeNode root,Symbol column)
  {
    String table=new StringTokenizer(column.getName(),".").nextToken();
    int count = root.getChildCount();DefaultMutableTreeNode node=null;
    for(int i=0;i < count;i++){
      node = (DefaultMutableTreeNode)root.getChildAt(i);
      if ( table.equals(node.getUserObject()) ) break;
      else node=null;
    }
    if (node==null) root.add( node=new DefaultMutableTreeNode( table ) );
    this.add(node,column);
  }
  private final void add(DefaultMutableTreeNode symbols){
    if (symbols.getChildCount() > 0) {
      this.roots.put(symbols.getUserObject(),symbols);
      this.root.add(symbols);
    }
  }
}
