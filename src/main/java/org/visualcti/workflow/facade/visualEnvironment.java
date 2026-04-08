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
package org.visualcti.workflow.facade;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.visualcti.briquette.*;
import org.jdom.*;
import org.visualcti.workflow.environment.*;
import org.visualcti.workflow.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, the panel for represents the Environment</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class visualEnvironment extends JPanel
{
/**
 * <attribute>
 * The layout for Environment's parts
 * */
private final CardLayout layout = new CardLayout(0,0);
/**
 * <attribute>
 * The place for environment's part Editor
 * */
private final JPanel editor = new JPanel( layout, false );
/**
 * <attribute>
 * The model of ComboBox
 * */
private final DefaultComboBoxModel partsModel = new DefaultComboBoxModel();
/**
 * <attribute>
 * The selector of parts
 * */
private final JComboBox parts = new JComboBox( partsModel );
/**
 * <attribute>
 * The facade of IDE
 */
private final Facade facade;
  /**
   * <accessor>
   * To get access to IDE facade's instance
   * @return
   */
  public final Facade getFacade(){return facade;}
  /**
   * <constructor>
   * */
  public visualEnvironment(Facade facade) {
    super( new BorderLayout(), true );
    this.facade=facade;
    // to place Parts's selection
    JPanel selector = new JPanel(new BorderLayout(),false);
    TitledBorder border = new TitledBorder("Runtime part");
    border.setTitleJustification(TitledBorder.CENTER);
    border.setTitleFont(UI.titleFont);
    border.setTitleColor(UI.titleColor);
    selector.setBorder(border);
    super.add( selector, BorderLayout.NORTH );
    selector.add(this.parts,BorderLayout.CENTER);
    this.parts.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if (isManual) partSelected();
      }
    });
    // to place part's editor
    super.add( this.editor, BorderLayout.CENTER );
    // fake init
    //this.addPart("fake",new JButton("fake"));
    //this.addPart("fake2",new JButton("fake2"));
    this.addPart("Database", new dbPartEditor() );
    this.addPart("Server", new serverPartEditor() );
    this.addPart("Telephony", new telephonyPartEditor() );
  }
/**
 * <pool>
 * The pool of enironment's parts
 * */
private final java.util.HashMap pool = new java.util.HashMap();
  /**
   * <notify>
   * Selected environment part
   * */
  private final void partSelected(){
    if ( this.pool.size() == 0) return;
    String part = (String)this.parts.getSelectedItem();
    if ( this.pool.get(part) != null){
      this.layout.show( this.editor, part );
    }
  }
/**
 * <const>
 * The name of root's element
 * */
public static final String ELEMENT = "environment";
  /**
   * <translator>
   * To store the Environment to XML
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    for(java.util.Iterator i = this.pool.values().iterator();i.hasNext();)
    {
      xml.addContent( ((partEditor)i.next()).getXML() );
    }
    return xml;
  }
  /**
   * <translator>
   * To restore the Environment from XML
   * */
  public final void setXML(Element XML) throws Exception
  {
    this.isManual = false;
    this.pool.clear();
    this.partsModel.removeAllElements();
    this.editor.removeAll();
    this.isManual = true;
    if ( XML == null || !ELEMENT.equals(XML.getName()) ) return;
    for(java.util.Iterator i= XML.getChildren(partEditor.ELEMENT).iterator();i.hasNext();)
    {
      Element xml = (Element)i.next();
      String name = xml.getAttributeValue("name");
      if ( name != null) continue;
      partEditor editor = this.editor( xml );
      if (editor != null) this.addPart( name, editor );
    }
  }
  /**
   * <producer>
   * To make the editor from XML
   * */
  private final partEditor editor(Element XML){
    String className = XML.getAttributeValue("class");
    if ( className == null ) return null;
    try {
      partEditor editor = (partEditor)Class.forName(className).newInstance();
      editor.setXML( XML ); editor.setOwner( this );
      return editor;
    }catch(Exception e){}
    return null;
  }
/**
 * <flag>
 * Is changes from user's choice
 * */
private transient boolean isManual = true;
  /**
   * <mutator>
   * To add the editor
   * */
  private final void addPart(String name,partEditor editor){
    this.isManual = false;
    editor.setOwner( this );
    this.pool.put(name,editor);
    this.partsModel.addElement( name );
    this.editor.add(editor, name );
    this.isManual = true;
  }
/**
 * <attribute>
 * The owner of environment
 * */
private Chain owner = null;
  /**
   * <accessor>
   * To get access to Chain
   * */
  public final Chain getChain(){return this.owner;}
  /**
   * <mutator>
   * To assign the Chain with Environment's editors
   * */
  public final void setChain(Chain owner)
  {
    this.owner = owner;
    for(java.util.Iterator i = this.pool.values().iterator();i.hasNext();)
    {
      ((partEditor)i.next()).reload();
    }
  }
  /**
   * <mutator>
   * To detach the owner
   * */
  public final void detachChain()
  {

    this.owner = null;
    for(java.util.Iterator i = this.pool.values().iterator();i.hasNext();)
    {
      ((partEditor)i.next()).clean();
    }
  }
}
