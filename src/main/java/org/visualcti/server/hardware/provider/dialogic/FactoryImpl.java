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

import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.DTEV_SIG;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_CALLP;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_CST;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_DIAL;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_ERROR;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_GETDIG;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_PLAY;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_PLAYTONE;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_RECORD;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TDX_SETHOOK;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TFX_FAXERROR;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TFX_FAXRECV;
import static org.visualcti.server.hardware.provider.dialogic.HardwareConst.TFX_FAXSEND;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.AbstractTelephonyChannel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractDeviceEvent;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.util.Tools;

/**
 * <singleton>
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
public class FactoryImpl<H extends Integer, D extends DialogicDevice<H, ?>> extends AbstractTelephonyFactory<H, D> {
    // the instance of the factory (singleton)
    private static FactoryImpl<?, ?> instance = null;
    // the thread where Dialogic's events provider is running
    protected final AtomicReference<Thread> dialogicWorkingThreadHolder = new AtomicReference<>(null);

    protected FactoryImpl(Executor deviceEventsExecutor, DialogicServiceProvider<H> provider) {
        super(deviceEventsExecutor, provider);
    }

    /**
     <producer>
     To get access to FactoryImpl instance
     */
    public static FactoryImpl<?, ?> getInstance() {
        if (instance == null) {
            synchronized (FactoryImpl.class) {
                if (instance == null) {
                    instance = new FactoryImpl<>(
                            Executors.newScheduledThreadPool(2),
                            new DialogicServiceProvider<>()
                    );
                }
            }
        }
        return instance;
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
        // check the factory's devices
        if (!devices().findAny().isPresent()) {
            // no available devices inside the factory
            return;
        }
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
        final Thread workingThread = dialogicWorkingThreadHolder.get();
        if (workingThread != null) {
            workingThread.stop();
            // waiting for working thread has been stopped
            waitFor(() -> dialogicWorkingThreadHolder.get() != null);
        }
    }

    /// private methods
    // to get and process Dialogic's events
    private void dialogicEventProcessing(CountDownLatch processingStarted) {
        dialogicWorkingThreadHolder.getAndSet(Thread.currentThread());
        processingStarted.countDown();
        try {
            final TelephonyServiceProvider<H> provider = getProvider();
            while (isStarted()) {
                final dialogicEvent event = Hardware.getEvent();
                if (event != null) {
                    dispatchEvent("Got Dialogic Hardware Event: " + event);
                    provider.putEvent(toDeviceEvent(event, provider));
                }
            }
        } catch (UnsatisfiedLinkError e) {
            Tools.error("Thread name:" + Thread.currentThread().getName());
            Tools.error("==Native== Cannot get Dialogic's device event. Stopping the events processing Thread.");
            e.printStackTrace(Tools.err);
        } catch (ThreadDeath death) {
            // The working tread is stopped outside by Thread.stop() method
        } finally {
            dialogicWorkingThreadHolder.getAndSet(null);
        }
    }

    // transforming Dialogic event to the DeviceEvent to put the event
    private DeviceEvent<H> toDeviceEvent(dialogicEvent event, TelephonyServiceProvider<H> provider) {
        final AbstractDeviceEvent<H> dialogicDeviceEvent = AbstractDeviceEvent.of(DeviceEvent.Type.DEVICE_SPECIFIC);
        final H deviceHandle = (H) Integer.valueOf(event.getHandle());
        dialogicDeviceEvent.deviceHandle(deviceHandle)
                .deviceName(provider.openedNameByHandle(deviceHandle).orElse("unknown"))
                .description("Dialogic device event").vendor(vendorName);
        // Switch according to the event received.
        switch (event.getEventID()) {
            case TDX_CST:               /* Call Status Transition */
                callStatusTransition(event, dialogicDeviceEvent);
                break;

            case TDX_PLAY:               /* Play Completed       */
            case TDX_RECORD:             /* Record Completed     */
                voiceTransmissionCompleted(event, dialogicDeviceEvent);
                break;

            case TDX_GETDIG:             /* Get Digits Completed */
                gettingDigitsCompleted(event, dialogicDeviceEvent);
                break;

            case TDX_CALLP:        /* Make call complete */
                makeCallCompleted(event, dialogicDeviceEvent);
                break;

            case TDX_PLAYTONE:            /* Play tone complete */
            case TDX_DIAL:                /* Dial without Call Analysis complete */
                dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.OK);
                break;

            case TDX_SETHOOK:            /* Set-Hook Complete    */
                // doing nothing here
                break;

            case TFX_FAXRECV:  /* The document has been successfully received. */
            case TFX_FAXSEND:    // Fax sending done
                transmittingFaxCompleted(event, dialogicDeviceEvent);
                break;

            case TFX_FAXERROR:/* Error during the fax session. */
                transmittingFaxMalfunction(event, dialogicDeviceEvent);
                break;

            case DTEV_SIG:               /* DTI signaling event */
                // not implemented yet
                dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR)
                        .eventType(DeviceEvent.Type.MALFUNCTION);
                break;

            case TDX_ERROR:                // Error detected
                dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR)
                        .eventType(DeviceEvent.Type.MALFUNCTION)
                        .description(event.getReason());
                break;

            default:
                // nothing
        }
        return dialogicDeviceEvent;
    }

    // transforming Dialogic event for fax transmission malfunction
    private static <H extends Integer> void transmittingFaxMalfunction(
            dialogicEvent event, AbstractDeviceEvent<H> dialogicDeviceEvent
    ) {
        if (Objects.equals(Result.ERROR.getValue(), event.getReason())) {
            // error
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR);
            dialogicDeviceEvent.eventType(DeviceEvent.Type.MALFUNCTION);
        } else if (Objects.equals(Result.FAX.COMPATIBILITY.getValue(), event.getReason())) {
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.FAX.COMPATIBILITY);
        } else if (Objects.equals(Result.FAX.COMMUNICATION_ERROR.getValue(), event.getReason())) {
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.FAX.COMMUNICATION_ERROR);
        } else if (Objects.equals(Result.FAX.NO_POLL.getValue(), event.getReason())) {
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.FAX.NO_POLL);
        } else if (Objects.equals(Result.FAX.USER_STOP.getValue(), event.getReason())) {
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.FAX.USER_STOP);
        }
    }

    // transforming Dialogic event for fax transmission completed
    private static <H extends Integer> void transmittingFaxCompleted(
            dialogicEvent event, AbstractDeviceEvent<H> dialogicDeviceEvent
    ) {
        if (Objects.equals(Result.ERROR.getValue(), event.getReason())) {
            // error
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR);
            dialogicDeviceEvent.eventType(DeviceEvent.Type.MALFUNCTION);
        } else if (Objects.equals(Result.IO.EOF.getValue(), event.getReason())) {
            // The document has been successfully received
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.IO.EOF);
        }
    }

    // transforming Dialogic event for make call completed
    private static <H extends Integer> void makeCallCompleted(
            dialogicEvent event, AbstractDeviceEvent<H> dialogicDeviceEvent
    ) {
        if (Objects.equals(Result.ERROR.getValue(), event.getReason())) {
            // default (Call Analyze Error)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR);
            dialogicDeviceEvent.eventType(DeviceEvent.Type.MALFUNCTION);
        } else if (Objects.equals(Result.CALL.Analysis.VOICE.getValue(), event.getReason())) {
            // connected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.VOICE);
        } else if (Objects.equals(Result.CALL.Analysis.FAX.getValue(), event.getReason())) {
            // fax tone detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.FAX);
        } else if (Objects.equals(Result.CALL.Analysis.BUSY.getValue(), event.getReason())) {
            // busy signal detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.BUSY);
        } else if (Objects.equals(Result.CALL.Analysis.NO_ANSWER.getValue(), event.getReason())) {
            // no answer detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.NO_ANSWER);
        } else if (Objects.equals(Result.CALL.Analysis.NO_RESPONDING.getValue(), event.getReason())) {
            // no ringback
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.NO_RESPONDING);
        } else if (Objects.equals(Result.CALL.Analysis.NO_DIAL_TONE.getValue(), event.getReason())) {
            // no dial tone
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.NO_DIAL_TONE);
        } else if (Objects.equals(Result.TERMINATED.getValue(), event.getReason())) {
            // user stop ( dx_stopch(handle) )
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.TERMINATED);
        }
    }

    // transforming Dialogic event for getting user input completed
    private static <H extends Integer> void gettingDigitsCompleted(
            dialogicEvent event, AbstractDeviceEvent<H> dialogicDeviceEvent
    ) {
        if (Objects.equals(Result.ERROR.getValue(), event.getReason())) {
            // error when resolve reason mask
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR);
            dialogicDeviceEvent.eventType(DeviceEvent.Type.MALFUNCTION);
        } else if (Objects.equals(Result.CALL.DISCONNECT.getValue(), event.getReason())) {
            // disconnect detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.DISCONNECT);
        } else if (Objects.equals(Result.CALL.Analysis.FAX.getValue(), event.getReason())) {
            // fax signal detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.FAX);
        } else if (Objects.equals(Result.TIMEOUT.getValue(), event.getReason())) {
            // timeout
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.TIMEOUT);
        } else if (Objects.equals(Result.IO.DTMF.getValue(), event.getReason())) {
            // terminated by the DTMF symbol (defined in mask)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.IO.DTMF);
            dialogicDeviceEvent.option(DeviceEvent.Option.INPUT, VOX.digitsBuffer(event.getHandle()));
        } else if (Objects.equals(Result.TERMINATED.getValue(), event.getReason())) {
            // user stop operation (dx_stopch)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.TERMINATED);
        }
    }

    // transforming Dialogic event for sound transmission completed
    private static <H extends Integer> void voiceTransmissionCompleted(
            dialogicEvent event, AbstractDeviceEvent<H> dialogicDeviceEvent
    ) {
        if (Objects.equals(Result.IO.EOF.getValue(), event.getReason())) {
            // operation finished normally or end of data
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.IO.EOF);
        } else if (Objects.equals(Result.IO.DTMF.getValue(), event.getReason())) {
            // terminated by the DTMF symbol (defined in mask)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.IO.DTMF);
            dialogicDeviceEvent.option(DeviceEvent.Option.INPUT, VOX.digitsBuffer(event.getHandle()));
        } else if (Objects.equals(Result.TIMEOUT.getValue(), event.getReason())) {
            // terminate by timeout (for record)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.TIMEOUT);
        } else if (Objects.equals(Result.IO.SILENCE.getValue(), event.getReason())) {
            // finished by silence detection (for record)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.IO.SILENCE);
        } else if (Objects.equals(Result.TERMINATED.getValue(), event.getReason())) {
            // user stop operation (dx_stopch)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.TERMINATED);
        } else if (Objects.equals(Result.CALL.Analysis.FAX.getValue(), event.getReason())) {
            // fax tone detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.Analysis.FAX);
        } else if (Objects.equals(Result.CALL.DISCONNECT.getValue(), event.getReason())) {
            // disconnect detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.DISCONNECT);
        } else if (Objects.equals(Result.ERROR.getValue(), event.getReason())) {
            // unknown reason
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.ERROR);
            dialogicDeviceEvent.eventType(DeviceEvent.Type.MALFUNCTION);
        }
    }

    // transforming Dialogic event for phone call status changed
    private static <H extends Integer> void callStatusTransition(
            dialogicEvent event, AbstractDeviceEvent<H> dialogicDeviceEvent
    ) {
        if (Objects.equals(Result.CALL.RINGS.getValue(), event.getReason())) {
            // Rings Received (Incoming Call)
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.RINGS);
        } else if (Objects.equals(Result.CALL.DISCONNECT.getValue(), event.getReason())) {
            // disconnect detected
            dialogicDeviceEvent.option(DeviceEvent.Option.REASON, Result.CALL.DISCONNECT);
        }
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
