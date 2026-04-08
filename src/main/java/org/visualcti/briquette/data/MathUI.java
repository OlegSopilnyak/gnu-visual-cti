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
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;

import org.visualcti.briquette.baseOperationUI;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust Math's sequence</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class MathUI extends baseOperationUI
{
/**
 * <attribute>
 * The panel of property
 * */
private final JPanel editor = new JPanel(new BorderLayout());
/**
 * <attribute>
 * The UI for math expressions set
 */
private final MathExpressionsSetUI ui=new MathExpressionsSetUI();
/**
 * <const>
 * The title's text
 * */
private static final String titleText = "Math sequence...";
  /**
   * <constructor>
   * To make a property's panel
   * */
  public MathUI()
  {
    UI_Store.addTitle(titleText,this.editor,BorderLayout.NORTH);
    this.editor.add(new JScrollPane(ui.getTree()),BorderLayout.CENTER);
    ui.setRootLabel("Sequence");
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette){
    try {Math value = (Math)briquette;
      this.ui.assign(value.getSet(),value);
    }catch (ClassCastException e){}
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.propertiesTree.add(this.editor, BorderLayout.CENTER);
    super.property.add( this.ui.getControlPanel() );
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.editor.revalidate();}
}
