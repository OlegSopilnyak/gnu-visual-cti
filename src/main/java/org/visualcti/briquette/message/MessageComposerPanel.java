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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.telephony.*;
import org.visualcti.briquette.core.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The panel for compose the message</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

final class MessageComposerPanel extends JPanel {
/**
 * <attribute>
 * the destination of message
 */
private Symbol destination;
/**
 * <attribute>
 * the source of messages */
private Symbol replyto;
/**
 * <attribute>
 * the type of the message
 */
private Symbol type;
/**
 * <attribute>
 * the binary part of the message
 */
private Symbol attachment;
/**
 * <attribute>
 * the text part of the message
 */
private Symbol text;
/**
 * <attribute>
 * Flag is login to server is necessary
 */
private boolean needLogin;
/**
 * <attribute>
 * The login to server
 */
private Symbol login;
/**
 * <attribute>
 * The the password for login to server
 */
private Symbol password;
/**
 * <attribute>
 * the optional properties of the message
 */
private final ArrayList properties = new ArrayList();
  /**
   * <editor>
   * Abstract string editor
   * */
  private final class attachmentEditor extends SymbolEditor{
    public attachmentEditor(){super();super.title.setText("Attachment");}
    protected final SymbolChooser getSymbolChooser(){return getBinaryDialog();}
    protected final boolean isConstValid(){return false;}
    protected final java.util.List availableGroups(){return binGroups;}
    protected final Symbol getSymbol(){return attachment;}
    protected final void setSymbol(Symbol symbol){attachment=symbol;}
    protected final Symbol getConst(){return PlayAction.system_cti_Voice;}
  }
private final SymbolChooser getBinaryDialog() {
  if (this.owner == null) return null;
  java.util.List all = this.owner.availableSymbols();
  SymbolChooser dialog = SymbolChooser.getInstance(this);
  ArrayList list = new ArrayList();Symbol symbol;
  for(Iterator i=all.iterator();i.hasNext();) {
    if ( (symbol = (Symbol)i.next()) == null) continue;
    int typeID = symbol.getTypeID();
    if (typeID == Symbol.VOICE || typeID==Symbol.FAX || typeID==Symbol.BIN) list.add(symbol);
  }
  dialog.setSymbols( list );
  return dialog;
}
  /**
   * <editor>
   * Abstract string editor
   * */
  private abstract class stringEditor extends SymbolEditor{
    protected abstract String getTitle();
    public stringEditor(){super();super.title.setText(this.getTitle());
      super.name.getDocument().addDocumentListener( new DocListener(){
        protected final void update() {Symbol symbol=getSymbol();
          if ( isAssigned || symbol==null || !symbol.isConst() ) return;
          symbol.setName( name.getText() );
        }
      });
    }
    protected final SymbolChooser getSymbolChooser(){return getStringDialog();}
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
private final SymbolChooser getStringDialog() {
  if (this.owner == null) return null;
  int typeID = Symbol.STRING;
  java.util.List all = this.owner.availableSymbols();
  SymbolChooser dialog = SymbolChooser.getInstance(this);
  ArrayList list = new ArrayList();
  for(Iterator i=all.iterator();i.hasNext();) {
    Symbol symbol = (Symbol)i.next();
    if (symbol != null && symbol.getTypeID() == typeID) list.add(symbol);
  }
  dialog.setSymbols( list );
  return dialog;
}
private Sent owner=null;
private volatile boolean isAssigned=false;
/***
 * The class-editor for destination
 */
private final class destinationEditor extends stringEditor{
    protected final String getTitle(){return "Destination";}
    protected final java.util.List availableGroups(){return groups;}
    protected final Symbol getSymbol(){return destination;}
    protected final void setSymbol(Symbol symbol){destination=symbol;}
    protected final Symbol getConst(){return Symbol.newConst("ANY");}
}
/***
 * The class-editor for replyto
 */
private final class replytoEditor extends stringEditor{
    protected final String getTitle(){return "ReplyTo";}
    protected final java.util.List availableGroups(){return groups;}
    protected final Symbol getSymbol(){return replyto;}
    protected final void setSymbol(Symbol symbol){replyto=symbol;}
    protected final Symbol getConst(){return Symbol.newConst("address");}
}
/***
 * The class-editor for type
 */
private final class typeEditor extends stringEditor{
    protected final String getTitle(){return "Type";}
    protected final java.util.List availableGroups(){return groups;}
    protected final Symbol getSymbol(){return type;}
    protected final void setSymbol(Symbol symbol){type=symbol;}
    protected final Symbol getConst(){return Symbol.newConst("warning");}
}
/***
 * The class-editor for text
 */
private final class textEditor extends stringEditor{
    public textEditor(){super();super.name.setColumns(20);}
    protected final String getTitle(){return "Text";}
    protected final java.util.List availableGroups(){return groups;}
    protected final Symbol getSymbol(){return text;}
    protected final void setSymbol(Symbol symbol){text=symbol;}
    protected final Symbol getConst(){return Symbol.newConst("Enter the text here...");}
}
/***
 * The class-editor for login
 */
private final class loginEditor extends stringEditor{
    protected final String getTitle(){return "Login";}
    protected final java.util.List availableGroups(){return groups;}
    protected final Symbol getSymbol(){return login;}
    protected final void setSymbol(Symbol symbol){login=symbol;}
    protected final Symbol getConst(){return Symbol.newConst("user");}
}
/***
 * The class-editor for password
 */
private final class passwordEditor extends stringEditor{
    protected final String getTitle(){return "Password";}
    protected final java.util.List availableGroups(){return groups;}
    protected final Symbol getSymbol(){return password;}
    protected final void setSymbol(Symbol symbol){password=symbol;}
    protected final Symbol getConst(){return Symbol.newConst("password");}
}
  /**
   * To init internal values of panel
   * @param briquette owner
   */
  final void init(Sent briquette)
  {
    this.isAssigned = true;
    this.owner=briquette;
    this.prepareGroups();
    this.destination = briquette.getDestination().copy();
    this.replyto = briquette.getReplyto().copy();
    this.type = briquette.getType().copy();
    this.attachment = briquette.getAttachment().copy();
    this.text = briquette.getText().copy();
    this.needLogin = briquette.isNeedLogin();
    this.login = briquette.getLogin().copy();
    this.password = briquette.getPassword().copy();
    this.properties.clear();this.properties.addAll(briquette.getProperties());
    // to reload the UIs
    this.destinationUI.reload();
    this.replytoUI.reload();
    this.typeUI.reload();
    this.attachmentUI.reload();
    this.textUI.reload();
    this.needLoginUI.setSelected( this.needLogin );
    this.loginUI.reload();
    this.passwordUI.reload();
    this.loginUI.setEnabled( this.needLogin );
    this.passwordUI.setEnabled( this.needLogin );
    this.propertyModel.removeAllElements();
    for(Iterator i=this.properties.iterator();i.hasNext();)
    {
      Property property=(Property)i.next();
      if ( property != null )
        this.propertyModel.addElement( new propertyLine(property) );
    }
    this.isAssigned = false;
  }
  /**
   * To update the briquette
   * @param briquette briquette to update
   */
  final void update(Sent briquette)
  {
    briquette.setDestination(this.destination);
    briquette.setReplyto(this.replyto);
    briquette.setType(this.type);
    briquette.setAttachment(this.attachment);
    briquette.setText(this.text);
    briquette.setNeedLogin(this.needLogin);
    briquette.setLogin(this.login);
    briquette.setPassword(this.password);
    ArrayList props = briquette.getProperties();
    props.clear();props.addAll(this.properties);
  }
/**
 * <attribute>
 * The list of available Symbol's groups
 * */
  private final void prepareGroups(){
    // to clear the groups
    this.groups.clear(); this.binGroups.clear();
    if ( this.owner == null ) return;
    // to make groups for strings
    int typeID = Symbol.STRING;
    java.util.List all = this.owner.availableSymbols();
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (symbol==null || symbol.isConst() || symbol.getTypeID()!=typeID) continue;
      String group = symbol.getGroup();
      if ( !this.groups.contains(group) ) this.groups.add(group);
    }
    // to make groups for attachment
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (symbol==null || symbol.isConst() ||
          (
          symbol.getTypeID()!=Symbol.VOICE ||
          symbol.getTypeID()!=Symbol.FAX ||
          symbol.getTypeID()!=Symbol.BIN
          )
         ) continue;
      String group = symbol.getGroup();
      if ( !this.binGroups.contains(group) ) this.binGroups.add(group);
    }
  }
/**
 * <attribute>
 * The list of available Symbol's groups for Strings
 * */
private final java.util.List groups = new ArrayList(4);
/**
 * <attribute>
 * The list of available Symbol's groups for attachment
 * */
private final java.util.List binGroups = new ArrayList(4);

// the UIs
private final destinationEditor destinationUI = new destinationEditor();
private final replytoEditor replytoUI = new replytoEditor();
private final typeEditor typeUI = new typeEditor();
private final attachmentEditor attachmentUI = new attachmentEditor();
private final textEditor textUI = new textEditor();
private final JCheckBox needLoginUI = new JCheckBox("Secure");
private final loginEditor loginUI = new loginEditor();
private final passwordEditor passwordUI = new passwordEditor();
  /**
   * <constructor>
   * To make the panel
   */
  MessageComposerPanel()
  {
    super( false );
    super.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
    super.add( this.mainPanel() );
    super.add( this.tabPanel() );
  }
  private final JPanel mainPanel(){
    JPanel panel = new JPanel();
    /*
    Dimension size = new Dimension(400,300);
    panel.setPreferredSize(size);
    panel.setSize(size);
    */
    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
    panel.setBorder(new TitledBorder("The message"));
    // to add destination
    panel.add(Box.createVerticalStrut(4)); panel.add( this.destinationUI );
    // to add replyto
    panel.add(Box.createVerticalStrut(4)); panel.add( this.replytoUI );
    // to add type
    panel.add(Box.createVerticalStrut(4)); panel.add( this.typeUI );
    // to add attachment
    panel.add(Box.createVerticalStrut(4)); panel.add( this.attachmentUI );
    // to add text
    panel.add(Box.createVerticalStrut(4)); panel.add( this.textUI );
    return panel;
  }
  private final JTabbedPane tabPanel(){
    JTabbedPane panel = new JTabbedPane(JTabbedPane.TOP);
    panel.add( "Server", this.serverPanel() );
    panel.add( "Properties", this.propertiesPanel() );
    return panel;
  }
  private final JPanel serverPanel(){
    JPanel server = new JPanel(new BorderLayout(),false);
    server.setBorder(new TitledBorder("Connection"));
    JPanel panel = new JPanel(false);
    server.add(panel,BorderLayout.NORTH);
    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
    // to add checkbox
    panel.add(Box.createVerticalStrut(4)); panel.add( this.needLoginUI );
    // to add checkbox
    panel.add(Box.createVerticalStrut(4)); panel.add( this.loginUI );
    // to add checkbox
    panel.add(Box.createVerticalStrut(4)); panel.add( this.passwordUI );
    // to add the listeners
    this.needLoginUI.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){changeSecurityMode();}
    });
    return server;
  }
  private final void changeSecurityMode(){
      if ( this.isAssigned ) return;
      this.needLogin = this.needLoginUI.isSelected();
      this.loginUI.setEnabled( this.needLogin );
      this.passwordUI.setEnabled( this.needLogin );
  }
private final class propertyControl extends controlPanel{
  public propertyControl(){super();
    super.getButton("add").setToolTipText("To create a property");
    super.getButton("del").setToolTipText("To delete the property");
    super.getButton("up").setToolTipText("To move Up the property");
    super.getButton("down").setToolTipText("To move Down the property");
    super.getButton("edit").setToolTipText("To edit the property");
  }
protected final void Add(){addProperty();}
protected final void Del(){delProperty();}
protected final void Up(){}
protected final void Down(){}
protected final void Edit(){editProperty();}
}
private static uiDialog propertyDialog=null;

private final void checkPropertyDialog(){
  if ( propertyDialog != null ) return;
  synchronized( MessageComposerPanel.class ){
    if ( propertyDialog == null ) propertyDialog=new uiDialog( this.control );
  }
}
private final boolean editProperty(Property property){
  this.checkPropertyDialog();
  try{
    final Property forEdit = new Property( property.getXML() );
    JPanel editor = new JPanel();
    editor.setLayout(new BoxLayout(editor,BoxLayout.Y_AXIS));
    JTextField name = new JTextField(15);
    name.setDocument(new UI.validatingDocument(){
      protected final void validate(String str) throws Exception {
        if (
            str.startsWith(" ") ||
            str.endsWith(" ") ||
            new StringTokenizer(str," \t").countTokens() > 1
            )
          throw new Exception("invalid property's name");
        forEdit.setName(str);
      }
    });
    name.setText( forEdit.getName() );
    editor.add(name);
    final class symEditor extends stringEditor{
      symEditor(){super();super.disableTitle();}
      protected final String getTitle(){return "";}
      protected final java.util.List availableGroups(){return groups;}
      protected final Symbol getSymbol(){return (Symbol)forEdit.getValue();}
      protected final void setSymbol(Symbol symbol){forEdit.setValue(symbol);}
      protected final Symbol getConst(){return Symbol.newConst("value");}
    }
    symEditor valueUI = new symEditor();valueUI.reload();
    editor.add( valueUI );
    editor.setBorder(new TitledBorder("Message's property"));
    propertyDialog.setEditor(editor);
    propertyDialog.setVisible(true);
    if ( propertyDialog.isAccept() ) {
      property.setXML( forEdit.getXML() );
      return true;
    }
    return false;
  }catch(Exception e){
    e.printStackTrace();
    return false;
  }
}
private final void addProperty(){
  Property property=new Property("property",Symbol.newConst("value"));
  if ( this.editProperty(property) ) {
    this.properties.add(property);
    this.propertyModel.addElement( new propertyLine(property) );
  }
}
private final void delProperty(){
  int index = this.list.getSelectedIndex();
  if ( index == -1 ) return;
  Property property=(Property)this.properties.get(index);
  if ( property != null ) {
    this.properties.remove( index );
    this.propertyModel.removeElementAt(index );
  }
}
private final void editProperty(){
  int index = this.list.getSelectedIndex();
  if ( index == -1 ) return;
  Property property=(Property)this.properties.get(index);
  if ( property != null && this.editProperty(property) ) {
    this.properties.set( index, property );
    this.propertyModel.set(index, new propertyLine(property) );
  }
}
private final DefaultListModel propertyModel = new DefaultListModel();
private final JList list = new JList( propertyModel );
private final propertyControl control = new propertyControl();
private final static class propertyLine{
  final Property item;
  propertyLine(Property item){this.item=item;}
  public final String toString(){
    Object val = this.item.getValue();
    if (val instanceof Symbol ){
      return this.item.getName()+"="+((Symbol)val).cell();
    }else return this.item.getName();
  }
}
  private final JPanel propertiesPanel(){
    JPanel panel = new JPanel(new BorderLayout(),false);
    panel.add(new JScrollPane( this.list ),BorderLayout.CENTER);
    panel.add(this.control, BorderLayout.SOUTH);
    return panel;
  }
}
