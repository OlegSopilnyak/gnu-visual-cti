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
package org.visualcti.core.channel.telephony.operation.adapter;

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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
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
    /**
     * Enumeration: Parameter names for telephony device activity
     */
    public enum Parameter implements Device.ParameterName {
        SHARED("SHARED-SESSION-LOCK"),
        CAPTURE("SHARED-SESSION-INVADER"),
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

    // predicate to be sure that device is delivered to the correct events listener(phone-call-session)
    private final Predicate<DeviceEvent<?>> thisSessionEvent = event ->
            event != DeviceEvent.EMPTY
                    && Objects.equals(event.getDeviceName(), getDeviceName())
                    && hasDeviceHandle((H) event.getDeviceHandle());

    protected PhoneCallSession(TelephonyDevice<H, ?> deviceOwner, H deviceHandle) {
        super(deviceOwner, deviceHandle);
        parameter(Parameter.JOINT, Collections.emptyList());
        // the parameter value of device operation result
        parameter(Device.Parameter.RESULT, Result.NONE);
        // the parameter for shared session lock
        parameter(Parameter.SHARED, new ReentrantLock());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof PhoneCallSession && equals((PhoneCallSession<H>) o);
    }

    public boolean equals(final PhoneCallSession<H> that) {
        return Objects.equals(getDevice(), that.getDevice())
                && Objects.equals(getDeviceHandle(), that.getDeviceHandle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDevice(), getDeviceHandle());
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
     * @see DeviceActivitySession#getDeviceName()
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
     * @see DeviceActivitySession#isAlive()
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
    public void alive(final boolean alive) {
        parameter(Device.Parameter.ALIVE, alive);
    }

    /**
     * <checker>
     * Whether context's device handle valid or not
     *
     * @return true if device handle value is valid
     * @see #isOpened()
     */
    @Override
    protected boolean isValidDeviceHandle() {
        return device != null && !device.isInvalidHandle(getDeviceHandle());
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
     * Capturing the device's session for dedicate usage it in the sessions' joining feature of device
     *
     * @param invader the phone call session which is trying to capture
     * @return true if capture is successful
     * @see Lock
     * @see #isCaptive()
     * @see #parameter(Device.ParameterName)
     * @see Parameter#SHARED
     * @see Parameter#CAPTURE
     * @see #getDeviceName()
     */
    public boolean capture(final PhoneCallSession<H> invader) {
        if (isCaptive()) {
            // the session is already captive by capture()
            return false;
        } else {
            // capturing the session
            this.<Lock>parameter(Parameter.SHARED).lock();
            // storing the reference to the session-invader which will capture the session
            this.parameter(Parameter.CAPTURE, invader.getDeviceName());
            return true;
        }
    }

    /**
     * <checker>
     * Checking is the session captive
     *
     * @return true if session already captive by capture()
     * @see #capture(PhoneCallSession)
     * @see ReentrantLock#isLocked()
     * @see #parameter(Device.ParameterName)
     * @see Parameter#SHARED
     */
    public boolean isCaptive() {
        // checking the state of the telephony device session lock
        return this.<ReentrantLock>parameter(Parameter.SHARED).isLocked();
    }

    /**
     * <accessor>
     * To get the invader of the phone call session capture
     *
     * @return the name of the invader or empty
     * @see #capture(PhoneCallSession)
     * @see Optional
     */
    public Optional<String> captiveBy() {
        return Optional.ofNullable(parameter(Parameter.CAPTURE));
    }

    /**
     * <action>
     * Free the captive phone call session using the session-invader potentially has taken part in capturing of the session
     *
     * @param invader the phone call session which is captive the session
     * @see #capture(PhoneCallSession)
     * @see Lock
     * @see #parameter(Device.ParameterName)
     * @see Parameter#SHARED
     */
    public void release(final PhoneCallSession<H> invader) {
        if (Objects.equals(invader.getDeviceName(), this.<String>parameter(Parameter.CAPTURE))) {
            // clearing the invader reference which captive the session
            remove(Parameter.CAPTURE);
            // releasing the captive phone call session
            this.<Lock>parameter(Parameter.SHARED).unlock();
        }
    }

    /**
     * <accessor>
     * To get phone calls joint by device connection feature
     *
     * @return the stream of joint with this session other phone-call-sessions
     * @see #join(PhoneCall)
     * @see #detach(PhoneCall)
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
     * @see #joint()
     */
    @Override
    public void join(final PhoneCall anotherCall) {
        final List<PhoneCall> joint = new ArrayList<>(parameterOrDefault(Parameter.JOINT, Collections.emptyList()));
        if (!joint.contains(anotherCall)) {
            // adding another phone call to the joint collection
            joint.add(anotherCall);
            // saving updated joint sessions collection
            parameter(Parameter.JOINT, Collections.unmodifiableList(joint));
            // joining the phone call session to the added one
            anotherCall.join(this);
        }
    }

    /**
     * <mutator>
     * To detach the phone-call-session
     *
     * @param jointCall another session value
     * @see #joint()
     */
    @Override
    public void detach(final PhoneCall jointCall) {
        final List<PhoneCall> joint = new ArrayList<>(parameterOrDefault(Parameter.JOINT, Collections.emptyList()));
        // looking for the phone call session to detach, among joint ones
        final int index = joint.indexOf(jointCall);
        if (index >= 0) {
            // there is the joint phone call session, let's detach it from joint ones
            final PhoneCall detached = joint.remove(index);
            if (detached != null) {
                final PhoneCallSession<H> slave = (PhoneCallSession<H>) detached;
                if (slave.isCaptive()) {
                    // breaking the low-level connection
                    getDevice().getProvider().breakConnection(slave.getDeviceHandle(), getDeviceHandle());
                    // releasing the slave resource if any
                    slave.release(this);
                }
                // saving updated joint sessions collection
                parameter(Parameter.JOINT, Collections.unmodifiableList(joint));
                // detaching the phone call session from the detached one
                detached.detach(this);
            }
        }
    }

    /**
     * Closes this PhoneCall session and releases any system resources associated with it.
     * If the session is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (device != null) {
            // detaching joint phone-calls
            detachAll();
            // releasing common using session's parameters
            super.close();
        }
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
            // the telephony operation's event accepted, processing it
            proceedDeviceSpecificEvent((DeviceEvent<H>) event);
        } else if (event.getEventType() == DeviceEvent.Type.MALFUNCTION) {
            // signaling about device's malfunction
            device.dispatchError(String.valueOf(event.getDescription()));
            // the malfunction is detected during device's operation
            operationComplete(Result.ERROR);
        }
        // accepted event is processed well
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
     * @see Result#TERMINATED
     * @see Result.CALL#DISCONNECT
     * @see Result.IO#DTMF
     * @see #operationComplete(OperationResultValue)
     */
    protected void proceedDeviceSpecificEvent(final DeviceEvent<H> event) {
        final Optional<OperationResultValue> eventReason = event.getOption(DeviceEvent.Option.REASON);
        if (eventReason.isPresent()) {
            // getting the event's reason
            final OperationResultValue reason = eventReason.get();
            // analyze it
            if (reason == Result.CALL.RINGS) {
                // detected incoming telephony call
                incomingCallIsDetected(event);
            } else if (reason == Result.TERMINATED) {
                // terminating the current operation
                operationTerminationIsDetected();
            } else if (reason == Result.CALL.DISCONNECT) {
                // disconnect detected in the current operation
                operationDisconnectIsDetected();
            } else if (reason == Result.IO.DTMF) {
                // user input detected in the current operation
                userInputDetected(event);
            } else {
                // other event types just completing the operation which is waiting for complete
                operationComplete(reason);
            }
        }
    }

    /// private methods
    // detected incoming telephony call, accepted event with 'rings' reason
    private void incomingCallIsDetected(final DeviceEvent<H> event) {
        final DeviceStateValue currentState = getState();
        if (currentState == WAIT) {
            // detected incoming call event for session in WAIT state
            // completing the operation which wait for complete if any
            operationComplete(Result.CALL.RINGS);
        } else if (currentState == IDLE && isDisconnected()) {
            // detected incoming call event for session in IDLE state
            // and phone call isn't alive as well
            // rethrowing the device event as incoming one
            getDevice().getFactory().onDeviceEvent(incoming(event));
        }
    }

    // detected external termination, accepted event with 'terminate' reason
    private void operationTerminationIsDetected() {
        try {
            // trying to terminate current operation
            getDevice().terminate(this);
        } catch (IOException e) {
            getDevice().dispatchError(e, "Cannot process termination event");
        }
    }

    // disconnect detected in the current operation, accepted event with 'disconnect' reason
    private void operationDisconnectIsDetected() {
        alive(false);
        // completing the operation which wait for complete if any
        operationComplete(Result.CALL.DISCONNECT);
    }

    // user input detected in the current operation, accepted event with 'dtmf' reason
    private void userInputDetected(final DeviceEvent<H> event) {
        // getting user input from the event and store it to the parameter of phone-call-session
        this.parameter(Device.Parameter.USER_INPUT, parameterOrDefault(Device.Parameter.USER_INPUT, "")
                + event.<String>getOption(DeviceEvent.Option.INPUT).orElse(""));
        // completing the operation which wait for complete if any
        operationComplete(Result.IO.DTMF);
    }
}
