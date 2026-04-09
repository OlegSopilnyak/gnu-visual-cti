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
package org.visualcti.briquette.telephony;

import java.util.*;
import java.io.*;
import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.Subroutine;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.server.hardware.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To record the voice from the phone's line</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Record extends Basis
{
/**
<value>
The system symbol
the result of record voice (the voice file's image)
*/
public static final Symbol system_cti_Voice = Symbol.newSystem("cti.Voice", Symbol.VOICE);
/**
<value>
The system symbol
the result of record voice (the duration of voice)
*/
public static final Symbol system_cti_Voice_seconds = Symbol.newSystem("cti.Voice.seconds", Symbol.NUMBER);
/**
 * <pool>
 * The pool of predefined symbols
 */
private static final List predefined = new ArrayList();
/**
 * <init>
 * To initialize predefined Symbols
 */
static{
  predefined.add(system_cti_Voice);
  predefined.add(system_cti_Voice_seconds);
  // voice IO operations resluts
  predefined.add(Symbol.newConst( Reason.IO.TIMEOUT ));
  predefined.add(Symbol.newConst( Reason.IO.FORMAT ));
  predefined.add(Symbol.newConst( Reason.IO.EOF ));
  predefined.add(Symbol.newConst( Reason.IO.DTMF ));
  predefined.add(Symbol.newConst( Reason.IO.SILENCE ));
}
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * @return predefined symbols
   */
  public final List getPredefinedSymbols()
  {
    ArrayList list = new ArrayList(super.getPredefinedSymbols());
    list.addAll(predefined);
    return list;
  }
/**
 * <accessor>
 * To get access to new ID's prefix
 * */
public final String get_ID_prefix(){return "Record.";}
    /**
     * <constructor>
     * */
    public Record(){
      super.setAbout("To record the voice");
      this.initRuntime();
    }
    /** init runtime's properties */
    private final void initRuntime(){
      this.target.setContent( Record.system_cti_Voice );
      this.target.setType( Symbol.VOICE );
      this.target.setFormat( Audio.ULAW_8 );
      this.timeout = Symbol.newConst( new Integer(10) );
      this.silence = Symbol.newConst( new Integer(5) );
      this.DTMF.restore("#");
      this.beepBefore = true;
    }
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( this.target.getXML() );
      xml.addContent( new Property("timeout",this.timeout).getXML() );
      xml.addContent( new Property("silence",this.silence).getXML() );
      xml.addContent( this.DTMF.getXML() );
      xml.addContent( new Property("beepBefore",this.beepBefore).getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.initRuntime(); if ( xml == null ) return;
      // to restore the target
      this.target.setXML( xml.getChild(Media.ELEMENT) );
      // to restore the DTMF
      this.DTMF.setXML( xml.getChild(termination.ELEMENT) );
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 3 );
      // to iterate the properties of operation
      while( i.hasNext() )
      {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          // check the property's name
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of runtime properties!");
          // to solve the property by name
          if ( "timeout".equals(name) )    {
              this.timeout = property.getValue(this.timeout); names.add(name);
          }else
          if ( "silence".equals(name) )    {
              this.silence = property.getValue(this.silence); names.add(name);
          }else
          if ( "beepBefore".equals(name) )     {
              this.beepBefore = property.getValue(this.beepBefore); names.add(name);
          }
      }
    }
  /**
   * <main>
   * Main method of Operation
   * */
  protected void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_RECD);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");
    // to prepare the call
    // prepare the target's streams & format

    // to prepare the target's output stream
    this.target.setFormat( device.getRecordFormat() );
    OutputStream out = null;
    try {out = this.target.getOutputStream(caller);
    }catch(Exception e){
      caller.set(Basis.system_cti_Operation_Result, Reason.IO.FORMAT);
      return;
    }

    // to prepare the timeout
    int timeout = 10;
    try{timeout = ((Number)caller.get(this.timeout)).intValue();
    }catch(Exception e){}

    // to prepare the silence
    int silence = 5;
    try{silence = ((Number)caller.get(this.silence)).intValue();
    }catch(Exception e){}

    // to prepare the DTMF
    String dtmfString = this.DTMF.toString();

    // to execute the beep, if needs
    if ( this.beepBefore ) device.playTone( Tone.BEEP );

    // main method
    String result = device.record(out,dtmfString,silence,timeout);
    // to store the data
    try{out.close();}catch(Exception e){}

    caller.set(Basis.system_cti_Operation_Result,result);
  }
//////////////////////////////////
/////////// PROPERTIES ///////////
//////////////////////////////////
/**
* <attribute>
* The container for sound's data
* */
private final VoxTarget target = new VoxTarget();
  /**
   * <accessor>
   * To get access to record target's value
   * @return the value
   */
  public final VoxTarget getTarget() {return target;}
/**
 * <attribute>
 * The time to record
 * */
private Symbol timeout;
  /**
   * <mutator>
   * To setup timeout's value
   * @param timeout new value
   */
  public final void setTimeout(Symbol timeout) {this.timeout = timeout;}
  /**
   * <accessor>
   * To get access to timeout's value
   * @return the value
   */
  public final Symbol getTimeout() {return timeout;}
/**
 * <attribute>
 * The max time of silence to terminate record
 * */
private Symbol silence;
  /**
   * <mutator>
   * To setup the silence's value
   * @param silence new value
   */
  public final void setSilence(Symbol silence) {this.silence = silence;}
  /**
   * <accessor>
   * To get access to silence's value
   * @return the value
   */
  public final Symbol getSilence() {return silence;}
/**
 * <attribute>
 * The DTMF's set to terminate the record
 * */
private final termination DTMF=new termination();
  /**
   * <accessor>
   * To get access to DTMF's termination
   * @return the termination
   */
  public final termination getDTMF() {return DTMF;}
/**
 * <attribute>
 * Flag, is needs the beep before begin record
 * */
private boolean beepBefore;
  /**
   * <mutator>
   * To setup the flag's value
   * @param beepBefore new value
   */
  public final void setBeepBefore(boolean beepBefore) {this.beepBefore = beepBefore;}
  /**
   * <accessor>
   * To get access to flag's value
   * @return the value
   */
  public final boolean isBeepBefore() {return beepBefore;}
}
