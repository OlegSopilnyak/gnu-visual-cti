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
package org.visualcti.server.hardware.provider.dialogic;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * class realize Dialogic's SC Bus functions</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
final class SCBUS
{
/*
 ** Definitions
 **/
public static final int SC_VOX      = 0x01;                       /* Voice channel device */
public static final int SC_LSI      = 0x02;                       /* Analog Timeslot device */
public static final int SC_DTI      = 0x03;                       /* Digital Timeslot device */
public static final int SC_FAX      = 0x04;                       /* Fax channel device */
public static final int SC_MSI      = 0x05;                       /* MSI channel device */

public static final int SC_FULLDUP  = 0x00;                       /* Full duplex connection */
public static final int SC_HALFDUP  = 0x01;                       /* Half duplex connection */

      /**
       * <check> To check, is handle have a context
       * @param handle handle to test
       * @return true if handle have a context
       */
      private static final boolean valid(int handle){return Context.get(handle) != null;}
    /**
     * <check>
     * To check,is handle support SCBUS features
     * @param handle handle to test
     * @return true if handle support SCBUS
     */
    final static boolean isResource_SCBUS(int handle)
    {
        return !valid(handle) ? false:SCBUS.isSupport(handle);// (native)
    }
    /**
     * <producer>
     * To make the timeslot for the handle
     * @param handle the device's handle
     * @param type the type of device
     * @return the timeslot or DX_ERROR
     */
    final static int getTxTimeslot(int handle, int type)
    {
        return !valid(handle) ? Hardware.DX_ERROR:SCBUS.txTimeslot(handle,type);// (native)
    }
    /**
     * <action>
     * to connect handle with timeslot
     * @param handle the device's handle
     * @param type the type of device
     * @param timeslot the timeslot to attach
     * @return DX_ERROR, if can't listen
     */
    final static int listen(int handle, int type, int timeslot)
    {
        return !valid(handle) ? Hardware.DX_ERROR:SCBUS.attach(handle,type,timeslot);// (native)
    }
    /**
     * <action>
     * to disconnect handle from timeslot
     * @param handle the device's handle
     * @param type the type of device
     * @return DX_ERROR if can't unlisten
     */
    final static int unlisten(int handle, int type)
    {
        return !valid(handle)? Hardware.DX_ERROR:SCBUS.detach(handle,type);// (native)
    }
    /**
     * <action>
     * To connect two devices
     * @param handle1 the handle of device 1
     * @param type1 the type of device 1
     * @param handle2 the handle of device 2
     * @param type2 the type of device 2
     * @return DX_ERROR if can't route
     */
    final static int route
                        (
                        int handle1,
                        int type1,
                        int handle2,
                        int type2
                        )
    {
        return !valid(handle1) || !valid(handle2) ?
                        Hardware.DX_ERROR:
                        SCBUS.connect(handle1,type1,handle2,type2);// (native)
    }
    /**
     * <action>
     * To disconnect two devices
     * @param handle1 the handle of device 1
     * @param type1 the type of device 1
     * @param handle2 the handle of device 2
     * @param type2 the type of device 2
     * @return DX_ERROR if can't unroute
     */
    final static int unroute
                        (
                        int handle1,
                        int type1,
                        int handle2,
                        int type2
                        )
    {
        return !valid(handle1) || !valid(handle2) ?
                        Hardware.DX_ERROR:
                        SCBUS.disconnect(handle1,type1,handle2,type2);// (native)
    }

//********** NATIVES ***********
//********** NATIVES ***********
//********** NATIVES ***********
        /**
         * <native> To check, is device support SCBUS operstions
         * @param handle device's handle
         * @return true if support
         */
        private static native boolean isSupport(int handle);
        /**
         * <native> To make the timeslot for handle
         * @param handle device's handle
         * @param type device's type
         * @return the timeslot or DX_ERROR if can't
         */
        private static native int txTimeslot(int handle,int type);
        /**
         * <native> To attach device's handle to timeslot
         * @param handle device's handle
         * @param type device's type
         * @param timeslot the timeslot
         * @return DX_ERROR if can't
         */
        private static native int attach(int handle,int type, int timeslot);
        /**
         * <native> To detach device's handle from timeslot
         * @param handle device's handle
         * @param type device's type
         * @return DX_ERROR if can't
         */
        private static native int detach(int handle,int type);
        /**
         * <native> To route two resources
         * @param handle1 handle for resource 1
         * @param type1 type of resource 1
         * @param handle2 handle for resource 2
         * @param type2 type of resource 2
         * @return DX_ERROR if can't
         */
        private static native int connect(int handle1,int type1, int handle2,int type2);
        /**
         * <native> To unroute two resources
         * @param handle1 handle for resource 1
         * @param type1 type of resource 1
         * @param handle2 handle for resource 2
         * @param type2 type of resource 2
         * @return DX_ERROR if can't
         */
        private static native int disconnect(int handle1,int type1, int handle2,int type2);
}
