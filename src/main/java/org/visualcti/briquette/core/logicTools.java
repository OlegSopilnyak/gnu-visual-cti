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

import java.io.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br>
 * Class for calculate the LIKE expression</p>
 * <p>Copyright: Copyright (c) 2002 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class logicTools
{
  /**
   * <like>
   * Class for calculate the SQL's LIKE clause
   * */
  public final static class sqlLike extends Like
  {
    public sqlLike(String pattern){super(pattern,"\\");}
    protected final char anyOneSymbol(){return '_';}
    protected final char anySymbolsSequence(){return '%';}
    protected final char getSymbolByIndex(String string,int index) {
      return string.charAt(index);
    }
  }
  /**
   * <like>
   * Class for calculate the like for File's names
   * */
  public final static class fileLike extends Like
  {
    public fileLike(String pattern){super(pattern,null);}
    protected final char anyOneSymbol(){return '?';}
    protected final char anySymbolsSequence(){return '*';}
    protected final char getSymbolByIndex(String string,int index){
      return Character.toLowerCase(string.charAt(index));
    }
  }
/**
 * <inner>
 * The class for calculate the LIKE exression
 * */
private abstract static class Like
{
  protected abstract char getSymbolByIndex(String string,int index);
  protected abstract char anyOneSymbol();
  protected abstract char anySymbolsSequence();
  private final String pattern;
  private final char ignoreSymbol;
  /**
   * <constructor>
   * To make the Like's calculator for the pattern object for
   * @param pattern the pattern
   * @param ignore the symbol to ignore or null if nothing to ignore
   */
  Like(String pattern,String ignore){
    this.pattern=pattern;
    if ( ignore == null)
      this.ignoreSymbol=0;
    else
      this.ignoreSymbol=ignore.charAt(0);
  }
  public final boolean isLike(String value)
  {
    return this.isLikePattern(value,0,this.pattern,0);
  }
  /**
   * <calculator>
   * To calculate the LIKE expression
   * */
  private final boolean isLikePattern
                (
                String valueSequence,
                int valueIndex,
                String patternSequence,
                int patternIndex
                )
  {
      // loop, while pattern's symbol is exists
      while (patternIndex < patternSequence.length())
      {
          // to get next symbol from the pattern
          char patternSymbol = this.getSymbolByIndex(patternSequence, patternIndex);
          patternIndex++;
          // if in pattern any symbols sequence Symbol (% | *)
          if ( patternSymbol == this.anySymbolsSequence() )
          {
              // if no more symbols on the value or on the pattern
              if
                (
                valueIndex >= valueSequence.length() ||
                patternIndex >= patternSequence.length()
                ) return true;
              // to compare the value's sequence with rest of the pattern
              while (valueIndex < valueSequence.length())
              {
                  if
                    (
                    this.isLikePattern
                            (
                            valueSequence,
                            valueIndex,
                            patternSequence,
                            patternIndex
                            )
                      ) return true;
                    valueIndex++;
              }
              return false;
          }else
          // if in pattern any one symbol Symbol (_ | ?)
          if ( patternSymbol == this.anyOneSymbol() )
          {
              // if no more symbols in the value's sequence
              if (valueIndex >= valueSequence.length()) return false;
              valueIndex++;
          }else
          // if in value's sequence the Symbol to ignore (\)
          if (patternSymbol == this.ignoreSymbol)
          {
              // no symbols in the pattern's sequence
              if (patternIndex >= patternSequence.length()) return false;
              // to get next pattern's Symbol
              patternSymbol = getSymbolByIndex(patternSequence, patternIndex);
              patternIndex++;
          } else
          {
              // no more symbols in value's sequence
              if (valueIndex >= valueSequence.length()) return false;
              char valueSymbol = getSymbolByIndex(valueSequence, valueIndex);
              valueIndex++;
              //value's symbol MUST be equals the pattern's symbol
              if (valueSymbol != patternSymbol) return false;
          }
      }
      // the value Like pattern
      return valueIndex >= valueSequence.length();
  }
}
}
