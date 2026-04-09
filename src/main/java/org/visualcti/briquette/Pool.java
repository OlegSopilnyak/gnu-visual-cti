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
/**
<store>
Class for represent values pool
*/
final public class Pool
{
/**
 * <listener>
 * The listener of Pool's changes
 * This listner will use only in design mode
 * */
public interface Listener {
  /**
   * <notify>
   * Notified, when the value in pool is changed
   * */
  void poolChanged(Event event);
}
/**
 * The collection of listeners
 * */
private final ArrayList listeners = new ArrayList(2);
/**
 * <mutator>
 * To add Pool's listener
 * */
public final void addPoolListener(Pool.Listener listener)
{
  synchronized(this.listeners)
  {
    if (listener != null) this.listeners.add(listener);
  }
}
/**
 * <mutator>
 * To remove Pool's listener
 * */
public final void removePoolListener(Pool.Listener listener)
{
  synchronized(this.listeners)
  {
    if (listener != null) this.listeners.remove(listener);
  }
}
/**
 * <notify>
 * To notify listeners about Pool's changes
 * */
private final void changedEvent(Event event){
  synchronized(this.listeners) {
    if (this.listeners.size() <= 0) return;
    for(ListIterator i= this.listeners.listIterator();i.hasNext();){
      Pool.Listener listener = (Pool.Listener)i.next();
      if (listener == null) i.remove();
      else listener.poolChanged( event );
    }
  }
}
/**
Pool of common symbols
*/
private final Map system = Collections.synchronizedMap( new HashMap() );
/**
Pool of database symbols
*/
private final Map database = Collections.synchronizedMap( new HashMap() );
/**
Pool of database's columns symbols
*/
private final Map dbcolumns = Collections.synchronizedMap( new HashMap() );
/**
Pool of auto symbols
*/
private final Map auto = Collections.synchronizedMap( new HashMap() );
/**
<attribute>
The owner of the Pool
*/
private Subroutine owner;
    /**
    <constructor>
    */
    public Pool(Subroutine owner){
      this.owner=owner;
      // to place predefined System symbol
      Symbol copyright = Symbol.newSystem("Copyright (c)",Symbol.STRING);
      this.system.put(copyright,"Prominic Technologies Inc. & Prominic Ukraine Co.");
    }
/**
 * <event>
 * Class for solve the event
 * */
public static final class Event
{
  /**
   * <const>
   * Some part of pool cleared
   * */
  public static final int CLEAR = 1;
  /**
   * <const>
   * Symbol modified or added or removed
   * */
  public static final int MODIFY = 2;
  /**
   * Event's type
   * */
  private final int ID;
  public final int getID(){return this.ID;}
  /**
   * group, where event is occur
   * */
  private final int group;
  public final int getGroup(){return this.group;}
  /**
   * Modified Symbol
   * */
  private final Symbol who;
  public final Symbol getWho(){return this.who;}
  /**
   * The changed pool
   * */
  private final Pool pool;
  public final Pool getPool(){return this.pool;}
  /**
   * <constructor>
   * */
  private Event(int ID,int group,Symbol who, Pool pool){
    this.ID=ID;this.group=group;this.who=who;this.pool=pool;
  }
  public final String toString(){
    StringBuffer sb = new StringBuffer("Pool event ");
    String type = this.ID == Event.CLEAR ? "Clear:":(this.ID==Event.MODIFY)?"Modify:":"???";
    sb.append(type).append(" Group:").append(this.group);
    sb.append(" Symbol:").append(this.who);
    return sb.toString();
  }
}
/**
 * <producer>
 * To create a clear event
 * */
private final Event clearEvent(int group){
  return new Event(Event.CLEAR,group,null,this);
}
/**
 * <producer>
 * To create a modify event
 * */
private final Event modifyEvent(int group,Symbol who){
  return new Event(Event.MODIFY,group,who,this);
}
    /**
    <clear>
    to clear the values all group's pool
    */
    public final void clearAll()
    {
        this.auto.clear();
        this.database.clear();
        this.dbcolumns.clear();
        this.system.clear();
        this.changedEvent( this.clearEvent(Symbol.ALL) );
    }
    /**
    <clear>
    to clear the values in group's pool
    */
    public final void clear(int groupID)
    {
        switch( groupID )
        {
            case Symbol.USER:// the local value
                this.auto.clear();
                this.changedEvent( this.clearEvent(Symbol.USER) );
                break;
            case Symbol.DATABASE:// the database value
                this.database.clear();
                this.changedEvent( this.clearEvent(Symbol.DATABASE) );
                break;
            case Symbol.DBCOLUMN:// the database's column value
                this.dbcolumns.clear();
                this.changedEvent( this.clearEvent(Symbol.DBCOLUMN) );
                break;
            case Symbol.SYSTEM:// the system value
                this.system.clear();
                this.changedEvent( this.clearEvent(Symbol.SYSTEM) );
                break;
        }
    }
    /**
    <store>
    To store symbol's value to pool
    */
    public final void set(Symbol name,Object value)
    {
        if ( owner.getEntity() == null || name.isConst() ) return;
        // to solve name by Symbol's group
        switch( name.getGroupID() )
        {
            // the local value
            case Symbol.USER:
                this.put( this.auto, name, value );
//System.out.println("Sub:["+this.owner.callStack()+"] sym:"+name.getName()+"="+value);
                this.changedEvent( this.modifyEvent(Symbol.USER,name) );
                break;

            // the database value
            case Symbol.DATABASE:
                if ( !owner.getEntity().isLocal() )
                    // external chain have no access to owner database's stuffs
                    this.put( this.database, name, value );
                else
                if ( owner.getCaller() != null )
                    // to store symbol's value to owner (if exists owner)
                    owner.getCaller().set( name, value);
                else
                    // no owner exists, store value to local database pool
                    this.put( this.database, name, value );
                this.changedEvent( this.modifyEvent(Symbol.DATABASE,name) );
                break;

            // the database's column value
            case Symbol.DBCOLUMN:
                if ( !owner.getEntity().isLocal() )
                    // external chain have no access to owner database's stuffs
                    this.put( this.dbcolumns, name, value );
                else
                if ( owner.getCaller() != null )
                    // to store symbol's value to owner (if exists owner)
                    owner.getCaller().set( name, value);
                else
                    // no owner exists, store value to local database pool
                    this.put( this.dbcolumns, name, value );
                this.changedEvent( this.modifyEvent(Symbol.DBCOLUMN,name) );
                break;

            // the system value
            case Symbol.SYSTEM:
                if ( owner.getCaller() != null )
                    // to store symbol's value to owner (if exists owner)
                    owner.getCaller().set( name, value);
                else
                    // no owner exists, store value to local system pool
                    this.put( this.system, name, value );
                this.changedEvent( this.modifyEvent(Symbol.SYSTEM,name) );
                break;
        }
    }
    /**
     * <mutator>
     * To setup the Pool's HashMap
     * */
    private final void put(Map pool,Symbol key,Object value){
      if ( value != null) pool.put( key, value );
      else  pool.remove( key );
    }
    /**
    <restore>
    To get symbol's value from pool
    */
    public final Object get(Symbol name)
    {
        if (owner.getEntity() == null) return null;// no chain assosiated with subroutine
        // to calculate the const
        if ( name.isConst() )
        {
            Object value = null;
            switch( name.getTypeID() )
            {
                case Symbol.NUMBER:
                    try {
                        double doubleNumber = Double.valueOf( name.getName() ).doubleValue();
                        long longNumber=(long)doubleNumber;// Try to present's it as Long
                        value = (double)longNumber==doubleNumber ?
                                (Object)new Long(longNumber):
                                (Object)new Double(doubleNumber);
                    }catch(NumberFormatException e){
                        value = new Double(0);
                        name.setName( value.toString() );
                    }
                    break;
                case Symbol.STRING:// the constant is string
                    value = name.getName();
                    break;
            }
            return value;
        }
        // to solve the symbol's group
        switch( name.getGroupID() )
        {
            case Symbol.USER:// the local value
                return this.auto.get( name );
            case Symbol.DATABASE:// the database value
                if ( !owner.getEntity().isLocal() )
                    // external chain have no access to owner database's stuffs
                    return this.database.get( name );
                else
                if ( owner.getCaller() != null )
                    // get symbol's value from owner (if exists owner)
                    return owner.getCaller().get(name);
                else
                    // no owner exists, try to get from local database pool
                    return this.database.get(name);
            case Symbol.DBCOLUMN:// the database's column value
                if ( !owner.getEntity().isLocal() )
                    // external chain have no access to owner database's stuffs
                    return this.dbcolumns.get( name );
                else
                if ( owner.getCaller() != null )
                    // get symbol's value from owner (if exists owner)
                    return owner.getCaller().get(name);
                else
                    // no owner exists, try to get from local database pool
                    return this.dbcolumns.get(name);
            case Symbol.SYSTEM:// the system value
                if ( owner.getCaller() != null )
                    // get symbol's value from owner (if exists owner)
                    return owner.getCaller().get(name);
                else
                    // no owner exists, try to get from local system pool
                    return this.system.get(name);
        }
        return null;
    }
    /**
     * <accessor>
     * To get acces to ALL Pool's symbols
     * */
    public final List symbolsAll()
    {
      ArrayList list = new ArrayList();
      list.addAll( this.symbolsGroup(Symbol.USER) );
      list.addAll( this.symbolsGroup(Symbol.DATABASE) );
      list.addAll( this.symbolsGroup(Symbol.DBCOLUMN) );
      list.addAll( this.symbolsGroup(Symbol.SYSTEM) );
      return list;
    }
    /**
     * <accessor>
     * To get access to List of Symbols by group's ID
     * */
    public final List symbolsGroup(int groupID)
    {
      ArrayList list = new ArrayList();
      switch(groupID)
      {
        // local values
        case Symbol.USER:
          this.fill( list, this.auto );
          break;

        // database values
        case Symbol.DATABASE:
          if ( !owner.getEntity().isLocal() )
              // external chain have no access to owner database's stuffs
              this.fill( list, this.database );
          else
          if ( owner.getCaller() != null )
              // get symbol's value from owner (if exists owner)
              return owner.getCaller().getPool().symbolsGroup( Symbol.DATABASE );
          else
              // no owner exists, try to get from local database pool
              this.fill( list, this.database );
          break;

        // the database's column value
        case Symbol.DBCOLUMN:
          if ( !owner.getEntity().isLocal() )
              // external chain have no access to owner database's stuffs
              this.fill( list, this.dbcolumns );
          else
          if ( owner.getCaller() != null )
              // get symbol's value from owner (if exists owner)
              return owner.getCaller().getPool().symbolsGroup( Symbol.DBCOLUMN );
          else
              // no owner exists, try to get from local database pool
              this.fill( list, this.dbcolumns );
          break;

        // the system value
        case Symbol.SYSTEM:
          if ( owner.getCaller() != null )
              // get symbol's value from owner (if exists owner)
              return owner.getCaller().getPool().symbolsGroup( Symbol.SYSTEM );
          else
              // no owner exists, try to get from local system pool
              this.fill(list,this.system);
          break;
      }
      return list;
    }
    /**
     * To fill the list of symbols
     * @param list the list to fill
     * @param pool the pool by symbols' group
     */
    private final void fill(List list,Map pool){list.addAll( pool.keySet() );}
}
