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
#include <stdlib.h>
#include "dialogicChannel.hpp"
#include "dialogicSignalChannel.hpp"

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
 * setup max symbols count for retrieve
 */
inline
void retrieve_Termination(DV_TPT *tpt, int count)
{
	// entry: accept count symbols
	tpt->tp_type	= IO_CONT;
	tpt->tp_termno	= DX_MAXDTMF;
	tpt->tp_length	= (unsigned short)count;
	tpt->tp_flags	= TF_MAXDTMF;
}
/*
 * to make the termination table for dx_getdig() call 
 */
inline
void makeGetDigitTerminationTable
		(
		int handle,
		DV_TPT *tpt,
		int timeout
		)
{
	// to make default terminations table entries
	DV_TPT *entry = dialogicChannel::defaultTerminationTable( tpt, handle );
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// accept one symbol 
	retrieve_Termination( entry, 1);
	entry->tp_type	= IO_CONT;// to continue table
	entry++; // next entry

	// detect transfer maxtime
	maxtime_Termination( entry, timeout );
}
/*
 * to start signal retrieve (one symbol)
 */
int dialogicSignalChannel::retrieveSignal(int handle, int timeout)
{
	Context *context = Context::getContext( handle );// get context by handle
	if (context == NULL) return DX_ERROR;// invalid handle

	::makeGetDigitTerminationTable( handle, context->tpt, timeout );// make TPT
	::dx_setdigtyp(handle, DM_DTMF | DM_DPD);// set digits type (DTMF tone and Pulse )
	// to start execution
	return ::dx_getdig( handle, context->tpt, &context->digp, EV_ASYNC );
}
/*
 * to start playing tone
 */
int dialogicSignalChannel::playTone
								(
								int handle,
								int freq1,
								int freq2,
								int duration
								)
{
	Context *context = Context::getContext( handle );// get context by handle
	if (context == NULL) return DX_ERROR;//invalid handle

	// make TN_GEN structure
#ifdef WIN32
	context->tngen.tg_dflag = freq2 == 0 ? TN_SINGLE:TN_DUAL;
	context->tngen.tg_freq1 = (unsigned short)freq1;
	context->tngen.tg_freq2 = (unsigned short)freq2;
	context->tngen.tg_ampl1 = TGEN_AMP;
	context->tngen.tg_ampl2 = TGEN_AMP;
	context->tngen.tg_dur = (unsigned short)duration/10;
#else
	::dx_bldtngen
		( 
		&context->tngen, 
		(unsigned short)freq1, 
		(unsigned short)freq2, 
		TGEN_AMP, 
		TGEN_AMP, 
		(unsigned short)duration/10 
		);
#endif
	dialogicChannel::defaultTerminationTable( context->tpt , handle);
	/*
	// make termination reasons
	context->tpt[0].tp_type = IO_EOT;
	context->tpt[0].tp_termno = DX_LCOFF;// disconnect
	context->tpt[0].tp_length = TIME_LCOFF;
	context->tpt[0].tp_flags = TF_LEVEL|TF_USE;
	*/
	// start playing tone
	return dx_playtone( handle, &context->tngen, context->tpt, EV_ASYNC );
}
/*
 ******************************************
 * structure for resolve signal from string
 ******************************************
*/
typedef struct {
	int ToneID;
	int freq1;
	int fq1dev; 
	int freq2;
	int fq2dev;
	int OnTime;
	int OnTimeDev;
	int OffTime;
	int OffTimeDev;
	int repcnt;
} Signal;
/*
 * get next digit from string
 */
static 
const char *get
			(
			const char *string,
			int *digit
			)
{
	if (string == NULL || strlen(string) == 0) return string;// nothing to do
	// skip spaces
	while(*string == ' ') string++;
	// try find the comma after spaces
	char *comma = strchr(string,',');
	if (comma == NULL) 
	{
		*digit = atoi(string); // try to make number
		string = NULL;
	} else {
		comma[0] = 0; // fix coma position
		*digit = atoi(string); // try to make number
		string = comma+1; // shift pointer to next token
	}
	return string;
}
/*
 * to resolve signal by descriptor (example "257,500,200,500,200,55,40,55,40,4")
 */
inline
void resolveSignal
		(
		Signal *signal,
		const char *descriptor
		)
{
	const char *current = descriptor;
	current = get(current,&signal->ToneID);
	current = get(current,&signal->freq1);
	current = get(current,&signal->fq1dev);
	current = get(current,&signal->freq2);
	current = get(current,&signal->fq2dev);
	current = get(current,&signal->OnTime);
	current = get(current,&signal->OnTimeDev);
	current = get(current,&signal->OffTime);
	current = get(current,&signal->OffTimeDev);
	current = get(current,&signal->repcnt);
}
/*
 * to setting up signal to handle
 */
inline
void storeSignal( Signal *signal )
{
	/* To update frequencies of a signal and their deviation */
	dx_chgfreq
		(
		signal->ToneID,
		signal->freq1,
		signal->fq1dev,
		signal->freq2,
		signal->fq2dev
		);
	// By necessity, ontime/offtime, deviation of a signal and repeat count
	if( signal->ToneID > TID_DIAL_XTRA || signal->ToneID == TID_DISTONE)
	{
		// Change time periods for signal
		dx_chgdur
				(
				signal->ToneID,
				signal->OnTime,
				signal->OnTimeDev,
				signal->OffTime,
				signal->OffTimeDev
				);
		// Quantity of repetitions of a signal
		dx_chgrepcnt
				(
				signal->ToneID,
				signal->repcnt
				);
	}
}
/*
 * to add telephony tone to opend handle
 */
int dialogicSignalChannel::addTelephonyTone
								(
								int handle,
								const char *description
								)
{
	Context *context = Context::getContext( handle );// get context by handle
	if (context == NULL) return DX_ERROR;// invalid handle

	Signal signal;// empty signal
	::memset( &signal, 0, sizeof(Signal) );// clear signals fields
	::resolveSignal(&signal, description);// fill signal fields from description

	// redefine statndart DISCONNECT SIGNAL to user (May be, it is vain)
	if (signal.ToneID == TID_DISCONNECT) {
		signal.ToneID = TID_DISTONE;
	}

	::storeSignal( &signal );// to store signal to Dialogic global table
	return TRUE;
}
