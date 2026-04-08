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
package org.visualcti.briquette.filesystem;

import org.visualcti.briquette.*;
import org.jdom.*;
import java.io.*;
import java.util.*;
import org.visualcti.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * To erase the file briquette</p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FileErase extends Basis
{
/**
 * <attribute>
 * The location of the file
 */
private Symbol location;
  /**
   * <accessor>
   * To get access to the location of the file
   * @return the location
   */
  public final Symbol getLocation() {return location;}
  /**
   * <mutator>
   * To change the location of the file
   * @param location new location
   */
  public final void setLocation(Symbol location) {this.location = location;}
  /**
   * <attribute>
   * The name of the file
   */
private Symbol name;
  /**
   * <accessor>
   * To get access to the name of the file
   * @return the name
   */
  public final Symbol getName() {return name;}
  /**
   * <mutator>
   * To change the name of the file
   * @param name new name's value
   */
  public final void setName(Symbol name) {this.name = name;}
/**
 * <accessor>
 * To get access to new ID's prefix
 * */
public final String get_ID_prefix(){return "FileErase.";}
  /**
   * <contructor>
   */
  public FileErase()
  {
    super.setAbout("To delete the file");
    this.initRuntime();
  }
  private final void initRuntime(){
    this.location = Basis.system_file_Location;
    this.name = Basis.system_file_Name;
  }
  /**
   * <transform>
   * to store the runtime parameters to XML's element
   * @param xml runtime parameters container
   */
  protected final void storeRuntimeProperties(Element xml)
  {
    xml.addContent(new Property("location",this.location).getXML());
    xml.addContent(new Property("name",this.name).getXML());
  }
  /**
   * <transform>
   * To restore the runtime parameters from XML
   * @param xml runtime parameters container
   * @throws Exception if can't restore
   */
  protected final void restoreRuntimeProperties(Element xml) throws java.lang.Exception
  {
    this.initRuntime();
    // to check of XML's integrity
    if (xml == null) return;
    // to make the properties's iterator
    Iterator i = xml.getChildren(Property.ELEMENT).iterator();
    ArrayList names = new ArrayList(2);
    // to iterate the properties of operation
    while (i.hasNext()) {
      Property property = new Property( (Element) i.next());
      String name = property.getName();
      // check the property's name
      if (name == null)
        throw new Exception("Property without name!");
      if (names.contains(name))
        throw new Exception("Multiple definition of runtime properties!");
      // to solve the property by name
      if ( "location".equals(name) ) {
        this.location = property.getValue(this.location);names.add(name);
      } else
      if ( "name".equals(name) ) {
        this.name = property.getValue(this.name);names.add(name);
      }
    }
  }
  /**
   * <action>
   * To erase the file
   * @param caller the subroutine-executer of operation
   * @return the reference to next briquette
   */
  public final Operation doIt(Subroutine caller)
  {
    // to make the file name
    String dir = (String)caller.get( this.location );
    String name = (String)caller.get( this.name );
    File file = new File(dir,name);
    // to check file's accessibility
    if ( file.exists() && file.isFile() )
    {// try to erase the file
      String result = file.delete() ? "SUCCESS":"FAIL";
      caller.set(system_file_OperationResult, result);
    }else
    { // the file is not accessible or not a file
      caller.set(system_file_OperationResult, "FAIL");
    }
    // to return the reference to the next Operation
    return super.getLink(Operation.DEFAULT_LINK);
  }
  /**
   * <action>
   * To interrupt the execution
   * Do nothing.
   */
  public final void stopExecute() {}
}
