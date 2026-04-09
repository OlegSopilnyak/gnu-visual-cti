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
package org.visualcti.briquette;

import org.jdom.*;
import org.visualcti.server.database.connectionRequest;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI  The interface for implement Subrourine's entity</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public interface Entity
{
    /**
     * The name of root XML's element
     */
    String ELEMENT = "entity";
    /**
     * <mutator>
     * To attach the Entity to subroutine
     * @param owner the subroutine
     */
    void attach(Subroutine owner);
    /**
     * <mutator>
     * To detach the attached Entity
     */
    void detach();
    /**
     * <accessor>
     * To get access to type of the Entity
     * @return the type of the Entity
     */
    String getType();
    /**
     * <accessor>
     * To get access to the name of entity
     * */
    String getName();
    /**
     * <accessor>
     * Is this entity is local for subroutine
     * This is database's values flag
     * If false database values will saved to caller's pool
     * else values will use local pool
     * */
    boolean isLocal();
    /**
     * <executor>
     * To execute the enity
     * */
    void doIt( Subroutine owner ) throws Exception;
    /**
     * <executor>
     * To cancel Entity's execution
     * */
    void cancel();
    /**
     * <accessor>
     * To get access to formal's parameters of entity
     * */
    ParametersSet getFormalParameters();
    /**
     * <accessor>
     * To get access to request to Database's connection
     * */
    connectionRequest getConnectionRequest();
    /**
     * <translator>
     * To store the entity to XML's element
     * */
    void store(Element xml);
    /**
     * <translator>
     * To restore the entity from XML
     * */
    void restore(Element xml) throws Exception;
}
