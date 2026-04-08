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
package org.visualcti.server;

import java.io.*;
import java.util.*;
import org.visualcti.util.Tools;

import org.jdom.*;
/**
Class for process XML config
*/
public class Config
{
/**
to save configurations to VisualCTI.xml
*/
public static void save()
{
    Tools.xmlSave(Config.visualCTI,Config.iniFile);
}
/**
to get primary child Element from Element's context
*/
public static Element getPrimaryEntry(Element root)
{
    Iterator i = root.getContent().iterator();
    Element entry = null;// result
    // try find Element entry in root content
    while( i.hasNext() ) {
        try {entry = (Element)i.next(); break;
        }catch(ClassCastException e){}
    }
    return entry;
}
/**
to get VisualCTI part Element
*/
public static Element getMainPart(String name)
{
    return (Element)Config.mainSystem.get( name );
}
/**
to get Server system Element by name
*/
public static Element getServerSystem(String name)
{
    return (Element)Config.serverSystem.get( name );
}
////////////////// PUBLIC end ////////////////////////
/** VisualCTI ini-file */
public static final File iniFile = new File("./conf/VisualCTI.server.xml");
    /** Document with VisualCTI properties */
    private static Element visualCTI = null;
    /** Store of VisualCTI Server systems */
    private static final HashMap serverSystem = new HashMap();
    /** Store of VisualCTI part systems */
    private static final HashMap mainSystem = new HashMap();
    /**
    To read VisualCTI.xml file and resolve it
    */
    private static void load()
    {
        mainSystem.clear();// to clear all VisualCTI entries
        serverSystem.clear();// to clear all Server entries
        Tools.out.print("Process ["+iniFile.getName()+"] .");
        Tools.out.flush();
        // make Element from ini-file
        Element XML = Tools.xmlLoad(Config.iniFile);
        if ( !XML.getName().equals(Tools.ROOT_ELEMENT) )
        {
          Tools.print("x Mistake");
          Tools.error("Can't find correct file "+iniFile.getAbsolutePath());
          Config.visualCTI = null;
          return;
        }
        Tools.out.print(".");
        Tools.out.flush();
        // to iterate VisualCTI Element
        for(Iterator i=XML.getContent().iterator();i.hasNext();)
        {
            Tools.out.print(".");
            Tools.out.flush();
            try {// we will process only Element.class
                Element part = (Element)i.next();
                String partName = part.getName();
                Config.mainSystem.put( partName, part );
            }catch(ClassCastException e){}// will ignore not Element entry
        }
        // to process Server part
        solveServer( getMainPart("Server") );
        Tools.out.print(".");
        Tools.out.flush();
        // store document inside
        Config.visualCTI = XML;
        Tools.out.println(". Done!");
    }
/////////////// SERVER SOLVING PART begin ///////////////
    /**
    to solve Server element
    */
    private static void solveServer(Element root)
    {
        if (root == null) return;
        // Get the list of systems
        List systems = root.getChildren("system");
        // To iterate system entries
        for(Iterator i = systems.iterator();i.hasNext();) {
            solveServerSystem( (Element) i.next() );
        }
    }
    /**
    To process Server/system Element
    */
    private static void solveServerSystem(Element sys)
    {
        Element entry = getPrimaryEntry( sys );
        if (entry != null) serverSystem.put(entry.getName(), entry);
    }
/////////////// SERVER SOLVING PART end ///////////////
    /**
    To init the configuration system
    */
    static void init()
    {
        Config.load();
        // to make and save default configuration
        if (Config.visualCTI == null)
        {
            Element root = new Element(Tools.ROOT_ELEMENT);
            Element server = new Element("Server");
            root.addContent(server);
            //###########################
            // to fill server element
            server.addContent(new Comment("Base interface of any Server's parts"));
            // to setup base section
            Element base = new Element("base").setText("serverUnit");
            base.setAttribute(new Attribute("package","org.visualcti.server"));
            server.addContent(base);
            server.addContent(new Comment("Properties of main RMI registry"));
            // to setup RMI section
            Element rmi = new Element("rmi").setText("local RMI Registry properties");
            rmi.setAttribute(new Attribute("port","2888"));
            rmi.setAttribute(new Attribute("start","true"));
            server.addContent(rmi);
            // store document inside
            Config.visualCTI = root;
            // to save configuration to file
            Config.save();
        }
    }
}
