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
import java.util.*;
    /**
    Inner class user information
	<user login="adm" password="adm" group="Administrators" name="Oleg Sopilnyak">
olegsopilnyak@yahoo.com
    <filter default="allowed">
			<!--Filter for Servers-->
			<rule path="{Server}" type="allowed" error="true" event="stop|start|state" command="get|set" response="true">
				<!--Permissible definition of a rule of a filter-->
			</rule>
		</filter>
	</user>
    */
    public final class UserInfo
    {
        /**
        Constructor
        */
        public UserInfo(Element xml)
        {
            this.contacts = xml.getText();
            this.login = xml.getAttributeValue("login");
            this.password = xml.getAttributeValue("password");
            String group = xml.getAttributeValue("group");
            if (group != null)
            {
                StringTokenizer st=new StringTokenizer(group,", ");
                while(st.hasMoreTokens()) this.setGroup( st.nextToken() );
            }
            this.name = xml.getAttributeValue("name");
            if (this.password == null) this.password=this.login;
            if (this.name == null) this.name=this.login;
            if (this.contacts == null) this.contacts="Unknown";
            this.filter = new Filter(xml.getChild("filter"));
        }
        /** Overrided boolean Object.equals(Object o) */
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o instanceof UserInfo)
            {
                UserInfo info = (UserInfo)o;
                return info.getLogin().equals(this.login);
            }
            return false;
        }
        /**
        <accessor>
        check, is Object maked well?
        */
        public final boolean isValid(){return this.login != null;}
        /**
        <attribute> Login
        */
        private String login;
        /**
        <accessor>
        Get access to user's login
        */
        public final String getLogin(){return this.login;}
        /**
        <attribute> Password
        */
        private String password;
        /**
        <accessor>
        Get access to user's password
        */
        public final String getPassword(){return this.password;}
        /**
        <attribute> Name
        */
        private String name;
        /**
        <accessor>
        Get access to user's name
        */
        public final String getName(){return this.name;}
        /**
        <attribute> Contacts
        */
        private String contacts;
        /**
        <accessor>
        Get access to user's name
        */
        public final String getContacts(){return this.name;}
        /**
        <attribute> Group names set
        */
        private final ArrayList groups = new ArrayList();
        /**
        <accessor>
        Get set of group name
        */
        public final Iterator groups(){return this.groups.iterator();}
        /**
        <mutator>
        To assign group name for user
        */
        public final UserInfo setGroup(String group)
        {
            if ( !this.groups.contains(group) ) this.groups.add(group);
            return this;
        }
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
        public final UserInfo setFilter(Filter filter)
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
            Element xml = new Element("user").setText(this.contacts);
            xml.setAttribute(new Attribute("login",this.login));
            xml.setAttribute(new Attribute("password",this.password));
            xml.setAttribute(new Attribute("name",this.name));
            String group=""; boolean primary=true;
            for(Iterator i=this.groups();i.hasNext();) {
                if (primary) {group+=i.next();primary=false;}
                else group += (", "+i.next());
            }
            xml.setAttribute(new Attribute("group",group));
            xml.addContent( this.filter.getXML() );
            return xml;
        }
    }
