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

import java.util.Map;
import org.visualcti.server.UnitRegistry;
import org.visualcti.core.channel.device.Device;
import org.visualcti.server.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.core.unit.ServerUnit;

/**
 * The Channel: The channel through device of which task is communicating with external world
 *
 * @param <D> the type of channel device
 * @see ServerUnit
 */
@SuppressWarnings("unchecked")
public interface Channel<D extends Device> extends ServerUnit {
    // the value of type of the server unit
    String UNIT_TYPE = "[channel]";

    /**
     * <accessor>
     * To get the device of the channel
     *
     * @return channel-device instance associated with the channel
     */
    D getDevice();

    /**
     * <accessor>
     * To get the device's factory of the channel
     *
     * @param <F> the type of channel device factory
     * @return device factory instance associated with the channel's device
     */
    default <F extends Factory> F getDeviceFactory() {
        return (F) getDevice().getFactory();
    }

    /**
     * <accessor>
     * To get access to the status of the channel
     *
     * @return status
     * @see Device#getStatus()
     */
    default String getStatus() {
        return getDevice().getStatus();
    }

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     * @see Device#isOpened()
     */
    default boolean isOpened() {
        final Device channelDevice = getDevice();
        return channelDevice != null && channelDevice.isOpened();
    }

    /**
     * <action>
     * Before start task execution on the channel
     *
     * @param task task which is going to be executed
     * @see org.visualcti.server.core.channel.ChannelTaskRunner#attachTask(Task)
     * @see #isBusy()
     */
    void beforeStart(Task task);

    /**
     * <action>
     * After stop task execution on the channel
     *
     * @param task task which was executed
     * @see org.visualcti.server.core.channel.ChannelTaskRunner#detachTask(Task)
     * @see #isBusy()
     */
    void afterStop(Task task);

    /**
     * <accessor>
     * To get executing tasks
     * key: task name
     * value: executing quantity
     *
     * @return the value
     */
    Map<String, Integer> getOnlineTasks();

    /**
     * <accessor>
     * To get the quantity of tasks executing in the channel now
     *
     * @return how many tasks are executing now
     * @see #getOnlineTasks()
     */
    default int onlineTasksCount() {
        return getOnlineTasks().values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * <accessor>
     * To check is channel busy to accept incoming device event
     *
     * @return true if channel is busy
     */
    default boolean isBusy() {
        // by default channel support multiple usage
        return false;
    }

    /**
     * <accessor>
     * To get the Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    default String getType() {
        return UNIT_TYPE;
    }

    /**
     * <accessor>
     * To get the Name of the channel using cannel device's name
     *
     * @return the value
     * @see ServerUnit#getName()
     * @see #getDevice()
     * @see Device#getName()
     */
    @Override
    default String getName() {
        return getDevice().getName();
    }

    /**
     * <accessor>
     * To get the Name of the channel device factory vendor
     *
     * @return the value
     * @see Device#getFactory()
     * @see Factory#getVendor()
     * @see #getDeviceFactory()
     */
    default String getDeviceVendor() {
        return getDeviceFactory().getVendor();
    }

    /**
     * <mutator>
     * To add device events listener for particular device's events
     *
     * @param listener the listener instance
     * @see DeviceEvent.Listener
     * @see Device#getName()
     */
    default void addDeviceEventListenerFor(DeviceEvent.Listener listener) {
        getDeviceFactory().addDeviceEventListenerFor(getDevice().getName(), listener);
    }


    /**
     * <mutator>
     * To remove device events listener for particular device's events
     *
     * @param listener the listener instance
     * @see DeviceEvent.Listener
     * @see Device#getName()
     */
    default void removeDeviceEventListenerFor(DeviceEvent.Listener listener) {
        getDeviceFactory().removeDeviceEventListenerFor(getDevice().getName(), listener);
    }

    /**
     * <accessor>
     * To check is unit needs to be registered in units registry
     * by default we don't need the registration for that kid of server units
     *
     * @return true if unit needed registration
     * @see UnitRegistry#register(ServerUnit)
     */
    @Override
    default boolean isNeedRegistration() {
        return false;
    }
}
