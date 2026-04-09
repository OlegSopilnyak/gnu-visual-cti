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

import org.jdom.*;
import java.util.*;

import org.visualcti.briquette.*;
import org.visualcti.workflow.visualOperation;
import org.visualcti.workflow.visualChainPiece;
/**
<model's item>
for storing a geometry of visual component and unique ID
*/
final class operationItem extends Item
{
  /**
   * <constructor>
   * */
  public operationItem(chainModel model){super(model);}
    /**
     * <translator>
     * To translate the Item as XML's Element
     * */
    public final Element getXML()
    {
      Element xml = super.getXML();
      xml.addContent( this.oper.getXML() );
      return xml;
    }
    /**
     * <translator>
     * To restore the Item's properties from XML's Element
     * */
    public final void setXML(Element xml) throws Exception
    {
      super.setXML( xml );
      this.oper = Toolkit.makeOperation( xml.getChild(Operation.ELEMENT) );
      this.visual = new visualOperation( this.oper, model().getCanvas() );
      this.ID = this.oper.getID();
      this.geometry.setFrame(this.oper.coordinates(),this.visual.getSize());
    }
/**
 * <attribute>
 * The pool of operation's related items
 * */
private final HashMap pool = new HashMap(4);
  /**
   * <accessor>
   * To get access to pool by name
   * */
   private final ArrayList pool(String name){
      synchronized( this.pool ){
          ArrayList itemsPool = (ArrayList)this.pool.get(name);
          if (itemsPool == null){
            this.pool.put( name, itemsPool = new ArrayList() );
          }
          return itemsPool;
      }
   }
/**
<attribute>
The briquette's Operation reference
*/
private Operation oper;
    /**
     * <accessor>
     * To get access to briquette's part of the Item
     * */
    public final Operation getBriquette(){return this.oper;}
/**
 * <attribute>
 * Visual component of the item
 * */
private visualOperation visual;
    /**
    <accessor>
    to get access to a visual component
    */
    final visualChainPiece visual(){return this.visual;}
    /**
    <constructor>
    */
    public operationItem(chainModel model,Operation oper){super( model );
        this.visual = new visualOperation( this.oper=oper, model.getCanvas() );
        this.ID = oper.getID();
        this.geometry.setFrame(oper.coordinates(),this.visual.getIconSize());
    }
    /**
    <action>
    Method will call after this item is pushed to the item's store
    */
    void itemPushed(){this.visual.revalidate();}
    /**
    <action>
    Method called after item deleted from store
    To delete from model all links of the Operation
    */
    void itemDeleted()
    {
        // to remove an operation related Items
        for(Iterator i=this.pool.values().iterator();i.hasNext();)
        {
          ArrayList items = (ArrayList)i.next();
          this.clearPool( items );
        }
        // to clear container of pool
        this.pool.clear();
    }
    /**
     * <utility>
     * To delete all items from pool
     * */
    private final void clearPool(ArrayList pool)
    {
      chainModel model = this.model();
      ArrayList clone = null;
      // to make a clone of the Items pool
      synchronized(pool){clone = (ArrayList)pool.clone();}
      // to iterate a pool's clone
      for(Iterator i=clone.iterator();i.hasNext();)
      {
        // to delete item from a chain's model
        model.deleteItem( (Item)i.next() );
      }
    }
    /**
     * <action>
     * To refresh visual part of the item
     * */
    final void refresh()
    { // to refresh all link's items
      this.refresh( this.pool("input links")  );
      this.refresh( this.pool("output links") );
    }
    /**
     * <mutator>
     * To assign new input link
     * */
    public final boolean addInput(linkItem link)
    {
        return this.addItem( this.pool("input links"), link );
    }
    /**
     * <accessor>
     * To get access to linkItem by linkID
     * */
    final linkItem getLinkItem(int linkID)
    {
      ArrayList pool = this.pool("output links");
      synchronized( pool ){
        for (Iterator i=pool.iterator();i.hasNext();){
          linkItem item = (linkItem)i.next();
          if (item.getLinkID() == linkID) return item;
        }
      }
      return null;
    }
    /**
     * <mutator>
     * To assign new output link
     * */
    public final boolean addOutput(linkItem link)
    {
      // to delete old link with similar linkID
      this.deleteExists( link.getLinkID() );
      // try to add link's Item
      return this.addItem( this.pool("output links"), link );
    }
    /** <mutator> To delete exists link */
    private final void deleteExists(int linkID) {
      ArrayList pool = this.pool("output links");
      synchronized( pool ){ pool = (ArrayList)pool.clone();
        for (Iterator i=pool.iterator();i.hasNext();){
          linkItem item = (linkItem)i.next();
          if (item.getLinkID() == linkID) {
            this.model().deleteItem( item );// to delete link's item from model
          }
        }
      }
    }
    /**
     * <mutator>
     * To remove input link
     * */
    public final boolean removeInput(linkItem link) {
      return this.removeItem( this.pool("input links"), link);
    }
    /**
     * <mutator>
     * To remove output link
     * */
    public final boolean removeOutput(linkItem link){
      return this.removeItem( this.pool("output links"), link);
    }
        /**
         * <utility>
         * To refresh an items in pool
         * */
        private final void refresh(ArrayList pool){
          synchronized(pool) {
            for(Iterator i=pool.iterator();i.hasNext();){
              ((Item)i.next()).refresh();
            }
          }
        }
        /**
         * <utility>
         * To add item to pool
         * */
        private final boolean addItem(ArrayList pool,Item item) {
            synchronized( pool ){
                if ( pool.contains(item) ) return false;
                else {pool.add( item ); return true;}
            }
        }
        /**
         * <utility>
         * To remove the item from pool
         * */
        private final boolean removeItem(ArrayList pool,Item item){
            synchronized( pool ){return pool.remove(item);}
        }
}
