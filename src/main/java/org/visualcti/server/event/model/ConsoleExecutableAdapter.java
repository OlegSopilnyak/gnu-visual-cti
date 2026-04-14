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


import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.model.ServerConsoleExecutable;
import org.visualcti.server.core.unit.model.UnitActionMessage;

/**
 * Implementation Adapter: The Server Unit Activity Basic Executable
 *
 * @see UnitActionMessage
 */
abstract class ConsoleExecutableAdapter extends UnitMessageAdapter implements ServerConsoleExecutable {
    // the optional parameters associated with executable entity
    private transient Map<String, Parameter> parameters = new ConcurrentHashMap<>();
    // correlation ID of the executable entity message
    private transient String correlationId = "_";
    // name of the link to the console shell
    private transient String linkName = "Unknown";

    /**
     * <accessor>
     * To get the correlation ID of the executable message
     * Uses, in general, for correlation request/response pair
     *
     * @return current message correlation ID
     */
    @Override
    public String getCorrelationID() {
        return correlationId;
    }

    /**
     * <mutator>
     * To set up the correlation ID of the executable message
     *
     * @param correlationId new value of message's correlation ID
     * @return reference to the message
     */
    @Override
    public ServerConsoleExecutable setCorrelationID(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * <initializer>
     * To prepare parameters container for updates
     *
     * @see #setParameter(Parameter)
     */
    @Override
    public synchronized void initializeParameters() {
        if (parameters == null) {
            parameters = new ConcurrentHashMap<>();
        }
    }

    /**
     * <accessor>
     * To get the stream to executable parameters
     *
     * @return stream of available parameters
     * @see Parameter
     */
    @Override
    public Stream<Parameter> getParameters() {
        return parameters.values().stream();
    }

    /**
     * <accessor>
     * To get the parameter by name
     *
     * @param name the name of the parameter
     * @return parameter value or empty
     * @see Parameter
     * @see Optional
     */
    @Override
    public Optional<Parameter> getParameter(String name) {
        return Optional.ofNullable(parameters.get(name));
    }

    /**
     * <mutator>
     * To set up the parameter of executable (parameter instance shouldn't be null)
     *
     * @param parameter the parameter value to set for executable
     * @return reference to the executable
     * @see Parameter
     */
    @Override
    public ServerConsoleExecutable setParameter(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
        return this;
    }

    /**
     * <accessor>
     * To get the name of the link to the console shell
     *
     * @return the name of the link
     */
    @Override
    public String getLinkName() {
        return linkName;
    }

    /**
     * <mutator>
     * To set up the name of the link to the console shell
     *
     * @param linkName the name of the link
     * @return reference to the executable
     */
    @Override
    public ServerConsoleExecutable setLinkName(String linkName) {
        this.linkName = linkName;
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
     * @see UnitMessageAdapter#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConsoleExecutableAdapter)) return false;
        if (!super.equals(o)) return false;
        ConsoleExecutableAdapter that = (ConsoleExecutableAdapter) o;
        return Objects.equals(parameters, that.parameters) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(linkName, that.linkName);
    }
}
