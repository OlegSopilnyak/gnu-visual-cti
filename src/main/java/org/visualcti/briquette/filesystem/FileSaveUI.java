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
package org.visualcti.briquette.filesystem;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * To adjust the parameters of FileSave briquette</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FileSaveUI extends baseOperationUI
{
private final static String locationTitleText = "Location";
private final static String nameTitleText = "Name";
private final static String contentTitleText = "Content";

  /**
   * <editor>
   * Abstract string editor
   * */
  private abstract class stringEditor extends SymbolEditor{
    public stringEditor(){super();super.disableTitle();
      super.name.getDocument().addDocumentListener( new DocListener(){
        protected final void update() {Symbol symbol=getSymbol();
          if ( isAssigned || symbol==null || !symbol.isConst() ) return;
          symbol.setName( stringEditor.super.name.getText() );
        }
      });
    }
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){return getDialog(Symbol.STRING);}
  }
  /**
   * <listener>
   * The listener of document's changes
   * */
  private abstract class DocListener implements DocumentListener {
    public final void insertUpdate(DocumentEvent e) {this.update();}
    public final void removeUpdate(DocumentEvent e) {this.update();}
    public final void changedUpdate(DocumentEvent e){}
    /** to check and process changes */
    protected abstract void update();
  }
/**
 * to process Symbols chooser only for typeID
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
 * to process Symbols chooser only for binary types
 * */
private final SymbolChooser getBinDialog() {
  if (this.owner == null) return null;
  java.util.List all = this.owner.availableSymbols();
  SymbolChooser dialog = SymbolChooser.getInstance(this.params);
  ArrayList list = new ArrayList();
  for(Iterator i=all.iterator();i.hasNext();) {
    Symbol symbol = (Symbol)i.next();
    if (symbol != null &&
        (symbol.getTypeID() == Symbol.VOICE ||
        symbol.getTypeID() == Symbol.FAX ||
        symbol.getTypeID() == Symbol.BIN)
        ) list.add(symbol);
  }
  dialog.setSymbols( list );
  return dialog;
}
  /**
   * <editor>
   * the editor for "location"
   * */
  private final class locationEditor extends stringEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getLocation();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setLocation(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("./");}
  }
/**
 * <attribute>
 * To edit the location's property
 */
private final locationEditor location = new locationEditor();
  /**
   * <editor>
   * the editor for "name"
   * */
  private final class nameEditor extends stringEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getName();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setName(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("file.txt");}
  }
/**
 * <attribute>
 * To edit the name property
 */
private final nameEditor name = new nameEditor();
  /**
   * <editor>
   * the editor for "content"
   * */
  private final class contentEditor extends SymbolEditor{
    public contentEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){return getBinDialog();}
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getContent();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setContent(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("");}
    protected final boolean isConstValid(){return false;}
  }
/**
 * <attribute>
 * To edit the location's property
 */
private final contentEditor content = new contentEditor();
/**
 * <panel>
 * The panel of file's parameters
 */
private final class FilePanel extends JPanel{
  private FilePanel(){
    // to add components
    super.setLayout( new BoxLayout(this,BoxLayout.Y_AXIS) );
    // to add the location's editor
    UI_Store.addTitle( locationTitleText, this);
    super.add( FileSaveUI.this.location );
    // to add the name's editor
    super.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( nameTitleText, this);
    super.add( FileSaveUI.this.name );
    // to add the content's editor
    super.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( contentTitleText, this);
    super.add( FileSaveUI.this.content );
    // to setup the border
    super.setBorder( new TitledBorder("File") );
  }
}
private final static String mergeTitleText = "Merge content";
/**
 * <attribute>
 * The editor of "merge"
 */
private final JComboBox merge = new JComboBox( new Object[]{"Enabled","Disabled"}  );
private final static String forceTitleText = "Force location";
/**
 * <attribute>
 * The editor of "force"
 */
private final JComboBox force = new JComboBox( new Object[]{"Enabled","Disabled"}  );
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private FileSave owner = null;
  /**
   * <constructor>
   */
  public FileSaveUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add file's panel
    this.params.add( new FilePanel() );
    // to add the force's editor
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( forceTitleText, this.params);
    this.params.add( this.force );
    // to add the merge's editor
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( mergeTitleText, this.params);
    this.params.add( this.merge );
    // to add the listeners
    this.force.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned )owner.setForce(force.getSelectedIndex()==0);
      }
    });
    this.merge.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned )owner.setMerge(merge.getSelectedIndex()==0);
      }
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
    this.location.reload();
    this.name.reload();
    this.content.reload();
    this.propertiesTree.add(this.params, BorderLayout.NORTH);
  }
/**
 * <attribute>
 * The list of available Symbol's groups
 * */
private final java.util.List groups = new ArrayList(4);
  private final void prepareGroups(){
    this.groups.clear();
    if (this.owner == null) return;
    java.util.List all = this.owner.availableSymbols();
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (
          symbol == null  ||
          symbol.isConst()||
          symbol.getTypeID() != Symbol.STRING
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
   * @param briquette assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof FileSave) {
      this.owner=(FileSave)briquette;
      this.isAssigned = true;
      this.merge.setSelectedIndex(this.owner.isMerge()?0:1);
      this.force.setSelectedIndex(this.owner.isForce()?0:1);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}

}
