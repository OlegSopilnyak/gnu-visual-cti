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
package org.visualcti.server;

import java.util.*;
import org.jdom.*;
import java.rmi.registry.Registry;
//import org.visualcti.util.Config;
import org.visualcti.server.core.unit.message.command.UnknownCommandException;

/**
Parent of any server part
atomic(indivisible) piece of server
*/
public abstract class serverUnitAdapter implements serverUnit
{
    /**
    <accessor>
    To get local RMI registry for share objects
    */
    public Registry localRegistry() {
        try {
            return this.getOwner().localRegistry();
        }catch(NullPointerException e) {
            return null;
        }
    }
//////////////// ACTIONS PART (begin) ///////////////////
    /**
    <dispatcher>
    To dispatch event, error, or command response from unit
    The method will call inside of the unit.
    */
    public void dispatch(unitAction action)
    {
        if (this.owner != null) this.owner.dispatch(action);
        else {
          org.visualcti.util.Tools.error
                        (
                        "Warning! Unhandled action in "+
                        this.getPath()+
                        " action:"+
                        action
                        );
        }
    }
    /**
    <executer>
    To execute command for this unit.
    The method will call from the outside of unit.
    If command invalid, the exception will be occurred.
    */
    public void execute(unitCommand command) throws Exception
    {
        if (command.getID() == unitCommand.GET_ID)
        {   // to process meta-info get request
            Parameter target = command.getParameter("target");
            if (target != null && "meta".equals(target.getValue()))
            {
                this.getMeta(command);
                return;
            }
        }
        throw new UnknownCommandException("not supported");
    }
        /**
        Response to "get meta" command
        */
        private final void getMeta(unitCommand command)
        {
            MetaData info = new MetaData(this);
            unitResponse response = new unitResponse(command).well();
            for(Iterator i=command.parameters();i.hasNext();)
            {
                Parameter param = (Parameter)i.next();
                if (param.isOutput()) response.set(param);
            }
            info.fill(response); this.dispatch( response );
        }
//////////////// ACTIONS PART (end) ///////////////////

/////////// INHERITING PART (begin) ////////////////////
/**
<attribut>
Owner of this unit
*/
protected serverUnit owner = null;
   /**
<accessor>
To get access to owner of this unit (null for root unit)
   */
   public final serverUnit getOwner(){return this.owner;}

   /**
<mutator>
To set new owner of this unit (null for root unit)
   */
   public void setOwner(serverUnit owner)
   {
        UnitRegistry.unRegister(this);
        if (owner == null) {
            this.owner=null; this.path=this.getName();
        }else{
            String old = this.path;
            try {
                this.path = owner.getPath()+"/"+this.getName();
                UnitRegistry.register( this );
                this.owner = owner;
            }catch(Exception e){
                this.path = old;// restore old path
            }
        }
   }
/////////// INHERITING PART (end) ////////////////////

/////////////////// NAMING PART (begin) //////////////////
   /**
<accessor>
To get Name of unit (abstract)
   */
abstract public String getName();//{return "Adapter";}

/**
<attribut>
Path to unit in UnitRegistry
*/
private String path = this.getName();
   /**
<accessor>
To get Path to unit instance in repository
   */
   public String getPath(){return this.path;}
   /**
<accessor>
To get Current state of unit
   */
abstract public String getUnitState();
/////////////////// NAMING PART (end) //////////////////

///////////// PROPERTIES PART (begin) //////////////
protected Element configuration=null;
    /**
    <config>
    To configure the unit, using information from Element
    */
    public final void configure( Element configuration )
    {
        this.configuration = configuration;
        this.processConfiguration(configuration);
    }
/**
To process coniguration
Will copy information from Element configuration
to HashMap properties
(abstract)
*/
protected abstract void processConfiguration(Element xml);
/**
<attribut>
Map of unit properties
*/
protected Map properties=null;
   /**
<accessor>
get serverUnit properties
may use for visual editing in GUI
   */
   public Map getProperties(){return this.properties;}

   /**
<mutator>
assign properties set to serverUnit
Properties may changed in GUI
   */
   public void setProperties(Map properties){this.properties=properties;}

/**
<attribut>
GUI of this unit
*/
protected serverUnit.GUI GUI=null;
   /**
<accessor>
get GUI for serverUnit properties
   */
   public serverUnit.GUI getUnitGUI(){return this.GUI;}
///////////// PROPERTIES PART (end) //////////////
}
