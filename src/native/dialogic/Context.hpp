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
#ifndef __CONTEXT // is we not parse this header-file
	#define __CONTEXT // to stop recursive compile

#define MAX_CONTEXT 128
#define MAX_TPT 10
#define MAX_NAME 20
#define MAX_DIGIT 20

#include <string.h>
#include <stdio.h>

#ifndef WIN32
	#include <unistd.h>
#else
	#include <io.h>
#endif

#include "dialogicLib.h"

#define DXX_TYPE 1
#define DTI_TYPE 2
#define MSI_TYPE 3
#define FAX_TYPE 4
//
// Class for realize context of opened resource
//
class Context
{
private:

	/*
	 * to free, unused context
	 */
	inline void free()
	{
		this->handle=	// to clear handle
		this->control=	// to clear control handle ( for stopch() calls )
		this->timeslot=	// to clear timeslot handle
		this->type =	// to clear resource type
		DX_ERROR;
		// disable disconnect tone detection
		this->detectDisconnectTone = FALSE;

		this->seek_pos = 0L;	// reset position of voice-data
		this->digitsCount= 0;	// clear the user input count
		this->incomingCall = FALSE;	// clear incoming call flag
		memset( this->name, 0, sizeof(this->name) );	// clear internal name of resource

		// to clear all Dialogic transfer structures
		memset( &xbp, 0, sizeof(DX_XPB) );
		memset( &iott, 0 ,sizeof(DX_IOTT) );
		memset( &features, 0, sizeof(FEATURE_TABLE) );
		memset( &devinfo, 0, sizeof(CT_DEVINFO) );
		memset( &fiott, 0, sizeof(DF_IOTT) );
		memset( &asciidata, 0, sizeof(DF_ASCIIDATA) );

		dx_clrtpt( &tpt[0], MAX_TPT );
		dx_clrcap(&cap);
	}
	/*
	 * is Context already used (have valid handle)
	 */
	inline unsigned char isUsed(){return this->handle > 0;}

	// Hook state
	volatile unsigned char hookState;
	// Flag, is incoming call present
	volatile unsigned char incomingCall;
	// Array of user input digits
	char digits[ MAX_DIGIT+1 ];
	// Number of digit in buffer
	volatile unsigned char digitsCount;
	// type of resource
	int type;
	// is detect disconnect tone needed
	unsigned short detectDisconnectTone;

public:
	// Internal name of resource
	char name[ MAX_NAME+1 ];
	// Handle to opened dialogic resource
	volatile int handle;
	// Handle to opened resource for control operation from other thread
	volatile int control;
	// Handle to timeslot
	volatile int timeslot;
	// current I/O data position (for the play/record)
	volatile long seek_pos;
	// Termination Parameter Table Entry Structure.
	DV_TPT tpt[ MAX_TPT ];
	// Transfer parameter block
	DX_XPB xbp;
	// I/O Transfer Table structure
	DX_IOTT iott;
	// buffer for store input digit
	DV_DIGIT digp;
	// structure for tone generation
	TN_GEN tngen;
	// Call Analysis structure
	DX_CAP cap;
	// features
	FEATURE_TABLE features;
	// device info
	CT_DEVINFO devinfo;
	// Fax I/O Transfer Table structure
	DF_IOTT fiott;
	/* 
	* The DF_ASCIIDATA structure - for use with transmission of ASCII data
	* A pointer to this structure may be passed in the DF_IOTT structure for
	* transmitting an ASCII file. Default values are assumed if no DF_ASCIIDATA
	* is specified.
	*/
	DF_ASCIIDATA asciidata;

// dynamic calls section
public:
	/*
	 * Context empty constructor
	 */
	Context(): 
			handle		( DX_ERROR	),
			control		( DX_ERROR	),
			timeslot	( DX_ERROR	),
			incomingCall( FALSE		),
			digitsCount	( 0			),
			hookState	( DX_ONHOOK ),
			type		( DX_ERROR	),
			seek_pos	( 0L		),
			detectDisconnectTone(FALSE)
	{
		memset( name,0,sizeof(name) );
		memset( &xbp, 0, sizeof(DX_XPB) );
		memset( &iott, 0 ,sizeof(DX_IOTT) );
		memset( &digp, 0, sizeof(DV_DIGIT) );
		memset( &tngen, 0, sizeof(TN_GEN) );
		memset( &features, 0, sizeof(FEATURE_TABLE) );
		memset( &devinfo, 0, sizeof(CT_DEVINFO) );
		memset( &fiott, 0, sizeof(DF_IOTT) );
		memset( &asciidata, 0, sizeof(DF_ASCIIDATA) );

		dx_clrtpt( &tpt[0], MAX_TPT );
		dx_clrcap(&cap);
	}

	///////// NAMES /////////////
	/*
	 * access to context name
	 */
	inline char *getName(){return this->name;}
	/*
	 * To update the Context's internal name
	 * Called, when maked new Context
	 */
	inline unsigned char setName(char *name)
	{
		if ( this->isUsed() ) return FALSE;// context used now! Disable change the internal name
		strncpy( this->name, name, MAX_NAME );// to update the internal name
		return TRUE;
	}

	/////////// TYPES ////////////
	/*
	 * To get access to Context's resource type
	 */
	int getType(){return this->type;}
	/*
	 * To update the Context's resource type
	 */
	inline unsigned char setType(int Type){
		switch ( Type )
		{
			case DXX_TYPE:
			case DTI_TYPE:
			case MSI_TYPE: 
			case FAX_TYPE:
				this->type = Type; 
				return TRUE;
			default: 
				return FALSE;
		}
	}

	///////////// INCOMING CALLS /////////////////
	/*
	 *  Detected incoming call for Context owner.
	 *  called when solving Dialogic event
	 */
	inline void callAlerted(){this->incomingCall = TRUE;}
	/*
	 *  Detected lost of incoming call for Context owner.
	 *  called when solving Dialogic event and when
	 *  checked incoming call outside (from Java)
	 */
	inline void clearIncomingCall(){this->incomingCall = FALSE;}
	/*
	 *  To check, is incoming call present
	 *  Called from scaning process
	 */
	inline int isIncomingCall()
	{
//printf("C++: request to incoming call for %d\n",this->handle);
		if ( this->incomingCall ) // check incoming call flag
		{
//printf("C++: request to incoming call, flag cleared for %d\n",this->handle);
			this->clearIncomingCall(); // clear incoming call flag
			return TRUE;// present
		}else return FALSE;// not present
	}

	////////////// HOOK STATE /////////////////////
	/*
	 *  To set new hookState for Context
	 *  called when solving Dialogic event
	 */
	inline void setHookState(unsigned char state)
	{
		if (state == DX_ONHOOK || state == DX_OFFHOOK) 
		{
			this->hookState=state;
		}
	}
	/*
	 * To get access to Context's hook state
	 */
	inline int getHookState(){return this->hookState;}

	////////// USER INPUT ////////////////
	/*
	 *  To store user input to internal buffer
	 *  called when solving Dialogic event
	 */
	inline void storeDigit(char digit)
	{
		if (this->digitsCount >= MAX_DIGIT) return;// no free space to store
		this->digits[ this->digitsCount++ ] = digit; // save symbol
	}
	/*
	 *  To get access to internal buffer of user input
	 *  Called from Java
	 */
	inline int receiveDigits(char *result)
	{
		int count = this->digitsCount;// count of entered symbols
		if (count > 0) // in buffer presents symbols
		{
			::memcpy( result, this->digits, count );// copy buffer to result
			this->digitsCount = 0;// clear buffer
		}
		return count;
	}
	/////////// DISCONNECT TONE DETECTION MANAGMENT /////////////
	/*
	 * is detect disconnect tone
	 */
	inline unsigned short isDetectDisconnectTone(){return this->detectDisconnectTone;}
	/*
	 * to enable detect disconnect tone
	 */
	inline void enableDetectDisconnectTone(){this->detectDisconnectTone = TRUE;}
	/*
	 * to disable detect disconnect tone
	 */
	inline void disableDetectDisconnectTone(){this->detectDisconnectTone = FALSE;}

// static calls section
public:
	// to init Context's internal array
	static void init(); 
	// access to context by handle
	static Context *getContext(int handle);
	// to make new entry of context for handle
	static Context *allocContext(int handle);
	// to destroy unused context
	static void freeContext(int handle){Context *context;
		if ((context=getContext(handle)) != NULL) context->free();
	}
};
#endif // #ifndef __CONTEXT // is we not parse this header-file

