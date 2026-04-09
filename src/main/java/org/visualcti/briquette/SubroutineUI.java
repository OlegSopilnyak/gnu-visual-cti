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
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.visualcti.briquette.core.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The User Interface for adjust Subroutine briquette</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class SubroutineUI extends baseOperationUI
{
  /**
   *
   * <p>Title: Visual CTI Java Telephony Server</p>
   * <p>Description: VisualCTI WorkFlow, class for edit the Subroutine's Entity</p>
   * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
   * <p>Company: Prominic Ukraine Co</p>
   * @author Sopilnyak Oleg
   * @version 1.0
   */
  private final class EntityEditor extends JPanel{
    /**
     * <attribute>
     * The list of Entity types
     */
    private final JComboBox types = new JComboBox();
    /**
     * <attribute>
     * The name of the Entity
     */
    private final JTextField name;
    /**
     * <constructor>
     * To make the Entity's editor
     */
    EntityEditor(){super( false );
      super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      TitledBorder border = new TitledBorder("Entity");
      super.setBorder(border);
      JPanel typePane = new JPanel(new BorderLayout(2,2),false);
      // to make types panel
      super.add( typePane );
      this.types.setEditable(false);
      this.types.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          SubroutineUI.this.changeEntityTo(types.getSelectedItem());
        }
      });
      typePane.add(this.types,BorderLayout.CENTER);
      JButton adjust = new JButton("...");
      adjust.setMargin( new Insets(1,1,1,1) );
      typePane.add(adjust,BorderLayout.EAST);
      adjust.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          SubroutineUI.this.adjustEntity();
        }
      });
      // to add the name's label
      super.add(Box.createVerticalStrut(4));
      this.name = UI_Store.addTitle("???", this);
    }
    /**
     * To setup the types set
     * @param set types set
     */
    void setTypes(String[] set){
      if ( set == null || set.length == 0 ) return;
      DefaultComboBoxModel model = new DefaultComboBoxModel(set);
      this.types.setModel( model );
    }
    /**
     * To select the type
     * @param type to select
     */
    void selectType(String type){this.types.setSelectedItem( type );}
    void setEntityName(String name){
      this.name.setText( name );
      this.revalidate();
    }
  }
/**
 * <attribute>
 * The owner of UI
 * */
private Subroutine owner = null;
/**
 * <attribute>
 * The container of editors
 * */
private final JPanel main = new JPanel(new BorderLayout(2,2));
/**
 * <attribute>
 * The UI for edit Entity's stuffs
 * */
private final EntityEditor entityUI = new EntityEditor();
/**
 * <attributte>
 * The UI for edit the parameters set
 * */
private final ParametersSetUI parameters = new ParametersSetUI(){
  /**
   * <accessor>
   * To get access to formal parameters of this Actual ParametersSet
   * */
  protected final ParametersSet getFormalParameters(){
    if (owner == null || owner.getEntity() == null) return null;
    return owner.getEntity().getFormalParameters();
  }
  protected final java.util.List getAvailabledGroups(){return groups;}
  protected final java.util.List getAvailabledSymbols(){
    return owner.availableSymbols();
  }
};
private ArrayList groups=new ArrayList();
  /**
   * <constructor>
   * To make the Subroutine's UI
   * */
  public SubroutineUI()
  {
    // to add entity editor
    this.main.add(this.entityUI,BorderLayout.NORTH);
    // to add parameters editor
    this.main.add(this.parameters,BorderLayout.CENTER);
    // to add parameters control
    this.main.add(this.parameters.getControl(),BorderLayout.SOUTH);
  }
  /**
   * <action>
   * To change the entity type
   * reaction to this.entityUI.types choose
   * */
  private final void changeEntityTo(Object type){
    if ( this.owner == null ) return;
    this.entityUI.setEntityName(this.owner.getEntity().getName());
  }
/**
 * <UI>
 * User Interface for adjust the Entity
 * */
private EntityUI ui = null;
  /**
   * <action>
   * To adjust the entity
   * reaction to this.entityUI.adjust button
   * */
  private final void adjustEntity(){
    if ( this.owner != null && this.ui != null ) {
      this.ui.editEntity();
      this.entityUI.setEntityName(this.owner.getEntity().getName());
    }
  }
  /**
   * <notify>
   * To notify about Operation assigned
   * */
  protected final void assigned(Operation briquette) {
    if (briquette instanceof Subroutine) {
      this.owner=(Subroutine)briquette;
      if ( this.owner == null ) return;
      Entity entity = this.owner.getEntity();
      this.ui = UI_Store.getUI( entity );
      this.entityUI.types.setEnabled(false);
      String type = entity.getType();
      this.entityUI.setTypes(new String[]{type});
      this.entityUI.selectType( type );
      this.entityUI.setEntityName(this.owner.getEntity().getName());
      this.parameters.assign(this.owner.getActualParameters(),Parameter.Actual.class);
    }
  }
  /**
   * <action>
   * To prepare parts of UI for editing
   * */
  protected final void prepare(){
    if (super.propertiesTree.getLayout() instanceof BorderLayout){}
    else super.propertiesTree.setLayout( new BorderLayout() );
    super.propertiesTree.add(this.main, BorderLayout.CENTER);
    this.ui.initUI( this.main );
    this.entityUI.setEntityName( this.owner.getEntity().getName() );
    this.prepareGroups();
  }
  /**
   * To prepare the groups
   */
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
   * <action>
   * To refresh properties list before visualizing
   * */
  protected final void refreshProperties(){this.main.revalidate();}
}
