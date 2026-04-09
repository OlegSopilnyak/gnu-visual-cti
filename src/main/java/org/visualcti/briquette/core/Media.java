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
package org.visualcti.briquette.core;

import org.jdom.*;
import java.io.*;
import java.util.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.control.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow class parent of all stream-based media</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class Media {
/**
 * <accessor>
 * To get access to content's Symbol by default
 * */
protected abstract Symbol defaultSymbol(int type);
/**
 * <accessor>
 * To get access to the name of group (voice/fax/video)
 * ;)
 * */
protected abstract String getMediaGroup();
/**
 * <accessor>
 * to get access to RAW type ID
 * */
protected abstract int getRawTypeID();
/**
 * <const>
 * The name of XML's element
 * */
public static final String ELEMENT = "media";
/**
 * <const>
 * The value of direction's XML-attribute
 * */
public final static String DIRECTION = "direction";
  /**
   * <translator>
   * To translate the media to XML's Element
   * */
  public Element getXML()
  {
    Element xml = new Element(ELEMENT);
    xml.setAttribute( new Attribute("group",this.getMediaGroup()) );
    String type = (String)this.getTypes().get( this.type );
    xml.addContent( new Property("type",type).getXML() );
    xml.addContent( new Property("content",this.content).getXML() );
    return xml;
  }
  /**
   * <tarnslator>
   * To restore the media from XML's element
   * */
  public void setXML(Element xml) throws Exception
  {
    this.type = RAW; this.content = this.defaultSymbol(RAW);
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    String group = xml.getAttributeValue("group");
    if (group == null || !group.equals(this.getMediaGroup()) ) return;
    String type = Types[ 0 ];
    // to make the properties's iterator
    Iterator i=xml.getChildren(Property.ELEMENT).iterator();
    ArrayList names = new ArrayList( 2 );
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
        if ( "type".equals(name) )    {
            type = property.getValue(type); names.add(name);
            this.setType( type );
        }else
        if ( "content".equals(name)){
            this.content = property.getValue(this.content); names.add(name);
        }
    }
  }
/**
 * <const>
 * Type of media source.
 * The content referenced to raw data (in memory)
 * */
public final static int RAW = 0;
/**
 * <const>
 * Type of media source
 * The content referenced to file name
 * */
public final static int FILE = 1;
/**
 * <const>
 * The names of source's types
 * */
public final static String[] Types = new String[]{"RAW","FILE"};
/**
 * <attribute>
 * The type of media-source
 * */
private int type = RAW;
  /**
   * <accessor>
   * To get list of valid type's names
   * */
  public List getTypes(){return Arrays.asList(Types);}
  /**
   * <accessor>
   * To get access to source's type
   * */
  public final int getType(){return this.type;}
  /**
   * <mutator>
   * To set the type of source
   * */
  public final void setType(int type)
  {
    if (type >= 0 && type < this.getTypes().size())
    {
      this.content = this.defaultSymbol(this.type=type);
    }
  }
  /**
   * <mutator>
   * To set the type of source
   * */
  public final void setType(String type)
  {
    // try to find out the index of type
    int index = this.getTypes().indexOf( type );
    // to assign the new type
    if (index >= 0)
    {
      this.content = this.defaultSymbol(this.type=index);
    }
  }
/**
 * <attribute>
 * The symbol for references to content
 * */
private Symbol content = this.defaultSymbol(RAW);
  /**
   * <accessor>
   * To get access to content's Symbol
   * */
  public final Symbol getContent()
  {
    return this.content;
  }
  /**
   * <mutator>
   * To setup the content's Symbol
   * */
  public final void setContent(Symbol content)
  {
    this.content = content;
  }
  /**
   * <accessor>
   * To get access to content's Value or throw IOException, if Value is null
   * */
  protected final Object getContentValue(Subroutine caller) throws IOException {
    if ( this.content == null) throw new IOException("Media's content Symbol is null");
    Object value = caller.get( this.content );
    if ( value == null )
    {
      StringBuffer sb = new StringBuffer("No data in Subroutine's pool for ");
      throw new IOException( sb.append(content.cell()).toString() );
    }
    return value;
  }
  /**
   * <translate>
   * To represent media as String
   * */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    Object protocol = this.getTypes().get( this.type );
    sb.append(protocol).append(":");
    if (this.type == FILE && this.content.isConst())
      sb.append( new File(this.content.getName()).getAbsolutePath() );
    else
      sb.append( this.content.cell() );
    return sb.toString();
  }
}
