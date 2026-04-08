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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow the UI for MathExpression's item</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class MathExpressionUI extends ExpressionUI {
/**
 * <attribute>
 * The type of the MathExpression
 * */
private int type = Symbol.ANY;
/**
 * <editor>
 * The panel for place a visual components
 * */
private final JPanel editor = new JPanel(true);
  /**
   * <maker>
   * To make the editor
   * */
  private final void makeEditor(){
    this.editor.setLayout(new BoxLayout(this.editor,BoxLayout.X_AXIS));
    this.editor.add(Box.createHorizontalStrut(8));
    this.editor.add( new JLabel("!") );
    this.editor.add(Box.createHorizontalStrut(5));
    this.editor.add( this.name );
    this.name.setDocument(new UI.validatingDocument(){
      protected final void validate(String str) throws Exception{
        try {NumberFormat.getNumberInstance().parse(str);
        }catch(ParseException e){
          return;// str is valid target's name
        }
        throw new Exception("Invalid name of target");
      }
    });
    this.editor.add( this.list );
    this.list.setMargin(new Insets(1,1,1,1));
    this.list.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        MathExpressionUI.this.targetList();
      }
    });
  }
/**
 * <editor>
 * Visual component for edit the name of target
 * */
private final JTextField name = new JTextField();
/**
 * <editor>
 * Visual component for choose the Symbol from list
 * */
private final JButton list = new JButton("...");
/**
 * <pool>
 * the pool of buttons-operations
 * */
private final HashMap buttons = new HashMap();
  /** Constructor */
  public MathExpressionUI() {
    this.makeEditor();
    super.add(this.editor,0);
    super.add(Box.createVerticalStrut(5),0);
    this.addOperation("+");
    this.addOperation("-");
    this.addOperation("/");
    this.addOperation("*");
    super.first.title.setText("1");
    super.second.title.setText("2");
    super.first.setEnabled(false);
    super.operation.disable();
    super.second.setEnabled(false);
    // to add listeners
    this.name.getDocument().addDocumentListener(new nameListener(){
      protected final void nameChanged(){targetName();}
    });
    this.first.name.getDocument().addDocumentListener(new nameListener(){
      protected final void nameChanged(){firstName();}
    });
    this.second.name.getDocument().addDocumentListener(new nameListener(){
      protected final void nameChanged(){secondName();}
    });
  }
  /**
   * To add operation's feature
   * */
  private final void addOperation(final String operation)
  {
    JToggleButton button = super.operation.add(operation);
    this.buttons.put(operation,button);
    button.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e) {
        value.setOperation( operation );
      }
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
  /**
   * <producer>
   * To make new Symbol's stub for target
   * */
  protected Symbol newSymbol(){return Symbol.newLocal("",Symbol.ANY);}
  /**
   * <process>
   * to process target's name changes
   * */
  private final void targetName(){
    if (this.assign) return;
    // get a changed information from the textfield
    String text = this.name.getText();
    Symbol target = this.newSymbol();
    if ("".equals(text)) {// the name is empty
      this.type = Symbol.ANY;
      target = Symbol.newConst("");
      // to disable the Expression's UI
      super.first.setEnabled(false);
      super.operation.disable();
      super.second.setEnabled(false);
    } else {// the name is not empty
      // to enable first operand
      super.first.setEnabled(true);
    }
    // to adjust type and name of target's Symbol
    target.setType( this.type ); target.setName( text );
    // to store new target to MathExpression's value
    ((MathExpression)this.value).setTarget(target);
  }
  /**
   * <process>
   * To process list-button
   * */
  private final void targetList(){
    MathExpression val = ((MathExpression)this.value);
    SymbolChooser dialog = SymbolChooser.getInstance(this);
    java.util.List symbols = this.groupList(val.targetGroupID());
    if ( symbols.size() <= 0 )
    {
      JOptionPane.showMessageDialog
              (
              dialog.getParent(),
              "No availabled Symbols",
              "Symbols set...",
              JOptionPane.INFORMATION_MESSAGE
              );
      return;
    }
    dialog.setSymbols( symbols );
    dialog.setChoosed( val.getTarget() );
    dialog.setVisible( true );
    Symbol target = dialog.getChoosed();
    if (target != null) {
      this.assign = true;// begin assigment
      val.setTarget(target);
      this.name.setText( target.getName() );
      this.name.setEditable(false);
      this.type = target.getTypeID();
      super.value.first.setType(this.type);
      super.first.reload();
      // to enable first operand
      super.first.setEnabled(true);
      this.assign = false;// end assigment
    }
  }
  /**
   * <check>
   * Check is this new MathExpression (may change the type)
   * */
  private final boolean isNewExpression(){
    return this.name.isEditable();
  }
  /**
   * <process>
   * to process first's name changes
   * */
  private final void firstName(){
    if ( this.assign ) return;
    // to solve the data
    String text = super.first.name.getText();
    if ( "".equals(text) ) {
      this.firstEmptyName(); return;
    }
    Symbol first = super.value.first;
    Symbol second = super.value.second;
    // to setup new name from text-field
    if ( first.isConst() ) first.setName(text);
    // to analize the type
    if (this.type == Symbol.ANY || this.isNewExpression())
    { // to define new target Symbol's type
      if ( first.isConst() && first.isMayNumber() ) {
        // to change the type of first's operand
        first.setType( Symbol.NUMBER ); first.setName(text);
        if (second.getTypeID() != Symbol.NUMBER) {
          // to setup Number type of second operand
          second.setType(Symbol.NUMBER); super.second.reload();
        }
      }
      this.type = first.getTypeID(); this.targetName();
    }
    this.afterFirst();// adjust operations set and second value
    this.value.changed();
  }
  /**
   * <process>
   * To process operation and second
   * */
  private final void afterFirst(){
    Symbol second = super.value.second;
    switch( this.type ) {
      case Symbol.NUMBER:
        this.enableAllOperations();
        if ( second.getTypeID() != Symbol.NUMBER) {
          super.value.second = Symbol.newConst(new Integer(0));
          super.second.reload();
        }
        super.second.setEnabled(true);
        break;
      case Symbol.STRING:
        this.enableStringOperations();
        super.second.setEnabled(true);
        break;
      default:
        this.operation.disable();
        super.second.setEnabled(false);
        break;
    }
  }
  /**
   * <process>
   * To process empty name
   * */
  private final void firstEmptyName(){
    // to disable the operation and second operand's editor
    super.operation.disable();
    super.second.setEnabled(false);
    if( this.isNewExpression() ) {// for new expression set ANY type
      this.type = Symbol.ANY;this.targetName();
    }
  }
  /**
   * <process>
   * to process second's name changes
   * */
  private final void secondName(){
    if ( super.value.second.isConst() ) {
      super.value.second.setName( super.second.name.getText() );
    }
    this.value.changed();
  }
  /**
   * <action>
   * To enable the String's operations
   * */
  private final void enableStringOperations(){
    super.operation.enable();
    for(Iterator i=this.buttons.keySet().iterator();i.hasNext();) {
      Object key = i.next();
      JComponent button=(JComponent)this.buttons.get(key);
      button.setEnabled( "+".equals(key) );
    }
  }
  /**
   * <action>
   * To enable all operations
   * */
  private final void enableAllOperations(){
    super.operation.enable();
    for(Iterator i=this.buttons.values().iterator();i.hasNext();)
      ((JComponent)i.next()).setEnabled(true);
  }
/**
 * <flag>
 * Is assigment in progress
 * */
private volatile boolean assign = false;
  /**
   * to assign the expression to edit
   * */
  public final void assign(MathExpression expression,Operation owner)
  {
    this.collectSymbols(owner, expression);
    // begin assigment
    this.assign = true;
    Symbol target = expression.getTarget();
    this.name.setText( target.getName() );
    if ( target.isConst() )
    {// new or empty MathExpression
      this.name.setEditable( true );
      this.type = Symbol.ANY;
    }else{
      this.name.setEditable( false );
      this.type = target.getTypeID();
    }
    // to assign the first/operation/second
    super.assign( expression );
    // end assigment
    this.assign = false;
  }
  /**
   * to collect all available Symbols
   * */
  private final void collectSymbols(Operation owner,MathExpression value) {
    this.symbols.clear(); this.groups.clear();
    java.util.List all = value.valid( owner.availableSymbols() );
    if (owner != null) this.symbols.addAll( all );
    for(Iterator i=this.symbols.iterator();i.hasNext();){
      Symbol symbol = (Symbol)i.next();
      if (symbol == null) continue;
      String group = symbol.getGroup();
      if ( !this.groups.contains(group) ) this.groups.add(group);
    }
  }
/**
 * <pool>
 * The pool of available Symbols
 * */
private final java.util.List symbols = new ArrayList(100);
  /**
   * <accessor>
   * To get access to availabled Symbols (for SymbolChooser)
   * */
  protected final java.util.List availableSymbols(Symbol forWho)
  {
    if (forWho == ((MathExpression)value).getTarget() ) {
      return this.groupList(Symbol.USER);
    }else {
      if (this.type == Symbol.STRING) return this.stringsList();
      else return this.typeList( this.type);
    }
  }
  /**
   * <accessor>
   * To make String's compatible List
   * For operands
   * */
  private final java.util.List stringsList(){
      ArrayList symbols = new ArrayList();
      symbols.addAll( this.typeList(Symbol.STRING) );
      symbols.addAll( this.typeList(Symbol.NUMBER) );
      return symbols;
  }
  /**
   * <accessor>
   * to make List for one type Only
   * */
  private final java.util.List typeList(int type){
      ArrayList symbols = new ArrayList();
      for(Iterator i=this.symbols.iterator();i.hasNext();) {
        Symbol symbol = (Symbol)i.next();
        if ( symbol.getTypeID() == type || type == Symbol.ANY) symbols.add(symbol);
      }
      return symbols;
  }
  /**
   * <accessor>
   * to make List for one group Only
   * */
  private final java.util.List groupList(int group){
      ArrayList symbols = new ArrayList();
      for(Iterator i=this.symbols.iterator();i.hasNext();) {
        Symbol symbol = (Symbol)i.next();
        if ( !symbol.isConst() && symbol.getGroupID()==group ) symbols.add(symbol);
      }
      return symbols;
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

}
