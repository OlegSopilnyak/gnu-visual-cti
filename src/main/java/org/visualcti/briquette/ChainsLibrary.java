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


import java.util.*;

import org.jdom.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Briquettes, The library, set of chains </p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class ChainsLibrary
{
/**
 * <const>
 * The name of root xml element
 * */
public static final String ELEMENT = "library";
/**
 * <const>
 * The prefix of library chain's ID
 * */
public static final String ID_prefix = "Library.chain.";
/**
 * <attribute>
 * The owner of this library
 * */
private final Chain chain;
/**
 * <attribute>
 * The Map of chains
 * */
private final Map chainsMap = Collections.synchronizedMap(new HashMap());

  /**
   * <attribute>
   * The list of chains, for XML's store restore only (the order)
   * */
  private final ArrayList library = new ArrayList();

  /**
   * <accessor>
   * To get access to library's Chain by ID
   * */
  public final Chain getChain(String chainID)
  {
    return (Chain)this.chainsMap.get( chainID );
  }
  /**
   * <constructor>
   * */
  public ChainsLibrary(Chain chain)
  {
    this.chain=chain;
  }
  /**
   * <accessor>
   * To get access to local library's chains
   * */
  public final List getLocal()
  {
    ArrayList list = new ArrayList();
    for(Iterator i=this.library.iterator();i.hasNext();)
    {
      Chain chain = (Chain)i.next();
      if ( chain.isLocal() ) list.add( chain );
    }
    return list;
  }
  /**
   * <accessor>
   * To get access to external library's chains
   * */
  public final List getExternal()
  {
    ArrayList list = new ArrayList();
    for(Iterator i=this.library.iterator();i.hasNext();)
    {
      Chain chain = (Chain)i.next();
      if ( !chain.isLocal() ) list.add( chain );
    }
    return list;
  }
  /**
   * <producer>
   * To copy the library
   * */
  final void copyFrom(ChainsLibrary lib)
  {
    // to clear the pools
    this.chainsMap.clear(); this.library.clear();
    // copying...
    for(Iterator i=lib.library.iterator();i.hasNext();)
    {
      Chain chain = (Chain)i.next();
      if ( chain != null ) this.addChain( chain );
    }
  }
  /**
   * <translator>
   * To store the library to XML
   * */
  public final Element getXML()
  {
    Element xml = new Element( ELEMENT );
    for(Iterator i=this.library.iterator();i.hasNext();)
      xml.addContent( ((Chain)i.next()).getXML() );
    return xml;
  }
  /**
   * <translate>
   * To restore the library from XML
   * */
  public final void setXML(Element xml) throws Exception
  {
    // to clear the pools
    this.chainsMap.clear(); this.library.clear();
    // to check XML's integrity
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    // restoring the sequence
    for(Iterator i=xml.getChildren(Chain.ELEMENT).iterator();i.hasNext();)
    {
      Chain chain = Toolkit.restoreChain( (Element)i.next(), this.chain );
      try {
        if ( chain.getID().equals("") ) continue;
      }catch(NullPointerException e){
        continue;// chain of chain.ID is null
      }
      this.addChain( chain );
    }
  }
  /**
   * <mutator>
   * To add new chain to library
   * */
  public final boolean add( Chain chain)
  {
    // check, is chain belongs the this.chain
    if ( !chain.getParent().equals(this.chain) ) return false;
    synchronized ( this.library )
    {
      if ( chain == null || this.library.contains(chain) )return false;
      chain.setID( this.uniqueID() ); this.addChain( chain );
      return true;
    }
  }
  /**
   * <mutator>
   * To remove the chain
   * */
  public final boolean remove( Chain chain )
  {
    synchronized( this.library )
    {
      if ( !this.library.contains(chain) )return false;
      this.removeChain( chain );
      this.clearEntities( chain );
      return true;
    }
  }
    /**
     * To make unique ID
     * */
    private final String uniqueID(){
      for( int counter=0;true;counter++ ){
        StringBuffer sb = new StringBuffer( ID_prefix ).append( counter );
        String chainID = sb.toString();
        // check new ID
        if ( !this.chainsMap.containsKey( chainID) ) return chainID;
      }
    }
    /**
     * To add the Chain
     * */
    private final void addChain(Chain chain){
      synchronized( this.library ){
        this.chainsMap.put(chain.getID(), chain);
        this.library.add( chain );
      }
    }
    /**
     * To remove the chain
     * */
    private final void removeChain(Chain chain){
      synchronized( this.library ){
        this.chainsMap.remove( chain.getID() );
        this.library.remove( chain );
      }
    }
    /**
     * To clear a Subroutine entities
     * */
    private final void clearEntities(Chain chain){
      List briquettes = this.chain.getSequence().getList();
      for(Iterator i=briquettes.iterator();i.hasNext();){
        try{
          Subroutine sub = (Subroutine)i.next();
          ChainEntity entity = (ChainEntity)sub.getEntity();
          if ( chain.equals(entity.getChain()) ) entity.setChain( null );
        }catch(ClassCastException e){
          continue;
        }
      }
    }
}
