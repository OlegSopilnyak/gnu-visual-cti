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

import java.io.IOException;
import java.net.URL;
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
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.adapter.AbstractGeneralFactory;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.ToneId;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.media.Audio;
import org.visualcti.media.Sound;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.unit.ServerUnitAdapter;


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

    protected AbstractTelephonyFactory(final Executor deviceEventsExecutor, final TelephonyServiceProvider<H> provider) {
        super(deviceEventsExecutor, provider);
    }

    /**
     * <accessor>
     * To get access to the telephony device's service provider
     *
     * @return the device's service provider reference
     */
    @Override
    public TelephonyServiceProvider<H> getProvider() {
        return (TelephonyServiceProvider<H>) super.getProvider();
    }

    /**
     * <converter>
     * <applier>
     * To apply configuration parameter of the server unit
     *
     * @param parameter the unit parameter to apply
     * @see ConfigurationParameter
     * @see ServerUnitAdapter#processParameter(ConfigurationParameter)
     * @see ServerUnit.Builder#build(Element)
     */
    @Override
    protected void applyUnitParameter(final ConfigurationParameter parameter) {
        switch (parameter.getName()) {
            case VENDOR_PARAMETER_NAME:
                // the configuration parameter of the vendor's name
                setVendor(parameter.getValue());
                // cleaning cached unit xml-configuration
                this.unitConfiguration = null;
                break;
            case CONFIGURATION_URL_PARAMETER_NAME:
                // the configuration parameter of the devices-configurations-file URL from the vendor
                loadVendorConfigurationFrom(parameter.getValue());
                // cleaning cached unit xml-configuration
                this.unitConfiguration = null;
                break;
            case VENDOR_FACTORY_VERSION_PARAMETER_NAME:
                vendorVersion = parameter.getValue();
                // cleaning cached unit xml-configuration
                this.unitConfiguration = null;
                break;
        }
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @return the instance of the class that extends server unit main class
     * @see ServerUnit#getUnitExtendsClass()
     * @see TelephonyFactory
     */
    @Override
    public Class<? extends ServerUnit> getUnitExtendsClass() {
        return TelephonyFactory.class;
    }

    /**
     * <converter>
     * To represent the entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see ServerUnitAdapter#getXML()
     */
    @Override
    public Element getXML() {
        return this.unitConfiguration = super.getXML();
    }

    /**
     * <converter>
     * To represent the parameters of the unit as an XML element
     * Here managed the icon of the server unit
     *
     * @param rootElement building from unit XML Element
     * @see Element
     * @see ServerUnitAdapter#prepareUnitParametersXML(Element)
     */
    protected void prepareUnitParametersXML(Element rootElement) {
        // saving default unit parameters
        super.prepareUnitParametersXML(rootElement);
        // saving the vendor's name parameter
        rootElement.addContent(ConfigurationParameter.of(VENDOR_PARAMETER_NAME, getVendor()).getXml());
        // saving the vendor's configuration file url parameter
        rootElement.addContent(
                ConfigurationParameter.of(CONFIGURATION_URL_PARAMETER_NAME, "file:" + configurationFilePath()).getXml()
        );
        // checking the vendor's devices-factory version
        final String vendorFactoryVersion = getVersion();
        if (Objects.equals(vendorFactoryVersion, VENDOR_FACTORY_DEFAULT_VERSION)) {
            // vendor's devices-factory version is default
            return;
        }
        // saving the vendor's devices-factory version
        rootElement.addContent(
                ConfigurationParameter.of(VENDOR_FACTORY_VERSION_PARAMETER_NAME, vendorFactoryVersion).getXml()
        );
    }

    /**
     * <action>
     * To share opened phone-call-session for the connection feature
     *
     * @param session the phone-call-session instance the device is working with
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
     * To un-share opened phone-call-session for the connection feature
     *
     * @param session the phone-call-session instance the device is working with
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
     * To find a shared telephony device session for the connection feature
     *
     * @param callableNumber the number to connect to
     * @param master         the session that will capture and join the connectable session
     * @return the ready for connection session or empty if not exists
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
     * @see #defaultNetworkXml()
     * @see #defaultMediaXml()
     */
    @Override
    public Element defaultDeviceXml() {
        final Element rootElement = new Element(Device.DEFAULT_ROOT);
        rootElement.addContent(defaultNetworkXml());
        rootElement.addContent(defaultMediaXml());
        return rootElement;
    }

    /**
     * <builder>
     * To prepare the network xml-section of the telephony device
     *
     * @return prepared telephony device network configuration-xml-section
     * @see Element
     * @see #defaultDeviceXml()
     * @see #allowedParameter(Device.ParameterName)
     */
    protected Element defaultNetworkXml() {
        return new Element(TelephonyDevice.DEVICE_NETWORK_ROOT)
                .addContent(allowedParameter(CallsPortEngine.Parameter.ACCEPT_CALL_ALLOWED))
                .addContent(allowedParameter(CallsPortEngine.Parameter.MAKE_CALL_ALLOWED))
                .addContent(allowedParameter(CallsPortEngine.Parameter.SHARE_CALL_PORT_ALLOWED))
                .addContent(defaultOriginNumber());
    }

    /**
     * <builder>
     * To prepare the media xml-section of the telephony device
     *
     * @return prepared telephony device media configuration-xml-section
     * @see Element
     * @see #defaultDeviceXml()
     * @see #toneDefinition(ToneId, String)
     * @see #formatDefinition(Device.ParameterName, Audio)
     */
    protected Element defaultMediaXml() {
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

    /**
     * <accessor>
     * To get the device's default codec for recording depends on the vendor
     *
     * @return default codec instance for recording the audio
     * @implNote Should be redefined in the vendor's factory implementation
     */
    protected Audio defaultRecordCodec() {
        return Audio.LINEAR_16;
    }

    /**
     * <accessor>
     * To get the device's default codec for playing back depends on the vendor
     *
     * @return default codec instance for playing back the audio
     * @implNote Should be redefined in the vendor's factory implementation
     */
    protected Audio defaultPlaybackCodec() {
        return Audio.LINEAR_16;
    }

    /**
     * <builder>
     * To make the channel for the device
     *
     * @param device channel to build for
     * @return built channel
     */
    @Override
    protected abstract TelephonyChannel<TD> makeChannelFor(Device<?, ?> device);

    ///
    /// private methods
    // loading the configurations of devices of the vendor from the URL
    private void loadVendorConfigurationFrom(final String vendorConfigurationURL) {
        try {
            this.configurationFileName = new URL(vendorConfigurationURL).getFile();
            loadFactoryConfiguration();
        } catch (IOException | DataConversionException e) {
            dispatchError(e, "Cannot load vendor configuration file: " + vendorConfigurationURL);
        }
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
