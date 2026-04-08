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

import java.util.*;

import org.jdom.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Briquettes, Class for manage the sequence of briquettes</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class BriquettesSequence
{
/**
 * <const>
 * The name of root XML Element
 * */
public final static String ELEMENT = "sequence";
/**
 * <attribute>
 * The Map of briquettes
 * */
private final Map briquettesMap = Collections.synchronizedMap(new HashMap());
/**
 * <attribute>
 * The pool of briquettes
 * */
private final ArrayList sequence = new ArrayList();
  /**
   * <accessor>
   * To get access to sequence's List
   * */
   final List getList(){return this.sequence;}
/**
 * <attribute>
 * The owner of this sequence
 * */
private final Chain chain;
  /**
   * <constructor>
   * */
  public BriquettesSequence(Chain chain)
  {
    this.chain=chain;
  }
  /**
   * <producer>
   * To copy the content from other sequence
   * */
  final void copyFrom(BriquettesSequence seq)
  {
    // to clear the pools
    this.sequence.clear();
    this.briquettesMap.clear();
    // copying
    for(Iterator i=seq.sequence.iterator();i.hasNext();)
    {
      Operation briquette = (Operation)i.next();
      if ( briquette != null ) this.addBriquette(briquette);
    }
  }
  /**
   * <translator>
   * To store the sequence to XML
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    for(Iterator i= this.sequence.iterator();i.hasNext();)
    {
      Operation briquette = (Operation)i.next();
      xml.addContent( briquette.getXML() );
    }
    return xml;
  }
  /**
   * <translator>
   * To restore the sequence from XML
   * */
  public final void setXML(Element xml) throws Exception
  {
    // to clear the pools
    this.sequence.clear();
    this.briquettesMap.clear();
    // to check XML's integrity
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    // To get the pool of briquettes XMLs
    List items = xml.getChildren( Operation.ELEMENT );
    // backward compability, will be deprecated soon :-\
    if ( items.size() == 0 ) items = xml.getChildren("operation");
    // to collect the briquetets from XML
    this.collectBriquettes( items );
    // to link the briquettes
    this.linkBriquettes();
  }
      /**
       * to collect all briquettes from XML's list
       * */
      private final void collectBriquettes( List items ) throws Exception {
        // to iterate a XML elements
        for(Iterator i=items.iterator();i.hasNext();){
          // try to restore a operation from XML's Element
          Operation operation = Toolkit.makeOperation( (Element)i.next() );
          // to check integrity
          if (operation == null) continue;// some wrong :(
          // check a Operation ID's integrity
          String ID = operation.getID();
          if ( ID == null || "".equals(ID) ) continue;// wrong ID
          // to assign the owner
          operation.setOwner( this.chain );
          // to clear the master's flag
          operation.setMaster(false);
          // to add it to sequence
          this.addBriquette( operation );
        }
      }
      /**
       * To link the briquettes (Links, Subroutines)
       * */
      private final void linkBriquettes(){
        // to iterate the sequence's list
        for(Iterator i=this.sequence.iterator();i.hasNext();){
          // process the sequence's briquette
          Operation briquette = (Operation)i.next();
          // to link alive briquettes
          this.makeLinks( briquette );
        }
      }
      /**
       * To make the briquette's links
       * */
      private final void makeLinks( Operation briquette ){
        String linkID[] = briquette.getLinkIDs();
        if (linkID == null || linkID.length == 0) {
          // to links defined
          briquette.clearLinks(); return;
        }
        // to assign defined links
        for(int i=0;i < linkID.length;i++){
          Operation link = (Operation)this.briquettesMap.get(linkID[i]);
          briquette.setLink( i, link );
        }
      }
  /**
   * <accessor>
   * To get access to the briquette by ID
   * */
  public final Operation get(String ID)
  {
    return (ID == null) ? null: (Operation)this.briquettesMap.get(ID);
  }
  /**
   * <mutator>
   * To add operation to the sequence
   * */
  public final synchronized boolean add(Operation briquette)
  {
      String ID = null;
      // check, is briquette may added
      try {
        if ( this.sequence.contains(briquette) ) return false;
        // to make an unique briquette's ID (may throws...)
        briquette.setID( ID=this.uniqueID(briquette) );
      }catch(NullPointerException e){// method's parameter is null
        return false;
      }
      // to assign owner
      briquette.setOwner( this.chain );
      // to clear all links
      briquette.clearLinks();
      // to add operation to sequence
      this.addBriquette( briquette );
      // notify, operation was added successfully
      briquette.added();
      return true;
  }
  /**
   * <mutator>
   * To remove operation from the sequence
   * */
  public final boolean remove(Operation briquette)
  {
      // check, is operations present in chain
      if ( !this.contains(briquette) ) return false;
      // to remove operation from sequence
      this.removeBriquette( briquette );
      // to remove links to this briquette
      for(Iterator i=this.sequence.iterator();i.hasNext();)
          ((Operation)i.next()).removeLinkTo( briquette );
      // operation was removed successfully
      return true;
  }
  /**
   * <checker>
   * Is this sequence contains the briquette?
   * */
  final boolean contains(Operation briquette)
  {
    try{
      return
        this.sequence.contains(briquette) ||
        this.isExistID( briquette.getID() );
    }catch(NullPointerException e){
      return false;
    }
  }
  /**
   * <accessor>
   * To get access to Chain's local Symbols's list
   * Used only in design mode!
   * */
  public final List getLocalSymbols()
  {
    ArrayList locals = new ArrayList();
    // to iterate the List of Operations
    for(Iterator i=this.sequence.iterator();i.hasNext();)
    {
      this.addFromOperation(locals,(Operation)i.next());
    }
    return locals;
  }
  /**
   * to add Symbol from Operation
   * Used only in design mode!
   * */
  private final void addFromOperation(List pool,Operation oper){
    for(Iterator i=oper.getLocalSymbols().iterator();i.hasNext();){
      Object symbol = i.next();
      if ( !pool.contains(symbol) ) pool.add(symbol);
    }
  }
    /**
     * To add briquette to sequence
     * */
    private final void addBriquette(Operation briquette){
      this.sequence.add( briquette );
      this.briquettesMap.put(briquette.getID(),briquette);
    }
    /**
     * to remove briquette from sequence
     * */
    private final void removeBriquette(Operation briquette){
      this.sequence.remove( briquette );
      this.briquettesMap.remove( briquette.getID() );
    }
    /**
     * <producer>
     * To make new operation's ID
     * */
    private final synchronized String uniqueID( Operation briquette ){
      // to make the prefix of new Operation's ID
      String baseID = new StringBuffer( this.chain.getID() )// the ID of Chain
                                .append( ":" )              // delimiter
                                .append( briquette.get_ID_prefix() )// briquette's prefix
                                .toString();
      // finding unique ID for this briquette
      for(int counter = 0; true ;counter++) {
        String ID = new StringBuffer( baseID ).append(counter).toString();
        if ( !this.isExistID(ID) ) return ID;// unique ID
      }
    }
    /**
     * <checker>
     * is operation with ID exists in sequence
     * */
    private final boolean isExistID( String ID ) {
        if ( ID == null) return true;
        return this.briquettesMap.get(ID) != null;
    }
}
