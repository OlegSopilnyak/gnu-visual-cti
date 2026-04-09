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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import java.util.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow the UI for Media</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class MediaUI extends JPanel {
/**
 * <attribute>
 * The visual component for show/edit the Content's type
 * */
private final JComboBox types = new JComboBox();
/**
 * <attribute>
 * The visual component for show/edit the Content's Symbol
 * */
private final JTextField content = new JTextField(  );
/**
 * <attribute>
 * The visual component for edit the Content's Symbol via Dialog
 * */
private final JButton list = new JButton( "..." );
  /**
   * <constructor>
   * */
  public MediaUI() {
    super( true );
    super.setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );
    // to add type of Media check box
    super.add( this.types );
    // to add types events listener
    this.types.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned ) setType();
      }
    });
    // to add content's Symbol editor
    super.add( this.content );
    // to add content's listeners
    this.content.getDocument().addDocumentListener( new DocumentListener(){
      public final void insertUpdate(DocumentEvent e) {this.update();}
      public final void removeUpdate(DocumentEvent e) {this.update();}
      public final void changedUpdate(DocumentEvent e){}
      /** to check and process changes */
      private void update() {if ( !isAssigned ) setContent();}
    });
    // to add list button
    super.add( this.list );
    this.list.setMargin(new Insets(1,1,1,1));
    // to add types events listener
    this.list.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){symbol();}
    });
  }
/**
 * <attribute>
 * The owner of this UI
 * */
protected Media owner = null;
  /**
   * <mutator>
   * To apply type's changes from visual component
   * */
  private final void setType() {
    if (this.owner == null) return;
    this.owner.setType( this.types.getSelectedIndex() );
    // to update the UI
    this.assign( this.owner );
  }
  /**
   * <mutator>
   * To apply type's changes from visual component
   * */
  private final void setContent() {
    if (this.owner == null) return;
    Symbol content = this.owner.getContent();
    if ( !content.isConst() ) return;
    content.setName( this.content.getText() );
  }
  /**
   * <mutator>
   * To choose the symbol or symbol's name using dialog
   * */
  private final void symbol() {
    if (this.owner == null) return;
    int type = this.owner.getType();
    Symbol symbol = this.getContentSymbol( type );
    if (symbol != null) {
      // to update content
      this.owner.setContent( symbol );
      // to update the UI
      this.assign( this.owner );
    }
  }
  /**
   * <editor>
   * To get the content's Symbol via Dialog
   * */
  protected Symbol getContentSymbol(int type)
  {
    switch( type )
    {
      case Media.RAW:
        return this.rawSymbol();
      case Media.FILE:
        return this.fileSymbol();
    }
    return Symbol.newConst("");
  }
  /**
   * <dialog>
   * To choose the RAW symbol
   * */
  private final Symbol rawSymbol() {
    SymbolChooser dialog = SymbolChooser.getInstance(this);
    ArrayList valid = new ArrayList();
    valid.addAll( this.symbols(this.owner.getRawTypeID()) );
    valid.addAll( this.symbols(Symbol.BIN) );
    if (valid.size() == 0) {
      JOptionPane.showMessageDialog
                    (
                    dialog.getParent(),
                    "No availabled Symbols",
                    "Symbols set...",
                    JOptionPane.INFORMATION_MESSAGE
                    );
      return null;
    }
    dialog.setChoosed( this.owner.getContent() );
    dialog.setSymbols( valid );
    dialog.setVisible(true);
    return dialog.getChoosed();
  }
/**
 * <attribute>
 * The popup-menu for choose the file name's type
 * */
private JPopupMenu fileSymbolMenu = null;
  /**
   * <dialog>
   * To choose the file's name
   * */
  private final Symbol fileSymbol(){
    if (this.fileSymbolMenu == null) {
      this.fileSymbolMenu = new JPopupMenu("Name from");
      JMenuItem
      item = this.fileSymbolMenu.add("The memory");
      item.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){fileNameInSymbol();}
      });
      item = this.fileSymbolMenu.add("The file");
      item.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){fileNameSymbol();}
      });
    }
    this.fileSymbolMenu.show(this.list,0,20);
    return null;// will update later
  }
  /**
   * <dialog>
   * To choose from symbols
   * */
  private final void fileNameInSymbol(){
    SymbolChooser dialog = SymbolChooser.getInstance(this);
    ArrayList valid = new ArrayList();
    valid.addAll( this.symbols(Symbol.STRING) );
    if (valid.size() == 0) {
      JOptionPane.showMessageDialog
                    (
                    dialog.getParent(),
                    "No availabled Symbols",
                    "Symbols set...",
                    JOptionPane.INFORMATION_MESSAGE
                    );
      return;
    }
    dialog.setChoosed( this.owner.getContent() );
    dialog.setSymbols( valid );
    dialog.setVisible(true);
    Symbol symbol = dialog.getChoosed();
    if ( symbol != null ) {
      this.owner.setContent(symbol);
      this.assign( this.owner );
    }
  }
/**
 * <member>
 * The dialog for choose the file
 * */
private final static JFileChooser fileChooser = new JFileChooser();
  /**
   * <mutator>
   * To configure the file's chooser
   * */
  protected abstract void configure(JFileChooser chooser);
  /**
   * <show>
   * to show JFileChooser dialog
   * */
  protected abstract int show(JFileChooser chooser);
  /**
   * <check>
   * Is valid file choosed
   * */
  protected abstract boolean valid(File file);
  /**
   * <dialog>
   * To choose from file
   * */
  private final void fileNameSymbol(){
    Symbol symbol = null;
    File file = new File( this.owner.getContent().getName() );
    fileChooser.resetChoosableFileFilters();
    // to adjust the filters
    this.configure( fileChooser );
    fileChooser.setSelectedFile( file );
    int result = this.show(fileChooser);
    if(result == JFileChooser.APPROVE_OPTION) {
      file = fileChooser.getSelectedFile();
      if ( this.valid(file) ) symbol = Symbol.newConst(file.getAbsolutePath());
    }
    if ( symbol != null ) {
      this.owner.setContent( symbol );
      this.assign( this.owner );
    }
  }
  /**
   * <select>
   * To select Symbols by type
   * */
  protected final java.util.List symbols(int type){
    ArrayList valid = new ArrayList();
    for(ListIterator i=this.availabledSymbols.listIterator();i.hasNext();){
      Symbol symbol = (Symbol)i.next();
      if (symbol == null) i.remove();
      else {
        if ( !symbol.isConst() && type==symbol.getTypeID()) valid.add(symbol);
      }
    }
    return valid;
  }
/**
 * <pool>
 * The pool of availabled Symbols
 * */
private final ArrayList availabledSymbols = new ArrayList();
  /**
   * <mutator>
   * To transfer availabled Symbols
   * */
  public final void setAvailabledSymbols(java.util.List symbols)
  {
    this.availabledSymbols.clear();
    this.availabledSymbols.addAll( symbols );
  }
/**
 * <flag>
 * Is assigned values
 * */
private volatile boolean isAssigned = false;
  /**
   * <mutator>
   * To assign the UI's owner
   * */
  public final void assign(Media owner)
  {
    this.isAssigned = true;
    // begin assign
    this.owner = owner;
    // to assign types values
    java.util.List types = owner.getTypes();
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    for(Iterator i=types.iterator();i.hasNext();) model.addElement(i.next());
    this.types.setModel( model );
    this.types.setSelectedIndex( owner.getType() );
    // to assign the content's visual
    Symbol content = owner.getContent();
    this.content.setText( content.getName() );
    this.content.setEditable( owner.getType()==Media.FILE && content.isConst() );
    //end assign
    this.isAssigned = false;
  }
}
