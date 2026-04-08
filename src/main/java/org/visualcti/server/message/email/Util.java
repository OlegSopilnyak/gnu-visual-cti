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
package org.visualcti.server.message.email;

import java.util.*;
import java.io.*;

import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.InvalidMessageException;
import org.visualcti.util.MediaData;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
/**
Utils class for operate by messages
*/
class Util
{
   /**
   to copy content from message to MIME message
   */
    static void message2mime
                    (
                    org.visualcti.server.message.Message in,
                    javax.mail.internet.MimeMessage out
                    )
                    throws MessageException, javax.jms.JMSException
    {
        try {
            // check message fields
            String reply = in.getJMSReplyTo().toString();
            if (reply.length() == 0) throw new InvalidMessageException("Invalid replyTo attribut.");
            String destination = in.getJMSDestination().toString();
            if (destination.length() == 0) throw new InvalidMessageException("Invalid destination attribut.");

            // to make MIME message addresses
            Address
    	    reply_addr[]= {new InternetAddress( reply )},           // ReplyTo
    	    from_addr = new InternetAddress( reply ),               // From
    	    recipient_addr[]= {new InternetAddress( destination )}; // To

            // to setup MIME message header fields
    		out.setFrom( from_addr );
    		out.setReplyTo( reply_addr );
    		out.setRecipients( Message.RecipientType.TO, recipient_addr );
    		String subject = in.getJMSType();
    		out.setSubject( subject==null ? "":subject );
    		// to copy message user properties to MIME message
    		copyUserProperties(in,out);

    		// to fill message body
    		Util.setContent(in,out);

    		// to save changes to MIME messages
            out.saveChanges();
        }catch(MessagingException  e){
            new InvalidMessageException("javamail:"+e.getMessage());
        }
    }
/**
<prefix>
prefix of message's user property name, for MIME message headers
*/
final static String MUP_PREFIX = "User_Property_";
        /**
        to copy message user properties to MIME message
        */
        private static void copyUserProperties
                                (
                                org.visualcti.server.message.Message in,
                                javax.mail.internet.MimeMessage out
                                )
                                throws MessagingException, javax.jms.JMSException
        {
            Enumeration e = in.getPropertyNames();
            while ( e.hasMoreElements() )
            {
                String name = (String)e.nextElement();
                if ( name.startsWith(in.A_PREFIX) ) continue;
                String value = (String)in.getStringProperty(name);
                if(value!=null)out.addHeader(MUP_PREFIX+name,value);
            }
        }
        /** to setup MIME message content */
        private static void setContent
                                (
                                org.visualcti.server.message.Message in,
                                javax.mail.internet.MimeMessage out
                                )
                                throws MessagingException
        {
            // public container
            MimeMultipart mainPart = new MimeMultipart();

            // solve text
    		String bodyText = in.getBodyText();
            // Text part container
            MimeBodyPart textPart = new MimeBodyPart();
            // store message's bodyText
            textPart.setText( bodyText==null ? "":bodyText );
            // To add Text part to public container
            mainPart.addBodyPart( textPart );

            // solve attachment
            Object attachment = in.getBodyAttachment();
            if (attachment != null) {
                Util.solveAttachmnet(mainPart,attachment);
            }
            // To save public container to message, as message content
            out.setContent(mainPart);
        }

        /**
        to solve attachment and make BLOG MimeBodyPart
        */
        private static void solveAttachmnet
                                (
                                MimeMultipart mainPart,
                                Object attachment
                                )
                                throws MessagingException
        {
            DataSource source = null;
            String fileName = null;
            byte[]data=null;
            if (attachment instanceof File)
            {
                try {// load data from file
                    File file = (File)attachment;
                    if (!file.exists()) return;
                    FileInputStream in = new FileInputStream(file);
                    data = new byte[ (int)file.length() ];
                    in.read(data); in.close();
                    fileName = file.getName();
                }catch(IOException e){
                    return;
                }
            }else {
                try{// casting
                    data = (byte[])attachment;
                }catch(ClassCastException e){
                    return;
                }
            }
            // to solve the data content type
    		switch( MediaData.getTypeByHeader(data) )
    		{
    		    case MediaData.Type.SOUND:
    		        if (fileName == null) fileName = "Voice.wav";
    		        source = new SoundBytesDataSource( data, fileName );
    		        break;
    		    case MediaData.Type.FAX:
    		        if (fileName == null) fileName = "Fax.tif";
    		        source = new TiffBytesDataSource( data, fileName );
    		        break;
    		    default: return;
    		}
            // Attachment's container
            MimeBodyPart blobPart = new MimeBodyPart();
            // setup datahandler for this source
            blobPart.setDataHandler( new DataHandler(source) );
            // to setu the file's name
            blobPart.setFileName(fileName);
            // To add attachment's container to public container
            mainPart.addBodyPart(blobPart);
        }
        /**
        class data source for byte[] contains WAV-file image
        */
        private static final class SoundBytesDataSource extends  MediaBytesDataSource{
            SoundBytesDataSource(byte[] data,String name){super(data,name);}
            public final String getContentType(){return "audio/x-wav";}
        }
        /**
        class data source for byte[] contains TIFF-file image
        */
        private static final class TiffBytesDataSource extends  MediaBytesDataSource{
            TiffBytesDataSource(byte[] data,String name){super(data,name);}
            public final String getContentType(){return "image/tiff";}
        }
/***
 * <source>
 * Class-datasource for media's array
 */
public static abstract class MediaBytesDataSource  implements DataSource {
    private final byte[] data;
    private final String name;
    public MediaBytesDataSource(byte[] data,String name){
      this.data=data;this.name=name;
    }
    public final InputStream getInputStream() throws IOException
    {
        return new ByteArrayInputStream( this.data );
    }
    public final OutputStream getOutputStream()  throws IOException
    {
        throw new IOException("Can't write data");
    }
    public abstract String getContentType();
    public final String getName(){
      return this.name;
    }
}
}
