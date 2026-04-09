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

import org.visualcti.briquette.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow to edit the Symbol</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class SymbolEditor extends JPanel {
/**
* <accessor>
* to get access to related Symbol
* */
abstract protected Symbol getSymbol();
/**
* <accessor>
* to get access to const Symbol
* */
abstract protected Symbol getConst();
/**
* <mutator>
* To change the related Symbol
* */
abstract protected void setSymbol(Symbol symbol);
/**
* <accessor>
* To get access to SymbolChooser
* */
abstract protected SymbolChooser getSymbolChooser();
/**
* <accessor>
* To get access to available groups
* */
abstract protected java.util.List availableGroups();
    /**
     * <visual>
     * The label, title of the edited Symbol
     * */
    public final JLabel title = new JLabel("?");
    /**
     * <visual>
     * The name of Symbol
     * */
    public final JTextField name = new JTextField();
    /**
     * <visual>
     * The button for visualize SymbloChooser and store the result
     * */
    public final JButton list = new JButton("...");
    /**
     * <visual>
     * To change the group of the Symbol
     * */
    public final JComboBox group = new JComboBox(Symbol.GROUP);
    /**
     * <renderer>
     * The class for show the Group as Icon
     * */
    private final static class groupRenderer extends JLabel implements ListCellRenderer {
    public groupRenderer(){
        setOpaque(true);
        setVerticalAlignment(CENTER);
    }
    private final HashMap icons = new HashMap();
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
     {
        Icon icon = (Icon)this.icons.get(value);
        String text = value.toString();
        if ( icon == null) {
          icon=UI_Store.makeIcon(SymbolEditor.class,"/icon/"+text+"16.gif");
          if (icon != null) this.icons.put(value,icon);
        }
        list.setToolTipText( "group:"+text );
        if (icon != null) super.setIcon(icon);
        else              super.setText(text);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
     }
    }
    /**
     * <constructor>
     * */
    public SymbolEditor(){super(false);// not doubleBufferred
      super.setLayout( new BoxLayout(this,BoxLayout.X_AXIS) );
      this.makeTitle();
      this.makeName();
      this.name.setDocument(new UI.validatingDocument(){
        /** to validate the string as Double */
        protected final void validate(String str) throws Exception {
          if (!reload && str.length() > 0) getSymbol().validate(str);
        }
      });
      this.groupModel = (DefaultComboBoxModel)this.group.getModel();
      this.group.setSelectedIndex(0);
      this.makeGroups();
      this.list.setMargin(new java.awt.Insets(1,1,1,1));
      this.makeList();
      // listener of SymbolChooser launcher
      this.list.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          SymbolEditor.this.chooseSymbol();
        }
      });
      // listener of group's ComboBox
      this.group.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          SymbolEditor.this.groupChanged();
        }
      });
    }
    /**
     * <maker>
     * To make the component without Groups
     * */
    public final void disableGroups(){
      boolean isTitle = this.contains(this.title);
      this.removeAll();
      if (isTitle) this.makeTitle();
      this.makeName(); this.makeList();
    }
    /**
     * <maker>
     * To make the component without Title
     * */
    public final void disableTitle(){
      boolean isGroup = this.contains(this.group);
      this.removeAll();
      this.makeName();
      if ( isGroup ) this.makeGroups();
      this.makeList();
    }
    /** To make the title's part */
    private final void makeTitle() {
      super.add(Box.createHorizontalStrut(8));
      super.add( this.title );
      super.add(Box.createHorizontalStrut(5));
    }
    /** To make the name's part */
    private final void makeName() {super.add( this.name );}
    /** To make the group's part */
    private final void makeGroups(){
      this.group.setRenderer(new groupRenderer());
      super.add( this.group );
    }
    /** To make the group's part */
    private final void makeList(){super.add( this.list );}
    /**
     * <action>
     * To change enabled status of the Editor
     * */
    public final void setEnabled(boolean enabled)
    {
      this.title.setEnabled(enabled);
      this.name.setEnabled(enabled);
      this.group.setEnabled(enabled);
      this.list.setEnabled(enabled);
    }
/**
 * <model>
 * the model of group's ComboBox
 * */
private final DefaultComboBoxModel groupModel;
      /**
       * <notify>
       * to process group changed event
       * */
      private final void groupChanged() {
        if ( this.reload ) return;
        Symbol symbol = this.getSymbol();
        if (symbol == null) return;
        Object group = this.groupModel.getSelectedItem();
        if ( "const".equals(group) ) {
          if (this.constSymbol != null) this.setSymbol(this.constSymbol);
          else this.setSymbol( this.getConst() );
          this.reload();
        }else{
          if( !symbol.getGroup().equals(group) ) this.name.setText("");
        }
        int typeID=symbol.getTypeID();
        if (typeID==Symbol.STRING || typeID==Symbol.NUMBER) this.name.setEditable( CONST.equals(group) );
      }
/**
 * <attribute>
 * Last value of constant
 * */
private Symbol constSymbol = null;
      /**
       * <notify>
       * Pressed List button
       * */
      private final void chooseSymbol() {
        if ( this.reload ) return;
        SymbolChooser dialog = this.getSymbolChooser();
        if (dialog == null) return;
        dialog.setChoosed( this.getSymbol() );
        dialog.setVisible( true );
        Symbol result = dialog.getChoosed();
        if (result != null) {
          Symbol old = this.getSymbol();
          if ( old.isConst() ) this.constSymbol=old;
          this.setSymbol( result );
          this.reload();
        }
      }
/**
 * <flag>
 * is reload in progress
 * */
private volatile boolean reload = false;
  /**
   * <accessor>
   * To get access to reload's flag
   * */
  protected final boolean isReload(){return this.reload;}
/**
 * <const>
 * The name of group for constant
 * */
protected final static String CONST = "const";
    /**
     * <accessor>
     * Is may editor set Symbol as constant
     * */
    protected boolean isConstValid(){return true;}
    /** to check is component exists in the Container */
    private final boolean contains(Component c){
      Component[] list = this.getComponents();
      for(int i=0;i < list.length;i++) if (c == list[i]) return true;
      return false;
    }
    /**
     * <refresh>
     * To reaload the Values to editor
     * */
    public final void reload()
    {
      Symbol symbol = this.getSymbol();
      if (symbol == null) return;

      //## begin reload
      this.reload = true;
      // to update the groups model
      this.reloadGroups( symbol );
      // setup the name of Symbol
      this.reloadName(symbol);
      //## end reload
      this.reload = false;
    }
    /**
     * <refresh>
     * To reload the name of the Symbol
     * */
    private final void reloadName(Symbol symbol) {
      boolean isConst = symbol.isConst();
      int type = symbol.getTypeID();
      int aligment = (isConst && type==Symbol.NUMBER) ? JTextField.RIGHT:JTextField.LEFT;
      this.name.setHorizontalAlignment(aligment);
      this.name.setText( symbol.getName() );
      this.name.setEditable( isConst && (type==Symbol.STRING || type==Symbol.NUMBER) );
      this.name.setCaretPosition(0);
    }
    /**
     * <refresh>
     * To reload the groups
     * */
    private final void reloadGroups(Symbol symbol) {
      boolean groupExists = false;
      String symbolGroup = symbol.getGroup();
      // to remove all items
      this.groupModel.removeAllElements();
      // if constant is enabled add "const" entry
      if( this.isConstValid() )this.groupModel.addElement( CONST );
      // to copy available groups's names to the groupModel
      for(Iterator i=this.availableGroups().iterator();i.hasNext();){
        Object group = i.next();
        this.groupModel.addElement( group );
        if ( symbolGroup.equals(group) ) groupExists = true;
      }
      // to add to list the group's name from Symbol
      if ( !groupExists && !symbol.isConst() )
        this.groupModel.addElement(symbolGroup);
      // to select the group
      if ( symbol.isConst() ){// for constant
        this.groupModel.setSelectedItem( CONST );
      }else {// for value
        this.groupModel.setSelectedItem( symbolGroup );
      }
    }
}
