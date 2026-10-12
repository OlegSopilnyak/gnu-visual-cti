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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.io.File;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.media.Audio;
import org.visualcti.workflow.hardware.javasound.SoundCardDevice;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
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
    final PlayAudioPane playing;
    final RecordAudioPane recording;
    private final JComboBox<Audio> formats;

    public VoiceManagementPane(final SoundCardUI<H> rootPanel) {
        super(new BorderLayout(), true);
        activePhoneCallSession = rootPanel.getActiveSession();
        telephonyDevice = (SoundCardDevice<H, ?>) this.activePhoneCallSession.getDevice();
        // making the playback pane
        formats = new JComboBox<>(telephonyDevice.canRecord());
        formats.setEditable(false);
        formats.setSelectedItem(telephonyDevice.getRecordFormat());
        playing = new PlayAudioPane();
        add(playing, BorderLayout.NORTH);
        // vertical glue
        add(Box.createVerticalGlue(), BorderLayout.CENTER);
        // making the record pane
        recording = new RecordAudioPane();
        add(recording, BorderLayout.SOUTH);
        formats.addActionListener(e -> {
            final Audio selected = (Audio) formats.getSelectedItem();
            if (selected != null) telephonyDevice.setRecordFormat(selected);
        });
    }

    @Override
    public void setEnabled(final boolean enabled) {
        playing.setEnabled(enabled);
        recording.setEnabled(enabled);
    }

    public void startPlaying(final File audioFile) {
        playing.setEnabled(true);
        playingBackFile.setText(audioFile.getName());
        recording.setEnabled(false);
        recordingFile.setText("");
    }

    public void stopPlaying() {
        playingBackFile.setText("");
        recording.setEnabled(true);
    }

    public void startRecording(final File audioFile) {
        recording.setEnabled(true);
        recordingFile.setText(audioFile.getName());
        formats.setEnabled(false);
        playing.setEnabled(false);
        playingBackFile.setText("");
    }

    public void stopRecording() {
        recordingFile.setText("");
        formats.setEnabled(true);
        playing.setEnabled(true);
    }

    private final class PlayAudioPane extends JPanel {
        private final JLabel useFile = new JLabel("Use File: ");
        private PlayAudioPane() {
            super(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new TitledBorder("Playback"));
            final JPanel actionPane = new JPanel();
            add(actionPane);
            actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
            actionPane.add(useFile);
            actionPane.add(playingBackFile);
            playingBackFile.setEditable(false);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            useFile.setEnabled(enabled);
            playingBackFile.setEnabled(enabled);
        }
    }

    private final class RecordAudioPane extends JPanel {
        private final JLabel useFile = new JLabel("Use File: ");
        private final JLabel format = new JLabel("Format: ");
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
            actionPane.add(useFile);
            actionPane.add(recordingFile);
            recordingFile.setEditable(false);
            formatPane.add(format);
            formatPane.add(formats);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            useFile.setEnabled(enabled);
            format.setEnabled(enabled);
            formats.setEnabled(enabled);
        }
    }
}
