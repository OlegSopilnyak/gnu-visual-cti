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

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust the SendFax</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class SendFaxUI extends baseOperationUI
{
private final static String sourceTitleText = "Source of document";
private final static String signalAfterTitleText = "Signal after";
private final static String qualityTitleText = "Transmit's quality";
private final static String headerTitleText = "Document's header";
private final static String idTitleText = "Fax's localID";
private final static String startTitleText = "Start from";
/**
 * to add the title to the editors's container
 * */
private static final void addTitle(String theTitleText,JPanel container) {
  JLabel title = new JLabel(theTitleText,JLabel.CENTER);
  title.setFont(UI.titleFont);
  title.setForeground(UI.titleColor);
  container.add( title );
}
/**
 * <attribute>
 * The owner of this UI
 * */
private SendFax owner = null;
  /**
   * <editor>
   * the editor for "header"
   * */
  private final class headerEditor extends SymbolEditor{
    public headerEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){
      return getDialog(Symbol.STRING);
    }
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getHeader();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setHeader(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("VisualCTI's fax");}
  }
  /**
   * <editor>
   * the editor for "localID"
   * */
  private final class localIDEditor extends SymbolEditor{
    public localIDEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){
      return getDialog(Symbol.STRING);
    }
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getLocalID();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setLocalID(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("VisualCTI's server");}
  }
  /**
   * <editor>
   * the editor for "startPage"
   * */
  private final class startPageEditor extends SymbolEditor{
    public startPageEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){
      return getDialog(Symbol.NUMBER);
    }
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getStartPage();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setStartPage(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(1));}
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
 * <attribute>
 * The list of available Symbols
 * */
private final java.util.List groups = new ArrayList(4);
/**
 * <attribute>
 * visual component for edit source's property
 * */
private final FaxSourceUI source = new FaxSourceUI();
/**
 * <attribute>
 * visual component for edit issueVoiceRequest's property
 * */
private final JComboBox issueVoiceRequest = new JComboBox(new Object[]{"true","false"});
/**
 * <mutator>
 * To update the issueVoiceRequest property
 * */
private final void setIssueVoiceRequest(boolean flag){
  if (this.owner != null) this.owner.setIssueVoiceRequest(flag);
}
/**
 * <attribute>
 * visual component for edit quality's property
 * */
private final JComboBox quality = new JComboBox(new Object[]{"FINE","STANDART"});
/**
 * <mutator>
 * To update the quality property
 * */
private final void setQuality(int quality){
  if (this.owner != null) this.owner.setQuality(quality);
}
/**
 * <attribute>
 * visual component for edit header's property
 * */
private final headerEditor header = new headerEditor();
/**
 * <mutator>
 * To update the header property
 * */
private final void setHeader(){
  if (this.owner == null) return;
  String data = this.header.name.getText();
  Symbol symbol = this.owner.getHeader();
  if (symbol.isConst()) symbol.setName(data);
}
/**
 * <attribute>
 * visual component for edit localID's property
 * */
private final localIDEditor localID = new localIDEditor();
/**
 * <mutator>
 * To update the localID property
 * */
private final void setLocalID(){
  if (this.owner == null) return;
  String data = this.localID.name.getText();
  Symbol symbol = this.owner.getLocalID();
  if (symbol.isConst()) symbol.setName(data);
}
/**
 * <attribute>
 * visual component for edit startPage's property
 * */
private final startPageEditor startPage = new startPageEditor();
/**
 * <mutator>
 * To update the startPage property
 * */
private final void setStartPage(){
  if (this.owner == null) return;
  String data = this.startPage.name.getText();
  Symbol symbol = this.owner.getStartPage();
  if (symbol.isConst()) symbol.setName(data);
}
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <constructor>
   * */
  public SendFaxUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the source
    this.params.add(Box.createVerticalStrut(4));
    addTitle( sourceTitleText, this.params);
    this.params.add(this.source);
    // to add issueVoiceRequest
    this.params.add(Box.createVerticalStrut(4));
    addTitle( signalAfterTitleText, this.params);
    this.params.add( this.issueVoiceRequest );
    // to add listener
    this.issueVoiceRequest.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setIssueVoiceRequest( issueVoiceRequest.getSelectedIndex()==0 );
      }
    });
    // to add quality
    this.params.add(Box.createVerticalStrut(4));
    addTitle( qualityTitleText, this.params);
    this.params.add( this.quality );
    // to add listener
    this.quality.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setQuality( quality.getSelectedIndex() );
      }
    });
    // to add header
    this.params.add(Box.createVerticalStrut(4));
    addTitle( headerTitleText, this.params);
    this.params.add( this.header );
    // to add listener
    this.header.name.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {if ( !isAssigned ) setHeader();}
    });
    // to add localID
    this.params.add(Box.createVerticalStrut(4));
    addTitle( idTitleText, this.params);
    this.params.add( this.localID );
    // to add listener
    this.localID.name.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {if ( !isAssigned ) setLocalID();}
    });
    // to add startPage
    this.params.add(Box.createVerticalStrut(4));
    addTitle( startTitleText, this.params);
    this.params.add( this.startPage );
    // to add listener
    this.startPage.name.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {if ( !isAssigned ) setStartPage();}
    });
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
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.prepareGroups();
    this.source.setAvailabledSymbols( this.owner.availableSymbols() );
    this.header.reload();
    this.localID.reload();
    this.startPage.reload();
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
    if (briquette instanceof SendFax) {
      this.owner=(SendFax)briquette;
      this.isAssigned = true;
      this.source.assign( this.owner.getSource() );
      this.issueVoiceRequest.setSelectedIndex(this.owner.isIssueVoiceRequest()?0:1);
      this.quality.setSelectedIndex( this.owner.getQuality() );
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
