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
#include "dialogicChannelInfo.hpp"
#include "dialogicEvent.hpp"

/*
 * class realize the Dialogic FAX features
 */
class dialogicFaxChannel
{
private:
	/*
	 * To check, is handle support FAX features
	 */
	static
	unsigned char isFAX(int handle)
	{
		Context *context = Context::getContext( handle );//get context by handle
		if (context == NULL) return FALSE;//invalid handle
		return dialogicChannelInfo::isResource_FAX(handle);//is handle to fax resource
	}
	/*
	 * To initialize new state of fax resource
	 */
	static int initState(int handle,int state);
public:
	/*
	 * To start send fax as TIFF
	 */
	static int startSendAsTIFF
		(
		int handle,						// handle to fax resource
		const char *faxname,			// document file name
		unsigned char resHi = FALSE,	// is high resolution for document
		unsigned char issvrq= FALSE,	// is call user after transmition
		int firstpg = 1,				// starts transmition from page
		int pgcount = -1				// how many pages to transmition (default all)
		);
	/*
	 * To start send fax as ASCII text
	 */
	static int startSendAsText
		(
		int handle,						// handle to fax resource
		const char *faxname,			// document file name
		unsigned char resHi = FALSE,	// is high resolution for document
		unsigned char issvrq= FALSE		// is call user after transmition
		);
	/*
	 * To start simple send fax
	 */
	static int startSendSimple
		(
		int handle,						// handle to fax resource
		char *faxname,					// document file name
		unsigned char isTIFF = TRUE		// document type
		)
	{
		return isTIFF ?
			startSendAsTIFF(handle,faxname):
			startSendAsText(handle,faxname);
	}
public:
	/*
	 * To initialize receive fax state
	 */
	static int init_RX_state(int handle){return initState(handle,DF_RX);}
	/*
	 * To initialize transmit fax state
	 */
	static int init_TX_state(int handle){return initState(handle,DF_TX);}
	/*
	 * To start receive fax
	 */
	static int startRecieve(int handle, const char *file, int issvrq);
	/*
	* To setting up the document header
	* Parameters for Header format 2. 
	* If the application wishes to configure the entire page header string
	* the FC_HDRATTRIB parameter must be set to DF_HDRFMT2 and the 
	* FC_HDRUSER2 parameter set to the string to be displayed.
	* The FC_HDRUSER2 may contain %R and %P to display the remote id and
	* page number.
	*/
	static int setHeader(int handle, const char *header);
	/* 
	* To setting up the DateTime format
	* User formatted Date/Time string parameter - application provides a 
	* string (max 27 chars + null termination) which is directly used in 
	* Date/Time field.  Disable internal generation by setting one or both
	* of the Date/Time format parameters (above) to format 0.
	*/
	static int setDateTime(int handle, const char *timeStamp)
	{

		return isFAX(handle) ?
			fx_setparm(handle,FC_HDRDATETIME, (void *)timeStamp):
			DX_ERROR;// not fax resource handle
	}
	/* 
	* To setup the local ID for document (phone number used for transmission)
	* Local Id parameters.  The NULL terminated id string can 
	* have a maximum length of 20 characters plus NULL termination.
	*/
	static int setLocalID(int handle, const char *ID)
	{
		return isFAX(handle) ?
			fx_setparm(handle,FC_LOCALID, (void *)ID):
			DX_ERROR;// not fax resource handle
	}
	/* 
	* To setup the remote ID for document (phone number used for reception)
	* Remote Id parameters.  The NULL terminated id string can 
	* have a maximum length of 20 characters plus NULL termination.
	*/
	static int setRemoteID(int handle, const char *ID)
	{
		return isFAX(handle) ?
			fx_setparm(handle,FC_REMOTEID, (void *)ID):
			DX_ERROR;// not fax resource handle
	}
	/*
	 * To get, how many pages transferred
	 */
	static int transferredPages(int handle)
	{
		return isFAX(handle) ? ATFX_PGXFER(handle):DX_ERROR;
	}
	/*
	 * To setup, from what page to begin
	 * numbering of the transfered documents
	 */
	static int setStartPage(int handle, int page)
	{
		return isFAX(handle) ? 
			fx_setparm(handle, FC_HDRSTARTPAGE, (void *)&page):
			DX_ERROR;// not fax resource handle
	}
	/*
	 * To translate fax-errors to defined values
	 */
	static int getLastError(int handle, dialogicEvent *event = NULL);
};
#define FX_NORMAL 0;
#define FX_POLL_REQUEST 1
#define FX_FORMAT_ERR 2
#define FX_COMM_ERR 3
#define FX_DISCONNECT 4
#define FX_TIMEOUT 5
#define FX_REMOTE_STOP 6
#define FX_NOCOMPATIBLE 7
#define FX_POLL_REJECT 8
#define FX_STOP 9
