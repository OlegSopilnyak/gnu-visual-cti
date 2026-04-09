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

import java.util.*;
import org.jdom.*;
import org.visualcti.briquette.*;
//import org.visualcti.briquette.control.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: to represent and calculate the logical expression</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Logic {
/**
 * <const>
 * Next part not exists
 * */
public final static int NONE = -1;
/**
 * <const>
 * Apply OR for next part
 * */
public final static int OR = 0;
/**
 * <const>
 * Apply AND for next part
 * */
public final static int AND = 1;
/**
 * <mutator>
 * To clear the sequence
 */
public final void clear(){this.sequence.clear();}
/**
 * <mutator>
 * To add new Part
 * */
public final void addPart(Part part)
{
  if ( this.sequence.size() > 0 )
  {
    Part last = (Part)this.sequence.getLast();
    last.nextCondition = Logic.AND;
  }
  this.sequence.add(part);
}
/**
 * <mutator>
 * To delete part
 * */
public final Part delPart(Part part)
{
  int index = this.sequence.indexOf(part);
  if ( index == -1 ) return null;
  this.sequence.remove(index);
  if (this.sequence.size() == 0) return null;
  try {return (Logic.Part)this.sequence.get(index);
  }catch(IndexOutOfBoundsException e){}
  return (Logic.Part)sequence.get(index-1);
}
/**
 * <mutator>
 * To move up the part
 * */
public final boolean moveUpPart(Part part)
{
  if ( sequence.size() <= 1 ) return false;
  int index = this.sequence.indexOf(part);
  if (index <= 0) return false;
  Object entry = this.sequence.remove(index);
  this.sequence.add(index-1,entry);
  if (part.isNoNext()) part.nextCondition = Logic.AND;
  return true;
}
/**
 * <mutator>
 * To move down the part
 * */
public final boolean moveDownPart(Part part)
{
  int size;
  if ( (size=sequence.size()) <= 1 ) return false;
  int index = this.sequence.indexOf(part);
  if (index < 0 || index+1 >= size) return false;
  Object entry = this.sequence.remove(index);
  if ( index >= size )
    this.sequence.add( entry );
  else
    this.sequence.add(index+1,entry);
  return true;
}

/**
 * <part>
 * The part of logical sequence
 * */
public final static class Part {
  /**
   * <attribute>
   * The expression of this part
   * */
  public LogicalExpression expression;
  /** <constructor> */
  public Part(LogicalExpression expression){this.expression=expression;}
  private int nextCondition = NONE;
  public final void setNextAND(){this.nextCondition=AND;}
  public final void setNextOR(){this.nextCondition=OR;}
  public final boolean isNextOR(){return nextCondition == OR;}
  public final boolean isNextAND(){return nextCondition == AND;}
  public final boolean isNoNext(){return !isNextAND() && !isNextOR();}
  /** to represents the part as String */
  public final String toString(){return String.valueOf(this.expression);}
  /** to represent the part as SQL */
  public final String getSQL(Subroutine owner) throws Exception{
    return this.expression.getSQL(owner);
  }
  /** to calculate the part's expression*/
  public final boolean calculate(Subroutine owner) throws Exception{
    return this.expression.calculate(owner);
  }
  /**<const> the name of XML's element */
  public static final String ELEMENT = "logic_part";
  /** to store the part to XML's format */
  public final Element getXML(){
    Element xml = new Element(Part.ELEMENT);
    xml.addContent(this.expression.getXML());
    Element condXML = new Element("next");
    switch( this.nextCondition ){
      case OR:
        condXML.setAttribute(new Attribute("condition","OR"));
        xml.addContent( condXML );
        break;
      case AND:
        condXML.setAttribute(new Attribute("condition","AND"));
        xml.addContent( condXML );
        break;
    }
    return xml;
  }
  /** to restore part from XML */
  public final void setXML(Element xml) throws Exception {
    this.expression = new LogicalExpression();
    this.nextCondition = NONE;
    if ( xml == null || !Part.ELEMENT.equals(xml.getName()) ) return;
    this.expression.setXML(xml.getChild(LogicalExpression.ELEMENT));
    Element nextXML = xml.getChild("next");
    if ( nextXML == null ) return;
    String cond = nextXML.getAttributeValue("condition");
    if ( "OR".equalsIgnoreCase(cond) ) this.nextCondition = OR;
    else
    if ( "AND".equalsIgnoreCase(cond) ) this.nextCondition = AND;
  }
}
/**
 * <attribute>
 * The logic's sequence
 * */
private final LinkedList sequence = new LinkedList();
  /**
   * <accessor>
   * To get access to the sequence
   * */
  public final LinkedList getSequence(){return this.sequence;}
/**
 * <const>
 * The name of XML's element
 * */
public final static String ELEMENT = "logic";
  /**
   * <translator>
   * To store logic's sequence to XML
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    // to iterate the sequence
    synchronized( this.sequence )
    {
      for(Iterator i=this.sequence.iterator();i.hasNext();)
      {
        Part part = (Part)i.next();
        xml.addContent( part.getXML() );
        if ( part.isNoNext() ) break;// end of sequence detected
      }
    }
    return xml;
  }
  /**
   * <translator>
   * To restore logic's sequence from XML's Element
   * */
  public final void setXML(Element xml) throws Exception
  {
    this.sequence.clear();
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    for(Iterator i=xml.getChildren(Part.ELEMENT).iterator();i.hasNext();)
    {
      Part part = new Part( null );
      part.setXML( (Element)i.next() );
      this.sequence.add( part );
      if ( part.isNoNext() ) break;
    }
  }
  /**
   * <calculator>
   * To calculate the logical expression
   * */
  public final boolean calculate(Subroutine owner) throws Exception
  {
    boolean result = false;
    for(Iterator i=this.sequence.iterator();i.hasNext();)
    {
      Part part = (Part)i.next();
      result = part.calculate(owner);
      if ( result == true  && part.isNextOR()  ) return true;
      if ( result == false && part.isNextAND() ) return false;
      if ( part.isNoNext() ) break;// end of sequence detected
    }
    return result;
  }
  /**
   * <translator>
   * To translate logic's sequence to SQL format
   * */
  public final String getSQL(Subroutine owner)
  {
    StringBuffer sb = new StringBuffer();
    for(Iterator i=this.sequence.iterator();i.hasNext();)
    {
      Part part = (Part)i.next();
      try {sb.append( part.getSQL(owner) );
      }catch(Exception e){
        break;
      }
      boolean isNormal = true;
      switch( part.nextCondition ){
        case OR:
          sb.append(" OR ");
          break;
        case AND:
          sb.append(" AND ");
          break;
        default:
          isNormal = false;
          break;
      }
      if ( !isNormal ) break;
    }
    return sb.toString();
  }
}
