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

import org.visualcti.core.channel.telephony.TelephonyDevice;

/**
 * Phone Call: Keep all information about phone call
 */
public interface PhoneCall {
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
     * To get access to the result of the operation that initiated or updated the call
     *
     * @return the value
     * @see ResultValue
     */
    ResultValue operationResult();

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
     * Call Number: Keep all information about phone number of the call
     *
     * @see PhoneCall#getCalledNumber()
     * @see PhoneCall#getCallingNumber()
     */
    interface Number {
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
