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

import java.util.*;

import org.jdom.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
//import org.visualcti.briquette.control.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The parameter of the Call (Subroutine)</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class Parameter {
  /**
   * <parameter>
   * Class, formal parameter of Subroutine
   * */
  public static final class Formal extends Parameter
  {
    public static final String TYPE = "formal";
    /**
     * <producer>
     * To make the copy of parameter
     * */
    public final Formal copy()
    {
      Formal copy = new Formal();
      copy.name = this.name.copy();
      copy.value= this.value;
      return copy;
    }
    /**
     * <constructor>
     * */
    public Formal(String name){super(name);}
    private Formal(){super("");}
      /**
       * <accessor>
       * to get access to parameter's type for XML (actual/formal)
       * */
      protected final String parameterType(){return Formal.TYPE;}
      /**
       * <translate>
       * To translate the parameter to XML
       * */
      public final Element getXML()
      {
        Element xml = super.getXML();
        String value = this.storeValue();
        if (value != null) xml.addContent(new Property("value",value).getXML());
        return xml;
      }
      /** to make the value to store */
      private final String storeValue(){
        if (this.value == null) return null;
        int type = super.getType();
        if (type==Symbol.STRING || type==Symbol.NUMBER) return value.toString();
        return null;
      }
      /**
       * <translator>
       * To restore the parameter from XML
       * */
      public final void setXML(Element xml) throws Exception
      {
        super.setXML( xml );
        // to define the array of Properties name
        ArrayList names = new ArrayList( 1 );// for check a duplicates
        for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();)
        {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of base properties!");
          // to solve the names of property
          if ( "value".equals( name ) ) {
            String value = property.getValue( "" );names.add(name);
            this.restoreValue(value);
          }
        }
      }
      /** to restore value from string */
      private final void restoreValue(String str){
        if ( this.name.getTypeID() == Symbol.NUMBER ) {
          try {
            this.value = new Double(str);
          }catch(NumberFormatException e){
            this.value = new Integer(0);
          }
        }else
        if ( this.name.getTypeID() == Symbol.STRING ) {
          this.value = str;
        }else
          this.value = null;
      }
  }
  /**
   * <parameter>
   * Class, actual parameter of Subroutine
   * */
  public static final class Actual extends Parameter
  {
    public static final String TYPE = "actual";
    /**
     * <producer>
     * To make the copy of parameter
     * */
    public final Actual copy()
    {
      Actual copy = new Actual();
      copy.name  = this.name.copy();
      copy.value = this.value;
      copy.externalName= this.externalName.copy();
      return copy;
    }
    /**
     * <constructor>
     * */
    public Actual(Parameter.Formal par){
      this();
      this.name = par.name.copy();
      Object value = par.getValue();
      this.externalName.setType( par.name.getTypeID() );
      if ( value != null) this.externalName.setName( value.toString() );
    }
    private Actual(){super("");}
      /**
       * <mutator>
       * To get & store the value from parent of owner
       * */
      public final Object getValue(Subroutine parent)
      {
        if (parent != null && this.externalName != null)
          return this.value = parent.get(this.externalName);
        else
          return null;
      }
      /**
       * <attribute>
       * The source of value
       * */
      private Symbol externalName = Symbol.newConst("");
      /**
       * <accessor>
       * To get access to value's source
       * */
      public final Symbol getExtenalName(){return this.externalName;}
      /**
       * <mutator>
       * To setup the value's source
       * */
      public final void setExtrenalName(Symbol source){this.externalName=source;}
      /**
       * <accessor>
       * To get access to actual parameter's local Symbol
       * @return
       */
      public final Symbol getLocalSymbol(){return this.externalName.isConst()?this.name:this.externalName;}
      /**
       * <accessor>
       * to get access to parameter's type for XML (actual/formal)
       * */
      protected final String parameterType(){return Actual.TYPE;}
      /**
       * <translate>
       * To translate the parameter to XML
       * */
      public final Element getXML()
      {
        Element xml = super.getXML();
        xml.addContent( new Property("source",this.externalName).getXML() );
        return xml;
      }
      /**
       * <translator>
       * To restore the parameter from XML
       * */
      public final void setXML(Element xml) throws Exception
      {
        super.setXML( xml );
        // to define the array of Properties name
        ArrayList names = new ArrayList( 1 );// for check a duplicates
        for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();)
        {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of base properties!");
          // to solve the names of property
          if ( "source".equals( name ) ) {
            this.externalName = property.getValue( this.externalName );names.add(name);
          }
        }
      }
  }
/**
 * <producer>
 * To restore parameter from xml
 * */
public static Parameter restore(Element xml) throws Exception
{
  if ( xml == null || !ELEMENT.equals(xml.getName()) ) return null;
  String type = xml.getAttributeValue("type");
  Parameter param = null;
  if ( Formal.TYPE.equals(type) )
  {
    param = new Formal();
  }else
  if ( Actual.TYPE.equals(type) )
  {
    param = new Actual();
  }
  if ( param != null) param.setXML(xml);
  return param;
}
  /**
   * <constructor>
   * */
  public Parameter(String name)
  {
    if (name != null) this.name = Symbol.newLocal(name,Symbol.STRING);
    else this.name = Symbol.newLocal("param",Symbol.STRING);
  }
  /**
   * Check, is object equals
   * @param o other object
   * @return result
   */
  public final boolean equals(Object o){
    if ( o instanceof Parameter )
    {
      Parameter other = (Parameter)o;
      return other.getStringName().equals(this.getStringName());
    }
    return super.equals(o);
  }
  /**
   * <setup>
   * To place to subroutine the value of parameter
   * */
  public void placeValue(Subroutine owner)
  {
    if (owner != null && this.value != null) owner.set(this.name,this.value);
  }
/**
 * <const>
 * The name of XML element
 * */
public final static String ELEMENT = "parameter";
  /**
   * <accessor>
   * to get access to parameter's type for XML (actual/formal)
   * */
  protected abstract String parameterType();
  /**
   * <translate>
   * To translate the parameter to XML
   * */
  public Element getXML()
  {
    Element xml = new Element(ELEMENT);
    xml.setAttribute(new Attribute("type",this.parameterType()));
    xml.addContent( new Property("name",this.name).getXML() );
    return xml;
  }
  /**
   * <translator>
   * To restore the parameter from XML
   * */
  public void setXML(Element xml) throws Exception
  {
    this.name.setName("");this.name.setType(Symbol.STRING);this.value=null;
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    String type = xml.getAttributeValue( "type" );
    if ( type == null || !type.equals(this.parameterType()) )
      throw new NullPointerException("Invalid type of parameter in XML declared");
    // to define the array of Properties name
    ArrayList names = new ArrayList( 1 );// for check a duplicates
    for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();)
    {
      Property property = new Property( (Element)i.next() );
      String name = property.getName();
      if (name == null) throw new Exception("Property without name!");
      if ( names.contains(name) )
        throw new Exception("Multiple definition of base properties!");
      // to solve the names of property
      if ( "name".equals( name ) ) {
        Symbol this_name = property.getValue( this.name );names.add(name);
        this.setType( this_name.getTypeID() );
        this.setName( this_name.getName() );
      }
    }
  }
/**
 * <attribute>
 * The name & type of parameter
 * */
protected Symbol name;
/**
 * <attribute>
 * The value of parameter
 * */
protected Object value = null;
  /**
   * <accessor>
   * To get access to not modifable parameter's name
   * */
  public final Symbol getName(){return this.name.copy();}
  /**
   * <accessor>
   * To get access to not modifable parameter's name (as String)
   * */
  public final String getStringName(){return this.name.getName();}
  /**
   * <accessor>
   * To get access to modifable parameter's name
   * */
  public final Symbol getNameNative(){return this.name;}
  /**
   * <mutator>
   * to change the parameters name
   * */
  public final void setName(String name){this.name.setName(name);}
  /**
   * <accessor>
   * To get access to parameter's type's ID
   * */
  public int getType() {return this.name.getTypeID();}
  /**
   * <mutator>
   * To change the type of the parameter
   * */
  public final void setType(int type)
  {
    if (type >= Symbol.STRING && type <= Symbol.MAX_TYPE)
    {
      String name = this.name.getName();
      this.name.setType( type );
      this.name.setName( name );
    }
  }
  /**
   * <accessor>
   * To get access to the value of parameter
   * */
  public final Object getValue() {return value;}
  /**
   * <mutator>
   * To store the value of parameter
   * */
  public final void setValue(Object value) {this.value = value;}
  /**
   * <translator>
   * To translate the parameter to the String
   * */
  public final String toString(){return this.name.cell();}
}
