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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
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
 * PhoneCalls Control Pane</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see SoundCardDevice
 * @see SoundCardUI
 */
@SuppressWarnings("unchecked")
public class PhoneCallsPane<H extends SoundCardHandle> extends JPanel {
    private final JTextField originate;
    private final JTextField caller;
    private final JTextField calling;
    private final JTextField state;
    private final JButton dropCall;
    // phone call related panels
    private final WaitForIncomingCallPane waitCall;
    private final MakeOutgoingCallPane makeCall;
    private final PhoneNumbersPane callPar;
    // telephony device stuff
    private final transient PhoneCallSession<H> activeSession;
    private final transient SoundCardDevice<H, ?> device;

    public static <H extends SoundCardHandle> void main(String[] args) throws IOException, DataConversionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No devices in the factory"));
            PhoneCallsPane<H> pane = new PhoneCallsPane<>(new SoundCardUI<>(device.startSession()));
            JFrame frame = new JFrame("emulator");
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public PhoneCallsPane(final SoundCardUI<H> rootPanel) {
        super(new BorderLayout(), true);
        activeSession = rootPanel.getActiveSession();
        device = (SoundCardDevice<H, ?>) activeSession.getDevice();
        // phone numbers
        originate = new JTextField(12);
        caller = new JTextField(12);
        calling = new JTextField(12);
        // phone call status pane
        final JPanel phoneCallStatus = new JPanel(new BorderLayout(), false);
        // add status to the top of main pane
        super.add(phoneCallStatus, BorderLayout.NORTH);
        this.state = new JTextField("OFFLINE");
        this.state.setBorder(null);
        this.state.setFont(new Font("sanserif", Font.BOLD, 12));
        phoneCallStatus.add(this.state, BorderLayout.CENTER);
        this.dropCall = new JButton("Drop Call");
        phoneCallStatus.add(this.dropCall, BorderLayout.EAST);
        this.dropCall.addActionListener(e -> device.dropCall(activeSession));
        // calls controls pane
        final JPanel callControlPane = new JPanel(false);
        super.add(callControlPane, BorderLayout.CENTER);
        callControlPane.setLayout(new BoxLayout(callControlPane, BoxLayout.Y_AXIS));
        callControlPane.add(Box.createVerticalStrut(10));
        waitCall = new WaitForIncomingCallPane(device);
        makeCall = new MakeOutgoingCallPane(device);
        callPar = new PhoneNumbersPane();
        callControlPane.add(this.callPar);
        callControlPane.add(this.waitCall);
        callControlPane.add(this.makeCall);
    }

    private final class PhoneNumbersPane extends JPanel {
        PhoneNumbersPane() {
            super(false);
            super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            addPhoneNumber("Origin", originate, this);
            addPhoneNumber("Caller", caller, this);
            addPhoneNumber("Calling", calling, this);
        }

        private void addPhoneNumber(String title, JTextField field, JPanel owner) {
            final JPanel pane = new JPanel(new FlowLayout(), false);
            pane.add(new JLabel(title));
            pane.add(field);
            owner.add(pane);
            owner.add(Box.createVerticalStrut(4));
        }

        @Override
        public void setEnabled(boolean enabled) {
            originate.setEnabled(!enabled);
            caller.setEnabled(!enabled);
            calling.setEnabled(!enabled);
        }
    }

    private final class WaitForIncomingCallPane extends JPanel {
        private final JButton ring;
        private final SoundCardDevice<H, ?> soundCardDevice;

        WaitForIncomingCallPane(final SoundCardDevice<H, ?> device) {
            super(new FlowLayout(FlowLayout.CENTER), false);
            this.ring = new JButton("Ring");
            super.add(this.ring);
            super.setBorder(new TitledBorder("Wait For PhoneCall"));
            this.ring.setFocusPainted(false);
            this.soundCardDevice = device;
            this.ring.addActionListener(e -> soundCardDevice.callAlerted());
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.ring.setEnabled(!enabled);
        }
    }

    private final class MakeOutgoingCallPane extends JPanel {
        private final transient SoundCardDevice<H, ?> soundCardDevice;
        private final transient OperationResultValue[] types = new OperationResultValue[]{
                Result.CALL.Analysis.VOICE,
                Result.CALL.Analysis.FAX,
                Result.CALL.Analysis.BUSY,
                Result.CALL.Analysis.NO_ANSWER,
                Result.CALL.Analysis.NO_DIAL_TONE
        };
        private final JCheckBox autoAnswerCheck;
        private final JCheckBox randomCheck;
        private final JComboBox<OperationResultValue> answerType;
        private final JButton answer;
        private final Random randomizer = new Random();

        MakeOutgoingCallPane(final SoundCardDevice<H, ?> device) {
            super(new BorderLayout(), false);
            this.soundCardDevice = device;
            // preparing auto answer control pane
            JPanel autoAnswerPane = new JPanel(new FlowLayout(FlowLayout.CENTER), false);
            super.add(autoAnswerPane, BorderLayout.NORTH);
            autoAnswerCheck = new JCheckBox("autoAnswer");
            randomCheck = new JCheckBox("random", false);
            answerType = new JComboBox<>(types);
            autoAnswerPane.add(this.autoAnswerCheck);
            autoAnswerPane.add(this.randomCheck);
            this.randomCheck.setEnabled(false);
            this.autoAnswerCheck.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent ev) {
                    if (autoAnswerCheck.isSelected()) {
                        randomCheck.setEnabled(true);
                        randomCheck.setSelected(false);
                        answer.setEnabled(false);
                        answerType.setEnabled(false);
                        answerCall();
                    } else {
                        randomCheck.setEnabled(false);
                        randomCheck.setSelected(false);
                        answer.setEnabled(true);
                        answerType.setEnabled(true);
                    }
                }
            });
            JPanel ca = new JPanel(new FlowLayout(FlowLayout.LEFT), false);
            super.add(ca, BorderLayout.CENTER);
            answer = new JButton("Answer");
            ca.add(this.answerType);
            ca.add(this.answer);
            this.answer.setFocusPainted(false);
            this.answer.setMargin(new Insets(1, 1, 1, 1));
            this.answer.addActionListener(e -> answerCall());
            super.setBorder(new TitledBorder("Make PhoneCall"));
            // randomize values in generator
            randomizer.ints(10, 1, types.length).sum();
        }

        private void answerCall() {
            if (this.randomCheck.isSelected()) {
                int index = this.randomizer.nextInt(types.length - 1);
                this.answerType.setSelectedIndex(index);
            }
            soundCardDevice.answerCall(String.valueOf(answerType.getSelectedItem()));
        }
    }

    @Override
    public final void setEnabled(boolean enabled) {
        this.dropCall.setEnabled(enabled);
        String text = enabled ? "ONLINE" : "OFFLINE";
        Color background = enabled ? Color.green : Color.lightGray;
        this.state.setText(text);
        this.state.setBackground(background);
        this.callPar.setEnabled(enabled);
        this.waitCall.setEnabled(enabled);
        this.makeCall.setEnabled(enabled);
    }
}

