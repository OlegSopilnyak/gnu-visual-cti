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
import java.awt.*;
import java.util.*;
import org.visualcti.workflow.visualChainPiece;
/**
<model's item>
for storing a geometry of visual component and unique ID
*/
abstract class Item
{
/**
 * <member>
 * The name of Item XML element
 * */
public static final String ELEMENT="item";
    /**
     * <translator>
     * To translate the Item as XML's Element
     * */
    public Element getXML()
    {
      Element xml = new Element(Item.ELEMENT);
      // setup the class-name of an Item
      xml.setAttribute(new Attribute("class",this.getClass().getName()));
      // setup the geometry
      StringBuffer sb = new StringBuffer();
      sb.append(geometry.x).append(",").append(geometry.y).append(",");
      sb.append(geometry.width).append(",").append(geometry.height);
      xml.setAttribute(new Attribute("geometry",sb.toString()));
      return xml;
    }
    /**
     * <translator>
     * To restore the Item's properties from XML's Element
     * */
    public void setXML(Element xml) throws Exception
    {
      // to clear geometry's Rectangle
      this.geometry.setFrame(0,0,0,0);
      // process XML
      String rect = xml.getAttributeValue("geometry");
      if (rect == null) throw new NullPointerException("Item's geometry is invalid");
      try{
        StringTokenizer st = new StringTokenizer(rect,", ");
        this.geometry.x     = Integer.parseInt( st.nextToken() );
        this.geometry.y     = Integer.parseInt( st.nextToken() );
        this.geometry.width = Integer.parseInt( st.nextToken() );
        this.geometry.height= Integer.parseInt( st.nextToken() );
      }catch(Exception e){
        // to clear geometry's Rectangle
        this.geometry.setFrame(0,0,0,0);
        throw new NullPointerException("Item's geometry's parameters is invalid");
      }
    }
    /**
    <constructor>
    */
    public Item(chainModel model){this.model=model;}
/**
<attribute>
Model-owner of this Item
*/
final private chainModel model;
    /**
    <accessor>
    To get access to owner of Item
    */
    final chainModel model(){return this.model;}
/**
<attribute>
The geometric parameters of item
*/
protected final Rectangle geometry = new Rectangle();
    /**
    <accessor>
    to get access to a geometric parameters of item
    */
    final Rectangle geometry(){return this.geometry;}
    /**
    <accessor>
    to get access to a visual component
    */
    abstract visualChainPiece visual();
/**
<attribute>
The ID of a item
*/
protected String ID=null;
    /**
    <accessor>
    To get access to a item's ID
    */
    final String getID(){return this.ID;}

//#################################
//      item's notification      //
//#################################
    /**
     * <action>
     * To refresh visual part of the item
     * */
    abstract void refresh();
    /**
    <action>
    Method will call after this item is delete from a item's store
    */
    void itemDeleted(){}
    /**
    <action>
    Method will call after this item is pushed to the item's store
    */
    void itemPushed(){}
}
