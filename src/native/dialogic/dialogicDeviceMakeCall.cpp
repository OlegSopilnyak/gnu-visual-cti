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

#define CA_VOICE		"VOICE"
#define CA_FAX			"FAX"
#define CA_MODEM		"MODEM"
#define CA_AUTOANSWER	"AUTOANSWER"
#define CA_BUSY			"BUSY"
#define CA_NO_ANSWER	"NO ANSWER"
#define CA_NO_RINGBACK	"NO RINGBACK"
#define CA_NO_DIAL_TONE	"NO DIAL TONE"
#define CA_STOP			"TERMINATION"
#define CA_ERROR		"ERROR"

//######### process make call event
void dialogicDevice::processMakeCallEvent( dialogicEvent *event )
{
	int handle = event->getHandle();// get event dialogic handle
	int reasonID = ATDX_CPTERM( handle );// get Call Analize termination reason ID
	const char* reason = CA_ERROR; // default (Call Analize error)
	// to process reason ID
	switch ( reasonID )
	{
		case CR_CNCT:	// connected
			reason = ATDX_CONNTYPE( handle ) == CON_PAMD ?
				/* CA_AUTOANSWER */CA_VOICE : CA_VOICE;
			break;
		case CR_FAXTONE:	reason = CA_FAX; break;// fax tone detected
		case CR_BUSY:		reason = CA_BUSY; break;// busy signal detected
		case CR_NOANS:		reason = CA_NO_ANSWER; break;// no answer detected
		case CR_NORB:		reason = CA_NO_RINGBACK; break;// no ringback
		case CR_NODIALTONE:	reason = CA_NO_DIAL_TONE; break;// no dial tone
		case CR_CEPT:		reason = CA_NO_RINGBACK; break;// operator intercept
		case CR_STOPD:		reason = CA_STOP; break;// user stop ( dx_stopch(handle) )
		case CR_ERROR:		reason = CA_ERROR; break;// error
	}
	event->setTermReason( reasonID );// store reason ID
	event->setTermReason( reason );// store termination reason as string
}
