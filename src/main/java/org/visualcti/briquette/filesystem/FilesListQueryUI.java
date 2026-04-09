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
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * The User Interface to adjust Files List parameters</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class FilesListQueryUI extends baseOperationUI
{
private final static String locationTitleText = "Location";
private final static String filterTitleText = "The filter";
private final static String orderTitleText = "Order by";

/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private FilesListQuery owner = null;
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <editor>
   * Abstract string editor
   * */
  private abstract class stringEditor extends SymbolEditor{
    public stringEditor(){super();super.disableTitle();
      super.name.getDocument().addDocumentListener( new DocListener(){
        protected final void update() {Symbol symbol=getSymbol();
          if ( isAssigned || symbol==null || !symbol.isConst() ) return;
          symbol.setName( name.getText() );
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
 * <attribute>
 * The editor of filter
 * */
private final LogicUI filter = new LogicUI();
/**
 * <attribute>
 * The editor of the order
 */
private final DefaultComboBoxModel orderModel = new DefaultComboBoxModel();
private final JComboBox order = new JComboBox( orderModel );
  /**
   * <constructor>
   */
  public FilesListQueryUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the location's editor
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( locationTitleText, this.params);
    this.params.add( this.location );
    // to add order
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( orderTitleText, this.params);
    this.params.add( this.order );
    this.order.setEditable( false );
    // to add location's listener
    this.order.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned ) owner.setOrder( order.getSelectedIndex() );
      }
    });
    // to add filter
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( filterTitleText, this.params);
    this.filter.setRootLabel("VIEW");
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
    this.order.setSelectedIndex( this.owner.getOrder() );

    this.propertiesTree.add(this.params, BorderLayout.NORTH);
    this.propertiesTree.add(this.filter, BorderLayout.CENTER);
    this.propertiesTree.add(this.filter.getControlPanel(),BorderLayout.SOUTH);
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
    if (briquette instanceof FilesListQuery) {
      this.owner=(FilesListQuery)briquette;
      this.isAssigned = true;
      this.orderModel.removeAllElements();
      String[] dirs=FilesListQuery.sortOrder;
      for(int i=0;i < dirs.length;i++) this.orderModel.addElement(dirs[i]);
      this.filter.assign(this.owner.getFilter(),this.owner);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}

}
