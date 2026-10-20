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
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
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
            frame.getContentPane().add(control, BorderLayout.SOUTH);
            control.add(callControlPane(session, scheduler));
            control.add(userInputPane(session, scheduler));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            // enabling to get of DTMF events
            provider.enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
        }
    }

    private static <H extends SoundCardHandle> JPanel callControlPane(PhoneCallSession<H> session, ExecutorService executor) {
        TelephonyDevice<H, ?> device = session.getDevice();
        JPanel callControl = new JPanel(new FlowLayout());
        callControl.setBorder(new TitledBorder("Call Control"));
        JButton test1 = new JButton("wait call (answer)");
        JButton test10 = new JButton("wait call (no answer)");
        JButton test2 = new JButton("make call");
        Runnable waitForCallAnswered = () -> {
            Tools.print("Waiting for call: 2 rings, 2 secs, answer...");
            Tools.print("Wait for call succeed:" + device.waitForCall(session, 2, 2, true));
            Tools.print("Wait for call result:" + session.operationResult());
            // enabling to get of DTMF events
            session.getDevice().getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test1.setEnabled(true);
        };
        test1.addActionListener(e -> {
            test1.setEnabled(false);
            executor.submit(waitForCallAnswered);
        });
        callControl.add(test1);
        Runnable waitForCallNotAnswered = () -> {
            Tools.print("Waiting for call: 1 rings, 2 secs, no answer...");
            Tools.print("Wait for call succeed:" + device.waitForCall(session, 1, 2, false));
            Tools.print("Wait for call result:" + session.operationResult());
            // enabling to get of DTMF events
            session.getDevice().getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test10.setEnabled(true);
        };
        callControl.add(test10);
        test10.addActionListener(e -> {
            test10.setEnabled(false);
            executor.submit(waitForCallNotAnswered);
        });
        Runnable makeCallFor = () -> {
            String target = "12435";
            Tools.print("Making call to :" + target);
            Tools.print("Making call to succeed :" + device.makeCall(session, PhoneNumber.of(target), 5));
            Tools.print("Making call to result :" + session.operationResult());
            // enabling to get of DTMF events
            session.getDevice().getProvider().enableEvents(session.getDeviceHandle(), Result.IO.DTMF);
            test2.setEnabled(true);
        };
        test2.addActionListener(e -> {
            test2.setEnabled(false);
            executor.submit(makeCallFor);
        });
        callControl.add(test2);
        test1.setFocusable(false);
        test10.setFocusable(false);
        test2.setFocusable(false);
        return callControl;
    }

    private static <H extends SoundCardHandle> JPanel userInputPane(PhoneCallSession<H> session, ExecutorService executor) {
        TelephonyDevice<H, ?> device = session.getDevice();
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("User Input"));
        JButton test = new JButton("get user input");
        test.setFocusable(false);
        Runnable gettingUserInput = () -> {
            Tools.print("Got user input :[" + device.getInputSymbols(session) + "]");
            test.setEnabled(true);
        };
        test.addActionListener(e -> {
            test.setEnabled(false);
            executor.submit(gettingUserInput);
        });
        panel.add(test);
        return panel;
    }
    @Test
    public void useMain() {
        assertThat(true).isTrue();
    }
}
