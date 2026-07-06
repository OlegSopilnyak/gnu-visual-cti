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
#include "Context.hpp"
/*
Array of availabled Contexts entries
*/
static Context bodies[ MAX_CONTEXT ];

///////////////// OPERATIONS with Contexts list
// ########### to init the internal array of Contexts 
void Context::init()
{
//    printf("\nInitializing Context system for %d entries...", MAX_CONTEXT / 2);
    for(int i=0;i < MAX_CONTEXT;i++) bodies[i].free();
//    printf("Done.\n");
}
//######## access to context by handle
Context *Context::getContext(int handle) 
{
	// find context entry for handle
	for(int i=0;i < MAX_CONTEXT;i++)
	{
		if ( bodies[i].handle == handle	) return &bodies[i];// return refer to finded entry
	}
	return NULL;// not found entry by handle
}

//########## to make new entry of context for handle
Context *Context::allocContext(int handle)
{
	if (handle <= 0) return NULL;// invalid handle
	// iterate all entries for find free entry
	for(int i=0;i < MAX_CONTEXT;i++)
	{
		if (bodies[i].handle == DX_ERROR)
		{	// this is free entry
			bodies[i].handle = handle;// store handle to free entry (lock entry)
			return &bodies[i];// return refer to allocated entry
		}
	}
	return NULL;// no free entry
}

