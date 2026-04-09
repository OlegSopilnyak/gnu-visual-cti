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
import org.visualcti.workflow.visualChain;
import org.visualcti.server.task.Environment;
import org.visualcti.briquette.*;
import org.visualcti.workflow.facade.*;
import org.visualcti.workflow.model.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: WorkFlow, class for isolate the IDE and Chain's parts</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Prominic Inc & Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class Facade implements ideFacade
{
/**
 * <attribute>
 * the owner of the facade
 * */
private final IDE ide;
/**
 * <constructor>
 * @param ide the owner of facade
 */
public Facade(IDE ide)
{
  this.ide = ide;
  this.breakpoints = new visualBreakpoints( this );
  this.library = new visualLibrary( this );
  this.environment = new visualEnvironment( this );
  this.values = new visualValues(this);
}
/**
 * <accessor>
 * To get access to runtime environment
 * @return the reference to environment
 */
public final Environment getEnvironment()
{
  return this.ide.runtime.getEnvironment();
}
/**
 * <attribute>
 * The connection to the server
 */
private ServerConnection serverConnection=null;
  /**
   * <accessor>
   * To get access to Server's connection
   * @return the connection's instance
   */
  public final ServerConnection getServerConnection() {return serverConnection;}
  /**
   * <mutator>
   * To setting up the Server's connection
   * @param serverConnection new connection's instance
   */
  public final void setServerConnection(ServerConnection serverConnection) {
    this.serverConnection = serverConnection;
  }
/**
 * <mutator>
 * To change the Name of current chain
 * @param name titled name of the chain
 */
public final void setChainName(String name){this.ide.setChainName(name);}
/**
 * <mutator>
 * To set enabled the action
 * @param action the name of action
 * @param enable flag
 */
public final void setEnabled(String action,boolean enable)
{
  this.ide.setEnabled(action,enable);
}
/**
 * <visual>
 * Is grid enabled (by default, Yes)
 * */
public final JCheckBox gridOn = new JCheckBox("Grid", true );
/**
 * <visual>
 * Is show the signature under briquette's Icon
 */
public final JCheckBox showAbout = new JCheckBox("Show about",true);
/**
 * <slider>
 * Class for visualize the Grid's slider
 * */
private static final class gridSlider extends JSlider {
    private gridSlider(){super(5,50,10);
    // to adjust the slider
    super.putClientProperty( "JSlider.isFilled", Boolean.TRUE );
    super.setMajorTickSpacing(5); super.setPaintTicks(true);
    super.setPaintTrack(true); super.setSnapToTicks(true);
  }
}
/**
 * <visual>
 * The grid size's slider
 * */
public final JSlider gridSize = new gridSlider();
  /**
   * <mutator>
   * To clean right panels
   * */
  public final void clean()
  {
    this.parameters.removeAll();
    this.environment.detachChain();
    this.values.clear();
    this.library.clean();
  }
/**
 * <attribute>
 * The pool of control's panels
 * */
final java.util.HashMap panels = new java.util.HashMap();
/**
 * <attribute>
 * The container of controls
 * */
private final JTabbedPane control = new JTabbedPane(JTabbedPane.TOP);
  /**
   * <accessor>
   * To get access to current tab in control
   * @return the title of selected Tab
   */
  final String currentControl(){
    Component tab = control.getSelectedComponent();
    for(java.util.Iterator i=panels.entrySet().iterator();i.hasNext();){
      java.util.Map.Entry entry = (java.util.Map.Entry)i.next();
      if ( entry.getValue() == tab ) return (String)entry.getKey();
    }
    return null;
  }
  /**
   * <mutator>
   * To select the graph's Tab
   * @param tab name of the chain
   */
  final void setControlTab(String tab){
    if ( tab == null ) control.setSelectedIndex(0);
    else{
      Component pane = (Component)panels.get(tab);
      if ( pane == null) control.setSelectedIndex(0);
      else control.setSelectedComponent( pane );
    }
  }
   /**
    * <mutator>
    * To close the tab
    * @param chain the chain's reference
    */
  final void closeGraph(Chain chain){this.ide.closeGraph(chain);}
  /**
   * <accessor>
   * To get access to control's Tabbed panel
   * @return tabbed panel
   */
  final JTabbedPane control(){return this.control;}
/**
 * <panel>
 * place for operation/chain parameters
 * */
private final JPanel parameters = new JPanel( new BorderLayout(), true );
  /**
   * <accessor>
   * To get access to parameter's place
   * @return the place
   * @see ideFacade
   */
  public final JPanel parametersPlace(){return this.parameters;}
/**
 * <panel>
 * place for values
 * */
private final visualValues values;
  /**
   * <accessor>
   * To get access to values's place
   * @return the place
   * @see ideFacade
   */
  public final JPanel valuesPlace(){return this.values;}
/**
 * <panel>
 * place for store chain's breakpoints
 * */
private final visualBreakpoints breakpoints;
  /**
   * <accessor>
   * To get access to breakpoints place
   * @return the place
   * @see ideFacade
   */
  public final JPanel breakpointsPlace(){return this.breakpoints;}
  /**
   * <action>
   * To select & show the operation
   * @param briquetteID the ID of briquette
   */
  public final void showOperation(String briquetteID)
  {
    chainModel model = this.ide.getChain().getModel();
    visualOperation v = model.getVisualOperation(briquetteID);
    model.showOperation( v );
    this.ide.getChain().visualize( v );
  }
  /**
   * <action>
   * To launch breakpoint's setup
   */
  public final void addBreakPoint()
  {
    this.ide.getChain().addBreakpoint();
  }
  /**
   * <action>
   * To delete the breakpoint
   * @param ID operation's ID
   */
  public final void deleteBreakPoint(String ID)
  {
    this.ide.getChain().deleteBreakPoint(ID);
  }
  /**
   * <accessor>
   * To get access to breakpoint's flag
   * @param ID the ID of Operation
   * @return flag's value
   */
  public final boolean isBreakpointEnabled(String ID)
  {
    visualOperation v = this.ide.getChain().getModel().getVisualOperation(ID);
    if ( v != null ) return v.isEnabledBreakPoint();
    return false;
  }
/**
 * <panel>
 * place for library's component
 * */
private final visualLibrary library;
  /**
   * <accessor>
   * To get access to library's place
   * @return the place
   * @see ideFacade
   */
  public final JPanel libraryPlace(){return this.library;}
/**
 * <panel>
 * place for View of algorithm
 * */
JPanel view;
  /**
   * <accessor>
   * To get access to view's place
   * @return the place
   * @see ideFacade
   */
  public final JPanel viewPlace(){return this.view;}
/**
 * <panel>
 * place for Server's properties
 * */
private final visualEnvironment environment;
  /**
   * <accessor>
   * To get access to view's place
   * @return the place
   * @see ideFacade
   */
  public final JPanel environmentPlace(){return this.environment;}
  /**
   * <mutator>
   * To assign the Chain
   * @param chain the chain to activate
   */
  public final void setChain(Chain chain)
  {
    this.environment.setChain( chain );
    this.library.setChain( chain );
    this.breakpoints.setChain( chain );
    // to title the chain's name
    this.ide.setChainName( chain.getName() );
    // to title the URL to chain's body
    Chain.Source source = chain.getSource();
    if ( source != null )
    {
      java.net.URL url = source.getPath();
      StringBuffer buf = new StringBuffer(url.getProtocol()).append("::");
      String file = new java.io.File( url.getFile() ).getName();
      this.ide.setChainURL( buf.append(file).toString() );
    }
  }
  /**
   * <mutator>
   * To select the Chain
   * @param chain the chain to show graph
   */
  public final void selectChain(Chain chain){this.ide.openGraph( chain );}
}
