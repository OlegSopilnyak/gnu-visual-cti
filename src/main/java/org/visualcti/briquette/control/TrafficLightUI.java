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
package org.visualcti.briquette.control;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User's Interface for TrafficLight properties</p>
 * <p>Copyright: Copyright Prominic Inc $ Prominic Ukraine Co.(c) 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class TrafficLightUI extends baseOperationUI {

/**
 * <notify>
 * To notify about Operation assigned
 * */
protected final void assigned(Operation briquette)
{
  if (briquette instanceof TrafficLight) {
    TrafficLight semaphore = (TrafficLight)briquette;
    TrafficLight.Pool set = semaphore.dowSet;
    final String[] names = set.getNames();
    ListModel lm = new AbstractListModel() {
      public int getSize() { return names.length; }
      public Object getElementAt(int index) {return names[index];}
    };
    this.table.setModel( new greenModel(semaphore) );
    this.table.setDefaultRenderer(Boolean.class,new BooleanRenderer());
    this.table.setDefaultEditor(Boolean.class,new BooleanEditor(set));
    int maxCol = this.table.getColumnCount();
    for(int i=0;i < maxCol;i++)
    {
      String key = String.valueOf(i);
      TableColumn column = this.table.getColumn(key);
      column.setMaxWidth(12);
    }
    JList rowHeader = new JList(lm);
    rowHeader.setForeground(Color.cyan);
    rowHeader.setBackground(Color.blue);
    rowHeader.setFont(theFont);
    rowHeader.setFixedCellWidth(25);
    rowHeader.setFixedCellHeight(this.table.getRowHeight()
                               /*+ this.table.getRowMargin()*/);
    rowHeader.setCellRenderer(new RowHeaderRenderer(table));
    this.scroll.setRowHeaderView(rowHeader);
  }
}
private class RowHeaderRenderer extends JLabel implements ListCellRenderer {
  RowHeaderRenderer(JTable table)
  {
    JTableHeader header = table.getTableHeader();
    setOpaque(true);
    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    setHorizontalAlignment(CENTER);
    setForeground(header.getForeground());
    setBackground(header.getBackground());
    setFont(header.getFont());
  }
  public Component getListCellRendererComponent
                      (
                      JList list,
                      Object value,
                      int index,
                      boolean isSelected,
                      boolean cellHasFocus
                      )
  {
    setText((value == null) ? "" : value.toString());
    return this;
  }
}
/**
 * <attribute>
 * The component for edit the property
 * */
private final JScrollPane scroll;
/**
 * <attribute>
 * The table for hours
 * */
private final JTable table;
/**
 * <const>
 * The font for all things
 * */
private final static Font theFont = new Font("monospaced",Font.PLAIN,10);

    public TrafficLightUI()
    {
      this.table = new JTable( );
      this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.table.setFont(theFont);

      JTableHeader header = table.getTableHeader();
      header.setFont(theFont);
      header.setUpdateTableInRealTime( false );
      header.setReorderingAllowed( false );

      this.scroll = new JScrollPane( table );
    }
/**
 * <const>
 * The text of property's title
 * */
private final static String titleText = "The times diagram";
    /**
     * <action>
     * To prepare parts of UI for editing
     * */
    protected final void prepare(){
      if (this.propertiesTree.getLayout() instanceof BorderLayout){}
      else this.propertiesTree.setLayout( new BorderLayout() );
      JLabel title = new JLabel(TrafficLightUI.titleText,JLabel.CENTER);
      title.setFont(TrafficLightUI.titleFont);
      title.setForeground(TrafficLightUI.titleColor);
      this.propertiesTree.add(title,BorderLayout.NORTH);
      this.propertiesTree.add(this.scroll,BorderLayout.CENTER);
      this.propertiesTree.repaint();
    }
private static final ImageIcon disabled = UI_Store.makeIcon(UI_Store.class,"/icon/redBall.gif");
private static final ImageIcon enabled = UI_Store.makeIcon(UI_Store.class,"/icon/greenBall.gif");
    /**
     * <renderer>
     * To represent the cell of table
     * */
    final class BooleanRenderer extends DefaultTableCellRenderer
    {
	public BooleanRenderer() {
	    super();
            super.setHorizontalAlignment(JLabel.CENTER);
            Dimension size = new Dimension(12,12);
            this.setSize(size);
            this.setPreferredSize(size);
            this.setMinimumSize(size);
            this.setMaximumSize(size);
	}

        public Component getTableCellRendererComponent
                                                  (
                                                  JTable table,
                                                  Object value,
						  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row,
                                                  int column
                                                  )
        {
          Icon icon = ((Boolean)value).booleanValue() ? enabled:disabled;
          this.setIcon(icon);
          return this;
        }
    }
    /**
     * <editor>
     * The class for updating the cell
     * */
    final class BooleanEditor extends AbstractCellEditor implements TableCellEditor {
        private TrafficLight.Pool set;
        private int day=0,hour=0;
        private final JLabel editor = new JLabel(disabled);
	public BooleanEditor(TrafficLight.Pool set) {this.set=set;}
        public Component getTableCellEditorComponent(JTable table, Object value,
                                              boolean isSelected,
                                              int row, int column)
        {
          this.day=row; this.hour=column;
          boolean selected = !this.set.isEnabled(this.day,this.hour);
          this.set.setEnabled(this.day,this.hour,selected);
          this.editor.setIcon(selected ? enabled:disabled);
          return this.editor;
        }
        public Object getCellEditorValue()
        {
          boolean selected = this.set.isEnabled(this.day,this.hour);
          return selected ? Boolean.TRUE:Boolean.FALSE;
        }
    }
    /**
     * <model>
     * The model of the JTable
     * */
    final class greenModel extends AbstractTableModel
    {
      private TrafficLight.Pool set;
      greenModel(TrafficLight owner){this.set=owner.dowSet;}
      public final String getColumnName(int column){return String.valueOf(column);}
      public final Class getColumnClass(int columnIndex){return Boolean.class;}
      public final Object getValueAt(int rowIndex, int columnIndex){
        boolean enabled = this.set.isEnabled(rowIndex,columnIndex);
        return enabled ? Boolean.TRUE : Boolean.FALSE;
      }
      public void setValueAt(Object aValue, int row, int col) {
        boolean enabled = ((Boolean)aValue).booleanValue();
        this.set.setEnabled(row,col,enabled);
      }
      public final boolean isCellEditable(int rowIndex, int columnIndex) {
          return true;
      }
      public final int getColumnCount(){return 24;}
      public final int getRowCount(){return 7;}
    }
}
