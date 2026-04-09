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
package org.visualcti.briquette;

import org.visualcti.server.database.connectionRequest;
import org.visualcti.server.database.Database;
import java.sql.*;
import java.util.*;

import org.jdom.*;
/**
Context of not local subroutine
*/
public final class Context
{
    /**
    <constructor>
    */
    public Context(connectionRequest request)
    {
        this.request = request;
    }
/**
 * <attribute>
 * The list of requested column's Symbols
 * */
private final ArrayList columns = new ArrayList();
  /**
   * <accessor>
   * To get access to columns List
   * */
  public final List getColumns(){return this.columns;}
/**
<attribute>
The database connection description
*/
private final connectionRequest request;
/**
<attribbute>
The reference to database connection
*/
private Connection connect=null;
    /**
     * <accessor>
     * To get access to database's connection
     * */
    public final Connection database(){return this.connect;}
    /**
    <accessor>
    get Connection by context
    */
    public final Connection getConnection( Database service )
    {
        if (this.connect == null)
        {
            this.connect = service.getConnection( this.request );
        }
        return this.connect;
    }
/**
<attribute>
reference to opened Statement
*/
private Statement stmt = null;
    /**
    <accessor>
    to get access to Statement
    */
    public final Statement getStatement(){return this.stmt;}
    /**
    <mutator>
    to save the Statement value
    */
    public final void setStatement(Statement stmt){this.stmt=stmt;}
/**
<attribute>
reference to opened ResultSet
*/
private ResultSet result = null;
    /**
    <accessor>
    to get access to ResultSet
    */
    public final ResultSet getResultSet(){return this.result;}
    /**
    <mutator>
    to save the ResultSet value
    */
    public final void setResultSet(ResultSet result){
      //To store results set
      this.result=result;
      // to save the Columns from results set
    }
    /**
    <cleaner>
    to close all resoureces
    */
    public final void close()
    {
        this.closeResultSet();
        this.closeStatement();
        this.closeConnection();
    }
    /**
    <cleaner>
    to close Connection
    */
    public final void closeConnection()
    {
        if (this.connect == null) return;
        try {this.connect.close();
        }catch(Exception e){}
        this.connect = null;
    }
    /**
    <cleaner>
    to close Statement
    */
    public final void closeStatement()
    {
        if (this.stmt == null) return;
        try {this.stmt.close();
        }catch(Exception e){}
        this.stmt = null;
    }
    /**
    <cleaner>
    to close result set
    */
    public final void closeResultSet()
    {
        if (this.result == null) return;
        try {this.result.close();
        }catch(Exception e){}
        this.result = null;
    }
}
