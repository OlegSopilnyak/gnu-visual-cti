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

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The UI for edit LogicalExpression</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class LogicalExpressionUI extends ExpressionUI
{
/**
 * <attribute>
 * The type of expression
 */
private int type = Symbol.ANY;
  /**
   * <constructor>
   * */
  public LogicalExpressionUI()
  {
    // to add the operations
    this.addOperation("<");
    this.addOperation(">");
    this.addOperation("=");
    this.addOperation("!=");
    this.addOperation("<=");
    this.addOperation(">=");
    this.addOperation("LIKE");
    // to adjust the editors
    super.first.disableTitle();
    super.second.disableTitle();
    super.first.setEnabled(true);
    super.operation.enable();
    super.second.setEnabled(true);
    // to add listeners
    this.first.name.getDocument().addDocumentListener(new nameListener(){
      protected final void nameChanged(){firstName();}
    });
    this.second.name.getDocument().addDocumentListener(new nameListener(){
      protected final void nameChanged(){secondName();}
    });
  }
  /**
   * <listener>
   * Listener of Operand name's changes
   * */
  private abstract class nameListener implements DocumentListener
  {
    protected abstract void nameChanged();
    public void insertUpdate(DocumentEvent e) {this.nameChanged();}
    public void removeUpdate(DocumentEvent e) {this.nameChanged();}
    public void changedUpdate(DocumentEvent e){}
  }
private final Map buttons = new HashMap();
  /**
   * To add operation's feature
   * */
  private final void addOperation(final String operation) {
    JToggleButton button = super.operation.add(operation);
    this.buttons.put( operation, button );
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e) {
        value.setOperation(operation);
      }
    });
  }
  /**
   * <notify>
   * To notify the UI about first operand's changes
   * @param symbol new value
   */
  protected final void firstChanged(Symbol symbol){
    if (symbol.getTypeID() != this.type ){
      this.type = symbol.getTypeID();
      this.firstTypeChanged();
    }
  }
  /* the notify about first operand type's changes */
  private final void firstTypeChanged(){
    JToggleButton like = (JToggleButton)this.buttons.get("LIKE");
    if ( this.type == Symbol.STRING ){
      like.setEnabled(true);
      Symbol second = super.second.getSymbol();
      String name = second.getName();
      second.setType(Symbol.STRING);second.setName(name);
      super.second.reload();
    }else
    if ( this.type == Symbol.NUMBER ){
      like.setEnabled(false);
      super.operation.select("=");
      Symbol second = super.second.getSymbol();
      if ( second.getTypeID() != Symbol.NUMBER ){
        second.setType(Symbol.NUMBER); second.setName("0");
        super.second.reload();
      }
    }
  }
  /**
   * to process first's name changes
   * */
  private final void firstName(){
    JTextField name = super.first.name;
    // symbol's changes
    if ( name.isEditable() ){// to adjust the new const value
      Symbol symbol = super.first.getSymbol();
      int oldType = symbol.getTypeID();
      this.adjustName( symbol, name.getText() );
      this.type = symbol.getTypeID();
      if ( oldType != this.type ) this.firstTypeChanged();
    }
  }
  /**
   * to process second's name changes
   * */
  private final void secondName(){
    JTextField name = super.second.name;
    // symbol's changes
    if ( name.isEditable() ){// to adjust the new const value
      Symbol symbol = super.second.getSymbol();
      this.adjustName( symbol, name.getText() );
      if ( this.type == Symbol.NUMBER && symbol.getTypeID() != Symbol.NUMBER ){
        symbol.setType(Symbol.NUMBER); symbol.setName("0");
      }
    }
  }
  /**
   * To adjust the symbol with new name
   * @param field symbol to edit
   * @param text new value of name
   */
  private final void adjustName(Symbol value,String text){
    // to store new name to the symbol
    if ( value.isConst() ) value.setName( text );
    // is it possible to use it as Number
    if ( value.isMayNumber() && value.getTypeID() != Symbol.NUMBER) {
      value.setType(Symbol.NUMBER); value.setName(text);
    }else if ( "".equals(text) ) {
      value.setType(Symbol.STRING);
    }
    super.value.changed();
  }
/**
 * <pool>
 * The pool of available Symbol's groups
 * */
private final java.util.List groups = new ArrayList(10);
  /**
   * <accessor>
   * To get access to availabled groups (for SymbolChooser)
   * */
  protected final java.util.List availableGroups(){return this.groups;}
/**
 * <pool>
 * The pool of available Symbols
 * */
private final java.util.List symbols = new ArrayList(100);
/**
 * To make the list of symbol in group of operand
 * @param forWho symbol-operand
 * @return the symbols list
 */
  private final java.util.List getGroupSymbols(Symbol forWho){
    ArrayList symbols = new ArrayList();
    boolean isConstSelected = this.isConstSelected(forWho);
    int selectedGroupID = this.groupID(forWho);
    for(Iterator i=this.symbols.iterator();i.hasNext();){
      Symbol symbol=(Symbol)i.next();
      if (
          (symbol.isConst() && isConstSelected) ||
           symbol.getGroupID() == selectedGroupID
          )
      {
        symbols.add(symbol);
      }
    }
    return symbols;
  }
  /**
   * <accessor>
   * To get access to availabled Symbols (for SymbolChooser)
   * */
  protected final java.util.List availableSymbols(Symbol forWho){
    java.util.List groupSymbols = this.getGroupSymbols(forWho);
    if ( forWho == value.first ) return groupSymbols;
    else{
      int type = value.first.getTypeID();
      ArrayList symbols = new ArrayList();
      for(Iterator i=groupSymbols.iterator();i.hasNext();){
        Symbol symbol=(Symbol)i.next();
        switch( type )
        {
          case Symbol.STRING:
            symbols.add(symbol);
            break;
          case Symbol.NUMBER:
            if ( symbol.getTypeID() == Symbol.NUMBER ) symbols.add(symbol);
            break;
        }
      }
      return symbols;
    }
  }
  /* to get access to selected group name */
  private final Object getSelectedGroup(Symbol symbol){
    if (super.first.getSymbol() == symbol) {
      return super.first.group.getSelectedItem();
    }else
    if (super.second.getSymbol() == symbol) {
      return super.second.group.getSelectedItem();
    }else{
      return null;
    }
  }
  /** get access to group's ID for Symbol (is const)*/
  private final boolean isConstSelected(Symbol symbol){
    Object selected = this.getSelectedGroup(symbol);
    return SymbolEditor.CONST.equals(selected);
  }
  /** get access to group's ID for Symbol */
  private final int groupID(Symbol symbol){
    Object selected = this.getSelectedGroup(symbol);
    return Symbol.getGroupID(selected);
  }

  /**
   * to assign the expression to edit
   * */
  public final void assign(LogicalExpression expression,Operation owner)
  {
    // to collect symbols from Operation
    this.collectSymbols(owner);
    // to assign the first/operation/second
    super.assign( expression );
    // to store the type of first
    this.type = super.value.first.getTypeID();
  }
  /*
   * to collect all available Symbols for expression
   * */
  private final void collectSymbols(Operation owner) {
    this.symbols.clear();
    if (owner != null) this.symbols.addAll( owner.availableSymbols() );
    // to prepare the groups
    this.groups.clear(); Symbol symbol = null;
    ArrayList stringList = new ArrayList(100);
    ArrayList numericList = new ArrayList(100);
    // to iterate the dirty symbols list
    for(Iterator i=this.symbols.iterator();i.hasNext();){
      if ( (symbol=(Symbol)i.next()) == null || "".equals(symbol.getName())) continue;
      int type = symbol.getTypeID();
      if (type == Symbol.NUMBER || type == Symbol.STRING) {
        if ( type == Symbol.STRING ) stringList.add(symbol);
        if ( type == Symbol.NUMBER ) numericList.add(symbol);
        String group = symbol.getGroup();
        if ( !this.groups.contains(group) ) this.groups.add(group);
      }
    }
    // finally to validate the symbols list
    this.symbols.clear();
    this.symbols.addAll(stringList);
    this.symbols.addAll(numericList);
  }
}
