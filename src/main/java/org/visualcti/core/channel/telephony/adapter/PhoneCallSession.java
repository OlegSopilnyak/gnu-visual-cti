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
package org.visualcti.core.channel.telephony.adapter;

import static org.visualcti.core.channel.device.Device.State.IDLE;
import static org.visualcti.core.channel.device.adapter.AbstractDeviceEvent.incoming;
import static org.visualcti.core.channel.telephony.TelephonyDevice.State.WAIT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.DeviceStateValue;
import org.visualcti.core.channel.device.adapter.AbstractDeviceSession;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.Result;

/**
 * Implementation: Phone Call: Keep all information about phone call
 *
 * @param <H> the type of the device's low-level operations handle
 * @see PhoneCall
 * @see AbstractDeviceSession
 */
@SuppressWarnings("unchecked")
public abstract class PhoneCallSession<H> extends AbstractDeviceSession<H> implements PhoneCall {
    // predicate to be sure that device is delivered to the correct events listener(phone-call-session)
    private final Predicate<DeviceEvent<?>> thisSessionEvent = event -> event != DeviceEvent.EMPTY
            && Objects.equals(event.getDeviceName(), getDeviceName())
            && Objects.equals(event.getDeviceHandle(), getDeviceHandle());

    protected PhoneCallSession(TelephonyDevice<H, ?> deviceOwner, H deviceHandle) {
        super(deviceOwner, deviceHandle);
        parameter(Parameter.JOINT, Collections.emptyList());
    }

    /**
     * <accessor>
     * To get access to device-owner of the context
     *
     * @return the device-owner reference
     */
    @Override
    public TelephonyDevice<H, ?> getDevice() {
        return (TelephonyDevice<H, ?>) super.getDevice();
    }

    /**
     * <accssor>
     * To get the device name where the call is appeared
     *
     * @return the value
     * @see PhoneCall#getDeviceName()
     */
    @Override
    public String getDeviceName() {
        return super.getDeviceName();
    }

    /**
     * <accssor>
     * To check up the condition of a telephone call
     *
     * @return true if the call is in service
     * @see PhoneCall#isAlive()
     */
    @Override
    public boolean isAlive() {
        return super.isAlive();
    }

    /**
     * <mutator>
     * To set up the alive flag
     *
     * @param alive new value
     * @see OperationResultValue
     * @see #isAlive()
     */
    public void alive(boolean alive) {
        parameter(Device.Parameter.ALIVE, alive);
    }

    /**
     * <accssor>
     * To get access to the result of the operation that initiated or updated the call
     *
     * @return the value
     * @see OperationResultValue
     * @see PhoneCall#operationResult()
     * @see #operationResult(OperationResultValue)
     */
    @Override
    public OperationResultValue operationResult() {
        return parameter(Parameter.RESULT);
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
    public PhoneCallSession<H> operationResult(OperationResultValue operationResult) {
        parameter(Parameter.RESULT, operationResult);
        return this;
    }

    /**
     * <accssor>
     * To get the called number of the call
     *
     * @return the value
     * @see Number
     * @see PhoneCall#getCalledNumber()
     */
    @Override
    public Number getCalledNumber() {
        return parameterOrDefault(Parameter.CALLED, Number.EMPTY);
    }

    /**
     * <mutator>
     * To set up the called number value of the call
     *
     * @param calledNumber new value
     * @return updated phone call instance
     * @see Number
     * @see #getCalledNumber()
     */
    public PhoneCallSession<H> calledNumber(Number calledNumber) {
        parameter(Parameter.CALLED, calledNumber);
        return this;
    }

    /**
     * <accssor>
     * To get the calling number of the call
     *
     * @return the value
     * @see Number
     * @see PhoneCall#getCallingNumber()
     */
    @Override
    public Number getCallingNumber() {
        return parameterOrDefault(Parameter.CALLING, Number.EMPTY);
    }

    /**
     * <mutator>
     * To set up the calling number value of the call
     *
     * @param callingNumber new value
     * @return updated phone call instance
     * @see Number
     * @see #getCallingNumber()
     */
    public PhoneCallSession<H> callingNumber(Number callingNumber) {
        parameter(Parameter.CALLING, callingNumber);
        return this;
    }

    /**
     * <action>
     * To wait the running operation complete or timeout
     *
     * @param timeout how long to wait
     * @throws InterruptedException if operation is interrupted outside
     * @see #operationComplete(OperationResultValue)
     * @see CountDownLatch
     * @see CountDownLatch#await(long, TimeUnit)
     * @see CountDownLatch#await()
     */
    @Override
    public void waitForOperationComplete(long timeout) throws InterruptedException {
        // preparing running operation's latch
        final CountDownLatch latch = new CountDownLatch(1);
        parameter(Parameter.LATCH, latch);
        operationResult(Result.NONE);
        if (timeout > 0) {
            // start waiting until operation complete or timeout
            if (latch.await(timeout, TimeUnit.MILLISECONDS)) {
                // latch was notified outside
                device.dispatchEvent("Operation was completed");
            } else {
                // latch wasn't notified outside
                device.dispatchEvent("Operation was timed out");
            }
        } else {
            // start waiting until operation complete no limits
            latch.await();
        }
    }

    /**
     * <action>
     * To notify about the previously running in the phone-call-session operation is completed
     *
     * @see #waitForOperationComplete(long)
     * @see CountDownLatch
     * @see CountDownLatch#countDown()
     */
    @Override
    public void operationComplete(final OperationResultValue completionReason) {
        // updating session's operation result
        operationResult(completionReason);
        // completing the running operation
        final CountDownLatch completeOperationLatch = remove(Parameter.LATCH);
        if (completeOperationLatch != null) {
            // releasing the latch of running operation
            completeOperationLatch.countDown();
        }
    }

    /**
     * <accessor>
     * To get phone calls joint by device connection feature
     *
     * @return the stream of joint with this session other phone-call-sessions
     * @see #join(PhoneCall)
     */
    @Override
    public Stream<PhoneCall> joint() {
        final Collection<PhoneCall> joint = parameterOrDefault(Parameter.JOINT, Collections.emptyList());
        return joint.stream();
    }

    /**
     * <mutator>
     * To join another phone-call-session
     *
     * @param anotherCall another session value
     */
    @Override
    public void join(PhoneCall anotherCall) {
        final List<PhoneCall> joint = new ArrayList<>(parameterOrDefault(Parameter.JOINT, Collections.emptyList()));
        joint.add(anotherCall);
        parameter(Parameter.JOINT, Collections.unmodifiableList(joint));
    }

    /**
     * Closes this PhoneCall session and releases any system resources associated
     * with it. If the session is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        // terminate current operation if it's waiting
        operationComplete(Result.TERMINATED);
        // closing joint phone call sessions
        final List<PhoneCall> joint = new ArrayList<>(parameterOrDefault(Parameter.JOINT, Collections.emptyList()));
        parameter(Parameter.JOINT, Collections.emptyList());
        joint.forEach(PhoneCallSession::closeJoint);
        // releasing common using session's parameters
        super.close();
    }

    /**
     * <action>
     * Whether the given event is accepted by this listener.
     *
     * @param event the fired Event
     * @return true if the event accepted for the processing
     * @see DeviceEvent
     * @see #thisSessionEvent
     * @see DeviceEvent.Type#DEVICE_SPECIFIC
     * @see #proceedDeviceSpecificEvent(DeviceEvent)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean accept(final DeviceEvent<?> event) {
        // checking is the device event comply with the session's attributes
        if (thisSessionEvent.negate().test(event)) {
            // the event isn't for the phone call as an events listener
            return false;
        }
        // analyzing the device event
        if (event.getEventType() == DeviceEvent.Type.DEVICE_SPECIFIC) {
            // some telephony operation event received, processing it
            proceedDeviceSpecificEvent((DeviceEvent<H>) event);
        }
        // event processed well
        return true;
    }

    /**
     * <event-processing>
     * To process device specific event
     *
     * @param event the device specific event
     * @see DeviceEvent
     * @see DeviceEvent.Option#REASON
     * @see DeviceEvent#getOption(Device.ParameterName)
     * @see OperationResultValue
     * @see Result.CALL#RINGS
     * @see Result.CALL#DISCONNECT
     * @see Result#TIMEOUT
     * @see Result#TERMINATED
     */
    protected void proceedDeviceSpecificEvent(final DeviceEvent<H> event) {
        final Optional<OperationResultValue> eventReason = event.getOption(DeviceEvent.Option.REASON);
        if (eventReason.isPresent()) {
            // getting event's reason
            final OperationResultValue reason = eventReason.get();
            if (reason == Result.CALL.RINGS) {
                incomingCallDetected(event);
            } else if (reason == Result.CALL.DISCONNECT) {
                disconnectedCallDetected(event);
            } else if (reason == Result.TIMEOUT) {
                timeoutDetected(event);
            } else if (reason == Result.TERMINATED) {
                terminationDetected(event);
            }
        }
    }

    /**
     * Enumeration: Parameter names for telephony device activity
     */
    enum Parameter implements Device.ParameterName {
        RESULT("OPERATION-RESULT"),
        LATCH("OPERATION-LATCH"),
        JOINT("JOINT-SESSIONS-SET"),
        CALLED("PHONE-CALL-CALLED-NUMBER"),
        CALLING("PHONE-CALL-CALLING-NUMBER");
        private final String name;

        Parameter(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name.toLowerCase();
        }
    }

    /// private methods
    // detected incoming telephony call
    private void incomingCallDetected(final DeviceEvent<H> event) {
        final DeviceStateValue currentState = getState();
        if (currentState == WAIT) {
            // detected incoming call event for session in WAIT state
            // completing the wait for call operation
            operationComplete(Result.CALL.RINGS);
        } else if (currentState == IDLE) {
            // detected incoming call event for session in IDLE state
            // rethrowing the device event as incoming one
            getDevice().getFactory().onDeviceEvent(incoming(event));
        }
    }

    // detected telephony call disconnection
    private void disconnectedCallDetected(DeviceEvent<?> event) {

    }

    // telephony operation timeout detected
    private void timeoutDetected(DeviceEvent<?> event) {

    }

    // telephony operation termination detected
    private void terminationDetected(DeviceEvent<?> event) {

    }

    // to close joint phone call
    private static void closeJoint(PhoneCall call) {
        try {
            call.close();
        } catch (IOException e) {
            // just ignoring it
        }
    }
}
