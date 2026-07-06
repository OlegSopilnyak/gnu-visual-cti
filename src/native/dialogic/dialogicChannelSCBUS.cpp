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
#include "dialogicChannelSCBUS.hpp"
/*
 * To get SC Bus time slot for making connection
 */
long dialogicChannelSCBUS::getTxTimeslot(int handle,int handleType)
{
	if ( !dialogicChannelSCBUS::isSCBUS(handle) ) return FALSE;// handle not support SCBUS

	SC_TSINFO sc_tsinfo;// SCBus timeslot info
	long time_slot;		// timeslot handle
	// refer to the received timeslot handle store in structure
	sc_tsinfo.sc_tsarrayp = &time_slot;
	sc_tsinfo.sc_numts = 1;// one timeslot
	// to get transmitting timeslot of the resource
	switch( handleType ) 
	{
		case SC_LSI: // LSI(analog call resources)
			if (ag_getxmitslot(handle, &sc_tsinfo) == DX_ERROR) return DX_ERROR;
			break;
		case SC_VOX:// VOX (voice,signals) resource
			if (dx_getxmitslot(handle, &sc_tsinfo) == DX_ERROR) return DX_ERROR;
			break;
		case SC_FAX:// FAX resource
			if (dx_getxmitslot(handle, &sc_tsinfo) == DX_ERROR) return DX_ERROR;
			break;
		case SC_DTI:// (DTI) digital board call resource (E1/T1/ISDN)
			if (dt_getxmitslot(handle, &sc_tsinfo) == DX_ERROR) return DX_ERROR;
			break;
		case SC_MSI:// Supports conferencing applications resource
			if (ms_getxmitslot(handle, &sc_tsinfo) == DX_ERROR) return DX_ERROR;
			break;
		default:
			return DX_ERROR;
	}
	return time_slot;// valid timeslot for resource
}
/*
 * to connect handle to timeslot
 */
unsigned char dialogicChannelSCBUS::listen(int handle, int handleType, long timeSlot)
{
	if ( !dialogicChannelSCBUS::isSCBUS(handle) ) return FALSE;// handle not support SCBUS

	SC_TSINFO sc_tsinfo;// SCBus timeslot info
	long scts = timeSlot;// timeslot for route
	sc_tsinfo.sc_numts = 1;// one timeslot
	sc_tsinfo.sc_tsarrayp = &scts;// store refer to disconecting timeslot
	// to connect handle to timeslot
	switch( handleType ) 
	{
		case SC_LSI: // LSI(analog call resources)
			if (ag_listen(handle, &sc_tsinfo) == DX_ERROR) return FALSE;
			break;
		case SC_VOX:// VOX (voice,signals) resource
			if (dx_listen(handle, &sc_tsinfo) == DX_ERROR) return FALSE;
			break;
		case SC_FAX:// FAX resource
			if (dx_listen(handle, &sc_tsinfo) == DX_ERROR) return FALSE;
			break;
		case SC_DTI:// (DTI) digital board call resource (E1/T1/ISDN)
			if (dt_listen(handle, &sc_tsinfo) == DX_ERROR) return FALSE;
			break;
		case SC_MSI:// Supports conferencing applications resource
			if (ms_listen(handle, &sc_tsinfo) == DX_ERROR) return FALSE;
			break;
		default:
			return FALSE;
	}
	return TRUE;// success
}
/*
 * To disconnect connected handle from timeslot
 */
unsigned char dialogicChannelSCBUS::unlisten( int handle, int handleType )
{
	if ( !dialogicChannelSCBUS::isSCBUS(handle) ) return FALSE;// handle not support SCBUS

	// to disconnect handle
	switch( handleType )
	{
	case SC_LSI:// LSI(analog call resources)
		if (ag_unlisten(handle) == DX_ERROR) return FALSE;
		break;
	case SC_VOX:// VOX (voice,signals) resource
		if (dx_unlisten(handle) == DX_ERROR) return FALSE;
		break;
	case SC_FAX:// FAX resource
		if (fx_unlisten(handle) == DX_ERROR) return FALSE;
		break;
	case SC_DTI:// (DTI) digital board call resource (E1/T1/ISDN)
		if (dt_unlisten(handle) == DX_ERROR) return FALSE;
		break;
	case SC_MSI:// Supports conferencing applications resource
		if (ms_unlisten(handle) == DX_ERROR) return FALSE;
		break;
	default:
		return FALSE;
	}
	return TRUE;
}
