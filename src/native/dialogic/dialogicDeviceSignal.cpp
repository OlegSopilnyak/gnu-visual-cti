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
#include "dialogicDevice.hpp"
#include "Context.hpp"

/*
 * to resolve termination reason by mask
 */
static
char getReason(int handle)
{
	Context *context = Context::getContext(handle);
	if (context == NULL) return 'z';// error (not valid handle)
	// get termination reason mask
	int term = ATDX_TERMMSK(handle);
	// to process mask
	if( (term & TM_ERROR)		!= 0 )	return 'z';/* error */
	else if( (term & TM_LCOFF)	!= 0 )	return 'x';/* disconnect */
	else if( (term & TM_TONE)	!= 0 ) 
	{
		int toneID = ATDX_TONEID(handle);
		switch( toneID )
		{
		case		TID_DISTONE  :		return 'x';/* disconnect */
		case		TID_FAXTONE1 :		return 'e';/* fax */
		case		TID_FAXTONE2 :		return 'f';/* fax */
		}
	}
	else if( (term & TM_USRSTOP)!= 0 )	return 'y';// user stop
	else if( (term & TM_MAXTIME)!= 0 )	return 't';/* timeout */
	else if( (term & TM_MAXDTMF)!= 0 )
	{
		char symbol = context->digp.dg_value[0];
		context->storeDigit( symbol );// store entered simbol
		return symbol;/* symbol entered */
	}
	return 'z';/* error */
}
/*
 * to process the get digit termination reason
 */
void dialogicDevice::processGetDigitsEvent(dialogicEvent *event)
{
	int reasonID = getReason( event->getHandle() );// get reason ID
	char *reason = CTERM_DTMF;// default reason (entered simbol)

	switch ( reasonID )
	{
		case 'z':// error when resolve reason mask
			reason = "ERROR";			break;

		case 'x':// disconnect detected
			reason = CTERM_DISCONNECT;
			break;
		case 'e':// fax signal detected
			reason = "FAX";				break;

		case 't':// timeout
			reason = CTERM_TIMEOUT;		break;

		case 'y':// user stop
			reason = CTERM_TERMINATED;	break;
	}
	event->setTermReason( reasonID );// store reason ID
	event->setTermReason( reason );// store resolved reason string
}
