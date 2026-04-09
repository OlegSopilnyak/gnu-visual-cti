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
package org.visualcti.util;

import java.util.Stack;
import java.lang.reflect.Method;
/**
 * <B>SelectorParser</B>Provide methods for parsing of selector string and 
 * producing revers Polish record as stack.
 * Be careful with selector string, it's must be valid expression in SQL 92 format.  
 */
public class SelectorParser {
  /**
   *<action> Find in the string expression in the brackets and extract it as separate string
   */
  private static Stack findBracketExpressions(Stack oldStack) throws SelectorParseException {
    boolean hasMoreBrackets = false;
    Stack newStack = new Stack();
    do {
      hasMoreBrackets = false;
      oldStack = findLogicOperators(oldStack);
      oldStack = findCompareOperators(oldStack);
      oldStack = findArithmeticOperators(oldStack);
      while (!oldStack.empty()) {
        String s = (String)oldStack.pop();
        int startBracket = -1, endBracket = -1, bracketCount = 0;
        for(int i = 0; i < s.length(); i++) {
          if (s.charAt(i) == '(') {
            hasMoreBrackets = true;
            ++bracketCount;
            if (startBracket == -1) {startBracket = i;}
          }
          if (s.charAt(i) == ')') {
            --bracketCount;
            if ((bracketCount == 0)&&(startBracket >= 0)&&(endBracket == -1)) {endBracket = i;}
          }
        }
        if (bracketCount > 0){throw new SelectorParseException("')' missing");}
        if (bracketCount < 0){throw new SelectorParseException("'(' missing");}
        if ((startBracket >= 0)&&(endBracket > 0)) {
          if (startBracket > 0) {newStack.push(s.substring(0, startBracket));}
          newStack.push(s.substring(startBracket + 1, endBracket));
          if (endBracket < s.length() - 1) {newStack.push(s.substring(endBracket + 1));}
        } else {
          newStack.push(s);
        }
      }
      while(!newStack.empty()) {
        String temp = (String)newStack.pop();
	    if (!(temp.trim()).equals("")) {oldStack.push(temp.trim());}
      }     
    }while(hasMoreBrackets);
    return oldStack;
  }

  /**
   *<action> Find in the string expression with logic operators(not, and, or - in precedence order)
   * and extract operators and their parameters as separate strings in revers Polish record
   */
  private static Stack findLogicOperators(Stack oldStack) {
    boolean hasMoreOperators = false;
    Stack newStack = new Stack();
    do {
      hasMoreOperators = false;
      while (!oldStack.empty()) {
        String s = (String)oldStack.pop();
        int bracketCount = 0;
        out:
        while (true) {
          for(int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == '(') {
              ++bracketCount;
            }
            if (s.charAt(i) == ')') {
              --bracketCount;
            }
            if(i > s.length() - 4) continue;
            if ((s.substring(i - 1, i + 3).equalsIgnoreCase(")or(")||
                 s.substring(i - 1, i + 3).equalsIgnoreCase(" or(")||
                 s.substring(i - 1, i + 3).equalsIgnoreCase(")or ")||
                 s.substring(i - 1, i + 3).equalsIgnoreCase(" or "))&&
                (bracketCount == 0)) {

              hasMoreOperators = true;
              newStack.push(s.substring(i, i + 2));
              newStack.push(s.substring(0, i));
              newStack.push(s.substring(i + 2));
              break out;
            }
          }
          for(int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == '(') {
              ++bracketCount;
            }
            if (s.charAt(i) == ')') {
              --bracketCount;
            }
            if(i > s.length() - 4) continue;
            if ((s.substring(i - 1, i + 4).equalsIgnoreCase(")and(")||
                 s.substring(i - 1, i + 4).equalsIgnoreCase(" and(")||
                 s.substring(i - 1, i + 4).equalsIgnoreCase(")and ")||
                 s.substring(i - 1, i + 4).equalsIgnoreCase(" and "))&&
                (bracketCount == 0)&&(!isBetween(s, i))) {

              hasMoreOperators = true;
              newStack.push(s.substring(i, i + 3));
              newStack.push(s.substring(0, i));
              newStack.push(s.substring(i + 3));
              break out;
            }
          } 
          for(int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == '(') {
              ++bracketCount;
            }
            if (s.charAt(i) == ')') {
              --bracketCount;
            }
            if(i > s.length() - 4) continue;
            if ((s.substring(i - 1, i + 4).equalsIgnoreCase(" not(")||
                 s.substring(i - 1, i + 4).equalsIgnoreCase(" not "))&&
                 (bracketCount == 0)&&(!(s.substring(i - 1, i + 8)).equalsIgnoreCase(" not null"))) {

              hasMoreOperators = true;
              newStack.push(s.substring(i, i + 3));
              newStack.push(s.substring(0, i) + s.substring(i + 3));
              break out;
            }
          }
          if ((s.startsWith("not ")||s.startsWith("not("))&&(!s.startsWith("not null"))) {
            newStack.push(s.substring(0, 3));
            newStack.push(s.substring(3));
            break out;
          }
          newStack.push(s);
          break out;
        }
        bracketCount = 0;
      }
      while(!newStack.empty()) {
        String temp = (String)newStack.pop();
	    if (!temp.equals("")) {oldStack.push(temp);}
      }
    } while(hasMoreOperators);
    return oldStack;
  }

  /**
   *<action> Check is substring 'and' the part of compare operator between.
   */
  private static boolean isBetween(String s, int i) {
    int j = 0;
    String op = "between";
    while (j <= s.length()) {
      int pos = -1;
      if ((pos = s.lastIndexOf(op, j)) != -1) {
        if(i == s.toLowerCase().indexOf("and", pos)) return true;
      }
      j += op.length();
    }
    return false;
  }

  /**
   *<action> Find in the string expression with compare operators(<, >, =, <=, >=, <>, between, in, like, is)
   * and extract operators and their parameters as separate strings in revers Polish record.
   */  
  private static Stack findCompareOperators(Stack oldStack) {
    boolean hasMoreOperators = false;
    Stack newStack = new Stack();
    do {
      while (!oldStack.empty()) {
        hasMoreOperators = false;
        boolean isModify = false;
        String s = (String)oldStack.pop();
        int bracketCount = 0;
        for(int i = s.length() - 1; i > 0; i--) {
          if (s.charAt(i) == '(') {
            ++bracketCount;
          }
          if (s.charAt(i) == ')') {
            --bracketCount;
          }
          if(i > s.length() - 3) continue;
          if ((s.substring(i, i + 1).equalsIgnoreCase("<")||
               s.substring(i, i + 1).equalsIgnoreCase(">")||
               s.substring(i, i + 1).equalsIgnoreCase("="))&&
              (bracketCount == 0)) {

            hasMoreOperators = true;
            isModify = true;
            newStack.push(s.substring(i, i + 1));
            newStack.push(s.substring(0, i));
            newStack.push(s.substring(i + 1));
            break;
          }
          if ((s.substring(i, i + 2).equalsIgnoreCase("<=")||
               s.substring(i, i + 2).equalsIgnoreCase(">=")||
               s.substring(i, i + 2).equalsIgnoreCase("<>"))&&
               (bracketCount == 0)) {

            hasMoreOperators = true;
            isModify = true;
            newStack.push(s.substring(i, i + 2));
            newStack.push(s.substring(0, i));
            newStack.push(s.substring(i + 2));
            break;
          }
          if(i > s.length() - 4) continue;
          if (s.substring(i - 1, i + 3).equalsIgnoreCase(" is ")&&(bracketCount == 0)) {
            hasMoreOperators = true;
            isModify = true;
            newStack.push(s.substring(i, i + 2));
            newStack.push(s.substring(0, i));
            newStack.push(s.substring(i + 2));
            break;
          }
          if(i > s.length() - 6) continue;
          if ((s.substring(i - 1, i + 3).equalsIgnoreCase(" in(")||
               s.substring(i - 1, i + 4).equalsIgnoreCase(" in ("))&&
              (bracketCount == 0)) {

            hasMoreOperators = true;
            isModify = true;
            newStack.push(s.substring(i, i + 2));
            newStack.push(s.substring(0, i));
            newStack.push(s.substring(i + 2));
            break;
          }
          if ((s.substring(i - 1, i + 5).equalsIgnoreCase(" like ")||
               s.substring(i - 1, i + 5).equalsIgnoreCase(" like("))&&
              (bracketCount == 0)) {

            hasMoreOperators = true;
            isModify = true;
            int pos; 
            newStack.push(s.substring(i, i + 4));
            newStack.push(s.substring(0, i));
            if ((pos = s.toLowerCase().indexOf("escape", i)) == -1) {
              newStack.push(s.substring(i + 4));
              newStack.push("'no'");
            } else {
              newStack.push(s.substring(i + 4, pos));
              newStack.push(s.substring(pos + 6));
            }  
            break;
          }
          if(i > s.length() - 10) continue;
          if (s.substring(i - 1, i + 8).equalsIgnoreCase(" between ")&&(bracketCount == 0)) {
            hasMoreOperators = true;
            isModify = true;
            int pos = s.toLowerCase().indexOf(" and ", i);
            newStack.push(s.substring(i, i + 7));
            newStack.push(s.substring(0, i));
            newStack.push(s.substring(i + 7, pos));
            newStack.push(s.substring(pos + 5));
            break;
          }
        }
        if(!isModify) {newStack.push(s);}
      }
      while(!newStack.empty()) {
        String temp = (String)newStack.pop();
	    if (!temp.equals("")) {oldStack.push(temp);}
      }
    } while(hasMoreOperators);
    return oldStack;
  }

  /**
   *<action> Find in the string expression with arithmetic operators(unary + and -, * and /, - and + - in precedence order)
   * and extract operators and their parameters as separate strings in revers Polish record.
   */
  private static Stack findArithmeticOperators(Stack oldStack) {
    boolean hasMoreOperators = false;
    Stack newStack = new Stack();
    do {
      hasMoreOperators = false;
      while (!oldStack.empty()) {
        String s = (String)oldStack.pop();
        int bracketCount = 0;
        out:
        while (true) {
          for(int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == '(') {
              ++bracketCount;
            }
            if (s.charAt(i) == ')') {
              --bracketCount;
            }
            if(i > s.length() - 2) continue;
            if ((s.substring(i, i + 1).equalsIgnoreCase("-")||
                 s.substring(i, i + 1).equalsIgnoreCase("+"))&&
                (bracketCount == 0)&&(!isUnary(s, i))) {

              hasMoreOperators = true;
              newStack.push(s.substring(i, i + 1));
              newStack.push(s.substring(0, i));
              newStack.push(s.substring(i + 1));
              break out;
            }
          }
          for(int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == '(') {
              ++bracketCount;
            }
            if (s.charAt(i) == ')') {
              --bracketCount;
            }
            if(i > s.length() - 2) continue;
            if ((s.substring(i, i + 1).equalsIgnoreCase("/")||
                 s.substring(i, i + 1).equalsIgnoreCase("*"))&&
                (bracketCount == 0)) {

              hasMoreOperators = true;
              newStack.push(s.substring(i, i + 1));
              newStack.push(s.substring(0, i));
              newStack.push(s.substring(i + 1));
              break out;
            }
          }
          for(int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '(') {
              ++bracketCount;
            }
            if (s.charAt(i) == ')') {
              --bracketCount;
            }
            if ((s.substring(i, i + 1).equalsIgnoreCase("-")||
                 s.substring(i, i + 1).equalsIgnoreCase("+"))&&
                (bracketCount == 0)&&(isUnary(s, i))) {

              hasMoreOperators = true;
              if (s.substring(i, i + 1).equalsIgnoreCase("-")) newStack.push("unmin");
              newStack.push(s.substring(i + 1));
              break out;
            }
          }
          newStack.push(s);
          break out;
        }
        bracketCount = 0;
      }
      while(!newStack.empty()) {
        String temp = (String)newStack.pop();
	    if (!temp.equals("")) {oldStack.push(temp);}
      }
    } while(hasMoreOperators);
    return oldStack;
  }

  /**
   *<action> Check is - or + unary operators.
   */  
  private static boolean isUnary(String s , int pos){
    String temp = s.substring(0, pos).trim();
    if (temp.length() > 0) {
      int index = temp.length() - 1;
      if(Character.isJavaIdentifierPart(temp.charAt(index))||temp.charAt(index) == ')') return false;
    } else {
      if(s.length() < 2) return false;
    }
    return true;
  }

  /**
   *<action>Parse selector string and return revers Polish record for expression in the selector as stack
   */ 
  public static Stack parse(String selector) throws SelectorParseException {
    Stack temp = new Stack();
    Stack reversPolishRecord = new Stack();
    Class cl = (new Operators()).getClass();
    Class[] params = {(new Stack()).getClass()};
    temp.push(selector);
    temp = findBracketExpressions(temp);
    while (!temp.empty()) {
      String s = (String)temp.pop();
      if (s.equalsIgnoreCase("or")) {
        try {reversPolishRecord.push(cl.getMethod("or", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("and")) {
        try {reversPolishRecord.push(cl.getMethod("and", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("not")) {
        try {reversPolishRecord.push(cl.getMethod("not", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("<")) {
        try {reversPolishRecord.push(cl.getMethod("lt", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase(">")) {
        try {reversPolishRecord.push(cl.getMethod("gr", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("=")) {
        try {reversPolishRecord.push(cl.getMethod("eq", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("<=")) {
        try {reversPolishRecord.push(cl.getMethod("ltOrEq", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase(">=")) {
        try {reversPolishRecord.push(cl.getMethod("grOrEq", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("<>")) {
        try {reversPolishRecord.push(cl.getMethod("notEq", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("between")) {
        try {reversPolishRecord.push(cl.getMethod("between", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("in")) {
        try {reversPolishRecord.push(cl.getMethod("in", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("is")) {
        try {reversPolishRecord.push(cl.getMethod("is", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("like")) {
        try {reversPolishRecord.push(cl.getMethod("like", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("+")) {
        try {reversPolishRecord.push(cl.getMethod("plus", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("-")) {
        try {reversPolishRecord.push(cl.getMethod("minus", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("/")) {
        try {reversPolishRecord.push(cl.getMethod("division", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("*")) {
        try {reversPolishRecord.push(cl.getMethod("multiplication", params));} catch(NoSuchMethodException e) {}
      } else if(s.equalsIgnoreCase("unmin")) {
        try {reversPolishRecord.push(cl.getMethod("unMinus", params));} catch(NoSuchMethodException e) {}
      } else {
        reversPolishRecord.push(s);
      }	
    }
    return reversPolishRecord; 
  }

}
