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
package org.visualcti.server.database;
import java.util.*;

import org.jdom.*;
import org.visualcti.util.Property;

/**
Class for represent properties of request to connection
*/
public final class connectionRequest
{
  {
    this.initAttributes();
  }
/**
 * To calculate the hash code for this object
 * @return
 */
public final int hashCode()
{
  StringBuffer sb=new StringBuffer(this.driverClass).append(this.URL);
  sb.append(this.schema).append(this.login).append(this.password).append(this.autoCommit);
  return sb.toString().hashCode();
}
/**
<attribute>
The name of class-driver for
making the connection to database
*/
private volatile String driverClass;
/**
<accessor>
To get access to class name
*/
public final String getDriverClass(){return this.driverClass;}
/**
<attribute>
The URL to database instance
*/
private volatile String URL;
/**
<accessor>
to get access to database's URL
*/
public final String getURL(){return this.URL;}
/**
 * <attribute>
 * Connection's Schema for filter the Database's objects
 * */
private volatile String schema;
/**
 * <accessor>
 * To get access to the schema
 * */
public final String getSchema(){return this.schema;}
/**
<attribute>
Login name for driver
*/
private volatile String login;
/**
<accessor>
to get access to Login for driver
*/
public final String getLogin(){return this.login;}
/**
<attribute>
Login password for driver
*/
private volatile String password;
/**
<accessor>
to get access to password for login
*/
public final String getPassword(){return this.password;}
/**
 * <attribute>
 * Flag, is connection must by switched to autoCommit mode
 */
private volatile boolean autoCommit;
/**
 * <accessor>
 * To get access to autocommit flag
 * @return
 */
public final boolean isAutoCommit() {return autoCommit;}
/**
 * <const>
 * The name of XML's element
 * */
public final static String ELEMENT = "database";
/**
<transform>
To represent request as XML element
*/
public final Element getXML()
{
    Element xml=new Element(ELEMENT);
    xml.addContent(new Comment("Request to database connection"));
    xml.addContent( new Property("class",this.driverClass).getXML() );
    xml.addContent( new Property("url",this.URL).getXML() );
    xml.addContent( new Property("schema",this.schema).getXML() );
    xml.addContent( new Property("login",this.login).getXML() );
    xml.addContent( new Property("password",this.password).getXML() );
    xml.addContent( new Property("autoCommit",this.autoCommit).getXML() );
    return xml;
}
    private final void initAttributes(){
      driverClass="sun.jdbc.odbc.JdbcOdbcDriver";
      URL="jdbc:odbc:DSN";
      schema = "Admin"; login="Admin";password="admin";
      autoCommit=true;
    }
/**
<transform>
To update request from XML element
*/
public final void setXML(Element xml) throws Exception
{
    this.initAttributes();
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    // to iterate the properties
    ArrayList names = new ArrayList( 6 );// for check a duplicates
    for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();)
    {
        Property property = new Property( (Element)i.next() );
        String name = property.getName();
        if (name == null) throw new Exception("Property without name!");
        if ( names.contains(name) ) throw new Exception("Multiple definition of base properties!");
        // to solve the names of property
        if ( "class".equals( name ) ) {
            this.driverClass = property.getValue( this.driverClass );names.add(name);
        }else
        if ( "url".equals( name ) ) {
            this.URL = property.getValue( this.URL );names.add(name);
        }else
        if ( "schema".equals( name ) ) {
            this.schema = property.getValue( this.schema );names.add(name);
        }else
        if ( "login".equals( name ) ) {
            this.login = property.getValue( this.login );names.add(name);
        }else
        if ( "password".equals( name ) ) {
            this.password = property.getValue( this.password );names.add(name);
        }else
        if ( "autoCommit".equals( name ) ) {
            this.autoCommit = property.getValue( this.autoCommit );names.add(name);
        }
    }
}
/**
<equals>
*/
public final boolean equals(Object o)
{
    if (o == this) return true;
    try {return this.equals( (connectionRequest)o );
    }catch(ClassCastException e){
    }catch(NullPointerException e){
    }
    return false;
}
public final boolean equals(connectionRequest request)
{
        return
            this.driverClass.equals(request.driverClass) &&
            this.URL.equals(request.URL) &&
            this.login.equals(request.login) &&
            this.password.equals(request.password)&&
            this.autoCommit==request.autoCommit;
}
/**
<to string>
*/
public final String toString()
{
    return "request to DB connection {"+
           "driver="+this.driverClass+", "+
           "URL="+this.URL+", "+
           "user="+this.login+", password=******"+
           ", autoCommit="+this.autoCommit+"}";
}
// mutators
  public final void setURL(String URL) {this.URL = URL;}
  public final void setSchema(String schema) {this.schema = schema;}
  public final void setPassword(String password) {this.password = password;}
  public final void setLogin(String login) {this.login = login;}
  public final void setDriverClass(String driverClass) {this.driverClass = driverClass;}
  public final void setAutoCommit(boolean autoCommit) {this.autoCommit = autoCommit;}
}
