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

import java.util.*;

/**
 * <B>Operators</B> Provide static methods for implementaton logic(not, and, or),
 * compare(<, >, =, <=, >=, <>, between, in, like, is) and arithmetic(unary -, *, /, -. +)
 * operators. 
 */
public class Operators {

  /**
   * Implementation of logic operator OR.
   */
  public static void or(Stack data) throws SelectorParseException {
    try {
      Boolean fst = (Boolean)data.pop();
      Boolean scd = (Boolean)data.pop();
      boolean res = fst.booleanValue()||scd.booleanValue();
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of logic operator AND.
   */
  public static void and(Stack data) throws SelectorParseException {
    try {
      Boolean fst = (Boolean)data.pop();
      Boolean scd = (Boolean)data.pop();
      boolean res = fst.booleanValue()&&scd.booleanValue();
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of logic operator NOT.
   */
  public static void not(Stack data) throws SelectorParseException {
    try {
      Boolean fst = (Boolean)data.pop();
      boolean res = !fst.booleanValue();
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator >.
   */
  public static void gr(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      boolean res = fst > scd;
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator <.
   */
  public static void lt(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      boolean res = fst < scd;
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator =.
   */
  public static void eq(Stack data) throws SelectorParseException {
    Object o1 = data.pop();
    Object o2 = data.pop();
    boolean res;
    try {
      double d1 = ((Number)o1).doubleValue();
      double d2 = ((Number)o2).doubleValue();
      res = (d1 == d2);
      data.push(new Boolean(res));
      return;
    } catch (ClassCastException e) {}

    res = o1.equals(o2);
    data.push(new Boolean(res));
  }

  /**
   * Implementation of compare operator >=.
   */
  public static void grOrEq(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      boolean res = fst >= scd;
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator <=.
   */
  public static void ltOrEq(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      boolean res = fst <= scd;
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator <>.
   */
  public static void notEq(Stack data) throws SelectorParseException {
    Object o1 = data.pop();
    Object o2 = data.pop();
    boolean res;
    try {
      double d1 = ((Number)o1).doubleValue();
      double d2 = ((Number)o2).doubleValue();
      res = (d1 != d2);
      data.push(new Boolean(res));
      return;
    } catch (ClassCastException e) {}

    res = !o1.equals(o2);
    data.push(new Boolean(res));
  }

  /**
   * Implementation of compare operator BETWEEN.
   */
  public static void between(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      double thd = ((Number)data.pop()).doubleValue();
      boolean res = (fst >= scd)&&(fst <= thd);
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator IS.
   */
  public static void is(Stack data) throws SelectorParseException {
    try {
      Object fst = data.pop();
      String scd = (String)data.pop();
      boolean res;
      if (scd.equalsIgnoreCase("null")) {
        res = (fst == null);
      } else if(scd.equalsIgnoreCase("not null")) {
        res = (fst != null);
      } else {
        throw new SelectorParseException("not valid argument :" + scd);
      }
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of compare operator IN.
   */
  public static void in(Stack data) throws SelectorParseException {
    try {
      String fst = (String)data.pop();
      String scd = (String)data.pop();
      boolean res = false;
      StringTokenizer st = new StringTokenizer(scd, " ,'");
      while (st.hasMoreTokens()) {
        if (fst.equals(st.nextToken())) {res = true; break;}
      }
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}    
  }

  /**
   * Implementation of compare operator LIKE.
   */
  public static void like(Stack data) throws SelectorParseException {
    try {
      String fst = (String)data.pop();
      String scd = (String)data.pop();
      String thd = (String)data.pop();
      boolean res = false;
      if (fst.equals("")&&scd.equals("")) {
        res = true;
        data.push(new Boolean(res));
        return;
      }
      int sPos = 0;
      out:
      for(int i = 0; i < scd.length(); i++) {
        if((sPos >= fst.length())&&(scd.charAt(i) != '%' )) {res = false; break;}		
        if (thd.equals("no")) {
        } else {
          if ((scd.charAt(i) == thd.charAt(0))){
            if (scd.charAt(i + 1) == fst.charAt(sPos)) {
              res = true;
              if(i == scd.length() - 1) break;
              sPos++;
              i++;               
			} else {
              res = false;
              break;
            }
		  }
        }
        if (scd.charAt(i) == '_' ) {
          if((i == scd.length() - 1)&&(sPos != fst.length() - 1)) {res = false; break;}
          if((i == scd.length() - 1)&&(sPos == fst.length() - 1)) {res = true; break;}
          sPos++;
          res = true;
          continue;
        } else if (scd.charAt(i) == '%') {
          if(i == scd.length() - 1) {res = true; break;}
          if((scd.charAt(i + 1) == '%')||(scd.charAt(i + 1) == '_')) continue;
          while (fst.charAt(sPos) != scd.charAt(i + 1)) {
            if(sPos >= fst.length()) {res = false; break out;}
            sPos++;
            continue;
          }
          i++;
          sPos++;
          res = true;
          continue;
        } else {
          if (scd.charAt(i) == fst.charAt(sPos)) {
            res = true;
            sPos++; 
          } else {
            res = false;
            break;
          }
        }
      }
      data.push(new Boolean(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}        
  }

  /**
   * Implementation of arithmetic operator +.
   */
  public static void plus(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      double res = fst + scd;
      data.push(new Double(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of arithmetic operator -.
   */
  public static void minus(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      double res = fst - scd;
      data.push(new Double(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of arithmetic operator unary -.
   */
  public static void unMinus(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double res = -fst;
      data.push(new Double(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of arithmetic operator /.
   */
  public static void division(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      double res = fst / scd;
      data.push(new Double(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * Implementation of arithmetic operator *.
   */
  public static void multiplication(Stack data) throws SelectorParseException {
    try {
      double fst = ((Number)data.pop()).doubleValue();
      double scd = ((Number)data.pop()).doubleValue();
      double res = fst * scd;
      data.push(new Double(res));
    } catch(ClassCastException e) {throw new SelectorParseException();}
  }

  /**
   * <action>Check string - can this be the idetifier.
   */
  public static boolean isJavaIdentifier(String s) {
    boolean isIdentifier = true;
    if ((s.length() == 0)||(!Character.isJavaIdentifierStart(s.charAt(0)))) {return isIdentifier = false;}
    for(int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {return isIdentifier = false;}  
    }
    return isIdentifier;
  }

  /**
   * <action>Delete start and end quota.
   */
  public static String delQuotas(String s) {
    if ((s.length() > 1)&&(s.charAt(0) == '\'')&&(s.charAt(s.length() - 1) == '\'')) {return s.substring(1,s.length() - 1);}
    return s;
  }

  /**
   * <action>Check string - have it sinlge start or end quota.
   */
  public static boolean hasFaildQuota(String s) {
    if ((s.length() > 1)&&
        (((s.charAt(0) == '\'')&&(s.charAt(s.length() - 1) != '\''))||
	     ((s.charAt(0) != '\'')&&(s.charAt(s.length() - 1) == '\''))))
    return true;
    return false;
  }

  /**
   * Check string - have it start and end quota.
   */
  public static boolean isQuoted(String s) {
    if ((s.length() > 1)&&(s.charAt(0) == '\'')&&(s.charAt(s.length() - 1) == '\'')) return true;
    return false;
  }

}
