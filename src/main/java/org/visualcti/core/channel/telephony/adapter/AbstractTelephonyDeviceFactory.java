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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.adapter.AbstractFactory;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyDeviceFactory;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.media.Sound;


/**
 * The Abstract Factory of the Telephony Devices: The factory of the telephony channel-devices
 *
 * @param <H> the type of the device's low-level operations handle
 * @param <D> the type of factory's devices
 * @see TelephonyDevice
 * @see TelephonyDeviceFactory
 */
public abstract class AbstractTelephonyDeviceFactory<H, D extends TelephonyDevice<H, ?>>
        extends AbstractFactory<H, D> implements TelephonyDeviceFactory<H, D> {
    // the set of the shared telephony device sessions
    private final Set<PhoneCallSession<H>> sharedDeviceSessions = new HashSet<>();
    private final Lock sessionsLock = new ReentrantLock();
    // the executer for expired sessions closing
    private final ScheduledExecutorService sharedSessionCloser;
//    = Executors.newSingleThreadScheduledExecutor();

    protected AbstractTelephonyDeviceFactory(final ScheduledExecutorService sharedSessionCloser,
                                             final Executor deviceEventExecutor,
                                             final DeviceEvent.Provider<H> eventsProvider) {
        super(deviceEventExecutor, eventsProvider);
        this.sharedSessionCloser = sharedSessionCloser;
    }

    /**
     * <accessor>
     * get access to factory's vendor name
     *
     * @return vendor's name
     * @see Factory#getName()
     */
    @Override
    public String getVendor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <accessor>
     * get access to factory's version
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <action>
     * To share opened phone call session for the connection feature
     *
     * @param session the phone call's session, device is working with
     * @param delay   maximum time (milliseconds) of device session sharing or forever for negative value,
     *                waiting for usage in connect(...) feature
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    @Override
    public void shareDevice(PhoneCallSession<H> session, long delay) {
        safeOperation(() -> {
            if (!sharedDeviceSessions.contains(session)) {
                sharedDeviceSessions.add(session);
                if (delay > 0) {
                    sharedSessionCloser.schedule(() -> unShareDevice(session), delay, TimeUnit.MILLISECONDS);
                }
            }
            return session;
        });
    }

    /**
     * <action>
     * To un-share opened phone call session for the connection feature
     *
     * @param session the phone call's session, device is working with
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    @Override
    public void unShareDevice(PhoneCallSession<H> session) {
        safeOperation(() -> sharedDeviceSessions.remove(session) ? session : null);
    }

    /**
     * <finder>
     * To find telephony device session for the connection feature by phone number
     *
     * @param callableNumber the number to connect to
     * @return the ready for connect session or empty if not exists
     * @see Optional
     * @see PhoneCallSession
     * @see PhoneCall.Number
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    @Override
    public Optional<PhoneCallSession<H>> findConnectableFor(final PhoneCall.Number callableNumber) {
        return Optional.ofNullable(safeOperation(() -> lookForNumber(callableNumber)));
    }

    /**
     * <builder>
     * To make the channel for device
     *
     * @param device channel to build for
     * @return built channel
     */
    @Override
    protected abstract TelephonyChannel<D> makeChannelFor(Device<?, ?> device);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractTelephonyDeviceFactory)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /// private methods
    // to do safe the related to sessions collection operation
    private PhoneCallSession<H> safeOperation(Supplier<PhoneCallSession<H>> operation) {
        sessionsLock.lock();
        try {
            return operation.get();
        } finally {
            sessionsLock.unlock();
        }
    }

    // looking for the shared session which can allow to play as second one for the telephony call by phone call connection feature
    private PhoneCallSession<H> lookForNumber(final PhoneCall.Number callableNumber) {
        // looking for among live sessions
        final PhoneCallSession<H> aliveSession = aliveSessionWith(callableNumber);
        if (Optional.ofNullable(aliveSession).isPresent()) {
            return aliveSession;
        } else {
            final PhoneCallSession<H> waitForCallSession =
                    sessionWith(session -> session.getState() == TelephonyDevice.State.WAIT);
            if (Optional.ofNullable(waitForCallSession).isPresent()) {
                return waitForCallSession;
            }
            final PhoneCallSession<H> connectableSession = sessionWith(session -> true);
            if (Optional.ofNullable(connectableSession).isPresent()) {
                return connectableSession;
            }
        }
        return null;
    }

    // to look for session with is alive and has the callable number inside
    private PhoneCallSession<H> aliveSessionWith(final PhoneCall.Number number) {
        return sharedDeviceSessions.stream()
                .filter(session -> session.getDevice().canBeConnected()
                        && session.isAlive() && session.hasNumber(number)
                ).findFirst().orElse(null);
    }

    // to look for session which can be used in the connection feature
    private PhoneCallSession<H> sessionWith(final Predicate<PhoneCallSession<H>> predicate) {
        return sharedDeviceSessions.stream()
                .filter(session -> predicate.test(session)
                        && session.getDevice().canBeConnected() && session.getDevice().canMakeCall()
                ).findFirst().orElse(null);
    }
}
