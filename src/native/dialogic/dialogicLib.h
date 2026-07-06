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
/**
 ** Dialogic Header Files
 **/
#ifndef WIN32

	#define LPDWORD long
	#define PDWORD long
	#define DWORD long
	
#endif
#include <srllib.h>
#include <dxxxlib.h>
#include <faxlib.h>
#include <dtilib.h>
#include <cclib.h>
#include <msilib.h>

#include "sctools.h"

#define DX_ERROR -1

#include "135.h"

#define TIME_LCOFF 5

#define CT_DFD41D 0x01

#define TID_FAXTONE1 121
#define TID_FAXTONE2 122
#define TID_RINGBACK 123
#define TID_BUSY 124
#define TID_LDTONE 125
#define TID_DISTONE 126
#define TID_DIALTONE 127

#define TERM_NORMAL	 0
#define TERM_EOD	 0x200
#define TERM_LCOFF	 0x08
#define TERM_SIGMASK 0x40
#define TERM_MAXTIME 0x20
#define TERM_SILENCE 0x02
#define TERM_STOP	 0x100
#define TERM_FAXTONE 0x2000

/** The reason of end of a call (nothing) */
#define CTERM_OK           "OK"
/** The reason of end of a call (timeout) */
#define CTERM_TIMEOUT      "TIMEOUT"
/** The reason of end of a call (accepted incoming call) */
#define CTERM_RINGS        "RINGS"
/** The reason of end of a call (invalid data format) */
#define CTERM_FORMAT       "FORMAT"
/** The reason of end of a call (detected end of data) */
#define CTERM_EOF          "EOF"
/** The reason of end of a call (detected DTMF) */
#define CTERM_DTMF         "DTMF"
/** The reason of end of a call (detected disconnect) */
#define CTERM_DISCONNECT   "DISCONNECT"
/** 
* The reason of end of a call 
* (during wait operation, port used other port for make outgoing call) 
*/
#define CTERM_CONNECT      "CONNECT"
/** The reason of end of a call (silence detected during record) */
#define CTERM_SILENCE      "SILENCE"
/** The reason of end of a call (reserved) */
#define CTERM_MESSAGE      "MESSAGE"
/** The reason of end of a call (cti-call terminated) */
#define CTERM_TERMINATED   "TERMINATION"


#define FALSE 0
#define TRUE 1

#define CT_DFMSI          0x04

