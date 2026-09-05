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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
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
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.unit.ServerUnitAdapter;
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
    // the attribute for the devices-factory-vendor's external configuration file name value
    protected String configurationFileName = null;
    // the attribute for the devices-factory-vendor's name value
    protected String vendorName = "AbstractVendor";
    // the attribute for the devices-factory-vendor's version value
    protected String vendorVersion = VENDOR_FACTORY_DEFAULT_VERSION;
    // XML-Document of the list of tasks in the pool
    protected final Document vendorDevicesConfigurationDocument = new Document().setContent(Arrays.asList(
            new Comment(Tools.getLicenceHeader()),
            new Element(getVendor()).addContent(defaultDeviceXml())
    ));

    public AbstractGeneralFactory(Executor deviceEventExecutor, Device.ServiceProvider<H> serviceProvider) {
        this(deviceEventExecutor, serviceProvider, new DefaultDeviceEventListenersHub());
    }

    protected AbstractGeneralFactory(Executor deviceEventExecutor, Device.ServiceProvider<H> serviceProvider, DeviceEvent.Listener.Hub eventListenersHub) {
        super(deviceEventExecutor, serviceProvider, eventListenersHub);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractGeneralFactory && equals((AbstractGeneralFactory<H, D>) o);
    }

    public boolean equals(AbstractGeneralFactory<H, D> that) {
        return Objects.equals(vendorName, that.vendorName)
                && Objects.equals(vendorVersion, that.vendorVersion)
                && Objects.equals(configurationFileName, that.configurationFileName)
                && super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorName, vendorVersion, configurationFileName, super.hashCode());
    }

    /**
     * <accessor>
     * To get access to the device's events provider
     *
     * @return the device's events provider reference
     */
    @Override
    public Device.ServiceProvider<H> getProvider() {
        return (Device.ServiceProvider<H>) super.getProvider();
    }

    /**
     * <converter>
     * To update the factory-unit's fields from XML
     *
     * @param devicesFactoryXml possible factory-unit's XML
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Factory#devices()
     * @see Device#close()
     * @see ServerUnit#cleanUnitsTree()
     * @see Factory#getProvider()
     * @see Factory#addDevice(Device)
     * @see Factory#buildDevice(String, Device.ServiceProvider)
     * @see Element
     * @see ServerUnitAdapter#setXML(Element)
     */
    @Override
    public void setXML(final Element devicesFactoryXml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        //
        // cleaning the factory of the devices opened earlier
        // closing exist devices
        for (final D device : (Iterable<D>) devices()::iterator) {
            // closing previously opened device of the factory
            device.close();
        }
        // to clean the tree of devices of the factory
        cleanUnitsTree();
        //
        // building & adding factory's devices for allowed device-names
        final Device.ServiceProvider<H> provider = getProvider();
        for (final String deviceName : provider.allowedDevices()) {
            addDevice(buildDevice(deviceName, provider));
        }
        //
        // adjusting by xml-configuration the factory as a server unit
        //
        super.setXML(devicesFactoryXml);
    }

    /**
     * <aceessor>
     * to get the root xml-document of the configuration
     *
     * @return root xml-document of the configuration instance
     * @see Document
     * @see #vendorDevicesConfigurationDocument
     */
    @Override
    public Document getConfigurationDocument() {
        return vendorDevicesConfigurationDocument;
    }

    /**
     * <configuration-loader>
     * To load the vendor-specific configuration of the factory from the external file
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
                // updating the name of configuration-xml
                vendorConfigurationXml.setName(getVendor());
                // updating root-element of vendor configuration xml-document
                this.vendorDevicesConfigurationDocument.setRootElement(vendorConfigurationXml);
                // passing it to all factory's devices
                for (final D device : (Iterable<D>) devices()::iterator) {
                    device.setXML(vendorConfigurationXml);
                }
            }
        } else {
            final Element vendorRoot = this.vendorDevicesConfigurationDocument.getRootElement();
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
        return new Element("abstract-default");
    }

    /**
     * <accessor>
     * get access to the factory's vendor name
     *
     * @return vendor's name
     * @see Factory#getName()
     */
    @Override
    public String getVendor() {
        return vendorName;
    }

    /**
     * <mutator>
     * To change the value of the factory vendor
     *
     * @param vendorName new name of the vendor
     * @see #vendorDevicesConfigurationDocument
     */
    public void setVendor(String vendorName) {
        // updating the vendor's name
        this.vendorName = vendorName;
        // updating the name of devices-configuration root-xml
        vendorDevicesConfigurationDocument.getRootElement().setName(vendorName);
    }

    /**
     * <accessor>
     * To get the description of the unit
     *
     * @see ServerUnitAdapter#buildUnitRootElement()
     */
    @Override
    protected String getUnitDescription() {
        return null;
    }

    /**
     * <accessor>
     * get access to factory's version
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        return vendorVersion;
    }

    /**
     * <accessor>
     * to get the configuration file
     *
     * @return configuration file instance
     * @see File
     * @see #getVendor()
     * @see #CONFIG_FILE_SUFFIX
     * @see #saveFactoryConfiguration()
     * @see #loadFactoryConfiguration()
     */
    @Override
    public File configurationFile() {
        return Paths.get(configurationFilePath()).toFile();
    }

    protected String configurationFilePath() {
        return configurationFileName == null
                ? "./conf/" + getVendor().toLowerCase() + CONFIG_FILE_SUFFIX
                : configurationFileName;
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
