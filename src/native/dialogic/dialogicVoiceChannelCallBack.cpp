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
#include "dialogicVoiceChannel.hpp"
#include "Context.hpp"

/*
 * To receive an environment of the Java-virtual machine
 * for realize JNI features
 */
JNIEnv *dialogicVoiceChannel::getJNIEnv()
{
	JNIEnv *java;	// refer to JNI
	void *thr_args = NULL;// arguments
	if (jvm == NULL) {
		return NULL; // java not initialized
	} else if( jvm->AttachCurrentThread( (void **)&java, NULL) != 0) {
		return NULL; // can't attach thread to JVM
	} else {
		return java;// vaid refer to Java environment
	}
}
/*
 * call-back function, called when Dialogic recording the voice
 */
int dialogicVoiceChannel::writeData
								(
								int handle,			// handle to opened channel
								char *pointer,		// pointer to Dialogic data buffer
								unsigned int count	// bytes count (for write)
								)
{
	JNIEnv *java = dialogicVoiceChannel::getJNIEnv();
	Context *context = Context::getContext( handle );

	if(java == NULL || context == NULL) return DX_ERROR;/* The environment is inaccessible */
	/* We receive the reference to memory of the buffer of an exchange */
	/* call the Hardware.getBuffer(int chdev,int size) */
	jbyteArray array = (jbyteArray)java->CallStaticObjectMethod
															(
															device_class, 
															java_getBuffer,
															handle,
															count
															);
	/* Filling of memory by the data Dialogic ptr => array */
	java->SetByteArrayRegion( array, 0, count, (signed char*)pointer );
	/* Call of function serving output of this channel */
	/* Hardware.writeData(int chdev,byte[] array,int off,int size) */
	int result = java->CallStaticIntMethod
									(
									device_class, 
									mid_writeData, 
									handle, 
									array,
									0,
									count
									);	
	context->seek_pos += count;
	/* If is written down in a stream 0, the mistake (-1) means */
	return result > 0 ? count:0 ;
}
/*
 * call-back function, called when Dialogic play the sound
 */
int dialogicVoiceChannel::readData
							(
							int handle,			// handle to opened channel
							char *pointer,		// pointer to Dialogic data buffer
							unsigned int count	// bytes count (need for play)
							)
{
	JNIEnv *java = dialogicVoiceChannel::getJNIEnv();
	Context *context = Context::getContext( handle );
	
	if(java == NULL || context == NULL) return DX_ERROR;/* The environment is inaccessible */
	/* We receive the reference to memory of the buffer of an exchange */
	/* call the Hardware.getBuffer(int chdev,int size) */
	jbyteArray array = (jbyteArray)java->CallStaticObjectMethod
															(
															device_class, 
															java_getBuffer,
															handle,
															count
															);
	/* We call the method of reading given from a stream of input in the buffer of an exchange */
	/* call the Hardware.readData(int chdev,byte[] array,int off,int size) */
	int readed = java->CallStaticIntMethod
										(
										device_class, 
										mid_readData, 
										handle, 
										array, 
										0, 
										count
										);
	if (readed != DX_ERROR) { /* The data are read out and are in the buffer */
		/* Copies the read out data Dialogic array => ptr 
			(res - how many is read out from a stream of input) */
		java->GetByteArrayRegion(array, 0, readed , (signed char*)pointer);
		context->seek_pos += readed;
	}
	return readed;
}

/*
 * to correct size of writed buffer (less), called from Dialogic's seek()
 */
static 
void truncateWriteBuffer(int handle,int offset)
{
	JNIEnv *java = dialogicVoiceChannel::getJNIEnv();
	if (java == NULL) return;
	java->CallStaticVoidMethod(device_class, mid_lessData, handle, offset); 
}
/*
 * call-back function, called when Dialogic seek to voice data
 * for example when need erase the terminated DTMF signal
 */
long dialogicVoiceChannel::seekData
								(
								int handle,	// handle to opened channel
								long offset,// offset of seek
								int whence	// direction of seek
								)
{
	Context *context = Context::getContext( handle );
	if (context == NULL) return DX_ERROR;//invalid handle

	switch( whence )
	{
		case SEEK_CUR://#define SEEK_CUR    1
		case SEEK_END://#define SEEK_END    2
			if (offset < 0) 
			{
				::truncateWriteBuffer(handle,offset);
			}
			context->seek_pos += offset; // update context's position
			break;
		case SEEK_SET://#define SEEK_SET    0
			context->seek_pos = offset;  // update context's position
			break;
	}
	return context->seek_pos; // returns current context position
}
