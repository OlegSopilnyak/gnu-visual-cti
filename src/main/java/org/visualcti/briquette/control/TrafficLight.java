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
package org.visualcti.briquette.control;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import org.jdom.Attribute;
import org.jdom.Element;
import org.visualcti.briquette.*;


/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The semaphore of executing the next briquettes
 * subchain Date/Time constrains</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class TrafficLight extends Operation
{
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "Semaphore.";}
    /**
     * <notify>
     * Chain will notify the Operation after successfully added
     * */
    public void added(){
      if (this.owner.getMainOperation() == null) this.setMaster(true);
    }
    /**
     * <constructor>
     * To make default, all enabled pool
     * */
    public TrafficLight(){
      this.setAbout("Traffic Light for subchain");
    }
/**
 * <const>
 * The name of day of week XML element
 * */
final static String dayOfWeek_XML_name = "enable_for";
/**
 * <const>
 * The name of property
 * */
final static String greenLight_XML_name = "greenLight";
/**
 * <attribute>
 * the pool of the days
 * */
final Pool dowSet = new Pool();
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * */
    protected final void storeRuntimeProperties(Element xml){
        xml.addContent( this.dowSet.getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      this.dowSet.setXML(xml.getChild(greenLight_XML_name));
    }
  /**
   * <flag>
   * Is valid continue subchain's executing
   * */
private boolean greenLight = false;
    /**
     * <action>
     * To stop execute this briquette (do nothing)
     * */
    public final void stopExecute() {}
    /**
     * <action>
     * To execute this operator
     * After execution, method returns the reference
     * to the next executable operator.
     * If null has returned, the Chain (owner) must finish
     * */
    public final Operation doIt(Subroutine caller)
    {
      this.finalized = this.greenLight = false;
      try {
        this.greenLight = this.dowSet.isEnabled( new Date() );
      }catch(Exception e){}
      return this.greenLight ? this.getLink(Operation.DEFAULT_LINK):null;
    }
private static final SimpleDateFormat parser = new SimpleDateFormat( "EEE:HH", Locale.US);
    /**
     * <pool>
     * Class for store the day of week's pool
     * */
    final class Pool{
      private final ArrayList pool = new ArrayList(7);
      private final HashMap index = new HashMap(7);
      private final String names[] = new String[7];
      String[] getNames(){return this.names;}
      /** <mutator> */
      void setEnabled(int day, int hour,boolean enabled){
        try {
          dayOfWeek dow = (dayOfWeek)this.pool.get(day);
          if (enabled) dow.enable(hour);else dow.disable(hour);
        }catch(IndexOutOfBoundsException e){}
      }
      /** <accessor> */
      boolean isEnabled(int day,int hour){
        try {
          return ((dayOfWeek)this.pool.get(day)).isEnabled(hour);
        }catch(IndexOutOfBoundsException e){}
        return false;
      }
      boolean isEnabled(Date now) throws Exception{
        StringTokenizer st = new StringTokenizer(parser.format(now)," :");
        String day = st.nextToken();
        dayOfWeek dow = (dayOfWeek)this.index.get(day);
        String hour = st.nextToken();
        return dow.isEnabled( Integer.parseInt(hour) );
      }
      Pool(){
        Calendar cal = Calendar.getInstance(Locale.US);
        SimpleDateFormat parser = new SimpleDateFormat("EEE", Locale.US);
        for(int i=1;i <= 7;i++){
          cal.add(Calendar.DAY_OF_WEEK,1);
          String dow = parser.format( cal.getTime() );
          dayOfWeek data = new dayOfWeek(dow,cal.get(Calendar.DAY_OF_WEEK));
          this.pool.add( data ); this.index.put( dow, data );
        }
        Collections.sort( this.pool );
        for(int i=0;i < 7;i++) names[i]=((dayOfWeek)this.pool.get(i)).getDay();
      }
      final Element getXML() {
        Element xml = new Element(greenLight_XML_name);
        for(Iterator i=this.pool.iterator();i.hasNext();){
          xml.addContent( ((dayOfWeek)i.next()).getXML() );
        }
        return xml;
      }
      final void setXML(Element xml) {
        if (xml == null) return;
        Iterator i = xml.getChildren( dayOfWeek_XML_name ).iterator();
        while( i.hasNext() ){
          dayOfWeek data = new dayOfWeek( (Element)i.next() );
          int pos = this.pool.indexOf(data);
          if ( pos >= 0 && data.isValid() ){
            this.pool.set(pos,data);
            this.index.put(data.getDay(),data);
          }
        }
      }
    }
    /**
     * <container>
     * The class for represents the hours of day of week
     * */
    final class dayOfWeek implements Comparable{
      private String day = null;
      private Integer ID = new Integer(-1);
      private final ArrayList hours = new ArrayList( 24 );
      /** <constructor> */
      public dayOfWeek(String day,int ID){
        this.ID=new Integer(ID); this.day=day;
        for(int i=0;i < 24;i++) this.hours.add(new Integer(i));
      }
      /** <constructor> */
      public dayOfWeek(Element XML){this.setXML(XML);}
      /** to compare with other day of week, from Comparable */
      public final int compareTo(Object o){
        dayOfWeek other = (dayOfWeek)o;
        return this.ID.compareTo(other.ID);
      }
      /** overrided Object.equals(..) */
      public final boolean equals(Object o){
        if (o == this) return true;
        if (o instanceof dayOfWeek){// other is dayOfWeek
          dayOfWeek other = (dayOfWeek)o;
          return
            other.isValid() && this.isValid() &&
            other.day.equalsIgnoreCase(this.day);
        }else if (o instanceof String){// other is String
          return
            this.isValid() &&
            ((String)o).equalsIgnoreCase(this.day);
        }
        return false;
      }
      /* check is object valid */
      public final boolean isValid(){return this.day != null;}
      /** to get access to day's name */
      public String getDay(){return this.day;}
      /** to enable the hour */
      public void enable(int hour){
        Integer HR = new Integer(hour);
        if ( !this.hours.contains(HR) ) this.hours.add(HR);
      }
      /** to disable the hour */
      public void disable(int hour) {this.hours.remove( new Integer(hour) );}
      /** to check, is hour enabled */
      public boolean isEnabled(int hour) {
        return this.hours.contains(new Integer(hour));
      }
      /** to get day's XML */
      public final Element getXML(){
        Element xml = new Element(dayOfWeek_XML_name);
        xml.setAttribute( new Attribute("day",this.day) );
        if (this.hours.size() > 0)
        {
          int size = this.hours.size();
          StringBuffer buf = new StringBuffer();
          for(int i=1;i < size;i++) {
            buf.append( ", " ).append( this.hours.get(i) );
          }
          Collections.sort( this.hours );
          String set = this.hours.get(0)+buf.toString();
          xml.setText( set );
        }
        return xml;
      }
      /** to setup the day via XML */
      public final dayOfWeek setXML(Element xml){
        this.hours.clear();
        this.day = xml.getAttributeValue("day");
        String set = xml.getTextNormalize();
        StringTokenizer st = new StringTokenizer(set," ,");
        while( st.hasMoreTokens() ){
          String hour = st.nextToken();
          try{this.enable( Integer.parseInt(hour) );
          }catch(NumberFormatException e){}
        }
        Collections.sort( this.hours );
        return this;
      }
    }
    // end dayOfWeek class
}
