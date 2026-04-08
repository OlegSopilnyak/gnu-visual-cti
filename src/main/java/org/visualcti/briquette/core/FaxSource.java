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
import org.visualcti.media.*;
import org.visualcti.util.MediaData;
import org.visualcti.briquette.*;
import org.visualcti.briquette.telephony.*;
import org.visualcti.briquette.control.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the source of Fax's stream</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class FaxSource extends mediaSource implements Document{
  /**
   * <accessor>
   * To get list of valid type's names
   * */
  public final java.util.List getTypes(){return super.getTypes();}
  /**
   * <accessor>
   * To get access to content's Symbol by default
   * */
  protected final Symbol defaultSymbol(int type) {
    switch( type )
    {
      case Media.RAW:
        return ReceiveFax.system_cti_Fax;
      case Media.FILE:
        return Symbol.newConst("fax.tiff");
    }
    return Symbol.newConst("<none>");
  }
  /**
   * <accessor>
   * to get access to RAW type ID
   * */
  protected final int getRawTypeID() {return Symbol.FAX;}
  /**
   * <accessor>
   * To get access to the name of group
   * */
  protected final String getMediaGroup() {return "FAX";}
  /**
   * <format>
   * The format of the stream
   * */
  private Fax format = Fax.TEXT;
  /**
   * <accessor>
   * To get access to format of the sream
   * */
  public final Fax getFormat(){return this.format;}
  /**
   * <mutator>
   * To setup the format of source
   * */
  public final void setFormat(Fax format) {this.format = format;}
  /**
   * <attribute>
   * The owner of the stream
   * */
  private Subroutine owner=null;
  /**
   * <mutator>
   * To assign the source with Subroutine
   * */
  public final void setOwner(Subroutine owner) {this.owner = owner;}
  /**
   * <accessor>
   * To get access sound's audio-data Stream
   * */
  public final InputStream getInputStream() throws IOException
  {
    if (this.owner == null) throw new IOException("Can't get access to owner of the source");
    return this.getInputStream( this.owner );
  }
  /**
   * <producer>
   * To make the stream to media source
   * */
  public final InputStream getInputStream(Subroutine caller) throws IOException
  {
    this.owner = caller;
    return this.check( super.getInputStream(caller) );
  }
  /**
   * <check>
   * to solve the format of fax stream
   * */
  private final InputStream check(InputStream in) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(30000);
    byte[] block = new byte[4096]; int readed = -1;
    while( (readed = in.read(block)) != -1) {
      if (readed == 0) break;
      buffer.write(block,0,readed);
    }
    buffer.flush();
    byte[] image = buffer.toByteArray();
    StringBuffer description = new StringBuffer();
    if ( MediaData.isValidTiff(image,description) )
      this.format = Fax.TIFF; else this.format = Fax.TEXT;
    return new ByteArrayInputStream( image );
  }
}
