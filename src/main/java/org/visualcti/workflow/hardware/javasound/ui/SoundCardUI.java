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
package org.visualcti.workflow.hardware.javasound.ui;

import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardDevicesFactory;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider;
import org.visualcti.workflow.hardware.javasound.ui.panel.DtmfInputPane;
import org.visualcti.workflow.hardware.javasound.ui.panel.FaxManagementPane;
import org.visualcti.workflow.hardware.javasound.ui.panel.PhoneCallsPane;
import org.visualcti.workflow.hardware.javasound.ui.panel.VoiceManagementPane;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 * Swing UI Part of the emulator</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see PhoneCallSession
 * @see PhoneCallsPane
 * @see DtmfInputPane
 */
public class SoundCardUI<H extends SoundCardHandle> extends JPanel {
    private final transient PhoneCallSession<H> deviceActivitySession;
    private final transient PhoneCallsPane<H> callsControlPane;
    private final transient DtmfInputPane<H> dtmfInputPane;
    private final transient VoiceManagementPane<H> voicePane;
    private final transient FaxManagementPane<H> faxPane;
    private final JLabel state = new JLabel("IDLE");

    public SoundCardUI(PhoneCallSession<H> deviceActivitySession) {
        super(new BorderLayout(),true);
        if (deviceActivitySession == null) {
            throw new IllegalArgumentException("PhoneCall session cannot be null.");
        }
        this.deviceActivitySession = deviceActivitySession;
        // preparing UI tabs
        this.callsControlPane = new PhoneCallsPane<>(this);
        this.dtmfInputPane = new DtmfInputPane<>(this);
        this.voicePane = new VoiceManagementPane<>(this);
        this.faxPane = new FaxManagementPane<>(this);
        // making UI tabs
        add(state,BorderLayout.NORTH);
        state.setBorder(new TitledBorder("State of device"));
        state.setFont(new Font("sansserif",Font.BOLD,14));
        final JTabbedPane modes = new JTabbedPane(BOTTOM);
        modes.setFont(new Font("dialog",Font.BOLD,9));
        super.add(modes,BorderLayout.CENTER);
        modes.addTab("call",callsControlPane);
        modes.addTab("dial pad",dtmfInputPane);
        modes.addTab("voice",voicePane);
        modes.addTab("fax",faxPane);
        setFocusable(true);
        addKeyListener(dtmfInputPane);
    }

    @Deprecated
    public static <H extends SoundCardHandle> void main(String[] args) throws IOException, DataConversionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No any device in the factory."));
            PhoneCallSession<H> session = device.startSession();
            // emulate connected phone call session
            session.alive(true);
            SoundCardUI<H> pane = new SoundCardUI<>(session);
            JFrame frame = new JFrame("Emulator");
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public PhoneCallSession<H> getActiveSession() {
        return deviceActivitySession;
    }

    public void stateChanged(String newState) {
        state.setText(newState);
    }

    public void online() {
        callsControlPane.setEnabled(true);
        dtmfInputPane.setEnabled(true);
        voicePane.setEnabled(true);
        faxPane.setEnabled(true);
    }

    public void setHandset(boolean on) {
        callsControlPane.setHandset(on);
    }
}
