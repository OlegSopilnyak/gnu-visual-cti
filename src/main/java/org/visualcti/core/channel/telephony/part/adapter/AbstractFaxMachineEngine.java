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
package org.visualcti.core.channel.telephony.part.adapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceCore;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.media.Fax;

/**
 * Adapter: The Part of the Telephony Channel Device: The root device part of the telephony fax-document exchange management
 *
 * @param <H> the type of the telephony device's low-level operations handle
 * @see FaxMachineEngine
 * @see AbstractDevicePart
 */
public abstract class AbstractFaxMachineEngine<H> extends AbstractDevicePart<H> implements FaxMachineEngine<H> {
    // predicate to test whether the fax device handle is valid or not
    private final transient Predicate<PhoneCallSession<H>> validFaxDeviceHandle =
            phoneSession -> super.validResourceHandle.test(phoneSession.parameter(Device.Parameter.FAX_DEVICE_HANDLE));
    // predicate for connected phone call's fax-operation failed result values
    private static final Predicate<OperationResultValue> faxOperationFailed =
            value -> value == Result.TIMEOUT || value == Result.FAX.COMMUNICATION_ERROR
                    || value == Result.FAX.COMPATIBILITY || value == Result.FAX.NO_POLL || value == Result.FAX.USER_STOP;

    /**
     * <action>
     * To open and activate the fax-machine on the opened telephony device session
     *
     * @param session the session of the opened device
     * @throws IOException if device cannot open fax-machine for the telephony device session
     * @see PhoneCallSession#getDevice()
     * @see TelephonyDevice#getName()
     * @see TelephonyDeviceCore#getProvider()
     * @see TelephonyServiceProvider#openFaxResource(String)
     * @see Device.Parameter#FAX_DEVICE_HANDLE
     */
    @Override
    public void open(PhoneCallSession<H> session) throws IOException {
        if (canFax()) {
            if (isOpened(session)) {
                throw new IOException("Cannot open FAX device part! Already opened!");
            }
            final H faxResourceHandle = deviceCore.getProvider().openFaxResource(session.getDevice().getName());
            session.parameter(Device.Parameter.FAX_DEVICE_HANDLE, faxResourceHandle);
        }
    }

    /**
     * <checker>
     * To check is phone call session opened.
     * Checks device's fax-machine opened as well.
     *
     * @param session the phone call's session, device is working with
     * @return true if session opened well and device's fax-machine is opened as well
     * @see AbstractDevicePart#isOpened(PhoneCallSession)
     * @see PhoneCallSession#parameter(Device.ParameterName)
     * @see Device.Parameter#FAX_DEVICE_HANDLE
     */
    @Override
    public boolean isOpened(final PhoneCallSession<H> session) {
        return super.closedSession.negate().and(super.validDeviceHandle).and(validFaxDeviceHandle).test(session);
    }

    /**
     * <accessor>
     * To check, whether device can operate with fax-machines
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can operate with fax-machine
     * @see Parameter#FAX_ALLOWED
     */
    @Override
    public boolean canFax() {
        return deviceCore.getParameter(Parameter.FAX_ALLOWED)
                .<Boolean>map(ConfigurationParameter::getValue).orElse(false);
    }

    /**
     * <action>
     * Closing the fax-machine part of the device
     *
     * @param session the session of the opened device
     * @see Device.Parameter#FAX_DEVICE_HANDLE
     */
    @Override
    public void close(PhoneCallSession<H> session) {
        if (isOpened(session)) {
            deviceCore.getProvider().closeFaxResource(session.parameter(Device.Parameter.FAX_DEVICE_HANDLE));
            session.parameter(Device.Parameter.FAX_DEVICE_HANDLE, null);
        }
    }

    /**
     * <action>
     * To receive the fax document.
     *
     * @param session           the phone call's session, device is working with
     * @param target            the stream for saving data of the received fax document in a TIFF format
     * @param pollingMode       flag, to initiate receive of a fax in a polling mode;
     * @param issueVoiceRequest upon termination of receive to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result<p>
     * {@link Result.IO#EOF} - normal end of document transferring<br>
     * {@link Result.CALL#DISCONNECT} - the receiving is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of transfer starting)<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document receiving<br>
     * {@link Result.FAX#POLLING} - the inquiry on polling from the remote fax-device is received<br>
     * {@link Result.FAX#NO_POLL} - the remote fax-device has not accepted inquiry on polling<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-machine is not compatible with device's one
     * @see OperationResultValue
     */
    @Override
    public OperationResultValue receive(final PhoneCallSession<H> session, final OutputStream target,
                                        final boolean pollingMode, final boolean issueVoiceRequest) {
        if (canProceedWith(session)) {
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H faxDeviceHandle = session.parameter(Device.Parameter.FAX_DEVICE_HANDLE);
            // adjusting events producing rules
            serviceProvider.disableEvents(faxDeviceHandle);
            serviceProvider.enableEvents(faxDeviceHandle, Result.CALL.DISCONNECT);
            if (pollingMode) {
                serviceProvider.enableEvents(faxDeviceHandle, Result.FAX.POLLING);
            }
            // staring fax document receiving
            session.getDevice().dispatchEvent("Receiving fax is starting...");
            session.setState(TelephonyDevice.State.RECVFAX);
            try {
                final File tempFile = File.createTempFile(session.getDevice().getName(), ".fax");
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.FAX_TEMPORARY, tempFile);
                // starting fax receiving operation
                final boolean starting = serviceProvider
                        .startFaxReceiving(faxDeviceHandle, tempFile.getAbsolutePath(), issueVoiceRequest);
                if (!starting) {
                    // start fax receiving is failed
                    final String errorReason = "Cannot start FAX receiving.";
                    return receiveFaxError(faxDeviceHandle, tempFile, session, errorReason);
                }
                // start to wait for the operation result
                session.operationResult(Result.NONE);
                // waiting for operation complete event from the events source for particular device handle
                while (session.getState() == TelephonyDevice.State.RECVFAX) {
                    // waiting for an event during the fax document transmit
                    session.waitingForOperationComplete(1000L);
                    final OperationResultValue operationResult = session.operationResult();
                    // checking the operation result value after waiting operation complete
                    //
                    // checking end of file operation results
                    if (operationResult == Result.IO.EOF) {
                        // operation is complete
                        session.getDevice().dispatchEvent("Receive fax document is completed.");
                        // copying received data to the target output stream and deleting temporary file
                        copyReceivedData(tempFile, target);
                        if (tempFile.delete()) {
                            // leaving the fax-document receiving loop
                            break;
                        }
                        // checking for device errors
                    } else if (operationResult == Result.ERROR) {
                        // fax transmitting error is detected
                        final String errorReason = "Receive fax document is failed.";
                        return receiveFaxError(faxDeviceHandle, tempFile, session, errorReason);
                        // checking the operation termination event
                    } else if (session.isTerminated()) {
                        session.getDevice().dispatchEvent("Receive fax document is terminated.");
                        // operation termination is detected
                        stopFaxReceiving(serviceProvider, faxDeviceHandle);
                        // removing unnecessary temp file
                        if (tempFile.delete()) {
                            session.operationResult(Result.TERMINATED);
                            session.setState(Device.State.IDLE);
                        }
                        return Result.TERMINATED;
                        // checking disconnection during the operation
                    } else if (session.isDisconnected()) {
                        session.getDevice().dispatchError("Receive fax document is failed because of phone line disconnection.");
                        // phone line disconnection is detected
                        stopFaxReceiving(serviceProvider, faxDeviceHandle);
                        // removing unnecessary temp file
                        if (tempFile.delete()) {
                            session.operationResult(Result.CALL.DISCONNECT);
                            session.setState(Device.State.ERROR);
                        }
                        return Result.CALL.DISCONNECT;
                        // checking not end-of-file operation results
                    } else if (faxOperationFailed.test(operationResult)) {
                        // checking not end-of-file operation result is detected
                        stopFaxReceiving(serviceProvider, faxDeviceHandle);
                        session.getDevice().dispatchError("Receive fax document is failed.");
                        session.setState(Device.State.ERROR);
                        // removing unnecessary temp file
                        return tempFile.delete() ? operationResult : Result.ERROR;
                    }
                }
                // stop the operation
                stopFaxReceiving(serviceProvider, faxDeviceHandle);
                // make receive fax operation is complete
                session.setState(Device.State.IDLE);
                // removing unnecessary temp file
                return session.operationResult();
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Cannot  receive fax file");
            } catch (InterruptedException e) {
                session.getDevice().dispatchError(e, "Cannot  wait receive fax completion.");
            }
            // disabling any events producing for opened fax resource
            serviceProvider.disableEvents(faxDeviceHandle);
        }
        session.setState(Device.State.ERROR);
        return Result.ERROR;
    }

    /**
     * <action>
     * To transmit the fax document.
     *
     * @param session           the phone call's session, device is working with
     * @param source            stream to fax data
     * @param format            format of data in the stream(resolution is a field)
     * @param issueVoiceRequest upon termination of reception to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * <p>
     * {@link Result.IO#EOF} - normal end of the transmitted document<br>
     * {@link Result.CALL#DISCONNECT} - the transmitting is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of reception or transfer)<br>
     * {@link Result.IO#FORMAT} - the format of the data in the transmitted file is not supported by fax-device<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document transmitting<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-device is not compatible or can't accept a fax with the given resolution<br>
     * @see Fax
     * @see OperationResultValue
     */
    @Override
    public OperationResultValue transmit(PhoneCallSession<H> session, InputStream source, Fax format, boolean issueVoiceRequest) {
        if (isOpened(session)) {
            // staring fax document transmitting
            session.getDevice().dispatchEvent("Transmitting fax is starting...");
            session.setState(TelephonyDevice.State.SENDFAX);
            // getting device service provider
            final TelephonyServiceProvider<H> serviceProvider = deviceCore.getProvider();
            final H faxDeviceHandle = session.parameter(Device.Parameter.FAX_DEVICE_HANDLE);
            // adjusting events producing rules
            serviceProvider.disableEvents(faxDeviceHandle);
            serviceProvider.enableEvents(faxDeviceHandle, Result.CALL.DISCONNECT);
            final File tempFile;
            try {
                tempFile = File.createTempFile(session.getDevice().getName(), ".fax");
                copyTransmittingData(tempFile, source);
                tempFile.deleteOnExit();
                // saving the file for tests purposes
                session.parameter(Parameter.FAX_TEMPORARY, tempFile);
                // starting fax receiving operation
                final boolean starting = serviceProvider.startFaxTransmitting(
                        faxDeviceHandle, tempFile.getAbsolutePath(), issueVoiceRequest,
                        format.isTIFF(), format.isHighResolution(), 0, -1
                );
                if (!starting) {
                    throw new IOException("Cannot start FAX sending");
                }
                // start to wait for the operation result
                session.operationComplete(Result.NONE);
                // waiting for operation complete event from the events source for particular device handle
                while (session.getState() == TelephonyDevice.State.SENDFAX) {
                    // waiting for an event during the fax document transmitting
                    session.waitingForOperationComplete(1000L);
                    final OperationResultValue operationResult = session.operationResult();
                    // checking the operation result value after waiting operation complete
                    if (operationResult == Result.ERROR) {
                        // disabling any events producing for opened fax resource
                        serviceProvider.disableEvents(faxDeviceHandle);
                        // device error is detected
                        onDeviceError(session, "Send fax document is failed.");
                        // unreachable statement
                        return operationResult;
                        // checking end of data operation results
                    } else if (operationResult == Result.IO.EOF) {
                        // operation is complete
                        session.getDevice().dispatchEvent("Send fax document is completed.");
                        // stop fax's transmitting
                        stopFaxTransmitting(serviceProvider, faxDeviceHandle);
                        // deleting temporary file
                        if (tempFile.delete()) {
                            break;
                        }
                    } else if (session.isTerminated()) {
                        // operation termination is detected
                        // stop fax's transmitting
                        stopFaxTransmitting(serviceProvider, faxDeviceHandle);
                        // removing unnecessary temp file
                        if (tempFile.delete()) {
                            session.operationResult(Result.TERMINATED);
                            session.setState(Device.State.IDLE);
                        }
                        return Result.TERMINATED;
                    } else if (session.isDisconnected()) {
                        session.getDevice().dispatchError("Send fax document is failed.");
                        // stop fax's transmitting
                        stopFaxTransmitting(serviceProvider, faxDeviceHandle);
                        session.operationResult(Result.CALL.DISCONNECT);
                        session.setState(Device.State.ERROR);
                        return Result.CALL.DISCONNECT;
                    } else if (faxOperationFailed.test(operationResult)) {
                        session.getDevice().dispatchError("Send fax document is failed.");
                        session.setState(Device.State.ERROR);
                        stopFaxTransmitting(serviceProvider, faxDeviceHandle);
                        return operationResult;
                    }
                }
                // stop fax's transmitting
                stopFaxTransmitting(serviceProvider, faxDeviceHandle);
                // make receive fax operation is complete
                session.setState(Device.State.IDLE);
                return session.operationResult();
            } catch (IOException e) {
                session.getDevice().dispatchError(e, "Cannot send fax file");
            } catch (InterruptedException e) {
                session.getDevice().dispatchError(e, "Cannot wait fax transmitting completion.");
            }
            // disabling any events producing for opened fax resource
            serviceProvider.disableEvents(faxDeviceHandle);
        }
        session.setState(Device.State.ERROR);
        return Result.ERROR;
    }

    /**
     * <action>
     * To create and dispatch the error-type message from the device
     *
     * @param exception   the cause of the error
     * @param description the description of the error
     */
    @Override
    public void dispatchError(Throwable exception, String description) {
        deviceCore.dispatchError(exception, description);
    }

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @param session the phone call's session, device is working with
     * @throws IOException If the device can't terminate current operation
     * @see PhoneCallSession
     * @see TelephonyDevice.State#SENDFAX
     * @see TelephonyDevice.State#RECVFAX
     */
    @Override
    public void terminate(PhoneCallSession<H> session) throws IOException {
        if (isOpened(session)) {
            final DeviceStateValue state = session.getState();
            if (state == TelephonyDevice.State.SENDFAX) {
                terminateOperation(session, handle -> deviceCore.getProvider().stopFaxTransmitting(handle));
            } else if (state == TelephonyDevice.State.RECVFAX) {
                terminateOperation(session, handle -> deviceCore.getProvider().stopFaxReceiving(handle));
            }
            session.terminate();
        }
    }

    /// private methods
    // to check is it possible to do fax operations
    private boolean canProceedWith(PhoneCallSession<H> session) {
        return canFax() && session.isAlive() && isOpened(session);
    }

    // terminate current fax machine active operation
    private void terminateOperation(PhoneCallSession<H> session, Consumer<H> operation) {
        operation.accept(session.parameter(Device.Parameter.FAX_DEVICE_HANDLE));
        session.operationComplete(Result.TERMINATED);
    }

    // copying received fax document from temporary file to the target output stream
    private void copyReceivedData(final File targetFile, final OutputStream target) throws IOException {
        final int DEFAULT_BUFFER_SIZE = 8192;
        try (final InputStream in = new BufferedInputStream(new FileInputStream(targetFile))) {
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                target.write(buffer, 0, read);
            }
        }
    }

    // copying transmitting fax document to the temporary file from the source input stream
    private void copyTransmittingData(final File targetFile, final InputStream source) throws IOException {
        final int DEFAULT_BUFFER_SIZE = 8192;
        try (final InputStream in = new BufferedInputStream(source);
             final OutputStream target = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                target.write(buffer, 0, read);
            }
        }
    }

    // error detected during fax-document receiving
    private OperationResultValue receiveFaxError(H deviceHandle, File tempFile, PhoneCallSession<H> session, String reason) {
        // stopping audio data transmitting by service provider
        stopFaxReceiving(deviceCore.getProvider(), deviceHandle);
        // deleting temporary file
        if (!tempFile.delete()) {
            // session is broken
            session.setState(Device.State.ERROR);
        } else {
            // updating the session's operation result
            session.operationResult(Result.ERROR);
            // throwing DeviceMalfunction error here
            onDeviceError(session, reason);
        }
        return Result.ERROR;
    }

    private static <H> void stopFaxReceiving(TelephonyServiceProvider<H> serviceProvider, H faxDeviceHandle) {
        // stop fax's transmitting
        serviceProvider.stopFaxReceiving(faxDeviceHandle);
        // disabling any events producing for opened fax resource
        serviceProvider.disableEvents(faxDeviceHandle);
    }

    private static <H> void stopFaxTransmitting(TelephonyServiceProvider<H> serviceProvider, H faxDeviceHandle) {
        // stop fax's transmitting
        serviceProvider.stopFaxTransmitting(faxDeviceHandle);
        // disabling any events producing for opened fax resource
        serviceProvider.disableEvents(faxDeviceHandle);
    }
}
