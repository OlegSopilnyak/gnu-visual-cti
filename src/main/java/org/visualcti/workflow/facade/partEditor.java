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
package org.visualcti.workflow.facade;

import javax.swing.*;
import org.jdom.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, base class of Environment's editors</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public abstract class partEditor extends JPanel
{
public final static String ELEMENT = "part";
  /**
   * <translator>
   * To restore the editor from XML
   * */
  public void setXML(Element XML){}
  /**
   * <translator>
   * To store the editor to XML
   * */
  public Element getXML()
  {
    Element xml = new Element(ELEMENT);
    xml.setAttribute(new Attribute("name",this.getName()));
    xml.setAttribute(new Attribute("class",this.getClass().getName()));
    return xml;
  }
  /**
   * <accessor>
   * To get the name of environment's part
   * */
  public abstract String getName();
/**
 * <attribute>
 * */
protected transient visualEnvironment owner=null;
  /**
   * <mutator>
   * To assign the owner
   * */
  public final void setOwner(visualEnvironment owner){this.owner=owner;}
  /**
   * <refresh>
   * To reload visuals from env part
   * */
  public abstract void reload();
  /**
   * <cleaner>
   * To clean all references to Chain
   * */
  public abstract void clean();
}
