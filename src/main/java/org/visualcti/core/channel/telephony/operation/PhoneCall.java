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
package org.visualcti.core.channel.telephony.operation;

import java.io.Closeable;
import java.util.stream.Stream;
import org.visualcti.core.channel.device.operation.OperationResultValue;
import org.visualcti.core.channel.telephony.TelephonyDevice;

/**
 * Phone Call: Keep all information about phone call
 */
public interface PhoneCall extends Closeable {
    /**
     * Failed value of phone's call
     */
    PhoneCall FAILED = new PhoneCall() {
        @Override
        public void close() {

        }

        @Override
        public String getDeviceName() {
            return "no matter";
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public OperationResultValue operationResult() {
            return Result.ERROR;
        }

        @Override
        public void waitForOperationComplete(long timeout) throws InterruptedException {

        }

        @Override
        public void operationComplete(OperationResultValue completionReason) {

        }

        @Override
        public Stream<PhoneCall> joint() {
            return Stream.empty();
        }

        @Override
        public void join(PhoneCall anotherCall) {

        }

        @Override
        public Number getCalledNumber() {
            return null;
        }

        @Override
        public Number getCallingNumber() {
            return null;
        }
    };
    /**
     * <accssor>
     * To get the device name where the call is appeared
     *
     * @return the value
     * @see TelephonyDevice#getName()
     */
    String getDeviceName();

    /**
     * <accssor>
     * To check up the condition of a telephone call
     *
     * @return true if the call is in service
     */
    boolean isAlive();

    /**
     * <accessor>
     * To check up the condition of a telephone call
     *
     * @return true (The call does not serve anymore) or false(The call is in service)
     * @see #isAlive()
     */
    default boolean isDisconnected() {
        return !isAlive();
    }

    /**
     * <accssor>
     * To get access to the last result of the operation that initiated or updated the phone call
     *
     * @return the last result value
     * @see OperationResultValue
     */
    OperationResultValue operationResult();

    /**
     * <accssor>
     * To get the called number of the call
     *
     * @return the value
     * @see PhoneCall.Number
     */
    PhoneCall.Number getCalledNumber();

    /**
     * <accssor>
     * To get the calling number of the call
     *
     * @return the value
     * @see PhoneCall.Number
     */
    PhoneCall.Number getCallingNumber();

    /**
     * <action>
     * To wait the running operation complete or timeout
     *
     * @param timeout how long to wait
     * @throws InterruptedException if operation is interrupted outside
     */
    void waitForOperationComplete(long timeout) throws InterruptedException;

    /**
     * <action>
     * To wait the running operation complete
     *
     * @throws InterruptedException if operation is interrupted outside
     * @see #waitForOperationComplete(long)
     */
    default void waitForOperationComplete() throws InterruptedException {
        waitForOperationComplete(-1L);
    }

    /**
     * <action>
     * To notify about the previously running in the phone-call-session operation is completed
     *
     * @param completionReason the reason of the operation's complete
     * @see #waitForOperationComplete(long)
     */
    void operationComplete(OperationResultValue completionReason);

    /**
     * <accessor>
     * To get phone calls joint by device connection feature
     *
     * @return the stream of joint with this session other phone-call-sessions
     * @see #join(PhoneCall)
     */
    Stream<PhoneCall> joint();

    /**
     * <mutator>
     * To join another phone-call-session
     *
     * @param anotherCall another session value
     */
    void join(PhoneCall anotherCall);

    /**
     * Call Number: Keep all information about phone number of the call
     *
     * @see PhoneCall#getCalledNumber()
     * @see PhoneCall#getCallingNumber()
     */
    interface Number {
        // the none, empty phone-call number
        Number EMPTY = new Number() {
            @Override
            public int countryCode() {
                return -1;
            }

            @Override
            public int areaCode() {
                return -1;
            }

            @Override
            public int number() {
                return -1;
            }

            @Override
            public int extension() {
                return -1;
            }
        };
        /**
         * <accssor>
         * To get the country's code
         *
         * @return the value
         */
        int countryCode();

        /**
         * <accssor>
         * To get the area's code
         *
         * @return the value
         */
        int areaCode();

        /**
         * <accssor>
         * To get the local number
         *
         * @return the value
         */
        int number();

        /**
         * <accssor>
         * To get the local number extension
         *
         * @return the value
         */
        int extension();
    }
}
