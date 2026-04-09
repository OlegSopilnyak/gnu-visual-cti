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
import javax.swing.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The dialog for edit Entity's properties</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public abstract class EntityUI
{
/**
 * <accessor>
 * To get access to Entity's editor panel
 * @return editor's panel
 */
protected abstract JPanel getEditor();
/**
 * <notify>
 * To update the Entity's changes
 */
protected abstract void confirmChanges();
/**
 * <notify>
 * To decline the Entity's changes
 */
protected abstract void declineChanges();
/**
 * <notify>
 * To notify about Entity's assigment
 * */
protected abstract void entityAssigned();
  /**
   * <action>
   * Main method of the UI
   */
  public final void editEntity()
  {
    this.dialog.setEditor( this.getEditor() );
    this.dialog.setVisible( true );
    if ( this.dialog.isAccept() )
      this.confirmChanges(); else this.declineChanges();
  }
/**
 * <attribute>
 * The dialog for edit the entity
 */
protected uiDialog dialog = null;
/**
 * <init>
 * To init the UI
 * @param owner the owner of the dialog
 */
public final void initUI(JComponent owner)
{
  if ( this.dialog == null ) this.dialog = new uiDialog( owner );
}
/**
 * <attribute>
 * The reference to Entity to edit
 * */
protected Entity owner = null;
  /**
   * <mutator>
   * To assign the entity with UI
   * @param owner the entity to edit
   */
  public final void assign(Entity owner){this.owner=owner; this.entityAssigned();}
}
