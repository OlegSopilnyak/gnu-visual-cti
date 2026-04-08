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
package org.visualcti.briquette.data;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.baseOperationUI;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FetchUI extends baseOperationUI
{
private final static String directionTitleText = "Skip to...";
/**
 * <attribute>
 * The panel of property
 * */
private JPanel property = new JPanel(new BorderLayout());
/**
 * <attribute>
 * The chooser
 * */
private JComboBox editor = new JComboBox();
  /**
   * <constructor>
   * To make a property's panel
   * */
  public FetchUI()
  {
    // to add direction's editor
    UI_Store.addTitle(directionTitleText,property,BorderLayout.NORTH);
    this.property.add(this.editor,BorderLayout.CENTER);
    this.editor.setEditable( false );
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette)
  {
    if (briquette instanceof Fetch)
    {
      final Fetch operation = (Fetch)briquette;
      DefaultComboBoxModel model = new DefaultComboBoxModel(operation.getValidDirections());
      editor.setModel(model);
      editor.setSelectedItem( operation.getDirection() );
//      ActionListener a[] = editor.getActionListeners();
      ActionListener a[] = (ActionListener[])editor.getListeners(ActionListener.class);
      if (a != null && a.length > 0){// to remove old listeners
        for(int i=0;i < a.length;i++) editor.removeActionListener(a[i]);
      }
      editor.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent a)
        {
          String selected = (String)FetchUI.this.editor.getSelectedItem();
          if (selected != null) operation.setDirection(selected);
        }
      });
    }
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.propertiesTree.add(this.property, BorderLayout.NORTH);
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){
    this.property.doLayout(); this.property.repaint();
    this.editor.doLayout();this.editor.repaint();
  }
}
