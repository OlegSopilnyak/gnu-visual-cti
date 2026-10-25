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

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdom.DataConversionException;
import org.junit.Test;
import org.visualcti.core.channel.telephony.TelephonyDevice;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneNumber;
import org.visualcti.media.Audio;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardDevicesFactory;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider;

public class SoundCardUITest {
    public static <H extends SoundCardHandle> void main(String[] args) throws IOException, DataConversionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(12);
        SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            factory.Start();
            SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No any device in the factory."));
            PhoneCallSession<H> session = device.startSession();
            // emulate connected phone call session
            JFrame frame = new JFrame("Emulator");
            frame.getContentPane().add(device.getUI(), BorderLayout.CENTER);
            JPanel control = new JPanel();
            control.setLayout(new BoxLayout(control, BoxLayout.Y_AXIS));
            JPanel results = new JPanel(new BorderLayout());
            results.setBorder(new TitledBorder("Operation Result"));
            control.add(results);
            JTextField operationResult = new JTextField();
            operationResult.setEditable(false);
            operationResult.setFocusable(false);
            operationResult.setFont(new Font("sanserif", Font.BOLD, 15));
            operationResult.setBackground(Color.blue);
            operationResult.setForeground(Color.yellow);
            operationResult.setText("===");
            results.add(operationResult, BorderLayout.CENTER);
            frame.getContentPane().add(control, BorderLayout.SOUTH);
            control.add(callControlPane(operationResult, session, scheduler));
            control.add(userInputPane(session, scheduler));
            control.add(voiceControlPane(operationResult, session, scheduler));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            // enabling to pass out the DTMF events
            provider.enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
        }
    }

    private static <H extends SoundCardHandle> JPanel callControlPane(
            JTextField operationResult, PhoneCallSession<H> session, ExecutorService executor
    ) {
        TelephonyDevice<H, ?> device = session.getDevice();
        JPanel callControl = new JPanel();
        callControl.setBorder(new TitledBorder("Call Control"));
        callControl.setLayout(new BoxLayout(callControl, BoxLayout.Y_AXIS));
        JPanel waiter = new JPanel(new FlowLayout());
        JPanel maker = new JPanel(new FlowLayout());
        callControl.add(waiter);
        callControl.add(maker);
        JButton test1 = new JButton("wait call (answer)");
        JButton test10 = new JButton("wait call (no answer)");
        waiter.add(test1);
        waiter.add(test10);
        JButton test2 = new JButton("make call");
        maker.add(test2);
        Runnable waitForCallAnswered = () -> {
            operationResult.setText("===");
            Tools.print("Waiting for call: 2 rings, 2 secs, answer...");
            Tools.print("Wait for call succeed:" + device.waitForCall(session, 2, 2, true));
            Tools.print("Wait for call result:" + session.operationResult());
            operationResult.setText("= " + session.operationResult() + " =");
            // enabling to get of DTMF events
            device.getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test1.setEnabled(true);
        };
        test1.addActionListener(e -> {
            test1.setEnabled(false);
            executor.submit(waitForCallAnswered);
        });
        Runnable waitForCallNotAnswered = () -> {
            operationResult.setText("===");
            Tools.print("Waiting for call: 1 rings, 2 secs, no answer...");
            Tools.print("Wait for call succeed:" + device.waitForCall(session, 1, 2, false));
            Tools.print("Wait for call result:" + session.operationResult());
            operationResult.setText("= " + session.operationResult() + " =");
            // enabling to get of DTMF events
            device.getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test10.setEnabled(true);
        };
        test10.addActionListener(e -> {
            test10.setEnabled(false);
            executor.submit(waitForCallNotAnswered);
        });
        Runnable makeCallFor = () -> {
            operationResult.setText("===");
            String target = "12435";
            Tools.print("Making call to :" + target);
            Tools.print("Making call to succeed :" + device.makeCall(session, PhoneNumber.of(target), 5));
            Tools.print("Making call to result :" + session.operationResult());
            operationResult.setText("= " + session.operationResult() + " =");
            // enabling to get of DTMF events
            device.getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test2.setEnabled(true);
        };
        test2.addActionListener(e -> {
            test2.setEnabled(false);
            executor.submit(makeCallFor);
        });
        test1.setFocusable(false);
        test10.setFocusable(false);
        test2.setFocusable(false);
        return callControl;
    }

    private static <H extends SoundCardHandle> JPanel userInputPane(PhoneCallSession<H> session, ExecutorService executor) {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("User Input"));
        JButton test = new JButton("get user input");
        test.setFocusable(false);
        panel.add(test);
        JPanel result = new JPanel(new BorderLayout());
        panel.add(result);
        result.add(new JLabel("Input Symbols: "), BorderLayout.WEST);
        JTextField userInput = new JTextField(15);
        userInput.setFocusable(false);
        userInput.setEditable(false);
        result.add(userInput, BorderLayout.CENTER);
        Runnable gettingUserInput = () -> {
            userInput.setText(session.getDevice().getInputSymbols(session));
            Tools.print("Got user input :[" + userInput.getText() + "]");
            test.setEnabled(true);
        };
        test.addActionListener(e -> {
            test.setEnabled(false);
            executor.submit(gettingUserInput);
        });
        return panel;
    }

    private static <H extends SoundCardHandle> JPanel voiceControlPane(
            JTextField operationResult, PhoneCallSession<H> session, ExecutorService executor
    ) {
        TelephonyDevice<H, ?> device = session.getDevice();
        JPanel voiceControl = new JPanel();
        voiceControl.setBorder(new TitledBorder("Sound Control"));
        voiceControl.setLayout(new BoxLayout(voiceControl, BoxLayout.Y_AXIS));
        JPanel player = new JPanel(new FlowLayout());
        JPanel recorder = new JPanel(new FlowLayout());
        voiceControl.add(player);
        voiceControl.add(recorder);
        JButton test1 = new JButton("Play (no mask)");
        JButton test10 = new JButton("Play (* mask)");
        player.add(test1);
        player.add(test10);
        JButton test2 = new JButton("Record (no mask)");
        JButton test21 = new JButton("Record (* mask)");
        recorder.add(test2);
        recorder.add(test21);
        Audio format = Audio.LINEAR;
        Runnable playingNoMask = () -> {
            operationResult.setText("===");
            String audioFileName = "/VM/prompts/PLAY_GREETING_PHONESOFT.WAV";
            try(InputStream in = device.getClass().getResourceAsStream(audioFileName)) {
                Tools.print("Playing with no mask : " + audioFileName);
                Tools.print("Playing with no mask result: " + device.playbackAudio(session, in, format, "", 10));
            } catch (IOException e) {
                operationResult.setText("= IO Exception :" + session.operationResult() + " =");
            }
            operationResult.setText("= " + session.operationResult() + " =");
            // enabling to get of DTMF events for tests
            device.getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test1.setEnabled(true);
        };
        test1.addActionListener(e -> {
            test1.setEnabled(false);
            executor.submit(playingNoMask);
        });
        Runnable playingWithMask = () -> {
            operationResult.setText("===");
            String audioFileName = "/VM/prompts/PLAY_GREETING_PHONESOFT.WAV";
            try(InputStream in = device.getClass().getResourceAsStream(audioFileName)) {
                Tools.print("Playing with * as mask : " + audioFileName);
                Tools.print("Playing with * as mask result: " + device.playbackAudio(session, in, format, "*", 10));
            } catch (IOException e) {
                operationResult.setText("= IO Exception :" + session.operationResult() + " =");
            }
            operationResult.setText("= " + session.operationResult() + " =");
            // enabling to get of DTMF events
            device.getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test10.setEnabled(true);
        };
        test10.addActionListener(e -> {
            test10.setEnabled(false);
            executor.submit(playingWithMask);
        });
        Runnable recordingNoMask = () -> {
            operationResult.setText("===");
            try {
                File audioFile = File.createTempFile("testAudio", ".WAV");
                audioFile.deleteOnExit();
                Tools.print("Recording with no mask : " + audioFile.getName());
                try(OutputStream out = Files.newOutputStream(audioFile.toPath())) {
                    Tools.print("Recording with no mask result: " + device.recordAudio(session, out, format, "", 2,5));
                    operationResult.setText("= " + session.operationResult() + " =");
                }
                try (InputStream in = Files.newInputStream(audioFile.toPath())) {
                    Tools.print("Playing with no mask : " + audioFile.getName());
                    device.playbackAudio(session, in, format, "", 10);
                    operationResult.setText("= " + session.operationResult() + " =");
                }
            } catch (IOException e) {
                operationResult.setText("= IO Exception :" + session.operationResult() + " =");
            }
            // enabling to get of DTMF events
            session.getDevice().getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test2.setEnabled(true);
        };
        test2.addActionListener(e -> {
            test2.setEnabled(false);
            executor.submit(recordingNoMask);
        });
        Runnable recordingWithMask = () -> {
            operationResult.setText("===");
            try {
                File audioFile = File.createTempFile("testAudio", ".WAV");
                audioFile.deleteOnExit();
                Tools.print("Recording with * as mask : " + audioFile.getName());
                try(OutputStream out = Files.newOutputStream(audioFile.toPath())) {
                    Tools.print("Recording with no mask result: " + device.recordAudio(session, out, format, "*", 2,5));
                    operationResult.setText("= " + session.operationResult() + " =");
                }
                try (InputStream in = Files.newInputStream(audioFile.toPath())) {
                    Tools.print("Playing with * as mask : " + audioFile.getName());
                    device.playbackAudio(session, in, format, "", 10);
                    operationResult.setText("= " + session.operationResult() + " =");
                }
            } catch (IOException e) {
                operationResult.setText("= IO Exception :" + session.operationResult() + " =");
            }
            // enabling to get of DTMF events
            session.getDevice().getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test21.setEnabled(true);
        };
        test21.addActionListener(e -> {
            test2.setEnabled(false);
            executor.submit(recordingWithMask);
        });
        //
        test1.setFocusable(false);
        test10.setFocusable(false);
        test2.setFocusable(false);
        test21.setFocusable(false);
        return voiceControl;
    }

    @Test
    public void useMain() {
        assertThat(true).isTrue();
    }
}
