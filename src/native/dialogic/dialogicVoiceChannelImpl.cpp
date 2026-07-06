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
#include "dialogicVoiceChannel.hpp"
#include "Context.hpp"

/*
 * to initialize call-back functions
 */
void dialogicVoiceChannel::Initialize()
{
	DX_UIO uioblk;
	uioblk.u_write	= dialogicVoiceChannel::writeData;
	uioblk.u_read	= dialogicVoiceChannel::readData;
	uioblk.u_seek	= dialogicVoiceChannel::seekData;

	int reason = dx_setuio(uioblk);
//	printf("UIO setup is [%d]\n",reason);
}

//////////FUNCTIONS FOR MAKE TERMINATION ENTRIES ////////////////
/*
 * setup maxtime termination entry
 */
inline
void maxtime_Termination(DV_TPT *tpt, int maxtime)
{
	// entry: detect transfer maxtime
	tpt->tp_type	= IO_EOT;
	tpt->tp_termno	= DX_MAXTIME;
	tpt->tp_length	= (unsigned short) maxtime*10;
	tpt->tp_flags	= TF_MAXTIME;
}
/*
 * setup DTMF termination
 */
inline
void dtmf_Termination(DV_TPT *tpt, int mask)
{
	// entry: detect DTMF mask
	tpt->tp_type	= IO_EOT;//IO_CONT;
	tpt->tp_termno	= DX_DIGMASK;
	tpt->tp_length	= (unsigned short) mask;
	tpt->tp_flags	= TF_DIGMASK;
}
/*
 * setup maxtime of silence before terminate record
 */
inline
void maxsilence_Termination(DV_TPT *tpt, int maxsilence)
{
	// entry: detect maxtime for silence
	tpt->tp_type	= IO_EOT;
	tpt->tp_termno	= DX_MAXSIL;
	tpt->tp_length	= (unsigned short) maxsilence*10;
	tpt->tp_flags	= TF_MAXSIL|TF_SETINIT;
	tpt->tp_data	= (unsigned short) maxsilence*10;
}
inline
void toneDisconnect_Termination(DV_TPT *tpt){
	// entry: detect user-defined disconnect tone
	tpt->tp_type	= IO_EOT;
	tpt->tp_termno	= DX_TONE;
	tpt->tp_length	= TID_DISTONE;
	tpt->tp_flags	= TF_TONE; //TF_LEVEL|TF_USE;
	tpt->tp_data	= DX_TONEON;
}
///////////// FUNCTIONS FOR RECORD/PLAY
/*
 * to make the terminations table for play Sound
 */
inline 
void makePlayTerminationTable
	(
	int handle,
	DV_TPT *tpt,// refer to terminations table
	int mask,	// DTMF mask
	int maxtime // maxtime to play (sec)
	)
{
	// to make default terminations table entries
	DV_TPT *entry = dialogicChannel::defaultTerminationTable( tpt, handle );
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// detect transfer maxtime
	maxtime_Termination( entry, maxtime );
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// detect DTMF
	dtmf_Termination( entry, mask );
}
/*
 * to make the terminations table for record Voice
 */
inline
void makeRecordTerminationTable
	(
	int handle,
	DV_TPT *tpt,	// refer to terminations table
	int mask,		// DTMF mask
	int maxsilence, // maxtime for silence
	int maxtime		// record maxtime (sec)
	)
{
	// to make default terminations table entries
	DV_TPT *entry = dialogicChannel::defaultTerminationTable( tpt, handle );
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// detect silence
	maxsilence_Termination( entry, maxsilence );
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// detect transfer maxtime
	maxtime_Termination( entry, maxtime );
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// detect DTMF
	dtmf_Termination( entry, mask );
}
/*
 * to fill device codec structure 
 */
inline
void makeCodecStructure
			(
			DX_XPB *xbp,
			int codec
			)
{
	if((codec & MD_PCM) != 0)
	{	// PCM data format
		xbp->wBitsPerSample = 8;
		if((codec & LINEAR)  != 0) xbp->wDataFormat = DATA_FORMAT_PCM;
		if((codec & PM_ULAW) != 0) xbp->wDataFormat = DATA_FORMAT_MULAW;
		if((codec & PM_ALAW) != 0) xbp->wDataFormat = DATA_FORMAT_ALAW;
	} else 
	{	// ADPCM data format
		xbp->wBitsPerSample = 4;
		xbp->wDataFormat = DATA_FORMAT_DIALOGIC_ADPCM;
	}
	if((codec & PM_SR6)  != 0) xbp->nSamplesPerSec = DRT_6KHZ;
	if((codec & PM_SR8)  != 0) xbp->nSamplesPerSec = DRT_8KHZ;
	if((codec & PM_SR11) != 0) xbp->nSamplesPerSec = DRT_11KHZ;
	xbp->wFileFormat = FILE_FORMAT_VOX;
}
/*
 * to fill structure for Voice I/O
 */
inline
void fill_DX_IOTT_struct
			(
			DX_IOTT *iott,
			int handle
			)
{
	iott->io_type = IO_DEV|IO_UIO|IO_EOT;
	iott->io_fhandle = handle;
	iott->io_length = -1;
	iott->io_offset = 0;
}
/*
 * to translate mask to termination maskID
 */
static
int makeTermmask(const char *Mask)
{
int mask=0, shift;
int length = strlen(Mask);
char digit;
	for (int i=0;i < length;i++)
	{
		digit = Mask[i];
		switch(digit)
		{
			case '1': case '2':	case '3':
			case '4': case '5':	case '6':
			case '7': case '8':	case '9':
				shift = digit - '0';
				mask |= 1 << shift;
				break;
            case '*': 
				mask |= 0x0800; 
				break;
            case '#': 
				mask |= 0x1000; 
				break;
            case '0': 
				mask |= 0x0400;
				break;
		}
	}
	return mask;
}
/*
 * to start sound playback
 */
int dialogicVoiceChannel::startPlay	
							(
							int handle,			// handle to openend channel
							const char *mask,	// string contains termination symbols set
							int time,			// max time to playback
							const char *codec	// string contain codec descriptor
							)
{
	Context *context = Context::getContext( handle);// get context by handle
	if (context == NULL) return DX_ERROR;// invalid handle

	int termmask = ::makeTermmask( mask );
	int codecID = dialogicVoiceChannel::getCodecID( codec );
	if (codecID  == DX_ERROR) return DX_ERROR;//invalid codec string

	::makePlayTerminationTable( handle, context->tpt, termmask, time );
	::makeCodecStructure	( &context->xbp, codecID );
	::fill_DX_IOTT_struct	( &context->iott, handle );

	context->seek_pos = 0;/* seek to begin of data */
	/* to start playback with user-defined callback function */
	int state= 
		::dx_playiottdata
			(
			handle, 
			&context->iott, 
			context->tpt, 
			&context->xbp, 
			EV_ASYNC
			);
	if ( state == DX_ERROR ) printf("C++:> The start play error [%s] Code is [%d]\n",ATDV_ERRMSGP(handle),ATDV_LASTERR( handle ) );
	return state;
}
/*
 * to start voice record
 */
int dialogicVoiceChannel::startRecord
							(
							int handle,			// handle to opened channel
							const char *mask,	// string contains termination symbols set
							int silence,		// max time to silence detection (termination condition)
							int time,			// max time to playback
							const char *codec	// string contain codec descriptor
							)
{
	Context *context = Context::getContext( handle);// get context by handle
	if (context == NULL) return DX_ERROR;// invalid handle

	int termmask = ::makeTermmask( mask );
	int codecID = dialogicVoiceChannel::getCodecID( codec );
	if (codecID == DX_ERROR) return DX_ERROR;// invalid codec

//	::dx_clrdigbuf( handle );// clear digits buffer

	::makeRecordTerminationTable( handle, context->tpt, termmask, silence, time );
	::makeCodecStructure		( &context->xbp, codecID );
	::fill_DX_IOTT_struct		( &context->iott, handle );

	context->seek_pos = 0;// set pointer to begin of data

	/* to start record with user-defined callback function */
	int state=
		::dx_reciottdata
			(
			handle, 
			&context->iott, 
			context->tpt, 
			&context->xbp, 
			EV_ASYNC
			);
	if ( state == DX_ERROR ) printf("C++:> The start record error [%s] Code is [%d]\n",ATDV_ERRMSGP(handle),ATDV_LASTERR( handle ) );
	return state;
}
