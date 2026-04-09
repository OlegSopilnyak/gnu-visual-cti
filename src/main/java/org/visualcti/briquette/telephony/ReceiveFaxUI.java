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
package org.visualcti.briquette.telephony;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import org.visualcti.briquette.core.*;
import org.visualcti.briquette.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface foe adjust the ReceiveFax</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class ReceiveFaxUI extends baseOperationUI
{
private final static String targetTitleText = "Container for fax";
private final static String signalTitleText = "Signal after";
private final static String poolingTitleText = "Using POOL";
/**
 * to add the title to the editors's container
 * */
private static final void addTitle(String theTitleText,JPanel container) {
  JLabel title = new JLabel(theTitleText,JLabel.CENTER);
  title.setFont(UI.titleFont);
  title.setForeground(UI.titleColor);
  container.add( title );
}
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private ReceiveFax owner = null;
/**
 * <attribute>
 * visual component for edit target's property
 * */
private final FaxTargetUI target = new FaxTargetUI();
/**
 * <attribute>
 * visual component for edit issueVoiceRequest's property
 * */
private final JComboBox signal = new JComboBox(new Object[]{"true","false"});
/**
 * <mutator>
 * To update the issueVoiceRequest property
 * */
private final void setSignal(boolean flag){
  if (this.owner != null) this.owner.setIssueVoiceRequest(flag);
}
/**
 * <attribute>
 * visual component for edit pooling's property
 * */
private final JComboBox pooling = new JComboBox(new Object[]{"true","false"});
/**
 * <mutator>
 * To update the pooling property
 * */
private final void setPooling(boolean flag){
  if (this.owner != null) this.owner.setPooling(flag);
}
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <constructor>
   * */
  public ReceiveFaxUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the target
    this.params.add(Box.createVerticalStrut(4));
    addTitle( targetTitleText, this.params);
    this.params.add(this.target);
    // to add signal
    this.params.add(Box.createVerticalStrut(4));
    addTitle( signalTitleText, this.params);
    this.params.add( this.signal );
    // to add listener
    this.signal.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setSignal( signal.getSelectedIndex()==0 );
      }
    });
    // to add polling
    this.params.add(Box.createVerticalStrut(4));
    addTitle( poolingTitleText, this.params);
    this.params.add( this.pooling );
    // to add listener
    this.pooling.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setPooling( pooling.getSelectedIndex()==0 );
      }
    });
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.prepareGroups();
    this.target.assign( this.owner.getTarget() );
    this.target.setAvailabledSymbols( this.owner.availableSymbols() );
    this.propertiesTree.add(this.params, BorderLayout.NORTH);
  }
/**
 * <attribute>
 * The list of available Symbols
 * */
private final java.util.List groups = new ArrayList(4);
  private final void prepareGroups(){
    this.groups.clear();
    if (this.owner == null) return;
    java.util.List all = this.owner.availableSymbols();
    for(Iterator i=all.iterator();i.hasNext();) {
      Symbol symbol = (Symbol)i.next();
      if (
          symbol == null  ||
          symbol.isConst()||
          symbol.getTypeID() != Symbol.NUMBER
          ) continue;

      String group = symbol.getGroup();
      if ( !this.groups.contains(group) ) this.groups.add(group);
    }
  }
/**
 * <flag>
 * flag is assign in progress
 * */
private volatile boolean isAssigned = false;
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof ReceiveFax) {
      this.owner=(ReceiveFax)briquette;
      this.isAssigned = true;
      this.target.assign( this.owner.getTarget() );
      this.signal.setSelectedIndex(this.owner.isIssueVoiceRequest()?0:1);
      this.pooling.setSelectedIndex(this.owner.isPooling()?0:1);
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
