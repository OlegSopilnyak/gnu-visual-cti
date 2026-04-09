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
package org.visualcti.briquette.control;

import org.jdom.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: Operation of branch on the condition</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Branching extends Operation
{
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "IF.";}
    /**
     * <constructor>
     * */
    public Branching(){this.setAbout("The branching briquette");}
/**
 * <attribute>
 * The logical sequence for branching
 * */
private final Logic content = new Logic();
    /**
     * <accessor>
     * To get access to branching's context
     * */
    public final Logic getContent(){return this.content;}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( this.content.getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.content.setXML( xml.getChild(Logic.ELEMENT) );
    }
    /**
     * <action>
     * To stop Operator executing
     * */
    public final void stopExecute(){}
    /**
     * <accessor>
     * to get count of links
     * 2 links (only for Branching)
     * */
    protected final int linksCount(){return 2;}
    /**
     * <action>
     * To execute this operator
     * After execution, method returns the reference
     * to the next executable operator.
     * If null has returned, the Chain (owner) must finish
     * */
    public final Operation doIt(Subroutine caller)
    {
      this.calculateCondition(caller);
      return this.condition ?
          this.getLink(Operation.DEFAULT_LINK):
          this.getLink(Operation.ALTERNATE_LINK);
    }
/**
 * <flag>
 * Is condition is true
 * */
private boolean condition=true;
  /**
   * <calculator>
   * To calculate the condition
   * */
  private final void calculateCondition(Subroutine caller)
  {
    try {
      this.condition = this.content.calculate(caller);
    }catch(Exception e) {
      this.condition = false;
    }
  }
}
