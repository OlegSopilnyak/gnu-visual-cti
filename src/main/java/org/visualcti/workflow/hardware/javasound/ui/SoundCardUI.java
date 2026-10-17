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

import static javax.swing.SwingConstants.BOTTOM;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;
import org.visualcti.workflow.hardware.javasound.ui.panel.DtmfInputPane;
import org.visualcti.workflow.hardware.javasound.ui.panel.FaxManagementPane;
import org.visualcti.workflow.hardware.javasound.ui.panel.PhoneCallsPane;
import org.visualcti.workflow.hardware.javasound.ui.panel.VoiceManagementPane;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 * Swing UI Part of the emulator</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see PhoneCallSession
 * @see PhoneCallsPane
 * @see DtmfInputPane
 */
public class SoundCardUI<H extends SoundCardHandle> extends JPanel {
    private final transient PhoneCallSession<H> deviceActivitySession;
    private final transient PhoneCallsPane<H> callsControlPane;
    private final transient DtmfInputPane<H> dtmfInputPane;
    private final transient VoiceManagementPane<H> voicePane;
    private final transient FaxManagementPane<H> faxPane;
    private final JLabel state = new JLabel("IDLE");

    public SoundCardUI(PhoneCallSession<H> deviceActivitySession) {
        super(new BorderLayout(), true);
        if (deviceActivitySession == null) {
            throw new IllegalArgumentException("PhoneCall session cannot be null.");
        }
        this.deviceActivitySession = deviceActivitySession;
        // device state
        add(state, BorderLayout.NORTH);
        state.setBorder(new TitledBorder("State of device"));
        state.setFont(new Font("sansserif", Font.BOLD, 14));
        //
        // preparing UI tabs content
        callsControlPane = new PhoneCallsPane<>(this);
        dtmfInputPane = new DtmfInputPane<>(this);
        voicePane = new VoiceManagementPane<>(this);
        faxPane = new FaxManagementPane<>(this);
        //
        // making tabbed panes
        final JTabbedPane modes = new JTabbedPane(BOTTOM);
        modes.setFont(new Font("dialog", Font.BOLD, 9));
        super.add(modes, BorderLayout.CENTER);
        modes.addTab("Call", callsControlPane);
        modes.addTab("Dial Pad", dtmfInputPane);
        modes.addTab("Voice", voicePane);
        modes.addTab("Fax", faxPane);
        setFocusable(true);
        // add listener for dtmf input
        addKeyListener(dtmfInputPane);
    }

    public void waitingForCall() {
        callsControlPane.waitingForCall();
    }

    public void makingCallFor(final PhoneCall.Number target) {
        callsControlPane.makingCall(target.toString());
    }

    public PhoneCallSession<H> getActiveSession() {
        return deviceActivitySession;
    }

    public void stateChanged(String newState) {
        state.setText(newState);
    }

    public void online() {
        callsControlPane.setEnabled(true);
        dtmfInputPane.setEnabled(true);
        voicePane.setEnabled(true);
        faxPane.setEnabled(true);
    }

    public void setHandset(boolean on) {
        callsControlPane.setHandset(on);
        dtmfInputPane.setEnabled(on);
        voicePane.setEnabled(on);
        faxPane.setEnabled(on);
    }

    public void makingCallResult(final BlockingQueue<OperationResultValue> answeredResultsQueue) {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            return;
        }
        callsControlPane.makingCallResult(answeredResultsQueue);
    }
}
