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
import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: parent of any math/logical expression</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

abstract class Expression
{
public final static String ELEMENT = "expression";
/**
 * <notify>
 * To notify about The expression is changed
 * */
public abstract void changed();
/**
 * <check>
 * To validate the operation
 * */
abstract protected boolean isOperation(String operation);
/**
 * <accessor>
 * To check, is Expression is empty
 * (is defined first operand)
 * */
public boolean isEmpty(){
  return "".equals(this.first.getName());
}
/**
 * <mutator>
 * To clear the expression
 * */
protected final void clear(){
  first = Symbol.newConst("");
  operation = "";
  second = Symbol.newConst("");
}
/**
 * <operand>
 * The key for get access to primary operand's value
 * */
protected Symbol first = Symbol.newConst("");
  public final Symbol getFirst() {return this.first;}
  public final void setFirst(Symbol first) {
    if (first != null){this.first = first;this.changed();}
  }
/**
 * <operation>
 * The operation between operands
 * */
protected String operation = "";
  public String getOperation() {return operation;}
  public void setOperation(String operation) {
    if (operation != null && this.isOperation(operation)) {
      this.operation = operation;this.changed();
    }
  }
/**
 * <operand>
 * The key for get access to secondary operand's value
 * */
protected Symbol second = Symbol.newConst("");
  public final Symbol getSecond() {return second;}
  public final void setSecond(Symbol second) {
   if (second != null){this.second = second;this.changed();}
  }
  /**
   * <translator>
   * To store the expression to XML's element
   * */
   public Element getXML()
   {
    Element xml = new Element(ELEMENT);
    xml.setAttribute(new Attribute("first",this.first.toString()));
    xml.setAttribute(new Attribute("operation",this.operation));
    xml.setAttribute(new Attribute("second",this.second.toString()));
    return xml;
   }
   /**
    * <translator>
    * To restore expression form XML's element
    * */
   public void setXML(Element xml) throws Exception
   {
    // to clear attributes
    this.clear();
    // to check the integrity of XML's element
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    // to solve XML
    String first = xml.getAttributeValue("first");
    if (first == null) return;// invalid first Symbol
    this.first = Symbol.fromString(first);
    String operation = xml.getAttributeValue("operation");
    if ( !this.isOperation(operation) ) return;// invalid operation
    String second = xml.getAttributeValue("second");
    if (second == null) return;// invalid second's Symbol
    this.second = Symbol.fromString(second);
    this.setOperation(operation);
   }
   /**
    * <translator>
    * To represent expression as String
    * Overrided Object.toString()
    * */
   public String toString()
   {
    if (this.first.getName().length() == 0) return "";
    StringBuffer sb = new StringBuffer(first.cell());
    if (this.operation.length() == 0) return sb.toString();
    if (this.second.getName().length() > 0)
    {
      sb.append(" ").append(this.operation);
      sb.append(" ").append(this.second.cell());
    }
    return sb.toString();
   }
}
