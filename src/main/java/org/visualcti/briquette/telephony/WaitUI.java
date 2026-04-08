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

import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust the Wait </p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class WaitUI extends baseOperationUI
{
private final static String ringsTitleText = "Rings count";
private final static String timeoutTitleText = "Timeout to wait";
private final static String answerTitleText = "answer to Call?";
  /**
   * <editor>
   * Editor for integer Symbol
   * */
  private abstract class IntegerEditor extends SymbolEditor {
    public IntegerEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){
      return WaitUI.this.getDialog();
    }
  }
  /** the editor for "rings" */
  private final class ringsEditor extends IntegerEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getRings();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setRings(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(0));}
  }
  /** the editor for "timeout" */
  private final class timeoutEditor extends IntegerEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getTimeout();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setTimeout(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(-1));}
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
 * <attribute>
 * visual component for edit rings's property
 * */
//private final JTextField rings = new IntegerTextField();
private final ringsEditor rings = new ringsEditor();
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
//private final JTextField timeout = new IntegerTextField();
private final timeoutEditor timeout = new timeoutEditor();
/**
 * <attribute>
 * visual component for edit answer's property
 * */
private final JComboBox answer = new JComboBox(new Object[]{"true","false"});
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  public WaitUI() {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    this.params.add(Box.createVerticalStrut(4));
    JLabel ringsTitle = new JLabel(ringsTitleText,JLabel.CENTER);
    ringsTitle.setFont(WaitUI.titleFont);
    ringsTitle.setForeground(WaitUI.titleColor);
    this.params.add(ringsTitle);
    this.params.add( this.rings );
    this.params.add(Box.createVerticalStrut(4));
    // to add rings's listeners
    this.rings.name.getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {this.update();}
      public void removeUpdate(DocumentEvent e) {this.update();}
      public void changedUpdate(DocumentEvent e){}
      /** to check and process changes */
      private void update() {if ( !isAssigned ) setRings();}
    });

    JLabel timeoutTitle = new JLabel(timeoutTitleText,JLabel.CENTER);
    timeoutTitle.setFont(WaitUI.titleFont);
    timeoutTitle.setForeground(WaitUI.titleColor);
    this.params.add(timeoutTitle);
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

    JLabel answerTitle = new JLabel(answerTitleText,JLabel.CENTER);
    answerTitle.setFont(WaitUI.titleFont);
    answerTitle.setForeground(WaitUI.titleColor);
    this.params.add(answerTitle);
    this.params.add( this.answer );
    // to add answer's listener
    this.answer.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setAnswer();
      }
    });
  }
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private Wait owner = null;
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.prepareGroups();
    this.rings.reload(); this.timeout.reload();
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
   * <listener>
   * To update the rings property
   * */
  private final void setRings(){
    String data = this.rings.name.getText();
    Symbol symbol = this.owner.getRings();
    if (symbol.isConst()) symbol.setName(data);
  }
  /**
   * <listener>
   * To update the timeout property
   * */
  private final void setTimeout(){
    String data = this.timeout.name.getText();
    Symbol symbol = this.owner.getTimeout();
    if (symbol.isConst()) symbol.setName(data);
  }
  /**
   * <listener>
   * To update the anwser property
   * */
  private final void setAnswer(){
    boolean isAnswer = this.answer.getSelectedIndex() == 0;
    this.owner.setAnswer( isAnswer );
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof Wait) {
      this.owner=(Wait)briquette;
      this.isAssigned = true;
      this.answer.setSelectedIndex(this.owner.isAnswer()?0:1);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){
    this.params.revalidate();/*
    this.params.doLayout(); this.params.repaint();
    this.answer.doLayout(); this.answer.repaint();
    */
  }
}
