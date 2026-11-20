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
package org.visualcti.server.service.adapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import org.jdom.Comment;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.core.XmlAware;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.core.unit.part.UnitsComposite;
import org.visualcti.server.service.Service;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * <service-facade>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Facade:Implemetation: The parent of any Service of the Server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see RunnableUnitAdapter
 * @see Service
 */
@SuppressWarnings("unchecked")
public abstract class AbstractService extends RunnableUnitAdapter implements Service {
    /**
     * <accessor>
     * To get main class of the unit
     *
     * @return class-implementation of base server unit type
     * @see ServerUnit#getUnitClass()
     */
    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return AbstractService.class;
    }

    /**
     * <accessor>
     * To get the parent class of the main class of the unit
     *
     * @return the instance of class which extends server unit main class
     * @see ServerUnit#getUnitExtendsClass()
     */
    @Override
    public Class<? extends ServerUnit> getUnitExtendsClass() {
        return Service.class;
    }

    /**
     * <accessor>
     * To get the name of the root element name in XML result
     *
     * @return the name of the unit's root element
     * @see XmlAware#getRootElementName()
     * @see XmlAware#getXML()
     */
    @Override
    public String getRootElementName() {
        return Service.XML_ROOT_NAME;
    }

    /**
     * <converter>
     * To represent entity as an XML element
     *
     * @return entity's XML
     * @see Element
     * @see XmlAware#getXML()
     */
    @Override
    public Element getXML() {
        final Element xml = new Element(getRootElementName());
        // adding service description as XML comment
        addServiceDescription(xml);
        // making the XML element of the unit by default and adding it to the XML
        return xml.addContent(super.getXML().setName(getName()));
    }

    /**
     * <converter>
     * To build parameters of the root XML element of the unit (for unit building)
     *
     * @return built the root unit's XML element
     * @see ServerUnitAdapter#buildUnitRootElement()
     * @see #getXML()
     */
    @Override
    protected Element buildUnitRootElement() {
        // building the root element of the unit by default
        final Element rootElement = super.buildUnitRootElement();
        final Predicate<Object> isComment = Comment.class::isInstance;
        // removing comments from element's content
        final Object[] children = rootElement.getContent().stream().filter(isComment.negate()).toArray();
        // putting content without comments
        return super.buildUnitRootElement().setContent(Arrays.asList(children));
    }

    /**
     * <converter>
     * To represent base parameters of unit as an XML element<BR/>
     * Here we are filling service related stuff as XML-Element
     *
     * @param rootElement the unit configuration root xml-element
     * @see Element
     * @see ServerUnitAdapter#prepareBaseUnitXML(Element)
     * @see #getXML()
     */
    @Override
    protected abstract void prepareBaseUnitXML(Element rootElement);

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
     * @see Service#extractServiceFrom(Element)
     * @see ServerUnitAdapter#setXML(Element)
     * @see #settingUpBasePart(Element)
     * @see #settingUpMainPart(Element)
     */
    @Override
    public void setXML(Element xml) throws IOException, DataConversionException, NumberFormatException, NullPointerException {
        super.setXML(extractServiceFrom(xml));
    }

    /**
     * <converter>
     * To prepare the main parameters of the unit using configuration as XML Element
     *
     * @param xml the XML Element of the unit
     * @throws IOException if something went wrong
     * @see Element
     * @see ServerUnitAdapter#settingUpMainPart(Element)
     */
    @Override
    protected void settingUpMainPart(Element xml) throws IOException {
        super.settingUpMainPart(xml);
    }

    /**
     * <converter>
     * To prepare properties of the unit using XML Element
     *
     * @param xml the root XML Element of the unit
     * @see Element
     * @see ServerUnitAdapter#settingUpPropertiesPart(Element)
     */
    @Override
    protected void settingUpPropertiesPart(Element xml) {
        super.settingUpPropertiesPart(xml);
    }

    /**
     * <accessor>
     * To check if operations with children are enabled
     *
     * @return true if operations with children are enabled
     * @apiNote The operations are disabled for the simple service type
     * @see UnitsComposite#isChildrenEnabled()
     */
    @Override
    public boolean isChildrenEnabled() {
        return false;
    }

    /// private methods
    // to add service description as XML comment above service's content XML
    private void addServiceDescription(Element xml) {
        // adding unit description as XML comment
        final String description = getUnitDescription();
        // to check the value of the unit's description
        if (isEmptyString.negate().test(description)) {
            xml.addContent(new Comment(" " + description.trim() + " "));
        }
    }
}
