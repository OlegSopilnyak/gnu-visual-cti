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

import org.visualcti.briquette.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.visualcti.workflow.model.chainModel;
/**
<visual>
Class for visualize the Operation
*/
public class visualOperation extends visualChainPiece
{
/**
 * <size>
 * The size of Icon-component
*/
public final static Dimension IconSize = new Dimension( 40, 40 );
/**
 * <size>
 * The size of component
 */
public final static Dimension SIZE = new Dimension( 100, 120 );
/**
 * <const>
 * The index of connector's placement
 * */
public static final int TOP = 0;
/**
 * <const>
 * The index of connector's placement
 * */
public static final int RIGHT = 1;
/**
 * <const>
 * The index of connector's placement
 * */
public static final int BOTTOM = 2;
/**
 * <const>
 * The index of connector's placement
 * */
public static final int LEFT = 3;
/**
 * <attribute>
 * The chain's data model
 * */
private final chainModel model;
/**
 * <attribute>
 * the refernce to briquette Operation
*/
private final Operation oper;
/**
 * <accesor>
 * To get access to briquette's operation
 * */
public final Operation getBriquette(){return this.oper;}
/**
 * <attribute>
 * the reference to Operation's UI
*/
private final UI operUI;
/**
 * <visual>
 * The operation's icon component
 */
private final visualOperationControl icon;
/**
 * <visual>
 * The operation's about area
 */
private final JLabel about=new JLabel();
/**
 * <accesor>
 * To get access to Operation's UI
 * */
public final UI getOperationUI(){return this.operUI;}
    /**
     * <accessor>
     * Check is operation is break-point
     * */
    public final boolean isBreakPoint()
    {
      return this.icon.isBreakPoint();
    }
    /**
     * <mutator>
     * To setting up a breakpoint's flag
     * */
    public final void setBreakPoint(boolean flag)
    {
      this.icon.setBreakPoint(flag);
    }
    /**
     * <accessor>
     * Is breakpoint enabled
     * */
    public final boolean isEnabledBreakPoint()
    {
      return this.icon.isEnabledBreakPoint();
    }
    /**
     * <mutator>
     * To set enabled break point
     * */
    public final void setEnabledBreakPoint(boolean enable)
    {
      this.icon.setEnabledBreakPoint(enable);
    }
    /**
     * <notify>
     * From icon notify about breakpoint state changed
     */
    final void breakPointChanged()
    {
      super.owner().breakPointChanged( this.icon );
    }
    /**
     * <accessor>
     * To get access to briquette's executed flag
     * @return the flag's value
     */
    public final boolean isExecuted(){return this.icon.isExecuted();}
    /**
     * <mutator>
     * To change value of the briquette's executed flag
     * @param executed new value
     */
    public final void setExecuted(boolean executed){this.icon.setExecuted(executed);}
    /**
     * <constructor>
     * to make a visual representation of operation
     */
    public visualOperation( Operation oper, visualChain chain )
    {
        super( chain );// to store the owner
        super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS) );
        this.model = chain.getModel();// to store the owner's model
        // to store a main attributes
        this.operUI = UI_Store.getUI( this.oper = oper );
        this.oper.addListener(new Operation.Listener(){
          public final void operationChanged(Operation o){repaint();}
        });
        // to make a visual representation
        this.icon = new visualOperationControl(oper,this);
        JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER,1,1));
        panel.setOpaque(false);
        panel.add(this.icon);
        super.add(panel);

        JPanel aPanel = new JPanel(new BorderLayout(0,0));
        super.add(aPanel);aPanel.setOpaque(false);
        this.about.setVerticalAlignment(JLabel.TOP);
        aPanel.add(this.about,BorderLayout.CENTER);

        this.setSize( SIZE );
        this.setPreferredSize(SIZE);
        this.setOpaque(false);
        this.setLocation( oper.coordinates() );
        // to make things for process events
        // to attach a mouse's processor
        MouseProcessor mouseProcessor = new MouseProcessor();
        this.icon.addMouseListener(mouseProcessor);
        this.icon.addMouseMotionListener(mouseProcessor);
    }
    /**
     * <notify>
     * Notified by linkConnector pressed
     * @param linkID ID of the link
     * @param location location of link button
     */
    final void activateConnect(int linkID,Point location){
      Point place = this.getLocation();
      location.translate(place.x,place.y);
      owner().makeLinkFrom(visualOperation.this.oper,linkID,location);
    }
    /**
     * <accessor>
     * To get access to input Points
     * */
    public Point[] getInputPoints(){return this.icon.getInputPoints();}
    /**
     * <accessor>
     * To get access to output Points
     * */
    public Point[] getOutputPointsFor(int linkID)
    {
      return this.icon.getOutputPointsFor(linkID);
    }
    /**
     * <move>
     * To move a connector to some operation's side
     * */
    public final void placeLinkConnector(int linkID,int side)
    {
      this.icon.placeLinkConnector(linkID,side);
    }
private final static Point iconMargin = new Point((SIZE.width-IconSize.width)/2-1,0);
  public final java.awt.Point getPieceLocation(){return this.getIconLocation();}
  public final Point getIconLocation()
  {
    Point location = this.getLocation();
    location.translate(iconMargin.x,iconMargin.y);
    return location;
  }
  public final Dimension getIconSize(){return IconSize;}
    /**
     * <mutator>
     * The location changed, signal
     * overrided JComponent.setLocation(Point p)
     * */
    public final void setLocation(Point p)
    {
        Point newPlace = this.owner().coordinates( p );
        // to recalculate icon's parts
        this.icon.setLocation( newPlace );
        // call a visual component's method
        // using owner related coordinates
        newPlace.translate(-iconMargin.x,-iconMargin.y);
        super.setLocation( newPlace );
    }
private static final java.text.MessageFormat aboutFormat=
new java.text.MessageFormat
    ( "<html><center><h4><font color=\"#000000\"size=\"2\">"+
      "{0}"+
      "<br></font></h4></center></html>");
    /**
    <painter>
    to paint himself without children
    to draw grid (if needs)
    */
    protected final void paintComponent(Graphics g)
    {
      String about = aboutFormat.format(new Object[]{this.oper.getAbout()});
      if ( !about.equals(this.about.getText()) ) this.about.setText(about);
      this.about.setVisible(this.showAbout);
    }
/**
 * <flag>
 * The flag, is needs to show the about's text
 */
private boolean showAbout=false;
    /**
     * <mutator>
     * To enable/disable the text label under Operation's Icon
     * @param flag the flag
     */
    public final void setShowAbout(boolean flag){this.showAbout=flag;invalidate();}
    /**
    <painter>
    to paint the border
    Painting the border and Link's outputs
    */
    protected final void paintBorder(Graphics g)
    {
        // to draw default border
        super.paintBorder(g);
    }
    /**
    <painter>
    to paint the children
    */
    protected final void paintChildren(Graphics g)
    {
        super.paintChildren( g );
    }
    /**
     * <notify>
     * Operation selected by mouse click
     * */
    private final void selectByMousePressed(MouseEvent event){
        this.model.select( this, event );
/*
        boolean set = this.isBreakPoint();
        boolean enabled = this.isEnabledBreakPoint();
        if ( !set ) {
          //System.out.println("from invisible");
          this.setBreakPoint( true );
          this.setEnabledBreakPoint(true);
        }else if ( set && enabled ){
          //System.out.println("from visible & enabled");
          this.setEnabledBreakPoint(false);
        }else if( set && !enabled ){
          //System.out.println("from visible & !enabled");
          this.setBreakPoint( false );
          this.setEnabledBreakPoint(false);
        }
*/
    }
    /**
     * <notify>
     * Operation dragged by mouse
     * */
    private final void draggedByMouse(Point delta){
      this.model.dragSelected( delta );
    }
    /**
     * <notify>
     * Operation has dropped
     * */
    private final void stopDragByMouse(Point delta){
      this.model.dropSelected( delta );
    }

/**
* <const>
* The point of no shadow
* */
private static final Point noShadow = new Point(Short.MIN_VALUE,Short.MIN_VALUE);
/**
 * <point>
 * Last location of operation's shadow
 * */
private final Point lastPoint = new Point(noShadow);
    /**
     * <notify>
     * To notify piece, about it dragged
     * */
    public final void dragged(Point delta)
    {
      Graphics g = this.getParent().getGraphics();
      Point location = this.getLocation();
      location.translate(iconMargin.x,iconMargin.y);
      synchronized(this.lastPoint)
      {
        if ( this.lastPoint.x != noShadow.x )
        {   // to erase old shadow
            this.shadow(g, this.lastPoint);
        }
        this.lastPoint.setLocation(location.x+delta.x,location.y+delta.y);
      }
      this.shadow(g, this.lastPoint);
    }
    /**
     * <notify>
     * To notify piece, about finish drag
     * */
    public final void stopDrag()
    {
      Graphics g = this.getParent().getGraphics();
      synchronized(this.lastPoint)
      {
        if ( this.lastPoint.x != noShadow.x )
        {   // to erase old shadow
            this.shadow(g, this.lastPoint);
            this.setLocation(this.lastPoint);
            // to stop a clearing of shadow
            this.lastPoint.setLocation( noShadow );
        }
      }
    }
    /**
     * <painter>
     * To draw/clear the shadow of operation
     * */
    private final void shadow(Graphics g,Point where)
    {
        g.setXORMode( Color.black );
        g.setColor( Color.gray );
        g.drawRect(where.x,where.y,IconSize.width,IconSize.height);
    }
    /**
    <listener>
    class for process Mouse events
    */
    final private class MouseProcessor
                            extends MouseAdapter
                            implements MouseMotionListener
    {
        /**
        <flag>
        Flag, is mouse button preeesd on this visualOperation
        */
        private volatile boolean pressed = false;
        /**
         * <flag>
         * is operation was draged between press and release
         * */
        private volatile boolean dragged = false;
        /**
         * <attribute>
         * Pressed point
         * */
        private final Point mouse=new Point(),delta = new Point();
        /**
         * <event>
         * preeesed event
         * */
        private volatile MouseEvent pressEvent=null;
        /**
        Invoked when a mouse button has been pressed on a component.
        (to begin drag mode)
        */
        public final void mousePressed(MouseEvent e)
        {
            if ( !(this.pressed = owner().isIdle()) )  return;
            this.dragged = false;
            mouse.setLocation( e.getX(),e.getY() );
            this.pressEvent = e;
        }
        /**
        Invoked when a mouse button is pressed on
        a component and then dragged.
        Mouse drag events will continue to
        be delivered to the component where
        the first originated until the mouse button
        is released (regardless of whether the mouse position
        is within the bounds of the component).
        [to process drag]
        */
        public final void mouseDragged(MouseEvent e)
        {
            if ( !this.pressed ) return;
            this.dragged = true;
            this.delta.x = e.getX()-this.mouse.x;
            this.delta.y = e.getY()-this.mouse.y;
            if ( visualOperation.this.isSelected() ){
              // to drag shadow
              visualOperation.this.draggedByMouse( this.delta );
            }else {
              // to select unselected operation before to drag
              visualOperation.this.selectByMousePressed( this.pressEvent );
            }
        }
        /**
        Invoked when a mouse button has been released on a component.
        */
        public final void mouseReleased(MouseEvent e){
          if ( owner().isIdle() ) {
            if ( !this.pressed ) return;
            this.pressed = false;// to clear pressed flag
            if ( this.dragged ) {
              this.delta.x = e.getX()-this.mouse.x;
              this.delta.y = e.getY()-this.mouse.y;
              // to stop drags shadow
              visualOperation.this.stopDragByMouse(this.delta);
            }else{
              // to select the operation
              visualOperation.this.selectByMousePressed( this.pressEvent );
            }
            this.dragged = false;// to clear dragged flag
          }else {
            owner().mousePressed( e );
          }
        }
        /**
        Invoked when the mouse button has been
        moved on a component (with no buttons no down).
        (to dispatch this event to a parent)
        */
        public final void mouseMoved(MouseEvent e)
        {
          if ( owner().isIdle() ) return;
          Point point = visualOperation.this.getLocation();
          Point iconPoint = visualOperation.this.icon.getLocation();
          point.translate(iconPoint.x,iconPoint.y);
          e.translatePoint(point.x,point.y);// translate point
          visualOperation.this.owner().dispatchEvent(e);// to dispatch event to visualChain
          e.translatePoint(-point.x,-point.y);// untranslate point
        }
    }
}
