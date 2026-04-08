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
package org.visualcti.server;

/**
<stream>
Stream for attach to task
that stream user for make PrintWriter
*/
abstract class SchedulerStream extends java.io.OutputStream
{
 /**
 <const>
 length of CRLF string for hosted OS
 */
 private final static int crlfLength = org.visualcti.util.Tools.CRLF.length();
 /**
 <semaphore>
 semaphore for synchronize access to notyfy owner
 */
 private Object semaphore = new Object();
    /**
    <notify>
    Notify owner of stream about print string
    */
    public abstract void notifyOwner(String text);
    /**
    <output>
    Override standart OutputStream.write(byte[] buffer,int off,int length)
    */
    public void write(byte[] buffer,int off,int length)
    {
        synchronized(this.semaphore)
        {
            String body = new String(buffer, off, length-crlfLength);
            this.notifyOwner( body );// to notify owner
        }
    }
    /**
    <output>
    Override standart OutputStream.write(int b)
    Do nothing
    */
    public void write(int b){}
}
