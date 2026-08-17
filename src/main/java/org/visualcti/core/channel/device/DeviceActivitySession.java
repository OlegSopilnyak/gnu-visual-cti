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
package org.visualcti.core.channel.device;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.visualcti.core.channel.device.operation.OperationResultValue;

/**
 * Device Activity Session: The session of device's activity for the task
 *
 * @param <H> the type of the device's low-level operations handle
 */
public interface DeviceActivitySession<H> extends DeviceEvent.Listener, Closeable {
    /**
     * <accessor>
     * To get access to device-owner of the context
     *
     * @return the device-owner reference
     */
    Device<H, ? extends Factory<H, ?>> getDevice();

    /**
     * <accessor>
     * To get access to device's internal name
     *
     * @return device's name
     * @see Device#getName()
     */
    default String getDeviceName() {
        return parameter(Device.Parameter.NAME);
    }

    /**
     * <accessor>
     * To get access to opened device's internal handle
     *
     * @return device's handle
     */
    default H getDeviceHandle() {
        return parameter(Device.Parameter.DEVICE_HANDLE);
    }

    /**
     * <checker>
     * To test whether session has the device's internal handle
     *
     * @return true if session as the device handle
     */
    default boolean hasDeviceHandle(H deviceHandle) {
        return Objects.equals(parameter(Device.Parameter.DEVICE_HANDLE), deviceHandle)
                || Objects.equals(parameter(Device.Parameter.FAX_DEVICE_HANDLE), deviceHandle);
    }

    /**
     * <accessor>
     * Check, is device already opened
     *
     * @return true if it's opened
     */
    default boolean isOpened() {
        return parameterOrDefault(Device.Parameter.OPEN, false);
    }

    /**
     * <accessor>
     * To get access to context's termination flag
     *
     * @return the flag's value
     */
    default boolean isTerminated() {
        return parameterOrDefault(Device.Parameter.TERMINATE, false);
    }

    /**
     * <action>
     * The unconditional termination anyone current active operation:
     * 1. operations with telephony calls (waiting or making call, connect, etc.)
     * 2. exchanges of the data (voice or fax)
     *
     * @throws IOException If the device can't terminate current operation
     */
    void terminate() throws IOException;

    /**
     * <accessor>
     * To get access to the state of the channel-device context
     *
     * @return value of device state
     * @see DeviceStateValue#getValue()
     * @see Device.State
     */
    default DeviceStateValue getState() {
        return parameter(Device.Parameter.STATE);
    }

    /**
     * <mutator>
     * To set up the new state value of the channel-device context
     *
     * @param state new value of device state
     * @see DeviceStateValue#getValue()
     */
    void setState(DeviceStateValue state);

    /**
     * <accssor>
     * To check up the condition of the channel-device context
     *
     * @return true if the device context is in service (connected)
     */
    default boolean isAlive() {
        return parameterOrDefault(Device.Parameter.ALIVE, false);
    }

    /**
     * <action>
     * To wait for the running operation complete or timeout
     *
     * @param timeout how long to wait
     * @throws InterruptedException if operation is interrupted outside
     */
    default void waitingForOperationComplete(long timeout) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <checker>
     * To check is operation in progress (waiting for completion or timeout)
     * For tests purposes
     *
     * @return true if operation is waiting for completion
     * @see #waitingForOperationComplete(long)
     * @see #operationComplete(OperationResultValue)
     */
    default boolean operationIsActive() {
        final CountDownLatch completeOperationLatch = parameter(Device.Parameter.LATCH);
        return completeOperationLatch != null && completeOperationLatch.getCount() > 0;
    }

    /**
     * <action>
     * To notify about the previously running in the phone-call-session operation is completed
     *
     * @param completionReason the reason of the operation's complete
     * @see #waitingForOperationComplete(long)
     */
    default void operationComplete(final OperationResultValue completionReason) {
        // updating session's operation result
        operationResult(completionReason);
        // completing the operation which is waiting for complete
        final CountDownLatch completeOperationLatch = remove(Device.Parameter.LATCH);
        if ((completeOperationLatch) != null) {
            // releasing the latch of running operation
            completeOperationLatch.countDown();
        }
    }

    /**
     * <accssor>
     * To get access to the last result of the operation that initiated or updated the phone call
     *
     * @return the last result value
     * @see OperationResultValue
     */
    default OperationResultValue operationResult() {
        return parameter(Device.Parameter.RESULT);
    }

    /**
     * <mutator>
     * To set up the result of the operation value of the call
     *
     * @param operationResult new value
     * @return updated phone call instance
     * @see OperationResultValue
     * @see #operationResult()
     */
    default DeviceActivitySession<H> operationResult(final OperationResultValue operationResult) {
        parameter(Device.Parameter.RESULT, operationResult);
        return this;
    }

    /**
     * <accessor>
     * To get the session parameter's value
     *
     * @param name the name of the session's parameter
     * @param <T>  the type of the session's parameter value
     * @return the value of the session's parameter
     */
    <T> T parameter(Device.ParameterName name);

    /**
     * <accessor>
     * To get the session parameter's value
     *
     * @param name the name of the session's parameter
     * @param <T>  the type of the session's parameter value
     * @return the value of the session's parameter
     */
    <T> T parameterOrDefault(Device.ParameterName name, T defaultValue);

    /**
     * <mutator>
     * To set up the new session parameter's value
     *
     * @param name  the name of the session's parameter
     * @param value the value of the session's parameter
     * @param <T>   the type of the session's parameter value
     * @return reference to the updated session
     */
    <T> DeviceActivitySession<H> parameter(Device.ParameterName name, T value);

    /**
     * <mutator>
     * To remove the session parameter's value
     *
     * @param name the name of the session's parameter
     * @param <T>  the type of the session's parameter value
     * @return previous parameter's value
     */
    <T> T remove(Device.ParameterName name);
}

