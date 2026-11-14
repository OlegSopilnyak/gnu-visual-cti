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
package org.visualcti.server.hardware.provider.virtual;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.media.Audio;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * Virtual telephony service provider</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyServiceProvider
 */
@SuppressWarnings("unchecked")
public class VirtualDeviceProvider<H extends UUID> extends AbstractTelephonyServiceProvider<H> {
    public static final String DEVICE_FACTORY_VENDOR = "VisualCTI";
    private static final PhoneCall.Number CALLER_ID = PhoneNumber.of("1234567890");
    private volatile transient String deviceNamePrefix = "virtual_";
    private volatile transient int devicesCount = 0;
    private final Predicate<String> isInvalidDeviceName =
            name -> name == null || name.trim().isEmpty() || !name.startsWith(deviceNamePrefix);
    // executor for scheduling device activities tasks
    private final ScheduledExecutorService scheduler;

    public VirtualDeviceProvider(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void setDeviceNamePrefix(String deviceNamePrefix) {
        this.deviceNamePrefix = deviceNamePrefix;
    }

    public void setDevicesCount(int devicesCount) {
        this.devicesCount = devicesCount;
    }

    @Override
    protected Collection<String> nativeAllowedDevices() {
        return IntStream.range(0, devicesCount).mapToObj(i -> deviceNamePrefix + (i + 1)).collect(Collectors.toList());
    }

    @Override
    protected H nativeResourceOpen(String name) throws IOException {
        if (isOpened(name) || isInvalidDeviceName.test(name)) {
            throw new IOException("Device :" + name + ": is already opened or has invalid name");
        }
        return (H) UUID.randomUUID();
    }

    @Deprecated
    @Override
    protected boolean isValid(H handle) {
        return super.isValid(handle);
    }

    @Override
    protected boolean nativeAnswerCall(H handle) {
        return isHandsetOff(handle);
    }

    @Override
    protected PhoneCall.Number nativeCallerID(H handle) {
        return CALLER_ID;
    }

    @Override
    protected boolean isReadyToPlay(H handle) {
        return true;
    }

    @Override
    protected boolean asyncAudioFilePlaying(File audioFile, Audio format, H handle) {
        return true;
    }

    @Override
    protected boolean isReadyToRecord(H handle) {
        return true;
    }

    @Override
    protected boolean asyncAudioFileRecording(Path targetFilePath, Audio format, H handle, int silence) {
        return true;
    }

    @Override
    protected Runnable sendTimeoutEventFor(H handle, String actionName) {
        final DeviceEvent<H> timeoutEvent = AbstractDeviceEvent.<H>of(DeviceEvent.Type.DEVICE_SPECIFIC)
                .deviceHandle(handle).description(actionName)
                .deviceName(openedNameByHandle(handle).orElse("unknown")).vendor(DEVICE_FACTORY_VENDOR)
                .option(DeviceEvent.Option.REASON, Result.TIMEOUT);
        return () -> putEvent(timeoutEvent);
    }

    @Override
    protected void schedulePostponedAction(H handle, Runnable action, long after, TimeUnit unit) {
        scheduler.schedule(action, after, unit);
    }
}
