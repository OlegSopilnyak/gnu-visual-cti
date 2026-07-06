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
#include <stdio.h>
#include <string.h>

#include "dialogicDevice.hpp"
#include "dialogicVoiceChannel.hpp"
#include "dialogicFaxChannel.hpp"
#include "Context.hpp"

#include <stdlib.h>
#include <errno.h>
// storage for valid Dialogic resource names list
static char channels[1024] = {0};
// buffer for save the error message
static char errorMessage[200] = {0};

/*
 * to get access to valid resource names list
 */
char *dialogicDevice::getValidNames(){return channels;}
/*
 * to append valid name for names list storage
 */
static
void appendChannelName(char *name)
{
	strcat(channels,name); strcat(channels,"\n");
}
/*
 * to open Dialogic device by type and name
 */
static
int openDevice(char *name,int type)
{
	switch(type){
		case DXX_TYPE: return dx_open(name,0);
		case DTI_TYPE: return dt_open(name,0);
		case MSI_TYPE: return ms_open(name,0);
	}
	return DX_ERROR;
}
/*
 * to close device by handle and type
 */
static
void closeDevice(int handle, int type)
{
	switch(type){
		case DXX_TYPE: dx_close(handle); break;
		case DTI_TYPE: dt_close(handle); break;
		case MSI_TYPE: ms_close(handle); break;
	}
}
/*
 * to process channels set for any dialogic device
 */
static
void processDevice
				(
				int type,
				int device,
				char *format
				)
{
	char name[10];// temporary buffer for current resource name
	for (int i=1;;i++) 
	{
		sprintf(name,format,device,i);		// made the resource name by counter
		int handle = openDevice(name,type);	// try to open resource by maked name
		if (handle == DX_ERROR) break;		// resource name not valid, exit loop
		closeDevice(handle,type);			// to close opened device
		appendChannelName(name);			// to add maked name to valid names list
	}
}
/*
 * names for board type
 */
static
void checkDevices
			(
			int type,
			char *boardFormat,
			char *channelFormat
			)
{
	char name[10];// temporary buffer for name
	for (int board=1;;board++)
	{
		sprintf(name,boardFormat,board);	// to make device name
		int handle = openDevice(name,type);	// try to open device by name
		if (handle == DX_ERROR) break;		// device name not valid, exit loop
		closeDevice(handle,type);			// to close opened device
		processDevice(type,board,channelFormat);// to process device name for contained resources
	}
}
/*
 * To initialize (Check of availability) the Dialogic devices
 */
void dialogicDevice::Initialize()
{
	if ( ::strlen(channels) > 0) return;
	::checkDevices(DXX_TYPE,"dxxxB%d","dxxxB%dC%d");// analog
	::checkDevices(DTI_TYPE,"dtiB%d", "dtiB%dT%d" );// digital
	::checkDevices(MSI_TYPE,"msiB%d", "msiB%dC%d" );// conference

#ifdef WIN32 //Smile
    // Set to SR_STASYNC so another thread is not created by the SRL to
    // monitor events to pass to the handler.  We will use this thread
	// to monitor events, creating another thread internally is not
	// necessary.
	int mode = SR_STASYNC;
	if ( ::sr_setparm( SRL_DEVICE, SR_MODELTYPE, &mode ) == DX_ERROR ) 
	{
		::printf( "Unable to set to Polled Mode to SR_STASYNC" );
	}
#endif
	// setup device to poll mode, for asyn calls
#ifdef WIN32
	int SRL_Mode = SR_STASYNC | SR_POLLMODE;
#else
	int SRL_Mode = SR_POLLMODE;
#endif
	if( ::sr_setparm( SRL_DEVICE, SR_MODEID, &SRL_Mode ) == DX_ERROR )
	{
		::printf("Cannot set to SR_POLLMODE");
	}

	Context::init();// Context system initialization
	dialogicVoiceChannel::Initialize();
}

/////////////// EVENT PROCESSING ///////////
// Dialogic event map-object (single object for all events)
static dialogicEvent Event;// maked at program startup
/*
 * to get and process next Dialogic event
 */
dialogicEvent *dialogicDevice::getEvent()
{
	// wait Dialogic event (never expired)
	::sr_waitevt( -1 );
	// to prepare new event wrapper
	dialogicEvent *event = &Event; // make refer to static dialogicEvent object
	event->free();	// clear event fields
	event->setHandle ( ::sr_getevtdev()  );// store handle to event source
	event->setEventID( ::sr_getevttype() );// store event type
	// to process new event
	dialogicDevice::processEvent( event );
	return event;//return processed event for transfer to Java
}
/*
 * to process recieved Dialogic event
 */
void dialogicDevice::processEvent(dialogicEvent *event)
{
   /*
    * Switch according to the event received.
    */
	switch( event->getEventID() ) {
		case TDX_CST:				/* Call Status Transition */
			dialogicDevice::process_CST_Event( event );
			break;

		case TDX_PLAY:               /* Play Completed       */
		case TDX_RECORD:             /* Record Completed     */
			dialogicDevice::processVoiceEvent( event );
			break;
			
		case TDX_GETDIG:             /* Get Digits Completed */
			dialogicDevice::processGetDigitsEvent( event );
			break;
			
		case TDX_CALLP:				/* Make call complete */
			dialogicDevice::processMakeCallEvent( event );
			break;
			
		case TDX_PLAYTONE:			/* Play tone complete */
		case TDX_DIAL:				/* Dial without Call Analysis complete */
			event->setTermReason( 0 );// no errors
			event->setTermReason( "OK" );
			break;
			
		case TDX_SETHOOK:            /* Set-Hook Complete    */
			dialogicDevice::processHookEvent( event );
			break;

        case TFX_FAXRECV:  /* The document has been successfully received. */  
            printf("Received %ld pages at speed %ld, resln %ld,width %ld\n", 
						ATFX_PGXFER(event->getHandle()),
						ATFX_SPEED(event->getHandle()),
						ATFX_RESLN(event->getHandle()), 
						ATFX_WIDTH(event->getHandle())
						);
			{
				Context *context = Context::getContext( event->getHandle() );
				if (context != NULL) {
					close(context->fiott.io_fhandle);
					event->setTermReason( 0 );// reason
					event->setTermReason( CTERM_EOF );
				} else {// invalid source handle
					event->setTermReason( DX_ERROR );// reason - error
					event->setTermReason( "ERROR" ); // reason - error(text)
				}
			}
			break;

		case TFX_FAXSEND:	// Fax sending done
			{
				Context *context = Context::getContext( event->getHandle() );
				if (context != NULL) {
					close(context->fiott.io_fhandle);
					event->setTermReason( 0 );// reason
					event->setTermReason( CTERM_EOF );
				} else {// invalid source handle
					event->setTermReason( DX_ERROR );// reason - error
					event->setTermReason( "ERROR" ); // reason - error(text)
				}
			}
			break;

        case TFX_FAXERROR:/* Error during the fax session. */  
            printf("Phase E status %d\n", ATFX_ESTAT(event->getHandle()));  
			{
				int handle = event->getHandle();
				Context *context = Context::getContext( handle );
				if (context != NULL) {
					close(context->fiott.io_fhandle);
					dialogicFaxChannel::getLastError(handle, event);
				}
			}
			break;

		case DTEV_SIG:               /* DTI signalling event */
			dialogicDevice::process_DTI_Event( event );
			break;
			
		case TDX_ERROR:				// Error detected
			{int handle = event->getHandle();
			event->setTermReason( DX_ERROR );// reason - error event
			sprintf(
					::errorMessage,
					"error on channel %d [%s] OS says (%s) last state %d",
					handle, 
					ATDV_ERRMSGP(handle),
					strerror(errno),
					ATDX_STATE(handle)
					);
			event->setTermReason( ::errorMessage );// store translated error message
			}
			break;
			
		default:
		/*
		* Unexpected or Error Termination Event
		*/
			printf("C:> Unexpected or Error Termination Event %d for %d\n",event->getEventID(),event->getHandle());
			printf("C:> lasterror's ID %d error message [%s] system says [%s]\n",
			ATDV_LASTERR(event->getHandle()),ATDV_ERRMSGP(event->getHandle()),strerror(errno)
			);
	}
}
/*
 * to get call state
 */
inline int getCallState(int handle)
{
	return ATDX_HOOKST( handle );// get current hook state
}
/*
 * to process Set-Hook Complete  event
 */
void dialogicDevice::processHookEvent(dialogicEvent *event)
{
	int handle = event->getHandle();// get handle to event source
	Context *context = Context::getContext( handle );// get context by handle
	if (context != NULL) 
	{
		int state = ::getCallState( context->control );// get current call state
		context->setHookState( state );// store new hook state
		// save new state
		event->setTermReason( state );// reason
		event->setTermReason( state == DX_ONHOOK ? "ONHOOK":"OFFHOOK" );
	} else 
	{	// invalid source handle
		event->setTermReason( DX_ERROR );// reason - error
		event->setTermReason( "ERROR" ); // reason - error(text)
	}
}
