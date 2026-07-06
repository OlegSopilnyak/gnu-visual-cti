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
#ifndef __DIALOGICEVENT
	#define __DIALOGICEVENT
#include "dialogicLib.h"

/*
 * class for support Dialogic event model
 */
class dialogicEvent 
{
private:
	int handle;			// Where is event occurred
	int eventID;		// Event type
	int termReason;		// The reason of termination of operation (ID)
	char *cTermReason;	// The reason of termination of operation (string)

public:
	/*
	 * Contructor of a new Dialogic event map-object
	 */
	dialogicEvent(): handle(DX_ERROR),eventID(DX_ERROR),termReason(DX_ERROR),cTermReason(NULL) {}

	//////// HANDLE property
	/*
	 * to setting up handle
	 */
	void setHandle(int value){this->handle = value;}
	/*
	 * to get access to handle
	 */
	int getHandle(){return this->handle;}

	///////// EVENT ID property
	/*
	 * to setting up event ID
	 */
	void setEventID(int value){this->eventID = value;}
	/*
	 * to get access to event ID
	 */
	int getEventID(){return this->eventID;}

	/////////// TERMINATION REASON property
	/*
	 * to setting up reason ID
	 */
	void setTermReason(int value){this->termReason = value;}
	/*
	 * to setting up reason string
	 */
	void setTermReason(char *value){this->cTermReason = value;}
	void setTermReason(const char *value){this->cTermReason = (char *)value;}
	/*
	 * to get access to termination reason ID
	 */
	int getTermReason(){return this->termReason;}
	/*
	 * to get access to termination reason string
	 */
	char *getCtermReason(){return this->cTermReason;}

	/*
	 * to free processed event
	 */
	void free()
	{
		this->handle=this->eventID=this->termReason = DX_ERROR;
		this->cTermReason = NULL;
	}
};
#endif
