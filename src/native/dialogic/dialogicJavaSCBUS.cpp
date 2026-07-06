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
#include "javaCommon.h"
#include "dialogicChannelSCBUS.hpp"
#include "dialogicChannelInfo.hpp"
#include "org_visualcti_server_hardware_provider_dialogic_SCBUS.h"
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_SCBUS
	 * Method:    isSupport
	 * Signature: (I)Z
	 * to check is device have a SCBUS features
	 */
JNIEXPORT jboolean JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_SCBUS_isSupport
	(
	JNIEnv *java, 
	jclass device, 
	jint handle
	)
{
	return dialogicChannelInfo::isSupportSCBUS(handle) ? JNI_TRUE:JNI_FALSE;
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_SCBUS
	 * Method:    attach
	 * Signature: (III)I
	 * to attach handle to timeslot
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_SCBUS_attach
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jint type, 
	jint timeslot
	)
{
	return dialogicChannelSCBUS::listen(handle,type,timeslot);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_SCBUS
	 * Method:    connect
	 * Signature: (IIII)I
	 * to connect two devices
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_SCBUS_connect
	(
	JNIEnv *java, 
	jclass device, 
	jint handle1, 
	jint type1, 
	jint handle2, 
	jint type2
	)
{
	return dialogicChannelSCBUS::full_route(handle1,type1,handle2,type2);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_SCBUS
	 * Method:    detach
	 * Signature: (II)I
	 * to detach handle from timeslot
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_SCBUS_detach
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jint type
	)
{
	return dialogicChannelSCBUS::unlisten(handle,type);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_SCBUS
	 * Method:    disconnect
	 * Signature: (IIII)I
	 * disconnect two devices
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_SCBUS_disconnect
	(
	JNIEnv *java, 
	jclass device, 
	jint handle1, 
	jint type1, 
	jint handle2, 
	jint type2
	)
{
	return dialogicChannelSCBUS::full_unroute(handle1,type1,handle2,type2);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_SCBUS
	 * Method:    txTimeslot
	 * Signature: (II)I
	 * to get access to device's timeslot
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_SCBUS_txTimeslot
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jint type
	)
{
	return dialogicChannelSCBUS::getTxTimeslot(handle,type);
}
