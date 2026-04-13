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
package org.visualcti.server.core.unit.model;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Server Unit Action Messages Family Type</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public enum MessageFamilyType {
    ERROR(-1, "The error detected in the Unit"),
    STOP(0, "Stopping Unit Action Event and Command"),
    START(1, "Starting Unit Action Event and Command"),
    STATE(2, "Unit's State Updated Action Event"),
    GET(3, "Get Data from the Unit Command Type"),
    SET(4, "Set Data to the Unit Command Type");
    // the ID of event type
    public final int ID;
    // the description of event type
    private final String description;

    MessageFamilyType(int ID, String description) {
        this.ID = ID;
        this.description = description;
    }


    /**
     * <builder>
     * To build message family type by input as integer value
     *
     * @param input ID of the message family ID as integer
     * @return exists type instance or null if not found
     */
    public static MessageFamilyType of(int input) {
        for (final MessageFamilyType familyType : MessageFamilyType.values()) {
            if (familyType.ID == input) {
                return familyType;
            }
        }
        return null;
    }


    /**
     * <builder>
     * To build message family type by input as string value
     *
     * @param input the name of the message family type as string
     * @return exists type instance or null if not found
     */
    public static MessageFamilyType byName(String input) {
        for (final MessageFamilyType messageType : MessageFamilyType.values()) {
            if (messageType.name().equalsIgnoreCase(input)) {
                return messageType;
            }
        }
        return null;
    }
}
