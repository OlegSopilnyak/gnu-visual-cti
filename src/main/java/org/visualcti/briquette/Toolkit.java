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

import java.io.*;
import java.net.*;
import java.util.*;

import org.jdom.*;
import org.visualcti.util.*;
/**
The tools for chain operation
*/
final public class Toolkit
{
/**
<pool>
The pool of maked chains
*/
private final static HashMap maked = new HashMap();
/**
<pool>
The list of still makers chains
*/
private final static ArrayList makers =  new ArrayList();
    /**
    <accessor>
    to get maked chain by URL
    */
    public final static Chain getChain(String URL,Chain parent)
    {
        Chain chain = null;
        synchronized( maked ){chain = (Chain)maked.get( URL );}
        return (chain == null) ? make( URL, parent ):chain;
    }
    /**
    <producer>
    to make the chain from URL
    */
    private final static Chain make(String URL,Chain parent)
    {
        MakeSession session = new MakeSession(URL,parent);
        synchronized( makers )
        {
            int index = makers.indexOf( session );
            if ( index < 0) makers.add( session );
            else session = (MakeSession)makers.get( index );
        }
        return session.getChain();
    }
    /**
    class of make chain sessions
    */
    private final static class MakeSession
    {
        private String URL;
        private Chain chain=null,parent;
        public MakeSession(String URL,Chain parent){this.URL=URL;this.parent=parent;}
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            try {return ((MakeSession)obj).URL.equals(this.URL);
            }catch (ClassCastException se){}
            return false;
        }
        private volatile boolean solved = false;
        public synchronized Chain getChain()
        {
            Chain chain = null;
            synchronized( maked ){chain = (Chain)maked.get( URL );}
            if ( chain != null) this.chain = chain;
            if (this.chain != null) return this.chain;
            if ( this.solved ) return null;
            this.chain = Toolkit.solveURL( this.URL, this.parent );
            if ( this.chain != null)
                synchronized( maked ){
                    maked.put(this.chain.getID(), this.chain);
                }
            this.solved =  true;
            return this.chain;
        }
    }
    /**
    <producer>
    try to make chain from URL
    */
    private static Chain solveURL(String URL,Chain parent)
    {
        Element xml = Tools.xmlLoad( URL );
        return restoreChain(xml,parent );
    }
    /**
    <producer>
    to restore Chain from XML elemnet
    */
    public final static Chain restoreChain(Element xml,Chain parent)
    {
        if (xml == null || !Chain.ELEMENT.equals( xml.getName() )) return null;
        try {
          Chain chain = new Chain( parent, source( xml ) );
          return chain.setXML(xml);
        }catch(Exception e){
          e.printStackTrace();
        }
        return null;
    }
    private static final Chain.Source source(Element xml) throws Exception {
      for(Iterator i=xml.getChildren(Property.ELEMENT).iterator();i.hasNext();){
        Property property = new Property( (Element)i.next() );
        if ( "URL".equals(property.getName()) ) {
          URL url = new URL( property.getValue((String)null) );
          return new urlSource( url );
        }
      }
      return null;
    }
    private final static class urlSource implements Chain.Source{
      URL url;
      urlSource(URL url){this.url=url;}
      public final URL getPath(){return this.url;}
      public final InputStream getInputStream() throws IOException {
        return this.url.openConnection().getInputStream();
      }
      public final OutputStream getOutputStream() throws IOException {
        return this.url.openConnection().getOutputStream();
      }
    }
    /**
    <producer>
    to restore Chain from XML elemnet
    */
    public final static Operation makeOperation(Element xml)
    {
        // to check XML's integrity
        if ( !Operation.valid(xml) ) return null;
        // to make & configure the operation form XML
        try {
            // to make class from XML's attribute
            Class producer = Class.forName( xml.getAttributeValue("class") );
            // to make a new instance of Operation
            Operation oper = (Operation)producer.newInstance();
            // to adjust the instance
            return oper.setXML( xml );
        }catch(Exception e){
          return null;// some wrong :(
        }
    }
}
