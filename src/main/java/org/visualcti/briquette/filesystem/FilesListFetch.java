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
package org.visualcti.briquette.filesystem;

import org.visualcti.briquette.*;
import org.jdom.*;
import java.io.*;
import java.util.*;
import org.visualcti.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * To move the point in files list</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FilesListFetch extends Basis
{
/**
 * <const>
 * The fetch's mode
 * To move pointer to the next file in list
 */
public static final int NEXT = 0;
/**
 * <const>
 * The fetch's mode
 * To move pointer to the prev file in list
 */
public static final int PREV = 1;
/**
 * <const>
 * The fetch's mode
 * To move pointer to the first file in list
 */
public static final int FIRST = 2;
/**
 * <const>
 * The fetch's mode
 * To move pointer to the last file in list
 */
public static final int LAST = 3;
/**
 * <const>
 * The text's representation of move's directions
 */
public static final String[] Direction = {"Next","Prev","First","Last"};
/**
 * <attribute>
 * The value of moving ID
 */
private int direction;
  /**
   * <accessor>
   * To get access to direction's ID
   * @return the ID
   */
  public final int getDirection() {return direction;}
  /**
   * <mutator>
   * To change the direction's ID
   * @param direction new ID
   */
  public final void setDirection(int direction)
  {
    try {
      String name = Direction[ direction ];
    }catch(ArrayIndexOutOfBoundsException e){
      return;
    }
    this.direction = direction;
  }

    /**
     * <accessor>
     * To get access to new ID's prefix
     * @return the prefix
     */
    public final String get_ID_prefix(){return "FilesFetch.";}
/**
 * <constructor>
 */
  public FilesListFetch()
  {
      super.setAbout("To move pointer in the files list");
      this.initRuntime();
  }
  private final void initRuntime(){this.direction=NEXT;}

  /**
   * <transform>
   * to store the runtime parameters to XML's element
   * @param xml runtime parameters container
   */
  protected final void storeRuntimeProperties(Element xml)
  {
    xml.addContent(new Property("direction",Direction[this.direction]).getXML() );
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
    ArrayList names = new ArrayList(1);
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
      if ("direction".equals(name)) {
        String directionName = property.getValue(Direction[0]);
        for(int j=0;j < Direction.length;j++) {
          if ( Direction[j].equals(directionName) ) {this.direction=j;break;}
        }
      }
    }
  }
  /**
   * <action>
   * To move the point in files list
   * @param caller the subroutine-executer of operation
   * @return the reference to next briquette
   */
  public final Operation doIt(Subroutine caller) {
    // to clear all
    caller.set(system_file_OperationResult,null);
    Basis.clearFile( caller );
    // to check the files list wrapper accessibility
    Basis.FilesList wrapper = (Basis.FilesList)caller.getCommon().get(Basis.COMMON);
    if ( wrapper != null )
    {// wrapper exists
      boolean success = true;
      switch( this.direction )
      {
        case NEXT:
          success = wrapper.nextFile(caller);
          break;
        case PREV:
          success = wrapper.prevFile(caller);
          break;
        case FIRST:
          success = wrapper.firstFile(caller);
          break;
        case LAST:
          success = wrapper.lastFile(caller);
          break;
      }
      caller.set(system_file_OperationResult, success?"SUCCESS":"FAIL");
    }else
    {// no maked wrapper
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
