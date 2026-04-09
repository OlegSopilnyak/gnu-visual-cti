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
package org.visualcti.briquette.core;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow panel for control parameters list</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class controlPanel extends JPanel
{
/**
 * <action>
 * Called when add pressed
 * */
protected abstract void Add();
/**
 * <action>
 * Called when del pressed
 * */
protected abstract void Del();
/**
 * <action>
 * Called when up pressed
 * */
protected abstract void Up();
/**
 * <action>
 * Called when down pressed
 * */
protected abstract void Down();
/**
 * <action>
 * Called when edit pressed
 * */
protected abstract void Edit();
/**
 * <pool>
 * the pool of buttons
 * */
private Map buttons = Collections.synchronizedMap( new HashMap() );
/**
 * <mutator>
 * to disable all features
 * */
public final void disableAll()
{
  this.add.setEnabled ( false );
  this.del.setEnabled ( false );
  this.up.setEnabled  ( false );
  this.down.setEnabled( false );
  this.edit.setEnabled( false );
}
/**
 * <mutator>
 * to enable add operation's only
 * */
public final void addOnly()
{
  this.add.setEnabled ( true );
  this.del.setEnabled ( false );
  this.up.setEnabled  ( false );
  this.down.setEnabled( false );
  this.edit.setEnabled( false );
}
/**
 * <mutator>
 * to enable edit operation's only
 * */
public final void editOnly()
{
  this.add.setEnabled ( false );
  this.del.setEnabled ( false );
  this.up.setEnabled  ( false );
  this.down.setEnabled( false );
  this.edit.setEnabled( true );
}
/**
 * <mutator>
 * to enable all operations
 * */
public final void allFeatures()
{
  this.add.setEnabled ( true );
  this.del.setEnabled ( true );
  this.up.setEnabled  ( true );
  this.down.setEnabled( true );
  this.edit.setEnabled( true );
}
/**
 * <accessor>
 * To get access to button by name
 * */
public JButton getButton(String name)
{
  if( name == null || "".equals(name) ) return null;
  return (JButton)this.buttons.get( name.toLowerCase() );
}
/**
 * <constructor>
 * */
public controlPanel()
{
  super(new FlowLayout(FlowLayout.CENTER,2,2),true);
  Insets insets = new Insets(1,1,1,1);String name;
  name = "add"; this.buttons.put(name,add);
  this.add( add ); add.setMargin(insets);add.setActionCommand(name);
  add.addActionListener(new ActionListener(){
    public final void actionPerformed(ActionEvent e){Add();}
  });
  name = "del"; this.buttons.put(name,del);
  this.add( del );del.setMargin(insets);del.setActionCommand(name);
  del.addActionListener(new ActionListener(){
    public final void actionPerformed(ActionEvent e){Del();}
  });
  name = "up"; this.buttons.put(name,up);
  this.add( up );up.setMargin(insets);up.setActionCommand(name);
  up.addActionListener(new ActionListener(){
    public final void actionPerformed(ActionEvent e){Up();}
  });
  name = "down"; this.buttons.put(name,down);
  this.add( down );down.setMargin(insets);down.setActionCommand(name);
  down.addActionListener(new ActionListener(){
    public final void actionPerformed(ActionEvent e){Down();}
  });
  name = "edit"; this.buttons.put(name,edit);
  this.add( edit );edit.setMargin(insets);edit.setActionCommand(name);
  edit.addActionListener(new ActionListener(){
    public final void actionPerformed(ActionEvent e){Edit();}
  });
}
private final JButton add = new JButton("+");
private final JButton del = new JButton("-");
private final JButton up = new JButton("^");
private final JButton down = new JButton("V");
private final JButton edit = new JButton("...");
}
