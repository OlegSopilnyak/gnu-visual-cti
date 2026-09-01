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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.core.channel.device.adapter.AbstractGeneralFactory;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Sound;


/**
 * Basic: The Factory of the Devices: The abstract telephony channel-devices factory
 * <p>
 * The parent factory of any type of telephony devices factory.
 * <p>
 *
 * @param <H>  the type of the device's low-level operations handle
 * @param <TD> the type of factory's telephony device
 * @see TelephonyDevice
 * @see TelephonyFactory
 * @see AbstractGeneralFactory
 */
@SuppressWarnings("unchecked")
public abstract class AbstractTelephonyFactory<H, TD extends TelephonyDevice<H, ?>>
        extends AbstractGeneralFactory<H, TD> implements TelephonyFactory<H, TD> {
    // to safeguard the access to the shared device sessions set
    private final Lock sessionsLock = new ReentrantLock();
    // the attribute for the devices factory vendor's name value
    protected String vendor = "AbstractVendor";

    protected AbstractTelephonyFactory(final Executor deviceEventsExecutor, final DeviceEvent.Provider<H> eventsProvider) {
        super(deviceEventsExecutor, eventsProvider);
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
        return vendor;
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
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    @Override
    public void shareDevice(final PhoneCallSession<H> session) {
        if (session.getDevice().canBeConnected()) {
            // device session can be shared
            safeOperation(() -> {
                if (sharedDeviceSessions().stream().noneMatch(session::equals)) {
                    // adding not exists session to shared sessions holder
                    sharedDeviceSessions().add(session);
                    // setting up session's operation result by default
                    TelephonyFactory.super.shareDevice(session);
                    return session;
                }
                return null;
            });
        }
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
        safeOperation(() -> {
            properties.replace(Device.Parameter.SHARED.value(), sharedDeviceSessions().stream()
                    .filter(s -> !s.equals(session))
                    .collect(Collectors.toCollection(LinkedHashSet::new))
            );
            return null;
        });
    }

    /**
     * <finder>
     * To find shared telephony device session for the connection feature
     *
     * @param callableNumber the number to connect to
     * @param master         the session which will capture and join the connectable session
     * @return the ready for connect session or empty if not exists
     * @see Optional
     * @see PhoneCallSession
     * @see PhoneCall.Number
     * @see TelephonyDevice#connect(PhoneCallSession, PhoneCall.Number, int, Sound)
     */
    @Override
    public Optional<PhoneCallSession<H>> findConnectableFor(final PhoneCall.Number callableNumber,
                                                            final PhoneCallSession<H> master) {
        return Optional.ofNullable(safeOperation(() -> lookUpForPhoneNumber(callableNumber, master)));
    }

    /**
     * <accessor>
     * to get the default configuration for factory's device
     *
     * @return the default configuration for factory's device
     * @see Element
     * @see #loadFactoryConfiguration()
     */
    @Override
    public Element defaultDeviceXml() {
        final Element rootElement = new Element(Device.DEFAULT_ROOT);
        rootElement.addContent(defaultNetworkXml());
        rootElement.addContent(defaultMediaXml());
        return rootElement;
    }

    /**
     * <accessor>
     * To get the device's default codec for recording depends on the vendor
     *
     * @return default codec instance for recording
     * @implNote Should be redefined in the vendor's factory implementation
     */
    protected Audio defaultRecordCodec() {
        return Audio.LINEAR_16;
    }

    /**
     * <accessor>
     * To get the device's default codec for playing back depends on the vendor
     *
     * @return default codec instance for recording
     */
    protected Audio defaultPlaybackCodec() {
        return Audio.LINEAR_16;
    }

    // preparing network xml-section of the telephony device
    private Element defaultNetworkXml() {
        return new Element(TelephonyDevice.DEVICE_NETWORK_ROOT)
                .addContent(allowedParameter(CallsPortEngine.Parameter.ACCEPT_CALL_ALLOWED))
                .addContent(allowedParameter(CallsPortEngine.Parameter.MAKE_CALL_ALLOWED))
                .addContent(allowedParameter(CallsPortEngine.Parameter.SHARE_CALL_PORT_ALLOWED))
                .addContent(defaultOriginNumber());
    }

    // prepare boolean xml-parameter
    private Element allowedParameter(final Device.ParameterName parameterName) {
        return ConfigurationParameter.of(parameterName.value(), true).getXml();
    }

    // prepare default origin phone number xml-parameter
    private Element defaultOriginNumber() {
        return ConfigurationParameter.of(
                CallsPortEngine.Parameter.ORIGIN.value(),
                PhoneNumber.domesticOf(123).toString()
        ).getXml();
    }

    // preparing media xml-section of the telephony device
    private Element defaultMediaXml() {
        return new Element(TelephonyDevice.DEVICE_MEDIA_ROOT)
                // tones definitions
                .addContent(toneDefinition(ToneId.DIAL, "250,400,125,400,125,0,0,0,0,0"))
                .addContent(toneDefinition(ToneId.BUSY, "253,500,200,0,0,55,40,55,40,4"))
                .addContent(toneDefinition(ToneId.RINGBACK, "254,450,150,0,0,150,100,550,400,0"))
                .addContent(toneDefinition(ToneId.DISCONNECT, "257,900,700,0,0,90,70,90,70,2"))
                .addContent(formatDefinition(MultimediaEngine.Parameter.RECORD_CODEC, defaultRecordCodec()))
                .addContent(formatDefinition(MultimediaEngine.Parameter.PLAYBACK_CODEC, defaultPlaybackCodec()))
                ;
    }

    // prepare tone xml-parameter
    private Element toneDefinition(final ToneId toneId, final String definition) {
        return new Element(TelephonyDevice.DEVICE_MEDIA_TONE_ROOT)
                .setAttribute(TelephonyDevice.DEVICE_MEDIA_TONE_NAME_ATTRIBUTE, toneId.toString().toLowerCase())
                .setAttribute(TelephonyDevice.DEVICE_PARAMETER_VALUE_ATTRIBUTE, definition);
    }

    // prepare tone xml-parameter
    private Element formatDefinition(final Device.ParameterName name, Audio format) {
        return new Element(TelephonyDevice.DEVICE_MEDIA_CODEC_ROOT)
                .setAttribute(TelephonyDevice.DEVICE_PARAMETER_TYPE_ATTRIBUTE, name.value().toLowerCase())
                .setAttribute(TelephonyDevice.DEVICE_PARAMETER_VALUE_ATTRIBUTE, format.toString());
    }

    /**
     * <builder>
     * To make the channel for device
     *
     * @param device channel to build for
     * @return built channel
     */
    @Override
    protected abstract TelephonyChannel<TD> makeChannelFor(Device<?, ?> device);

    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractTelephonyFactory && equals((AbstractTelephonyFactory<H, TD>) o);
    }

    public boolean equals(AbstractTelephonyFactory<H, TD> that) {
        return Objects.equals(vendor, that.vendor) && super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getVendor());
    }

    /// private methods
    // to do safe the related to sessions collection operation
    private PhoneCallSession<H> safeOperation(Supplier<PhoneCallSession<H>> operation) {
        // trying to lock the access shared sessions collection
        sessionsLock.lock();
        try {
            // doing operation with shared sessions collection
            return operation.get();
        } finally {
            // freeing the access the shared sessions collection
            sessionsLock.unlock();
        }
    }

    // to get the instance of factory's shared device sessions set
    private Set<PhoneCallSession<H>> sharedDeviceSessions() {
        final String sharedSessions = Device.Parameter.SHARED.value();
        return (Set<PhoneCallSession<H>>) this.properties.computeIfAbsent(sharedSessions, pn -> new LinkedHashSet<>());
    }

    // looking for the shared session which can allow to play as second one for the telephony call by phone call connection feature
    private PhoneCallSession<H> lookUpForPhoneNumber(final PhoneCall.Number targetPhoneNumber,
                                                     final PhoneCallSession<H> master) {
        // looking for session among live sessions
        final PhoneCallSession<H> aliveSession = aliveSessionWithPhoneNumber(targetPhoneNumber);
        if (Optional.ofNullable(aliveSession).isPresent()) {
            // returning the slave session to connect with
            return aliveSession;
        }
        // looking for session among free and disconnected sessions
        final PhoneCallSession<H> disconnectedSession = capturedFreeSession(master);
        if (Optional.ofNullable(disconnectedSession).isPresent()) {
            // preparing the captive, free and disconnected session for the further capturing by leader session
            // the session was captive by itself releasing it
            disconnectedSession.release(disconnectedSession);
            // returning the slave session to connect with
            return disconnectedSession;
        }
        // nothing is found
        return null;
    }

    // to look for the session with is alive and has the callable number inside
    private PhoneCallSession<H> aliveSessionWithPhoneNumber(final PhoneCall.Number number) {
        return liveSessionsWith(session -> session.hasNumber(number)).findFirst().orElse(null);
    }

    private Stream<PhoneCallSession<H>> liveSessionsWith(final Predicate<PhoneCallSession<H>> predicate) {
        return sharedDeviceSessions().stream().filter(PhoneCallSession::isAlive).filter(predicate);
    }

    // to look for the session which is not alive and captured for the dedicated usage
    private PhoneCallSession<H> capturedFreeSession(final PhoneCallSession<H> master) {
        return disconnectedSessionsWith(master, session -> session.capture(session))
                .findFirst().orElse(null);
    }

    private Stream<PhoneCallSession<H>> disconnectedSessionsWith(final PhoneCallSession<H> master,
                                                                 final Predicate<PhoneCallSession<H>> predicate) {
        return sharedDeviceSessions().stream()
                .filter(session -> session != master).filter(PhoneCallSession::isDisconnected)
                .filter(predicate);
    }
}
