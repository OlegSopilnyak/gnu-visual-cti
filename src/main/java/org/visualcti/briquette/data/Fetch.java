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
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.control.*;
import org.visualcti.util.Property;
import java.util.*;
import java.sql.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The movement on the query to the DataSource</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class Fetch extends Basis
{
    /**
     * <constructor>
     * */
    public Fetch(){super.setAbout("To move point in db results");}
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "Fetch.";}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("direction",this.direction).getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      if (xml == null) return;
      // to clear a runtime properties
      this.direction = "next";
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 3 );
      // to iterate the properties of operation
      while( i.hasNext() )
      {
        Property property = new Property( (Element)i.next() );
        String name = property.getName();
        // check the property's name
        if (name == null) throw new Exception("Property without name!");
        if ( names.contains(name) )
          throw new Exception("Multiple definition of runtime properties!");
        // to solve the property by name
        if ( "direction".equals(name) )    {
            this.direction = property.getValue(this.direction); names.add(name);
        }
      }
    }
  /**
   * <main>
   * The main entry for Data briquettes
   * */
  protected void dataMethod(Subroutine caller)
  {
    caller.set(Basis.system_db_DataAccess,"No");
    caller.clear( Symbol.DBCOLUMN );// to remove all columns's values
    try {
      ResultSet set = super.context.getResultSet();
      // try to solve results set
      if (set == null) {super.context.closeStatement(); return;}
      // to solve the direction and retrive the data
      if ( set.next() ) {
        // data exists
        dbTools.fill( set, caller );
        caller.set(Basis.system_db_DataAccess,"Yes");
      }else{
        // to close results set and statement
        super.context.closeResultSet();
        super.context.closeStatement();
      }
    }catch(Exception e){}
  }
private final static String valid[] = new String[]{"next","prev","first","last"};
  public final String[] getValidDirections(){return valid;}
private String direction = "next";
  public String getDirection() {return direction;}
  public void setDirection(String direction) {this.direction = direction;}
}
