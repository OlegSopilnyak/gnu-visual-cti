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
 * The User Interface to adjust FileCopy briquette</p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class FileCopyUI extends baseOperationUI
{
private final static String locationTitleText = "Location";
private final static String nameTitleText = "Name";
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
   * the editor for "source location"
   * */
  private final class sLocationEditor extends stringEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getSourceLocation();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setSourceLocation(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("./");}
  }
/**
 * <attribute>
 * The editor for source's location value
 */
private final sLocationEditor sLocation = new sLocationEditor();
  /**
   * <editor>
   * the editor for "target location"
   * */
  private final class tLocationEditor extends stringEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getTargetLocation();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setTargetLocation(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("./");}
  }
/**
 * <attribute>
 * The editor for target's location value
 */
private final tLocationEditor tLocation = new tLocationEditor();
  /**
   * <editor>
   * the editor for "source name"
   * */
  private final class sNameEditor extends stringEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getSourceName();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) {
        owner.setSourceName(symbol);
        owner.setTargetName( symbol.copy() );
        tName.reload();
      }
    }
    protected final Symbol getConst(){return Symbol.newConst("file.txt");}
  }
/**
 * <attribute>
 * The editor for source's name value
 */
private final sNameEditor sName = new sNameEditor();
  /**
   * <editor>
   * the editor for "target name"
   * */
  private final class tNameEditor extends stringEditor{
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getTargetName();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setTargetName(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("file.txt");}
  }
/**
 * <attribute>
 * The editor for target's name value
 */
private final tNameEditor tName = new tNameEditor();
/**
 * <attribute>
 * The editor for keepSource flag
 */
private final JComboBox keepSource = new JComboBox(new Object[]{"Enabled","Disabled"});
/**
 * <panel>
 * The panel of source file's parameters
 */
private final class sFilePanel extends JPanel{
  private sFilePanel(){
    // to setup the border
    super.setBorder( new TitledBorder("From") );
    // to add components
    super.setLayout( new BoxLayout(this,BoxLayout.Y_AXIS) );
    // to add the location's editor
    UI_Store.addTitle( locationTitleText, this);
    super.add( sLocation );
    // to add the name's editor
    super.add(Box.createVerticalStrut(2));
    UI_Store.addTitle( nameTitleText, this);
    super.add( sName );
    // to add the keep source editor
    super.add(Box.createVerticalStrut(2));
    UI_Store.addTitle( "Keep file", this);
    super.add( keepSource );
  }
}
/**
 * <attribute>
 * The editor for forceLocation flag
 */
private final JComboBox forceTarget = new JComboBox(new Object[]{"Enabled","Disabled"});
/**
 * <panel>
 * The panel of target file's parameters
 */
private final class tFilePanel extends JPanel{
  private tFilePanel(){
    // to setup the border
    super.setBorder( new TitledBorder("To") );
    // to add components
    super.setLayout( new BoxLayout(this,BoxLayout.Y_AXIS) );
    // to add the location's editor
    UI_Store.addTitle( locationTitleText, this);
    super.add( tLocation );
    // to add the name's editor
    super.add(Box.createVerticalStrut(2));
    UI_Store.addTitle( nameTitleText, this);
    super.add( tName );
    // to add the keep source editor
    super.add(Box.createVerticalStrut(2));
    UI_Store.addTitle( "Force location", this);
    super.add( forceTarget );
  }
}
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private FileCopy owner = null;
  /**
   * <contructor>
   */
  public FileCopyUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add source file panel
    this.params.add( new sFilePanel() );
    // to add target file panel
    //this.params.add(Box.createVerticalStrut(2));
    this.params.add( new tFilePanel() );
    // to add the listeners
    this.keepSource.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned )owner.setKeepSource(keepSource.getSelectedIndex()==0);
      }
    });
    this.forceTarget.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned )owner.setForceTarget(forceTarget.getSelectedIndex()==0);
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
    this.sLocation.reload();
    this.tLocation.reload();
    this.sName.reload();
    this.tName.reload();
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
    if (briquette instanceof FileCopy) {
      this.owner=(FileCopy)briquette;
      this.isAssigned = true;
      this.keepSource.setSelectedIndex(this.owner.isKeepSource()?0:1);
      this.forceTarget.setSelectedIndex(this.owner.isForceTarget()?0:1);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
