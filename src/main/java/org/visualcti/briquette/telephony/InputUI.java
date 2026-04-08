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
 * <p>Description: The User Interface for adjust Input</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class InputUI extends baseOperationUI
{
private final static String beepTitleText = "Beep before...";
private final static String quantityTitleText = "Digits number...";
private final static String timeoutTitleText = "Time to input...";
private final static String terminationTitleText = "To complete input...";
/**
 * <attribute>
 * The owner of this UI
 * */
private Input owner = null;
  /**
   * <editor>
   * Editor for integer Symbol
   * */
  private abstract class IntegerEditor extends SymbolEditor {
    public IntegerEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){return InputUI.this.getDialog();}
  }
  /**
   * <editor>
   * the editor for "quantity"
   * */
  private final class quantityEditor extends IntegerEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getQuantity();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setQuantity(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(1));}
  }
  /**
   * <editor>
   * the editor for "timeout"
   * */
  private final class timeoutEditor extends IntegerEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getTimeout();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setTimeout(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(5));}
  }
  /**
   * to process Symbols chooser only for number
   * */
  private final SymbolChooser getDialog() {
    if (this.owner == null) return null;
    java.util.List all = this.owner.availableSymbols();
    SymbolChooser dialog = SymbolChooser.getInstance(this.params);
    ArrayList list = new ArrayList();
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (symbol != null && symbol.getTypeID() == Symbol.NUMBER)
        list.add(symbol);
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
   * To change the Beep
   * */
  private final void setBeep(boolean beep){
    if (this.owner != null) this.owner.setBeepBefore( beep );
  }
  /**
   * <mutator>
   * To update the quantity property
   * */
  private final void setQuantity(){
    String data = this.quantity.name.getText();
    Symbol symbol = this.owner.getQuantity();
    if (symbol.isConst()) symbol.setName(data);
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
 * visual component for edit beep's property
 * */
private final JComboBox beep = new JComboBox(new Object[]{"true","false"});
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
private final quantityEditor quantity = new quantityEditor();
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
private final timeoutEditor timeout = new timeoutEditor();
/**
 * <attribute>
 * visual component for edit termination's property
 * */
private final terminationUI termination = new terminationUI();
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <constructor>
   * */
  public InputUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    this.params.add(Box.createVerticalStrut(4));
    // to add beep property
    JLabel beepTitle = new JLabel(beepTitleText,JLabel.CENTER);
    beepTitle.setFont(InputUI.titleFont);
    beepTitle.setForeground(WaitUI.titleColor);
    this.params.add(beepTitle);
    this.params.add( this.beep );
    // to add beep's listener
    this.beep.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setBeep( beep.getSelectedIndex()==0 );
      }
    });
    this.params.add(Box.createVerticalStrut(4));
    // to add quantity's property
    JLabel quantityTitle = new JLabel(quantityTitleText,JLabel.CENTER);
    quantityTitle.setFont(InputUI.titleFont);
    quantityTitle.setForeground(InputUI.titleColor);
    this.params.add( quantityTitle );
    this.params.add( this.quantity );
    this.params.add(Box.createVerticalStrut(4));
    // to add quantity's listeners
    this.quantity.name.getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {this.update();}
      public void removeUpdate(DocumentEvent e) {this.update();}
      public void changedUpdate(DocumentEvent e){}
      /** to check and process changes */
      private void update() {if ( !isAssigned ) setQuantity();}
    });
    // to add timeout's property
    JLabel timeoutTitle = new JLabel(timeoutTitleText,JLabel.CENTER);
    timeoutTitle.setFont(InputUI.titleFont);
    timeoutTitle.setForeground(InputUI.titleColor);
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
    // to add termination's property
    JLabel terminationTitle = new JLabel(terminationTitleText,JLabel.CENTER);
    terminationTitle.setFont(InputUI.titleFont);
    terminationTitle.setForeground(InputUI.titleColor);
    this.params.add( terminationTitle );
    JPanel DTMF = new JPanel(new FlowLayout(FlowLayout.CENTER));
    DTMF.add( this.termination );
    this.params.add( DTMF );
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
    this.quantity.reload();
    this.termination.assign( this.owner.getDTMF() );
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
    if (briquette instanceof Input) {
      this.owner=(Input)briquette;
      this.isAssigned = true;
      this.termination.assign( this.owner.getDTMF() );
      this.beep.setSelectedIndex(this.owner.isBeepBefore()?0:1);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
