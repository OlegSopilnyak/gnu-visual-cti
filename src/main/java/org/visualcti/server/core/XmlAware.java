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
package org.visualcti.server.core;

import static org.visualcti.util.Tools.CRLF;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.visualcti.util.Tools;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Actor: Server Entity Serialization/Deserialization of XML content support</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface XmlAware extends Serializable {
    /**
     * <tester>
     * To check is string empty
     *
     * @param value string to test
     * @return true if value is empty
     */
    default boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * <accessor>
     * To get the name of the root element name in XML result
     *
     * @return the name of root element
     * @see #getXML()
     */
    default String getRootElementName() {
        return getClass().getSimpleName();
    }
    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     */
    default Element getXML() {
        return new Element(getRootElementName());
    }

    /**
     * <converter>
     * To update the entity's fields from XML
     *
     * @param xml possible entity's XML
     * @throws IOException             if something went wrong
     * @throws DataConversionException if something went wrong
     * @throws NumberFormatException   if something went wrong
     * @throws NullPointerException    if something went wrong
     * @see Element
     */
    default void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet. Should be override in child.");
    }

    /**
     * <transport>
     * to store entity's content as XML to the OutputStream
     * will be used for transport objects in serialization flow
     *
     * @param out target output stream
     * @throws IOException if it cannot write to output stream
     * @see #store(OutputStream, boolean)
     * @see OutputStream
     */
    default void store(final OutputStream out) throws IOException {
        store(out, true);
    }

    /**
     * <transport>
     * to store entity's content as XML to the OutputStream
     * will be used for transport objects in serialization flow or for store to file
     *
     * @param out target output stream
     * @param compact flag which output we are expecting
     * @throws IOException if it cannot write to output stream
     * @see XMLOutputter
     * @see XmlAware#getXML()
     * @see XmlAware#prepareXmlDocument(Element)
     * @see XmlAware#compactXmlOutputter()
     * @see XmlAware#documentXmlOutputter()
     */
    default void store(final OutputStream out, boolean compact) throws IOException {
        // preparing outputter for the XML content
        final XMLOutputter outputter = compact ? compactXmlOutputter() : documentXmlOutputter();
        // preparing the JDOM document from entity's XML
        outputter.output(prepareXmlDocument(getXML()), out);
        out.flush();
    }

    /**
     * <builder>
     * Preparing the document from entity's XML
     *
     * @return prepared XML Document
     * @see Document
     * @see XmlAware#store(OutputStream)
     */
    default Document prepareXmlDocument(final Element xml) {
        final Document xmlDocument = new Document();
//        xmlDocument.setDocType(new DocType("serverAction","serverAction.dtd"));
        return xmlDocument.setContent(Collections.singletonList(xml.clone()));
    }

    /**
     * <builder>
     * Preparing compact XML outputter to an output stream
     *
     * @return prepared XML outputter
     * @see XMLOutputter
     * @see XmlAware#store(OutputStream, boolean)
     */
    default XMLOutputter compactXmlOutputter() {
        final XMLOutputter xmlOutStream = new XMLOutputter();
        xmlOutStream.setTextNormalize(false);
        xmlOutStream.setLineSeparator("");
        xmlOutStream.setNewlines(false);
        xmlOutStream.setIndent("");
        xmlOutStream.setIndent(false);
        return xmlOutStream;
    }

    /**
     * <builder>
     * Preparing human-readable XML outputter to an output stream
     *
     * @return prepared XML outputter
     * @see XMLOutputter
     * @see XmlAware#store(OutputStream, boolean)
     */
    default XMLOutputter documentXmlOutputter() {
        final XMLOutputter xmlOutStream = new XMLOutputter();
        xmlOutStream.setTextNormalize( true );
        xmlOutStream.setLineSeparator( CRLF );
        xmlOutStream.setNewlines( true );
        xmlOutStream.setIndent( "\t" );
        xmlOutStream.setIndent( true );
        return xmlOutStream;
    }

    /**
     * <transport>
     * to restore entity's content from the InputStream
     * will be used for transport objects in deserialization flow
     *
     * @param in source input stream
     * @throws IOException if it cannot read from input stream
     * @see InputStream
     */
    default void restore(final InputStream in) throws IOException {
        try {
            setXML(prepareXmlDocument(in).getRootElement());
        } catch (NullPointerException | NumberFormatException | DataConversionException e) {
            e.printStackTrace(Tools.err);
            throw new IOException("Invalid XML");
        }
    }

    /**
     * <builder>
     * Preparing the document from the InputStream
     *
     * @return prepared XML Document
     * @see Document
     * @see XmlAware#restore(InputStream)
     */
    default Document prepareXmlDocument(final InputStream in) throws IOException {
        try {
            return new SAXBuilder().build(in);
        } catch (JDOMException e) {
            e.printStackTrace(Tools.err);
            throw new IOException(e.getMessage());
        }
    }
}
