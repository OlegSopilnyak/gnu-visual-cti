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
package org.visualcti.workflow.facade;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import org.visualcti.briquette.*;
import org.visualcti.workflow.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the panel for represents Values</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class visualValues extends JPanel implements Pool.Listener
{
/**
 * <attribute>
 * The root of the tree
 * */
private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Watch");
/**
 * <attribute>
 * The root of the user's values tree
 * */
private final DefaultMutableTreeNode local = new DefaultMutableTreeNode("user");
/**
 * <attribute>
 * The root of the datbase's values tree
 * */
private final DefaultMutableTreeNode database = new DefaultMutableTreeNode("database");
/**
 * <attribute>
 * The root of the dbcolumn's values tree
 * */
private final DefaultMutableTreeNode dbcolumn = new DefaultMutableTreeNode("columns");
/**
 * <attribute>
 * The root of the dbcolumn's values tree
 * */
private final DefaultMutableTreeNode system = new DefaultMutableTreeNode("system");
/**
 * <attribute>
 * The tree for represents the Values
 * */
private final JTree tree;
/**
 * <attribute>
 * The model of the tree
 * */
private final DefaultTreeModel model;
/**
 * <attribute>
 * The facade of IDE
 */
private final Facade facade;
  /**
   * <constructor>
   * */
  public visualValues(Facade facade)
  {
    super(new BorderLayout(0,0),true);
    this.facade=facade;
    this.root.add(this.system);
    this.root.add(this.database);
    this.root.add(this.local);
    this.root.add(this.dbcolumn);
    this.model = new DefaultTreeModel(this.root);
    this.tree = new JTree( this.model );
    TreeSelectionModel selection = this.tree.getSelectionModel();
    selection.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    super.add(new JScrollPane(this.tree), BorderLayout.CENTER);
  }
  /**
   * <mutator>
   * To clear the view & free all references
   * */
  public final void clear()
  {
    this.data = null;
    this.clearTree();
  }
  /**
   * <mutator>
   * To clear the tree
   * */
  private final void clearTree(){
    this.paths.clear();
    this.system.removeAllChildren();
    this.database.removeAllChildren();
    this.local.removeAllChildren();
    this.dbcolumn.removeAllChildren();
    this.model.reload(this.root);
    this.tree.expandPath(new TreePath(this.root.getPath()));
  }
/**
 * <data>
 * The source of the data
 * */
private Pool data = null;
/**
 * <pool>
 * The pool of the paths
 * */
private final java.util.HashMap paths = new java.util.HashMap();
  /**
   * <notify>
   * Notified, when value in pool is changed
   * */
  public final synchronized void poolChanged(Pool.Event event)
  {
    synchronized( this.paths )
    {
      switch( event.getID() )
      {
        // pool claered
        case Pool.Event.CLEAR:
          {
            int groupID = event.getGroup();
            if ( groupID == Symbol.ALL) this.clearTree();
            else
            {
              DefaultMutableTreeNode gRoot = this.rootFor(groupID);
              if ( gRoot == null) break;
              // to remove the paths
              for(Enumeration e=gRoot.children();e.hasMoreElements();){
                DefaultMutableTreeNode sNode=(DefaultMutableTreeNode)e.nextElement();
                Node node = (Node)sNode.getUserObject();
                this.paths.remove( node.symbol );
              }
              gRoot.removeAllChildren();
              this.model.reload( gRoot );
            }
          }
          break;
        // pool's symbol modified
        case Pool.Event.MODIFY:
          {
            Symbol symbol = event.getWho();
            if (symbol == null) break;
            TreePath path = (TreePath)this.paths.get(symbol);
            Node node = new Node( symbol );
            DefaultMutableTreeNode sNode;
            if ( path == null ){
              if ( node.value == null ) break;
              DefaultMutableTreeNode gRoot = this.rootFor(symbol);
              sNode = new DefaultMutableTreeNode(node);
              gRoot.add( sNode );
              path = new TreePath( sNode.getPath() );
              this.paths.put( symbol, path );
              this.model.reload( gRoot );
            }else {
              sNode = (DefaultMutableTreeNode)path.getLastPathComponent();
              //node = (Node)sNode.getUserObject();
              if ( node.value != null ) {
                sNode.setUserObject( node );
                this.model.reload( sNode );
              }else {
                this.paths.remove( node.symbol );
                this.model.removeNodeFromParent(sNode);
              }
            }
          }
          break;
      }
    }
  }
/**
 * <visual>
 * Class for visualize the value
 * */
private final class Node{
  Symbol symbol; Object value;
  Node(Symbol symbol){this.symbol=symbol;this.value=data.get(symbol);}
  public final String toString(){return symbol.cell()+" = "+value;}
  public final int hashCode(){return this.symbol.hashCode();}
  public final boolean equals(Object o){
    if (o instanceof Node){
      return ((Node)o).symbol.equals(this.symbol);
    }
    return false;
  }
}
  /**
   * <mutator>
   * To assign new Pool to the Values's view
   * */
  public final void assign(Pool data)
  {
    // to clear old visuals
    this.paths.clear();
    if (this.data != null)  this.data.removePoolListener( this );
    java.util.HashMap roots = new java.util.HashMap();
    // to store collapse/expand flags
    this.fillExpand( roots );
    this.database.removeAllChildren();
    this.dbcolumn.removeAllChildren();
    this.local.removeAllChildren();
    this.system.removeAllChildren();
    // to store new data source
    this.data = data;
    // to register the listener
    this.data.addPoolListener( this );
    // to make paths
    synchronized( this.paths )
    {
      this.paths.clear();
      for(java.util.Iterator i = data.symbolsAll().iterator();i.hasNext();)
      {
        Symbol symbol = (Symbol)i.next();
        DefaultMutableTreeNode sRoot = this.rootFor(symbol);
        if ( sRoot == null ) continue;
        Node node = new Node( symbol );
        DefaultMutableTreeNode sNode = new DefaultMutableTreeNode(node);
        sRoot.add(sNode);
        TreePath pathTo = new TreePath( sNode.getPath() );
        this.paths.put(symbol,pathTo);
      }
    }
    // to reload tree's model
    this.model.reload( this.root );
    // to restore collapse/expand flags
    for(java.util.Iterator i= roots.keySet().iterator();i.hasNext();)
    {
      TreePath path = (TreePath)i.next();
      if ( ((Boolean)roots.get(path)).booleanValue() )
      {
        this.tree.expandPath( path );
      }else{
        this.tree.collapsePath( path );
      }
    }
  }
  private final void fillExpand(java.util.HashMap map){
    Boolean expanded;
    TreePath
    path = new TreePath(this.database.getPath());
    expanded = new Boolean( this.tree.isExpanded(path) );
    map.put(path,expanded);
    path = new TreePath(this.dbcolumn.getPath());
    expanded = new Boolean( this.tree.isExpanded(path) );
    map.put(path,expanded);
    path = new TreePath(this.local.getPath());
    expanded = new Boolean( this.tree.isExpanded(path) );
    map.put(path,expanded);
    path = new TreePath(this.system.getPath());
    expanded = new Boolean( this.tree.isExpanded(path) );
    map.put(path,expanded);
  }
  private final DefaultMutableTreeNode rootFor(int groupID){
    switch( groupID ){
      case Symbol.USER:
        return this.local;
      case Symbol.DATABASE:
        return this.database;
      case Symbol.DBCOLUMN:
        return this.dbcolumn;
      case Symbol.SYSTEM:
        return this.system;
      default:
        return null;
    }
  }
  private final DefaultMutableTreeNode rootFor(Symbol symbol){
    return this.rootFor( symbol.getGroupID() );
  }
}
