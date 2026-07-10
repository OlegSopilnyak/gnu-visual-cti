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
package org.visualcti.core.channel.telephony.impl;

import org.visualcti.core.channel.telephony.operation.PhoneCall;
import org.visualcti.core.channel.device.operation.OperationResultValue;

/**
 * Implementation: Phone Call: Keep all information about phone call
 *
 * @see PhoneCall
 */
public class AbstractPhoneCall implements PhoneCall {
    private String deviceName;
    private boolean alive;
    private OperationResultValue operationResult;
    private Number calledNumber;
    private Number callingNumber;

    /**
     * <accssor>
     * To get the device name where the call is appeared
     *
     * @return the value
     * @see PhoneCall#getDeviceName()
     */
    @Override
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * <accssor>
     * To check up the condition of a telephone call
     *
     * @return true if the call is in service
     * @see PhoneCall#isAlive()
     */
    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * <accssor>
     * To get access to the result of the operation that initiated or updated the call
     *
     * @return the value
     * @see OperationResultValue
     * @see PhoneCall#operationResult()
     */
    @Override
    public OperationResultValue operationResult() {
        return operationResult;
    }

    /**
     * <accssor>
     * To get the called number of the call
     *
     * @return the value
     * @see Number
     * @see PhoneCall#getCalledNumber()
     */
    @Override
    public Number getCalledNumber() {
        return calledNumber;
    }

    /**
     * <accssor>
     * To get the calling number of the call
     *
     * @return the value
     * @see Number
     * @see PhoneCall#getCallingNumber()
     */
    @Override
    public Number getCallingNumber() {
        return callingNumber;
    }
}
