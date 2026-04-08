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
 * <p>Description: VisualCTI WorkFlow, the target of media</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class mediaTarget extends Media {
/**
 * <const>
 * The value of direction's XML-attribute
 * */
public final static String DIRECTION = "output";
  /**
   * <translator>
   * To translate the media source to XML's Element
   * */
  public Element getXML()
  {
    Element xml = super.getXML();
    xml.setAttribute( new Attribute(Media.DIRECTION, mediaTarget.DIRECTION) );
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
        !mediaTarget.DIRECTION.equals(direction)
        )
    {
      throw new NullPointerException("Invalid direction's attribute Value");
    }
    super.setXML(xml);
  }
   /**
    * <producer>
    * To make the stream to store the media-data
    * */
  public final OutputStream getOutputStream(Subroutine caller) throws IOException
  {
    switch ( super.getType() )
    {
      case RAW:// in-memory file's image
          return this.getRawContainer( caller );

      case FILE:// file in disk
          return this.fileRawContainer( caller );

      default:// invalid type of media
        throw new IOException("Invalid type of media");
    }
  }
  /**
   * <producer>
   * To make the container for RAW-type
   * */
  protected abstract memoryContainer getRawContainer(Subroutine caller) throws IOException;
  /**
   * <producer>
   * To make the container for FILE-type
   * */
  protected abstract fileContainer fileRawContainer(Subroutine caller) throws IOException;
  /**
   * <container>
   * To store the media-data to memory
   * */
  public static abstract class memoryContainer extends Container{
    protected Symbol target;
    public memoryContainer(Subroutine owner,Symbol target) throws IOException
    {
      super(owner);
      if ( !this.isValid(target) ) throw new IOException("Invalid target");
      this.target=target;
    }
    protected boolean isValid(Symbol target){
      return target != null && !target.isConst();
    }
    protected final void storeImage() throws IOException {
      super.storeImage();
      this.owner.set( this.target, this.image() );
    }
  }
  /**
   * <container>
   * To store the media-data to the file
   * */
  public static abstract class fileContainer extends Container{
    protected Symbol target;
    public fileContainer(Subroutine owner,Symbol target) throws IOException
    {
      super(owner);
      if (target == null || target.getTypeID() != Symbol.STRING)
        throw new IOException("Invalid target");
      this.target=target;
    }
    protected final void storeImage() throws IOException {
      super.storeImage();
      try {
        String file = this.owner.get(this.target).toString();
        FileOutputStream out = new FileOutputStream( file );
        out.write( this.image() ); out.close();
      }catch (Exception e){
        throw new IOException("fileContainer,during close() "+e.getMessage());
      }
    }
  }
  /**
   * <container>
   * Class for store the media-data
   * */
  public static abstract class Container extends ByteArrayOutputStream {
    protected Subroutine owner;
    public Container(Subroutine owner) {this.owner=owner;}
    protected abstract Symbol defaultTarget();
    public final void close() throws IOException
    {
      super.close();
      if ( super.size() > 0 ) storeImage();
    }
    protected void storeImage() throws IOException {
      this.owner.set( this.defaultTarget(), this.image() );
    }
    protected byte[] image(){return super.toByteArray();}
  }
}
