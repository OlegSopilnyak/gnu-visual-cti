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

import org.jdom.*;
import java.util.*;

import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the set of math's expressions</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class MathExpressionsSet
{
  /**
   * <accessor>
   * To get access to name of root XML's Element
   * @return the name
   */
  public String xmlRoot(){return "maths_sequence";}
  /**
   * <producer>
   * To make new Math expression for add to the sequence
   * @return new instance of expression
   */
  public MathExpression newMathExpression(){return new MathExpression();}
  /**
   * <store>
   * To store the set to XML's format
   * @return stored set
   */
  public final Element getXML()
  {
    Element xml=new Element( this.xmlRoot() );
    for(Iterator i=this.sequence.iterator();i.hasNext();)
    {
      xml.addContent( ((MathExpression)i.next()).getXML() );
    }
    return xml;
  }
  /**
   * <restore>
   * To restore the set from XML
   * @param xml stored set
   * @throws Exception throws if some wrong
   */
  public final void setXML(Element xml) throws Exception
  {
    this.Clear();
    if ( xml==null || !this.xmlRoot().equals(xml.getName()) ) return;
    for(Iterator i=xml.getChildren(MathExpression.ELEMENT).iterator();i.hasNext();)
    {
      MathExpression item=this.newMathExpression();
      item.setXML( (Element)i.next() ); this.sequence.add( item );
    }
  }
/**
 * <attribute>
 * The sequence of math's expressions
 */
private final List sequence = new ArrayList();
  /**
   * <accessor>
   * To get access to copy of sequence
   * @return the copy of sequence's list
   */
  public final List getSequence(){return new ArrayList(this.sequence);}
  /**
   * <check>
   * To check is symbol presents in expressions set
   * @param symbol
   * @return
   */
  public final boolean contains(Symbol symbol)
  {
    for(Iterator i=this.sequence.iterator();i.hasNext();)
    {
      MathExpression item = (MathExpression)i.next();
      if ( item.getTarget().equals(symbol) ) return true;
    }
    return false;
  }
    /**
     * to validate the expression after editing
     * @param expression the expression to validate
     * @return valid expression
     */
    private final MathExpression valid(MathExpression expression){
      if( "".equals(expression.getSecond().getName()) )expression.setOperation("");
      return expression;
    }
  /**
   * <mutator>
   * To clear the set
   */
  public final void Clear(){this.sequence.clear();}
  /**
   * <accessor>
   * To get access to MathExpression by index
   * @param index index in sequence
   * @return the expression or null, if index is wrong
   */
  public final MathExpression getAt(int index)
  {
    try{return (MathExpression)this.sequence.get(index);
    }catch(IndexOutOfBoundsException e){}
    return null;
  }
  /**
   * <mutator>
   * To add new expression
   * @param expression new expression
   * @return valid, added expression
   */
  public final MathExpression Add(MathExpression expression){
    this.sequence.add( expression=this.valid(expression) );
    return expression;
  }
  /**
   * <mutator>
   * To update expression
   * @param expression new expression
   * @param index index of old expression
   * @return valid new expression or null if some wrong
   */
  public final MathExpression Update(MathExpression expression,int index){
    try{
      this.sequence.set( index, expression=this.valid(expression) );
      return expression;
    }catch(Exception e){}
    return null;
  }
  /**
   * <mutator>
   * To delete expression by index
   * @param index the index to delete
   * @return true if success, else false
   */
  public final boolean Delete(int index){
    try{
      this.sequence.remove(index); return true;
    }catch(IndexOutOfBoundsException e){}
    return false;
  }
  /**
   * <mutator>
   * To move up the item by index
   * @param index the index to move Up
   * @return true if success, else false
   */
  public final boolean moveUp(int index)
  {
    if (
        this.sequence.size() <= 1 ||
        index <= 0
        ) return false;
    Object entry = this.sequence.remove(index);
    if (entry != null) this.sequence.add(index-1,entry);
    return true;
  }
  /**
   * <mutator>
   * To move down the item by index
   * @param index the inde to move Down
   * @return true if success, else false
   */
  public final boolean moveDown(int index)
  {
    if (
        this.sequence.size() <= 1 ||
        index < 0 ||
        index >= this.sequence.size()-1
        ) return false;
    Object entry = this.sequence.remove(index);
    if (entry != null) this.sequence.add(index+1,entry);
    return true;
  }
}
