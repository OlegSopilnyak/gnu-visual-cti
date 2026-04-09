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

import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, </p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class ChainEntityUI extends EntityUI
{
/**
 * <accessor>
 * To get access to Entity's editor panel
 * @return editor's panel
 */
protected final JPanel getEditor()
{
  super.dialog.setTitle("To assign the chain");
  this.entityAssigned();
  return this.editor;
}
/**
 * <notify>
 * To update the Entity's changes
 */
protected final void confirmChanges()
{
  Chain selected = (Chain)this.model.getSelectedItem();
  ((ChainEntity)super.owner).setChain(selected);
}
/**
 * <notify>
 * To decline the Entity's changes
 */
protected final void declineChanges(){}
/**
 * <attribute>
 * The container for comboBox
 */
private final JPanel editor = new JPanel( new BorderLayout() );
/**
 * <attribute>
 * The model of combobox
 * */
private final DefaultComboBoxModel model = new DefaultComboBoxModel();
/**
 * <attribute>
 * The list of available Chains
 * */
private final JComboBox chains = new JComboBox( model );
private final class chainsRenderer extends DefaultListCellRenderer {
    public final java.awt.Component getListCellRendererComponent(
        JList list,
	Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
      super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
      if ( value instanceof Chain )
      {
        Chain chain = (Chain)value;
        String prefix = chain.isLocal() ? "[v] ":"[^] ";
        super.setText(prefix+chain.getName());
      }
      return this;
    }
}
  /**
   * <constructor>
   * */
  public ChainEntityUI()
  {
    this.editor.setBorder(new TitledBorder("Chain"));
    this.editor.add( this.chains, BorderLayout.SOUTH );
    this.chains.setEditable( false );
    this.chains.setRenderer( new chainsRenderer() );
  }
  /**
   * <notify>
   * When assigned the Entity
   */
  protected final void entityAssigned()
  {
    if ( super.owner instanceof ChainEntity )
    {
      this.model.removeAllElements();
      ChainEntity entity = (ChainEntity)super.owner;
      ChainsLibrary lib = entity.getLibrary();
      for( Iterator i=lib.getLocal().iterator();i.hasNext();)
        this.model.addElement( i.next() );
      for( Iterator i=lib.getExternal().iterator();i.hasNext();)
        this.model.addElement( i.next() );
      this.chains.setSelectedItem( entity.getChain() );
    }
  }
}
