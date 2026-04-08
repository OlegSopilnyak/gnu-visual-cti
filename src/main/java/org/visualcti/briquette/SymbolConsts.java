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
package org.visualcti.briquette;

/**
Constants of symbol
*/
public interface SymbolConsts
{
/* ***** Type definitions begin ***** */
// To define the symbol type
int ANY   =-1; // any type (for new local symbol)
int NONE   =0; // value type - not defined (for new local symbol)
int STRING =1; // value type - string
int NUMBER =2; // value type - number
int VOICE  =3; // value type - voice data
int FAX    =4; // value type - fax data
int BIN    =5; // value type - bynary data (from RDBMS column)
int MAX_TYPE = 5;
// To define string representation of symbol's type
String TYPE[] = new String[] {"not defined","string","number","voice","fax","binary"};
// To define string representation of symbol's type
String SHOTR_TYPE[] = new String[] {"","(C)","(N)","(V)","(F)","(B)"};
/* ***** Type definitions end ***** */

/* ***** Group definitions begin ***** */
int ALL         = -1;// all groups
int USER        = 0; // user values (auto)
int DATABASE    = 1; // database realated values
int DBCOLUMN    = 2; // database column
int SYSTEM      = 3; // system values
int MAX_GROUP = 3;
// To define string representation of symbol's group
String GROUP[] = new String[] {"local","db","db_col","system"};
/* ***** Group definitions end ***** */
}
