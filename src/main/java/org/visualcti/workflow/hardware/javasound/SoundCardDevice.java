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
package org.visualcti.workflow.hardware.javasound;

import javax.swing.JFrame;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyFactory;
import org.visualcti.core.channel.telephony.TelephonyServiceProvider;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyDevice;
import org.visualcti.core.channel.telephony.adapter.AbstractTelephonyFactory;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.core.channel.telephony.part.CallsPortEngine;
import org.visualcti.core.channel.telephony.part.FaxMachineEngine;
import org.visualcti.core.channel.telephony.part.MultimediaEngine;
import org.visualcti.core.channel.telephony.part.TonesEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractCallsPortEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractFaxMachineEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractMultimediaEngine;
import org.visualcti.core.channel.telephony.part.adapter.AbstractTonesEngine;
import org.visualcti.workflow.hardware.javasound.ui.SoundCardUI;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * sound-device-emulator of a telephony device</p>
 *
 * @param <H> sound-card device handle type
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractTelephonyDevice
 */
@SuppressWarnings("unchecked")
public class SoundCardDevice<H extends SoundCardHandle, F extends AbstractTelephonyFactory<H, ?>>
        extends AbstractTelephonyDevice<H, F> {
    private final AtomicReference<SoundCardUI<H>> uiHolder = new AtomicReference<>(null);

    public SoundCardDevice(String name, TelephonyServiceProvider<H> provider) {
        super(name, provider);
    }

    protected SoundCardDevice(String name, TelephonyServiceProvider<H> provider,
                              CallsPortEngine<H> calls, TonesEngine<H> tones, MultimediaEngine<H> media, FaxMachineEngine<H> faxes) {
        super(name, provider, calls, tones, media, faxes);
    }

    /**
     * <action>
     * To create and start device's session
     *
     * @return opened device's session
     * @throws IOException if device cannot start the session
     * @see Device#open()
     * @see #createSessionFor(Object)
     * @see #getProvider()
     */
    @Override
    public <S extends DeviceActivitySession<H>> S startSession() throws IOException {
        final PhoneCallSession<H> session = super.startSession();
        final SoundCardUI<H>former = uiHolder.getAndSet(new SoundCardUI<>(session));
        if (former != null) {
            former.setVisible(false);
        }
        return (S) session;
    }

    /**
     * <accessor>
     * To check, whether device can be used in operations of connections (conference)
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can be shared for another device
     * @see TelephonyFactory
     * @see CallsPortEngine.Parameter#SHARE_CALL_PORT_ALLOWED
     */
    @Override
    public boolean canBeConnected() {
        // cannot no doubts
        return false;
    }

    @Override
    protected H wrongHandle() {
        return SoundCardHandle.wrong();
    }

    @Override
    protected H errorHandle() {
        return wrongHandle();
    }

    @Override
    protected CallsPortEngine<H> callsPart() {
        return new CallControl();
    }

    @Override
    protected TonesEngine<H> tonesPart() {
        return new Tones();
    }

    @Override
    protected MultimediaEngine<H> mediaPart() {
        return new Media();
    }

    @Override
    protected FaxMachineEngine<H> faxPart() {
        return new Fax();
    }

    /**
     * <action>
     * To end a phone call.
     *
     * @param session the phone call's session, device is working with
     * @return true if operation complete successfully
     * @see PhoneCallSession
     * @see #isOpened()
     */
    @Override
    public boolean dropCall(final PhoneCallSession<H> session) {
        session.alive(false);
        uiHolder.get().setHandset(false);
        return true;
    }

    /**
     * <debug>
     * To show the ui of device in the Frame
     */
    public final void show() {
        JFrame frame = new JFrame("emulator");
        frame.getContentPane().add(this.uiHolder.get(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * <notify>
     * The notify from UI "Ring" button
     */
    public void callAlerted(final PhoneCallSession<H> session) {
        final SoundCardUI<H>ui = uiHolder.get();
        session.alive(!session.isAlive());
        ui.setHandset(session.isAlive());
    }

    /**
     * <notify>
     * To notify from UI "Answer" button
     *
     * @param analyzeResult CallAnalyze result
     */
    public final void answerCall(OperationResultValue analyzeResult) {
        // TODO add communication with sound card device
    }

    /**
     * <notify>
     * From UI notification when pressed button in dial pad
     * @param activePhoneCallSession active device's session
     * @param input pressed symbol
     */
    public void userInput(PhoneCallSession<H> activePhoneCallSession, String input) {
        // TODO add communication with sound card device
    }

    /// inner classes
    // PhoneCall management
    private class CallControl extends AbstractCallsPortEngine<H> {

    }

    // Telephony tones/user's input management
    private class Tones extends AbstractTonesEngine<H> {

    }

    // Audio Play/Record management
    private class Media extends AbstractMultimediaEngine<H> {

    }

    // Fax transmitting/receiving management
    private class Fax extends AbstractFaxMachineEngine<H> {

    }
}
