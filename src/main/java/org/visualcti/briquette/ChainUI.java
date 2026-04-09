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
import javax.swing.event.*;
import org.visualcti.briquette.core.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: the UI for Chain's properties </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

final public class ChainUI implements UI
{
private final static String nameTitle = "The Name of a chain";
private final static String urlTitle = "The URL to the chain";
private final static String paramTitle = "Formal parameters";
/**
 * <visual>
 * Main container of all visuals
 * */
private final JPanel properties = new JPanel( false );
/**
 * <editor>
 * The editor for name of chain
 * */
private final JTextField name = new JTextField( 16 );
/**
 * <editor>
 * The editor of URL to the Chain
 * */
private final JTextField url = new JTextField();
/**
 * <chooser>
 * The button to call URL's dialog
 * */
private final JButton chooseURL = new JButton("...");
/**
 * <editor>
 * The editor for formal parameters
 * */
private final ParametersSetUI params = new ParametersSetUI();
  /**
   * <constructor>
   * */
  public ChainUI()
  {
    this.properties.setLayout(new BoxLayout(this.properties,BoxLayout.Y_AXIS));
    // to add name's editor
    this.properties.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( nameTitle, this.properties);
    this.properties.add( this.name );
    // to add listener
    this.name.getDocument().addDocumentListener( new DocListener(){
      protected final void update() {if ( !isAssign ) nameUpdated();}
    });

    // to add URL's panel
    this.properties.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( urlTitle, this.properties);
    JPanel urlPane = new JPanel(new BorderLayout(), false);
    this.url.setEditable(false);
    urlPane.add(this.url,BorderLayout.CENTER);
    this.url.setFont(UI.titleFont);
    this.url.setToolTipText("The path to the chain");
    this.chooseURL.setMargin(new Insets(1,1,1,1));
    this.chooseURL.setToolTipText("To change the path to the chain");
    urlPane.add(this.chooseURL,BorderLayout.EAST);
    this.properties.add( urlPane );
    // to add listeners
    this.chooseURL.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){ chooseURL();}
    });

    // to add parameters editor
    this.properties.add(Box.createVerticalStrut(4));
    UI_Store.addTitle( paramTitle, this.properties);
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
 * <attribute>
 * The listener of changes
 * */
private NameChangeListener listener = null;
  /**
   * <mutator>
   * To add the listener
   * */
  public final synchronized void addNameChangeListener
      (
      NameChangeListener listener
      )
      throws java.util.TooManyListenersException
  {
    if (this.listener != null) throw new java.util.TooManyListenersException();
    this.listener = listener;
  }
  /**
   * <mutator>
   * To remove the listener
   * */
  public final synchronized void removeNameChangeListener
      (
      NameChangeListener listener
      )
  {
    this.listener = null;
  }
  /** class-listener of name changes */
  public interface NameChangeListener
  {
    void nameChanged(String name);
  }
  /**
   * <action>
   * The chain's name is changed
   * */
  private final synchronized void nameUpdated(){
    if ( this.owner == null) return;
    String name = this.name.getText();
    this.owner.setName( name );
    if (this.listener != null) this.listener.nameChanged(name);
  }
  /**
   * <action>
   * To choose the URL to the chain
   * */
  private final void chooseURL(){
    if ( this.owner == null || this.owner.getParent() == null ) return;
    String url = this.owner.getURL();
    this.url.setText( url == null ? "Local":url );
  }
/**
 * <attribute>
 * The Chain-owner of this UI
 * */
private Chain owner = null;
/**
 * <flag>
 * Is assign in progress
 * */
private transient boolean isAssign = false;
  /**
   * <mutator>
   * To assign a Chain to the UI
   * */
  public void assign(Chain chain)
  {
    this.owner=chain;
    this.name.setText( chain.getName() );
    this.name.setCaretPosition(0);
    String url = chain.getURL();
    this.url.setText( url == null ? "Local":url );
    this.chooseURL.setEnabled( chain.getParent() != null );
    this.params.assign( chain.getParametersSet(), Parameter.Formal.class );
  }
/**
 * <attribute>
 * is UI may change the Target
 * */
private boolean RO = false;
  /**
   * <accessor>
   * is properties ReadOnly
   * */
  public boolean isReadOnly(){return this.RO;}
  /**
   * <mutator>
   * set ReadOnly flag
   * */
  public void setReadOnly(boolean flag){this.RO=flag;}
  /**
  <accessor>
  get access to Operation's Icon
  */
  public final ImageIcon getIcon(){return null;}
  /**
   * <accessor>
   * To get access to UI's Cursor
   * */
  public final Cursor getCursor(){return Cursor.getDefaultCursor();}
  /**
   * <action>
   * To edit asigned operation's properties in a place
   * parameters:
   * place - the place, where will edit
   * */
  public void activate( JPanel place )
  {
    place.removeAll();
    if (place.getLayout() instanceof BorderLayout){}
    else place.setLayout( new BorderLayout() );
    place.add(this.properties,BorderLayout.NORTH);
    place.add(this.params, BorderLayout.CENTER);
    place.add(this.params.getControl(), BorderLayout.SOUTH);
    this.properties.doLayout();this.properties.repaint();
    this.params.doLayout();this.params.repaint();
    place.repaint();
  }
}
