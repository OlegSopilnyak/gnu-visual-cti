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
package org.visualcti.briquette;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, Base dialog for edit UI components</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public class uiDialog extends JDialog
{
/**
 * <attribute>
 * The place for editor
 */
private final JPanel main = new JPanel(new BorderLayout(2,2),true);
/**
 * <attribute>
 * Button to accept user's input
 * */
private final JButton Accept = new JButton("Accept");
  /**
   * <accessor>
   * To get access to Accept's button
   * @return the button
   */
  public final JButton getAcceptButton(){return this.Accept;}
/**
 * <attribute>
 * Button to decline user's input
 * */
private final JButton Decline = new JButton("Decline");
  /**
   * <accessor>
   * To get access to Decline's button
   * @return the button
   */
  public final JButton getDeclineButton(){return this.Decline;}
/**
 * <attribute>
 * The editor
 */
private JPanel editor = new JPanel();
/**
 * <flag>
 * Is input accepted
 */
private transient boolean accept = false;
/**
 * <flag>
 * for adjust the coordinates
 */
private transient boolean firstTime = true;
/**
 * <accessor>
 * To get access to dialog's result flag
 * @return accept flag
 */
public final boolean isAccept(){return this.accept;}
  /**
   * <constructor>
   * To make modal dialog without title & with empty editor
   * @param owner for make the Frame
   */
  public uiDialog(JComponent owner)
  {
    this( JOptionPane.getFrameForComponent(owner) );
    this.owner=owner;
  }
  /**
   * <constructor>
   * To make modal dialog without title & with empty editor
   * @param frame the owner of the dialog
   */
  public uiDialog(Frame frame)
  {
    super(frame,"",true);
    this.owner = frame;
    // to insert main panel
    super.getContentPane().add(this.main,BorderLayout.CENTER);
    // to insert editor to main panel
    this.main.add(this.editor,BorderLayout.CENTER);
    // to insert buttons to main panel
    JPanel buttons = new JPanel();
    buttons.setBorder( BorderFactory.createRaisedBevelBorder() );
    this.main.add(buttons,BorderLayout.EAST);
    buttons.setLayout(new BoxLayout(buttons,BoxLayout.Y_AXIS));
    // to adjust accept's button
    buttons.add( this.Accept );
    this.Accept.setToolTipText("Press it to confirm changes");
    this.Accept.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        setVisible( false );uiDialog.this.accept = true;
      }
    });
    // to adjust decline's button
    buttons.add( this.Decline );
    this.Decline.setToolTipText("Press it to decline changes");
    this.Decline.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        setVisible( false );uiDialog.this.accept = false;
      }
    });
  }
  /**
   * overrided method for register ESCAPE key
   * */
  protected final JRootPane createRootPane()
  {
    JRootPane rootPane = super.createRootPane();
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener hide = new ActionListener() {
      public final void actionPerformed(ActionEvent e){Decline.doClick();}
    };
    rootPane.registerKeyboardAction(hide, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    return rootPane;
  }
  /**
   * <mutator>
   * To insert the editor's panel to the dialog
   * @param editor the editor's panel
   */
  public final void setEditor(JPanel editor)
  {
    this.accept = false;
    this.main.remove( this.editor );
    this.main.add( this.editor=editor, BorderLayout.CENTER );
    this.main.revalidate();
    super.setResizable(true);
    super.pack();
    super.setResizable(false);
  }
private Component owner;
  /**
   * <mutator>
   * To visualize the dialog (overrided JDialog.setVisible(...))
   * @param flag show/hide flag
   */
  public final void setVisible(boolean flag)
  {
    if (flag == true)
    {
      this.accept = false;
      if ( this.firstTime )
      {
        this.firstTime = false;
        // to center the dialog
        super.setLocationRelativeTo( this.owner );
      }
    }
    super.setVisible( flag );
  }
}
