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
 * The briquette to move the file to new location</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FileCopy extends Basis
{
/**
 * <attribute>
 * The location (directory) of source file
 */
private Symbol sourceLocation;
/**
 * <accessor>
 * To get access to source location value
 * @return the value
 */
public final Symbol getSourceLocation(){return sourceLocation;}
/**
 * <mutator>
 * To change the value of source location
 * @param sourceLocation new value
 */
public final void setSourceLocation(Symbol sourceLocation){this.sourceLocation = sourceLocation;}
/**
 * <attribute>
 * The name of source file
 */
private Symbol sourceName;
/**
 * <accessor>
 * To get access to source name
 * @return the value
 */
public final Symbol getSourceName(){return sourceName;}
/**
 * <mutator>
 * To change the value of source name
 * @param sourceName new value
 */
public final void setSourceName(Symbol sourceName){this.sourceName = sourceName;}
/**
 * <attribute>
 * Flag, is keep the source after the operation
 */
private boolean keepSource;
/**
 * <accessor>
 * To get access to keep's flag
 * @return flag's value
 */
public final boolean isKeepSource(){return keepSource;}
/**
 * <mutator>
 * To change value of keep's flag
 * @param keepSource new flag's value
 */
public final void setKeepSource(boolean keepSource){this.keepSource = keepSource;}
/**
 * <attribute>
 * The location (directory) of target file
 */
private Symbol targetLocation;
/**
 * <accessor>
 * To get access to target location
 * @return the value
 */
public final Symbol getTargetLocation(){return targetLocation;}
/**
 * <mutator>
 * To change the value of target location
 * @param targetLocation new value
 */
public final void setTargetLocation(Symbol targetLocation){this.targetLocation = targetLocation;}
/**
 * <attribute>
 * The name of target file
 */
private Symbol targetName;
/**
 * <accessor>
 * To get access to target name
 * @return the value
 */
public final Symbol getTargetName(){return targetName;}
/**
 * <mutator>
 * To change the value of target name
 * @param targetName new value
 */
public final void setTargetName(Symbol targetName){this.targetName = targetName;}
/**
 * <attribute>
 * Flag, is create the traget's directory
 */
private boolean forceTarget;
/**
 * <accessor>
 * To get access to force's fla
 * @return flag's value
 */
public final boolean isForceTarget(){return forceTarget;}
/**
 * <mutator>
 * To change the force's flag
 * @param forceTarget new flag's value
 */
public final void setForceTarget(boolean forceTarget){this.forceTarget = forceTarget;}
/**
 * <accessor>
 * To get access to new ID's prefix
 * */
public final String get_ID_prefix(){return "FileCopy.";}
/**
 * <constructor>
 */
  public FileCopy()
  {
    super.setAbout("To move the file to new location");
    this.initRuntime();
  }
  private final void initRuntime(){
    this.sourceLocation = Basis.system_file_Location;
    this.sourceName = Basis.system_file_Name;
    this.keepSource = false;
    this.targetLocation = Symbol.newConst("./");
    this.targetName = Basis.system_file_Name;
    this.forceTarget = true;
  }
  /**
   * <transform>
   * to store the runtime parameters to XML's element
   * @param xml runtime parameters container
   */
  protected final void storeRuntimeProperties(Element xml)
  {
    xml.addContent(new Property("source.location",this.sourceLocation).getXML());
    xml.addContent(new Property("source.name",this.sourceName).getXML());
    xml.addContent(new Property("keep",this.keepSource).getXML());
    xml.addContent(new Property("target.location",this.targetLocation).getXML());
    xml.addContent(new Property("target.name",this.targetName).getXML());
    xml.addContent(new Property("force",this.forceTarget).getXML());
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
    if (xml == null)
      return;
    // to make the properties's iterator
    Iterator i = xml.getChildren(Property.ELEMENT).iterator();
    ArrayList names = new ArrayList(6);
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
      if ("source.location".equals(name)) {
        this.sourceLocation = property.getValue(this.sourceLocation);
        names.add(name);
      }
      else
      if ("source.name".equals(name)) {
        this.sourceName = property.getValue(this.sourceName);
        names.add(name);
      }
      else
      if ("keep".equals(name)) {
        this.keepSource = property.getValue(this.keepSource);
        names.add(name);
      }
      else
      if ("target.location".equals(name)) {
        this.targetLocation = property.getValue(this.targetLocation);
        names.add(name);
      }
      else
      if ("target.name".equals(name)) {
        this.targetName = property.getValue(this.targetName);
        names.add(name);
      }
      else
      if ("force".equals(name)) {
        this.forceTarget = property.getValue(this.forceTarget);
        names.add(name);
      }
    }
  }
  /**
   * <action>
   * To move the file to new location
   * @param caller the subroutine-executer of operation
   * @return the reference to next briquette
   */
  public final Operation doIt(Subroutine caller)
  {
    File source,target;
    if ( (source=this.sourceFile(caller)) == null ) {
      caller.set(system_file_OperationResult, "FAIL");
    }else
    if ( (target=this.targetFile(caller)) == null)
    {
      caller.set(system_file_OperationResult, "FAIL");
    }else
    if ( source.equals(target) )
    {
      caller.set(system_file_OperationResult, "FAIL");
    }else
    {
      caller.set(system_file_OperationResult, "SUCCESS");
      this.moveFile( source, target, caller);
    }
    // to return the reference to the next Operation
    return super.getLink(Operation.DEFAULT_LINK);
  }
  private final File sourceFile(Subroutine caller){
    String dir = (String)caller.get(this.sourceLocation);
    String name = (String)caller.get(this.sourceName);
    if ( dir == null || name == null ) return null;
    File file = new File(dir,name);
    return file.exists() ? file:null;
  }
  private final File targetFile(Subroutine caller){
    String dir = (String)caller.get(this.targetLocation);
    String name = (String)caller.get(this.targetName);
    if ( dir == null || name == null ) return null;
    File directory = new File(dir);
    if ( !directory.exists() && this.forceTarget && !directory.mkdirs() )
      return null; else  return new File(directory,name);
  }
  private final void moveFile(File source,File target,Subroutine caller){
    byte[] buffer = new byte[ Short.MAX_VALUE+1 ];
    try{
      FileInputStream src = new FileInputStream(source);
      FileOutputStream trg = new FileOutputStream(target);
      int counter = 0; this.cancel = false;
      while( (counter = src.read(buffer)) > 0 ) {
        if ( this.cancel ) break;
        trg.write(buffer, 0, counter);
      }
      // to free the resources
      src.close(); trg.close(); buffer=null; src=null; trg=null;
      // to delete the file
      if ( this.cancel ) target.delete();
      else if ( !this.keepSource ) source.delete();
    }catch(IOException e){
      caller.set(system_file_OperationResult, "FAIL");
    }
  }
/**
 * <attribute>
 * Stop execution flag
 */
private volatile boolean cancel;
  /**
   * <action>
   * To interrupt the execution
   */
  public final void stopExecute() {this.cancel=true;}
}
