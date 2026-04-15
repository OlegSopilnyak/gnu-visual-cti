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
package org.visualcti.server.core.unit.executable;

import org.jdom.Element;
import org.visualcti.server.task.Environment;
import org.visualcti.server.task.TaskPool;

/**
 * <task>
 * The interface of the executable task of the industrial CTI-application
 *
 * @author Oleg Sopilnyak
 * @version 3.0.1
 */
public interface Task extends Cloneable {
    /**
     * <const>
     * The name of root XML element of the Task
     */
    String ROOT_ELEMENT = "task";

    /**
     * <producer>
     * To make the clone(copy) of the task instance,
     * cloned instance will be placed to the sandbox
     *
     * @return the clone(copy) of this task
     */
    Task clone();

/// Block of calls of the information about the task and ////
//// adjustment of the concrete task ///
    /**
     * <accessor>
     * To get the XML representation of the Task
     * XML should contain all the parameters of the Task
     *
     * @return XML Element <task></task>
     * @see Task#ROOT_ELEMENT
     * @see Element
     */
    Element getXML();

    /**
     * <mutator>
     * Setting up the XML representation of the Task
     * XML should contain all the parameters of the Task
     *
     * @param xml The XML Element <task></task>
     * @see Task#ROOT_ELEMENT
     * @see Element
     * @throws Exception throw if it can't restore the task from input parameter
     */
    void setXML(Element xml) throws Exception;

    /**
     * <accessor>
     * To get the name of the task which is used during
     * deployment the task to the particular CT-device channel
     *
     * @return The name of the task
     * @see TaskPool#add(Task, boolean)
     */
    String getName();

    /**
     * <accessor>
     * To get the expanded information (description) of the task which is used during
     * deployment the task to the particular CT-device channel
     *
     * @return The expanded information (description) of the task
     */
    String getAbout();

    /////// Block of calls used for adjustment and start of the CTI-application task /////////
    /**
     * <action>
     * Method to start execution of the task ,
     * is called by the scheduler of the particular CT-device
     */
    void execute();

    /**
     * <action>
     * Method to stop execution of the started task,
     * is called by the scheduler of the particular CT-device
     * The task's TimerTask, will be canceled.
     *
     * @see java.util.TimerTask
     * @see TaskPool#Stop()
     * @see Task#execute()
     */
    void stopExecute();

    /**
     * <mutator>
     * To attach the environment to the task for execution
     *
     * @param env the environment of the task
     * @see Environment
     * @see org.visualcti.server.Scheduler
     */
    void setEnv(Environment env);

    /**
     * <action>
     * Calls after the task's stop execution called
     *
     * @throws Throwable throw if the task can't free the allocated before resources
     * @see Task#stopExecute()
     */
    void finalize() throws Throwable;
}
