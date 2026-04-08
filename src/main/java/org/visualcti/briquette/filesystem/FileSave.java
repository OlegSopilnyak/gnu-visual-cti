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
 * To save the value's content to the file</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FileSave extends Basis
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
   * <attribute>
   * The content of the file
   */
private Symbol content;
/**
 * <accessor>
 * To get access to the content of file
 * @return the content
 */
public final Symbol getContent() {return content;}
/**
 * <mutator>
 * To change the content
 * @param content new content
 */
public final void setContent(Symbol content) {this.content = content;}
/**
 * <accessor>
 * To get access to local Symbols borned in this Operation
 * @return the list of briquette's local values
 */
public final List getLocalSymbols()
{
  ArrayList symbols = new ArrayList();
  if (
      this.content != null &&
      this.content.getGroupID() == Symbol.USER
      )
    symbols.add( this.content.copy() );
  return symbols;
}
/**
 * <attribute>
 * Flag, is merge the content with file's content
 */
private boolean merge;
/**
 * <accessor>
 * To get access to merge's flag
 * @return the flag's value
 */
public final boolean isMerge(){return merge;}
/**
 * <mutator>
 * To change the merge's flag
 * @param merge new flag's value
 */
public final void setMerge(boolean merge){this.merge = merge;}
/**
 * <attribute>
 * Flag, is create the directory for file
 */
private boolean force;
/**
 * <accessor>
 * To get access to force's flag
 * @return the flag's value
 */
public final boolean isForce(){return force;}
/**
 * <mutator>
 * To change the force's flag
 * @param force new flag's value
 */
public final void setForce(boolean force){this.force = force;}
  /**
   * <contructor>
   */
  public FileSave()
  {
    super.setAbout("To save the content to file");
    this.initRuntime();
  }
  private final void initRuntime(){
    this.location = Basis.system_file_Location;
    this.name = Basis.system_file_Name;
    this.content = Symbol.newLocal("file.content",Symbol.BIN);
    this.merge = false;
    this.force = true;
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
    xml.addContent(new Property("content",this.content).getXML());
    xml.addContent(new Property("merge",this.merge).getXML());
    xml.addContent(new Property("force",this.force).getXML());
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
    ArrayList names = new ArrayList(5);
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
      } else
      if ( "content".equals(name) ) {
        this.content = property.getValue(this.content);names.add(name);
      } else
      if ( "merge".equals(name) ) {
        this.merge = property.getValue(this.merge);names.add(name);
      } else
      if ( "force".equals(name) ) {
        this.force = property.getValue(this.force);names.add(name);
      }
    }
  }
  /**
   * <action>
   * To save the data from content to the file
   * @param caller the subroutine-executer of operation
   * @return the reference to next briquette
   */
  public final Operation doIt(Subroutine caller)
  {
    // to make the file name
    String dir = (String)caller.get( this.location );
    File directory = new File(dir);
    if ( !directory.exists() && this.force && !directory.mkdirs() )
    {// can't to make the not exists target directory
      caller.set(system_file_OperationResult, "FAIL");
    } else
    {// directory exists or maked successfully
      caller.set(system_file_OperationResult, "SUCCESS");
      String name = (String) caller.get(this.name);
      File target = new File(directory,name);
      switch( this.content.getTypeID() )
      {
        case Symbol.BIN:
        case Symbol.FAX:
        case Symbol.VOICE:
          // to store content as bytes array
          this.storeBinary(caller,target);
          break;
        case Symbol.STRING:
        case Symbol.NUMBER:
          // to store content as text's string
          this.storeText(caller,target);
          break;
        default:// unknown type of content
          caller.set(system_file_OperationResult, "FAIL");
          break;
      }
    }
    // to return the reference to the next Operation
    return super.getLink(Operation.DEFAULT_LINK);
  }
  private final void storeBinary(Subroutine caller, File target){
    byte[] data = (byte[])caller.get(this.content);
    if ( data == null ){
      caller.set(system_file_OperationResult, "FAIL");
      return;
    }
    try{
      FileOutputStream out = new FileOutputStream(target, this.merge);
      out.write(data); out.close();
      // to clear the references
      out = null; data = null;
    }catch(IOException e){
      caller.set(system_file_OperationResult, "FAIL");
    }
  }
  private final void storeText(Subroutine caller, File target){
    Object data = caller.get(this.content);
    if ( data == null ){
      caller.set(system_file_OperationResult, "FAIL");
      return;
    }
    try{
      FileOutputStream out = new FileOutputStream(target, this.merge);
      PrintWriter print = new PrintWriter( out, true );
      print.println(data); print.close();
      // to clear the references
      print = null; out = null; data = null;
    }catch(IOException e){
      caller.set(system_file_OperationResult, "FAIL");
    }
  }
  /**
   * <action>
   * To interrupt the execution
   * Do nothing.
   */
  public final void stopExecute() {}
}
