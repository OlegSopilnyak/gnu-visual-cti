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
package org.visualcti.workflow;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: WorkFlow, the Panel of the chain's related vierws</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public final class centralControl extends JSplitPane
{
/**
<attribute>
Ownre of this panel
*/
private final IDE ide;
/**
 * <attribute>
 * The facade of IDE
 * */
private final Facade facade;
/**
 * <attribute>
 * The array of wrappers
 * */
private final java.util.ArrayList wrappers = new java.util.ArrayList();
/**
 * <attribute>
 * The container of graphs
 * */
private final JTabbedPane graphs = new JTabbedPane(JTabbedPane.TOP);
/**
 * <listener>
 * The listener of tabs
 * */
private final ChangeListener graphsListener = new ChangeListener(){
  public final void stateChanged(ChangeEvent e){
    int index = graphs.getSelectedIndex();
    if ( index == -1 ) return;// unknown selection
    synchronized( wrappers ){
      if ( selectedWrapper != null) selectedWrapper.deactivate();
      selectedWrapper = (ChainWrapper)wrappers.get(index);
      selectedWrapper.activate();
    }
  }
};
    /**
    <constructor>
    */
    public centralControl(IDE ide,Facade facade)
    {
        super( JSplitPane.HORIZONTAL_SPLIT, false );
        this.ide=ide; this.facade = facade;
        super.setOneTouchExpandable(true);
        // to adjust the selection listener
        this.graphs.getModel().addChangeListener(this.graphsListener);

        super.setLeftComponent( this.graphs );
        super.setRightComponent( this.right() );

        int pos = ide.getSize().width * 8 / 10;
        super.setDividerLocation( pos );
    }
    final void setSelectedTitle(String title)
    {
      int index = this.graphs.getSelectedIndex();
      if (index != -1) this.graphs.setTitleAt(index,title);
    }
/**
 * <attribute>
 * Last selected wrapper
 * */
private transient ChainWrapper selectedWrapper = null;
    /**
     * <accessor>
     * To get access to current Chain's graph
     * */
    final visualChain getGraph(){
      synchronized( this.wrappers ){
        try{
          return this.selectedWrapper.getGraph();
        }catch(NullPointerException e){}
        return null;
      }
    }
    /**
     * <clean>
     * To clean all Components
     * */
    public final void clean()
    {
      synchronized ( this.wrappers )
      {
        if (this.selectedWrapper != null) this.selectedWrapper.deactivate();
        this.selectedWrapper = null;
        this.graphs.removeAll();
        this.wrappers.clear();
      }
    }
    /**
     * <action>
     * To select the Chain for edit the graph
     * */
    final void select(Chain chain)
    {
      synchronized( this.wrappers )
      {
        int tab = this.findWrapper( chain );
        this.graphs.setSelectedIndex( tab );
      }
    }
    /**
     * To find the wrapper for the chain
     * */
    private final int findWrapper(Chain chain){
      ChainWrapper wrapper = null;int tabIndex = 0;
      for(java.util.Iterator i=this.wrappers.iterator();i.hasNext();){
        wrapper = (ChainWrapper)i.next();
        if ( wrapper.getChain() == chain ) return tabIndex;
        tabIndex++;
      }
      // to make the wrapper
      wrapper = new ChainWrapper( chain, this.facade );
      wrapper.setCloseEnabled( this.wrappers.size() > 0 );
      // to place it at new Tab panel
      this.wrappers.add( wrapper );
      // to store the wrapper to pool
      this.graphs.add( chain.getName(), wrapper );
      return tabIndex;
    }
    /**
     * <action>
     * To close the graph and detach the wrapper
     * */
    final void close( Chain chain )
    {
      int index = this.graphs.getSelectedIndex();
      synchronized( this.wrappers )
      {
        // to find the wrapper & remove it from the wrappers pool
        ChainWrapper wrapper = null;
        for(java.util.ListIterator i=this.wrappers.listIterator();i.hasNext();)
        {
          wrapper = (ChainWrapper)i.next();
          if ( wrapper.getChain() == chain ){i.remove(); break;
          }else wrapper = null;
        }
        // to deactivate current
        if ( wrapper != null && selectedWrapper == wrapper)
        {
          selectedWrapper.deactivate(); selectedWrapper = null;
        }
        // to remove graph from tabbed pane
        if ( wrapper != null ) this.graphs.remove( wrapper );
        // Call the Garbage Collector
        System.gc();
      }
    }
    /**
     * <producer>
     * To make right tabbed pane and fill facade's parts
     * */
    private final JComponent right()
    {
        JTabbedPane pane = this.facade.control();
        JPanel
        // to add parameters Tab
        control = this.facade.parametersPlace();
        this.facade.panels.put( "parameters", control );
        pane.addTab("parameters", null, control,"parameters of current element");

        control = this.facade.valuesPlace();
        this.facade.panels.put( "values", control );
        pane.addTab("values", null, control, "values of Symbols in system");

        control = this.facade.breakpointsPlace();
        this.facade.panels.put( "breakpoints", control );
        pane.addTab("breakpoints",null,control,"programm's breakpoints set");

        this.facade.view = new JPanel(new BorderLayout());
        control = this.facade.view;
        this.facade.panels.put( "view", control );
        pane.addTab("view",null, control, "view of Chain's graph");

        control = this.facade.libraryPlace();
        this.facade.panels.put( "library", control );
        pane.addTab("library",null, control, "library of a chains");

        control = this.facade.environmentPlace();
        this.facade.panels.put( "environment", control );
        pane.addTab("environment",null, control, "environment of the programm");

        pane.setSelectedIndex( 0 );
        return pane;
    }
}
