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

import org.jdom.*;

/**
 * <task>
 * The interface of the industrial CTI-application
 *
 * @author Oleg Sopilnyak
 * @version 3.0.1
 */
public interface Task extends java.lang.Cloneable
{
/**
 * <const>
 * The name of XML element
 */
String ELEMENT = "task";
/// Block of calls of the information about the cti-application and ////
//// adjustment of the concrete cti-application ///
/**
 * <producer>
 * To make a copy of the cti-application,
 * copy will placed to sandbox
 *
 * @return the clone(copy) of this task
 */
Object clone();
/**
 * <accessor>
 * Get access to XML presentation of Task
 * XML may contains the Task's parameters
 *
 * @return XML Element <task></task>
 */
Element getXML();
/**
 * <mutator>
 * Setting up new XML representation
 * of contents for Task
 * XML may contains Task's parameters
 *
 * @param xml The XML Element <task></task>
 * @throws Exception throw if can's restore the task
 */
void setXML(Element xml) throws Exception;
/**
 * <accessor>
 * To receive a name of the cti-application
 * the method carries information character and
 * is used at the moment of adjustment of the cti-application on
 * CT device
 *
 * @return The name of task
 */
String getName();
/**
 * To receive the extended information on the cti-application
 * the method carries information character and
 * is used at the moment of adjustment of the cti-application on
 * CTI device
 *
 * @return The task's description
 */
String getAbout();

 /////// Block of calls used for adjustment and start of the cti-application /////////
/**
 * <action>
 * Method of start of execution of the cti-application,
 * is called by the scheduler of CT-device
 */
void execute();
/**
 * <action>
 * Method of stop of execution of the cti-application,
 * is called by the scheduler of CT-device.
 * The task's TimerTask, will be canceled.
 */
void stopExecute();
/**
* <mutator>
* to attach environment to task
* @param env the environment of the task
*/
void setEnv(Environment env);
/**
 <action>
 *   Is called at end of the cti-application
 * @throws Throwable throw if can't free the task's resources
 */
void finalize() throws Throwable;
}
