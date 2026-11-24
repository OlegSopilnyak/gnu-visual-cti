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
package org.visualcti.server.system.manager;

import java.util.Objects;
import java.util.function.Predicate;
import org.jdom.Element;
import org.visualcti.core.channel.device.DevicesManager;
import org.visualcti.core.channel.device.adapter.AbstractDevicesManager;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.ServerUnit;

/**
 * <SystemManager>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Singleton:Manager: The manager of all Server's Channel Devices</p>
 *
 * @author Sopilnyak Oleg
 * @version 3.2
 * @see AbstractDevicesManager
 * @see SubSystem.Manager
 */
public class HardwareManager extends AbstractDevicesManager implements SubSystem.Manager {
    private static final String HARDWARE_MANAGER_UNIT_NAME = "Hardware Manager";
    // is unit's path is current te unit's name
    private static final Predicate<String> isCurrentUnitName =
            path -> Objects.equals(path, HARDWARE_MANAGER_UNIT_NAME);
    private static HardwareManager instance;
    private SubSystem subSystem;

    @Override
    public Class<? extends ServerUnit> getUnitClass() {
        return HardwareManager.class;
    }

    @Override
    public Class<? extends ServerUnit> getUnitExtendsClass() {
        return DevicesManager.class;
    }

    @Override
    public String getUnitBuilderMethodName() {
        return "getInstance";
    }

    @Override
    public SubSystem getSystem() {
        return subSystem;
    }

    public void setSystem(SubSystem subSystem) {
        this.subSystem = subSystem;
        this.unitPath = subSystem == null ? getName() : defaultManagerUnitPath();
    }

    @Override
    public String getName() {
        return HARDWARE_MANAGER_UNIT_NAME;
    }

    @Override
    protected String getUnitDescription() {
        return " The manager of channel devices ";
    }

    @Override
    public String getRootElementName() {
        return SubSystem.MANAGER_ROOT_ELEMENT;
    }

    /**
     * <builder>
     * To get access to the Tasks Manager instance as a singleton
     *
     * @return the singleton of the manager
     */
    public static HardwareManager getInstance() {
        if (instance == null) {
            synchronized (HardwareManager.class) {
                if (instance == null) {
                    instance = new HardwareManager();
                    instance.unitPath = instance.getName();
                }
            }
        }
        return instance;
    }

    /**
     * <action>
     * To reset the unit's path value to default one<BR>
     * Used only for tests purposes
     *
     * @see #unitPath
     * @see #getName()
     */
    public void resetUnitPath() {
        this.unitPath = getName();
    }

    /**
     * <action>
     * To do something before register unit in the units registry<BR>
     * updating the unit path value by default unit path of the manager if it needs
     *
     * @see #unitPath
     * @see #defaultManagerUnitPath()
     */
    @Override
    protected void beforeRegisterUnit() {
        if (isEmptyString.or(isCurrentUnitName).test(unitPath)) {
            // updating the unit path value by default manager path
            unitPath = defaultManagerUnitPath();
        }
    }

    @Override
    public void close() {
        this.unitPath = defaultManagerUnitPath();
    }

    @Override
    protected void settingUpBasePart(Element xml) {
        // doing nothing, keeps original XML
        // Here we don't use builder stuff of the server unit
    }

    @Override
    protected boolean updatedUnitConfiguration() {
        // keeping loaded configuration Element as is after changed branches
        return true;
    }

    /// private methods
    private HardwareManager() {
    }
}
