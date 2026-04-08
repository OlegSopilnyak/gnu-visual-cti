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
package org.visualcti.briquette;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jdom.*;
import org.visualcti.server.database.*;
import org.visualcti.util.Property;
import org.visualcti.briquette.core.*;
/**
$Header: /VisualCTI_project/src/org/visualcti/briquette/Chain.java 52    22.02.03 13:39 Olegs $
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The chain of briquettes</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
final public class Chain
{
/**
 * <const>
 * chain XML element name
 * */
public static final String ELEMENT = "chain";
    /**
    <translator>
    To represent the chain as XML element
    */
    public final Element getXML()
    {
        Element xml = new Element( ELEMENT );
        // to store base information
        this.storeBase( xml );
        // check is an external chain (for library's entries)
        if ( !this.isLocal() ) return xml;// external chain
        // to store database connection's request (only for root chain)
        if ( this.parent == null ) xml.addContent( this.request.getXML() );
        // to store Chain's formal parameters
        xml.addContent( this.formalParameters.getXML() );
        // to store the sequence of Operations
        xml.addContent( this.sequence.getXML() );
        // to store library
        xml.addContent( this.library.getXML() );
        // to store breakpoints
        xml.addContent( this.breakpointsXML() );
        return xml;
    }
      /**
       * <translator>
       * To store the base information to XML's element
       * */
      private final void storeBase(Element xml){
        // to store the name
        xml.addContent( new Property("name",this.name).getXML() );
        // to store chain's ID
        xml.addContent( new Property("ID",this.ID).getXML() );
        // check, is this external chain from library
        if ( !this.isLocal() ){// this is external chain
          // to store the URL to external Chain's body
          xml.addContent( new Property( "URL", this.getURL() ).getXML() );
        } else {
          // to store main Operation
          if ( this.mainBriquette != null )
            xml.addContent( new Property("main",this.mainBriquette.getID()).getXML() );
        }
      }
      /**
       * <translator>
       * To store the breakpoints to XML
       * @return the XML
       */
      private final Element breakpointsXML(){
        Element xml = new Element(BreakPoint.XMLPOOL);
        for(Iterator i=this.breakpoints.iterator();i.hasNext();){
          BreakPoint point = (BreakPoint)i.next();
          xml.addContent( point.getXML() );
        }
        return xml;
      }
    /**
    <translator>
    to restore chain from XML element
    */
    public final Chain setXML(Element xml) throws Exception
    {
        // to check this XML's integrity
        if ( xml==null || !ELEMENT.equals(xml.getName()) )
            throw new Exception("Not a "+ELEMENT+"'s Element!");
        // to solve base properties of chain
        String url = this.restoreBase( xml );
        // to restore the Chain's body
        if ( url == null ){// to restore local Chain's body
          this.restoreLocalChain(xml);
        }else {               // to restore Chain from external URL
          this.restoreExternalChain(url);
        }
        return this;
    }
      /**
       * <restore>
       * to restore a Base chain's properties
      */
      private final String restoreBase(Element xml) throws Exception{
        // to clear the values
        this.ID=""; this.name="Unknown"; this.mainID="";
        String URL = null;
        // to define the array of Properties name
        ArrayList names = new ArrayList( 4 );// for check a duplicates
        for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();)
        {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          if (name == null)
            throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of base properties!");
          // to solve the names of property
          if ( "ID".equals( name ) ) {
              this.ID = property.getValue( this.ID );names.add(name);
          }else
          if ( "URL".equals( name ) ) {
              URL = property.getValue( URL );names.add(name);
          }else
          if ( "name".equals( name ) ) {
              this.name = property.getValue( this.name );names.add(name);
          }else
          if ( "main".equals( name ) ) {
              this.mainID = property.getValue( this.mainID );names.add(name);
          }
        }
        return URL;
      }
      /**
       * <restore>
       * To restore local Chain body
       * */
      private final void restoreLocalChain(Element xml) throws Exception {
        this.request = new connectionRequest();// to clear the attribute
        // to restore a Database connection's properties
        this.request.setXML( xml.getChild(connectionRequest.ELEMENT) );
        // to restore formal parameters
        this.formalParameters.setXML( xml.getChild(ParametersSet.ELEMENT) );
        // to restore sequence of operations
        this.sequence.setXML( xml.getChild(BriquettesSequence.ELEMENT) );
        // to setup main operation of the chain
        this.mainBriquette = this.sequence.get( this.mainID );
        if (this.mainBriquette != null) this.mainBriquette.setMaster(true);
        // to restore the Chain's library
        this.library.setXML( xml.getChild(ChainsLibrary.ELEMENT) );
        // to link an entities of Subroutines
        this.linkSubroutines();
        // to restore breakpoints
        this.restoreBreakpoints( xml.getChild(BreakPoint.XMLPOOL) );
      }
      /**
       * <restore>
       * To link Subroutines with chains (s)
       * */
      private final void linkSubroutines() {
        // to iterate operations list
        for (Iterator i=this.sequence.getList().iterator();i.hasNext();){
          try{
            Subroutine sub = (Subroutine)i.next();
            ChainEntity entity = (ChainEntity)sub.getEntity();
            entity.setChain( this.library.getChain(entity.getID()) );
            sub.setEntity( entity );
          }catch(ClassCastException e){
          }
        }
      }
      /**
       * <restore>
       * To restore breakpoints list
       * @param xml container of breakpoints
       */
      private final void restoreBreakpoints(Element xml){
        this.breakpoints.clear();
        if ( xml == null ) return;
        List xmls = xml.getChildren(BreakPoint.ELEMENT);
        for(Iterator i=xmls.iterator();i.hasNext();){
          BreakPoint point = BreakPoint.restore((Element)i.next(),this);
          if ( point != null ) this.breakpoints.add(point);
        }
      }
      /**
       * <restore>
       * To restore the Chain from external URL
       * */
      private final void restoreExternalChain(String url) throws Exception{
        String ID = this.ID /*, url= this.URL*/;
        // get the chain's body from an URL
        Chain body = Toolkit.getChain(url, this.parent);
        if (body != null) body.copyTo( this );
        // to restore parameters from XML
        this.ID = ID; //this.URL = url;
      }
      /**
       * <copy>
       * to copy chain attributes to other chain
       * */
      private final void copyTo(Chain other) {
          other.ID        = this.ID;
          other.request   = this.request;
          other.mainBriquette      = this.mainBriquette;
          other.mainID    = this.mainID;
          other.name      = this.name;
          other.parent    = this.parent;
          other.formalParameters = this.formalParameters;
          other.library.copyFrom ( this.library );
          other.sequence.copyFrom( this.sequence );
      }
//////////////////// END of XML-part ////////////////////////////
  /**
   * <accessor>
   * To get access to predefined Symbols
   * Used only in design mode!
   * */
  public final List getPredefinedSymbols(){return Program.getPredefinedSymbols();}
  /**
   * <accessor>
   * To get access to available Database's columns Symbols
   * Used only in design mode!
   * */
  public final List getDatabaseColumnSymbols()
  {
    dbMetaData meta = dbTools.getMetaData( this.getConnectionRequest() );
    return meta.columns;
  }
  /**
   * <accessor>
   * To get access to Chain's local Symbols's list
   * Used only in design mode!
   * */
  public final List getLocalSymbols()
  {
    ArrayList localSymbols = new ArrayList( this.formalParameters.getSymbols() );
    // to add from the sequence
    for(Iterator i=this.sequence.getLocalSymbols().iterator();i.hasNext();)
    {
      Object symbol = i.next();
      if ( !localSymbols.contains(symbol) ) localSymbols.add(symbol);
    }
    return localSymbols;
  }
//////// END of transient part /////////////
/**
 * <attribute>
 * The ID of main operation
 * */
private String mainID="";

/**
 * <attribute>
 * The database connection description
 * */
private connectionRequest request = new connectionRequest();
    /**
    <accessor>
    get access to chain's request
    */
    public final connectionRequest getConnectionRequest(){
      if (this.isLocal() && parent != null)
        return parent.getConnectionRequest();
      else
        return this.request;
    }
/**
 * <attribute>
 * The source of the Chain, for store/restore the Chain
 * */
private final Source source;
    /**
     * <accessor>
     * To get access to Source of the Chain
     * */
    public final Source getSource(){return this.source;}
    /**
     * <accessor>
     * To get access to the URL to the chain
     * */
    public final String getURL()
    {
      if ( this.isLocal() ) return null;
      return this.source.getPath().toString();
    }
    /**
    <accessor>
    to get access to local flag
    */
    public final boolean isLocal()
    {
      return  this.source == null ||
              this.source instanceof Program.Source;
    }
/**
 * <attribute>
 * The parent of this chain
 * If parent is null this is Main chain of programm
 * */
 private Chain parent;
    /**
     * <accessor>
     * To get access to parent of this chain
     * */
    public final Chain getParent() {return this.parent;}
    /**
     * <constructor>
     * To make The chain without Start/Stop briquettes
     * */
    public Chain(Chain parent, Source source){this(parent,source,false);}
    /**
     * <constructor>
     * To make the chain with Start/Stop briquettes,
     * if isNewLocal flag selected
     * */
    public Chain(Chain parent, Source source, boolean isNewLocal)
    {
      if ((this.parent=parent) == null) this.ID = "Main-chain";
      this.source = source;
      this.sequence = new BriquettesSequence( this );
      this.library = new ChainsLibrary( this );
      if ( isNewLocal )
      {
        Operation begin = new org.visualcti.briquette.control.TrafficLight();
        Operation end = new org.visualcti.briquette.control.Return();
        this.sequence.add(begin); this.sequence.add(end);
        end.move(new java.awt.Point(0,50));
        begin.setLink(Operation.DEFAULT_LINK,end);
      }
    }
/**
 * <attribute>
 * The parameters set
 * */
private ParametersSet formalParameters = new ParametersSet("Formal parameters of the Chain of Operations");
    /**
     * <accessor>
     * to get access to parameters set
     * */
    final public ParametersSet getParametersSet(){return this.formalParameters;}
/**
 * <attribute>
 * The Chain's ID
 * */
private volatile String ID="";
    /**
    <accessor>
    to get access to chain's ID
    */
    public final String getID(){return this.ID;}
    /**
     * <mutator>
     * To setup the ID
     * */
    final void setID(String ID) {this.ID = ID;}
/**
<attribute>
The name of the chain of operations
*/
private volatile String name = "Unknown";
    /**
    <accessor>
    to get access to chain's name
    */
    public final String getName(){return this.name;}
    /**
    <mutator>
    to set the chain's name
    */
    public final void setName(String name){this.name=name;}
/**
 * <attribute>
 * The description of the chain of operations
 * */
private volatile String description = "Unknown";
    /**
     * <accessor>
     * to get access to chain's description
     * */
    public final String getDescription(){return this.description;}
/**
 * <attribute>
 * The sequence of briquettes
 * */
private final BriquettesSequence sequence;
    /**
     * <accessor>
     * To get access to Chain's briquettes sequence
     * */
    public final BriquettesSequence getSequence(){return this.sequence;}
/**
 * <attribute>
 * The library, set of chains
 * */
private final ChainsLibrary library;
    /**
    <accessor>
    to get access to chain's library
    */
    public final ChainsLibrary getLibrary(){return this.library;}
    /**
     * <accessor>
     * To get access to list of briquettes
     * */
    public final List getOperationsList()
    {
      return new ArrayList( this.sequence.getList() );
    }
/**
 * <attribute>
 * The begin of operators chain
 * */
private volatile Operation mainBriquette = null;
    /**
     * <accessor>
     * To get access to main Operation of a chain
     * */
    public final Operation getMainOperation(){return this.mainBriquette;}
    /**
     * <mutator>
     * To setting up new begin operator
     * */
    public final void setMainOperation(Operation briquette)
    {
        if ( this.sequence.contains(briquette) )
        {
            if (this.mainBriquette != null) this.mainBriquette.setMaster( false );
            this.mainBriquette = briquette;
        }
    }
/**
 * <attribute>
 * The reference to Chain's equipment
 */
private Equipment equipment = null;
  /**
   * <accessor>
   * To get access to chain's equipment
   * Used only in design mode!
   * @return the equipment
   */
  public final Equipment getEquipment()
  {
    return this.parent == null ? this.equipment:this.parent.getEquipment();
  }
  /**
   * <mutator>
   * To assign the equipment to the Chain
   * Used only in design mode!
   * @param equipment chain's equipment
   */
  public final void setEquipment(Equipment equipment)
  {
    this.equipment = equipment;
  }
    /**
     * <p>Title: Visual CTI Java Telephony Server</p>
     * <p>Description: VisualCTI WorkFlow, The source of the chain's XML</p>
     * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
     * <p>Company: Prominic Ukraine Co</p>
     * @author Sopilnyak Oleg
     * @version 1.0
     */
    public interface Source
    {
      /**
       * <accessor>
       * To get access to path to Chain's XML
       * @return the URL to chain's body
       */
      URL getPath();
      /**
       * <accessor>
       * To get access to InputStream for restore the Chain
       * @return the InputStream to XML to restore
       * @throws IOException if can't get it
       */
      InputStream getInputStream() throws IOException;
      /**
       * <accessor>
       * To get access to OutputStream for XML's store the Chain
       * @return the OutputStream to store
       * @throws IOException if can't get it
       */
      OutputStream getOutputStream() throws IOException;
    }

/**
 * <pool>
 * The list of Chain's breakpoints
 */
private final List breakpoints = new ArrayList();
/**
 * <accessor>
 * To get access to breakpoints' list
 * @return the list
 */
    public final List getBreakpoints(){return this.breakpoints;}
    /**
     * <mutator>
     * To add breakpoint to a Chain
     * @param oper the briquette
     * @return true if success
     */
    public final boolean addBreakPoint(Operation oper)
    {
      return this.addBreakPoint(oper.getID(),true);
    }
    /**
     * <mutator>
     * To add breakpoint to a Chain
     * @param ID briquette's ID
     * @param enabled flag
     * @return true if success
     */
    public final boolean addBreakPoint(String ID,boolean enabled)
    {
      if ( this.find(ID) != null || this.sequence.get(ID) == null) return false;
      this.breakpoints.add( new BreakPoint(ID,enabled) );
      return true;
    }
    /**
     * <mutator>
     * To update the breapoint's values
     * @param ID briquette's ID
     * @param enabled flag's value
     * @return true if success
     */
    public final boolean updateBreakPoint(String ID, boolean enabled)
    {
      BreakPoint point = this.find(ID);
      if ( point != null ) point.enabled = enabled;
      return point != null;
    }
    /**
     * <mutator>
     * To delete the breakpoint
     * @param ID briquette's ID
     */
    public final void deleteBreakPoint(String ID)
    {
      for(ListIterator i=this.breakpoints.listIterator();i.hasNext();)
      {
        BreakPoint point = (BreakPoint)i.next();
        if ( point.OperationID.equals(ID) ) i.remove();
      }
    }
    private final BreakPoint find(String ID){
      for(Iterator i=this.breakpoints.iterator();i.hasNext();){
        BreakPoint point = (BreakPoint)i.next();
        if ( point.OperationID.equals(ID) ) return point;
      }
      return null;
    }
    /**
     * <p>Title: Visual CTI Java Telephony Server</p>
     * <p>Description: VisualCTI WorkFlow, <br>
     * The wraper of Chain's operation breakpoint</p>
     * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
     * <p>Company: Prominic Ukraine Co</p>
     * @author Sopilnyak Oleg
     * @version 1.0
     */
    public static final class BreakPoint
    {
      public BreakPoint(String ID,boolean enabled)
      {
        this.OperationID=ID;this.enabled=enabled;
      }
      public static final String ELEMENT="break";
      public static final String XMLPOOL="breakpoints";
      private String OperationID;
      public final String getOperationID(){return this.OperationID;}
      private boolean enabled;
      public final boolean isEnabled(){return this.enabled;}
      public final void setEnabled(boolean enabled){this.enabled = enabled;}
      public final boolean equals(Object o)
      {
        if ( o == this ) return true;
        if ( o == null ) return false;
        try{
          BreakPoint b = (BreakPoint)o;
          return b.OperationID.equals(this.OperationID);
        }catch(ClassCastException e){}
        return false;
      }
      public final Element getXML()
      {
        Element xml = new Element(ELEMENT);
        xml.setAttribute(new Attribute("ID",this.OperationID));
        xml.setAttribute(new Attribute("enabled",String.valueOf(this.enabled)));
        return xml;
      }
      public static final BreakPoint restore(Element xml, Chain chain){
        if ( xml == null || !xml.getName().equals(ELEMENT) ) return null;
        String ID = xml.getAttributeValue("ID");
        if ( chain.sequence.get(ID) == null ) return null;
        String enabled = xml.getAttributeValue("enabled");
        if ( enabled == null ) return null;
        return new BreakPoint( ID, enabled.equalsIgnoreCase("true") );
      }
    }
}
