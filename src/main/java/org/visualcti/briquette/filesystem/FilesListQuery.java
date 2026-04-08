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
import org.visualcti.briquette.core.*;
import org.jdom.*;
import java.io.*;
import java.util.*;
import org.visualcti.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Briquettes,<br>
 * To make query to the file system, for get the files list in directory</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FilesListQuery extends Basis {
/**
 * <const>
 * List must be not sorted
 */
public static final int NO_ORDER = 0;
/**
 * <const>
 * List must be sorted by file's name
 */
public static final int NAME_ORDER = 1;
/**
 * <const>
 * List must be sorted by file's modify time
 */
public static final int TIME_ORDER = 2;
/**
 * <const>
 * List must be sorted by file's name (revers)
 */
public static final int REVERS_NAME_ORDER = 3;
/**
 * <const>
 * List must be sorted by file's modify time (revers)
 */
public static final int REVERS_TIME_ORDER = 4;
/**
 * <const>
 * String representation of sort modes
 */
public static final String sortOrder[] =
  new String[]{"<none>","by name ASC","by time ASC","by name DESC","by time DESC",};
    /**
     * <accessor>
     * To get access to new ID's prefix
     * @return the prefix
     */
    public final String get_ID_prefix(){return "FilesQuery.";}
    /**
     * <constructor>
     * */
    public FilesListQuery()
    {
      super.setAbout("To request the files list");
      this.initRuntime();
    }
      private final void initRuntime(){
        this.location = Symbol.newConst("./");
        try{this.filter.setXML( null );}catch(Exception e){}
        this.order = NO_ORDER;
      }

  /**
   * <transform>
   * to store the runtime parameters to XML's element
   * @param xml runtime parameters container
   */
  protected final void storeRuntimeProperties(Element xml)
  {
    xml.addContent(new Property("location",this.location).getXML() );
    xml.addContent(new Property("order",sortOrder[this.order]).getXML() );
    xml.addContent( this.filter.getXML() );
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
      if ("location".equals(name)) {
        this.location = property.getValue(this.location);
        names.add(name);
      }
      else
      if ("order".equals(name)) {
        String orderName = property.getValue(sortOrder[0]);
        for(int j=0;j < sortOrder.length;j++) {
          if ( sortOrder[j].equals(orderName) ) {this.order=j;break;}
        }
      }
    }
    // to restore the filter
    this.filter.setXML(xml.getChild(Logic.ELEMENT));
  }
  /**
   * <action>
   * To make the list & store the result
   * @param caller the subroutine-executer of operation
   * @return the reference to next briquette
   */
  public final Operation doIt(final Subroutine caller)
  {
    // to clear all
    caller.set(system_file_OperationResult,null);
    caller.getCommon().remove(Basis.COMMON);
    Basis.clearFile( caller );

    // to make the location's name
    String dir = ".";
    try{dir=caller.get(this.location).toString();}catch(Exception e){}
    final String directory=dir;
    /**
     * <filter>
     * To make the filter for files list
     */
    final FileFilter rule = new FileFilter(){
      public final boolean accept(File file){
        Basis.fillFile(caller,directory,file);
        try{return filter.calculate(caller);
        }catch(Exception e){}
        return false;
      }
    };

    // request to the FileSystem
    File []list = new File(directory).listFiles(rule);
    // to clear file's information from caller (after rule's calls)
    Basis.clearFile(caller);

    if ( list.length <= 0 ){
      // no files found
      caller.set(system_file_OperationResult,"FAIL");
    } else {
      // files exists
      caller.set(system_file_OperationResult,"SUCCESS");
      Basis.FilesList wrapper = new Basis.FilesList();
      wrapper.index=0; wrapper.files = this.sorted( list );
      caller.getCommon().put(Basis.COMMON,wrapper);
      // to make accessible first file
      wrapper.firstFile(caller);
    }
    // to return the reference to the next Operation
    return super.getLink(Operation.DEFAULT_LINK);
  }
    /**
     * <producer>
     * To make a sorted list of files
     * @param list source list
     * @return sorted list
     */
    private final File[] sorted(File[]list){
      ArrayList dirs = new ArrayList();
      ArrayList files= new ArrayList();
      for(int i=0;i < list.length;i++){File file=list[i];
        if ( file.isDirectory() ) dirs.add(file); else files.add(file);
      }
      if ( this.order != NO_ORDER ) {
        fileCompare comparator = new fileCompare(this.order);
        Collections.sort(dirs,comparator);
        Collections.sort(files,comparator);
      }
      // to merge the directories & files
      ArrayList sortedList = new ArrayList(dirs);sortedList.addAll(files);
      return (File[])sortedList.toArray(new File[0]);
    }
    /**
     * <comparator>
     * The class to compare files by ORDER
     */
    private final static class fileCompare implements Comparator{
      private final int ID;
      fileCompare(int orderID){this.ID=orderID;}
      public final int compare(Object o1, Object o2){
        File f1 = (File)o1, f2 = (File)o2;
        switch(this.ID){
          case NAME_ORDER:{
            return f1.getName().compareTo(f2.getName());
          }
          case TIME_ORDER:{
            long l1 = f1.lastModified(),l2 = f2.lastModified();
            return (l1 < l2 ? -1 : (l1==l2 ? 0 : 1));
          }
          case REVERS_NAME_ORDER:{
            return -f1.getName().compareTo(f2.getName());
          }
          case REVERS_TIME_ORDER:{
            long l1 = f1.lastModified(),l2 = f2.lastModified();
            return -(l1 < l2 ? -1 : (l1==l2 ? 0 : 1));
          }
        }
        // not needs the swap
        return 0;
      }
    }
  /**
   * <action>
   * To interrupt the execution
   * Do nothing.
   */
  public final void stopExecute() {}
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
 * <attribute>
 * The directory to scan
 */
private Symbol location;
  /**
   * <mutator>
   * To setup new directory to scan
   * @param location new directory
   */
  public final void setLocation(Symbol location) {this.location = location;}
  /**
   * <accessor>
   * To get access to directory to scan
   * @return the directory
   */
  public final Symbol getLocation() {return location;}
/**
 * <attribute>
 * The filter of query
 */
private final Logic filter = new Logic();
  /**
   * <accessor>
   * To get access to where's clause
   * @return query's filter
   */
  public final Logic getFilter(){return this.filter;}
/**
 * <attribute>
 * The ID of files list sorting type
 */
private int order;
  /**
   * <mutator>
   * To change the order's ID
   * @param order new ID
   */
  public final void setOrder(int order)
  {
    if (order >= 0 && order < this.sortOrder.length) this.order = order;
  }
  /**
   * <accessor>
   * To get access to order's ID
   * @return the ID
   */
  public final int getOrder() {return order;}
}
