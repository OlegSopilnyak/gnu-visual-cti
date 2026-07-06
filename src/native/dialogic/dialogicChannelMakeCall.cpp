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

/*
 * To adjust structure for Positive Voice Detection
 */
static 
void setup_DX_CAP_structure
				(
				DX_CAP *cap,
				int timeout
				)
{
	dx_clrcap( cap );// clear structure

	cap->ca_intflg		= DX_PAMDOPTEN;
	cap->ca_noanswer	= timeout*100;
	cap->ca_cnosig		= timeout*100;
//	cap->ca_pamd_spdval = 0;
	cap->ca_pamd_qtemp	= PAMD_QUAL1TMP;
}
/*
 * to make analog call
 */
static
int makeAnalog
		(
		Context *context,
		int handle,
		const char *number,
		int timeout
		)
{
	// tuning DX_CAP structure for set timeout
	setup_DX_CAP_structure( &context->cap, timeout );
	// make call with Call Analize
	return dx_dial(handle,number,&context->cap,DX_CALLP|EV_ASYNC);
}

/*
 * to make digital call
 */
static 
int makeDigital
		(
		Context *context,
		int handle,
		const char *number,
		int timeout
		)
{
	return DX_ERROR;
}

/*
 * to make conference call
 */
static 
int makeConference
		(
		Context *context,
		int handle,
		const char *number,
		int timeout
		)
{
	return DX_ERROR;
}
/*
 * to make outgoing call
 */
int dialogicChannel::makeCall
						(
						int handle,			// handle to opened port
						const char *number,	// call target
						int timeout			// timeout for making
						)
{
	Context *context = Context::getContext(handle);// get Context by handle
	if (context == NULL) return DX_ERROR;// invalid handle
	// to process make call by port type
	switch( context->getType() )
	{
		case DXX_TYPE:
			return makeAnalog	(context,handle,number,timeout);
		case DTI_TYPE:
			return makeDigital	(context,handle,number,timeout);
		case MSI_TYPE:
			return makeConference(context,handle,number,timeout);
	}
	return DX_ERROR;
}
