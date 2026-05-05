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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.core.VoxSource;
import org.visualcti.briquette.core.termination;
import org.visualcti.media.Audio;
import org.visualcti.server.hardware.Reason;
import org.visualcti.server.hardware.Tone;
import org.visualcti.server.hardware.proxy.deviceProxy;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To play the sound from some sources</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class PlayAction extends Basis
{
/**
<value>
The system symbol
the result of record voice (the voice file's image)
*/
public static final Symbol system_cti_Voice = Symbol.newSystem("cti.Voice", Symbol.VOICE);
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
  // voice IO operations resluts
  predefined.add(Symbol.newConst( Reason.IO.TIMEOUT ));
  predefined.add(Symbol.newConst( Reason.IO.FORMAT ));
  predefined.add(Symbol.newConst( Reason.IO.EOF ));
  predefined.add(Symbol.newConst( Reason.IO.DTMF ));
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
public final String get_ID_prefix(){return "Play.";}
    /**
     * <constructor>
     * */
    public PlayAction(){
      super.setAbout("To play sounds chain");
      this.initRuntime();
    }
/**
 * <attribute>
 * Is needs the signal (beep) after playing
 * */
private boolean beep = false;
/**
 * <accessor>
 * Is beep after play
 * */
public final boolean isBeep(){return this.beep;}
/**
 * <mutator>
 * To set the beep's flag
 * */
public final void setBeep(boolean beep){this.beep=beep;}
/**
 * <attribute>
 * The timeout of play
 * */
private Symbol timeout = Symbol.newConst( new Integer(-1) );
/**
 * <accessor>
 * To get access to play's timeout
 * */
public final Symbol getTimeout() {return this.timeout;}
/**
 * <mutator>
 * To setup a new timeout's value
 * */
public final void setTimeout(Symbol timeout) {this.timeout = timeout;}
/**
 * <attribute>
 * The termination's Mask
 * */
private final termination DTMF = new termination();
/**
 * <accessor>
 * To get access to termination's Set
 * */
public final termination getDTMF(){return this.DTMF;}
/**
 * <attribute>
 * The list of voice's sources
 * */
private final List sources = new ArrayList();
/**
 * <accessor>
 * To get access to list of media sources
 * */
public final List getVoxSources(){return sources;}
/**
 * <mutator>
 * to update voice source by index
 * */
public final void updateVoxSource(VoxSource source, int index)
{
  synchronized(this.sources){this.sources.set(index,source);}
}
/**
 * <mutator>
 * to add voice source
 * */
public final void addVoxSource(VoxSource source)
{
  synchronized(this.sources){this.sources.add(source);}
}
/**
 * <mutator>
 * to remove voice source
 * */
public final void removeVoxSource(int index)
{
  synchronized(this.sources){this.sources.remove(index);}
}
/**
 * <mutator>
 * To move up the voice source by index
 * */
public final void moveVoxSourceUp(int index){
  if (index <= 0) return;
  synchronized(this.sources)
  {
    Object entry = this.sources.remove(index);
    if (entry != null) this.sources.add(index-1,entry);
  }
}
/**
 * <mutator>
 * To move down the voice source by index
 * */
public final void moveVoxSourceDown(int index){
  synchronized(this.sources)
  {
    if (index < 0 || index >= this.sources.size()) return;
    Object entry = this.sources.remove(index);
    if (entry != null) this.sources.add(index+1,entry);
  }
}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent( new Property("beep",this.beep).getXML() );
      xml.addContent( new Property("timeout",this.timeout).getXML() );
      xml.addContent( this.DTMF.getXML() );
      Element chain = new Element("sources_chain");
      synchronized(this.sources){
        for(Iterator i = this.sources.iterator();i.hasNext();)
        {
          VoxSource source = (VoxSource)i.next();
          chain.addContent( source.getXML() );
        }
      }
      xml.addContent(chain);
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      // to clear a runtime properties
      this.initRuntime();
      // to check of XML's integrity
      if (xml == null) return;
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 2 );
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
          if ( "beep".equals(name) )    {
              this.beep = property.getValue(this.beep); names.add(name);
          }else
          if ( "timeout".equals(name) )     {
              this.timeout = property.getValue(this.timeout); names.add(name);
          }
      }
      this.DTMF.setXML( xml.getChild(termination.ELEMENT) );
      Element chainXML = xml.getChild("sources_chain");
      if ( chainXML == null) return;
      for(i=chainXML.getChildren(VoxSource.ELEMENT).iterator();i.hasNext();)
      {
        VoxSource source = new VoxSource();
        Element srcXML = (Element)i.next();
        source.setXML( srcXML );
        this.sources.add( source );
      }
    }
    /**
     * sto /ii
     * <init>
     * To init runtime's parameters
     * */
    private final void initRuntime(){
      this.beep = false;
      this.timeout = Symbol.newConst(new Integer(-1));
      this.DTMF.restore("1,2,3,4,5,6,7,8,9,0,#,*");
      this.sources.clear();
    }
  /**
   * <main>
   * The main method of telephony briquettes
   * */
  protected void telephonyMethod(deviceProxy device, Subroutine caller)
  {
    caller.set(Basis.system_cti_Operation,deviceProxy.DS_PLAY);
    caller.set(Basis.system_cti_Operation_Result,"Progress...");
    // to prepare the call
    String dtmfSet = this.DTMF.toString();
    int timeout = -1;// the play's timeout
    // to get values from the Subroutine's Pool
    try{timeout = ((Number)caller.get(this.timeout)).intValue();
    }catch(Exception e){}
    // to play the sounds chain
    String reason = this.play(caller,device,timeout,dtmfSet);
    // to beep after playing, if play completed with EOF
    if ( this.beep && Reason.IO.EOF.equals(reason) ) device.playTone(Tone.BEEP);
    // to store result to Subroutine's pool
    caller.set(Basis.system_cti_Operation_Result,reason);
    caller.info("Play complete by reason "+reason);
  }
    /**
     * <player>
     * To play the defined list of Sound sources during the timeout
     * @param timeout max time to play (-1 is not timeout)
     * @param mask DTMF's termination's mask
     * @return completion's reason
     */
    private final String play
                          (
                          Subroutine caller,
                          deviceProxy device,
                          int timeout,
                          String mask
                          ){
      // to make the list of valid formats
      List valid = Arrays.asList( device.canPlay() );
      if ( valid.size() == 0 ) return Reason.IO.FORMAT;
      // to init the result
      String reason = Reason.IO.EOF;
      long mark = System.currentTimeMillis();
      // to execute a telephony method (chain's play)
      for(Iterator i=this.sources.iterator();i.hasNext();){
        // To get & adjust the next play's source
        VoxSource source = (VoxSource)i.next();
        source.setOwner( caller );
        source.setDefaultFormat( device.getRawFormat() );
        try {
          // get source's stream for play (may throw IOException)
          InputStream in = source.getInputStream();
          // to solve the format of source
          Audio format = source.getFormat();
          if ( valid.contains(format) )
          { // if format compatible, play the stream
            reason = device.play( in, mask, timeout, format );
            // to close the sound's data stream
            try {in.close();}catch(Exception e){}
          } else
            // unsupported sound's format
            reason = Reason.IO.FORMAT;
        }catch (IOException e){
          e.printStackTrace();
        }
        // check, is completion is EOF?
        if ( !Reason.IO.EOF.equals(reason) ) return reason;
        else
        // to check the timeout
        if ( timeout != deviceProxy.NOTIMEOUT ) {
          long duration = System.currentTimeMillis() - mark;
          mark = System.currentTimeMillis();
          if ( (timeout*1000) > duration ) {
            timeout = (int) ((timeout*1000-duration)/1000);
          }else return Reason.IO.TIMEOUT;
        }
      }
      return reason;
    }
}
