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
package org.visualcti.workflow.model;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import org.visualcti.briquette.Chain;
import org.visualcti.briquette.Operation;
import org.visualcti.workflow.visualLink;
import org.visualcti.workflow.visualOperation;
import org.visualcti.workflow.visualChainPiece;
/**
<item>
Model's Link between 2 Operations
*/
final class linkItem extends Item
{
/**
<attribute>
The source of a link
*/
private final operationItem from;
/**
<attribute>
The target of a link
*/
private final operationItem to;
    /**
     * <acessor>
     * To get access to link's source
     * */
    final operationItem source(){return this.from;}
    /**
     * <acessor>
     * To get access to link's target
     * */
    final operationItem target(){return this.to;}
/**
 * <attribute>
 * Visual component of the item
 * */
private final visualLink visual;
    /**
    <accessor>
    to get access to a visual component
    */
    final visualChainPiece visual(){return this.visual;}
/**
 * <attribute>
 * The link's ID
 * */
private int linkID = Operation.DEFAULT_LINK;
    /**
     * <accessor>
     * to get access to link's ID
     * */
    final int getLinkID(){return this.linkID;}
    /**
    <constructor>
    */
    public linkItem(
                   chainModel model,  // the model,owner, of the item
                   operationItem from,// source of the link
                   operationItem to,  // target of the link
                   int linkID         // ID of the link
                   )
    {
        super(model);
        this.from=from; this.to=to; this.linkID=linkID;
        // to make a Item's ID
        this.ID = from.getID()+"->"+to.getID()+"["+linkID+"]";
        // to make a visual part
        this.visual = new visualLink( model.getCanvas(), linkID );
    }
    /**
     * <action>
     * To refresh coordinates of visual component
     * */
    final void refresh()
    {
        Point start = new Point(); Point finish = new Point();
        this.recalculate(start,finish);
        this.visual.relocate( start, finish );
    }
    /**
     * <utility>
     * To recalculate a coordinates and method for draw the link's lines
     * */
    private final void recalculate(Point start,Point finish)
    {
        visualOperation
        visual = (visualOperation)this.to.visual();
        Point inputs[] = visual.getInputPoints();
        visual = (visualOperation)this.from.visual();
        Point outputs[]= visual.getOutputPointsFor(this.linkID);

        int position = visualOperation.TOP;
        double distance = Double.MAX_VALUE;
        // to find short distance between visual operations
        for(int i=0;i < outputs.length;i++)
        {
            for(int j=0;j < inputs.length;j++)
            {
                double dist = outputs[i].distance(inputs[j]);
                if (dist < distance)
                { // to store best results
                  distance = dist; position = i;
                  start.setLocation ( outputs[i] );
                  finish.setLocation( inputs[j]  );
                }
            }
        }
        // to setup a position of output connector
        visual.placeLinkConnector( this.linkID, position );
    }
    /**
    <action>
    Method will call after this item is delete from a chain's model
    */
    final void itemDeleted(){
      this.from.removeOutput( this ); this.to.removeInput( this );
    }
    /**
    <action>
    Method will call after this item is pushed to the chain's model
    */
    final void itemPushed(){
        this.from.addOutput( this );this.to.addInput(this); this.refresh();
    }
}
