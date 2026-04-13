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
Error, what occur inside of serverUnit
*/
public final class unitError extends unitAction
{
    static final long serialVersionUID = -3770313880580128052L;
    /**
    <accessor>
    to get access to action class's ID
    attribute from enumeration {ERROR,EVENT,COMMAND}
    will use for store/restore methods.
    Will by defined in Action's implementation as final method
    */
    final public short actionClass(){return unitAction.ERROR;}
    /**
    <accessor>
    get type of action
    */
    final protected String getType(){return "error";}
/** counter for dispached sequence */
private static int sequenceCounter = 1;
    /**
    <producer>
    to made new sequenceID
    */
    final protected int nextSequenceID()
    {
        int nextID=-1;
        synchronized(unitError.class){nextID = sequenceCounter++;}
        return nextID;
    }
    /**
    Override Object.toString()
    */
    final public String toString()
    {
        String desc = this.getDescription();
        return "Error"+":"+this.sequenceID()+
               " in "+ this.getUnitPath()+
                ("".equals(desc)? "":" ("+desc+")")+
                (this.reason == null?"":"\nException:"+this.reason.toString());
    }
    /**
    <constructor>
    empty for make default object
    */
    public unitError(){super();}
    /**
    <constructor>
    make error from serverUnit and description
    */
    public unitError(serverUnit unit,String description)
    {
        this(unit,null,description);
    }
    /**
    <constructor>
    make error from serverUnit and exception
    */
    public unitError(serverUnit unit,Exception reason)
    {
        this(unit,reason,"");
    }
    /**
    <constructor>
    make error from serverUnit, exception and description
    */
    public unitError
                (
                serverUnit unit,
                Exception reason,
                String description
                )
    {
        this(unit.getPath(),reason,description);
    }
    /**
    <constructor>
    make error from serverUnit path, exception and description
    */
    public unitError
                (
                String unitPath,
                Exception exception,
                String description
                )
    {
        this();
        this.setUnitPath(unitPath);
        this.reason = exception != null ? new unitException(exception):null;
        this.setDescription(description);
        this.setID( unitAction.ERROR_ID );
    }
    /**
    <converter>
    to represent action as XML element
    to save in xml nested exception
    */
    public final Element getXML()
    {
        Element xml = super.getXML();
        if (this.reason != null)
        {
          Parameter par = new Parameter(EXCEPTION,this.reason.toString());
          xml.addContent( par.input().getXML() );
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
        super.setXML(xml); this.reason = null; Parameter par = null;
        Iterator i = xml.getChildren(Parameter.ELEMENT).iterator();
        while( i.hasNext() )
        { // to restore the parameter
          try{par = Parameter.restore((Element)i.next());
          }catch(Exception e){}
          // to solve a restored parameter
          if ( par != null && EXCEPTION.equals(par.getName()) )
          {
            String stackTrace = par.getValue("Invalid stacktrace!");
            this.reason = new unitException( stackTrace );
            break;
          } else par = null;
        }
    }
/**
<attribute>
nested exception
*/
private transient unitException reason=null;
/**
<parameter_name>
*/
private final static String EXCEPTION = "@exception";
    /**
    <accessor>
    The nested exception
    */
    public Exception nested(){return this.reason;}
    /**
    class for represent nested exception
    */
    private static final class unitException extends Exception
    {
        private transient String stack = "";
        private transient String message="";
            private void processStack()
            {
                StringTokenizer st = new StringTokenizer(this.stack,"\n\r");
                this.message = st.nextToken();
            }
        public unitException(String description){
            this.stack = description; this.processStack();
        }
        public unitException(Throwable t)
        {
            StringWriter strwrt = new StringWriter();
            PrintWriter prt = new PrintWriter(strwrt,true);
            // save message to object attribute
            this.message = t.getMessage();
            // save stack to object's attribute
            t.printStackTrace(prt);prt.flush();prt.close();
            this.stack = strwrt.toString();
        }
        public String toString(){return this.stack;}
        public String getMessage() {return this.message;}
        public String getLocalizedMessage() {return getMessage();}
        public void printStackTrace()
        {
          this.printStackTrace(System.err);
        }
        public void printStackTrace(java.io.PrintStream s)
        {
          synchronized (s) {s.println(this.stack);}
        }
        public void printStackTrace(java.io.PrintWriter s)
        {
          synchronized (s) {s.println(this.stack);}
        }
    }
}
