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
package org.visualcti.briquette.data;

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
 * <p>Description: The User Interface for adjust Update</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class UpdateUI extends baseOperationUI
{
private final static String tableTitleText = "Table...";
private final static String setTitleText = "Set";
private final static String whereTitleText = "Where";
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private Update owner = null;
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
/**
 * <editor>
 * The editor of table's name
 */
private final TableUI table = new TableUI();
/**
 * <editor>
 * To edit the columns set
 */
private final SetUI set = new SetUI();
/**
 * <editor>
 * The editor of where's clause
 */
private final WhereUI where = new WhereUI();
  /**
  * <constructor>
  */
  public UpdateUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add set's editor
    this.params.add(Box.createVerticalStrut(4));
    this.params.add( this.set );
    // to add where's editor
    this.params.add(Box.createVerticalStrut(4));
    this.params.add( this.where );
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.propertiesTree.add(this.table, BorderLayout.NORTH);
    // to add the container of editors
    this.propertiesTree.add(this.params, BorderLayout.CENTER);
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof Update) {
      this.owner=(Update)briquette;
      this.table.reload();
      this.set.reload();
      this.where.reload();
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
private static transient uiDialog dialog=null;
  private final void checkDialog(){
    if ( dialog != null ) return;
    synchronized( UpdateUI.class ){
      if ( dialog == null ) dialog=new uiDialog(this.params);
    }
  }
  /***
   * Class-editor of table's name
   */
  private final class TableUI extends JPanel{
    private final JTextField name=new JTextField();
    private final JButton choose=new JButton("...");
    private final JPanel tables=new JPanel(new BorderLayout(),false);
    private final DefaultListModel model=new DefaultListModel();
    private String table=null;
    TableUI(){super(new BorderLayout(),false);
      this.name.setEditable(false);
      UI_Store.addTitle(tableTitleText,this,BorderLayout.NORTH).setHorizontalAlignment(SwingConstants.LEFT);
      super.add(this.name,BorderLayout.CENTER);
      this.choose.setMargin(new Insets(1,1,1,1));
      this.choose.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){edit();}
      });
      super.add(this.choose,BorderLayout.EAST);
      final JList list=new JList(model);
      list.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public final void valueChanged(ListSelectionEvent e){
          TableUI.this.table=(String)list.getSelectedValue();
        }
      });
      this.tables.add( new JScrollPane(list) );
      this.tables.setBorder(new TitledBorder("Objects...") );
    }
    private final void reload(){
      if ( owner == null ) return;
      this.name.setText( owner.getTable() );
      this.name.setCaretPosition( 0 );
    }
    private final void edit(){
      if ( owner == null ) return;
      checkDialog();
      dbMetaData meta=dbTools.getMetaData( owner.getOwner().getConnectionRequest() );
      if ( meta.columns.size() <= 0 )
      {
        JOptionPane.showMessageDialog
                (
                dialog.getParent(),
                "No availabled Tables",
                "Tables set...",
                JOptionPane.INFORMATION_MESSAGE
                );
        return;
      }
      checkDialog(); ArrayList objects = new ArrayList();
      objects.addAll(meta.tables);objects.addAll(meta.views);
      String tableName=this.name.getText();
      this.model.removeAllElements();
      for(Iterator i=objects.iterator();i.hasNext();){
        String item=(String)i.next();
        if ( !item.equalsIgnoreCase(tableName) ) this.model.addElement(item);
      }
      this.table=null;dialog.setTitle("Tables set...");
      dialog.setEditor( this.tables ); dialog.setVisible(true);
      if ( dialog.isAccept() && this.table != null ) {
        owner.setTable(this.table);
        UpdateUI.this.assigned(owner);
      }
    }
  }
private static final Dimension treeSize = new Dimension(200,90);
  private final class SetUI extends JPanel{
    private final MathExpressionsSetUI ui=new MathExpressionsSetUI(){
      protected String getEditorTitle(){return "Column";}
    };
    public SetUI(){super(new BorderLayout(),false);
      JScrollPane scroll = new JScrollPane( this.ui.getTree() );
      scroll.setPreferredSize(treeSize);
      this.ui.setRootLabel("SET");
      UI_Store.addTitle(setTitleText,this,BorderLayout.NORTH).setHorizontalAlignment(SwingConstants.LEFT);
      super.add( scroll, BorderLayout.CENTER );
      super.add( this.ui.getControlPanel(), BorderLayout.SOUTH );
    }
    private final void reload(){this.ui.assign(owner.getSet(),owner);}
  }
  /***
   * Class editor of where clause
   */
  private final class WhereUI extends JPanel{
    private final LogicUI ui=new LogicUI();
    private WhereUI(){super(new BorderLayout(),false);
      this.ui.setRootLabel("WHERE");
      this.ui.setPreferredSize(treeSize);
      UI_Store.addTitle(whereTitleText,this,BorderLayout.NORTH).setHorizontalAlignment(SwingConstants.LEFT);
      super.add(this.ui,BorderLayout.CENTER);
      super.add(this.ui.getControlPanel(),BorderLayout.SOUTH);
    }
    private final void reload(){this.ui.assign(owner.getWhere(),owner);}
  }
}
