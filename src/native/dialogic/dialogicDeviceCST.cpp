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
#include "Context.hpp"
#include "dialogicDevice.hpp"

/*
 * to process Dialogic Call Status Transition event
 */
void dialogicDevice::process_CST_Event(dialogicEvent *event)
{
	DX_CST *cstp = (DX_CST *) sr_getevtdatap();// get CST structure from system
	int handle =  event->getHandle();
	Context *context = Context::getContext( handle );// get Context for handle
	// to process CST event	
	switch ( cstp->cst_event ) 
	{
		case DE_RINGS: // Rings Received (Incoming Call)
			if (context != NULL) 
			{
				context->callAlerted(); // setup Context's incoming call flag
//printf("C++: Registered incoming call in %d\n",handle);
				event->setTermReason( CTERM_RINGS );
				event->setTermReason( DM_RINGS );
			} else {
				event->setTermReason( "ERROR" );
				event->setTermReason( DX_ERROR );
			}
			break;
      
		case DE_DIGITS:    //received a digit 
			printf("received a digit\n");
			break;
		case DE_DIGOFF:    //digit tone-off event 
			printf("digit tone-off event\n");
			break;
		case DE_LCOFF:    //loop current off event 
			if (context != NULL) 
			{
				event->setTermReason( CTERM_DISCONNECT );
				event->setTermReason( DM_LCOF );
			} else {
				event->setTermReason( "ERROR" );
				event->setTermReason( DX_ERROR );
			}
			printf("loop current off event\n");
			break;
		case DE_LCON:    //loop current on event 
			printf("loop current on event\n");
			break;
		case DE_LCREV:    //loop current reversal event 
			printf("loop current reversal event\n");
			break;
		case DE_RNGOFF:    //caller hang up (incoming call is dropped before being accepted) event
			if (context != NULL) {
				context->clearIncomingCall();// clear Context's incoming call flag
				event->setTermReason( CTERM_RINGS );
				event->setTermReason( DM_RNGOFF );
			} else {
				event->setTermReason( "ERROR" );
				event->setTermReason( DX_ERROR );
			}
			break;
		case DE_SILOFF:    //silence off event 
			printf("silence off event\n");
			break;
		case DE_SILON:    //silence on event 
			printf("silence on event\n");
			break;
		case DE_TONEOFF:    //tone off event 
			printf("tone off event\n");
			break;
		case DE_TONEON:    //tone on event 
//			printf("tone %d on event\n", cstp->cst_data);
			if (context != NULL) {
				if (cstp->cst_data == TID_DISTONE)
				{
					event->setTermReason( CTERM_DISCONNECT );
					event->setTermReason( DM_LCOF );
				} else {
					event->setTermReason( "USER TONE" );
					event->setTermReason( cstp->cst_data );
				}
			} else {
				event->setTermReason( "USER TONE DETECTED" );
				event->setTermReason( DX_ERROR );
			}
			break;
		case DE_WINK:    //received a wink 
			printf("received a wink\n");
			break;
		default:// mistake CST event
		  printf("C:> CST unknown event %d, data = %d ignored.\n",cstp->cst_event, cstp->cst_data);
	     break;
   }
}
