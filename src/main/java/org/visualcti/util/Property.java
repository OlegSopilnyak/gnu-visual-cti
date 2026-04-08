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
package org.visualcti.util;

import java.util.Date;
import java.util.StringTokenizer;
import java.awt.Point;

import org.jdom.*;
import org.apache.soap.encoding.soapenc.Base64;

import org.visualcti.briquette.*;
/**
Class for store/restore(XML) of operation's property
*/
public class Property
{
/**
<const>
property XML element name
*/
public static final String ELEMENT = "property";
/**
<const>
type of value - briquette's Symbol
*/
public static final String SYMBOL  = "symbol";
/**
<const>
type of value - String
*/
public static final String STRING  = "string";
/**
<const>
type of value - Number
*/
public static final String NUMBER  = "number";
/**
<const>
type of value - Date
*/
public static final String DATE    = "date";
/**
<const>
type of value - Boolean
*/
public static final String BOOLEAN = "boolean";
/**
<const>
type of value - Point
*/
public static final String POINT = "point";
/**
<const>
type of value - byte[]
*/
public static final String RAWDATA = "bytes";
        /**
        <constructor>
        Constructor for make property (base)
        */
        private Property(String name,Object value)
        {
            this.name = name;this.value=value;
        }
        /**
        <constructor>
        empty, for reconstruction
        */
        protected Property(){this.name="";this.value=new Object();this.type="";}
    /**
    <attribute>
    the name of property
    */
    private String name;
    /**
    <accessor>
    to get access to name of property
    */
    public final String getName(){return this.name;}
    /**
     * <mutator>
     * To setup new name
     * @param name new name
     */
    public final void setName(String name) {this.name = name;}
    /**
    <attribute>
    the type of property's value
    */
    private String type = STRING;
    /**
    <accessor>
    to get access to type of property's value
    */
    public final String getType(){return this.type;}
    /**
    <attribute>
    the value of property
    */
    private Object value;
    /**
    <accessor>
    to get access to value of property
    */
    public final Object getValue(){return this.value;}
    /**
     * <mutator>
     * To setup new value
     * @param value new value
     */
    public final void setValue(Symbol value) {this.value = value;this.type=Property.SYMBOL;}
    /**
    <accessor>
    to get access to element's name
    */
    protected String xmlName(){return ELEMENT;}
    /**
    <constructor>
    name and value (briquette's Symbol value)
    */
    public Property(String name,Symbol value){this(name,(Object)value);this.type=SYMBOL;}
    /**
    <constructor>
    name and value (String value)
    */
    public Property(String name,String value){this(name,(Object)value);this.type=STRING;}
    /**
    <constructor>
    name and value (Number value)
    */
    public Property(String name,Number value){this(name,(Object)value);this.type=NUMBER;}
    /**
    <constructor>
    name and value (int value)
    */
    public Property(String name,int value){this(name,new Integer(value));}
    /**
    <constructor>
    name and value (long value)
    */
    public Property(String name,long value){this(name,new Long(value));}
    /**
    <constructor>
    name and value (double value)
    */
    public Property(String name,double value){this(name,new Double(value));}
    /**
    <constructor>
    name and value (Date value)
    */
    public Property(String name,Date value){this(name,(Object)value);this.type=DATE;}
    /**
    <constructor>
    name and value (Boolean value)
    */
    public Property(String name,Boolean value){this(name,(Object)value);this.type=BOOLEAN;}
    /**
    <constructor>
    name and value (boolean value)
    */
    public Property(String name,boolean value){this(name,new Boolean(value));}
    /**
    <constructor>
    name and value (Point value)
    */
    public Property(String name,Point value){this(name,(Object)value);this.type=POINT;}
    /**
    <constructor>
    name and value (byte[] value)
    */
    public Property(String name,byte[] value){this(name,(Object)value);this.type=RAWDATA;}
    /**
    <accessor>
    get parameter's value as briquette's Symbol
    */
    public final Symbol getValue(Symbol defaultValue){
        if ( SYMBOL.equals(this.type) )
            try {
                return (Symbol)this.value;
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as String
    */
    public final String getValue(String defaultValue){
        if ( STRING.equals(this.type) )
            try {
                return (String)this.value;
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as int
    */
    public final int getValue(int defaultValue){
        if ( NUMBER.equals(this.type) )
            try {
                return ((Number)this.value).intValue();
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as long
    */
    public final long getValue(long defaultValue){
        if ( NUMBER.equals(this.type) )
            try {
                return ((Number)this.value).longValue();
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as double
    */
    public final double getValue(double defaultValue){
        if ( NUMBER.equals(this.type) )
            try {
                return ((Number)this.value).doubleValue();
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as Date
    */
    public final Date getValue(Date defaultValue){
        if ( DATE.equals(this.type) )
            try{
                return (Date)this.value;
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as boolean
    */
    public final boolean getValue(boolean defaultValue){
        if ( BOOLEAN.equals(this.type) )
            try{
                return ((Boolean)this.value).booleanValue();
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as Point
    */
    public final Point getValue(Point defaultValue){
        if ( POINT.equals(this.type) )
            try{
                return ((Point)this.value);
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    get parameter's value as byte[]
    */
    public final byte[] getValue(byte[] defaultValue) throws Exception
    {
        if ( RAWDATA.equals(this.type) )
            try{
                return (byte[])this.value;
            }catch(Exception e){}
        return defaultValue;
    }
    /**
    <accessor>
    to get string representation of property
    */
    public String toString(){
        return this.name+"("+this.type+")="+this.value;
    }
    /**
    <transport>
    to present property as XML element
    */
    public Element getXML() {
        // to make the XML element
        Element xml = new Element( this.xmlName() );
        // to setup attributes
        xml.setAttribute( new Attribute("name",this.name) );
        xml.setAttribute( new Attribute("type",this.type) );

        String stringValue = "";
        // to store Point
        if (POINT.equals(this.type)) {
            Point coord = (Point)this.value;
            Element point = new Element("coord");
            point.setAttribute(new Attribute("X",""+coord.getX()));
            point.setAttribute(new Attribute("Y",""+coord.getY()));
            return xml.addContent( point );
        } else
        // to store Date object
        if (DATE.equals(this.type))    stringValue += ((Date)this.value).getTime();
        else
        // to store raw data array
        if (RAWDATA.equals(this.type)) stringValue += Base64.encode((byte[])this.value);
        else stringValue += this.value;
        // to store the value
        return xml.setText( stringValue );
    }
    /**
    <transport>
    to restore parameter from XML
    */
    public Property setXML(Element xml) throws Exception
    {
        if ( xml == null ||
             !this.xmlName().equals(xml.getName())
            ) throw new Exception("Invalid XML element");
        // to solve the attributes
        this.name = xml.getAttributeValue("name");
        this.type = xml.getAttributeValue("type");
        String value = xml.getTextNormalize();
        // to solve the value by type
        if ( SYMBOL.equals(this.type)  ) this.value = Symbol.fromString(value);
        else
        if ( STRING.equals(this.type)  ) this.value = value.toString();
        else
        if ( NUMBER.equals(this.type)  ) this.value = new Double( value );
        else
        if ( DATE.equals(this.type)    ) this.value = new Date(Long.parseLong(value));
        else
        if ( BOOLEAN.equals(this.type) ) this.value = new Boolean( value );
        else
        if ( POINT.equals(this.type) ) {
            Element point = xml.getChild("coord");
            String
            x = point.getAttributeValue("X"),
            y = point.getAttributeValue("Y");
            int X = Double.valueOf(x).intValue();
            int Y = Double.valueOf(y).intValue();
            this.value = new Point(X,Y);
        } else
        if ( RAWDATA.equals(this.type) ) this.value = Base64.decode( value );
        else {this.type = STRING;        this.value = value.toString();}
        // to return the reference to himself
        return this;
    }
    /**
    <constructor>
    from XML element
    */
    public Property(Element xml) throws Exception {this.setXML( xml );}
}
