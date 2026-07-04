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

/**
 * Results Enumeration: The results of common telephony operation completion
 */
public enum Result implements ResultValue {
    // The error during operation is detected
    ERROR("ERROR"),
    // The result of the operation, which is not required a reason of completion
    OK("OK"),
    // The timeout expiration is detected
    TIMEOUT("TIMEOUT"),
    // The operation was interrupted by manager
    TERMINATED("TERMINATED");
    // status value
    private final String status;

    Result(String status) {
        this.status = status;
    }

    @Override
    public String getValue() {
        return status;
    }

    /**
     * Results Enumeration: The results of Phone Call telephony operation completion
     */
    public enum CALL implements ResultValue {
        // The incoming call is alerted (no answer)
        ALERTING("ALERTING"),
        // The incoming call is alerted (after answer)
        RINGS("RINGS"),
        // The telephone call disconnect is detected
        DISCONNECT("DISCONNECT");
        // status value
        private final String status;

        CALL(String status) {
            this.status = status;
        }

        @Override
        public String getValue() {
            return status;
        }

        /**
         * Results Enumeration: The results of Phone Call Analyze telephony operation completion
         * for makeCall(...) method
         */
        public enum Analysis implements ResultValue {
            // The voice is detected in the outgoing call
            VOICE("VOICE"),
            // The fax-machine is detected in the outgoing call
            FAX("FAX"),
            // The modem is detected in the outgoing call
            MODEM("MODEM"),
            // The answering machine is detected in the outgoing call
            AUTOANSWER("AUTOANSWER"),
            // The BUSY signal is detected in the outgoing call
            BUSY("BUSY"),
            // There is no answer on the outgoing call
            NO_ANSWER("NO ANSWER"),
            // There is no ringback on the outgoing call
            NO_RESPONDING("NO RINGBACK"),
            // There is no dial tone in the telephony line,
            // meaning the phone line whether not connected or doesn't work properly
            NO_DIAL_TONE("NO DIAL TONE"),
            // The special information signal on the phone line is detected
            SIT("SIT"),
            // The phone number forbidden to make call to
            BAN("BAN");
            // status value
            private final String status;

            Analysis(String status) {
                this.status = status;
            }

            @Override
            public String getValue() {
                return status;
            }
        }
    }

    /**
     * Results Enumeration: The results of Media IO operation completion
     */
    public enum IO implements ResultValue {
        // The incompatible media format is detected
        FORMAT("FORMAT"),
        // The end of media stream is reached
        EOF("EOF"),
        // The defined DTMF symbol is detected
        DTMF("DTMF"),
        // The silence during voice recording is detected
        SILENCE("SILENCE");
        // status value
        private final String status;

        IO(String status) {
            this.status = status;
        }

        @Override
        public String getValue() {
            return status;
        }
    }

    /**
     * Results Enumeration: The results Fax operation completion
     */
    public enum FAX implements ResultValue {
        // The remote fax-device is not COMPATIBLE or
        // Can't accept or transfer a fax document with the given sanction
        COMPATIBILITY("COMPATIBILITY"),
        // The communication error is detected during fax document transferring
        COMMUNICATION_ERROR("COMMUNICATION ERROR"),
        // The STOP button is pressed on the remote side
        USER_STOP("USER STOP"),
        // The inquiry to POLLING from the remote fax-device is received
        POLLING("POLLING"),
        // The remote fax-device has not accepted inquiry on POLLING
        NO_POLL("NO POLL");
        // status value
        private final String status;

        FAX(String status) {
            this.status = status;
        }

        @Override
        public String getValue() {
            return status;
        }
    }
}
