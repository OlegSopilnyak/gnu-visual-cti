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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.visualcti.briquette.Subroutine;
import org.visualcti.util.Property;
/**
Parent of any Operations for VisualCTI algorithm
version 3.1
$Header: /VisualCTI_project/src/org/visualcti/briquette/Operation.java 45    22.02.03 13:39 Olegs $
*/
public abstract class Operation
{
/***
 * <listener>
 * The interface-listener Operation's changes
 */
public interface Listener {void operationChanged(Operation briquette);}
/**
 * <pool>
 * The pool of listeners
 */
private final ArrayList listeners = new ArrayList();
/**
 * <mutator>
 * To add operation's changes listener
 * @param listener the instans to add
 */
public final void addListener(Listener listener){
  synchronized( this.listeners ){
    if (listener != null)this.listeners.add(listener);
  }
}
/**
 * <mutator>
 * To remove the listener of changes
 * @param listener the listener to remove
 */
public final void removeListener(Listener listener){
  synchronized( this.listeners ){
    if (listener != null)this.listeners.remove(listener);
  }
}
/**
 * <action>
 * To notify the listeners about changes
 */
protected final void changed(){
  synchronized( this.listeners ){
    for(Iterator i=this.listeners.iterator();i.hasNext();){
      ((Listener)i.next()).operationChanged( this );
    }
  }
}
/** <stub> the empty unmodifable list */
private static final List emptyList = Collections.unmodifiableList(new ArrayList());
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * It may be overrided in children
   * @return predefined symbols
   */
  public List getPredefinedSymbols(){return emptyList;}
  /**
   * <accessor>
   * To get access to local Symbols borned in this Operation
   * It may be overrided in children
   * */
  public List getLocalSymbols(){return Operation.emptyList;}
  /**
   * <accessor>
   * To get access to Operation's Symbols set
   * Used only in design mode!
   * */
  public final List availableSymbols()
  {
    ArrayList symbols = new ArrayList();
    if (this.owner != null)
    {
      symbols.addAll( this.owner.getPredefinedSymbols() );
      symbols.addAll( this.owner.getLocalSymbols() );
      symbols.addAll( this.dbColumnsSymbol() );
    }
    return symbols;
  }
  /**
   * To get access to dbColumns of this briquette
   * @return the list of dbColumns
   */
  protected List dbColumnsSymbol(){return this.owner.getDatabaseColumnSymbols();}

    /**
     * <notify>
     * Chain will notify the Operation after successfully added
     * The children may override it
     * */
    public void added(){}
/**
 * <const>
 * operation XML's element name
 * */
public static final String ELEMENT = "briquette";
    /**
     * <check>
     * Is XML's elemnt valid
     * */
    public static final boolean valid(Element xml)
    {
      try {
        String name = xml.getName();
        return
          ELEMENT.equals(name) ||
          "operation".equals(name);// this is will be deprecated soon
      }catch(NullPointerException e){}
      return false;
    }
    /**
     * <accessor>
     * To get access to XML representation of operation
     * Final, can't override it in children
     * */
    public final Element getXML()
    {
        Element xml = new Element( ELEMENT );
        // setup the class-name of Operation
        xml.setAttribute(new Attribute("class",this.getClass().getName()));
        // add base properties
        xml.addContent( new Comment("base properties of the briquette") );
        xml.addContent( this.baseXML() );
        // add runtime properties
        xml.addContent( new Comment("runtime properties of the briquette") );
        xml.addContent( this.runtimeXML() );
        // add links
        xml.addContent( this.linkXML() );
        //
        return xml;
    }
    /**
     * <mutator>
     * To adjust operator, using XML's element
     * Final, can't override it in children
     * */
    public final Operation setXML(Element xml) throws Exception
    {
        if ( !valid(xml) ) throw new Exception("Not a "+ELEMENT+"'s Element!");
        // solve base's properties
        this.baseXML( xml.getChild("base") );
        // to solve runtime properties
        this.runtimeXML( xml.getChild("runtime") );
        // to solve links entry
        this.linkXML( xml.getChild("link") );
        // to return an adjusted operation
        return this;
    }

///////////////// Base properties section //////////////////////
    /**
     * <accessor>
     * To make a XML's element for base properties
     * */
    private final Element baseXML()
    {
      Element xml = new Element("base");
      xml.addContent( new Property("ID",this.ID).getXML() );
      xml.addContent( new Property("master",this.master).getXML() );
      xml.addContent( new Property("about", this.about).getXML() );
      xml.addContent( new Property("coordinates", this.coord).getXML() );
      return xml;
    }
    /**
     * <mutator>
     * To adjust the base's properties from XML's element
     * */
    private final void baseXML(Element xml) throws Exception
    {
      if (xml == null) return;
      // to clear a base Opertion's properties
      this.coord=new Point(0,0); this.master=false; this.ID="";
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 4 );
      // to iterate the properties of operation
      while( i.hasNext() ){
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          // check the property's name
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) ) throw new Exception("Multiple definition of base properties!");
          // to solve the property by name
          if ( "master".equals(name) )    {// setup master flag
              this.master = property.getValue(this.master); names.add(name);
          }else
          if ( "about".equals(name) )     {// setup about string
              this.about = property.getValue(this.about); names.add(name);
          }else
          if ( "coordinates".equals(name)){// setup coordinates of operation on canvas
              this.coord = property.getValue(this.coord); names.add(name);
          }else
          if ( "ID".equals(name) )        {// setup operation's ID
              this.ID = property.getValue(this.ID); names.add(name);
          }
      }
    }
/**
 * <attribute>
 * The coordinates of operator on the visual Chain
 * */
private volatile Point coord=new Point(0,0);
    /**
     * <accessor>
     * To get operator's coordinates on Visual view
     * */
    public final Point coordinates(){return this.coord;}
    /**
     * <mutator>
     * to change the coordinates
     * */
    public final void move(Point coord){this.coord=new Point(coord);this.changed();}
/**
 * <flag>
 * Whether Chain should start with this operator
 * */
private volatile boolean master=false;
    /**
     * <accessor>
     * To get access to master flag
     * */
    public final boolean isMaster(){return this.master;}
    /**
     * <mutator>
     * To setup the master flag
     * */
    public final void setMaster(boolean master)
    {
        boolean old = this.master;
        if ( (this.master=master) == true ) this.owner.setMainOperation( this );
        if (old != this.master) this.changed();
    }
/**
 * <attribute>
 * The Operator description
 * */
private volatile String about="<operation>";
    /**
    <accessor>
    To get access to operator description
    */
    public final String getAbout(){return this.about;}
    /**
    <mutator>
    To set operator descriptions
    */
    protected final void setAbout(String about){this.about=about;this.changed();}
    /**
     * <accessor>
     * To get access to new ID's prefix
     * */
    public String get_ID_prefix(){return "Operation.";}
/**
 * <attribute>
 * The Operator's ID
 * */
private volatile String ID="";
    /**
     * <accessor>
     * to get access to operation's ID
     * */
    public final String getID(){return this.ID;}
    /**
     * <mutator>
     * to assign ID for operator
     * */
    public final void setID(String ID){this.ID=ID;}
/**
 * <attribute>
 * The Chain, owner of this Operation
 * */
protected volatile Chain owner = null;
    /**
     * <accessor>
     * To get owner (chain) of this operator
     * */
    public final Chain getOwner(){return this.owner;}
    /**
     * <mutator>
     * To assign the owner for operator
     * */
    public final Operation setOwner(Chain owner)
    {
      this.owner=owner; return this;
    }

/////////// Runtime section //////////////////////
    /**
     * <accessor>
     * To make a XML's element for runtime properties
     * */
    private final Element runtimeXML() {
      Element xml = new Element("runtime");
      this.storeRuntimeProperties(xml);
      xml.setName("runtime");// maybe someone change it.... ;-)
      return xml;
    }
    /**
     * <mutator>
     * To adjust runtime's properties of Operation
     * */
    private final void runtimeXML(Element xml) throws Exception
    {
      if (xml != null) this.restoreRuntimeProperties(xml);
    }
    /**
     * <accessor>
     * To fill runtime information
     * This method MUST be overrided in children!
     * */
    protected abstract void storeRuntimeProperties(Element xml);
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * This method MUST be overrided in children!
     * */
    protected abstract void restoreRuntimeProperties(Element xml) throws Exception;
/**
 * <flag>
 * is operation's executing is finalized
 * The children may haved access to it
 * */
protected volatile boolean finalized = false;
    /**
     * <finalizer>
     * to free operation's resources
     * The children may override it
     * */
    public void finalize(){this.finalized=true;}

    /**
     * <action>
     * To stop Operator executing
     * */
    abstract public void stopExecute();

////////////////////////// Links section ////////////////////////////////////
/**
 * <constant>
 * Index of default link in links array
 * */
public static final int DEFAULT_LINK = 0;
/**
 * <constant>
 * Index of alternate link in links array
 * */
public static final int ALTERNATE_LINK = 1;
/**
 * <pool>
 * the array of linked operations
 * */
private final Operation[] links = new Operation[]{null,null};
/**
 * <pool>
 * The array of links's IDs
 * */
private final String[] linkIDs = new String[]{"",""};
    /**
     * <accessor>
     * to get count of links
     * The children may override it
     * */
    protected int linksCount(){return 1;}
    /**
     * <accessor>
     * get access to XML representaion of the links IDs
     * */
    private final Element linkXML() {
        Element xml = new Element("link");
        // to calculate defined the links quantity
        int linksCount = this.linksCount();
        // making....
        if (linksCount > 0) {// default link permited
          String ID = this.linkIDs[DEFAULT_LINK];
          if ( !"".equals(ID) ) xml.setAttribute(new Attribute("default",ID));
        } else return xml;// links not permited
        if (linksCount > 1) {// alternate link permited
          String ID = this.linkIDs[ALTERNATE_LINK];
          if ( !"".equals(ID) ) xml.setAttribute(new Attribute("alternate",ID));
        }
        return xml;
    }
    /**
     * <mutator>
     * to adjust the links IDs using XML's element
     * */
    private final void linkXML(Element xml){
        if ( xml == null ) return;// invalid element, do nothing
        // to clear the values of array of links IDs
        Arrays.fill( this.linkIDs, "" );
        // to calculate defined the links quantity
        int linksCount = this.linksCount();
        // adjusting.....
        if (linksCount > 0) {// default link permited
          String ID = xml.getAttributeValue("default");
          if (ID != null) this.linkIDs[DEFAULT_LINK] = ID;
        } else return;// links not permited
        if (linksCount > 1) {// alternate link permited
          String ID = xml.getAttributeValue("alternate");
          if (ID != null) this.linkIDs[ALTERNATE_LINK] = ID;
        }
    }
    /**
     * <accessor>
     * get reference to linked Operation by link's index
     * */
    public final Operation getLink(int index)
    {
        try {return this.links[ index ];
        }catch(ArrayIndexOutOfBoundsException e){}
        return null;
    }
    /**
     * <accessor>
     * to return the array of Operation,
     * linked to this Operation
     * */
    public final Operation[] getLinks()
    { // to make the result's array
      Operation[] result = new Operation[ this.linksCount() ];
      Arrays.fill(result, null);// to prepare the result
      // copying...
      for (int i=0;i < result.length;i++)
      {
          try {// copy Operation's reference to result
            result[ i ] = this.links[ i ];
          }catch(ArrayIndexOutOfBoundsException e){
              break;
          }
      }
      return result;// the array of Operations's references
    }
    /**
     * <accessor>
     * to return the array of Operation IDs,
     * linked to this Operation
     * */
    public final String[] getLinkIDs()
    { // to make the result's array
      String[] result = new String[ this.linksCount() ];
      Arrays.fill(result, "");// to prepare the result
      // copying...
      for (int i=0;i < result.length;i++)
      {
          try {// copy Operation's ID to result
            result[i] = this.linkIDs[i];
          }catch(ArrayIndexOutOfBoundsException e){
              break;
          }
      }
      return result;
    }
    /**
     * <mutator>
     * set reference to linked Operation by link's index
     * */
    public final boolean setLink
                              (
                              int index,          // the index of Link
                              Operation briquette // linked Operation
                              )
    {
        try {// copy information to the arrays
            this.links  [ index ] = briquette;
            this.linkIDs[ index ] = briquette==null ? "":briquette.getID();
            return true;
        }catch(ArrayIndexOutOfBoundsException e){}
        return false;
    }
    /**
     * <mutator>
     * To remove all links to operation
     * */
    public final void removeLinkTo(Operation briquette)
    {
        for(int index=0;index < this.links.length;index++)
        {
            if (this.links[index] == briquette)
            {
                this.links  [ index ] = null;
                this.linkIDs[ index ] = "";
            }
        }
    }
    /**
     * <mutator>
     * To clear all links
     * */
    public final void clearLinks()
    {
        Arrays.fill(this.links, null);// to clear Operation's references
        Arrays.fill(this.linkIDs, "");// to clear Operation's IDs
    }

/**
 * <action>
 * To execute this operator
 * After execution, method returns the reference
 * to the next executable operator.
 * If null has returned, the Chain (owner) must finish
 * */
abstract public Operation doIt(Subroutine caller);

///////////////// Final section ////////////////////////////
/**
 * <const>
 * Symbolum Nicaenum
 * */
public final static String Nicene_Creed=
"I believe in one God, "+
"the Father almighty, maker of heaven and earth, and of all things visible and invisible.\n"+
"And in one Lord, Jesus Christ, "+
"the only begotten Son of God, born of the Father before all ages. "+
"God from God, Light from Light, true God from true God, begotten, not made, "+
"one in being with the Father; through Whom all things were made.\n"+
"Who for us men and for our salvation came down from heaven. "+
"He was made flesh by the Holy Spirit from the Virgin Mary, and was made man.\n"+
"He was crucified for us under Pontius Pilate; suffered, and was buried. "+
"On the third day He rose again according to the Scriptures; "+
"He ascended into heaven and sits at the right hand of the Father.\n"+
"He will come again in glory to judge the living and the dead, and of His kingdom there shall be no end.\n"+
"And in the Holy Spirit, the Lord and giver of Life, Who proceeds from the Father and the Son.\n"+
"Who, with the Father and the Son, is adored and glorified: Who has spoken through the Prophets.\n"+
"And I believe in one holy, catholic and apostolic Church.\n"+
"I confess one baptism for the remission of sins. "+
"And I look for the resurrection of the dead, and the life of the age to come."+
"Amen."
;
}




