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
 * To load the data from the file user's value</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class FileLoad extends Basis
{
public static final int TEXT  = 0;
public static final int MEDIA = 1;
/**
 * <const>
 * The set of file types
 */
public final static String Type[] = {"Text","Media"};
/**
 * <attribute>
 * The type of file to load
 */
private String type;
/**
 * <accessor>
 * To get acces to file's type
 * @return the type
 */
  public final String getType() {return type;}
  /**
   * <mutator>
   * To change the file's type
   * @param type new type
   */
  public final void setType(String type)
  {
    boolean valid = false;
    for(int i=0;i < Type.length;i++) if ( Type[i].equals(type) ) {valid=true;break;}
    if(valid) this.type = type;
  }
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
   * @param name
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
   */
  public final List getLocalSymbols()
  {
    ArrayList symbols = new ArrayList();
    if (
        this.content != null &&
        this.content.getGroupID() == Symbol.USER
        )
      symbols.add(this.content.copy());
    return symbols;
  }
/**
 * <accessor>
 * To get access to new ID's prefix
 * */
public final String get_ID_prefix(){return "FileLoad.";}
/**
 * <constructor>
 */
  public FileLoad() {
    super.setAbout("To load file's content");
    this.initRuntime();
  }
  private final void initRuntime(){
    this.type = Type[0];
    this.location = Basis.system_file_Location;
    this.name = Basis.system_file_Name;
    this.content = Symbol.newLocal("file.content",Symbol.BIN);
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
    xml.addContent(new Property("type",this.type).getXML());
    xml.addContent(new Property("content",this.content).getXML());
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
    ArrayList names = new ArrayList(4);
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
      if ( "type".equals(name) ) {
        String typeName = property.getValue(Type[0]);
        for(int j=0;j < Type.length;j++) {
          if ( Type[j].equals(typeName) ) {this.type=typeName;break;}
        }
        names.add(name);
      }else
      if ( "content".equals(name) ) {
        this.content = property.getValue(this.content);names.add(name);
      }
    }
  }
  /**
   * <action>
   * To load the data from the file and store it in content
   * @param caller the subroutine-executer of operation
   * @return the reference to next briquette
   */
  public final Operation doIt(Subroutine caller) {
    // to clear all
    caller.set( this.content, null );
    // to make the file name
    String dir = (String)caller.get( this.location );
    String name = (String)caller.get( this.name );
    File source = new File(dir,name);
    // to load the file if it exists
    if ( source.exists() && source.isFile() )
    {
      caller.set(system_file_OperationResult, "SUCCESS");
      // to make the ID of the type
      switch( Arrays.asList(Type).indexOf(this.type) )
      {
        case TEXT:// text type
          this.textLoad(caller,source);
          break;
        case MEDIA:// media type
          this.mediaLoad(caller,source);
          break;
        default:// unknown type
          caller.set(system_file_OperationResult, "FAIL");
          break;
      }
    }else
    { // the file is not accessible or not a file
      caller.set(system_file_OperationResult, "FAIL");
    }
    // to return the reference to the next Operation
    return super.getLink(Operation.DEFAULT_LINK);
  }
  /**
   * <action>
   * To load the file's content as text
   * Only first line of the file will be loaded
   * @param caller the subroutine owner
   * @param source the file
   */
   private final void textLoad(Subroutine caller,File source){
     try{
       BufferedReader reader = new BufferedReader(new FileReader(source));
       String line = reader.readLine(); reader.close();
       // to store the line
       caller.set(this.content, line);
       // to clear the references
       reader = null; line = null;
     }catch(java.io.IOException e){
       caller.set(system_file_OperationResult, "FAIL");
     }
   }
  /**
   * <action>
   * To load the file's content as media's container
   * @param caller the subroutine owner
   * @param source the file
   */
   private final void mediaLoad(Subroutine caller,File source){
     byte buffer[] = new byte[ (int)source.length() ];
     try {
       FileInputStream in = new FileInputStream(source);
       in.read(buffer); in.close(); in=null;
     }catch(java.io.IOException e){
       caller.set(system_file_OperationResult, "FAIL");
       return;
     }
     Symbol content = this.content.copy();
     // to store the content of the file with content type
     switch( MediaData.getTypeByHeader(buffer) ){
       case MediaData.Type.FAX:
         content.setType(Symbol.FAX);break;
       case MediaData.Type.SOUND:
         content.setType(Symbol.VOICE);break;
       default:
         content.setType(Symbol.BIN);break;
     }
     // to store the image of the file
     caller.set( content, buffer );
     // to clear the local references
     buffer = null; content=null;
   }
  /**
   * <action>
   * To interrupt the execution
   * Do nothing.
   */
  public final void stopExecute() {}
}
