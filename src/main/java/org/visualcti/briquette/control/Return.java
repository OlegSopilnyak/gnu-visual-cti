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
package org.visualcti.briquette.control;

import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The briquette for return from the subroutine (no next operation)</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Return extends org.visualcti.briquette.telephony.Basis
{
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "Exit.";}
    /**
     * <constructor>
     * */
    public Return(){this.setAbout("To exit a subroutine");}
    /**
     * <accessor>
     * to get quantity of output's links
     * Return don't have any output connectors
     * */
    protected final int linksCount(){return 0;}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("dropCall",this.dropCall).getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      // to solve the dropCall's property
      Element propXML = xml.getChild(Property.ELEMENT);
      if (propXML == null) {this.dropCall = false; return;}
      Property property = new Property(propXML);
      // to solve property
      if ("dropCall".equals(property.getName()) ){
        this.dropCall = property.getValue( false );
      }else {
        this.dropCall = false;
      }
    }
  /**
   * <main>
   * Main method of telephony's Operation
   * */
  protected final void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    if ( this.dropCall ) device.dropCall();
  }
  /**
   * <mutator>
   * To change the drop call's flag
   * */
  public final void setDropCall(boolean dropCall) {this.dropCall = dropCall;}
  /**
   * <accessor>
   * To get access to drop call's flag
   * */
  public final boolean isDropCall() {return dropCall;}
  /**
   * <attribute>
   * The drop call's flag
   * */
  private boolean dropCall = false;
}
