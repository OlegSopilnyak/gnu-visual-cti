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
package org.visualcti.server.security.impl;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.jdom.*;

import org.visualcti.util.Tools;
import org.visualcti.server.*;
import org.visualcti.server.security.*;
import org.visualcti.server.action.serverAction;
import org.visualcti.server.security.Manager;
import org.visualcti.server.service.*;
/**
Security manager's implementation
*/
public final class ServerSecurityManager
                    extends ServiceAdapter
                    implements Manager
{
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return super.getIcon();}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState(){return super.getUnitState();}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return super.getType()+" security";}
    /** private for Singleton making */
    private ServerSecurityManager(){}
/** one instance for all */
private static Manager instance=null;
    /**
    <accessor>
    Singleton realization
    */
    public final static Manager getManager()
    {
        if (instance != null) return instance;
        synchronized(Manager.class) {
            if (instance == null) instance = new ServerSecurityManager();
        }
        return instance;
    }
/**
pool of filter descriptions
*/
private final HashMap filters = new HashMap();
/**
XML element contains security information;
*/
private Element security = null;
/**
File with security inforamtion
*/
private File securityFile = new File("ServerSecurity.xml");
/**
Groups pool
*/
private final ArrayList groups = new ArrayList();
/**
Users pool
*/
private final ArrayList users = new ArrayList();
/**
Unit paths pool
*/
private final ArrayList units = new ArrayList();
    /**
<processor>
to process configuration
    */
    protected final void processConfiguration(org.jdom.Element xml)
    {
        Element file = xml.getChild("security");
        if (file == null) {
            file = new Element("security").setText("conf/ServerSecurity.xml");
            Config.save();
        }
        this.securityFile = new File(file.getTextNormalize());
        this.security = Tools.xmlLoad( this.securityFile );
        xml = this.security;
        for(Iterator i=xml.getChildren("group").iterator();i.hasNext();)
        {
            Element e = (Element)i.next();
            UserGroup group = new UserGroup(e);
            if (group.isValid())this.groups.add(group);
        }
        for(Iterator i=xml.getChildren("user").iterator();i.hasNext();)
        {
            Element e = (Element)i.next();
            UserInfo user = new UserInfo(e);
            if (user.isValid())this.users.add(user);
        }
        for(Iterator i=xml.getChildren("unit").iterator();i.hasNext();)
        {
            Element e = (Element)i.next();
            this.units.add(e.getAttributeValue("name"));
        }
    }
   /**
<action>
to Start security manager
   */
   public void Start() throws IOException
   {
        if ( this.isStarted() ) return;// service already started
        if (this.security == null) throw new IOException("Not loaded security information");
        this.state = Service.State.START;// setting up new state
        this.dispatch(new unitEvent(this,serverAction.START_ID));
   }
   /**
<action>
to Stop security manager
   */
    public void Stop() throws IOException
    {
        if ( this.isStopped() ) return;
        this.state = Service.State.STOP;// setting up new state
        this.dispatch(new unitEvent(this,serverAction.STOP_ID));
    }
   /**
<accessor>
access to service name
   */
    public final String getName(){return "Security";}
    /**
<accessor>
get restrictions for user
if user not defined will
return null? or AccessDenied filter
    */
    public final Filter getFilter(String login, String password)
    {
        if ( !this.isStarted() ) return null;
        UserInfo user = this.getUser(login);
        if (user == null || !user.getPassword().equals(password)) return null;
        Filter filter = null;// resulting filter
        for(Iterator i=user.groups();i.hasNext();){
            UserGroup gr = this.getGroup( (String)i.next() );
            if (gr == null) continue;
            Filter grFilter = gr.getFilter();
            if (filter == null) filter = new Filter( grFilter.getXML() );
            else {
                filter.addRule(grFilter.getDefaultRule(), false);
                for(Iterator j = grFilter.rules();j.hasNext();)
                {
                    Filter.Rule rule = (Filter.Rule)j.next();
                    filter.addRule(rule, false);
                }
            }
        }
        if (filter == null) filter = new Filter(user.getFilter().getXML());
        else {
            filter.addRule(user.getFilter().getDefaultRule(), false);
            for(Iterator j = user.getFilter().rules();j.hasNext();)
            {
                Filter.Rule rule = (Filter.Rule)j.next();
                filter.addRule(rule, false);
            }
        }
        return filter;
    }
        /** get reference to user info by name */
        private UserInfo getUser(String login)
        {
            for(Iterator i=this.users.iterator();i.hasNext();) {
                UserInfo info = (UserInfo)i.next();
                if (info.getLogin().equals(login)) return info;
            }
            return null;
        }
        /** get reference to user group by name */
        private UserGroup getGroup(String name)
        {
            for(Iterator i=this.groups.iterator();i.hasNext();) {
                UserGroup group = (UserGroup)i.next();
                if (group.getName().equals(name)) return group;
            }
            return null;
        }
/** reference to save method */
private static Method saveMethod;
static
{
try {saveMethod = ServerSecurityManager.class.getMethod("save", new Class []{});
}catch(NoSuchMethodException e){}
}
    /**
<mutator>
to setting up filter
    */
    public final void setFilter(String login, String password,Filter filter)
    {
        if ( !this.isStarted() ) return;
        UserInfo user = this.getUser(login);
        if (user == null || !user.getPassword().equals(password)) return;
        synchronized(user){user.setFilter(filter);}
    }
    /**
<xml_saver>
To save update information
    */
    private final void save() {
      Tools.xmlSave(this.security,this.securityFile);
    }
}
