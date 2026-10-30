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
package org.visualcti.server.hardware.provider.dialogic;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.AbstractTelephonyChannel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.util.Tools;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The factory for a Dialogic's devices</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see DialogicDevice
 * @see AbstractTelephonyFactory
 */
@SuppressWarnings("unchecked")
public class AbstractDialogicDeviceFactory<H extends Integer, D extends DialogicDevice<H, ?>>
        extends AbstractTelephonyFactory<H, D> {
    // the thread where events provider is running
    protected final AtomicReference<Thread> workingThreadHolder = new AtomicReference<>(null);

    protected AbstractDialogicDeviceFactory(Executor deviceEventsExecutor, DialogicServiceProvider<H> provider) {
        super(deviceEventsExecutor, provider);
    }


    @Override
    protected TelephonyChannel<D> makeChannelFor(Device<?, ?> device) {
        return new DialogicDeviceChannel((D) device);
    }

    /**
     * <action>
     * To start factory's devices events processing
     */
    @Override
    protected void startingDeviceEventProcessing() {
        // starting general factory's device events processing
        super.startingDeviceEventProcessing();
        // starting Dialogic factory's device events processing
        final CountDownLatch processingStarted = new CountDownLatch(1);
        new Thread(() -> dialogicEventProcessing(processingStarted), "Dialogic events processing Thread").start();
        // waiting for Dialogic's events processing starts
        try {
            processingStarted.await();
        } catch (InterruptedException e) {
            Tools.error("Not started Dialogic events processing!");
            e.printStackTrace(Tools.err);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
            // stopping whole devices events processing
            stoppingDeviceEventProcessing();
        }
    }

    /**
     * <action>
     * To stop factory's devices events processing
     */
    @Override
    protected void stoppingDeviceEventProcessing() {
        // stopping general factory's device events processing
        super.stoppingDeviceEventProcessing();
        // stopping Dialogic event processing thread
        final Thread workingThread = workingThreadHolder.get();
        if (workingThread != null) {
            workingThread.stop();
            // waiting for working thread has been stopped
            waitFor(() -> workingThreadHolder.get() != null);
        }
    }

    public boolean isDialogicEventProcessingStarted() {
        return workingThreadHolder.get() != null;
    }

    /// private methods
    // to get and process Dialogic's events
    private void dialogicEventProcessing(CountDownLatch processingStarted) {
        workingThreadHolder.getAndSet(Thread.currentThread());
        processingStarted.countDown();
        try {
            final TelephonyServiceProvider<H> provider = getProvider();
            while (isStarted()) {
                final dialogicEvent event = Hardware.getEvent();
                provider.putEvent(toDeviceEvent(event, provider));
            }
        }catch (ThreadDeath death) {
            // The working tread is stopped outside by Thread.stop()
        } finally {
            workingThreadHolder.getAndSet(null);
        }
    }

    // transforming Dialogic event to the DeviceEvent to put the event
    private DeviceEvent<H> toDeviceEvent(dialogicEvent event, TelephonyServiceProvider<H> provider) {
        final AbstractDeviceEvent<H> deviceEvent = AbstractDeviceEvent.of(DeviceEvent.Type.DEVICE_SPECIFIC);
        final H deviceHandle = (H) Integer.valueOf(event.getHandle());
        deviceEvent.deviceHandle(deviceHandle)
                .deviceName(provider.openedNameByHandle(deviceHandle).orElse("unknown"))
                .description("Dialogic device event").vendor(vendorName)
                .option(DeviceEvent.Option.REASON, event.getReason());
        // todo make reason by event.getReasonID()
        return deviceEvent;
    }

    // waiting for condition is true
    private void waitFor(Callable<Boolean> condition) {
        try {
            while (condition.call()) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace(Tools.err);
        }
    }

    // inner classes
    private class DialogicDeviceChannel extends AbstractTelephonyChannel<D> {
        protected DialogicDeviceChannel(D device) {
            super(device);
        }
    }
}
