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
Command for execution by a serverUnit
*/
public final class unitCommand extends unitAction
{
    static final long serialVersionUID = -7702307690319924854L;
    /**
    <accessor>
    to get access to action's class
    attribute from enumeration {ERROR,EVENT,COMMAND}
    will use for store/restore methods.
    Will by defined in Action's implementation as final method
    */
    final public short actionClass(){return unitAction.COMMAND;}
    /**
    <accessor>
    get type of action
    */
    final protected String getType(){return "command";}
/** counter for dispached sequence */
private static int sequenceCounter = 1;
    /**
    <producer>
    to made new sequenceID
    */
    final protected int nextSequenceID()
    {
        int nextID=-1;
        synchronized(unitCommand.class){nextID = sequenceCounter++;}
        return nextID;
    }
    /**
    Overrided Object.toString()
    */
    final public String toString()
    {
        String desc = this.getDescription();
        String result=
                "Command: ["+
                this.actionByID()+
                ":"+this.sequenceID()+
                "] for "+this.getUnitPath()+
                ("".equals(desc)? "":" ("+desc+")")+
                ". via link ["+this.link+"] "+
                "To wait for response? "+this.needResponse;
        result += "\nparameter(s)\n{\n";
        for(Iterator i=this.parameters();i.hasNext();) result +="\t"+i.next()+"\n";
        result += "}\n";
        result += ">done:"+this.done+"\n";
        result += ">success:"+this.success+"\n";
        return result;
    }
/**
<container>
pool of parameters
*/
private transient final HashMap pool = new HashMap();
    /**
    <mutator>
    to set parameter to command
    */
    public final unitCommand set(Parameter parameter){
        String parName = parameter.getName();
        if (parName == null || parName.startsWith("@")) return this;
        this.pool.put(parName, parameter);// to store parameter in the pool
        return this;
    }
    /**
    <mutator>
    to set parameters set to command
    */
    public final unitCommand set(Parameter[] pars) {this.pool.clear();
        for(int i=0;i < pars.length;i++) this.set( pars[i] );
        return this;
    }
    /**
    <accessor>
    get parameter by name
    */
    public final Parameter getParameter(String name){
        return (Parameter)this.pool.get(name);
    }
    /**
    <accessor>
    get parameters set
    */
    public final Iterator parameters(){return this.pool.values().iterator();}
/**
<attribute>
flag, is command need response
*/
private transient boolean needResponse = false;
    /**
    <accessor>
    is need Command response
    */
    public final boolean isNeedResponse() {return this.needResponse;}
    /**
    <mutator>
    to setting up new response flag
    */
    public final unitCommand setNeedResponse(boolean needs){
        this.needResponse=needs; return this;
    }
/**
<flag>
command was executed
*/
private transient volatile boolean done = false;
    /**
    <accessor>
    Check, command was executed?
    */
    public final boolean isDone(){return this.done;}
    /**
    <action> to assign response to this command
    */
    public final void setResponse(unitResponse response)
    {
        this.success = response.isCommandSuccess();
        if ( !this.needResponse ) return;// no needs to process response
        if (response.isCommandSuccess())
        {   // to copy parameters from response to command
            for(Iterator i=response.results();i.hasNext();) {
                this.set( ((Parameter)i.next()).output() );
            }
        } else {// save response's description, as error's description
            this.pool.clear();
            Parameter err = new Parameter("@error",response.getDescription());
            this.pool.put("@error", err.output());// to store parameter in the pool
        }
        this.done = true;
        // To free the command's semaphore ;)
        synchronized(this){this.notify();}
    }
/**
<flag> is command executed well?
*/
private transient boolean success = false;
    /**
    <accessor>
    Check, is command was executed successful?
    */
    public final boolean isSuccessful(){return this.success;}
    /**
    <mutator>
    Set command executed successful flag
    */
    public final void setSuccessful(boolean flag){this.success=flag;}
/**
<attirbute>
Name of Link, the owner of command's transport
*/
private transient String link = "Unknown";
    /**
    <accessor>
    get name of link, owner of command
    */
    public final String getLink(){return this.link;}
    /**
    <mutator>
    set name of link, owner of command
    */
    public final unitCommand setLink(String link){this.link=link;return this;}

    /**
    <constructor>
    empty constructor of command
    */
    public unitCommand(){super();}
   /**
   <constructor>
   make command for serverUnit, with actionID
   */
   public unitCommand(serverUnit unit,short ID){this(unit,ID,"");}
   /**
   <constructor>
   make command for serverUnit, with actionID and description
   */
   public unitCommand
                (
                serverUnit unit,
                short ID,
                String description
                )
    {
        this(unit.getPath(),ID,description);
    }
   /**
   <constructor>
   make command for serverUnit path, with actionID and description
   */
   public unitCommand
                (
                String unitPath,
                short ID,
                String description
                )
    {   this();
        this.setUnitPath(unitPath);
        this.setDescription(description);
        this.setID(ID);
    }
/**
parameter name for need response parameter
*/
private static final String RESPONSE = "@need_response";
/**
parameter name for link
*/
private static final String LINK = "@link";
/**
The name of parameter
is command executed well
*/
private final static String SUCCESS = "@success";
/**
The name of parameter
is command was executed
*/
private final static String DONE = "@done";
    /**
    <converter>
    to represent action as XML element
    */
    public final Element getXML()
    {
        Element xml = super.getXML();
        Parameter
        par = new Parameter(RESPONSE,this.needResponse).input();
        xml.addContent( par.getXML() );
        par = new Parameter(LINK,this.link).input();
        xml.addContent( par.getXML() );
        par = new Parameter(SUCCESS,this.success).input();
        xml.addContent( par.getXML() );
        par = new Parameter(DONE,this.done).input();
        xml.addContent( par.getXML() );
        for(Iterator i=this.parameters();i.hasNext();){
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
        Iterator i = xml.getChildren( Parameter.ELEMENT ).iterator();
        this.needResponse = false; this.link = "Unknown";
        while( i.hasNext() ) {
            try{
                Parameter par = Parameter.restore((Element)i.next());
                String name = par.getName();
                if (name == null) continue;// invalid name attribute
                else
                if ( name.startsWith("@") ) {   // Reserved parameter
                    if (name.equals(RESPONSE)) this.needResponse = par.getBooleanValue();
                    else
                    if (name.equals(LINK)) this.link = par.getStringValue();
                    else
                    if (name.equals(SUCCESS)) this.success = par.getBooleanValue();
                    else
                    if (name.equals(DONE)) this.done = par.getBooleanValue();
                }else{// command's parameters
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
