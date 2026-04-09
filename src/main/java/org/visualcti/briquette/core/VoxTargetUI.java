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
package org.visualcti.briquette.core;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.visualcti.briquette.Symbol;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The UI for VoxTarget</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class VoxTargetUI extends MediaUI {
/**
 * <member>
 * The filter for Telephony files
 * */
private static FileFilter soundFilter = new FileFilter(){
  public boolean accept(java.io.File file){
    if ( file.isDirectory() ) return true;
    String name = file.getName().toUpperCase();
    return name.endsWith(".VOX") || name.endsWith(".WAV");
  }
  public String getDescription(){return "WAV & VOX sounds";}
};
/**
 * <member>
 * The filter for VOX files
 * */
private static FileFilter voxFilter = new FileFilter(){
  public boolean accept(java.io.File file){
    if ( file.isDirectory() ) return true;
    return file.getName().toUpperCase().endsWith(".VOX");
  }
  public String getDescription(){return "VOX sounds";}
};
/**
 * <member>
 * The filter for WAV files
 * */
private static FileFilter wavFilter = new FileFilter(){
  public boolean accept(java.io.File file){
    if ( file.isDirectory() ) return true;
    return file.getName().toUpperCase().endsWith(".WAV");
  }
  public String getDescription(){return "WAV sounds";}
};

  /**
   * <mutator>
   * To configure the file's chooser
   * */
  protected final void configure(JFileChooser chooser)
  {
    chooser.addChoosableFileFilter( soundFilter );
    chooser.addChoosableFileFilter( voxFilter );
    chooser.addChoosableFileFilter( wavFilter );
  }
  protected final int show(JFileChooser chooser) {
    return chooser.showSaveDialog( this );
  }
  protected final boolean valid(java.io.File file) {return true;}
  /**
   * <editor>
   * To get the content's Symbol via Dialog
   * */
  protected final Symbol getContentSymbol(int type){
    return super.getContentSymbol(type);
  }
}
