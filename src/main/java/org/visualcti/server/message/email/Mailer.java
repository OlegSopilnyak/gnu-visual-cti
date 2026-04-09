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
package org.visualcti.server.message.email;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.SendMessageException;
import org.visualcti.server.unitError;
import org.visualcti.server.unitEvent;

/**
to send org.visualcti.server.message.Message via smtp-protocol
*/
class Mailer {
    /**
    To send Message
    */
    static void sendMessage
                    (
                    org.visualcti.server.message.email.Factory factory,
                    org.visualcti.server.message.Message message,
                    String smtpServer
                    )
                    throws javax.jms.JMSException
    {
		Properties props = (Properties)System.getProperties().clone();
		// to set up delivery via smtpServer property
		props.put("mail.smtp.host", smtpServer);
		// to check server's accessibility
        try {Mailer.checkSMTP(props);
        }catch(Exception e){
		    factory.dispatchEvent(new unitError(factory,e));
		    throw new SendMessageException( "Can't get access to SMTP host "+smtpServer+" because "+e.getMessage() );
        }
		// to create JavaMail Session
		Session session = Session.getDefaultInstance(props, null);
		//session.setDebug(true);
		// to make MIME message
		MimeMessage oMessage = new MimeMessage(session);
		// to copy contents from Message to MIME message
		Util.message2mime(message, oMessage);
        factory.dispatchEvent( new unitEvent(factory,"Start SMTP transport...") );
		try {
			// try to deliver MIME message
			Transport.send(oMessage);
			message.setStringProperty(Message.RESULT, Message.RESULT_OK);
			message.setStringProperty(Message.MESSAGE, "Success.");
			// to notify thread suspended on this semaphore
			synchronized(message){message.notify();}
            factory.dispatchEvent( new unitEvent(factory,"SMTP transport have finished...") );
		}catch (javax.mail.MessagingException e){
		    factory.dispatchEvent(new unitError(factory,e));
			// exception occured :-\
			message.setStringProperty(Message.RESULT, Message.RESULT_ERROR);
			message.setStringProperty(Message.MESSAGE, e.getMessage());
			if (e instanceof javax.mail.SendFailedException)
			    throw new SendMessageException( e.getMessage() );
			else
			    throw new MessageException( e.getMessage() );
		}
    }
        /**
        to check smtpServer availability
        */
        private static void checkSMTP(Properties props) throws Exception
        {
            String server = props.getProperty("mail.smtp.host", "localhost");
            String port = props.getProperty("mail.smtp.port", "25");
		    InetAddress serverIP = InetAddress.getByName( server );
            Socket sock = new Socket(serverIP, Integer.parseInt(port));
            sock.close();
            // change SMTP server name to IP address
            props.put("mail.smtp.host", serverIP.getHostAddress());
        }
}
