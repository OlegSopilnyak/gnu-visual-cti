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

import org.jdom.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JCheckBox;
import javax.swing.JSlider;

import org.visualcti.util.Tools;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: WorkFlow, the IDE's controls panel</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public final class ideControl extends JPanel
{
/**
 * <pool>
 * The Actions pool
 * */
private final HashMap actions = new HashMap();
    /**
     * <mutator>
     * To disable/enable the IDE's action
     * */
    public final void setEnabled(String action,boolean enable)
    {
        try {((JButton)this.actions.get(action)).setEnabled( enable );
        }catch(Exception e){}
    }
    /**
     * <mutator>
     * to enable the action
     * */
    public final void enable(String action){this.setEnabled(action, true);}
    /**
     * <mutator>
     * to disable the action
     * */
    public final void disable(String action){this.setEnabled(action, false);}
/**
 * <attribute>
 * Owner of this panel
 * */
private final IDE ide;
    /**
     * <constructor>
     * */
    public ideControl
                (
                IDE ide,      // the parent of panel
                Element XML   // the set of Actions
                )
    {
        super( true );
        super.setLayout( new BoxLayout(this,BoxLayout.X_AXIS) );
        super.setBorder( new BevelBorder(BevelBorder.RAISED) );
        Facade facade = (this.ide=ide).getFacade();
        ideControl.attashGridControl( facade );
        this.setup(XML, facade);
    }
    /**
     * <producer>
     * to connect a grid control to IDE's facade
     * */
    private static final void attashGridControl(final Facade facade){
      // tooltips
      facade.gridOn.setToolTipText("To switch On/Off the grid's drawing");
      facade.gridSize.setToolTipText("To adjust the size of cell");
      // add checkbox's listener
      facade.gridOn.addItemListener( new ItemListener(){
        final public void itemStateChanged(ItemEvent e) {
          facade.gridSize.setEnabled( facade.gridOn.isSelected() );
        }
      });
    }
    /**
     * <setup>
     * to adjust the controls panel from XML
     * */
    private final void setup(Element XML, Facade facade){JPanel panel = null;
      boolean first=true;
      for(Iterator i=XML.getChildren(Config.GROUP).iterator(); i.hasNext(); ) {
        panel = new JPanel(false); panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        if (first) first=false; else this.add( Box.createHorizontalStrut(8) );
        this.add( panel ); this.setupGroup( (Element)i.next(), panel );
      }
      // to place grid's components to the last panel
      if (panel != null){
        panel.add( Box.createHorizontalStrut(8) );
        panel.add( facade.showAbout );
        panel.add( facade.gridSize ); panel.add( facade.gridOn );
      }
    }
    /**
     * <setup>
     * to setup the Actions of the group
     * */
    private final void setupGroup(Element xml,JPanel panel){
      Tools.print("Adjusting control for "+xml.getAttributeValue("name"));
      boolean first = true;
      for(Iterator i=xml.getChildren("action").iterator();i.hasNext();){
        if (first) first=false; else panel.add( Box.createHorizontalStrut(1) );
        this.setupAction( (Element)i.next(), panel );
      }
    }
    /**
     * <setup>
     * to setup group's Action
     * */
    private final void setupAction(Element xml,JPanel panel){
      String command = xml.getAttributeValue("command");
      String icon = xml.getAttributeValue("icon");
      String tooltip = xml.getAttributeValue("tooltip");
      String enabled = xml.getAttributeValue("enable");
      // check valid
      if (command==null || icon==null || tooltip==null) return;
      // to create and register the Action
      ImageIcon imageIcon = null;
      try {imageIcon = new ImageIcon( ideControl.class.getResource(icon) );
      }catch(NullPointerException e){}
      JButton button = new JButton( imageIcon );
      button.setRolloverEnabled( true );
      button.addActionListener( new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          ide.doAction( e.getActionCommand() );
        }
      });
      button.setActionCommand( command );
      button.setToolTipText( tooltip );
      button.setFocusPainted(false);
      button.setRequestFocusEnabled(false);
      button.setMargin(new Insets(1,1,1,1));
      button.setEnabled( Boolean.valueOf(enabled).booleanValue() );
      panel.add(button);
      this.actions.put( command, button );
    }
}
