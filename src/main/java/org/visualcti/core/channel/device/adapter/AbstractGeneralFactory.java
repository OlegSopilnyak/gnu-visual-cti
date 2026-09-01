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
package org.visualcti.core.channel.device.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.visualcti.core.channel.Channel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceEvent;
import org.visualcti.core.channel.device.Factory;
import org.visualcti.util.Tools;

/**
 * Basic: The Factory of the Devices: The abstract general channel-devices factory
 * <p>
 * The parent factory of any type of the devices factory.
 * <p>
 *
 * @param <H> the type of the device's low-level operations handle
 * @param <D> the type of factory's device
 * @see Factory
 * @see AbstractEventProcessor
 */
@SuppressWarnings("unchecked")
public abstract class AbstractGeneralFactory<H, D extends Device<?, ?>>
        extends AbstractEventProcessor<H> implements Factory<H, D> {
    // the holder of factory's device channels
    private final AtomicReference<Collection<Channel<?>>> channelsHolder = new AtomicReference<>(Collections.emptyList());
    // XML-Document of the list of tasks in the pool
    protected final Document factoryConfigurationDocument = new Document().setContent(Arrays.asList(
            new Comment(Tools.getLicenceHeader()),
            new Element(getVendor()).addContent(defaultDeviceXml())
    ));

    public AbstractGeneralFactory(Executor deviceEventExecutor, DeviceEvent.Provider<H> eventsProvider) {
        this(deviceEventExecutor, eventsProvider, new DefaultDeviceEventListenersHub());
    }

    protected AbstractGeneralFactory(Executor deviceEventExecutor, DeviceEvent.Provider<H> eventsProvider, DeviceEvent.Listener.Hub eventListenersHub) {
        super(deviceEventExecutor, eventsProvider, eventListenersHub);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractGeneralFactory && equals((AbstractGeneralFactory<H, D>) o);
    }

    public boolean equals(AbstractGeneralFactory<H, D> o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * <aceessor>
     * to get the root xml-document of the configuration
     *
     * @return root xml-document of the configuration instance
     * @see Document
     * @see #factoryConfigurationDocument
     */
    @Override
    public Document getConfigurationDocument() {
        return factoryConfigurationDocument;
    }

    /**
     * <configuration-loader>
     * To load the vendor specific configuration of the factory from the external file
     *
     * @throws IOException if it cannot load configuration
     * @see #configurationFile()
     * @see Factory#loadFactoryConfiguration()
     */
    @Override
    public void loadFactoryConfiguration() throws IOException, DataConversionException {
        // preparing vendor-configuration external file
        final File configurationFile = configurationFile();
        if (configurationFile.exists()) {
            try (final FileInputStream in = new FileInputStream(configurationFile)) {
                // getting the factory devices' configuration from the vendor's external file
                final Element vendorConfigurationXml = restoreDocumentFrom(in).getRootElement();
                // passing it to all factory's devices
                for (final D device : (Iterable<D>) devices()::iterator) {
                    device.setXML(vendorConfigurationXml);
                }
            }
        } else {
            final Element vendorRoot = this.factoryConfigurationDocument.getRootElement();
            // removing all children of the vendor's xml-element
            vendorRoot.removeChildren();
            // saving device's default configuration
            vendorRoot.addContent(defaultDeviceXml());
            // saving updated configuration document
            saveFactoryConfiguration();
            // recursive call
            loadFactoryConfiguration();
        }
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
        return new Element("default");
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
     * To start the internal runnable parts of the unit
     * Grabbing the factory's devices and making channels for them
     *
     * @throws IOException if something went wrong during the internal parts starting
     * @see #Start()
     * @see #makeChannelFor(Device)
     */
    @Override
    public void startUnitRunnable() throws IOException {
        channelsHolder.getAndSet(devices().map(this::makeChannelFor).collect(Collectors.toSet()));
    }

    /**
     * <action>
     * To stop the internal runnable parts of the unit
     * Clearing built in startUnitRunnable channels array
     *
     * @throws IOException if something went wrong during the internal parts stopping
     * @see #Stop()
     * @see #startUnitRunnable()
     */
    @Override
    public void stopUnitRunnable() throws IOException {
        channelsHolder.getAndSet(Collections.emptyList());
    }

    /**
     * <builder>
     * To make the channel for device
     *
     * @param device channel to build for
     * @return built channel
     */
    protected Channel<D> makeChannelFor(Device<?, ?> device) {
        throw new UnsupportedOperationException("Not supported here. Please implement it in the descendent.");
    }

    /**
     * <aceessor>
     * to get the array of available factory's channels
     *
     * @return the array of available channels
     * @see Channel
     */
    @Override
    public Collection<Channel<?>> channels() {
        return channelsHolder.get();
    }

    /**
     * Events Listeners Hub: The default implementation hub of the native device's event listeners
     *
     * @see DeviceEvent.Listener.Hub
     */
    public static class DefaultDeviceEventListenersHub extends AbstractEventListenersHub {
    }
}
