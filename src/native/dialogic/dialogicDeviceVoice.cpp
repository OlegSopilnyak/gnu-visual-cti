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
/*
 * To translate the termination reason mask to internal consts
 */
static
int rangeMask(int handle, int mask) 
{
	if		( (mask & TM_ERROR)	!= 0 )			return DX_ERROR;
	else if ( (mask & TM_LCOFF)	!= 0 )			return TERM_LCOFF;
	else if ( (mask & TM_TONE)	!= 0 ) 
	{
		int toneID = ATDX_TONEID(handle);
		switch( toneID )
		{
			case	TID_DISTONE  :			return TERM_LCOFF;
			case	TID_FAXTONE1 :			return TERM_FAXTONE;
			case	TID_FAXTONE2 :			return TERM_FAXTONE;
		}
	}
	else if ( (mask & TM_USRSTOP)	!= 0 )			return TERM_STOP;
	else if ( (mask & TM_DIGIT)		!= 0 )			return TERM_SIGMASK;
	else if ( (mask & TM_MAXDTMF)	!= 0 )			return TERM_SIGMASK;
	else if ( (mask & TM_NORMTERM)	!= 0 )			return TERM_NORMAL;
	else if ( (mask & TM_EOD)		!= 0 )			return TERM_EOD;
	else if ( (mask & TM_MAXTIME)	!= 0 )			return TERM_MAXTIME;
	else if ( (mask & TM_MAXSIL)	!= 0 )			return TERM_SILENCE;
	return DX_ERROR;
}
/*
 * to process Voice I/O termination event
 */
void dialogicDevice::processVoiceEvent(dialogicEvent *event)
{
	int handle = event->getHandle();
	int reason = rangeMask( handle, ATDX_TERMMSK(handle) );// get termintation reason ID
	event->setTermReason( reason );// store reason ID
	// make string reason
	switch ( reason )
	{
		case TERM_NORMAL:
		case TERM_EOD:
			// operation finished normaly or end of data
			event->setTermReason( CTERM_EOF );
			break;

		case TERM_LCOFF:	// disconnect detected
			event->setTermReason( CTERM_DISCONNECT );
			break;

		case TERM_SIGMASK:	// terminated by the DTMF symbol (defined in mask)
			event->setTermReason( CTERM_DTMF );
			break;

		case TERM_MAXTIME:	// terminate by timeout (for record)
			event->setTermReason( CTERM_TIMEOUT );
			break;

		case TERM_SILENCE:	// finished by silence detection (for record)
			event->setTermReason( CTERM_SILENCE );
			break;

		case TERM_STOP:		// user stop operation (dx_stopch)
			event->setTermReason( CTERM_TERMINATED );
			break;

		case TERM_FAXTONE:	// fax tone detected
			event->setTermReason( "FAX" );
			break;

		default:			// unknown reason
			event->setTermReason( "ERROR" );
			break;
	}
}
