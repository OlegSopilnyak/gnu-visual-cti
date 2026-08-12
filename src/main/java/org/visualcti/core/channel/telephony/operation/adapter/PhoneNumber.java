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
package org.visualcti.core.channel.telephony.operation.adapter;

import org.visualcti.core.channel.telephony.operation.PhoneCall;

/**
 * Call Number: Keep all information about phone number of the call
 */
public class PhoneNumber implements PhoneCall.Number {
    private final int countryCode;
    private final int areaCode;
    private final int number;
    private final int extension;

    public static PhoneNumber of(int countryCode, int areaCode, int number, int extension) {
        return new PhoneNumber(countryCode, areaCode, number, extension);
    }

    public static PhoneNumber internationalOf(int countryCode, int areaCode, int number, int extension) {
        return of(countryCode, areaCode, number, extension);
    }

    public static PhoneNumber internationalOf(int countryCode, int areaCode, int number) {
        return of(countryCode, areaCode, number, 0);
    }

    public static PhoneNumber localOf(int areaCode, int number, int extension) {
        return of(0, areaCode, number, extension);
    }

    public static PhoneNumber localOf(int areaCode, int number) {
        return of(0, areaCode, number, 0);
    }

    public static PhoneNumber domesticOf(int number, int extension) {
        return of(0, 0, number, extension);
    }

    public static PhoneNumber domesticOf(int number) {
        return of(0, 0, number, 0);
    }

    /**
     * <accssor>
     * To get the country's code
     *
     * @return the value
     */
    @Override
    public int countryCode() {
        return countryCode;
    }

    /**
     * <accssor>
     * To get the area's code
     *
     * @return the value
     */
    @Override
    public int areaCode() {
        return areaCode;
    }

    /**
     * <accssor>
     * To get the local number
     *
     * @return the value
     */
    @Override
    public int number() {
        return number;
    }

    /**
     * <accssor>
     * To get the local number extension
     *
     * @return the value
     */
    @Override
    public int extension() {
        return extension;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (countryCode > 0) {
            sb.append("+").append(countryCode).append(" ");
        }
        if (areaCode > 0) {
            if (countryCode > 0) {
                sb.append(areaCode).append(" ");
            } else {
                sb.append("(").append(areaCode).append(") ");
            }
        }
        if (number > 0) {
            final String stringNumber = String.valueOf(number);
            if (number > 999) {
                sb.append(stringNumber, 0, 3).append(" ").append(stringNumber.substring(3));
            } else {
                sb.append(stringNumber);
            }
        }
        if (extension > 0) {
            sb.append(" ext. ").append(extension);
        }
        return sb.toString();
    }

    private PhoneNumber(int countryCode, int areaCode, int number, int extension) {
        this.countryCode = countryCode;
        this.areaCode = areaCode;
        this.number = number;
        this.extension = extension;
    }
}
