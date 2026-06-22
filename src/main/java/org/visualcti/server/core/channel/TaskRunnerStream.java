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
package org.visualcti.server.core.channel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.visualcti.server.core.executable.task.Task;
import org.visualcti.server.task.Environment;
import org.visualcti.util.Tools;

/**
 * The output stream for the task
 *
 * @see Task#setEnv(Environment)
 */
public abstract class TaskRunnerStream extends OutputStream {
    // the length of CRLF string for hosted OS
    private static final int CRLF_LENGTH = Tools.CRLF.length();
    // lock for write operation
    private final Lock streamLock = new ReentrantLock();

    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        streamLock.lock();
        try {
            // to notify the owner
            notifyOwner(new String(data, off, len - CRLF_LENGTH));
        }finally {
            streamLock.unlock();
        }
    }

    @Override
    public void write(int b) throws IOException {

    }

    /**
     * <notify>
     * To notify the owner of stream about printed string
     *
     * @param printed printed string
     */
    public abstract void notifyOwner(String printed);
}
