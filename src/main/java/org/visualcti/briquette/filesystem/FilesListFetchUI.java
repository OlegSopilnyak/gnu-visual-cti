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
package org.visualcti.briquette.filesystem;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * The User Interface to adjust the briquette</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class FilesListFetchUI extends baseOperationUI
{
private final static String directionTitleText = "Direction";
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private FilesListFetch owner = null;
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
/**
 * <attribute>
 * The editor of the direction
 */
private final DefaultComboBoxModel directionModel = new DefaultComboBoxModel();
private final JComboBox direction = new JComboBox( directionModel );
  public FilesListFetchUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the direction's editor
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( directionTitleText, this.params);
    this.params.add( this.direction );
    this.direction.setEditable( false );
    // to add location's listener
    this.direction.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned ) owner.setDirection( direction.getSelectedIndex() );
      }
    });
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.direction.setSelectedIndex( this.owner.getDirection() );

    this.propertiesTree.add(this.params, BorderLayout.NORTH);
  }
/**
 * <flag>
 * flag is assign in progress
 * */
private volatile boolean isAssigned = false;
  /**
   * <notify>
   * To notify about Operation assigned
   * @param briquette assigned
   */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof FilesListFetch) {
      this.owner=(FilesListFetch)briquette;
      this.isAssigned = true;
      this.directionModel.removeAllElements();
      String[] dirs=FilesListFetch.Direction;
      for(int i=0;i < dirs.length;i++) this.directionModel.addElement(dirs[i]);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}

}
