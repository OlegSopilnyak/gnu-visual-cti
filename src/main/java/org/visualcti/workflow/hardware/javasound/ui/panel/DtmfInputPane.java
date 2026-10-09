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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.DataConversionException;
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
 * User Input by DTMF Control Pane (dial pad)</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see SoundCardDevice
 * @see SoundCardUI
 */
@SuppressWarnings("unchecked")
public class DtmfInputPane<H extends SoundCardHandle> extends JPanel {
    // The container of user's input
    private final transient JTextField dtmfBuffer = new JTextField();
    private final transient JPanel dialPad = new JPanel(new GridLayout(4, 3), false);
    private final transient Lock updateLock = new ReentrantLock(true);
    // telephony device stuff
    private final transient PhoneCallSession<H> activePhoneCallSession;
    private final transient SoundCardDevice<H, ?> telephonyDevice;

    public DtmfInputPane(final SoundCardUI<H> rootPanel) {
        super(new BorderLayout(), true);
        activePhoneCallSession = rootPanel.getActiveSession();
        telephonyDevice = (SoundCardDevice<H, ?>) this.activePhoneCallSession.getDevice();
        add(dtmfBuffer, BorderLayout.NORTH);
        dtmfBuffer.setEditable(false);
        final String[] dialPadSymbols = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"};
        Arrays.stream(dialPadSymbols).forEach(symbol -> dialPad.add(new DialPadButton(symbol)));
        add(dialPad, BorderLayout.CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        updateSafely(() -> {
            dtmfBuffer.setText("");
            Arrays.stream(dialPad.getComponents())
                    .filter(JButton.class::isInstance).forEach(b -> setEnabled(enabled));
        });
    }

    @Deprecated
    public static <H extends SoundCardHandle> void main(String[] args) throws IOException, DataConversionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        SoundCardServiceProvider<H> provider = new SoundCardServiceProvider<>(scheduler);
        try (SoundCardDevicesFactory<H, ?> factory = new SoundCardDevicesFactory<>(scheduler, provider)) {
            factory.open();
            SoundCardDevice<H, ?> device = factory.devices().findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No devices in the factory"));
            DtmfInputPane<H> pane = new DtmfInputPane<>(new SoundCardUI<>(device.startSession()));
            JFrame frame = new JFrame("emulator");
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        }
    }

    // private methods
    private void updateSafely(Runnable update) {
        try {
            updateLock.lock();
            update.run();
        } finally {
            updateLock.unlock();
        }
    }

    // private inner classes
    private class DialPadButton extends JButton {
        /**
         * Creates a button with text.
         *
         * @param input the text of the button
         */
        public DialPadButton(String input) {
            super(input);
            setFocusPainted(false);
            addActionListener(e -> updateSafely(() -> {
                dtmfBuffer.setText(dtmfBuffer.getText() + input);
                telephonyDevice.userInput(activePhoneCallSession, input);
            }));
        }
    }
}
