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

Contact oleg@visualcti.org or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg@visualcti.org
Home Phone:	380-62-3851086 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server.security;

import java.lang.reflect.*;
import java.util.*;

import org.jdom.*;

import org.visualcti.server.Config;
import org.visualcti.server.action.serverAction;
/**
Class for made some filtering of action
<filter default="allowed"
    <rule .... >
/filter>
Where rule XML element:
        <rule
            path="/Service/Messenger/EMAIL"
            type="allowed"
            error="true"
            event="stop|start|state"
            command="stop|start|get|set"
            response="true"/>
*/
public final class Filter
{
private Element configuration = new Element("filter");
/** rules pool */
private final ArrayList pool = new ArrayList();
/** in no one rool not satifate the action, use this rule */
private final Rule defaultRule = new Rule("",null).setType(Rule.DENIED);
    /**
    Constructor
    */
    public Filter(Element xml)
    {
        this.configuration = xml;
        Attribute type = xml.getAttribute("default");
        if (type != null) this.defaultRule.setType( type.getValue() );
        for(Iterator i=xml.getChildren("rule").iterator();i.hasNext(); )
        {
            Element ruleXML = (Element)i.next();
            Rule rule = new Rule(ruleXML,this);
            if ( rule.isValid() ) this.pool.add(rule);
        }
    }
    /** get XML Element */
    public final Element getXML() {
        return (Element)this.configuration.clone();
    }
    /**
    to add new rule to fliter
    */
    public final void addRule(Rule rule,boolean permanent)
    {
        if ( !rule.isValid() ) return;
        Element ruleXML = rule.getXML();
        this.pool.add(rule);
        this.configuration.addContent(ruleXML);
        rule.setOwner(this); rule.setXML(ruleXML);
        if ( permanent ) this.save();
    }
    /**
    get iterator for rules
    */
    public final Iterator rules(){return this.pool.iterator();}
    /**
    is access to serverUnit allowed for this filter
    */
    public final boolean isAllowed(String unitPath)
    {
        for(Iterator i = this.rules();i.hasNext();)
        {
            Rule rule = (Rule)i.next();
            if (rule.isForUnitPath(unitPath)) return rule.isAllowed();
        }
        return this.defaultRule.isAllowed();
    }
    /**
    is serverAction allowed for this filter
    */
    public final boolean isAllowed(serverAction action)
    {
        for(Iterator i = this.rules();i.hasNext();)
        {
            Rule rule = (Rule)i.next();
            if (rule.isAppliedRuleTo(action)) return rule.isAllowed();
        }
        return this.defaultRule.isAllowed();
    }
    /** get default rule of filter */
    public final Rule getDefaultRule(){return this.defaultRule;}
/** invoke this */
private Object obj;
/** invoke save method */
private Method save;
    /** Object instance initializer */
    {
        this.obj = Config.class;
        try {this.save = Config.class.getMethod("save", new Class []{});
        }catch(NoSuchMethodException e){
            this.obj = null; this.save = null;
        }
    }
    /**
     * <mutator>
     * to setting up the save's method
     * */
    public final void setSaveInvoke(Object obj,Method save) {
        this.obj = obj; this.save = save;
    }
        /** to save filter updates */
        private final void save(){
            try {
              this.save.invoke(this.obj, null);
            }catch (Exception e){}
        }
    /**
    Filter rule
    */
    public static final class Rule
    {
        public final static int ALLOWED = 1;
        public final static int DENIED = 0;
        /**
        predefined ID for stop action
        */
        private static final short STOP_ID = 0x01;
        /**
        predefined ID for start action
        */
        private static final short START_ID = 0x02;
        /**
        predefined ID for state update action
        */
        private static final short STATE_ID = 0x04;
        /**
        predefined ID for get action
        */
        private static final short GET_ID = 0x08;
        /**
        predefined ID for set action
        */
        private static final short SET_ID = 0x10;
        /**
        table for translate from serverAction ID to Rule ID
        */
        private static final int[] rule = new int[]{STOP_ID, START_ID, STATE_ID, GET_ID, SET_ID};
        ///////////////// FIELDS //////////////
        /**
        serverUnit path to UnitRegistry
        */
        private String path = null;
        /** check is valid rule */
        public final boolean isValid(){return this.path != null;}
        /**
        Entry type (ALLOWED/DENIED)
        */
        private int type = DENIED;
        /** to get access to Rule type */
        public final int getType(){return this.type;}
        /**
        defined Rule for error
        */
        private boolean error = true;
        /** to enable errors filtering */
        public final void enableError(){this.setError(true);}
        /** to disable errors filtering */
        public final void disableError(){this.setError(false);}
        /**
        defined Rule for response
        */
        private boolean response = true;
        /** to enable response filtering */
        public final void enableResponse(){this.setResponse(true);}
        /** to disable response filtering */
        public final void disableResponse(){this.setResponse(false);}
        /**
        defined Rule event IDs
        */
        private int event = STOP_ID | START_ID | STATE_ID;
        /** to set events filtering */
        public final void setEvent(String rule){
            this.event = this.getMask(rule);
            this.xml.setAttribute(new Attribute("event",this.getMask(this.event)));
            if (this.owner != null) this.owner.save();
        }
        /**
        defined Rule command IDs
        */
        private int command =  STOP_ID | START_ID | GET_ID | SET_ID;
        /**
        to set commandss filtering
        */
        public final void setCommand(String rule) {
            this.command = this.getMask(rule);
            this.xml.setAttribute(new Attribute("command",this.getMask(this.command)));
            if (this.owner != null) this.owner.save();
        }
        /** reference to Rule owner, Filter */
        private Filter owner = null;
            private final void setOwner(Filter owner){this.owner=owner;}
        /**
        Constructor with UnitRegistry path
        */
        public Rule(String path,Filter owner) {
            this.owner = owner; this.path = path;
            this.xml.setAttribute(new Attribute("path",path));
        }
        /** reference to XML element */
        private Element xml=new Element("rule");
            /**
             * <mutator>
             * To assign the XML to the rule
             * @param xml
             */
            private final void setXML(Element xml){this.xml=xml;}
        /**
        Constructor with XML Element like this:
        <rule
            path="/Service/Messenger/EMAIL"
            type="allowed"
            error="true"
            event="stop|start|state"
            command="stop|start|get|set"
            response="true"/>
        */
        public Rule(Element xml, Filter owner)
        {
            // setting up the Rule path, no defaults!
            Attribute path = xml.getAttribute("path");
            if (path != null) this.path = path.getValue();
            // setting up the Rule type, default DENIED
            Attribute type = xml.getAttribute("type");
            if (type != null) this.setType( type.getValue() );
            // setting up error, default no error
            Attribute error = xml.getAttribute("error");
            if (error != null){
                try{this.error = error.getBooleanValue();
                }catch(JDOMException e){}
            }else this.error = false;
            // setting up event, default no errors
            Attribute event = xml.getAttribute("event");
            this.event = (event != null) ? this.getMask(event.getValue()):Rule.DENIED;
            // setting up command, default no commands
            Attribute command = xml.getAttribute("command");
            this.command = (command != null) ? this.getMask(command.getValue()):Rule.DENIED;
            // setting up response mode, default no response
            Attribute response = xml.getAttribute("response");
            if (response != null){
                try{this.response = response.getBooleanValue();
                }catch(JDOMException e){}
            }else this.response = false;
            this.xml = xml;// to store the xml inside
            this.owner = owner;// to store owner
        }
        /**
        get Filter's rule as XML
        */
        public final Element getXML()
        {
            Element xml = new Element("rule");
            xml.addContent(new Comment((this.type == ALLOWED?"Permissible":"Not permissible")+" definition of a rule of a filter"));
            xml.setAttribute(new Attribute("path",this.path));
            xml.setAttribute(new Attribute("type",(this.type==ALLOWED?"allowed":"denied")));
            xml.setAttribute(new Attribute("error",""+this.error));
            xml.setAttribute(new Attribute("event",this.getMask(this.event)));
            xml.setAttribute(new Attribute("command",this.getMask(this.command)));
            xml.setAttribute(new Attribute("response",""+this.response));
            return xml;
        }
        /**
        to set entry type
        */
        public final Rule setType(String allow)
        {
            if ("allowed".equalsIgnoreCase(allow))  this.type = ALLOWED;
            else                                    this.type = DENIED;
            return this;
        }
        /**
        to set entry type
        */
        public final Rule setType(int allow)
        {
            if (Rule.ALLOWED == allow) this.type = Rule.ALLOWED;
            this.type = DENIED;
            return this;
        }
        /**
        Check, is rule for unit Path
        */
        public final boolean isForUnitPath(String unitPath)
        {
            if ( !this.isValid() ) return false;
            return this.path.equalsIgnoreCase(unitPath);
        }
        /**
        rule to operation is applied?
        is applied rule to action
        if this action defined in this Rule
        */
        public final boolean isAppliedRuleTo(serverAction action)
        {
            if ( !this.isValid() ) return false;
            if ( !this.path.equalsIgnoreCase(action.getUnitPath()) ) return false;
            int actionID = action.getID(), ruleID = Rule.DENIED;
            switch(action.actionClass())
            {
                // to check the error's permission
                case serverAction.ERROR:
                    return this.error;
                // to the the response's permission
                case serverAction.RESPONSE:
                    return this.response;
                // to check the event's permissions
                case serverAction.EVENT:
                    try {ruleID = Rule.rule[ actionID ];
                    }catch(ArrayIndexOutOfBoundsException e){}
                    return (this.event & ruleID) != Rule.DENIED;
                // to check the command's permissions
                case serverAction.COMMAND:
                    try {ruleID = Rule.rule[ actionID ];
                    }catch(ArrayIndexOutOfBoundsException e){}
                    return (this.command & ruleID) != Rule.DENIED;
            }
            return false;
        }
        /**
        if this rule is allowed
        */
        public final boolean isAllowed(){return this.type == ALLOWED;}
            /**
            resolve string as IDs mask
            */
            private final int getMask(String rule){
                StringTokenizer st = new StringTokenizer(rule," ,|");
                int mask = 0;
                while ( st.hasMoreTokens() )
                {
                    String ID = st.nextToken();
                    if ( "stop".equalsIgnoreCase(ID) ) mask |= STOP_ID;
                    else
                    if ( "start".equalsIgnoreCase(ID) ) mask |= START_ID;
                    else
                    if ( "state".equalsIgnoreCase(ID) ) mask |= STATE_ID;
                    else
                    if ( "get".equalsIgnoreCase(ID) ) mask |= GET_ID;
                    else
                    if ( "set".equalsIgnoreCase(ID) ) mask |= SET_ID;
                }
                return mask;
            }
            /**
            to present mask, as String
            */
            private final String getMask(int mask)
            {
                String result = "";
                if ((mask & STOP_ID) != 0) result = "stop";
                if ((mask & START_ID) != 0) result += (result.length() > 0 ?"|":"")+"start";
                if ((mask & STATE_ID) != 0) result += (result.length() > 0 ?"|":"")+"state";
                if ((mask & GET_ID) != 0) result += (result.length() > 0 ?"|":"")+"get";
                if ((mask & SET_ID) != 0) result += (result.length() > 0 ?"|":"")+"set";
                return result;
            }
            /**
            to upadte error mode
            */
            private final void setError(boolean error)
            {
                this.error = error;
                xml.setAttribute(new Attribute("error",""+error));
                if (this.owner != null) this.owner.save();
            }
            /**
            to upadte response mode
            */
            private final void setResponse(boolean response)
            {
                this.response = response;
                xml.setAttribute(new Attribute("response",""+response));
                if (this.owner != null) this.owner.save();
            }
   }
}
