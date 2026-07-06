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
#include "dialogicFaxChannel.hpp"
#include "dialogicChannelInfo.hpp"
#include "org_visualcti_server_hardware_provider_dialogic_FAX.h"

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    isSupport
	 * Signature: (I)Z
	 * is device have a fax features
	 */
JNIEXPORT jboolean JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_isSupport
	(
	JNIEnv *java, 
	jclass device, 
	jint handle
	)
{
	return dialogicChannelInfo::isResource_FAX(handle) ? JNI_TRUE : JNI_FALSE;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    GetTransferredPages
	 * Signature: (I)I
	 * how many pages transferred
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_GetTransferredPages
	(
	JNIEnv *java, 
	jclass device, 
	jint handle
	)
{
	return dialogicFaxChannel::transferredPages(handle);
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    InitRecieveState
	 * Signature: (I)I
	 * to init device to recieve state
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_InitRecieveState
	(
	JNIEnv * java, 
	jclass device, 
	jint handle
	)
{
	return dialogicFaxChannel::init_RX_state(handle);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    InitTransmitState
	 * Signature: (I)I
	 * to init device to transmit state
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_InitTransmitState
	(
	JNIEnv * java, 
	jclass device, 
	jint handle
	)
{
	return dialogicFaxChannel::init_TX_state(handle);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    SetDateTime
	 * Signature: (ILjava/lang/String;)I
	 * set date time header field
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_SetDateTime
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Info
	)
{
	const char *info = java->GetStringUTFChars(Info,0);
	int result = dialogicFaxChannel::setDateTime(handle,info);
	java->ReleaseStringUTFChars( Info, info);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    SetHeaderInfo
	 * Signature: (ILjava/lang/String;)I
	 * set header info
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_SetHeaderInfo
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Info
	)
{
	const char *info = java->GetStringUTFChars(Info,0);
	int result = dialogicFaxChannel::setHeader(handle,info);
	java->ReleaseStringUTFChars( Info, info);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    SetLocalID
	 * Signature: (ILjava/lang/String;)I
	 * set local ID
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_SetLocalID
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Info
	)
{
	const char *info = java->GetStringUTFChars(Info,0);
	int result = dialogicFaxChannel::setLocalID(handle,info);
	java->ReleaseStringUTFChars( Info, info);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    SetRemoteID
	 * Signature: (ILjava/lang/String;)I
	 * set remote ID
	 */
JNIEXPORT jint JNICALL
Java_org_visualcti_server_hardware_provider_dialogic_FAX_SetRemoteID
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Info
	)
{
	const char *info = java->GetStringUTFChars(Info,0);
	int result = dialogicFaxChannel::setRemoteID(handle,info);
	java->ReleaseStringUTFChars( Info, info);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    SetStartPage
	 * Signature: (II)I
	 * to setup the begin number page of document to be send
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_SetStartPage
	(
	JNIEnv *java,
	jclass device,
	jint handle,
	jint page
	)
{
	return dialogicFaxChannel::setStartPage(handle,page);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    StartRecieve
	 * Signature: (ILjava/lang/String;Z)I
	 * to start fax document receiving
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_FAX_StartRecieve
	(
	JNIEnv *java,
	jclass device,
	jint handle,
	jstring File,
	jboolean issvrq
	)
{
	const char *file = java->GetStringUTFChars(File,0);
	int result = dialogicFaxChannel::startRecieve(handle,file,issvrq);
	java->ReleaseStringUTFChars( File, file);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_FAX
	 * Method:    StartSend
	 * Signature: (IZLjava/lang/String;ZZII)I
	 * to start transfer of faximile document
	 */
JNIEXPORT jint JNICALL
Java_org_visualcti_server_hardware_provider_dialogic_FAX_StartSend
	(
	JNIEnv *java,
	jclass device,
	jint handle,
	jboolean isTIFF,
	jstring File,
	jboolean resHi,
	jboolean issvrq,
	jint firstpg,
	jint pgcount
	)
{
	const char *file = java->GetStringUTFChars(File,0);
	int result = isTIFF ?
		dialogicFaxChannel::startSendAsTIFF
									(
									handle,	/* handle to device */
									file,	/* document's filename */
									resHi,	/* is high resolution for document */
									issvrq,	/* is call user after transmition */
									firstpg,/* to start transmition from page */
									pgcount	/* how many pages to transmition (default all) */
									)
									:
		dialogicFaxChannel::startSendAsText
									(
									handle,	/* handle to device */
									file,	/* document's filename */
									resHi,	/* is high resolution for document */
									issvrq	/* is call user after transmition */
									);
	java->ReleaseStringUTFChars( File, file);
	return result;
}
