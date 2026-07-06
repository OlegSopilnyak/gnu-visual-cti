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
#include "dialogicChannel.hpp"
#include "dialogicSignalChannel.hpp"
#include "dialogicChannelInfo.hpp"


#include <string.h>

#define DXX_TYPE 1
#define DTI_TYPE 2
#define MSI_TYPE 3

static const char *DXX_PREFIX = "dxxx";
static const char *DTI_PREFIX = "dti";
static const char *MSI_PREFIX = "msi";

#include <errno.h>

/*
 * to check dialogic event type
 * is need to coninue processing event
 */
static int checkEvt()
{
	int ID = ::sr_getevttype();
	if (ID == TDX_CST)
	{
		DX_CST *cstp = (DX_CST *) sr_getevtdatap();// get CST structure from system
		return cstp->cst_event == DE_LCOFF;

	}
	return 0;
}
/*
 * dialogic event handler for control handle
 */
#ifdef WIN32
	static long nothing(unsigned long parm){return 0;}
#else
	static long nothing(void *par) {return 0;}
#endif

/*
 * Check, is string starts with prefix
 */
inline
unsigned char startsWith
				(
				const char *string,
				const char *prefix
				)
{
	return strncmp( string, prefix, strlen(prefix) ) == 0;
}
/*
 * To translate name of resource to type
 */
inline
int typeByName	(const char *name)
{
	if ( startsWith(name,DXX_PREFIX) ) return DXX_TYPE;
	if ( startsWith(name,DTI_PREFIX) ) return DTI_TYPE;
	if ( startsWith(name,MSI_PREFIX) ) return MSI_TYPE;
	return -1;
}
/*
 * to open dialogic resource
 */
inline
int openDialogicResource
				(
				const char *name,
				int type
				)
{
	switch ( type )
	{
		case DXX_TYPE: return dx_open(name,0);
		case DTI_TYPE: return dt_open((char *)name,0);
		case MSI_TYPE: return ms_open((char *)name,0);
		case FAX_TYPE: return fx_open(name,0);
	}
	return DX_ERROR;// mistake
}
/*
 * to close resource by context
 */
inline
int closeDialogicResource(Context *context)
{
	if (context == NULL) return -3;// invalid handle to close
	int result;
	// close resource by type
	switch( context->getType() )
	{
		case DXX_TYPE: // dxxxBxCx
//			result = sr_dishdlr( context->control, EV_ANYEVT, nothing );
//			printf("C:>disable handler for handle %d result = %d\n",context->control,result);
			result = dx_close( context->handle );
//			printf("C:>close handle %d result = %d\n",context->handle,result);
			result = dx_close( context->control );
//			printf("C:>close ctrl handle %d result = %d\n",context->control,result);
//			if ( dx_close( context->handle ) == DX_ERROR) return -2;
//			return dx_close( context->control);
			return result;
			break;
		case DTI_TYPE: // dtiBxTx
			dt_close( context->handle );
			break;
		case MSI_TYPE: // msiBxCx
			ms_close( context->handle );
			break;
		case FAX_TYPE: // Fax channel
//			sr_dishdlr( context->control, EV_ANYEVT, nothing );
			fx_close( context->handle );
			fx_close( context->control);
			break;
	}
	return DX_ERROR;
}
/*
 * to close resource by type immediate (mistake)
 */
inline
void closeResourceImmediate(int handle, int type)
{
	if ( handle == DX_ERROR ) return;
        printf("C:>alarm for %d, the device will close immediate :(\n",handle);
	switch ( type ) // type depended close
	{
		case DXX_TYPE: dx_close(handle);break;
		case DTI_TYPE: dt_close(handle);break;
		case MSI_TYPE: ms_close(handle);break;
		case FAX_TYPE: fx_close(handle);break;
	}
}
/*
 * to fill resource features structure
 */
static
int fillFeatures
				(
				int handle,
				FEATURE_TABLE *features,
				int type
				)
{
	if (type != DXX_TYPE && type != FAX_TYPE) return DX_ERROR;
#ifdef WIN32
	return dx_getfeaturelist(handle, features);
#else
	CTEX_DEVINFO info;
	int result = dx_getctexinfo( handle, &info);
	if (result != DX_ERROR)	memcpy(features,&info.ctex_feature_table,sizeof(FEATURE_TABLE));
	return result;
#endif
}
/*
 * to fill CT-DEVINFO structure
 */
static
int fillDevinfo
				(
				int handle,
				CT_DEVINFO *devinfo,
				int type
				)
{
	switch ( type ) // type depended close
	{
		case FAX_TYPE:
		case DXX_TYPE: return dx_getctinfo(handle, devinfo);
		case DTI_TYPE: return dt_getctinfo(handle, devinfo);
		case MSI_TYPE: return ms_getctinfo(handle, devinfo);
	}
	return DX_ERROR;
}
/*
 * to fill Context for DXX_TYPE
 */
static
void dxxxFillOpenedContext
				(
				Context *context,
				const char *name,
				int type
				)
{
	unsigned short parmval;// the parameter of the device
	int state = ATDX_HOOKST( context->handle );//to get current hook state
	if (state == AT_FAILURE) return;// hookstate invalid

	// suspend SRL event processing
	sr_hold();
	// open control handle
	context->control = openDialogicResource( name, type );

	/* Enable the caller ID functionality */
	parmval = DX_CALLIDENABLE;
	dx_setparm(context->handle, DXCH_CALLID, (void *) &parmval);
	/******************************************************************
	* Set the number of rings required for a RING event to permit
	* receipt of the caller ID information.  In the US, caller ID
	* information is transmitted between the first and second rings
	******************************************************************/
	parmval = 2;   /* 2 in the US */
	dx_setparm(context->handle, DXCH_RINGCNT, &parmval);
	// wait 1 ring, then generate alert event
	// dx_setrings( context->handle, 2 );
	// disable events from control
	dx_setevtmsk( context->control, 0 );

	// enable incoming call alert events
	dx_setevtmsk( context->handle, DM_RINGS );
	// install user-defined tones
	int uTones = dialogicSignalChannel::makeUserTones( context->handle );
//printf("FILL: user tones loaded [%s]\n",uTones == TRUE ?"yes":"no");

	// check current hook state, hangs up, if need
	if (state == DX_OFFHOOK)
	{	// off hook, set to onhook (hangs up)
		dx_sethook(context->handle,DX_ONHOOK,EV_ASYNC);
	}else {
		context->setHookState( DX_ONHOOK );
	}
	// resume SRL event procesing
	sr_release();
}
/*
 * to fill Context for FAX_TYPE
 */
static
void faxFillOpenedContext
					(
					Context *context,
					const char *name,
					int type
					)
{
	// open control handle
	context->control = openDialogicResource(name,type);
	// set handler for control handler
	// nothing to do, remove event for handle
	/*
	if( sr_enbhdlr( context->control, EV_ANYEVT, nothing ) == -1 )
	{	// mistake when remove handler for control handle
		printf(
			"Error: could not enable handler for fax control handle of %s\n" ,
			name,
			ATDV_ERRMSGP(context->control)
			);
	}
	*/
}
/*
 * to fill Context for DTI_TYPE
 */
static
void dtiFillOpenedContext
					(
					Context *context,
					const char *name,
					int type
					)
{
}
/*
 * to fill Context for MSI_TYPE
 */
static void msiFillOpenedContext(Context *context,const char *name, int type)
{
}
/*
 * to fill resource's Context fields
 */
static
void fillOpenedContext
				(
				Context *context,
				const char *name,
				int type
				)
{	// to prepare and fill resource features Context
	switch (type)
	{
		case DXX_TYPE:// for dxxxBxCx
			dxxxFillOpenedContext(context,name,type);
			break;
		case FAX_TYPE:// for dxxxBxCx
			faxFillOpenedContext(context,name,type);
			break;
		case DTI_TYPE:// for dtiBxTx
			dtiFillOpenedContext(context,name,type);
			break;
		case MSI_TYPE:// for msiBxCx
			msiFillOpenedContext(context,name,type);
			break;
	}
	// public information about handle
	context->setName( (char *)name );
	context->setType( type );
	// save features information about handle
	memset( &context->features, 0, sizeof(FEATURE_TABLE) );
	memset( &context->devinfo, 0, sizeof(CT_DEVINFO) );
	// fill fetures list and devinfo for channel
	fillFeatures( context->handle, &context->features, type );
	fillDevinfo	( context->handle, &context->devinfo, type );
}
/*
 * To open dialogic resource by name
 */
int dialogicChannel::openChannel(const char *name)
{
	int type = ::typeByName( name );// get type of resource by name
	int handle = ::openDialogicResource( name, type );// try to open resource by name
	// try make context for opened resource hsndle
	Context *context = Context::allocContext( handle );
	if (context == NULL)
	{	// no more free contexts or invalid handle (name)
		::closeResourceImmediate( handle, type );
		return DX_ERROR;// error open resource by name
	}
//printf("C++: opened channel %s on handle %d\n",name,context->handle);
	// to fill handle context
	::fillOpenedContext( context, name, type);
	return handle;// valid handle to resource by name
}
/*
 * To open Dialogic FAX resource
 */
int dialogicChannel::openFaxChannel(const char *name)
{
	int type = FAX_TYPE;
	int handle = ::openDialogicResource( name, type );// try open by name
	// try make context for opened resource hsndle
	Context *context = Context::allocContext( handle );
	if (context == NULL) {// no more free contexts or invalid handle (name)
		::closeResourceImmediate( handle, type );
		return DX_ERROR;// error open resource by name
	}
	::fillOpenedContext( context, name, type);
	return handle;
}
/*
 * To close handle
 */
void dialogicChannel::closeChannel(int handle)
{
	// close resource, using context
	::closeDialogicResource( Context::getContext( handle ) );
	Context::freeContext( handle );// free context
}
/*
 * To get access to last Dialogic error message
 */
void dialogicChannel::getLastError(int handle, char *message)
{
	// copying to external char[] buffer (message)
	::strcpy(
		message,
		Context::getContext(handle) != NULL ?
											::ATDV_ERRMSGP(handle):
											"Context for handle not found!?"
			);
}
/* get DXX caller ID */
static
int dxxxGetCallerID( int handle, unsigned char *number ){
	return ::dx_gtcallid(handle, number);
}
/* get DTI caller ID */
static
int dtiGetCallerID( int handle, unsigned char *number ){
	return DX_ERROR;
}
/* get MSI caller ID */
static
int msiGetCallerID( int handle, unsigned char *number ){
	return DX_ERROR;
}
/*
 * to get access to callerID
 */
int dialogicChannel::getCallerID(int handle,char *number)
{
	Context *context = Context::getContext(handle);
	unsigned char *buffer = (unsigned char *)number;
	if ( context == NULL ) return DX_ERROR;
	switch( context->getType() )
	{
		case DXX_TYPE: // LSI
			return dxxxGetCallerID( handle, buffer );
		case DTI_TYPE: // DTI
			return dtiGetCallerID( handle, buffer );
		case MSI_TYPE: // MSI
			return msiGetCallerID( handle, buffer );
	}
	return DX_ERROR;
}
/* dxxx set hook state */
static
int dxxxSetHook
		(
		int handle,
		unsigned char state
		)
{
	return dx_sethook(handle,state,EV_ASYNC);// request to change hookState
}
/* dti set hook state */
static
int dtiSetHook
		(
		int handle,
		unsigned char state
		)
{
	return DX_ERROR;
}
/* dti set hook state */
static
int msiSetHook
		(
		int handle,
		unsigned char state
		)
{
	return DX_ERROR;
}
/*
 * to make request to setting up new state of hook (call)
 */
// set new status of Call Control Resource
// by type of resource
inline
int setHook
		(
		int handle,
		int type,
		unsigned char state
		)
{
	switch (type)
	{
		case DXX_TYPE: // LSI
			return dxxxSetHook( handle, state );
		case DTI_TYPE: // DTI
			return dtiSetHook( handle, state );
		case MSI_TYPE: // MSI
			return msiSetHook( handle, state );
	}
	return DX_ERROR;
}
/*
 * to stop all VOX activity
 */
static
void stopVoxActivity(int handle,int ctrlHandle)
{
	// to stop for LSI | VOX
	switch( ATDX_STATE(handle) )
	{
		case CS_PLAY:
		case CS_RECD:
		case CS_DIAL:
		case CS_CALL:
			::dx_stopch( ctrlHandle, EV_ASYNC) ;// stop VOX activity using control handle
			break;
	}
	while ( ATDX_STATE( handle ) != CS_IDLE );// wait while IO active
}
/*
 * to stop all FAX activity
 */
static
void stopFaxActivity(int handle,int ctrlHandle)
{
	// to stop for FAX
	switch( ATFX_STATE(handle) )
	{
		case CS_SENDFAX:
		case CS_RECVFAX:
		case CS_FAXIO:
			::fx_stopch(ctrlHandle, EV_ASYNC);// stop FAX activity using control handle
			break;
	}
	while ( ATFX_STATE( handle ) != CS_IDLE );// wait while FAX active
}
/*
 * to start to change hook state
 */
//####### to change call resource state
int dialogicChannel::setHookState
							(
							int handle,
							unsigned char state
							)
{
	Context *context = Context::getContext(handle);// get access to hanlde Context
	if (context == NULL || (state != DX_ONHOOK && state != DX_OFFHOOK)) return DX_ERROR;
	::stopVoxActivity(handle, context->control);
	return ::setHook( handle, context->getType(), state );// make request to setup new hook state
}
/*
 * To cancel activity (cancel execution) in handle
 */
//######## to stop channel activity
void dialogicChannel::stopActivity(int handle)
{
	Context *context = Context::getContext( handle );
	if (context == NULL) return;
	// try to stop FAX device
	if (context->getType() == FAX_TYPE)
	{
		::stopFaxActivity(handle,context->control);
	}
	// try to stop VOX device
	if (context->getType() == DXX_TYPE)
	{
		::stopVoxActivity(handle,context->control);
	}
}
/*
 * To disable events generation
 */
void dialogicChannel::disableEvents(int handle)
{
	if ( Context::getContext(handle) == NULL ) return;// invalid handle
//printf("C++: disabled all events generation\n");
	::dx_distone(handle, TID_DISTONE, DM_TONEON);
//	::dx_setevtmsk( handle, 0 );// new events mask for LSI, disable all events generation
}
/*
 * to enable some events generation
 */
void dialogicChannel::enableEvents(int handle, const char *what)
{
	Context *context = Context::getContext(handle);
	if ( context == NULL || what == NULL) return;// invalid parameters set
	// to process what parameter
	if ( ::strcmp(what, CTERM_RINGS) == 0)
	{	// new events mask for LSI, enable rings events generation
//printf("C++: enabled events for %s on device %d\n",what,handle);
		::dx_distone(handle, TID_DISTONE, DM_TONEON);
//		::dx_setevtmsk( handle, DM_RINGS );
//printf("C:>Now handle %d will wait ring\n",handle);
	}else
	if ( ::strcmp(what, CTERM_DISCONNECT) == 0)
	{
//printf("C++: enabled events for %s on device %d\n",what,handle);
		if (context->isDetectDisconnectTone()) ::dx_enbtone(handle, TID_DISTONE, DM_TONEON);
//		dialogicChannel::disableEvents( handle );
		// new events mask for LSI, enable disconnect events generation
//		::dx_setevtmsk( handle, DM_LCOFF /*|DM_LCON | DM_TONEON*/ );
//printf("C:>Now handle %d will wait disconnect\n",handle);
	}
//	::dx_setevtmsk( handle, DM_RINGS );
}
inline
void lcoff_Termination(DV_TPT *tpt){
	// entry: detect loop current off disconnect
	tpt->tp_type	= IO_EOT;
	tpt->tp_termno	= DX_LCOFF;
	tpt->tp_length	= TIME_LCOFF;
	tpt->tp_flags	= TF_LCOFF;//TF_LEVEL|TF_USE;
}
inline
void toneDisconnect_Termination(DV_TPT *tpt){
	// entry: detect user-defined disconnect tone
	tpt->tp_type	= IO_EOT;
	tpt->tp_termno	= DX_TONE;
	tpt->tp_length	= TID_DISTONE;
	tpt->tp_flags	= TF_TONE; //TF_LEVEL|TF_USE;
	tpt->tp_data	= DX_TONEON;
}
/*
 * To make default termination Table for long time operation
 */
DV_TPT *dialogicChannel::defaultTerminationTable(DV_TPT *tpt, int handle)
{
	Context *context = Context::getContext(handle);
	if (context == NULL) return tpt;// nothing to do
	// detect loop current off disconnect
	::lcoff_Termination( tpt );
	if ( context->isDetectDisconnectTone() )
	{	// add disconnect tone detection termination
		tpt->tp_type = IO_CONT; // continue table
		tpt++;// to next entry
		// detect user-defined disconnect tone
		::toneDisconnect_Termination( tpt );
	}
	return tpt;// return pointer to last entry of TPT
}
