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
import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jdom.DataConversionException;
import org.junit.Test;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardDevicesFactory;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.SoundCardServiceProvider;
import org.visualcti.workflow.hardware.javasound.ui.SoundCardUI;

public class VoiceManagementPaneTest {

    public static <H extends SoundCardHandle> void main(
            String[] args
    ) throws IOException, DataConversionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No any device in the factory."));
            PhoneCallSession<H> session = device.startSession();
            // emulate connected phone call session
            session.alive(true);
            VoiceManagementPane<H> pane = new VoiceManagementPane<>(new SoundCardUI<>(session));
            JFrame frame = new JFrame("Emulator");
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            JPanel control = new JPanel(new FlowLayout());
            frame.getContentPane().add(control, BorderLayout.SOUTH);
            JButton test1 = new JButton("test play");
            JButton test2 = new JButton("test record");
            test1.addActionListener(e -> {
                try {
                    testPlaying(pane, scheduler);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            control.add(test1);
            test2.addActionListener(e -> {
                try {
                    testRecording(pane, scheduler);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            control.add(test2);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }

    @Test
    public void useMain() {
        assertThat(true).isTrue();
    }

    private static <H extends SoundCardHandle> void testPlaying(
            VoiceManagementPane<H> pane, ScheduledExecutorService scheduler
    ) throws IOException {
        File audioFile = File.createTempFile("audio", ".wav");
        pane.startPlaying(audioFile);
        audioFile.deleteOnExit();
        scheduler.schedule(pane::stopPlaying, 1000, TimeUnit.MILLISECONDS);
        audioFile.delete();
    }
    private static <H extends SoundCardHandle> void testRecording(
            VoiceManagementPane<H> pane, ScheduledExecutorService scheduler
    ) throws IOException {
        File audioFile = File.createTempFile("audio", ".wav");
        pane.startRecording(audioFile);
        audioFile.deleteOnExit();
        scheduler.schedule(pane::stopRecording, 1000, TimeUnit.MILLISECONDS);
        audioFile.delete();
    }
}
