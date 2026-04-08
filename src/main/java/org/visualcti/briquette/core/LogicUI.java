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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, UI for edit the logic's sequence</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class LogicUI extends JPanel
{
/**
 * <attribute>
 * The root of the tree
 * */
private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
  /**
   * <mutator>
   * To change the root's Label
   * */
  public final void setRootLabel(String label){this.root.setUserObject(label);}
/**
 * <attribute>
 * The tree for represents the Logic
 * */
private final JTree tree;
/**
 * <attribute>
 * The model of the tree
 * */
private final DefaultTreeModel model;
/**
 * <attribute>
 * The selected path in the Tree
 * */
private TreePath selectedPath=null;
  /**
   * <constructor>
   * */
  public LogicUI()
  {
    super(new BorderLayout(0,0),true);
    this.model = new DefaultTreeModel(this.root);
    this.tree = new JTree( this.model );
    TreeSelectionModel selection = this.tree.getSelectionModel();
    selection.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    selection.addTreeSelectionListener(new TreeSelectionListener(){
      public final void valueChanged(TreeSelectionEvent event){
        LogicUI.this.selectedPath = event.getPath();
        Object selected = selectedPath.getLastPathComponent();
        LogicUI.this.nodeSelected( selected );
      }
    });
    JScrollPane scrollPane = new JScrollPane(this.tree);
    /*
    Dimension size = new Dimension(100,55);
    scrollPane.setSize(size);
    scrollPane.setPreferredSize(size);
    */
    super.add(scrollPane, BorderLayout.CENTER);
  }
  /**
   * <accessor>
   * To get access to selected Object
   * */
  private final Object selectedObject()
  {
    TreePath path = this.tree.getSelectionPath();
    if (path == null) return null;
    Object node = path.getLastPathComponent();
    if ( node instanceof DefaultMutableTreeNode ){
      return ((DefaultMutableTreeNode)node).getUserObject();
    }
    return null;
  }
  /**
   * <action>
   * To do when node selected
   * */
  private final void nodeSelected(Object node){
    if ( node instanceof DefaultMutableTreeNode ){
      Object value = ((DefaultMutableTreeNode)node).getUserObject();
      // is part selected
      if (value instanceof Logic.Part){
        partControl.allFeatures();
        control.showPartControl();
        return;
      }
      // is and/or node selected
      if (value instanceof nextNode){
        control.showAndOr();
        return;
      }
      // is string's node selected
      if (value instanceof String){
        // empty root selected
        if ( this.root.getChildCount() == 0 ) {
          partControl.addOnly();
          control.showPartControl();
          return;
        }
      }
    }
    // unknown selection remove all controls
    control.showEmpty();
  }
private static volatile LogicalExpressionDialog dialog=null;
private final static Object SEMAPHORE = new Object();
  /** to check the dialog's integrity */
  private final void checkDialog(){
    if (dialog != null) return;
    synchronized(SEMAPHORE) {
      if (dialog == null) {
        Frame frame = JOptionPane.getFrameForComponent(this);
        dialog = new LogicalExpressionDialog(frame,"",true);
        dialog.setExpressionUI(new LogicalExpressionUI());
        dialog.setEditorTitle("Logic");
      }
    }
  }
  /**
   * <action>
   * To add new Expression will added AND expression
   * */
  private final void addExpression(){
    this.checkDialog();
    LogicUI.dialog.setTitle("Please, create the expression...");
    LogicalExpression expression = new LogicalExpression();
    LogicUI.dialog.getExpressionUI().assign(expression,this.briquette);
    LogicUI.dialog.setVisible(true);
    if ( LogicUI.dialog.isAccepted() && expression.isValid() ) {
      java.util.LinkedList sequence = this.owner.getSequence();
      Logic.Part part = new Logic.Part(expression);
      this.owner.addPart( part );
      this.assign( this.owner, this.briquette );
      TreePath path = (TreePath)this.parts.get( part );
      this.tree.setSelectionPath(path);
    }
  }
  /**
   * <action>
   * To edit the expression
   * */
  private final void editExpression(){
    Object o = this.selectedObject();
    if ( !(o instanceof Logic.Part) ) return;
    Logic.Part part = (Logic.Part)o;
    this.checkDialog();
    LogicalExpression expression = part.expression.copy();
    LogicUI.dialog.setTitle("Please, adjust the expression...");
    LogicUI.dialog.getExpressionUI().assign(expression,this.briquette);
    LogicUI.dialog.setVisible(true);
    if ( LogicUI.dialog.isAccepted() && expression.isValid() ) {
      part.expression = expression;
      this.model.reload( (TreeNode)this.selectedPath.getLastPathComponent() );
    }
  }
  /**
   * <action>
   * To delete Expression
   * */
  private final void delExpression(){
    Object o = this.selectedObject();
    if ( !(o instanceof Logic.Part) ) return;
    Logic.Part toDelete = (Logic.Part)o;
    Logic.Part part = this.owner.delPart(toDelete);
    this.assign(this.owner,this.briquette);
    TreePath to_select = null;
    if (part == null) {
      to_select = new TreePath( this.root.getPath() );
    }else {
      to_select = (TreePath)this.parts.get(part);
    }
    this.tree.setSelectionPath( to_select );
  }
  /**
   * <action>
   * To move up the expression
   * */
  private final void upExpression(){
    Object o = this.selectedObject();
    if ( !(o instanceof Logic.Part) ) return;
    Logic.Part toMove = (Logic.Part)o;
    if ( !this.owner.moveUpPart( toMove ) ) return;
    this.assign(this.owner,this.briquette);
    this.tree.setSelectionPath( (TreePath)this.parts.get(toMove) );
  }
  /**
   * <action>
   * To move down the expression
   * */
  private final void downExpression(){
    Object o = this.selectedObject();
    if ( !(o instanceof Logic.Part) ) return;
    Logic.Part toMove = (Logic.Part)o;
    if ( !this.owner.moveDownPart( toMove ) ) return;
    this.assign(this.owner,this.briquette);
    this.tree.setSelectionPath( (TreePath)this.parts.get(toMove) );
  }
  /**
   * <action>
   * To set AND condition
   * */
  private final void setAnd(){
    Object o = this.selectedObject();
    if ( !(o instanceof nextNode) ) return;
    ((nextNode)o).getOwner().setNextAND();
    this.model.reload( (TreeNode)this.selectedPath.getLastPathComponent() );
  }
  /**
   * <action>
   * To set OR condition
   * */
  private final void setOr(){
    Object o = this.selectedObject();
    if ( !(o instanceof nextNode) ) return;
    ((nextNode)o).getOwner().setNextOR();
    this.model.reload( (TreeNode)this.selectedPath.getLastPathComponent() );
  }
/**
 * <attribute>
 * The control of UI
 * */
private final Control control = new Control();
  /**
   * <accessor>
   * To get access to UI's control as JPanel
   * */
  public final JPanel getControlPanel(){return this.control;}
/**
 * <control>
 * The class for control the expression
 * */
private final class expressionControl extends controlPanel {
  public expressionControl(){super();
    super.getButton("add").setToolTipText("To create an expression");
    super.getButton("del").setToolTipText("To delete an expression");
    super.getButton("up").setToolTipText("To move Up the expression");
    super.getButton("down").setToolTipText("To move Down the expression");
    super.getButton("edit").setToolTipText("To edit the expression");
  }
protected final void Add(){addExpression();}
protected final void Del(){delExpression();}
protected final void Up(){upExpression();}
protected final void Down(){downExpression();}
protected final void Edit(){editExpression();}
}
/**
 * <attribute>
 * The control for Logic.Part
 * */
private expressionControl partControl;
/**
 * <control>
 * The class for control the Logic
 * */
private final class Control extends JPanel
{
  private final CardLayout layout = new CardLayout(0,0);
  Control(){super(true);
    super.setLayout(this.layout);
    super.add("EMPTY",new JLabel());
    super.add("PART",partControl = new expressionControl());
    JPanel and_or_Panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
    super.add("ANDOR",and_or_Panel);
    Insets insets = new Insets(1,1,1,1);
    JButton add = new JButton("+");add.setMargin(insets);
    and_or_Panel.add(add);
    add.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){LogicUI.this.addExpression();}
    });
    JButton and = new JButton("AND");and.setMargin(insets);
    and_or_Panel.add(and);
    and.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){LogicUI.this.setAnd();}
    });
    JButton or = new JButton("OR");or.setMargin(insets);
    and_or_Panel.add(or);
    or.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){LogicUI.this.setOr();}
    });
  }
  /** to show empty panel */
  public final void showEmpty(){this.layout.show(this,"EMPTY");}
  /** to show part control */
  public final void showPartControl(){this.layout.show(this,"PART");}
  /** to show or/and control */
  public final void showAndOr(){this.layout.show(this,"ANDOR");}
}
/**
 * <attribute>
 * The owner of UI
 * */
private Logic owner=null;
/**
 * <flag>
 * The assign in progress
 * */
private volatile boolean isAssign = false;
/**
 * <attribute>
 * Owner of Logic
 * */
private Operation briquette = null;
  /**
   * <accessor>
   * To get access to the owner of Logic
   * */
  public Operation getBriquette() {return this.briquette;}
/**
 * <pool>
 * The pools of parts and TreePath to it
 * */
private final java.util.HashMap parts = new java.util.HashMap();
  /**
   * <assign>
   * To assign the Logic to UI
   * */
  public final void assign(Logic owner, Operation oper)
  {
    this.isAssign = true;
    this.owner=owner;
    this.briquette=oper;
    this.parts.clear();
    this.root.removeAllChildren();
    java.util.Iterator seq = this.owner.getSequence().iterator();
    if ( !seq.hasNext() ) {this.model.reload(this.root);return;}
    // first part
    Logic.Part part = (Logic.Part)seq.next();
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(part);
    this.root.add( node );
    this.parts.put( part, new TreePath(node.getPath()) );
    // other parts
    while( seq.hasNext() )
    {
      Logic.Part next = (Logic.Part)seq.next();
      this.add(part,next);
      // to switch current to the next entry
      part = next;
    }
    this.model.reload( this.root );
    this.isAssign = false;
  }
  /** to add Part with it parent */
  private final void add(Logic.Part parent,Logic.Part part){
    DefaultMutableTreeNode node = new DefaultMutableTreeNode( new nextNode(parent) );
    DefaultMutableTreeNode partNode = new DefaultMutableTreeNode(part);
    node.add( partNode ); this.root.add( node );
    TreePath path = new TreePath( partNode.getPath() );
    // store part to the pool
    this.parts.put( part, path );
  }
  private final static class nextNode {
    Logic.Part owner;
    public final Logic.Part getOwner(){return this.owner;}
    public nextNode(Logic.Part owner){this.owner=owner;}
    public final String toString(){
      return owner.isNextAND() ? "AND": (owner.isNextOR() ? "OR":"???");
    }
  }

}
