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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jdom.DataConversionException;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.media.Audio;
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
 * voice management control pane</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see SoundCardDevice
 * @see SoundCardUI
 */
@SuppressWarnings("unchecked")
public class VoiceManagementPane<H extends SoundCardHandle> extends JPanel {
    // telephony device stuff
    private final transient PhoneCallSession<H> activePhoneCallSession;
    private final transient SoundCardDevice<H, ?> telephonyDevice;
    // ui stuff
    final JTextField playingBackFile = new JTextField(12);
    final JTextField recordingFile = new JTextField(12);
    private final JComboBox<Audio> formats;

    public VoiceManagementPane(final SoundCardUI<H> rootPanel) {
        super(new BorderLayout(), true);
        activePhoneCallSession = rootPanel.getActiveSession();
        telephonyDevice = (SoundCardDevice<H, ?>) this.activePhoneCallSession.getDevice();
        // making the main voice pane
        // playback pane
        add(new PlayAudioPane(), BorderLayout.NORTH);
        formats = new JComboBox<>(telephonyDevice.canRecord());
        formats.setEditable(false);
        formats.setSelectedItem(telephonyDevice.getRecordFormat());
        add(Box.createVerticalGlue(), BorderLayout.CENTER);
        // record pane
        add(new RecordAudioPane(), BorderLayout.SOUTH);
        formats.addActionListener(e -> {
            final Audio selected = (Audio) formats.getSelectedItem();
            if (selected != null) telephonyDevice.setRecordFormat(selected);
        });
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
            File audioFile = File.createTempFile("audio", ".wav");
            VoiceManagementPane<H> pane = new VoiceManagementPane<>(new SoundCardUI<>(session));
            JFrame frame = new JFrame("Emulator");
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            pane.startPlaying(audioFile);
            pane.startRecording(audioFile);
            scheduler.schedule(() -> {
                pane.stopPlaying();
                pane.stopRecording();
            }, 1400, TimeUnit.MILLISECONDS);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.formats.setEnabled(enabled);
    }

    public void startPlaying(final File audioFile) {
        playingBackFile.setText(audioFile.getName());
    }

    public void stopPlaying() {
        playingBackFile.setText("");
    }

    public void startRecording(final File audioFile) {
        recordingFile.setText(audioFile.getName());
    }

    public void stopRecording() {
        recordingFile.setText("");
    }

    private class PlayAudioPane extends JPanel {
        private PlayAudioPane() {
            super(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            super.setBorder(new TitledBorder("Playback"));
            final JPanel actionPane = new JPanel();
            add(actionPane);
            actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
            actionPane.add(new JLabel("Use File: "));
            actionPane.add(playingBackFile);
            playingBackFile.setEditable(false);
        }
    }

    private class RecordAudioPane extends JPanel {
        private RecordAudioPane() {
            super(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new TitledBorder("Record"));
            final JPanel actionPane = new JPanel();
            actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
            final JPanel formatPane = new JPanel();
            formatPane.setLayout(new BoxLayout(formatPane, BoxLayout.X_AXIS));
            add(actionPane);
            add(Box.createVerticalStrut(5));
            add(formatPane);
            actionPane.add(new JLabel("Use File: "));
            actionPane.add(recordingFile);
            recordingFile.setEditable(false);
            formatPane.add(new JLabel("Format: "));
            formatPane.add(formats);
        }
    }
}
