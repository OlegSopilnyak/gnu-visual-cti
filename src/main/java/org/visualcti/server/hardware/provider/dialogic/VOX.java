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
package org.visualcti.server.hardware.provider.dialogic;


import java.io.*;
import java.util.*;

import org.visualcti.media.*;
import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * class for realize Dialogic's VOX features (play/record/tone/input)</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
final class VOX
{
      /**
       * <check> To check, is handle have a context
       * @param handle handle to test
       * @return true if handle have a context
       */
      private static final boolean valid(int handle){return Context.get(handle) != null;}
    /**
     * <check>
     * To test, is device have a VOX features
     * @param handle device's handle
     * @return true if device is support VOX features
     */
    final static boolean isResource_VOX(int handle)
    {
      return !valid(handle) ? false:VOX.isSupport(handle);// (native)
    }
    /**
     * <action>
     * To fill the context
     * @param handle device's handle
     */
    static final void openCodecs(int handle)
    {
      // to check the handle (context)
      if ( !VOX.valid(handle) ) return;
      // to clear context's codecs
      Context.get(handle).setAvailableCodecs(new ArrayList());
      // to check the handle's features
      if ( !VOX.isResource_VOX(handle) ) return;
      String codecs = VOX.getCodecsList(handle);
      if (codecs != null)
      { // audio codecs exists
        Hardware.debug("Hardware: handle="+handle+" support codecs \n"+codecs);
        ArrayList list = new ArrayList();// the list of supported formats
        StringTokenizer st = new StringTokenizer(codecs," \n");
        while( st.hasMoreTokens() )
        {
          String descriptor = st.nextToken();
          Audio format = Audio.fromString(descriptor);
          if (format != null)
          {
            list.add( format );
            Hardware.debug("Hardware:for handle="+handle+" add codec "+descriptor);
          }
        }
        if (list.size() > 0) Context.get(handle).setAvailableCodecs(list);
      }
    }
    /**
     * <accessor>
     * To get access to codecs array for handle
     * @param handle device's handle
     * @return the array
     */
    final static Audio[] getAvailableCodecs(int handle)
    {
      try{return Context.get(handle).getAvailabledCodecs();
      }catch(NullPointerException e){}// invalid handle
      Hardware.error("getAvailableCodecs:can't find context for "+handle);
      return null;
    }
    /**
     * <action>
     * To play back voice-data
     * @param handle device's handle
     * @param stream the InputStream to media
     * @param mask DTMF's termination mask
     * @param time maxtime to play
     * @param format the format of media
     * @return termination's reason
     */
    final static String play
        (
        int handle,
        InputStream stream,
        String mask,
        int time,
        Audio format
        )
    {
      Context context = Context.get(handle);// get context for handle
      if ( context.isDisconnected() ) return Reason.CALL.DISCONNECT;// no connection
      try {
        if ( !VOX.isSupported(context, format) ) return Reason.IO.FORMAT;// invalid format
        // to execute playback
        VOX.executePlay(context, handle, stream, mask, time, format);
        // to solve the termination's reason
        return Hardware.termReason(handle);
      }catch(Exception e){
        throw new HardwareError( e.getMessage() );/* throw error */
      }
    }
        /* to execute play back operation or throw the error */
        private final static void executePlay
            (
            Context context,
            int handle,
            InputStream stream,
            String mask,
            int time,
            Audio format
            )
            throws InterruptedException
        {
          Object owner = context.getOwner();// get owner of handle
          context.setInputStream( stream );// store stream for callbacks
          String codec = format.toString();// make string from format
          // try to start play
          int state = VOX.startPlay(handle, mask, time, codec);// (native)
          if ( state != Hardware.DX_ERROR )
          {   // playback has start successful
            // to wait a dialogic's event
            synchronized(owner){owner.wait();}
            context.setInputStream( null );// to clear the stream in context
          } else {
            // hardware's malfunction
            context.setInputStream( null );// to clear the stream in context
            Hardware.malfunction(handle, "playBack");// throws error
          }
        }
    /**
     * <action>
     * To record the voice & store it to OutputStream
     * @param handle device's handle
     * @param stream the OuputStream to media's container
     * @param mask DTMF's termination mask
     * @param silence maxtime of silent for terminate the record
     * @param time maxtime of record
     * @param format the format of media-data
     * @return termination's reasob
     */
    final static String record
	    (
	    int handle,
	    OutputStream stream,
	    String mask,
	    int silence,
	    int time,
	    Audio format
	    )
    {
      Context context = Context.get(handle);
      if ( context.isDisconnected() ) return Reason.CALL.DISCONNECT;
      try {
        if ( !VOX.isSupported(context, format) ) return Reason.IO.FORMAT;// invalid format
        // to execute record
        VOX.executeRecord(context, handle, stream, mask, silence, time, format);
        // to solve the termination's reason
        return Hardware.termReason( handle );
      }catch(Exception e){
        throw new HardwareError( e.getMessage() );// to throw error
      }
    }
        /* to execute record operation or throw the error */
        private final static void executeRecord
            (
            Context context,
            int handle,
            OutputStream stream,
            String mask,
            int silence,
            int time,
            Audio format
            )
        throws InterruptedException
        {
          Object owner = context.getOwner();// get owner of the handle
          context.setOutputStream( stream );// set stream for call-backs
          String codec = format.toString();// make codec string
          // try to start record
          int state = VOX.startRecord(handle, mask, silence, time, codec);// (native)
          if (state != Hardware.DX_ERROR )
          {   // record started
            // to wait a dialogic's event
            synchronized(owner){owner.wait();}
            context.setOutputStream( null );// to clear the stream in context
          } else {// hardware's malfunction
            context.setOutputStream( null );// to clear the stream in context
            Hardware.malfunction(handle, "record");// throws error
          }
        }
        /* to check the codec's compatibility */
        private final static boolean isSupported(Context context, Audio codec){
          Audio[] codecs = context.getAvailabledCodecs();
          if (codecs != null && codecs.length > 0) {
            for (int i=0; i < codecs.length;i++)
                if ( codecs[i].equals(codec) ) return true;
          }
          return false;
        }
    /**
     * <action>
     * To dial the symbols sequence
     * @param handle device's handle
     * @param digits the sequence
     */
    final static void sendDigits( int handle, String digits )
    {
      Context context = Context.get(handle);// to get context for handle
      if ( context.isDisconnected() ) return;
      try{
        // to get the owner of context
        Object owner = context.getOwner();
        // try to start dial
        int state = VOX.dial(handle,digits); // (native)
        if ( state != Hardware.DX_ERROR)
        {   // dial started
          // wait a dialogic's event
          synchronized(owner){owner.wait();}
        } else {   // hardware's malfunction
          Hardware.malfunction(handle, "dial string ["+digits+"]");// throws error
        }
      }catch(Exception e){}
    }
    /**
     * <action>
     * To play the tone
     * @param handle device's handle
     * @param freq1 fequency 1 (Hz)
     * @param freq2 frequency 2 (Hz)
     * @param duration duration of play (msec)
     */
    final static void playTone(int handle,int freq1,int freq2,int duration)
    {
      Context context = Context.get(handle);// to get context for handle
      if ( context.isDisconnected() ) return;
      try{Object owner = context.getOwner();// to get owner of handle
        // try to start playing tone
        int state = VOX.generateTone(handle, freq1, freq2, duration); // (native)
        if ( state != Hardware.DX_ERROR)
        {   // play tone started
          // wait a dialogic's event
          synchronized(owner){owner.wait();}
        } else {// hardware malfunction
          Hardware.malfunction(handle,"play tone");
        }
      }catch(Exception e){}
    }
    /**
     * <action>
     * To get the value of user's input
     * @param handle device's handle
     * @return value
     */
    final static String getDigitsBuffer(int handle)
    {
      Context context = Context.get(handle);
      return context == null ? "" : context.getDigitsBuffer();
    }
    /**
     * <action>
     * To accept One DTMF symbol of user's input
     * @param handle device's handle
     * @param timeout time to retrive the symbol
     * @return termination's reason
     */
    final static String retriveSignal(int handle,int timeout )
    {
      Context context = Context.get(handle);// to get device's context
      try{
        Object owner = context.getOwner();
        // to start retrieve the signal
        synchronized( owner )
        {
          int state = VOX.getDigit(handle, timeout); // (native)
          if ( state != Hardware.DX_ERROR )
          {   // the retrieve started
              // to wait a Dialogic's event
              // ##################################
              // In this place is danger situation :-(
              // It is posible the driver already
              // call the notify for the owner (event arrived)
              // #################################
              // this is reason why I wait the notify timeout * 1 sec
              // maybe later I solve it
  //            synchronized(owner){owner.wait((timeout+1) * 1000);}
              // to wait the notify from Dialogic's events queue
              owner.wait();
          } else {/* hardware malfunction */
  //                return portDriver.PS_ERROR;// we will not throw error, only information
            Hardware.malfunction(handle, "get digit");// for check, why mistake (this is temporary)
          }
        }
        // to solve & return the termination's reason
        return Hardware.termReason(handle);
      }catch(Exception e){
        throw new HardwareError( e.getMessage() );
      }
    }
    /**
     * <action>
     * To begin to make tones table for handle
     * @param handle device's handle
     */
    final static void beginMakeTones(int handle)
    {
      if ( valid(handle) )
      {
        // to clear tones table of device
        int state = VOX.clearTones(handle); // (native)
        if ( state == Hardware.DX_ERROR )
        {   // mistake
          throw new HardwareError("can't to clear tones table for "+handle);
        }
      }
    }
    /**
     * <action>
     * To update the Tone in tones table
     * @param handle device's handle
     * @param descriptor description of the tone
     */
    final static void updateTone(int handle, String descriptor)
    {
      if ( valid(handle) )
      {
        // tone's updating
        int state = VOX.setTone(handle,descriptor); // (native)
        if ( state == Hardware.DX_ERROR )
        {   // mistake
          throw new HardwareError("can't update tone for "+handle);
        }
      }
    }
    /**
     * <action>
     * To fix changes of tones table
     * @param handle device's handle
     */
    final static void endMakeTones(int handle)
    {
      if ( valid(handle) )
      {
        // to fix the changes
        int state = VOX.fixTones(handle); // (native)
        if ( state == Hardware.DX_ERROR )
        {   // mistake
          throw new HardwareError("can't to fix tone table for "+handle);
        }
      }
    }
    /**
     * <action>
     * To enable/disable Tone's detection
     * @param handle device's handle
     * @param tone the name of Tone
     * @param enable flag
     */
    final static void setToneDetect(int handle,String tone,boolean enable)
    {
      if ( valid(handle) )
      {
        // to change state of tone detection
        int state = VOX.toneDetect(handle, tone, enable); // (native)
        if ( state == Hardware.DX_ERROR )
        {   // mistake
          throw new HardwareError("can't change state of tone detection for "+handle);
        }
      }
    }
//********** NATIVES ***********/
        /*<native> to check is handle to VOX's device */
        private static native boolean isSupport(int handle);
        /*<native> to get access to device's codecs list */
        private static native String getCodecsList(int handle);
        /*<native> to start the record voice */
        private static native int startRecord
                    (
                    int handle,
                    String mask,
                    int maxsilence,
                    int maxtime,
                    String codec
                    );
        /*<native> to start the playback */
        private static native int startPlay
                    (
                    int handle,
                    String mask,
                    int maxtime,
                    String codec
                    );
        /*<native> to start the dial number */
        private static native int dial(int handle,String number);
        /*<native> to start Tone's playing */
        private static native int generateTone
                    (
                    int handle,
                    int freq1,
                    int freq2,
                    int duration
                    );
        /*<native> To retrive the buffer of user's inputs */
        static native String digitsBuffer(int handle);
        /*<native> to start getting one symbol of user input */
        private static native int getDigit(int handle,int timeout);
        /*<native> To clear tones table for handle */
        private static native int clearTones(int handle);
        /*<native> To update tone in global table */
        private static native int setTone(int handle,String descriptor);
        /*<native> To store tones table for handle */
        private static native int fixTones(int handle);
        /*<native> to enable/disable detect tone */
        private static native int toneDetect(int handle,String name,boolean enable);
}
