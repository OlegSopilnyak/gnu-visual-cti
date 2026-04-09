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
package org.visualcti.briquette.message;

import org.visualcti.briquette.*;
import java.util.*;


public abstract class Basis extends Operation {
/**
<value>
The system symbol
The flag of message's availability (Yes/No)
*/
public static final Symbol system_msg_Available = Symbol.newSystem("msg.Available", Symbol.STRING);
/**
<value>
The system symbol
The message's TimeStamp
*/
public static final Symbol system_msg_TimeStamp = Symbol.newSystem("msg.TimeStamp", Symbol.STRING);
/**
<value>
The system symbol
The message sender's address
*/
public static final Symbol system_msg_ReplyTo = Symbol.newSystem("msg.ReplyTo", Symbol.STRING);
/**
<value>
The system symbol
The message's destination
*/
public static final Symbol system_msg_Destination = Symbol.newSystem("msg.Destination", Symbol.STRING);
/**
<value>
The system symbol
The type of the message
*/
public static final Symbol system_msg_Type = Symbol.newSystem("msg.Type", Symbol.STRING);
/**
<value>
The system symbol
The text part of the message
*/
public static final Symbol system_msg_Text = Symbol.newSystem("msg.Text", Symbol.STRING);
/**
<value>
The system symbol
The codepage of text part of the message
*/
public static final Symbol system_msg_Text_CodePage = Symbol.newSystem("msg.Text.CodePage", Symbol.STRING);
/**
<value>
The system symbol
The binary part of the message
*/
public static final Symbol system_msg_Attachment = Symbol.newSystem("msg.Attachment", Symbol.BIN);
/**
<value>
The system symbol
The type of binary part of the message
*/
public static final Symbol system_msg_AttachmentType = Symbol.newSystem("msg.Attachment.Type", Symbol.STRING);
/**
 * <pool>
 * The pool of predefined symbols
 */
private static final List predefined = new ArrayList();
/**
 * <init>
 * To initialize predefined Symbols
 */
static{
  Basis.predefined.add(Basis.system_msg_Available);
  Basis.predefined.add(Basis.system_msg_TimeStamp);
  Basis.predefined.add(Basis.system_msg_ReplyTo);
  Basis.predefined.add(Basis.system_msg_Destination);
  Basis.predefined.add(Basis.system_msg_Type);
  Basis.predefined.add(Basis.system_msg_Text);
  Basis.predefined.add(Basis.system_msg_Text_CodePage);
  Basis.predefined.add(Basis.system_msg_Attachment);
  Basis.predefined.add(Basis.system_msg_AttachmentType);

  // for msg.attachment.type
  Basis.predefined.add(Symbol.newConst("Voice"));
  Basis.predefined.add(Symbol.newConst("Fax"));
  Basis.predefined.add(Symbol.newConst("Binary"));
  // for msg.Available
  Basis.predefined.add(Symbol.newConst("Yes"));
  Basis.predefined.add(Symbol.newConst("No"));
}
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * It may be overrided in children
   * @return predefined symbols
   */
  public final List getPredefinedSymbols(){return predefined;}
}
