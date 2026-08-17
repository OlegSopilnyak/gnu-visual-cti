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
package org.visualcti.core.channel.telephony.part;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.visualcti.core.channel.device.Device;
import org.visualcti.core.channel.device.DeviceActivitySession;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.operation.Result;
import org.visualcti.core.channel.telephony.operation.adapter.PhoneCallSession;
import org.visualcti.media.Document;
import org.visualcti.media.Fax;

/**
 * The Part of the Telephony Channel Device: The root device part of the telephony fax-document exchange management
 *
 * @param <H> the type of the telephony device's low-level operations handle
 * @see TelephonyDevicePart
 */
public interface FaxMachineEngine<H> extends TelephonyDevicePart<H> {
    /**
     * Configured Parameter Names Enumeration: The parameter names of call parts of the telephony device
     */
    enum Parameter implements Device.ParameterName {
        // whether device can operate with the fax machines
        FAX_ALLOWED("FAX"),
        FAX_TEMPORARY("FAX-TEMP-FILE"),
        FAX_DEVICE_HANDLE("FAX"),
        TRANSFERRED_FAX_PAGES("TRANSFERRED PAGES"),
        REMOTE_FAX_ID("REMOTE FAX LOCAL ID"),
        FAX_PAGE_HEADER("FAX PAGE HEADER"),
        LOCAL_FAX_ID("FAX LOCAL ID");
        private final String name;

        Parameter(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name.toLowerCase();
        }
    }

    /**
     * <action>
     * To open and activate the fax-machine on the opened telephony device session
     *
     * @param session the session of the opened device
     * @throws IOException if device cannot open fax-machine for the telephony device session
     * @see PhoneCallSession
     */
    default void open(DeviceActivitySession<H> session) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <action>
     * Closing the fax-machine part of the device
     *
     * @param session the session of the opened device
     * @see PhoneCallSession
     */
    default void close(PhoneCallSession<H> session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <accessor>
     * To check, whether device can operate with fax-machines
     * This flag, the factory may set in properties of the device
     *
     * @return true if device can operate with fax-machine
     * @see FaxMachineEngine.Parameter#FAX_ALLOWED
     */
    default boolean canFax() {
        return false;
    }

    /**
     * <accessor>
     * To get the quantity of the transferred fax-pages
     *
     * @param session the phone call's session, device is working with
     * @return how many pages transferred
     * @see FaxMachineEngine.Parameter#TRANSFERRED_FAX_PAGES
     */
    default int getTransferredPages(final PhoneCallSession<H> session) {
        return canFax() ? session.parameterOrDefault(Parameter.TRANSFERRED_FAX_PAGES, 0) : 0;
    }

    /**
     * <accessor>
     * To get the local ID of the remote fax machine
     *
     * @param session the phone call's session, device is working with
     * @return localId of the remote fax-machine
     * @see FaxMachineEngine.Parameter#REMOTE_FAX_ID
     */
    default String getRemoteID(PhoneCallSession<H> session) {
        return canFax() ? session.parameterOrDefault(Parameter.REMOTE_FAX_ID, "") : "";
    }

    /**
     * <mutator>
     * To set up the heading of page of the fax-document
     *
     * @param session the phone call's session, device is working with
     * @param header  the new value
     * @see FaxMachineEngine.Parameter#FAX_PAGE_HEADER
     */
    default void setFaxHeader(PhoneCallSession<H> session, String header) {
        if (canFax()) {
            session.parameter(Parameter.FAX_PAGE_HEADER, header);
        }
    }

    /**
     * <mutator>
     * To set up fax local ID for fax machine
     *
     * @param session the phone call's session, device is working with
     * @param localID new value of device's fax-machine localId
     * @see FaxMachineEngine.Parameter#LOCAL_FAX_ID
     */
    default void setFaxLocalID(PhoneCallSession<H> session, String localID) {
        if (canFax()) {
            session.parameter(Parameter.LOCAL_FAX_ID, localID);
        }
    }

    /**
     * <action>
     * To receive the fax document.
     *
     * @param session           the phone call's session, device is working with
     * @param target            the stream for saving data of the received fax document in a TIFF format
     * @param pollingMode       flag, to initiate receive of a fax in a polling mode;
     * @param issueVoiceRequest upon termination of receive to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result<p>
     * {@link Result.IO#EOF} - normal end of document transferring<br>
     * {@link Result.CALL#DISCONNECT} - the receiving is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of transfer starting)<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document receiving<br>
     * {@link Result.FAX#POLLING} - the inquiry on polling from the remote fax-device is received<br>
     * {@link Result.FAX#NO_POLL} - the remote fax-device has not accepted inquiry on polling<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-machine is not compatible with device's one
     * @see OperationResultValue
     */
    OperationResultValue receive(PhoneCallSession<H> session, OutputStream target, boolean pollingMode, boolean issueVoiceRequest);

    /**
     * <action>
     * To transmit the fax document.
     *
     * @param session           the phone call's session, device is working with
     * @param source            stream to fax data
     * @param format            format of data in the stream(resolution is a field)
     * @param issueVoiceRequest upon termination of reception to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * <p>
     * {@link Result.IO#EOF} - normal end of the transmitted document<br>
     * {@link Result.CALL#DISCONNECT} - the transmitting is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of reception or transfer)<br>
     * {@link Result.IO#FORMAT} - the format of the data in the transmitted file is not supported by fax-device<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document transmitting<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-device is not compatible or can't accept a fax with the given resolution<br>
     * @see Fax
     * @see OperationResultValue
     */
    OperationResultValue transmit(PhoneCallSession<H> session, InputStream source, Fax format, boolean issueVoiceRequest);

    /**
     * <action>
     * To transmit the fax document
     *
     * @param session           the phone call's session, device is working with
     * @param doc               The fax-document (the pair fax data InputStream & Format)
     * @param issueVoiceRequest upon termination of reception to give out a
     *                          sound signal on the remote fax-device
     * @return the operation's result
     * <p>
     * {@link Result.IO#EOF} - normal end of the transmitted document<br>
     * {@link Result.CALL#DISCONNECT} - the transmitting is interrupted by telephony line disconnection<br>
     * {@link Result#TIMEOUT} - the remote fax-device does not answer (there is no signal of reception or transfer)<br>
     * {@link Result.IO#FORMAT} - the format of the data in the transmitted file is not supported by fax-device<br>
     * {@link Result.FAX#COMMUNICATION_ERROR} - detected communication error during fax-document transmitting<br>
     * {@link Result.FAX#USER_STOP} - on the remote fax-device the button STOP is pressed<br>
     * {@link Result.FAX#COMPATIBILITY} - the remote fax-device is not compatible or can't accept a fax with the given resolution<br>
     * {@link Result#TERMINATED} - the fax-document has broken data input stream<br>
     * @see Document#getFormat()
     * @see Document#getInputStream()
     * @see #transmit(PhoneCallSession, InputStream, Fax, boolean)
     * @see #dispatchError(Throwable, String)
     */
    default OperationResultValue transmit(PhoneCallSession<H> session, Document doc, boolean issueVoiceRequest) {
        try {
            return transmit(session, doc.getInputStream(), doc.getFormat(), issueVoiceRequest);
        } catch (IOException e) {
            dispatchError(e, "Cannot get input stream of the Fax Document.");
            return Result.ERROR;
        }
    }

    /**
     * <action>
     * To create and dispatch the error-type message from the device
     *
     * @param exception   the cause of the error
     * @param description the description of the error
     */
    void dispatchError(Throwable exception, String description);
}
