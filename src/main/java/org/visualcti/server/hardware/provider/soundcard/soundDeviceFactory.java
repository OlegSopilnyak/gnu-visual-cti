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
package org.visualcti.server.hardware.provider.soundcard;

import org.visualcti.server.hardware.provider.stubDeviceFactory;
import org.visualcti.media.*;
import org.visualcti.server.*;
import org.jdom.*;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * The factory of sound's devices</p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class soundDeviceFactory extends stubDeviceFactory
{
  public static void main(String[]args){
    soundDeviceFactory factory = new soundDeviceFactory();
    soundDevice device = (soundDevice)factory.devices()[0];
    device.show();
    try{
      device.open();String result=
      device.waitForCall(2,30,true);
      System.out.println("Result is :"+result);
      FileInputStream in = new FileInputStream("./VM/prompts/GET_REVIEW_SUBJECT.WAV");
      Sound
      sound = mediaParser.getSound(in,Audio.LINEAR);
      result = device.play(sound.getInputStream(),"*",-1,sound.getFormat());
      System.out.println("Result is :"+result);
      result = device.makeCall("1111",30);
      System.out.println("Result is :"+result);
      delay(500);
      result = device.makeCall("2222",30);
      System.out.println("Result is :"+result);
      delay(500);
      in = new FileInputStream("./VM/prompts/GET_REVIEW_SUBJECT.WAV");
      sound = mediaParser.getSound(in,Audio.LINEAR);
      result = device.play(sound.getInputStream(),"#",-1,sound.getFormat());
      System.out.println("Result is :"+result);
      result = device.makeCall("3333",30);
      System.out.println("Result is :"+result);
      delay(500);
      result = device.record(new ByteArrayOutputStream(),"#",-1,5);
      System.out.println("record Result is :"+result);
      result = device.makeCall("4444",30);
      System.out.println("Result is :"+result);
      delay(500);
      result = device.makeCall("5555",30);
      System.out.println("Result is :"+result);
      delay(500);
      device.close();
    }catch(Exception e){
      e.printStackTrace();
    }
    System.exit(0);
  }
  private final static void delay(int sleep){
    try{Thread.sleep(sleep);}catch(Exception e){}
  }
soundPlayer player = null;
soundRecorder recorder = null;

  public soundDeviceFactory()
  {
    try{
      System.out.println("Prepare the sound system...");
      Vector devices = (Vector) CaptureDeviceManager.getDeviceList(null).clone();
      CaptureDeviceInfo cdi = this.findAudioDevice(devices);
      if ( cdi != null ){
        MediaLocator locator = cdi.getLocator();
        this.playWelcome();
        DataSource src = Manager.createDataSource( cdi.getLocator() );
        //if ( src instanceof CaptureDevice) {this.recorder = src;}
        src.disconnect(); src.stop(); src = null; System.gc();
        this.player = new soundPlayer( cdi.getFormats()[0], locator );
        this.recorder = new soundRecorder( cdi.getFormats()[0], locator );
        System.out.println("SoundCard installed...");
      }else
        System.out.println("No SoundCard found...");
      // to make and register the soundDevice
      super.appendDevice( new soundDevice(this) );
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  /**
   * Try to play ./soundcard.welcome.wav
   * @throws Exception if some wrong during playback...
   */
  private final void playWelcome() throws Exception{
    File welcome = new File("./soundcard.welcome.wav");
    if ( !welcome.exists() ) return;
    final Object Semaphore = new Object();
    Player player = Manager.createRealizedPlayer( welcome.toURL() );
    ControllerListener control = new ControllerAdapter(){
      public final void stop(StopEvent e){this.wakeup();}
      public final void controllerError(ControllerErrorEvent e){this.wakeup();}
      private final void wakeup(){synchronized(Semaphore){Semaphore.notify();}}
    };
    player.addControllerListener(control); player.start();
    System.out.println("Try to play welcome...");
    synchronized(Semaphore){try{Semaphore.wait();;}catch(Exception e){}}
    player.stop(); player.deallocate(); player = null; System.gc();
  }
  private final CaptureDeviceInfo findAudioDevice(Vector devices){
    for(Enumeration e=devices.elements();e.hasMoreElements();){
      CaptureDeviceInfo cdi = (CaptureDeviceInfo)e.nextElement();
      Format[] formats = cdi.getFormats();
      for(int i=0;i < formats.length;i++)
        if ( formats[i] instanceof AudioFormat ) return cdi;
    }
    return null;
  }
  /**
   * <restore>
   * To restore factory's configuration
   * @param xml the factory's configuration
   */
  protected final void processConfiguration(org.jdom.Element xml){}
  /**
   * <accessor>
   * To get access to Factory's vendor name
   * @return the vendor's name
   */
  public final String getVendor(){return "JMF";}
  /**
   * <accessor>
   * To get access to Factory's version
   * @return the version
   */
  public final String getVersion(){return "3.2";}
}
