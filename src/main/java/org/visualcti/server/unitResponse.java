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
/**
The Response at the Command, executed in serverUnit
*/
public final class unitResponse extends unitAction
{
    static final long serialVersionUID = -8317331964829569469L;
    /**
    <accessor>
    to get access to action's class
    attribute from enumeration {ERROR,EVENT,COMMAND}
    will use for store/restore methods.
    Will by defined in Action's implementation as final method
    */
    final public short actionClass(){return unitAction.RESPONSE;}
    /**
    <accessor>
    get type of action
    */
    final protected String getType(){return "response";}
/** counter for dispached sequence */
private static int sequenceCounter = 1;
    /**
    <producer>
    to made new sequenceID
    */
    final protected int nextSequenceID()
    {
        int nextID=-1;
        synchronized(unitResponse.class){nextID = sequenceCounter++;}
        return nextID;
    }
    /**
    Overrided Object.toString()
    */
    final public String toString()
    {
      StringBuffer buf = new StringBuffer(" Response to command: [");
      buf.append(this.actionByID()).append(":").append(this.correlationID)
         .append("] executed in ").append(this.getUnitPath());
      String desc = this.getDescription();
      if ( !"".equals(desc) ) buf.append("(").append(desc).append(")");
      buf.append(". via link [").append(this.link).append("]")
         .append(" Is executed well? ").append(this.success)
         .append("\nresult(s)\n{\n");
      for(Iterator i=this.results();i.hasNext();) buf.append("\t").append(i.next()).append("\n");
      return buf.append("}\n").toString();
    }
    /**
    <constructor>
    empty constructor for reconstruction
    */
    public unitResponse(){super();this.well();}
    /**
    <constructor>
    with related command and description
    */
    public unitResponse(unitCommand command,String description)
    {
        this();
        this.setID( command.getID() );
        this.setUnitPath( command.getUnitPath() );
        this.setDescription( description );
        this.setCorrelationID( command.sequenceID() );
        this.link = command.getLink();
    }
    /**
    <constructor>
    with related command
    */
    public unitResponse(unitCommand command){this(command,"");}
/**
<container>
pool of results
*/
private transient final HashMap pool = new HashMap();
    /**
    <mutator>
    to set parameter to command
    */
    public final unitResponse set(Parameter result){
        String parName = result.getName();
        if (parName == null || parName.startsWith("@")) return this;
        this.pool.put(parName, result);// to store parameter in the pool
        return this;
    }
    /**
    <mutator>
    to set parameters set to command
    */
    public final unitResponse set(Parameter[] results) {this.pool.clear();
        for(int i=0;i < results.length;i++) this.set( results[i] );
        return this;
    }
    /**
    <accessor>
    get parameter by name
    */
    public final Parameter getResult(String name){
        return (Parameter)this.pool.get(name);
    }
    /**
    <accessor>
    get parameters set
    */
    public final Iterator results() {return this.pool.values().iterator();}
/**
<attribute>
Related command's sequenceID
*/
private transient int correlationID = -2;
    /**
    <accessor>
    The response at a command with Command's SequenceID
    */
    public final int getCorrelationID(){return this.correlationID;}
    /**
    <mutator>
    To setting up relate command's sequence ID
    */
    public final unitResponse setCorrelationID(int ID)
    {
        this.correlationID = ID; return this;
    }
/**
<attribute>
flag, is command executed well
*/
private transient boolean success = true;
    /**
    <accessor>
    is command executed well
    */
    public final boolean isCommandSuccess(){return this.success;}
    /**
    <mutator>
    setting up command's success (well)
    */
    public final unitResponse well(){this.success=true;return this;}
    /**
    <mutator>
    setting up command's success (well)
    */
    public final unitResponse bad(){this.success=false;return this;}
/**
<attirbute>
Name of link, owner of command
*/
private transient String link = "Unknown";
    /**
    <accessor>
    get name of link, owner of command
    */
    public final String getLink(){return this.link;}
/**
The name of parameter
Related command's sequenceID
*/
private final static String RELATED = "@corellationID";
/**
The name of parameter
is command executed well
*/
private final static String SUCCESS = "@success";
/**
parameter name for link
*/
private static final String LINK = "@link";
    /**
    <converter>
    to represent action as XML element
    */
    public final Element getXML()
    {
        Element xml = super.getXML();
        Parameter
        par = new Parameter(RELATED,this.correlationID).input();
        xml.addContent( par.getXML() );
        par = new Parameter(SUCCESS,this.success).input();
        xml.addContent( par.getXML() );
        par = new Parameter(LINK,this.link).input();
        xml.addContent( par.getXML() );
        for(Iterator i=this.results();i.hasNext();) {
            xml.addContent( ((Parameter)i.next()).getXML() );
        }
        return xml;
    }
    /**
    <converter>
    to translate form XML to action's fields
    */
    public final void setXML(Element xml)
                                throws  IOException,
                                        DataConversionException,
                                        NumberFormatException
    {
        super.setXML(xml);
        Iterator i = xml.getChildren( "parameter" ).iterator();
        this.correlationID = -2; this.success=true; this.link="Unknown";
        while( i.hasNext() ) {
            try{
                Parameter par = Parameter.restore((Element)i.next());
                String name = par.getName();
                if (name == null) continue;// invalid name attribute
                else
                if ( name.startsWith("@") ) {   // Reserved parameter
                    if (name.equals(RELATED)) this.correlationID = par.getIntValue();
                    else
                    if (name.equals(SUCCESS)) this.success = par.getBooleanValue();
                    else
                    if (name.equals(LINK)) this.link = par.getStringValue();
                }else{// response's parameters
                    this.set( par );
                }
            }catch(IOException e){
                throw e;
            }catch(DataConversionException e){
                throw e;
            }catch(NumberFormatException e){
                throw e;
            }catch(Exception e){}
        }
    }
}
