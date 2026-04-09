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

/**
Header of briquette Value for search in some pools
*/
public final class Symbol implements SymbolConsts, Comparable
{
public final int compareTo(Object o)
{
  if ( this.equals(o) ) return 0;
  return this.compareTo((Symbol)o);
}
private final int compareTo(Symbol symbol){
  if ( symbol.group != this.group ) return this.group-symbol.group;
//  if ( symbol.type != this.type ) return this.type-symbol.type;
  String myName = this.name, anotherName=symbol.name;
  if ( this.group == DBCOLUMN ){
    myName=myName.toUpperCase(); anotherName=anotherName.toUpperCase();
  }
  return myName.compareTo(anotherName);
}
/**
 * <producer>
 * To make the copy of Symbol
 * */
public final Symbol copy()
{
  Symbol copy=new Symbol();
  copy.name = String.valueOf(this.name);
  copy.Const = this.Const;
  copy.group = this.group;
  copy.type = this.type;
  return copy;
}
/**
<attribute>
The name of symbol
*/
private volatile String name = "";
    /**
    <accessor>
    to get access to symbol's name
    */
    public final String getName(){return this.name;}
    /**
    <mutator>
    to change the symbol's name
    */
    public final Symbol setName(String name){this.name=name;return this;}
    /**
     * <check>
     * Check is valid the name for this Symbol
     * */
    public final void validate(String name) throws Exception
    {
      if (this.Const && this.type == NUMBER) Double.parseDouble(name);
    }
    /**
     * <check>
     * Is may this name use as Number type constant
     * */
    public final boolean isMayNumber() {
      if ( !this.Const ) return false;// negative
      if ( this.type == NUMBER) return false;// negative, already
      if ( this.type == STRING || this.type == ANY)
        try {
          Double.parseDouble(this.name);// may throw NumberFormatException
          return true;// positive
        }catch(Exception e) {}
      return false;// negative
    }
/**
<flag>
Flag, is Symbol constant
*/
private volatile boolean Const = false;
    /**
    <accessor>
    to get access to constant flag
    */
    public final boolean isConst(){return this.Const;}
/**
<attribute>
The ID of symbol's type
*/
private volatile int type = ANY;
    /**
    <accessor>
    to get access to type's ID
    */
    public final int getTypeID(){return this.type;}
    /**
    <mutator>
    to assign new Symbol's type ID
    */
    public final Symbol setType(int type){
      this.name = "";
      this.type=type;return this;
    }
    /**
    <accessor>
    to get access to Symbol's type
    */
    public final String getType(){
        try { return TYPE[ this.type ];
        }catch(ArrayIndexOutOfBoundsException e){}
        return "????";
    }
/**
<attribute>
The ID of symbol's group
*/
private volatile int group = USER;
    /**
    <accessor>
    to get access to group's ID
    */
    public final int getGroupID(){return this.group;}
    /**
    <accessor>
    to get access to group
    */
    public final String getGroup(){
        try { return GROUP[ this.group ];
        }catch(ArrayIndexOutOfBoundsException e){}
        return "????";
    }
    /**
     * <accessor>
     * To get GroupID by name
     * */
    public static int getGroupID(Object name)
    {
      for(int i=0;i < GROUP.length;i++)
      {
        if (GROUP[i].equals(name)) return i;
      }
      return Symbol.ANY;
    }
    /**
    <hash>
    To get access to Symbol hashCode
    */
    public final int hashCode()
    {
        if ( this.Const ) return this.name.hashCode();
        String name = this.group==DBCOLUMN ? this.name.toUpperCase():this.name;
        return name.hashCode() ^ (this.group+1);
    }
    /**
    <equals>
    to compare with other Object
    */
    public final boolean equals(Object o)
    {
        if (this == o) return true;
        try {
            Symbol other = (Symbol)o;
            if ( this.group == SymbolConsts.DBCOLUMN )
                return
                    this.name.equalsIgnoreCase(other.name) &&
                    this.group == other.group;
            else if ( this.Const ) return this.name.equals(other.name);
            else return
                    this.name.equals(other.name) &&
                    this.group == other.group;
        }catch(ClassCastException e) {}
        return false;
    }
    /**
    <translator>
    To get String representation of Symbol
    */
    public final String toString()
    {
        return
            this.getGroup()+":"+
            this.getName()+
            "("+this.getType()+") const "+this.Const;
    }
    /**
     * <translator>
     * To translate to tree cell Label's text
     * */
    public final String cell()
    {
      if ( this.Const )
      {
        StringBuffer sb = new StringBuffer();
        if (type == STRING) sb.append("\'");
        sb.append( this.name );
        if (type == STRING) sb.append("\'");
        return sb.toString();
      }
      StringBuffer text = new StringBuffer(this.name);
      // to adjust the type
      switch( this.type ){
        case Symbol.ANY: text.append("(*)");break;
        case Symbol.NONE: text.append("(?)");break;
        case Symbol.STRING: text.append("(C)");break;
        case Symbol.NUMBER: text.append("(N)");break;
        case Symbol.VOICE: text.append("(V)");break;
        case Symbol.FAX: text.append("(F)");break;
        case Symbol.BIN: text.append("(B)");break;
        default:
          this.type = Symbol.NONE;
          text.append("(?)");
          break;
      }
      // to adjust the group
      switch( this.group ){
        case Symbol.USER:         break;
        case Symbol.DATABASE:     break;
        case Symbol.DBCOLUMN:     break;
        case Symbol.SYSTEM:       break;
        default:
          break;
      }
      return text.toString();
    }
    /**
    <constructor>
    private only
    */
    private Symbol(){}
    /**
    <producer>
    To restore symbol from String representation
    */
    public final static Symbol fromString(String sym) throws Exception
    {
        Symbol value = new Symbol();
        if ( "null".equalsIgnoreCase(sym) ) return null;
        int index = sym.indexOf(":");
        if (index < 0) throw new Exception("Invalid format of Symbol's string");
        String groupS = sym.substring(0,index).trim();
        int group = -100;
        for(int i=0;i < GROUP.length;i++) if ( GROUP[i].equals(groupS) ) {group = i;break;}
        if (group == -100) throw new Exception("Invalid group in Symbol's string");
        sym = sym.substring(index+1);
        index = sym.indexOf("(");
        if (index < 0) throw new Exception("Invalid type in Symbol's string");
        String name = sym.substring(0, index);
        // to cut an opening bracket
        sym = sym.substring(index+1).trim();
        index = sym.indexOf(")");
        if (index < 0) throw new Exception("Invalid type in Symbol's string");
        value.group=group; value.name=name; value.type=ANY; value.Const=false;
        String type = sym.substring(0, index);
        for(int i=0;i < TYPE.length;i++) if ( TYPE[i].equals(type) ) {value.type = i;break;}
        // to cut an closing bracket
        sym = sym.substring(index+1).trim();
        index = sym.indexOf("const");
        if ( index >= 0 )
        {
            index += 5; sym = sym.substring(index+1);
            value.Const = Boolean.valueOf(sym).booleanValue();
        }
        return value;
    }
    /**
    <producer>
    to make string Const
    */
    public static final Symbol newConst(String val)
    {
        Symbol value = new Symbol();
        value.Const=true; value.group=USER;
        value.type=STRING; value.name=val;
        return value;
    }
    /**
    <producer>
    to make numeric Const
    */
    public static final Symbol newConst(Number val)
    {
        Symbol value = new Symbol();
        value.Const=true; value.group=USER;
        value.type=NUMBER; value.name=val.toString();
        return value;
    }
    /**
    <producer>
    to make local Symbol
    */
    public static final Symbol newLocal(String name, int type)
    {
        Symbol value = new Symbol();
        value.Const=false; value.group=USER;
        value.type=type; value.name=name;
        return value;
    }
    /**
    <producer>
    to make database Symbol
    */
    public static final Symbol newDatabase(String name, int type)
    {
        Symbol value = new Symbol();
        value.Const=false; value.group=DATABASE;
        value.type=type; value.name=name;
        return value;
    }
    /**
    <producer>
    to make database column's Symbol
    */
    public static final Symbol newDbColumn(String name, int type)
    {
        Symbol value = new Symbol();
        value.Const=false; value.group=DBCOLUMN;
        value.type=type; value.name=name;
        return value;
    }
    /**
    <producer>
    to make system Symbol
    */
    public static final Symbol newSystem(String name, int type)
    {
        Symbol value = new Symbol();
        value.Const=false; value.group=SYSTEM;
        value.type=type; value.name=name;
        return value;
    }
}
