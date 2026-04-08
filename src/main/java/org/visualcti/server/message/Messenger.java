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
package org.visualcti.server.message;

import java.util.Properties;
import org.visualcti.server.service.MasterService;

/**
The interface define methods for access
to the messages (create,send,receive)
*/
public interface Messenger extends MasterService
{
   /**
<produce>
to create message of factory by factory name
   */
   Message create(String factoryName) throws MessageException;
   /**
<produce>
to create message as reply to message for factory
   */
   Message createReplyToMessage(String factoryName, Message source) throws MessageException;
   /**
<transfer>
to send the message via factory by factory name
   */
   void send(String factoryName, Message message) throws MessageException;
   /**
<transfer>
to receive message from factory, using selector
   */
   Message recieve(String factoryName,String selector,Properties properties) throws MessageException;
   /**
<accessor>
To get access to factory, using the name of factory
   */
   MessageFactory getFactory(String name);
}
