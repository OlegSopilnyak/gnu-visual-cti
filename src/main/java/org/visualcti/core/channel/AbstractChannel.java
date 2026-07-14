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
package org.visualcti.core.channel;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.visualcti.core.channel.device.Device;
import org.visualcti.server.core.channel.ChannelTaskRunner;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * The Channel Adapter: The channel through device of which task is communicating with external world
 *
 * @see ServerUnit
 * @see Channel
 * @param <D> the type of channel device
 */
public abstract class AbstractChannel<D extends Device<?, ?>> extends ServerUnitAdapter implements Channel<D> {
    // the device associated with the channel
    protected final transient D device;
    // the map of online tasks counters
    private final transient Map<String, Integer> executingTasks;

    protected AbstractChannel(D device) {
        this.device = device;
        // the counter of tasks in executing state at the moment
        executingTasks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractChannel)) return false;
        if (!super.equals(o)) return false;
        AbstractChannel<?> that = (AbstractChannel<?>) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName());
    }

    /**
     * <accessor>
     * To get the device of the channel
     *
     * @return channel-device instance associated with the channel
     */
    @Override
    public D getDevice() {
        return device;
    }

    /**
     * <accessor>
     * To get executing tasks
     * key: task name
     * value: executing quantity
     *
     * @return the value
     * @see #beforeStart(Task) (Task)
     * @see #afterStop(Task) (Task)
     */
    @Override
    public Map<String, Integer> getOnlineTasks() {
        return Collections.unmodifiableMap(executingTasks);
    }

    /**
     * <accessor>
     * To check is channel busy to accept incoming device event
     *
     * @return true if channel is busy
     * @see org.visualcti.server.channel.ChannelTaskRunnerAdapter#accept(DeviceEvent)
     */
    @Override
    public boolean isBusy() {
        return Channel.super.isBusy();
    }

    /**
     * <action>
     * Before start task execution on the channel
     *
     * @param task task which is going to be executed
     * @see ChannelTaskRunner#attachTask(Task)
     * @see #isBusy()
     */
    @Override
    public void beforeStart(final Task task) {
        // incrementing particular task counter
        executingTasks.compute(task.getName(), (name, counter) -> counter == null ? 1 : counter + 1);
    }

    /**
     * <action>
     * After stop task execution on the channel
     *
     * @param task task which was executed
     * @see ChannelTaskRunner#detachTask(Task)
     * @see #isBusy()
     */
    @Override
    public void afterStop(final Task task) {
        // decrementing particular task counter
        executingTasks.compute(task.getName(), (name, counter) -> counter == null || counter <= 0 ? 0 : counter - 1);
    }
}
