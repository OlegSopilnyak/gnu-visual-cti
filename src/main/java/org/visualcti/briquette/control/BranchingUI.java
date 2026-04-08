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
package org.visualcti.briquette.control;

import java.awt.*;
import javax.swing.*;

import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust the Branching briquette</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class BranchingUI extends baseOperationUI
{
/**
 * <attribute>
 * The panel of property
 * */
private final JPanel property = new JPanel(new BorderLayout());
/**
 * <attribute>
 * The chooser
 * */
private final LogicUI editor = new LogicUI();
/**
 * <const>
 * The title's text
 * */
private static final String titleText = "Logical expression";

  public BranchingUI()
  {
    JLabel title = new JLabel(BranchingUI.titleText,JLabel.CENTER);
    title.setFont( ReturnUI.titleFont );
    title.setForeground(ReturnUI.titleColor);
    this.editor.setRootLabel("IF");
    this.property.add(title,BorderLayout.NORTH);
    this.property.add(this.editor,BorderLayout.CENTER);
    this.property.add(this.editor.getControlPanel(),BorderLayout.SOUTH);
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette)
  {
    if (briquette instanceof Branching)
    {
      Branching operation = (Branching)briquette;
      this.editor.assign(operation.getContent(),operation);
    }
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.propertiesTree.add(this.property, BorderLayout.CENTER);
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){
    this.property.revalidate();
    /*
    this.property.doLayout(); this.property.repaint();
    this.editor.doLayout();this.editor.repaint();
    */
  }
}
