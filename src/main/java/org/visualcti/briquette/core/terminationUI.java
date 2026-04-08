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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow for visualize & edit DTMFs set</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class terminationUI extends JPanel {
  /*
  public static void main(String[] args)
  {
    JFrame frame = new JFrame();
    frame.setSize(200,200);
    frame.setLocation(100,100);
    terminationUI ui = new terminationUI();
    termination exp = new termination("1,2,3,4,5,6,7,8,9,0,*,#");
    JPanel place = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ui.assign(exp);place.add(ui);
    frame.getContentPane().add(place);
    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){System.exit(0);}
    });
    frame.pack();
    frame.setVisible(true);
  }
  */

/**
 * <attribute>
 * The reference to owner of UI
 * */
private termination owner=null;
/**
 * <attribute>
 * The pool of CheckBoxs
 * */
private HashMap pool = new HashMap();
  /**
   * <constructor>
   * */
  public terminationUI()
  {
    GridLayout layout = new GridLayout(4,3,0,0);
    super.setLayout(layout);
    this.addCheck("1");this.addCheck("2");this.addCheck("3");
    this.addCheck("4");this.addCheck("5");this.addCheck("6");
    this.addCheck("7");this.addCheck("8");this.addCheck("9");
    this.addCheck("*");this.addCheck("0");this.addCheck("#");
  }
  /**
   * <init>
   * To add check box
   * */
  private final void addCheck(String name){
    JRadioButton visual = new JRadioButton(name);
    visual.setFont( new Font("serif", Font.PLAIN, 12) );
    super.add(visual);
    this.pool.put(name,visual);
    visual.setFocusPainted(false);
    visual.setActionCommand(name);
    visual.addItemListener( new check() );
  }
  /**
   * <mutator>
   * To assign the owner
   * */
  public final void assign(termination owner)
  {
    this.assigned = true;
    this.owner = owner;
    for(Iterator i=this.pool.keySet().iterator();i.hasNext();)
    {
      String name = (String)i.next();
      JRadioButton visual = (JRadioButton)this.pool.get(name);
      visual.setSelected( owner.isSignalOn(name) );
    }
    this.assigned = false;
  }
/**
 * <flag>
 * is changes during assign
 * */
private volatile boolean assigned = false;
  /**
   * <process>
   * To process selected event
   * */
  private final void processSelection(String name,boolean selected){
    if (this.owner == null || this.assigned) return;
    this.owner.setSignal(name,selected);
  }
  /**
   * <listener>
   * The listener of CheckBox
   * */
  private final class check implements ItemListener {
    public void itemStateChanged(ItemEvent e){
      if (e.getID() != ItemEvent.ITEM_STATE_CHANGED || assigned) return;
      boolean selected = e.getStateChange() == ItemEvent.SELECTED;
      String name = ((JRadioButton)e.getItem()).getActionCommand();
      processSelection(name,selected);
    }
  }
}
