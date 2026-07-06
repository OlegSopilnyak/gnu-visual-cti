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
#include "dialogicChannelInfo.hpp"

/*
 * class for realize SCBUS functions
 */
class dialogicChannelSCBUS
{
private:
	/*
	 * check, is handle valid and support SCBUS
	 */
	static
	unsigned char isSCBUS(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return dialogicChannelInfo::isSupportSCBUS(handle);// is SCBUS supported?
	}
public:
	// get access to the resource handle timeslot
	static long getTxTimeslot(int handle,int handleType);
	// to connect handle to timeslot
	static unsigned char listen(int handle, int handleType, long timeSlot);
	// to disconnect handle from timeslot
	static unsigned char unlisten(int handle, int handleType);
	/*
	 * To create a full duplex connection between two SCBus devices.
	 */
	static int full_route
						(
						int handle1,
						int handleType1,
						int handle2,
						int handleType2
						)
	{
		if ( isSCBUS(handle1) && isSCBUS(handle2) )
		{
			return
				::nr_scroute
					(
					handle1,
					handleType1,
					handle2,
					handleType2,
					SC_FULLDUP
					);
		}else return DX_ERROR;
	}
	/*
	 * To break the full duplex connection between two SCBus devices
	 */
	static int full_unroute
						(
						int handle1,
						int handleType1,
						int handle2,
						int handleType2
						)
	{
		if ( isSCBUS(handle1) && isSCBUS(handle2) )
		{
			return
				::nr_scunroute
					(
					handle1,
					handleType1,
					handle2,
					handleType2,
					SC_FULLDUP
					);
		}else return DX_ERROR;
	}
};
