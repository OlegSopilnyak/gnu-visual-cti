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

import org.visualcti.server.core.unit.message.MessageFamilyType;
import org.visualcti.server.core.unit.message.action.UnitActionError;
import org.visualcti.server.core.unit.message.UnitMessage;

/**
 * Implementation: The Server Unit Activity Error Message
 *
 * @see UnitActionError
 */
class UnitError extends  UnitMessageAdapter implements UnitActionError {
    // nested error's exception (can be null)
    private transient ErrorNestedException nestedException;

    /**
     * <accessor>
     * To get the messages family type of the message
     *
     * @return type of the unit's action message
     * @see MessageFamilyType#ERROR
     */
    @Override
    public MessageFamilyType getFamilyType() {
        return MessageFamilyType.ERROR;
    }

    /**
     * <mutator>
     * To set up the messages family type of the message (ignore setter for this kind of the message)
     *
     * @param messageFamilyType new value of message's family type
     * @return reference to the message
     * @see UnitMessageAdapter#setFamilyType(MessageFamilyType)
     */
    @Override
    public UnitMessage setFamilyType(MessageFamilyType messageFamilyType) {
        // do nothing
        return this;
    }

    /**
     * <accessor>
     * The get access to the nested error's exception (can be null)
     *
     * @see UnitActionError#EXCEPTION_PARAMETER_NAME
     * @see ErrorNestedException
     */
    @Override
    public Exception getNestedException() {
        return nestedException;
    }

    /**
     * <mutator>
     * To set up the nested error's exception
     *
     * @param nestedException new value of the nested error's exception
     * @return reference to the message
     */
    @Override
    public UnitActionError setNestedException(Exception nestedException) {
        this.nestedException = nestedException instanceof ErrorNestedException ?
                (ErrorNestedException) nestedException
                : new ErrorNestedException(nestedException);
        return this;
    }
}
