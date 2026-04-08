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
package org.visualcti.media;
/**
class describe the format of fax data
This is proxy between device and images
*/
public final class Fax
{
public static final Fax TIFF = new Fax("tiff");
public static final Fax TEXT = new Fax("text");
    /**
    <producer>
    To make the copy of format
    */
    public final Fax copy()
    {
      Fax copy = new Fax(""+this.type);
      copy.highResolution = this.highResolution;
      return copy;
    }
    /**
    <accessor>
    To check is TIFF
    */
    public final boolean isTIFF(){return type.equals(TIFF.type);}
    /**
    <attribute>
    Type of fax data (for send only)
    */
    private String type;
    /**
    <attribute>
    */
    private boolean highResolution = false;
    /**
    <accessot>
    */
    public final boolean isHighResolution(){return this.highResolution;}
    /**
    <mutator>
    */
    public final Fax high(){this.highResolution=true;return this;}
    /**
    <mutator>
    */
    public final Fax normal(){this.highResolution=false;return this;}
    /**
    <constructor>
    for internal use only
    */
    private Fax(String type){this.type=type;}
}
