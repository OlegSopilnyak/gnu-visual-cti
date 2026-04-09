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
package org.visualcti.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.net.URLClassLoader;

import org.visualcti.server.unitCommand;
/** VisualCTI server starter */
public class Main
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Server server = null;
        try {
            server = new Server();
        }catch(Throwable e){
            e.printStackTrace();
            System.exit(0);
        }
        try{
            unitCommand start = new unitCommand(server,unitCommand.START_ID);
            System.out.println(">>>Starting the server...");
            server.execute(start);
//            System.out.println(">>>The server has started.");
/*
            Thread.sleep(1000000);
            unitCommand stop = new unitCommand(server,unitCommand.STOP_ID);
            System.out.println("\n>>>Begin stop the server...");

            server.execute(stop);
            System.out.println(">>>The server has stopped.");
*/
        }catch(Throwable e){
            e.printStackTrace();
            System.exit(0);
        }
    }
}
