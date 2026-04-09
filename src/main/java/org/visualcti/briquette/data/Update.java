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

import org.jdom.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;

import java.util.*;
import java.text.*;
import java.sql.*;
import java.io.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To update the data in the DataSource</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class Update extends Basis
{
    /**
     * <accessor>
     * To get access to new ID's prefix
     * @return the prefix
     */
    public final String get_ID_prefix(){return "Update.";}
    /**
     * <constructor>
     * */
    public Update()
    {
      super.setAbout("To update the data");
      this.initRuntime();
    }
      private final void initRuntime(){
        this.table = "";
        this.set.Clear();
        this.where.clear();
      }
    /**
     * To get access to dbColumns of this briquette
     * Design mode only
     * @return the list of dbColumns
     */
    protected final List dbColumnsSymbol()
    {
      ArrayList columns=new ArrayList();String prefix=this.table+".";
      for(Iterator i=super.dbColumnsSymbol().iterator();i.hasNext();)
      {
        Symbol column = (Symbol)i.next();
        if ( column.getName().startsWith(prefix) ) columns.add(column);
      }
      return columns;
    }
/**
 * <attribute>
 * The name of updated DB's object
 */
private String table;
  /**
   * <accessor>
   * To get access to the name of DB's object
   * @return the name
   */
  final String getTable(){return this.table;}
  /**
   * <mutator>
   * To change the name of DB's object
   * @param table
   */
  final void setTable(String table){this.initRuntime(); this.table=table;}
/**
 * <attribute>
 * The set of expressions to update
 */
private final Set set = new Set();
  /**
   * <accessor>
   * To get access to set of expressions
   * @return the set of expressions
   */
  final Set getSet(){return this.set;}
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
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("table",this.table).getXML() );
      xml.addContent( this.set.getXML() );
      xml.addContent( this.where.getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.initRuntime();
      Property property = new Property( xml.getChild(Property.ELEMENT) );
      if ( !"table".equals(property.getName()) ) throw new Exception("Invalid Table part of XML.");
      this.table = property.getValue(this.table);
      this.set.setXML( xml.getChild(set.xmlRoot()) );
      this.where.setXML( xml.getChild(Logic.ELEMENT) );
    }
  /**
   * <action>
   * Main method of the briquette
   * @param caller who is call
   */
  protected void dataMethod(Subroutine caller)
  {
    if ( this.set.getSequence().size() <= 0 ) return;
    String sql = makeSQL( caller );
    try {
      caller.set( Basis.system_db_RowCount, new Integer(0) );
      int rows = this.executeUpdate( super.context.database(), sql );
      this.binaries.clear(); System.gc();
      caller.set( Basis.system_db_RowCount, new Integer(rows) );
    }catch(Exception e){}
  }
    /**
     * To execute the SQL command
     * @param sql SQL-command
     * @return the quantity of updated rows
     */
    private final int executeUpdate(Connection database, String sql){
      boolean prepared = this.binaries.size() > 0;
      try{
        if ( !prepared )
          // to execute simple "update" command
          return database.createStatement().executeUpdate(sql);
        // to execute prepared statement
        PreparedStatement stmt = database.prepareStatement(sql);
        // store to statement the binary's streams
        for(Iterator i=this.binaries.iterator();i.hasNext();){
          ByteArrayInputStream bytesStream = (ByteArrayInputStream)i.next();
          int length=0,parameterIndex=1;
          try{length=bytesStream.available();}catch(Exception e){}
          if ( length > 0 ){
            stmt.setBinaryStream(parameterIndex,bytesStream,length);
            parameterIndex++;
          }
        }
        // to execute the statement
        return stmt.executeUpdate();
      }catch(SQLException e){
      }
      return 0;
    }
/**
 * <attribute>
 * The pool of binary streams
 */
private final ArrayList binaries=new ArrayList();
/**
 * <const>
 * The format for make the SQL expression
 */
private final static MessageFormat sqlPattern = new MessageFormat("UPDATE {0} SET {1} {2}");
  /**
   * <producer>
   * To make SQL command for update
   * @param owner the caller of the briquette
   * @return the SQL command
   */
  private final String makeSQL(Subroutine owner)
  {
    this.binaries.clear();
    String set = this.set.getSQL(owner,this);
    String where = this.where.getSQL(owner);
    where = "".equals(where) ? (""):("WHERE "+where);
    return sqlPattern.format(new Object[]
    {
      this.table, set, where
    }
    );
  }
  /***
   * Class for represent the expression with the DB's column
   */
  private final class dbColumnExpression extends MathExpression
  {
    public dbColumnExpression()
    {
      super.setTarget(Symbol.newDbColumn("",Symbol.ANY));
    }
    public final java.util.List valid(java.util.List symbols)
    {
      ArrayList valid = new ArrayList();
      for(Iterator i=symbols.iterator();i.hasNext();)
      {
        Symbol symbol = (Symbol)i.next();
        if ( symbol.getGroupID() != Symbol.DBCOLUMN ) valid.add(symbol);
        else
        if ( !Update.this.set.contains(symbol) )  valid.add(symbol);
      }
      return valid;
    }
    protected final String type(){return "column";}
    public final MathExpression copy()
    {
      dbColumnExpression copy=new dbColumnExpression();
      try{copy.setXML(this.getXML());}catch(Exception e){}
      return copy;
    }
    private ByteArrayInputStream stream = null;
    private final String getSQL(Subroutine owner) throws Exception
    {
      this.stream=null;
      if ( !this.isValid() ) return "";
      StringBuffer sb = new StringBuffer();
      // to add first operand
      if (first.getGroupID() == Symbol.DBCOLUMN) sb.append(first.getName());
      else this.addSymbolSQL(first,sb,owner,"first");
      if ( "".equals( this.operation ) ) return sb.toString();
      // to add operation
      sb.append(" ").append(super.operation).append(" ");
      // to add second operand
      if (second.getGroupID() == Symbol.DBCOLUMN) sb.append(second.getName());
      else this.addSymbolSQL(second,sb,owner,"second");
      return sb.toString();
    }
    private final void addSymbolSQL(Symbol symbol,StringBuffer sb,Subroutine owner,String prefix)
                          throws Exception {
      Object value = symbol.getName();
      if ( !symbol.isConst() )
      {
        if ((value=owner.get(symbol)) == null)
          throw new NullPointerException(prefix+" operand's value is null");
      }
      // to make the expression for update
      switch(symbol.getTypeID() ){
        // string type
        case Symbol.STRING:
          sb.append("\'").append(value).append("\'");
          break;
        // binary types
        case Symbol.BIN: case Symbol.VOICE: case Symbol.FAX:
          sb.append("?");
          this.stream = new ByteArrayInputStream( (byte[])value );
          this.operation="";
          break;
        // other types
        default:
          sb.append(value);
          break;
      }
    }
    protected final int targetGroupID(){return Symbol.DBCOLUMN;}
  }
  /***
   * Class, the set of columns to update
   */
  private final class Set extends MathExpressionsSet
  {
    public String xmlRoot(){return "set_sequence";}
    public MathExpression newMathExpression(){return new dbColumnExpression();}
    private final String getSQL(Subroutine owner,Update briquette)
    {
      boolean first=true;StringBuffer sb=new StringBuffer();
      for(Iterator i=this.getSequence().iterator();i.hasNext();)
      {
        dbColumnExpression expression = (dbColumnExpression)i.next();
        if ( !expression.isValid() ) continue;
        try{
          String sql = expression.getSQL(owner);
          if ( expression.stream != null) briquette.binaries.add(expression.stream);
          if ( first ) first=false; else sb.append(", ");
          sb.append( sql );
        }catch(Exception e){}
      }
      return sb.toString();
    }
  }
}
