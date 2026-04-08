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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.visualcti.workflow.model.chainModel;
import org.visualcti.workflow.facade.*;
import org.visualcti.briquette.control.*;
/**
$Header: /VisualCTI_project/src/org/visualcti/workflow/visualChain.java 36    22.02.03 13:40 Olegs $
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, Class for visualize and edit Chain's graph</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public class visualChain extends JPanel
{
/**
 * <colour>
 * The colour of a canvas background
 * */
private Color canvasColor = Color.white;
/**
 * <colour>
 * The colour of a grid's lines
 * */
private Color gridColor = Color.lightGray;
/**
 * <size>
 * A default size of Chain's canvas
 * */
public static final Dimension Size = new Dimension(1280,1024);
/**
 * <attribute>
 * The editor of Chain's properties
 * */
private final UI ui;
    /**
     * <accessor>
     * To get access to Chain's UI
     * */
    public final UI getChainUI(){return this.ui;}
/**
<attribute>
The model of this visual chain
*/
private final chainModel model;
    /**
    <accessor>
    To get access to chain's model
    */
    public final chainModel getModel(){return this.model;}
/**
 * <attribute>
 * The owner of the Graph
 * */
private final ChainWrapper wrapper;
    /**
     * <accessor>
     * To get access to "show about" flag
     * @return flag' value
     */
    public final boolean isShowAbout(){return this.wrapper.isShowAbout();}
    /**
     * <accessor>
     * To get access to IDE's facade
     * */
    final public Facade facade(){return this.wrapper.getIDE();}
    /**
     * <accessor>
     * to get access to briquettes Chain
     * */
    public final BriquettesSequence getBriquettesSequence()
    {
      return this.wrapper.getChain().getSequence();
    }
    /**
     * <visualize>
     * To set visible the operation
     * @param oper the operatio to visualize
     */
    public final void visualize(visualOperation oper)
    {
      if (oper != null) this.wrapper.visualize(oper);
    }
    /**
     * <constructor>
     * To make visual representation of Chain's graph
     * */
    public visualChain(final ChainWrapper wrapper)
    {
        super( null, true );// no layouts, double buffered
        this.wrapper = wrapper;
        // to make the model for this visual chain
        this.model = new chainModel( this );
        // to adjust visual properties
        // to setting up a canvas's default size
        this.setSize( visualChain.Size );
        this.setPreferredSize( visualChain.Size );
        // to setting up a canvas's background color
        this.setBackground( this.canvasColor );

        Chain chain = wrapper.getChain();
        // to place an operations to model
        this.placeChain( chain );
        // to make a properties editor
        ChainUI gui = (ChainUI)UI_Store.getUI( chain );
        try {gui.addNameChangeListener(new ChainUI.NameChangeListener(){
          public final void nameChanged(String name){wrapper.setName(name);}
        });
        }catch(Exception e){e.printStackTrace();}
        this.ui = gui;

        // to make things for process events
        // to attach a mouse's processor
        MouseProcessor mouseProcessor = new MouseProcessor();
        this.addMouseListener(mouseProcessor);
        this.addMouseMotionListener(mouseProcessor);
    }
    /**
     * Class for define chain's action
     * */
    abstract class chainAction {
      abstract void finish(MouseEvent e);
      void cancel(){}
      void mouseMoved(MouseEvent e){}
      void mousePressed(MouseEvent e){}
    }
    /**
     * Class-action to add breakpoint to briquette
     */
    private final class addBreakpoint extends chainAction
    {
      /** mouse pressed */
      final void mousePressed(MouseEvent e){
        if (e.getSource() instanceof visualOperationControl) this.finish(e);
      }
      /** action successful finished */
      final void finish(MouseEvent e)
      {
          visualOperationControl oper = (visualOperationControl)e.getSource();
          if ( !oper.isBreakPoint() )
          {
            Operation briq = oper.getBriquette();
            if (wrapper.getChain().addBreakPoint(briq)) {
              oper.setBreakPoint( true );
              visualBreakpoints visual =
                  (visualBreakpoints)facade().breakpointsPlace();
              visual.breakpointAdded(briq);
            }
          }
          visualChain.this.clearAction();
      }
    }
    /**
     * Class-action for make link
     * */
    private final class makeLinkAction extends chainAction
    {
      /**<attribute> from operation */
      private Operation from;
      /**<attribute> the type of link */
      private int linkID;
      /** <attribute> the link's start point */
      private Point start;
      /** <attribute> the link's end point */
      private Point end = new Point();
      /**<constructor>*/
      makeLinkAction(Operation from,int linkID,Point start)
      {
        this.from=from; this.linkID=linkID;
        this.end.setLocation( this.start=start );
      }
      /** action successful finished */
      final void finish(MouseEvent e)
      {
          visualOperationControl oper = (visualOperationControl)e.getSource();
          Operation to = oper.getBriquette();
          visualChain.this.model.link(this.from,to,this.linkID);
          visualChain.this.clearAction();
      }
      /** the operation canceled */
      final void cancel(){this.line();}
      /** mouse pressed */
      final void mousePressed(MouseEvent e){
        if (e.getSource() instanceof visualOperationControl) this.finish(e);
      }
      /** mouse moved in canvas */
      final void mouseMoved(MouseEvent e){
        //clear old line
        this.line();
        int X = e.getX(),Y=e.getY();
        X = X < 0 ? 0:X; Y = Y < 0 ?0:Y;
        this.end.setLocation( X, Y );
        // to draw new line
        this.line();
      }
      /** to clear old line */
      final private synchronized void line()
      {
        Graphics g = visualChain.this.getGraphics();
        Color old = g.getColor();
        g.setXORMode( Color.black );
        g.setColor( Color.gray );
        g.drawLine(start.x,start.y,end.x,end.y);
        g.setColor( old );
      }
    }
    /**
     * Class-action for add Operation to chain
     * */
    private final class addOperationAction extends chainAction
    {
      /**<attribute> operation */
      private Operation oper;
      /**<constructor>*/
      addOperationAction(Operation oper)
      {
        this.oper=oper;
      }
      /** action successful finished */
      final void finish(MouseEvent e)
      {
        this.oper.move( new Point(e.getX(),e.getY()) );
        /* to place new Operation to model */
        visualChain.this.model.unselectAll();
        visualChain.this.model.place( this.oper, true );
        /* to clear the action */
        visualChain.this.clearAction();
      }
      /** the operation canceled */
      final void cancel(){}
      /** mouse pressed */
      final void mousePressed(MouseEvent e){
        if (e.getSource() == visualChain.this) this.finish(e);
      }
      /** mouse moved in canvas */
      final void mouseMoved(MouseEvent e){}
    }
    /**
     * <action>
     * To begin link's making
     * */
    final void makeLinkFrom(Operation from,int linkID,Point start)
    {
      this.launchAction
          (
          new makeLinkAction(from, linkID, start),
          new Cursor(Cursor.HAND_CURSOR)
          );
    }
    /**
     * <action>
     * To launch the add new Operation to the chain Action
     * */
    final void addOperation(Operation oper)
    {
      this.launchAction
          (
          new addOperationAction(oper),
          UI_Store.getCursor(oper)
          );
    }
    /**
     * <action>
     * To launch the set breakpoint sequence
     */
    final void addBreakpoint()
    {
      Cursor cursor = UI_Store.getCursor("..BreakPoint");
      chainAction action = new addBreakpoint();
      this.launchAction
          (
          new addBreakpoint(),
          UI_Store.getCursor("..BreakPoint")
          );
    }
    /**
     * <action>
     * To delete the breakpoint from Operation
     * @param ID operation's ID
     */
    final void deleteBreakPoint(String ID)
    {
      wrapper.getChain().deleteBreakPoint(ID);
      visualOperation v = this.model.getVisualOperation(ID);
      if ( v != null ) v.setBreakPoint( false );
    }
    /**
     * <notify>
     * From icon notify about breakpoint state changed
     */
    final void breakPointChanged(visualOperationControl icon)
    {
      visualBreakpoints visual = (visualBreakpoints) facade().breakpointsPlace();
      Operation briquette = icon.getBriquette();
      boolean enabled = icon.isEnabledBreakPoint();
      visual.breakpointStateUpdated( briquette );
      this.wrapper.getChain().updateBreakPoint(briquette.getID(),enabled);
    }
    /**
     * To launch the chain's action
     * @param action the action
     * @param actionCursor new defualt cursor
     */
    private final void launchAction(chainAction action,Cursor actionCursor){
      synchronized(this.actionSemaphore)
      {
        if (this.action != null) this.action.cancel();
        this.action = action;
        this.setCursor(actionCursor);
      }
    }
/**
 * <attribute>
 * Current chain's action
 * */
private volatile chainAction action = null;
/**
 * <attribute>
 * The semaphore for chain's action access
 * */
private final Object actionSemaphore = new Object();
    /** when mouse moved in a chain's canvas */
    private final void mouseMoved(MouseEvent e){
      synchronized( this.actionSemaphore ){
        if (this.action != null) this.action.mouseMoved(e);
      }
    }
    /**
     * <notify>
     * To notify from child about mouse pressed
     * */
    final synchronized void mousePressed(MouseEvent e)
    {
      synchronized( this.actionSemaphore )
      {
          if ( this.action != null )
          {
            /* check, is right mouse's button pressed */
            boolean isCancel = (e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0;
            if( isCancel ) {
              // to stop current action and clear reference
              this.clearAction();
            }else {
              // to transfer the event to current action's instance
              this.action.mousePressed(e);
            }
          }
      }
    }
    /**
     * <mutator>
     * To clear a current action
     * */
    private final void clearAction(){
      synchronized( this.actionSemaphore ){
        if (this.action != null) action.cancel();// to cancel current action
        this.action=null;
      }
      // to restore default Cursor
      this.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
    }
    /**
     * <accessor>
     * Check is visual chain Idle
     * */
    final boolean isIdle()
    {
      synchronized( this.actionSemaphore ){return this.action == null;}
    }
    /**
     * <init>
     * To place a operations from chain
     * */
    private final void placeChain(Chain chain){
      // to place an operations
      for(Iterator i = chain.getOperationsList().iterator();i.hasNext(); )
          this.model.push( (Operation)i.next() );
      // to place a links
      for(Iterator i = chain.getOperationsList().iterator();i.hasNext(); )
          this.placeLinks( (Operation)i.next() );
      // to place the breakpoints
      for(Iterator i = chain.getBreakpoints().iterator();i.hasNext();){
        Chain.BreakPoint point = (Chain.BreakPoint)i.next();
        String ID = point.getOperationID();
        visualOperation v = this.model.getVisualOperation(ID);
        v.setBreakPoint(true);
        v.setEnabledBreakPoint( point.isEnabled() );
      }
    }
    /**
     * <init>
     * To place this link between operations
     * */
    private final void placeLinks(Operation oper){
      Operation link[] = oper.getLinks();
      for(int i=0;i < link.length;i++) this.model.link(oper,link[i],i);
    }
    /**
     * <painter>
     * to paint himself without children
     * to draw grid (if needs)
     * */
    protected final void paintComponent(Graphics g)
    {
      Grid grid = this.wrapper.getGrid();
      Dimension place = this.getSize();
      // to clear a canvas, using canvas's color
      g.setColor( this.canvasColor );
      g.fillRect( 0, 0, place.width, place.height);// clear a place
      // to check the grid's activity
      if ( !grid.active ) return;
      // to draw the grid's lines
      int cell = grid.cell;
      // to setup a grid's color
      g.setColor( this.gridColor );
      // to draw vertical lines
      for(int i=0;i <= place.width;i+=cell) g.drawLine(i, 0, i, place.height);
      // to draw horizontal lines
      for(int i=0;i <= place.height;i+=cell) g.drawLine(0, i, place.width, i);
    }
    /**
     * <painter>
     * to paint the border (do nothing)
     * */
    protected final void paintBorder(Graphics g) {}
    /**
     * <translator>
     * To return the coordinates of visualOperation
     * (grid realted)
     * */
    final Point coordinates(Point last)
    {
      Grid grid = this.wrapper.getGrid();
      if ( !grid.active ) return last;
      int X = last.x,Y = last.y;
      X -= X % grid.cell; Y -= Y % grid.cell;
      return new Point(X,Y);
    }
    /**
     * <painter>
     * To draw/clear the shadow of mark
     * */
    private final synchronized void shadow(Graphics g,Rectangle mark){
        g.setXORMode( Color.blue ); g.setColor( Color.gray );
        g.drawRect( mark.x, mark.y, mark.width, mark.height );
    }
/**
 * <attribute>
 * The shadow for mark operations
 * */
private final Rectangle shadow = new Rectangle();
    /**
     * <marker>
     * To mark an operations
     * */
    private final void mark(Rectangle union) {
        Graphics g = this.getGraphics();
        if (this.shadow.x >= 0){   // to erase old shadow
            this.shadow(g,this.shadow);
            this.shadow.x = -1;
        }
        if (union != null){    // to draw shadow
            this.shadow.setBounds(union);
            this.shadow(g,this.shadow);
            // to select marked
            this.model.mark( union );
        }else {
            // refresh the picture
            this.repaint();
        }
    }
    /**
     * <listener>
     * class for process Mouse's events
     * */
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
        private final Point mouse=new Point(),current = new Point();
        /**
         * <attribute>
         * The rectangle for select operations
         * */
        private Rectangle union = new Rectangle();
        /**
        Invoked when a mouse button has been pressed on a component.
        (to begin drag mode)
        */
        public final void mousePressed(MouseEvent e)
        {
          if ( !visualChain.this.isIdle() ) return;
          this.pressed = true; this.dragged = false;
          mouse.setLocation( e.getX(),e.getY() );
          visualChain.this.model.unselectAll();
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
            this.dragged = true; this.current.move( e.getX(), e.getY() );
            this.current.x = this.current.x < 0 ? 0:this.current.x;
            this.current.y = this.current.y < 0 ? 0:this.current.y;
            this.union.setFrameFromDiagonal( this.mouse, this.current );
            // to process drag
            visualChain.this.mark( union );
        }
        /**
        Invoked when a mouse button has been released on a component.
        */
        public final void mouseReleased(MouseEvent e)
        {
          if ( visualChain.this.isIdle() ) {
            this.pressed = false;// to clear pressed flag
            if ( this.dragged ) {
              // to stop drag
              visualChain.this.mark( null );
            }else{
              // to select the operation
            }
            // to clear dragged flag
            this.union.setBounds(0,0,0,0);
            this.dragged = false;
          } else {
            visualChain.this.mousePressed(e);
          }
        }
        /**
        Invoked when the mouse button has been
        moved on a component (with no buttons no down).
        (do nothing)
        */
        public final void mouseMoved(MouseEvent e)
        {
           visualChain.this.mouseMoved(e);
        }
    }
}
