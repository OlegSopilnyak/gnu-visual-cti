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
package org.visualcti.server.task;

import java.io.*;

import org.jdom.*;

/**
 * <task>
 * The stub for the industrial CTI-application
 *
 * @author Oleg Sopilnyak
 * @version 3.0.1
 */
public abstract class stubTask implements Task
{

/**
 * <producer>
 * To make a copy of the cti-application,
 * copy will placed to sandbox
 *
 * @return the clone(copy) of this task
 */
final public Object clone()
{
    try {
        return super.clone();
    } catch(CloneNotSupportedException ce) {
        throw new InternalError("Task.clone -> Unknown error");
    }
}

/**
 * <accessor>
 * Get access to XML presentation of Task
 * XML may contains the Task's parameters
 *
 * @return XML Element <task></task>
 *
 */
public Element getXML(){
    Element xml = new Element(Task.ELEMENT);
    xml.setAttribute( new Attribute("class",this.getClass().getName()) );
    return xml;
}

/**
 * <mutator>
 * Setting up new XML representation
 * of contents for Task
 * XML may contains Task's parameters
 *
 * @param xml The XML Element <task></task>
 */
public void setXML(Element xml)throws Exception
{
    if ( !Task.ELEMENT.equals(xml.getName()) )
      throw new Exception("The XML Element not a task");
    String taskClass = xml.getAttributeValue("class");
    if ( taskClass == null )
      throw new Exception("Classname not defined in XML");
}

/**
<attribute>
The name of the task
*/
protected transient String name = "Task stub";
/**
 * <accessor>
 * To receive a name of the task
 * the method carries information character and
 * is used at the moment of adjustment of the cti-application on
 * CT device
 *
 * @return The name of task
 */
final public String getName(){return this.name;}

/**
<attribute>
The name of the task
*/
protected transient String about = "Task stub";
/**
 * To receive the extended information on the cti-application
 * the method carries information character and
 * is used at the moment of adjustment of the cti-application on
 * CTI device
 *
 * @return The task's description
 */
final public String getAbout(){return this.about;}

/**
 * <action>
 * Method of start of execution of the cti-application,
 * is called by the scheduler of CT-device
 */
abstract public void execute();

/**
 * <action>
 * Method of stop of execution of the cti-application,
 * is called by the scheduler of CT-device.
 * The task's TimerTask, will be canceled.
 */
abstract public void stopExecute();

/**
<attribute>
The Environment (resources) of the Task
*/
protected Environment env;
/**
* <mutator>
* to attach environment to the task
*/
public void setEnv( Environment env )
{
    // to stop old TimerTask
    if (this.clock != null) this.clock.cancel();
    // to disconnect environment's streams
    this.out = null; this.err = null;
    // to process new environment
    if (env != null)
    {
        // to adjust the timer
        this.clock = new SystemClockSynchronize();
        java.util.Timer timer = null;
        try{timer = (java.util.Timer)env.getPart( "timer", java.util.Timer.class );
        }catch(Exception e){}
        // to notify the task every second (clock synchronization)
        if ( timer != null ) timer.schedule( this.clock, new java.util.Date(), 1000);
        // to connect environment's streams
        OutputStream output = null;
        // standart output
        try{output = (OutputStream)env.getPart("stdout",OutputStream.class);
        }catch(Exception e){}
        if ( output != null ) this.out = new PrintWriter(output,true);
        // errors output
        try{output = (OutputStream)env.getPart("stderr",OutputStream.class);
        }catch(Exception e){}
        if ( output != null ) this.err = new PrintWriter(output,true);
    }
    // to store reference to environment
    this.env = env;
}

/**
<attribute>
*   Temporary variable for preservation of time synchronization
*/
private transient SystemClockSynchronize clock = null;
/**
<attribute>
*   Temporary variable for preservation of standard
*   cti-application output
*/
private transient PrintWriter out = new PrintWriter(System.out,true) ;
        /**
        <printer>
        * Method for a print from your cti-application standard
        * or debugging messages, you can call this method on demand
        * from the your cti-application.
        */
        protected final void debug(String message){this.out.println(message);}
/**
<attribute>
*   Temporary variable for preservation of cti-application
*   error messages
*/
private transient PrintWriter err = new PrintWriter(System.err,true) ;
        /**
        <printer>
        * Method for a print of error messages of the your cti-application,
        * you can call this method on demand from the your cti-application.
        */
        protected final void error(String message){this.err.println(message);}
        /**
        <printer>
        * Method for print an exception's stack trace to stderr,
        * you can call this method on demand from the your cti-application.
        */
        protected final void exception(Throwable t){t.printStackTrace(err);}
/**
 <action>
 *   Is called at end of the cti-application
*/
public synchronized void finalize() throws Throwable
{
    if (this.clock != null) this.clock.cancel();
    super.finalize(); this.clock=null;
}
/**
<event_processor>
* Notification from SystemClockSynchronize
* You can override it
*/
protected void clockEvent(){}
/**
<timer>
Class for realize TimerTask
*/
private final class SystemClockSynchronize extends java.util.TimerTask
{
    /** There has occurred the necessary time */
    public final void run(){stubTask.this.clockEvent();}
}
}
