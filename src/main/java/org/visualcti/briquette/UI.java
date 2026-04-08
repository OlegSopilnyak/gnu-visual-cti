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
package org.visualcti.briquette;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

import java.awt.Toolkit;
import java.text.*;
import java.util.Locale;
/**
User Interface,
parent of any User Interfaces
*/
public interface UI
{
/**
 * <const>
 * The font for property's title
 * */
Font titleFont = new Font("monospaced",Font.PLAIN,10);
/**
 * <const>
 * The color for property's title
 * */
Color titleColor = Color.blue;
    /**
     * <accessor>
     * is properties ReadOnly
     * */
    boolean isReadOnly();
    /**
     * <mutator>
     * set ReadOnly flag
     * */
    void setReadOnly(boolean flag);
    /**
    <accessor>
    get access to Operation's Icon
    */
    javax.swing.ImageIcon getIcon();
    /**
     * <accessor>
     * To get access to UI's Cursor
     * */
    java.awt.Cursor getCursor();
    /**
     * <action>
     * To edit asigned operation's properties in a place
     * parameters:
     * place - the place, where will edit
     * */
    void activate( javax.swing.JPanel place );
  /**
   * <formater>
   * The class for validating JTextField by Integer input
   * */
  final public static class onlyIntegerDocument extends validatingDocument {
      /** to validate the string as Integer */
      protected final void validate(String str) throws Exception {
        if (str.length() > 0) Integer.parseInt(str);
      }
  }
  /**
   * <formater>
   * The class for validating JTextField by Integer input
   * */
  final public static class onlyDoubleDocument extends validatingDocument {
      /** to validate the string as Double */
      protected final void validate(String str) throws Exception {
        if (str.length() > 0) Double.parseDouble(str);
      }
  }
  /**
   * <formater>
   * The class for validating JTextField by input
   * */
  public abstract static class validatingDocument extends PlainDocument {
      private final Toolkit toolkit = Toolkit.getDefaultToolkit();
      /** to validate the string */
      protected abstract void validate(String str) throws Exception;
      /** call when want insert the text */
      public final void insertString(int offs, String str, AttributeSet a)
          throws BadLocationException {

          String currentText = getText(0, getLength());
          String beforeOffset = currentText.substring(0, offs);
          String afterOffset = currentText.substring(offs, currentText.length());
          String proposedResult = beforeOffset + str + afterOffset;

          try {this.validate(proposedResult);
          } catch (Exception e) {
              this.toolkit.beep(); return;
          }
          super.insertString(offs, str, a);
      }
      /** call when want remove the text */
      public final void remove(int offs, int len) throws BadLocationException {
          String currentText = getText(0, getLength());
          String beforeOffset = currentText.substring(0, offs);
          String afterOffset = currentText.substring(len + offs,
                                                     currentText.length());
          String proposedResult = beforeOffset + afterOffset;

          try {this.validate(proposedResult);
          } catch (Exception e) {
              this.toolkit.beep(); return;
          }
          super.remove(offs, len);
      }
  }
}
