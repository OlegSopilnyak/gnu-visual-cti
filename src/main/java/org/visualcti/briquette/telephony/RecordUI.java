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
 * <p>Description: The User Interface for adjust the Record</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class RecordUI extends baseOperationUI
{
private final static String targetTitleText = "Container for sound";
private final static String timeoutTitleText = "How long...";
private final static String silenceTitleText = "Silence timeout";
private final static String dtmfTitleText = "Terminated using";
private final static String beepBeforeTitleText = "Beep before";
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
   * <editor>
   * the editor for "timeout"
   * */
  private final class timeoutEditor extends SymbolEditor{
    public timeoutEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){return getDialog();}
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getTimeout();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setTimeout(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(10));}
  }
  /**
   * <editor>
   * the editor for "silence"
   * */
  private final class silenceEditor extends SymbolEditor{
    public silenceEditor(){super();super.disableTitle();}
    protected final java.util.List availableGroups(){return groups;}
    protected final SymbolChooser getSymbolChooser(){return getDialog();}
    protected final Symbol getSymbol(){
      return owner == null ? null:owner.getSilence();
    }
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setSilence(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst(new Integer(5));}
  }
  /**
   * to process Symbols chooser only for number
   * */
  private final SymbolChooser getDialog() {
    if (this.owner == null) return null;
    java.util.List all = this.owner.availableSymbols();
    SymbolChooser dialog = SymbolChooser.getInstance(this.params);
    ArrayList list = new ArrayList(); Symbol symbol;
    for(Iterator i=all.iterator();i.hasNext();) {
      if ((symbol=(Symbol)i.next()) == null) continue;
      if (symbol.getTypeID() == Symbol.NUMBER) list.add( symbol );
    }
    dialog.setSymbols( list );
    return dialog;
  }
/**
 * <attribute>
 * The list of available Symbols
 * */
private final java.util.List groups = new ArrayList(4);
/**
 * <attribute>
 * The briquette for edit runtime's properties
 * */
private RecordAction owner = null;
/**
 * <attribute>
 * visual component for edit target's property
 * */
private final VoxTargetUI target = new VoxTargetUI();
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
private final timeoutEditor timeout = new timeoutEditor();
/**
 * <mutator>
 * To update the timeout property
 * */
private final void setTimeout(){
  if (this.owner == null) return;
  String data = this.timeout.name.getText();
  Symbol symbol = this.owner.getTimeout();
  if (symbol.isConst()) symbol.setName(data);
}
/**
 * <attribute>
 * visual component for edit silence's property
 * */
private final silenceEditor silence = new silenceEditor();
/**
 * <mutator>
 * To update the silence property
 * */
private final void setSilence(){
  if (this.owner == null) return;
  String data = this.silence.name.getText();
  Symbol symbol = this.owner.getSilence();
  if (symbol.isConst()) symbol.setName(data);
}
/**
 * <attribute>
 * visual component for edit dtmf's property
 * */
private final terminationUI DTMF = new terminationUI();
/**
 * <attribute>
 * visual component for edit beep's property
 * */
private final JComboBox beepBefore = new JComboBox(new Object[]{"true","false"});
/**
 * <mutator>
 * To update the beepBefore property
 * */
private final void setBeepBefore(boolean flag){
  if (this.owner != null) this.owner.setBeepBefore(flag);
}
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <constructor>
   * */
  public RecordUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    // to add the target
    this.params.add(Box.createVerticalStrut(4));
    addTitle( targetTitleText, this.params);
    this.params.add(this.target);
    // to add timeout
    this.params.add(Box.createVerticalStrut(4));
    addTitle( timeoutTitleText, this.params);
    this.params.add( this.timeout );
    // to add listener
    this.timeout.name.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {if ( !isAssigned ) setTimeout();}
    });
    // to add silence
    this.params.add(Box.createVerticalStrut(4));
    addTitle( silenceTitleText, this.params);
    this.params.add( this.silence );
    // to add listener
    this.silence.name.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {if ( !isAssigned ) setSilence();}
    });
    // to add DTMF
    this.params.add(Box.createVerticalStrut(4));
    addTitle( dtmfTitleText, this.params);
    JPanel termination = new JPanel(new FlowLayout(FlowLayout.CENTER));
    termination.add( this.DTMF );
    this.params.add( termination );
    // to add beepBefore
    this.params.add(Box.createVerticalStrut(4));
    addTitle( beepBeforeTitleText, this.params);
    this.params.add( this.beepBefore );
    // to add listener
    this.beepBefore.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setBeepBefore( beepBefore.getSelectedIndex()==0 );
      }
    });
  }
  /**
   * <listener>
   * The listener of document's changes
   * */
  private abstract class DocListener implements DocumentListener {
    public final void insertUpdate(DocumentEvent e) {this.update();}
    public final void removeUpdate(DocumentEvent e) {this.update();}
    public final void changedUpdate(DocumentEvent e){}
    /** to check and process changes */
    protected abstract void update();
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
    this.timeout.reload();
    this.silence.reload();
    this.propertiesTree.add(this.params, BorderLayout.NORTH);
  }
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
    if (briquette instanceof RecordAction) {
      this.owner=(RecordAction)briquette;
      this.isAssigned = true;
      this.target.assign( this.owner.getTarget() );
      this.beepBefore.setSelectedIndex( this.owner.isBeepBefore()?0:1 );
      this.DTMF.assign( this.owner.getDTMF() );
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
}
