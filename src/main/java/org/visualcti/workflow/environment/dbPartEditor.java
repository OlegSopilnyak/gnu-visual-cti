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
package org.visualcti.workflow.environment;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import org.visualcti.workflow.facade.*;
import org.visualcti.server.database.*;
import org.visualcti.briquette.core.*;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the editor of DB connection</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class dbPartEditor extends partEditor
{
  private final JTextField driver =new JTextField(12);
  private final JTextField url    =new JTextField(12);
  private final JTextField schema =new JTextField(12);
  private final JTextField login  =new JTextField(12);
  private final JTextField password=new JPasswordField(12);
  private final JCheckBox  autoCommit = new JCheckBox("AutoCommit changes");
  /**
   * <constructor>
   * To make the editor
   */
  public dbPartEditor()
  {
    super.setLayout(new BorderLayout());super.setDoubleBuffered(false);
    JPanel editor = new JPanel( false );
    Border border = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(142, 142, 142)),"Connection");
    editor.setBorder( border );
    editor.setLayout(new BoxLayout(editor,BoxLayout.Y_AXIS));
    this.driver.setToolTipText("The classname of connection's driver");
    this.url.setToolTipText("The URL to the DataBase");
    this.schema.setToolTipText("The schema/owner of the connection");
    this.login.setToolTipText("The connection's login");
    this.password.setToolTipText("The connection's login password");
    this.autoCommit.setToolTipText("To enable/disable the autocommit DB's changes");
    addLine("[D]",this.driver,editor); editor.add(Box.createVerticalStrut(2));
    addLine("[U]",this.url,editor); editor.add(Box.createVerticalStrut(2));
    addLine("[S]",this.schema,editor); editor.add(Box.createVerticalStrut(2));
    addLine("[L]",this.login,editor); editor.add(Box.createVerticalStrut(2));
    addLine("[P]",this.password,editor); editor.add(Box.createVerticalStrut(2));
    editor.add(this.autoCommit);
    this.autoCommit.addItemListener(new ItemListener(){
      public final void itemStateChanged(ItemEvent e){
        if ( !isAssign )request.setAutoCommit(autoCommit.isSelected());
      }
    });
    JButton connect=new JButton("Try"); connect.setToolTipText("Try to connect to the database");
    JPanel panel=new JPanel(new FlowLayout(),false);panel.add(connect);
    editor.add(panel);
    connect.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){tryConnect();}
    });
    super.add( new JScrollPane(editor), BorderLayout.NORTH );
  }
  /*
   * <mutator>
   * To add text's field
   */
  private final void addLine(final String title,final JTextField editor,JPanel place){
    JPanel line = new JPanel(false);line.setLayout(new BoxLayout(line,BoxLayout.X_AXIS));
    line.add(new JLabel(title));
    line.add(Box.createHorizontalStrut(2));
    line.add(editor);
    // to add the listener of editor's changes
    editor.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {editorChanged(title,editor.getText());}
    });
    editor.addFocusListener(new FocusListener(){
      public final void focusLost(FocusEvent e){editor.select(0,0);}
      public final void focusGained(FocusEvent e){editor.selectAll();}
    });
    place.add(line);
  }
  /*
   * <notify>
   * will call when text's field has change
   */
  private final void editorChanged(String title,String text){
    if ( this.isAssign || this.request == null ) return;
    if ( "[D]".equals(title) ) this.request.setDriverClass(text);
    else
    if ( "[U]".equals(title) ) this.request.setURL(text);
    else
    if ( "[S]".equals(title) ) {this.request.setSchema(text);this.login.setText(text);}
    else
    if ( "[L]".equals(title) ) this.request.setLogin(text);
    else
    if ( "[P]".equals(title) ) this.request.setPassword(text);
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
private connectionRequest request=null;
private transient boolean isAssign=false;
  /**
   * <connect>
   * To try to connect to DB
   */
  private final void tryConnect(){
    if ( this.request == null ) return;
    dbTools.refresh( this.request );
    dbMetaData meta = dbTools.getMetaData(this.request);
    MessageFormat format=new MessageFormat("<html><center><H1>{0}</H1><br>{1}</center></html>");
    String message;int type;
    if ( meta.columns.size() > 0){
      String info = "Tables="+meta.tables.size()+
                    ", Views="+meta.views.size()+
                    ", Columns="+meta.columns.size();
      message = format.format(new Object[]{":-)",info});
      type=JOptionPane.INFORMATION_MESSAGE;
    }else{
      message = format.format(new Object[]{":-(","Can't make the connection!"});
      type=JOptionPane.ERROR_MESSAGE;
    }
    JOptionPane.showMessageDialog(this,message,"Database connection",type);
  }
  /**
   * <refresh>
   * To reload visuals from the Chain
   * */
  public final void reload() {
    this.isAssign=true;
    this.clearEditors();
    try{this.request = super.owner.getChain().getConnectionRequest();
    }catch(NullPointerException e){
      this.isAssign=false;
      return;
    }
    if ( this.request != null ){
      this.setEditorsEditable(true);
      this.driver.setText(request.getDriverClass());
      this.url.setText(request.getURL());
      this.schema.setText(request.getSchema());
      this.login.setText(request.getLogin());
      this.password.setText(request.getPassword());
      this.autoCommit.setSelected( request.isAutoCommit() );
    }else this.setEditorsEditable(false);
    this.isAssign=false;
    //System.out.println("DB editor is reloaded....");
  }
  private final void clearEditors(){
    this.driver.setText("");
    this.url.setText("");
    this.schema.setText("");
    this.login.setText("");
    this.password.setText("");
  }
  private final void setEditorsEditable(boolean editable){
    this.driver.setEditable(editable);
    this.url.setEditable(editable);
    this.schema.setEditable(editable);
    this.login.setEditable(editable);
    this.password.setEditable(editable);
  }
  public final String getName() {return "Database";}
  public final void clean() {
    this.request=null;
    System.out.println("DB editor is cleared....");
  }
}
