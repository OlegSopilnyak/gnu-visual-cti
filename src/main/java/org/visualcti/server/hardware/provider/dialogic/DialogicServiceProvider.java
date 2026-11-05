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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * Dialogic service provider of the telephony device features</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyServiceProvider
 */
@SuppressWarnings("unchecked")
public class DialogicServiceProvider<H extends Integer> extends AbstractTelephonyServiceProvider<H> {
    // the state of handset true = handset is off false = handset is on
    private final AtomicBoolean handsetOff = new AtomicBoolean(true);
    // reference to the sound-card phone number as singleton
    private final AtomicReference<PhoneCall.Number> callerId = new AtomicReference<>(PhoneCall.Number.EMPTY);

    @Override
    protected boolean isValid(H handle) {
        return handle != null && handle.intValue() > 0;
    }

    @Override
    protected List<Audio> availableFormatsFor(H handle) {
        return !VOX.isSupport(handle)
                // nothing to give
                ? Collections.emptyList()
                // getting codecs from native
                : Collections.list(new StringTokenizer(VOX.getCodecsList(handle), " \n")).stream()
                .map(String.class::cast).map(Audio::of).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    protected Collection<String> nativeAllowedDevices() {
        return Collections.list(new StringTokenizer(Hardware.getNamesList(), "\n ")).stream()
                .map(String.class::cast).collect(Collectors.toSet());
    }

    @Override
    protected H nativeResourceOpen(String name) {
        final H resourceHandle = (H) Integer.valueOf(Hardware.open(name));
        //
        // post-processing after resource handle's getting
        if (isValid(resourceHandle)) {
            // registering the available codecs for the opened resource by the resource's handle
            registeringAvailableMediaFormats(resourceHandle);
            // making the context for the resource's handle
            makingResourceHandleContext(resourceHandle, name);
        }
        return withContext(resourceHandle);
    }

    @Override
    protected void nativeResourceClose(H handle) {
        if (Context.get(handle) != null) {
            // freeing context for the handle if any
            Context.free(handle);
        }
        Hardware.closeChannel(handle);
    }

    @Override
    protected boolean isHandsetOff(H handle) {
        return super.isHandsetOff(handle) && handsetOff.get();
    }

    @Override
    protected boolean nativeHandsetOff(H handle) {
        handsetOff.getAndSet(true);
        CCM.setHookState(handle, Hardware.DX_ONHOOK);
        return true;
    }

    @Override
    protected boolean nativeAnswerCall(H handle) {
        if (handsetOff.get()) {
            // the phone handset is in the off state changing it to on
            handsetOff.getAndSet(false);
            CCM.setHookState(handle, Hardware.DX_OFFHOOK);
        }
        return true;
    }

    @Override
    protected PhoneCall.Number nativeCallerID(H handle) {
        return isOpened(handle) ? callerId.get() : PhoneCall.Number.EMPTY;
    }

    @Override
    protected boolean nativeStartCalling(H handle, PhoneCall.Number number, int timeout) {
        return Hardware.call(handle, number.toString(), timeout) != Hardware.DX_ERROR;
    }

    @Override
    protected DeviceEvent<H> nativeGetEvent(long during) {
        // not implemented for current version of Dialogic's natives
        return null;
    }

    @Override
    protected void nativeEnableEvents(H deviceHandle, String eventType) {
        Hardware.enableEvents(deviceHandle, eventType);
    }

    @Override
    protected void nativeDisableEvents(H deviceHandle, String eventType) {
        // disabling ALL events for the handle (implementation specific)
        Hardware.disableEvt(deviceHandle);
        // enabling rest event types for the handle except disabled one
        enabledEventTypes(deviceHandle).stream().map(OperationResultValue::getValue)
                // skipping disabled device event type (exclude it)
                .filter(enabledEventType -> !Objects.equals(enabledEventType, eventType))
                // enabling the rest event types
                .forEach(enabledEventType -> Hardware.enableEvt(deviceHandle, enabledEventType));
    }

    @Override
    protected H nativeFaxResourceOpen(String name) throws IOException {
        final H resourceHandle = (H) Integer.valueOf(Hardware.openFax(name));
        //
        // post-processing after resource handle's getting
        if (isValid(resourceHandle)) {
            // making the context for the resource's handle
            makingResourceHandleContext(resourceHandle, name);
        }
        return withContext(resourceHandle);
    }

    @Override
    protected void nativeFaxResourceClose(H handle) {
        // uses the same close as for regular evice handle
        nativeResourceClose(handle);
    }

    @Override
    protected boolean nativeStartFaxReceiving(H handle, String filePath, boolean issueVoiceRequest) {
        return super.nativeStartFaxReceiving(handle, filePath, issueVoiceRequest)
                // checking is the handle of valid opened fax's resource
                && FAX.isResource_FAX(handle)
                // starting the native fax-receiving function
                && FAX.StartReceive(handle, filePath, issueVoiceRequest) != Hardware.DX_ERROR;
    }

    @Override
    protected void nativeStopFaxReceiving(H handle) {
        stopTransmission(handle);
    }

    @Override
    protected boolean nativeStartFaxTransmitting(
            H handle, String filePath, boolean issueVoiceRequest, boolean isTiff,
            boolean isHighResolution, int firstPageNumber, int totalPages
    ) {
        return super.nativeStartFaxTransmitting(handle, filePath, issueVoiceRequest, isTiff,
                isHighResolution, firstPageNumber, totalPages)
                // checking is the handle of valid opened fax's resource
                && FAX.isResource_FAX(handle)
                // starting the native fax-sending function
                && FAX.StartSend(handle, isTiff, filePath, isHighResolution, issueVoiceRequest,
                firstPageNumber, totalPages) != Hardware.DX_ERROR;
    }

    @Override
    protected void nativeStopFaxTransmitting(H handle) {
        stopTransmission(handle);
    }

    @Override
    protected boolean isReadyToPlay(H handle) {
        return VOX.isResource_VOX(handle);
    }

    ///
    /// private methods
    // to initialize the Dialogic's activities context for the valid resource handle
    private void makingResourceHandleContext(H resourceHandle, String resourceName) {
        final Object monitor = new Object();
        try {
            Context.init(resourceHandle, monitor, resourceName);
        } catch (Exception e) {
            Tools.error("openResource:Can't make context for " + resourceHandle);
            e.printStackTrace(Tools.err);
            // closing
            nativeResourceClose(resourceHandle);
        }
    }

    // to check opened device's handle (with context) and return valid value
    private H withContext(H handle) {
        if (Context.get(handle) == null) {
            // no available any context for the device's handle
            nativeResourceClose(handle);
            return (H) Integer.valueOf(Hardware.DX_ERROR);
        }
        return handle;
    }

    //
    // stopping all device's I/O activity
    private void stopTransmission(H handle) {
        // method to stop all device's I/O (vox/fax) activity
        Hardware.stopAll(handle);
    }

    // registering the available codecs for the opened resource by the resource's handle
    private void registeringAvailableMediaFormats(H handle) {
        final Device.ParameterName name = MultimediaEngine.Parameter.ALLOWED_CODECS;
        final ConfigurationParameter parameter = ConfigurationParameter.of(name.value(), availableFormatsFor(handle));
        nativeSetResourceParameter(handle, name, parameter);
    }
}
