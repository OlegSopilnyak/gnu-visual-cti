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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jdom.Element;
import org.visualcti.briquette.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: WorkFlow, the Panel of the briquettes</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public final class briquettesControl extends JTabbedPane
{
/**
 * <attribute>
 * The owner of this panel
 * */
private final IDE ide;
    /**<constructor>*/
    public briquettesControl(IDE ide,Element xml)
    {
        super( JTabbedPane.TOP ); this.ide=ide;
        super.setFont(new Font("sansserif",Font.BOLD,10));
        this.setup( xml );
    }
    /**
     * <trace>
     * To print the message
     * */
    private static final void trace(String message){
      org.visualcti.util.Tools.print(message);
    }
    /**
     * <trace>
     * To print the Throwable
     * */
    private static final void trace(Throwable t){
      t.printStackTrace( org.visualcti.util.Tools.err );
    }
    /**
     * <attribute>
     * The defualt Icon of the briquette
     */
private ImageIcon defaultIcon = null;
    /**
     * <builder>
     * To build a tabbed panel
     * */
    private void setup(Element XML){
      // to make the icons by default briquette
      this.defaultIcon( XML.getChild("base") );
      // to make the briquettes groups
      Iterator i = XML.getChildren(Config.GROUP).iterator();
trace("Making the briquettes's groups....");
      while( i.hasNext() ){
        Element grXML = (Element)i.next();
        String name = grXML.getAttributeValue("name");
        if (name == null) continue;
        // to make the container of buttons
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // to add the tab
        super.addTab( name, panel );
        // to build the group and fill the panel
        this.setupGroup( grXML, name, panel);
      }
    }
    /**
     * <builder>
     * To build the group of briquettes
     * */
    private final void setupGroup(Element XML,String group,JPanel place){
trace("Making group ["+group+"]...");
      for(Iterator i=XML.getChildren("briquette").iterator();i.hasNext();){
        JButton button = this.makeButton( group, (Element)i.next() );
        if (button != null) place.add(button);
      }
    }
    private final void defaultIcon(Element xml)
    {
      if (xml == null) return;
      xml = xml.getChild("briquette");
      Element classXML = xml.getChild("class");
      Element paramXML = xml.getChild("parameter");
      // check the XML's integrity
      if (classXML == null || paramXML == null)return;
      // to make the classname
      String className =
          classXML.getAttributeValue("package") + "." +
          classXML.getAttributeValue("name");
      String iconPath = paramXML.getAttributeValue("icon");
      String tooltip = paramXML.getAttributeValue("tooltip");
      // to check the button's integrity
      if (iconPath == null || tooltip == null)
        return;
      // to assosiate the Icon with the class
      this.defaultIcon = UI_Store.register(className, iconPath, null);
    }
    /**
     * <builder>
     * To make the briquette's Button from XML's element
     * */
    private final JButton makeButton(String group,Element xml){
        Element classXML = xml.getChild("class");
        Element paramXML = xml.getChild("parameter");
        // check the XML's integrity
        if (classXML == null || paramXML == null) return null;
        // to make the command
        String actionCommand = group+"."+xml.getAttributeValue("name");
        // to make the classname
        String className =
          classXML.getAttributeValue("package")+"."+
          classXML.getAttributeValue("name");
        String iconPath = paramXML.getAttributeValue("icon");
        String tooltip = paramXML.getAttributeValue("tooltip");
        // to check the button's integrity
        if (iconPath == null || tooltip == null) return null;
        // to assosiate the Icon with the class
        ImageIcon icon = UI_Store.register( className, iconPath, this.defaultIcon );
        // to make the opeartion's object
        Operation operStub = this.makeOperation(className);
        if (operStub == null) return null;
        // to store the operation's predefined symbols
        Program.storePredefinedSymbols(operStub);
        // to store the button's information
        // the pair: the action's name, Operation's class
        this.classes.put( actionCommand, operStub.getClass() );
        // to make the visual's parts of the JButton
        JButton button = new JButton( icon == null ? this.defaultIcon:icon );
        button.setActionCommand(actionCommand);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setRequestFocusEnabled(false);
        button.setMargin(new Insets(1,1,1,1));
        // to register the actions's listener
        button.addActionListener(new ActionListener(){
          public final void actionPerformed(ActionEvent e){
            briquettesControl.this.addOperation( e.getActionCommand() );
          }
        });
//trace("Registered action "+actionCommand);
        return button;
    }
    /**
     * <producer>
     * To make the Operation's instance from class's name
     * */
    private static final Operation makeOperation(String className){
      try{return (Operation)Class.forName(className).newInstance();
      }catch(Throwable e){trace(e);}
      return null;// invalid some things
    }

/**
 * <pool>
 * The pool of briquettes' classes
 * */
private final HashMap classes = new HashMap(20);
    /**
     * <action>
     * To add Operation's instance to chain
     * */
    private final void addOperation(String theAction){
//trace("Pressed "+theAction);
      Class operClass = (Class)this.classes.get(theAction);
      try {
        Operation oper = (Operation)operClass.newInstance();
        if (oper != null) this.ide.getChain().addOperation( oper );
      }catch(Exception e){
        this.trace(e);
      }
    }
}
