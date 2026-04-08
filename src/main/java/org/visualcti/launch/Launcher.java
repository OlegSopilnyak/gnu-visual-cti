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
package org.visualcti.launch;

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Applications Server,<br>
 * The parent of all launchers. Prepare the classloader for launch</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc. & Prominic Ukraine Co.</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 3.01
 */

class Launcher
{
/**
 * <attribute>
 * The classloader for application
 */
protected final ClassLoader mainClassLoader;
  /**
   * <constructor>
   * To make & prepare the classloader & Java-environment
   */
  Launcher()
  {
    URL[] path = null;
    ArrayList paths = new ArrayList();
    try{
      paths.add( new URL("file:./class/") );
      makeEnvironment(paths);
      path = (URL[])paths.toArray( new URL[]{} );
//System.out.println("Env"+System.getProperties());
//System.out.println("The library is "+path);
//for ( int i=0;i < path.length;i++) System.out.println(path[i]);
    }catch(MalformedURLException e){}
    if ( path != null ) {
      this.mainClassLoader =
//        URLClassLoader.newInstance(path,Launcher.class.getClassLoader());
        URLClassLoader.newInstance(path);
    }else {
      this.mainClassLoader = ClassLoader.getSystemClassLoader();
    }
  }
    /**
    to make the server's environment
    insert bin directory, as DLL source
    insert all .jar from lib to classpath
    */
    private static void makeEnvironment(ArrayList paths)
    {

        Properties props = System.getProperties();
        String pathSep = System.getProperty("path.separator");
        String binPath = System.getProperty("java.library.path");
        File binary = new File("bin");
        // to setup the "./bin", as dynamic library's directory
        StringBuffer buf = new StringBuffer( binPath );
        buf.insert(0, pathSep);
        buf.insert(0,binary.getAbsolutePath());
        System.setProperty("java.library.path", buf.toString());
        // to process "./lib" directory for add JARs
        File lib = new File("lib"),jar=null;
        String jars[] = lib.list();
        if (jars == null) return;

        System.out.print("Solve the lib directory.");System.out.flush();
        Arrays.sort(jars, Collections.reverseOrder());
        for (int i=0;i < jars.length;i++)
        {
            String name = jars[i].toLowerCase();
            if ( !name.endsWith(".jar") ) continue;
            jar = new File(lib,jars[i]);
            try{
              paths.add( jar.toURL() );
              System.out.print("..");System.out.flush();
            }catch(MalformedURLException e){}
        }
        System.out.println(". Done");
    }
}
