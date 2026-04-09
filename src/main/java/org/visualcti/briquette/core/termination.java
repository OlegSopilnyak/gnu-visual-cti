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
package org.visualcti.briquette.core;

import java.util.*;
import org.jdom.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow representation of DTMF's termination</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class termination {
public static final String ELEMENT = "set";
  /**
   * <constructor>
   * */
  public termination(){}
  /**
   * <constructor>
   * */
  public termination(String set){
    this(); this.restore(set);
  }
/**
 * <attribute>
 * Store of signals Set
 * */
private final ArrayList signals = new ArrayList();
  /**
   * <accessor>
   * To check, is signal is On
   * */
  public final boolean isSignalOn(String signal)
  {
    synchronized(this.signals)
    {
      return this.signals.contains(signal);
    }
  }
  /**
   * <mutator>
   * To set signal off/on
   * */
  public final void setSignal(String signal,boolean enable)
  {
    if (enable) this.signalOn(signal);else this.signalOff(signal);
  }
  /**
   * <mutator>
   * To add signal
   * */
  public final void signalOn(String signal)
  {
    if ( signal == null ) return;
    synchronized(this.signals)
    {
      if ( !signals.contains(signal) ) signals.add(signal);
    }
  }
  /**
   * <mutator>
   * To remove the signal
   * */
  public final void signalOff(String signal)
  {
    if ( signal == null ) return;
    synchronized(this.signals)
    {
      signals.remove(signal);
    }
  }
  /**
   * <translator>
   * To translate the signals set to XML's Element
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    xml.setAttribute( new Attribute("type","DTMF") );
    synchronized(this.signals)
    {
      Collections.sort( this.signals );
      xml.setAttribute( new Attribute("value",this.toString()) );
    }
    return xml;
  }
  /**
   * <translate>
   * To translate XML's element to signals set
   * */
  public final void setXML(Element xml) throws Exception
  {
    this.signals.clear();
    if (xml == null || !ELEMENT.equals(xml.getName())) return;
    String type = xml.getAttributeValue("type");
    if ( !"DTMF".equals(type) ) return;
    this.restore( xml.getAttributeValue("value") );
  }
  /** to restore the set from string */
  public final void restore(String set){
    this.signals.clear();
    if (set == null) return;
    StringTokenizer st = new StringTokenizer(set,", ");
    synchronized(this.signals){
      while( st.hasMoreTokens() ) this.signalOn( st.nextToken() );
      Collections.sort( this.signals );
    }
  }
  /**
   * <translator>
   * To represent the termination as String
   * */
  public final String toString()
  {
    StringBuffer sb = new StringBuffer();
    Iterator i=this.signals.iterator();
    if (i.hasNext()) sb.append( i.next() );
    while( i.hasNext() ){
      sb.append(", ").append( i.next() );
    }
    return sb.toString();
  }
}
