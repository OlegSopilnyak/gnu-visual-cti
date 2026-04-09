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

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Cursor;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

abstract public class UI_adapter implements UI
{
/**
 * <const>
 * The briquette's icon
 * */
protected static final ImageIcon briquetteIcon =
    UI_Store.makeIcon(UI.class,"/icons/briquette.gif");
/**
 * <flag>
 * is edit will activated in ReadOnly mode?
 * */
private boolean ro=false;
  /**
   * <accessor>
   * is properties ReadOnly
   * */
  public boolean isReadOnly(){return this.ro;}
  /**
   * <mutator>
   * set ReadOnly flag
   * */
  public  void setReadOnly(boolean flag){this.ro=flag;}
  /**
   * <constructor>
   * To make a stub of UI
   * */
  public UI_adapter(){}
/**
 * <attribute>
 * The Icon of editable object
 * */
 protected ImageIcon theIcon = briquetteIcon;
  /**
   * <accessor>
   * To get access to Object's icon
   * */
  public ImageIcon getIcon(){return this.theIcon;}
/**
 * <attribute>
 * The Icon of editable object
 * */
 protected Cursor theCursor = new Cursor(Cursor.DEFAULT_CURSOR);
  /**
   * <accessor>
   * To get access to UI's Cursor
   * */
   public Cursor getCursor(){return this.theCursor;}
  /**
   * <action>
   * to activate UI before editing
   * */
  abstract public void activate(JPanel place);
}
