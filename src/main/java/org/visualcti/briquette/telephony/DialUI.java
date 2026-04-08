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
package org.visualcti.briquette.telephony;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust the Dial</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class DialUI extends baseOperationUI
{
private final static String toDialTitleText = "To dial...";
private final static String makeCallTitleText = "Make call...";
private final static String timeoutTitleText = "Wait answer secs...";
/**
 * <attribute>
 * The owner of this UI
 * */
private Dial owner = null;
  /**
   * <editor>
   * the editor for "toDial"
   * */
  private final class toDialEditor extends SymbolEditor{
    public toDialEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){
      return DialUI.this.getDialog(Symbol.STRING);
    }
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getToDial();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setToDial(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("");}
  }
  /**
   * <editor>
   * the editor for "timeout"
   * */
  private final class timeoutEditor extends SymbolEditor{
    public timeoutEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){
      return DialUI.this.getDialog(Symbol.NUMBER);
    }
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getTimeout();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setTimeout(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(30));}
  }
  /**
   * to process Symbols chooser only for number
   * */
  private final SymbolChooser getDialog(int typeID) {
    if (this.owner == null) return null;
    java.util.List all = this.owner.availableSymbols();
    SymbolChooser dialog = SymbolChooser.getInstance(this.params);
    ArrayList list = new ArrayList();
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (symbol != null && symbol.getTypeID() == typeID) list.add(symbol);
    }
    dialog.setSymbols( list );
    return dialog;
  }
/**
 * <attribute>
 * The list of available Symbols
 * */
private final java.util.List groups = new ArrayList(4);
  /**
   * <mutator>
   * To update the toDial property
   * */
  private final void setToDial(){
    String data = this.toDial.name.getText();
    Symbol symbol = this.owner.getToDial();
    if (symbol.isConst()) symbol.setName(data);
  }
  /**
   * <mutator>
   * To change the makeCall's flag
   * */
  private final void setMakeCall(boolean flag){
    if (this.owner != null) this.owner.setMakeCall( flag );
  }
  /**
   * <mutator>
   * To update the timeout property
   * */
  private final void setTimeout(){
    String data = this.timeout.name.getText();
    Symbol symbol = this.owner.getTimeout();
    if (symbol.isConst()) symbol.setName(data);
  }
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
private final toDialEditor toDial = new toDialEditor();
/**
 * <attribute>
 * visual component for edit makeCall's property
 * */
private final JComboBox makeCall = new JComboBox(new Object[]{"true","false"});
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
private final timeoutEditor timeout = new timeoutEditor();
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <constructor>
   * */
  public DialUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    this.params.add(Box.createVerticalStrut(4));
    // to add toDial's property
    JLabel toDialTitle = new JLabel(toDialTitleText,JLabel.CENTER);
    toDialTitle.setFont(DialUI.titleFont);
    toDialTitle.setForeground(DialUI.titleColor);
    this.params.add( toDialTitle );
    this.params.add( this.toDial );
    this.params.add(Box.createVerticalStrut(4));
    // to add toDial's listeners
    this.toDial.name.getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {this.update();}
      public void removeUpdate(DocumentEvent e) {this.update();}
      public void changedUpdate(DocumentEvent e){}
      /** to check and process changes */
      private void update() {if ( !isAssigned ) setToDial();}
    });
    // to add makeCall property
    JLabel makeCallTitle = new JLabel(makeCallTitleText,JLabel.CENTER);
    makeCallTitle.setFont(DialUI.titleFont);
    makeCallTitle.setForeground(WaitUI.titleColor);
    this.params.add(makeCallTitle);
    this.params.add( this.makeCall );
    // to add beep's listener
    this.makeCall.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setMakeCall( makeCall.getSelectedIndex()==0 );
      }
    });
    this.params.add(Box.createVerticalStrut(4));
    // to add timeout's property
    JLabel timeoutTitle = new JLabel(timeoutTitleText,JLabel.CENTER);
    timeoutTitle.setFont(DialUI.titleFont);
    timeoutTitle.setForeground(DialUI.titleColor);
    this.params.add( timeoutTitle );
    this.params.add( this.timeout );
    this.params.add(Box.createVerticalStrut(4));
    // to add timeout's listeners
    this.timeout.name.getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {this.update();}
      public void removeUpdate(DocumentEvent e) {this.update();}
      public void changedUpdate(DocumentEvent e){}
      /** to check and process changes */
      private void update() {if ( !isAssigned ) setTimeout();}
    });
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.prepareGroups();
    this.timeout.reload();
    this.toDial.reload();
    this.propertiesTree.add(this.params, BorderLayout.NORTH);
  }
  private final void prepareGroups(){
    this.groups.clear();
    if (this.owner == null) return;
    java.util.List all = this.owner.availableSymbols();
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (
          symbol == null  ||
          symbol.isConst()||
          symbol.getTypeID() != Symbol.NUMBER
          ) continue;

      String group = symbol.getGroup();
      if ( !this.groups.contains(group) ) this.groups.add(group);
    }
  }
/**
 * <flag>
 * flag is assign in progress
 * */
private volatile boolean isAssigned = false;
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof Dial) {
      this.owner=(Dial)briquette;
      this.isAssigned = true;
      this.makeCall.setSelectedIndex(this.owner.isMakeCall()?0:1);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
