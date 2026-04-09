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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.*;

import org.jdom.Element;
import org.visualcti.briquette.*;
import org.visualcti.util.Tools;
import org.visualcti.briquette.core.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, Main frame of the IDE</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public final class IDE extends JFrame
{
/**
 * <attribute>
 * The facade of IDE for visualChain
 * */
private final Facade facade;
  /**
   * <accessor>
   * To get access to IDE's facade
   * @return the reference to facade
   */
  public final Facade getFacade(){return this.facade;}
/**
 * <attribute>
 * The Program, main container of Operations
 * */
private Program programm;
    /**
     * <accessor>
     * To get access to current task
     * @return current task
     */
    public final Program getProgramm(){return this.programm;}
    /**
     * <mutator>
     * To assign the programm to IDE
     * @param programm new current task
     */
    public final void setProgramm(Program programm)
    {
      // to free allocated resources
      if ( this.programm != null )
      {
        this.programm.setEnv(null);
        this.programm.getChain().setEquipment(null);
        this.programm = null;
        this.central.clean();
        System.gc();
      }
      // to setup new programm for IDE
      programm.setEnv( this.runtime.getEnvironment() );
      Chain mainChain = (this.programm=programm).getChain();
      dbTools.refresh( mainChain.getConnectionRequest() );
      mainChain.setEquipment( this.runtime.getEquipment() );
      this.central.clean(); this.central.select( mainChain );
      this.runtime.Reset();
    }
    /**
     * <mutator>
     * To select the Chain (make visible the graph)
     * @param chain the chain to visualize
     */
    public final void openGraph( Chain chain ){this.central.select( chain );}
    /**
     * <mutator>
     * To close the graph of the Chain
     * @param chain the owner of the graph
     */
    public final void closeGraph( Chain chain ){this.central.close( chain );}
/**
 * <attribute>
 * The URL to chain's container
 * */
private String URL = "<None>";
  /**
   * <mutator>
   * To setup new Chain's URL
   * @param url titled URL to the chain
   */
  public final void setChainURL(String url)
  {
    if ( url == null)
    {
      File file = new File(this.programm.getFileName());
      this.URL = file.getName();
    }else  this.URL = url;
    this.updateTitle();
  }
/**
 * <attribute>
 * The name of the chain
 * */
private String chainName = "<?>";
  /**
   * <mutator>
   * To setup new Chain's name
   * @param chainName titled name of the chain
   */
  public final void setChainName(String chainName)
  {
    this.central.setSelectedTitle( this.chainName=chainName );
    this.updateTitle();
  }
/**
 * <format>
 * The format for IDE's Title
 * */
private static final
MessageFormat titleFormat = new MessageFormat("VisualCTI WorkFlow: {0} [{1}]");
  private final void updateTitle() {
      super.setTitle( titleFormat.format(new String[]{this.URL,this.chainName}) );
  }
    /**
     * <main>
     * The main method of the IDE
     * @param args startup arguments
     */
    public static void main(String[] args)
    {
        IDE WorkFlow = new IDE( args );
        WorkFlow.setProgramm( Program.newProrgamm() );
        Tools.print("Show the main frame...");
        WorkFlow.setVisible( true );
    }
/**
 * <attribute>
 * The control panel of the IDE
 */
final ideControl control;
    /**
     * <mutator>
     * To set enabled the action in IDE's control panel
     * @param action the name of action
     * @param enable flag
     */
    public final void setEnabled(String action,boolean enable)
    {
      this.control.setEnabled( action, enable );
    }
/**
 * <attribute>
 * The runtime of the IDE
 */
final ideRuntime runtime;
/**
 * <const>
 * The size of IDE's frame
 */
private static final Dimension size = new Dimension(800, 600);

    /**
     * <constructor>
     * To make the Frame and all panels to serve
     * @param args startup parameters
     */
    private IDE(String []args)
    {
        // to make the IDE's facade
        this.facade = new Facade( this );
        // to make the processor for IDE's commands
        this.processor = new ideProcessor( this );
        // to make the Runtime part of the IDE
        Element runtimeXML = Config.getPart(Config.EQUIPMENT_PART);
        this.runtime = new ideRuntime( this );
        try{this.runtime.getEquipment().setXML(runtimeXML);
        }catch(Exception e){
          e.printStackTrace();
        }
        // to adjust visible parameters
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setSize( IDE.size );
        // To center the frame on the screen
        Dimension screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int X = (screen.width - IDE.size.width)/2;
        int Y = (screen.height-IDE.size.height)/2;
        if (X < 0) {X=Y=0;this.setSize(640,480);}
        this.setLocation(X, Y);
        // to make & add IDE's control panel
        Element controlXML = Config.getPart(Config.CONTROLS_PART);
        this.control = new ideControl( this, controlXML );
        this.getContentPane().add( control, BorderLayout.NORTH );
        // to add briquettes panel
        Element briquettesXML = Config.getPart(Config.BRIQUETTES_PART);
        briquettesControl briquettes = new briquettesControl( this, briquettesXML );
        this.getContentPane().add( briquettes, BorderLayout.SOUTH );
        // to add central panel
        this.central = new centralControl( this, this.facade );
        this.getContentPane().add( this.central, BorderLayout.CENTER );
        // to add listener for Window closing's event
        this.addWindowListener(new WindowAdapter(){
            public final void windowClosing(WindowEvent e){IDE.this.exitIDE();}
        });
    }
    /**
     * <accessor>
     * To get access to current visualChain
     * @return current graph of the chain
     */
    final visualChain getChain(){return this.central.getGraph();}
/**
 * <attribute>
 * The processor for execute a commands
 * */
private final ideProcessor processor;
    /**
     * <notify>
     * To notify about action from ide's control
     * @param action the name of action to do
     */
    final void doAction(String action) {this.processor.doAction(action);}
/**
<control>
Main control of chain's graph and editor
*/
private centralControl central;
    /**
     * <transform>
     * To store programm design information
     * @param xml the design's xml
     */
    public final void storeDesign(Element xml)
    {
    }
    /**
     * <transform>
     * To to restore programm design information
     * @param xml the design's xml
     */
    public final void reStoreDesign(Element xml)
    {
      if (xml == null) return;
    }
    /**
     * <exit>
     * To do some action before finish the IDE
     */
    private final void exitIDE()
    {
        System.exit(0);
    }
    /** initialization */
    static
    {
      //IDE.initLF();
      Config.initialize();
    }
    private final static void initLF(){
      try{
        String name = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
        Class lfClass = Class.forName(name,true,IDE.class.getClassLoader());
        LookAndFeel lf = (LookAndFeel)lfClass.newInstance();
        if ( lf != null ) UIManager.setLookAndFeel(lf);
        //UIManager.setLookAndFeel(new com.l2fprod.gui.plaf.skin.SkinLookAndFeel());
      }catch(Exception e){
        e.printStackTrace();
      }
    }
}
