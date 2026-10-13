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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import org.visualcti.briquette.UI;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
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
    private final transient JButton startTransmission = new JButton("Start");
    private final transient JButton stopTransmission = new JButton("Stop");

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
        startTransmission.addActionListener(e -> startFax());
        stopTransmission.addActionListener(e -> stopFax());
        stopTransmission.setEnabled(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        faxDocumentPanel.setEnabled(enabled);
        faxDocumentPanel.setEnabled(enabled);
        faxMachineControlPanel.setEnabled(enabled);
    }

    protected void startFax() {
        startTransmission.setEnabled(false);
        stopTransmission.setEnabled(true);
    }

    protected void stopFax() {
        startTransmission.setEnabled(true);
        stopTransmission.setEnabled(false);
    }

    public void sendFax() {
        startTransmission.setEnabled(true);
        activePhoneCallSession.setState(TelephonyDevice.State.SENDFAX);
    }

    public void receiveFax() {
        startTransmission.setEnabled(true);
        activePhoneCallSession.setState(TelephonyDevice.State.RECVFAX);
    }

    private class BasicFaxMachinePanel extends JPanel {
        private final JTextField pagesCount = new JTextField(5);
        private final JTextField remoteID = new JTextField(10);
        private final JTextField localID = new JTextField(10);
        final JTextField documentHeader = new JTextField(10);

        private BasicFaxMachinePanel() {
            super(false);
            setBorder(new TitledBorder("Basis"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            // preparing UI stuff
            add(transferredPages());
            add(remoteFaxMachineId());
            add(localFaxMachineId());
            add(faxDocumentHeader());
        }

        private JPanel transferredPages() {
            final JPanel panel = new JPanel(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(new JLabel("Transferred pages "));
            final int transferredPages = telephonyDevice.getTransferredPages(activePhoneCallSession);
            pagesCount.setEditable(false);
            pagesCount.setText(Integer.toString(transferredPages));
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
            return panel;
        }

        private JPanel remoteFaxMachineId() {
            final JPanel panel = new JPanel(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(new JLabel("RemoteID: "));
            remoteID.setEditable(false);
            remoteID.setText(telephonyDevice.getRemoteID(activePhoneCallSession));
            remoteID.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    telephonyDevice.setRemoteID(activePhoneCallSession, str);
                }
            });
            panel.add(remoteID);
            return panel;
        }

        private JPanel localFaxMachineId() {
            final JPanel panel = new JPanel(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(new JLabel("LocalID: "));
            localID.setEditable(false);
            localID.setText(telephonyDevice.getLocalID(activePhoneCallSession));
            localID.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    telephonyDevice.setLocalID(activePhoneCallSession, str);
                }
            });
            panel.add(localID);
            return panel;
        }

        private JPanel faxDocumentHeader() {
            final JPanel panel = new JPanel(new BorderLayout(), false);
            panel.setBorder(new TitledBorder("Fax Header"));
            documentHeader.setEditable(false);
            documentHeader.setText(telephonyDevice.getFaxDocumentHeader(activePhoneCallSession));
            documentHeader.setDocument(new UI.validatingDocument() {
                @Override
                protected void validate(String str) {
                    telephonyDevice.setFaxDocumentHeader(activePhoneCallSession, str);
                }
            });
            panel.add(documentHeader, BorderLayout.CENTER);
            return panel;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            pagesCount.setEnabled(enabled);
            remoteID.setEnabled(enabled);
            localID.setEnabled(enabled);
            documentHeader.setEnabled(enabled);
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
            add(startTransmission);
            startTransmission.addActionListener(e -> startFax());
            add(stopTransmission);
            stopTransmission.addActionListener(e -> stopFax());
        }
    }
}
