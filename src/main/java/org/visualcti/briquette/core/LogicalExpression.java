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

import java.util.Arrays;
import java.util.Collections;
import org.jdom.Attribute;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class LogicalExpression extends Expression
{
/** the array of valid operations */
private static final String
operations[] = new String[]{"<",">","=","!=","<>","<=",">=","LIKE",};
/**
 * <const>
 * The list of valid logical operations
 * */
public static final java.util.List
validOperations = Collections.unmodifiableList(Arrays.asList(operations));
/**
 * <const>
 * The name of XML's element
 * */
public static final String ELEMENT="calculate";
/**
 * <constructor>
 * */
public LogicalExpression()
{
  super.first = Symbol.newConst("");
  super.operation = "=";
  super.second = Symbol.newConst("");
}
/**
* <translator>
* To represent expression as String
* Overrided Object.toString()
* */
public final String toString()
{
StringBuffer sb = new StringBuffer(first.cell());
sb.append(" ").append(this.operation);
sb.append(" ").append(this.second.cell());
return sb.toString();
}
/**
 * <producer>
 * To make the copy of expression
 * */
public final LogicalExpression copy()
{
  LogicalExpression copy = new LogicalExpression();
  copy.first = this.first.copy();
  copy.operation = String.valueOf(this.operation);
  copy.second= this.second.copy();
  return copy;
}
  /**
   * <check>
   * To check integrity of expression
   * */
  public final boolean isValid()
  {
    return
      this.operation.length() > 0   &&
      this.isOperation( this.operation );
  }
  /**
   * <translator>
   * To store the expression to XML's element
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    if ( this.isValid() ) {
      xml.setAttribute(new Attribute("type","logical"));
      xml.addContent( super.getXML() );
    }
    return xml;
  }
  /**
   * <translator>
   * To get SQL representation of Expression
   * */
  public final String getSQL(Subroutine owner) throws Exception
  {
    if ( !this.isValid() ) return "";
    StringBuffer sb = new StringBuffer();
    // to add first operand
    if (first.getGroupID() == Symbol.DBCOLUMN)
      sb.append(first.getName());
    else {
      this.addSymbolSQL(first,sb,owner,"first");
    }
    // to add operation
    sb.append(" ").append(super.operation).append(" ");
    // to add second operand
    if (second.getGroupID() == Symbol.DBCOLUMN)
      sb.append(second.getName());
    else {
      this.addSymbolSQL(second,sb,owner,"second");
    }
    return sb.toString();
  }
  /**
   * To translate the Symbol to SQL's part
   * */
  private final void addSymbolSQL
                        (
                        Symbol symbol,
                        StringBuffer sb,
                        Subroutine owner,
                        String prefix
                        )
                        throws Exception
  {
    Object value = symbol.getName();
    if ( !symbol.isConst() ) {
      value = owner.get( symbol );
      if (value == null) throw new NullPointerException(prefix+" operand's value is null");
    }
    // to make the SQL const's string
    boolean isString = symbol.getTypeID() == Symbol.STRING;
    if ( isString ) sb.append("\'");
    sb.append( value );
    if ( isString ) sb.append("\'");
  }
  /**
  * <translator>
  * To restore expression from XML's element
  * */
  public void setXML(Element xml) throws Exception
  {
    super.clear();
    if ( xml == null ) return;
    if ( !"logical".equals(xml.getAttributeValue("type")) ) return;
    super.setXML( xml.getChild(Expression.ELEMENT) );
  }
  protected final boolean isOperation(String operation) {
    if ( operation == null || "".equals(operation) ) return false;
    return validOperations.contains(operation);
  }
  public final void changed() {
    //System.out.println("Logical expression = "+this.toString());
  }
  /**
   * <process>
   * To calculate the expression
   * */
  public final boolean calculate(Subroutine owner) throws Exception
  {
    if ( !this.isValid() ) throw new Exception("Invalid expresssion");
    Object o1 = owner.get( super.first );
    if (o1 == null) throw new NullPointerException("first operand is null");
    Object o2 = owner.get( super.second );
    if (o2 == null) throw new NullPointerException("second operand is null");
    //if (rFirst == null || rSecond == null) return false;// or throw Exception?
    int different = first.getTypeID()==Symbol.NUMBER ?
                                        this.nDifferent(o1,o2):
                                        this.sDifferent(o1,o2);
    // to calculate the expression's values
    if      (  "<".equals(operation) ) return different  < 0;
    else if (  ">".equals(operation) ) return different  > 0;
    else if (  "=".equals(operation) ) return different == 0;
    else if ( "<=".equals(operation) ) return different <= 0;
    else if ( ">=".equals(operation) ) return different >= 0;
    else if ( "!=".equals(operation) ) return different != 0;
    else if ( "<>".equals(operation) ) return different != 0;
    else if ( "LIKE".equalsIgnoreCase(operation) ) return this.like(o1,o2);
    return false;
  }
  /**
   * To calculate the diffrences between Numbers
   * */
  private final int nDifferent(Object o1,Object o2){
    double delta = ((Number)o1).doubleValue() - ((Number)o2).doubleValue();
    // to make differnet like String.compareTo(Object o)
    if (delta < 0) return -1;
    else
    if (delta > 0) return  1;
    else           return  0;
  }
  /**
   * To calculate the diffrences between Strings
   * */
  private final int sDifferent(Object o1,Object o2){
    return ((String)o1).compareTo( o2.toString() );
  }
  /**
   * To calculate the like's expression
   */
  private final boolean like(Object o1,Object o2){
    return new logicTools.sqlLike(o2.toString()).isLike(o1.toString());
  }
}
