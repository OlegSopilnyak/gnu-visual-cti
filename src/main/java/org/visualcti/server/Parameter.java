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
package org.visualcti.server;

import java.util.Date;
import java.util.Objects;
import org.apache.soap.encoding.soapenc.Base64;
import org.jdom.Attribute;
import org.jdom.Element;

/**
Class for represent parameter of command
or return value of response
*/
public class Parameter
{
/** The name of XML's root */
public static final String ELEMENT = "parameter";
/** type of value - XML's Element */
public static final String XML  = "xml";
/** type of value - String */
public static final String STRING  = "string";
/** type of value - Number */
public static final String NUMBER  = "number";
/** type of value - Date */
public static final String DATE    = "date";
/** type of value - Boolean */
public static final String BOOLEAN = "boolean";
/** type of value - byte[] */
public static final String RAWDATA = "bytes";
/**
<attribute>
the parameter name
*/
private transient String name;
    /**
    <accessor>
    get name of parameter
    */
    public final String getName(){return this.name;}
/**
<attribute>
type of value
*/
private transient String type;
    /**
    <accessor>
    get type of parameter
    */
    public final String getType(){return this.type;}
/**
<attribute>
direction of parameter
*/
private transient String direction = "input";
    /**
    <accessor>
    is this is input parameter
    */
    public final boolean isInput(){return "input".equals(this.direction);}
    /**
    <accessor>
    is this is output parameter
    */
    public final boolean isOutput(){return "output".equals(this.direction);}
    /**
    <mutator>
    set parameter as input
    */
    public final Parameter input(){this.direction="input";return this;}
    /**
    <mutator>
    set parameter as input
    */
    public final Parameter output(){this.direction="output";return this;}
/**
<attribute>
value
*/
private transient Object value;
    /**
    <accessor>
    get parameter's value as Object
    */
    public Object getValue(){return this.value;}
    /**
    <accessor>
    get parameter's value as XML
    */
    public final Element getXmlValue() throws Exception
    {
        if ( !XML.equals(this.type) ) throw new Exception("can't convert");
        return (Element)this.value;
    }
    public final Element getValue(Element value)
    {
      try{
        return this.getXmlValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as String
    */
    public String getStringValue() throws Exception
    {
        if ( !STRING.equals(this.type) ) throw new Exception("can't convert");
        return (String)this.value;
    }
    public final String getValue(String value)
    {
      try{
        return this.getStringValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as int
    */
    public int getIntValue() throws Exception
    {
        if ( !NUMBER.equals(this.type) ) throw new Exception("can't convert");
        return ((Number)this.value).intValue();
    }
    public final int getValue(int value)
    {
      try{
        return this.getIntValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as long
    */
    public long getLongValue() throws Exception
    {
        if ( !NUMBER.equals(this.type) ) throw new Exception("can't convert");
        return ((Number)this.value).longValue();
    }
    public final long getValue(long value)
    {
      try{
        return this.getLongValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as double
    */
    public double getDoubleValue() throws Exception
    {
        if ( !NUMBER.equals(this.type) ) throw new Exception("can't convert");
        return ((Number)this.value).doubleValue();
    }
    public final double getValue(double value)
    {
      try{
        return this.getDoubleValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as Date
    */
    public Date getDateValue() throws Exception
    {
        if ( !DATE.equals(this.type) ) throw new Exception("can't convert");
        return (Date)this.value;
    }
    public final Date getValue(Date value)
    {
      try{
        return this.getDateValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as boolean
    */
    public boolean getBooleanValue() throws Exception
    {
        if ( !BOOLEAN.equals(this.type) ) throw new Exception("can't convert");
        return ((Boolean)this.value).booleanValue();
    }
    public final boolean getValue(boolean value)
    {
      try{
        return this.getBooleanValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <accessor>
    get parameter's value as byte[]
    */
    public byte[] getBytesValue() throws Exception
    {
        if ( !RAWDATA.equals(this.type) ) throw new Exception("can't convert");
        return (byte[])this.value;
    }
    public final byte[] getValue(byte[] value)
    {
      try{
        return this.getBytesValue();
      }catch(Exception e){
        return value;
      }
    }
    /**
    <constructor> empty, for reconstruction
    */
    private Parameter(){this.name="";this.value=new Object();this.type="";}
    /**
    <constructor>
    name and value (XML value)
    */
    public Parameter(String name,Element value)
    {
        this.name=name; this.value=value.clone();this.type=XML;
    }
    /**
    <constructor>
    name and value (String value)
    */
    public Parameter(String name,String value)
    {
        this.name=name; this.value=value;this.type=STRING;
    }
    /**
     <builder>
     To make the parameter from parameter name and value (String value)
     */
    public static Parameter of(final String name, final String value) {
        return new Parameter(name, value);
    }
    /**
    <constructor>
    name and value (Number value)
    */
    public Parameter(String name,Number value)
    {
        this.name=name; this.value=value;this.type=NUMBER;
    }
    /**
    <constructor>
    name and value (int value)
    */
    public Parameter(String name,int value)
    {
        this(name,new Integer(value));
    }
    /**
    <constructor>
    name and value (long value)
    */
    public Parameter(String name,long value)
    {
        this(name,new Long(value));
    }
    /**
    <constructor>
    name and value (int value)
    */
    public Parameter(String name,double value)
    {
        this(name,new Double(value));
    }
    /**
    <constructor>
    name and value (Date value)
    */
    public Parameter(String name,Date value)
    {
        this.name=name; this.value=value;this.type=DATE;
    }
    /**
    <constructor>
    name and value (Boolean value)
    */
    public Parameter(String name,Boolean value)
    {
        this.name=name; this.value=value;this.type=BOOLEAN;
    }
    /**
    <constructor>
    name and value (boolean value)
    */
    public Parameter(String name,boolean value)
    {
        this(name,new Boolean(value));
    }
    /**
    <constructor>
    name and value (byte[] value)
    */
    public Parameter(String name,byte[] value)
    {
        this.name=name; this.value=value;this.type=RAWDATA;
    }
    /**
    to restore parameter from XML
    */
    public static Parameter restore(Element xml) throws Exception
    {
        if ( xml == null || !ELEMENT.equals(xml.getName()) ) return null;
        return new Parameter().setXML( xml );
    }
    /**
    <transport>
    to present parameter as XML
    */
    public Element getXML()
    {
        // to make the XML element "parameter"
        Element xml = new Element(ELEMENT);
        xml.setAttribute(new Attribute("name",this.name));
        xml.setAttribute(new Attribute("type",this.type));
        xml.setAttribute(new Attribute("direction",this.direction));
        StringBuffer buffer = new StringBuffer();
        // to store Date object
        if (DATE.equals(this.type))    buffer.append( ((Date)this.value).getTime() );
        else
        // to store raw data array
        if (RAWDATA.equals(this.type)) buffer.append( Base64.encode((byte[])this.value) );
        // to store the xml
        if ( XML.equals(this.type) ) {
          Element xmlValue = (Element)this.value;
          if ( xmlValue != null ) {
            // to store the XML's name
            buffer.append( xmlValue.getName() );
            xml.setAttribute(new Attribute("root",xmlValue.getName()));
            xmlValue = (Element)xmlValue.clone();
            // to store the content
            xml.addContent( xmlValue );
            return xml;
          }
        }
        else buffer.append( this.value );
        xml.setText( buffer.toString() );
        //xml.setAttribute(new Attribute("value",buffer.toString()));
        return xml;
    }
    /**
    <transport>
    to restore parameter from XML
    */
    public Parameter setXML(Element xml) throws Exception
    {
        if ( xml == null || !ELEMENT.equals(xml.getName()) )
          throw new Exception("Invalid Parameter's XML!");
        // to solve the attributes
        this.name = xml.getAttributeValue("name");
        this.type = xml.getAttributeValue("type");
        String text = xml.getText();
        String value = xml.getAttributeValue("value");
        // to solve the value by type
        if ( XML.equals(this.type)  ) {
          String root = xml.getAttributeValue("root");
          if (root != null) {
            Element XML = xml.getChild( root );
            this.value = XML != null ? (Element)XML.clone():null;
          }
        }
        else
        if ( STRING.equals(this.type)  ) {
          if ( value == null ) this.value = text;
          else this.value = value;
        }
        else
        if ( NUMBER.equals(this.type)  ) {
          if ( value == null) this.value = new Double(text);
          else this.value = new Double(value);
        }
        else
        if ( DATE.equals(this.type)    ) {
          if ( value == null ) this.value = new Date(Long.parseLong(text));
          else this.value = new Date(Long.parseLong(value));
        }
        else
        if ( BOOLEAN.equals(this.type) ) {
          if ( value == null ) this.value = new Boolean(text);
          else this.value = new Boolean(value);
        }
        else
        if ( RAWDATA.equals(this.type) ) {
          if ( value == null ) this.value = Base64.decode(text);
          this.value = Base64.decode(value);
        }
        // unknown type, type is STRING
        else {this.type = STRING;        this.value = text;}
        // to solve the direction
        this.direction = xml.getAttributeValue("direction");
        // if direction not defined, in XML, parameter is input
        if ( !this.isInput() && !this.isOutput()) return this.input();
        return this;
    }
    /**
    Tp translate the parameter to the String
    like this:
    {=> name(type) = value}
    parameter's direction ("=>" input, "<=" output)
    */
    public final String toString()
    {
      StringBuffer buffer = new StringBuffer();
      return buffer
              .append("{")
              .append(this.isInput() ? "=>":"<=")
              .append(" ").append(this.name)
              .append("(").append(this.type).append(")")
              .append(" = ").append(this.value)
              .append("}").toString();
    }

    /**
     * Compares the argument to the receiver, and answers true
     * if they represent the <em>same</em> object using a class
     * specific comparison. The implementation in Object answers
     * true only if the argument is the exact same object as the
     * receiver (==).
     *
     * @param		o Object
     *					the object to compare with this object.
     * @return		boolean
     *					<code>true</code>
     *						if the object is the same as this object
     *					<code>false</code>
     *						if it is different from this object.
     * @see			#hashCode
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Parameter)) return false;
        Parameter parameter = (Parameter) o;
        return Objects.equals(name, parameter.name) &&
                Objects.equals(type, parameter.type) &&
                Objects.equals(direction, parameter.direction) &&
                Objects.equals(value, parameter.value);
    }

}
