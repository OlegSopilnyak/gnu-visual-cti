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
package org.visualcti.workflow.model;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import java.lang.reflect.*;
import java.util.*;
import org.jdom.*;
import org.visualcti.briquette.*;
import org.visualcti.workflow.*;
/**
 * <model>
 * The model of a operations chain's visualization
*/
public class chainModel
{
  /**
   * <constructor>
   * To make model for canvas
   * */
  public chainModel(visualChain canvas){this.canvas=canvas;}
/**
 * <attribute>
 * visual panel (canvas) of the chain
 * */
private final visualChain canvas;
  /**
   * <accessor>
   * to get access to chain's canvas
   */
  final public visualChain getCanvas(){return this.canvas;}
//////////////// PRODUCER PART ////////////////////
///////////////// SELECTIONS PART (begin) ///////////////////////
/**
 * <accessor>
 * To get Selection as XML Edit->Copy
 * */
public final Element EditCopy()
{
  Element xml = new Element("selected");
  synchronized(this.selected)
  {
    for(Iterator i=this.selected.iterator();i.hasNext();)
    {
      String ID = (String)i.next();
      if (ID != null) xml.addContent( ((Item)this.items.get(ID)).getXML() );
    }
  }
  return xml;
}
/**
 * <member>
 * The name of entry in the pool
 * */
private final static String BRIQUETTE = "operation";
/**
 * <mutator>
 * To place the items from XML
 * */
public final void EditPaste(Element xml)
{
  if (xml == null) return;
  this.eraseSelected();
  HashMap pool = new HashMap();
  java.util.List items = xml.getChildren( Item.ELEMENT );
  if ( items == null || items.size() == 0 ) return;
  for(Iterator i=items.iterator();i.hasNext();) this.restoreItem( (Element)i.next(), pool );
  this.solveBriquettes( this.poolList(pool,BRIQUETTE) );
}
/**
 * <producer>
 * To restore the item from XML
 * */
private final void restoreItem(Element xml,HashMap pool){
  String className = xml.getAttributeValue("class");
  if (className == null) return;
  try {
    Class itemClass = Class.forName( className );
    if ( !Item.class.isAssignableFrom(itemClass) ) return;
    Constructor init = itemClass.getConstructor(new Class[]{chainModel.class});
    Item item = (Item)init.newInstance(new Object[]{this});
    item.setXML( xml ); this.pushItem(item, pool);
  }catch(Exception e){
    e.printStackTrace();
  }
}
/**
 * <deploy>
 * To push the item to model
 * */
private final void pushItem(Item item, HashMap pool){
  if ( item instanceof operationItem ){
    Operation briquette = ((operationItem)item).getBriquette();
    this.poolList(pool,BRIQUETTE).add( briquette );
  }else {
    this.pushItem(item);
  }
}
/**  to restore the briquettes */
private final void solveBriquettes(java.util.List list){
  HashMap map = new HashMap();
  ArrayList links = new ArrayList();
  // class for store information about connection
  final class link {Operation from,to;int type;
    link(Operation from,Operation to,int type){
      this.from=from;this.to=to;this.type=type;}
  }
  // collect information about new briquettes's connections
  for(Iterator i=list.iterator();i.hasNext();) {
    Operation oper = (Operation)i.next();
    // remove master's flag
    if ( oper.isMaster() ) oper.setMaster( false );
    // place the operation to the Map
    map.put( oper.getID(), oper );
  }
  // make the list of connections
  for(Iterator i=list.iterator();i.hasNext();) {
    Operation oper=(Operation)i.next();
    String linkID[] = oper.getLinkIDs();
    for(int type=0;type < linkID.length;type++) {
      Operation target = (Operation)map.get(linkID[type]);
      if (target != null) links.add(new link(oper,target,type));
    }
  }
  // place briquettes to model
  for(Iterator i=list.iterator();i.hasNext();)
    this.place((Operation)i.next(),true);
  // to restore the links between pasted Operations
  for(Iterator i=links.iterator();i.hasNext();){link con=(link)i.next();
    this.link(con.from,con.to,con.type);
  }
}
/** get list by name */
private final java.util.List poolList(HashMap pool, String entry) {
  java.util.List list = (java.util.List)pool.get(entry);
  if (list == null) pool.put(entry,list = new ArrayList());
  return list;
}
/**
 * <action>
 * to enable Copy-action
 * */
private final void enableCopy(){
  this.canvas.facade().setEnabled("Cut",true);
  this.canvas.facade().setEnabled("Copy",true);
  this.canvas.facade().setEnabled("Delete",true);
}
/**
 * <action>
 * to disable Copy-action
 * */
private final void disableCopy(){
  this.canvas.facade().setEnabled("Cut",false);
  this.canvas.facade().setEnabled("Copy",false);
  this.canvas.facade().setEnabled("Delete",false);
}
/**
 * <attribute>
 * The set of selected Items
*/
private final HashSet selected = new HashSet( 100 );
    /**
     * <show>
     * To select and set visible the operation
     * @param oper operation to show
     */
    public final void showOperation(visualOperation oper)
    {
      if ( oper == null ) return;
      this.unselectAll();
      Item item = (Item)this.items.get( oper.getBriquette().getID() );
      this.select(item); this.selectOperation( oper );

    }
    /**
     * <mutator>
     * To select a piece of visualChain
     * */
    public final void select(visualChainPiece piece,MouseEvent event)
    {
        Item item = this.find(piece);// try to find the Item for visual
        if (item == null || event == null) return;
        // check, is Ctrl key pressed, during click
        boolean multiSelect = (event.getModifiers() & Event.CTRL_MASK) != 0;
        synchronized (this.selected)
        {   // set flag, is Item alerady selected
            boolean alreadySelected = this.selected.contains( item.getID() );
            // to process selection
            if ( multiSelect ) {// to add/remove to selections pool
                if ( alreadySelected ) this.unselect(item);
                else this.select(item);
                // to check selections count
                if (this.selected.size() != 1) {
                  this.selectChain();// multiselection
                } else {// one selection
                  String ID = (String)this.selected.iterator().next();
                  item = (Item)this.items.get(ID);
                  this.selectOperation((visualOperation)item.visual());
                }
            } else  {// to select one operation
                this.unselectAll();// to clear all selections
                this.select(item);
                this.selectOperation((visualOperation)item.visual());
            }
            if ( this.selected.size() > 0 ) this.enableCopy();
            else                            this.disableCopy();
        }
        // request to repaint a canvas
        this.canvas.repaint();
    }
      /**
       * To find an Item by visual Component's reference
       * */
      private final Item find(visualChainPiece piece){
        if (piece == null) return null;// invalid method's use
        for(Iterator i=this.items.values().iterator();i.hasNext();){
            Item item = (Item)i.next();
            if (item != null && item.visual() == piece) return item;
        }
        return null;
      }
      /**
       * to unselect one Item
       * */
      private final void unselect(Item item){
          synchronized(this.selected){
              if (item.getID() != null){
                this.selected.remove( item.getID() );
                item.visual().unselect();
              }
            if ( this.selected.size() <= 0 ) this.disableCopy();
          }
      }
      /**
       * to select one Item
       * */
      private final void select(Item item){
          synchronized(this.selected){
              if (item.getID() != null){
                this.selected.add( item.getID() );
                item.visual().select();
              }
              this.enableCopy();
          }
      }
    /**
     * <mutator>
     * To unselect ALL items
     * */
    public final void unselectAll()
    {
        synchronized(this.selected)
        {
          for(Iterator i=this.selected.iterator();i.hasNext();)
          {
            String ID = (String)i.next();
            if (ID == null) continue;
            ((Item)this.items.get(ID)).visual().unselect();
          }
          this.selected.clear();
          this.disableCopy();
          this.selectChain();
        }
    }
/**
 * listener of selection
 * */
public interface Selection{
  void selected(visualOperation oper);
}
/**
 * <attribute>
 * Selection's listener
 * */
private Selection listener = null;
private final Object selectionSemaphore = new Object();
public final void addSelectionListener(Selection listener) throws TooManyListenersException
{
  if ( this.listener != null ) throw new TooManyListenersException();
  synchronized( this.selectionSemaphore ){this.listener=listener;}
}
public final void removeSelectionListener(Selection listener)
{
  synchronized ( this.selectionSemaphore ){
    if ( this.listener == listener) this.listener = null;
  }
}
private final void paramsSelectedFor(visualOperation oper){
  synchronized ( this.selectionSemaphore ){
    if ( this.listener != null) this.listener.selected(oper);
  }
}
    /**
     * to select parameters for operation
     * After Chain's wrapper activation
     * */
    public final void selectParamsFor(visualOperation oper)
    {
      if (oper == null) this.selectChain(); else this.selectOperation(oper);
    }
    /**
     * <mutator>
     * To enable/disable show the text lable under Operation's Icon
     * @param flag the flag
     */
    public final void setEnabledAbout(boolean flag)
    {
      for(Iterator i=this.items.values().iterator();i.hasNext();)
      {
        try{
          operationItem item = (operationItem)i.next();
          visualOperation visual = (visualOperation)item.visual();
          visual.setShowAbout( flag );
        }catch(ClassCastException e){}
      }
    }
      /**
       * no parameters, or parametrs of the chain
       * */
      private final void selectChain(){
        Facade facade = this.canvas.facade();
        UI editor = this.canvas.getChainUI();
        editor.activate( facade.parametersPlace() );
        if ( this.selected.size() > 0 ) this.enableCopy();
        else                            this.disableCopy();
        this.paramsSelectedFor( null );
      }
      /**
       * To select a Operation's parameters
       * */
      private final void selectOperation(visualOperation oper){
        Facade facade = this.canvas.facade();
        oper.getOperationUI().activate( facade.parametersPlace() );
        this.paramsSelectedFor( oper );
      }
    /**
     * <selector>
     * To select all Operations that intersects a union
     * (from visualChain)
     * */
    public final void mark(Rectangle union)
    { // to iterate all model's items
      for(Iterator i=this.items.values().iterator();i.hasNext();)
      {
          Item item = (Item)i.next();
          // is wrong entry or link's Item
          if (item == null || item instanceof linkItem) continue;
          // to select/unselect items
          if ( union.intersects(item.geometry) )
          {
            // is not selected?
            if ( !item.visual().isSelected() ) this.select(item);
            // repaint selected operation's visuals
            item.visual().repaint();
          } else {
            // is selected
            if ( item.visual().isSelected() ) this.unselect(item);
          }
      }
    }

/**
 * <const>
 * point for selectedPlace when no active drag
 * */
private final static Point noDrag = new Point(Short.MIN_VALUE,Short.MIN_VALUE);
/**
 * <attribute>
 * The place of selected items
 * */
private final Rectangle selectedPlace = new Rectangle(noDrag);
    /**
     * <notify>
     * from mouse's listener
     * selected item draged
     * */
    public final void dragSelected(Point delta)
    {
      synchronized(this.selectedPlace)
      {
        // to init selected place
        this.initSelectedPlace();
        // to check selection's  borders with delta
        this.checkSelectedPlace( delta );
        // to drag all selected pieces
        for(Iterator i=this.selected.iterator();i.hasNext();) {
            Item item = (Item)this.items.get( i.next() );
            // to change an Item's coordinates
            if (item != null)item.visual().dragged( delta );
        }
      }
    }
        /**
         * To init selected place, if needed
         * */
        private final void initSelectedPlace(){
          if(this.selectedPlace.x == noDrag.x)
            this.makeSelectedPlace();
          // to make starting geometry
        }
        /**
         * To make the union of selected Item's geometries
         * */
        private final void makeSelectedPlace(){
          Iterator i = this.selected.iterator();
          Item item = (Item)this.items.get( i.next() );
          // to make union from first selected Item's geometry
          this.selectedPlace.setBounds( item.geometry() );
          // to add other Item's geometries to union
          while( i.hasNext() ){
            item = (Item)this.items.get( i.next() );
            Rectangle union = this.selectedPlace.union( item.geometry() );
            this.selectedPlace.setBounds(union);
          }
        }
        /**
         * to check selected place borders
         * */
        private final void checkSelectedPlace(Point delta){
            int x = this.selectedPlace.x + delta.x;
            if (x < 0) delta.x -= x;// check & fix X
            int y = this.selectedPlace.y + delta.y;
            if (y < 0) delta.y -= y;// check & fix Y
        }
        /**
         * To notify selected Items about drag
         * */

    /**
     * <notify>
     * from mouse's listener
     * selected item dropped
     * */
    public final void dropSelected(Point delta)
    {
      synchronized(this.selectedPlace)
      {
          // to notify about stop drag and relocate items
          for(Iterator i=this.selected.iterator();i.hasNext();){
              Item item = (Item)this.items.get( i.next() );
              item.visual().stopDrag();// to draw visual on the new place
              // to relocate Operation's geometry
              item.geometry().setLocation( item.visual().getPieceLocation() );
          }
          // to refresh a visual component with new coordinates
          for(Iterator i=this.selected.iterator();i.hasNext();){
              ((Item)this.items.get( i.next() )).refresh();
          }
          // to adjust visualChain's visual size with
          // new coordinates of union
          this.checkCanvasBorders();
          // finish dragging, clear all flags
          this.dragFinished();
      }
      this.canvas.repaint();
    }
      /**
       * To setup new canvas's borders
       * */
      private final void checkCanvasBorders() {
        // to make union with new coordinates
        this.makeSelectedPlace();
        // check borders
        this.adjustBordersFor( this.selectedPlace );
      }
      /**
       * To adjust the canvas's borders for show the Rectangle
       * */
      private final void adjustBordersFor( Rectangle geometry ){
        // get geometry's maximums
        double maxX = geometry.getMaxX();
        double maxY = geometry.getMaxY();
        // to get actual canvas's size
        Dimension size = this.canvas.getSize();
        if (size.width < maxX || size.height < maxY)
        { // to make new size for a canvas
          size.width = (int)Math.max(maxX,size.width);
          size.height= (int)Math.max(maxY,size.height);
          this.canvas.setSize( size );
          this.canvas.setPreferredSize( size );
        }
      }
      /**
       * To clear dragged flags
       * */
      private final void dragFinished(){
        this.selectedPlace.setLocation( chainModel.noDrag );
      }
///////////////// SELECTIONS PART (end) ///////////////////////

///////////// PLACE/LINK/REMOVE Operation (begin) ///////////////
    /**
     * <mutator>
     * To place new Operation to model
     * */
    public final Item place(Operation briquette,boolean select)
    {
        BriquettesSequence sequence = this.canvas.getBriquettesSequence();
        // to add Operation to the Chain
        Item item = sequence.add(briquette) ? this.push(briquette) : null;
        if (select && item != null)
        {
          visualChainPiece visual = item.visual();
          MouseEvent event =
            new MouseEvent(visual,MouseEvent.MOUSE_RELEASED,-1,
                            Event.CTRL_MASK,0,0,1,false);
          this.select(visual, event);
        }
        return item;
    }
    /**
     * <mutator>
     * To place exists Operation to model
     * */
    public final Item push(Operation briquette)
    {
        Item item = new operationItem( this, briquette );
        this.pushItem( item );
        return item;
    }
    /**
     * <accessor>
     * Tp get access to operation's visual by briquette
     * @param briquette the owner of visual
     * @return the visual briqette
     */
    public final visualOperation getVisualOperation(Operation briquette)
    {
      if ( briquette == null ) return null;
      return this.getVisualOperation(briquette.getID());
    }
    /**
     * <accessor>
     * Tp get access to operation's visual by briquette
     * @param briquette the owner of visual
     * @return the visual briqette
     */
    public final visualOperation getVisualOperation(String ID)
    {
      try{
        operationItem item = (operationItem)this.items.get( ID );
        return (visualOperation)item.visual();
      }catch(Exception e){}
      return null;
    }
    /**
     * <mutator>
     * to place link between operations
     * */
    public final void link
                        (
                        Operation from, // the source of the Link
                        Operation to,   // the target of the Link
                        int linkID      // the type of the Link
                        )
    {
        if ( from == null || to == null ) return;// invalide link
        // get access to model's items
        operationItem itemFrom = (operationItem)this.items.get( from.getID() );
        operationItem itemTo = (operationItem)this.items.get( to.getID() );
        if ( itemFrom == null || itemTo == null ) return;// invalid operations
        // to get access to old Link
        linkItem old = itemFrom.getLinkItem(linkID);
        // to delete old Link
        if (old != null) this.deleteItem(old.getID());
        // check the target
        if ( from.getID().equals(to.getID()) )
        { // maked link to himself, connector will place to right side
          visualOperation visual=(visualOperation)itemFrom.visual();
          visual.placeLinkConnector( linkID, visualOperation.RIGHT );
          // to clear the Operation's connection
          from.setLink( linkID, null );
          return;
        }
        // to make the Operation's connection
        from.setLink( linkID, to );
        // to push new link's Item to model
        this.pushItem( new linkItem(this,itemFrom,itemTo,linkID) );
    }
    /**
     * <mutator>
     * to remove all selected Items
    */
    public final void eraseSelected()
    {
        synchronized( this.selected )
        { // to collect selected items for remove
          HashMap selection = this.selectedOperations();
          // to get Operations Chain's sequence
          BriquettesSequence sequence = this.canvas.getBriquettesSequence();
          // to remove selected from visual and briquette chains
          this.eraseSelected(selection, sequence);
          // to clear selected pool
          this.selected.clear();
          this.selectChain();
          this.disableCopy();
        }
    }
      /**
       * to collect selected items
       * */
      private final HashMap selectedOperations()
      {
          HashMap selection = new HashMap(10); String ID; Item item;
          // to iterate a selected IDs
          for(Iterator i=this.selected.iterator();i.hasNext();)
          {   // get ID
              if ((ID = (String)i.next()) == null) continue;
              // get Item by ID
              if ((item = (Item)this.items.get( ID )) == null) continue;
              // to add Item's visual ID and briquette
              Component visible = item.visual();
              if (visible instanceof visualOperation)
              {
                  visualOperation vo = (visualOperation)visible;
                  Operation briquette = vo.getBriquette();
                  selection.put( item.getID(), briquette );
              }
          }
          return selection;
      }
      /**
       * to remove selected
       * */
       private final void eraseSelected(HashMap selection, BriquettesSequence chain){
              // to remove selected from visual and briquette chains
              for(Iterator i=selection.entrySet().iterator();i.hasNext();){
                  Map.Entry entry = (Map.Entry)i.next();
                  String ID = (String)entry.getKey();
                  Operation briquette = (Operation)entry.getValue();
                  // try to remove from both chains
                  if ( chain.remove(briquette) ) this.deleteItem(ID);
              }
       }
///////////// PLACE/LINK/REMOVE Operation (end) ///////////////

/**
 * <attribute>
 * The pool of model's Items
*/
private final HashMap items = new HashMap( 100 );
      /**
       * To push item to model
      */
      private final void pushItem(Item item)
      {
          if (item == null) return;// invalid Item
          String itemID = item.getID();
          // to delete previous item
          this.deleteItem( itemID );
          // push the item to a store
          synchronized (this.items)
          {
              this.items.put(itemID, item);
              // to place visual part to the Container
              this.placeVisual( item );
          }
          // to check the canvas's borders
          this.adjustBordersFor( item.geometry );
          // to notify a Item
          item.itemPushed();
          // request for repaint a canvas
          this.canvas.repaint();
      }
      /**
       * to place visual part of the Item
       * */
      private final void placeVisual(Item item){
        if (item instanceof linkItem) {
          this.placeVisualLink( (linkItem)item );
        }else {
          this.canvas.add( item.visual(), item.getID() );
        }
        if (item instanceof operationItem){
          visualOperation oper=(visualOperation)item.visual();
          oper.setShowAbout( this.canvas.isShowAbout() );
        }
      }
      /**
       * To place a visual Link
       * */
      private final void placeVisualLink(linkItem link){
        Component visualFrom = link.source().visual();
        Component[] content = this.canvas.getComponents();
        // to iterate container's content
        for(int i=0;i < content.length;i++) {
            if (content[i] == visualFrom){
                // insert link, after source of the link
                this.canvas.add( link.visual(), link.getID(), i+1 );
                break;
            }
        }
      }
  /**
   * <mutator>
   * To delete Item's instance (and all related Items) in a model
  */
  final void deleteItem( Item item )
  {
    if (item != null) this.deleteItem( item.getID() );
  }
      /**
       * To delete Item (and all related Items) in a model
       * by an Item's ID
      */
      private final void deleteItem( String itemID ){Item item=null;// item, to delete
          synchronized( this.items )
          {   // to get removed item from pool
              item = (Item)this.items.remove( itemID );
              if ( item != null )
              {   // to remove Item's visual part
                  // from a chain's canvas
                  this.canvas.remove( item.visual() );
              }
          }
          // to notify a Item's instance
          if (item != null) item.itemDeleted();
          // request for repaint a canvas
          this.canvas.repaint();
      }
}
