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

import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * Class for manage of multithread connection</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */
class Hardware implements HardwareConst {
/**
 * Const for error state return
 */
public static final int DX_ERROR = -1;
/**
 * Const of hook state (onhook)
 */
public static final int DX_ONHOOK = 0;
/**
 * Const of hook state (offhook)
 */
public static final int DX_OFFHOOK = 1;
/**
 * <flag>
 * flag to print debug information
 */
private static boolean DEBUG = false;
/**
 * <flag>
 * flag to print error information
 */
private static boolean ERROR = true;
    /**
     * <action>
     * to process hardware's malfunction
     * throws HardwareError with full inforamtion about error
     * @param handle the handle of corrupted device
     * @param action the name of action (Play/Record...)
     */
    static void malfunction(int handle,String action)
    {
      String errorDescription = "can't to start "+action+" for handle "+handle;
      errorDescription += "\ndriver says ["+Hardware.getErrorMessage(handle)+"]";
      throw new HardwareError(errorDescription);/* throw error */
    }
    /**
     * <print>
     * To print debug message if flagged
     * @param message the message to print
     */
    static void debug(Object message) {
        if (Hardware.DEBUG) System.out.println( message );
    }
    /**
     * <print>
     * To print error message if flagged
     * @param message the message to print
     */
    static void error(Object message){
        if (Hardware.ERROR) System.out.println( "ERROR:"+message );
    }
    /**
     * <adjust>
     * to disable all event generation for handle
     * @param handle the handle for device
     */
    static void disableEvt(int handle) {
      if ( Context.get(handle) != null ) {
        Hardware.disableEvents( handle );// (native)
        Hardware.debug("Hardware:disabled events for "+handle);
      }
    }
    /**
     * <adjust>
     * to enable some event generation for handle
     * @param handle handle to device
     * @param what what events enable
     */
    static void enableEvt(int handle,String what){
      if ( Context.get(handle) != null ) {
        Hardware.enableEvents( handle, what );// (native)
        Hardware.debug("Hardware:enabled events ["+what+"] for "+handle);
      }
    }
    /**
     * <translator>
     * To make the reason of completion of command
     * @param handle device's handle
     * @param normal normal reason
     * @return the reason
     */
    final static String reason(int handle,String normal)
    {
      Context context = Context.get(handle);
      // to check the handle
      if ( context == null ) return Reason.ERROR;
      // to check the disconnect
      if ( context.isDisconnected() ) return Reason.CALL.DISCONNECT;
      // to check the termination
      if ( context.isTerminated() ) return Reason.TERMINATED;
      return normal;
    }
    /**
     * <solve>
     * to resolve termination reason
     * @param handle the handle to device
     * @return reason of termination
     */
    final static String termReason(int handle)
    {
      Context context = Context.get(handle);// get context for handle
      String reason = context.getLastEvent().getReason();
      if ( context.isDisconnected() || Reason.CALL.DISCONNECT.equals(reason))
      {   // reason for detected disconnect
        Hardware.setHook(handle, Hardware.DX_ONHOOK);// request to drop call (native)
        context.setDisconnected();// disable all I/O operations
        return Reason.CALL.DISCONNECT;// port disconnected
      }
      return reason;
    }
        /**
         * <notify>
         * to notify suspended thread (during event processing)
         * @param owner who is wait a notify
         */
        private final static void wakeup(Object owner){
            synchronized(owner){owner.notify();}/* wake up owner */
            debug("Hardware: notified owner:"+owner);
        }
        /**
         * <solve>
         * Method for resolve Dialogic's events
         * @param event the event to solve
         */
        private final static void resolveEvent(dialogicEvent event){
            int deviceHandle = event.getHandle();
            // To get the context for device's handle
            Context context = Context.get( deviceHandle );
            if (context == null) return;// invalid handle
            context.setLastEvent( event );
            Object owner = context.getOwner();

                // Switch according to the event received.
	        switch( event.getEventID() )
                {
		        case TDX_CST:               /* Call Status Transition */
		            Hardware.debug("Accepted CST event "+event);
		            if ( Reason.CALL.DISCONNECT.equals(event.getReason()) )
		            {
		                context.setDisconnected();
		                Hardware.stopAll( deviceHandle ); // stop all activity (native)
		            }
                            break;

		        case TDX_PLAY:               /* Play Completed       */
		        case TDX_RECORD:             /* Record Completed     */
		            Hardware.wakeup( owner );
                            break;

		        case TDX_GETDIG:             /* Get Digits Completed */
		            Hardware.wakeup( owner );
                            break;

		        case TDX_CALLP:		/* Make call complete */
		            Hardware.wakeup( owner );
                            break;

		        case TDX_PLAYTONE:			/* Play tone complete */
		        case TDX_DIAL:				/* Dial without Call Analysis complete */
		            Hardware.wakeup( owner );
                            break;

		        case TDX_SETHOOK:            /* Set-Hook Complete    */
		            context.setHookState( event.getReasonID() );
		            Hardware.wakeup( owner );
                            break;

                case TFX_FAXRECV:  /* The document has been successfully received. */
		            Hardware.wakeup( owner );
                    /*
                    printf("Received %ld pages at speed %ld, resln %ld,width %ld\n",
						        ATFX_PGXFER(event->getHandle()),
						        ATFX_SPEED(event->getHandle()),
						        ATFX_RESLN(event->getHandle()),
						        ATFX_WIDTH(event->getHandle())
						        );
			        {
				        Context *context = Context::getContext( event->getHandle() );
				        if (context != NULL) {
					        close(context->fiott.io_fhandle);
					        event->setTermReason( 0 );// reason
					        event->setTermReason( CTERM_EOF );
				        } else {// invalid source handle
					        event->setTermReason( DX_ERROR );// reason - error
					        event->setTermReason( "ERROR" ); // reason - error(text)
				        }
			        }
			        */
                            break;

		        case TFX_FAXSEND:	// Fax sending done
		            Hardware.wakeup( owner );
		            /*
			        {
				        Context *context = Context::getContext( event->getHandle() );
				        if (context != NULL) {
					        close(context->fiott.io_fhandle);
					        event->setTermReason( 0 );// reason
					        event->setTermReason( CTERM_EOF );
				        } else {// invalid source handle
					        event->setTermReason( DX_ERROR );// reason - error
					        event->setTermReason( "ERROR" ); // reason - error(text)
				        }
			        }
			        */
			    break;

                case TFX_FAXERROR:/* Error during the fax session. */
		            Hardware.wakeup( owner );
                    /*
                    printf("Phase E status %d\n", ATFX_ESTAT(event->getHandle()));
			        {
				        int handle = event->getHandle();
				        Context *context = Context::getContext( handle );
				        if (context != NULL) {
					        close(context->fiott.io_fhandle);
					        dialogicFaxChannel::getLastError(handle, event);
				        }
			        }
			        */
			    break;

		        case DTEV_SIG:               /* DTI signalling event */
		            Hardware.wakeup( owner );
        //			dialogicDevice::process_DTI_Event( event );
			    break;

		        case TDX_ERROR:				// Error detected
		            Hardware.wakeup( owner );
		            /*
			        {int handle = event->getHandle();
			        event->setTermReason( DX_ERROR );// reason - error event
			        sprintf(
					        ::errorMessage,
					        "error on channel %d [%s] system (%s)",
					        handle,
					        ATDV_ERRMSGP(handle),
					        strerror(errno)
					        );
			        event->setTermReason( ::errorMessage );// store translated error message
			        }
			        */
			    break;

		        default:
		        // nothing
		        /*
		        * Unexpected or Error Termination Event
		        */
        //			printf("C:> Unexpected or Error Termination Event %d for %d\n",event->getEventID(),event->getHandle());
	        }

        }
/**
 * Thread for getting and resolving dialogic events
 */
private static final class eventsProcessor extends Thread
{
    private eventsProcessor()
    {
      super("Dialogic events scaner");
      super.setDaemon( true );
      super.setPriority( Thread.MIN_PRIORITY );
    }
    /* main loop */
    public final void run(){
        Hardware.debug("Hardware: Started Dialogic's events processor...");
        while(true)
        {
            super.yield();// transfer context to other threads
            dialogicEvent event = Hardware.getEvent();
            Hardware.debug( "Received the EVENTS:"+event );
            Hardware.resolveEvent( event );
        }
    }
}
/*
#
# Static section executed before all
#
*/
static
{
    System.out.print("Starting Dialogic's hadware support");
    // to start & init hardware systems
    try {
      // try to load native library
      System.loadLibrary("dialogic");
      System.out.print("..");System.out.flush();
      // to collect information about devices and start events processor
      Hardware.init();
      System.out.print("..");System.out.flush();
      // to start the Calls Control Manager
      CCM.init();
      System.out.print("..");System.out.flush();
    }catch(Throwable t){
      Hardware.error(t);
      Hardware.error("cannot start because of "+t.getMessage());
      System.exit(1);
    }
    System.out.println(" Done.");
}

        /**
         * to start events processor (from native)
         */
        final private static void initEvents(){
            Hardware.debug("\nHardware: from native call init events scaner");
            new Hardware.eventsProcessor().start();
        }
///////////////////////////////////////////

    /**
     * <action>
     * to open dialogic channel and register owner
     * @param name the name of the channel
     * @param owner the owner of the channel
     * @return valid handle or DX_ERROR
     */
    final static int openChannel(String name, Object owner)
    {
      debug("Hardware: try to open "+name+" for "+owner);
      int handle = Hardware.DX_ERROR;
      synchronized (Hardware.class){
        // try to open Dialogic resource by name (native)
        if ( (handle = Hardware.open(name)) == DX_ERROR) return DX_ERROR;
      }
      debug("Hardware: opened "+name+" in handle "+handle);
      Context context = null;
      // try to make the context for handle
      try{
        context=Context.init(handle,owner,name);
      }catch (Exception e){}
      if ( context == null )
      {// context not maked :-(
        error("openChannel:Can't make context for "+handle);
        // to close device's handle (native)
        synchronized (Hardware.class) {Hardware.close(handle);}
        return Hardware.DX_ERROR;
      }
      // to return the valid handle to the device
      return handle;
    }
    /**
     * <action>
     * to open dialogic's fax channel and register owner
     * @param name device's name
     * @param owner the owner of the device
     * @return DX_ERROR if some wrong
     */
    final static int openFaxChannel(String name, Object owner){
      debug("Hardware: try to open fax-device "+name+" for "+owner);
      int handle = Hardware.DX_ERROR;
      synchronized (Hardware.class) {
        // try open Dialogic's fax-resource by name
        if ( (handle = Hardware.openFax(name)) == DX_ERROR) return DX_ERROR;
        if ( !FAX.isResource_FAX(handle) ) {
          Hardware.close(handle); return Hardware.DX_ERROR;
        }
      }
      Context context = null;
      // try to make context for handle
      try{
        context=Context.init( handle, owner, name);
      }catch (Exception e){}
      if ( context == null )
      {// context not maked
        error("openFaxChannel:Can't make context for "+handle);
        // to close the device's handle
        synchronized (Hardware.class){Hardware.close(handle);}
        return Hardware.DX_ERROR;
      }
      // to return the valid handle to the device
      return handle;
    }
    /**
     * <action>
     * to close dialogic's device and unregister owner
     * @param handle device's handle
     */
    final static void closeChannel(int handle)
    {
      debug("Hardware: close handle "+handle);
      if (Context.get(handle) != null)
      {
        // to unregister handle's context
        Context.free( handle );
        // to close device's handle (native)
        synchronized (Hardware.class){Hardware.close(handle);}
      }
    }
    /**
     * <action>
     * To terminate device's activity
     * @param handle device's handle
     */
    final static void terminate(int handle)
    {
      debug("Hardware: request to termination for "+handle);
      Context context;
      if ( (context=Context.get(handle)) != null )
      {
        context.setTerminated(true);
        // to stop any device's operations (native)
        Hardware.stopAll( handle );
        debug( "Terminated device "+context.getHandleName() );
      }
    }
    /**
    to get access to last error message of the handle
    */
    /**
     * <accessor>
     * To get access to last error message of device
     * @param handle device's handle
     * @return error's message
     */
    final static String getErrorMessage(int handle)
    {
      if ( Context.get( handle ) == null ) return "VisualCTI System Error";
      // to get Dialogic's error message (natiev)
      return Hardware.errorMessage( handle );
    }
//**************** NATIVE METHODS begin  ****************/
    /*<native> to get access to the device names list */
    static native String getNamesList();
        /*<native> method for init dialogic events service */
        private static native void init();
        /*<native> to disable all events generation for handle */
        private static native void disableEvents( int handle );
        /*<native> to enable some event generation for handle */
        private static native void enableEvents( int handle, String what );
        /*<native> to check is incoming call alerted */
        static native boolean isCallAlerted(int handle);
        /*<native> to get the CallerID */
        static native String getCallerID(int handle);
        /*<native> to start make outgoing call */
        static native int call(int handle, String number, int timeout);
        /*<native> method to open dialogic channel by name */
        private static native int open(String name);
        /*<native> method to open dialogic fax channel by name */
        private static native int openFax(String name);
        /*<native> method to close dialogic channel by handle */
        private static native void close(int handle);
        /*<native> method to accept a dialogic's event */
        private static native dialogicEvent getEvent();
        /*<native> method to start change hook state */
        static native int setHook(int handle, int state);
        /*<native> method for stop all device's I/O activity */
        private static native void stopAll(int handle);
        /*<native> method for access to last error message (for print) */
        private static native String errorMessage( int handle );
/**************** NATIVE METHODS end  ****************/

        //// functions defined as call back for generate name & signature
        //private static native byte[] getBuffer_n(int handle,int length);
        //private static native int readData_n(int handle,byte[] data,int pos,int size);
        //private static native int writeData_n(int handle,byte[] data ,int pos,int size);
        //private static native void lessData_n(int handle, int offset);
        //private static native void initEvents_n();
        //// functions defined as call back

/************  NOTIFY from native begin  *****************/
        /**
         * <callback>
         * To receive the reference to the buffer of an necessary length
         * called from callback write method
         * @param handle device's handle
         * @param length the size of buffer
         * @return the bytes array
         */
        private final static byte[] getBuffer(int handle, int length) {
            try {
              return Context.get(handle).getBuffer();
            }catch(NullPointerException e){}
            return null;
        }
        /**
         * <callback>
         * Reading given from a input stream,
         * Associated with handle.
         * It's called from the callback play call
         * @param handle device's handle
         * @param data I/O buffer
         * @param pos the begin position in the buffer
         * @param size needs bytes
         * @return the count of really readed bytes or DX_ERROR if some wrong
         */
        private static final int readData(int handle,byte[] data,int pos,int size){
          try{
            int count = Context.get(handle).read(data, pos, size);
            return count == -1 ? 0:count;
          } catch(Exception e){
            e.printStackTrace();
          }
          return DX_ERROR;
        }
        /**
         * <callback>
         * To write the data to output stream,
         * Associated with handle.
         * It's called from record callback call
         * @param handle device's handle
         * @param data I/O buffer
         * @param pos the begin position in the buffer
         * @param size needs bytes
         * @return 1 if success or DX_ERROR
         */
        private static final int writeData(int handle,byte[] data,int pos,int size){
          try {
            Context.get(handle).write(data, pos, size);
            return 1;
          }catch(Exception e){}
          return DX_ERROR;
        }
        /**
         * <callback>
         * To truncate last writed buffer
         * the offset must be less 0
         * It's called from record callback call
         * @param handle device's handle
         * @param offset the offset
         */
        private static final void lessData(int handle,int offset) {
          try{Context.get(handle).seek(offset);}catch(Exception e){}
        }
/************  NOTIFY from native end  *****************/
}
