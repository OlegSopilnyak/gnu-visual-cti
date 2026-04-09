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
package org.visualcti.briquette;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: WorkFlow, base UI for any Briquette's UI </p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public class baseOperationUI extends UI_adapter implements OperationUI
{
/**
 * <attribute>
 * The owner of this UI
 * */
protected Operation briquette=null;
/**
 * <mutator>
 * To assign a Operation to the UI
 * */
final public void assign(Operation briquette)
{
  this.briquette = briquette;
  this.theIcon = UI_Store.getIcon( briquette );
  this.assigned( briquette );// to notify
}
/**
 * <notify>
 * To notify about Operation assigned
 * */
protected void assigned(Operation briquette){}
/**
 *  <accessor>
 *  To get access to Operation's Tooltip text
*/
final public String getTooltip()
{
    try{return this.briquette.getAbout();
    }catch(NullPointerException e){return "?????";}
}

/**
 * <editor>
 * The checkbox of main operation
 * */
private final JCheckBox main;
/**
 * <editor>
 * The fieald for edit about property
 * */
protected JTextField about;
/**
 * <panel>
 * The editor of basis's properties
 * Main's flag & briquette's about
 * */
private class Base extends JPanel
{
  /** <constructor> */
  public Base(){super(false);// not doublebuffered output
    super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    this.setBorder(new BevelBorder(BevelBorder.RAISED));

    this.add( baseOperationUI.this.main );// checkbox
    this.add(Box.createVerticalStrut(5));
    this.add( baseOperationUI.this.about );// textfield

    // to add main's listeners
    main.addItemListener( new ItemListener(){
        public final void itemStateChanged(ItemEvent ev){
            if ( !isPreparing ){
              boolean master = main.isSelected();
              briquette.setMaster( master );
              if (master) main.setEnabled( false );
            }
        }
    });
    // to add about's listeners
    about.getDocument().addDocumentListener( new DocumentListener(){
      private void update() {
        if ( !isPreparing ) briquette.setAbout(about.getText());
      }
      public void insertUpdate(DocumentEvent e) {this.update();}
      public void removeUpdate(DocumentEvent e) {this.update();}
      public void changedUpdate(DocumentEvent e){}
    });
  }
}
/**
 * <flag>
 * is changes during prepare the base properties
 * */
private volatile boolean isPreparing = false;
/**
 * <constructor>
 * */
public baseOperationUI()
{
  this.main = new JCheckBox("main");
  this.main.setHorizontalAlignment(JCheckBox.CENTER);
  this.about = new JTextField();
  this.main.setToolTipText("Main operation checkbox");
  this.about.setToolTipText("About this operation");
  this.base = new Base();
  this.propertiesTree.setBorder( new BevelBorder(BevelBorder.LOWERED) );
}
/**
 * <attribute>
 * The base properties panel (main,about)
 * */
private final Base base;
/**
 * <attribute>
 * The properties tree panel
 * */
protected JPanel propertiesTree = new JPanel( new BorderLayout(), false );
/**
 * <attribute>
 * The panel for selected property editor
 * */
protected JPanel property = new JPanel(false);
    /**
     * <producer>
     * to prepare the UI for edit
     * */
    public final void activate(JPanel place)
    {
        place.removeAll();// to clear old elemnets
        this.property.removeAll(); // to remove property editor
        this.propertiesTree.removeAll();// to remove all from properties
        if ( !place.isDoubleBuffered() ) place.setDoubleBuffered(true);

        // to prepare the parts of properties
        this.prepare();

        // to place parts of properties
        place.setLayout( new BorderLayout() );
        place.add( this.base, BorderLayout.NORTH );
        place.add( this.propertiesTree, BorderLayout.CENTER );
        place.add( this.property, BorderLayout.SOUTH );

        // to relayout place and all children
        place.doLayout();
        this.base.doLayout(); this.base.repaint();
        this.propertiesTree.doLayout(); this.propertiesTree.repaint();
        this.property.doLayout();   this.property.repaint();

        // to refresh the briquette's properties's editors before showing
        this.refreshProperties();

        // to check UI's integrity
        if ( this.briquette == null )
          throw new NullPointerException("Not assigned the briquette for UI");

        // actualize Base's parameters
        this.isPreparing = true;// disable notification
        // to solve master flag
        boolean master =  this.briquette.isMaster();
        this.main.setEnabled( !master );
        this.main.setSelected( master );
        // to solve about
        String about = this.briquette.getAbout();
        this.about.setText(about);
        this.about.setCaretPosition(0);
        // all things solved :-)
        this.isPreparing = false;// enable notification
    }
    /**
     * <producer>
     * To prepare parts of UI for editing
     * */
    protected void prepare(){}
    /**
     * <producer>
     * To refresh properties list before visualizing
     * */
    protected void refreshProperties(){}
}
