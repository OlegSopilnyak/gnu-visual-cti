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
package org.visualcti.briquette.core;

import java.util.*;
import org.jdom.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the equipment of the Chain. Used only in design mode</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Equipment
{
/**
 * <const>
 * The name of root element in XML
 */
public static final String ELEMENT = "equipment";
  /**
   * <translator>
   * To store the equipment to XML format
   * @return stored equipment
   */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    for(Iterator i=this.allInfo().iterator();i.hasNext();)
    {
      Info info = (Info)i.next();
      if ( !info.isTemporary() ) xml.addContent( info.getXML() );
    }
    return xml;
  }
  /**
   * <translator>
   * To restore the equipment from XML
   * @param xml stored equipment
   * @throws Exception if some wrong
   */
  public final void setXML(Element xml) throws Exception
  {
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) throw new Exception("Invalid Equipment's XML");
    this.init();
    for(Iterator i=xml.getChildren(Info.ELEMENT).iterator();i.hasNext();)
    {
      Info info = new Info();
      info.setXML( (Element)i.next() );
      info.setTemporary( false );
      this.add( info );
    }
System.out.println("Equipment groups:");
    for(Iterator i=this.getGroups().iterator();i.hasNext();){
System.out.println("Groups:"+i.next());
    }
  }
/**
 * <const>
 * The name of telephony equipment's group
 */
public static final String TELEPHONY = "telephony";
/**
 * <const>
 * The name of database equipment's group
 */
public static final String DATABASE = "database";
/**
 * <const>
 * The name of messenger equipment's group
 */
public static final String MESSENGER = "messenger";
/**
 * <pool>
 * The pool of the groups
 */
private final ArrayList pool = new ArrayList();
  /**
   * <accessor>
   * To get access to the list of groups
   * @return The list of groups
   */
  public final List getGroups()
  {
    synchronized( this.pool )
    {
      ArrayList groups = new ArrayList();
      for(Iterator i=this.pool.iterator();i.hasNext();)
        groups.add( ((Group)i.next()).group );
      return groups;
    }
  }
  /**
   * <accessor>
   * To get access to equipment's group by name
   * @param group the name of group
   * @return the list of group's Infos
   */
  public final List getGroup(String group)
  {
    Group gr = this.getGroupNative(group);
    return gr == null ? null:(List)gr.entries.clone();
  }
  /**
   * <mutator>
   * To add Info to the equipment
   * @param info the info to add
   * @return true if successful
   */
  public final boolean add(Info info)
  {
    if ( info == null || !info.isValid() ) return false;
    Group gr = this.getGroupNative( info.group );
    if ( gr == null ) return false;
    synchronized( gr.entries ){gr.entries.add(info);}
    return true;
  }
  /**
   * <mutator>
   * To remove the info from the equipment
   * @param info the info to remove
   */
  public final void remove(Info info)
  {
    if ( info == null || !info.isValid() ) return;
    Group gr = this.getGroupNative( info.group );
    if ( gr != null ) synchronized( gr.entries ){gr.entries.remove(info);}
  }
  private final void init(){synchronized( this.pool ){this.pool.clear();}}
  private final List allInfo(){
    ArrayList all = new ArrayList();
    synchronized( this.pool ) {
      for(Iterator i=this.pool.iterator();i.hasNext();){
        Group gr = (Group)i.next();
        synchronized( gr.entries ){all.addAll(gr.entries);}
      }
    }
    return all;
  }
  private final Group getGroupNative(String group){
    if ( !Info.isValid(group) ) return null;
    synchronized( this.pool ) {
      for(Iterator i=this.pool.iterator();i.hasNext();){
        Group gr = (Group)i.next();
        if ( group.equals(gr.group) ) return gr;
      }
      Group gr = new Group();gr.group=group;gr.entries=new ArrayList();
      this.pool.add(gr);
      return gr;
    }
  }
private final static class Group{String group;ArrayList entries;}
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The description for equipment's entity. Used only in design mode</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public static final class Info
{
  /**
   * <const>
   * The name of root XML's element
   */
  public static final String ELEMENT = "info";
  /**
   * <translator>
   * To store the info in XML's format
   * @return the XML
   */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    xml.setAttribute(new Attribute("group",this.group));
    xml.setAttribute(new Attribute("entity",this.name));
    String options = String.valueOf(this.options);
    xml.setAttribute(new Attribute("options",options));
    return xml;
  }
  /**
   * <translator>
   * To restore the info from XML format
   * @param xml XML to restore
   * @throws Exception when invalid format of XML
   */
  public final void setXML(Element xml) throws Exception
  {
    if (xml == null || !ELEMENT.equals(xml.getName()) ) throw new Exception("Invalid Info's XML");
    this.init();
    this.group = xml.getAttributeValue("group");
    this.name = xml.getAttributeValue("entity");
    String options = xml.getAttributeValue("options");
    if ( !"null".equals(options) ) this.options = options;
  }
  /**
   * to check Info's integrity
   * @return true if Info is valid
   */
  public final boolean isValid()
  {
    return this.isValid(this.group) && this.isValid(this.name);
  }
  private final static boolean isValid(String attribute){
    return attribute != null && !"".equals(attribute);
  }
  /**
   * <constructor>
   * To make the Equipment's Info
   * @param group the group of entity
   */
  public Info(String group){this.group=group;}
  /**
   * <constructor>
   * For restore from XML
   */
  private Info(){this.init();}
  /**
   * To init the attributes
   */
  private final void init(){
    this.group="no group";this.name=null;this.options=null;this.temporary=true;
  }
  /**
   * <attribute>
   * The group of equipment
   */
  private String group;
  /**
   * <accessor>
   * To get access to group of entity
   * @return the group's name
   */
  public final String getGroup(){return group;}
  /**
   * <attribute>
   * The name of entity
   */
  private String name;
  /**
   * <accessor>
   * To get access to entity's name
   * @return the name
   */
  public final String getEntityName(){return this.name;}
  /**
   * To assign new entity's name
   * @param name new name
   */
  public final void setEntityName(String name){this.name=name;}
  /**
   * <attribute>
   * The info's options
   */
  private Object options;
  /**
   * <accessor>
   * to get access to info's options
   * @return the options
   */
  public final Object getOptions(){return this.options;}
  /**
   * <mutator>
   * To assign the options to the Info
   * @param options
   */
  public final void setOptions(Object options){this.options=options;}
  /**
   * <attribute>
   * Flag, is need to store this Info
   */
  private boolean temporary;
  /**
   * <accessor>
   * To get access to temporary flag
   * @return the value of the flag
   */
  public final boolean isTemporary(){return this.temporary;}
  /**
   * <mutator>
   * To change the temporary flag
   * @param temporary new value of the flag
   */
  public final void setTemporary(boolean temporary){this.temporary=temporary;}
}
}
