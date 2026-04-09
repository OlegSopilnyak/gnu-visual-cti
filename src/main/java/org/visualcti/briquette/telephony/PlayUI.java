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
 * <p>Description: The class for visualize the Play Operation's properties</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class PlayUI extends baseOperationUI
{
private final static String beepTitleText = "Beep after";
private final static String timeoutTitleText = "Maxtime to play";
private final static String terminationTitleText = "May terminate";
private final static String sourcesTitleText = "Chain to play";
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
    protected final Symbol getConst(){return Symbol.newConst(new Integer(-1));}
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
private Play owner = null;
  /**
   * <mutator>
   * To change the Beep
   * */
  private final void setBeep(boolean beep){
    if (this.owner != null) this.owner.setBeep( beep );
  }
  /**
   * <mutator>
   * To update the timeout property
   * */
  private final void setTimeout(){
    String data = this.timeout.name.getText();
    Symbol symbol = this.owner.getTimeout();
    if (symbol.isConst()) symbol.setName(data);
  }
/**
 * <attribute>
 * visual component for edit beep's property
 * */
private final JComboBox beep = new JComboBox(new Object[]{"true","false"});
/**
 * <attribute>
 * visual component for edit timeout's property
 * */
private final timeoutEditor timeout = new timeoutEditor();
/**
 * <attribute>
 * visual component for edit termination's property
 * */
private final terminationUI termination = new terminationUI();
/**
 * <attribute>
 * The list of sources
 * */
private final JList list = new JList( new String[]{"one", "two", "three"});
private final JScrollPane scrollPane = new JScrollPane( this.list );
/**
 * <attribute>
 * The visual component for control a sources list
 * */
private final ControlPanel control = new ControlPanel();
/**
 * <action>
 * To process Add action
 * */
private final void Add(){
  VoxSource source = new VoxSource();
  VoxSourceDialog dialog = VoxSourceDialog.getInstance(this.control);
  dialog.setSource( source );
  dialog.setAvailabledSymbols( this.owner.availableSymbols() );
  dialog.setVisible( true );
  if ( (source=dialog.getSource()) != null)
  {
    DefaultListModel model = (DefaultListModel)this.list.getModel();
    model.addElement(source);
    this.owner.addVoxSource(source);
  }
}
/**
 * <action>
 * To process Del action
 * */
private final void Del(){
  int index = this.list.getSelectedIndex();
  if ( index < 0 ) return;
  DefaultListModel model = (DefaultListModel)this.list.getModel();
  model.remove(index);
  this.owner.removeVoxSource(index);
  if (model.size() == 0) this.control.addOnly();
}
/**
 * <action>
 * To process Up action
 * */
private final void Up(){
  int index = this.list.getSelectedIndex();
  if (index < 1) return;
  Object line = ((DefaultListModel)this.list.getModel()).remove(index);
  if (line != null){
    ((DefaultListModel)this.list.getModel()).insertElementAt(line,index-1);
    this.list.setSelectedIndex(index-1);
    this.owner.moveVoxSourceUp(index);
  }
}
/**
 * <action>
 * To process Down action
 * */
private final void Down(){
  int index = this.list.getSelectedIndex();
  int maxIndex = this.list.getModel().getSize()-2;
  if (index < 0 || index > maxIndex) return;
  Object line = ((DefaultListModel)this.list.getModel()).remove(index);
  if (line != null){
    ((DefaultListModel)this.list.getModel()).insertElementAt(line,index+1);
    this.list.setSelectedIndex(index+1);
    this.owner.moveVoxSourceDown(index);
  }
}
/**
 * <action>
 * To process Edit action
 * */
private final void Edit(){
  VoxSource source = (VoxSource)this.list.getSelectedValue();
  if (source == null) return;
  VoxSourceDialog dialog = VoxSourceDialog.getInstance(this.control);
  dialog.setSource( source );
  dialog.setAvailabledSymbols( this.owner.availableSymbols() );
  dialog.setVisible( true );
  if ( (source=dialog.getSource()) != null)
  {
    DefaultListModel model = (DefaultListModel)this.list.getModel();
    int index = this.list.getSelectedIndex();
    model.set(index,source);
    this.owner.updateVoxSource(source,index);
  }
}
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel params = new JPanel();
  /**
   * <constructor>
   * */
  public PlayUI()
  {
    this.params.setLayout(new BoxLayout(this.params,BoxLayout.Y_AXIS));
    this.params.add(Box.createVerticalStrut(4));
    // to add beep property
    JLabel beepTitle = new JLabel(beepTitleText,JLabel.CENTER);
    beepTitle.setFont(PlayUI.titleFont);
    beepTitle.setForeground(WaitUI.titleColor);
    this.params.add(beepTitle);
    this.params.add( this.beep );
    // to add beep's listener
    this.beep.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent a){
        if ( !isAssigned ) setBeep( beep.getSelectedIndex()==0 );
      }
    });
    this.params.add(Box.createVerticalStrut(4));
    // to add timeout's property
    JLabel timeoutTitle = new JLabel(timeoutTitleText,JLabel.CENTER);
    timeoutTitle.setFont(PlayUI.titleFont);
    timeoutTitle.setForeground(PlayUI.titleColor);
    this.params.add( timeoutTitle );
    this.params.add( this.timeout );
    this.params.add(Box.createVerticalStrut(4));
    // to add timeout's listeners
    this.timeout.name.getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {this.update();}
      public void removeUpdate(DocumentEvent e) {this.update();}
      public void changedUpdate(DocumentEvent e){}
      /** to check and process changes */
      private void update() {if ( !isAssigned ) setTimeout();}
    });
    // to add termination's property
    JLabel terminationTitle = new JLabel(terminationTitleText,JLabel.CENTER);
    terminationTitle.setFont(PlayUI.titleFont);
    terminationTitle.setForeground(PlayUI.titleColor);
    this.params.add( terminationTitle );
    JPanel DTMF = new JPanel(new FlowLayout(FlowLayout.CENTER));
    DTMF.add( this.termination );
    this.params.add( DTMF );
    // to adjust the sources's list
    this.list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.list.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
          public final void valueChanged(ListSelectionEvent e) {PlayUI.this.selected();}
        });
  }
  /**
   * <notify>
   * To process the list's selection
   * */
  private final void selected(){
    if (this.list.getSelectedValue() != null)
      this.control.allFeatures(); else this.control.addOnly();
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (this.propertiesTree.getLayout() instanceof BorderLayout){}
    else this.propertiesTree.setLayout( new BorderLayout() );
    this.prepareGroups();
    this.termination.assign( this.owner.getDTMF() );
    this.timeout.reload();
    this.propertiesTree.add(this.params, BorderLayout.NORTH);
    this.prepareSources();
  }
  /**
   * <prepare>
   * To prepare the groups of symbols
   * */
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
   * <prepare>
   * to prepare sources List editor
   * */
  private final void prepareSources()
  {
    JPanel sources = new JPanel(new BorderLayout(0,0),true);
    JLabel sourcesTitle = new JLabel(sourcesTitleText,JLabel.CENTER);
    sourcesTitle.setFont(PlayUI.titleFont);
    sourcesTitle.setForeground(PlayUI.titleColor);
    sources.add( sourcesTitle, BorderLayout.NORTH );

    Dimension size = new Dimension(100,55);
    this.scrollPane.setSize(size);
    this.scrollPane.setPreferredSize(size);

    sources.add(this.scrollPane, BorderLayout.CENTER);
    sources.add(this.control, BorderLayout.SOUTH);
    super.property.setLayout(new BorderLayout(0,0));
    super.property.add( sources, BorderLayout.CENTER);
    this.control.addOnly();
    if (this.list.getModel().getSize() > 0) this.list.setSelectedIndex(0);
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
    if (briquette instanceof Play) {
      this.owner=(Play)briquette;
      this.isAssigned = true;
      this.termination.assign( this.owner.getDTMF() );
      this.beep.setSelectedIndex(this.owner.isBeep()?0:1);
      // to make & assign the model for sources list
      DefaultListModel model = new DefaultListModel();
      for(Iterator i=this.owner.getVoxSources().iterator();i.hasNext();)
        model.addElement( i.next() );
      this.list.setModel( model );
      this.isAssigned = false;
    }
  }
  /**
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.params.revalidate();}
/**
 * <control>
 * the class for control the sources list
 * */
private final class ControlPanel extends controlPanel {
  public ControlPanel(){super();
    super.getButton("add").setToolTipText("To create a source");
    super.getButton("del").setToolTipText("To delete the source");
    super.getButton("up").setToolTipText("To move Up the source");
    super.getButton("down").setToolTipText("To move Down the source");
    super.getButton("edit").setToolTipText("To edit the source");
  }
  protected final void Add(){PlayUI.this.Add();}
  protected final void Del(){PlayUI.this.Del();}
  protected final void Up(){PlayUI.this.Up();}
  protected final void Down(){PlayUI.this.Down();}
  protected final void Edit(){PlayUI.this.Edit();}
}
}
