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
import org.visualcti.briquette.core.*;

import java.util.*;
/**
 * Class, represents parameters's set
 * for transfer parameters to subroutine
 * And returns results from Subroutine
 * */
public final class ParametersSet
{
/**
 * <const>
 * The name of root's XML element
 * */
public static String ELEMENT = "parameters";
/**
 * <attribute>
 * The comment of this Parameters Set
 * */
private String comment = "No comments";
  /**
   * <constructor>
   * */
  public ParametersSet(String comment){this.comment=comment;}
  /**
   * <accessor>
   * To get access to parameters's pool (ReadOnly)
   * */
  private final ArrayList getParameters(ArrayList pool){
    synchronized( pool ){return (ArrayList)pool.clone();}
  }
  /**
   * <mutator>
   * To add parameter to parameters's pool
   * */
  private final void addParameter(ArrayList pool,Parameter param){
    synchronized( pool ){pool.add( param );}
  }
  /**
   * <accessor>
   * To get access to index of parameter in the pool
   * */
  private final int indexOf(ArrayList pool,Parameter param){
    if ( pool.size() == 0 ) return -1;
    Symbol toFind = param.getNameNative();
    int index = 0;
    for(Iterator i=pool.iterator();i.hasNext();){
      Parameter p = (Parameter)i.next();
      if ( p.getNameNative().equals(toFind) ) return index;
      index++;
    }
    return -1;
  }
  /**
   * <mutator>
   * To delete parameter from parameters's pool
   * */
  private final Parameter deleteParameter(ArrayList pool, Parameter param){
    synchronized( pool ){
      int index = indexOf(pool,param);
      if (index == -1) return null;
      pool.remove( index );
      if ( pool.size() == 0) return null;
      try {return (Parameter)pool.get(index);
      }catch(IndexOutOfBoundsException e){}
      return (Parameter)pool.get(index-1);
    }
  }
  /**
   * <mutator>
   * To update the parameter
   * */
  private final void updateParameter(ArrayList pool, Parameter Old, Parameter New){
    synchronized( pool ){
      int index = pool.indexOf(Old);
      if ( index >= 0 ) pool.set(index,New);
    }
  }
  /**
   * <mutator>
   * To move up the parameter
   * */
  private final boolean moveUpParameter(ArrayList pool, Parameter param){
    synchronized( pool ) {
      if ( pool.size() <= 1 ) return false;
      int index = this.indexOf(pool,param);
      if (index <= 0) return false;
      Object entry = pool.remove(index);
      pool.add(index-1,entry);
      return true;
    }
  }
  /**
   * <mutator>
   * To move down the parameter
   * */
  private final boolean moveDownParameter(ArrayList pool, Parameter param){
    synchronized( pool ) {
      int size;
      if ( (size=pool.size()) <= 1 ) return false;
      int index = this.indexOf(pool,param);
      if (index < 0 || index+1 >= size) return false;
      Object entry = pool.remove(index);
      if ( index >= size )
        pool.add( entry );
      else
        pool.add(index+1,entry);
      return true;
    }
  }
/**
 * <pool>
 * The pool of input's parameters
 * */
private final ArrayList inputParameters = new ArrayList();
  /**
   * <accessor>
   * To get access to input parameters (ReadOnly)
   * */
  public final List getInputParameters()
  {
    return this.getParameters(this.inputParameters);
  }
  /**
   * <mutator>
   * To add input parameter
   * */
  public final void addInputParameter(Parameter param)
  {
    this.addParameter(this.inputParameters,param);
  }
  /**
   * <mutator>
   * to delete input parameter
   * */
  public final Parameter deleteInputParameter(Parameter param)
  {
    return this.deleteParameter(this.inputParameters,param);
  }
  /**
   * <mutator>
   * to update input parameter
   * */
  public final void updateInputParameter(Parameter Old,Parameter New)
  {
    this.updateParameter(this.inputParameters,Old,New);
  }
  /**
   * <mutator>
   * To move up the input parameter
   * */
  public final boolean moveUpInputParameter(Parameter param)
  {
    return this.moveUpParameter(this.inputParameters,param);
  }
  /**
   * <mutator>
   * To move down the input parameter
   * */
  public final boolean moveDownInputParameter(Parameter param)
  {
    return this.moveDownParameter(this.inputParameters,param);
  }
/**
 * <pool>
 * The pool of output's parameters (results)
 * */
private final ArrayList outputParameters = new ArrayList();
  /**
   * <accessor>
   * To get access to output parameters (ReadOnly)
   * */
  public final List getOutputParameters()
  {
    return this.getParameters(this.outputParameters);
  }
  /**
   * <mutator>
   * To add output parameter
   * */
  public final void addOutputParameter(Parameter param)
  {
    this.addParameter(this.outputParameters,param);
  }
  /**
   * <mutator>
   * to delete output parameter
   * */
  public final Parameter deleteOutputParameter(Parameter param)
  {
    return this.deleteParameter(this.outputParameters,param);
  }
  /**
   * <mutator>
   * to update output parameter
   * */
  public final void updateOutputParameter(Parameter Old,Parameter New)
  {
    this.updateParameter(this.outputParameters,Old,New);
  }
  /**
   * <mutator>
   * To move up the output parmater
   * */
  public final boolean moveUpOutputParameter(Parameter param)
  {
    return this.moveUpParameter(this.outputParameters,param);
  }
  /**
   * <mutator>
   * To move down the output parmater
   * */
  public final boolean moveDownOutputParameter(Parameter param)
  {
    return this.moveDownParameter(this.outputParameters,param);
  }
  /**
   * <translator>
   * To store the parameters set to XML
   * */
  public final Element getXML()
  {
    Element xml = new Element(ELEMENT);
    xml.addContent( new Comment(this.comment) );
    Element inputXML = new Element("input");
    Element outputXML = new Element("output");
    this.store(this.inputParameters, inputXML);
    this.store( this.outputParameters, outputXML );
    xml.addContent(inputXML); xml.addContent( outputXML );
    return xml;
  }
  /** to store the pool to XML */
  private final void store(ArrayList pool,Element xml){
    for(Iterator i= pool.iterator();i.hasNext();){
      xml.addContent( ((Parameter)i.next()).getXML() );
    }
  }
  /**
   * <translator>
   * To restore the parameters set from XML
   * */
  public final void setXML(Element xml) throws Exception
  {
    this.inputParameters.clear();this.outputParameters.clear();
    if ( xml == null || !ELEMENT.equals(xml.getName()) ) return;
    for(Iterator i=xml.getChildren().iterator();i.hasNext();)
    {
      Object entry = i.next();
      if ( entry instanceof Comment )
        this.comment = ((Comment)entry).getText();
    }
    this.restore(xml.getChild("input"), this.inputParameters);
    this.restore(xml.getChild("output"), this.outputParameters);
  }
  /** to restore the pool */
  private final void restore(Element xml,ArrayList pool) throws Exception {
    if ( xml == null ) return;
    for(Iterator i=xml.getChildren(Parameter.ELEMENT).iterator();i.hasNext();){
      pool.add( Parameter.restore((Element)i.next()) );
    }
  }
  /**
   * <accessor>
   * To get access to all parameters list
   * */
  public final List getSymbols()
  {
    ArrayList symbols = new ArrayList();
    // to copy input parameter names
    for(Iterator i=this.inputParameters.iterator();i.hasNext();)
      symbols.add( ((Parameter)i.next()).getName() );
    // to copy output parameter names
    for(Iterator i=this.outputParameters.iterator();i.hasNext();)
      symbols.add( ((Parameter)i.next()).getName() );
    return symbols;
  }
}
