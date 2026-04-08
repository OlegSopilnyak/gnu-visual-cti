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
package org.visualcti.briquette.core;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.control.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow the source of media stream </p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class mediaSource extends Media
{
/**
 * <const>
 * The value of direction's XML-attribute
 * */
public final static String DIRECTION = "input";
  /**
   * <translator>
   * To translate the media source to XML's Element
   * */
  public Element getXML()
  {
    Element xml = super.getXML();
    xml.setAttribute( new Attribute(Media.DIRECTION, mediaSource.DIRECTION) );
    return xml;
  }
  /**
   * <tarnslator>
   * To restore the media's source from XML
   * */
  public void setXML(Element xml) throws Exception
  {
    String direction = xml.getAttributeValue( Media.DIRECTION );
    if  (
        direction == null ||
        !mediaSource.DIRECTION.equals(direction)
        )
    {
      throw new NullPointerException("Invalid direction's attribute Value");
    }
    super.setXML(xml);
  }
   /**
    * <producer>
    * To make the stream to media source
    * */
  public InputStream getInputStream(Subroutine caller) throws IOException
  {
    switch ( super.getType() )
    {
      case RAW:// in-memory file's image
          return this.rawInputStream(caller);

      case FILE:// file in disk
          return this.fileInputStream(caller);

      default:// invalid type of media
        throw new IOException("Invalid type of media");
    }
  }
  /**
   * <producer>
   * to make the Input stream to in-memory bytes array
   * */
  private final ByteArrayInputStream rawInputStream(Subroutine caller) throws IOException {
    Object value = super.getContentValue( caller );
    try {
      return new ByteArrayInputStream( (byte[])value );
    }catch(ClassCastException e){
      throw new IOException("Invalid RAW-data format in the pool");
    }
  }
  /**
   * <producer>
   * to make the Input stream to file
   * */
  private final FileInputStream fileInputStream(Subroutine caller) throws IOException {
    String file = super.getContentValue( caller ).toString();
//System.out.println("Will use ["+file+"]");
    if ( "".equals(file) ) throw new IOException("Filename not defined...");
    return new FileInputStream( file );
  }
}
