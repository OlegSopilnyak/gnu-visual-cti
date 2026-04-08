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
import java.io.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To delete the data from the DataSource</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Delete extends Basis
{
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "Delete.";}
    /**
     * <constructor>
     * */
    public Delete(){
      super.setAbout("To delete data from DataSource");
      this.initRuntime();
    }
      private final void initRuntime(){
        this.table = ""; this.where.clear();
        // to disable any deletions, by default
        LogicalExpression FALSE = new LogicalExpression();
        FALSE.setFirst(Symbol.newConst("ONE"));
        FALSE.setOperation("=");
        FALSE.setSecond(Symbol.newConst("TWO"));
        this.where.addPart( new Logic.Part( FALSE ) );
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
      this.where.setXML( xml.getChild(Logic.ELEMENT) );
    }
  /**
   * <action>
   * Main method of the briquette
   * @param caller who is call
   */
  protected void dataMethod(Subroutine caller)
  {
    String sql = makeSQL( caller );
    try {
      caller.set( Basis.system_db_RowCount, new Integer(0) );
      int rows = this.executeUpdate( super.context.database(), sql );
      caller.set( Basis.system_db_RowCount, new Integer(rows) );
    }catch(Exception e){}
  }
    /**
     * To execute the SQL command
     * @param sql SQL-command
     * @return the quantity of updated rows
     */
    private final int executeUpdate(Connection database, String sql){
      try{
        // to execute simple "delete" command
        return database.createStatement().executeUpdate(sql);
      }catch(SQLException e){
      }
      return 0;
    }
/**
 * <const>
 * The format for make the SQL expression
 * DELETE FROM Salespeople  WHERE snum = 1003;
 * {0} the table
 * {1} "where" clause
 */
private final static MessageFormat sqlPattern = new MessageFormat("DELETE FROM {0} {1}");
  /**
   * <producer>
   * To make SQL command for update
   * @param owner the caller of the briquette
   * @return the SQL command
   */
  private final String makeSQL(Subroutine owner)
  {
    String where = this.where.getSQL(owner);
    where = "".equals(where) ? (""):("WHERE "+where);
    return sqlPattern.format(new Object[]
    {
      this.table, where
    }
    );
  }
}
