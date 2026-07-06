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
#include "org_visualcti_server_hardware_provider_dialogic_VOX.h"
#include "dialogicSignalChannel.hpp"
#include "dialogicVoiceChannel.hpp"
#include "dialogicChannelInfo.hpp"
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    isSupport
	 * Signature: (I)Z
	 * Check, is device have a VOX's features
	 */
JNIEXPORT jboolean JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_isSupport
	(
	JNIEnv *java,
	jclass device, 
	jint handle
	)
{
	return dialogicChannelInfo::isResource_VOX( handle ) ? JNI_TRUE:JNI_FALSE;
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    clearTones
	 * Signature: (I)I
	 * to clear tones table for device
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_clearTones
	(
	JNIEnv *java,
	jclass device, 
	jint handle
	)
{
	return dialogicSignalChannel::clearTones( handle );
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    dial
	 * Signature: (ILjava/lang/String;)I
	 * to dial the string
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_dial
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Number
	)
{
	const char *number = java->GetStringUTFChars(Number,0);
	int result = dialogicSignalChannel::sendSignal(handle, number);
	java->ReleaseStringUTFChars( Number, number);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    digitsBuffer
	 * Signature: (I)Ljava/lang/String;
	 * to get access to user's input
	 */
JNIEXPORT jstring JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_digitsBuffer
	(
	JNIEnv *java,
	jclass device, 
	jint handle
	)
{
	char buffer[20] = {0};
	dialogicSignalChannel::getSignalsBuffer(handle, buffer);
	return java->NewStringUTF( buffer );
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    fixTones
	 * Signature: (I)I
	 * to setup the device's tones from global table
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_fixTones
	(
	JNIEnv *java,
	jclass device, 
	jint handle
	)
{
	return dialogicSignalChannel::storeTones(handle);
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    generateTone
	 * Signature: (IIII)I
	 * to play tone
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_generateTone
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
    jint freq1, 
    jint freq2, 
    jint duration
	)
{
	return dialogicSignalChannel::playTone(handle,freq1,freq2,duration);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    getCodecsList
	 * Signature: (I)Ljava/lang/String;
	 * get access to available codecs list for device
	 */
JNIEXPORT jstring JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_getCodecsList
	(
	JNIEnv *java, 
	jclass device, 
	jint handle
	)
{
	char codecs[200];
	dialogicVoiceChannel::getAvailableCodecs(handle,codecs);
	return java->NewStringUTF( codecs );
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    getDigit
	 * Signature: (II)I
	 * to retrive one symbol (user input)
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_getDigit
	(
	JNIEnv *java, 
	jclass device, 
	jint handle,
	jint timeout
	)
{
	return dialogicSignalChannel::retrieveSignal(handle,timeout);
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    setTone
	 * Signature: (ILjava/lang/String;)I
	 * to update tone in global table
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_setTone
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Tone
	)
{
	const char *tone = java->GetStringUTFChars(Tone,0);
	int result = dialogicSignalChannel::addTelephonyTone(handle, tone);
	java->ReleaseStringUTFChars( Tone, tone);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    startPlay
	 * Signature: (ILjava/lang/String;ILjava/lang/String;)I
	 * to start a playback sound
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_startPlay
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Mask, 
	jint time, 
	jstring Codec
	)
{
	const char *mask = java->GetStringUTFChars(Mask,0);
	const char *codec = java->GetStringUTFChars(Codec,0);
	int result = dialogicVoiceChannel::startPlay(handle,mask,time,codec);
	java->ReleaseStringUTFChars( Mask, mask);
	java->ReleaseStringUTFChars( Codec, codec);
	return result;
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    startRecord
	 * Signature: (ILjava/lang/String;IILjava/lang/String;)I
	 * to start the voice's record
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_startRecord
	(
	JNIEnv *java, 
	jclass device, 
	jint handle, 
	jstring Mask, 
	jint silence, 
	jint time, 
	jstring Codec
	)
{
	const char *mask = java->GetStringUTFChars(Mask,0);
	const char *codec = java->GetStringUTFChars(Codec,0);
	int result = dialogicVoiceChannel::startRecord(handle,mask,silence,time,codec);
	java->ReleaseStringUTFChars( Mask, mask);
	java->ReleaseStringUTFChars( Codec, codec);
	return result;
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_VOX
	 * Method:    toneDetect
	 * Signature: (ILjava/lang/String;Z)I
	 * to change state of tone detection
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_VOX_toneDetect
	(
	JNIEnv *java,
	jclass device,
	jint handle,
	jstring toneName,
	jboolean enable
	)
{
	const char *tone = java->GetStringUTFChars(toneName,0);
	int res = dialogicSignalChannel::setToneDetect(handle,tone,enable);
	java->ReleaseStringUTFChars( toneName, tone);
	return res;
}
