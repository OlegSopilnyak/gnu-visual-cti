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
import java.util.Optional;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.visualcti.server.core.unit.ServerUnit;
import org.visualcti.server.service.Service;
import org.visualcti.server.service.ServicesManager;
import org.visualcti.server.unit.RunnableUnitAdapter;
import org.visualcti.server.unit.ServerUnitAdapter;

/**
 * <services-manager-facade>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Facade:Implementation: The manager of all Services of the Server</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see RunnableUnitAdapter
 * @see ServicesManager
 */
public class AbstractServicesManager extends RunnableUnitAdapter implements ServicesManager {
    protected AbstractServicesManager() {
        this.unitPath = UNIT_PATH;
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
     * <checker>
     * To check is unit can start according the internal state
     *
     * @return true if unit can start
     */
    @Override
    public boolean canStartUnit() {
        return services().findAny().isPresent();
    }

    /**
     * <mutator>
     * to add child to the server unit composite units tree<BR/>
     * set up the owner for the child unit current unit
     * Works for Service units only
     *
     * @param unit the unit to add
     * @return true if it's succeeded
     * @see ServerUnitAdapter#add(ServerUnit)
     * @see Service
     */
    @Override
    public boolean add(ServerUnit unit) {
        return Optional.ofNullable(unit).filter(Service.class::isInstance).map(super::add).orElse(false);
    }
}
