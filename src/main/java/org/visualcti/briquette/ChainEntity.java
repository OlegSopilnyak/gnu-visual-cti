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

import java.util.ArrayList;
import java.util.Iterator;
import org.jdom.Element;
import org.visualcti.server.database.connectionRequest;
import org.visualcti.util.Property;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The entity for Chains</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class ChainEntity implements Entity
{
/**
 * <attribute>
 * The reference to Chain's Library
 */
private ChainsLibrary library=null;
/**
 * <accessor>
 * To get access to chain's library
 * @return
 */
public final ChainsLibrary getLibrary() {return this.library;}
/**
 * <mutator>
 * To attach the Entity to subroutine
 * @param owner the subroutine
 */
public final void attach(Subroutine sub)
{
  if ( sub != null)
    this.library = sub.getOwner().getLibrary();
  else this.detach();
}
/**
 * <mutator>
 * To detach the attached Entity
 */
public final void detach(){this.library=null;}
/**
 * <flag>
 * Is needs to cance the Chain's execution
 * */
private volatile boolean canceled = false;
/**
 * <attribute>
 * Current Operation of the chain
 * */
private Operation current = null;
/**
 * <semaphore>
 * The semaphore for current operation switching
 * */
private final Object SEMAPHORE = new Object();
/**
 * <attribute>
 * The chain for execute the subroutine
 * */
private Chain chain = null;
    /**
     * <accessor>
     * To get access to the chain
     * */
    public final Chain getChain(){return this.chain;}
    /**
     * <mutator>
     * To setup the chain
     * */
    public final void setChain(Chain chain)
    {
      if ((this.chain=chain) != null)
        this.ID = chain.getID(); else this.ID = "???";
    }
/**
 * <attribute>
 * the ID of chain in library
 * */
private volatile String ID = "???";
    /**
    <accessor>
    to get access to chain's ID
    */
    public final String getID(){return this.ID;}
//////////////// ENTITY part ////////////////////
    /**
     * <accessor>
     * To get access to type of the Entity
     * */
    public final String getType(){return "Briquettes chain";}
    /**
     * <accessor>
     * To get access to the name of entity
     * Returns the name of the Chain, ReadOnly
     * */
    public final String getName()
    {
      return this.chain == null ? "???":this.chain.getName();
    }
    /**
     * <accessor>
     * Is this entity is local for subroutine
     * */
    public final boolean isLocal()
    {
      return this.chain == null ? false:this.chain.isLocal();
    }
/**
 * <attribute>
 * The owner of the Entity
 * */
private Subroutine owner = null;
    /**
     * <executor>
     * To execute the enity
     * */
    public final void doIt(Subroutine owner) throws Exception
    {
      // check, is chain assigned
      if ( this.chain == null ) return;
      // let's begin
      this.canceled = false;
      this.owner=owner;
      // to setup main Chain's Operation
      this.current = this.chain.getMainOperation();
      // to iterate the Chain's sequence
      while ( !this.canceled )
      {
        if (this.current == null) break; else Thread.yield();
        // to print the about message
        owner.info( "executing :"+this.current.getAbout() );
        // to execute the operation
        Operation next = this.current.doIt( owner );
        // to setup a next operation
        this.setCurrent( next );
      }
      // to clear reference to the owner
      this.owner=null;
    }
    /**
     * <mutator>
     * To setup current operation
     * */
    private final void setCurrent(Operation current){
      // to setup new value of current Operation
      synchronized(this.SEMAPHORE){
        // to store new current
        this.current = this.canceled ? null:current;
      }
      // to transfer Thread's context to other thread ( multi-thread )
      Thread.yield();
    }
    /**
     * <executor>
     * To cancel Entity's execution
     * */
    public final void cancel()
    {
      synchronized( this.SEMAPHORE)
      {
        // setup the flag
        this.canceled = true;
        // to stop execution of current operation
        if ( this.current != null ) this.current.stopExecute();
      }
    }
    /**
     * <accessor>
     * To get access to formal's parameters of entity
     * */
    public final ParametersSet getFormalParameters()
    {
      return this.chain == null ? null:this.chain.getParametersSet();
    }
    /**
     * <accessor>
     * To get access to request to Database's connection
     * */
    public final connectionRequest getConnectionRequest()
    {
      return this.chain == null ? null:this.chain.getConnectionRequest();
    }
    /**
     * <translator>
     * To store the entity to XML's element
     * */
    public final void store(Element xml)
    {
      xml.addContent( new Property("type",this.getType()).getXML() );
      xml.addContent( new Property("name",this.getName()).getXML() );
      xml.addContent( new Property("libraryID",this.ID).getXML() );
    }
    /**
     * <translator>
     * To restore the entity from XML
     * */
    public final void restore(Element xml) throws Exception
    {
      ArrayList names = new ArrayList( 3 );// for check a duplicates
      // to iterate the properties of subroutine
      for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();)
      {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of entity properties!");
          if ( "type".equals(name) ) {names.add(name);
          }else
          if ( "name".equals(name) ) {names.add(name);
          }else
          if ( "libraryID".equals(name) ) {names.add(name);
              this.ID = property.getValue(this.ID);
          }
      }
    }
}
