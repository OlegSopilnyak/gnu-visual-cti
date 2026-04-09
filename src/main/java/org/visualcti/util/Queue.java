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

/** Object queue implemention */
public final class Queue {
 /** default queue size */
 private static final int _default_capacity = 100;
 private int _capacity;/* queue size */
 private int _first;/* point to first element */
 private int _last;/* point to last element */
 private volatile int _n_items;/* number of elements in queue */
 private Object _contents[];/* container */
    /** constructor by queue size */
    public Queue(int capacity)
    {
        _capacity = capacity;
        _first = _last = _n_items = 0;
        _contents = new Object[_capacity];
    }
    /** default constructor */
    public Queue(){this(_default_capacity);}
    /**
    to push element to queue tail
    */
    final public synchronized void push(Object element){
        this.add(element); this.notify();
    }
/** flag for Object state */
private volatile boolean wait = false;
    /**
    is Queue wait for any object
    */
    public final boolean isWait(){return this.wait;}
    /**
    to pop element from queue head
    */
    final public synchronized Object pop(){
        while ( _n_items == 0 ) {
            this.wait = true;
            try{this.wait();}catch(InterruptedException e){}
            this.wait = false;
        }
        return this.remove();
    }
    /** check is queue empty */
    public final boolean empty(){return _n_items == 0;}
    /** number of elements in queue */
    public final int size(){return _n_items;}
        /** to add object to queue tail */
        private final void add(Object o){
            need(_n_items + 1);
            _contents[_last] = o;
            if(++_last == _capacity) _last = 0;
            _n_items++;
        }
        /** to remove first object from queue head */
        private final Object remove() {
            Object o = _contents[_first];
            _contents[_first] = null;
            if(++_first == _capacity) _first = 0;
            _n_items--;
            return o;
        }
        /** remake queue size */
        private void need(int n){
            if(n > this._capacity)
            {
                Queue q = new Queue((int)((double)n * 1.5D));
                for(; !this.empty(); q.add(this.remove()));
                this._capacity = q._capacity;
                this._first = q._first;
                this._last = q._last;
                this._n_items = q._n_items;
                this._contents = q._contents;
            }
        }
}
