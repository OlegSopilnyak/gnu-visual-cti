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
package org.visualcti.workflow.hardware.javasound.ui.panel;


import static javax.swing.SwingConstants.RIGHT;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.visualcti.briquette.UI;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardDevicesFactory;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider;
import org.visualcti.workflow.hardware.javasound.ui.SoundCardUI;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 * Swing UI Part of the emulator</p>
 * faxes management control pane</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see SoundCardDevice
 * @see SoundCardUI
 */
@SuppressWarnings("unchecked")
public class FaxManagementPane<H extends SoundCardHandle> extends JPanel {
    // telephony device stuff
    private final transient PhoneCallSession<H> activePhoneCallSession;
    private final transient SoundCardDevice<H, ?> telephonyDevice;
    // ui stuff
    private final transient BasicFaxMachinePanel faxMachinePanel;
    private final transient FaxDocumentPane faxDocumentPanel;
    private final transient FaxTransmissionControlPane faxMachineControlPanel;

    public FaxManagementPane(final SoundCardUI<H> rootPanel) {
        super(new BorderLayout(), true);
        activePhoneCallSession = rootPanel.getActiveSession();
        telephonyDevice = (SoundCardDevice<H, ?>) this.activePhoneCallSession.getDevice();
        // making the main voice pane
        faxMachinePanel = new BasicFaxMachinePanel();
        faxDocumentPanel = new FaxDocumentPane();
        faxMachineControlPanel = new FaxTransmissionControlPane();
        add(faxMachinePanel, BorderLayout.NORTH);
        add(faxDocumentPanel, BorderLayout.CENTER);
        add(faxMachineControlPanel, BorderLayout.SOUTH);
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
            FaxManagementPane<H> pane = new FaxManagementPane<>(new SoundCardUI<>(session));
            JFrame frame = new JFrame("Emulator");
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    protected void startFax() {
        // doing noting here
    }

    protected void stopFax() {
        // doing noting here
    }

    private class BasicFaxMachinePanel extends JPanel {
        private BasicFaxMachinePanel() {
            super(false);
            setBorder(new TitledBorder("Basis"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JPanel panel = new JPanel(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            add(panel);
            panel.add(new JLabel("Transferred pages "));
            final int transferredPages = telephonyDevice.getTransferredPages(activePhoneCallSession);
            final JTextField pagesCount = new JTextField(Integer.toString(transferredPages), 5);
            pagesCount.setHorizontalAlignment(RIGHT);
            pagesCount.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    int newValue = 0;
                    if (!str.isEmpty()) newValue = Integer.parseInt(str);
                    telephonyDevice.setTransferredPages(activePhoneCallSession, newValue);
                }
            });
            panel.add(pagesCount);
            panel = new JPanel(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(new JLabel("RemoteID: "));
            add(panel);
            final JTextField remoteID = new JTextField(telephonyDevice.getRemoteID(activePhoneCallSession), 10);
            remoteID.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    telephonyDevice.setRemoteID(activePhoneCallSession, str);
                }
            });
            panel.add(remoteID);
            panel = new JPanel(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(new JLabel("LocalID: "));
            add(panel);
            final JTextField localID = new JTextField(telephonyDevice.getLocalID(activePhoneCallSession), 10);
            localID.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    telephonyDevice.setLocalID(activePhoneCallSession, str);
                }
            });
            panel.add(localID);
            panel = new JPanel(new BorderLayout(), false);
            panel.setBorder(new TitledBorder("Fax Header"));
            add(panel);
            final JTextField header = new JTextField(telephonyDevice.getFaxDocumentHeader(activePhoneCallSession), 10);
            header.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    telephonyDevice.setFaxDocumentHeader(activePhoneCallSession, str);
                }
            });
            panel.add(header, BorderLayout.CENTER);
        }
    }

    private class FaxDocumentPane extends JPanel {
        private FaxDocumentPane() {
            super(new BorderLayout(), false);
            setBorder(new TitledBorder("Document"));
        }
    }

    private class FaxTransmissionControlPane extends JPanel {
        private FaxTransmissionControlPane() {
            super(new FlowLayout(), false);
            setBorder(new TitledBorder("Transmission"));
            final JButton start = new JButton("Start");
            add(start);
            start.addActionListener(e -> startFax());
            final JButton stop = new JButton("Stop");
            add(stop);
            stop.addActionListener(e -> stopFax());
        }
    }
}
