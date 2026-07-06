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
#include "Context.hpp"

#define DIAL1_DESCRIPTION		"250,400,125,400,125,0,0,0,0,0"
#define DIAL2_DESCRIPTION		"251,400,125,400,125,0,0,0,0,0"
#define BUSY_DESCRIPTION		"253,500,200,0,0,55,40,55,40,4"
#define RINGBACK_DESCRIPTION	"254,450,150,0,0,150,100,550,400,0"
#define DISCONNECT_DESCRIPTION	"257,500,200,500,200,55,40,55,40,4"

/*
 * class manage send signal, retrieve signal, play tone features
 */
class dialogicSignalChannel
{
public:
	/*
	 * to make predefined tones table
	 */
	static
	unsigned char makeUserTones(int handle)
	{
//printf("SIGNAL:>make user defined disconnect tone.\n");
		::dx_deltones( handle );// clear old user-defined tones
		// setting user-defined DISCONNECT tone 
		::dx_blddtcad(TID_DISTONE,900,700,0,0,90,70,90,70,2);
		if( ::dx_addtone(handle, (unsigned char)NULL, 0) == DX_ERROR ) return FALSE;
		::dx_distone(handle, TID_DISTONE, DM_TONEOFF);// disable tone off detection
		::dx_distone(handle, TID_DISTONE, DM_TONEON); // disable tone on detection

/*		// setting user-defined FAX-tone 
		dx_bldstcad(TID_FAXTONE1,2150,150,10,0,0,0,0);
		if( dx_addtone(handle, (unsigned char)NULL, 0) == DX_ERROR ) return FALSE;
		dx_distone(handle, TID_FAXTONE1, DM_TONEOFF);

		// setting user-defined FAX-tone 
		dx_bldstcad(TID_FAXTONE2, 1100,50,10,0,0,0,0);
		if( dx_addtone(handle, (unsigned char)NULL, 0) == DX_ERROR ) return FALSE;
		dx_distone(handle, TID_FAXTONE2, DM_TONEOFF);
*/
		return TRUE;
	}
	/*
	 * to start send DTMF digits
	 */
	static
	int sendSignal(int handle,const char *digits)
	{
		Context *context = Context::getContext(handle);// get context by handle
		if (context == NULL) return DX_ERROR;// invalid handle
		return dx_dial(handle, digits, NULL, EV_ASYNC);// start dialing
	}
	/*
	 * to start retrieve one user input symbol
	 */
	static
	int retrieveSignal(int handle, int timeout);
	/*
	 * to get user input from buffer
	 */
	static
	int getSignalsBuffer(int handle, char *buffer)
	{
		Context *context = Context::getContext(handle);// get context by handle
		if (context == NULL) return DX_ERROR;// invalid handle
		context->receiveDigits( buffer );
		return TRUE;
	}
	/*
	 * to start playing tone
	 */
	static int playTone
			(
			int handle, 
			int freq1, 
			int freq2, 
			int duration // duration in mSec
			);
	/*
	 * to clear tones table
	 */
	static 
	int clearTones(int handle)
	{
		Context *context = Context::getContext(handle);// get context by handle
		if (context == NULL) return DX_ERROR;// invalid handle
		::dx_deltones( handle );// delete all tones
		// to add user tones
		return dialogicSignalChannel::makeUserTones(handle) == TRUE ? 0:DX_ERROR;
		
	}
	/*
	 * to add tone for handle
	 */
	static
	int addTelephonyTone(int handle,const char *description);
	/*
	 * to fix tones update
	 */
	static
	int storeTones(int handle)
	{
		Context *context = Context::getContext(handle);// get context by handle
		if (context == NULL) return DX_ERROR;// invalid handle
		return dx_initcallp( handle ); // fix updates
	}
	/*
	 * to enable/disable detect tone
	 */
	static
	int setToneDetect(int handle,const char *tone,unsigned char enable)
	{
		Context *context = Context::getContext(handle);
		if (context == NULL) return DX_ERROR;// invalid handle
//printf("for handle %d tone %s detection is %s\n",handle,tone,enable?"enabled":"disabled");
		if ( enable ) context->enableDetectDisconnectTone();
		else		  context->disableDetectDisconnectTone();
		return 0;
	}
};

#define TGEN_AMP  -20
