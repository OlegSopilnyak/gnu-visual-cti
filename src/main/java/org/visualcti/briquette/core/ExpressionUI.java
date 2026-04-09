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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

abstract class ExpressionUI extends JPanel {
/**
 * <editor>
 * The editor for first's operand
 * */
final SymbolEditor first = new firstUI();
/**
 * <editor>
 * The chooser of operations
 * */
final operationsUI operation = new operationsUI();
/**
 * <editor>
 * The editor for second's operand
 * */
final SymbolEditor second = new secondUI();
/**
 * <chooser> Dialog
 * To choose the availbale symbol
 * */
private static SymbolChooser symDialog=null;
  /**
   * <accessor>
   * To get access to Symbol's chooser Dialog
   * */
public final static SymbolChooser getSymbolChooser(){return ExpressionUI.symDialog;}
  /**
   * <check>
   * To check the symbols for build SymbolChooser instance
   * */
  private final void checkSymbolChooser() {
    if (ExpressionUI.symDialog != null) return;
    // to make a dialog's instance
    synchronized(ExpressionUI.class)
    {
      if (ExpressionUI.symDialog == null)
        ExpressionUI.symDialog=SymbolChooser.getInstance( this );
    }
  }
  /**
   * Constructor
   * */
  public ExpressionUI()
  {
    super.setDoubleBuffered( false );
    super.setLayout( new BoxLayout(this,BoxLayout.Y_AXIS) );
    super.add(Box.createVerticalStrut(8));
    super.add(this.first);
    super.add(this.operation);
    super.add(this.second);
    super.add(Box.createVerticalStrut(8));
  }
/**
 * <value>
 * The reference to editable expression
 * */
protected Expression value=null;
  /**
   * <mutator>
   * To assign an Expression
   * And reload the UI
   * */
  protected void assign(Expression exp)
  {
    this.value = exp;
    this.first.reload();
    this.second.reload();
    this.operation.select( exp.operation );
  }
  /**
   * <accessor>
   * To get access to availabled groups (for SymbolEditor)
   * */
  abstract protected java.util.List availableGroups();
  /**
   * <accessor>
   * To get access to availabled Symbols (for SymbolChooser)
   * */
  abstract protected java.util.List availableSymbols(Symbol forWho);
  /**
   * <editor>
   * The editor for any operand
   * */
  abstract class operandUI extends SymbolEditor
  {
    /**
     * <accessor>
     * To get access to list of availabled groups
     * For make groups's chooser List
     * */
    protected final java.util.List availableGroups()
    {
      return ExpressionUI.this.availableGroups();
    }
    /**
     * <accessor>
     * To get access to SymbolChooser dialog for edited Symbol
     * Method will returns the null if no availabled Symbols
     * */
    protected final SymbolChooser getSymbolChooser()
    {
      ExpressionUI.this.checkSymbolChooser();
      java.util.List availabled = ExpressionUI.this.availableSymbols(this.getSymbol());
      if (availabled.size() == 0) {
        JOptionPane.showMessageDialog
                      (
                      symDialog.getParent(),
                      "No availabled Symbols",
                      "Symbols set...",
                      JOptionPane.INFORMATION_MESSAGE
                      );
        return null;
      }else {
        symDialog.setSymbols( availabled );
        return symDialog;
      }
    }
    protected final Symbol getConst(){return Symbol.newConst("");}
  }
  /**
   * <editor>
   * Class for edit first operand
   * */
  private final class firstUI extends operandUI{
    protected final void setSymbol(Symbol symbol) {
      if ( value != null) firstChanged( value.first=symbol );
    }
    protected final Symbol getSymbol(){
      return value == null ? null:value.first;
    }
  }
  /**
   * <notify>
   * To notify the UI about first operand's changes
   * @param symbol new value
   */
  protected void firstChanged(Symbol symbol){}
  /**
   * <editor>
   * Class for edit second operand
   * */
  private final class secondUI extends operandUI {
    protected final void setSymbol(Symbol symbol){
      if ( value != null) secondChanged( value.second=symbol );
    }
    protected final Symbol getSymbol(){
      return  value == null ? null:value.second;
    }
  }
  /**
   * <notify>
   * To notify the UI about second operand's changes
   * @param symbol new value
   */
  protected void secondChanged(Symbol symbol){}
  /**
   * <operation>
   * Class for visualize the operations
   * */
  public static class operationsUI extends JPanel
  {
    private final java.util.List pool = Collections.synchronizedList( new ArrayList(6) );
    public operationsUI(){
      super(new FlowLayout(FlowLayout.CENTER),true);
    }
    public JToggleButton add(final String operName){
      JToggleButton button = new JToggleButton(operName);
      button.setActionCommand(operName);
      button.setMargin(new Insets(1,1,1,1));
      super.add(button); this.pool.add(button);
      button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent a){
          operationsUI.this.select( operName );
        }
      });
      return button;
    }
    public final void enable(){
      for(Iterator i=this.pool.iterator();i.hasNext();)
        ((JToggleButton)i.next()).setEnabled(true);
    }
    public final void disable(){
      this.unselectAll();
      for(Iterator i=this.pool.iterator();i.hasNext();)
        ((JToggleButton)i.next()).setEnabled(false);
    }
    public JToggleButton remove(String operName){
      for(ListIterator i=this.pool.listIterator();i.hasNext();){
        JToggleButton button = (JToggleButton)i.next();
        if ( button.getActionCommand().equals(operName) ){
          i.remove(); this.revalidate(); return button;
        }
      }
      return null;
    }
    public void select(String operName){
      for(Iterator i=this.pool.iterator();i.hasNext();){
        JToggleButton button = (JToggleButton)i.next();
        boolean select = button.getActionCommand().equals(operName);
        button.setSelected( select );
      }
    }
    public String selected(){
      for(Iterator i=this.pool.iterator();i.hasNext();){
        JToggleButton button = (JToggleButton)i.next();
        if (button.isSelected()) return button.getActionCommand();
      }
      return null;
    }
    public void unselectAll(){
      for(Iterator i=this.pool.iterator();i.hasNext();)
        ((JToggleButton)i.next()).setSelected( false );
    }
  }
}
