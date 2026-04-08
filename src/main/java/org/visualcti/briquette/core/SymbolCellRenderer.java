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
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.plaf.ColorUIResource;
import java.io.*;

import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The tree cell's renderer for SymbolChooser</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class SymbolCellRenderer extends JPanel implements TreeCellRenderer {

  private final JLabel       icon;
  private final TreeTextArea text;

  public SymbolCellRenderer() {
    setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
    this.icon = new JLabel() {
      public void setBackground(Color color) {
        super.setBackground(color instanceof ColorUIResource? null:color);
      }
    };
    add(this.icon);
    add(Box.createHorizontalStrut(4));
    add(this.text  = new TreeTextArea());
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value,
               boolean isSelected, boolean expanded,
               boolean leaf, int row, boolean hasFocus) {
    Object data = ((DefaultMutableTreeNode)value).getUserObject();
    boolean isSymbol = data instanceof Symbol;
    String  stringValue;
    if ( isSymbol )
      stringValue = ((Symbol)data).cell();
    else
      stringValue = tree.convertValueToText(value, isSelected,expanded, leaf, row, hasFocus);

    super.setEnabled( tree.isEnabled() );
    this.text.setText( stringValue );
    this.text.setSelect( isSelected );
    this.text.setFocus(hasFocus);
    if (leaf) {
      if ( isSymbol )
        this.icon.setIcon(UIManager.getIcon("Tree.leafIcon"));
      else
        this.icon.setIcon(UIManager.getIcon("Tree.leafIcon"));
    } else if (expanded) {
      this.icon.setIcon(UIManager.getIcon("Tree.openIcon"));
    } else {
      this.icon.setIcon(UIManager.getIcon("Tree.closedIcon"));
    }
    return this;
  }

  public Dimension getPreferredSize() {
    Dimension iconD = this.icon.getPreferredSize();
    Dimension textD = this.text.getPreferredSize();
    int height = iconD.height < textD.height ? textD.height:iconD.height;
    return new Dimension(iconD.width + textD.width, height);
  }

  public void setBackground(Color color) {
    super.setBackground(color instanceof ColorUIResource? null:color);
  }

  private static class TreeTextArea extends JTextField {
    Dimension preferredSize;
    TreeTextArea() { super.setOpaque(true); }
    public void setBackground(Color color) {
        super.setBackground(color instanceof ColorUIResource? null:color);
    }
    public void setPreferredSize(Dimension d) {
      if (d != null) this.preferredSize = d;
    }
    public Dimension getPreferredSize() {return preferredSize;}
    public void setText(String str) {
      FontMetrics fm = getToolkit().getFontMetrics(super.getFont());
      int width = SwingUtilities.computeStringWidth( fm, str );
      this.setPreferredSize(new Dimension(width +6,fm.getHeight()));
      super.setText(str);
    }
    void setSelect(boolean isSelected) {
      Color bColor;
      if (isSelected) {
        bColor = UIManager.getColor("Tree.selectionBackground");
      } else {
        bColor = UIManager.getColor("Tree.textBackground");
      }
      super.setBackground(bColor);
    }
    void setFocus(boolean hasFocus) {
      if (hasFocus) {
        Color lineColor = UIManager.getColor("Tree.selectionBorderColor");
        setBorder(BorderFactory.createLineBorder(lineColor));
      } else {
        setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
      }
    }
  }
}
