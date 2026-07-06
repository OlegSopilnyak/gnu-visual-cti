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
#include "dialogicLib.h"
#include "Context.hpp"
/*
 * Class realize Call Control Resource features
 */
class dialogicChannel 
{
public:
	// to enable some events generation
	static void enableEvents(int handle, const char *what);
	// to disable all event generation
	static void disableEvents(int handle);
	// to open Dialogic resource by name
	static int openChannel(const char *name);
	// to open Dialogic fax resource by name
	static int openFaxChannel(const char *name);
	// to close Dialogic resource by handle
	static void closeChannel(int handle);
	// to recieve last error
	static void getLastError( int handle, char *message );
	// To make default termination Table for long time operation
	static DV_TPT *defaultTerminationTable(DV_TPT *tpt, int handle);

	/// CALL CONTROL CALLS //////////////
	/*
	 *  To check, is incoming call, for handle, alerted
	 */
	static unsigned char isIncomingCall(int handle)
	{
		Context *context = Context::getContext( handle );
		return context == NULL ? FALSE : context->isIncomingCall();
	}
	/*
	 *  To answer, for handle, alerted call
	 */
	static int answerCall(int handle)
	{
		return dialogicChannel::setHookState(handle,DX_OFFHOOK);
	}
	/*
	 * To get access to caller ID
	 */
	static int getCallerID(int handle, char *number);
	/*
	 *  To drop, for handle, current call (disconnect)
	 */
	static int dropCall(int handle)
	{
		return dialogicChannel::setHookState(handle,DX_ONHOOK);
	}
	// to set new hook state
	static int setHookState(int handle, unsigned char state);
	// to make outgoing call
	static int makeCall(int handle, const char *number, int timeout);

	///// TO CANCEL CURRENT OPERATION //////
	// to stop activity
	static void stopActivity(int handle);
};
