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
package org.visualcti.server.security;
import org.jdom.*;
    /**
    class user's Group
	<group name="Administrators">
User group, which have
permissions to control a server
<filter default="allowed">
			<!--Filter for Administrators-->
			<rule path="{Server}" type="allowed" error="true" event="stop|start|state" command="stop|start|get|set" response="true">
				<!--Permissible definition of a rule of a filter-->
			</rule>
		</filter>
	</group>
    */
    public final class UserGroup
    {
        /** constructor */
        public UserGroup(Element xml)
        {
            this.description = xml.getText();
            this.name = xml.getAttributeValue("name");
            if (this.description == null) this.description = "Group";
            this.filter = new Filter( xml.getChild("filter") );
        }
        /**
        <accessor>
        check, is Object maked well?
        */
        public final boolean isValid(){return this.name != null;}
        /**
        <attribute>
        Group name
        */
        private String name;
        /**
        <accessor>
        To get access to group name
        */
        public final String getName(){return this.name;}
        /**
        <attribute>
        Group name
        */
        private String description;
        /**
        <accessor>
        To get access to group name
        */
        public final String getDescription(){return this.description;}
        /**
        <attribute>
        user's permissions
        */
        private Filter filter;
        /**
        <accessor>
        To get access to user permissions
        */
        public final Filter getFilter(){return this.filter;}
        /**
        <mutator>
        To setting up new user's permissions
        */
        public final UserGroup setFilter(Filter filter)
        {
            this.filter=filter; return this;
        }
        /**
        <translator>
        To represent as XML Element
        */
        public final Element getXML()
        {
            if ( !this.isValid() ) return null;
            Element xml = new Element("group").setText(this.description);
            xml.setAttribute(new Attribute("name",this.name));
            xml.addContent( this.filter.getXML() );
            return xml;
        }
    }
