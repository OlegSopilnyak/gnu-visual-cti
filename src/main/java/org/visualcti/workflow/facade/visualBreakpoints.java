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
import javax.swing.event.*;

import org.jdom.*;
import org.visualcti.workflow.Facade;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, container for manage the breakpoints</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class visualBreakpoints extends JPanel
{
/**
 * <model>
 * List's model
 * */
private final DefaultListModel model;
/**
 * <visual>
 * The list of program's breakpoints
 * */
private final JList list;
/**
 * <control>
 * The class for control the breakpoints
 * */
private final class listControl extends controlPanel {
  public listControl(){super();
    super.getButton("add").setToolTipText("To add a breakpoint");
    super.getButton("del").setToolTipText("To delete the breakpoint");
    super.remove( super.getButton("up") );
    super.remove( super.getButton("down") );
    super.getButton("edit").setToolTipText("To visualize the Operation");
  }
protected final void Add(){addBreakpoint();}
protected final void Del(){delBreakpoint();}
protected final void Up(){}
protected final void Down(){}
protected final void Edit(){selectBreakpoint();}
}
/**
 * <visual>
 * The control of breakpoints list
 * */
private final listControl control = new listControl();
/**
 * <const>
 * The name of XML element
 * */
public static final String ELEMENT = "breakpoints";
  /**
   * <translator>
   * To store this list to XML
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    for(java.util.Enumeration i=this.model.elements();i.hasMoreElements();)
      xml.addContent( ((Item)i.nextElement()).getXML() );
    return xml;
  }
  /**
   * <translator>
   * To restor the list from XML
   * */
  public final void setXML(Element xml) throws Exception
  {
    this.clear();
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    for(java.util.Iterator i=xml.getChildren(Item.ELEMENT).iterator();i.hasNext();)
    {
      Item item = Item.restore((Element)i.next());
      if ( item != null) this.addItem( item );
    }
  }
/**
 * <attribute>
 * The owner of panel
 * */
private final Facade owner;
  /**
   * <constructor>
   * */
  public visualBreakpoints(Facade owner)
  {
    super( new BorderLayout(), true );
    this.owner = owner;
    this.model = new DefaultListModel();
    this.list = new JList( this.model );
    super.add( new JScrollPane(this.list), BorderLayout.CENTER );
    ListSelectionModel selection = this.list.getSelectionModel();
    selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    selection.addListSelectionListener(new ListSelectionListener(){
      public final void valueChanged(ListSelectionEvent e){
        if ( !model.isEmpty() ) itemSelected();
      }
    });
    super.add( this.control, BorderLayout.SOUTH );
    // to register the icon
    UI_Store.register("..BreakPoint","/icon/BreakPoint12.gif",null);
    this.control.addOnly();
  }
  public final void setChain(Chain chain)
  {
    this.model.clear();
    if ( chain == null ) return;
    for(Iterator i=chain.getBreakpoints().iterator();i.hasNext();)
    {
      Chain.BreakPoint point = (Chain.BreakPoint)i.next();
      this.addItem( new Item(point) );
    }
  }
  /**
   * <notify>
   * To process list's selection
   * */
  private final void itemSelected(){
    if ( this.selected() == null )
      this.control.addOnly(); else control.allFeatures();
  }
  /**
   * <notify>
   * Will call when the breakpoint is added
   * @param oper breakpoint for briquette
   */
  public final void breakpointAdded(Operation oper)
  {
    Item item = new Item(oper.getID(), true);
    this.addItem( item );
  }
  /**
   * <notify>
   * Will call when the breapoint change the state
   * @param oper breakpoint's briquette
   */
  public final void breakpointStateUpdated(Operation oper)
  {
    Item item = this.find(oper);
    if ( item != null )
    {
      item.enabled = owner.isBreakpointEnabled( oper.getID() );
      this.list.repaint();
    }
  }
  /**
   * <notify>
   * Will call when the execution stop at breakpoint
   * @param oper stoppped briquette
   * @param stopped flag of stopped
   */
  public final void stopAtBreakpoint(Operation oper,boolean stopped)
  {
    Item item = this.find(oper);
    if ( item != null )
    {
      item.stopped = stopped;
      this.list.repaint();
    }
  }
  private final Item find(Operation oper){
    String ID = oper.getID();
    for(Enumeration e=this.model.elements();e.hasMoreElements();)
    {
      Item item = (Item)e.nextElement();
      if ( item.ID.equals(ID) ) return item;
    }
    return null;
  }
  /**
   * <mutator>
   * To clear the visuals
   * */
  public final void clear()
  {
    this.model.clear();
  }
  /**
   * <action>
   * To add breakpoint
   * */
  private final void addBreakpoint(){
    this.owner.addBreakPoint();
  }
  /**
   * <action>
   * To delete breakpoint
   * */
  private final void delBreakpoint(){
    String ID = this.selected();
    if ( ID == null ) return;
    this.owner.deleteBreakPoint(ID);
    int index = this.list.getSelectedIndex();
    this.model.removeElementAt( index );
  }
  /**
   * <action>
   * To select breakpointed Operation
   * */
  private final void selectBreakpoint(){
    String ID = this.selected();
    if ( ID == null ) return;
    this.owner.showOperation( ID );
  }
  /**
   * <accessor>
   * To get selected ID
   * */
  private final String selected(){
    Item item = (Item)this.list.getSelectedValue();
    return item == null ? null:item.ID;
  }
  /**
   * class for visualize the breakpoint
   * */
  private final static class Item{
    final static String ELEMENT = "item";
    String ID;boolean enabled,stopped;
    Item(Chain.BreakPoint point)
    {
      this.ID = point.getOperationID();
      this.enabled = point.isEnabled();
      this.stopped = false;
    }
    Item(String ID,boolean enabled){this.ID=ID;this.enabled=enabled;}
    public final String toString(){
      String mark = this.stopped ? "=>":"";
      StringBuffer sb = new StringBuffer(mark);
      sb.append( (this.enabled)?"(+)":"(-)" ).append(this.ID);
      return sb.toString();
    }
    public final Element getXML(){
      Element xml = new Element( ELEMENT );
      xml.setAttribute( new Attribute( "ID", this.ID) );
      xml.setAttribute( new Attribute( "enabled", String.valueOf(this.enabled)) );
      return xml;
    }
    public final static Item restore(Element xml) throws Exception {
      if ( xml == null || !ELEMENT.equals(xml.getName()) ) throw new NullPointerException();
      String ID = xml.getAttributeValue("ID");
      String flag = xml.getAttributeValue("enabled");
      if ( ID == null ) return null;
      return new Item( ID, Boolean.valueOf(flag).booleanValue() );
    }
  }
  /**
   * <mutator>
   * To add breakpoint to the list
   * */
  private final void addItem(Item item){this.model.addElement( item );}
  /**
   * <mutator>
   * To add breakpoint to the list
   * */
  private final void addItem(String ID, boolean enabled){
    this.addItem( new Item(ID,enabled) );
  }
}
