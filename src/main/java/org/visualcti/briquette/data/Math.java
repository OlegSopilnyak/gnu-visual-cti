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
package org.visualcti.briquette.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.core.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To realize mathematical operations</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class Math extends Basis
{
  /**
   * <accessor>
   * To get access to local Symbols borned in this Operation
   * */
  public final List getLocalSymbols(){
    ArrayList symbols = new ArrayList();
    for(Iterator i=this.set.getSequence().iterator();i.hasNext();)
    {
      Symbol symbol = ((MathExpression)i.next()).getTarget();
      if ( !symbols.contains(symbol) ) symbols.add(symbol);
    }
    return symbols;
  }
    /**
     * <constructor>
     * */
    public Math(){super.setAbout("To calculate the Math's expressions");}
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "Math.";}
  /**
   * <check>
   * Is db-connection needs for this Operation (not needs)
   * */
  protected final boolean isDatabase(){return false;}
/**
 * <attribute>
 * The sequence of math expressions
 */
private final MathExpressionsSet set = new MathExpressionsSet();
  /**
   * <acessor>
   * To get access to math expressions set
   * @return math expressions set
   */
  public final MathExpressionsSet getSet(){return this.set;}
    /**
     * <store>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( this.set.getXML() );
    }
    /**
     * <restore>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      // to clear old pool
      this.set.Clear();
      // check the input parameter
      if (xml == null) return;
      Element setXML = xml.getChild( this.set.xmlRoot() );
      if ( setXML != null)
      {
        this.set.setXML( setXML );
        return;
      }
      // to iterate all children with name "calculate" (backward compability)
      List expressions = xml.getChildren(MathExpression.ELEMENT);
      for(Iterator i=expressions.iterator();i.hasNext();)
      { // to make, configure and add the restored expression
        MathExpression expression = new MathExpression();
        expression.setXML( (Element)i.next() );
        this.set.Add( expression );
      }
    }
  /**
   * <main>
   * The main method of Operation
   * */
  protected final void dataMethod(Subroutine caller)
  {
    for(Iterator i=this.set.getSequence().iterator();i.hasNext();)
    {
      try{
        ((MathExpression)i.next()).calculate( caller );
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }
}
