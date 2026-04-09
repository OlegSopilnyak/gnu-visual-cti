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
package org.visualcti.briquette;

import org.visualcti.server.hardware.Reason;
public interface ProgrammSymbols
{
/////////// VALUES BEGIN //////////////////
/**
<value>
The system symbol
the system's date in string format
*/
Symbol system_Date = Symbol.newSystem("System.Date", Symbol.STRING);
/**
<value>
The system symbol
the system's time in string format
*/
Symbol system_Time = Symbol.newSystem("System.Time", Symbol.STRING);
/**
<value>
The system symbol
the system's time in number format
*/
Symbol system_Seconds = Symbol.newSystem("System.Seconds", Symbol.NUMBER);
/**
<value>
The system symbol
the name of current JDBC driver
*/
Symbol system_db_Driver = Symbol.newDatabase("db.Driver", Symbol.STRING);
/**
<value>
The system symbol
the URL to current JDBC driver connection
*/
Symbol system_db_URL = Symbol.newDatabase("db.URL", Symbol.STRING);
/**
<value>
The system symbol
the login of current JDBC driver connection
*/
Symbol system_db_User = Symbol.newDatabase("db.User", Symbol.STRING);
/**
<value>
The system symbol
the flag, is have result after request
*/
Symbol system_db_DataAccess = Symbol.newDatabase("db.DataAccess", Symbol.STRING);
/**
<value>
The system symbol
the number of updated rows in database
*/
Symbol system_db_RowCount = Symbol.newDatabase("db.RowCount", Symbol.NUMBER);

/**
<value>
The system symbol
the class-name of telepehony device
*/
Symbol system_cti_Driver = Symbol.newSystem("cti.Driver", Symbol.STRING);
/**
<value>
The system symbol
the name of telepehony device
*/
Symbol system_cti_portName = Symbol.newSystem("cti.portName", Symbol.STRING);
/**
<value>
The system symbol
the name of current telepehony operation
*/
Symbol system_cti_Operation = Symbol.newSystem("cti.Operation", Symbol.STRING);
/**
<value>
The system symbol
the result of last telepehony operation
*/
Symbol system_cti_Operation_Result = Symbol.newSystem("cti.Operation.Result", Symbol.STRING);
/**
<value>
The system symbol
the result of user's input
*/
Symbol system_cti_Input = Symbol.newSystem("cti.Input", Symbol.STRING);
/**
<value>
The system symbol
the result of record voice (the voice file's image)
*/
Symbol system_cti_Voice = Symbol.newSystem("cti.Voice", Symbol.VOICE);
/**
<value>
The system symbol
the result of record voice (the duration of voice)
*/
Symbol system_cti_Voice_seconds = Symbol.newSystem("cti.Voice.seconds", Symbol.NUMBER);
/**
<value>
The system symbol
the result of fax receiving (the document's image)
*/
Symbol system_cti_Fax = Symbol.newSystem("cti.Fax", Symbol.FAX);
/**
<value>
The system symbol
the number of pages in fax document
*/
Symbol system_cti_Fax_Pages = Symbol.newSystem("cti.Fax.Pages", Symbol.NUMBER);
/**
<value>
The system symbol
the number of transferred pages
*/
Symbol system_cti_Fax_TransferredPages = Symbol.newSystem("cti.Fax.TransferredPages", Symbol.NUMBER);
/**
<value>
The system symbol
the local ID of remote fax-machine
*/
Symbol system_cti_Fax_RemoteID = Symbol.newSystem("cti.Fax.RemoteID", Symbol.STRING);
/**
<value>
The system symbol
Call's target phone-number
(The telephone number, where is made a Call)
*/
Symbol system_cti_CalledNumber = Symbol.newSystem("cti.CalledNumber", Symbol.STRING);
/**
<value>
The system symbol
Call's source phone-number
(The telephone number, whence is made a Call)
*/
Symbol system_cti_CallingNumber = Symbol.newSystem("cti.CallingNumber", Symbol.STRING);
/**
<value>
The system symbol
The originate phone-number of telephone device
*/
Symbol system_cti_OriginateNumber = Symbol.newSystem("cti.OriginateNumber", Symbol.STRING);
/**
<value>
The system symbol
The flag of message's availability (Yes/No)
*/
Symbol system_msg_Available = Symbol.newSystem("msg.Available", Symbol.STRING);
/**
<value>
The system symbol
The message's TimeStamp
*/
Symbol system_msg_TimeStamp = Symbol.newSystem("msg.TimeStamp", Symbol.STRING);
/**
<value>
The system symbol
The message sender's address
*/
Symbol system_msg_ReplyTo = Symbol.newSystem("msg.ReplyTo", Symbol.STRING);
/**
<value>
The system symbol
The message's destination
*/
Symbol system_msg_Destination = Symbol.newSystem("msg.Destination", Symbol.STRING);
/**
<value>
The system symbol
The type of the message
*/
Symbol system_msg_Type = Symbol.newSystem("msg.Type", Symbol.STRING);
/**
<value>
The system symbol
The text part of the message
*/
Symbol system_msg_Text = Symbol.newSystem("msg.Text", Symbol.STRING);
/**
<value>
The system symbol
The codepage of text part of the message
*/
Symbol system_msg_Text_CodePage = Symbol.newSystem("msg.Text.CodePage", Symbol.STRING);
/**
<value>
The system symbol
The binary part of the message
*/
Symbol system_msg_Attachment = Symbol.newSystem("msg.Attachment", Symbol.BIN);
/**
<value>
The system symbol
The type of binary part of the message
*/
Symbol system_msg_AttachmentType = Symbol.newSystem("msg.Attachment.Type", Symbol.STRING);
/////////// VALUES END //////////////////
/**
<array>
The array of system values
*/
Symbol Values[] =
new Symbol[] {
    // clock related
    system_Date,system_Time,system_Seconds,
    // database related
    system_db_Driver,system_db_URL,system_db_User,system_db_DataAccess,system_db_RowCount,
    // telephony related
    system_cti_Driver,system_cti_portName,system_cti_Operation,system_cti_Operation_Result,
    system_cti_Input,system_cti_Voice,system_cti_Voice_seconds,system_cti_Fax,
    system_cti_Fax_Pages,system_cti_Fax_TransferredPages,system_cti_Fax_RemoteID,
    system_cti_CalledNumber,system_cti_CallingNumber,system_cti_OriginateNumber,
    // messages related
    system_msg_Available,
    system_msg_TimeStamp,system_msg_ReplyTo,system_msg_Destination,system_msg_Type,
    system_msg_Text,system_msg_Text_CodePage,
    system_msg_Attachment,system_msg_AttachmentType
};
/**
<array>
The array of system consts
*/
Symbol Consts[] =
new Symbol[] {
    //# for cti.Operation.Result
    // terminated by admin
    Symbol.newConst( Reason.TERMINATED ),
    // call control
    Symbol.newConst( Reason.CALL.DISCONNECT ),
    Symbol.newConst( Reason.CALL.RINGS ),
    // voice IO operations resluts
    Symbol.newConst( Reason.IO.TIMEOUT ),
    Symbol.newConst( Reason.IO.FORMAT ),
    Symbol.newConst( Reason.IO.EOF ),
    Symbol.newConst( Reason.IO.DTMF ),
    Symbol.newConst( Reason.IO.SILENCE ),
    // fax operations resluts
    Symbol.newConst( Reason.FAX.COMPATIBILITY ),
    Symbol.newConst( Reason.FAX.COMMUNICATION_ERROR ),
    Symbol.newConst( Reason.FAX.USER_STOP ),
    Symbol.newConst( Reason.FAX.POLLING ),
    Symbol.newConst( Reason.FAX.NOPOLL ),
    // make call results
    Symbol.newConst( Reason.CA.VOICE ),
    Symbol.newConst( Reason.CA.FAX ),
    Symbol.newConst( Reason.CA.BUSY ),
    Symbol.newConst( Reason.CA.NO_ANSWER ),
    Symbol.newConst( Reason.CA.NO_RESPONDING ),
    // for db.DataAccess
    Symbol.newConst("Yes"), Symbol.newConst("No"),
    // for msg.attachment.type
    Symbol.newConst("Voice"), Symbol.newConst("Fax"), Symbol.newConst("Binary")
};
}
