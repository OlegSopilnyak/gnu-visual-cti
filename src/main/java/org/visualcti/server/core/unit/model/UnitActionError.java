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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.visualcti.server.Parameter;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Server's Unit Error Message</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface UnitActionError extends UnitActionMessage {
    // the name of exception's attribute in serialization/deserialization parameters
    String EXCEPTION_PARAMETER_NAME = "@exception";

    /**
     * <accessor>
     * The get access to the nested error's exception (can be null)
     *
     * @see UnitActionError#EXCEPTION_PARAMETER_NAME
     * @see ErrorNestedException
     */
    Exception getNestedException();

    /**
     * <mutator>
     * To set up the nested error's exception
     *
     * @param nestedException new value of the nested error's exception
     * @return reference to the message
     */
    UnitActionError setNestedException(Exception nestedException);

    /**
     * <builder>
     * To make the base part of the Unit Action Message XML
     *
     * @return base part of the message XML
     * @see Element
     * @see UnitActionMessage#baseMessageXML()
     * @see UnitActionError#EXCEPTION_PARAMETER_NAME
     * @see UnitActionError#getNestedException()
     * @see ErrorNestedException#toString()
     */
    @Override
    default Element baseMessageXML() {
        final Element baseMessageXML = UnitActionMessage.super.baseMessageXML();
        final Exception nestedException = getNestedException();
        if (nestedException != null) {
            final Parameter parameter = new Parameter(EXCEPTION_PARAMETER_NAME, nestedException.toString());
            baseMessageXML.addContent(parameter.input().getXML());
        }
        return baseMessageXML;
    }

    /**
     * To update the message by restored parameter
     *
     * @param parameter the value
     * @see UnitActionMessage#updateMessagePropertyBy(Parameter)
     */
    @Override
    default void updateMessagePropertyBy(final Parameter parameter) {
        if (EXCEPTION_PARAMETER_NAME.equals(parameter.getName())) {
            final String stackTrace = parameter.getValue("Invalid stacktrace!");
            setNestedException(new ErrorNestedException(stackTrace));
        }
    }

    /**
     * class for represent an exception used in the nested-exception of the error-message
     *
     * @see UnitActionError#getNestedException()
     */
    final class ErrorNestedException extends Exception {
        private transient final String stack;
        private transient String message = "";

        private void processStack() {
            StringTokenizer st = new StringTokenizer(this.stack, "\n\r");
            this.message = st.nextToken();
        }

        public ErrorNestedException(String stackTrace) {
            this.stack = stackTrace;
            this.processStack();
        }

        public ErrorNestedException(Throwable t) {
            // save input exception's message to the attribute
            message = t.getMessage();
            // saving stack trace of the exception
            final StringWriter writer = new StringWriter();
            try (final PrintWriter printWriter = new PrintWriter(writer, true)) {
                // save stack to object's attribute
                t.printStackTrace(printWriter);
                // flush changes to the writer
                printWriter.flush();
            }
            // save input exception's stack trace the attribute
            stack = writer.toString();
        }

        public String toString() {
            return this.stack;
        }

        public String getMessage() {
            return this.message;
        }

        public String getLocalizedMessage() {
            return getMessage();
        }

        public void printStackTrace() {
            this.printStackTrace(System.err);
        }

        public void printStackTrace(final java.io.PrintStream s) {
            synchronized (s) {
                s.println(this.stack);
            }
        }

        public void printStackTrace(final java.io.PrintWriter s) {
            synchronized (s) {
                s.println(this.stack);
            }
        }
    }
}
