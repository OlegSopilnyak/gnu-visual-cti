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
package org.visualcti.server.core.unit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.visualcti.server.Parameter;
import org.visualcti.server.core.unit.message.command.ServerCommandRequest;
import org.visualcti.server.core.unit.message.command.ServerCommandResponse;
import org.visualcti.server.core.unit.part.UnitMessageExchange;

/**
 * Metadata: The Meta Information about server unit
 *
 * @see UnitMessageExchange#execute(ServerCommandRequest)
 */
public final class UnitMetaData {
    public enum MetaDataName {
        // Meta Name, content of the image
        ICON("meta.icon"),
        // Meta Name, type of the unit
        TYPE("meta.type"),
        // Meta Name, class name of the unit
        CLASS("meta.class"),
        // Meta Name, name of the unit
        NAME("meta.name"),
        // Meta Name, the path to unit instance in the registry
        PATH("meta.path"),
        // Meta Name, the current state of the unit
        STATE("meta.state");
        //
        // the name of metadata
        private final String name;

        MetaDataName(String name) {
            this.name = name;
        }

        public static MetaDataName byMetaName(String metaName) {
            return Arrays.stream(MetaDataName.values())
                    .filter(dataName -> dataName.name.equalsIgnoreCase(metaName))
                    .findFirst().orElse(null);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    //
    // The map of unit values of the server unit by meta-names
    private final transient Map<MetaDataName, Object> meta = new HashMap<>();

    /**
     * <builder>
     * To make metadata for the server unit
     *
     * @param unit server unit
     * @return made metadata for the unit
     * @see UnitMessageExchange#execute(ServerCommandRequest)
     * @see RunnableServerUnit
     */
    public static UnitMetaData of(RunnableServerUnit unit) {
        return new UnitMetaData(unit);
    }

    /**
     * <builder>
     * To make metadata for the server unit
     *
     * @param unit server unit
     * @return made metadata for the unit
     * @see UnitMessageExchange#execute(ServerCommandRequest)
     * @see ServerUnit
     */
    public static UnitMetaData of(ServerUnit unit) {
        return new UnitMetaData(unit);
    }

    /**
     * <accessor>
     * To get body unit's Icon Image (gif | jpeg)
     *
     * @return the value
     */
    public byte[] getIcon() {
        return (byte[]) meta.get(MetaDataName.ICON);
    }

    /**
     * <accessor>
     * To get Type of unit as string (service, manager, services tree, etc.)
     *
     * @return the value
     */
    public String getType() {
        return String.valueOf(meta.get(MetaDataName.TYPE));
    }

    /**
     * <acessor>
     * Get class name of the unit
     *
     * @return the value
     */
    public String className() {
        return String.valueOf(meta.get(MetaDataName.CLASS));
    }

    /**
     * <accessor>
     * To get Name of the unit to show in UI
     *
     * @return the value
     */
    public String getName() {
        return String.valueOf(meta.get(MetaDataName.NAME));
    }

    /**
     * <accessor>
     * To get Path to unit instance in repository
     *
     * @return the value
     */
    public String getPath() {
        return String.valueOf(meta.get(MetaDataName.PATH));
    }

    /**
     * <accessor>
     * To get Current state of unit (active/passive/broken)
     *
     * @return the value
     */
    public RunnableServerUnit.UnitState currentUnitState() {
        return RunnableServerUnit.UnitState.of(meta.get(MetaDataName.STATE));
    }


    /**
     * <acessor>
     * To get metadata as string
     *
     * @return the value
     * @see Object#toString()
     */
    public String toString() {
        final RunnableServerUnit.UnitState state = currentUnitState();
        final String unitState = state == null ? "unknown" : state.toString();
        return "MetaData of " + this.getName() +
                "\n\tClass:" + this.className() +
                "\n\tState:" + unitState +
                "\n\tRegistry path:" + this.getPath() + "\n";
    }

    /**
     * <fill>
     * to transfer meta information to response according to response state
     *
     * @param response target meta parameters container
     * @see MetaDataName
     * @see ServerCommandResponse
     * @see Parameter
     */
    public void transferTo(final ServerCommandResponse response) {
        final List<String> metaNames = response.getParameters()
                .map(Parameter::getName).filter(s -> s.startsWith("meta."))
                .collect(Collectors.toList());
        // transferring unit values to the command response
        if (metaNames.isEmpty()) {
            fullTransferTo(response);
        } else {
            transferOnly(metaNames, response);
        }
    }

    // private methods
    private UnitMetaData(RunnableServerUnit unit) {
        this((ServerUnit) unit);
        meta.put(MetaDataName.STATE, unit.currentUnitState());
    }

    private UnitMetaData(ServerUnit unit) {
        meta.put(MetaDataName.ICON, unit.getIcon());
        meta.put(MetaDataName.TYPE, unit.getType());
        meta.put(MetaDataName.CLASS, unit.getClass().getName());
        meta.put(MetaDataName.NAME, unit.getName());
        meta.put(MetaDataName.PATH, unit.getPath());
    }

    private void fullTransferTo(ServerCommandResponse response) {
        if (getIcon() != null) {
            response.setParameter(new Parameter(MetaDataName.ICON.name, getIcon()).output());
        }
        response.setParameter(new Parameter(MetaDataName.TYPE.name, getType()).output());
        response.setParameter(new Parameter(MetaDataName.CLASS.name, className()).output());
        response.setParameter(new Parameter(MetaDataName.NAME.name, getName()).output());
        response.setParameter(new Parameter(MetaDataName.PATH.name, getPath()).output());
        response.setParameter(new Parameter(MetaDataName.STATE.name, currentUnitState().toString()).output());
    }

    private void transferOnly(List<String> metaNames, ServerCommandResponse response) {
        metaNames.forEach(metaName -> {
            final MetaDataName metaDataName = MetaDataName.byMetaName(metaName);
            if (metaDataName != null) {
                if (MetaDataName.ICON.name.equalsIgnoreCase(metaName)) {
                    response.setParameter(new Parameter(metaName, getIcon()).output());
                } else {
                    final Object value = meta.get(metaDataName);
                    if (value != null) {
                        response.setParameter(new Parameter(metaName, value.toString()).output());
                    }
                }
            }
        });
    }
}
