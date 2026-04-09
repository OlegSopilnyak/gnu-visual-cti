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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.telephony.ReceiveFax;
import org.visualcti.media.Fax;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the container for receive the fax's data</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class FaxTarget extends mediaTarget {
/**
 * <member>
 * The default Symbol for any fax's container
 * */
public static final Symbol defaultTarget = ReceiveFax.system_cti_Fax;

  protected final int getRawTypeID() {return Symbol.FAX;}
  protected final String getMediaGroup() {return "FAX";}
  protected final Symbol defaultSymbol(int type) {
    switch( type )
    {
      case Media.RAW:
        return defaultTarget;
      case Media.FILE:
        return Symbol.newConst("fax.tiff");
    }
    return Symbol.newConst("<none>");
  }

  protected final memoryContainer getRawContainer(Subroutine caller) throws IOException
  {
    return new memoryFaxContainer(caller);
  }
  protected final fileContainer fileRawContainer(Subroutine caller) throws IOException
  {
    return new fileFaxContainer(caller);
  }
  /**
   * <mutator>
   * To setup the format of fax's stream
   * */
  public void setFormat(Fax format) {this.format = format;}
/**
 * <attribute>
 * The format of the fax
 * */
private Fax format=null;
  /**
   * <producer>
   * To make a valid fax file's image
   * */
  private final byte[] validImage(byte[] body){
    ByteArrayOutputStream image = new ByteArrayOutputStream();
    return image.toByteArray();
  }
  /**
   * <container>
   * The conatiner, to store data to the memory
   * */
  private final class memoryFaxContainer extends memoryContainer {
    /** to return a valid image of the sound's file */
    protected final byte[] image(){return validImage(super.image());}
    /** constructor */
    memoryFaxContainer(Subroutine caller) throws IOException
    {
      super(caller,getContent());
      if ( format == null ) throw new IOException("Not defined the Format of container");
    }
    protected final Symbol defaultTarget(){return defaultTarget;}
    /** to validate the target's Symbol */
    protected final boolean isValid(Symbol target){
      return super.isValid(target) && target.getTypeID() == Symbol.FAX;
    }
  }
  /**
   * <container>
   * The conatiner, to store data to the file
   * */
  private class fileFaxContainer extends fileContainer {
    /** to return a valid image of the sound's file */
    protected final byte[] image(){return validImage(super.image());}
    /** constructor */
    fileFaxContainer(Subroutine caller) throws IOException
    {
      super(caller,getContent());
      if ( format == null ) throw new IOException("Not defined the Format of container");
    }
    protected final Symbol defaultTarget(){return defaultTarget;}
  }
}
