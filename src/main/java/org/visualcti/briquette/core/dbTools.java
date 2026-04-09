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
package org.visualcti.briquette.core;

import java.sql.*;
import java.io.*;
import java.util.*;
import org.visualcti.server.action.*;
import org.visualcti.server.action.event.unitEventListener;
import org.visualcti.server.database.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.control.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow to execute some Database features </p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class dbTools {
/**
 * <pool>
 * The pool of columns for requests
 * */
private static final Map pool = Collections.synchronizedMap( new HashMap() );
/**
 * <pool>
 * The pool of connections for requests
 * */
private static final Map dbs = Collections.synchronizedMap( new HashMap() );
/**
 * <manager>
 * The service for process JDBC features
 * */
private static final DataManager manager = new DataManager();
/**
 * <accessor>
 * To get access to Database's manager
 * @return the manager's instance
 */
  public static final Database getDatabase(){return dbTools.manager;}
  static
  {
    try{// to disable the print
      dbTools.manager.removeUnitEventListener(dbTools.manager);
      dbTools.manager.addUnitEventListener(new unitEventListener(){
        public final void handleEvent(serverAction event){}// nothing by event
      });
      dbTools.manager.Start();
    }catch(Exception e){}
  }
  /**
   * <accessor>
   * To get columns set for connectionRequest
   * */
   public static final dbMetaData getMetaData(connectionRequest key){
    dbMetaData data = (dbMetaData)pool.get( key );
    if (data == null){
      dbTools.refresh( key );// to refresh the request's information
      return getMetaData(key);// recursive call
    }
    return data;
   }
   /**
    * <mutator>
    * To update information about request
    * */
    public static final void refresh(connectionRequest request){
      dbMetaData data = new dbMetaData();
      data.request = request;
      dbTools.collect ( data );
      dbTools.pool.put( request, data );
    }
      /**
       * <collect>
       * To collect information about request
       * */
      private final static void collect(dbMetaData data){
        connectionRequest request = data.request;
        Connection db = (Connection)dbs.get(request);
        if (db == null) {// not in pool
          db=dbTools.manager.getConnection(request);
          if (db == null) return;// can't connect to database
          dbTools.dbs.put(request,db);
        }
        try {// to check connection
          if ( db.isClosed() ) db=dbTools.manager.getConnection(data.request);// reopen connection
          DatabaseMetaData meta=db.getMetaData();
          String schema = request.getSchema();
          List names = objectNames( meta, data, schema );
          ResultSet set = dbTools.objectColumnsSet(meta,schema);
          while( set.next() ) dbTools.addDbSymbol(set,names,data.columns);
          // to free the resources
          set.close(); set = null; System.gc();
          Collections.sort(data.columns);
        }catch(Exception e){
          //e.printStackTrace();
        }
      }
      /**
       * <collect>
       * To retrive the names of Database Objects (TABLE,VIEW)
       * */
      private final static List objectNames
                                    (
                                    DatabaseMetaData meta,
                                    dbMetaData data,
                                    String schema
                                    )
                                    throws Exception{
        ArrayList names = new ArrayList();
        ResultSet set=dbTools.objectNamesSet(meta,schema);
        final int TABLE_NAME = 3;
        final int TABLE_TYPE = 4;
        // to iterate the ResultSet for collect names
        while( set.next() ){
            String name =set.getString(TABLE_NAME);// The object's name
            String type =set.getString(TABLE_TYPE);// The object's type
            // to solve the object's meta-data
            if (type.toUpperCase().equals("TABLE")) {
              names.add( name.toUpperCase() );// to add the Table's name
              data.tables.add( name );
            }else
            if (type.toUpperCase().equals("VIEW")) {
              names.add( name.toUpperCase() );// to add the View's name
              data.views.add( name );
            }
        }
        Collections.sort(data.tables);
        Collections.sort(data.views);
        // to free resources
        set.close(); set=null; System.gc();
        return names;
      }
      /**
       * <collect>
       * To add valid Symbol to target
       * */
      private final static void addDbSymbol(ResultSet set,List names,List target) throws Exception{
        final int TABLE_NAME =3;
        final int COLUMN_NAME=4;
        final int DATA_TYPE  =5;
        final int COLUMN_SIZE=7;
        final int TYPE_NAME  =6;
        // to retrive the data
        String table =set.getString( TABLE_NAME);// Object's name
        if ( !names.contains(table.toUpperCase()) ) return;// wrong table
        // to solve the column
        String column=set.getString(COLUMN_NAME);// Column's name
        short  type  =set.getShort (  DATA_TYPE);// SQL type from java.sql.Types
        int    size  =set.getInt   (COLUMN_SIZE);// The width of column
        String name;// the name of future Symbol
            switch ( type )
            {
              case java.sql.Types.BIT		:
              case java.sql.Types.TINYINT 	:
              case java.sql.Types.SMALLINT	:
              case java.sql.Types.INTEGER 	:
              case java.sql.Types.BIGINT	:
              case java.sql.Types.FLOAT	        :
              case java.sql.Types.REAL 	        :
              case java.sql.Types.DOUBLE	:
              case java.sql.Types.NUMERIC 	:
              case java.sql.Types.DECIMAL	:
                    name = table+"."+column;
                    target.add(Symbol.newDbColumn(name,Symbol.NUMBER));
                    break;

              case java.sql.Types.CHAR	        :
              case java.sql.Types.VARCHAR 	:
              case java.sql.Types.LONGVARCHAR   :
              case java.sql.Types.DATE 	        :
              case java.sql.Types.TIME 	        :
              case java.sql.Types.TIMESTAMP 	:
                    name = table+"."+column;
                    target.add(Symbol.newDbColumn(name,Symbol.STRING));
                    break;

              case java.sql.Types.BINARY	:
              case java.sql.Types.VARBINARY 	:
              case java.sql.Types.LONGVARBINARY :
                      name = table+"."+column;
                      target.add(Symbol.newDbColumn(name,Symbol.BIN));
                      break;
            }
      }
      /**
       * <maker>
       * To make request to get the metadata for Tables
       * */
      private final static ResultSet objectNamesSet(DatabaseMetaData meta,String schema) throws Exception{
        schema= "".equals(schema) ? null:schema;
        String types[] = new String[]{"TABLE","VIEW"};
        try { return meta.getTables(null,schema,null,types);
        } catch (SQLException e){// database not supported the Schema
        }
        // to get data without schema
        return meta.getTables(null, null, null, types);
      }
      /**
       * <maker>
       * To make request to get the metadata for Columns
       * */
      private final static ResultSet objectColumnsSet(DatabaseMetaData meta,String schema) throws Exception {
        schema= "".equals(schema) ? null:schema;
        try {return meta.getColumns(null, schema, null, null);
        } catch (SQLException e){// database not supported the Schema
        }
        // to get data without schema
        return meta.getColumns(null, null, null, null);
      }
  /**
   * <fill>
   * To fill information from ResulSet
   * */
   public static final void fill(ResultSet set,Subroutine caller) throws Exception
   {
    List columns = caller.getContext().getColumns();
    int columnIndex = 1;
    for(Iterator i=columns.iterator();i.hasNext();)
    {
      Symbol column = (Symbol)i.next();
      dbTools.fillResult(set,columnIndex,column,caller);
      columnIndex++;
    }
   }
   /**
    * <fill>
    * To fill data from results set
    * */
  private static void fillResult(
                                ResultSet set,
                                int index,
                                Symbol column,
                                Subroutine owner
                                )
                                throws Exception
  {
            int java_sql_Type=set.getMetaData().getColumnType(index);
            switch ( java_sql_Type )
            {
	case java.sql.Types.BIT		:
	case java.sql.Types.TINYINT 	:
	case java.sql.Types.SMALLINT	:
	case java.sql.Types.INTEGER 	:
	case java.sql.Types.BIGINT	:
        	        long lDigit = set.getLong( index );
                        owner.set( column, new Long(lDigit) );
        	        break;

	case java.sql.Types.FLOAT	:
	case java.sql.Types.REAL	:
	case java.sql.Types.DOUBLE	:
	case java.sql.Types.NUMERIC 	:
	case java.sql.Types.DECIMAL	:
        	        double dDigit = set.getDouble( index );
                        owner.set( column, new Double(dDigit) );
        	        break;

	case java.sql.Types.CHAR	:
	case java.sql.Types.VARCHAR 	:
	case java.sql.Types.LONGVARCHAR :

	case java.sql.Types.DATE 	:
	case java.sql.Types.TIME 	:
	case java.sql.Types.TIMESTAMP 	:
        	        String sBody = set.getString( index );
                        if (sBody != null) owner.set( column, sBody );
        	        break;

	case java.sql.Types.BINARY		:
	case java.sql.Types.VARBINARY 	:
	case java.sql.Types.LONGVARBINARY:
	                InputStream in = set.getBinaryStream( index );
	                try {
	                    int symbol=-1;
	                    try { symbol=in.read(); } catch(IOException io){}
	                    if (symbol == -1)
                                return;
	                        //alg.SysReport("db Field loading ["+name+"] unexpected end of stream, not loaded");
	                    else
	                        downloadData(column,symbol,owner,in);
	                }catch (NullPointerException e){
	                    //alg.SysReport("db Field loading ["+name+"] inputStream is NULL, not loaded");
	                }
            }
  }
  private static void downloadData(Symbol column,int type,Subroutine owner,InputStream in){
      ByteArrayOutputStream buffer=new ByteArrayOutputStream();
      /*
      byte block[]=new byte[100000];
      int count=-1;
      cubik.CTI.Data data=new cubik.CTI.Data("BIN");
      try {
          buffer.write(type);
          while((count=in.read(block)) > 0){
              buffer.write(block,0,count);
          }
          data.setData( buffer.toByteArray() );
          in.close(); buffer=null; System.gc();
      } catch(IOException io){}
      column.setType(CubikValueItem.BIN);
      alg.setPipedValue(column,data);
      */
  }
}
