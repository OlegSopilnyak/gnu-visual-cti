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
package org.visualcti.server.event.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import org.visualcti.server.core.unit.model.MessageFamilyType;
import org.visualcti.server.core.unit.model.UnitActionMessage;

/**
 * Implementation Adapter: The Server Unit Activity Basic Message
 *
 * @see UnitActionMessage
 */
abstract class UnitMessageAdapter implements UnitActionMessage {
    // messages family type of the message
    private transient MessageFamilyType familyType;
    // description of the message
    private transient String description = "";
    // date-time, when action has happened
    private transient Date date;
    // Path of ServerUnit in UnitRegistry
    private transient String unitPath = "Unknown";

    /**
     * <accessor>
     * To get the messages family type of the message
     *
     * @return type of the unit's action message
     * @see MessageFamilyType
     */
    @Override
    public MessageFamilyType getFamilyType() {
        return familyType;
    }

    /**
     * <mutator>
     * To set up the messages family type of the message
     *
     * @param messageFamilyType new value of message's family type
     * @return reference to the message
     */
    @Override
    public UnitActionMessage setFamilyType(MessageFamilyType messageFamilyType) {
        this.familyType = messageFamilyType;
        return this;
    }

    /**
     * <accessor>
     * To get the description
     *
     * @return description of the unit's action message
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <mutator>
     * To set up the description
     *
     * @param description new value of the unit's action message
     * @return reference to the message
     */
    @Override
    public UnitActionMessage setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * <accessor>
     * The date-time, when action has happened
     *
     * @return the value
     */
    @Override
    public Date getDate() {
        return date;
    }

    /**
     * <mutator>
     * To set up the date-time, when action has happened
     *
     * @param dateTime new value of message's date-time
     * @return reference to the message
     */
    @Override
    public UnitActionMessage setDate(long dateTime) {
        this.date = dateTime < 0 ? null : new Date(dateTime);
        return this;
    }

    /**
     * <accesor>
     * To get access to Path of ServerUnit in UnitRegistry
     *
     * @return the unit-path associated with message
     */
    @Override
    public String getUnitPath() {
        return unitPath;
    }

    /**
     * <mutator>
     * To set up the Path of ServerUnit in UnitRegistry
     *
     * @param unitPath new value of message's unit-path
     * @return reference to the message
     */
    @Override
    public UnitActionMessage setUnitPath(String unitPath) {
        this.unitPath = unitPath;
        return this;
    }

    /**
     * Compares the argument to the receiver, and answers true
     * if they represent the <em>same</em> object using a class
     * specific comparison. The implementation in Object answers
     * true only if the argument is the exact same object as the
     * receiver (==).
     *
     * @param		o Object
     *					the object to compare with this object.
     * @return		boolean
     *					<code>true</code>
     *						if the object is the same as this object
     *					<code>false</code>
     *						if it is different from this object.
     * @see			#hashCode
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnitMessageAdapter)) return false;
        UnitMessageAdapter that = (UnitMessageAdapter) o;
        return
                familyType == that.familyType &&
                Objects.equals(description, that.description) &&
                Objects.equals(date, that.date) &&
                Objects.equals(unitPath, that.unitPath);
    }

    /**
     * Calling during Java serialization
     *
     * @param stream output stream to serialize the object
     * @throws IOException if something went wrong
     * @see Serializable
     * @see ObjectOutputStream
     * @see UnitActionMessage#store(OutputStream)
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        // saving the message as XML bytes to output stream
        store(stream);
    }

    /**
     * Calling during Java deserialization
     *
     * @param stream input stream to bytes of serialized object
     * @throws IOException            if something went wrong
     * @throws ClassNotFoundException if in serialized bytes exists reference to unknown object type(class)
     * @see Serializable
     * @see ObjectInputStream
     * @see UnitActionMessage#restore(InputStream)
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        // restoring the message from the input stream
        restore(stream);
    }
}
