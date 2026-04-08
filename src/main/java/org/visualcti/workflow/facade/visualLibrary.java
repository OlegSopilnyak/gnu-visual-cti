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
package org.visualcti.workflow.facade;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;
import org.visualcti.workflow.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, Chain's library visualization</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class visualLibrary extends JPanel
{
/**
 * <attribute>
 * The root of the tree
 * */
private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("chains");
/**
 * <attribute>
 * The root of the main chain
 * */
private final DefaultMutableTreeNode main = new DefaultMutableTreeNode("main");
/**
 * <attribute>
 * The root of the local chains tree
 * */
private final DefaultMutableTreeNode local = new DefaultMutableTreeNode("local");
/**
 * <attribute>
 * The root of the external chains tree
 * */
private final DefaultMutableTreeNode external = new DefaultMutableTreeNode("external");
/**
 * <attribute>
 * The tree for represents the Library
 * */
private final JTree tree;
/**
 * <attribute>
 * The model of the tree
 * */
private final DefaultTreeModel model;
/**
 * <visualization>
 * To visualize the Chain in tree
 * */
private final static class ChainCellRenderer extends DefaultTreeCellRenderer{
    public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus) {
      super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
      Object data = ((DefaultMutableTreeNode)value).getUserObject();
      if ( data instanceof Chain ) super.setText( ((Chain)data).getName() );
      return this;
    }
}
private static uiDialog dialog = null;
private static final Object semaphore=new Object();
private final void checkDialog(){
  if ( this.dialog != null) return;
  synchronized( this.semaphore ){
    if ( this.dialog != null) return;
    this.dialog = new uiDialog( this.tree );
  }
}
/**
 * <control>
 * The class for control the libraries Tree
 * */
private final class Control extends controlPanel {
  public Control(){super();
    super.getButton("add").setToolTipText("To add a chain");
    super.getButton("del").setToolTipText("To delete the chain");
    super.getButton("up").setToolTipText("To move up the chain");
    super.getButton("down").setToolTipText("To move down the chain");
    super.getButton("edit").setToolTipText("To edit the chain");
  }
protected final void Add(){ addChain(); }
protected final void Del(){}
protected final void Up(){}
protected final void Down(){}
protected final void Edit(){ selectChain(); }
  public final void noSelection(){ super.disableAll(); }
  public final void mainSelected(){ super.disableAll(); }
  public final void mainRootSelected(){ super.disableAll(); }
  public final void chainSelected(){super.allFeatures();}
  public final void chainRootSelected(){super.addOnly();}
}
/**
 * <mutator>
 * To add the chain
 * */
private final void addChain(){
  if ( this.isLocalChain() ) this.addLocalChain();
}
/**
 * <mutator>
 * To add to library new local chain
 * */
private final void addLocalChain(){
  if ( this.parent == null ) return;
  this.checkDialog();
  //to make the editor
  final JTextField name = new JTextField("local chain",20);
  JPanel editor = new JPanel(new BorderLayout());
  JPanel control = new JPanel(new FlowLayout());
  editor.add(control,BorderLayout.SOUTH);
  editor.setBorder(new TitledBorder("The name"));
//  editor.add(name,BorderLayout.SOUTH);
  JButton iButton = new JButton("import");
  iButton.setToolTipText("To import the chain from the file");
  iButton.setMargin(new Insets(1,1,1,1));
  iButton.addActionListener(new ActionListener(){
    public final void actionPerformed(ActionEvent e){
      name.setEditable( false );
      dialog.getAcceptButton().doClick();
    }
  });
  control.add(name,BorderLayout.SOUTH);
  control.add(iButton,BorderLayout.EAST);
  this.dialog.setTitle("New local chain");
  this.dialog.setEditor(editor);
  this.dialog.setVisible(true);
  String chainName = name.getText();
  if ( !this.dialog.isAccept() || "".equals(chainName) ) return;
  // to make the new
  Chain local = new Chain(this.parent,null,true);
  local.setName( chainName );
  this.lib.add( local );
  DefaultMutableTreeNode node = new DefaultMutableTreeNode(local);
  this.local.add( node );
  this.model.reload( this.local );
  this.tree.setSelectionPath( new TreePath(node.getPath()) );
  this.owner.selectChain(local);
}
/**
 * <mutator>
 * To select the chain for edit the graph
 * */
private final void selectChain(){
  Object data = this.selected().getUserObject();
  if ( data instanceof Chain ) this.owner.selectChain((Chain)data);
}
/**
 * check is selected local or external chain
 * */
private final boolean isLocalChain(){
  DefaultMutableTreeNode selected = this.selected();
  if ( selected == this.local ) return true;
  else
  if ( selected == this.external ) return false;
  else
  if ( selected.getUserObject() instanceof Chain ){
    return selected.getParent() == this.local;
  }
  return false;
}
/**
 * <attribute>
 * The control of lybrary's Tree
 * */
private final Control control = new Control();
/**
 * <attribute>
 * The owner of panel
 * */
private final Facade owner;
  /**
   * <constructor>
   * */
  public visualLibrary(Facade owner)
  {
    super(new BorderLayout(0,0),true);
    TitledBorder border = new TitledBorder("Library");
    border.setTitleColor(UI.titleColor);
    border.setTitleFont(UI.titleFont);
    border.setTitleJustification(TitledBorder.CENTER);
    super.setBorder(border);
    this.owner = owner;
    this.root.add(this.main);
    this.root.add(this.local);
    this.root.add(this.external);
    this.model = new DefaultTreeModel(this.root);
    this.tree = new JTree( this.model );
    this.tree.setCellRenderer( new ChainCellRenderer() );
    TreeSelectionModel selection = this.tree.getSelectionModel();
    selection.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    selection.addTreeSelectionListener(new TreeSelectionListener(){
      public final void valueChanged(TreeSelectionEvent e){
        visualLibrary.this.nodeSelected();
      }
    });

    super.add(new JScrollPane(this.tree), BorderLayout.CENTER);
    super.add(this.control, BorderLayout.SOUTH);
    this.control.noSelection();
  }
  /**
   * <notify>
   * To process Tree's node selection
   * */
  private final void nodeSelected(){
    TreeNode node = this.selected();
    if ( node == this.root ) this.control.noSelection();
    else
    if ( node == this.main ) this.control.mainRootSelected();
    else
    if ( node == this.local || node == this.external ) this.control.chainRootSelected();
    else
    if ( node != null ){
      node = node.getParent();
      if ( node == this.main ) this.control.mainSelected();
      else
      if ( node == this.local || node == this.external ) this.control.chainSelected();
      else this.control.noSelection();
    }
  }
  /**
   * <mutator>
   * To clear all refeernces to The chain
   * */
  public final void clean()
  {
    this.parent=null; this.lib=null;
    this.main.removeAllChildren();
    this.local.removeAllChildren();
    this.external.removeAllChildren();
  }
/**
 * <attribute>
 * */
private ChainsLibrary lib = null;
/**
 * <attribute>
 * The parent for new Chains
 * */
private Chain parent = null;
  /**
   * <mutator>
   * To assign the Chain
   * */
  public final void setChain(Chain chain)
  {
    this.clean();
    this.lib = (this.parent = chain).getLibrary();
    // fill the tree
    this.main.add( new DefaultMutableTreeNode(chain) );
    this.fillLibrary( this.local, this.lib.getLocal() );
    this.fillLibrary( this.external, this.lib.getExternal() );
    this.model.reload();
    this.tree.expandPath( new TreePath(this.root.getPath()) );
    this.tree.expandPath( new TreePath(this.local.getPath()) );
    this.tree.expandPath( new TreePath(this.external.getPath()) );
    this.control.noSelection();
  }
  private final void fillLibrary(DefaultMutableTreeNode node, java.util.List list){
    for(java.util.Iterator i=list.iterator();i.hasNext();){
      node.add( new DefaultMutableTreeNode(i.next()) );
    }
  }
  /**
   * <accessor>
   * To get selected node
   * */
  private final DefaultMutableTreeNode selected(){
    return (DefaultMutableTreeNode)this.tree.getLastSelectedPathComponent();
  }
}
