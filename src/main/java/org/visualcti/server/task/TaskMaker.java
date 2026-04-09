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
package org.visualcti.server.task;

import java.util.*;
import org.jdom.*;

import org.visualcti.util.Tools;
/**
<producer>
Class for restore the task object from XML
*/
public final class TaskMaker
{
/**
<const>
default TaskProducer
*/
public static final TaskProducer defaultProducer = new defaultTaskProducer();
    /**
    <producer>
    To make and adjust the Task
    */
    public static final Task restore(Element xml)
    {
        if ( !"task".equals(xml.getName()) ) return null;// not task XML
        String taskClass = xml.getAttributeValue("class");
        if (taskClass == null) return null;
        try {
            Task task = producer(xml).create( Class.forName(taskClass) );
            if ( task != null ) task.setXML(xml);
            return task;
        }catch(Exception e){
            e.printStackTrace( Tools.err );
        }
        return null;
    }
/**
<pool>
The pool of producers instances
*/
private static final Map producers = Collections.synchronizedMap(new HashMap());
/** add to producers pool the default producer */
static {
    producers.put(defaultProducer.getClass().getName(), defaultProducer);
}
    /**
    <accessor>
    */
    private final static TaskProducer producer(Element xml)
    {
        String className = xml.getAttributeValue("producer");
        if (className == null) className = defaultProducer.getClass().getName();
        Object prod = producers.get(className);
        if (prod == null)
        {   // try to make new TaskProducer and store instance to the producers pool
            try {
                prod = Class.forName(className).newInstance();
            }catch(Exception e){}
            if (prod != null && prod instanceof TaskProducer)
            {   // put valid producer to pool
                producers.put(className,prod);
                return (TaskProducer)prod;
            }
        }else
        if (prod instanceof TaskProducer) return (TaskProducer)prod;// producer from pool
        else
        {   // to remove invalid object from producers pool
            producers.remove(className);
        }
        return defaultProducer;
    }
}
