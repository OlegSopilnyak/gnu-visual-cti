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
package org.visualcti.util;

import java.io.*;
import java.net.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: The XML's tools</p>
 * <p>Copyright: Copyright (c) Prominic Inc. Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public class Tools
{
/**
 * <const>
 * OS depended on line separator
 * */
public static final String CRLF = System.getProperties().getProperty("line.separator");
/**
 * <const>
 * The name of root XML element
 * */
public static final String ROOT_ELEMENT = "VisualCTI";
/** standard PrintStream */
public static PrintStream out = System.out;
/** error's PrintStream */
public static PrintStream err = System.err;
/**
 * <printer>
 * to print the standard message
 * */
public static final void print(String message){Tools.out.println(message);}
/**
 * <printer>
 * to print the errors message
 * */
public static final void error(String message){Tools.err.println(message);}
/**
 * <mutator>
 * To set new standart output for print messages
 * */
public static void setOut(PrintStream out) {
    synchronized(Tools.class){Tools.out=out;}
}
/**
 * <mutator>
 * To set new standart output for print messages
 * */
public static void setOut(OutputStream out) {
  Tools.setOut( new PrintStream(out,true) );
}
/**
 * <mutator>
 * To set new error output for print error messages
 * */
public static void setErr(PrintStream err){
    synchronized(Tools.class){Tools.err=err;}
}
/**
 * <mutator>
 * To set new error output for print error messages
 * */
public static void setErr(OutputStream err){
  Tools.setErr( new PrintStream(err,true) );
}
/**
 * <const>
 * The empty XML's element
 */
public static final Element emptyXML = new Element("empty");

private static final Element xmlLoad(URL url){
  try {
    InputStream in = url.openConnection().getInputStream();
    Document doc = new SAXBuilder().build( in );
    Element result = (Element)doc.getRootElement().clone();
    in.close(); doc=null; in=null;
    return result;
  }catch(Exception e){
    Tools.error("Exception in URL's parser....");
    e.printStackTrace(Tools.err);
    return Tools.emptyXML;// mistake, when build
  }
}
/**
 * <producer>
 * To load and solve the XML's file and return a root's Element
 * */
public static final Element xmlLoad( String xml_url )
{
  try {
    return xmlLoad( new URL(xml_url) );
  }catch(Exception e){
    e.printStackTrace();
    return Tools.emptyXML;
  }
}
/**
 * <producer>
 * To load and solve the XML's file and return a root's Element
 * */
public static final Element xmlLoad(File xml_file)
{
  try {
    return xmlLoad( xml_file.toURL() );
  }catch(Exception e){
    return Tools.emptyXML;
  }
}
/**
 * <saver>
 * To save the XML's element to a file
 */
public static final void xmlSave(Element xml,String file)
{
  Tools.xmlSave(xml,new File(file));
}
  /**
  * <saver>
  * To save the XML's element to a file
  * */
  public static final void xmlSave(Element xml,File file)
  {
    if (xml == null || file == null) return;
    xml = (Element)xml.clone();
    Document config = new Document( xml );
    java.util.List content = new java.util.ArrayList();
    content.add( new Comment(xmlLicence) );
    content.add( xml.clone() );
    config.setContent(content);
    XMLOutputter outputter = new XMLOutputter();
    outputter.setTextNormalize( true );
    outputter.setLineSeparator( CRLF );
    outputter.setNewlines( true );
    outputter.setIndent( "\t" );
    outputter.setIndent( true );
    synchronized(Tools.class)
    {
        try{// to save document
          FileOutputStream out = new FileOutputStream( file );
          outputter.output(config, out);
          out.flush(); out.close();
          Tools.print( "Saved XML to file "+file.getAbsolutePath() );
        }catch (IOException ioe){
          ioe.printStackTrace( Tools.err );
        }
    }
    config = null;
  }
/**
 * <accessor>
 * To get access to system's licence
 * @return
 */
public static final String getLicence(){return licence;}
/**
 * <licence>
 * The text of licence
 */
private static String licence="";
private static String xmlLicence=
"\n"+
"##############################################################################\n"+
"##\n"+
"##  DO NOT REMOVE THIS LICENSE AND COPYRIGHT NOTICE FOR ANY REASON\n"+
"##\n"+
"##############################################################################\n"+
"\n";
/**
 * <init>
 * To load the licenses
 */
static
{
//    java.io.InputStream licenceInputStream = Tools.class.getResourceAsStream("/visualcti.licence");
  File license = new File("conf/visualcti.licence");
  StringBuffer sb = new StringBuffer();
  try{
    FileInputStream fin = new FileInputStream(license);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
//      BufferedReader reader = new BufferedReader(new InputStreamReader(licenceInputStream));
    while ( true ){
      String line = reader.readLine();
      if ( line == null ) break;
      sb.append(line).append("\n");
    }
    licence = sb.toString();
    xmlLicence += licence;
  }catch(IOException e){
    throw new InternalError("Can't load the visualcti.licence...");
  }
}
}
