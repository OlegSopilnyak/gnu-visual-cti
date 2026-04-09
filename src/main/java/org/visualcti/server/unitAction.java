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

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.visualcti.server.action.serverAction;
import org.visualcti.util.Tools;
/**
action of serverUnit
parent of all in/out unit atoms
*/
public abstract class unitAction implements serverAction
{
    static final long serialVersionUID = -83294122927484246L;
    /**
    <constructor>
    empty constructor
    */
    public unitAction(){this.sequenceID();}
/**
<accessor>
to get access to action's class
attribute from enumeration {ERROR,EVENT,COMMAND}
will use for store/restore methods.
Will by defined in Action's implementation as final method
*/
abstract public short actionClass();

/**
<attribute>
event ID
*/
private transient short ID = -2;
    /**
    <accessor>
    to get action's ID (type)
    */
    public final short getID(){return this.ID;}
    /**
    <mutator>
    to setting up action's ID (type)
    */
    public final void setID(short ID){this.ID=ID;}

/**
<attribute>
event description
*/
private transient String description="";
    /**
    <accessor>
    to get action's description
    */
    public final String getDescription(){return this.description;}
    /**
    <mutator>
    to setting up action's description
    */
    public final void setDescription(String description){this.description=description;}

/**
<attribute>
date of delivery event
*/
private transient long date = new Date().getTime();
    /**
    <accessor>
    The date and time, when happened action
    serverAction producer must setting up this attribute
    */
    public final Date getDate(){return new Date(this.date);}
    /**
    <mutator>
    to set date of action
    ClassValidationException
    */
    public final void setDate(Date date){this.date=date.getTime();}

/**
<attribute>
the action's sequence ID
*/
private transient int seqID = -1;
    /**
    <accessor>
    to get action's sequence ID
    serverAction producer must setting up this attribute
    */
    public final int sequenceID()
    {
        if (this.seqID <= 0) this.seqID = this.nextSequenceID();
        return this.seqID;
    }
/**
<producer>
to made new sequenceID
*/
abstract protected int nextSequenceID();

/**
<attribute>
the path to unit
*/
private transient String unitPath="";
    /**
    <accesor>
    to get access to Path to serverUnit in UnitRegistry
    when action will constucted, in constructor will
    transferred serverObject reference
    */
    public final String getUnitPath(){return this.unitPath;}
    /**
    <mutator>
    to setting up the path to source
    */
    public final void setUnitPath(String path){this.unitPath=path;}

/**
Mapping strin action to int
*/
private static final HashMap actionID = new HashMap();
// actionID initialization
static
{
    actionID.put(new Short(ERROR_ID),   "error");
    actionID.put(new Short(STOP_ID),    "stop");
    actionID.put(new Short(START_ID),   "start");
    actionID.put(new Short(STATE_ID),   "state");
    actionID.put(new Short(GET_ID),     "get");
    actionID.put(new Short(SET_ID),     "set");
}
        /**
        <accessor>
        get String from action's ID
        */
        protected final String actionByID() {
            Short key = new Short(this.ID);
            String action = (String)actionID.get(key);
            return action == null ? "???":action;
        }
        /**
        <mutator>
        to setting up ne action's ID from
        XML action attribute
        */
        protected final void setID(String action)
        {
            for(Iterator i = actionID.entrySet().iterator();i.hasNext();)
            {
                Map.Entry entry = (Map.Entry)i.next();
                if (entry.getValue().equals(action)) {
                    this.ID = ((Short)entry.getKey()).shortValue();
                    break;
                }
            }
        }
/**
<accessor>
to get access to the type of action
*/
abstract protected String getType();
/**
<parameter_name>
The name of description's attribute in serialization parameters
*/
private final static String DECRIPTION = "@description";
/**
 * <const>
 * The name of root's Element in xml
 */
public static final String ELEMENT = "unitAction";
    /**
    <converter>
    to represent action as XML element
    */
    public Element getXML()
    {
      Element xml = new Element(ELEMENT);
      //xml.addContent( new Comment("Server unit event dispatched") );
      xml.addContent( this.baseXML() );
      if ( !"".equals(this.description) )
      {
        Parameter descr = new Parameter(DECRIPTION,this.description);
        xml.addContent( descr.input().getXML() );
      }
      return xml;
    }
      /*<producer> to make the base part of an unitAction's XML */
      private final Element baseXML(){
        Element xml = new Element("base");
        xml.setAttribute(new Attribute("action",this.actionByID()));
        xml.setAttribute(new Attribute("type",this.getType()));
        xml.setAttribute(new Attribute("date",String.valueOf(this.date)));
        xml.setAttribute(new Attribute("id","_"+this.sequenceID()));
        xml.setAttribute(new Attribute("unitPath",this.unitPath));
        return xml;
      }
      private final void baseXML(Element xml)throws IOException,
                                                    DataConversionException,
                                                    NumberFormatException,
                                                    NullPointerException {
        if ( xml == null )
          throw new IOException("Invalid unitAction's base part.");
        // to check the action's type
        String type = this.getType();
        if ( !type.equals(xml.getAttributeValue("type")) )
          throw new IOException("XML document not for the "+type);
        this.ID = -2;
        String action = xml.getAttributeValue("action");
        this.setID( action );
        if ( this.ID == -2 )
          throw new IOException("Invalid unitAction action ["+action+"]");
        this.date = xml.getAttribute("date").getLongValue();
        String ID = xml.getAttributeValue("id").substring(1);
        this.seqID = Integer.parseInt(ID);
        this.unitPath = xml.getAttributeValue("unitPath");


      }
    /**
    <converter>
    to translate form XML to action's fields
    */
    public void setXML(Element xml)
                                throws  IOException,
                                        DataConversionException,
                                        NumberFormatException,
                                        NullPointerException
    { // to restore the base properties
      this.baseXML( xml.getChild("base") );
      // to restore the description
      this.description = ""; Parameter par = null;
      for(Iterator i = xml.getChildren(Parameter.ELEMENT).iterator();i.hasNext();)
      {
        try {par = Parameter.restore((Element)i.next());
        }catch(Exception e){}
        if ( par != null && DECRIPTION.equals(par.getName()) )
        { // to get the description,
          // if some wrong description will be "Invalid action's description!"
          this.description = par.getValue("Invalid action's description!");
          break;
        }else par = null;
      }
      par = null;
    }
/**
Overrided Object.toString()
*/
abstract public String toString();
    /**
    <transport>
    to store action's attributes to DataOutput
    will be used for transport objects
    */
    public final void store(OutputStream out) throws IOException
    {
        Document doc = new Document( this.getXML() );
        //doc.setDocType(new DocType("serverAction","serverAction.dtd"));
        XMLOutputter outputter = new XMLOutputter();
        outputter.setTextNormalize( false );
        outputter.setLineSeparator( "" );
        outputter.setNewlines( false );
        outputter.setIndent( "" );
        outputter.setIndent( false );
        OutputStreamWriter stream = new OutputStreamWriter(out);
        outputter.output(doc, stream); stream.flush();
    }

    /**
    <transport>
    to restore action's attributes from DataInput
    will be used for transport objects
    */
    public final void restore(InputStream in) throws IOException
    {
        Document doc=null;
        // to create builder for solve XML ini-file
        try {doc =  new SAXBuilder().build( in );
        }catch(JDOMException e){
            e.printStackTrace(Tools.err);
            throw new IOException(e.getMessage());
        }
        try {this.setXML( doc.getRootElement() );
        }catch(NullPointerException e) {
            e.printStackTrace(Tools.err);
            throw new IOException("Invalid XML");
        }catch(NumberFormatException e) {
            e.printStackTrace(Tools.err);
            throw new IOException("Invalid XML");
        }catch(DataConversionException e) {
            e.printStackTrace(Tools.err);
            throw new IOException("Invalid XML");
        }
    }
/**
<producer>
to make unitAction object from stream
*/
public static final unitAction Restore(InputStream in) throws IOException
{
    Document doc=null; unitAction result = null;
    // to create builder for solve XML ini-file
    try {doc =  new SAXBuilder().build( in );
    }catch(JDOMException e){
        e.printStackTrace(Tools.err);
        throw new IOException(e.getMessage());
    }
    try {return solve( doc.getRootElement() );
    }catch(NullPointerException e) {
        e.printStackTrace(Tools.err);
        throw new IOException("Invalid XML");
    }catch(NumberFormatException e) {
        e.printStackTrace(Tools.err);
        throw new IOException("Invalid XML");
    }catch(DataConversionException e) {
        e.printStackTrace(Tools.err);
        throw new IOException("Invalid XML");
    }
}
/**
<producer>
to solve the xml
*/
private static unitAction solve(Element xml)
                            throws  IOException,
                                    DataConversionException,
                                    NumberFormatException
{
    unitAction result = null;
    Element base = xml.getChild("base");
    if ( base == null ) throw new IOException("Invalid [base] part in the unitAction's XML.");
    String type = base.getAttributeValue("type");
    // to make the action by type
    if ( "event".equals(type) ) result = new unitEvent();
    else
    if ( "error".equals(type) ) result = new unitError();
    else
    if ( "command".equals(type) ) result = new unitCommand();
    else
    if ( "response".equals(type) ) result = new unitResponse();
    // to restore the action
    if (result != null) result.setXML(xml);
    return result;
}
        /**
        Called, during deserialization
        */
        private final void readObject
                                (
                                java.io.ObjectInputStream stream
                                )
                                throws IOException, ClassNotFoundException
        {
            stream.defaultReadObject();
            this.restore( stream );// to restore action from XML
        }
        /**
        Called durin serialization
        */
        private final void writeObject
                                (
                                java.io.ObjectOutputStream stream
                                )
                                throws IOException
        {
            stream.defaultWriteObject();
            this.store(stream);// to save action as XML
        }
}
