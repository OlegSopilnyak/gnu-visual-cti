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

Contact oleg.sopilnyak@gmail.com or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server.hardware.provider.dialogic;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Consts set for solve dialogic's events</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
interface HardwareConst
{
/**
 ** Define Event Types
 **/
 int TDX_PLAY        =0x81;     /* Play Completed */
 int TDX_RECORD      =0x82;     /* Record Completed */
 int TDX_GETDIG      =0x83;     /* Get Digits Completed */
 int TDX_DIAL        =0x84;     /* Dial Completed */
 int TDX_CALLP       =0x85;     /* Call Progress Completed */
 int TDX_CST         =0x86;     /* CST Event Received */
 int TDX_SETHOOK     =0x87;     /* SetHook Completed */
 int TDX_WINK        =0x88;     /* Wink Completed */
 int TDX_ERROR       =0x89;     /* Error Event */
 int TDX_PLAYTONE    =0x8A;     /* Play Tone Completed */
 int TDX_GETR2MF     =0x8B;     /* Get R2MF completed */
 int TDX_BARGEIN     =0x8C;     /* Barge in completed */
 int TDX_NOSTOP      =0x8D;     /* No Stop needed to be Issued */

/*
 * Device specific identifiers
 */
 int DT_IO  =0x00000000;    /* I/O interface dev class */
 int DT_DTI =0x00002000;    /* DTI device class */
/*
 * Unsolicited DTI event types
 */
 int DTEV_RCVPDG   =(DT_DTI | 0x48); /* Received a pulse digit */
 int DTEV_T1ERRC   =(DT_DTI | 0x49); /* T1 error condition event */
 int DTEV_E1ERRC   =DTEV_T1ERRC;    /* E1 error condition event == T1 error */
 int DTEV_COMRSP   =(DT_DTI | 0x4A); /* Successful com test */
 int DTEV_DATRSP   =(DT_DTI | 0x4B); /* Response to data test */
 int DTEV_PDDONE   =(DT_DTI | 0x4C); /* Pulse dial complete */
 int DTEV_SIG      =(DT_DTI | 0x4D); /* Signalling event */
 int DTEV_RETDIAG  =(DT_DTI | 0x4E); /* Diagnostic complete */
 int DTEV_WINKCPLT =(DT_DTI | 0x4F); /* Wink complete */
 int DTEV_MTFCNCPT =(DT_DTI | 0x50); /* Multi-tasking func complete */
 int DTEV_CLKFAIL  =(DT_DTI | 0x51); /* Clock FallBack Event */
 int DTEV_CASTEVT  =(DT_DTI | 0x52); /* CAS DTI template matched event */
 int DTEV_CASSENDENDEVT =(DT_DTI | 0x53); /* CAS DTI template send finished event */
 int DTEV_ERREVT   =(DT_DTI | 0xF0); /* Error condition event */
 int DTEV_OUTSERVICE =(DT_DTI | 0xF1); /* Device out of Service event */
/*
 * Fax events returned to the application
 */
 int  TFX_FAXSEND   =0x0A1;      /* Send fax complete */
 int  TFX_FAXRECV   =0x0A2;      /* Receive fax complete */
 int  TFX_FAXERROR  =0x0A3;      /* Error event for Fax */
 int  TFX_PHASEB    =0x0A4;      /* Phase B event */
 int  TFX_PHASED    =0x0A5;      /* Phase D event */
 int  TFX_LOADFONT  =0x0A6;      /* Font loading complete */
}
