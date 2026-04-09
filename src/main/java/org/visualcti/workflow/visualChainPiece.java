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

import javax.swing.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The visual chain's piece (parent of visualOperation & visualLink) </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Prominic Inc & Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class visualChainPiece extends JPanel
{
/**
  * <attribute>
  * Flag is operator selected (by default, not selected)
  * */
private volatile boolean selected;
  /**
   * <accessor>
   * to return selcted flag's value
   * */
  public final boolean isSelected(){return this.selected;}
  /**
   * <mutator>
   * to set selection flag to ON
   * */
  public final void select(){this.selected=true;this.repaint();}
  /**
   * <mutator>
   * to set selection flag to OFF
   * */
  public final void unselect(){this.selected=false;this.repaint();}
/**
 * <attribute>
 * The owner of this piece
 * */
private final visualChain owner;
  /**
   * <accessor>
   * to get acceess to owner (for notification)
   * */
  protected visualChain owner(){return this.owner;}

  /**
   * <accessor>
   * To get access to location of piece in the canvas
   * @return
   */
  public java.awt.Point getPieceLocation(){return super.getLocation();}

  /**
   * <constructor>
   * to make a base properties
   * */
  public visualChainPiece(visualChain chain)
  {   // without layout manager & use a double buffering paint
      super(null, true);
      // to save an owner
      this.owner = chain;
      // to unselect piece
      this.selected = false;
  }
  /**
   * <notify>
   * To notify piece, about it dragged
   * */
  public void dragged(java.awt.Point delta){}
  /**
   * <notify>
   * To notify piece, about finish drag
   * */
  public void stopDrag(){}
}
