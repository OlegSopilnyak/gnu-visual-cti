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

Contact oleg.sopilnyak@gmail.com or gennady@visualCTI.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server.task;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.jdom.Attribute;
import org.jdom.Element;
import org.visualcti.server.core.unit.executable.Task;

/**
 * <task>
 * The adapter of the executable task of the industrial CTI-application
 *
 * @author Oleg Sopilnyak
 * @version 3.0.1
 */
public abstract class TaskAdapter implements Task {
    public static final String TASK_CLASS_ATTRIBUTE = "class";
    public static final String TIMER_ENV_PART = "timer";
    public static final String STDOUT_ENV_PART = "stdout";
    public static final String STDERR_ENV_PART = "stderr";

    // The name of the task
    protected String name = "Task stub";
    // The description of the task
    protected String about = "Task stub description";
    // The environment (resources set) of the task
    protected Environment env = null;
    // Temporary variable for preservation of timer clock of the task
    private transient volatile TimerTask clock = null;
    // Temporary variable for preservation of standard output of the task
    private transient volatile PrintWriter standard = new PrintWriter(System.out, true);
    // Temporary variable for preservation of error messages output of the task
    private transient volatile PrintWriter errors = new PrintWriter(System.err, true);

    /**
     * <producer>
     * To make the clone(copy) of the task instance,
     * cloned instance will be placed to the sandbox
     *
     * @return the clone(copy) of this task
     */
    @Override
    public final Task clone() {
        try {
            return (Task) super.clone();
        } catch (CloneNotSupportedException ce) {
            throw new InternalError("Task.clone -> Unknown error");
        }
    }

    /**
     * <accessor>
     * To get the XML representation of the Task
     * XML should contain all the parameters of the Task
     *
     * @return XML Element <task></task>
     * @see Task#ROOT_ELEMENT
     * @see Element
     */
    @Override
    public Element getXML() {
        Element xml = new Element(Task.ROOT_ELEMENT);
        xml.setAttribute(new Attribute(TASK_CLASS_ATTRIBUTE, getClass().getName()));
        return xml;
    }

    /**
     * <mutator>
     * Setting up the XML representation of the Task
     * XML should contain all the parameters of the Task
     *
     * @param xml The XML Element <task></task>
     * @throws Exception throw if it can't restore the task from input xml-parameter
     * @see Task#ROOT_ELEMENT
     * @see Element
     */
    @Override
    public void setXML(Element xml) throws Exception {
        if (!Task.ROOT_ELEMENT.equals(xml.getName())) {
            throw new Exception("The XML Element not a task");
        }
        String taskClass = xml.getAttributeValue(TASK_CLASS_ATTRIBUTE);
        if (taskClass == null) {
            throw new Exception("Task's type name is not defined in XML");
        }
    }

    /**
     * <accessor>
     * To get te name of the task which is used during
     * deployment the task to the particular CT-device channel
     *
     * @return The name of the task
     * @see TaskPool#add(Task, boolean)
     */
    @Override
    public final String getName() {
        return this.name;
    }

    /**
     * <accessor>
     * To get the expanded information (description) of the task which is used during
     * deployment the task to the particular CT-device channel
     *
     * @return The expanded information (description) of the task
     */
    @Override
    public final String getAbout() {
        return this.about;
    }

    /**
     * <mutator>
     * To attach the environment to the task for execution
     *
     * @param env the environment of the task
     * @see Environment
     * @see org.visualcti.server.Scheduler
     */
    @Override
    public void setEnv(Environment env) {
        // to disconnect former environment's parts
        // to stop and disconnect exists TimerTask
        if (clock != null) {
            clock.cancel();
            clock = null;
        }
        // to disconnect former streams of the task
        standard = null;
        errors = null;
        //
        // processing input environment
        if (env != null) {
            try {
                Timer timer = (Timer) env.getPart(TIMER_ENV_PART, Timer.class);
                if (timer != null) {
                    // to make the timer-task of the task
                    clock = new TimerTask() {
                        @Override
                        public void run() {
                            clockEvent();
                        }
                    };
                    // to notify the task every second (clock synchronization)
                    timer.schedule(clock, new Date(), 1000);
                }
            } catch (Exception e) {
                error("Can't get environment variable " + TIMER_ENV_PART);
                exception(e);
            }
            // to connect environment's streams
            try {
                OutputStream output = (OutputStream) env.getPart(STDOUT_ENV_PART, OutputStream.class);
                if (output != null) {
                    // standard output stream of the task
                    standard = new PrintWriter(output, true);
                }
            } catch (Exception e) {
                error("Can't get environment variable " + STDOUT_ENV_PART);
                exception(e);
            }
            try {
                OutputStream output = (OutputStream) env.getPart(STDERR_ENV_PART, OutputStream.class);
                if (output != null) {
                    // error messages stream of the task
                    errors = new PrintWriter(output, true);
                }
            } catch (Exception e) {
                error("Can't get environment variable " + STDERR_ENV_PART);
                exception(e);
            }
        }
        // to store reference to environment of the task
        this.env = env;
    }

    /**
     * <action>
     * Method to start execution of the task ,
     * is called by the scheduler of the particular CT-device
     */
    @Override
    public abstract void execute();

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
    @Override
    public abstract void stopExecute();

    /**
     * <action>
     * Calls after the task's stop execution called
     *
     * @throws Throwable throw if the task can't free the allocated before resources
     * @see Task#stopExecute()
     */
    @Override
    public synchronized void finalize() throws Throwable {
        if (clock != null) {
            clock.cancel();
            clock = null;
        }
        super.finalize();
    }

    /**
     * <printer>
     * Method for print messages from your CTI-application standard
     * or debugging messages, you can call this method on demand
     * from the CTI-application task implementation.
     */
    protected final void debug(String message) {
        standard.println(message);
    }

    /**
     * <printer>
     * Method for print  error messages of the CTI-application task,
     * you can call this method on demand from the CTI-application task implementation.
     */
    protected final void error(String message) {
        errors.println(message);
    }

    /**
     * <printer>
     * Method for print an exception's stack trace to stderr,
     * you can call this method on demand from the CTI-application task implementation.
     */
    protected final void exception(Throwable t) {
        t.printStackTrace(errors);
    }

    /**
     * <event_processor>
     * Notification from SystemClockSynchronize
     * You must override it
     */
    protected abstract void clockEvent();
}
