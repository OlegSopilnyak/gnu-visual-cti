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
package org.visualcti.briquette.core;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow the dialog for edit the VoxSource</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class VoxSourceDialog extends JDialog {
/**
 * <const>
 * The title of the Dialog
 * */
public final static String TITLE = "Adjust the Sound's source";
/**
 * <producer>
 * To get one instance of the Dialog
 * */
public static final VoxSourceDialog getInstance(Component component)
{
  if (VoxSourceDialog.dialog != null) return VoxSourceDialog.dialog;
  synchronized(TITLE){
    if (VoxSourceDialog.dialog == null) {
      Frame frame = JOptionPane.getFrameForComponent(component);
      VoxSourceDialog.dialog = new VoxSourceDialog(frame);
    }
  }
  return VoxSourceDialog.dialog;
}
/**
 * <member>
 * Singleton
 * */
private static VoxSourceDialog dialog = null;
/**
 * <flag>
 * Is changes accepted
 * */
private boolean accepted = false;
/**
 * <attribute>
 * Editable source
 * */
private VoxSource source = null;
/**
 * <attribute>
 * The UI for edit the source
 * */
private final VoxSourceUI ui = new VoxSourceUI();
  /**
   * <mutator>
   * To transfer to dialog availabled Symbols
   * */
  public final void setAvailabledSymbols(java.util.List availabled)
  {
    this.ui.setAvailabledSymbols(availabled);
  }
  /**
   * <accessor>
   * To get access to updated source
   * */
  public VoxSource getSource() {return this.accepted ? this.source:null;}
  /**
   * <mutator>
   * To setup the source for edit
   * */
  public void setSource(VoxSource source) {
    this.accepted = false;
    this.ui.assign( this.source=source.copy() );
  }
  /**
   * <constructor>
   * */
  private VoxSourceDialog(Frame parent)
  {
    super(parent, TITLE, true);
    JPanel content = new JPanel(true);
    super.getContentPane().add(content);
    content.setLayout(new BoxLayout(content,BoxLayout.X_AXIS));
    // to add UI's panel
    JPanel uiPanel = new JPanel(new BorderLayout(),false);
    uiPanel.add(this.ui, BorderLayout.NORTH);
    uiPanel.setBorder(BorderFactory.createTitledBorder("[The Sound]"));
    content.add(uiPanel);
    content.add(Box.createHorizontalStrut(3));
    // to add control panel
    JPanel control = new JPanel(false);
    control.setLayout(new BoxLayout(control,BoxLayout.Y_AXIS));
    JButton ok = new JButton("Accept");
    control.add(Box.createVerticalStrut(3));
    control.add(ok);
    ok.setMargin(new Insets(1,1,1,1));
    ok.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        accepted = true; setVisible( false );
      }
    });
    JButton cancel = new JButton("Decline");
    control.add(cancel);
    cancel.setMargin(new Insets(1,1,1,1));
    cancel.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        accepted = false; setVisible( false );
      }
    });
    content.add(control);
    // to configure the geometry
    super.setSize(300,80); super.setLocation(100,100);
    super.setResizable(false);
  }
  /*
  // testing entry
  public static void main(String[] args)
  {
    JFrame frame = new JFrame();
    frame.setSize(200,200);
    frame.setLocation(100,100);
    mediaSourceUI ui = new VoxSourceUI();
    final VoxSource exp = new VoxSource();
    JPanel place = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ui.assign(exp);
    final java.util.List availabled =  new Chain(null).getPredefinedSymbols();
    ui.setAvailabledSymbols(availabled);
    final JButton dlg = new JButton("VoxSource dialog");
    place.add(dlg);
    dlg.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        VoxSourceDialog dialog = VoxSourceDialog.getInstance(dlg);
        dialog.setSource( exp );
        dialog.setAvailabledSymbols(availabled);
        dialog.setVisible( true );
System.out.println( "Choosed "+dialog.getSource() );
      }
    });
    frame.getContentPane().add(place);
    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){System.exit(0);}
    });
    frame.pack();
    frame.setVisible(true);
  }
  */
}
