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
package org.visualcti.util;

import java.io.*;
/**
class for soft getting data from InputStream (if data avilable)
apply for socket's InputStream
*/
public abstract class SoftInputStream extends InputStream {
/**
<attribute>
observable stream
*/
private InputStream source;
/**
<buffer>
buffer of readed bytes
*/
private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
/**
<semaphore>
semaphore for this.buffer
*/
private final Object LOCK = new Object();
/**
<semaphore>
semaphore for read operation
*/
private final Object SEMAPHORE = new Object();
/**
<attribute>
flag, cleared when called close()
*/
private volatile boolean opened = false;

    /**
    constructor
    */
    public SoftInputStream(InputStream source)
    {
        this.source = source; /* store observable stream */
    }
    /**
    to start stream reading
    */
    public void start()
    {
        if ( this.opened ) return;/* thread already started */
        this.opened = true;/* enable to work softReader thread */
        new softReader().start();/* to start check available and read to internal buffer */
    }
    /**
    finalize the object, called from System.gc()
    */
    protected void finalize() throws Throwable {this.close();}
    /**
    to close stream
    overrided from java.io.InputStream
    */
    public void close()
    {
        this.opened = false;/* clear opened flag, for stop softReader thread */
        synchronized(SEMAPHORE){SEMAPHORE.notify();}/* wakeup read() call */

    }
    public final int available() throws IOException {
      if ( !this.opened ) throw new IOException("Stream is closed");
      return this.buffer.size();
    }
    /**
    to read bytes array
    overrided from java.io.InputStream
    */
    public int read(byte[] b,int off,int len) throws IOException
    {
        if (this.buffer.size() == 0 && this.opened)
        {
            try {// in buffer no data found, wait the data...
                synchronized(this.SEMAPHORE){this.SEMAPHORE.wait();}
            }catch(InterruptedException e){}
        }
        if (this.buffer.size() == 0 || !this.opened)
        {
            // stream closed, or exception during access
            // to source stream, throws End Of File exception
            throw new EOFException("stream "+this.source+" closed.");
        }
        synchronized(LOCK)
        {   // access to buffer data
            byte[] data = buffer.toByteArray();     // get data from buffer
            int dataLength = data.length;           // calculate the data length
            int need = len - off;                   // calculate needed byte array size
            int length = Math.min(dataLength, need);
            System.arraycopy(data, 0, b, off, length);// copy data from buffer
            // clear the buffer data
            buffer.reset();
            if (dataLength > need)
            {// length needed data less of buffer data
                int Length = dataLength - need;
                buffer.write(data, length, Length); // store data back to the buffer
            }
            return length;
        }
    }
    /**
    to read one byte
    overrided from java.io.InputStream
    */
    public int read() throws IOException
    {
        byte data[] = new byte[1];
        int i = read(data,0,1);
        return i == 0 ? -1:data[0];
    }
/**
method for make softReader threads name
must by overrided in children
*/
abstract protected String getThreadName();

////////////////////////////////////////////////////////////////////
    /**
    inner-class for getting data from
    real stream an push it to buffer
    */
    private class softReader extends Thread  {
        /**
        constructor
        */
        public softReader(){super( getThreadName() );}
        /**
        main scaner loop
        */
        public void run()
        {   final int max = 4096;// internal buffer size
            byte[] buf = new byte[ max ]; // interanl buffer for read from observable stream
            int length;
            try {
                while ( opened )
                {
                    Thread.yield();// to give context to another Threads
                    length = source.available();// check, is data available (can throw exception)
                    if (length > 0)
                    {   // in observable input stream present data
                        int i = Math.min(max,length);// To calculate how many bytes will read
                        // to read available data from observable stream to internal buffer
                        length = source.read(buf, 0, i);
                        // to store data from internal to external buffer
                        synchronized(LOCK) {buffer.write(buf,0,length);}
                        synchronized(SEMAPHORE){SEMAPHORE.notify();}// wakeup the read method
                    }else{
                        try{sleep(100);}catch(Exception e){}// no data in stream, try to sleep
                    }
                }
            }catch(IOException e){// oops...
                synchronized(LOCK) {buffer.reset();}// clear buffer size
                synchronized(SEMAPHORE){SEMAPHORE.notify();}// wakeup the read method
            }
        }
    }
}
