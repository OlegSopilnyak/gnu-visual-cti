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

import org.visualcti.briquette.*;
import org.visualcti.briquette.Operation;
import org.visualcti.briquette.Subroutine;
import org.visualcti.server.database.*;
import java.util.*;
import java.sql.*;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class Basis extends Operation
{
/**
<value>
The system symbol
the name of current JDBC driver
*/
public final static Symbol system_db_Driver = Symbol.newDatabase("db.Driver", Symbol.STRING);
/**
<value>
The system symbol
the URL to current JDBC driver connection
*/
public final static Symbol system_db_URL = Symbol.newDatabase("db.URL", Symbol.STRING);
/**
<value>
The system symbol
the login of current JDBC driver connection
*/
public final static Symbol system_db_User = Symbol.newDatabase("db.User", Symbol.STRING);
/**
<value>
The system symbol
the flag, is have result after request
*/
public final static Symbol system_db_DataAccess = Symbol.newDatabase("db.DataAccess", Symbol.STRING);
/**
<value>
The system symbol
the number of updated rows in database
*/
public final static Symbol system_db_RowCount = Symbol.newDatabase("db.RowCount", Symbol.NUMBER);
/**
 * <pool>
 * The pool of predefined symbols
 */
private static final List predefined = new ArrayList();
/**
 * <init>
 * To initialize predefined Symbols
 */
static{
  Basis.predefined.add(Basis.system_db_Driver);
  Basis.predefined.add(Basis.system_db_URL);
  Basis.predefined.add(Basis.system_db_User);
  Basis.predefined.add(Basis.system_db_DataAccess);
  Basis.predefined.add(Basis.system_db_RowCount);

  // for db.DataAccess
  Basis.predefined.add(Symbol.newConst("Yes"));
  Basis.predefined.add(Symbol.newConst("No"));
}
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * It may be overrided in children
   * @return predefined symbols
   */
  public final List getPredefinedSymbols(){return predefined;}
/**
 * <attribute>
 * The context of DB connection
 * */
protected volatile Context context=null;
  /**
   * <accessor>
   * To get access to DataBase's context of the briquette
   * @return
   */
  public final Context getContext(){return this.context;}
  /**
   * <check>
   * Is db-connection needs for this Operation
   * May overrided in the child
   * */
  protected boolean isDatabase(){return true;}
/**
 * <main>
 * The main method of Data-Operations
 * */
protected abstract void dataMethod(Subroutine caller);
  /**
   * <action>
   * The main method of the Operation
   * */
  public final Operation doIt(Subroutine caller)
  {
    // to check the database's connection
    if ( this.isDatabase() && !isConnectionAlive(caller) ) return null;

    // to execute main method
    this.dataMethod(caller);

    // to clear the reference to caller's context
    this.context = null;

    // to return the reference to the next Operation
    return super.getLink(Operation.DEFAULT_LINK);
  }
  /**
   * <action>
   * To stop action's execution
   */
  public void stopExecute(){}
    /**
     * <check>
     * To check & restore the database's connection
     * @param caller the subroutine-caller
     * @return true if connection is alive
     */
    private final boolean isConnectionAlive(Subroutine caller) {
      // try to refresh the connection
      Context context = caller.getContext();
      if ( context == null )throw new ArithmeticException("Internal error, please report...");
      else
      if ( context.database() == null ) {
        try {
          org.visualcti.server.task.Environment env = caller.getProgramm().getEnv();
          // to get access to messenger's Service
          Database db = (Database)env.getPart("database",Database.class);
          // to make the connection
          context.getConnection( db );
        }catch(Exception e){
          caller.error( e.getMessage() );
          return false;
        }
      }
      // to store the caller's context
      if ( (this.context=context).database() == null ) {
        caller.error("No database's connection...");
        return false;
      }
      // to fill database's parameters
      connectionRequest request = caller.getEntity().getConnectionRequest();
      caller.set( system_db_Driver, request.getDriverClass() );
      caller.set( system_db_URL, request.getURL() );
      caller.set( system_db_User, request.getLogin() );
      caller.set( system_db_RowCount, new Integer(0) );
      caller.set( system_db_DataAccess, "No" );
      return true;
    }
}
