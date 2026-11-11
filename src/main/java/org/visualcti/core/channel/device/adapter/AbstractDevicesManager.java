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
package org.visualcti.core.channel.device.adapter;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.device.DevicesManager;
import org.visualcti.core.channel.device.DeviceEventsProcessor;
import org.visualcti.server.unit.RunnableUnitAdapter;

/**
 * <manager>
 * Implementation: The manager of available hardware factories/devices
 *
 * @author Sopilnyak Oleg
 * @version 3.02
 * @see DevicesManager
 */
public class AbstractDevicesManager extends RunnableUnitAdapter implements DevicesManager {
    private final AtomicReference<ExecutorService> providerEventsExecutorHolder = new AtomicReference<>(null);

    protected AbstractDevicesManager() {
        unitPath = "AbstractDevicesManager";
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, subsystem, etc.)
     *
     * @return the value
     */
    @Override
    public String getType() {
        return "[manager]";
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    @Override
    public String getName() {
        return "Devices Manager";
    }

    /**
     * <aceessor>
     * The executor of device factories' events management
     *
     * @return threads pool for device events processing
     * @see DeviceEventsProcessor#grabProviderEvents()
     */
    @Override
    public Executor deviceEventExecutor() {
        return providerEventsExecutorHolder.get();
    }

    /**
     * <checker>
     * To check is unit can start according the internal state
     *
     * @return true if unit can start
     */
    @Override
    public boolean canStartUnit() {
        return factories().findAny().isPresent();
    }

    /**
     * <action>
     * To start the internal runnable parts of the unit
     *
     * @throws IOException if something went wrong during the internal parts starting
     * @see #Start()
     */
    @Override
    public void startUnitRunnable() throws IOException {
        providerEventsExecutorHolder.getAndSet(buildProviderEventsExecutor());
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     *
     * @throws IOException if something went wrong during the internal parts stopping
     * @see #Stop()
     */
    @Override
    public void stopUnitRunnable() throws IOException {
        final ExecutorService threadPool = providerEventsExecutorHolder.getAndSet(null);
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    /**
     * <builder>
     * To build the provider events entities executer
     *
     * @return built events executer
     */
    protected ExecutorService buildProviderEventsExecutor() {
        final int cores = Runtime.getRuntime().availableProcessors();
        // declare threads pool with active tread as cores counter
        return Executors.newScheduledThreadPool(cores);
    }
}
