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
package org.visualcti.server.log.impl;

import java.io.*;
import java.util.*;
import org.jdom.*;
import java.text.DateFormat;

import org.visualcti.server.*;
import org.visualcti.server.action.serverAction;
import org.visualcti.server.security.Filter;
import org.visualcti.server.service.*;
import org.visualcti.server.log.Log;
import org.visualcti.util.Tools;
/**
Service for log all serverActions
we will use the properties for this service
*/
public final class LogImpl extends ServiceAdapter implements Log
{
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return super.getIcon();}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState(){return super.getUnitState();}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return super.getType()+" logger";}
   /**
   process configuration
   */
   protected final void processConfiguration(Element xml)
   {
       Attribute homeDir = xml.getAttribute("home");
       if (homeDir == null){
           xml.setAttribute(homeDir = new Attribute("home","./log"));
       }
       // important thing, we will use the properties for this service
       HashMap props = new HashMap();
       props.put("home", homeDir.getValue());
       // to solve filter information
       Element filter = xml.getChild("filter");
       if (filter == null)
       {
            filter = new Element("filter");
            filter.addContent(new Comment("Filter for Log System"));
            filter.setAttribute(new Attribute("default","allowed"));
            Filter.Rule rule = new Filter.Rule("/Service/Log",null).setType("allowed");
            filter.addContent(rule.getXML());
            xml.addContent(filter);
            Config.save();
       }
       this.filter = new Filter( filter );
//       Filter.Rule rule = new Filter.Rule("/Service/Oleg",this.filter).setType("allowed");
//       this.filter.addRule(rule);
       props.put("filter", this.filter.getXML());
       this.setProperties( props );
   }
   /**
<mutator>
assign properties set to serverUnit
Properties may changed in GUI
   */
   public final void setProperties(Map props)
   {
        super.setProperties( props );
        String homeDir = (String)props.get("home");
        if (homeDir == null)
        {
            this.configuration.setAttribute(new Attribute("home",homeDir = "./log"));
            props.put("home", homeDir); Config.save();
        }
        this.home = new File( homeDir );
   }
   /**
<action>
to Start log service
   */
   public void Start() throws IOException
   {
        if ( !this.isStarted() )
        {
            this.state = Service.State.START;// setting up new state
        }
        if ( !this.home.exists() ) this.home.mkdirs();
        dispatch(new unitEvent(this,unitEvent.START_ID));
   }

   /**
<action>
to Stop log service
   */
    public void Stop() throws IOException
    {
        if ( this.isStopped() ) return;
        //this.state = Service.State.STOP;// setting up new state
        dispatch(new unitEvent(this,unitEvent.STOP_ID));
    }
/** Home directory of log FileSystem */
private File home = new File("./log");
   /**
<accessor>
access to service name
   */
   public final String getName(){return "Log";}
/** semaphores for serverAction UnitPath */
private final HashMap semaphores = new HashMap();
        /** get path depended semaphore */
        private Object semaphore(String path)
        {
                Object semaphore = this.semaphores.get(path);
                if (semaphore != null) return semaphore;
                synchronized(this.semaphores)
                {
                    semaphore = this.semaphores.get(path);
                    if (semaphore != null) return semaphore;
                    semaphore = new Object();
                    this.semaphores.put(path,semaphore);
                }
                return semaphore;
        }
private Filter filter;
  /**
  <accessor>
  to get Log serverAction's Filter
  */
  public Filter getFilter(){return this.filter;}
  /**
   <action>
   to save action's information
   */
   public final void save(DateFormat format, serverAction action)
   {
       if ( !this.isStarted() || //) return;
       //if (
            !this.filter.isAllowed(action) )
       {
//Config.err.println("FILTER>action not allowed");
//Config.err.println("Denied>"+action);
        return;
       }
       String unitPath = action.getUnitPath();
       Object semaphore = this.semaphore(unitPath);
       File path = new File(this.home,unitPath);
       String fileName = "std.log";
       if (action.actionClass() == action.ERROR){
            fileName = "err.log";
       }
       if ( !path.exists() ) path.mkdirs();
       File toSave = new File(path,fileName);
       try {
        synchronized( semaphore )
        {
            FileWriter fout = new FileWriter(toSave.getAbsolutePath(),true);
            PrintWriter log = new PrintWriter(fout,true);
            log.print(format.format(action.getDate())+">");
            log.println( action.toString());
            if (action.actionClass() == action.ERROR){
                this.saveError(format,action);
                unitError err = (unitError)action;
                if (err.nested() != null) err.nested().printStackTrace(log);
            }
             log.close(); log=null; fout=null;
        }
       }catch(IOException e){
            e.printStackTrace(Tools.err);
       }
   }
        /** to save to all errors log */
        private void saveError(DateFormat format,serverAction err) throws IOException
        {
            File toSave = new File(this.home,"errors.log");
            FileWriter fout = new FileWriter(toSave.getAbsolutePath(),true);
            PrintWriter log = new PrintWriter(fout,true);
            log.print(format.format(err.getDate())+"! Error in >");
            log.println( err.getUnitPath());log.close();
        }
}
