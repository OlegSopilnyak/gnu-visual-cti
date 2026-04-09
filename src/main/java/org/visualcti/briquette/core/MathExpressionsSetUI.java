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
package org.visualcti.briquette.core;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import org.visualcti.briquette.*;

import java.util.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The components for edit maths set </p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class MathExpressionsSetUI
{
/**
 * <attribute>
 * The maths set to edit
 */
private MathExpressionsSet value=null;
/**
 * <attribute>
 * The owner of the set
 */
private Operation owner=null;
/**
 * <attribute>
 * The root node of the tree
 */
private final DefaultMutableTreeNode root=new DefaultMutableTreeNode("Math");
  /**
   * */
  /**
   * <mutator>
   * To change the root's Label
   * @param label new name of the root Node
   */
  public final void setRootLabel(String label){this.root.setUserObject(label);}
/**
 * <attribute>
 * The model of the sequence's Tree
 */
private final DefaultTreeModel model=new DefaultTreeModel( root );
/**
 * <attribute>
 * The tree for represents the Sequence
 * */
private final JTree tree = new JTree( model );
  /**
   * <accessor>
   * To get access to UI's tree
   * @return the tree-component
   */
  public final JTree getTree(){return this.tree;}
/**
 * <attribute>
 * The selected path in the Tree
 * */
private TreePath selectedPath=null;
    /**
     * To get access to selected expression's object
     * @return expression or null
     */
    private MathExpression selectedExpression(){
      if ( this.selectedPath == null ) return null;
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.selectedPath.getLastPathComponent();
      if ( node == null ) return null;
      try{
        return (MathExpression)node.getUserObject();
      }catch(ClassCastException e){
        return null;
      }
    }
/**
 * <attribute>
 * The controls panel of the UI
 */
private final expressionControl control=new expressionControl();
  /**
   * <accessor>
   * To get access to UI's control as JPanel
   * @return the panel
   */
  public final JPanel getControlPanel(){return this.control;}
  /**
   * <contructor>
   * To make the UI
   */
  public MathExpressionsSetUI()
  {
    this.tree.setCellRenderer( new CellRenderer() );
    TreeSelectionModel selection = this.tree.getSelectionModel();
    selection.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    selection.addTreeSelectionListener(new TreeSelectionListener(){
      public final void valueChanged(TreeSelectionEvent event){
        selectedPath = event.getPath();
        nodeSelected( selectedPath.getLastPathComponent() );
      }
    });
  }
    /**
     * <notify>
     * To do when node selected
     * @param node selected tree's node
     */
    private final void nodeSelected(Object node){
      if ( node instanceof DefaultMutableTreeNode ){
        Object value = ((DefaultMutableTreeNode)node).getUserObject();
        if ( value instanceof MathExpression ) this.control.allFeatures();
        else this.control.addOnly();
      }
    }
  /**
   * <mutator>
   * To assign the values to edit
   * @param value the maths set
   * @param owner the owner of the set
   */
  public final void assign(MathExpressionsSet value,Operation owner)
  {
    this.value=value; this.owner=owner; this.selectedPath=null;
    this.rebuildTree(); this.control.addOnly();
  }
/**
 * <pool>
 * The pools of indexes of expressions
 * */
private final java.util.HashMap indexes = new java.util.HashMap();
      /**
       * <refresh>
       * To rebuild the Tree's model
       */
      private final void rebuildTree(){
        this.indexes.clear(); this.root.removeAllChildren();int index=0;
        for(Iterator i=this.value.getSequence().iterator();i.hasNext();index++){
          this.add( (MathExpression)i.next(), index );
        }
        this.model.reload( this.root );
        this.control.addOnly();
      }
      private final void selectExpression(MathExpression expression){
        if ( expression == null ) return;
        int childCount=this.root.getChildCount();
        for(int i=0;i < childCount;i++){
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.root.getChildAt(i);
          if ( expression.equals(node.getUserObject()) ){
            node=node.getFirstLeaf();
            TreePath path = new TreePath(node.getPath());
            this.tree.setSelectionPath( path );
            //this.tree.expandPath( path );
          }
        }
      }
      /**
       *
       * To add expression's node to tree
       * @param item expression to add
       * @param index the index in sequence
       * @return the path to expression's tree node
       */
      private final TreePath add(MathExpression item,int index){
        DefaultMutableTreeNode itemNode=new DefaultMutableTreeNode(item);
        DefaultMutableTreeNode itemNodeExp=new DefaultMutableTreeNode(item);
        itemNode.add( itemNodeExp );
        this.root.add( itemNode );
        TreePath itemPath = new TreePath( itemNode.getPath() );
        TreePath itemPathExt = new TreePath( itemNodeExp.getPath() );
        this.indexes.put(itemPath, new Integer(index) );
        this.indexes.put(itemPathExt, new Integer(index) );
        return itemPathExt;
      }
/**
 * <dialog>
 * For add/ edit the expression
 */
private static volatile MathExpressionDialog dialog=null;
    /** to check the dialog's integrity */
    private final void checkDialog(){
      if (dialog != null) return;
      synchronized(MathExpressionsSetUI.class) {
        if (dialog == null) {
          Frame frame = JOptionPane.getFrameForComponent(this.control);
          dialog = new MathExpressionDialog(frame,"",true);
          dialog.setMathExpressionUI( new MathExpressionUI() );
          dialog.setEditorTitle( this.getEditorTitle() );
        }
      }
    }
  /**
   * <accessor>
   * To get access to title in dialog
   * @return the title
   */
  protected String getEditorTitle(){return "Math";}
    /**
     * <action>
     * To add new Expression will added AND expression
     * */
    private final void addExpression(){
      this.checkDialog();
      dialog.setTitle("Please, create the expression...");
      MathExpression expression = this.value.newMathExpression();
      dialog.getMathExpressionUI().assign(expression,this.owner);
      dialog.setVisible(true);
      int index = this.value.getSequence().size();
      if ( dialog.isAccepted() && expression.isValid() ) {
        TreePath path = this.add( this.value.Add(expression), index );
        this.model.reload(this.root);
        this.selectExpression( expression  );
      }
    }
    /**
     * <action>
     * To delete selected Expression
     * */
    private final void delExpression(){
      if ( this.selectedPath == null ) return;
      Integer index = (Integer)this.indexes.get(this.selectedPath);
      if ( index != null && this.value.Delete( index.intValue() ) ){
        this.rebuildTree();this.selectedPath=null;
      }
    }
    /**
     * <action>
     * To move up selected Expression
     * */
    private final void upExpression(){
      if ( this.selectedPath == null ) return;
      Integer index = (Integer)this.indexes.get(this.selectedPath);
      if ( index != null && this.value.moveUp( index.intValue() ) ){
        MathExpression expression = this.selectedExpression();
        this.rebuildTree();this.selectExpression(expression);
      }
    }
    /**
     * <action>
     * To move down selected Expression
     * */
    private final void downExpression(){
      if ( this.selectedPath == null ) return;
      Integer index = (Integer)this.indexes.get(this.selectedPath);
      if ( index != null && this.value.moveDown( index.intValue() ) ){
        MathExpression expression = this.selectedExpression();
        this.rebuildTree();this.selectExpression(expression);
      }
    }
    /**
     * <action>
     * To edit selected Expression
     * */
    private final void editExpression(){
      if ( this.selectedPath == null ) return;
      MathExpression expression = this.selectedExpression();
      if (expression == null) return;
      Integer index = (Integer)this.indexes.get(this.selectedPath);
      if ( index == null ) return;
      this.checkDialog();
      dialog.setTitle("Please, edit the expression...");
      expression = expression.copy();
      dialog.getMathExpressionUI().assign(expression,this.owner);
      dialog.setVisible(true);
      if ( dialog.isAccepted() && expression.isValid() ) {
        this.value.Update(expression,index.intValue());
        this.rebuildTree();this.selectExpression(expression);
      }
    }
/***
 * <renderer>
 * Class for visualize the MathExpression
 */
private final static class CellRenderer extends DefaultTreeCellRenderer{
  public final Component getTreeCellRendererComponent(JTree tree, Object value,
                                 boolean selected, boolean expanded,
                                 boolean leaf, int row, boolean hasFocus){
    super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
    Object data = ((DefaultMutableTreeNode)value).getUserObject();
    if ( data instanceof MathExpression ){
      MathExpression item=(MathExpression)data;
      String text = leaf ? item.expString():item.getTarget().cell()+"=";
      super.setText( text );
    }
    return this;
  }
}
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
}
