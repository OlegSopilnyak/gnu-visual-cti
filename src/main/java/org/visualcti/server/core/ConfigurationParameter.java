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

import org.jdom.Element;

/**
 * Parameter: used in xml-documents
 */
public class ConfigurationParameter {
    // The name of the root XML element
    public static final String ELEMENT = "parameter";
    // The name of attribute for the name of the parameter
    private static final String NAME_ATTRIBUTE = "name";
    // The name of attribute for the type of the parameter
    private static final String TYPE_ATTRIBUTE = "type";
    // The name of attribute for the type of the parameter
    private static final String VALUE_ATTRIBUTE = "value";
    // The types of the parameter
    private static final String NUMBER_TYPE = "number";
    private static final String STRING_TYPE = "string";
    // parameter's fields
    // the name of the parameter
    private final String name;
    // the type of the parameter
    private final String type;
    // the value of the parameter
    private final String value;

    /**
     * <builder>
     * To make the parameter from parameter name and value (String value)
     *
     * @param name the name of parameter
     * @param value the value of parameter
     * @return built instance
     */
    public static ConfigurationParameter of(String name, String value) {
        return new ConfigurationParameter(name, STRING_TYPE, value);
    }

    /**
     * <builder>
     * To make the parameter from parameter name and value (Number value)
     *
     * @param name the name of parameter
     * @param value the value of parameter
     * @return built instance
     */
    public static ConfigurationParameter of(String name, Number value) {
        return new ConfigurationParameter(name, NUMBER_TYPE, value.toString());
    }

    /**
     * <builder>
     * To make the parameter from XML
     *
     * @param xml the XML of the parameter
     * @return built instance
     */
    public static ConfigurationParameter of(Element xml) {
        if (ELEMENT.equals(xml.getName())) {
            return new ConfigurationParameter(
                    xml.getAttributeValue(NAME_ATTRIBUTE),
                    xml.getAttributeValue(TYPE_ATTRIBUTE),
                    xml.getAttributeValue(VALUE_ATTRIBUTE)
            );
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) valueByType();
    }

    public Element getXml() {
        return new Element(ELEMENT)
                .setAttribute(NAME_ATTRIBUTE, name)
                .setAttribute(TYPE_ATTRIBUTE, type)
                .setAttribute(VALUE_ATTRIBUTE, value)
                ;
    }

    // private methods
    private Object valueByType() {
        switch (type) {
            case NUMBER_TYPE:
                return new Integer(value);
            case STRING_TYPE:
                return value;
        }
        return null;
    }

    private ConfigurationParameter(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}
