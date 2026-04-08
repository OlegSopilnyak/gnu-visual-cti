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
package org.visualcti.server.hardware.provider.dialogic;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The Event from Dialogic's board</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
final class dialogicEvent
{
/**
 * <attribute>
 * Dialogic's event ID
 */
private final int ID;
/**
 * <attribute>
 * The handle to device, where is event occur
 */
private final int handle;

    public dialogicEvent(int handle, int ID){this.handle = handle; this.ID = ID;}

    /**
     * <accesor>
     * To get access to device's handle
     * @return the handle to device
     */
    public final int getHandle(){return this.handle;}
    /**
     * <accesor>
     * To get access to event's ID
     * @return event's ID
     */
    public final int getEventID(){return this.ID;}
/**
 * <attribute>
 * The ID of reason (why event occur)
 */
private int reasonID;
    /**
     * <accessor>
     * To get access to reason's ID
     * @return The reason's ID
     */
    public final int getReasonID(){return this.reasonID;}
    /**
     * <mutator>
     * To setting up the reason's ID
     * @param ID
     */
    public final void setReason(int ID){this.reasonID = ID;}
/**
 * <attribute>
 * The reason of the event
 */
private String reason;
    /**
     * <accessor>
     * To get access to event's reason
     * @return the reason
     */
    public final String getReason(){return this.reason;}
    /**
     * <mutator>
     * To settin up the event's reason
     * @param reason new event's reason
     */
    public final void setReason(String reason){this.reason = reason;}


    public final String toString()
    {
      StringBuffer sb = new StringBuffer("Dialogic event ");
      sb.append("for ").append(this.handle);

      return "Dialogic event "+
          " for "+this.handle+
          " event ID = "+this.ID+
          " reason ID = "+this.reasonID+
          " reason ["+this.reason+"]";
    }
    /** get signature for dialogic event constructor */
    //public native void dialogicEvent_n(int handle, int ID);
    /** get signature for setup reason ID */
    //public native void setReason_n(int ID);
    /** get signature for setup reason string */
    //public native void setReason_n(String reason);
}
