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

import org.jdom.Attribute;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the math expression</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class MathExpression extends Expression {
/**
 * <producer>
 * To make the copy of expression
 * */
public MathExpression copy()
{
  MathExpression copy = new MathExpression();
  try{copy.setXML(this.getXML());}catch(Exception e){}
  return copy;
}
/**
 * To copy expression's content to other expression
 * @param other other expression
 */
public void copyTo(MathExpression other)
{
  other.target    = this.target.copy();
  other.first     = this.first.copy();
  other.operation = String.valueOf(this.operation);
  other.second    = this.second.copy();
}
/**
 * <notify>
 * To notify about The expression is changed
 * */
public final void changed()
{
//  System.out.println("Expression:"+this.toString());
}
/**
 * <accessor>
 * To check, is MathExpression is empty
 * */
public final boolean isEmpty(){
  return "".equals(this.target.getName()) || super.isEmpty();
}
/**
 * <set>
 * The set of valid opartions for string
 * */
private static String string = "+";
/**
 * <set>
 * The set of valid opartions for number
 * */
private static String number = "+-/*";
/**
 * <attribute>
 * The symbol for target
 * */
private Symbol target = Symbol.newConst("");
  /**
   * <accessor>
   * To get access to target Symbol
   * */
  public final Symbol getTarget(){return target;}
  /**
   * <mutator>
   * To setup the target's Symbol
   * */
  public final void setTarget(Symbol target)
  {
    if (target != null && target.getGroupID() == this.targetGroupID())
    {
      this.target = target; this.changed();
    }
  }
  /**
   * <accessor>
   * to check the symbols list
   * Can be redefined in dbMathExpression
   * @param symbols availabled
   */
  public java.util.List valid(java.util.List symbols){return symbols;}
  /**
   * <accessor>
   * To get access to target's Group's ID
   * Can be redefined in dbMathExpression
   * */
  protected int targetGroupID(){return Symbol.USER;}
  /**
   * <check>
   * To check is expression valid
   * */
  public final boolean isValid()
  {
    return !this.target.isConst() &&
            this.target.getGroupID() == this.targetGroupID() &&
            this.target.getTypeID() == super.first.getTypeID();
  }
/**
 * <const>
 * The name of XML's element
 * */
public static final String ELEMENT="calculate";
  /**
   * <accessor>
   * To get access to type name of expression
   * @return the name
   */
  protected String type(){return "math";}
  /**
   * <translator>
   * To store the expression to XML's element
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    if ( this.isValid() ) {
      xml.setAttribute(new Attribute("type",this.type()));
      xml.setAttribute(new Attribute("target",this.target.toString()));
      xml.addContent( super.getXML() );
    }
    return xml;
  }
  /**
  * <translator>
  * To restore expression form XML's element
  * */
  public void setXML(Element xml) throws Exception
  {
    super.clear(); this.target = Symbol.newConst("");
    if ( xml == null || !this.type().equals(xml.getAttributeValue("type")) ) return;
    String target = xml.getAttributeValue("target");
    if ( target != null)
    {
      this.target = Symbol.fromString( target );
      super.setXML( xml.getChild(Expression.ELEMENT) );
    }
  }
  public final String expString(){return super.toString();}
  /**
  * <translator>
  * To represent expression as String
  * Overrided Object.toString()
  * */
  public String toString()
  {
    if (this.target.getName().length() == 0) return "";
    StringBuffer sb = new StringBuffer(this.target.getName());
    sb.append("=").append(super.toString());
    return sb.toString();
  }
  /**
   * <check>
   * To validate the operation
   * */
  final protected boolean isOperation(String operation)
  {
    if (operation == null || "".equals(operation) ) return false;
    switch( super.first.getTypeID() )
    {
      case Symbol.NUMBER:// value is Number
        return MathExpression.number.indexOf(operation) != -1;
      case Symbol.STRING:// value is String
        return MathExpression.string.indexOf(operation) != -1;
    }
    return false;
  }
  /**
   * <process>
   * To process the expression
   * */
  public final void calculate(Subroutine owner) throws Exception
  {
    if (owner == null || !this.isValid() ) {
System.out.println("Invalid:"+this);
      return;
    }
    Object result = owner.get( super.first );
    if ( result == null) return;
    if ( !this.isOperation(this.operation) )
    {
      owner.set(this.target,result);
      return;
    }
    // to calculate the expression
    switch( super.first.getTypeID() )
    {
      case Symbol.NUMBER:
          result=calculateNumber(owner,result);
          break;
      case Symbol.STRING:
          result = calculateString(owner,result);
    }
    // to store result to owner
    owner.set(target,result);
  }
  /**
   * <calculator>
   * To calculate the String's expression, only "+"
   * */
  private final Object calculateString(Subroutine owner,Object first)
                            throws Exception
  {
    Object second = owner.get( super.second );
    if (second == null) return first;
    return new StringBuffer(first.toString()).append(second).toString();
  }
  /**
   * <calculator>
   * To calculate the Number's expression
   * */
  private final Object calculateNumber(Subroutine owner,Object first)
                            throws Exception
  {
    Object second = owner.get( super.second );
    if (second == null) return first;
    double operand1 = ((Number)first).doubleValue();
    double operand2 = ((Number)second).doubleValue();
    double result = 0.0;
    switch( this.operation.charAt(0) )
    {
      case '+':
        result = operand1+operand2;
        break;

      case '-':
        result = operand1-operand2;
        break;

      case '*':
        result = operand1*operand2;
        break;

      case '/':
        if (operand2 == 0) {
          throw new Exception("Secondary value is 0 (divide by zero)");
        }
        result = operand1/operand2;
        break;
    }
    long i=(long)result;// Try to present's it as Long
    return (double)i==result ? (Object)new Long(i): (Object)new Double(result);
    //return new Double(result);
  }
}
