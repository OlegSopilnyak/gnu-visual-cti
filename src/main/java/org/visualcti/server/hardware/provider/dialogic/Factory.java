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

Contact oleg@visualcti.org or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg@visualcti.org
Home Phone:	380-62-3851086 (russian)

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

import org.jdom.*;
import org.visualcti.media.*;
import org.visualcti.server.*;
import org.visualcti.server.hardware.*;
import org.visualcti.server.hardware.provider.*;
import org.visualcti.server.hardware.proxy.*;
import org.visualcti.util.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The factory for a Dialogic's devices</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

public final class Factory extends stubDeviceFactory
{
public static final String VENDOR = "Dialogic";
public static final String VERSION= "Prominic ver. 3.01(alfa)";
public final static String defaultURL = "file:./dialogic.configuration.xml";
/**
 * <attribute>
 * The URL to factory's configuration
 */
private String configurationURL = Factory.defaultURL;
/**
 * <attribute>
 * The container of factory's properties
 */
private Element configurationXML = Tools.emptyXML;
/**
 * <attribute>
 * Default properties for the device
 */
final devParams properties = new devParams();
/**
 * <flag>
 * Is need to save configuration
 */
private volatile boolean isSave = false;
  /**
   * <constructor>
   * Factory's constructor
   */
  public Factory()
  {
    for(Iterator i=Factory.factoryNames.iterator();i.hasNext();)
    {
      // to make & register the Dialogic's device
      super.appendDevice( new Device((String)i.next(),this) );
    }
  }
  /**
   * <restore>
   * To restore factory's configuration
   * @param xml the factory's configuration
   */
  protected final void processConfiguration(Element xml){
    if ( super.isStarted() ) return;// need to stop the factory
    // to clear devices pools
    this.initFactory();
    // to check the XML
    if ( xml == null || !deviceFactory.ELEMENT.equals(xml.getName()) ) return;
    // to solve xml's parameters
    try{ this.solveParameters(xml); }catch(Exception e){}
    if (
        !Factory.VENDOR.equalsIgnoreCase( this.vendor ) ||
        this.configurationURL == null ||
        (this.configurationXML=Tools.xmlLoad(this.configurationURL)) == Tools.emptyXML
        ) return;
    // to prepare devices iterations
    xml = this.configurationXML;this.isSave = false;
    // to restore Factory device's default properties
    this.properties.restore( xml.getChild("default") );
    // to make the devices & configure it
    for(Iterator i=xml.getChildren(deviceProxy.ELEMENT).iterator();i.hasNext();){
      // to make the wrappers for defined channels & configure it
      Element XML = (Element)i.next();
      String name = XML.getAttributeValue("name");
      generalDeviceProxy device = super.getDevice(name);
      if ( device != null ) device.configure(XML);
    }
    // check is need to save the configuration
    if ( this.isSave ) return;
  }
    /* to init the factory, close all devices */
    private final void initFactory(){
      //super.init();
      configurationURL=Factory.defaultURL;
      configurationXML=null;
      this.properties.restore( null );
    }
/**
 * <attribute>
 * Temporary attribut for solve the vendor
 */
private String vendor = Factory.VENDOR;
    /* to solve the factory's parameters */
    private final void solveParameters(Element xml) throws Exception{
      for(Iterator i=xml.getChildren(Parameter.ELEMENT).iterator();i.hasNext();){
        Parameter param = Parameter.restore((Element)i.next());
        String parName = null;
        if ( param == null || (parName=param.getName()) == null) continue;
        String value = param.getStringValue();
        if ( parName.equalsIgnoreCase("vendor") )
          this.vendor = value;
        else
        if ( parName.equalsIgnoreCase("url") )
          this.configurationURL = value;
        else continue;
      }
    }
  /**
   * <accessor>
   * To get access to Factory's vendor name
   * @return the vendor's name
   */
  public final String getVendor(){return Factory.VENDOR;}
  /**
   * <accessor>
   * To get access to Factory's version
   * @return the version
   */
  public final String getVersion(){return Factory.VERSION;}
  /**
   * To start the factory
   * @throws IOException if can't start
   */
  public final void Start() throws java.io.IOException{
    if ( super.devicesCount() == 0 ) throw new IOException("No devices to start");
    super.Start();
  }
  /**
   * To stop the factory
   * @throws IOException
   */
  public final void Stop() throws java.io.IOException{
    //if ( super.devicesCount() == 0 ) throw new IOException("No devices to stop");
    super.Stop();
  }
  /**
   * <check>
   * To check, is device's name is valid
   * @param name device's name
   * @return true if name is valid
   */
  final boolean isValidName(String name){return factoryNames.contains(name);}
/**
 * <const>
 * The list of names of availabled Dislogic's resources
 */
private static List factoryNames;
/**
 * <pool>
 * The pool of defult tones
 */
static Map defaultTones=new HashMap();
  static{
    String Names = Hardware.getNamesList();
    ArrayList list = new ArrayList();
    StringTokenizer st = new StringTokenizer(Names,"\n ");
    while( st.hasMoreTokens() ) list.add(st.nextToken());
    Factory.factoryNames = Collections.unmodifiableList(list);
    // to make default signals table
    Factory.defaultTones.put("dial","250,400,125,400,125,0,0,0,0,0");
    Factory.defaultTones.put("busy","253,500,200,0,0,55,40,55,40,4");
    Factory.defaultTones.put("ringback","254,450,150,0,0,150,100,550,400,0");
    Factory.defaultTones.put("disconnect","257,900,700,0,0,90,70,90,70,2");
  }
  /**
   * Class-store of Dialogic's device properties
   */
  public static final class devParams
  {
    public static final String MEDIA = "media";
    public static final String NETWORK = "network";
    private final Map network = new HashMap();
    private final Map tones = new HashMap();
    private final Map formats = new HashMap();
    public devParams()
    {
      this.network.put("IN",Boolean.TRUE);
      this.network.put("OUT",Boolean.TRUE);
      this.network.put("SHARE",Boolean.TRUE);
      tones.put("dial","250,400,125,400,125,0,0,0,0,0");
      tones.put("busy","253,500,200,0,0,55,40,55,40,4");
      tones.put("ringback","254,450,150,0,0,150,100,550,400,0");
      tones.put("disconnect","257,900,700,0,0,90,70,90,70,2");
      formats.put("record", Audio.ADPCM_6);
    }
    public final Map getNetwork(){return this.network;}
    public final Map getTones(){return this.tones;}
    public final Map getFormats(){return this.formats;}
    /** to store the properties to XML */
    public final void store(Element xml)
    {
//      xml.removeChildren();
      // to store the network's parameters
      this.storeNetwork( xml );
      // to store the media's parameters
      this.storeMedia( xml );
    }
      private final void storeNetwork(Element xml){
        Element networkXML = new Element(NETWORK);
        for(Iterator i=this.network.entrySet().iterator();i.hasNext();){
          Map.Entry entry = (Map.Entry)i.next();
          Object value = entry.getValue(); String name=(String)entry.getKey();
          Parameter par = null;
          if (value instanceof Boolean)
            par = new Parameter(name.toLowerCase(),(Boolean)value);
          else
          if (value instanceof String)
            par = new Parameter(name.toLowerCase(),(String)value);
          else
          if (value instanceof Number)
            par = new Parameter(name.toLowerCase(),(Number)value);
          // to store as Parameter's XML
          if ( par != null)  networkXML.addContent( par.getXML() );
        }
        xml.addContent( networkXML );
      }
      private final void storeMedia( Element xml){
        Element mediaXML = new Element(MEDIA);
        // to store tones information
        for(Iterator i=this.tones.entrySet().iterator();i.hasNext();)
        {
          Map.Entry tone = (Map.Entry)i.next();
          Element toneXML = new Element("tone");
          toneXML.setAttribute(new Attribute("name",(String)tone.getKey()));
          toneXML.setAttribute(new Attribute("value",(String)tone.getValue()));
          mediaXML.addContent(toneXML);
        }
        // to store the formats set
        for(Iterator i=this.formats.entrySet().iterator();i.hasNext();)
        {
          Map.Entry tone = (Map.Entry)i.next();
          Element formatXML = new Element("format");
          formatXML.setAttribute(new Attribute("type",(String)tone.getKey()));
          formatXML.setAttribute(new Attribute("value",((Audio)tone.getValue()).toString()));
          mediaXML.addContent(formatXML);
        }
        xml.addContent( mediaXML );
      }
    /** to restore the properties from XML */
    public final void restore(Element xml)
    {
      this.tones.clear();this.formats.clear();this.network.clear();
      this.tones.putAll(Factory.defaultTones);
      formats.put("record", Audio.ADPCM_6);
      this.network.put("IN",Boolean.TRUE);
      this.network.put("OUT",Boolean.TRUE);
      this.network.put("SHARE",Boolean.TRUE);
      if ( xml == null ) return;
      // to restore the network
      this.restoreNetwork( xml.getChild(NETWORK) );
      // to restore the media
      this.restoreMedia( xml.getChild(MEDIA) );
    }
      private final void restoreNetwork(Element xml){
        if (xml == null) return;
        for( Iterator i=xml.getChildren(Parameter.ELEMENT).iterator();i.hasNext();){
          try{
            Parameter par = Parameter.restore((Element)i.next());
            this.network.put(par.getName().toUpperCase(),par.getValue());
          }catch(Exception e){}
        }
      }
      private final void restoreMedia(Element xml){
        if (xml == null) return;
        // to restore the tones
        for(Iterator i=xml.getChildren("tone").iterator();i.hasNext();){
          Element toneXML = (Element)i.next();
          String name = toneXML.getAttributeValue("name");
          String value= toneXML.getAttributeValue("value");
          if ( name != null && value != null) this.tones.put(name,value);
        }
        // to restore the formats
        for(Iterator i=xml.getChildren("format").iterator();i.hasNext();)
        {
          Element formatXML = (Element)i.next();
          String type = formatXML.getAttributeValue("type");
          Audio format=Audio.fromString( formatXML.getAttributeValue("value") );
          if ( type != null && format != null) this.formats.put(type,format);
        }
      }
  }
  /**
   * <device>
   * The factory's device
   */
  private final class Device extends genericChannel
  {
  /**
   * <handle>
   * The handle for control the device
   */
  private int handle = Hardware.DX_ERROR;
    /**
     * <accessor>
     * To get access to device's handle
     * @return the device's handle
     */
    public final int getHandle(){return this.handle;}
  /**
   * <attribute>
   * The properties of the device
   */
  private final devParams properties = new devParams();
    /**
     * <accessor>
     * To get access to device's properties
     * @return
     */
    final devParams getDeviceProperties(){return this.properties;}
    /**
     * <action>
     * The request to save configuration
     */
    protected final void save(){Factory.this.isSave = true;}
    /**
     * <constructor>
     * @param name the channel's name
     * @param owner the factory
     */
    public Device( String name, Factory owner){
      super( name, owner );
      this.handle = Hardware.openChannel(name,this);
    }
    /**
     * <action>
     * To open and activate the device.
     * If the device can't open, device may throw the exception
     * @throws IOException if some wrong
     * @see generalDevice
     */
    public final void open() throws java.io.IOException{
      if ( this.isOpened() ) this.close();
      if (this.handle != Hardware.DX_ERROR) super.open();// to open fax
    }
    /**
     * <action>
     *  To close the device, if there are no active operations and
     *  the expectation of end of the current operation, if still execute
     * @throws IOException if the device can't close
     * @see generalDevice
     */
    public final void close() throws java.io.IOException{
      this.terminate();// to terminate all device's activity
      super.close();// to close the vox's part
    }
    /**
    <action>
     Attempt to restore serviceability of the device.
     Must return the success of device restoring.
    */
    public final boolean restore()
    {
      try{
        super.newState( deviceProxy.DS_ERROR );
        // to terminate all activity
        this.terminate();
      }catch(IOException e){}
      // to terminate the current call
      this.dropCall();
      // to close the channel
      Hardware.closeChannel( this.handle );
      // try to open channel
      this.handle = Hardware.openChannel( this.getName(), this );
      if ( handle != Hardware.DX_ERROR ) return super.restore();
      // can't open try open it 20 times
      for(int i=1;i < 20;i++)
      {
        this.handle = Hardware.openChannel(this.getName(),this);
        if ( handle != Hardware.DX_ERROR ) return super.restore();
        // to sleep 3 sec, before next try
        try{Thread.sleep(3000);}catch(Exception e){}
      }
      // can't restore the device
      return false;
    }
    /**
     * <restore>
     * To restore the device's properties from XML
     * @param xml stored device's properties
     */
    protected final void processConfiguration(Element xml)
    {
      this.type = xml.getAttributeValue("type");
      Element defaultXML = new Element("default");
      Factory.this.properties.store(defaultXML);
      this.properties.restore( defaultXML );
      this.properties.restore( xml );
      super.processConfiguration(xml);
    }
  /**
   * <attribute>
   * The type of the channel
   */
  private String type=null;
    /**
     * <accessor>
     * To get access to unit's type
     * @return the type
     */
    public final String getType(){return this.type;}

      /* called from Garbage Collector before free the memory */
      protected final void finalize() throws Throwable {
        if ( this.handle != Hardware.DX_ERROR ) {
          this.terminate(); this.close();
          Hardware.closeChannel(this.handle);
          this.handle = Hardware.DX_ERROR;
        }
        super.finalize();
      }
  }
}
