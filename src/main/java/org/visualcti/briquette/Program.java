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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;
import org.visualcti.server.task.Environment;
import org.visualcti.server.task.stubTask;

/**
$Header: /VisualCTI_project/src/org/visualcti/briquette/Program.java 20    23.02.03 18:59 Olegs $
 * <Task>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * Class-task for briquette project</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 3.0
 */
final public class Program extends stubTask
                    //implements ProgrammSymbols
{
/**
<value>
The system symbol
the system's date in string format
*/
public final static Symbol system_Date = Symbol.newSystem("System.Date", Symbol.STRING);
/**
<value>
The system symbol
the system's time in string format
*/
public final static Symbol system_Time = Symbol.newSystem("System.Time", Symbol.STRING);
/**
<value>
The system symbol
the system's time in number format
*/
public final static Symbol system_Seconds = Symbol.newSystem("System.Seconds", Symbol.NUMBER);
/**
 * <attribute>
 * The filename of the programm to save to disk
 */
private String fileName = "<noname>";
/**
 * <accessor>
 * To get access to programm's filename
 * @return the filename
 */
public final String getFileName() {return fileName;}
/**
 * <mutator>
 * To change the filename
 * @param fileName new filename of the programm
 */
public final void setFileName(String fileName) {this.fileName = fileName;}
/**
 * <producer>
 * To make and configure simple programm
 * */
public static final Program newProrgamm()
{
  Program prog = new Program();
  prog.chain = new Chain( null, new Source(prog), true );
  prog.chain.setName("basic chain");
  return prog;
}
/**
 * <inner>
 * The source of the chain
 * */
public final static class Source implements Chain.Source{
  private final Program prog;
  public Source(Program prog){this.prog=prog;}
  private final File file(){return new File(prog.fileName);}
  public final URL getPath(){
    try {return this.file().toURL();
    }catch( MalformedURLException e){}
    return null;
  }
  public final InputStream getInputStream() throws IOException {
    return new FileInputStream( this.file() );
  }
  public final OutputStream getOutputStream() throws IOException {
    return new FileOutputStream( this.file() );
  }
}
/**
 * <accessor>
 * To get access to task's Environment
 * for made db connection, use telephone device features, etc.
 * */
    final public Environment getEnv(){return super.env;}
/**
 * <attribute>
 * The main subroutine of the programm
 * */
private volatile Subroutine main = null;
/**
 * <mutator>
 * To assign the subroutine as main
 * @param main main Subroutine
 */
public final void setMain(Subroutine main)
{
    synchronized(this.SEMAPHORE)
    {
      if (main == null) {
        if (this.main != null)
          this.main.finalize();
      }
      else{
        main.setAbout("Main");
      }
      this.main = main;
    }
}
/**
 * <semaphore>
 * to synchronize access to the main subroutine
 * */
final private Object SEMAPHORE = new Object();
/**
 * <action>
 * Method of start of execution of the cti-application,
 * is called by the scheduler of CT-device
 */
final public void execute()
{
    if (this.chain == null) return;
    // to make and prepare the main subroutine
    this.setMain( new Subroutine(this) );
    // to execute the main subroutine
    this.main.doIt( null );
    // to finalize the main subroutine and to destroy the object
    this.setMain(null);
}

/**
 * <action>
 * Method of stop of execution of the cti-application,
 */
final public void stopExecute()
{
    synchronized(SEMAPHORE)
    {
        if (this.main != null) this.main.stopExecute();
    }
}
/**
 * <attribute>
 * The chain of programm
 * */
private Chain chain;
  /**
   * <accessor>
   * To get access to Program's main chain
   * */
  final public Chain getChain(){return this.chain;}
/**
 * <accessor>
 * Get access to XML presentation of Task
 * XML may contains the Task's parameters
 *
 * @return XML Element <task></task>
 *
 */
    final public Element getXML(){
        Element xml = super.getXML();
        Element format = new Element("format");
        String
        fmt = this.dateFormat.toPattern();
        format.setAttribute(new Attribute("date",fmt));
        fmt = this.timeFormat.toPattern();
        format.setAttribute(new Attribute("time",fmt));
        xml.addContent( format );
        if (this.chain != null) xml.addContent( this.chain.getXML() );
        return xml;
    }

/**
 * <mutator>
 * Setting up new XML representation
 * of contents for Task
 * XML may contains Task's parameters
 *
 * @param xml The XML Element <task></task>
 */
    final public void setXML(Element xml)throws Exception {
        super.setXML( xml );
        Element format = xml.getChild("format");
        if (format != null) {
            String
            fmt = format.getAttributeValue("date");
            if ( fmt != null) this.dateFormat.applyPattern(fmt);
            fmt = format.getAttributeValue("time");
            if ( fmt != null) this.timeFormat.applyPattern(fmt);
        }
        this.chain = new Chain( null, new Source(this) );
        this.chain.setXML( xml.getChild(Chain.ELEMENT) );
        this.name = chain.getName();
        this.about = chain.getDescription();
    }
    /**
     * <printer>
     * Method for a print from your cti-application standard
     * or debugging messages, you can call this method on demand
     * from the your cti-application.
     * */
    final public synchronized void printMessage(String message){this.debug(message);}
    /**
     * <printer>
     * Method for a print of error messages of the your cti-application,
     * you can call this method on demand from the your cti-application.
     * */
    final public synchronized void printError(String message){this.error(message);}
/**
 * <attribute>
 * The format of Date representation
 * */
public final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
/**
 * <attribute>
 * The format of Time representation
 * */
public final SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm");
    /**
     * <event_processor>
     * Notification from SystemClockSynchronize
     * */
    final protected void clockEvent()
    {
        synchronized(SEMAPHORE)
        {
            if ( this.main == null) return;
            long seconds = System.currentTimeMillis();
            this.main.set(Program.system_Seconds,new Long(seconds/1000));
            Date mark = new Date(seconds);
            this.main.set(Program.system_Date,this.dateFormat.format(mark));
            this.main.set(Program.system_Time,this.timeFormat.format(mark));
        }
    }
  /**
   * <accessor>
   * To get access to maked array of predefined Symbols
   * For design mode only...
   * @return the list of predefined symbols
   */
  public static final List getPredefinedSymbols()
  {
    ArrayList predefined = new ArrayList(100);
    synchronized( Program.class )
    {
      predefined.addAll(Program.systemSymbolsList);
      predefined.addAll(Program.constsList);
    }
    return predefined;
  }
/**
 * <pool>
 * The pool of predefined consts
 */
private static final ArrayList constsList = new ArrayList();
private final static void addPredefinedConst(Symbol symbol){
  if ( !Program.constsList.contains(symbol) ) Program.constsList.add(symbol);
}
/**
 * <pool>
 * The pool of predefined symbols
 */
private static final ArrayList systemSymbolsList = new ArrayList();
private final static void addPredefinedSymbol(Symbol symbol){
  if ( !Program.systemSymbolsList.contains(symbol) ) Program.systemSymbolsList.add(symbol);
}
/**
 * <init>
 * To initialize the system's symbols list
 */
static {
  Program.addPredefinedSymbol(Program.system_Date);
  Program.addPredefinedSymbol(Program.system_Time);
  Program.addPredefinedSymbol(Program.system_Seconds);
}

  /**
   * <mutator>
   * To add predefined symbols from Operation
   * For design mode only...
   * @param briquette the source of predefined symbols
   */
  public static final void storePredefinedSymbols(Operation briquette)
  {
    synchronized( Program.class )
    {
      for(Iterator i=briquette.getPredefinedSymbols().iterator();i.hasNext();)
      {
        Object item = i.next();
        if ( !(item instanceof Symbol) ) continue;
        Symbol symbol = (Symbol)item;
        if ( symbol.isConst() ) Program.addPredefinedConst(symbol);
        else
        switch(symbol.getGroupID())
        {
          case Symbol.SYSTEM:
          case Symbol.DATABASE:
            Program.addPredefinedSymbol(symbol);
            break;
        }
      }
    }
  }
}
