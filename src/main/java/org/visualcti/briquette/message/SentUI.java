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
package org.visualcti.briquette.message;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust Sent</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class SentUI extends baseOperationUI
{
private final static String protocolTitleText = "Message's protocol";
private final static String serverTitleText = "Server";
private final static String messageTitleText = "Message";
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private Sent owner = null;
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
/**
 * <attribute>
 * The editor the protocol
 */
private final DefaultComboBoxModel protocolModel = new DefaultComboBoxModel();
private final JComboBox protocol = new JComboBox( protocolModel );
  /**
   * <editor>
   * the editor for "server"
   * */
  private final class serverEditor extends SymbolEditor{
    public serverEditor(){super();super.disableTitle();
      super.name.getDocument().addDocumentListener( new DocListener(){
        protected final void update() {Symbol symbol=getSymbol();
          if ( isAssigned || symbol==null || !symbol.isConst() ) return;
          symbol.setName( name.getText() );
        }
      });
    }
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){return getDialog(Symbol.STRING);}
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getServer();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setServer(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("localhost");}
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
 * To edit the server's property
 */
private final serverEditor server = new serverEditor();
  /**
   * <constructor>
   * To make the UI
   */
  public SentUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the protocol
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( protocolTitleText, this.params);
    this.params.add( this.protocol );
    this.protocol.setEditable( false );
    // to add protocol's listener
    this.protocol.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( !isAssigned ) owner.setProtocol( protocol.getSelectedItem().toString());
      }
    });
    // to add server
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( serverTitleText, this.params);
    this.params.add( this.server );
    // to add login
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( messageTitleText, this.params);
    JPanel cPane = new JPanel(new BorderLayout());
    JButton compose = new JButton("Compose Message");
    cPane.add(compose,BorderLayout.CENTER);
    compose.setToolTipText("To compose/edit the message");
    compose.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        SentUI.this.composeMessage();
      }
    });
    this.params.add( cPane );
  }
private static uiDialog dialog=null;
  private final void checkDialog(){
    if ( dialog != null ) return;
    synchronized( SentUI.class ){
      if ( dialog == null ) dialog=new uiDialog(this.params);
    }
  }
/**
 * <attribute>
 * The panel for edit the message
 */
private final MessageComposerPanel composer = new MessageComposerPanel();
  private final void composeMessage(){
    this.checkDialog();
    this.composer.init( this.owner );
    dialog.setEditor( this.composer ); dialog.setVisible( true );
    if ( dialog.isAccept() ) this.composer.update( this.owner );
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.prepareGroups();
    this.protocol.setSelectedItem( this.owner.getProtocol() );
    this.server.reload();
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
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof Sent) {
      this.owner=(Sent)briquette;
      this.isAssigned = true;
      this.protocolModel.removeAllElements();
      Equipment equipment = this.owner.getOwner().getEquipment();
      Iterator i=equipment.getGroup(Equipment.MESSENGER).iterator();
      while( i.hasNext() ){
        Equipment.Info info = (Equipment.Info)i.next();
        if ( info.getOptions() == null ) continue;
        if ( "OUT".equalsIgnoreCase(info.getOptions().toString()) )
          this.protocolModel.addElement(info.getEntityName());
      }
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
