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
#ifndef __INFO__
	#define __INFO__

#include "Context.hpp"

/*
 * Class for retrieve resource information, using handle
 */
class dialogicChannelInfo 
{
public:
	/*
	 * Check, is resource support the SCBUS operations (route,unroute)
	 */
	static
	unsigned char isSupportSCBUS(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return context->devinfo.ct_busmode == CT_BMSCBUS;
	}
	/*
	// is shared resource
	static
	unsigned char canShared(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return context->devinfo.ct_devmode != 0;
	}
	*/
	/*
	 * Check, is resource support the VOX operations (play,record,playtone,dial,getdigit)
	 */
	static
	unsigned char isResource_VOX(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return 
			context->features.ft_play	!= 0 ||
			context->features.ft_record != 0 ||
			context->features.ft_tone	!= 0;
	}
	/*
	 * Check, is resource support the DSP operations (playtone,dial,getdigit)
	 */
	static
	unsigned char isResource_DSP(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return context->features.ft_tone != 0;
	}
	/*
	 * Check, is resource support the LSI operations (wait_ring,call_analize)
	 */
	static
	unsigned char isResource_LSI(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return 
			(
			/*context->devinfo.ct_devfamily == CT_DFD41E ||*/	// analog or voice channel of a D/41ESC or VFX/40ESC board
			context->devinfo.ct_nettype   == CT_NTANALOG	// analog and voice devices on board are handling call processing
			) 
			||
			(context->devinfo.ct_devfamily  == CT_DFSPAN && // analog channel of a D/160SC-LS board, 
														// a voice channel of a D/240SC, D/320SC, D/240SC-T1, D/300SC-E1 or D/160SC-LS board,
														// or a digital channel of a D/240SC-T1 or D/300SC-E1 board
			context->devinfo.ct_devmode == CT_DMNETWORK	// analog channel available to process calls from the telephone network
			);
	}
	/*
	 * Check, is resource support the FAX operations (send,receive,faxio)
	 */
	static
	unsigned char isResource_FAX(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return context->features.ft_fax != 0; 
		//bitmask FT_FAX || FT_VFX40 || FT_VFX40E || FT_VFX40E_PLUS
	}
	/*
	 * Check, is resource support the DTI operations (T1,E1 signals)
	 */
	static
	unsigned char isResource_DTI(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return 
			context->devinfo.ct_nettype == CT_NTT1 ||	// D/240SC-T1 T-1 digital network interface 
			context->devinfo.ct_nettype == CT_NTE1	;	// D/300SC-E1 E-1 digital network interface 
	}
	/*
	 * Check, is resource support the MSI operations (conferencing)
	 */
	static
	unsigned char isResource_MSI(int handle)
	{
		Context *context = Context::getContext(handle); // get context by handle
		if (context == NULL) return FALSE;// invalid handle
		return context->devinfo.ct_devfamily  == CT_DFMSI; // a station on an MSI board
	}
};
#endif
