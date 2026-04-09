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
package org.visualcti.briquette.filesystem;

import org.visualcti.briquette.*;
import java.util.*;
import java.io.*;
/**
 * <basis>
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI Briquette framework, <br>
 * Basis of the FileSystems' briquettes <br>
 * The parent of any briquette from filesystem's group</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
public abstract class Basis extends Operation
{
/**
<value>
The system symbol
The file's name
*/
public static final Symbol system_file_Name = Symbol.newSystem("file.Name", Symbol.STRING);
/**
<value>
The system symbol
The file's directory, where file is placed
*/
public static final Symbol system_file_Location = Symbol.newSystem("file.Location", Symbol.STRING);
/**
<value>
The system symbol
The flag, is file is directory (Yes/No)
*/
public static final Symbol system_file_isDirectory = Symbol.newSystem("file.isDirectory", Symbol.STRING);
/**
<value>
The system symbol
The file's size
*/
public static final Symbol system_file_Size = Symbol.newSystem("file.Size", Symbol.NUMBER);
/**
<value>
The system symbol
The flag of file's availability (Yes/No)
*/
public static final Symbol system_file_Available = Symbol.newSystem("file.Available", Symbol.STRING);
/**
<value>
The system symbol
The file's modify time (String format)
*/
public static final Symbol system_file_Time = Symbol.newSystem("file.MTime", Symbol.STRING);
/**
<value>
The system symbol
The file's modify time (Number format)
*/
public static final Symbol system_file_TimeSeconds = Symbol.newSystem("file.MTimeSecods", Symbol.NUMBER);
/**
<value>
The system symbol
The the result of operation with file
*/
public static final Symbol system_file_OperationResult = Symbol.newSystem("file.Operation.Result", Symbol.STRING);
/**
 * <const>
 * The key for get access to Subroutine's common entry
 */
public static final String COMMON = "files.list";
/**
 * <pool>
 * The pool of predefined symbols
 */
private static final List predefined = new ArrayList();
/**
 * <init>
 * To initialize predefined Symbols
 */
static{
  Basis.predefined.add(Basis.system_file_Location);
  Basis.predefined.add(Basis.system_file_Name);
  Basis.predefined.add(Basis.system_file_isDirectory);
  Basis.predefined.add(Basis.system_file_Size);
  Basis.predefined.add(Basis.system_file_Available);
  Basis.predefined.add(Basis.system_file_Time);
  Basis.predefined.add(Basis.system_file_TimeSeconds);
  Basis.predefined.add(Basis.system_file_OperationResult);

  // for file.Available
  Basis.predefined.add(Symbol.newConst("Yes"));
  Basis.predefined.add(Symbol.newConst("No"));
  // for file operations
  Basis.predefined.add(Symbol.newConst("SUCCESS"));
  Basis.predefined.add(Symbol.newConst("FAIL"));
}
  /**
   * <accessor>
   * To get access to Operation's predefined Symbols List
   * Used only in design mode!
   * It may be overrided in children
   * @return predefined symbols
   */
  public final List getPredefinedSymbols(){return predefined;}
  /**
   * <service>
   * To fill information about the file
   * @param caller Subroutine-caller of operation
   * @param dir location's directory
   * @param file the file
   */
  static final void fillFile(Subroutine caller,String dir,File file){
    caller.set(system_file_Available,"Yes");
    caller.set(system_file_isDirectory,file.isDirectory() ? "Yes":"No");
    caller.set(system_file_Location,dir);
    caller.set(system_file_Name,file.getName());
    caller.set(system_file_Size,new Long(file.length()));
    long MTime = file.lastModified();
    Date time = new Date(MTime);
    Program task = caller.getProgramm();
    String timeStamp = task.dateFormat.format(time)+" "+task.timeFormat.format(time);
    caller.set( system_file_Time, timeStamp );
    caller.set( system_file_TimeSeconds, new Long(MTime/1000) );
  }
  /**
   * <service>
   * To fill information about the file
   * @param caller Subroutine-caller of operation
   * @param file the file
   */
  static final void fillFile(Subroutine caller,File file){
    caller.set(system_file_Available,file.exists() ? "Yes":"No");
    caller.set(system_file_isDirectory,file.isDirectory() ? "Yes":"No");
    caller.set(system_file_Location,file.getParentFile().getAbsolutePath());
    caller.set(system_file_Name,file.getName());
    caller.set(system_file_Size,new Long(file.length()));
    long MTime = file.lastModified();
    Date time = new Date(MTime);
    Program task = caller.getProgramm();
    String timeStamp = task.dateFormat.format(time)+" "+task.timeFormat.format(time);
    caller.set( system_file_Time, timeStamp );
    caller.set( system_file_TimeSeconds, new Long(MTime/1000) );
  }
  /**
   * <service>
   * To clear all information about the file
   * @param caller Subroutine caller of the operation
   */
  static final void clearFile(Subroutine caller){
    caller.set(system_file_Available,"No");
    caller.set(system_file_isDirectory,null);
    caller.set(system_file_Location,null);
    caller.set(system_file_Name,null);
    caller.set(system_file_Size,null);
    caller.set(system_file_Time,null);
    caller.set(system_file_TimeSeconds,null);
  }
  /**
   * <wrapper>
   * Class wrapper of files list
   */
  final static class FilesList {
    File[] files = null;
    int index = -1;
    final synchronized boolean firstFile(Subroutine caller){
      clearFile(caller);
      try{
        Basis.fillFile( caller, files[0] );
        this.index = 0;
        return true;
      }catch(Exception e){
        return false;
      }
    }
    final synchronized boolean lastFile(Subroutine caller){
      clearFile(caller);
      try{
        Basis.fillFile( caller, files[files.length-1] );
        this.index=files.length-1;
        return true;
      }catch(Exception e){
        return false;
      }
    }
    final synchronized boolean nextFile(Subroutine caller){
      clearFile(caller);
      try{
        Basis.fillFile( caller, files[index+1] );
        this.index++;
        return true;
      }catch(Exception e){
        return false;
      }
    }
    final synchronized boolean prevFile(Subroutine caller){
      clearFile(caller);
      try{
        Basis.fillFile( caller, files[index-1] );
        this.index--;
        return true;
      }catch(Exception e){
        return false;
      }
    }
  }
}
