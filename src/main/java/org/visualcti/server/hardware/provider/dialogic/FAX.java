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
import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * class realize Dialogic's Fax features</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
final class FAX
{
      /**
       * <check> To check, is handle have a context
       * @param handle handle to test
       * @return true if handle have a context
       */
      private static final boolean valid(int handle){return Context.get(handle) != null;}
    /**
     * <action>
     * To check, is device is have a FAX's fetures
     * @param handle device's handle
     * @return true if device have a FAX's features
     */
    final static boolean isResource_FAX(int handle)
    {
      return !valid(handle)? false: FAX.isSupport( handle );// (native)
    }
/*
 * Initial protocol states - set with fx_initstat()
 */
public static final int     DF_RX       =0;      /* Receiver    */
public static final int     DF_TX       =1;      /* Transmitter */

    /**
     * <action>
     * To setup new local ID of fax-machine
     * Local Id parameters.  The NULL terminated id string can
     * have a maximum length of 20 characters plus NULL termination.
     * @param handle device's handle
     * @param ID new local ID
     * @return DX_ERROR if some wrong
     */
    final static int setLocalID(int handle,String ID)
    {
      return !valid(handle) ? Hardware.DX_ERROR:FAX.SetLocalID(handle,ID);// (native)
    }
    /*
     * Remote Id parameters.  The NULL terminated id string can
     * have a maximum length of 20 characters plus NULL termination.
    */
    /**
     * <action>
     * To setup a remote fax-machine's localID
     * Remote Id parameters.  The NULL terminated id string can
     * have a maximum length of 20 characters plus NULL termination.
     * @param handle device's handle
     * @param ID new local ID
     * @return DX_ERROR if some wrong
     */
    final static int setRemoteID(int handle, String ID)
    {
      return !valid(handle) ? Hardware.DX_ERROR:FAX.SetRemoteID(handle,ID);// (native)
    }
    /**
     * <action>
     * To setup a fax-document's header
     * Parameters for Header format 2.
     * If the application wishes to configure the entire page header string
     * the FC_HDRATTRIB parameter must be set to DF_HDRFMT2 and the
     * FC_HDRUSER2 parameter set to the string to be displayed.
     * The FC_HDRUSER2 may contain %R and %P to display the remote id and
     * page number.
     * @param handle device's handle
     * @param info header's value
     * @return DX_ERROR if some wrong
     */
    final static int setHeaderInfo(int handle,String info)
    {
        return !valid(handle) ? Hardware.DX_ERROR:FAX.SetHeaderInfo(handle,info);// (native)
    }
    /**
     * <action>
     * To setup new Date/Time format
     * User formatted Date/Time string parameter - application provides a
     * string (max 27 chars + null termination) which is directly used in
     * Date/Time field.  Disable internal generation by setting one or both
     * of the Date/Time format parameters (above) to format 0.
     * @param handle device's handle
     * @param timeStamp new format's string
     * @return DX_ERROR if some wrong
     */
    final static int setDateTime(int handle, String timeStamp)
    {
      return !valid(handle) ? Hardware.DX_ERROR:FAX.SetDateTime( handle, timeStamp );// (native)
    }
    /**
     * <action>
     * To setup From what page to begin numbering of the transfered document
     * @param handle device's handle
     * @param page start page
     * @return DX_ERROR if some wrong
     */
    final static int setStartPage(int handle, int page)
    {
      return !valid(handle) ? Hardware.DX_ERROR:FAX.SetStartPage( handle, page );// (native)
    }
    /**
     * <action>
     * To init receive state in device
     * @param handle device's handle
     * @return DX_ERROR if some wrong
     */
    final static int initRecieveState(int handle)
    {
        return !valid(handle) ? Hardware.DX_ERROR:FAX.InitRecieveState( handle );// (native)
    }
    /**
     * <action>
     * To init transmite state in device
     * @param handle device's handle
     * @return DX_ERROR if some wrong
     */
    final static int initTransmitState(int handle)
    {
        return !valid(handle) ? Hardware.DX_ERROR:FAX.InitTransmitState( handle );// (native)
    }
    /**
     * <action>
     * To receive the fax-document to the file
     * @param handle device's handle
     * @param file file to store the document
     * @param issvrq flag
     * @return termination's reason
     */
    final static String recieve(int handle, String file, boolean issvrq)
    {
      Context context = Context.get(handle);
      try{
        // to start the operation
        int state = FAX.startRecieve( handle, file, issvrq);// (native)
        if ( state != Hardware.DX_ERROR )
        { // The operation have started
          // to wait a Dialogic's event
          Object owner = context.getOwner();
          synchronized(owner){owner.wait();}
        } else {// hardware malfunction
          Hardware.malfunction(handle, "receive fax"); // throws error
        }
        // to return the event's reason
        return context.getLastEvent().getReason();
      }catch(Exception e){
        throw new HardwareError( "FAX system, receive error:"+e.getMessage() );
      }
    }
    /**
     * <action>
     * To start fax's recieve
     * @param handle device's handle
     * @param file file to store the document
     * @param issvrq flag
     * @return DX_ERROR if some wrong
     */
    final static int startRecieve(int handle, String file, boolean issvrq)
    {
      return !valid(handle) ? Hardware.DX_ERROR:FAX.StartRecieve( handle, file, issvrq );// (native)
    }
    /**
     * <action>
     * To send a fax-document
     * @param handle device's handle
     * @param isTIFF flag, is fax file is TIFF
     * @param faxname document's filename
     * @param resHi flag, is high resolution for document
     * @param issvrq flag, is call user after transmition
     * @param firstpg to starts transmition from page
     * @param pgcount how many pages to transmition (default all)
     * @return termination's reason
     */
    final static String send
                                (
                                int handle,	 // handle to fax resource
                                boolean isTIFF,  // fax file is TIFF
                                String faxname,	 // document file name
                                boolean resHi,	 // is high resolution for document
                                boolean issvrq,	 // is call user after transmition
                                int firstpg,	 // starts transmition from page
                                int pgcount	 // how many pages to transmition (default all)
                                )
    {
      Context context = Context.get(handle);
      try{
        // to start the operation
        int state = FAX.startSend
                                (
                                handle,  // handle to fax resource
                                isTIFF,  // fax file is TIFF
                                faxname, // document file name
                                resHi,   // is high resolution for document
                                issvrq,  // is call user after transmition
                                firstpg, // starts transmition from page
                                pgcount  // how many pages to transmition (default all)
                                );

        if ( state != Hardware.DX_ERROR )
        {
          // operation have started
          // to wait a Dialogic's event
          Object owner = context.getOwner();
          synchronized(owner){owner.wait();}
        } else {
          Hardware.malfunction(handle, "send fax");
        }
        return context.getLastEvent().getReason();
      }catch(Exception e){
        throw new HardwareError( "FAX system, send error:"+e.getMessage() );
      }
    }
    /* to start send fax */
    final static int startSend
                                (
                                int handle,	 // handle to fax resource
                                boolean isTIFF,  // fax file is TIFF
                                String faxname,	 // document file name
                                boolean resHi,	 // is high resolution for document
                                boolean issvrq,	 // is call user after transmition
                                int firstpg,	 // starts transmition from page
                                int pgcount	 // how many pages to transmition (default all)
                                )
    {
    return !valid(handle) ? Hardware.DX_ERROR:
                            FAX.StartSend
                                (
                                handle,	 // handle to fax resource
                                isTIFF,  // fax file is TIFF
                                faxname, // document file name
                                resHi,	 // is high resolution for document
                                issvrq,	 // is call user after transmition
                                firstpg, // starts transmition from page
                                pgcount	 // how many pages to transmition (default all)
                                );// (native)
    }
    /**
     * <action>
     * To get access, how many pages is transferred
     * @param handle device's handle
     * @return pages quantity
     */
    final static int getTransferredPages(int handle)
    {
      return !valid(handle) ? Hardware.DX_ERROR:FAX.GetTransferredPages( handle );// (native)
    }

//********** NATIVES ***********/
        private static native boolean isSupport(int handle);
        private static native int GetTransferredPages( int handle );
        private static native int SetLocalID(int handle,String ID);
        private static native int SetRemoteID(int handle,String ID);
        private static native int SetHeaderInfo(int handle,String ID);
        private static native int SetDateTime(int handle,String ID);
        private static native int SetStartPage(int handle,int page);
        private static native int InitRecieveState(int handle);
        private static native int InitTransmitState(int handle);
        private static native int StartRecieve(int handle, String file, boolean issvrq);
        private static native int StartSend
		                                (
		                                int handle,		 // handle to fax resource
		                                boolean isTIFF,  // fax file is TIFF
		                                String faxname,	 // document file name
		                                boolean resHi,	 // is high resolution for document
		                                boolean issvrq,	 // is call user after transmition
		                                int firstpg,	 // starts transmition from page
		                                int pgcount		 // how many pages to transmition (default all)
		                                );
}
