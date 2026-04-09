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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.visualcti.briquette.uiDialog;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the dialog fo edit the LogicalExpression</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public final class LogicalExpressionDialog extends uiDialog
{
private final JPanel editor = new JPanel(new BorderLayout());
private final TitledBorder editorTitle;
private transient LogicalExpressionUI expressionUI;

  public LogicalExpressionDialog(Frame frame, String title, boolean modal) {
    super(frame);super.setTitle(title);super.setModal(modal);
    this.editorTitle = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(142, 142, 142)),"Editor");
    this.editor.setBorder(this.editorTitle);
    this.editor.setPreferredSize(new Dimension(350, 125));
  }
  public LogicalExpressionUI getExpressionUI() {return this.expressionUI;}
  public void setExpressionUI(LogicalExpressionUI expressionUI)
  {
    this.editor.removeAll();
    this.editor.add(this.expressionUI=expressionUI,BorderLayout.SOUTH);
    super.setEditor( this.editor );
  }
  public void setEditorTitle(String title){this.editorTitle.setTitle(title);}
  public boolean isAccepted() {return super.isAccept();}
}
