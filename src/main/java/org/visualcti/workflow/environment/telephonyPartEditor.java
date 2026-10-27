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
package org.visualcti.workflow.environment;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.visualcti.server.hardware.generalDeviceProxy;
import org.visualcti.server.task.Environment;
import org.visualcti.util.Tools;
import org.visualcti.workflow.facade.partEditor;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardDevicesFactory;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider;
//import org.visualcti.server.hardware.provider.soundcard.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br></p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 *
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class telephonyPartEditor extends partEditor {
    /**
     * <accessor>
     * To get the name of environment's part
     *
     * @return the name of the environment's part
     */
    public String getName() {
        return "Telephony";
    }

    /**
     * The container of visual components of devices
     */
    private final JTabbedPane devicesPlace = new JTabbedPane(SwingConstants.TOP);

    /**
     * <constructor>
     * Making the editor of telephony device
     */
    public telephonyPartEditor() {
        setLayout(new BorderLayout(0, 0));
        setDoubleBuffered(false);
        add(this.devicesPlace, BorderLayout.CENTER);
        this.devicesPlace.setFont(new Font("dialog", Font.BOLD, 10));
        this.devicesPlace.getModel().addChangeListener(e -> {
            final int index = devicesPlace.getSelectedIndex();
            if (index != -1) selectDevice(index);
        });
        this.placeSoundCard();
//        this.devicesPlace.setSelectedIndex(0);
    }

    /**
     * The container of devices
     */
    private final Map devices = new HashMap();
    private final ArrayList tabs = new ArrayList();

    // To attach the sound-card telephony device
    private <H extends SoundCardHandle> void placeSoundCard() {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(12);
        final SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (final SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            factory.Start();
            final SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No any device in the factory."));
            device.startSession();
            devicesPlace.addTab("emulator", new JScrollPane(device.getUI()));
        } catch (IOException | DataConversionException e) {
            e.printStackTrace(Tools.err);
            throw new RuntimeException(e);
        }
    }

    public void clean() {
        // doing nothing here
    }

    public void reload() {
        Environment env = this.getEnvironment();
        if (env == null) return;
        for (Iterator i = devices.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = "telephony/" + entry.getKey();
            env.setPart(name, entry.getValue());
        }
        this.selectDevice(this.devicesPlace.getSelectedIndex());
    }

    private Environment getEnvironment() {
        try {
            return super.owner.getFacade().getEnvironment();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void selectDevice(int index) {
        if (this.tabs.isEmpty()) {
            return;
        }
        Environment env = this.getEnvironment();
        if (env == null) return;
        generalDeviceProxy device = (generalDeviceProxy) this.tabs.get(index);
        if (device == null) return;
        String name = "telephony/" + device.getDeviceName();
        env.setPart(generalDeviceProxy.SELECTED_DEVICE, name);
    }
}
