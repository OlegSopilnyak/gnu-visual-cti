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

import org.jdom.*;
import java.util.*;

import org.visualcti.server.database.*;
import org.visualcti.briquette.core.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The subroutine of chain</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 * $Header: /VisualCTI_project/src/org/visualcti/briquette/Subroutine.java 15    3/05/03 9:18p Olegs $
 */

public class Subroutine extends Operation {
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public final String get_ID_prefix(){return "Subroutine.";}
    /**
     * <constructor>
     * Empty for restore operation
     * */
    public Subroutine()
    {
      // to setup about, by default
      super.setAbout("The subroutine");
      // to make and the variables pool
      this.pool = new Pool( this );
      // to make the default entity
      this.entity = new ChainEntity();
    }
    final String callStack(){
      if (this.caller == null) return super.getAbout();
      return this.caller.callStack()+"/"+getAbout();
    }

    /**
    <constructor>
    For main main subroutine of the Program
    */
    public Subroutine(Program programm)
    {
       this();this.programm=programm;
       ((ChainEntity)this.entity).setChain(programm.getChain());
    }
/**
 * <attribute>
 * The pool of Symbols's values
 * */
protected final Pool pool;
    /**
     * <accessor>
     * To get acces to subroutine's Pool
     * */
    public final Pool getPool(){return this.pool;}
    /**
     * <print>
     * To print the info's message from the briquette
     * @param message message to print
     */
    public final void info(String message)
    {
      try{
        this.getProgramm().printMessage(message);
      }catch(NullPointerException e){}
    }
    /**
     * <print>
     * To print the error's message from the briquette
     * @param message message to print
     */
    public final void error(String message)
    {
      try{
        this.getProgramm().printError(message);
      }catch(NullPointerException e){}
    }
    /**
     * <attribute>
     * The reference to programm
     * This is main entry of the application
     * */
    protected volatile Program programm=null;
    /**
    <accessor>
    To get access to programm of subroutine
    */
    public final Program getProgramm()
    {
        return (this.caller == null) ? this.programm:this.caller.getProgramm();
    }
/**
 * <attribute>
 * The execution context of subroutine (database)
 * */
private Context context=null;
    /**
    <accessor>
    to get access to subroutine's context
    */
    public final Context getContext()
    {
      if ( this.entity == null) return null;
      if ( this.entity.isLocal() )
      {
          if ( this.caller == null ) return this.context;
          else return this.caller.getContext();
      } else return this.context;
    }
    /**
     * <notify>
     * Chain will notify the Operation after successfully added
     * The children may override it
     * */
    public final void added()
    {
      if ( this.entity != null ) this.entity.attach(this);
    }
/**
 * <attribute>
 * The entity of subroutine
 * */
private Entity entity=null;
  /**
   * <accessor>
   * To get access to Subroutine's entity
   * */
  public final Entity getEntity() {return this.entity;}
  /**
   * <mutator>
   * To assign the Subroutine' entity
   * */
  public final void setEntity(Entity entity)
  {
    if ( this.entity != null ) this.entity.detach();
    if ( (this.entity=entity) != null ) this.entity.attach(this);
  }
  /**
   * <translator>
   * To fill runtime information
   * To store representation of subroutine to XML
   * */
  protected final void storeRuntimeProperties(Element xml)
  {
    xml.addContent( this.actual.getXML() );
    if ( this.entity != null )
    {
      Element eXML = new Element( Entity.ELEMENT );
      this.entity.store( eXML );
      Object o = this.entity;
      String entityClass = o.getClass().getName();
      eXML.setAttribute( new Attribute("class",entityClass) );
      xml.addContent( eXML );
    }
  }
  /**
   * <translator>
   * To adjust runtime properties in Operation
   * To adjust Subroutine's runtime
   * */
  protected final void restoreRuntimeProperties(Element xml) throws Exception
  {
      // to restore actual parameters of Subroutine
      this.actual.setXML( xml.getChild(ParametersSet.ELEMENT) );
      // to restore the Subroutine's entity
      Element eXML = xml.getChild(Entity.ELEMENT);
      String className = eXML.getAttributeValue("class");
      this.entity = (Entity)Class.forName( className ).newInstance();
      this.entity.restore( eXML );
  }
  /**
   * <action>
   * To stop Operator's executing
   * */
  public final void stopExecute()
  {
    this.canceled = true;
    if ( this.entity != null ) this.entity.cancel();
  }
/**
 * <attribute>
 * The actual parameters set
 * */
final private ParametersSet actual = new ParametersSet("Actual parameters of the Subroutine");
    /**
    <accessor>
    to get access to parameters set
    */
    final public ParametersSet getActualParameters(){return this.actual;}
/**
 * <attribute>
 * Who call this subroutine
 * */
private volatile Subroutine caller = null;
/**
 * <accessor>
 * To get access to doIt's caller
 * */
public final Subroutine getCaller() {return this.caller;}
/**
 * <attribute>
 * The pool of common values (for store complex Objects)
 */
public final HashMap common = new HashMap();
/**
 * <accessor>
 * To get access to common values pool
 * @return the common values pool
 */
public final HashMap getCommon(){return this.common;}
/**
 * <flag>
 * Is stopExceute was called
 * */
private volatile boolean canceled = false;
    /**
    <action>
    To execute this operator
    After execution, method returns the reference
    to the next executable operator.
    If null has returned, the Chain (owner) must finish
    */
  public Operation doIt(Subroutine caller) {
    // to check the entity
    if ( this.entity == null ) return null;
    // let's begin
    this.caller=caller;
    this.canceled = false;super.finalized = false;
    this.common.clear();
    try {
      this.info("\tbegin ["+this.callStack()+"]");
      // to init the Subroutine's paramters
      this.initParameters( caller );
      // to execute Subroutine's entity
      this.entity.doIt( this );
      // to return the parameters
      this.returnParameters( caller );
      // to free all resources
      this.finalize();
      this.info("\tend ["+this.callStack()+"]");
    }catch(Exception e){
      // some exception during entity's execution
      canceled = true;
    }
    // to clear the caller's reference
    this.caller=null;
    // make next Operation's jump
    return this.canceled ? null:super.getLink(Operation.DEFAULT_LINK);
  }
  /**
   * <finalizer>
   * To free all allocated resources
   * */
  public final void finalize()
  {
    // to check the flag
    if ( super.finalized ) return;
    super.finalize();
    // to clear the pool
    this.pool.clearAll();
    // to solve the Subroutine's entity
    if ( this.entity != null )
    {
      this.entity.detach();
      if ( this.entity.isLocal() ) return;
    }
    // to clear the database's context
    if (this.context != null)
    {
        this.context.close();
        this.context = null;
    }
    // to clear the common values pool
    this.common.clear();
  }
  /**
   * <accessor>
   * To get access to local Symbols borned in this Operation
   * */
  public final List getLocalSymbols()
  {
    ArrayList localSymbols = new ArrayList();
    // to solve the actual parameters
    for(Iterator i=this.actual.getOutputParameters().iterator();i.hasNext();)
    {
      Parameter.Actual parameter = (Parameter.Actual)i.next();
      localSymbols.add( parameter.getLocalSymbol() );
    }
    return localSymbols;
  }
  /**
   * <return>
   * To return the parameters to caller
   * Only declared, actual parameters
   * */
  public final void returnParameters(Subroutine caller)
  {
    for(Iterator i=this.actual.getOutputParameters().iterator();i.hasNext();){
      Parameter.Actual parameter = (Parameter.Actual)i.next();
      // update local parameters in the caller
      caller.set( parameter.getLocalSymbol(), this.get(parameter.getNameNative()) );
    }
    // to delete local values from the pool
    this.pool.clear( Symbol.USER );
    this.caller = null;
  }
  /**
   * <init>
   * To init the parameters of the Subroutine
   * */
  public final void initParameters( Subroutine caller )
  {
    this.caller = caller;
    // to clear the Pool
    this.pool.clearAll();
    // to make the database's context
    this.initContext(caller);
    // to init formal parameters from Entity
    this.initFormalParameters();
    // to init Subroutine's actual parameters
    this.initActualParameters( caller );
  }
  /**
   * <init>
   * To init the Subroutine's context
   * */
  private final void initContext( Subroutine caller ){
    this.context=null;
    if ( caller != null && this.entity.isLocal() ) return;
    connectionRequest request = this.entity.getConnectionRequest();
    if (request != null) this.context = new Context( request );
  }
  /**
   * <init>
   * To init Entity's formal parameters
   * */
  private final void initFormalParameters(){
    ParametersSet formal = this.entity.getFormalParameters();
    if ( formal == null ) return;
    // to iterate the input's parameters
    this.initFormalParameters( formal.getInputParameters() );
    // to iterate the output's parameters
    this.initFormalParameters( formal.getOutputParameters() );
  }
  /**
   * <init>
   * To init formal parameters in the list
   * */
  private final void initFormalParameters(List params){
    for(Iterator i=params.iterator();i.hasNext();){
      Parameter param = (Parameter)i.next();
      if ( param instanceof Parameter.Formal && param.getValue() != null)
        this.set(param.getNameNative(),param.getValue());
    }
  }
  /**
   * <init>
   * To init actual Subroutine's parameters
   * */
  private final void initActualParameters(Subroutine caller){
    if ( caller == null ) return;
    // to init the actual parameters
    ParametersSet actual = this.actual;
    // to iterate the input's parameters
    this.initActualParameters( this.actual.getInputParameters(), caller );
    // to iterate the output's parameters
    this.initActualParameters( this.actual.getOutputParameters(), caller );
  }
  /**
   * <init>
   * To init actual parameters in the List
   * */
  private final void initActualParameters(List params, Subroutine caller){
    for(Iterator i=params.iterator();i.hasNext();){
      Parameter param = (Parameter)i.next();
      if ( param instanceof Parameter.Actual )
      {
        Parameter.Actual pa = (Parameter.Actual)param;
        if ( pa.getValue(caller) != null)
          this.set( pa.getNameNative(), pa.getValue() );
      }
    }
  }
/**
 * <mutator>
 * to remove form pool all values
 * Symbols from the group by group's ID
 * */
public final void clear(int groupID){ this.pool.clear(groupID); }
/**
 * <mutator>
 * to change/append value of symbol
 * */
public final void set(Symbol name,Object value){this.pool.set(name,value);}
/**
 * <accessor>
 * to get access to value of symbol
 * */
public final Object get(Symbol name){return this.pool.get(name);}
}
