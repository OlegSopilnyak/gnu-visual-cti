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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PhoneNumberTest {

    @Test
    public void shouldBuildGeneralOf() {
        // preparing test data
        String phoneNumberAsString = "+1 2 123 4567 ext. 4";
        int countryCode = 1;
        int areaCode = 2;
        int number = 1234567;
        int extension = 4;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.of(countryCode, areaCode, number, extension);

        // check results
        assertThat(phoneNumber.countryCode()).isEqualTo(countryCode);
        assertThat(phoneNumber.areaCode()).isEqualTo(areaCode);
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isEqualTo(extension);
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildInternationalOf_WithExtension() {
        // preparing test data
        String phoneNumberAsString = "+1 2 123 4567 ext. 4";
        int countryCode = 1;
        int areaCode = 2;
        int number = 1234567;
        int extension = 4;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.internationalOf(countryCode, areaCode, number, extension);

        // check results
        assertThat(phoneNumber.countryCode()).isEqualTo(countryCode);
        assertThat(phoneNumber.areaCode()).isEqualTo(areaCode);
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isEqualTo(extension);
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildInternationalOf_NoExtension() {
        // preparing test data
        String phoneNumberAsString = "+1 2 123 4567";
        int countryCode = 1;
        int areaCode = 2;
        int number = 1234567;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.internationalOf(countryCode, areaCode, number);

        // check results
        assertThat(phoneNumber.countryCode()).isEqualTo(countryCode);
        assertThat(phoneNumber.areaCode()).isEqualTo(areaCode);
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isZero();
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildLocalOf_WithExtension() {
        // preparing test data
        String phoneNumberAsString = "(2) 123 4567 ext. 4";
        int areaCode = 2;
        int number = 1234567;
        int extension = 4;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.localOf(areaCode, number, extension);

        // check results
        assertThat(phoneNumber.countryCode()).isZero();
        assertThat(phoneNumber.areaCode()).isEqualTo(areaCode);
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isEqualTo(extension);
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildLocalOf_NoExtension() {
        // preparing test data
        String phoneNumberAsString = "(2) 123 4567";
        int areaCode = 2;
        int number = 1234567;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.localOf(areaCode, number);

        // check results
        assertThat(phoneNumber.countryCode()).isZero();
        assertThat(phoneNumber.areaCode()).isEqualTo(areaCode);
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isZero();
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildDomesticOf_WithExtension() {
        // preparing test data
        String phoneNumberAsString = "123 4567 ext. 4";
        int number = 1234567;
        int extension = 4;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.domesticOf(number, extension);

        // check results
        assertThat(phoneNumber.countryCode()).isZero();
        assertThat(phoneNumber.areaCode()).isZero();
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isEqualTo(extension);
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildDomesticOf_NoExtension() {
        // preparing test data
        String phoneNumberAsString = "123 4567";
        int number = 1234567;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.domesticOf(number);

        // check results
        assertThat(phoneNumber.countryCode()).isZero();
        assertThat(phoneNumber.areaCode()).isZero();
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isZero();
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }

    @Test
    public void shouldBuildDomesticOf_NoExtensionShortNumber() {
        // preparing test data
        String phoneNumberAsString = "123 4";
        int number = 1234;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.domesticOf(number);

        // check results
        assertThat(phoneNumber.countryCode()).isZero();
        assertThat(phoneNumber.areaCode()).isZero();
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isZero();
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }
    @Test
    public void shouldBuildDomesticOf_NoExtensionVeryShortNumber() {
        // preparing test data
        String phoneNumberAsString = "12";
        int number = 12;

        // acting
        PhoneNumber phoneNumber = PhoneNumber.domesticOf(number);

        // check results
        assertThat(phoneNumber.countryCode()).isZero();
        assertThat(phoneNumber.areaCode()).isZero();
        assertThat(phoneNumber.number()).isEqualTo(number);
        assertThat(phoneNumber.extension()).isZero();
        assertThat(phoneNumber.toString()).isEqualTo(phoneNumberAsString);
    }
}