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
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The control of Briquette's icon</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

final class visualOperationControl extends JPanel
{
/**
 * <attribute>
 * The array of input links Points
 * */
private static final Point
  input[] = new Point[]{new Point(),new Point(),new Point(),new Point()};
/**
 * <attribute>
 * The array of output links Points (for Yes alternative)
 * */
private static final Point
  outputYes[] = new Point[]{new Point(),new Point(),new Point(),new Point()};
/**
 * <attribute>
 * The array of output links Points (for No alternative)
 * */
private static final Point
  outputNo[] = new Point[]{new Point(),new Point(),new Point(),new Point()};
/**
 * static section for install a basis coordinates
 * */
static {
  // to make a this component's corners Points
  Point corner[] = new Point[ 4 ];
  corner[0] = new Point( 0,          0 );
  corner[1] = new Point( visualOperation.IconSize.width, 0 );
  corner[2] = new Point( visualOperation.IconSize.width, visualOperation.IconSize.height );
  corner[3] = new Point( 0,          visualOperation.IconSize.height );
  // to make input points
  input[ visualOperation.TOP    ].setLocation( center(corner[0], corner[1]) );
  input[ visualOperation.RIGHT  ].setLocation( center(corner[1], corner[2]) );
  input[ visualOperation.BOTTOM ].setLocation( center(corner[2], corner[3]) );
  input[ visualOperation.LEFT   ].setLocation( center(corner[3], corner[0]) );
  // to make an output points
  outputNo [ visualOperation.TOP    ].setLocation( center(corner[0],input[visualOperation.TOP])   );
  outputYes[ visualOperation.TOP    ].setLocation( center(input[visualOperation.TOP],corner[1])   );
  outputNo [ visualOperation.RIGHT  ].setLocation( center(corner[1],input[visualOperation.RIGHT]) );
  outputYes[ visualOperation.RIGHT  ].setLocation( center(input[visualOperation.RIGHT],corner[2]) );
  outputNo [ visualOperation.BOTTOM ].setLocation( center(corner[2],input[visualOperation.BOTTOM]));
  outputYes[ visualOperation.BOTTOM ].setLocation( center(input[visualOperation.BOTTOM],corner[3]));
  outputNo [ visualOperation.LEFT   ].setLocation( center(corner[3],input[visualOperation.LEFT])  );
  outputYes[ visualOperation.LEFT   ].setLocation( center(input[visualOperation.LEFT],corner[0])  );
}
/**
* To make a center point between 2 Point
* */
private static Point center(Point p1, Point p2){Point result = new Point();
  double X = (p1.getX()+p2.getX()) / 2.0,Y = (p1.getY()+p2.getY()) / 2.0;
  result.setLocation( X, Y );
  return result;
}
/**
 * The pool of coordinates
 * */
private final Connectors conn = new Connectors();
/**
 * <init>
 * To calculate a connections coordinates
 * */
private final void calculateConnectors() {
    for(int i=0;i < 4;i++){
      this.conn.input[i] = new Point(visualOperationControl.input[i]);
      this.conn.outputYes[i] = new Point(visualOperationControl.outputYes[i]);
      this.conn.outputNo[i] = new Point(visualOperationControl.outputNo[i]);
    }
}
/***
 * <connector>
 * The buttom for make an YES connector (by default)
 * */
private final linkConnector YES = new linkConnector("Yes");
/***
 * <connector>
 * The buttom for make a NO connector (by alternate)
 * */
private final linkConnector NO = new linkConnector("No");
 /**
  * <attribute>
  * Maximum Operation link's ID
  * */
private int maxLinkID = Operation.DEFAULT_LINK;
 /**
  * breakpoint's visualization class
  */
private final class BreakPoint extends JLabel{
  private transient boolean enabled=true;
  private final Dimension size;
  private BreakPoint(){
    ImageIcon bIcon = getImageIcon("/icon/BreakPoint.gif");
    size = new Dimension(bIcon.getIconWidth(),bIcon.getIconHeight());
    this.setIcon(bIcon); this.setSize(size);
    // to add mouse listener
    MouseProcessor mouseListener = new MouseProcessor();
    this.addMouseListener(mouseListener);
    this.addMouseMotionListener(mouseListener);
  }
  public final void setEnabled(boolean flag){this.enabled=flag;repaint();}
  public final boolean isEnabled(){return this.enabled;}
  protected final void paintComponent(Graphics g) {
    if ( !this.enabled ) {
      Color old = g.getColor();
      g.setColor(Color.red);
      g.drawOval(1,1,size.width-2,size.height-2);
      g.drawOval(2,2,size.width-4,size.height-4);
      g.setColor(old);
    }else super.paintComponent(g);
  }
  /**
   * class for process Mouse events on the breakpoint
   */
  final private class MouseProcessor
      extends MouseAdapter
      implements MouseMotionListener
  {
    private transient boolean pressed = false;
    public final void mousePressed(MouseEvent e){translateEvent(e,true);}
    public final void mouseReleased(MouseEvent e)
    {
      if (pressed && breakPoint.isVisible()) {
        enabled = !enabled; pressed = false; repaint();
        owner.breakPointChanged();
      }else
        translateEvent(e,false);
    }
    public final void mouseDragged(MouseEvent e){translateEvent(e,false);}
    public final void mouseMoved(MouseEvent e){translateEvent(e,false);}
    private final void translateEvent(MouseEvent event,boolean pressed){
      breakPoint.getParent().dispatchEvent(event); this.pressed=pressed;
    }
  }
}
/**
<attribute>
Icon for understand a breakpoint
*/
private final BreakPoint breakPoint = new BreakPoint();
/**
 * <container>
 * The owner of this control
 */
private final visualOperation owner;
/**
 * <accessor>
 * To get access to the briquette
 * @return the reference to briquette
 */
public final Operation getBriquette(){return this.owner.getBriquette();}
  /**
   * <contructor>
   * To make the control using briquette
   * @param briquette the owner of control
   * @param owner the owner of control
   */
  public visualOperationControl(Operation briquette,visualOperation owner)
  {
    super(null,true);this.owner=owner;
    // break point part
    this.placeBreakpoint();
    // connectors part
    this.calculateConnectors();
    if ( briquette.getLinkIDs().length > 0)
    { // the operator have a connector
      this.addYesConnector();
    }
    if ( briquette.getLinkIDs().length > 1 )
    {   // the operation have 2 connectors
      this.addNoConnector();
    }
    // to place the icon
    this.placeOperationIcon(briquette);
    // tooltip part, to connect component to a manager
    ToolTipManager.sharedInstance().registerComponent( this );
    // the geometry
    this.setSize( visualOperation.IconSize );
    this.setPreferredSize( visualOperation.IconSize );
    this.setMinimumSize( visualOperation.IconSize );
    this.setMaximumSize( visualOperation.IconSize );
  }
  private final void placeBreakpoint() {
    this.breakPoint.setLocation(0, 0);
    this.breakPoint.setVisible( false );
  }
  private final void addNoConnector() {
    this.add(this.NO);
    this.maxLinkID = Operation.ALTERNATE_LINK;
    this.NO.coordinates(this.conn.outputNo);
    this.NO.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        connect(e,Operation.ALTERNATE_LINK);
      }
    });
    this.NO.right();
  }
  private final void addYesConnector() {
    this.add(this.YES);// by default connector
    this.YES.coordinates( this.conn.outputYes );
    this.YES.addActionListener( new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        connect(e,Operation.DEFAULT_LINK);
      }
    });
    this.YES.right();
  }
  private final void placeOperationIcon(Operation briquette) {
    // Icon part
    JLabel icon = new JLabel( UI_Store.getIcon(briquette) );
    icon.setSize( visualOperation.IconSize );
    icon.setLocation(0, 0);
    this.add( icon );// to place operation's Icon
    this.add(this.breakPoint);// to place a breakpoint Icon
  }
  /**
   * <visualize>
   * To show the Operation's tooltip
   * @param event the mouse's event
   * @return the text of tooltip
   */
  public final String getToolTipText(MouseEvent event)
  {
    return this.owner.getBriquette().getAbout();
  }
    /**
     * <notify>
     * Notified when link's connector pressed
     * @param a the event
     * @param linkID the ID of the link
     */
    private final void connect(ActionEvent a,int linkID){
      linkConnector source = (linkConnector)a.getSource();
      Point location = source.getLocation();
      Point operLoc = source.getParent().getLocation();
      location.translate(operLoc.x,operLoc.y);
      this.owner.activateConnect(linkID,location);
    }
private transient boolean executed = false;
  /**
   * <accessor>
   * To get access to executed flag
   * @return the value of flag
   */
  public final boolean isExecuted(){return executed;}
  /**
   * <mutator>
   * To change the exceuted flag
   * @param executed new value of flag
   */
  public final void setExecuted(boolean executed)
  {
    this.executed = executed;this.repaint();
  }
  /**
   * <accessor>
   * Check is operation is break-point
   * */
  final boolean isBreakPoint(){return this.breakPoint.isVisible();}
  /**
   * <mutator>
   * To setting up a breakpoint's flag
   * */
  final void setBreakPoint(boolean flag)
  {
    this.breakPoint.setVisible( flag );
    this.breakPoint.setEnabled( flag );
  }
  /**
   * <accessor>
   * Is breakpoint enabled
   * */
  final boolean isEnabledBreakPoint(){return this.breakPoint.isEnabled();}
  /**
   * <mutator>
   * To set enabled break point
   * */
  final void setEnabledBreakPoint(boolean enable)
  {
    this.breakPoint.setEnabled(enable);
  }
  /**
   * <accessor>
   * To get access to input Points
   * */
  final Point[] getInputPoints(){return this.conn.input;}
  /**
   * <accessor>
   * To get access to output Points
   * */
  final Point[] getOutputPointsFor(int linkID)
  {
    switch( linkID )
    {
      case Operation.DEFAULT_LINK:
          // to process by default ID
          return this.conn.outputYes;

      case Operation.ALTERNATE_LINK:
          // to process by alternate ID
          return
            this.maxLinkID!=Operation.ALTERNATE_LINK ? null:this.conn.outputNo;

      // unknown ID
      default: return null;
    }
  }
  /**
   * <move>
   * To move a connector to some operation's side
   * */
  public final void placeLinkConnector(int linkID,int side)
  {
      linkConnector link = this.YES;// link to place
      if (linkID == Operation.ALTERNATE_LINK) link = this.NO;
      switch( side )
      {
        case visualOperation.TOP:
                  // place link to top side
                  link.top(); break;
        case visualOperation.RIGHT:
                  // place link to right side
                  link.right(); break;
        case visualOperation.BOTTOM:
                  // place link to bottom side
                  link.bottom(); break;
        case visualOperation.LEFT:
                  // place link to left side
                  link.left(); break;
      }
  }
  /**
   * <mutator>
   * To change the location of the icon in visualChain's canvas
   * The location changed, signal
   * overrided JComponent.setLocation(Point p)
   * */
  public final void setLocation(Point p)
  {
      Point newPlace = this.owner.owner().coordinates( p );
      // to recalculate connectors's coordinates
      for(int i=0;i < 4;i++)
      {
        this.conn.input[i].setLocation(visualOperationControl.input[i]);
        this.conn.input[i].translate(newPlace.x,newPlace.y);

        this.conn.outputYes[i].setLocation(visualOperationControl.outputYes[i]);
        this.conn.outputYes[i].translate(newPlace.x,newPlace.y);

        this.conn.outputNo[i].setLocation(visualOperationControl.outputNo[i]);
        this.conn.outputNo[i].translate(newPlace.x,newPlace.y);
      }
      // to move the Operation
      this.owner.getBriquette().move( newPlace );
  }
  /**
  <painter>
  to paint the children
  */
  protected final void paintChildren(Graphics g)
  {
      Color current = g.getColor();
      Color operColor = this.owner.isSelected()?
                            SystemColor.activeCaption:
                            SystemColor.inactiveCaption;
      g.setColor( operColor );
      g.fillRect(0, 0, visualOperation.IconSize.width-1, visualOperation.IconSize.height-1);
      if ( this.executed ) drawExecutedBorder(g);
      g.setColor(current);
      super.paintChildren( g );
  }
    private final static void drawExecutedBorder(Graphics g){
      g.setColor(Color.red);
      int width = visualOperation.IconSize.width-1;
      int height = visualOperation.IconSize.height-1;
      // to draw the red border
      g.drawRoundRect(0,0,width,height,3,3);
      g.setColor(Color.white);
      // to draw the white border
      g.drawRoundRect(1,1,width-2,height-2,3,3);
    }
    /**
     * <producer>
     * To make the image by icon's path
    */
    private static final ImageIcon getImageIcon(String iconPath){
        return UI_Store.makeIcon(visualOperationControl.class,iconPath);
    }
    /**
     * <coordinates>
     * Class for represent a coordinates sets
     * */
    private final class Connectors {
      private Point input[] = new Point[4];
      private Point outputYes[] = new Point[4];
      private Point outputNo[] = new Point[4];
    }
    /**
    <link>
    Class for represent the link button
    */
    private static final class linkConnector extends JButton {
        /**
         * <attribute>
         * the coordinates
         * */
        private final Point[] coord = new Point[4];
        /**
         * <constructor>
         * to make a connector buttom, using meta-name
         * */
        public linkConnector(String meta)
        {
            Dimension size = new Dimension(6,6);
            ImageIcon link = getImageIcon("/icon/"+meta+"Connector.gif");
            if (link != null){
                // to seting up new size of button
                size = new Dimension(link.getIconHeight()-1,link.getIconWidth()-1);
                this.setIcon(link);
                this.setPressedIcon(getImageIcon("/icon/"+meta+"ConnectorPressed.gif"));
                this.setSelectedIcon(getImageIcon("/icon/"+meta+"ConnectorPressed.gif"));
            }
            //this.setCursor( new Cursor(Cursor.CROSSHAIR_CURSOR) );
            // to setup a geometry
            this.setSize(size);this.setPreferredSize(size);this.setOpaque(false);
            this.setBorderPainted(false);this.setFocusPainted(false);
        }
        /**
         * <mutator>
         * To attach the coordinate of button's center
         * */
        public final void coordinates(Point []coordinate)
        {
            int delta = this.getSize().width;
            int half = delta / 2;//delta--;
            for(int i=0;i < 4;i++) this.coord[i]=(Point)coordinate[i].clone();
            coord[visualOperation.TOP].translate(-half,-1);
            coord[visualOperation.RIGHT].translate(-delta,-half);
            coord[visualOperation.BOTTOM].translate(-half,-delta);
            coord[visualOperation.LEFT].translate(-1,-half);
        }
        /**
         * <move>
         * To move button to Top side of Operator
         * */
        public final void top()
        {
            this.setLocation(this.coord[visualOperation.TOP]);
        }
        /**
         * <move>
         * To move button to Right side of Operator
         * */
        public final void right()
        {
            this.setLocation(this.coord[visualOperation.RIGHT]);
        }
        /**
         * <move>
         * To move button to Bottom side of Operator
         * */
        public final void bottom()
        {
            this.setLocation(this.coord[visualOperation.BOTTOM]);
        }
        /**
         * <move>
         * To move button to Left side of Operator
         * */
        public final void left()
        {
            this.setLocation(this.coord[visualOperation.LEFT]);
        }
    }
}
