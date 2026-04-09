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
package org.visualcti.workflow;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.visualcti.util.Tools;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The config of the IDE</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class Config
{
/**
 * <const>
 * The ini-file of briquettes' workflow
 * */
public static final File iniFile = new File("conf/VisualCTI.workflow.xml");
/**
 * <xml>
 * The configuration
 * */
private static Element configuiration = null;
  /**
   * <saver>
   * To save updating configuration
   * */
  public static void save()
  {
    Tools.xmlSave( Config.configuiration, Config.iniFile );
  }
/**
 * <pool>
 * The pool of IDE's parts
 * */
private final static HashMap parts = new HashMap(3);
  private static void load(){load(iniFile);}
  /**
   * <loader>
   * To load & solve the configuration from ini-file
   * @param xmlFile the file to load & solve
   */
  private static void load( File xmlFile ) {
    Tools.print("Loading ini-file....");
    Element xml = Tools.xmlLoad( xmlFile );
    Tools.print("Solving the configuration from "+xmlFile.getName());
    if ( !xml.getName().equals(Tools.ROOT_ELEMENT) ) { // not a VisualCTI document
      Tools.error("Invalid XML structure in "+Config.iniFile.getAbsolutePath());
      Config.configuiration = null;
      return;
    }
    Config.configuiration = xml;
    // get workflow's things
    xml = xml.getChild("WorkFlow");
    // to sort a parts
    synchronized(Config.parts) {
      // to clear the parts
      Config.parts.clear();
      // to solving the parts of configuration
      for(Iterator i=xml.getContent().iterator();i.hasNext();){
        try {// we will process only Element.class
          Element partXML = (Element)i.next();
          String part = partXML.getName();
          // to store the part
          Config.parts.put( part, partXML );
        }catch(ClassCastException e){}// will ignore not Element entry
      }
    }
  }
/**
 * <name>
 * The name of control's Element
 * */
public static final String CONTROLS_PART = "controls";
/**
 * <name>
 * The name of briquettes's Element
 * */
public static final String BRIQUETTES_PART = "briquettes";
/**
 * <name>
 * The name of briquettes's Element
 * */
public static final String EQUIPMENT_PART = "equipment";
/**
 * <name>
 * The name of group in the part
 * */
public static final String GROUP = "group";
  /**
   * <accessor>
   * To get access to IDE's part by part's name
   * @param name the name of configuration's part
   * @return The XML from configuration
   */
  static Element getPart(String name)
  {
    synchronized(Config.parts)
    {
      Element xml = (Element)Config.parts.get(name);
      return xml == null ? new Element(name):xml;
    }
  }
  /**
   * <init>
   * To initialize the configuration of IDE
   * */
  static void initialize()
  {
    Config.load();
  }
}
