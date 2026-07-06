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
#include "dialogicFaxChannel.hpp"

#include<fcntl.h>
#include <stdlib.h>
#include <errno.h>

/*
 * To initialize the new fax state
 */
int dialogicFaxChannel::initState(int handle, int state)
{
	if ( !dialogicFaxChannel::isFAX(handle) ) return DX_ERROR;

	int rtn	  = 15;
	int rxcod = DF_MMR;
	int txcod = DF_MMR|DF_ECM;
	int retry = DF_RETRY1;
	int tagch = TF_MINTAGS;

	::fx_setparm( handle, FC_RTN,		(void *)&rtn	);
	::fx_setparm( handle, FC_RXCODING,	(void *)&rxcod	);
	::fx_setparm( handle, FC_RETRYCNT,	(void *)&retry	);
	::fx_setparm( handle, FC_TFTAGCHECK,(void *)&tagch	);
	::fx_setparm( handle, FC_TXCODING,	(void *)&txcod	);

	return ::fx_initstat(handle, state);
}
/*
 * to start receive fax
 */
int dialogicFaxChannel::startRecieve
							(
							int handle,
							const char *file,
							int issvrq
							)
{
	if ( !dialogicFaxChannel::isFAX(handle) ) return DX_ERROR;
	// tuning receive flags mask
	int flags = DF_ACCEPT_VRQ | DF_POLL | EV_ASYNC;
	if( issvrq ) flags = flags | DF_ISSUE_VRQ;
	
	return ::fx_rcvfax(handle, file, flags);// try start fax receiving
}
/*
 * to setting up the fax document header
 */
int dialogicFaxChannel::setHeader(int handle, const char *header)
{
	if ( !dialogicFaxChannel::isFAX(handle) ) return DX_ERROR;
	// tuning header parameter
	int cod = DF_HDRFMT2|DF_HDRBOLD;
	::fx_setparm(handle, FC_HDRATTRIB, (void *)&cod);
	return ::fx_setparm(handle, FC_HDRUSER2, (void *)header);
}
/*
 * to start send fax like tiff-file
 */
int dialogicFaxChannel::startSendAsTIFF
							(
							int handle,				// handle to fax resource
							const char *faxname,	// document file name
							unsigned char resHi,	// is high resolution for document
							unsigned char issvrq,	// is call user after transmition
							int firstpg,			// starts transmition from page
							int pgcount				// how many pages to transmition (default all)
							)
{
	Context *context = Context::getContext(handle);// get context by handle
	if (context == NULL) return DX_ERROR;// invalid handle

	DF_IOTT *iott = &context->fiott;// get structure body from context
	// tuning transfer flags mask
	int flags = DF_ACCEPT_VRQ | EV_ASYNC;
	if( !resHi )  flags = flags | DF_TXRESLO;
	if( issvrq )  flags = flags | DF_ISSUE_VRQ;
	
	// open the file for readonly, using open call
#ifdef WIN32
	iott->io_fhandle = ::open(faxname, O_RDONLY|O_BINARY);
#else
	iott->io_fhandle = ::open(faxname, O_RDONLY);
#endif
	// check open result
	if (iott->io_fhandle == -1)    return DX_ERROR;// can't open file

    // Set up the DF_IOTT structure 
	iott->io_type	= IO_DEV|IO_EOT;
	iott->io_firstpg = firstpg;
	iott->io_pgcount = pgcount;
	iott->io_datatype = DF_TIFF;
	iott->io_phdcont = DFC_AUTO;
	iott->io_bufferp = NULL;
	// starting a fax sending
	return ::fx_sendfax(handle, iott, flags);
}
/*
 * to start send as text
 */
int dialogicFaxChannel::startSendAsText
								(
								int handle,				// handle to fax resource
								const char *faxname,	// document file name
								unsigned char resHi,	// is high resolution for document
								unsigned char issvrq	// is call user after transmition
								)
{
	Context *context = Context::getContext(handle);// get context by handle
	if (context == NULL) return DX_ERROR;// invalid handle

	DF_IOTT *iott = &context->fiott; // get structure body from context
	DF_ASCIIDATA *asciidata = &context->asciidata;// get structure body from context
	// tuning transfer flags mask
	int flags = DF_ACCEPT_VRQ | EV_ASYNC;
	if(!resHi)  flags = flags | DF_TXRESLO;
	if(issvrq)  flags = flags | DF_ISSUE_VRQ;

	// open the file for readonly, using open call
#ifdef WIN32
	iott->io_fhandle = ::open(faxname, O_RDONLY|O_BINARY);
#else
	iott->io_fhandle = ::open(faxname, O_RDONLY);
#endif
	// check open result
	if (iott->io_fhandle == -1)    return DX_ERROR;

	// Set up the DF_IOTT structure 
	::fx_setiott(iott,iott->io_fhandle,DF_ASCII,DFC_AUTO);

	iott->io_type |= IO_EOT;
	// tuning ascii structure 
	asciidata->pagepad = DF_NOPAD;
	asciidata->units = DF_UNITS_IN10;
	asciidata->leftmargin = 3;
	asciidata->rightmargin = 3;
	asciidata->font = DF_FONT_0;
	asciidata->linespace = DF_SINGLESPACE;
	asciidata->tabstops = 3;
	asciidata->topmargin = 3;
	asciidata->botmargin = 3;
	asciidata->pagelength = 110;
	// setup ascii definition to io srtucture
	iott->io_datap = (void *)asciidata;
	// starting a fax sending as text
	return ::fx_sendfax(handle, iott, flags);
}
/*
 * To understand fax disconnect reason (ATDV_LASTERR == EFX_DISCONNECT)
 */
static
int getFaxDisconnectedReason(int state)
{
	switch( state )
	{
		/* General status values returned to receiver or transmitter */

//		case EFX_BUSYCHN     :  /* Request to start Fax while channel busy */
//		case EFX_OPINTFAIL   :  /* Operator intervention failed */  

		case EFX_ABORTCMD    :  /* Stop Fax command received */
			return FX_REMOTE_STOP;

		case EFX_CHIPNORESP  :  /* Fax modem not responding */ 
			return FX_TIMEOUT;

		case EFX_CEDTONE     :  /* Remote CED tone is longer than 5 secs */ 
			return FX_NOCOMPATIBLE;

		case EFX_HDLCCARR    :  /* Excessive HDLC carrier */ 
			return FX_COMM_ERR;

		/* Status values returned to transmitter */
/* ? */	case EFX_T1EXPTX	 :  /* Timer T1 expired waiting for message */  
		case EFX_ECMRNRTX    :  /* Timer T5 expired, receiver not ready */
			return FX_TIMEOUT;

		case EFX_NOWIDTHTX   :  /* Remote cannot receive at specified width */
		case EFX_NOFINERECTX :  /* Remote cannot receive fine resln documents */
		case EFX_RXCOMP      :  /* Remote site is not receive compatible */  
			return FX_NOCOMPATIBLE;

		case EFX_INVALMMRTX  :    /* Invalid input MMR data */
			return FX_FORMAT_ERR;

		case EFX_BADPGTX     :  /* DCN response after sending page */  
		case EFX_GOTDCNTX    :  /* Got DCN while waiting for DIS */  
			return FX_DISCONNECT;

		case EFX_NODISTX     :  /* Got other than DIS while waiting for DIS */  
		case EFX_BADDCSTX    :  /* Bad response to DCS, training */  
		case EFX_PHBDEADTX   :  /* No response to DCS, training or TCF */  
		case EFX_NOISETX     :  /* Too much noise training at 2400 bps */  
		case EFX_PHDDEADTX   :  /* No response after sending page */  
		case EFX_INVALRSPTX  :  /* No valid response after sending page */  
		case EFX_COMMERRTX   :  /* Transmit communication error */
		case EFX_ECMPHDTX    :  /* Invalid ECM response from receiver */
		case EFX_NXTCMDTX    :  /* Timeout waiting for next send page cmd */  
			return FX_COMM_ERR;
		
		/* Status values returned to receiver */

		case EFX_T1EXPRX	 :  /* Timer T1 expired waiting for message */  
			return FX_TIMEOUT;

		case EFX_TXCOMP      :  /* Remote site is not transmit compatible */  
			return FX_NOCOMPATIBLE;

		case EFX_WHYDCNRX	 :  /* Unexpected DCN while waiting for DCS/DIS */  
		case EFX_DCNDATARX	 :  /* Unexpected DCN while wtng for Fax data */  
		case EFX_DCNFAXRX    :  /* Unexpected DCN while wtng for EOM/EOP/MPS */
		case EFX_DCNPHDRX    :  /* Unexpected DCN after EOM/MPS sequence */  
		case EFX_DCNNORTNRX  :  /* DCN after requested retransmission */  
		case EFX_DCNRRDRX    :  /* Unexpected DCN after RR/RNR sequence */
			return FX_DISCONNECT;

/* ? */	case EFX_T2EXPDCNRX  :  /* Timer T2 expired waiting for DCN */
	
		case EFX_T2EXPRX     :  /* Timer T2 expired waiting for NSS/DCS/MCF */
		case EFX_T2EXPMPSRX  :  /* Timer T2 expired waiting for next Fax page */
		case EFX_T2EXPDRX    :  /* Timer T2 expired waiting for Phase D */
		case EFX_T2EXPFAXRX  :  /* Timer T2 expired waiting for fax page */
		case EFX_T2EXPRRRX   :  /* Timer T2 expired waiting for RR command */
		case EFX_COMMERRRX   :  /* Receive communication error */
		case EFX_INVALCMDRX  :  /* Unexpected command after page received */
		case EFX_GOTDCSRX	 :  /* DCS received while waiting for DTC */
		case EFX_NOFAXRX	 :  /* Timed out waiting for first line */  
		case EFX_NOEOLRX	 :  /* Timed out waiting for EOL */  
		case EFX_NOCARRIERRX :  /* Lost carrier during Fax receive */  
		case EFX_NXTCMDRX	 :  /* Timed out wtng for next receive page cmd */
		case EFX_PNSUCRX	 :  /* No PN_SUCCESS returned by modem during rcv */
		case EFX_ECMPHDRX    :  /* Invalid ECM response from transmitter */
		default				 :
			return FX_DISCONNECT;
	}
}
/*
 * To translate fax-error IDs to defined values
 */
int dialogicFaxChannel::getLastError
							(
							int handle,			// handle to opnened resource
							dialogicEvent *event// event to be process
							)
{
	int errorID = ATDV_LASTERR(handle); // to get last error ID

	if ( event != NULL )
	{
		event->setTermReason( errorID );
		event->setTermReason( ATDV_ERRMSGP(handle) );
	}

//printf("Fax Error: %s Estate: %d\n", ATDV_ERRMSGP(handle), estat);
	switch( errorID )
	{
	case EDX_SYSTEM		:
		if( errno != 0 ) return DX_ERROR;

	case 0				:
	case EFX_DISCONNECT : /* Remote has disconnected */
		return getFaxDisconnectedReason( ATFX_ESTAT(handle) );

	case EFX_POLLED		: /* Received poll from remote */
		return FX_POLL_REQUEST;

	case EFX_NOPOLL		: /* Remote did not accept poll */
		return FX_POLL_REJECT;

	case EFX_COMPAT		: /* Hardware incapable of transmitting at specified width and resolution */
	case EFX_BADTIF     : /* Incorrect TIFF/F format */
	case EFX_BADTAG     : /* Incorrect values for TIFF/F tags */
	case EFX_NOPAGE     : /* Specified page missing in TIFF/F file */
	case EFX_BADPAGE    : /* Not a valid page in TIFF/F file */
	case EFX_BADTFHDR   : /* Bad TIFF/F header */
		return FX_FORMAT_ERR;

	case EFX_RETRYDCN   : /* Disconnected after specifed retries */
	case EFX_BADPHASE   : /* unexpected phase transition */
		return FX_COMM_ERR;

	default				:
		return DX_ERROR;
	}
}
