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

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
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
    private final JTextField state = new JTextField("OFFLINE");
    private final JButton dropCall = new JButton("Drop Call");
    // phone call related panels
    private final WaitForIncomingCallPane waitForCallPane;
    private final MakeOutgoingCallPane makeCallPane;
    private final PhoneNumbersPane phoneNumbersPane;
    // telephony device stuff
    private final transient PhoneCallSession<H> activePhoneCallSession;
    private final transient SoundCardDevice<H, ?> telephonyDevice;

    @Deprecated
    public static <H extends SoundCardHandle> void main(String[] args) throws IOException, DataConversionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No devices in the factory"));
            PhoneCallsPane<H> pane = new PhoneCallsPane<>(new SoundCardUI<>(device.startSession()));
            JFrame frame = new JFrame("emulator");
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public PhoneCallsPane(final SoundCardUI<H> rootPanel) {
        super(new BorderLayout(), true);
        activePhoneCallSession = rootPanel.getActiveSession();
        telephonyDevice = (SoundCardDevice<H, ?>) this.activePhoneCallSession.getDevice();
        // phone call status pane
        // adding status to the top of main pane
        add(buildingCallStatusPane(), BorderLayout.NORTH);
        // calls controls pane
        phoneNumbersPane = new PhoneNumbersPane();
        waitForCallPane = new WaitForIncomingCallPane();
        makeCallPane = new MakeOutgoingCallPane();
        // adding status to the center of main pane
        add(buildingCallsControlsPane(), BorderLayout.CENTER);
        // prepare pane for visualization
        setEnabled(true);
        setHandset(activePhoneCallSession.isAlive());
    }

    private JPanel buildingCallStatusPane() {
        final JPanel phoneCallStatus = new JPanel(new BorderLayout(), false);
        state.setBorder(null);
        state.setFont(new Font("sanserif", Font.BOLD, 12));
        phoneCallStatus.add(state, BorderLayout.CENTER);
        phoneCallStatus.add(dropCall, BorderLayout.EAST);
        dropCall.addActionListener(e -> {
            telephonyDevice.dropCall(activePhoneCallSession);
            setHandset(false);
        });
        return phoneCallStatus;
    }

    private JPanel buildingCallsControlsPane() {
        final JPanel callControlsPane = new JPanel(false);
        callControlsPane.setLayout(new BoxLayout(callControlsPane, BoxLayout.Y_AXIS));
        callControlsPane.add(Box.createVerticalStrut(10));
        callControlsPane.add(this.phoneNumbersPane);
        callControlsPane.add(this.waitForCallPane);
        callControlsPane.add(this.makeCallPane);
        return callControlsPane;
    }

    public void setHandset(boolean on) {
        SwingUtilities.invokeLater(() -> {
            dropCall.setEnabled(on);
            waitForCallPane.ring.setEnabled(!on);
            makeCallPane.setEnabled(!on);
        });
    }

    private final class PhoneNumbersPane extends JPanel {
        private final JTextField origin = new JTextField(12);
        private final JTextField caller = new JTextField(12);
        private final JTextField calling = new JTextField(12);

        PhoneNumbersPane() {
            super(false);
            // setting up the origin phone number
            origin.setText(telephonyDevice.getParameter(CallsPortEngine.Parameter.ORIGIN)
                    .map(param -> param.getValue().toString()).orElse("")
            );
            super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            addPhoneNumber("Origin", origin, this);
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
            origin.setEnabled(!enabled);
            caller.setEnabled(!enabled);
            calling.setEnabled(!enabled);
        }
    }

    private final class WaitForIncomingCallPane extends JPanel {
        private final JButton ring = new JButton("Ring");

        WaitForIncomingCallPane() {
            super(new FlowLayout(FlowLayout.CENTER), false);
            add(ring);
            setBorder(new TitledBorder("Wait For PhoneCall"));
            ring.setFocusPainted(false);
            ring.addActionListener(e -> {
                setHandset(true);
                telephonyDevice.callAlerted(activePhoneCallSession);
            });
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.ring.setEnabled(!enabled);
        }
    }

    private final class MakeOutgoingCallPane extends JPanel {
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

        MakeOutgoingCallPane() {
            super(new BorderLayout(), false);
            // preparing auto answer control pane
            setBorder(new TitledBorder("Make PhoneCall"));
            autoAnswerCheck = new JCheckBox("autoAnswer");
            randomCheck = new JCheckBox("random", false);
            randomCheck.setEnabled(false);
            answerType = new JComboBox<>(types);
            // preparing call analysis control pane
            answer = new JButton("Answer");
            answer.setFocusPainted(false);
            this.answer.addActionListener(e -> answerCall());
            //
            // adding answer call analysis pane to the center of the root pane
            add(buildingAnswerCallAnalysisPane(), BorderLayout.CENTER);
            //
            // building auto-answer pane
            final JPanel autoAnswerPane = new JPanel(new FlowLayout(FlowLayout.CENTER), false);
            autoAnswerPane.add(autoAnswerCheck);
            autoAnswerPane.add(randomCheck);
            // adding auto answer control pane to the top of the root pane
            add(autoAnswerPane, BorderLayout.NORTH);
            // randomizing the values in generator
            assert (randomizer.ints(10, 1, types.length).count() == 10) : "Randomizer didn't init.";
            // answer value combo box item changed listener
            this.autoAnswerCheck.addItemListener(itemEvent -> {
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
            });
        }

        private JPanel buildingAnswerCallAnalysisPane() {
            final JPanel callAnalysis = new JPanel(new FlowLayout(FlowLayout.LEFT), false);
            callAnalysis.add(this.answerType);
            callAnalysis.add(this.answer);
            this.answer.setFocusPainted(false);
            this.answer.setMargin(new Insets(1, 1, 1, 1));
            return callAnalysis;
        }

        // notification from UI-component (answer button) after make call device method
        private void answerCall() {
            if (randomCheck.isSelected()) {
                final int index = randomizer.nextInt(types.length - 1);
                answerType.setSelectedIndex(index);
            }
            // notify about outgoing call answer
            telephonyDevice.answerCall((OperationResultValue) answerType.getSelectedItem());
        }

        @Override
        public void setEnabled(boolean enabled) {
            autoAnswerCheck.setEnabled(enabled);
            randomCheck.setEnabled(enabled);
            answerType.setEnabled(enabled);
            answer.setEnabled(enabled);
        }
    }

    @Override
    public final void setEnabled(boolean enabled) {
        this.dropCall.setEnabled(enabled);
        String text = enabled ? "ONLINE" : "OFFLINE";
        Color background = enabled ? Color.green : Color.lightGray;
        this.state.setText(text);
        this.state.setBackground(background);
        this.phoneNumbersPane.setEnabled(enabled);
        this.waitForCallPane.setEnabled(enabled);
        this.makeCallPane.setEnabled(enabled);
    }
}

