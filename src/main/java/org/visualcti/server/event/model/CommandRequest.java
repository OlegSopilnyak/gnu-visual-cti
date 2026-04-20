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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jdom.Element;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;

/**
 * <prototype>
 * Implementation: The Server Console Request Executable Message
 *
 * @see ServerCommandRequest
 */
class CommandRequest extends CommandAdapter implements ServerCommandRequest {
    // is request needs response
    private transient boolean needResponse;
    // is request executed well
    private transient boolean requestSuccess;
    // is the request finished
    private transient boolean done;
    // the lock of request to provide synchronous request execution
    private transient Lock lock = new ReentrantLock();

    /**
     * <accessor>
     * To check is request executed well
     *
     * @return the value
     */
    @Override
    public boolean isSuccess() {
        return requestSuccess;
    }

    /**
     * <mutator>
     * To set up the success of the request execution
     *
     * @param requestSuccess the value
     * @return reference to the request
     */
    @Override
    public ServerCommandRequest setSuccess(boolean requestSuccess) {
        this.requestSuccess = requestSuccess;
        return this;
    }

    /**
     * <accessor>
     * To check is request needs response
     *
     * @return the value
     */
    @Override
    public boolean isNeedResponse() {
        return needResponse;
    }

    /**
     * <mutator>
     * To set up is response needed after the request execution
     *
     * @param needResponse the value
     * @return reference to the request
     */
    @Override
    public ServerCommandRequest setNeedResponse(boolean needResponse) {
        this.needResponse = needResponse;
        return this;
    }

    /**
     * <accessor>
     * To check has the request finished
     *
     * @return true if request is finished
     * @see ServerCommandRequest#assignResponse(ServerCommandResponse)
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * <mutator>
     * To set up the value of is request done flag
     *
     * @param done the value
     * @return reference to the request
     */
    @Override
    public ServerCommandRequest setDone(boolean done) {
        this.done = done;
        return this;
    }

    /**
     * <accessor>
     * To get the lock of request to provide synchronous request execution
     *
     * @return the lock associated with the request
     * @see Lock
     * @see ServerCommandRequest#isNeedResponse()
     */
    @Override
    public Lock getLock() {
        return lock;
    }

    /**
     * <mutator>
     * To set up the lock of request
     *
     * @see Lock
     * @see ServerCommandRequest#setXML(Element)
     */
    @Override
    public void setLock(Lock lock) {
        this.lock = lock;
    }

    /**
     * <action>
     * To assign the response to the request
     *
     * @param response the response to the request
     * @see ServerCommandRequest#assignResponse(ServerCommandResponse)
     * @see ServerCommandResponse
     */
    @Override
    public void assignResponse(ServerCommandResponse response) {
        ServerCommandRequest.super.assignResponse(response);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CommandRequest)) return false;
        if (!super.equals(o)) return false;
        CommandRequest that = (CommandRequest) o;
        return needResponse == that.needResponse &&
                requestSuccess == that.requestSuccess &&
                done == that.done;
    }
}
