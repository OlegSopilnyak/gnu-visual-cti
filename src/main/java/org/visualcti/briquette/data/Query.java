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

import org.jdom.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;
import java.util.*;
import java.text.*;
import java.sql.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To make the SQL's query to the DataSource</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Query extends Basis
{
    /**
     * <accessor>
     * To get access to new ID's prefix
     * @return the prefix
     */
    public final String get_ID_prefix(){return "Query.";}
    /**
     * <constructor>
     * */
    public Query()
    {
      super.setAbout("To request data from DB");
      this.initRuntime();
    }
      private final void initRuntime(){
        this.columns.init(); this.order.init();
        try{this.where.setXML( null );}catch(Exception e){}
      }
    /**
     * To check briquette's integrity
     */
    final void refresh()
    {
      ArrayList objects = this.columns.getObjects();
      for (ListIterator i=this.order.pool.listIterator();i.hasNext();)
      {
        OrderBy.Item item = (OrderBy.Item)i.next();
        String name = ColumnsSet.table( item.getColumn() );
        if ( !objects.contains(name) ) i.remove();
      }
    }
    /**
     * To get access to dbColumns of this briquette
     * Design mode only
     * @return the list of dbColumns
     */
    protected final List dbColumnsSymbol()
    {
      ArrayList columns=new ArrayList(), objects = this.columns.getObjects();
      for(Iterator i=super.dbColumnsSymbol().iterator();i.hasNext();)
      {
        Symbol column = (Symbol)i.next();
        if ( objects.contains(ColumnsSet.table(column)) ) columns.add(column);
      }
      return columns;
    }
/**
 * <attribute>
 * The columns set of Query
 */
private final ColumnsSet columns=new ColumnsSet();
  /**
   * <accessor>
   * To get access to columns set
   * @return Query's columns set
   */
  final ColumnsSet getColumnsSet(){return this.columns;}
/**
 * <attribute>
 * The filter of query
 */
private final Logic where = new Logic();
  /**
   * <accessor>
   * To get access to where's clause
   * @return query's filter
   */
  final Logic getWhere(){return this.where;}
/**
 * <attribute>
 * The "odrer by" clause
 */
private final OrderBy order = new OrderBy();
  /**
   * <accessor>
   * To get access to query's order
   * @return the order
   */
  final OrderBy getOrderBy(){return this.order;}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * @param xml container to store
     */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( this.columns.getXML() );
      xml.addContent( this.where.getXML() );
      xml.addContent( this.order.getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * @param xml container to restore
     * @throws Exception if some wrong
     */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.initRuntime();
      this.columns.setXML( xml.getChild(ColumnsSet.ELEMENT) );
      this.where.setXML( xml.getChild(Logic.ELEMENT) );
      this.order.setXML( xml.getChild(OrderBy.ELEMENT) );
      this.refresh();
    }
  /**
   * <action>
   * To execute the briquette's content
   * @param caller the owner of briquette
   */
  protected void dataMethod(Subroutine caller)
  {
    // to remove all columns's values
    caller.clear( Symbol.DBCOLUMN );
    // clear accessible's flag
    caller.set(Basis.system_db_DataAccess,"No");
    // make SQL clause
    String request = makeSQL( caller );
    try {
      // to close results set and statement
      super.context.closeResultSet();
      super.context.closeStatement();
      // to make the statement
      Statement stmt = super.context.database().createStatement();
      ResultSet rs = stmt.executeQuery( request );
      StringBuffer message = new StringBuffer("SQL[").append(request);
      message.append("] Data Accessible - ");
      super.context.setStatement( stmt );
      super.context.setResultSet( rs );
      this.columns.fillContext( super.context );
      // to retrive the data
      if ( rs.next() ) {
        // data exists
        dbTools.fill( rs, caller );
        caller.set(Basis.system_db_DataAccess,"Yes");
        message.append("Yes");
      }else{
        // to close results set and statement
        super.context.closeResultSet();
        super.context.closeStatement();
        message.append("No");
      }
      caller.info( message.toString() );
    }catch(Exception e){}
  }
/**
 * The format of query SQL's pattern
 * 0. columns list
 * 1. objects list
 * 2. "where" clause
 * 3. "order by" clause
 */
private static final MessageFormat pattern=new MessageFormat("SELECT {0} FROM {1} {2} {3}");
  private final String makeSQL(Subroutine caller){
    String where = this.where.getSQL(caller).trim();
    where = "".equals(where) ? "":"WHERE "+where;
    return
      Query.pattern.format(new Object[]
                                        {
                                        this.columns.columns(),
                                        this.columns.from(),
                                        where,
                                        this.order.clause()
                                        }
                          );
  }
  /**
   * Class-container of columns set
   * */
  public static final class ColumnsSet
  {
    public final static String ELEMENT="columns_set";
    /**
     * To store the context to other object
     * @param other store to
     */
    public final void copyTo(ColumnsSet other)
    {
      other.columns.clear();other.columns.addAll(this.columns);
      other.objects.clear();other.objects.putAll(this.objects);
    }
    /**
     * <clone>
     * To get copy of coluns set
     * @return the copy
     */
    public final ColumnsSet copy()
    {
      ColumnsSet copy = new ColumnsSet();
      copy.columns.clear(); copy.columns.addAll( this.columns );
      copy.objects.clear(); copy.objects.putAll( this.objects );
      return copy;
    }
    private final void setXML(Element xml) throws Exception{
      this.init();
      if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
      List objs = xml.getChildren(Property.ELEMENT);
      for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();){
        Property prop = new Property((Element)i.next());
        this.addColumn( prop.getValue((Symbol)null) );
      }
    }
    private final Element getXML(){
      Element xml=new Element(ELEMENT);
      for( Iterator i=this.getObjects().iterator();i.hasNext();){
        String oName = (String)i.next();
        this.addObjectXML(xml,oName);
      }
      return xml;
    }
    private final void addObjectXML(Element xml,String oName){
      if ( oName == null ) return;
      for(Iterator i=this.getObject(oName).iterator();i.hasNext();){
        Symbol column = (Symbol)i.next();
        xml.addContent( new Property(oName,column).getXML() );
      }
    }
    /**
     * <mutator>
     * To add column
     * Design mode only
     * @param column the column
     */
    public final void addColumn(Symbol column)
    {
      if ( column==null || column.getGroupID() != Symbol.DBCOLUMN ) return;
      String objName = getObjectName( column.getName() );
      if ( objName != null )
      {
        this.getObjectColumns( objName ).add(column);
        this.columns.add(column);
      }
    }
    /**
     * <mutator>
     * To remove the column
     * Design mode only
     * @param column the column
     */
    public final void removeColumn(Symbol column)
    {
      if ( column.getGroupID() != Symbol.DBCOLUMN ) return;
      String objName = getObjectName( column.getName() );
      if ( objName != null )
      {
        List oColumns = this.getObjectColumns( objName ); oColumns.remove(column);
        if ( oColumns.size() == 0 ) this.objects.remove(objName);
        this.columns.remove(column);
      }
    }
    /**
     * <accessor>
     * To get access to object's columns by name
     * Design mode only
     * @param name the name of the Object
     * @return the list of columns
     */
    public final ArrayList getObject(String name)
    {
      ArrayList oColumns = new ArrayList();
      oColumns.addAll(this.getObjectColumns(name));
      Collections.sort( oColumns );
      return oColumns;
    }
    /**
     * <accessor>
     * To get access to names of objects
     * Design mode only
     * @return the list of names
     */
    public final ArrayList getObjects()
    {
      ArrayList objs = new ArrayList();
      objs.addAll( this.objects.keySet() );
      Collections.sort( objs );
      return objs;
    }
    /**
     * <accessor>
     * To get access to columns list.
     * @return the Query's columns
     */
    public final String columns()
    {
      StringBuffer cols=new StringBuffer(); boolean first=true;
      ArrayList temp = new ArrayList(); temp.addAll(this.columns);
      Collections.sort(temp);
      for(Iterator i=temp.iterator();i.hasNext();)
      {
        Symbol column = (Symbol)i.next();
        if ( first ) first=false; else cols.append(", ");
        cols.append( column.getName() );
      }
      return cols.toString();
    }
    /**
     * <transfer>
     * To fill the  connection's context
     * @param context
     */
    private final void fillContext(Context context){
      context.getColumns().clear();
      context.getColumns().addAll(this.columns);
    }
    /**
     * <accessor>
     * To get access to objects names list
     * @return names list
     */
    public final String from()
    {
      StringBuffer objs=new StringBuffer(); boolean first=true;
      ArrayList temp = new ArrayList(); temp.addAll(this.columns);
      Collections.sort(temp);
      ArrayList names=new ArrayList();
      for(Iterator i=temp.iterator();i.hasNext();)
      {
        String table = table( (Symbol)i.next() );
        if ( !names.contains(table) )
        {
          names.add( table );
          if ( first ) first=false; else objs.append(", ");
          objs.append( table );
        }
      }
      return objs.toString();
    }
    static final String table(Symbol column){
      try {
        return new StringTokenizer(column.getName(),".").nextToken();
      }catch(NoSuchElementException e){}
      return "???";
    }
    private static final String getObjectName(String name){
      StringTokenizer st=new StringTokenizer(name,".");
      if ( st.countTokens() != 2 ) return null;
      return st.nextToken();
    }
    private static final String getColumnName(String name){
      StringTokenizer st=new StringTokenizer(name,".");
      if ( st.countTokens() != 2 ) return null;
      st.nextToken();return st.nextToken();
    }
    private final ArrayList getObjectColumns(String name){
      ArrayList list = (ArrayList)this.objects.get(name);
      if ( list == null){
        list = new ArrayList(); this.objects.put(name,list);
      }
      return list;
    }
    private final void init(){this.columns.clear();this.objects.clear();}
    /**
     * <attribute>
     * The list of columns to query
     */
    private final ArrayList columns=new ArrayList();
    /**
     * <attribute>
     * The pool of query's objects
     */
    private final HashMap objects=new HashMap();
  }
  /**
   * Class-container of "ORDER BY" expression
   */
  public static final class OrderBy
  {
    public static final String ELEMENT = "order_by";
    /**
     * <check>
     * Is symbol presents in the pool
     * @param symbol symbol to check
     * @return true if contains
     */
    public final boolean contains(Symbol symbol)
    {
      for(Iterator i=this.pool.iterator();i.hasNext();)
      {
        Item item=(Item)i.next();
        if ( item.getColumn().equals(symbol) ) return true;
      }
      return false;
    }
    /**
     * To store the context to other object
     * @param other store to
     */
    public final void copyTo(OrderBy other)
    {
      other.pool.clear();other.pool.addAll(this.pool);
    }
    /**
     * <clone>
     * To get the copy of object
     * @return the copy
     */
    public final OrderBy copy()
    {
      OrderBy copy = new OrderBy();
      copy.pool.clear();copy.pool.addAll( this.pool );
      return copy;
    }
    /**
     * <accessor>
     * To get access to text's representation of Order By clause
     * @return
     */
    public final String text()
    {
      if ( this.pool.size() == 0) return "";
      StringBuffer clause=new StringBuffer(); boolean first=true;
      for(Iterator i=this.pool.iterator();i.hasNext();)
      {
        Object item = i.next();
        if ( item != null )
        {
          if ( first ) first=false; else clause.append(", ");
          clause.append( item.toString() );
        }
      }
      return clause.toString();
    }
    private final String clause(){String text = this.text();
      return  "".equals(text) ? text:"ORDER BY "+text;
    }
    /**
     * <store>
     * To store clause to XML's format
     * @return stored clause
     */
    public final Element getXML()
    {
      Element xml = new Element(ELEMENT);
      for(Iterator i=this.pool.iterator();i.hasNext();)
      {
        Item item = (Item)i.next();
        if ( item != null ) xml.addContent(item.getXML());
      }
      return xml;
    }
    private final void init(){this.pool.clear();}
    /**
     * <restore>
     * To restore the clause from xml
     * @param xml stored clause
     * @throws Exception throws if invalid xml
     */
    public final void setXML(Element xml) throws Exception
    {
      this.pool.clear();// to clear the pool
        if (xml == null || !ELEMENT.equals(xml.getName()) ) return;
        for(Iterator i=xml.getChildren(Item.ELEMENT).iterator();i.hasNext();)
        {
          try{this.pool.add( new Item((Element)i.next()) );
          }catch(Exception e){}
        }
    }
    /**
     * <accessor>
     * To get access to items list
     * @return the list
     */
    public final ArrayList items(){return (ArrayList)this.pool.clone();}
    /**
     * <mutator>
     * To add new item
     * */
    public final Item add(Item item){
      if ( item!=null ) this.pool.add( item );
      return item;
    }
    /**
     * <mutator>
     * To update item
     * */
    public final Item update(Item item,int index){
      if ( item!=null ) this.pool.set( index, item );
      return item;
    }
    /**
     * <mutator>
     * To delete item by index
     * */
    public final void delete(int index){this.pool.remove(index);}
    /**
     * <mutator>
     * To move up the item by index
     * */
    public final void moveUp(int index){
      if (index <= 0) return;
      Object entry = this.pool.remove(index);
      if (entry != null) this.pool.add(index-1,entry);
    }
    /**
     * <mutator>
     * To move down the item by index
     * */
    public final void moveDown(int index){
      if (
          index < 0 ||
          index >= this.pool.size()-1 ||
          this.pool.size() == 1
          ) return;
      Object entry = this.pool.remove(index);
      if (entry != null) this.pool.add(index+1,entry);
    }
    /**
     * <attribute>
     * The pool of the items
     */
    private final ArrayList pool=new ArrayList();

    /**
     * Class- item of ORDER BY
     */
    public static final class Item
    {
      public final static String ELEMENT="order_item";
      private Symbol column;
      private boolean descend;
      public final String toString(){return column.getName()+(descend ? " DESC":"");}
      public Item(Symbol column,boolean descend){this.column=column.copy();this.descend=descend;}
      public Item(Symbol column){this(column,false);}
      public final Symbol getColumn(){return this.column;}
      public final boolean isDescend(){return this.descend;}
      public final void setDescend(boolean descend){this.descend=descend;}
      public final Element getXML()
      {
        Element xml = new Element(ELEMENT);
        xml.setAttribute(new Attribute("column",this.column.toString()));
        xml.setAttribute(new Attribute("descend",String.valueOf(this.descend)));
        return xml;
      }
      public Item(Element xml) throws Exception
      {
        String columnXML=null;
        if (
            xml == null ||
            !ELEMENT.equals(xml.getName()) ||
            (columnXML = xml.getAttributeValue("column")) == null
           ) throw new Exception("Invalid order by item's XML.");
        this.column = Symbol.fromString( columnXML );
        this.descend = Boolean.valueOf(xml.getAttributeValue("descend")).booleanValue();
      }
    }
  }
}
