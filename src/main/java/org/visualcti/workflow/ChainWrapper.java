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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.visualcti.briquette.*;
import org.visualcti.workflow.model.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The wrapper for the briquettes Chain.
 * The visualization of Chain's graph, parameters set,
 * Graph's view, chains library, server's part (device, db-connection)</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class ChainWrapper extends JPanel
{
private final JScrollPane scroll = new JScrollPane();
private final JButton close = new JButton("x");
  /**
   * <constructor>
   * */
  public ChainWrapper(final Chain chain, final Facade IDE)
  {
    super( new BorderLayout(0,0) );
    // to build the graph's place
    this.chain=chain; this.IDE=IDE;
    this.graph = new visualChain(this);
    try{
      this.graph.getModel().addSelectionListener(new chainModel.Selection(){
        public final void selected(visualOperation oper){last=oper;}
      });
    }catch(Exception e){}
    // to build & prepare the vieport
    this.viewPort = new JViewport();
    this.viewPort.setView( this.graph );
    this.viewPort.setScrollMode( JViewport.BLIT_SCROLL_MODE );
    this.viewPort.addChangeListener(new ChangeListener(){
      public final void stateChanged(ChangeEvent e){viewPortChanged();}
    });
    // to prepare the scroll
    JPanel right = new JPanel(new BorderLayout(0,2));
    this.close.setToolTipText("To close current graph");
    this.close.setMargin(new Insets(1,1,1,1));
    this.close.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){IDE.closeGraph(chain);}
    });
    JScrollBar vertical = this.scroll.getVerticalScrollBar();
    new JScrollBar(JScrollBar.VERTICAL);
    right.add(this.close,BorderLayout.NORTH);
    right.add(vertical,BorderLayout.EAST);
    int height = this.scroll.getHorizontalScrollBar().getPreferredSize().height;
    right.add(Box.createVerticalStrut(height),BorderLayout.SOUTH);
    super.add(right,BorderLayout.EAST);
    this.scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    this.scroll.setViewport( this.viewPort );
    super.add( this.scroll );
    adjustScrolls();
  }
/**
 * <attribute>
 * The name of last control
 * */
private String control = null;
  /**
   * <action>
   * To activate the wrapper
   * */
  public final void activate()
  {
    // to disable the reaction to events
    this.manual = false;
    // to adjust Grid's control
    this.IDE.gridOn.setSelected( this.grid.active );
    this.IDE.gridOn.addItemListener( this.gridOnListener );
    this.IDE.gridSize.setValue( this.grid.cell );
    this.IDE.gridSize.addChangeListener( this.gridSizeListener );
    this.IDE.showAbout.addItemListener( this.showAboutListener );
    this.IDE.showAbout.setSelected( this.showAbout );
    // to adjust the parameters panel
    this.graph.getModel().selectParamsFor( this.last );
    // to adjust the facade
    this.IDE.setChain( this.chain );
    // to restor the control's Tab
    this.IDE.setControlTab( this.control );
    // to restore show about feature
    this.graph.getModel().setEnabledAbout(this.showAbout);
    // to enable the reaction to events
    this.manual = true;
  }
  /**
   * <mutator>
   * To enable/disable the graph's close
   * */
  public final void setCloseEnabled(boolean flag){this.close.setEnabled(flag);}
  /**
   * <action>
   * To deactivate the wrapper
   * */
  public final void deactivate()
  {
    // to remove the listeners from Grid's control
    this.IDE.gridOn.removeItemListener( this.gridOnListener );
    this.IDE.gridSize.removeChangeListener( this.gridSizeListener );
    this.IDE.showAbout.removeItemListener( this.showAboutListener );
    // to store the Control's tab position
    this.control = this.IDE.currentControl();
  }
  /**
   * To free the resources before kill the instance
   * */
  protected final void finalize() throws Throwable {this.last=null;}

/**
 * <attribute>
 * The last operation
 * */
private visualOperation last = null;
/**
 * <attribute>
 * Visible part of Graph
 * */
private final JViewport viewPort;
  /**
   * <show>
   * To visualize the operation's icon
   * @param oper operation to visualize
   */
  final void visualize(visualOperation oper)
  {
    if ( oper == null ) return;
    Rectangle place = new Rectangle(oper.getPieceLocation(),oper.getSize());
    Rectangle view = this.viewPort.getViewRect();

    if ( !view.contains(place) )
    {
      this.viewPort.setViewPosition( new Point(0,0) );
      this.viewPort.scrollRectToVisible( place );
    }
  }
  /**
   * Notify about viewPort's changes
   * */
  private final void viewPortChanged(){
System.out.println("view port changed to "+this.viewPort.getViewRect() );
  }
/**
 * <flag>
 * is changes from customer
 * */
private transient boolean manual = false;
/**
 * <flag>
 * Is needs to show text labels under Operation's Icon
 */
private transient boolean showAbout=true;
  /**
   * <accessor>
   * To get access to enable/disable text label under Operation's icon flag
   * @return the flag
   */
  public final boolean isShowAbout(){return this.showAbout;}
/**
 * <listener>
 * The listener of Grid's enable/disable changes (checkbox)
 * */
private final ItemListener showAboutListener = new ItemListener(){
  final public void itemStateChanged(ItemEvent e) {
    if( !manual ) return;
    showAbout = IDE.showAbout.isSelected();
    graph.getModel().setEnabledAbout(showAbout);
    repaint();
  }
};
/**
 * <listener>
 * The listener of Grid's enable/disable changes (checkbox)
 * */
private final ItemListener gridOnListener = new ItemListener(){
  final public void itemStateChanged(ItemEvent e) {
    if( !manual ) return;
    grid.active = IDE.gridOn.isSelected();
    IDE.gridSize.setEnabled( grid.active );
    adjustScrolls(); repaint();
  }
};
/**
 * <listener>
 * The listener of grid's cell size
 * */
private final ChangeListener gridSizeListener = new ChangeListener(){
 final public void stateChanged(ChangeEvent e){
    if( !manual ) return;
    grid.cell = IDE.gridSize.getValue();
    adjustScrolls(); repaint();
 }
};
private final void setStep(JScrollBar bar, int size){
  bar.setUnitIncrement( size );
  bar.setBlockIncrement( size * 10 );
}
private final void setStep(int size){
  this.setStep(this.scroll.getVerticalScrollBar(),size);
  this.setStep(this.scroll.getHorizontalScrollBar(),size);
}
private final void adjustScrolls(){this.setStep(this.grid.active ? this.grid.cell:5);}
/**
 * <attribute>
 * Wrapped Chain
 * */
private final Chain chain;
  /**
   * <accessor>
   * To get access to the wrapped chain
   * */
  public final Chain getChain() {return this.chain;}
  /**
   * <mutator>
   * To change the name of wrapped Chain
   * */
  public final void setName(String name)
  {
    this.chain.setName( name );
    this.IDE.setChainName( name );
  }
  /**
   * <accessor>
   * To get access to wraper's name
   * */
  public final String getName(){return chain.getName();}
/**
 * <attribute>
 * The Facade pattern of IDE
 * */
private final Facade IDE;
  /**
   * <accessor>
   * To get access to IDE's facade
   * */
  public final Facade getIDE() {return this.IDE;}
/**
 * <attribute>
 * The grid of Chain's graph
 * */
private final Grid grid = new Grid();
  /**
   * <accessor>
   * To get access to graph's grid
   * */
  public final Grid getGrid() {return this.grid;}
/**
 * <attribute>
 * The graph of the Chain
 * */
private final visualChain graph;
  /**
   * <accessor>
   * To get access to to Chain's graph
   * */
  final visualChain getGraph() {return this.graph;}
}
