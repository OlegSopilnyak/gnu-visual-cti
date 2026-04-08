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
package org.visualcti.server;

import java.util.Map;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Parent of any server part atomic(indivisible) piece of the Server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
public interface serverUnit
{
    /**
    <accessor>
    To get local RMI registry for share objects
    */
    java.rmi.registry.Registry localRegistry();
//////////////// ACTIONS PART (begin) ///////////////////
    /**
    <dispatcher>
    To dispatch event, error, or command response from unit
    The method will call inside of the unit.
    */
    void dispatch(unitAction action);
    /**
    <executer>
    To execute command for this unit.
    The method will call from the outside of unit.
    If command invalid, the exception will be occurred.
    */
    void execute(unitCommand command) throws Exception;
//////////////// ACTIONS PART (end) ///////////////////

/////////// INHERITING PART (begin) ////////////////////
   /**
<accessor>
To get access to owner of this unit (null for root unit)
   */
   serverUnit getOwner();

   /**
<mutator>
To set new owner of this unit (null for root unit)
   */
   void setOwner(serverUnit owner);
/////////// INHERITING PART (end) ////////////////////

/////////////////// CORE PART (begin) //////////////////
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   byte[] getIcon();
   /**
<accessor>
To get Type of unit
   */
   String getType();
   /**
<accessor>
To get Name of unit
   */
   String getName();

   /**
<accessor>
To get Path to unit instance in repository
   */
   String getPath();
   /**
<accessor>
To get Current state of unit
   */
   String getUnitState();
/////////////////// CORE PART (end) //////////////////

///////////// PROPERTIES PART (begin) //////////////
   /**
<config>
To configure the unit, using inforamtion from Element
   */
   void configure( org.jdom.Element configuration );
  /**
<accessor>
get serverUnit properties
may use for visual editing in GUI
   */
   Map getProperties();

   /**
<mutator>
assign properties set to serverUnit
Properties may changed in GUI
   */
   void setProperties(Map properties);

   /**
<accessor>
get GUI for serverUnit properties
   */
   GUI getUnitGUI();
///////////// PROPERTIES PART (end) //////////////
   /**
   Class
   <updater>
   class for represent serverObject's Graphics User Interface
   for updating object's properties must be exetnds java.awt.Container
   */
   public interface GUI
   {
        /**
        <accessor>
        Check, is change the properties of the serverObject
        */
        boolean isUpdated();
        /**
        <accessor>
        get updated serverObject properties or null if not updated
        */
        Map getProperties();
        /**
        <mutator>
        assign properties set to GUI
        */
        void setProperties(Map properties);
    }
}
