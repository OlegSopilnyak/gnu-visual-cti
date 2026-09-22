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
package org.visualcti.workflow.hardware.javasound;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.ConfigurationParameter;
import org.visualcti.core.channel.AbstractTelephonyChannel;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.telephony.TelephonyChannel;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.util.Tools;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-card telephony device service provider</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 */
@SuppressWarnings("unchecked")
public class SoundCardDevicesFactory<H extends SoundCardHandle, D extends SoundCardDevice<H, ?>>
        extends AbstractTelephonyFactory<H, D> {
    // The configuration of the factory as XML
    public static final String CONFIGURATION_XML = "<factory package=\"org.visualcti.workflow.hardware.javasound\" " +
            "class=\"SoundCardDevicesFactory\" " +
            "extends=\"org.visualcti.core.channel.telephony.TelephonyFactory\">\n" +
            "  <parameter name=\"vendor\" type=\"string\" value=\"" + SoundCardServiceProvider.DEVICE_FACTORY_VENDOR + "\" />\n" +
            "  <parameter name=\"version\" type=\"string\" value=\"3.2\" />\n" +
            "  <parameter name=\"url\" type=\"string\" value=\"file:./conf/javasound.configuration.xml\" />\n" +
            "</factory>\n";
    // the value of type of the factory as a server unit
    public static final String DEVICES_FACTORY_UNIT_TYPE = "[SoundCard:telephony-channel-devices-board]";

    protected SoundCardDevicesFactory(Executor deviceEventsExecutor, TelephonyServiceProvider<H> provider) {
        super(deviceEventsExecutor, provider);
    }

    /**
     * <action>
     * To open the factory (prepare for the further work)
     *
     * @throws IOException if it cannot prepare
     */
    public void open() throws IOException, DataConversionException {
        final Element factoryXml = load(new ByteArrayInputStream(CONFIGURATION_XML.getBytes()));
        Tools.print("=== Configuring devices factory by ===\n" + CONFIGURATION_XML);
        configure(factoryXml);
    }

    @Override
    public String getVendor() {
        return SoundCardServiceProvider.DEVICE_FACTORY_VENDOR;
    }

    @Override
    public String getType() {
        return DEVICES_FACTORY_UNIT_TYPE;
    }

    @Override
    public Device<H, ?> buildDevice(final String deviceName, final Device.ServiceProvider<H> serviceProvider) {
        return new SoundCardDevice<>(SoundCardServiceProvider.SOUND_DEVICE, getProvider());
    }

    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return SoundCardDevicesFactory.class;
    }

    @Override
    public Class<?> getUnitBuilderClass() {
        return null;
    }

    @Override
    public boolean isNeedRegistration() {
        return false;
    }

    @Deprecated
    @Override
    protected void settingUpBasePart(Element xml) {
        super.settingUpBasePart(xml);
    }

    @Deprecated
    @Override
    protected void settingUpMainPart(Element xml) throws IOException {
        super.settingUpMainPart(xml);
    }

    @Deprecated
    @Override
    protected void applyUnitParameter(ConfigurationParameter parameter) {
        super.applyUnitParameter(parameter);
    }

    @Override
    protected TelephonyChannel<D> makeChannelFor(Device<?, ?> device) {
        return new SoundCardChannel((D) device);
    }

    // inner classes
    private class SoundCardChannel extends AbstractTelephonyChannel<D> {
        protected SoundCardChannel(D device) {
            super(device);
        }
    }
}
