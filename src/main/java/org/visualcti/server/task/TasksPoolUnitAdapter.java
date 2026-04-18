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
package org.visualcti.server.task;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.core.executable.Engine;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.executable.task.TasksPoolUnit;
import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.MessageType;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.action.UnitActionEvent;
import org.visualcti.server.core.unit.message.UnitMessage;
import org.visualcti.server.core.unit.message.UnitMessageFactory;
import org.visualcti.server.event.model.UnitMessageFactoryAdapter;
import org.visualcti.util.Tools;

/**
 * Implementation Adapter: server tasks pool unit engine
 *
 * @see Task
 * @see TasksPoolUnit
 */
public class TasksPoolUnitAdapter implements TasksPoolUnit {
    // The list of tasks in the pool
    private final List<Task> pool = Collections.synchronizedList(new LinkedList<>());
    // working ring of the tasks (used in the in-service engine state)
    private final LinkedList<Task> inServiceTasksRing = new LinkedList<>();
    // locker for started tasks ring
    private final Lock tasksRingLock = new ReentrantLock();
    // Current task from the tasks ring
    private transient volatile Task currentTask = null;
    // the factory of server action messages
    // TODO implement factory as injection to the unit
    private UnitMessageFactory actionMessageFactory = new UnitMessageFactoryAdapter() {
    };
    // type of tasks pool
    private PoolType poolType;
    // the state of engine (tasks pool)
    private Engine.State unitEngineState = Engine.State.OUT_OF_SERVICE;

    /**
     * <accessor>
     * To get the type of task-pool
     *
     * @return the value
     * @see PoolType
     */
    @Override
    public PoolType getPoolType() {
        return poolType;
    }

    /**
     * <mutator>
     * To set up the type of task-pool
     *
     * @param poolType new value of task-pool type
     * @return reference to the pool
     * @see PoolType
     */
    @Override
    public TasksPoolUnit setPoolType(PoolType poolType) {
        this.poolType = poolType;
        return this;
    }

    /**
     * <action>
     * to Start the engine
     *
     * @throws IOException if engine can't start
     * @see #isStarted()
     */
    @Override
    public void Start() throws IOException {
        if (!isStarted() && !pool.isEmpty()) {
            dedicatedTasksRing(() -> {
                // to copying tasks from pool to the tasks ring
                inServiceTasksRing.addAll(pool);
                // update the state
                unitEngineState = State.IN_SERVICE;
            });
            // preparing engine started successfully event
            final UnitActionEvent event = actionMessageFactory.build(MessageType.EVENT, UnitActionEvent.class);
            // dispatching success engine started event
            dispatch(event.setUnitPath(getPath()).setFamilyType(MessageFamilyType.START));
        }
    }

    /**
     * <action>
     * to Stop the engine
     *
     * @throws IOException if engine can't stop
     */
    @Override
    public void Stop() throws IOException {
        if ( isStopped() ) {
            return;
        }
        try {
            // to stop execution of the current task
            current().stopExecute();
        } catch (NullPointerException e) {
            // do nothing here, just ignore if current task is null
        }
        dedicatedTasksRing(() -> {
            // clear the tasks ring
            inServiceTasksRing.clear();
            currentTask = null;
            // update the state
            unitEngineState = State.OUT_OF_SERVICE;
        });
        // preparing engine stopped successfully event
        final UnitMessage event = actionMessageFactory.build(MessageType.EVENT, UnitActionEvent.class);
        // dispatching engine stopped successfully event
        dispatch(event.setUnitPath(getPath()).setFamilyType(MessageFamilyType.STOP));
    }

    /**
     * <accessor>
     * To check is Engine is working (in service)
     *
     * @return true if Engine is in service
     * @see State#IN_SERVICE
     */
    @Override
    public boolean isStarted() {
        return unitEngineState == Engine.State.IN_SERVICE;
    }

    /**
     * <accessor>
     * To check is Engine is stopped (out of service)
     *
     * @return true if Engine is out of service
     * @see State#OUT_OF_SERVICE
     */
    @Override
    public boolean isStopped() {
        return unitEngineState == State.OUT_OF_SERVICE;
    }

    /**
     * <accessor>
     * To get the current(executed) task. (returned last next() call)
     * This method works only when engine isStarted()
     *
     * @return current server task
     * @see Task
     */
    @Override
    public Task current() {
        return isStarted() ? currentTask : null;
    }

    /**
     * <action>
     * To get next active task.
     * This method works only when engine isStarted()
     * otherwise will return null
     *
     * @return next server task from started tasks pool
     * @see Task
     * @see LinkedList#removeFirst()
     * @see LinkedList#addLast(Object)
     * @see #current()
     */
    @Override
    public Task next() {
        if (!this.isStarted() || inServiceTasksRing.isEmpty()) {
            // engine isn't started or tasks ring is empty
            return null;
        } else {
            // rotate tasks ring and setup current one
            dedicatedTasksRing(() -> {
                // get first Task in the linked list
                final Task taskFromTheTop = inServiceTasksRing.removeFirst();
                // set current pool's task
                currentTask = taskFromTheTop.clone();
                // place to tail of list
                inServiceTasksRing.addLast(taskFromTheTop);
            });
            return currentTask;
        }
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     */
    @Override
    public Element getXML() {
        return null;
    }

    /**
     * <converter>
     * To update the entity's fields from XML
     *
     * @param xml possible entity's XML
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     */
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {

    }

    /**
     * <accessor>
     * To get reference to messages factory
     *
     * @return not null reference to the factory
     */
    @Override
    public UnitMessageFactory getMessageFactory() {
        return actionMessageFactory;
    }

    /**
     * <executer>
     * To execute console command for this unit.
     * The method will call outside the unit.
     * If command is invalid the exception will be thrown.
     *
     * @param command command to execute
     * @throws Exception if it cannot execute
     */
    @Override
    public void execute(ServerCommandRequest command) throws Exception {

    }

    /**
     * <accessor>
     * To get access to owner of this unit (null for root unit)
     */
    @Override
    public ServerUnit getOwner() {
        return null;
    }

    /**
     * <mutator>
     * To set new owner of this unit (null for the root unit)
     *
     * @param owner
     */
    @Override
    public void setOwner(ServerUnit owner) {

    }

    /**
     * <accessor>
     * To get body unit's Icon (gif | jpeg)
     */
    @Override
    public byte[] getIcon() {
        return new byte[0];
    }

    /**
     * <accessor>
     * To get Type of unit
     */
    @Override
    public String getType() {
        return "";
    }

    /**
     * <accessor>
     * To get Name of unit
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * <accessor>
     * To get Path to unit instance in repository
     */
    @Override
    public String getPath() {
        return "";
    }

    /**
     * <accessor>
     * To get current state of unit as string
     *
     * @return unit's state value as string
     */
    @Override
    public String getUnitState() {
        return isStarted() ? "active":"passive";
    }

    /**
     * <config>
     * To configure the unit, using information from XML
     *
     * @param configuration XML to configure the unit from
     * @see ServerUnit#configure(Element)
     * @see org.visualcti.server.core.XmlAware#setXML(Element)
     */
    @Override
    public void configure(Element configuration) {
        try {
            setXML(configuration);
        } catch (IOException | DataConversionException e) {
            try {
                // preparing engine wasn't configured error
                final UnitActionError error = actionMessageFactory.build(MessageType.ERROR, UnitActionError.class);
                // dispatching engine wasn't configured error
                dispatch(error.setNestedException(e).setUnitPath(getPath()));
            } catch (IOException ex) {
                ex.printStackTrace(Tools.err);
            }
        }
    }

    /**
     * <accessor>
     * get serverUnit properties
     * may use for visual editing in GUI
     */
    @Override
    public Map getProperties() {
        return Collections.emptyMap();
    }

    /**
     * <mutator>
     * assign properties set to serverUnit
     * Properties may be changed in GUI
     *
     * @param properties
     */
    @Override
    public void setProperties(Map properties) {

    }

    // private methods
    private void dedicatedTasksRing(final Runnable action) {
        tasksRingLock.lock();
        try {
            action.run();
        } finally {
            tasksRingLock.unlock();
        }
    }
}
