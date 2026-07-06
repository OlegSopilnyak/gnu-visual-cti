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
#include "org_visualcti_server_hardware_provider_dialogic_Hardware.h"

#include "dialogicDevice.hpp"
#include "dialogicEvent.hpp"
#include "dialogicChannel.hpp"

/*############################################################
 * Visible on all source files which include "javaCommon.h" BEGIN
 *############################################################
 */
/*
 * The reference to the Java virtual machine's instance
 */
JavaVM *jvm;
/*
 * The global JNI reference to org.visualcti.server.hardware.provider.dialogic.Hardware class
 */
jclass device_class;
/*
 * The references to Hardware class methodIDs 
 */
jmethodID 
	mid_readData,	/* java method for get voice data from java input stream */
	mid_writeData,	/* java method for put voice data to java output stream */
	mid_lessData,	/* to truncute last writed data */
	java_getBuffer;	/* to get access to I/O buffer */
/*############################################################
 * Visible on all source files which include "javaCommon.h" END
 *############################################################
 */

/*
 * To initialize the Java depended values 
 */
static
void initJava
	(
	JNIEnv *java,	/* the JNI reference to the Java environment */
	jclass device	/* the JNI reference to Hardware class */
	)
{
	/* To store the reference to current Java virtual machine (jvm) */
	if( java->GetJavaVM( &jvm ) != 0 )
	{
		printf("Error getting JVM pointer\n");
	}
	/* To store reference to Hardware class ID as global reference */
	device_class	= (jclass)java->NewGlobalRef( device );
	/* get and store method IDs of Hardware class */
	java_getBuffer	= java->GetStaticMethodID( device,"getBuffer","(II)[B");	/* Hardware.getBuffer(...) */
	mid_readData	= java->GetStaticMethodID( device,"readData","(I[BII)I");	/* Hardware.readData (...) */
	mid_writeData	= java->GetStaticMethodID( device,"writeData","(I[BII)I");	/* Hardware.writeData(....) */
	mid_lessData	= java->GetStaticMethodID( device,"lessData","(II)V");		/* Hardware.lessData (...) */
}
/*
 * to start Hardware.events thread
 */
static
void startEventsThread
	(
	JNIEnv *java, 	/* the JNI reference to the Java environment */
	jclass device	/* the JNI reference to Hardware class */
	)
{	/* to get the methodID for private static void Hardware.initEvents() */
	jmethodID mid_initEvents = java->GetStaticMethodID( device,"initEvents","()V");
	java->CallStaticVoidMethod( device, mid_initEvents ); /* to execute Hardware.initEvents() */
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    init
	 * Signature: ()V
	 * Method for initialize Dialogic hardware part, and
	 * to adjustment the Java environment
	 */
JNIEXPORT void JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_init
	(
	JNIEnv *java, 
	jclass device
	)
{
//printf("\nTo init java env\n");
	::initJava(java, device);			/* to initialize Java part */
//printf("To init dialogic device env\n");
	dialogicDevice::Initialize();	/* to initialize Dialogic part */
//printf("To start events thread\n");
	::startEventsThread(java, device);/* to start Dialogic event processing */
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    disableEvents
	 * Signature: (I)V
	 * To disable all events generations
	 */
JNIEXPORT void JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_disableEvents
	(
	JNIEnv *java,
	jclass device,
	jint handle
	)
{
	dialogicChannel::disableEvents( handle );
//printf("C: On the handle %d all events disabled\n",handle);
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    enableEvents
	 * Signature: (ILjava/lang/String;)V
	 * To enable some events generations
	 */
JNIEXPORT void JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_enableEvents
	(
	JNIEnv *java,
	jclass device,
	jint handle,
	jstring What
	)
{
	const char *what = java->GetStringUTFChars(What,0);	/* to translate the data from Java to C++ pointer */
//printf("C: On the handle %d events for %s enabled\n",handle,what);
	dialogicChannel::enableEvents( handle, what );		/* try to enable, what events generation */
	java->ReleaseStringUTFChars( What, what);			/* to release the allocated memory */
}
/*
 * The JNI reference to ID of org.visualcti.server.hardware.provider.dialogic.dialogicEvent class
 * for internal use only. Visible only in this source's file
 */
static jclass dialogicEventClass = NULL;
/*
 * Method IDs for class dialogicEvent
 */
static jmethodID constructor = 0;	/* Constructor dialogicEvent(int handle, int eventID); */
static jmethodID setReasonI = 0;	/* dialogicEvent.this.setReason(int reasonID) */
static jmethodID setReasonC = 0;	/* dialogicEvent.this.setReason(String reason) */
/*
 * To init all dialogicEvent IDs
 */
static int initDialogicEnent(JNIEnv *java)
{	/* try to get & store class and methods IDs */
	dialogicEventClass =  java->FindClass( "org/visualcti/server/hardware/provider/dialogic/dialogicEvent" );
	if (dialogicEventClass == NULL) return JNI_FALSE;/* class not found */
	/* to store the reference to class's ID */
	dialogicEventClass = (jclass)java->NewGlobalRef( dialogicEventClass );
	/* to get & store the method IDs of dialogicEvent class */
	/* <constructor> dialogicEvent(int handle, int eventID); */
	constructor = java->GetMethodID(dialogicEventClass,"<init>","(II)V");
	/* <mutator> setReason(int reasonID); */
	setReasonI  = java->GetMethodID(dialogicEventClass,"setReason","(I)V");
	/* <mutator> setReason(String reason); */
	setReasonC  = java->GetMethodID(dialogicEventClass,"setReason","(Ljava/lang/String;)V");
	return JNI_TRUE;
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    getEvent
	 * Signature: ()Lorg/visualcti/server/hardware/provider/dialogic/dialogicEvent;
	 * Method for a receive next Dialogic event,
	 * process it, make Java object(dialogicEvent) and returns it to Java
	 */
JNIEXPORT jobject 
JNICALL Java_org_visualcti_server_hardware_provider_dialogic_Hardware_getEvent
	(
	JNIEnv *java,
	jclass device
	)
{
	/* to check the intergrity */
	if ( dialogicEventClass == NULL && ::initDialogicEnent(java) == JNI_FALSE) return NULL;
	/* to get native Dialogic's event from system queue (the call will block if no events) */
	dialogicEvent *event = dialogicDevice::getEvent();
	/* to make the java object dialogicEvent(int handle, int eventID); */
	jobject javaEvent = java->NewObject
								(
								dialogicEventClass,
								constructor,
								event->getHandle(),
								event->getEventID()
								);
	/* to store reason's ID to object
	this.setReason(int reasonID); */
	java->CallVoidMethod
					(
					javaEvent,	/* dialogicEvent.this */
					setReasonI,	/* setReason(int reasonID) */
					event->getTermReason()/* parameter */
					);
	/* to store reason's description to object
	this.setReason(String reason); */
	java->CallVoidMethod
					(
					javaEvent,	/* dialogicEvent.this */
					setReasonC,	/* setReason(String reason); */
					java->NewStringUTF( event->getCtermReason() )/* parameter */
					);
	return javaEvent;/* returns the dialogicEvent's instance */
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    getNamesList
	 * Signature: ()Ljava/lang/String;
	 * Explore hardware and returns all valid resource internal names (channel names)
	 */
JNIEXPORT jstring JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_getNamesList
	(
	JNIEnv *java,
	jclass device
	)
{
	char *names = dialogicDevice::getValidNames();
	return java->NewStringUTF( names );
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    open
	 * Signature: (Ljava/lang/String;)I
	 * to open Dialogic's channel by name
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_open
	(
	JNIEnv *java, 
	jclass device, 
	jstring Name
	)
{
	const char *name = java->GetStringUTFChars(Name,0);	/* to translate the data from Java to C++ pointer */
	int handle = dialogicChannel::openChannel( name );	/* try to open the device by name */
	java->ReleaseStringUTFChars( Name, name);	/* to release allocated memory */
	return handle;/* returns result of open operation (-1 if mistake, else valid handle to device) */
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    openFax
	 * Signature: (Ljava/lang/String;)I
	 * to open Dialogic fax resource
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_openFax
	(
	JNIEnv *java,
	jclass device,
	jstring Name
	)
{
	const char *name = java->GetStringUTFChars(Name,0);		/* to translate the data from Java to C++ pointer */
	int handle = dialogicChannel::openFaxChannel( name );	/* try to open FAX device by name */
	java->ReleaseStringUTFChars( Name, name);	/* to release allocated memory */
	return handle;/* returns result of open operation (-1 if mistake, else valid handle to device) */
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    close
	 * Signature: (I)V
	 * to close opened device
	 */
JNIEXPORT void JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_close
	(
	JNIEnv *java, 
	jclass device, 
	jint handle
	)
{
	dialogicChannel::closeChannel( handle );
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    stopAll
	 * Signature: (I)V
	 * to terminate all I/O activity
	 */
JNIEXPORT void JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_stopAll
	(
	JNIEnv *java, 
	jclass device, 
	jint handle
	)
{
	dialogicChannel::stopActivity( handle );
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    errorMessage
	 * Signature: (I)Ljava/lang/String;
	 * to get access to last error message of device
	 */
JNIEXPORT jstring JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_errorMessage
	(
	JNIEnv *java,
	jclass device,
	jint handle
	)
{
	char message[200];
	dialogicChannel::getLastError(handle,message);/* to get the message */
	return java->NewStringUTF( message );/* to return the message to Java */
}

	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    isCallAlerted
	 * Signature: (I)Z
	 * check, is incoming call present
	 */
JNIEXPORT jboolean JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_isCallAlerted
	(
	JNIEnv *java, 
	jclass device, 
	jint handle		/* the handle to LSI device */
	)
{
	return dialogicChannel::isIncomingCall(handle) ? JNI_TRUE : JNI_FALSE;
}
/*
 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
 * Method:    getCallerID
 * Signature: (I)Ljava/lang/String;
 * To get access to caller ID filled incoming call's alert
 */
JNIEXPORT jstring JNICALL
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_getCallerID
	(
	JNIEnv *java, 
	jclass device, 
	jint handle		/* the handle to LSI device */
	)
{
	char number[41] = {0};
	dialogicChannel::getCallerID( handle, number );
	return java->NewStringUTF( (const char *)&number[0] );
}
	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    call
	 * Signature: (ILjava/lang/String;I)I
	 * To start making outgoing telephony call
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_call
	(
	JNIEnv *java, 
	jclass device, 
	jint handle,		/* the handle to LSI */
	jstring Number,		/* destination's address */
	jint timeout		/* maxtime to make call */
	)
{
	const char *number = java->GetStringUTFChars(Number,0);
	int result = dialogicChannel::makeCall( handle, number, timeout );
	java->ReleaseStringUTFChars( Number, number);
	return result;
}


	/*
	 * Class:     org_visualcti_server_hardware_provider_dialogic_Hardware
	 * Method:    setHook
	 * Signature: (II)I
	 * To start the change of hook state
	 */
JNIEXPORT jint JNICALL 
Java_org_visualcti_server_hardware_provider_dialogic_Hardware_setHook
	(
	JNIEnv *java, 
	jclass device, 
	jint handle,	/* the handle to LSI */
	jint state		/* new hook's state */
	)
{
	return dialogicChannel::setHookState(handle,(unsigned char)state);
}
