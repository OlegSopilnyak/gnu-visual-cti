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
import java.lang.reflect.*;

import org.visualcti.briquette.*;
import org.visualcti.workflow.model.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The visualization of link between Operations </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Prominic Inc & Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class visualLink extends visualChainPiece
{
  /**
   * <constructor>
   * To make a link's visualization
   * */
  public visualLink(visualChain chain,int linkID)
  {
    super(chain);
    this.color = linkID==Operation.DEFAULT_LINK ? Color.green:Color.red;
  }
/**
 * <attribute>
 * The color of link
 * */
private final Color color;
/**
 * <attribute>
 * The starting point of link
 * */
private final Point start = new Point();
/**
 * <attribute>
 * The ending point of link
 * */
private final Point finish = new Point();
  /**
   * <mutator>
   * To setting new location of link
   * */
   public final void relocate(Point from,Point to)
   {
      if (from.x < to.x) {
          this.start.setLocation(from);
          this.finish.setLocation(to);
      }else{
          this.start.setLocation(to);
          this.finish.setLocation(from);
      }
      Rectangle border = new Rectangle();
      border.setFrameFromDiagonal(start,finish);
      Point corner = border.getLocation();
      if (corner.equals(from) || corner.equals(to))
      {
        this.start.setLocation(2,2);
        this.finish.setLocation(border.width+2,border.height+2);
      }else {
        this.start.setLocation(2,border.height+2);
        this.finish.setLocation(border.width+2,2);
      }
      if (border.width == 0) border.width++;
      if (border.height== 0) border.height++;
      border.x-=2;border.y-=2;
      border.width += 4; border.height += 4;

      this.setSize( border.getSize() );
      this.setLocation( border.getLocation() );
   }
/**
 * <attribute>
 * The method for draw a link
 * */
private Method draw = linkAlgorithm.simpleAlg;
  /**
   * <mutator>
   * To change the draw method
   * */
  public final void setDrawMethod(Method draw){this.draw = draw;}
    /**
    <painter>
    to paint himself without children
    */
    protected void paintComponent(Graphics g)
    {
        // {Graphics.class,Point.class,Point.class,Color.class}
        Object parameters[]=new Object[]{g,this.start,this.finish,this.color};
        // to invoke draw's method
        try{
          this.draw.invoke(null,parameters);
        }catch(Exception e){
          e.printStackTrace();
        }
    }
    /**
    <painter>
    to paint the border
    */
    protected void paintBorder(Graphics g){}
    /**
    <painter>
    to paint the children
    */
    protected void paintChildren(Graphics g){}
}
