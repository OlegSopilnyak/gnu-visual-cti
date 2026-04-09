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
package org.visualcti.server.service;

import java.io.*;
import org.visualcti.server.message.*;
/** class for test services features  */
public class tester
{
   
    /**
    Method for start "service manager" separately
    */
    public static void main(String[] args)
    {
        Manager manager = ServiceManager.getManager();
        try {
            manager.Start();
            sendEmailMessage( manager );
            manager.Stop();
        }catch(Exception e){
            e.printStackTrace();
        }
        System.exit(0);
    }
        private static void sendEmailMessage(Manager mng) throws Exception
        {
            Messenger msgr = (Messenger)mng.getService("Messenger");
            messageDeliver delivers[] = new messageDeliver[ 10 ];
            for(int i=0;i < delivers.length;i++)
            {
                delivers[i] = new messageDeliver(msgr,i+1);
            }
            for(int i=delivers.length-1;i >= 0 ;i--) delivers[i].start();
            
            while (true){
                boolean finished = true;
                for(int i=0;i < delivers.length;i++)
                {
                    Thread.yield(); Thread.sleep(100);
                    if (delivers[i].isAlive()) {finished = false; break;}
                }
                if (finished) break;
            }
            Thread.sleep(2000);
        }
        /** thread for make and delivery message via e-mail */
        static class messageDeliver extends Thread
        {   Messenger msgr;int count;MessageFactory factory;
            public messageDeliver(Messenger msgr,int count)
            {
                this.msgr=msgr; this.count=count;
                this.setName("thread"+count);
                factory = msgr.getFactory("EMAIL");
            }
            public void run()
            {
                try {
                    Message out = factory.createMessage();
                    out.setJMSType("Test");
                    out.setJMSDestination(new Message.Destination("olegsopilnyak@yahoo.com"));
                    out.setJMSReplyTo(new Message.Destination("\"Phone Soft\" <"+this.getName()+"@yahoo.com>"));
                    out.setJMSDeliveryMode(Message.PERSISTENT);
                    out.setBodyText("You have message!\n");
                    out.setStringProperty("Vendor", "Prominic Technology Inc.");
                    out.setStringProperty("oleg", ":-P");
                    
                    try{
                        File voice = new File("15oe.wav");
                        if (voice.exists()) {
                            byte[] data = new byte[(int)voice.length()];
                            FileInputStream in = new FileInputStream(voice);
                            in.read(data); in.close();
                            System.out.println("Add attachment from "+voice+" size = "+data.length);
                            out.setBodyAttachment( data );
                        }
                    }catch(IOException e){}
                    this.yield();
                    out.setStringProperty(Message.PROTOCOL,"SMTP");
                    out.setStringProperty(MessageFactory.SERVER,"localhost");
                    factory.send(out);
                    System.out.println("Start sending message from "+this.getName()+"...");
                    this.yield();
                    synchronized(out){out.wait();}// wait while message will delivered
                    System.out.println("Sending message from "+this.getName()+" have finished...");
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
}
