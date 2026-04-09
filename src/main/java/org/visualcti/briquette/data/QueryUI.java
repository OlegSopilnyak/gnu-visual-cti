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
 * <p>Description: The User Interface for adjust Query</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class QueryUI extends baseOperationUI
{
private final static String columnsTitleText = "Columns...";
private final static String fromTitleText = "From";
private final static String whereTitleText = "Where";
private final static String orderTitleText = "Order By";
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private Query owner = null;
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
private final JTextField columns=new JTextField();
private final JTextField from=new JTextField();
private final JTextField order=new JTextField();
/**
 * <attribute>
 * The editor of where clause
 */
private final LogicUI where = new LogicUI();
      private static final JButton editButton(){
        JButton edit = new JButton("Edit...");
        return edit;
      }
/**
 * <attribute>
 * The button for edit the order
 */
private final JButton editOrder = QueryUI.editButton();
  /**
   * <constructor>
   * To make the UI
   */
  public QueryUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the columns & tables editor
    this.params.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( columnsTitleText, this.params).setHorizontalAlignment(SwingConstants.LEFT);
    this.params.add(this.columns);this.columns.setEditable(false);
    UI_Store.addTitle( fromTitleText, this.params).setHorizontalAlignment(SwingConstants.LEFT);
    this.params.add(this.from);this.from.setEditable(false);
    JPanel colsEditor = new JPanel(new FlowLayout(),false);
    JButton editColumnsSet = QueryUI.editButton();
    editColumnsSet.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){QueryUI.this.editColumnsSet();}
    });
    colsEditor.add(editColumnsSet);
    this.params.add(colsEditor);
    // to add where clause
    UI_Store.addTitle( whereTitleText, this.params).setHorizontalAlignment(SwingConstants.LEFT);
    this.where.setRootLabel("WHERE");
    // to add listener for edit the order
    this.editOrder.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){QueryUI.this.editOrder();}
    });
  }
/**
 * <dialog>
 * The wrapper for editors
 */
private static uiDialog dialog=null;
private final ColumnsSetUI columnsEditor=new ColumnsSetUI();
  /**
   * <action>
   * To activate the dialog for edit columns set
   */
  private final void editColumnsSet(){
    this.checkDialog();
    this.columnsEditor.reload();
    dialog.setTitle("Columns Set...");
    dialog.setEditor( this.columnsEditor );
    dialog.setVisible(true);
    if ( !dialog.isAccept() ) return;
    this.columnsEditor.value.copyTo( this.owner.getColumnsSet() );
    this.owner.refresh();
    this.assigned( this.owner );
  }
private final OrderByUI orderBy = new OrderByUI();
  /**
   * <action>
   * To activate the dialog for edit orders set
   */
  private final void editOrder(){
    this.checkDialog();
    this.orderBy.reload();
    dialog.setTitle("Order By...");
    dialog.setEditor( this.orderBy );
    dialog.setVisible(true);
    if ( !dialog.isAccept() ) return;
    this.orderBy.value.copyTo( this.owner.getOrderBy() );
    this.assigned(this.owner);
  }
    private final void checkDialog(){if(dialog != null)return;
      synchronized(QueryUI.class){
        if ( dialog == null ) dialog=new uiDialog( super.propertiesTree );
      }
    }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    //this.prepareGroups();
    this.propertiesTree.add(this.params, BorderLayout.NORTH);
    this.propertiesTree.add(this.where, BorderLayout.CENTER);
    JPanel southPanel = new JPanel();
    this.propertiesTree.add(southPanel,BorderLayout.SOUTH);
    southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.Y_AXIS ));
    southPanel.add(this.where.getControlPanel());
    // to add order by editor
    JPanel orderPanel = new JPanel(false);
    orderPanel.setLayout(new BoxLayout(orderPanel,BoxLayout.Y_AXIS ));
    southPanel.add(orderPanel);
    orderPanel.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( orderTitleText, orderPanel).setHorizontalAlignment(SwingConstants.LEFT);
    orderPanel.add(this.order);this.order.setEditable(false);
    JPanel orderEditor = new JPanel(new FlowLayout(),false);
    orderEditor.add(this.editOrder);
    orderPanel.add(orderEditor);
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof Query) {
      this.owner=(Query)briquette;
      Query.ColumnsSet cs = this.owner.getColumnsSet();
      Query.OrderBy ord = this.owner.getOrderBy();
      QueryUI.storeTheText( this.columns, cs.columns() );
      QueryUI.storeTheText( this.from,    cs.from()    );
      QueryUI.storeTheText( this.order,   ord.text()   );
      this.where.assign(this.owner.getWhere(),this.owner);
    }
  }
    private static final void storeTheText(JTextField store,String text){
      store.setText(text);store.setCaretPosition(0);
    }
  private final ArrayList getAvailableColumns(){
    ArrayList available = new ArrayList();
    Iterator objs = this.owner.getColumnsSet().getObjects().iterator();
    dbMetaData meta = dbTools.getMetaData(this.owner.getOwner().getConnectionRequest());
    while( objs.hasNext() ){
      String prefix = objs.next()+".";
      for(Iterator i=meta.columns.iterator();i.hasNext();){
        Symbol column = (Symbol)i.next();
        if ( column.getName().startsWith(prefix) ) available.add(column);
      }
    }
    return available;
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){
    this.params.revalidate();
  }
  /**
   * <renderer>
   * Class for visualize the symbol in JList
   */
  private final class SymbolListCellRenderer extends DefaultListCellRenderer{
    public final Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
      super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
      try{super.setText( ((Symbol)value).cell() );
      }catch(ClassCastException e){}
      return this;
    }
  }
  private  static final void sort(DefaultListModel model){
    Object []list = model.toArray();
    java.util.List array = Arrays.asList( model.toArray() );
    Collections.sort(array); model.removeAllElements();
    for(Iterator i=array.iterator();i.hasNext();) model.addElement(i.next());
  }
  /**
   * <editor>
   * Class-panel for edit the Columns set
   */
  private final class ColumnsSetUI extends JPanel {
    private final chooser objects;
    private final chooser columns;
    private ColumnsSetUI(){super(false);
      super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      this.objects=new chooser(){
        protected final void free(Object[] selection){freeObjects(selection);}
        protected final void select(Object[] selection){selectObjects(selection);}
      };
      this.objects.setBorder(new TitledBorder("[Objects]"));
      super.add(this.objects);
      this.columns=new chooser(){
        protected final void free(Object[] selection){freeColumns(selection);}
        protected final void select(Object[] selection){selectColumns(selection);}
      };
      this.columns.aList.setCellRenderer(new SymbolListCellRenderer());
      this.columns.sList.setCellRenderer(new SymbolListCellRenderer());
      this.columns.setBorder(new TitledBorder("[Columns]"));
      super.add(this.columns);
    }
    /**
     * <postaction>
     * To do after some objects unselected
     * @param selection the array of selected items
     */
    private final void freeObjects(Object[]selection){
      for(int i=0;i < selection.length;i++){
        this.freeColumns(this.columns.available,selection[i]+".");
        this.freeColumns(this.columns.selected, selection[i]+".");
      }
      dbMetaData meta = dbTools.getMetaData(owner.getOwner().getConnectionRequest());
      this.fillAvailableObjects( meta );
    }
      private final void freeColumns(DefaultListModel model,String prefix){
        ArrayList valid = new ArrayList();
        for(Enumeration e = model.elements();e.hasMoreElements();){
          try{
            Symbol column = (Symbol)e.nextElement();
            if ( !column.getName().startsWith(prefix) ) valid.add(column);
            else this.value.removeColumn( column );
          }catch(Exception ex){}
        }
        model.removeAllElements();
        for(Iterator i=valid.iterator();i.hasNext();) model.addElement(i.next());
      }
    /**
     * <postaction>
     * To do after some objects selected
     * @param selection the array of selected items
     */
    private final void selectObjects(Object[]selection){
      ArrayList columns=new ArrayList();
      dbMetaData meta = dbTools.getMetaData(owner.getOwner().getConnectionRequest());
      for(int i=0;i < selection.length;i++) {
        columns.addAll( this.getColumns(selection[i], meta) );
      }
      for(Iterator i=columns.iterator();i.hasNext();)
      {
        Symbol column = (Symbol)i.next();
        if ( !this.isSelected(column) ) this.columns.available.addElement(column);
      }
    }
      private final ArrayList getColumns(Object name, dbMetaData meta){
        ArrayList valid = new ArrayList(); String prefix = name+".";
        for(Iterator i=meta.columns.iterator();i.hasNext();){
          try{
            Symbol column = (Symbol)i.next();
            if ( column.getName().startsWith(prefix) ) valid.add(column);
          }catch(Exception ex){}
        }
        return valid;
      }
    /**
     * <postaction>
     * To do after unselect the columns
     * @param selection the pool
     */
    private final void freeColumns(Object[]selection){
      for(int i=0;i < selection.length;i++){
        try{this.value.removeColumn((Symbol)selection[i]);
        }catch(Exception e){}
      }
      dbMetaData meta = dbTools.getMetaData(owner.getOwner().getConnectionRequest());
      this.fillAvailableColumns( meta );
    }
    /**
     * <postaction>
     * To do after select the columns
     * @param selection the pool
     */
    private final void selectColumns(Object[]selection){
      for(int i=0;i < selection.length;i++){
        try{this.value.addColumn((Symbol)selection[i]);
        }catch(Exception e){}
      }
    }
    /**
     * <attribute>
     * The editable value
     */
    private Query.ColumnsSet value;
    /**
     * <refresh>
     * To reload the model from the owner
     */
    private final void reload(){
      this.value=owner.getColumnsSet().copy();
      this.objects.clear();this.columns.clear();
      for(Iterator i=this.value.getObjects().iterator();i.hasNext();){
        String name = (String)i.next();
        this.objects.selected.addElement( name );
        this.addObjectColumns( name );
      }
      dbMetaData meta = dbTools.getMetaData(owner.getOwner().getConnectionRequest());
      this.fillAvailableObjects( meta );
      this.fillAvailableColumns( meta );
    }
      private final void fillAvailableObjects(dbMetaData meta){
        this.objects.available.removeAllElements();
        ArrayList objects = new ArrayList();
        objects.addAll(meta.tables); objects.addAll(meta.views);
        for(Iterator i=objects.iterator();i.hasNext();){
          String name = (String)i.next();
          if ( this.isSelected(name) ) continue;
          this.objects.available.addElement( name );
        }
      }
      private final void fillAvailableColumns(dbMetaData meta){
        this.columns.available.removeAllElements();
        for(Iterator i=meta.columns.iterator();i.hasNext();){
          Symbol column = (Symbol)i.next();
          if ( this.isSelected(column) ) continue;
          String table = Query.ColumnsSet.table( column );
          if ( this.isSelected( table ) ) this.columns.available.addElement(column);
        }
      }
      private final void addObjectColumns(String name){
        for(Iterator i=this.value.getObject(name).iterator();i.hasNext();){
          this.columns.selected.addElement( (Symbol)i.next() );
        }
      }
      private final boolean isSelected(String name){
        return this.objects.selected.contains(name);
      }
      private final boolean isSelected(Symbol column){
        return this.columns.selected.contains(column);
      }
  }
  /**
   * <chooser>
   * To move items
   */
  private abstract class chooser extends JPanel{
    DefaultListModel available=new DefaultListModel();
    DefaultListModel selected=new DefaultListModel();
    private final void clear(){
      this.available.removeAllElements();
      this.selected.removeAllElements();
    }
    private final JList addList(String title,ListModel model){
      JPanel listPanel = new JPanel(new BorderLayout(),false);
      listPanel.setBorder(new TitledBorder(title));
      JList list = new JList(model);
      list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      JScrollPane scroll = new JScrollPane(list);
      scroll.setPreferredSize( new Dimension(200,120) );
      listPanel.add(scroll ,BorderLayout.CENTER); super.add(listPanel);
      return list;
    }

    protected abstract void free(Object[] selection);
    protected abstract void select(Object[] selection);

    private final void addControlPanel(){
      JPanel controlPanel = new JPanel();
      controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.Y_AXIS));
      controlPanel.add(Box.createVerticalStrut(10));
      JButton free = new JButton("<="); controlPanel.add(free);
      free.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          chooser.this.Free( sList.getSelectedValues() );
        }
      });
      controlPanel.add(Box.createVerticalStrut(2));
      JButton select = new JButton("=>"); controlPanel.add(select);
      select.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          chooser.this.Select( aList.getSelectedValues() );
        }
      });
      super.add( controlPanel );
    }
    private final void Free(Object[] selection){
      if ( selection.length == 0 ) return;
      for(int i=0;i < selection.length;i++){
        this.available.addElement(selection[i]);
        this.selected.removeElement(selection[i]);
      }
      QueryUI.sort( this.available );
      this.free( selection );
    }
    private final void Select(Object[] selection){
      if ( selection.length == 0 ) return;
      for(int i=0;i < selection.length;i++){
        this.selected.addElement(selection[i]);
        this.available.removeElement(selection[i]);
      }
      QueryUI.sort( this.selected );
      this.select( selection );
    }
    private final JList aList,sList;
    private chooser(){
      super.setDoubleBuffered(false);
      super.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
      // available panel
      this.aList = this.addList("Available",this.available);
      // control panel
      this.addControlPanel();
      // selected panel
      this.sList = this.addList("Selected",this.selected);
    }
  }
   /**
   * <renderer>
   * Class for visualize the symbol in JList
   */
  private final class OrderBy_Item_ListCellRenderer extends DefaultListCellRenderer{
    public final Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
      super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
      try{
        Query.OrderBy.Item item = (Query.OrderBy.Item)value;
        super.setText( item.getColumn().getName()+(item.isDescend()?" DESC":" ASC") );
      }catch(ClassCastException e){}
      return this;
    }
  }
 /**
   * <editor>
   * Class for edit OrderBy clause
   */
  private final class OrderByUI extends JPanel{
    private Query.OrderBy value;
    private final DefaultListModel model=new DefaultListModel();
    private final DefaultListSelectionModel selection;
    private final Control control=new Control();
    private final JTextField name = new JTextField(12);
    private final JComboBox direction=new JComboBox(new String[]{"DESC","ASC"});
    private OrderByUI(){
      super.setDoubleBuffered(false);
      super.setLayout( new BorderLayout() );
      this.name.setToolTipText("The name of column");
      this.direction.setToolTipText("The order's direction");
      final JList list = new JList( this.model );
      list.setCellRenderer( new OrderBy_Item_ListCellRenderer() );
      this.selection = (DefaultListSelectionModel)list.getSelectionModel();
      this.selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.selection.addListSelectionListener(new ListSelectionListener(){
        public final void valueChanged(ListSelectionEvent e){
          if ( !isReload && e.getValueIsAdjusting()==false) itemIndexSelected( list.getSelectedIndex() );
        }
      });
      JScrollPane scroll = new JScrollPane(list);
      scroll.setPreferredSize( new Dimension(200,120) );
      super.add(scroll,BorderLayout.CENTER);
      JPanel ctrl=new JPanel(false); ctrl.setLayout(new BoxLayout(ctrl,BoxLayout.Y_AXIS));
      ctrl.add(this.control);
      JPanel editor=new JPanel(new FlowLayout());
      editor.add(this.name); editor.add(this.direction);
      this.direction.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          if ( isReload ) return;
          Query.OrderBy.Item item=getSelectedItem();
          if ( item != null) {
            item.setDescend( direction.getSelectedIndex()==0 );
            refreshSelectedItem();
          }
        }
      });
      ctrl.add(editor);this.name.setEditable(false);
      super.add(ctrl,BorderLayout.SOUTH);
    }
    private transient int currentIndex=-1;
    private final void itemIndexSelected(int index){
      this.clearItemsControl();
      try{
        Query.OrderBy.Item item = (Query.OrderBy.Item)model.elementAt(index);
        this.name.setText( item.getColumn().getName() ); this.name.setCaretPosition(0);
        this.direction.setEnabled(true);
        this.isReload=true;
          this.direction.setSelectedIndex( item.isDescend() ? 0:1 );
          this.currentIndex = index;
        this.isReload=false;
        this.control.allFeatures();
      }catch(Exception e){}
    }
    private final Query.OrderBy.Item getSelectedItem(){
      try{return (Query.OrderBy.Item)model.elementAt( this.currentIndex );
      }catch(Exception e){
        return null;
      }
    }
    private final void refreshSelectedItem(){
      try{
        Object item=this.model.getElementAt(this.currentIndex);
        this.model.setElementAt(item,this.currentIndex);
        this.value.update(getSelectedItem(), this.currentIndex);
      }catch(Exception e){}
    }
    private transient boolean isReload=false;
    class Control extends controlPanel{
      Control(){
          super.getButton("add").setToolTipText("To create an Item");
          super.getButton("del").setToolTipText("To delete the Item");
          super.getButton("up").setToolTipText("To move Up the Item");
          super.getButton("down").setToolTipText("To move Down the Item");
          super.remove( super.getButton("edit") );
          super.getButton("edit").setToolTipText("To edit the Item");
        }
      protected final void Add(){addItem();}
      protected final void Del(){delItem();}
      protected final void Up(){upItem();}
      protected final void Down(){downItem();}
      protected final void Edit(){}
    }
    private final void addItem(){
      SymbolChooser chooser=SymbolChooser.getInstance(this);
      ArrayList valid = new ArrayList();
      for(Iterator i=getAvailableColumns().iterator();i.hasNext();){
        Symbol column=(Symbol)i.next();
        if ( !this.value.contains(column) ) valid.add(column);
      }
      if ( valid.size() <= 0 )
      {
        JOptionPane.showMessageDialog
                (
                dialog.getParent(),
                "No availabled Symbols",
                "Symbols set...",
                JOptionPane.INFORMATION_MESSAGE
                );
        return;
      }
      chooser.setSymbols( valid );
      try{
        chooser.setChoosed(this.getSelectedItem().getColumn());
      }catch(NullPointerException e){}
      chooser.setVisible(true);
      Symbol choosed = chooser.getChoosed();
      if ( choosed == null ) return;
      Query.OrderBy.Item item = new Query.OrderBy.Item(choosed);
      this.value.add( item );
      this.clearItemsControl(); this.fillItemsList();
      int index = this.model.indexOf(item);
      if ( index != -1) this.selection.setSelectionInterval(index,index);
    }
    private final void delItem(){
      Query.OrderBy.Item item = this.getSelectedItem();
      if ( item == null ) return;
      this.model.removeElement(item);
      this.value.delete(this.currentIndex);
    }
    private final void upItem(){
      Query.OrderBy.Item item = this.getSelectedItem();
      if ( item == null ) return;
      this.value.moveUp( this.currentIndex );
      this.clearItemsControl();
      this.fillItemsList();
      int index = this.model.indexOf(item);
      if ( index != -1) this.selection.setSelectionInterval(index,index);
    }
    private final void downItem(){
      Query.OrderBy.Item item = this.getSelectedItem();
      if ( item == null ) return;
      this.value.moveDown( this.currentIndex );
      this.clearItemsControl();
      this.fillItemsList();
      int index = this.model.indexOf(item);
      if ( index != -1) this.selection.setSelectionInterval(index,index);
    }
    private final void reload(){
      this.value = owner.getOrderBy().copy();
      this.isReload = true;
      this.clearItemsControl();
      this.fillItemsList();
      this.isReload = false;

    }
    private final void clearItemsControl(){
      this.currentIndex=-1;
      this.direction.setEnabled(false);
      this.name.setText("");
      this.control.addOnly();
    }
    private final void fillItemsList(){
      this.model.removeAllElements();
      for(Iterator i = this.value.items().iterator();i.hasNext();){
        this.model.addElement( i.next() );
      }
    }
  }
}
